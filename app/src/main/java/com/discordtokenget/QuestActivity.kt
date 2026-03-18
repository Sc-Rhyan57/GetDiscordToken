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
import android.media.MediaPlayer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
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

private val http = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private object DC {
    val Bg        = Color(0xFF0E0F13)
    val Surface   = Color(0xFF17181C)
    val Card      = Color(0xFF1E1F26)
    val CardAlt   = Color(0xFF22232B)
    val Border    = Color(0xFF2C2D35)
    val Primary   = Color(0xFF5865F2)
    val Success   = Color(0xFF23A55A)
    val Warning   = Color(0xFFFAA61A)
    val Error     = Color(0xFFED4245)
    val White     = Color(0xFFF2F3F5)
    val SubText   = Color(0xFFB5BAC1)
    val Muted     = Color(0xFF72767D)
    val OrbViolet = Color(0xFFB675F0)
    val Teal      = Color(0xFF43B581)
}

private val SUPER_PROPS =
    "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTI7IE1vYmlsZTsgcnY6MTQ4LjApIEdlY2tvLzE0OC4wIEZpcmVmb3gvMTQ4LjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNDguMCIsIm9zX3ZlcnNpb24iOiIxMiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiIiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiIiLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1MTA3MzMsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"

private fun req(url: String, token: String) = Request.Builder().url(url).apply {
    header("Authorization",      token)
    header("Content-Type",       "application/json")
    header("User-Agent",         "Mozilla/5.0 (Android 12; Mobile; rv:148.0) Gecko/148.0 Firefox/148.0")
    header("X-Super-Properties", SUPER_PROPS)
    header("X-Discord-Locale",   "pt-BR")
    header("X-Discord-Timezone", "America/Sao_Paulo")
    header("X-Debug-Options",    "bugReporterEnabled")
    header("Referer",            "https://discord.com/quest-home")
}

private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}
private fun parseIso(s: String?): Long =
    if (s.isNullOrEmpty() || s == "null") 0L
    else try { isoFmt.parse(s.substringBefore('.'))?.time ?: 0L } catch (_: Exception) { 0L }

private fun fmtExpiry(ms: Long): String {
    val diff = ms - System.currentTimeMillis()
    val h    = diff / 3_600_000L
    return when { diff < 0 -> "Expired"; h > 48 -> "${h/24}d"; h > 0 -> "${h}h"; else -> "< 1h" }
}
private fun fmtShort(ms: Long): String =
    try { SimpleDateFormat("d/M", Locale.getDefault()).format(Date(ms)) } catch (_: Exception) { "" }

private fun bannerUrl(id: String, cfg: JSONObject): String? {
    val a = cfg.optJSONObject("assets") ?: return null
    for (k in listOf("quest_bar_hero", "hero", "logotype", "game_tile")) {
        val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
        if (v.contains(".mp4") || v.contains(".m3u8") || v.contains(".webm")) continue
        return if (v.startsWith("http")) v
               else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v"
               else "https://cdn.discordapp.com/quests/$id/$v"
    }
    val appId = cfg.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
    return if (appId != null) "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg" else null
}

private fun rewardIconUrl(id: String, cfg: JSONObject): String? {
    val a = cfg.optJSONObject("assets")
    if (a != null) {
        for (k in listOf("reward_generic_android", "reward", "collectible_preview", "quest_reward", "reward_tile", "game_tile")) {
            val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
            return if (v.startsWith("http")) v else "https://cdn.discordapp.com/quests/$id/$v"
        }
    }
    val skuId = cfg.optJSONObject("rewards_config")?.optJSONArray("rewards")
        ?.optJSONObject(0)?.optString("sku_id")?.takeIf { it.isNotEmpty() && it != "null" }
    return if (skuId != null) "https://cdn.discordapp.com/collectibles-assets/$skuId/icon.png?size=80" else null
}

private fun videoUrl(id: String, cfg: JSONObject): String? {
    cfg.optJSONObject("video_metadata")?.optString("video_url", "")
        ?.takeIf { it.isNotEmpty() && it != "null" }?.let { return it }
    val a = cfg.optJSONObject("assets")
    if (a != null) {
        for (k in listOf("quest_bar_video", "hero_video", "quest_bar_hero_video", "video", "promo_video")) {
            val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
            return if (v.startsWith("http")) v
                   else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v"
                   else "https://cdn.discordapp.com/quests/$id/$v"
        }
        for (k in a.keys()) {
            val v = a.optString(k, "")
            if (v.contains(".mp4") || v.contains(".m3u8") || v.contains(".webm")) {
                return if (v.startsWith("http")) v
                       else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v"
                       else "https://cdn.discordapp.com/quests/$id/$v"
            }
        }
    }
    val appId = cfg.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
        ?: return null
    return "https://cdn.discordapp.com/quests/$id/${appId}_mx480.m3u8"
}

private val SUPPORTED = listOf(
    "WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO",
    "PLAY_ACTIVITY", "PLAY_ON_DESKTOP", "PLAY_ON_DESKTOP_V2", "STREAM_ON_DESKTOP"
)
private val MOBILE = setOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ACTIVITY")

data class QuestItem(
    val id: String,
    val name: String,
    val reward: String,
    val rewardType: String,
    val expiresMs: Long,
    val taskName: String,
    val secondsNeeded: Long,
    val secondsDone: Long,
    val publisher: String,
    val appName: String?,
    val appId: String?,
    val bannerUrl: String?,
    val rewardIconUrl: String?,
    val videoUrl: String?,
    val questLink: String?,
    val enrolledAt: String?,
    val completedAt: String?,
    val claimedAt: String?,
    val rawConfig: JSONObject
)

data class CollectibleItem(
    val skuId: String,
    val name: String,
    val summary: String,
    val type: Int,
    val purchaseType: Int,
    val purchasedAt: String,
    val expiresAt: String?,
    val items: List<CollectibleSubItem>
)
data class CollectibleSubItem(val type: Int, val asset: String, val label: String, val skuId: String)

enum class RunState { IDLE, RUNNING, DONE, ERROR, NOT_ENROLLED, DESKTOP_ONLY }

data class QuestState(
    val quest: QuestItem,
    val runState: RunState = RunState.IDLE,
    val progress: Long = 0L,
    val log: String = ""
)

