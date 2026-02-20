package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

// ─────────────────── HTTP CLIENT ───────────────────
private val httpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

// ─────────────────── COLORS ───────────────────
private object DC {
    val Bg         = Color(0xFF0E0F13)
    val Surface    = Color(0xFF17181C)
    val Card       = Color(0xFF1E1F26)
    val Border     = Color(0xFF2C2D35)
    val Primary    = Color(0xFF5865F2)
    val PrimaryGlow= Color(0xFF7B87FF)
    val Success    = Color(0xFF23A55A)
    val Warning    = Color(0xFFFAA61A)
    val Error      = Color(0xFFED4245)
    val White      = Color(0xFFF2F3F5)
    val Muted      = Color(0xFF72767D)
    val OrbPurple  = Color(0xFF9B59B6)
    val OrbViolet  = Color(0xFFB675F0)
    val Gold       = Color(0xFFFFD700)
    val Teal       = Color(0xFF43B581)
}

private val SUPER_PROPS =
    "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ3LjApIEdlY2tvLzE0Ny4wIEZpcmVmb3gvMTQ3LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDcuMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tL2xvZ2luP3JlZGlyZWN0X3RvPSUyRnF1ZXN0LWhvbWUiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiJkaXNjb3JkLmNvbSIsInJlbGVhc2VfY2hhbm5lbCI6InN0YWJsZSIsImNsaWVudF9idWlsZF9udW1iZXIiOjQ5OTEyMywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0="

private fun discordHeaders(token: String): Map<String, String> = mapOf(
    "Authorization"      to token,
    "Content-Type"       to "application/json",
    "User-Agent"         to "Mozilla/5.0 (Android 12; Mobile; rv:147.0) Gecko/147.0 Firefox/147.0",
    "X-Super-Properties" to SUPER_PROPS,
    "X-Discord-Locale"   to "pt-BR",
    "X-Discord-Timezone" to "America/Sao_Paulo",
    "X-Debug-Options"    to "bugReporterEnabled",
    "Referer"            to "https://discord.com/quest-home"
)

