package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.*
import coil.request.ImageRequest
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private object DC {
    val Bg          = Color(0xFF0E0F13)
    val Surface     = Color(0xFF17181C)
    val Card        = Color(0xFF1E1F26)
    val CardAlt     = Color(0xFF22232B)
    val Border      = Color(0xFF2C2D35)
    val Primary     = Color(0xFF5865F2)
    val PrimaryGlow = Color(0xFF7B87FF)
    val Success     = Color(0xFF23A55A)
    val Warning     = Color(0xFFFAA61A)
    val Error       = Color(0xFFED4245)
    val White       = Color(0xFFF2F3F5)
    val Muted       = Color(0xFF72767D)
    val OrbPurple   = Color(0xFF9B59B6)
    val OrbViolet   = Color(0xFFB675F0)
    val Gold        = Color(0xFFFFD700)
    val Teal        = Color(0xFF43B581)
}

private val SUPER_PROPS =
    "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ3LjApIEdlY2tvLzE0Ny4wIEZpcmVmb3gvMTQ3LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDcuMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tL2xvZ2luP3JlZGlyZWN0X3RvPSUyRnF1ZXN0LWhvbWUiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiJkaXNjb3JkLmNvbSIsInJlbGVhc2VfY2hhbm5lbCI6InN0YWJsZSIsImNsaWVudF9idWlsZF9udW1iZXIiOjQ5OTEyMywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0="

private fun buildReq(url: String, token: String) = Request.Builder().url(url).apply {
    header("Authorization",      token)
    header("Content-Type",       "application/json")
    header("User-Agent",         "Mozilla/5.0 (Android 12; Mobile; rv:147.0) Gecko/147.0 Firefox/147.0")
    header("X-Super-Properties", SUPER_PROPS)
    header("X-Discord-Locale",   "pt-BR")
    header("X-Discord-Timezone", "America/Sao_Paulo")
    header("X-Debug-Options",    "bugReporterEnabled")
    header("Referer",            "https://discord.com/quest-home")
}

private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}
private fun parseIso(s: String?): Long {
    if (s.isNullOrEmpty() || s == "null") return 0L
    return try { isoFmt.parse(s.substringBefore('.'))?.time ?: 0L } catch (_: Exception) { 0L }
}
private fun fmtDate(s: String?): String {
    val ms = parseIso(s)
    if (ms == 0L) return ""
    return try { SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(ms)) } catch (_: Exception) { "" }
}

