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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
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
    val TextMuted     = Color(0xFF80848E)
    val Divider       = Color(0xFF3F4147)
    val Orbs          = Color(0xFF7C43E0)
    val Decor         = Color(0xFF57F287)
    val Claimed       = Color(0xFF4E5058)
}

private val SUPER_PROPERTIES =
    "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ3LjApIEdlY2tvLzE0Ny4wIEZpcmVmb3gvMTQ3LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDcuMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tL2xvZ2luP3JlZGlyZWN0X3RvPSUyRnF1ZXN0LWhvbWUiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiJkaXNjb3JkLmNvbSIsInJlbGVhc2VfY2hhbm5lbCI6InN0YWJsZSIsImNsaWVudF9idWlsZF9udW1iZXIiOjQ5OTEyMywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0="

private val DISCORD_HEADERS = mapOf(
    "Content-Type"       to "application/json",
    "User-Agent"         to "Mozilla/5.0 (Android 12; Mobile; rv:147.0) Gecko/147.0 Firefox/147.0",
    "X-Super-Properties" to SUPER_PROPERTIES,
    "X-Discord-Locale"   to "pt-BR",
    "X-Discord-Timezone" to "America/Sao_Paulo",
    "X-Debug-Options"    to "bugReporterEnabled",
    "Referer"            to "https://discord.com/quest-home"
)

private val UTC_SDF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}

private val DISPLAY_SDF = SimpleDateFormat("MMM dd, HH:mm", Locale.US)

private fun parseTs(iso: String?): Long {
    if (iso.isNullOrEmpty() || iso == "null") return 0L
    return try { UTC_SDF.parse(iso.substringBefore('.'))?.time ?: 0L } catch (_: Exception) { 0L }
}

private fun formatTs(iso: String?): String {
    if (iso.isNullOrEmpty() || iso == "null") return ""
    return try { DISPLAY_SDF.format(UTC_SDF.parse(iso.substringBefore('.'))!!) } catch (_: Exception) { "" }
}

data class QuestItem(
    val id: String,
    val name: String,
    val reward: String,
    val rewardType: String,
    val expiresDisplay: String,
    val expiresMs: Long,
    val taskName: String,
    val secondsNeeded: Long,
    val secondsDone: Long,
    val applicationName: String?,
    val bannerUrl: String?,
    val enrolledAt: String?,
    val completedAt: String?,
    val claimedAt: String?
)

enum class CompletionStatus { IDLE, RUNNING, DONE, ERROR }