private fun buildReq(url: String, token: String) = Request.Builder().url(url).apply {
    discordHeaders(token).forEach { (k, v) -> header(k, v) }
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

// ─────────────────── BANNER URL BUILDER ───────────────────
// Discord CDN for quest assets: https://cdn.discordapp.com/quests/{questId}/{hash}.ext
// For app assets: https://cdn.discordapp.com/app-assets/{appId}/store/header.jpg
private fun buildBannerUrl(questId: String, config: JSONObject): String? {
    val assets = config.optJSONObject("assets") ?: return null
    // Prefer quest_bar_hero (small), then hero, then logotype
    val candidates = listOf("quest_bar_hero", "hero", "logotype", "game_tile")
    for (key in candidates) {
        val v = assets.optString(key, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
        return when {
            v.startsWith("http")   -> v
            v.startsWith("quests/") -> "https://cdn.discordapp.com/$v"
            v.contains("/")        -> "https://cdn.discordapp.com/quests/$v"
            // bare hash — build full path
            else -> "https://cdn.discordapp.com/quests/$questId/$v"
        }
    }
    val appId = config.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
    if (appId != null) return "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg"
    return null
}

// ─────────────────── DATA MODELS ───────────────────
data class QuestItem(
    val id: String,
    val name: String,
    val reward: String,
    val rewardType: String,       // "orbs" | "decor" | "nitro" | "prize"
    val expiresDisplay: String,
    val expiresMs: Long,
    val taskName: String,         // "WATCH_VIDEO" | "WATCH_VIDEO_ON_MOBILE" | "PLAY_ON_DESKTOP" | "PLAY_ACTIVITY" | "STREAM_ON_DESKTOP"
    val secondsNeeded: Long,
    val secondsDone: Long,
    val appName: String?,
    val bannerUrl: String?,
    // user status
    val enrolledAt: String?,      // ISO string, null = not enrolled (need to enroll first!)
    val completedAt: String?,
    val claimedAt: String?,
    val configVersion: Int,
    val appId: String?
)

enum class RunState { IDLE, RUNNING, DONE, ERROR, NOT_ENROLLED }

data class QuestState(
    val quest: QuestItem,
    var runState: RunState = RunState.IDLE,
    var progress: Long = 0L,
    var log: String = ""
)

// ─────────────────── PARSER ───────────────────
private fun parseQuest(q: JSONObject): QuestItem? {
    val id     = q.optString("id").takeIf { it.isNotEmpty() } ?: return null
    val config = q.optJSONObject("config") ?: return null
    val us     = q.optJSONObject("user_status")

    val expiresAt = config.optString("expires_at", "")
    val expiresMs = parseIso(expiresAt)
    val expiresDisplay = try {
        SimpleDateFormat("MMM dd", Locale.US).format(Date(expiresMs))
    } catch (_: Exception) { "?" }

    // Task resolution — try both taskConfig and task_config and taskConfigV2 and task_config_v2
    val taskConfig = config.optJSONObject("task_config")
        ?: config.optJSONObject("taskConfig")
        ?: config.optJSONObject("task_config_v2")
        ?: config.optJSONObject("taskConfigV2")
        ?: return null
    val tasks = taskConfig.optJSONObject("tasks") ?: return null

    val SUPPORTED = listOf(
        "WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO",
        "PLAY_ON_DESKTOP", "PLAY_ACTIVITY", "STREAM_ON_DESKTOP"
    )
    val taskName = SUPPORTED.firstOrNull { tasks.has(it) } ?: return null
    val taskObj  = tasks.optJSONObject(taskName) ?: return null
    val secondsNeeded = taskObj.optLong("target", 0L)
    val secondsDone   = us?.optJSONObject("progress")
        ?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

    // App info
    val app    = config.optJSONObject("application")
    val appId  = app?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
    val appName= app?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }

    // Quest name
    val msgs  = config.optJSONObject("messages")
    val qName = msgs?.optString("quest_name")?.takeIf { it.isNotEmpty() }
        ?: appName ?: "Quest"

    // Reward — try rewards_config (active quests) and rewards (claimed quests structure)
    val rewardStr: String
    val rewardsConfig = config.optJSONObject("rewards_config")
    val rewardsArr    = rewardsConfig?.optJSONArray("rewards") ?: config.optJSONArray("rewards")
    val firstReward   = rewardsArr?.optJSONObject(0)
    rewardStr = firstReward?.optJSONObject("messages")?.optString("name")
        ?.takeIf { it.isNotEmpty() }
        ?: firstReward?.optString("name")?.takeIf { it.isNotEmpty() }
        ?: ""

    val rewardType = when {
        rewardStr.contains("orb", ignoreCase = true)        -> "orbs"
        rewardStr.contains("decoration", ignoreCase = true)
            || rewardStr.contains("decor", ignoreCase = true) -> "decor"
        rewardStr.contains("nitro", ignoreCase = true)      -> "nitro"
        else -> "prize"
    }

    val configVersion = config.optInt("config_version", 2)
    val bannerUrl     = buildBannerUrl(id, config)

    val enrolledAt  = us?.optString("enrolled_at")?.takeIf { it.isNotEmpty() && it != "null" }
    val completedAt = us?.optString("completed_at")?.takeIf { it != "null" && it.isNotEmpty() }
    val claimedAt   = us?.optString("claimed_at")?.takeIf { it != "null" && it.isNotEmpty() }

    return QuestItem(
        id             = id,
        name           = qName,
        reward         = rewardStr,
        rewardType     = rewardType,
        expiresDisplay = expiresDisplay,
        expiresMs      = expiresMs,
        taskName       = taskName,
        secondsNeeded  = secondsNeeded,
        secondsDone    = secondsDone,
        appName        = appName,
        bannerUrl      = bannerUrl,
        enrolledAt     = enrolledAt,
        completedAt    = completedAt,
        claimedAt      = claimedAt,
        configVersion  = configVersion,
        appId          = appId
    )
}

// ─────────────────── FETCH ───────────────────
data class FetchResult(
    val active: List<QuestItem>,
    val claimed: List<QuestItem>,
    val orbs: Int?
)

private suspend fun fetchAll(token: String): FetchResult = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()

    // 1. Active quests
    val activeResp = httpClient.newCall(
        buildReq("https://discord.com/api/v9/quests/@me", token).build()
    ).execute()
    val activeBody = activeResp.body?.string() ?: ""
    if (!activeResp.isSuccessful) {
        val msg = try { JSONObject(activeBody).optString("message", "HTTP ${activeResp.code}") }
                  catch (_: Exception) { "HTTP ${activeResp.code}" }
        throw Exception(msg)
    }
    val activeArr = try { JSONObject(activeBody).optJSONArray("quests") ?: JSONArray() }
                   catch (_: Exception) { JSONArray() }

    val active = mutableListOf<QuestItem>()
    for (i in 0 until activeArr.length()) {
        val item = parseQuest(activeArr.getJSONObject(i)) ?: continue
        if (item.expiresMs > 0 && item.expiresMs < now) continue  // skip expired
        if (item.claimedAt != null) continue                        // skip already claimed
        active.add(item)
    }

    // 2. Claimed quests
    val claimedResp = httpClient.newCall(
        buildReq("https://discord.com/api/v9/quests/@me/claimed", token).build()
    ).execute()
    val claimedBody = claimedResp.body?.string() ?: ""
    val claimedArr  = try { JSONObject(claimedBody).optJSONArray("quests") ?: JSONArray() }
                     catch (_: Exception) { JSONArray() }
    val claimed = mutableListOf<QuestItem>()
    for (i in 0 until claimedArr.length()) {
        val item = parseQuest(claimedArr.getJSONObject(i)) ?: continue
        claimed.add(item)
    }

    // 3. Orb balance
    val orbResp = httpClient.newCall(
        buildReq("https://discord.com/api/v9/users/@me/virtual-currency/balance", token).build()
    ).execute()
    val orbBody = orbResp.body?.string() ?: ""
    val orbs = try { JSONObject(orbBody).optInt("balance", -1).takeIf { it >= 0 } }
               catch (_: Exception) { null }

    FetchResult(active, claimed, orbs)
}

