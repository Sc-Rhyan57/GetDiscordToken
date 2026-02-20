package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Cast
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.outlined.Warning
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
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.min

private object QC {
    val Bg            = Color(0xFF1E1F22)
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
    val OnPrimary     = Color(0xFFFFFFFF)
    val OrbViolet     = Color(0xFFB675F0)
    val OrbPurple     = Color(0xFF9B59B6)
    val Gold          = Color(0xFFFFD700)

    val R_Small  = 8.dp
    val R_Medium = 12.dp
    val R_Large  = 16.dp
    val R_XLarge = 20.dp
    val R_Card   = 18.dp
    val R_Button = 14.dp
    val R_Badge  = 20.dp
}

private data class QuestLog(val timestamp: String, val level: String, val tag: String, val message: String, val detail: String? = null)

private val questLogs       = mutableStateListOf<QuestLog>()
private val questLogEnabled = mutableStateOf(true)

private fun qLog(level: String, tag: String, message: String, detail: String? = null) {
    if (!questLogEnabled.value) return
    val ts = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US).format(java.util.Date())
    questLogs.add(0, QuestLog(ts, level, tag, message, detail))
    if (questLogs.size > 300) questLogs.removeAt(questLogs.size - 1)
}

private val questHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private val QUEST_SUPER_PROPS =
    "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ3LjApIEdlY2tvLzE0Ny4wIEZpcmVmb3gvMTQ3LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDcuMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tL2xvZ2luP3JlZGlyZWN0X3RvPSUyRnF1ZXN0LWhvbWUiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiJkaXNjb3JkLmNvbSIsInJlbGVhc2VfY2hhbm5lbCI6InN0YWJsZSIsImNsaWVudF9idWlsZF9udW1iZXIiOjQ5OTEyMywiY2xpZW50X2V2ZW50X3NvdXJjZSI6bnVsbH0="

private fun questHeaders(token: String): Map<String, String> = mapOf(
    "Authorization"      to token,
    "Content-Type"       to "application/json",
    "User-Agent"         to "Mozilla/5.0 (Android 12; Mobile; rv:147.0) Gecko/147.0 Firefox/147.0",
    "X-Super-Properties" to QUEST_SUPER_PROPS,
    "X-Discord-Locale"   to "pt-BR",
    "X-Discord-Timezone" to "America/Sao_Paulo",
    "X-Debug-Options"    to "bugReporterEnabled",
    "Referer"            to "https://discord.com/quest-home"
)

private fun questReq(url: String, token: String) = Request.Builder().url(url).apply {
    questHeaders(token).forEach { (k, v) -> header(k, v) }
}

private val questIsoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}

private fun parseQuestIso(s: String?): Long {
    if (s.isNullOrEmpty() || s == "null") return 0L
    return try { questIsoFmt.parse(s.substringBefore('.'))?.time ?: 0L } catch (_: Exception) { 0L }
}

private fun fmtQuestDate(s: String?): String {
    val ms = parseQuestIso(s)
    if (ms == 0L) return ""
    return try { SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date(ms)) } catch (_: Exception) { "" }
}