private fun parseQuest(q: JSONObject): QuestItem? {
    val id  = q.optString("id").takeIf { it.isNotEmpty() } ?: return null
    val cfg = q.optJSONObject("config") ?: return null
    val us  = q.optJSONObject("user_status")
    val msg = cfg.optJSONObject("messages") ?: return null

    val taskConfig = cfg.optJSONObject("task_config") ?: return null
    val tasks      = taskConfig.optJSONObject("tasks") ?: return null
    val taskName   = SUPPORTED.firstOrNull { tasks.has(it) } ?: return null
    val taskObj    = tasks.optJSONObject(taskName) ?: return null
    val needed     = taskObj.optLong("target", 0L)
    val done       = us?.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

    val rewardsArr = cfg.optJSONObject("rewards_config")?.optJSONArray("rewards")
    var reward = ""; var rewardType = "prize"
    if (rewardsArr != null && rewardsArr.length() > 0) {
        val r = rewardsArr.optJSONObject(0)
        if (r != null) {
            rewardType = when (r.optString("type", "0")) { "4" -> "orbs"; "3" -> "decor"; "2" -> "nitro"; else -> "prize" }
            reward = when (rewardType) {
                "orbs"  -> { val qty = r.optInt("orb_quantity", 0); if (qty > 0) "$qty Discord Orbs" else r.optJSONObject("messages")?.optString("name_with_article", "") ?: "Orbs" }
                "decor" -> r.optJSONObject("messages")?.optString("name_with_article", "")?.removePrefix("a ")?.removePrefix("an ") ?: "Avatar Decoration"
                "nitro" -> "Nitro"
                else    -> r.optJSONObject("messages")?.optString("name_with_article", "")?.removePrefix("a ")?.removePrefix("an ") ?: "In-game Reward"
            }
        }
    }

    return QuestItem(
        id            = id,
        name          = msg.optString("quest_name", ""),
        reward        = reward,
        rewardType    = rewardType,
        expiresMs     = parseIso(cfg.optString("expires_at", "")),
        taskName      = taskName,
        secondsNeeded = needed,
        secondsDone   = done,
        publisher     = msg.optString("game_publisher", ""),
        appName       = cfg.optJSONObject("application")?.optString("name")?.takeIf { it.isNotEmpty() && it != "null" },
        appId         = cfg.optJSONObject("application")?.optString("id")?.takeIf   { it.isNotEmpty() && it != "null" },
        bannerUrl     = bannerUrl(id, cfg),
        rewardIconUrl = rewardIconUrl(id, cfg),
        videoUrl      = videoUrl(id, cfg),
        questLink     = cfg.optJSONObject("application")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" }
                        ?: cfg.optJSONObject("cta_config")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" },
        enrolledAt    = us?.optString("enrolled_at")?.takeIf  { it.isNotEmpty() && it != "null" },
        completedAt   = us?.optString("completed_at")?.takeIf { it.isNotEmpty() && it != "null" },
        claimedAt     = us?.optString("claimed_at")?.takeIf   { it.isNotEmpty() && it != "null" },
        rawConfig     = cfg
    )
}

private suspend fun fetchQuests(token: String): Pair<List<QuestItem>, Int?> = withContext(Dispatchers.IO) {
    val now  = System.currentTimeMillis()
    val resp = http.newCall(req("https://discord.com/api/v9/quests/@me", token).build()).execute()
    val body = resp.body?.string() ?: ""
    if (!resp.isSuccessful) throw Exception(try { JSONObject(body).optString("message", "HTTP ${resp.code}") } catch (_: Exception) { "HTTP ${resp.code}" })
    val arr  = try { JSONObject(body).optJSONArray("quests") ?: JSONArray() } catch (_: Exception) { JSONArray() }
    val list = mutableListOf<QuestItem>()
    for (i in 0 until arr.length()) {
        val item = parseQuest(arr.getJSONObject(i)) ?: continue
        if (item.expiresMs > 0 && item.expiresMs < now) continue
        if (item.claimedAt != null) continue
        list.add(item)
    }
    val orbResp = http.newCall(req("https://discord.com/api/v9/users/@me/virtual-currency/balance", token).build()).execute()
    val orbs = try { JSONObject(orbResp.body?.string() ?: "{}").optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }
    list to orbs
}

private suspend fun doEnroll(token: String, questId: String): String? = withContext(Dispatchers.IO) {
    val body = JSONObject()
        .put("location_context", JSONObject().put("guild_id", "0").put("channel_id", "0").put("channel_type", "0"))
        .toString().toRequestBody("application/json".toMediaType())
    val resp = http.newCall(req("https://discord.com/api/v9/quests/$questId/enroll", token).post(body).build()).execute()
    val rb   = resp.body?.string() ?: "{}"
    try {
        val j = JSONObject(rb)
        j.optString("enrolled_at").takeIf { it.isNotEmpty() && it != "null" }
            ?: j.optJSONObject("user_status")?.optString("enrolled_at")?.takeIf { it.isNotEmpty() && it != "null" }
    } catch (_: Exception) { null }
}

private suspend fun doGetStatus(token: String, questId: String): JSONObject = withContext(Dispatchers.IO) {
    try { JSONObject(http.newCall(req("https://discord.com/api/v9/quests/@me/$questId", token).build()).execute().body?.string() ?: "{}") }
    catch (_: Exception) { JSONObject() }
}