private fun buildBannerUrl(questId: String, config: JSONObject): String? {
    val assets = config.optJSONObject("assets") ?: return null
    for (key in listOf("quest_bar_hero", "hero", "logotype", "game_tile")) {
        val v = assets.optString(key, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
        return when {
            v.startsWith("http")    -> v
            v.startsWith("quests/") -> "https://cdn.discordapp.com/$v"
            v.contains("/")         -> "https://cdn.discordapp.com/quests/$v"
            else                    -> "https://cdn.discordapp.com/quests/$questId/$v"
        }
    }
    val appId = config.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
    if (appId != null) return "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg"
    return null
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
    val appName: String?,
    val appId: String?,
    val bannerUrl: String?,
    val enrolledAt: String?,
    val completedAt: String?,
    val claimedAt: String?,
    val configVersion: Int
)

enum class RunState { IDLE, RUNNING, DONE, ERROR, NOT_ENROLLED }

data class QuestState(
    val quest: QuestItem,
    var runState: RunState = RunState.IDLE,
    var progress: Long = 0L,
    var log: String = ""
)

private fun parseQuest(q: JSONObject): QuestItem? {
    val id     = q.optString("id").takeIf { it.isNotEmpty() } ?: return null
    val config = q.optJSONObject("config") ?: return null
    val us     = q.optJSONObject("user_status")

    val expiresAt      = config.optString("expires_at", "")
    val expiresMs      = parseIso(expiresAt)
    val expiresDisplay = try { SimpleDateFormat("MMM dd", Locale.US).format(Date(expiresMs)) } catch (_: Exception) { "?" }

    val taskConfig = config.optJSONObject("task_config")
        ?: config.optJSONObject("taskConfig")
        ?: config.optJSONObject("task_config_v2")
        ?: config.optJSONObject("taskConfigV2")
        ?: return null
    val tasks = taskConfig.optJSONObject("tasks") ?: return null

    val SUPPORTED = listOf("WATCH_VIDEO_ON_MOBILE","WATCH_VIDEO","PLAY_ON_DESKTOP","PLAY_ACTIVITY","STREAM_ON_DESKTOP")
    val taskName     = SUPPORTED.firstOrNull { tasks.has(it) } ?: return null
    val taskObj      = tasks.optJSONObject(taskName) ?: return null
    val secondsNeeded = taskObj.optLong("target", 0L)
    val secondsDone   = us?.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

    val app     = config.optJSONObject("application")
    val appId   = app?.optString("id")?.takeIf  { it.isNotEmpty() && it != "null" }
    val appName = app?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }

    val rewardsConfig = config.optJSONObject("rewards_config")
    val rewardsArr    = rewardsConfig?.optJSONArray("rewards") ?: config.optJSONArray("rewards")
    var reward = ""; var rewardType = "prize"
    if (rewardsArr != null && rewardsArr.length() > 0) {
        val r = rewardsArr.optJSONObject(0)
        if (r != null) {
            rewardType = r.optString("type", "prize").lowercase()
            reward = when (rewardType) {
                "orbs"  -> { val amt = r.optJSONObject("orbs")?.optInt("amount") ?: r.optInt("amount", 0); if (amt > 0) "$amt Orbs" else "Orbs" }
                "decor" -> r.optJSONObject("avatar_decoration")?.optString("name") ?: "Avatar Decoration"
                "nitro" -> "Nitro Boost"
                else    -> r.optString("name", "Prize").takeIf { it.isNotEmpty() } ?: "Prize"
            }
        }
    }

    val completedAt = us?.optString("completed_at")?.takeIf { it != "null" && it.isNotEmpty() }
    val claimedAt   = us?.optString("claimed_at")?.takeIf  { it != "null" && it.isNotEmpty() }
    val enrolledAt  = us?.optString("enrolled_at")?.takeIf { it != "null" && it.isNotEmpty() }
    val configVersion = config.optInt("config_version", config.optInt("configVersion", 2))

    return QuestItem(
        id = id, name = config.optJSONObject("messages")?.optString("questName","")?.takeIf { it.isNotEmpty() } ?: appName ?: id,
        reward = reward, rewardType = rewardType,
        expiresDisplay = expiresDisplay, expiresMs = expiresMs,
        taskName = taskName, secondsNeeded = secondsNeeded, secondsDone = secondsDone,
        appName = appName, appId = appId,
        bannerUrl = buildBannerUrl(id, config),
        enrolledAt = enrolledAt, completedAt = completedAt, claimedAt = claimedAt,
        configVersion = configVersion
    )
}

data class FetchResult(val active: List<QuestItem>, val claimed: List<QuestItem>, val orbs: Int?)

