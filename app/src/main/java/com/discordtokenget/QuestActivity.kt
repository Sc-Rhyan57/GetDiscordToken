package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import android.media.MediaPlayer
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.*
import coil.request.ImageRequest
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.io.File
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
    val OrbViolet   = Color(0xFFB675F0)
    val Teal        = Color(0xFF43B581)
    val GradTop     = Color(0xFF1A1040)
    val GradMid     = Color(0xFF0F1B3D)
    val GradBot     = Color(0xFF0E0F13)
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

private fun accentForType(rewardType: String, isClaimed: Boolean): Color = when {
    isClaimed -> DC.Teal
    rewardType == "orbs"  -> DC.OrbViolet
    rewardType == "decor" -> DC.Success
    rewardType == "nitro" -> DC.Primary
    else -> DC.Primary
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
    val configVersion: Int,
    val questLink: String?
)

enum class RunState { IDLE, RUNNING, DONE, ERROR, NOT_ENROLLED, DESKTOP_ONLY }

data class QuestState(
    val quest: QuestItem,
    var runState: RunState = RunState.IDLE,
    var progress: Long = 0L,
    var log: String = ""
)

private val SUPPORTED_TASKS = listOf(
    "WATCH_VIDEO_ON_MOBILE",
    "WATCH_VIDEO",
    "PLAY_ACTIVITY",
    "PLAY_ON_DESKTOP",
    "PLAY_ON_DESKTOP_V2",
    "STREAM_ON_DESKTOP"
)

private val MOBILE_CAPABLE = setOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ACTIVITY")

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

    val taskName      = SUPPORTED_TASKS.firstOrNull { tasks.has(it) } ?: return null
    val taskObj       = tasks.optJSONObject(taskName) ?: return null
    val secondsNeeded = taskObj.optLong("target", 0L)
    val secondsDone   = us?.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

    val app     = config.optJSONObject("application")
    val appId   = app?.optString("id")?.takeIf  { it.isNotEmpty() && it != "null" }
    val appName = app?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }
    val appLink = app?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" }
        ?: config.optJSONObject("cta_config")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" }

    val rewardsConfig = config.optJSONObject("rewards_config")
    val rewardsArr    = rewardsConfig?.optJSONArray("rewards") ?: config.optJSONArray("rewards")
    var reward = ""; var rewardType = "prize"
    if (rewardsArr != null && rewardsArr.length() > 0) {
        val r = rewardsArr.optJSONObject(0)
        if (r != null) {
            val typeRaw = r.optString("type", "0")
            rewardType = when (typeRaw) {
                "4", "orbs"  -> "orbs"
                "3", "decor" -> "decor"
                "2", "nitro" -> "nitro"
                else -> "prize"
            }
            reward = when (rewardType) {
                "orbs"  -> {
                    val qty = r.optInt("orb_quantity", 0).takeIf { it > 0 }
                        ?: r.optJSONObject("orbs")?.optInt("amount", 0) ?: 0
                    if (qty > 0) "$qty Orbs" else
                        r.optJSONObject("messages")?.optString("name","")?.takeIf { it.isNotEmpty() } ?: "Orbs"
                }
                "decor" -> r.optString("name","").takeIf { it.isNotEmpty() } ?: "Avatar Decoration"
                "nitro" -> "Nitro Boost"
                else    -> r.optString("name","Prize").takeIf { it.isNotEmpty() } ?: "Prize"
            }
        }
    }

    val completedAt   = us?.optString("completed_at")?.takeIf { it != "null" && it.isNotEmpty() }
    val claimedAt     = us?.optString("claimed_at")?.takeIf  { it != "null" && it.isNotEmpty() }
    val enrolledAt    = us?.optString("enrolled_at")?.takeIf { it != "null" && it.isNotEmpty() }
    val configVersion = config.optInt("config_version", config.optInt("configVersion", q.optInt("config_version", 2)))
    val questName     = config.optJSONObject("messages")?.let {
        it.optString("quest_name","").takeIf { s -> s.isNotEmpty() }
            ?: it.optString("questName","").takeIf { s -> s.isNotEmpty() }
    } ?: appName ?: id

    return QuestItem(
        id = id, name = questName, reward = reward, rewardType = rewardType,
        expiresDisplay = expiresDisplay, expiresMs = expiresMs,
        taskName = taskName, secondsNeeded = secondsNeeded, secondsDone = secondsDone,
        appName = appName, appId = appId, bannerUrl = buildBannerUrl(id, config),
        enrolledAt = enrolledAt, completedAt = completedAt, claimedAt = claimedAt,
        configVersion = configVersion, questLink = appLink
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

private suspend fun claimQuestReward(token: String, questId: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val resp = httpClient.newCall(
            buildReq("https://discord.com/api/v9/quests/$questId/claim-reward", token)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
        ).execute()
        resp.isSuccessful
    } catch (_: Exception) { false }
}

