package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val questHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private val QC = object {
    val Background    = Color(0xFF1E1F22)
    val Surface       = Color(0xFF2B2D31)
    val SurfaceVar    = Color(0xFF313338)
    val Primary       = Color(0xFF5865F2)
    val Success       = Color(0xFF23A55A)
    val Warning       = Color(0xFFFAA61A)
    val Error         = Color(0xFFED4245)
    val TextPrimary   = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFB5BAC1)
    val TextMuted     = Color(0xFF80848E)
    val Divider       = Color(0xFF3F4147)
    val Orbs          = Color(0xFF7C43E0)
    val Decor         = Color(0xFF57F287)
    val Nitro         = Color(0xFF5865F2)
}

private val SUPER_PROPERTIES = "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ3LjApIEdlY2tvLzE0Ny4wIEZpcmVmb3gvMTQ3LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDcuMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tL2xvZ2luP3JlZGlyZWN0X3RvPSUyRnF1ZXN0LWhvbWUiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiJkaXNjb3JkLmNvbSIsInJlbGVhc2VfY2hhbm5lbCI6InN0YWJsZSIsImNsaWVudF9idWlsZF9udW1iZXIiOjQ5OTEyMywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0="
private val DISCORD_HEADERS = mapOf(
    "Content-Type" to "application/json",
    "User-Agent" to "Mozilla/5.0 (Android 12; Mobile; rv:147.0) Gecko/147.0 Firefox/147.0",
    "X-Super-Properties" to SUPER_PROPERTIES,
    "X-Discord-Locale" to "pt-BR",
    "X-Discord-Timezone" to "America/Sao_Paulo",
    "X-Debug-Options" to "bugReporterEnabled",
    "Referer" to "https://discord.com/quest-home"
)

data class QuestItem(
    val id: String,
    val name: String,
    val description: String,
    val reward: String,
    val rewardType: String,
    val expiresAt: String,
    val expiresMs: Long,
    val taskName: String,
    val secondsNeeded: Long,
    val secondsDone: Long,
    val applicationId: String?,
    val applicationName: String?,
    val bannerUrl: String?,
    val thumbnailUrl: String?
)

enum class QuestStatus { IDLE, RUNNING, DONE, ERROR }

data class QuestState(
    val quest: QuestItem,
    var status: QuestStatus = QuestStatus.IDLE,
    var progress: Long = 0L,
    var log: String = ""
)

class QuestActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        fun start(context: Context, token: String) {
            context.startActivity(Intent(context, QuestActivity::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = QC.Primary,
                background = QC.Background,
                surface = QC.Surface
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = QC.Background) {
                    QuestScreen(token = token, onBack = { finish() })
                }
            }
        }
    }

    @Composable
    fun QuestScreen(token: String, onBack: () -> Unit) {
        var loading by remember { mutableStateOf(true) }
        var fetchError by remember { mutableStateOf<String?>(null) }
        val questStates = remember { mutableStateListOf<QuestState>() }
        var refreshKey by remember { mutableStateOf(0) }

        LaunchedEffect(refreshKey) {
            loading = true
            fetchError = null
            questStates.clear()
            try {
                val quests = fetchQuests(token)
                questStates.addAll(quests.map { QuestState(it) })
            } catch (e: Exception) {
                fetchError = e.message ?: "Unknown error"
            }
            loading = false
        }

        Column(Modifier.fillMaxSize().background(QC.Background)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QC.Surface)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = QC.TextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Outlined.EmojiEvents, null, tint = QC.Primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Quest Completer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = QC.TextPrimary)
                        Text("Auto-complete Discord quests", fontSize = 11.sp, color = QC.TextMuted)
                    }
                }
                IconButton(onClick = { refreshKey++ }, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = QC.Primary, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = QC.Divider)

            when {
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CircularProgressIndicator(color = QC.Primary, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                            Text("Loading quests...", color = QC.TextMuted, fontSize = 14.sp)
                        }
                    }
                }
                fetchError != null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(64.dp).background(QC.Error.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.ErrorOutline, null, tint = QC.Error, modifier = Modifier.size(32.dp))
                            }
                            Text("Failed to load quests", color = QC.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(fetchError ?: "", color = QC.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Button(onClick = { refreshKey++ }, colors = ButtonDefaults.buttonColors(containerColor = QC.Primary), shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Try Again", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                questStates.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(64.dp).background(QC.Success.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.TaskAlt, null, tint = QC.Success, modifier = Modifier.size(32.dp))
                            }
                            Text("All caught up!", color = QC.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("No incomplete quests found.", color = QC.TextMuted, fontSize = 13.sp)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item { Spacer(Modifier.height(4.dp)) }
                        items(questStates, key = { it.quest.id }) { state ->
                            QuestCard(
                                state = state,
                                token = token,
                                onStateChange = { updated ->
                                    val i = questStates.indexOfFirst { it.quest.id == state.quest.id }
                                    if (i >= 0) questStates[i] = updated
                                }
                            )
                        }
                        item { Spacer(Modifier.height(20.dp)) }
                    }
                }
            }
        }
    }

    @Composable
    fun QuestCard(state: QuestState, token: String, onStateChange: (QuestState) -> Unit) {
        val ctx = LocalContext.current
        val rewardColor = when (state.quest.rewardType) {
            "orbs"  -> QC.Orbs
            "decor" -> QC.Decor
            "nitro" -> QC.Nitro
            else    -> QC.Primary
        }

        val shimmerT = rememberInfiniteTransition(label = "sq")
        val shimmerAlpha by shimmerT.animateFloat(0.4f, 1f, infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse), label = "sqa")

        val gifLoader = remember(ctx) {
            ImageLoader.Builder(ctx).components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }.build()
        }

        val timeLeft = remember(state.quest.expiresMs) {
            val diff = state.quest.expiresMs - System.currentTimeMillis()
            val hours = diff / 3600000L
            val days = hours / 24
            when {
                days > 1 -> "${days}d left"
                hours > 0 -> "${hours}h left"
                else -> "Expiring soon"
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = QC.Surface),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, rewardColor.copy(0.18f), RoundedCornerShape(18.dp))
        ) {
            Column(Modifier.fillMaxWidth()) {
                if (state.quest.bannerUrl != null) {
                    Box(
                        Modifier.fillMaxWidth().height(110.dp)
                            .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(state.quest.bannerUrl).crossfade(true).build(),
                            contentDescription = "Quest Banner",
                            imageLoader = gifLoader,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent, QC.Surface))
                            )
                        )
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp)
                                .background(rewardColor.copy(0.85f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    when (state.quest.rewardType) {
                                        "orbs"  -> Icons.Outlined.AutoAwesome
                                        "decor" -> Icons.Outlined.CardGiftcard
                                        else    -> Icons.Outlined.EmojiEvents
                                    },
                                    null, tint = Color.White, modifier = Modifier.size(11.dp)
                                )
                                Text(state.quest.rewardType.uppercase(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().height(70.dp)
                            .background(
                                Brush.horizontalGradient(listOf(rewardColor.copy(0.25f), rewardColor.copy(0.08f))),
                                RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).background(rewardColor.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(
                                    when (state.quest.rewardType) {
                                        "orbs"  -> Icons.Outlined.AutoAwesome
                                        "decor" -> Icons.Outlined.CardGiftcard
                                        else    -> Icons.Outlined.EmojiEvents
                                    },
                                    null, tint = rewardColor, modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(state.quest.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QC.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(state.quest.reward, fontSize = 11.sp, color = rewardColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp)
                                .background(rewardColor.copy(0.85f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(
                                    when (state.quest.rewardType) {
                                        "orbs"  -> Icons.Outlined.AutoAwesome
                                        "decor" -> Icons.Outlined.CardGiftcard
                                        else    -> Icons.Outlined.EmojiEvents
                                    },
                                    null, tint = Color.White, modifier = Modifier.size(11.dp)
                                )
                                Text(state.quest.rewardType.uppercase(), fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.quest.bannerUrl != null) {
                        Text(state.quest.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QC.TextPrimary)
                        if (state.quest.reward.isNotBlank()) {
                            Text(state.quest.reward, fontSize = 12.sp, color = rewardColor, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (state.quest.description.isNotBlank()) {
                        Text(state.quest.description, fontSize = 12.sp, color = QC.TextSecondary, lineHeight = 17.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QChip(
                            icon = when (state.quest.taskName) {
                                "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> Icons.Outlined.Videocam
                                "PLAY_ACTIVITY", "PLAY_ON_DESKTOP"     -> Icons.Outlined.SportsEsports
                                else                                   -> Icons.Outlined.Schedule
                            },
                            text = state.quest.taskName.replace("_", " "),
                            color = QC.TextMuted
                        )
                        QChip(Icons.Outlined.HourglassEmpty, timeLeft, QC.Warning)
                        if (state.quest.applicationName != null) {
                            QChip(Icons.Outlined.SportsEsports, state.quest.applicationName, QC.Primary)
                        }
                    }

                    if (state.quest.secondsNeeded > 0 && (state.status == QuestStatus.RUNNING || state.status == QuestStatus.DONE || state.quest.secondsDone > 0)) {
                        val prog = (state.progress.coerceAtLeast(state.quest.secondsDone).toFloat() / state.quest.secondsNeeded.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progress", fontSize = 10.sp, color = QC.TextMuted, fontWeight = FontWeight.SemiBold)
                                Text("${(prog * 100).toInt()}%", fontSize = 10.sp, color = rewardColor, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { prog },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = rewardColor,
                                trackColor = QC.SurfaceVar,
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                "${state.progress.coerceAtLeast(state.quest.secondsDone)}s / ${state.quest.secondsNeeded}s",
                                fontSize = 9.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (state.log.isNotBlank()) {
                        Box(
                            Modifier.fillMaxWidth()
                                .background(when (state.status) {
                                    QuestStatus.ERROR -> QC.Error.copy(0.08f)
                                    QuestStatus.DONE  -> QC.Success.copy(0.08f)
                                    else              -> QC.SurfaceVar
                                }, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        ) {
                            Text(state.log, fontSize = 11.sp, color = when (state.status) {
                                QuestStatus.ERROR -> QC.Error
                                QuestStatus.DONE  -> QC.Success
                                else              -> QC.TextMuted
                            }, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
                        }
                    }

                    when (state.status) {
                        QuestStatus.IDLE -> {
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        completeQuest(token, state, onStateChange)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = rewardColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Complete Quest", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            }
                        }
                        QuestStatus.RUNNING -> {
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(rewardColor.copy(0.10f), RoundedCornerShape(12.dp))
                                    .border(1.dp, rewardColor.copy(0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = rewardColor.copy(shimmerAlpha), strokeWidth = 2.5.dp)
                                Spacer(Modifier.width(10.dp))
                                Text("Running...", fontWeight = FontWeight.Bold, color = rewardColor, fontSize = 14.sp)
                            }
                        }
                        QuestStatus.DONE -> {
                            Row(
                                Modifier.fillMaxWidth()
                                    .background(QC.Success.copy(0.10f), RoundedCornerShape(12.dp))
                                    .border(1.dp, QC.Success.copy(0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Outlined.CheckCircle, null, tint = QC.Success, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Completed!", fontWeight = FontWeight.Bold, color = QC.Success, fontSize = 14.sp)
                            }
                        }
                        QuestStatus.ERROR -> {
                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        completeQuest(token, state.copy(status = QuestStatus.IDLE, progress = state.quest.secondsDone, log = ""), onStateChange)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = QC.Error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Retry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
        Row(
            Modifier
                .background(color.copy(0.09f), RoundedCornerShape(20.dp))
                .border(1.dp, color.copy(0.22f), RoundedCornerShape(20.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
            Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    private fun buildRequest(url: String, token: String): Request.Builder {
        return Request.Builder().url(url).apply {
            header("Authorization", token)
            DISCORD_HEADERS.forEach { (k, v) -> header(k, v) }
        }
    }

    private suspend fun fetchQuests(token: String): List<QuestItem> = withContext(Dispatchers.IO) {
        val heartbeatSessionId = java.util.UUID.randomUUID().toString()
        val adSessionId = java.util.UUID.randomUUID().toString()

        val collected = mutableListOf<JSONObject>()
        var placement = 1
        val seenIds = mutableSetOf<String>()

        while (placement <= 50) {
            val url = "https://discord.com/api/v9/quests/decision?placement=$placement" +
                "&client_heartbeat_session_id=$heartbeatSessionId" +
                "&client_ad_session_id=$adSessionId"

            val resp = questHttpClient.newCall(buildRequest(url, token).build()).execute()
            val body = resp.body?.string() ?: break

            if (!resp.isSuccessful) {
                if (placement == 1) {
                    val errorMsg = try {
                        JSONObject(body).optString("message", "HTTP ${resp.code}")
                    } catch (_: Exception) { "HTTP ${resp.code}" }
                    throw Exception(errorMsg)
                }
                break
            }

            val obj = try { JSONObject(body) } catch (_: Exception) { break }
            val questObj = obj.optJSONObject("quest") ?: break
            val questId = questObj.optString("id", "")
            if (questId.isEmpty() || seenIds.contains(questId)) break
            seenIds.add(questId)

            val userStatusResp = questHttpClient.newCall(
                buildRequest("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
            ).execute()
            val userStatusBody = userStatusResp.body?.string() ?: ""
            val userStatusObj = try { JSONObject(userStatusBody) } catch (_: Exception) { JSONObject() }

            questObj.put("user_status", userStatusObj)
            collected.add(questObj)
            placement++
        }

        if (collected.isEmpty()) return@withContext emptyList<QuestItem>()

        val arr = JSONArray()
        collected.forEach { arr.put(it) }

        val result = mutableListOf<QuestItem>()
        val now = System.currentTimeMillis()
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }

        for (i in 0 until arr.length()) {
            val q = arr.getJSONObject(i)
            val userStatus = q.optJSONObject("user_status") ?: continue
            if (userStatus.has("completed_at") && !userStatus.isNull("completed_at")) continue

            val config = q.optJSONObject("config") ?: continue
            val expiresAt = config.optString("expires_at", "")
            val expiresMs = try {
                sdf.parse(expiresAt.substringBefore('.'))?.time ?: 0L
            } catch (_: Exception) { 0L }
            if (expiresMs > 0 && expiresMs < now) continue

            val taskConfig = config.optJSONObject("task_config")
                ?: config.optJSONObject("taskConfig")
                ?: config.optJSONObject("taskConfigV2")
                ?: continue
            val tasks = taskConfig.optJSONObject("tasks") ?: continue
            val taskName = listOf(
                "WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO",
                "PLAY_ACTIVITY", "PLAY_ON_DESKTOP", "STREAM_ON_DESKTOP"
            ).firstOrNull { tasks.has(it) } ?: continue
            val taskObj = tasks.optJSONObject(taskName) ?: continue
            val secondsNeeded = taskObj.optLong("target", 0L)
            val secondsDone = userStatus.optJSONObject("progress")
                ?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

            val application = config.optJSONObject("application")
            val appId = application?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
            val appName = application?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }

            val messages = config.optJSONObject("messages")
            val questName = messages?.optString("quest_name")?.takeIf { it.isNotEmpty() }
                ?: messages?.optString("questName")?.takeIf { it.isNotEmpty() }
                ?: appName ?: "Quest"
            val description = messages?.optString("quest_description")?.takeIf { it.isNotEmpty() }
                ?: messages?.optString("description")?.takeIf { it.isNotEmpty() }
                ?: ""
            val rewardStr = messages?.optString("reward_preview")?.takeIf { it.isNotEmpty() }
                ?: messages?.optString("rewardPreview")?.takeIf { it.isNotEmpty() }
                ?: messages?.optString("reward_description")?.takeIf { it.isNotEmpty() }
                ?: messages?.optString("rewardDescription")?.takeIf { it.isNotEmpty() }
                ?: ""

            val rewardType = when {
                rewardStr.contains("orb", ignoreCase = true)        -> "orbs"
                rewardStr.contains("decoration", ignoreCase = true) -> "decor"
                rewardStr.contains("nitro", ignoreCase = true)      -> "nitro"
                else                                                -> "prize"
            }

            val expDisplay = try {
                val d = sdf.parse(expiresAt.substringBefore('.'))
                java.text.SimpleDateFormat("MMM dd", java.util.Locale.US).format(d!!)
            } catch (_: Exception) { "?" }

            val assets = config.optJSONObject("assets")
            val bannerUrl = assets?.optString("logotype")?.takeIf { it.isNotEmpty() && it != "null" }
                ?.let { "https://cdn.discordapp.com/quests/assets/$it" }
                ?: if (appId != null) "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg" else null
            val thumbUrl = assets?.optString("thumbnail")?.takeIf { it.isNotEmpty() && it != "null" }
                ?.let { "https://cdn.discordapp.com/quests/assets/$it" }

            result.add(QuestItem(
                id = q.optString("id"),
                name = questName,
                description = description,
                reward = rewardStr,
                rewardType = rewardType,
                expiresAt = expDisplay,
                expiresMs = expiresMs,
                taskName = taskName,
                secondsNeeded = secondsNeeded,
                secondsDone = secondsDone,
                applicationId = appId,
                applicationName = appName,
                bannerUrl = bannerUrl,
                thumbnailUrl = thumbUrl
            ))
        }
        result
    }

    private suspend fun completeQuest(token: String, initialState: QuestState, onStateChange: (QuestState) -> Unit) {
        val questId = initialState.quest.id
        val taskName = initialState.quest.taskName
        val secondsNeeded = initialState.quest.secondsNeeded
        var secondsDone = initialState.quest.secondsDone

        var current = initialState.copy(status = QuestStatus.RUNNING, log = "Starting...", progress = secondsDone)
        withContext(Dispatchers.Main) { onStateChange(current) }

        try {
            when (taskName) {
                "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                    val enrollResp = questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
                    ).execute()
                    val enrolledAt = try {
                        val statusObj = try { JSONObject(enrollResp.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                        var ea = System.currentTimeMillis() - 60000L
                        val str = statusObj.optString("enrolled_at", "")
                        if (str.isNotEmpty()) {
                            ea = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                                .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                .parse(str.substringBefore('.'))?.time ?: ea
                        }
                        ea
                    } catch (_: Exception) { System.currentTimeMillis() - 60000L }

                    val maxFuture = 10L
                    val speed = 7L

                    while (secondsDone < secondsNeeded) {
                        val maxAllowed = ((System.currentTimeMillis() - enrolledAt) / 1000L) + maxFuture
                        if (maxAllowed - secondsDone >= speed) {
                            val ts = minOf(secondsNeeded, secondsDone + speed).toDouble() + (Math.random() * 0.4)
                            val reqBody = JSONObject().apply { put("timestamp", ts) }
                                .toString().toRequestBody("application/json".toMediaType())
                            val resp = questHttpClient.newCall(
                                buildRequest("https://discord.com/api/v9/quests/$questId/video-progress", token)
                                    .post(reqBody).build()
                            ).execute()
                            val respBody = resp.body?.string() ?: ""
                            val completed = try {
                                val obj = JSONObject(respBody)
                                obj.has("completed_at") && !obj.isNull("completed_at")
                            } catch (_: Exception) { false }
                            secondsDone = minOf(secondsNeeded, secondsDone + speed)
                            current = current.copy(progress = secondsDone, log = "Sending video progress: ${secondsDone}s / ${secondsNeeded}s")
                            withContext(Dispatchers.Main) { onStateChange(current) }
                            if (completed || secondsDone >= secondsNeeded) break
                        } else {
                            current = current.copy(log = "Waiting for allowed window... (${secondsDone}s done)")
                            withContext(Dispatchers.Main) { onStateChange(current) }
                        }
                        delay(1000)
                    }

                    val finalBody = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }
                        .toString().toRequestBody("application/json".toMediaType())
                    questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/quests/$questId/video-progress", token)
                            .post(finalBody).build()
                    ).execute()

                    current = current.copy(status = QuestStatus.DONE, progress = secondsNeeded, log = "Quest completed successfully!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                "PLAY_ACTIVITY" -> {
                    val dmResp = questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/users/@me/channels", token).build()
                    ).execute()
                    val channelId = try {
                        val arr = JSONArray(dmResp.body?.string() ?: "[]")
                        if (arr.length() > 0) arr.getJSONObject(0).optString("id")
                        else throw Exception("No DM channels")
                    } catch (e: Exception) { throw Exception("Need at least one DM channel: ${e.message}") }
                    val streamKey = "call:$channelId:1"

                    while (secondsDone < secondsNeeded) {
                        val reqBody = JSONObject().apply {
                            put("stream_key", streamKey)
                            put("terminal", false)
                        }.toString().toRequestBody("application/json".toMediaType())
                        val resp = questHttpClient.newCall(
                            buildRequest("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                                .post(reqBody).build()
                        ).execute()
                        val respStr = resp.body?.string() ?: "{}"
                        secondsDone = try {
                            JSONObject(respStr).optJSONObject("progress")
                                ?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                        } catch (_: Exception) { secondsDone }
                        current = current.copy(progress = secondsDone, log = "Heartbeat sent: ${secondsDone}s / ${secondsNeeded}s")
                        withContext(Dispatchers.Main) { onStateChange(current) }
                        if (secondsDone >= secondsNeeded) break
                        delay(20000)
                    }

                    val termBody = JSONObject().apply {
                        put("stream_key", "call:$channelId:1")
                        put("terminal", true)
                    }.toString().toRequestBody("application/json".toMediaType())
                    questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                            .post(termBody).build()
                    ).execute()

                    current = current.copy(status = QuestStatus.DONE, progress = secondsNeeded, log = "Quest completed successfully!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                else -> {
                    current = current.copy(status = QuestStatus.ERROR, log = "Task '$taskName' requires the Discord desktop app and cannot run on mobile.")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }
            }
        } catch (e: Exception) {
            current = current.copy(status = QuestStatus.ERROR, log = "Error: ${e.message}")
            withContext(Dispatchers.Main) { onStateChange(current) }
        }
    }
}