private suspend fun doClaim(token: String, questId: String, captchaKey: String? = null, rqtoken: String? = null): Pair<Int, JSONObject> = withContext(Dispatchers.IO) {
    val b = req("https://discord.com/api/v9/quests/$questId/claim-reward", token)
        .post("{}".toRequestBody("application/json".toMediaType()))
    if (captchaKey != null) b.header("X-Captcha-Key", captchaKey)
    if (rqtoken    != null) b.header("X-Captcha-Rqtoken", rqtoken)
    val r = http.newCall(b.build()).execute()
    r.code to try { JSONObject(r.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
}

private suspend fun doFirstDmChannelId(token: String): String? = withContext(Dispatchers.IO) {
    try {
        val arr = JSONArray(http.newCall(req("https://discord.com/api/v9/users/@me/channels", token).build()).execute().body?.string() ?: "[]")
        if (arr.length() > 0) arr.getJSONObject(0).optString("id").takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) { null }
}

private suspend fun doFetchCollectibles(token: String): JSONArray = withContext(Dispatchers.IO) {
    try { JSONArray(http.newCall(req("https://discord.com/api/v9/users/@me/collectibles-purchases", token).build()).execute().body?.string() ?: "[]") }
    catch (_: Exception) { JSONArray() }
}

private suspend fun autoComplete(token: String, state: QuestState, onUpdate: (QuestState) -> Unit) {
    val q            = state.quest
    val questId      = q.id
    val taskName     = q.taskName
    val secondsNeeded = q.secondsNeeded
    var secondsDone  = q.secondsDone
    var cur          = state.copy(runState = RunState.RUNNING, log = "Starting...", progress = secondsDone)
    withContext(Dispatchers.Main) { onUpdate(cur) }

    fun upd(log: String, prog: Long = secondsDone, rs: RunState = RunState.RUNNING) {
        cur = cur.copy(runState = rs, log = log, progress = prog)
    }

    if (taskName !in MOBILE) {
        upd("Requires Discord Desktop app.\nPlay/Stream tasks use Electron IPC — not available on Android.", rs = RunState.DESKTOP_ONLY)
        withContext(Dispatchers.Main) { onUpdate(cur) }
        return
    }

    try {
        if (q.completedAt != null && q.claimedAt == null) {
            upd("Claiming reward...", secondsNeeded)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val (code, _) = doClaim(token, questId)
            upd(if (code == 200) "Reward claimed!" else "Completed! Claim in Discord.", secondsNeeded, RunState.DONE)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            return
        }

        var enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Enrolling in quest...", 0)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            val ea = doEnroll(token, questId)
            if (ea != null) {
                enrolledAt = ea
            } else {
                delay(800)
                val st = doGetStatus(token, questId)
                enrolledAt = st.optJSONObject("user_status")?.optString("enrolled_at")?.takeIf { it.isNotEmpty() && it != "null" }
                if (enrolledAt == null) {
                    upd("Not enrolled. Accept the quest in Discord first.", 0, RunState.NOT_ENROLLED)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    return
                }
            }
        }

        when (taskName) {
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                val enrollMs  = parseIso(enrolledAt).takeIf { it > 0L } ?: (System.currentTimeMillis() - 60_000L)
                val maxFuture = 10L
                val speed     = 7L
                upd("Spoofing video: ${q.name}", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                while (true) {
                    val maxAllowed = (System.currentTimeMillis() - enrollMs) / 1000 + maxFuture
                    val diff       = maxAllowed - secondsDone
                    val nextTs     = secondsDone + speed
                    if (diff >= speed) {
                        val sendTs = minOf(secondsNeeded.toDouble(), nextTs.toDouble() + Math.random())
                        val body   = JSONObject().put("timestamp", sendTs).toString().toRequestBody("application/json".toMediaType())
                        val resp   = http.newCall(req("https://discord.com/api/v9/quests/$questId/video-progress", token).post(body).build()).execute()
                        val rj     = try { JSONObject(resp.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                        val done2  = rj.optString("completed_at", "").isNotEmpty()
                        secondsDone = minOf(secondsNeeded, nextTs)
                        upd("Video: ${secondsDone}s / ${secondsNeeded}s (${if (secondsNeeded > 0) secondsDone * 100 / secondsNeeded else 0}%)", secondsDone)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        if (done2 || secondsDone >= secondsNeeded) break
                    }
                    if (nextTs >= secondsNeeded) break
                    delay(1000L)
                }
                val fb = JSONObject().put("timestamp", secondsNeeded.toDouble()).toString().toRequestBody("application/json".toMediaType())
                http.newCall(req("https://discord.com/api/v9/quests/$questId/video-progress", token).post(fb).build()).execute()
                secondsDone = secondsNeeded
                delay(800L)
                upd("Claiming reward...", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                val (code, _) = doClaim(token, questId)
                upd(if (code == 200) "Reward claimed!" else "Completed! Claim in Discord.", secondsDone, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
            "PLAY_ACTIVITY" -> {
                val channelId = doFirstDmChannelId(token) ?: throw Exception("No DM channel found. Open a DM in Discord first.")
                val streamKey = "call:$channelId:1"
                upd("Spoofing activity (~${maxOf(0L, (secondsNeeded - secondsDone) / 60)} min remaining)", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                while (true) {
                    val rb = JSONObject().put("stream_key", streamKey).put("terminal", false).toString().toRequestBody("application/json".toMediaType())
                    val rs = http.newCall(req("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(rb).build()).execute()
                    val rj = try { JSONObject(rs.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                    secondsDone = rj.optJSONObject("progress")?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                    upd("Activity: ${secondsDone}s / ${secondsNeeded}s (~${maxOf(0L, (secondsNeeded - secondsDone) / 60)} min left)", secondsDone)
                    withContext(Dispatchers.Main) { onUpdate(cur) }
                    if (secondsDone >= secondsNeeded) {
                        val tb = JSONObject().put("stream_key", streamKey).put("terminal", true).toString().toRequestBody("application/json".toMediaType())
                        http.newCall(req("https://discord.com/api/v9/quests/$questId/heartbeat", token).post(tb).build()).execute()
                        break
                    }
                    delay(20_000L)
                }
                delay(800L)
                upd("Claiming reward...", secondsDone)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                val (code, _) = doClaim(token, questId)
                upd(if (code == 200) "Reward claimed!" else "Completed! Claim in Discord.", secondsNeeded, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
            else -> {
                upd("Task '$taskName' not supported on Android.", rs = RunState.DESKTOP_ONLY)
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
        private const val PREFS       = "quest_prefs_v3"
        fun start(ctx: Context, token: String) =
            ctx.startActivity(Intent(ctx, QuestActivity::class.java).putExtra(EXTRA_TOKEN, token))
    }
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = DC.Primary, background = DC.Bg, surface = DC.Surface)) {
                Surface(Modifier.fillMaxSize(), color = DC.Bg) {
                    val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
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
        TosDialog(
            onAccept  = { prefs.edit().putBoolean("tos_ack", true).apply(); tosAcked = true },
            onDecline = onBack
        )
    } else {
        QuestScreen(token, onBack)
    }
}

@Composable
private fun TosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxWidth(0.92f).clip(RoundedCornerShape(20.dp))
                .background(DC.Card).border(1.dp, DC.Warning.copy(0.35f), RoundedCornerShape(20.dp)).padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(40.dp).background(DC.Warning.copy(0.15f), CircleShape), Alignment.Center) {
                        Icon(Icons.Outlined.Warning, null, tint = DC.Warning, modifier = Modifier.size(20.dp))
                    }
                    Text("Terms of Service Warning", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White)
                }
                HorizontalDivider(color = DC.Border)
                Text(
                    "This tool automates Discord Quest completion via the official API.\n\n" +
                    "Using automation tools may violate Discord's Terms of Service. " +
                    "Your account could be suspended or banned.\n\nFor educational purposes only. Use at your own risk.",
                    fontSize = 13.sp, color = DC.SubText, lineHeight = 20.sp
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, DC.Error.copy(0.5f))) {
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
    var refreshKey  by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    var showCollect by remember { mutableStateOf(false) }
    var videoQuest  by remember { mutableStateOf<QuestItem?>(null) }
    var moreQuest   by remember { mutableStateOf<QuestItem?>(null) }

    var sortMode    by remember { mutableIntStateOf(0) }
    var fOrbs       by remember { mutableStateOf(false) }
    var fDecor      by remember { mutableStateOf(false) }
    var fInGame     by remember { mutableStateOf(false) }
    var fWatch      by remember { mutableStateOf(false) }
    var fPlay       by remember { mutableStateOf(false) }

    val states = remember { mutableStateListOf<QuestState>() }

    LaunchedEffect(refreshKey) {
        loading = true; fetchError = null; states.clear(); orbBalance = null
        try {
            val (list, orbs) = fetchQuests(token)
            states.addAll(list.map { q ->
                val rs = when {
                    q.completedAt != null && q.claimedAt == null -> RunState.DONE
                    q.taskName !in MOBILE -> RunState.DESKTOP_ONLY
                    q.enrolledAt == null  -> RunState.NOT_ENROLLED
                    else                  -> RunState.IDLE
                }
                QuestState(quest = q, runState = rs, progress = q.secondsDone,
                    log = when (rs) { RunState.DONE -> "Completed! Tap to claim."; RunState.DESKTOP_ONLY -> "Requires Discord Desktop app."; else -> "" })
            })
            orbBalance = orbs
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    var displayed = states.toList()
    if (fOrbs)   displayed = displayed.filter { it.quest.rewardType == "orbs" }
    if (fDecor)  displayed = displayed.filter { it.quest.rewardType == "decor" }
    if (fInGame) displayed = displayed.filter { it.quest.rewardType != "orbs" && it.quest.rewardType != "decor" }
    if (fWatch)  displayed = displayed.filter { it.quest.taskName.contains("WATCH") }
    if (fPlay)   displayed = displayed.filter { it.quest.taskName.contains("PLAY") || it.quest.taskName.contains("STREAM") }
    displayed = when (sortMode) {
        1    -> displayed.sortedByDescending { it.quest.expiresMs }
        2    -> displayed.sortedBy { it.quest.expiresMs }
        3    -> displayed.filter { it.quest.enrolledAt != null }
        else -> displayed
    }

    Box(Modifier.fillMaxSize().background(DC.Bg)) {
        Column(Modifier.fillMaxSize()) {
            QuestTopBar(
                orbBalance  = orbBalance,
                questCount  = states.size,
                filterCount = listOf(fOrbs, fDecor, fInGame, fWatch, fPlay).count { it },
                onBack      = onBack,
                onFilter    = { showFilters = true },
                onCollect   = { showCollect = true },
                onRefresh   = { refreshKey++ },
                canCompleteAll = states.any { it.runState == RunState.IDLE || it.runState == RunState.NOT_ENROLLED },
                onCompleteAll  = {
                    CoroutineScope(Dispatchers.IO).launch {
                        states.forEachIndexed { idx, st ->
                            if (st.runState == RunState.IDLE || st.runState == RunState.NOT_ENROLLED) {
                                autoComplete(token, st) { updated ->
                                    if (idx < states.size) states[idx] = updated
                                }
                            }
                        }
                    }
                }
            )
            when {
                loading        -> LoadingPane()
                fetchError != null -> ErrorPane(fetchError!!) { refreshKey++ }
                else           -> QuestList(
                    displayed = displayed,
                    token     = token,
                    onUpdate  = { upd -> val i = states.indexOfFirst { it.quest.id == upd.quest.id }; if (i >= 0) states[i] = upd },
                    onWatch   = { videoQuest = it },
                    onMore    = { moreQuest  = it }
                )
            }
        }

        if (showFilters) {
            FiltersSheet(
                sortMode = sortMode, fOrbs = fOrbs, fDecor = fDecor, fInGame = fInGame, fWatch = fWatch, fPlay = fPlay,
                onApply = { sm, o, d, ig, w, p -> sortMode = sm; fOrbs = o; fDecor = d; fInGame = ig; fWatch = w; fPlay = p; showFilters = false },
                onReset = { sortMode = 0; fOrbs = false; fDecor = false; fInGame = false; fWatch = false; fPlay = false; showFilters = false },
                onDismiss = { showFilters = false }
            )
        }

        if (showCollect) {
            CollectiblesScreen(token = token, onDismiss = { showCollect = false })
        }

        videoQuest?.let { q ->
            VideoPlayerDialog(
                quest    = q,
                token    = token,
                onDismiss = { videoQuest = null },
                onComplete = { upd ->
                    val i = states.indexOfFirst { it.quest.id == upd.quest.id }
                    if (i >= 0) states[i] = upd
                    videoQuest = null
                }
            )
        }

        moreQuest?.let { q ->
            MoreMenuSheet(quest = q, ctx = ctx, onDismiss = { moreQuest = null })
        }
    }
}

@Composable
private fun QuestTopBar(
    orbBalance: Int?, questCount: Int, filterCount: Int,
    onBack: () -> Unit, onFilter: () -> Unit, onCollect: () -> Unit, onRefresh: () -> Unit,
    canCompleteAll: Boolean, onCompleteAll: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1040), Color(0xFF0F1B3D), DC.Bg)))
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).padding(top = 28.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.size(36.dp).background(Color.Black.copy(0.4f), CircleShape)
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White, modifier = Modifier.size(18.dp)) }

                Spacer(Modifier.width(10.dp))

                Column(Modifier.weight(1f)) {
                    Text("Quests", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = DC.White)
                    if (orbBalance != null && orbBalance > 0) {
                        Text("\u25e6 $orbBalance Orbs", fontSize = 11.sp, color = DC.OrbViolet, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (canCompleteAll) {
                        Button(
                            onClick = onCompleteAll,
                            colors  = ButtonDefaults.buttonColors(containerColor = DC.Primary.copy(0.9f)),
                            shape   = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Outlined.DoneAll, null, Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Complete All", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Box {
                        IconButton(onClick = onFilter, modifier = Modifier.size(36.dp).background(DC.CardAlt, RoundedCornerShape(10.dp)).border(1.dp, DC.Border, RoundedCornerShape(10.dp))) {
                            Icon(Icons.Outlined.Tune, null, tint = DC.SubText, modifier = Modifier.size(17.dp))
                        }
                        if (filterCount > 0) {
                            Box(
                                Modifier.size(14.dp).align(Alignment.TopEnd).offset(2.dp, (-2).dp)
                                    .background(DC.Primary, CircleShape),
                                Alignment.Center
                            ) { Text("$filterCount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.ExtraBold) }
                        }
                    }
                    IconButton(onClick = onCollect, modifier = Modifier.size(36.dp).background(DC.CardAlt, RoundedCornerShape(10.dp)).border(1.dp, DC.Border, RoundedCornerShape(10.dp))) {
                        Icon(Icons.Outlined.Inventory2, null, tint = DC.SubText, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp).background(DC.CardAlt, RoundedCornerShape(10.dp)).border(1.dp, DC.Border, RoundedCornerShape(10.dp))) {
                        Icon(Icons.Outlined.Refresh, null, tint = DC.SubText, modifier = Modifier.size(17.dp))
                    }
                }
            }
            if (questCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text("$questCount active quest${if (questCount != 1) "s" else ""}", fontSize = 11.sp, color = DC.Muted)
            }
        }
    }
}

@Composable
private fun QuestList(
    displayed: List<QuestState>, token: String,
    onUpdate: (QuestState) -> Unit, onWatch: (QuestItem) -> Unit, onMore: (QuestItem) -> Unit
) {
    if (displayed.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(32.dp)) {
                Box(Modifier.size(72.dp).background(DC.Muted.copy(0.1f), CircleShape), Alignment.Center) {
                    Icon(Icons.Outlined.SportsEsports, null, tint = DC.Muted, modifier = Modifier.size(36.dp))
                }
                Text("No quests available", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Check back later or adjust your filters.", color = DC.Muted, fontSize = 13.sp)
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Spacer(Modifier.height(6.dp)) }
        items(displayed, key = { it.quest.id }) { state ->
            QuestCard(state = state, token = token, onUpdate = onUpdate, onWatch = onWatch, onMore = onMore)
        }
        item { Spacer(Modifier.height(28.dp)) }
    }
}

@Composable
private fun QuestCard(
    state: QuestState, token: String,
    onUpdate: (QuestState) -> Unit, onWatch: (QuestItem) -> Unit, onMore: (QuestItem) -> Unit
) {
    val ctx   = LocalContext.current
    val q     = state.quest
    val scope = rememberCoroutineScope()

    val accentColor = when {
        q.claimedAt   != null -> DC.Teal
        q.rewardType == "orbs"  -> DC.OrbViolet
        q.rewardType == "decor" -> DC.Primary
        q.rewardType == "nitro" -> DC.Primary
        else -> DC.Success
    }

    val gifLoader = remember(ctx) {
        ImageLoader.Builder(ctx).components {
            if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
        }.build()
    }

    val pct = if (q.secondsNeeded > 0) (state.progress.coerceAtLeast(q.secondsDone).toFloat() / q.secondsNeeded).coerceIn(0f, 1f) else 0f

    val pulse  = rememberInfiniteTransition(label = "pulse")
    val pAlpha by pulse.animateFloat(0.15f, 0.55f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pa")
    val shimmerT = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by shimmerT.animateFloat(-300f, 1400f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "sx")

    val borderAlpha = if (state.runState == RunState.RUNNING) pAlpha else 0.2f

    Card(
        colors   = CardDefaults.cardColors(containerColor = DC.Card),
        shape    = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, accentColor.copy(borderAlpha), RoundedCornerShape(16.dp))
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                if (q.bannerUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(ctx).data(q.bannerUrl).crossfade(true).build(),
                        contentDescription = null, imageLoader = gifLoader,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(accentColor.copy(0.35f), DC.Card))))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DC.Card.copy(0.7f), DC.Card), startY = 60f)))

                if (state.runState == RunState.RUNNING) {
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(
                        listOf(Color.Transparent, accentColor.copy(0.25f), Color.Transparent),
                        startX = shimmerX, endX = shimmerX + 380f
                    )))
                }

                Row(
                    Modifier.align(Alignment.TopStart).padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (q.publisher.isNotEmpty()) {
                        Row(
                            Modifier.background(DC.Success.copy(0.85f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Outlined.Verified, null, tint = Color.White, modifier = Modifier.size(9.dp))
                            Text(q.publisher, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                Text(
                    "Ends ${fmtShort(q.expiresMs)}",
                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
                    fontSize = 9.sp, color = DC.SubText.copy(0.9f), fontWeight = FontWeight.SemiBold
                )

                Box(
                    Modifier.align(Alignment.BottomStart).offset(14.dp, 28.dp).size(64.dp)
                ) {
                    if (q.rewardIconUrl != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(ctx).data(q.rewardIconUrl).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier.size(52.dp).align(Alignment.Center).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.size(52.dp).align(Alignment.Center).background(DC.CardAlt, CircleShape)) {
                            Icon(Icons.Outlined.CardGiftcard, null, tint = accentColor, modifier = Modifier.align(Alignment.Center).size(26.dp))
                        }
                    }
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = 3.5f
                        val r = size.minDimension / 2f - stroke / 2f
                        drawCircle(color = DC.Border.copy(0.8f), radius = r, style = Stroke(stroke))
                        if (pct > 0f) {
                            drawArc(
                                color      = accentColor,
                                startAngle = -90f,
                                sweepAngle = 360f * pct,
                                useCenter  = false,
                                style      = Stroke(stroke, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 36.dp, bottom = 4.dp)) {
                Text(
                    "QUEST: ${q.name.uppercase()}",
                    fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.06.sp
                )
                Text(q.reward, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(describeTask(q.taskName, q.secondsNeeded), fontSize = 12.sp, color = DC.SubText, lineHeight = 17.sp)
            }

            if (q.secondsNeeded > 0) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress", fontSize = 9.sp, color = DC.Muted, fontWeight = FontWeight.SemiBold)
                        Text("${(pct * 100).toInt()}%", fontSize = 9.sp, color = accentColor, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(DC.Border)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct).background(
                            Brush.horizontalGradient(listOf(accentColor.copy(0.7f), accentColor)), RoundedCornerShape(3.dp)
                        ))
                    }
                }
            }

            if (state.log.isNotBlank() && state.runState != RunState.IDLE) {
                val logColor = when (state.runState) {
                    RunState.ERROR, RunState.NOT_ENROLLED -> DC.Error
                    RunState.DONE    -> accentColor
                    RunState.DESKTOP_ONLY -> DC.Warning
                    else -> DC.Muted
                }
                Text(
                    state.log, fontSize = 10.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 14.sp,
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .background(logColor.copy(0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when {
                    state.runState == RunState.RUNNING -> {
                        Box(
                            Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                                .background(accentColor.copy(0.15f))
                                .border(1.dp, accentColor.copy(pAlpha), RoundedCornerShape(12.dp))
                                .drawWithContent {
                                    drawContent()
                                    drawRect(Brush.horizontalGradient(listOf(Color.Transparent, accentColor.copy(0.3f), Color.Transparent), startX = shimmerX, endX = shimmerX + 280f), size = size)
                                },
                            Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(Modifier.size(14.dp), color = accentColor, strokeWidth = 2.dp)
                                Text(state.log.lines().lastOrNull()?.take(30) ?: "Running...", fontSize = 11.sp, color = accentColor, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    state.runState == RunState.DONE && q.completedAt != null && q.claimedAt == null -> {
                        ActionBtn("Claim Reward", accentColor, Icons.Outlined.CardGiftcard, Modifier.weight(1f)) {
                            scope.launch(Dispatchers.IO) {
                                val (code, j) = doClaim(token, q.id)
                                withContext(Dispatchers.Main) {
                                    onUpdate(state.copy(runState = if (code == 200) RunState.DONE else state.runState,
                                        log = if (code == 200) "Reward claimed!" else "Claim failed ($code)"))
                                }
                            }
                        }
                    }
                    state.runState == RunState.DONE -> {
                        Row(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(accentColor.copy(0.12f)).border(1.dp, accentColor.copy(0.25f), RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.CheckCircle, null, tint = accentColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Completed!", fontWeight = FontWeight.ExtraBold, color = accentColor, fontSize = 13.sp)
                        }
                    }
                    state.runState == RunState.DESKTOP_ONLY -> {
                        Row(Modifier.weight(1f).height(44.dp).clip(RoundedCornerShape(12.dp))
                            .background(DC.Warning.copy(0.08f)).border(1.dp, DC.Warning.copy(0.2f), RoundedCornerShape(12.dp)),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.Computer, null, tint = DC.Warning, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Desktop Only", fontSize = 12.sp, color = DC.Warning, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    else -> {
                        val isVideo = q.taskName.contains("WATCH")
                        ActionBtn(
                            label  = "Auto Complete",
                            color  = accentColor,
                            icon   = Icons.Outlined.PlayArrow,
                            mod    = Modifier.weight(if (isVideo) 0.55f else 1f),
                            shimmerX = shimmerX
                        ) {
                            scope.launch(Dispatchers.IO) { autoComplete(token, state, onUpdate) }
                        }
                        if (isVideo && q.videoUrl != null) {
                            Spacer(Modifier.width(2.dp))
                            OutlinedActionBtn("Watch Video", Icons.Outlined.Videocam, Modifier.weight(0.45f)) {
                                onWatch(q)
                            }
                        }
                    }
                }

                IconButton(
                    onClick  = { onMore(q) },
                    modifier = Modifier.size(44.dp).background(DC.CardAlt, RoundedCornerShape(12.dp)).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
                ) { Icon(Icons.Outlined.MoreVert, null, tint = DC.Muted, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun ActionBtn(label: String, color: Color, icon: ImageVector, mod: Modifier, shimmerX: Float = 0f, onClick: () -> Unit) {
    Box(
        mod.height(44.dp).clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(color, color.copy(0.8f))))
            .drawWithContent {
                drawContent()
                drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.15f), Color.Transparent), startX = shimmerX, endX = shimmerX + 220f), size = size)
            }
            .clickable(onClick = onClick),
        Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
            Text(label, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp)
        }
    }
}

@Composable
private fun OutlinedActionBtn(label: String, icon: ImageVector, mod: Modifier, onClick: () -> Unit) {
    Box(
        mod.height(44.dp).clip(RoundedCornerShape(12.dp))
            .background(DC.CardAlt).border(1.dp, DC.Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, null, tint = DC.SubText, modifier = Modifier.size(14.dp))
            Text(label, fontWeight = FontWeight.Bold, color = DC.SubText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FiltersSheet(
    sortMode: Int, fOrbs: Boolean, fDecor: Boolean, fInGame: Boolean, fWatch: Boolean, fPlay: Boolean,
    onApply: (Int, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onReset: () -> Unit, onDismiss: () -> Unit
) {
    var tmpSort  by remember { mutableIntStateOf(sortMode) }
    var tmpOrbs  by remember { mutableStateOf(fOrbs) }
    var tmpDecor by remember { mutableStateOf(fDecor) }
    var tmpInGame by remember { mutableStateOf(fInGame) }
    var tmpWatch by remember { mutableStateOf(fWatch) }
    var tmpPlay  by remember { mutableStateOf(fPlay) }

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).clickable(onClick = onDismiss)) {
        Box(
            Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(Color(0xFF23242B))
                .clickable(onClick = {})
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 24.dp)) {
                Box(Modifier.width(36.dp).height(4.dp).background(DC.Border, RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Text("Filters", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))

                Text("Sort by", fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.08.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                FilterGroup {
                    listOf("Suggestions", "Most recent", "Expiring soon", "Active").forEachIndexed { i, label ->
                        RadioRow(label, tmpSort == i) { tmpSort = i }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Rewards", fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.08.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                FilterGroup {
                    CheckRow("Orbs", tmpOrbs)        { tmpOrbs   = !tmpOrbs }
                    CheckRow("Avatar decoration", tmpDecor) { tmpDecor = !tmpDecor }
                    CheckRow("In-game rewards", tmpInGame)  { tmpInGame = !tmpInGame }
                }

                Spacer(Modifier.height(14.dp))
                Text("Quest type", fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.08.sp, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
                FilterGroup {
                    CheckRow("Play", tmpPlay)  { tmpPlay  = !tmpPlay }
                    CheckRow("Watch", tmpWatch) { tmpWatch = !tmpWatch }
                }

                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onReset, modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, DC.Border)
                    ) { Text("Reset", color = DC.Muted, fontWeight = FontWeight.Bold) }
                    Button(
                        onClick = { onApply(tmpSort, tmpOrbs, tmpDecor, tmpInGame, tmpWatch, tmpPlay) },
                        modifier = Modifier.weight(2f).height(50.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = DC.Primary),
                        shape    = RoundedCornerShape(24.dp)
                    ) { Text("Done", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) }
                }
            }
        }
    }
}

@Composable
private fun FilterGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFF1A1B22)),
        content = content
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = DC.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(20.dp).border(2.dp, if (selected) DC.Primary else DC.Muted.copy(0.6f), CircleShape)
                .padding(4.dp).background(if (selected) DC.Primary else Color.Transparent, CircleShape)
        )
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = DC.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(20.dp).clip(RoundedCornerShape(4.dp))
                .background(if (checked) DC.Primary else Color.Transparent)
                .border(2.dp, if (checked) DC.Primary else DC.Muted.copy(0.6f), RoundedCornerShape(4.dp)),
            Alignment.Center
        ) {
            if (checked) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun CollectiblesScreen(token: String, onDismiss: () -> Unit) {
    val ctx  = LocalContext.current
    var loading   by remember { mutableStateOf(true) }
    var items     by remember { mutableStateOf(listOf<CollectibleItem>()) }
    val gifLoader = remember(ctx) { ImageLoader.Builder(ctx).components { if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }

    LaunchedEffect(Unit) {
        val arr = doFetchCollectibles(token)
        val list = mutableListOf<CollectibleItem>()
        for (i in 0 until arr.length()) {
            try {
                val o    = arr.getJSONObject(i)
                val subs = mutableListOf<CollectibleSubItem>()
                val ia   = o.optJSONArray("items")
                if (ia != null) for (j in 0 until ia.length()) {
                    val it = ia.getJSONObject(j)
                    subs.add(CollectibleSubItem(it.optInt("type"), it.optString("asset"), it.optString("label"), it.optString("sku_id")))
                }
                list.add(CollectibleItem(
                    skuId = o.optString("sku_id"), name = o.optString("name"), summary = o.optString("summary"),
                    type = o.optInt("type"), purchaseType = o.optInt("purchase_type"),
                    purchasedAt = o.optString("purchased_at"),
                    expiresAt = o.optString("expires_at").takeIf { it.isNotEmpty() && it != "null" },
                    items = subs
                ))
            } catch (_: Exception) {}
        }
        items   = list
        loading = false
    }

    Box(Modifier.fillMaxSize().background(DC.Bg)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 16.dp, vertical = 14.dp).padding(top = 28.dp)) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White)
                }
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Collectibles", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White)
                    if (!loading) Text("${items.size} item${if (items.size != 1) "s" else ""}", fontSize = 11.sp, color = DC.Muted)
                }
            }
            HorizontalDivider(color = DC.Border)
            if (loading) { LoadingPane() }
            else if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.Inventory2, null, tint = DC.Muted, modifier = Modifier.size(48.dp))
                        Text("No collectibles yet", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.skuId }) { c ->
                        CollectibleCard(c, gifLoader, ctx)
                    }
                    item { Spacer(Modifier.height(28.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CollectibleCard(c: CollectibleItem, gifLoader: ImageLoader, ctx: Context) {
    val typeColor = when (c.type) { 0 -> DC.OrbViolet; 1 -> DC.Primary; 2 -> DC.Warning; 3000 -> DC.Success; else -> DC.Muted }
    val typeName  = when (c.type) { 0 -> "Avatar Decoration"; 1 -> "Profile Effect"; 2 -> "Bundle"; 3000 -> "Badge"; else -> "Collectible" }
    val purchName = when (c.purchaseType) { 1 -> "Direct Purchase"; 10 -> "Quest Reward"; else -> "Gift / Other" }

    val iconUrl = c.items.firstOrNull()?.let { sub ->
        val asset = sub.asset.takeIf { it.isNotEmpty() && it != "null" } ?: return@let null
        when {
            asset.startsWith("http") -> asset
            sub.type == 0 -> "https://cdn.discordapp.com/avatar-decoration-presets/$asset.png?size=160&passthrough=true"
            sub.type == 1 -> "https://cdn.discordapp.com/profile-effects/$asset.png"
            else          -> "https://cdn.discordapp.com/collectibles/$asset.png"
        }
    }

    Card(colors = CardDefaults.cardColors(containerColor = DC.Card), shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, typeColor.copy(0.2f), RoundedCornerShape(14.dp))) {
        Column {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(54.dp).background(DC.CardAlt, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))) {
                    if (iconUrl != null) {
                        AsyncImage(ImageRequest.Builder(ctx).data(iconUrl).crossfade(true).build(), null, gifLoader, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Outlined.Inventory2, null, tint = DC.Muted, modifier = Modifier.align(Alignment.Center).size(26.dp))
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(c.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DC.White)
                    Text(typeName, fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.Bold)
                    Text(purchName, fontSize = 9.sp, color = DC.Muted)
                }
            }
            if (c.summary.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = DC.Border)
                Text(c.summary, fontSize = 11.sp, color = DC.SubText, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
            }
            HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = DC.Border)
            val expiry = if (c.expiresAt != null) "Expires ${fmtShort(parseIso(c.expiresAt))}" else "Never expires"
            Text(
                "Purchased ${fmtShort(parseIso(c.purchasedAt))}  ·  $expiry",
                fontSize = 9.sp, color = DC.Muted,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun VideoPlayerDialog(
    quest: QuestItem, token: String,
    onDismiss: () -> Unit, onComplete: (QuestState) -> Unit
) {
    val ctx         = LocalContext.current
    val scope       = rememberCoroutineScope()
    val secondsNeeded = quest.secondsNeeded
    val taskName    = quest.taskName

    var spoofDone   by remember { mutableLongStateOf(quest.secondsDone) }
    var log         by remember { mutableStateOf("Loading video...") }
    var spoofActive by remember { mutableStateOf(false) }
    var completed   by remember { mutableStateOf(false) }
    val pct         = if (secondsNeeded > 0) (spoofDone.toFloat() / secondsNeeded).coerceIn(0f, 1f) else 0f

    val pulse = rememberInfiniteTransition(label = "vp")
    val pA by pulse.animateFloat(0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "vpa")

    DisposableEffect(Unit) { onDispose { spoofActive = false } }

    Dialog(onDismissRequest = { spoofActive = false; onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            Modifier.fillMaxWidth(0.96f).clip(RoundedCornerShape(20.dp))
                .background(DC.Card).border(1.dp, DC.Border, RoundedCornerShape(20.dp))
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)).background(Color.Black)) {
                    AndroidView(
                        factory = { context ->
                            android.widget.VideoView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                setMediaController(android.widget.MediaController(context).also { it.setAnchorView(this) })
                                setVideoURI(Uri.parse(quest.videoUrl))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = true
                                    mp.start()
                                    log = "Watching & syncing..."
                                    spoofActive = true
                                    scope.launch(Dispatchers.IO) {
                                        var enrolledAt = quest.enrolledAt
                                        if (enrolledAt == null) {
                                            val ea = doEnroll(token, quest.id)
                                            if (ea != null) {
                                                enrolledAt = ea
                                            } else {
                                                delay(800)
                                                val st = doGetStatus(token, quest.id)
                                                enrolledAt = st.optJSONObject("user_status")?.optString("enrolled_at")?.takeIf { it.isNotEmpty() && it != "null" }
                                                    ?: run { withContext(Dispatchers.Main) { log = "Could not enroll. Accept quest in Discord." }; return@launch }
                                            }
                                        }
                                        val enrollMs  = parseIso(enrolledAt).takeIf { it > 0L } ?: (System.currentTimeMillis() - 60_000L)
                                        val speed     = 7L
                                        val maxFuture = 10L
                                        while (spoofActive && spoofDone < secondsNeeded) {
                                            delay(7_000L)
                                            if (!spoofActive) break
                                            val maxAllowed = (System.currentTimeMillis() - enrollMs) / 1000 + maxFuture
                                            val diff = maxAllowed - spoofDone
                                            val nextTs = spoofDone + speed
                                            if (diff >= speed) {
                                                val sendTs = minOf(secondsNeeded.toDouble(), nextTs.toDouble() + Math.random())
                                                val body   = JSONObject().put("timestamp", sendTs).toString().toRequestBody("application/json".toMediaType())
                                                val resp   = http.newCall(req("https://discord.com/api/v9/quests/${quest.id}/video-progress", token).post(body).build()).execute()
                                                val rj     = try { JSONObject(resp.body?.string() ?: "{}") } catch (_: Exception) { JSONObject() }
                                                val done2  = rj.optString("completed_at", "").isNotEmpty()
                                                spoofDone  = minOf(secondsNeeded, nextTs)
                                                val p = if (secondsNeeded > 0) (spoofDone * 100 / secondsNeeded).toInt() else 0
                                                withContext(Dispatchers.Main) { log = "${spoofDone}s / ${secondsNeeded}s ($p%)" }
                                                if (done2 || spoofDone >= secondsNeeded) {
                                                    withContext(Dispatchers.Main) { log = "Video complete! Tap Claim Reward."; completed = true }
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                                setOnErrorListener { _, _, _ -> log = "Video unavailable. Use Auto Complete instead."; false }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(quest.reward, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = DC.White)

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (spoofActive && !completed) {
                            CircularProgressIndicator(Modifier.size(12.dp), color = DC.Primary, strokeWidth = 2.dp, modifier = Modifier.graphicsLayer { alpha = pA })
                        } else if (completed) {
                            Icon(Icons.Outlined.CheckCircle, null, tint = DC.Success, modifier = Modifier.size(13.dp))
                        }
                        Text(log, fontSize = 11.sp, color = if (completed) DC.Success else DC.SubText, fontFamily = FontFamily.Monospace)
                    }

                    Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)).background(DC.Border)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct).background(
                            Brush.horizontalGradient(listOf(DC.Primary.copy(0.7f), DC.Primary)), RoundedCornerShape(3.dp)
                        ))
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { spoofActive = false; onDismiss() },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape    = RoundedCornerShape(12.dp),
                            border   = BorderStroke(1.dp, DC.Border)
                        ) { Text("Close", color = DC.Muted, fontWeight = FontWeight.Bold) }

                        Button(
                            onClick  = {
                                spoofActive = false
                                scope.launch(Dispatchers.IO) {
                                    val (code, j) = doClaim(token, quest.id)
                                    withContext(Dispatchers.Main) {
                                        val rs  = if (code == 200) RunState.DONE else RunState.ERROR
                                        val msg = if (code == 200) "Reward claimed!" else "Claim failed ($code)"
                                        onComplete(QuestState(quest = quest, runState = rs, progress = secondsNeeded, log = msg))
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = DC.Primary),
                            shape    = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.CardGiftcard, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Claim Reward", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreMenuSheet(quest: QuestItem, ctx: Context, onDismiss: () -> Unit) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.5f)).clickable(onClick = onDismiss)) {
        Box(
            Modifier.fillMaxWidth(0.8f).wrapContentHeight().align(Alignment.BottomEnd).offset((-12).dp, (-80).dp)
                .clip(RoundedCornerShape(14.dp)).background(Color(0xFF2B2D35))
                .clickable(onClick = {})
        ) {
            Column(Modifier.padding(4.dp)) {
                if (quest.questLink != null) {
                    MoreItem("Play now", Icons.Outlined.OpenInBrowser) {
                        try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(quest.questLink)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}
                        onDismiss()
                    }
                }
                MoreItem("Why am I seeing this?", Icons.Outlined.HelpOutline) { onDismiss() }
                MoreItem("Copy link", Icons.Outlined.ContentCopy) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("quest", quest.questLink ?: quest.name))
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun MoreItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = DC.SubText, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 14.sp, color = DC.White)
        Spacer(Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.Muted, modifier = Modifier.size(12.dp).graphicsLayer { rotationZ = 180f })
    }
}

@Composable
private fun LoadingPane() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val inf = rememberInfiniteTransition(label = "ld")
            val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "lr")
            Box(Modifier.size(48.dp).rotate(rot).border(2.5.dp, Brush.sweepGradient(listOf(DC.Primary, Color.Transparent)), CircleShape))
            Text("Loading quests...", color = DC.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).background(DC.Error.copy(0.1f), CircleShape), Alignment.Center) {
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

private fun describeTask(name: String, seconds: Long): String {
    val mins = seconds / 60
    val dur  = if (mins < 60) "${mins}min" else "${mins / 60}h${if (mins % 60 > 0) " ${mins % 60}min" else ""}"
    return when (name) {
        "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> "Watch a video for $dur"
        "PLAY_ACTIVITY"                         -> "Play an activity for $dur"
        "PLAY_ON_DESKTOP", "PLAY_ON_DESKTOP_V2" -> "Play on Desktop for $dur"
        "STREAM_ON_DESKTOP"                     -> "Stream on Desktop for $dur"
        "PLAY_ON_XBOX"                          -> "Play on Xbox for $dur"
        "PLAY_ON_PLAYSTATION"                   -> "Play on PlayStation for $dur"
        else                                    -> name
    }
}