private suspend fun findFirstChannelId(token: String): String? = withContext(Dispatchers.IO) {
    try {
        val resp = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/channels", token).build()).execute()
        val arr = JSONArray(resp.body?.string() ?: "[]")
        if (arr.length() > 0) arr.getJSONObject(0).optString("id").takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) { null }
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

    if (taskName !in MOBILE_CAPABLE) {
        upd(
            when (taskName) {
                "PLAY_ON_DESKTOP", "PLAY_ON_DESKTOP_V2" ->
                    "Requires Discord desktop app.\nPLAY_ON_DESKTOP uses Electron IPC to register the game — not possible via HTTP from Android."
                "STREAM_ON_DESKTOP" ->
                    "Requires Discord desktop app.\nSTREAM_ON_DESKTOP needs an active stream from the Electron client."
                else -> "Task '$taskName' is not supported from Android."
            },
            prog = secondsDone,
            rs = RunState.DESKTOP_ONLY
        )
        withContext(Dispatchers.Main) { onUpdate(cur) }
        return
    }

    try {
        if (q.completedAt != null && q.claimedAt == null) {
            upd("Claiming reward...", secondsNeeded)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val ok = claimQuestReward(token, questId)
            upd(if (ok) "Reward claimed!" else "Completed! Claim reward in Discord.", secondsNeeded, RunState.DONE)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            return
        }

        var enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Enrolling in quest...")
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val ok = enrollQuest(token, questId)
            if (!ok) {
                val sb = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()).execute().body?.string() ?: "{}"
                enrolledAt = try { JSONObject(sb).optString("enrolled_at","").takeIf { it.isNotEmpty() && it != "null" } } catch (_: Exception) { null }
                if (enrolledAt == null) {
                    upd("Not enrolled. Accept the quest in Discord first.", prog = 0, rs = RunState.NOT_ENROLLED)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    return
                }
            } else {
                delay(800)
                val sb = httpClient.newCall(buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()).execute().body?.string() ?: "{}"
                enrolledAt = try { JSONObject(sb).optString("enrolled_at","").takeIf { it.isNotEmpty() && it != "null" } } catch (_: Exception) { null }
                    ?: (System.currentTimeMillis() - 10_000L).toString()
            }
        }

        when (taskName) {
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                val startMs   = System.currentTimeMillis()
                val speed     = 7L
                val interval  = 1000L

                upd("Spoofing video: ${q.name}")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                var completed = false
                while (secondsDone < secondsNeeded) {
                    val elapsedSec = (System.currentTimeMillis() - startMs) / 1000L
                    val nextTs     = secondsDone + speed
                    val sendTs     = minOf(secondsNeeded.toDouble(), nextTs.toDouble())

                    if (elapsedSec + 2 >= secondsDone) {
                        val body = JSONObject().apply { put("timestamp", sendTs) }.toString().toRequestBody("application/json".toMediaType())
                        val resp = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token).post(body).build()).execute()
                        val rj   = try { JSONObject(resp.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                        completed = rj.optString("completed_at","").isNotEmpty()
                        secondsDone = minOf(secondsNeeded, nextTs)
                        upd("Video: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        if (completed) break
                    }

                    delay(interval)
                }

                if (!completed) {
                    val fb = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }.toString().toRequestBody("application/json".toMediaType())
                    val fr = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token).post(fb).build()).execute()
                    val fj = try { JSONObject(fr.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                    completed = fj.optString("completed_at","").isNotEmpty()
                }

                secondsDone = secondsNeeded
                upd("Attempting to claim reward...", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                delay(1000L)
                val claimed = claimQuestReward(token, questId)
                upd(if (claimed) "Reward claimed!" else "Completed! Claim the reward in Discord.", secondsDone, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ACTIVITY" -> {
                val channelId = findFirstChannelId(token)
                    ?: throw Exception("No DM channel found. Open a DM in Discord first.")
                val streamKey = "call:$channelId:1"

                upd("Spoofing activity (~${maxOf(0L,(secondsNeeded - secondsDone) / 60)} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (true) {
                    val rb = JSONObject().apply {
                        put("stream_key", streamKey)
                        put("terminal", false)
                    }.toString().toRequestBody("application/json".toMediaType())

                    val rs  = httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(rb).build()).execute()
                    val rsb = rs.body?.string() ?: "{}"
                    val rj  = try { JSONObject(rsb) } catch (_: Exception) { JSONObject() }

                    val newP = rj.optJSONObject("progress")?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                    secondsDone = newP
                    upd("Activity: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0L,(secondsNeeded-secondsDone)/60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (secondsDone >= secondsNeeded) {
                        val tb = JSONObject().apply {
                            put("stream_key", streamKey)
                            put("terminal", true)
                        }.toString().toRequestBody("application/json".toMediaType())
                        httpClient.newCall(buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(tb).build()).execute()
                        break
                    }
                    delay(20_000L)
                }

                upd("Attempting to claim reward...", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                delay(1000L)
                val claimed = claimQuestReward(token, questId)
                upd(if (claimed) "Reward claimed!" else "Completed! Claim reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            else -> {
                upd("Task '$taskName' not supported from Android.", prog = 0, rs = RunState.DESKTOP_ONLY)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
        }
    } catch (e: Exception) {
        upd("Error: ${e.message}", rs = RunState.ERROR)
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
                    "This tool automates Discord Quest completion via the official API.\n\n" +
                    "Using automation tools may violate Discord's Terms of Service. " +
                    "Your account could be suspended or banned.\n\n" +
                    "Educational purposes only. Use at your own risk.",
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
    val ctx = LocalContext.current
    var loading     by remember { mutableStateOf(true) }
    var fetchError  by remember { mutableStateOf<String?>(null) }
    var orbBalance  by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshKey  by remember { mutableIntStateOf(0) }
    var completing  by remember { mutableStateOf(false) }

    val activeStates  = remember { mutableStateListOf<QuestState>() }
    val claimedStates = remember { mutableStateListOf<QuestState>() }

    LaunchedEffect(refreshKey) {
        loading = true; fetchError = null
        activeStates.clear(); claimedStates.clear(); orbBalance = null
        try {
            val res = fetchAll(token)
            activeStates.addAll(res.active.map { q ->
                val rs = when {
                    q.completedAt != null && q.claimedAt == null -> RunState.DONE
                    q.taskName !in MOBILE_CAPABLE -> RunState.DESKTOP_ONLY
                    q.enrolledAt == null -> RunState.NOT_ENROLLED
                    else -> RunState.IDLE
                }
                val log = when (rs) {
                    RunState.DONE -> "Completed! Tap to claim reward."
                    RunState.DESKTOP_ONLY -> "Requires Discord desktop app (PLAY/STREAM tasks use Electron IPC)."
                    else -> ""
                }
                QuestState(quest = q, runState = rs, progress = q.secondsDone, log = log)
            })
            claimedStates.addAll(res.claimed.map { QuestState(it, RunState.DONE) })
            orbBalance = res.orbs
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(DC.GradTop, DC.GradMid, DC.GradBot)))) {
        Column(Modifier.fillMaxSize()) {

            Box(Modifier.fillMaxWidth()) {
                QuestVideoBackground(ctx)
                Column(
                    Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 52.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(38.dp).background(Color.Black.copy(0.45f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text("Discord Quests", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = DC.White)
                                if (orbBalance != null && orbBalance!! > 0)
                                    Text("${orbBalance} orbs", fontSize = 11.sp, color = DC.OrbViolet, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (!loading && fetchError == null && selectedTab == 0) {
                                val anyRunnable = activeStates.any { it.runState == RunState.IDLE || it.runState == RunState.NOT_ENROLLED }
                                if (anyRunnable && !completing) {
                                    Button(
                                        onClick = {
                                            completing = true
                                            CoroutineScope(Dispatchers.IO).launch {
                                                activeStates.forEachIndexed { idx, st ->
                                                    if (st.runState == RunState.IDLE || st.runState == RunState.NOT_ENROLLED) {
                                                        completeQuest(token, st) { updated ->
                                                            if (idx < activeStates.size) activeStates[idx] = updated
                                                        }
                                                    }
                                                }
                                                withContext(Dispatchers.Main) { completing = false }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DC.Primary.copy(0.85f)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(34.dp)
                                    ) {
                                        Icon(Icons.Outlined.DoneAll, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(Modifier.width(5.dp))
                                        Text("Complete All", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                    }
                                }
                            }
                            IconButton(
                                onClick = { refreshKey++ },
                                modifier = Modifier.size(34.dp).background(Color.Black.copy(0.45f), RoundedCornerShape(10.dp))
                            ) {
                                Icon(Icons.Outlined.Refresh, null, tint = DC.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            if (!loading && fetchError == null) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = Color.Transparent,
                    contentColor     = DC.Primary,
                    divider          = { HorizontalDivider(color = DC.Border.copy(0.5f)) }
                ) {
                    listOf("Active (${activeStates.size})", "Claimed (${claimedStates.size})").forEachIndexed { idx, label ->
                        Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }) {
                            Text(
                                label,
                                modifier   = Modifier.padding(vertical = 12.dp),
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
                    states = activeStates, isClaimed = false,
                    empty  = "No active quests" to "No incomplete quests right now.",
                    token  = token
                ) { updated ->
                    val i = activeStates.indexOfFirst { it.quest.id == updated.quest.id }
                    if (i >= 0) activeStates[i] = updated
                }
                else -> QuestListPane(
                    states = claimedStates, isClaimed = true,
                    empty  = "No claimed quests" to "Completed quests appear here.",
                    token  = token, onUpdate = {}
                )
            }
        }
    }
}

@Composable
private fun QuestVideoBackground(ctx: Context) {
    val videoUrl  = "https://github.com/RhyanXG7/host-de-imagens/raw/refs/heads/BetterStar/imagens-Host/Novo%20projeto%203%20%5B6ED5A22%5D.mp4"
    val cacheFile = remember { File(ctx.cacheDir, "quest_bg2.mp4") }

    Box(Modifier.fillMaxWidth().height(200.dp)) {
        AndroidView(
            factory = { context ->
                SurfaceView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    holder.addCallback(object : SurfaceHolder.Callback {
                        private var mp: MediaPlayer? = null
                        override fun surfaceCreated(h: SurfaceHolder) {
                            val player = MediaPlayer()
                            mp = player
                            try {
                                if (cacheFile.exists() && cacheFile.length() > 0) {
                                    player.setDataSource(cacheFile.absolutePath)
                                } else {
                                    player.setDataSource(videoUrl)
                                }
                                player.setDisplay(h)
                                player.isLooping = true
                                player.setVolume(0f, 0f)
                                player.setOnPreparedListener { p ->
                                    p.start()
                                    if (!cacheFile.exists() || cacheFile.length() == 0L) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val resp = OkHttpClient().newCall(
                                                    Request.Builder().url(videoUrl).build()
                                                ).execute()
                                                resp.body?.byteStream()?.use { input ->
                                                    cacheFile.outputStream().use { output -> input.copyTo(output) }
                                                }
                                            } catch (_: Exception) {}
                                        }
                                    }
                                }
                                player.prepareAsync()
                            } catch (_: Exception) {}
                        }
                        override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hi: Int) {}
                        override fun surfaceDestroyed(h: SurfaceHolder) { mp?.release(); mp = null }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Black.copy(alpha = 0.25f),
                        0.6f to Color.Black.copy(alpha = 0.45f),
                        1.0f to DC.Bg
                    )
                )
            )
        )
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
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Spacer(Modifier.height(4.dp)) }
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

    val accentColor = accentForType(q.rewardType, isClaimed)

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
    val pulseA by pulseT.animateFloat(0.15f, 0.55f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pa")
    val borderAlpha = if (state.runState == RunState.RUNNING) pulseA else 0.18f

    Card(
        colors   = CardDefaults.cardColors(containerColor = DC.Card),
        shape    = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(borderAlpha), RoundedCornerShape(18.dp))
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))) {
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
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DC.Card), startY = 40f)))
                if (state.runState == RunState.RUNNING) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, accentColor.copy(0.3f), Color.Transparent),
                        startX = shimmerX, endX = shimmerX + 350f
                    )))
                }
                Box(
                    Modifier.align(Alignment.TopEnd).padding(10.dp)
                        .background(accentColor.copy(0.9f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    val rIcon: ImageVector = when (q.rewardType) {
                        "orbs"  -> Icons.Outlined.Stars
                        "decor" -> Icons.Outlined.Palette
                        "nitro" -> Icons.Outlined.Diamond
                        else    -> Icons.Outlined.CardGiftcard
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(if (isClaimed) Icons.Outlined.CheckCircle else rIcon, null, tint = Color.White, modifier = Modifier.size(9.dp))
                        Text(
                            if (isClaimed) "CLAIMED" else q.reward.take(16).uppercase(),
                            fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                if (!q.appName.isNullOrEmpty()) {
                    Box(
                        Modifier.align(Alignment.BottomStart).padding(10.dp)
                            .background(Color.Black.copy(0.6f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(q.appName, fontSize = 9.sp, color = DC.White.copy(0.9f), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(Modifier.padding(14.dp, 10.dp, 14.dp, 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Text(q.name, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DC.White, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (q.reward.isNotBlank()) {
                    Text(q.reward, fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    val tIcon: ImageVector = when {
                        q.taskName.contains("WATCH")  -> Icons.Outlined.Videocam
                        q.taskName.contains("PLAY")   -> Icons.Outlined.SportsEsports
                        q.taskName.contains("STREAM") -> Icons.Outlined.Cast
                        else                          -> Icons.Outlined.Schedule
                    }
                    MiniChip(tIcon, q.taskName.replace("_"," "), accentColor.copy(0.8f))
                    MiniChip(Icons.Outlined.HourglassEmpty, timeLeft, if (isClaimed) DC.Teal else accentColor)
                    if (q.enrolledAt == null && !isClaimed) MiniChip(Icons.Outlined.ErrorOutline, "NOT ENROLLED", DC.Error)
                    if (q.completedAt != null && q.claimedAt == null && !isClaimed) MiniChip(Icons.Outlined.CheckCircle, "COMPLETED", DC.Success)
                    if (state.runState == RunState.DESKTOP_ONLY && !isClaimed) MiniChip(Icons.Outlined.Computer, "DESKTOP ONLY", DC.Warning)
                }

                if (isClaimed && q.claimedAt != null) {
                    val fmtd = fmtDate(q.claimedAt)
                    if (fmtd.isNotEmpty()) Text("Claimed $fmtd", fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                }

                val curProgress = state.progress.coerceAtLeast(q.secondsDone)
                if (!isClaimed && q.secondsNeeded > 0) {
                    val frac = (curProgress.toFloat() / q.secondsNeeded).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", fontSize = 9.sp, color = DC.Muted, fontWeight = FontWeight.SemiBold)
                            Text("${(frac * 100).toInt()}%", fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(DC.Border)) {
                            Box(Modifier.fillMaxHeight().fillMaxWidth(frac).background(
                                Brush.horizontalGradient(listOf(accentColor.copy(0.6f), accentColor)), RoundedCornerShape(3.dp)
                            ))
                        }
                        Text("${curProgress}s / ${q.secondsNeeded}s", fontSize = 8.sp, color = DC.Muted.copy(0.7f), fontFamily = FontFamily.Monospace)
                    }
                }

                if (!isClaimed && state.log.isNotBlank()) {
                    val logBg    = when (state.runState) {
                        RunState.ERROR        -> DC.Error.copy(0.1f)
                        RunState.DONE         -> accentColor.copy(0.1f)
                        RunState.NOT_ENROLLED -> DC.Warning.copy(0.1f)
                        RunState.DESKTOP_ONLY -> DC.Warning.copy(0.08f)
                        else                  -> DC.CardAlt
                    }
                    val logColor = when (state.runState) {
                        RunState.ERROR        -> DC.Error
                        RunState.DONE         -> accentColor
                        RunState.NOT_ENROLLED -> DC.Warning
                        RunState.DESKTOP_ONLY -> DC.Warning
                        else                  -> DC.Muted
                    }
                    Text(state.log, fontSize = 10.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 15.sp,
                        modifier = Modifier.fillMaxWidth().background(logBg, RoundedCornerShape(8.dp)).padding(9.dp, 7.dp))
                }

                when {
                    isClaimed -> ClaimedActionRow(q, ctx, accentColor)
                    state.runState == RunState.DESKTOP_ONLY -> DesktopOnlyRow(q, ctx, accentColor)
                    state.runState == RunState.DONE && q.completedAt != null && q.claimedAt == null ->
                        ClaimActionRow(accentColor, q, ctx) {
                            CoroutineScope(Dispatchers.IO).launch {
                                completeQuest(token, state, onStateChange)
                            }
                        }
                    state.runState == RunState.DONE -> DoneActionRow(q, ctx, accentColor)
                    state.runState == RunState.RUNNING -> RunningRow(accentColor, shimmerX, state.log)
                    state.runState == RunState.NOT_ENROLLED ->
                        QuestActionRow(
                            mainLabel   = "Enroll & Complete",
                            mainIcon    = Icons.Outlined.AddCircleOutline,
                            accentColor = accentColor,
                            quest       = q,
                            ctx         = ctx,
                            onMain      = { CoroutineScope(Dispatchers.IO).launch { completeQuest(token, state.copy(runState = RunState.IDLE, log = ""), onStateChange) } }
                        )
                    else ->
                        QuestActionRow(
                            mainLabel   = "Auto Complete",
                            mainIcon    = Icons.Outlined.PlayArrow,
                            accentColor = accentColor,
                            quest       = q,
                            ctx         = ctx,
                            shimmerX    = shimmerX,
                            onMain      = { CoroutineScope(Dispatchers.IO).launch { completeQuest(token, state, onStateChange) } }
                        )
                }
            }
        }
    }
}

@Composable
private fun QuestActionRow(
    mainLabel: String,
    mainIcon: ImageVector,
    accentColor: Color,
    quest: QuestItem,
    ctx: Context,
    shimmerX: Float = 0f,
    onMain: () -> Unit
) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(0.8f))))
                .drawWithContent {
                    drawContent()
                    drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.18f), Color.Transparent), startX = shimmerX, endX = shimmerX + 240f), size = size)
                }
                .clickable(onClick = onMain),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(mainIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text(mainLabel, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp)
            }
        }
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", quest.name)) },
            modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
        }
        if (quest.questLink != null) {
            IconButton(
                onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(quest.questLink)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                },
                modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.OpenInBrowser, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun ClaimActionRow(accentColor: Color, quest: QuestItem, ctx: Context, onClaim: () -> Unit) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(0.8f))))
                .clickable(onClick = onClaim),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.CardGiftcard, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Text("Claim Reward", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 13.sp)
            }
        }
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", quest.name)) },
            modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
        }
        IconButton(
            onClick = {
                try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/channels/@me")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
            },
            modifier = Modifier.size(46.dp).background(accentColor.copy(0.15f), RoundedCornerShape(12.dp)).border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.OpenInBrowser, null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DesktopOnlyRow(quest: QuestItem, ctx: Context, accentColor: Color) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(DC.Warning.copy(0.08f)).border(1.dp, DC.Warning.copy(0.25f), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.Computer, null, tint = DC.Warning, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Desktop App Only", fontSize = 12.sp, color = DC.Warning, fontWeight = FontWeight.SemiBold)
        }
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", quest.name)) },
            modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
        }
        if (quest.questLink != null) {
            IconButton(
                onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(quest.questLink)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                },
                modifier = Modifier.size(46.dp).background(accentColor.copy(0.12f), RoundedCornerShape(12.dp)).border(1.dp, accentColor.copy(0.25f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.OpenInBrowser, null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun RunningRow(color: Color, shimmerX: Float, log: String) {
    val pulseT = rememberInfiniteTransition(label = "rp")
    val alpha  by pulseT.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "rpa")
    Box(
        Modifier.fillMaxWidth().height(46.dp).clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(alpha), RoundedCornerShape(12.dp))
            .drawWithContent {
                drawContent()
                drawRect(brush = Brush.horizontalGradient(listOf(Color.Transparent, color.copy(0.25f), Color.Transparent), startX = shimmerX, endX = shimmerX + 240f), size = size)
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(15.dp), color = color, strokeWidth = 2.dp)
            val display = log.trim().lines().lastOrNull()?.take(36) ?: "Running..."
            Text(display, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun DoneActionRow(q: QuestItem, ctx: Context, accentColor: Color) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(0.12f)).border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = accentColor, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Completed!", fontWeight = FontWeight.ExtraBold, color = accentColor, fontSize = 13.sp)
        }
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", q.name)) },
            modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
        }
        IconButton(
            onClick = {
                try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/channels/@me")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
            },
            modifier = Modifier.size(46.dp).background(accentColor.copy(0.15f), RoundedCornerShape(12.dp)).border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.OpenInBrowser, null, tint = accentColor, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ClaimedActionRow(q: QuestItem, ctx: Context, accentColor: Color) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp))
                .background(accentColor.copy(0.1f)).border(1.dp, accentColor.copy(0.2f), RoundedCornerShape(12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = accentColor, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Reward Claimed", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
        }
        IconButton(
            onClick = { clipboard.setPrimaryClip(ClipData.newPlainText("quest", q.name)) },
            modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
        ) {
            Icon(Icons.Outlined.ContentCopy, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
        }
        if (q.questLink != null) {
            IconButton(
                onClick = {
                    try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(q.questLink)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                },
                modifier = Modifier.size(46.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Outlined.OpenInBrowser, null, tint = DC.Muted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun MiniChip(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier.background(color.copy(0.1f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(10.dp))
        Text(text, fontSize = 8.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CenteredSpinner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val inf = rememberInfiniteTransition(label = "si")
            val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "sr")
            Box(Modifier.size(48.dp).rotate(rot).border(2.5.dp, Brush.sweepGradient(listOf(DC.Primary, Color.Transparent)), CircleShape))
            Text("Loading quests...", color = DC.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).background(DC.Error.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = DC.Error, modifier = Modifier.size(32.dp))
            }
            Text("Failed to load quests", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text(msg, color = DC.Muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(7.dp))
                Text("Try Again", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}