data class QuestState(
    val quest: QuestItem,
    var status: CompletionStatus = CompletionStatus.IDLE,
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
                primary    = QC.Primary,
                background = QC.Background,
                surface    = QC.Surface
            )) {
                Surface(Modifier.fillMaxSize(), color = QC.Background) {
                    QuestScreen(token = token, onBack = { finish() })
                }
            }
        }
    }

    @Composable
    private fun QuestScreen(token: String, onBack: () -> Unit) {
        var loading    by remember { mutableStateOf(true) }
        var error      by remember { mutableStateOf<String?>(null) }
        var orbBalance by remember { mutableStateOf<Int?>(null) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var refreshKey  by remember { mutableIntStateOf(0) }

        val activeStates  = remember { mutableStateListOf<QuestState>() }
        val claimedStates = remember { mutableStateListOf<QuestState>() }

        LaunchedEffect(refreshKey) {
            loading = true
            error   = null
            activeStates.clear()
            claimedStates.clear()
            orbBalance = null
            try {
                val result = fetchAll(token)
                activeStates.addAll(result.active.map { QuestState(it) })
                claimedStates.addAll(result.claimed.map { QuestState(it, status = CompletionStatus.DONE) })
                orbBalance = result.orbs
            } catch (e: Exception) {
                error = e.message ?: "Unknown error"
            }
            loading = false
        }

        Column(Modifier.fillMaxSize().background(QC.Background)) {
            TopBar(
                orbBalance  = orbBalance,
                onBack      = onBack,
                onRefresh   = { refreshKey++ }
            )
            HorizontalDivider(color = QC.Divider)

            if (!loading && error == null) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = QC.Surface,
                    contentColor     = QC.Primary,
                    divider          = { HorizontalDivider(color = QC.Divider) }
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text(
                            "Active (${activeStates.size})",
                            modifier  = Modifier.padding(vertical = 12.dp),
                            fontSize  = 13.sp,
                            color     = if (selectedTab == 0) QC.Primary else QC.TextMuted,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text(
                            "Completed (${claimedStates.size})",
                            modifier  = Modifier.padding(vertical = 12.dp),
                            fontSize  = 13.sp,
                            color     = if (selectedTab == 1) QC.Primary else QC.TextMuted,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            when {
                loading -> LoadingState()
                error != null -> ErrorState(message = error!!, onRetry = { refreshKey++ })
                selectedTab == 0 -> QuestList(
                    states     = activeStates,
                    isClaimed  = false,
                    emptyTitle = "No active quests!",
                    emptyBody  = "No incomplete quests found for your account.",
                    token      = token,
                    onUpdate   = { updated ->
                        val idx = activeStates.indexOfFirst { it.quest.id == updated.quest.id }
                        if (idx >= 0) activeStates[idx] = updated
                    }
                )
                else -> QuestList(
                    states     = claimedStates,
                    isClaimed  = true,
                    emptyTitle = "No completed quests yet.",
                    emptyBody  = "",
                    token      = token,
                    onUpdate   = {}
                )
            }
        }
    }

    @Composable
    private fun TopBar(orbBalance: Int?, onBack: () -> Unit, onRefresh: () -> Unit) {
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
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = QC.TextPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(4.dp))
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, tint = QC.Primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("Quest Completer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = QC.TextPrimary)
                    Text("Auto-complete Discord quests", fontSize = 11.sp, color = QC.TextMuted)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (orbBalance != null) {
                    Row(
                        modifier = Modifier
                            .background(QC.Orbs.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                            .border(1.dp, QC.Orbs.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = QC.Orbs, modifier = Modifier.size(14.dp))
                        Text("$orbBalance Orbs", fontSize = 12.sp, color = QC.Orbs, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = QC.Primary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }

    @Composable
    private fun LoadingState() {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator(color = QC.Primary, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                Text("Loading quests...", color = QC.TextMuted, fontSize = 14.sp)
            }
        }
    }

    @Composable
    private fun ErrorState(message: String, onRetry: () -> Unit) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.size(64.dp).background(QC.Error.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = QC.Error, modifier = Modifier.size(32.dp))
                }
                Text("Failed to load quests", color = QC.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(message, color = QC.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = QC.Primary),
                    shape  = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try Again", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    private fun QuestList(
        states: List<QuestState>,
        isClaimed: Boolean,
        emptyTitle: String,
        emptyBody: String,
        token: String,
        onUpdate: (QuestState) -> Unit
    ) {
        if (states.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(64.dp).background(QC.Success.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.TaskAlt, contentDescription = null, tint = QC.Success, modifier = Modifier.size(32.dp))
                    }
                    Text(emptyTitle, color = QC.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    if (emptyBody.isNotEmpty()) Text(emptyBody, color = QC.TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                items(states, key = { it.quest.id }) { state ->
                    QuestCard(state = state, isClaimed = isClaimed, token = token, onStateChange = onUpdate)
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    @Composable
    private fun QuestCard(
        state: QuestState,
        isClaimed: Boolean,
        token: String,
        onStateChange: (QuestState) -> Unit
    ) {
        val ctx = LocalContext.current

        val rewardColor = if (isClaimed) QC.Claimed else when (state.quest.rewardType) {
            "orbs"  -> QC.Orbs
            "decor" -> QC.Decor
            else    -> QC.Primary
        }

        val shimmerT = rememberInfiniteTransition(label = "shimmer")
        val shimmerAlpha by shimmerT.animateFloat(
            initialValue    = 0.4f,
            targetValue     = 1f,
            animationSpec   = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
            label           = "shimmerAlpha"
        )

        val gifLoader = remember(ctx) {
            ImageLoader.Builder(ctx).components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }.build()
        }

        val timeLeft = remember(state.quest.expiresMs, isClaimed) {
            if (isClaimed) return@remember "Completed"
            val diff  = state.quest.expiresMs - System.currentTimeMillis()
            val hours = diff / 3_600_000L
            val days  = hours / 24L
            when {
                diff  < 0  -> "Expired"
                days  > 1  -> "${days}d left"
                hours > 0  -> "${hours}h left"
                else       -> "Expiring soon"
            }
        }

        val badgeLabel = when {
            isClaimed -> "CLAIMED"
            else      -> state.quest.rewardType.uppercase()
        }
        val badgeIcon: ImageVector = when {
            isClaimed                       -> Icons.Outlined.CheckCircle
            state.quest.rewardType == "orbs"  -> Icons.Outlined.AutoAwesome
            state.quest.rewardType == "decor" -> Icons.Outlined.CardGiftcard
            else                            -> Icons.Outlined.EmojiEvents
        }
        val taskIcon: ImageVector = when {
            state.quest.taskName.contains("WATCH") -> Icons.Outlined.Videocam
            state.quest.taskName.contains("PLAY")  -> Icons.Outlined.SportsEsports
            else                                   -> Icons.Outlined.Schedule
        }

        Card(
            colors   = CardDefaults.cardColors(containerColor = QC.Surface),
            shape    = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, rewardColor.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
        ) {
            Column(Modifier.fillMaxWidth()) {
                if (state.quest.bannerUrl != null) {
                    Box(Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))) {
                        AsyncImage(
                            model             = ImageRequest.Builder(ctx).data(state.quest.bannerUrl).crossfade(true).build(),
                            contentDescription = null,
                            imageLoader       = gifLoader,
                            modifier          = Modifier.fillMaxSize(),
                            contentScale      = ContentScale.Crop
                        )
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, QC.Surface))))
                        Badge(Modifier.align(Alignment.TopEnd).padding(10.dp), badgeIcon, badgeLabel, rewardColor)
                    }
                } else {
                    Box(
                        Modifier.fillMaxWidth().height(72.dp)
                            .background(Brush.horizontalGradient(listOf(rewardColor.copy(alpha = 0.25f), rewardColor.copy(alpha = 0.06f))), RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).background(rewardColor.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(badgeIcon, contentDescription = null, tint = rewardColor, modifier = Modifier.size(22.dp))
                            }
                            Column {
                                Text(state.quest.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = QC.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (state.quest.reward.isNotBlank())
                                    Text(state.quest.reward, fontSize = 11.sp, color = rewardColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Badge(Modifier.align(Alignment.TopEnd).padding(10.dp), badgeIcon, badgeLabel, rewardColor)
                    }
                }

                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (state.quest.bannerUrl != null) {
                        Text(state.quest.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = QC.TextPrimary)
                        if (state.quest.reward.isNotBlank())
                            Text(state.quest.reward, fontSize = 12.sp, color = rewardColor, fontWeight = FontWeight.SemiBold)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Chip(taskIcon, state.quest.taskName.replace("_", " "), QC.TextMuted)
                        Chip(Icons.Outlined.HourglassEmpty, timeLeft, if (isClaimed) QC.Success else QC.Warning)
                        if (!state.quest.applicationName.isNullOrEmpty())
                            Chip(Icons.Outlined.SportsEsports, state.quest.applicationName, QC.Primary)
                    }

                    if (isClaimed && !state.quest.claimedAt.isNullOrEmpty()) {
                        val claimedFormatted = formatTs(state.quest.claimedAt)
                        if (claimedFormatted.isNotEmpty())
                            Text("Claimed on $claimedFormatted", fontSize = 10.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace)
                    }

                    val currentProgress = state.progress.coerceAtLeast(state.quest.secondsDone)
                    val showProgress = !isClaimed
                        && state.quest.secondsNeeded > 0
                        && (state.status == CompletionStatus.RUNNING || state.status == CompletionStatus.DONE || currentProgress > 0)

                    if (showProgress) {
                        val prog = (currentProgress.toFloat() / state.quest.secondsNeeded.toFloat()).coerceIn(0f, 1f)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progress", fontSize = 10.sp, color = QC.TextMuted, fontWeight = FontWeight.SemiBold)
                                Text("${(prog * 100).toInt()}%", fontSize = 10.sp, color = rewardColor, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress    = { prog },
                                modifier    = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color       = rewardColor,
                                trackColor  = QC.SurfaceVar,
                                strokeCap   = StrokeCap.Round
                            )
                            Text("${currentProgress}s / ${state.quest.secondsNeeded}s", fontSize = 9.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace)
                        }
                    }

                    if (!isClaimed && state.log.isNotBlank()) {
                        val logBg = when (state.status) {
                            CompletionStatus.ERROR -> QC.Error.copy(alpha = 0.08f)
                            CompletionStatus.DONE  -> QC.Success.copy(alpha = 0.08f)
                            else                   -> QC.SurfaceVar
                        }
                        val logColor = when (state.status) {
                            CompletionStatus.ERROR -> QC.Error
                            CompletionStatus.DONE  -> QC.Success
                            else                   -> QC.TextMuted
                        }
                        Box(Modifier.fillMaxWidth().background(logBg, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Text(state.log, fontSize = 11.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
                        }
                    }

                    if (isClaimed) {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(QC.Success.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, QC.Success.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = QC.Success, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Reward already claimed", fontSize = 13.sp, color = QC.Success, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        when (state.status) {
                            CompletionStatus.IDLE -> {
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            completeQuest(token, state, onStateChange)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = rewardColor),
                                    shape    = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Auto Complete", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                            }
                            CompletionStatus.RUNNING -> {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(rewardColor.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                        .border(1.dp, rewardColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = rewardColor.copy(alpha = shimmerAlpha), strokeWidth = 2.5.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Running...", fontWeight = FontWeight.Bold, color = rewardColor, fontSize = 14.sp)
                                }
                            }
                            CompletionStatus.DONE -> {
                                Row(
                                    Modifier.fillMaxWidth()
                                        .background(QC.Success.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                        .border(1.dp, QC.Success.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = QC.Success, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Completed!", fontWeight = FontWeight.Bold, color = QC.Success, fontSize = 14.sp)
                                }
                            }
                            CompletionStatus.ERROR -> {
                                Button(
                                    onClick = {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            completeQuest(token, state.copy(status = CompletionStatus.IDLE, progress = state.quest.secondsDone, log = ""), onStateChange)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors   = ButtonDefaults.buttonColors(containerColor = QC.Error),
                                    shape    = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Retry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Badge(modifier: Modifier, icon: ImageVector, label: String, color: Color) {
        Box(
            modifier = modifier
                .background(color.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Black)
            }
        }
    }

    @Composable
    private fun Chip(icon: ImageVector, text: String, color: Color) {
        Row(
            modifier = Modifier
                .background(color.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
                .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(20.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(11.dp))
            Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    private fun buildRequest(url: String, token: String): Request.Builder =
        Request.Builder().url(url).apply {
            header("Authorization", token)
            DISCORD_HEADERS.forEach { (k, v) -> header(k, v) }
        }

    private fun parseActiveQuest(q: JSONObject): QuestItem? {
        val config     = q.optJSONObject("config") ?: return null
        val userStatus = q.optJSONObject("user_status")

        val expiresAt = config.optString("expires_at", "")
        val expiresMs = parseTs(expiresAt)
        val expiresDisplay = try {
            SimpleDateFormat("MMM dd", Locale.US).format(UTC_SDF.parse(expiresAt.substringBefore('.'))!!)
        } catch (_: Exception) { "?" }

        val taskConfig = config.optJSONObject("task_config")
            ?: config.optJSONObject("taskConfigV2")
            ?: return null
        val tasks = taskConfig.optJSONObject("tasks") ?: return null

        val taskName = listOf(
            "WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO",
            "PLAY_ON_DESKTOP", "PLAY_ACTIVITY", "STREAM_ON_DESKTOP"
        ).firstOrNull { tasks.has(it) } ?: return null

        val taskObj      = tasks.optJSONObject(taskName) ?: return null
        val secondsNeeded = taskObj.optLong("target", 0L)
        val secondsDone  = userStatus
            ?.optJSONObject("progress")
            ?.optJSONObject(taskName)
            ?.optLong("value", 0L) ?: 0L

        val application = config.optJSONObject("application")
        val appId       = application?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
        val appName     = application?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }

        val messages  = config.optJSONObject("messages")
        val questName = messages?.optString("quest_name")?.takeIf { it.isNotEmpty() }
            ?: appName ?: "Quest"

        val rewardsConfig = config.optJSONObject("rewards_config")
        val firstReward   = rewardsConfig?.optJSONArray("rewards")?.optJSONObject(0)
        val rewardStr     = firstReward?.optJSONObject("messages")?.optString("name")
            ?.takeIf { it.isNotEmpty() } ?: ""

        val rewardType = when {
            rewardStr.contains("orb", ignoreCase = true)        -> "orbs"
            rewardStr.contains("decoration", ignoreCase = true) -> "decor"
            rewardStr.contains("nitro", ignoreCase = true)      -> "nitro"
            else                                                -> "prize"
        }

        val assets    = config.optJSONObject("assets")
        val logotype  = assets?.optString("logotype")?.takeIf { it.isNotEmpty() && it != "null" }
        val bannerUrl = when {
            logotype != null && logotype.startsWith("quests/") ->
                "https://cdn.discordapp.com/quests/assets/$logotype"
            logotype != null ->
                "https://cdn.discordapp.com/quests/assets/$logotype"
            appId != null ->
                "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg"
            else -> null
        }

        val enrolledAt  = userStatus?.optString("enrolled_at")?.takeIf { it.isNotEmpty() && it != "null" }
        val completedAt = userStatus?.optString("completed_at")?.takeIf { it != "null" }
        val claimedAt   = userStatus?.optString("claimed_at")?.takeIf { it != "null" }

        return QuestItem(
            id              = q.optString("id"),
            name            = questName,
            reward          = rewardStr,
            rewardType      = rewardType,
            expiresDisplay  = expiresDisplay,
            expiresMs       = expiresMs,
            taskName        = taskName,
            secondsNeeded   = secondsNeeded,
            secondsDone     = secondsDone,
            applicationName = appName,
            bannerUrl       = bannerUrl,
            enrolledAt      = enrolledAt,
            completedAt     = completedAt,
            claimedAt       = claimedAt
        )
    }

    private fun parseClaimedQuest(q: JSONObject): QuestItem? {
        val config     = q.optJSONObject("config") ?: return null
        val userStatus = q.optJSONObject("user_status") ?: return null
        val claimedAt  = userStatus.optString("claimed_at").takeIf { it.isNotEmpty() && it != "null" } ?: return null

        val messages  = config.optJSONObject("messages")
        val questName = messages?.optString("quest_name")?.takeIf { it.isNotEmpty() } ?: "Quest"

        val rewardsArr  = config.optJSONArray("rewards")
        val firstReward = rewardsArr?.optJSONObject(0)
        val rewardStr   = firstReward?.optString("name")?.takeIf { it.isNotEmpty() } ?: ""
        val rewardType  = when {
            rewardStr.contains("orb", ignoreCase = true)        -> "orbs"
            rewardStr.contains("decoration", ignoreCase = true) -> "decor"
            rewardStr.contains("nitro", ignoreCase = true)      -> "nitro"
            else                                                -> "prize"
        }

        val expiresAt      = config.optString("expires_at", "")
        val expiresMs      = parseTs(expiresAt)
        val expiresDisplay = try {
            SimpleDateFormat("MMM dd", Locale.US).format(UTC_SDF.parse(expiresAt.substringBefore('.'))!!)
        } catch (_: Exception) { "?" }

        val assets    = config.optJSONObject("assets")
        val logotype  = assets?.optString("logotype")?.takeIf { it.isNotEmpty() && it != "null" }
        val bannerUrl = logotype?.let { "https://cdn.discordapp.com/quests/assets/$it" }

        val taskConfig = config.optJSONObject("task_config")
            ?: config.optJSONObject("taskConfigV2") ?: JSONObject()
        val tasks      = taskConfig.optJSONObject("tasks") ?: JSONObject()
        val taskName   = listOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ON_DESKTOP", "PLAY_ACTIVITY")
            .firstOrNull { tasks.has(it) } ?: "WATCH_VIDEO"

        return QuestItem(
            id              = q.optString("id"),
            name            = questName,
            reward          = rewardStr,
            rewardType      = rewardType,
            expiresDisplay  = expiresDisplay,
            expiresMs       = expiresMs,
            taskName        = taskName,
            secondsNeeded   = 0L,
            secondsDone     = 0L,
            applicationName = null,
            bannerUrl       = bannerUrl,
            enrolledAt      = userStatus.optString("enrolled_at").takeIf { it.isNotEmpty() && it != "null" },
            completedAt     = userStatus.optString("completed_at").takeIf { it != "null" },
            claimedAt       = claimedAt
        )
    }

    data class FetchResult(val active: List<QuestItem>, val claimed: List<QuestItem>, val orbs: Int?)

    private suspend fun fetchAll(token: String): FetchResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()

        val activeResp = questHttpClient.newCall(
            buildRequest("https://discord.com/api/v9/quests/@me", token).build()
        ).execute()
        val activeBody = activeResp.body?.string() ?: ""

        if (!activeResp.isSuccessful) {
            val msg = try {
                JSONObject(activeBody).optString("message", "HTTP ${activeResp.code}")
            } catch (_: Exception) { "HTTP ${activeResp.code}" }
            throw Exception(msg)
        }

        val activeArr = try { JSONObject(activeBody).optJSONArray("quests") ?: JSONArray() } catch (_: Exception) { JSONArray() }
        val active = mutableListOf<QuestItem>()
        for (i in 0 until activeArr.length()) {
            val q    = activeArr.getJSONObject(i)
            val item = parseActiveQuest(q) ?: continue
            if (item.expiresMs > 0 && item.expiresMs < now) continue
            if (item.claimedAt != null) continue
            active.add(item)
        }

        val claimedResp = questHttpClient.newCall(
            buildRequest("https://discord.com/api/v9/quests/@me/claimed", token).build()
        ).execute()
        val claimedBody = claimedResp.body?.string() ?: ""
        val claimedArr  = try { JSONObject(claimedBody).optJSONArray("quests") ?: JSONArray() } catch (_: Exception) { JSONArray() }
        val claimed = mutableListOf<QuestItem>()
        for (i in 0 until claimedArr.length()) {
            val item = parseClaimedQuest(claimedArr.getJSONObject(i)) ?: continue
            claimed.add(item)
        }

        val orbsResp  = questHttpClient.newCall(
            buildRequest("https://discord.com/api/v9/users/@me/virtual-currency/balance", token).build()
        ).execute()
        val orbsBody  = orbsResp.body?.string() ?: ""
        val orbs      = try { JSONObject(orbsBody).optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }

        FetchResult(active, claimed, orbs)
    }

    private suspend fun completeQuest(
        token: String,
        initialState: QuestState,
        onStateChange: (QuestState) -> Unit
    ) {
        val questId       = initialState.quest.id
        val taskName      = initialState.quest.taskName
        val secondsNeeded = initialState.quest.secondsNeeded
        var secondsDone   = initialState.quest.secondsDone

        var current = initialState.copy(status = CompletionStatus.RUNNING, log = "Starting...", progress = secondsDone)
        withContext(Dispatchers.Main) { onStateChange(current) }

        try {
            when (taskName) {
                "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                    val statusResp = questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
                    ).execute()
                    val enrolledAt = try {
                        val obj = JSONObject(statusResp.body?.string() ?: "{}")
                        val str = obj.optString("enrolled_at", "")
                        if (str.isNotEmpty() && str != "null")
                            UTC_SDF.parse(str.substringBefore('.'))?.time ?: (System.currentTimeMillis() - 60_000L)
                        else
                            System.currentTimeMillis() - 60_000L
                    } catch (_: Exception) { System.currentTimeMillis() - 60_000L }

                    val maxFutureSecs = 10L
                    val stepSecs      = 7L

                    while (secondsDone < secondsNeeded) {
                        val elapsedSecs = (System.currentTimeMillis() - enrolledAt) / 1000L
                        val maxAllowed  = elapsedSecs + maxFutureSecs

                        if (maxAllowed - secondsDone >= stepSecs) {
                            val ts       = minOf(secondsNeeded, secondsDone + stepSecs).toDouble() + (Math.random() * 0.4)
                            val reqBody  = JSONObject().apply { put("timestamp", ts) }.toString().toRequestBody("application/json".toMediaType())
                            val resp     = questHttpClient.newCall(
                                buildRequest("https://discord.com/api/v9/quests/$questId/video-progress", token).post(reqBody).build()
                            ).execute()
                            val respBody = resp.body?.string() ?: ""
                            val done     = try { val o = JSONObject(respBody); o.has("completed_at") && !o.isNull("completed_at") } catch (_: Exception) { false }
                            secondsDone  = minOf(secondsNeeded, secondsDone + stepSecs)
                            current      = current.copy(progress = secondsDone, log = "Sending video progress: ${secondsDone}s / ${secondsNeeded}s")
                            withContext(Dispatchers.Main) { onStateChange(current) }
                            if (done || secondsDone >= secondsNeeded) break
                        } else {
                            current = current.copy(log = "Waiting for time window... (${secondsDone}s done)")
                            withContext(Dispatchers.Main) { onStateChange(current) }
                        }
                        delay(1000L)
                    }

                    val finalBody = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }.toString().toRequestBody("application/json".toMediaType())
                    questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/quests/$questId/video-progress", token).post(finalBody).build()
                    ).execute()

                    current = current.copy(status = CompletionStatus.DONE, progress = secondsNeeded, log = "Quest completed successfully!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                "PLAY_ON_DESKTOP", "PLAY_ACTIVITY" -> {
                    val dmResp    = questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/users/@me/channels", token).build()
                    ).execute()
                    val channelId = try {
                        val arr = JSONArray(dmResp.body?.string() ?: "[]")
                        if (arr.length() > 0) arr.getJSONObject(0).optString("id")
                        else throw Exception("No DM channels found")
                    } catch (e: Exception) { throw Exception("Need at least one DM: ${e.message}") }

                    val streamKey = "call:$channelId:1"

                    while (secondsDone < secondsNeeded) {
                        val reqBody  = JSONObject().apply { put("stream_key", streamKey); put("terminal", false) }.toString().toRequestBody("application/json".toMediaType())
                        val resp     = questHttpClient.newCall(
                            buildRequest("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(reqBody).build()
                        ).execute()
                        val respStr  = resp.body?.string() ?: "{}"
                        secondsDone  = try {
                            JSONObject(respStr).optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", secondsDone) ?: secondsDone
                        } catch (_: Exception) { secondsDone }
                        current = current.copy(progress = secondsDone, log = "Heartbeat sent: ${secondsDone}s / ${secondsNeeded}s")
                        withContext(Dispatchers.Main) { onStateChange(current) }
                        if (secondsDone >= secondsNeeded) break
                        delay(20_000L)
                    }

                    val termBody = JSONObject().apply { put("stream_key", streamKey); put("terminal", true) }.toString().toRequestBody("application/json".toMediaType())
                    questHttpClient.newCall(
                        buildRequest("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(termBody).build()
                    ).execute()

                    current = current.copy(status = CompletionStatus.DONE, progress = secondsNeeded, log = "Quest completed successfully!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                else -> {
                    current = current.copy(status = CompletionStatus.ERROR, log = "Task '$taskName' is not supported on this device.")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }
            }
        } catch (e: Exception) {
            current = current.copy(status = CompletionStatus.ERROR, log = "Error: ${e.message}")
            withContext(Dispatchers.Main) { onStateChange(current) }
        }
    }
}