// ─────────────────── AUTO-ENROLL ───────────────────
private suspend fun enrollQuest(token: String, questId: String): Boolean = withContext(Dispatchers.IO) {
    // POST to enroll — try platform 0 (mobile)
    val body = JSONObject().apply { put("platform", 0) }.toString()
        .toRequestBody("application/json".toMediaType())
    val resp = httpClient.newCall(
        buildReq("https://discord.com/api/v9/quests/$questId/enroll", token).post(body).build()
    ).execute()
    resp.isSuccessful
}

// ─────────────────── COMPLETER ───────────────────
private suspend fun completeQuest(
    token: String,
    state: QuestState,
    onUpdate: (QuestState) -> Unit
) {
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
        // ── Check enrollment ──────────────────────────────────────────
        var enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Not enrolled yet, attempting to enroll...")
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val ok = enrollQuest(token, questId)
            if (!ok) {
                // Try fetching fresh status
                val statusResp = httpClient.newCall(
                    buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
                ).execute()
                val statusBody = statusResp.body?.string() ?: "{}"
                enrolledAt = try { JSONObject(statusBody).optString("enrolled_at", "").takeIf { it.isNotEmpty() && it != "null" } }
                             catch (_: Exception) { null }
                if (enrolledAt == null) {
                    upd("⚠ Quest not enrolled. Please open Discord and click Accept on this quest first.", prog = 0, rs = RunState.NOT_ENROLLED)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    return
                }
            } else {
                // Fetch enrolledAt from fresh status
                delay(800)
                val statusResp = httpClient.newCall(
                    buildReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
                ).execute()
                val statusBody = statusResp.body?.string() ?: "{}"
                enrolledAt = try { JSONObject(statusBody).optString("enrolled_at", "").takeIf { it.isNotEmpty() && it != "null" } }
                             catch (_: Exception) { null }
                    ?: (System.currentTimeMillis() - 10_000L).toString()  // fallback
            }
        }

        when (taskName) {
            // ── VIDEO QUESTS ──────────────────────────────────────────────
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                val enrollMs   = parseIso(enrolledAt).takeIf { it > 0L } ?: (System.currentTimeMillis() - 60_000L)
                val maxFuture  = 10L   // seconds we can be ahead of real time
                val step       = 7L    // seconds per tick (matches aamiaa's proven script)
                val intervalMs = 1000L // 1s delay between ticks

                upd("Spoofing video watch for: ${q.name}")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                var completed = false
                while (!completed) {
                    val elapsedReal = (System.currentTimeMillis() - enrollMs) / 1000L
                    val maxAllowed  = elapsedReal + maxFuture
                    val diff        = maxAllowed - secondsDone

                    if (diff >= step) {
                        val ts  = minOf(secondsNeeded, secondsDone + step).toDouble() + Math.random()
                        val body = JSONObject().apply { put("timestamp", ts) }.toString()
                            .toRequestBody("application/json".toMediaType())

                        val resp     = httpClient.newCall(
                            buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token)
                                .post(body).build()
                        ).execute()
                        val respBody = resp.body?.string() ?: "{}"

                        completed    = try { val o = JSONObject(respBody); o.has("completed_at") && !o.isNull("completed_at") }
                                       catch (_: Exception) { false }
                        secondsDone  = minOf(secondsNeeded, secondsDone + step)
                        upd("📺 Video progress: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                    } else {
                        upd("⏳ Waiting for time window... (${secondsDone}s / ${secondsNeeded}s)", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                    }

                    if (secondsDone >= secondsNeeded) break
                    delay(intervalMs)
                }

                // Final flush if not yet confirmed complete
                if (!completed) {
                    val finalBody = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }.toString()
                        .toRequestBody("application/json".toMediaType())
                    httpClient.newCall(
                        buildReq("https://discord.com/api/v9/quests/$questId/video-progress", token)
                            .post(finalBody).build()
                    ).execute()
                }

                upd("✅ Quest completed! You can now claim the reward.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            // ── PLAY ON DESKTOP (heartbeat via spoofed RPC game) ─────────
            "PLAY_ON_DESKTOP" -> {
                // For external HTTP calls we can only do the heartbeat approach.
                // The actual RunningGameStore spoof only works inside Discord's renderer.
                // We'll do heartbeats and track progress via QUESTS_SEND_HEARTBEAT_SUCCESS equivalent.
                // The heartbeat endpoint accepts stream_key in format "call:{channelId}:1"
                val dmResp = httpClient.newCall(
                    buildReq("https://discord.com/api/v9/users/@me/channels", token).build()
                ).execute()
                val channelId = try {
                    val arr = JSONArray(dmResp.body?.string() ?: "[]")
                    if (arr.length() > 0) arr.getJSONObject(0).optString("id") else null
                } catch (_: Exception) { null }
                    ?: throw Exception("No DM channels found. Open at least one DM in Discord.")

                val streamKey = "call:$channelId:1"
                upd("🎮 Spoofing game play... (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (secondsDone < secondsNeeded) {
                    val reqBody = JSONObject().apply {
                        put("stream_key", streamKey)
                        put("terminal", false)
                    }.toString().toRequestBody("application/json".toMediaType())

                    val resp    = httpClient.newCall(
                        buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                            .post(reqBody).build()
                    ).execute()
                    val respStr = resp.body?.string() ?: "{}"

                    // Read progress from response
                    val newProgress = try {
                        val o  = JSONObject(respStr)
                        val pr = o.optJSONObject("progress")
                        when {
                            q.configVersion == 1 -> o.optLong("stream_progress_seconds", secondsDone)
                            pr != null -> pr.optJSONObject(taskName)?.optLong("value", secondsDone) ?: secondsDone
                            else -> secondsDone
                        }
                    } catch (_: Exception) { secondsDone }

                    secondsDone = newProgress
                    upd("🎮 Game progress: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0, (secondsNeeded - secondsDone) / 60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (secondsDone >= secondsNeeded) break
                    delay(20_000L) // 20s heartbeat interval
                }

                // Terminal heartbeat
                val termBody = JSONObject().apply {
                    put("stream_key", streamKey)
                    put("terminal", true)
                }.toString().toRequestBody("application/json".toMediaType())
                httpClient.newCall(
                    buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                        .post(termBody).build()
                ).execute()

                upd("✅ Quest completed! You can now claim the reward.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            // ── PLAY ACTIVITY ──────────────────────────────────────────────
            "PLAY_ACTIVITY" -> {
                val dmResp = httpClient.newCall(
                    buildReq("https://discord.com/api/v9/users/@me/channels", token).build()
                ).execute()
                val channelId = try {
                    val arr = JSONArray(dmResp.body?.string() ?: "[]")
                    if (arr.length() > 0) arr.getJSONObject(0).optString("id") else null
                } catch (_: Exception) { null }
                    ?: throw Exception("No DM channels found. Open at least one DM in Discord.")

                val streamKey = "call:$channelId:1"
                upd("🕹️ Spoofing activity... (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (true) {
                    val reqBody = JSONObject().apply {
                        put("stream_key", streamKey)
                        put("terminal", false)
                    }.toString().toRequestBody("application/json".toMediaType())

                    val resp    = httpClient.newCall(
                        buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                            .post(reqBody).build()
                    ).execute()
                    val respStr = resp.body?.string() ?: "{}"

                    val newProgress = try {
                        JSONObject(respStr).optJSONObject("progress")
                            ?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                    } catch (_: Exception) { secondsDone }

                    secondsDone = newProgress
                    upd("🕹️ Activity progress: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (secondsDone >= secondsNeeded) {
                        val termBody = JSONObject().apply {
                            put("stream_key", streamKey)
                            put("terminal", true)
                        }.toString().toRequestBody("application/json".toMediaType())
                        httpClient.newCall(
                            buildReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                                .post(termBody).build()
                        ).execute()
                        break
                    }
                    delay(20_000L)
                }

                upd("✅ Quest completed! You can now claim the reward.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            // ── STREAM ON DESKTOP ──────────────────────────────────────────
            "STREAM_ON_DESKTOP" -> {
                // Stream quests require being inside Discord's Electron renderer for the
                // ApplicationStreamingStore spoof. Via external HTTP we can try heartbeats,
                // but they usually fail for this task type.
                upd("⚠ STREAM_ON_DESKTOP quests require the Discord desktop app.\n" +
                    "This task cannot be completed via external API calls.\n" +
                    "Open Discord desktop → join a VC → start streaming any window.", prog = 0, rs = RunState.ERROR)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            else -> {
                upd("⚠ Task type '$taskName' is not supported.", prog = 0, rs = RunState.ERROR)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
        }
    } catch (e: Exception) {
        upd("❌ Error: ${e.message}", rs = RunState.ERROR)
        withContext(Dispatchers.Main) { onUpdate(cur) }
    }
}

// ─────────────────── ACTIVITY ───────────────────
class QuestActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        private const val PREFS_NAME  = "quest_prefs"
        private const val KEY_TOS_ACK = "tos_ack"

        fun start(ctx: Context, token: String) {
            ctx.startActivity(Intent(ctx, QuestActivity::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
            })
        }
    }

    private fun prefs(): SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary    = DC.Primary,
                    background = DC.Bg,
                    surface    = DC.Surface
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = DC.Bg) {
                    QuestRoot(token = token, prefs = prefs(), onBack = { finish() })
                }
            }
        }
    }
}