private suspend fun fetchAll(token: String): FetchResult = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()

    val activeResp = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/@me", token).build()).execute()
    val activeBody = activeResp.body?.string() ?: ""
    if (!activeResp.isSuccessful) {
        val msg = try { JSONObject(activeBody).optString("message", "HTTP ${activeResp.code}") } catch (_: Exception) { "HTTP ${activeResp.code}" }
        throw Exception(msg)
    }
    val activeArr = try { JSONObject(activeBody).optJSONArray("quests") ?: JSONArray() } catch (_: Exception) { JSONArray() }
    val active = mutableListOf<QuestItem>()
    for (i in 0 until activeArr.length()) {
        val item = parseQuest(activeArr.getJSONObject(i)) ?: continue
        if (item.expiresMs > 0 && item.expiresMs < now) continue
        if (item.claimedAt != null) continue
        active.add(item)
    }

    val claimedResp = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/@me/claimed", token).build()).execute()
    val claimedBody = claimedResp.body?.string() ?: ""
    val claimedArr  = try { JSONObject(claimedBody).optJSONArray("quests") ?: JSONArray() } catch (_: Exception) { JSONArray() }
    val claimed = mutableListOf<QuestItem>()
    for (i in 0 until claimedArr.length()) {
        val item = parseQuest(claimedArr.getJSONObject(i)) ?: continue
        claimed.add(item)
    }

    val orbResp = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/virtual-currency/balance", token).build()).execute()
    val orbBody  = orbResp.body?.string() ?: ""
    val orbs     = try { JSONObject(orbBody).optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }

    FetchResult(active, claimed, orbs)
}

private suspend fun enrollQuest(token: String, questId: String): Boolean = withContext(Dispatchers.IO) {
    val body = JSONObject().apply { put("platform", 0) }.toString().toRequestBody("application/json".toMediaType())
    httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/enroll", token).post(body).build()).execute().isSuccessful
}

