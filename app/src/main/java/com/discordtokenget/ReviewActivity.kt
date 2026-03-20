package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val rdbHttp = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

private val RDB_API = "https://manti.vendicated.dev/api/reviewdb"

private object RColors {
    val Bg        = Color(0xFF1E1F22)
    val Surface   = Color(0xFF2B2D31)
    val Card      = Color(0xFF313338)
    val Primary   = Color(0xFF5865F2)
    val Success   = Color(0xFF23A55A)
    val Warning   = Color(0xFFFAA61A)
    val Error     = Color(0xFFED4245)
    val White     = Color(0xFFF2F3F5)
    val SubText   = Color(0xFFB5BAC1)
    val Muted     = Color(0xFF80848E)
    val Divider   = Color(0xFF3F4147)
    val Gold      = Color(0xFFFFD700)
}

data class RDBReview(
    val id: Int,
    val comment: String,
    val senderUsername: String,
    val senderDiscordId: String,
    val senderAvatar: String?,
    val timestamp: Long,
    val star: Int,
    val isSystem: Boolean
)

data class RDBBadge(
    val name: String,
    val icon: String,
    val description: String
)

class ReviewActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_TOKEN    = "extra_token"
        private const val EXTRA_USER_ID  = "extra_user_id"
        private const val EXTRA_USERNAME = "extra_username"
        fun start(ctx: Context, token: String, userId: String, username: String) =
            ctx.startActivity(
                Intent(ctx, ReviewActivity::class.java)
                    .putExtra(EXTRA_TOKEN, token)
                    .putExtra(EXTRA_USER_ID, userId)
                    .putExtra(EXTRA_USERNAME, username)
            )
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val token    = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        val userId   = intent.getStringExtra(EXTRA_USER_ID) ?: run { finish(); return }
        val username = intent.getStringExtra(EXTRA_USERNAME) ?: "User"
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = RColors.Primary, background = RColors.Bg, surface = RColors.Surface)) {
                Surface(Modifier.fillMaxSize(), color = RColors.Bg) {
                    ReviewScreen(token = token, userId = userId, username = username, onBack = { finish() })
                }
            }
        }
    }
}

private suspend fun fetchReviews(userId: String): List<RDBReview> = withContext(Dispatchers.IO) {
    try {
        val resp = rdbHttp.newCall(
            Request.Builder()
                .url("$RDB_API/users/$userId/reviews?flags=0")
                .header("Accept", "application/json")
                .build()
        ).execute()
        if (!resp.isSuccessful) return@withContext emptyList()
        val body = resp.body?.string() ?: return@withContext emptyList()
        val json = try { JSONObject(body) } catch (_: Exception) { return@withContext emptyList() }
        val arr = json.optJSONArray("reviews") ?: return@withContext emptyList()
        val list = mutableListOf<RDBReview>()
        for (i in 0 until arr.length()) {
            val r = arr.getJSONObject(i)
            val sender = r.optJSONObject("sender") ?: continue
            val discordId = sender.optString("discordID", "").takeIf { it.isNotEmpty() } ?: continue
            val profilePhoto = sender.optString("profilePhoto", "").takeIf { it.isNotEmpty() && it != "null" }
            val rawAvatar = if (profilePhoto != null && profilePhoto.contains(".png?size=128")) {
                profilePhoto.substringBefore(".png?size=128") + ".webp?size=128"
            } else profilePhoto
            list.add(RDBReview(
                id             = r.optInt("id", 0),
                comment        = r.optString("comment", ""),
                senderUsername = sender.optString("username", "Unknown"),
                senderDiscordId= discordId,
                senderAvatar   = rawAvatar,
                timestamp      = r.optLong("timestamp", 0L),
                star           = r.optInt("star", 0),
                isSystem       = r.optInt("type", 0) == 3
            ))
        }
        list
    } catch (e: Exception) { emptyList() }
}

private suspend fun fetchReviewCount(userId: String): Int = withContext(Dispatchers.IO) {
    try {
        val resp = rdbHttp.newCall(
            Request.Builder()
                .url("$RDB_API/users/$userId/reviews?flags=0")
                .header("Accept", "application/json")
                .build()
        ).execute()
        if (!resp.isSuccessful) return@withContext 0
        val body = resp.body?.string() ?: return@withContext 0
        JSONObject(body).optInt("reviewCount", 0)
    } catch (_: Exception) { 0 }
}

private fun formatTimestamp(ts: Long): String = try {
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ts * 1000))
} catch (_: Exception) { "" }

private fun avatarUrl(discordId: String): String {
    val idx = (discordId.toLongOrNull()?.ushr(22))?.rem(6) ?: 0
    return "https://cdn.discordapp.com/embed/avatars/$idx.png"
}