private fun buildQuestBannerUrl(questId: String, config: JSONObject): String? {
    val assets = config.optJSONObject("assets") ?: return null
    val candidates = listOf("quest_bar_hero", "hero", "logotype", "game_tile")
    for (key in candidates) {
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
    val bannerUrl: String?,
    val enrolledAt: String?,
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

private fun parseQuestItem(q: JSONObject): QuestItem? {
    val id     = q.optString("id").takeIf { it.isNotEmpty() } ?: return null
    val config = q.optJSONObject("config") ?: return null
    val us     = q.optJSONObject("user_status")

    val expiresAt      = config.optString("expires_at", "")
    val expiresMs      = parseQuestIso(expiresAt)
    val expiresDisplay = try { SimpleDateFormat("MMM dd", Locale.US).format(Date(expiresMs)) } catch (_: Exception) { "?" }

    val taskConfig = config.optJSONObject("task_config")
        ?: config.optJSONObject("taskConfig")
        ?: config.optJSONObject("task_config_v2")
        ?: config.optJSONObject("taskConfigV2")
        ?: return null
    val tasks = taskConfig.optJSONObject("tasks") ?: return null

    val SUPPORTED = listOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ON_DESKTOP", "PLAY_ACTIVITY", "STREAM_ON_DESKTOP")
    val taskName      = SUPPORTED.firstOrNull { tasks.has(it) } ?: return null
    val taskObj       = tasks.optJSONObject(taskName) ?: return null
    val secondsNeeded = taskObj.optLong("target", 0L)
    val secondsDone   = us?.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

    val app     = config.optJSONObject("application")
    val appId   = app?.optString("id")?.takeIf  { it.isNotEmpty() && it != "null" }
    val appName = app?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" }

    val msgs  = config.optJSONObject("messages")
    val qName = msgs?.optString("quest_name")?.takeIf { it.isNotEmpty() }
        ?: appName ?: "Quest"

    val rewardsConfig = config.optJSONObject("rewards_config")
    val rewardsArr    = rewardsConfig?.optJSONArray("rewards") ?: config.optJSONArray("rewards")
    val firstReward   = rewardsArr?.optJSONObject(0)
    val rewardStr     = firstReward?.optJSONObject("messages")?.optString("name")?.takeIf { it.isNotEmpty() }
        ?: firstReward?.optString("name")?.takeIf { it.isNotEmpty() }
        ?: ""

    val rewardType = when {
        rewardStr.contains("orb",        ignoreCase = true) -> "orbs"
        rewardStr.contains("decoration", ignoreCase = true)
            || rewardStr.contains("decor", ignoreCase = true) -> "decor"
        rewardStr.contains("nitro",      ignoreCase = true) -> "nitro"
        else -> "prize"
    }

    val configVersion = config.optInt("config_version", 2)
    val bannerUrl     = buildQuestBannerUrl(id, config)

    val enrolledAt  = us?.optString("enrolled_at")?.takeIf  { it.isNotEmpty() && it != "null" }
    val completedAt = us?.optString("completed_at")?.takeIf { it.isNotEmpty() && it != "null" }
    val claimedAt   = us?.optString("claimed_at")?.takeIf   { it.isNotEmpty() && it != "null" }

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

data class QuestFetchResult(
    val active: List<QuestItem>,
    val claimed: List<QuestItem>,
    val orbs: Int?
)

private suspend fun fetchAllQuests(token: String): QuestFetchResult = withContext(Dispatchers.IO) {
    val now = System.currentTimeMillis()
    qLog("INFO", "Quest", "Fetching active quests")

    val activeResp = questHttpClient.newCall(
        questReq("https://discord.com/api/v9/quests/@me", token).build()
    ).execute()
    val activeBody = activeResp.body?.string() ?: ""

    qLog(if (activeResp.isSuccessful) "SUCCESS" else "ERROR", "Quest", "Active quests HTTP ${activeResp.code}")

    if (!activeResp.isSuccessful) {
        val msg = try { JSONObject(activeBody).optString("message", "HTTP ${activeResp.code}") }
                  catch (_: Exception) { "HTTP ${activeResp.code}" }
        throw Exception(msg)
    }

    val activeArr = try { JSONObject(activeBody).optJSONArray("quests") ?: JSONArray() }
                   catch (_: Exception) { JSONArray() }

    val active = mutableListOf<QuestItem>()
    for (i in 0 until activeArr.length()) {
        val item = parseQuestItem(activeArr.getJSONObject(i)) ?: continue
        if (item.expiresMs > 0 && item.expiresMs < now) continue
        if (item.claimedAt != null) continue
        active.add(item)
    }
    qLog("SUCCESS", "Quest", "Active: ${active.size}")

    qLog("INFO", "Quest", "Fetching claimed quests")
    val claimedResp = questHttpClient.newCall(
        questReq("https://discord.com/api/v9/quests/@me/claimed", token).build()
    ).execute()
    val claimedBody = claimedResp.body?.string() ?: ""
    qLog(if (claimedResp.isSuccessful) "SUCCESS" else "WARN", "Quest", "Claimed quests HTTP ${claimedResp.code}")

    val claimedArr = try {
        JSONObject(claimedBody).optJSONArray("quests") ?: JSONArray()
    } catch (_: Exception) { JSONArray() }

    val claimed = mutableListOf<QuestItem>()
    for (i in 0 until claimedArr.length()) {
        val item = parseQuestItem(claimedArr.getJSONObject(i)) ?: continue
        claimed.add(item)
    }
    qLog("SUCCESS", "Quest", "Claimed: ${claimed.size}")

    qLog("INFO", "Quest", "Fetching orb balance")
    val orbResp = questHttpClient.newCall(
        questReq("https://discord.com/api/v9/users/@me/virtual-currency/balance", token).build()
    ).execute()
    val orbBody = orbResp.body?.string() ?: ""
    val orbs = try { JSONObject(orbBody).optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }
    if (orbs != null) qLog("SUCCESS", "Quest", "Orbs: $orbs") else qLog("WARN", "Quest", "Orbs not available")

    QuestFetchResult(active, claimed, orbs)
}

private suspend fun enrollInQuest(token: String, questId: String): Boolean = withContext(Dispatchers.IO) {
    qLog("INFO", "Quest", "Enrolling in quest $questId")
    val body = JSONObject().apply { put("platform", 0) }.toString().toRequestBody("application/json".toMediaType())
    val resp = questHttpClient.newCall(
        questReq("https://discord.com/api/v9/quests/$questId/enroll", token).post(body).build()
    ).execute()
    val ok = resp.isSuccessful
    qLog(if (ok) "SUCCESS" else "ERROR", "Quest", "Enroll HTTP ${resp.code}")
    ok
}

private suspend fun completeQuestTask(
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
        qLog(when (rs) { RunState.ERROR -> "ERROR"; RunState.DONE -> "SUCCESS"; else -> "INFO" }, "Quest", log)
    }

    try {
        var enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Not enrolled. Attempting auto-enroll...")
            withContext(Dispatchers.Main) { onUpdate(cur) }

            val ok = enrollInQuest(token, questId)
            if (ok) {
                delay(800)
                val statusResp = questHttpClient.newCall(
                    questReq("https://discord.com/api/v9/users/@me/quests/$questId", token).build()
                ).execute()
                val statusBody = statusResp.body?.string() ?: "{}"
                enrolledAt = try { JSONObject(statusBody).optString("enrolled_at", "").takeIf { it.isNotEmpty() && it != "null" } } catch (_: Exception) { null }
                    ?: (System.currentTimeMillis() - 10_000L).toString()
                qLog("SUCCESS", "Quest", "Enrolled at $enrolledAt")
            } else {
                upd("Quest not enrolled. Open Discord → Quests → Accept quest first.", prog = 0, rs = RunState.NOT_ENROLLED)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                return
            }
        }

        when (taskName) {
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                val enrollMs  = parseQuestIso(enrolledAt).takeIf { it > 0L } ?: (System.currentTimeMillis() - 60_000L)
                val maxFuture = 10
                val speed     = 7
                val interval  = 1

                upd("Spoofing video watch for: ${q.name}")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                var completed = false
                while (true) {
                    val maxAllowed = ((System.currentTimeMillis() - enrollMs) / 1000).toInt() + maxFuture
                    val diff       = maxAllowed - secondsDone
                    val timestamp  = secondsDone + speed

                    if (diff >= speed) {
                        val sendTs = minOf(secondsNeeded.toDouble(), timestamp.toDouble() + Math.random())
                        val body = JSONObject().apply { put("timestamp", sendTs) }.toString()
                            .toRequestBody("application/json".toMediaType())

                        val resp     = questHttpClient.newCall(
                            questReq("https://discord.com/api/v9/quests/$questId/video-progress", token)
                                .post(body).build()
                        ).execute()
                        val respBody = resp.body?.string() ?: "{}"
                        qLog("INFO", "Quest", "video-progress HTTP ${resp.code} | $respBody")

                        completed   = try { JSONObject(respBody).optString("completed_at", "").isNotEmpty() } catch (_: Exception) { false }
                        secondsDone = minOf(secondsNeeded, timestamp)
                        upd("Video progress: ${secondsDone}s / ${secondsNeeded}s", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                    }

                    if (timestamp >= secondsNeeded) break
                    delay(interval * 1000L)
                }

                if (!completed) {
                    val finalBody = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }.toString()
                        .toRequestBody("application/json".toMediaType())
                    val finalResp = questHttpClient.newCall(
                        questReq("https://discord.com/api/v9/quests/$questId/video-progress", token)
                            .post(finalBody).build()
                    ).execute()
                    qLog("INFO", "Quest", "Final video flush HTTP ${finalResp.code}")
                }

                upd("Quest completed! Claim reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ACTIVITY" -> {
                val dmResp = questHttpClient.newCall(
                    questReq("https://discord.com/api/v9/users/@me/channels", token).build()
                ).execute()
                qLog("INFO", "Quest", "DM channels HTTP ${dmResp.code}")

                val channelId = try {
                    val arr = JSONArray(dmResp.body?.string() ?: "[]")
                    if (arr.length() > 0) arr.getJSONObject(0).optString("id") else null
                } catch (_: Exception) { null }
                    ?: throw Exception("No DM channels found. Open at least one DM in Discord.")

                val streamKey = "call:$channelId:1"
                qLog("INFO", "Quest", "Using streamKey: $streamKey")
                upd("Spoofing activity play (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (true) {
                    val reqBody = JSONObject().apply {
                        put("stream_key", streamKey)
                        put("terminal", false)
                    }.toString().toRequestBody("application/json".toMediaType())

                    val resp    = questHttpClient.newCall(
                        questReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                            .post(reqBody).build()
                    ).execute()
                    val respStr = resp.body?.string() ?: "{}"
                    qLog("INFO", "Quest", "heartbeat HTTP ${resp.code}")

                    val newProgress = try {
                        JSONObject(respStr).optJSONObject("progress")
                            ?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                    } catch (_: Exception) { secondsDone }

                    secondsDone = newProgress
                    upd("Activity progress: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0, (secondsNeeded - secondsDone) / 60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (secondsDone >= secondsNeeded) {
                        val termBody = JSONObject().apply {
                            put("stream_key", streamKey)
                            put("terminal", true)
                        }.toString().toRequestBody("application/json".toMediaType())
                        val termResp = questHttpClient.newCall(
                            questReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                                .post(termBody).build()
                        ).execute()
                        qLog("SUCCESS", "Quest", "Terminal heartbeat HTTP ${termResp.code}")
                        break
                    }
                    delay(20_000L)
                }

                upd("Quest completed! Claim reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ON_DESKTOP" -> {
                val dmResp = questHttpClient.newCall(
                    questReq("https://discord.com/api/v9/users/@me/channels", token).build()
                ).execute()
                val channelId = try {
                    val arr = JSONArray(dmResp.body?.string() ?: "[]")
                    if (arr.length() > 0) arr.getJSONObject(0).optString("id") else null
                } catch (_: Exception) { null }
                    ?: throw Exception("No DM channels found. Open at least one DM in Discord.")

                val streamKey = "call:$channelId:1"
                qLog("INFO", "Quest", "PLAY_ON_DESKTOP via heartbeat, streamKey=$streamKey")
                upd("Spoofing game play (~${(secondsNeeded - secondsDone) / 60} min remaining)")
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (secondsDone < secondsNeeded) {
                    val reqBody = JSONObject().apply {
                        put("stream_key", streamKey)
                        put("terminal", false)
                    }.toString().toRequestBody("application/json".toMediaType())

                    val resp    = questHttpClient.newCall(
                        questReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                            .post(reqBody).build()
                    ).execute()
                    val respStr = resp.body?.string() ?: "{}"
                    qLog("INFO", "Quest", "heartbeat HTTP ${resp.code}")

                    val newProgress = try {
                        val o  = JSONObject(respStr)
                        val pr = o.optJSONObject("progress")
                        when {
                            q.configVersion == 1 -> o.optLong("stream_progress_seconds", secondsDone)
                            pr != null           -> pr.optJSONObject(taskName)?.optLong("value", secondsDone) ?: secondsDone
                            else                 -> secondsDone
                        }
                    } catch (_: Exception) { secondsDone }

                    secondsDone = newProgress
                    upd("Game progress: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0, (secondsNeeded - secondsDone) / 60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (secondsDone >= secondsNeeded) break
                    delay(20_000L)
                }

                val termBody = JSONObject().apply {
                    put("stream_key", streamKey)
                    put("terminal", true)
                }.toString().toRequestBody("application/json".toMediaType())
                questHttpClient.newCall(
                    questReq("https://discord.com/api/v9/quests/$questId/heartbeat", token)
                        .post(termBody).build()
                ).execute()

                upd("Quest completed! Claim reward in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "STREAM_ON_DESKTOP" -> {
                upd(
                    "STREAM_ON_DESKTOP requires Discord desktop app.\n" +
                    "This task cannot be completed via external API.\n" +
                    "Open Discord desktop → join a VC → start streaming any window.",
                    prog = 0, rs = RunState.ERROR
                )
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            else -> {
                upd("Task type '$taskName' is not supported.", prog = 0, rs = RunState.ERROR)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
        }
    } catch (e: Exception) {
        qLog("ERROR", "Quest", "completeQuestTask: ${e.message}")
        upd("Error: ${e.message}", rs = RunState.ERROR)
        withContext(Dispatchers.Main) { onUpdate(cur) }
    }
}

class QuestActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        private const val PREFS_NAME  = "quest_prefs"

        fun start(ctx: Context, token: String) {
            ctx.startActivity(Intent(ctx, QuestActivity::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary    = QC.Primary,
                background = QC.Bg,
                surface    = QC.Surface
            )) {
                Surface(Modifier.fillMaxSize(), color = QC.Bg) {
                    QuestRoot(token = token, prefs = prefs, onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun QuestRoot(token: String, prefs: SharedPreferences, onBack: () -> Unit) {
    var tosAcked by remember { mutableStateOf(prefs.getBoolean("tos_ack", false)) }
    if (!tosAcked) {
        QuestTosDialog(
            onAccept  = { prefs.edit().putBoolean("tos_ack", true).apply(); tosAcked = true },
            onDecline = onBack
        )
        return
    }
    QuestMainScreen(token = token, onBack = onBack)
}

@Composable
private fun QuestTosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    val scale = remember { Animatable(0.92f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy)) }

    Box(
        Modifier.fillMaxSize().background(QC.Bg.copy(0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).scale(scale.value),
            colors   = CardDefaults.cardColors(containerColor = QC.Surface),
            shape    = RoundedCornerShape(QC.R_XLarge),
            border   = BorderStroke(1.dp, QC.Warning.copy(0.4f))
        ) {
            Column(Modifier.padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(44.dp).background(QC.Warning.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Warning, null, tint = QC.Warning, modifier = Modifier.size(24.dp))
                    }
                    Text("Terms of Service Warning", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = QC.TextPrimary)
                }
                HorizontalDivider(color = QC.Divider)
                Text(
                    "This tool automates Discord Quest completion via the official API.\n\n" +
                    "Using third-party automation may violate Discord's Terms of Service (Section 9). " +
                    "Your account could be suspended or permanently banned.\n\n" +
                    "This is for educational purposes only. Use at your own risk. " +
                    "Developers are not responsible for any consequences.",
                    fontSize = 13.sp, color = QC.TextSecondary, lineHeight = 20.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick  = onDecline,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(QC.R_Button),
                        border   = BorderStroke(1.dp, QC.Error.copy(0.6f))
                    ) { Text("Decline", color = QC.Error, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick  = onAccept,
                        modifier = Modifier.weight(1f),
                        colors   = ButtonDefaults.buttonColors(containerColor = QC.Primary),
                        shape    = RoundedCornerShape(QC.R_Button)
                    ) { Text("I Understand", fontWeight = FontWeight.ExtraBold, color = QC.OnPrimary) }
                }
            }
        }
    }
}

@Composable
private fun QuestMainScreen(token: String, onBack: () -> Unit) {
    var loading     by remember { mutableStateOf(true) }
    var fetchError  by remember { mutableStateOf<String?>(null) }
    var orbBalance  by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshKey  by remember { mutableIntStateOf(0) }
    var showLogs    by remember { mutableStateOf(false) }
    var logEnabled  by remember { mutableStateOf(true) }
    var footerTaps  by remember { mutableIntStateOf(0) }

    questLogEnabled.value = logEnabled

    val activeStates  = remember { mutableStateListOf<QuestState>() }
    val claimedStates = remember { mutableStateListOf<QuestState>() }

    LaunchedEffect(refreshKey) {
        loading = true; fetchError = null
        activeStates.clear(); claimedStates.clear(); orbBalance = null
        try {
            val res = fetchAllQuests(token)
            activeStates.addAll(res.active.map { q ->
                QuestState(
                    quest    = q,
                    runState = if (q.enrolledAt == null) RunState.NOT_ENROLLED else RunState.IDLE,
                    progress = q.secondsDone
                )
            })
            claimedStates.addAll(res.claimed.map { QuestState(it, RunState.DONE, it.secondsDone) })
            orbBalance = res.orbs
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    if (showLogs) {
        QuestLogsScreen(
            onClose    = { showLogs = false },
            logEnabled = logEnabled,
            onToggle   = { logEnabled = it; questLogEnabled.value = it }
        )
    }

    Column(Modifier.fillMaxSize().background(QC.Bg)) {
        QuestTopBar(
            orbBalance  = orbBalance,
            onBack      = onBack,
            onRefresh   = { refreshKey++ },
            onLogsClick = { footerTaps++; if (footerTaps >= 3) { showLogs = true; footerTaps = 0 } }
        )
        HorizontalDivider(color = QC.Divider)

        if (!loading && fetchError == null) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = QC.Surface,
                contentColor     = QC.Primary,
                divider          = { HorizontalDivider(color = QC.Divider) }
            ) {
                listOf("Active (${activeStates.size})", "Claimed (${claimedStates.size})").forEachIndexed { idx, label ->
                    Tab(selected = selectedTab == idx, onClick = { selectedTab = idx }) {
                        Text(
                            label,
                            modifier   = Modifier.padding(vertical = 12.dp),
                            fontSize   = 13.sp,
                            fontWeight = if (selectedTab == idx) FontWeight.ExtraBold else FontWeight.Normal,
                            color      = if (selectedTab == idx) QC.Primary else QC.TextMuted
                        )
                    }
                }
            }
        }

        when {
            loading        -> QuestSpinner()
            fetchError != null -> QuestErrorPane(fetchError!!) { refreshKey++ }
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
private fun QuestLogsScreen(onClose: () -> Unit, logEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(QC.Bg)) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().background(QC.Surface).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Terminal, null, tint = QC.Primary, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Quest Console", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = QC.TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.background(QC.Primary.copy(0.15f), RoundedCornerShape(QC.R_Badge)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text("${questLogs.size}", fontSize = 10.sp, color = QC.Primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Logging", fontSize = 11.sp, color = QC.TextMuted)
                        Spacer(Modifier.width(6.dp))
                        Switch(
                            checked        = logEnabled,
                            onCheckedChange = onToggle,
                            modifier       = Modifier.height(24.dp),
                            colors         = SwitchDefaults.colors(checkedThumbColor = QC.Primary, checkedTrackColor = QC.Primary.copy(0.3f))
                        )
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { questLogs.clear() }) { Text("Clear", color = QC.Error, fontSize = 11.sp) }
                        IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Outlined.Close, null, tint = QC.TextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                if (questLogs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Terminal, null, tint = QC.TextMuted, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No logs yet", color = QC.TextMuted, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                        items(questLogs) { log ->
                            var expanded by remember { mutableStateOf(false) }
                            val levelColor = when (log.level) {
                                "SUCCESS" -> QC.Success; "ERROR" -> QC.Error; "WARN" -> QC.Warning; else -> QC.Primary
                            }
                            Column(
                                Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    .background(QC.Surface.copy(0.5f), RoundedCornerShape(QC.R_Small))
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(log.timestamp, fontSize = 10.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace)
                                    Spacer(Modifier.width(5.dp))
                                    Box(Modifier.background(levelColor.copy(0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                        Text(log.level, fontSize = 9.sp, color = levelColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Spacer(Modifier.width(5.dp))
                                    Text("[${log.tag}]", fontSize = 10.sp, color = QC.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.width(4.dp))
                                    Text(log.message, fontSize = 11.sp, color = QC.TextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f), maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis)
                                    Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = QC.TextMuted, modifier = Modifier.size(14.dp))
                                }
                                if (expanded && log.detail != null) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(log.detail, fontSize = 10.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace, lineHeight = 14.sp, modifier = Modifier.padding(start = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestTopBar(orbBalance: Int?, onBack: () -> Unit, onRefresh: () -> Unit, onLogsClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(QC.Surface).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = QC.TextPrimary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(36.dp).background(QC.Primary.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.EmojiEvents, null, tint = QC.Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Quest Completer", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = QC.TextPrimary)
                Text("Auto-complete Discord Quests", fontSize = 11.sp, color = QC.TextMuted)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (orbBalance != null) { OrbsBadge(orbBalance, onLogsClick) }
            else { Box(Modifier.size(40.dp).clickable { onLogsClick() }, contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Terminal, null, tint = QC.TextMuted, modifier = Modifier.size(18.dp))
            }}
            IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.Refresh, null, tint = QC.Primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun OrbsBadge(count: Int, onClick: () -> Unit) {
    val infT     = rememberInfiniteTransition(label = "orb")
    val glowAlpha by infT.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "ga")
    Box(
        Modifier
            .background(Color(0xFF9B59B6).copy(0.18f), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFB675F0).copy(glowAlpha * 0.6f), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Outlined.Star, null, tint = Color(0xFFB675F0).copy(glowAlpha), modifier = Modifier.size(14.dp))
            Text("$count Orbs", fontSize = 12.sp, color = Color(0xFFB675F0), fontWeight = FontWeight.ExtraBold)
        }
    }
}

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier            = Modifier.padding(32.dp)
            ) {
                Box(Modifier.size(64.dp).background(QC.Success.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.TaskAlt, null, tint = QC.Success, modifier = Modifier.size(32.dp))
                }
                Text(empty.first,  color = QC.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(empty.second, color = QC.TextMuted,    fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }
            items(states, key = { it.quest.id }) { state ->
                QuestCard(state = state, isClaimed = isClaimed, token = token, onStateChange = onUpdate)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun QuestCard(state: QuestState, isClaimed: Boolean, token: String, onStateChange: (QuestState) -> Unit) {
    val ctx = LocalContext.current
    val q   = state.quest

    val accentColor = if (isClaimed) QC.Success else when (q.rewardType) {
        "orbs"  -> Color(0xFFB675F0)
        "decor" -> QC.Success
        "nitro" -> QC.Primary
        else    -> Color(0xFFFFD700)
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

    val shimmerT = rememberInfiniteTransition(label = "sh")
    val shimmerX by shimmerT.animateFloat(-300f, 1000f, infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart), label = "sx")
    val cardScale = remember { Animatable(0.97f) }
    LaunchedEffect(Unit) { cardScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy)) }

    Card(
        colors   = CardDefaults.cardColors(containerColor = QC.SurfaceVar),
        shape    = RoundedCornerShape(QC.R_Card),
        modifier = Modifier.fillMaxWidth().scale(cardScale.value)
            .border(1.dp, accentColor.copy(0.22f), RoundedCornerShape(QC.R_Card))
    ) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(130.dp)
                    .clip(RoundedCornerShape(topStart = QC.R_Card, topEnd = QC.R_Card))
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
                    Box(Modifier.fillMaxSize().background(
                        Brush.linearGradient(
                            listOf(accentColor.copy(0.30f), QC.SurfaceVar),
                            start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    ))
                }
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, QC.SurfaceVar), startY = 40f)
                ))
                if (state.runState == RunState.RUNNING) {
                    Box(Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, accentColor.copy(0.3f), Color.Transparent),
                            startX = shimmerX, endX = shimmerX + 300f
                        )
                    ))
                }
                QuestRewardBadge(
                    modifier    = Modifier.align(Alignment.TopEnd).padding(12.dp),
                    rewardType  = q.rewardType,
                    reward      = q.reward,
                    color       = accentColor,
                    isClaimed   = isClaimed
                )
            }

            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(q.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = QC.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)

                if (q.reward.isNotBlank()) {
                    Text(q.reward, fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.SemiBold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    QuestMiniChip(questTaskIcon(q.taskName), q.taskName.replace("_", " "), QC.TextMuted)
                    QuestMiniChip(Icons.Outlined.HourglassEmpty, timeLeft, if (isClaimed) QC.Success else QC.Warning)
                    if (!q.appName.isNullOrEmpty()) QuestMiniChip(Icons.Outlined.SportsEsports, q.appName, QC.Primary)
                    if (q.enrolledAt == null && !isClaimed) QuestMiniChip(Icons.Outlined.ErrorOutline, "NOT ENROLLED", QC.Error)
                }

                if (isClaimed && q.claimedAt != null) {
                    val fmtd = fmtQuestDate(q.claimedAt)
                    if (fmtd.isNotEmpty())
                        Text("Claimed $fmtd", fontSize = 10.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace)
                }

                val curProgress = state.progress.coerceAtLeast(q.secondsDone)
                if (!isClaimed && q.secondsNeeded > 0 && curProgress > 0) {
                    val frac = (curProgress.toFloat() / q.secondsNeeded).coerceIn(0f, 1f)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Progress", fontSize = 10.sp, color = QC.TextMuted, fontWeight = FontWeight.SemiBold)
                            Text("${(frac * 100).toInt()}%", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.ExtraBold)
                        }
                        LinearProgressIndicator(
                            progress   = { frac },
                            modifier   = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(4.dp)),
                            color      = accentColor,
                            trackColor = QC.Divider,
                            strokeCap  = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Text("${curProgress}s / ${q.secondsNeeded}s", fontSize = 9.sp, color = QC.TextMuted, fontFamily = FontFamily.Monospace)
                    }
                }

                if (!isClaimed && state.log.isNotBlank()) {
                    val logBg    = when (state.runState) {
                        RunState.ERROR        -> QC.Error.copy(0.08f)
                        RunState.DONE         -> QC.Success.copy(0.08f)
                        RunState.NOT_ENROLLED -> QC.Warning.copy(0.08f)
                        else                  -> QC.Divider.copy(0.4f)
                    }
                    val logColor = when (state.runState) {
                        RunState.ERROR        -> QC.Error
                        RunState.DONE         -> QC.Success
                        RunState.NOT_ENROLLED -> QC.Warning
                        else                  -> QC.TextMuted
                    }
                    Box(
                        Modifier.fillMaxWidth()
                            .background(logBg, RoundedCornerShape(QC.R_Medium))
                            .border(1.dp, logColor.copy(0.2f), RoundedCornerShape(QC.R_Medium))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Text(state.log, fontSize = 11.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 16.sp)
                    }
                }

                when {
                    isClaimed -> QuestClaimedRow()
                    state.runState == RunState.DONE -> QuestDoneRow()
                    state.runState == RunState.RUNNING -> QuestRunningRow(accentColor, shimmerX)
                    state.runState == RunState.NOT_ENROLLED ->
                        QuestNotEnrolledButton(accentColor) {
                            CoroutineScope(Dispatchers.IO).launch {
                                completeQuestTask(token, state.copy(runState = RunState.IDLE, log = ""), onStateChange)
                            }
                        }
                    else ->
                        QuestAutoCompleteButton(accentColor, shimmerX) {
                            CoroutineScope(Dispatchers.IO).launch {
                                completeQuestTask(token, state, onStateChange)
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun QuestRewardBadge(modifier: Modifier, rewardType: String, reward: String, color: Color, isClaimed: Boolean) {
    val label = when {
        isClaimed              -> "✓ CLAIMED"
        rewardType == "orbs"   -> "★ ORBS"
        rewardType == "decor"  -> "DECOR"
        rewardType == "nitro"  -> "NITRO"
        else -> reward.take(18).uppercase()
    }
    Box(
        modifier
            .background(color.copy(0.88f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun QuestMiniChip(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier
            .background(color.copy(0.10f), RoundedCornerShape(20.dp))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(11.dp))
        Text(text, fontSize = 9.sp, color = color, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

private fun questTaskIcon(taskName: String): ImageVector = when {
    taskName.contains("WATCH")  -> Icons.Outlined.Videocam
    taskName.contains("PLAY")   -> Icons.Outlined.SportsEsports
    taskName.contains("STREAM") -> Icons.Outlined.Cast
    else                        -> Icons.Outlined.Schedule
}

@Composable
private fun QuestAutoCompleteButton(color: Color, shimmerX: Float, onClick: () -> Unit) {
    val glow by animateFloatAsState(1f, tween(500), label = "glow")
    Box(
        Modifier.fillMaxWidth().height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .graphicsLayer { shadowElevation = 24f; shape = RoundedCornerShape(QC.R_Button); clip = false }
                .background(color.copy(0.35f), RoundedCornerShape(QC.R_Button + 2.dp))
        )
        Box(
            Modifier.fillMaxWidth().height(52.dp)
                .clip(RoundedCornerShape(QC.R_Button))
                .background(Brush.verticalGradient(listOf(color.copy(1f), color.copy(0.82f))))
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(0.22f), Color.Transparent),
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
}

@Composable
private fun QuestNotEnrolledButton(color: Color, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().height(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.fillMaxWidth().height(56.dp)
                .graphicsLayer { shadowElevation = 18f; shape = RoundedCornerShape(QC.R_Button); clip = false }
                .background(QC.Warning.copy(0.28f), RoundedCornerShape(QC.R_Button + 2.dp))
        )
        Box(
            Modifier.fillMaxWidth().height(52.dp)
                .clip(RoundedCornerShape(QC.R_Button))
                .background(Brush.verticalGradient(listOf(QC.Warning, QC.Warning.copy(0.85f))))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.AddCircleOutline, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                Text("Enroll & Complete", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun QuestRunningRow(color: Color, shimmerX: Float) {
    val pulseT = rememberInfiniteTransition(label = "p")
    val alpha  by pulseT.animateFloat(0.6f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pa")
    Box(
        Modifier.fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(QC.R_Button))
            .background(color.copy(0.14f))
            .border(1.dp, color.copy(alpha), RoundedCornerShape(QC.R_Button))
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, color.copy(0.35f), Color.Transparent),
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
private fun QuestDoneRow() {
    Row(
        Modifier.fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(QC.R_Button))
            .background(QC.Success.copy(0.12f))
            .border(1.dp, QC.Success.copy(0.35f), RoundedCornerShape(QC.R_Button)),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = QC.Success, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Quest Completed! Claim in Discord.", fontWeight = FontWeight.ExtraBold, color = QC.Success, fontSize = 14.sp)
    }
}

@Composable
private fun QuestClaimedRow() {
    Row(
        Modifier.fillMaxWidth().height(52.dp)
            .clip(RoundedCornerShape(QC.R_Button))
            .background(QC.Success.copy(0.08f))
            .border(1.dp, QC.Success.copy(0.22f), RoundedCornerShape(QC.R_Button)),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.CheckCircle, null, tint = QC.Success, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Reward already claimed", fontSize = 13.sp, color = QC.Success, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun QuestSpinner() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = QC.Primary, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
            Text("Loading quests...", color = QC.TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun QuestErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier            = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.size(64.dp).background(QC.Error.copy(0.12f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.ErrorOutline, null, tint = QC.Error, modifier = Modifier.size(32.dp))
            }
            Text("Failed to load quests", color = QC.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text(msg, color = QC.TextMuted, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = QC.Primary),
                shape   = RoundedCornerShape(QC.R_Button)
            ) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Try Again", fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