private suspend fun completeQuest(token: String, state: QuestState, onUpdate: (QuestState) -> Unit) {
    val q             = state.quest
    val questId       = q.id
    val taskName      = q.taskName
    val secondsNeeded = q.secondsNeeded
    var secondsDone   = q.secondsDone

    var cur = state.copy(runState = RunState.RUNNING, log = "Starting...", progress = secondsDone)
    withContext(Dispatchers.Main) { onUpdate(cur) }

    fun upd(log: String, prog: Long = secondsDone, rs: RunState = RunState.RUNNING) {
        cur = cur.copy(runState = rs, log = log, progress = prog)
    }

    try {
        var enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Not enrolled yet, attempting to enroll...")
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val ok = enrollQuest(token, questId)
            if (!ok) {
                val statusBody = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()).execute().body?.string() ?: "{}"
                enrolledAt = try { JSONObject(statusBody).optString("enrolled_at","").takeIf { it.isNotEmpty() && it != "null" } } catch (_: Exception) { null }
                if (enrolledAt == null) {
                    upd("⚠ Not enrolled. Open Discord and accept this quest first.", prog = 0, rs = RunState.NOT_ENROLLED)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    return
                }
            } else {
                delay(800)
                val statusBody = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()).execute().body?.string() ?: "{}"
                enrolledAt = try { JSONObject(statusBody).optString("enrolled_at","").takeIf { it.isNotEmpty() && it != "null" } } catch (_: Exception) { null }
                    ?: (System.currentTimeMillis() - 10_000L).toString()
            }
        }

        when (taskName) {
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                val enrollMs  = parseIso(enrolledAt).takeIf { it > 0L } ?: (System.currentTimeMillis() - 60_000L)
                val maxFuture = 10
                val speed     = 7
                val interval  = 1

                upd("📺 Spoofing video: ${q.name}")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                var completed = false
                while (true) {
                    val maxAllowed = ((System.currentTimeMillis() - enrollMs) / 1000).toInt() + maxFuture
                    val diff       = maxAllowed - secondsDone
                    val timestamp  = secondsDone + speed

                    if (diff >= speed) {
                        val sendTs = minOf(secondsNeeded.toDouble(), timestamp.toDouble() + Math.random())
                        val body   = JSONObject().apply { put("timestamp", sendTs) }.toString().toRequestBody("application/json".toMediaType())
                        val resp   = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token).post(body).build()).execute()
                        val rb     = resp.body?.string() ?: "{}"
                        completed  = try { JSONObject(rb).optString("completed_at","").isNotEmpty() } catch (_: Exception) { false }
                        secondsDone = minOf(secondsNeeded, timestamp)
                        upd("📺 Video progress: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                    }

                    if (timestamp >= secondsNeeded) break
                    delay(interval * 1000L)
                }

                if (!completed) {
                    val fb = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }.toString().toRequestBody("application/json".toMediaType())
                    httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token).post(fb).build()).execute()
                }
                upd("✅ Quest completed! Claim the reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ON_DESKTOP" -> {
                val dmBody  = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/channels", token).build()).execute().body?.string() ?: "[]"
                val channelId = try { val a = JSONArray(dmBody); if (a.length() > 0) a.getJSONObject(0).optString("id") else null } catch (_: Exception) { null }
                    ?: throw Exception("No DM channel found. Open at least one DM in Discord.")
                val streamKey = "call:$channelId:1"
                upd("🎮 Spoofing game play... (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (secondsDone < secondsNeeded) {
                    val rb = JSONObject().apply { put("stream_key", streamKey); put("terminal", false) }.toString().toRequestBody("application/json".toMediaType())
                    val rs = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(rb).build()).execute().body?.string() ?: "{}"
                    val newP = try {
                        val o = JSONObject(rs); val pr = o.optJSONObject("progress")
                        when { q.configVersion == 1 -> o.optLong("stream_progress_seconds", secondsDone)
                               pr != null -> pr.optJSONObject(taskName)?.optLong("value", secondsDone) ?: secondsDone
                               else -> secondsDone }
                    } catch (_: Exception) { secondsDone }
                    secondsDone = newP
                    upd("🎮 Game: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0L,(secondsNeeded-secondsDone)/60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    if (secondsDone >= secondsNeeded) break
                    delay(20_000L)
                }
                val tb = JSONObject().apply { put("stream_key", streamKey); put("terminal", true) }.toString().toRequestBody("application/json".toMediaType())
                httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(tb).build()).execute()
                upd("✅ Quest completed! Claim the reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ACTIVITY" -> {
                val dmBody    = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/channels", token).build()).execute().body?.string() ?: "[]"
                val channelId = try { val a = JSONArray(dmBody); if (a.length() > 0) a.getJSONObject(0).optString("id") else null } catch (_: Exception) { null }
                    ?: throw Exception("No DM channel found. Open at least one DM in Discord.")
                val streamKey = "call:$channelId:1"
                upd("🕹️ Spoofing activity... (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (true) {
                    val rb = JSONObject().apply { put("stream_key", streamKey); put("terminal", false) }.toString().toRequestBody("application/json".toMediaType())
                    val rs = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(rb).build()).execute().body?.string() ?: "{}"
                    val newP = try { JSONObject(rs).optJSONObject("progress")?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone } catch (_: Exception) { secondsDone }
                    secondsDone = newP
                    upd("🕹️ Activity: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    if (secondsDone >= secondsNeeded) {
                        val tb = JSONObject().apply { put("stream_key", streamKey); put("terminal", true) }.toString().toRequestBody("application/json".toMediaType())
                        httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(tb).build()).execute()
                        break
                    }
                    delay(20_000L)
                }
                upd("✅ Quest completed! Claim the reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "STREAM_ON_DESKTOP" -> {
                upd("⚠ STREAM_ON_DESKTOP requires the Discord desktop app.\nCannot be completed via API.", prog = 0, rs = RunState.ERROR)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            else -> {
                upd("⚠ Task '$taskName' not supported.", prog = 0, rs = RunState.ERROR)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
        }
    } catch (e: Exception) {
        upd("❌ Error: ${e.message}", rs = RunState.ERROR)
        withContext(Dispatchers.Main) { onUpdate(cur) }
    }
}

class QuestActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        private const val PREFS_NAME  = "quest_prefs"
        fun start(ctx: Context, token: String) {
            ctx.startActivity(Intent(ctx, QuestActivity::class.java).apply { putExtra(EXTRA_TOKEN, token) })
        }
    }
    private fun prefs(): SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = DC.Primary, background = DC.Bg, surface = DC.Surface)) {
                Surface(Modifier.fillMaxSize(), color = DC.Bg) {
                    QuestRoot(token = token, prefs = prefs(), onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun QuestRoot(token: String, prefs: SharedPreferences, onBack: () -> Unit) {
    var tosAcked by remember { mutableStateOf(prefs.getBoolean("tos_ack", false)) }
    if (!tosAcked) {
        TosDialog(onAccept = { prefs.edit().putBoolean("tos_ack", true).apply(); tosAcked = true }, onDecline = onBack)
        return
    }
    QuestScreen(token = token, onBack = onBack)
}

@Composable
private fun TosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(DC.Card).border(1.dp, DC.Primary.copy(0.4f), RoundedCornerShape(24.dp)).padding(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(44.dp).background(DC.Warning.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Warning, null, tint = DC.Warning, modifier = Modifier.size(24.dp))
                    }
                    Text("Terms of Service Warning", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White)
                }
                HorizontalDivider(color = DC.Border)
                Text(
                    "⚠️ This tool automates Discord Quest completion via the official API.\n\n" +
                    "Using third-party automation tools may violate Discord's Terms of Service. " +
                    "Your account could potentially be suspended or banned.\n\n" +
                    "Provided for educational purposes only. Use at your own risk.",
                    fontSize = 13.sp, color = DC.White.copy(0.85f), lineHeight = 20.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, DC.Error.copy(0.6f))) {
                        Text("Decline", color = DC.Error, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) {
                        Text("I Understand", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestScreen(token: String, onBack: () -> Unit) {
    var loading     by remember { mutableStateOf(true) }
    var fetchError  by remember { mutableStateOf<String?>(null) }
    var orbBalance  by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshKey  by remember { mutableIntStateOf(0) }

    val activeStates  = remember { mutableStateListOf<QuestState>() }
    val claimedStates = remember { mutableStateListOf<QuestState>() }

    LaunchedEffect(refreshKey) {
        loading = true; fetchError = null
        activeStates.clear(); claimedStates.clear(); orbBalance = null
        try {
            val res = fetchAll(token)
            activeStates.addAll(res.active.map { q -> QuestState(quest = q, runState = if (q.enrolledAt == null) RunState.NOT_ENROLLED else RunState.IDLE, progress = q.secondsDone) })
            claimedStates.addAll(res.claimed.map { QuestState(it, RunState.DONE) })
            orbBalance = res.orbs
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(DC.Bg)) {
        QuestTopBar(token = token, orbBalance = orbBalance, onBack = onBack, onRefresh = { refreshKey++ })
        HorizontalDivider(color = DC.Border)

        if (!loading && fetchError == null) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = DC.Surface,
                contentColor     = DC.Primary,
                indicator        = { tabPos ->
                    if (selectedTab < tabPos.size) {
                        Box(
                            Modifier.tabIndicatorOffset(tabPos[selectedTab]).height(3.dp)
                                .padding(horizontal = 24.dp).background(DC.Primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                },
                divider = { HorizontalDivider(color = DC.Border) }
            ) {
                listOf(
                    "⚔️  Active (${activeStates.size})",
                    "✅  Claimed (${claimedStates.size})"
                ).forEachIndexed { idx, label ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }) {
                        Text(
                            label,
                            modifier   = Modifier.padding(vertical = 13.dp),
                            fontSize   = 13.sp,
                            fontWeight = if (selectedTab == idx) FontWeight.ExtraBold else FontWeight.Medium,
                            color      = if (selectedTab == idx) DC.Primary else DC.Muted
                        )
                    }
                }
            }
        }

        when {
            loading        -> CenteredSpinner()
            fetchError != null -> ErrorPane(fetchError!!) { refreshKey++ }
            selectedTab == 0 -> QuestListPane(
                states    = activeStates,
                isClaimed = false,
                empty     = "No active quests" to "You have no incomplete quests right now.",
                token     = token
            ) { updated ->
                val i = activeStates.indexOfFirst { it.quest.id == updated.quest.id }
                if (i >= 0) activeStates[i] = updated
            }
            else -> QuestListPane(
                states    = claimedStates,
                isClaimed = true,
                empty     = "No claimed quests" to "Completed quests will appear here.",
                token     = token,
                onUpdate  = {}
            )
        }
    }
}

@Composable
private fun QuestTopBar(token: String, orbBalance: Int?, onBack: () -> Unit, onRefresh: () -> Unit) {
    val ctx = LocalContext.current
    Row(
        Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text("Discord Quests", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White)
                Text("Auto Completion", fontSize = 10.sp, color = DC.Muted)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (orbBalance != null && orbBalance > 0) {
                Box(
                    Modifier.background(DC.OrbViolet.copy(0.15f), RoundedCornerShape(20.dp))
                        .border(1.dp, DC.OrbViolet.copy(0.35f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("⭐", fontSize = 11.sp)
                        Text("$orbBalance orbs", fontSize = 11.sp, color = DC.OrbViolet, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            IconButton(
                onClick = {
                    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("Discord Token", token))
                },
                modifier = Modifier.size(36.dp).background(DC.Card, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(17.dp))
            }
            IconButton(
                onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/channels/@me")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) }
                    catch (_: Exception) {}
                },
                modifier = Modifier.size(36.dp).background(DC.Card, RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Outlined.OpenInBrowser, null, tint = DC.Muted, modifier = Modifier.size(17.dp))
            }
            IconButton(
                onClick = onRefresh,
                modifier = Modifier.size(36.dp).background(DC.Primary.copy(0.15f), RoundedCornerShape(10.dp))
            ) {
                Icon(Icons.Outlined.Refresh, null, tint = DC.Primary, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun QuestListPane(states: List<QuestState>, isClaimed: Boolean, empty: Pair<String,String>, token: String, onUpdate: (QuestState) -> Unit) {
    if (states.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                val (icon, color) = if (isClaimed) Icons.Outlined.TaskAlt to DC.Teal else Icons.Outlined.SportsEsports to DC.Muted
                Box(Modifier.size(72.dp).background(color.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(36.dp))
                }
                Text(empty.first, color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(empty.second, color = DC.Muted, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Spacer(Modifier.height(8.dp)) }
            items(states, key = { it.quest.id }) { state ->
                QuestCard(state, isClaimed, token, onUpdate)
            }
            item { Spacer(Modifier.height(28.dp)) }
        }
    }
}

@Composable
private fun QuestCard(state: QuestState, isClaimed: Boolean, token: String, onStateChange: (QuestState) -> Unit) {
    val ctx = LocalContext.current
    val q   = state.quest

    val accentColor = if (isClaimed) DC.Teal else when (q.rewardType) {
        "orbs"  -> DC.OrbViolet
        "decor" -> DC.Success
        "nitro" -> DC.Primary
        else    -> DC.Gold
    }

    val gifLoader = remember(ctx) {
        ImageLoader.Builder(ctx).components {
            if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    val timeLeft = remember(q.expiresMs, isClaimed) {
        if (isClaimed) return@remember "Claimed"
        val diff = q.expiresMs - System.currentTimeMillis()
        val h = diff / 3_600_000L
        when { diff < 0 -> "Expired"; h > 48 -> "${h/24}d left"; h > 0 -> "${h}h left"; else -> "< 1h left" }
    }

    val shimmerT = rememberInfiniteTransition(label = "sh")
    val shimmerX by shimmerT.animateFloat(-300f, 1200f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart), label = "sx")

    val pulseT = rememberInfiniteTransition(label = "pulse")
    val pulseA by pulseT.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pa")
    val borderAlpha = if (state.runState == RunState.RUNNING) pulseA else if (isClaimed) 0.35f else 0.2f

    Card(
        colors   = CardDefaults.cardColors(containerColor = DC.Card),
        shape    = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth().border(1.5.dp, accentColor.copy(borderAlpha), RoundedCornerShape(20.dp))
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))) {
                if (q.bannerUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(q.bannerUrl).crossfade(true).build(),
                        contentDescription = null,
                        imageLoader = gifLoader,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(
                        listOf(accentColor.copy(0.4f), DC.Card.copy(0.8f), DC.Surface),
                        start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DC.Card), startY = 50f)))
                if (state.runState == RunState.RUNNING) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(0.35f), Color.Transparent),
                        startX = shimmerX, endX = shimmerX + 350f
                    )))
                }
                Box(Modifier.align(Alignment.TopEnd).padding(12.dp)
                    .background(accentColor.copy(0.85f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    val label = when { isClaimed -> "✓ CLAIMED"; q.rewardType == "orbs" -> "⭐ ORBS"; q.rewardType == "decor" -> "🎨 DECOR"; q.rewardType == "nitro" -> "💜 NITRO"; else -> q.reward.take(14).uppercase() }
                    Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                }
                if (!q.appName.isNullOrEmpty()) {
                    Box(Modifier.align(Alignment.BottomStart).padding(12.dp)
                        .background(Color.Black.copy(0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(q.appName, fontSize = 10.sp, color = DC.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(Modifier.padding(14.dp, 12.dp, 14.dp, 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(q.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DC.White, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (q.reward.isNotBlank()) {
                    Text(q.reward, fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    val tIcon = when { q.taskName.contains("WATCH") -> Icons.Outlined.Videocam; q.taskName.contains("PLAY") -> Icons.Outlined.SportsEsports; q.taskName.contains("STREAM") -> Icons.Outlined.Cast; else -> Icons.Outlined.Schedule }
                    MiniChip(tIcon, q.taskName.replace("_"," "), DC.Muted)
                    MiniChip(Icons.Outlined.HourglassEmpty, timeLeft, if (isClaimed) DC.Teal else DC.Warning)
                    if (q.enrolledAt == null && !isClaimed) MiniChip(Icons.Outlined.ErrorOutline, "NOT ENROLLED", DC.Error)
                }

                if (isClaimed && q.claimedAt != null) {
                    val fmtd = fmtDate(q.claimedAt)
                    if (fmtd.isNotEmpty()) Text("Claimed $fmtd", fontSize = 10.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                }

                val curProgress = state.progress.coerceAtLeast(q.secondsDone)
                if (!isClaimed && q.secondsNeeded > 0) {
                    val frac = (curProgress.toFloat() / q.secondsNeeded).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.SemiBold)
                            Text("${(frac * 100).toInt()}%", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)).background(DC.Border)) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(frac).background(
                                Brush.horizontalGradient(listOf(accentColor.copy(0.7f), accentColor)),
                                RoundedCornerShape(4.dp)
                            ))
                        }
                        Text("${curProgress}s / ${q.secondsNeeded}s", fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                    }
                }

                if (!isClaimed && state.log.isNotBlank()) {
                    val logBg    = when (state.runState) { RunState.ERROR -> DC.Error.copy(0.08f); RunState.DONE -> DC.Success.copy(0.08f); RunState.NOT_ENROLLED -> DC.Warning.copy(0.08f); else -> DC.CardAlt }
                    val logColor = when (state.runState) { RunState.ERROR -> DC.Error; RunState.DONE -> DC.Success; RunState.NOT_ENROLLED -> DC.Warning; else -> DC.Muted }
                    Box(Modifier.fillMaxWidth().background(logBg, RoundedCornerShape(10.dp)).padding(10.dp, 8.dp)) {
                        Text(state.log, fontSize = 11.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                    }
                }

                when {
                    isClaimed -> ClaimedActionRow(q, ctx)
                    state.runState == RunState.DONE -> DoneActionRow(q, ctx)
                    state.runState == RunState.RUNNING -> RunningRow(accentColor, shimmerX, state.log)
                    state.runState == RunState.NOT_ENROLLED ->
                        NotEnrolledButton { CoroutineScope(Dispatchers.IO).launch { completeQuest(token, state.copy(runState = RunState.IDLE, log = ""), onStateChange) } }
                    else ->
                        AutoCompleteButton(accentColor, shimmerX) { CoroutineScope(Dispatchers.IO).launch { completeQuest(token, state, onStateChange) } }
                }
            }
        }
    }
}

@Composable
private fun AutoCompleteButton(color: Color, shimmerX: Float, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(color, color.copy(0.75f))))
            .drawWithContent {
                drawContent()
                drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.2f), Color.Transparent), startX = shimmerX, endX = shimmerX + 280f), size = size)
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.PlayArrow, null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text("Auto Complete", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
        }
    }
}

@Composable
private fun NotEnrolledButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DC.Warning),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(Icons.Outlined.AddCircleOutline, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Enroll & Complete", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
    }
}

@Composable
private fun RunningRow(color: Color, shimmerX: Float, log: String) {
    val pulseT = rememberInfiniteTransition(label = "rp")
    val alpha  by pulseT.animateFloat(0.5f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "rpa")
    Box(
        Modifier.fillMaxWidth().height(52.dp).clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.12f))
            .border(1.5.dp, color.copy(alpha), RoundedCornerShape(14.dp))
            .drawWithContent {
                drawContent()
                drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, color.copy(0.3f), Color.Transparent), startX = shimmerX, endX = shimmerX + 280f), size = size)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = color, strokeWidth = 2.5.dp)
            Text(
                if (log.contains("s /")) log.substringAfterLast("📺 ").substringAfterLast("🎮 ").substringAfterLast("🕹️ ").take(32)
                else "Running...",
                fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp, maxLines = 1
            )
        }
    }
}