// ─────────────────── ROOT ───────────────────
@Composable
private fun QuestRoot(token: String, prefs: SharedPreferences, onBack: () -> Unit) {
    var tosAcked by remember { mutableStateOf(prefs.getBoolean("tos_ack", false)) }

    if (!tosAcked) {
        TosDialog(
            onAccept = {
                prefs.edit().putBoolean("tos_ack", true).apply()
                tosAcked = true
            },
            onDecline = onBack
        )
        return
    }
    QuestScreen(token = token, onBack = onBack)
}

// ─────────────────── TOS DIALOG ───────────────────
@Composable
private fun TosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline) {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(DC.Card)
                .border(1.dp, DC.Primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(28.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier.size(44.dp).background(DC.Warning.copy(0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Warning, null, tint = DC.Warning, modifier = Modifier.size(24.dp))
                    }
                    Text("Terms of Service Warning", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White)
                }
                HorizontalDivider(color = DC.Border)
                Text(
                    "⚠️ This tool automates Discord Quest completion via the official API.\n\n" +
                    "Using third-party automation tools may violate Discord's Terms of Service " +
                    "(Section 9: Automated Use). Your account could potentially be suspended or " +
                    "permanently banned.\n\n" +
                    "This tool is provided for educational purposes only. " +
                    "Use at your own risk. The developers are not responsible for any " +
                    "consequences resulting from use of this software.",
                    fontSize = 13.sp,
                    color = DC.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DC.Error.copy(0.6f))
                    ) {
                        Text("Decline", color = DC.Error, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DC.Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I Understand", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ─────────────────── MAIN SCREEN ───────────────────
@Composable
private fun QuestScreen(token: String, onBack: () -> Unit) {
    var loading     by remember { mutableStateOf(true) }
    var fetchError  by remember { mutableStateOf<String?>(null) }
    var orbBalance  by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshKey  by remember { mutableIntStateOf(0) }

    val activeStates  = remember { androidx.compose.runtime.mutableStateListOf<QuestState>() }
    val claimedStates = remember { androidx.compose.runtime.mutableStateListOf<QuestState>() }

    LaunchedEffect(refreshKey) {
        loading = true; fetchError = null
        activeStates.clear(); claimedStates.clear(); orbBalance = null
        try {
            val res = fetchAll(token)
            activeStates.addAll(res.active.map { q ->
                QuestState(
                    quest = q,
                    runState = when {
                        q.enrolledAt == null -> RunState.NOT_ENROLLED
                        else                 -> RunState.IDLE
                    },
                    progress = q.secondsDone
                )
            })
            claimedStates.addAll(res.claimed.map { QuestState(it, RunState.DONE) })
            orbBalance = res.orbs
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    Column(Modifier.fillMaxSize().background(DC.Bg)) {
        // ─ Top bar
        QuestTopBar(orbBalance = orbBalance, onBack = onBack, onRefresh = { refreshKey++ })
        HorizontalDivider(color = DC.Border)

        if (!loading && fetchError == null) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = DC.Surface,
                contentColor     = DC.Primary,
                divider          = { HorizontalDivider(color = DC.Border) }
            ) {
                listOf("Active (${activeStates.size})", "Claimed (${claimedStates.size})").forEachIndexed { idx, label ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }) {
                        Text(
                            label,
                            modifier   = Modifier.padding(vertical = 12.dp),
                            fontSize   = 13.sp,
                            fontWeight = if (selectedTab == idx) FontWeight.ExtraBold else FontWeight.Normal,
                            color      = if (selectedTab == idx) DC.Primary else DC.Muted
                        )
                    }
                }
            }
        }

        when {
            loading    -> CenteredSpinner()
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

// ─────────────────── TOP BAR ───────────────────
@Composable
private fun QuestTopBar(orbBalance: Int?, onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(36.dp).background(DC.Primary.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = DC.PrimaryGlow, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Quest Completer", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DC.White)
                Text("Auto-complete Discord Quests", fontSize = 11.sp, color = DC.Muted)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (orbBalance != null) {
                OrbBadge(orbBalance)
            }
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.Refresh, null, tint = DC.Primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun OrbBadge(count: Int) {
    val infiniteT = rememberInfiniteTransition(label = "orb")
    val glowAlpha by infiniteT.animateFloat(0.4f, 1f,
        infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ga")

    Box(
        Modifier
            .background(DC.OrbPurple.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .border(1.dp, DC.OrbViolet.copy(alpha = glowAlpha * 0.6f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = DC.OrbViolet.copy(alpha = glowAlpha), modifier = Modifier.size(14.dp))
            Text("$count Orbs", fontSize = 12.sp, color = DC.OrbViolet, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ─────────────────── LIST PANE ───────────────────
@Composable
private fun QuestListPane(
    states: List<QuestState>,
    isClaimed: Boolean,
    empty: Pair<String, String>,
    token: String,
    onUpdate: (QuestState) -> Unit
) {
    if (states.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                Box(Modifier.size(64.dp).background(DC.Success.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.TaskAlt, null, tint = DC.Success, modifier = Modifier.size(32.dp))
                }
                Text(empty.first, color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(empty.second, color = DC.Muted, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }
            items(states, key = { it.quest.id }) { state ->
                QuestCard(state, isClaimed, token, onUpdate)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─────────────────── QUEST CARD ───────────────────
@Composable
private fun QuestCard(
    state: QuestState,
    isClaimed: Boolean,
    token: String,
    onStateChange: (QuestState) -> Unit
) {
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
            if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
            else add(GifDecoder.Factory())
        }.build()
    }

    val timeLeft = remember(q.expiresMs, isClaimed) {
        if (isClaimed) return@remember "Claimed"
        val diff  = q.expiresMs - System.currentTimeMillis()
        val hours = diff / 3_600_000L
        when {
            diff  < 0  -> "Expired"
            hours > 48 -> "${hours / 24}d left"
            hours > 0  -> "${hours}h left"
            else       -> "< 1h left"
        }
    }

    // Shimmer animation for RUNNING state
    val shimmerT = rememberInfiniteTransition(label = "sh")
    val shimmerX by shimmerT.animateFloat(
        -300f, 1000f,
        infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "sx"
    )

    Card(
        colors   = CardDefaults.cardColors(containerColor = DC.Card),
        shape    = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
    ) {
        Column {
            // ── Banner
            Box(
                Modifier.fillMaxWidth().height(130.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            ) {
                if (q.bannerUrl != null) {
                    AsyncImage(
                        model              = ImageRequest.Builder(ctx).data(q.bannerUrl).crossfade(true).build(),
                        contentDescription = null,
                        imageLoader        = gifLoader,
                        modifier           = Modifier.fillMaxSize(),
                        contentScale       = ContentScale.Crop
                    )
                } else {
                    Box(
                        Modifier.fillMaxSize()
                            .background(Brush.linearGradient(
                                listOf(accentColor.copy(0.3f), DC.Surface),
                                start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                            ))
                    )
                }
                // Gradient overlay
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, DC.Card), startY = 40f)
                ))
                // Shimmer sweep when running (right → left glow)
                if (state.runState == RunState.RUNNING) {
                    Box(Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, accentColor.copy(0.3f), Color.Transparent),
                            startX = shimmerX, endX = shimmerX + 300f
                        )
                    ))
                }
                // Reward type badge
                RewardBadge(Modifier.align(Alignment.TopEnd).padding(12.dp), q.rewardType, q.reward, accentColor, isClaimed)
            }

            // ── Body
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Title + reward
                Text(q.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DC.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (q.reward.isNotBlank()) {
                    Text(q.reward, fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }

                // Chips row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    MiniChip(taskIcon(q.taskName), q.taskName.replace("_", " "), DC.Muted)
                    MiniChip(Icons.Outlined.HourglassEmpty, timeLeft, if (isClaimed) DC.Teal else DC.Warning)
                    if (!q.appName.isNullOrEmpty()) MiniChip(Icons.Outlined.SportsEsports, q.appName, DC.Primary)
                    if (q.enrolledAt == null && !isClaimed) MiniChip(Icons.Outlined.ErrorOutline, "NOT ENROLLED", DC.Error)
                }

                // Claimed date
                if (isClaimed && q.claimedAt != null) {
                    val fmtd = fmtDate(q.claimedAt)
                    if (fmtd.isNotEmpty())
                        Text("Claimed $fmtd", fontSize = 10.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                }

                // Progress bar
                val curProgress = state.progress.coerceAtLeast(q.secondsDone)
                if (!isClaimed && q.secondsNeeded > 0 && curProgress > 0) {
                    val frac = (curProgress.toFloat() / q.secondsNeeded).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.SemiBold)
                            Text("${(frac * 100).toInt()}%", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.ExtraBold)
                        }
                        LinearProgressIndicator(
                            progress   = { frac },
                            modifier   = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                            color      = accentColor,
                            trackColor = DC.Border,
                            strokeCap  = StrokeCap.Round
                        )
                        Text("${curProgress}s / ${q.secondsNeeded}s", fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                    }
                }

                // Log box
                if (!isClaimed && state.log.isNotBlank()) {
                    val logBg    = when (state.runState) { RunState.ERROR -> DC.Error.copy(0.08f); RunState.DONE -> DC.Success.copy(0.08f); RunState.NOT_ENROLLED -> DC.Warning.copy(0.08f); else -> DC.Border.copy(0.4f) }
                    val logColor = when (state.runState) { RunState.ERROR -> DC.Error; RunState.DONE -> DC.Success; RunState.NOT_ENROLLED -> DC.Warning; else -> DC.Muted }
                    Box(Modifier.fillMaxWidth().background(logBg, RoundedCornerShape(10.dp)).padding(10.dp, 8.dp)) {
                        Text(state.log, fontSize = 11.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                    }
                }

                // CTA
                when {
                    isClaimed -> ClaimedRow()
                    state.runState == RunState.DONE -> DoneRow()
                    state.runState == RunState.RUNNING -> RunningRow(accentColor, shimmerX)
                    state.runState == RunState.NOT_ENROLLED ->
                        NotEnrolledButton(accentColor) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val attempt = state.copy(runState = RunState.IDLE, log = "")
                                completeQuest(token, attempt, onStateChange)
                            }
                        }
                    else -> AutoCompleteButton(accentColor, shimmerX) {
                        CoroutineScope(Dispatchers.IO).launch {
                            completeQuest(token, state, onStateChange)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────── REWARD BADGE ───────────────────
@Composable
private fun RewardBadge(modifier: Modifier, rewardType: String, reward: String, color: Color, isClaimed: Boolean) {
    val label = when {
        isClaimed          -> "✓ CLAIMED"
        rewardType == "orbs"  -> "⭐ ORBS"
        rewardType == "decor" -> "🎨 DECOR"
        rewardType == "nitro" -> "💜 NITRO"
        else -> reward.take(18).uppercase()
    }
    Box(
        modifier.background(color.copy(0.85f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}

// ─────────────────── MINI CHIP ───────────────────
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

private fun taskIcon(taskName: String): ImageVector = when {
    taskName.contains("WATCH")  -> Icons.Outlined.Videocam
    taskName.contains("PLAY")   -> Icons.Outlined.SportsEsports
    taskName.contains("STREAM") -> Icons.Outlined.Cast
    else                        -> Icons.Outlined.Schedule
}

// ─────────────────── ACTION BUTTONS ───────────────────
@Composable
private fun AutoCompleteButton(color: Color, shimmerX: Float, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.25f), Color.Transparent),
                        startX = shimmerX, endX = shimmerX + 250f
                    ),
                    size = size
                )
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
private fun NotEnrolledButton(color: Color, onClick: () -> Unit) {
    Button(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        colors   = ButtonDefaults.buttonColors(containerColor = DC.Warning),
        shape    = RoundedCornerShape(14.dp)
    ) {
        Icon(Icons.Outlined.AddCircleOutline, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Enroll & Complete", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
    }
}

@Composable
private fun RunningRow(color: Color, shimmerX: Float) {
    val pulseT = rememberInfiniteTransition(label = "p")
    val alpha  by pulseT.animateFloat(0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pa")
    Box(
        Modifier.fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.15f))
            .border(1.dp, color.copy(alpha), RoundedCornerShape(14.dp))
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, color.copy(0.4f), Color.Transparent),
                        startX = shimmerX, endX = shimmerX + 250f
                    ),
                    size = size
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = color.copy(alpha), strokeWidth = 2.5.dp)
            Text("Running...", fontWeight = FontWeight.ExtraBold, color = color, fontSize = 15.sp)
        }
    }
}

@Composable
private fun DoneRow() {
    Row(
        Modifier.fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DC.Success.copy(0.12f))
            .border(1.dp, DC.Success.copy(0.35f), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = DC.Success, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Quest Completed! Claim it in Discord.", fontWeight = FontWeight.ExtraBold, color = DC.Success, fontSize = 14.sp)
    }
}

@Composable
private fun ClaimedRow() {
    Row(
        Modifier.fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DC.Teal.copy(0.1f))
            .border(1.dp, DC.Teal.copy(0.25f), RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = DC.Teal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Reward already claimed", fontSize = 13.sp, color = DC.Teal, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────── UTILS ───────────────────
@Composable
private fun CenteredSpinner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = DC.Primary, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            Text("Loading quests...", color = DC.Muted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun ErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).background(DC.Error.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = DC.Error, modifier = Modifier.size(32.dp))
            }
            Text("Failed to load quests", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text(msg, color = DC.Muted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