@Composable
private fun ReviewScreen(token: String, userId: String, username: String, onBack: () -> Unit) {
    val ctx   = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading      by remember { mutableStateOf(true) }
    var reviews      by remember { mutableStateOf<List<RDBReview>>(emptyList()) }
    var reviewCount  by remember { mutableStateOf(0) }
    var error        by remember { mutableStateOf<String?>(null) }
    var refreshTick  by remember { mutableStateOf(0) }

    val gifLoader = remember(ctx) {
        ImageLoader.Builder(ctx).components {
            if (android.os.Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()
    }

    LaunchedEffect(refreshTick) {
        loading = true; error = null
        try {
            reviews     = fetchReviews(userId)
            reviewCount = fetchReviewCount(userId)
        } catch (e: Exception) { error = e.message }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(RColors.Bg)) {
        Box(
            Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF23243A), RColors.Bg)))
                .padding(top = 40.dp, bottom = 0.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp).background(RColors.Surface, RoundedCornerShape(12.dp))
                    ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = RColors.White, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Reviews", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = RColors.White)
                        Text("@$username", fontSize = 13.sp, color = RColors.Muted)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { refreshTick++ }, modifier = Modifier.size(40.dp).background(RColors.Surface, RoundedCornerShape(12.dp))) {
                        Icon(Icons.Outlined.Refresh, null, tint = RColors.SubText, modifier = Modifier.size(18.dp))
                    }
                }
                if (!loading && reviews.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .background(RColors.Surface.copy(0.6f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Reviews", fontSize = 11.sp, color = RColors.Muted)
                            Text("$reviewCount", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RColors.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { i ->
                                val avg = if (reviews.isNotEmpty()) reviews.map { it.star }.average() else 0.0
                                Icon(
                                    if (i < avg.toInt()) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    null,
                                    tint = if (i < avg.toInt()) RColors.Gold else RColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        when {
            loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                val inf = rememberInfiniteTransition(label = "ld")
                val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = androidx.compose.animation.core.LinearEasing)), label = "lr")
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(color = RColors.Primary, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                    Text("Loading reviews...", color = RColors.Muted, fontSize = 14.sp)
                }
            }

            error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Outlined.ErrorOutline, null, tint = RColors.Error, modifier = Modifier.size(48.dp))
                    Text("Failed to load reviews", color = RColors.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                    Text(error ?: "", color = RColors.Muted, fontSize = 11.sp)
                    Button(onClick = { refreshTick++ }, colors = ButtonDefaults.buttonColors(containerColor = RColors.Primary), shape = RoundedCornerShape(12.dp)) {
                        Text("Try Again", fontWeight = FontWeight.Bold)
                    }
                }
            }

            reviews.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(40.dp)) {
                    Icon(Icons.Outlined.RateReview, null, tint = RColors.Muted, modifier = Modifier.size(56.dp))
                    Text("No reviews yet", color = RColors.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Be the first to review @$username!", color = RColors.Muted, fontSize = 13.sp)
                }
            }

            else -> LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(Modifier.height(6.dp)) }
                items(reviews, key = { it.id }) { review ->
                    ReviewCard(review = review, gifLoader = gifLoader)
                }
                item { Spacer(Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: RDBReview, gifLoader: ImageLoader) {
    val ctx = LocalContext.current
    val isSystem = review.isSystem
    val accentColor = if (isSystem) RColors.Primary else RColors.Surface

    Card(
        colors = CardDefaults.cardColors(containerColor = if (isSystem) RColors.Primary.copy(0.08f) else RColors.Card),
        shape  = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, if (isSystem) RColors.Primary.copy(0.2f) else RColors.Divider.copy(0.4f), RoundedCornerShape(14.dp))
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp)) {
                    val avatarUrl = review.senderAvatar ?: avatarUrl(review.senderDiscordId)
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(avatarUrl).crossfade(true).build(),
                        contentDescription = null,
                        imageLoader = gifLoader,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (isSystem) {
                        Box(
                            Modifier.size(14.dp).align(Alignment.BottomEnd)
                                .background(RColors.Primary, CircleShape)
                                .border(1.5.dp, RColors.Card, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.Verified, null, tint = Color.White, modifier = Modifier.size(8.dp))
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(review.senderUsername, fontSize = 14.sp, color = RColors.White, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (isSystem) {
                            Box(
                                Modifier.background(RColors.Primary.copy(0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) { Text("SYSTEM", fontSize = 8.sp, color = RColors.Primary, fontWeight = FontWeight.ExtraBold) }
                        }
                    }
                    if (review.timestamp > 0) {
                        Text(formatTimestamp(review.timestamp), fontSize = 10.sp, color = RColors.Muted)
                    }
                }
                if (review.star > 0) {
                    Row {
                        repeat(5) { i ->
                            Icon(
                                if (i < review.star) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                null,
                                tint = if (i < review.star) RColors.Gold else RColors.Muted.copy(0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = RColors.Divider.copy(0.4f))
                Spacer(Modifier.height(10.dp))
                Text(
                    review.comment,
                    fontSize = 14.sp,
                    color = if (isSystem) RColors.SubText else RColors.White.copy(0.9f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