@Composable
private fun DoneActionRow(q: QuestItem, ctx: Context) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                .background(DC.Success.copy(0.12f)).border(1.dp, DC.Success.copy(0.35f), RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = DC.Success, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Completed!", fontWeight = FontWeight.ExtraBold, color = DC.Success, fontSize = 14.sp)
        }
        IconButton(
            onClick = {
                try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/channels/@me")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
            },
            modifier = Modifier.size(52.dp).background(DC.Primary.copy(0.15f), RoundedCornerShape(14.dp)).border(1.dp, DC.Primary.copy(0.3f), RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Outlined.OpenInBrowser, null, tint = DC.Primary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ClaimedActionRow(q: QuestItem, ctx: Context) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(14.dp))
                .background(DC.Teal.copy(0.1f)).border(1.dp, DC.Teal.copy(0.25f), RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = DC.Teal, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text("Reward Claimed", fontSize = 13.sp, color = DC.Teal, fontWeight = FontWeight.SemiBold)
        }
        val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", q.name)) },
            modifier = Modifier.size(50.dp).background(DC.CardAlt, RoundedCornerShape(14.dp)).border(1.dp, DC.Border, RoundedCornerShape(14.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(17.dp))
        }
        if (q.appId != null) {
            IconButton(
                onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/discovery/quests")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                },
                modifier = Modifier.size(50.dp).background(DC.CardAlt, RoundedCornerShape(14.dp)).border(1.dp, DC.Border, RoundedCornerShape(14.dp))
            ) {
                Icon(Icons.Outlined.OpenInBrowser, null, tint = DC.Muted, modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun MiniChip(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier.background(color.copy(0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
        Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CenteredSpinner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val inf = rememberInfiniteTransition(label = "si")
            val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(1000, easing = LinearEasing)), label = "sr")
            Box(Modifier.size(56.dp).rotate(rot).border(3.dp, Brush.sweepGradient(listOf(DC.Primary, Color.Transparent)), CircleShape))
            Text("Loading quests...", color = DC.Muted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(72.dp).background(DC.Error.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = DC.Error, modifier = Modifier.size(36.dp))
            }
            Text("Failed to load quests", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(msg, color = DC.Muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
