package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Games
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class DiscordUser(
    val id: String,
    val username: String,
    val discriminator: String,
    val globalName: String?,
    val avatar: String?,
    val banner: String?,
    val bannerColor: String?,
    val bio: String?,
    val email: String?,
    val phone: String?,
    val verified: Boolean,
    val mfaEnabled: Boolean,
    val locale: String?,
    val premiumType: Int,
    val publicFlags: Long,
    val accentColor: Int?
)

data class RichTimestamps(val start: Long?, val end: Long?)

data class RichAssets(
    val largeImage: String?,
    val largeText: String?,
    val smallImage: String?,
    val smallText: String?
)

data class PresenceActivity(
    val type: Int,
    val name: String,
    val details: String?,
    val state: String?,
    val applicationId: String?,
    val timestamps: RichTimestamps?,
    val assets: RichAssets?,
    val syncId: String?,
    val albumCoverUrl: String?
)

data class DiscordPresence(
    val status: String,
    val activities: List<PresenceActivity>,
    val platforms: List<String>
)

data class DiscordConnection(
    val type: String,
    val name: String,
    val verified: Boolean,
    val visibility: Int
)

data class AppLog(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String,
    val detail: String? = null
)

private object AppColors {
    val Background     = Color(0xFF1E1F22)
    val Surface        = Color(0xFF2B2D31)
    val SurfaceVar     = Color(0xFF313338)
    val CodeBg         = Color(0xFF1A1B1E)
    val Primary        = Color(0xFF5865F2)
    val OnPrimary      = Color(0xFFFFFFFF)
    val Success        = Color(0xFF23A55A)
    val Warning        = Color(0xFFFAA61A)
    val Error          = Color(0xFFED4245)
    val ErrorContainer = Color(0xFF3B1A1B)
    val OnError        = Color(0xFFFFDFDE)
    val TextPrimary    = Color(0xFFF2F3F5)
    val TextSecondary  = Color(0xFFB5BAC1)
    val TextMuted      = Color(0xFF80848E)
    val Divider        = Color(0xFF3F4147)
    val Spotify        = Color(0xFF1DB954)
    val LoginBg1       = Color(0xFF1A1C3A)
    val LoginBg2       = Color(0xFF111228)
    val LoginBg3       = Color(0xFF0A0B19)
}

private object Radius {
    val Small  = 8.dp
    val Medium = 12.dp
    val Large  = 16.dp
    val XLarge = 20.dp
    val Card   = 18.dp
    val Button = 14.dp
    val Badge  = 20.dp
    val Token  = 10.dp
}

private val DiscordDarkColorScheme = darkColorScheme(
    primary        = AppColors.Primary,
    onPrimary      = AppColors.OnPrimary,
    background     = AppColors.Background,
    surface        = AppColors.Surface,
    surfaceVariant = AppColors.SurfaceVar,
    onBackground   = AppColors.TextPrimary,
    onSurface      = AppColors.TextPrimary,
    error          = AppColors.Error,
    errorContainer = AppColors.ErrorContainer,
    onError        = AppColors.OnError
)

private const val JS_TOKEN =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"
private const val PREF_CRASH        = "crash_prefs"
private const val KEY_CRASH_TRACE   = "crash_trace"
private const val PREF_SESSION      = "session_prefs"
private const val KEY_TOKEN         = "saved_token"
private const val DISCORD_EPOCH     = 1420070400000L
private const val CURRENT_VERSION   = "1.0.20"
private const val GITHUB_API_LATEST = "https://api.github.com/repos/Sc-Rhyan57/GetDiscordToken/releases/latest"
private const val GATEWAY_URL       = "wss://gateway.discord.gg/?v=10&encoding=json"

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

private val appLogs = mutableStateListOf<AppLog>()
private var logsEnabled = mutableStateOf(true)

private fun addLog(level: String, tag: String, message: String, detail: String? = null) {
    if (!logsEnabled.value) return
    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    appLogs.add(0, AppLog(ts, level, tag, message, detail))
    if (appLogs.size > 200) appLogs.removeAt(appLogs.size - 1)
}

private fun snowflakeToDate(id: String): String = try {
    val ts = (id.toLong() ushr 22) + DISCORD_EPOCH
    SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(ts))
} catch (_: Exception) { "Unknown" }

private fun nitroLabel(t: Int): String = when (t) {
    1 -> "Nitro Classic"; 2 -> "Nitro"; 3 -> "Nitro Basic"; else -> "None"
}

private fun localeLabel(l: String): String = try {
    val p = l.split("-", "_")
    (if (p.size >= 2) Locale(p[0], p[1]) else Locale(p[0])).displayName
} catch (_: Exception) { l }

private fun statusColor(s: String): Color = when (s) {
    "online"    -> AppColors.Success
    "idle"      -> AppColors.Warning
    "dnd"       -> AppColors.Error
    else        -> AppColors.TextMuted
}

private fun statusLabel(s: String): String = when (s) {
    "online"    -> "Online"
    "idle"      -> "Idle"
    "dnd"       -> "Do Not Disturb"
    "invisible" -> "Invisible"
    else        -> "Offline"
}

private fun activityTypeLabel(type: Int): String = when (type) {
    0 -> "Playing"
    1 -> "Streaming"
    2 -> "Listening to"
    3 -> "Watching"
    5 -> "Competing in"
    else -> "Activity"
}

private fun connectionLabel(type: String): String = when (type) {
    "spotify"         -> "Spotify"
    "steam"           -> "Steam"
    "github"          -> "GitHub"
    "twitch"          -> "Twitch"
    "youtube"         -> "YouTube"
    "twitter"         -> "X (Twitter)"
    "reddit"          -> "Reddit"
    "playstation"     -> "PlayStation"
    "xbox"            -> "Xbox"
    "battlenet"       -> "Battle.net"
    "instagram"       -> "Instagram"
    "tiktok"          -> "TikTok"
    "facebook"        -> "Facebook"
    "epicgames"       -> "Epic Games"
    "leagueoflegends" -> "League of Legends"
    "riotgames"       -> "Riot Games"
    "crunchyroll"     -> "Crunchyroll"
    "domain"          -> "Domain"
    "paypal"          -> "PayPal"
    "skype"           -> "Skype"
    "ebay"            -> "eBay"
    else              -> type.replaceFirstChar { it.uppercase() }
}

private fun connectionColor(type: String): Color = when (type) {
    "spotify"         -> Color(0xFF1DB954)
    "steam"           -> Color(0xFF66C0F4)
    "github"          -> Color(0xFF8B949E)
    "twitch"          -> Color(0xFF9147FF)
    "youtube"         -> Color(0xFFFF0000)
    "twitter"         -> Color(0xFF1D9BF0)
    "reddit"          -> Color(0xFFFF4500)
    "playstation"     -> Color(0xFF0070D1)
    "xbox"            -> Color(0xFF107C10)
    "battlenet"       -> Color(0xFF009AE4)
    "instagram"       -> Color(0xFFE1306C)
    "tiktok"          -> Color(0xFFEE1D52)
    "epicgames"       -> Color(0xFF2ECC40)
    "leagueoflegends" -> Color(0xFFC89B3C)
    "riotgames"       -> Color(0xFFFF4655)
    else              -> AppColors.Primary
}

private fun badgeImageUrl(flag: String): String = when (flag) {
    "Staff"           -> "https://cdn.discordapp.com/badge-icons/5e74e9b61934fc1f67c65515d1f7e60d.png"
    "Partner"         -> "https://cdn.discordapp.com/badge-icons/3f9748e53446a137a052f3454e2de41e.png"
    "HypeSquad"       -> "https://cdn.discordapp.com/badge-icons/bf01d1073931f921909045f3a39fd264.png"
    "Bug Hunter"      -> "https://cdn.discordapp.com/badge-icons/2717692c7dca7289b35297368a940dd0.png"
    "Bravery"         -> "https://cdn.discordapp.com/badge-icons/8a88d63823d8a71cd5e390baa45efa02.png"
    "Brilliance"      -> "https://cdn.discordapp.com/badge-icons/011940fd013082d85d0ceb1ce53ffe02.png"
    "Balance"         -> "https://cdn.discordapp.com/badge-icons/3aa41de486fa12454c3761e8e223442e.png"
    "Early Supporter" -> "https://cdn.discordapp.com/badge-icons/7060786766c9c840eb3019e725d2b358.png"
    "Bug Hunter Lv.2" -> "https://cdn.discordapp.com/badge-icons/848f79194d4be5ff5f81505cbd0ce1e6.png"
    "Bot Developer"   -> "https://cdn.discordapp.com/badge-icons/6f9e37f9029ff57aef81db857890005e.png"
    "Active Dev"      -> "https://cdn.discordapp.com/badge-icons/6f9e37f9029ff57aef81db857890005e.png"
    "Nitro"           -> "https://cdn.discordapp.com/badge-icons/2ba85e8026a8614b640c2837bcdfe21b.png"
    "Nitro Classic"   -> "https://cdn.discordapp.com/badge-icons/f1e96cb3268ee9f1e39c4c74fa7e9ae5.png"
    "Nitro Basic"     -> "https://cdn.discordapp.com/badge-icons/2ba85e8026a8614b640c2837bcdfe21b.png"
    else              -> ""
}

private fun resolvePresenceStatus(sessions: JSONArray?): Pair<String, List<String>> {
    if (sessions == null || sessions.length() == 0) return "offline" to emptyList()
    val priority = mapOf("dnd" to 3, "online" to 2, "idle" to 1, "invisible" to 0, "offline" to -1)
    var bestStatus = "offline"
    val platforms = mutableListOf<String>()
    for (i in 0 until sessions.length()) {
        val s = sessions.getJSONObject(i)
        val st = s.optString("status", "offline")
        if ((priority[st] ?: -1) > (priority[bestStatus] ?: -1)) bestStatus = st
        val ci = s.optJSONObject("client_info")
        if (ci != null) {
            when (ci.optString("client")) {
                "mobile"  -> if (!platforms.contains("Mobile"))  platforms.add("Mobile")
                "desktop" -> if (!platforms.contains("Desktop")) platforms.add("Desktop")
                "web"     -> if (!platforms.contains("Web"))     platforms.add("Web")
            }
        }
    }
    return bestStatus to platforms
}

private fun parseActivitiesFromEvent(data: JSONObject): List<PresenceActivity> {
    val list = mutableListOf<PresenceActivity>()
    val acts = data.optJSONArray("activities") ?: return list
    for (j in 0 until acts.length()) {
        val a = acts.getJSONObject(j)
        val type = a.optInt("type", 0)
        if (type == 4) continue
        val name = a.optString("name").takeIf { it.isNotEmpty() } ?: continue

        val ts = a.optJSONObject("timestamps")
        val richTs = if (ts != null) RichTimestamps(
            start = if (!ts.isNull("start")) ts.optLong("start") else null,
            end   = if (!ts.isNull("end"))   ts.optLong("end")   else null
        ) else null

        val as_ = a.optJSONObject("assets")
        val appId = a.optString("application_id").takeIf { it.isNotEmpty() && it != "null" }
        var albumUrl: String? = null
        val richAs = if (as_ != null) {
            val li = as_.optString("large_image").takeIf { it.isNotEmpty() && it != "null" }
            val lt = as_.optString("large_text").takeIf { it.isNotEmpty() && it != "null" }
            val si = as_.optString("small_image").takeIf { it.isNotEmpty() && it != "null" }
            val st = as_.optString("small_text").takeIf { it.isNotEmpty() && it != "null" }
            if (type == 2 && li != null) {
                albumUrl = if (li.startsWith("spotify:")) {
                    val spotifyId = li.removePrefix("spotify:")
                    "https://i.scdn.co/image/$spotifyId"
                } else if (appId != null) {
                    "https://cdn.discordapp.com/app-assets/$appId/${li}.png"
                } else null
            }
            RichAssets(li, lt, si, st)
        } else null

        if (list.none { it.name == name && it.type == type }) {
            list.add(PresenceActivity(
                type          = type,
                name          = name,
                details       = a.optString("details").takeIf { it.isNotEmpty() && it != "null" },
                state         = a.optString("state").takeIf { it.isNotEmpty() && it != "null" },
                applicationId = appId,
                timestamps    = richTs,
                assets        = richAs,
                syncId        = a.optString("sync_id").takeIf { it.isNotEmpty() && it != "null" },
                albumCoverUrl = albumUrl
            ))
        }
    }
    return list
}

private fun parseActivitiesFromSessions(sessions: JSONArray?): List<PresenceActivity> {
    val list = mutableListOf<PresenceActivity>()
    if (sessions == null) return list
    for (i in 0 until sessions.length()) {
        val s = sessions.getJSONObject(i)
        val acts = s.optJSONArray("activities") ?: continue
        for (j in 0 until acts.length()) {
            val a = acts.getJSONObject(j)
            val type = a.optInt("type", 0)
            if (type == 4) continue
            val name = a.optString("name").takeIf { it.isNotEmpty() } ?: continue

            val ts = a.optJSONObject("timestamps")
            val richTs = if (ts != null) RichTimestamps(
                start = if (!ts.isNull("start")) ts.optLong("start") else null,
                end   = if (!ts.isNull("end"))   ts.optLong("end")   else null
            ) else null

            val as_ = a.optJSONObject("assets")
            val appId = a.optString("application_id").takeIf { it.isNotEmpty() && it != "null" }
            var albumUrl: String? = null
            val richAs = if (as_ != null) {
                val li = as_.optString("large_image").takeIf { it.isNotEmpty() && it != "null" }
                val lt = as_.optString("large_text").takeIf { it.isNotEmpty() && it != "null" }
                val si = as_.optString("small_image").takeIf { it.isNotEmpty() && it != "null" }
                val st = as_.optString("small_text").takeIf { it.isNotEmpty() && it != "null" }
                if (type == 2 && li != null) {
                    albumUrl = if (li.startsWith("spotify:")) {
                        val spotifyId = li.removePrefix("spotify:")
                        "https://i.scdn.co/image/$spotifyId"
                    } else if (appId != null) {
                        "https://cdn.discordapp.com/app-assets/$appId/${li}.png"
                    } else null
                }
                RichAssets(li, lt, si, st)
            } else null

            if (list.none { it.name == name && it.type == type }) {
                list.add(PresenceActivity(
                    type          = type,
                    name          = name,
                    details       = a.optString("details").takeIf { it.isNotEmpty() && it != "null" },
                    state         = a.optString("state").takeIf { it.isNotEmpty() && it != "null" },
                    applicationId = appId,
                    timestamps    = richTs,
                    assets        = richAs,
                    syncId        = a.optString("sync_id").takeIf { it.isNotEmpty() && it != "null" },
                    albumCoverUrl = albumUrl
                ))
            }
        }
    }
    return list
}

private fun isNewerVersion(latest: String, current: String): Boolean {
    val l = latest.trimStart('v').split(".").mapNotNull { it.toIntOrNull() }
    val c = current.trimStart('v').split(".").mapNotNull { it.toIntOrNull() }
    for (i in 0 until maxOf(l.size, c.size)) {
        val lv = l.getOrElse(i) { 0 }
        val cv = c.getOrElse(i) { 0 }
        if (lv > cv) return true
        if (lv < cv) return false
    }
    return false
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun parseBadges(flags: Long, premium: Int): List<Pair<String, Color>> {
    val list = mutableListOf<Pair<String, Color>>()
    if (premium == 2) list.add("Nitro" to Color(0xFF5865F2))
    if (premium == 1) list.add("Nitro Classic" to Color(0xFF5865F2))
    if (premium == 3) list.add("Nitro Basic" to Color(0xFF5865F2))
    if (flags and 1L      != 0L) list.add("Staff"           to Color(0xFF5865F2))
    if (flags and 2L      != 0L) list.add("Partner"         to Color(0xFF7289DA))
    if (flags and 4L      != 0L) list.add("HypeSquad"       to Color(0xFFFB923C))
    if (flags and 8L      != 0L) list.add("Bug Hunter"      to Color(0xFF22C55E))
    if (flags and 64L     != 0L) list.add("Bravery"         to Color(0xFFA855F7))
    if (flags and 128L    != 0L) list.add("Brilliance"      to Color(0xFFEF4444))
    if (flags and 256L    != 0L) list.add("Balance"         to Color(0xFF22D3EE))
    if (flags and 512L    != 0L) list.add("Early Supporter" to Color(0xFF8B5CF6))
    if (flags and 16384L  != 0L) list.add("Bug Hunter Lv.2" to Color(0xFFF59E0B))
    if (flags and 131072L != 0L) list.add("Bot Developer"   to Color(0xFF6366F1))
    if (flags and 4194304L!= 0L) list.add("Active Dev"      to Color(0xFF10B981))
    return list
}

private fun formatElapsed(startMs: Long): String {
    val elapsed = (System.currentTimeMillis() - startMs) / 1000
    val h = elapsed / 3600
    val m = (elapsed % 3600) / 60
    val s = elapsed % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun formatRemaining(endMs: Long): String {
    val remaining = (endMs - System.currentTimeMillis()) / 1000
    if (remaining <= 0) return "0:00"
    val h = remaining / 3600
    val m = (remaining % 3600) / 60
    val s = remaining % 60
    return if (h > 0) "%d:%02d:%02d left".format(h, m, s) else "%d:%02d left".format(m, s)
}

private sealed class MdBlock {
    data class Heading(val text: String, val level: Int)     : MdBlock()
    data class Blockquote(val text: String)                  : MdBlock()
    data class CodeBlock(val code: String, val lang: String) : MdBlock()
    data class SubText(val text: String)                     : MdBlock()
    data class Paragraph(val text: String)                   : MdBlock()
}

private fun parseBlocks(raw: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = raw.lines()
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("```") -> {
                val lang = line.removePrefix("```").trim()
                val sb = StringBuilder()
                i++
                while (i < lines.size && !lines[i].trimEnd().endsWith("```")) {
                    if (sb.isNotEmpty()) sb.append('\n')
                    sb.append(lines[i]); i++
                }
                blocks.add(MdBlock.CodeBlock(sb.toString(), lang))
            }
            line.startsWith("### ") -> blocks.add(MdBlock.Heading(line.removePrefix("### "), 3))
            line.startsWith("## ")  -> blocks.add(MdBlock.Heading(line.removePrefix("## "), 2))
            line.startsWith("# ")   -> blocks.add(MdBlock.Heading(line.removePrefix("# "), 1))
            line.startsWith("-# ")  -> blocks.add(MdBlock.SubText(line.removePrefix("-# ")))
            line.startsWith("> ")   -> blocks.add(MdBlock.Blockquote(line.removePrefix("> ")))
            line.startsWith(">") && line.length > 1 -> blocks.add(MdBlock.Blockquote(line.drop(1)))
            else -> {
                val pLines = mutableListOf(line)
                while (i + 1 < lines.size) {
                    val next = lines[i + 1]
                    if (next.startsWith("```") || next.startsWith("#") ||
                        next.startsWith(">")   || next.startsWith("-#")) break
                    i++; pLines.add(lines[i])
                }
                if (pLines.any { it.isNotBlank() })
                    blocks.add(MdBlock.Paragraph(pLines.joinToString("\n")))
            }
        }
        i++
    }
    return blocks
}

private data class InlinePattern(val marker: String, val style: SpanStyle, val isCode: Boolean = false)

private val inlinePatterns = listOf(
    InlinePattern("***", SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)),
    InlinePattern("**",  SpanStyle(fontWeight = FontWeight.Bold)),
    InlinePattern("__",  SpanStyle(textDecoration = TextDecoration.Underline)),
    InlinePattern("~~",  SpanStyle(textDecoration = TextDecoration.LineThrough)),
    InlinePattern("||",  SpanStyle(color = AppColors.SurfaceVar, background = AppColors.SurfaceVar)),
    InlinePattern("`",   SpanStyle(fontFamily = FontFamily.Monospace, background = AppColors.CodeBg, color = Color(0xFFE8739C)), isCode = true),
    InlinePattern("*",   SpanStyle(fontStyle = FontStyle.Italic)),
    InlinePattern("_",   SpanStyle(fontStyle = FontStyle.Italic))
)

private fun AnnotatedString.Builder.appendStyled(text: String, baseColor: Color) {
    var i = 0
    while (i < text.length) {
        var matched = false
        for (p in inlinePatterns) {
            if (text.startsWith(p.marker, i)) {
                val cs = i + p.marker.length
                val ci = text.indexOf(p.marker, cs)
                if (ci > cs) {
                    val inner = text.substring(cs, ci)
                    withStyle(p.style) {
                        if (p.isCode) append(inner) else appendStyled(inner, baseColor)
                    }
                    i = ci + p.marker.length; matched = true; break
                }
            }
        }
        if (!matched) { append(text[i]); i++ }
    }
}

private fun parseInline(text: String, base: Color = AppColors.TextSecondary): AnnotatedString =
    buildAnnotatedString { withStyle(SpanStyle(color = base)) { appendStyled(text, base) } }

@Composable
private fun DiscordMarkdown(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        parseBlocks(text).forEach { block ->
            when (block) {
                is MdBlock.Heading -> Text(
                    text = parseInline(block.text, AppColors.TextPrimary),
                    fontSize = when (block.level) { 1 -> 22.sp; 2 -> 18.sp; else -> 16.sp },
                    fontWeight = FontWeight.Bold,
                    lineHeight = when (block.level) { 1 -> 28.sp; 2 -> 24.sp; else -> 22.sp }
                )
                is MdBlock.SubText -> Text(
                    text = parseInline(block.text, AppColors.TextMuted),
                    fontSize = 11.sp, lineHeight = 16.sp
                )
                is MdBlock.Blockquote -> Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                        .background(AppColors.Surface.copy(0.5f), RoundedCornerShape(Radius.Small))
                        .padding(vertical = 6.dp)
                ) {
                    Box(modifier = Modifier.width(3.dp).fillMaxHeight()
                        .background(AppColors.TextMuted.copy(0.6f), RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(text = parseInline(block.text, AppColors.TextSecondary),
                        fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(end = 8.dp))
                }
                is MdBlock.CodeBlock -> Box(modifier = Modifier.fillMaxWidth()
                    .background(AppColors.CodeBg, RoundedCornerShape(Radius.Medium)).padding(12.dp)) {
                    Column {
                        if (block.lang.isNotEmpty()) Text(block.lang, fontSize = 10.sp,
                            color = AppColors.TextMuted, fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 4.dp))
                        Text(block.code, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE8739C), lineHeight = 18.sp)
                    }
                }
                is MdBlock.Paragraph -> Text(
                    text = parseInline(block.text, AppColors.TextSecondary),
                    fontSize = 14.sp, lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun ParticleBackground(modifier: Modifier = Modifier) {
    data class Particle(val x: Float, val y: Float, val speed: Float,
        val size: Float, val alpha: Float, val phase: Float, val wobble: Float)
    data class Glob(val x: Float, val y: Float, val r: Float, val color: Color, val phase: Float)
    val particles = remember {
        List(80) {
            Particle(Random.nextFloat(), Random.nextFloat(),
                Random.nextFloat() * 0.5f + 0.1f, Random.nextFloat() * 2.5f + 0.5f,
                Random.nextFloat() * 0.4f + 0.05f, Random.nextFloat() * 6.28f,
                Random.nextFloat() * 0.08f + 0.02f)
        }
    }
    val globs = remember {
        listOf(
            Glob(0.15f, 0.25f, 420f, Color(0xFF5865F2), 0.47f),
            Glob(0.80f, 0.60f, 350f, Color(0xFF7B5EA7), 2.51f),
            Glob(0.50f, 0.85f, 280f, Color(0xFF3B4EC8), 1.57f),
            Glob(0.70f, 0.10f, 220f, Color(0xFF5865F2), 3.14f),
            Glob(0.30f, 0.70f, 180f, Color(0xFF9B59B6), 0.92f)
        )
    }
    val transition = rememberInfiniteTransition(label = "bg")
    val time by transition.animateFloat(0f, 10000f,
        infiniteRepeatable(tween(80000, easing = LinearEasing)), label = "t")
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        globs.forEach { g ->
            val gx = g.x * w + sin(time * 0.00025f + g.phase) * 80f
            val gy = g.y * h + cos(time * 0.0003f + g.phase) * 70f
            drawCircle(color = g.color.copy(alpha = 0.09f), radius = g.r, center = Offset(gx, gy))
        }
        particles.forEach { p ->
            val px = ((p.x + sin(time * 0.0015f * p.speed + p.phase) * p.wobble).mod(1f) + 1f).mod(1f) * w
            val py = ((p.y + time * p.speed * 0.00032f).mod(1f)) * h
            drawCircle(color = Color.White.copy(alpha = p.alpha), radius = p.size, center = Offset(px, py))
        }
    }
}

class MainActivity : ComponentActivity() {

    private var gatewayWs: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var gatewaySequence = -1
    private var gatewayToken: String? = null
    private var onPresenceUpdate: ((DiscordPresence) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCrashHandler()
        val crashPrefs = getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE)
        val crashTrace = crashPrefs.getString(KEY_CRASH_TRACE, null)
        if (crashTrace != null) crashPrefs.edit().remove(KEY_CRASH_TRACE).apply()
        val savedToken = getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE)
            .getString(KEY_TOKEN, null)?.takeIf { it.isNotEmpty() }
        setContent {
            MaterialTheme(colorScheme = DiscordDarkColorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = AppColors.Background) {
                    if (crashTrace != null) CrashScreen(crashTrace) else DiscordTokenApp(savedToken)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        heartbeatJob?.cancel()
        gatewayWs?.close(1000, null)
    }

    private fun saveToken(token: String) {
        getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).apply()
    }

    private fun clearToken() {
        getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE).edit().remove(KEY_TOKEN).apply()
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
    }

    private fun setupCrashHandler() {
        val def = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
                val log = "Discord Token — Crash Report\nManufacturer: ${Build.MANUFACTURER}\n" +
                    "Device: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\nStacktrace:\n$sw"
                getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE).edit().putString(KEY_CRASH_TRACE, log).commit()
                startActivity(Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            } catch (_: Exception) { def?.uncaughtException(t, e) }
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(2)
        }
    }

    @Composable
    fun CrashScreen(trace: String) {
        val ctx = LocalContext.current
        Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, null, tint = AppColors.Error, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("App Crashed",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = AppColors.TextPrimary)
                }
                TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) {
                    Text("Close", color = AppColors.Error, fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                Text("An unexpected error occurred. Copy the log and report it to the developer.",
                    style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp))
                Button(onClick = {
                    (ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("Crash Log", trace))
                }, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(Radius.Button)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy Log", fontWeight = FontWeight.Bold, color = AppColors.OnPrimary)
                }
                Card(modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.ErrorContainer),
                    shape = RoundedCornerShape(Radius.Card)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(trace, modifier = Modifier.padding(14.dp),
                                fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                                color = AppColors.OnError, lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DiscordTokenApp(initialToken: String?) {
        val ctx = LocalContext.current
        var token            by remember { mutableStateOf(initialToken) }
        var user             by remember { mutableStateOf<DiscordUser?>(null) }
        var presence         by remember { mutableStateOf<DiscordPresence?>(null) }
        var connections      by remember { mutableStateOf<List<DiscordConnection>?>(null) }
        var guildCount       by remember { mutableStateOf<Int?>(null) }
        var loadingUser      by remember { mutableStateOf(false) }
        var loadingPresence  by remember { mutableStateOf(false) }
        var loadingConns     by remember { mutableStateOf(false) }
        var loadingGuilds    by remember { mutableStateOf(false) }
        var showWebView      by remember { mutableStateOf(false) }
        var updateTag        by remember { mutableStateOf<String?>(null) }
        var noInternet       by remember { mutableStateOf(false) }
        var showLogs         by remember { mutableStateOf(false) }
        var footerClickCount by remember { mutableIntStateOf(0) }
        var logOnceEnabled   by remember { mutableStateOf(true) }

        logsEnabled.value = logOnceEnabled

        LaunchedEffect(Unit) {
            if (!isNetworkAvailable(ctx)) { noInternet = true; return@LaunchedEffect }
            try {
                val latest = checkLatestVersion()
                if (latest != null && isNewerVersion(latest, CURRENT_VERSION)) updateTag = latest
            } catch (_: Exception) {}
        }

        LaunchedEffect(token) {
            val t = token ?: return@LaunchedEffect
            addLog("INFO", "Auth", "Starting data fetch for token", "Token: ${t.take(20)}...")

            loadingUser = true
            try {
                val u = fetchUserInfo(t)
                user = u
                loadingUser = false
                addLog("SUCCESS", "API", "User info fetched: ${u.username}", "HTTP 200 GET /users/@me")
            } catch (e: Exception) {
                loadingUser = false
                addLog("ERROR", "API", "Failed to fetch user: ${e.message}", e.toString())
                if (initialToken != null && user == null) { clearToken(); token = null }
                return@LaunchedEffect
            }

            loadingPresence = true
            launch {
                try {
                    val p = fetchPresenceViaGateway(t) { updated ->
                        presence = updated
                        addLog("INFO", "Gateway", "Presence updated live", updated.status)
                    }
                    presence = p
                    loadingPresence = false
                    addLog("SUCCESS", "Gateway", "Presence loaded: ${p?.status}", "Activities: ${p?.activities?.size}")
                } catch (e: Exception) {
                    loadingPresence = false
                    addLog("ERROR", "Gateway", "Presence fetch failed: ${e.message}", e.toString())
                }
            }

            loadingConns = true
            launch {
                try {
                    val c = fetchConnections(t)
                    connections = c
                    loadingConns = false
                    addLog("SUCCESS", "API", "Connections fetched: ${c.size}", "HTTP 200 GET /users/@me/connections")
                } catch (e: Exception) {
                    loadingConns = false
                    connections = emptyList()
                    addLog("ERROR", "API", "Connections fetch failed: ${e.message}", e.toString())
                }
            }

            loadingGuilds = true
            launch {
                try {
                    val g = fetchGuildCount(t)
                    guildCount = g
                    loadingGuilds = false
                    addLog("SUCCESS", "API", "Guild count: $g", "HTTP 200 GET /users/@me/guilds")
                } catch (e: Exception) {
                    loadingGuilds = false
                    addLog("ERROR", "API", "Guild count failed: ${e.message}", e.toString())
                }
            }
        }

        if (noInternet) {
            AlertDialog(
                onDismissRequest = { noInternet = false },
                icon = { Icon(Icons.Outlined.WifiOff, null, tint = AppColors.Error, modifier = Modifier.size(32.dp)) },
                title = { Text("No Internet Connection", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("This app requires an internet connection to work.", color = AppColors.TextSecondary) },
                confirmButton = {
                    Button(onClick = { noInternet = false; if (!isNetworkAvailable(ctx)) noInternet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(Radius.Button)) {
                        Text("Retry", color = AppColors.OnPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { noInternet = false }) { Text("Dismiss", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card)
            )
        }

        if (updateTag != null) {
            AlertDialog(
                onDismissRequest = { updateTag = null },
                icon = { Icon(Icons.Outlined.Update, null, tint = AppColors.Primary, modifier = Modifier.size(32.dp)) },
                title = { Text("Update Available", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("Version ${updateTag!!.trimStart('v')} is available. You are on v$CURRENT_VERSION.", color = AppColors.TextSecondary) },
                confirmButton = {
                    Button(onClick = {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://github.com/Sc-Rhyan57/GetDiscordToken/releases/tag/$updateTag")))
                        updateTag = null
                    }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(Radius.Button)) {
                        Text("Update", color = AppColors.OnPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { updateTag = null }) { Text("Continue", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card)
            )
        }

        if (showLogs) {
            LogsScreen(
                onClose = { showLogs = false },
                logsEnabled = logOnceEnabled,
                onToggleLogs = { logOnceEnabled = it; logsEnabled.value = it }
            )
        }

        val screen = when {
            showWebView && token == null -> "webview"
            token == null               -> "login"
            else                        -> "profile"
        }

        AnimatedContent(targetState = screen,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "nav") { s ->
            when (s) {
                "webview" -> WebViewScreen(
                    onTokenReceived = { t ->
                        addLog("SUCCESS", "Token", "Token extracted successfully", "Token length: ${t.length}")
                        token = t; saveToken(t); showWebView = false
                    },
                    onBack = { showWebView = false }
                )
                "login"   -> LoginScreen(
                    onLoginClick = { showWebView = true },
                    footerClickCount = footerClickCount,
                    onFooterClick = {
                        footerClickCount++
                        if (footerClickCount >= 5) {
                            showLogs = true
                            footerClickCount = 0
                        }
                    }
                )
                else      -> UserProfileScreen(
                    user = user,
                    token = token ?: "",
                    loadingUser = loadingUser,
                    loadingPresence = loadingPresence,
                    loadingConns = loadingConns,
                    loadingGuilds = loadingGuilds,
                    presence = presence,
                    connections = connections,
                    guildCount = guildCount,
                    footerClickCount = footerClickCount,
                    onFooterClick = {
                        footerClickCount++
                        if (footerClickCount >= 5) {
                            showLogs = true
                            footerClickCount = 0
                        }
                    },
                    onLogout = {
                        addLog("INFO", "Auth", "User logged out")
                        heartbeatJob?.cancel()
                        gatewayWs?.close(1000, null)
                        clearToken(); token = null; user = null
                        presence = null; connections = null; guildCount = null
                    }
                )
            }
        }
    }

    @Composable
    fun LogsScreen(onClose: () -> Unit, logsEnabled: Boolean, onToggleLogs: (Boolean) -> Unit) {
        Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth()
                        .background(AppColors.Surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Terminal, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("App Console", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                            Spacer(Modifier.width(8.dp))
                            Box(modifier = Modifier
                                .background(AppColors.Primary.copy(0.15f), RoundedCornerShape(Radius.Badge))
                                .padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text("${appLogs.size}", fontSize = 10.sp, color = AppColors.Primary, fontWeight = FontWeight.Bold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Logging", fontSize = 11.sp, color = AppColors.TextMuted)
                            Spacer(Modifier.width(6.dp))
                            Switch(
                                checked = logsEnabled,
                                onCheckedChange = onToggleLogs,
                                modifier = Modifier.height(24.dp),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.Primary,
                                    checkedTrackColor = AppColors.Primary.copy(0.3f)
                                )
                            )
                            Spacer(Modifier.width(10.dp))
                            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Outlined.Close, null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    if (appLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Terminal, null, tint = AppColors.TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No logs yet", color = AppColors.TextMuted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                            items(appLogs) { log ->
                                var expanded by remember { mutableStateOf(false) }
                                Column(modifier = Modifier.fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .background(AppColors.Surface.copy(0.5f), RoundedCornerShape(Radius.Small))
                                    .clickable(enabled = log.detail != null) { expanded = !expanded }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val levelColor = when (log.level) {
                                            "SUCCESS" -> AppColors.Success
                                            "ERROR"   -> AppColors.Error
                                            "WARN"    -> AppColors.Warning
                                            else      -> AppColors.Primary
                                        }
                                        Text(log.timestamp, fontSize = 10.sp, color = AppColors.TextMuted,
                                            fontFamily = FontFamily.Monospace)
                                        Spacer(Modifier.width(6.dp))
                                        Box(modifier = Modifier
                                            .background(levelColor.copy(0.15f), RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)) {
                                            Text(log.level, fontSize = 9.sp, color = levelColor,
                                                fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        }
                                        Spacer(Modifier.width(6.dp))
                                        Text("[${log.tag}]", fontSize = 10.sp, color = AppColors.Primary,
                                            fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(4.dp))
                                        Text(log.message, fontSize = 11.sp, color = AppColors.TextSecondary,
                                            fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f),
                                            maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis)
                                        if (log.detail != null) {
                                            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                                null, tint = AppColors.TextMuted, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    if (expanded && log.detail != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(log.detail, fontSize = 10.sp, color = AppColors.TextMuted,
                                            fontFamily = FontFamily.Monospace, lineHeight = 14.sp,
                                            modifier = Modifier.padding(start = 4.dp))
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
    fun LoginScreen(onLoginClick: () -> Unit, footerClickCount: Int, onFooterClick: () -> Unit) {
        val scale = remember { Animatable(0f) }
        LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
        val shimmerTransition = rememberInfiniteTransition(label = "shimmer")
        val shimmerOffset by shimmerTransition.animateFloat(
            initialValue = -1.5f, targetValue = 1.5f,
            animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "sho"
        )
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(AppColors.LoginBg1, AppColors.LoginBg2, AppColors.LoginBg3))))
            ParticleBackground(modifier = Modifier.fillMaxSize())
            Column(modifier = Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.weight(1f))
                Box(modifier = Modifier.size(96.dp)
                    .background(AppColors.Primary.copy(0.18f), CircleShape)
                    .border(1.5.dp, AppColors.Primary.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Key, null, tint = AppColors.Primary, modifier = Modifier.size(48.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("v$CURRENT_VERSION", fontSize = 10.sp, color = AppColors.TextMuted.copy(0.5f),
                    fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(40.dp))
                Card(modifier = Modifier.fillMaxWidth().scale(scale.value),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface.copy(0.9f)),
                    shape = RoundedCornerShape(Radius.XLarge),
                    elevation = CardDefaults.cardElevation(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(0.2f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .clip(RoundedCornerShape(Radius.Button))
                            .background(AppColors.Primary)
                            .clickable { onLoginClick() },
                            contentAlignment = Alignment.Center) {
                            val shimmerBrush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                start = Offset(shimmerOffset * 400f + 200f, 0f),
                                end = Offset(shimmerOffset * 400f + 350f, 80f)
                            )
                            Box(modifier = Modifier.fillMaxSize().background(shimmerBrush))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.Login, null, modifier = Modifier.size(19.dp), tint = Color.White)
                                Spacer(Modifier.width(10.dp))
                                Text("Sign in with Discord", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            SecurityBadge(Icons.Outlined.Security, "Secure")
                            SecurityBadge(Icons.Outlined.VisibilityOff, "Private")
                            SecurityBadge(Icons.Outlined.Smartphone, "Local")
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Footer(clickCount = footerClickCount, onClick = onFooterClick)
                Spacer(Modifier.height(12.dp))
                GitHubButton()
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    @Composable
    fun SecurityBadge(icon: ImageVector, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = AppColors.Success, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = AppColors.Success, fontWeight = FontWeight.SemiBold)
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        val currentOnTokenReceived by rememberUpdatedState(onTokenReceived)
        val currentOnBack by rememberUpdatedState(onBack)
        val webRef = remember { mutableStateOf<WebView?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(factory = { ctx ->
                WebView(ctx).also { wv ->
                    webRef.value = wv
                    wv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    wv.setBackgroundColor(android.graphics.Color.parseColor("#1E1F22"))
                    wv.settings.javaScriptEnabled = true
                    wv.settings.domStorageEnabled = true
                    wv.settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; SM-S921U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36"
                    wv.webViewClient = object : WebViewClient() {
                        @Deprecated("Deprecated in Java")
                        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (url.contains("/app") || url.endsWith("/channels/@me")) {
                                view.stopLoading()
                                Handler(Looper.getMainLooper()).postDelayed({ view.loadUrl(JS_TOKEN) }, 500)
                                return true
                            }
                            return false
                        }
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            addLog("INFO", "WebView", "Page loaded", url)
                            if (url.contains("/app") || url.endsWith("/channels/@me"))
                                Handler(Looper.getMainLooper()).postDelayed({ view.loadUrl(JS_TOKEN) }, 800)
                        }
                    }
                    wv.webChromeClient = object : WebChromeClient() {
                        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                            result.confirm(); view.visibility = View.GONE
                            if (message.isNotBlank() && message != "undefined")
                                Handler(Looper.getMainLooper()).postDelayed({ currentOnTokenReceived(message.trim()) }, 200)
                            return true
                        }
                    }
                    wv.loadUrl("https://discord.com/login")
                }
            }, modifier = Modifier.fillMaxSize())
            Box(modifier = Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 12.dp)) {
                TextButton(onClick = { currentOnBack() },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = AppColors.Background.copy(0.85f),
                        contentColor = AppColors.TextPrimary),
                    shape = RoundedCornerShape(Radius.Medium)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                webRef.value?.apply {
                    stopLoading(); loadUrl("about:blank")
                    Handler(Looper.getMainLooper()).postDelayed({ clearHistory(); destroy() }, 500)
                }
                webRef.value = null
            }
        }
    }

    @Composable
    fun UserProfileScreen(
        user: DiscordUser?,
        token: String,
        loadingUser: Boolean,
        loadingPresence: Boolean,
        loadingConns: Boolean,
        loadingGuilds: Boolean,
        presence: DiscordPresence?,
        connections: List<DiscordConnection>?,
        guildCount: Int?,
        footerClickCount: Int,
        onFooterClick: () -> Unit,
        onLogout: () -> Unit
    ) {
        val ctx = LocalContext.current
        var showToken    by remember { mutableStateOf(false) }
        var copied       by remember { mutableStateOf(false) }
        var expandConns  by remember { mutableStateOf(false) }
        val scale = remember { Animatable(0.95f) }
        LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy)) }

        Column(modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())) {

            Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                if (user?.banner != null) {
                    AsyncImage(
                        model = "https://cdn.discordapp.com/banners/${user.id}/${user.banner}.png?size=600",
                        contentDescription = null, modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)
                } else {
                    val bannerBrush = if (user?.accentColor != null) {
                        val c = Color(0xFF000000 or user.accentColor.toLong())
                        Brush.verticalGradient(listOf(c, c.copy(0.4f), AppColors.Background))
                    } else {
                        Brush.verticalGradient(listOf(AppColors.Primary, Color(0xFF7289DA), AppColors.Background))
                    }
                    Box(modifier = Modifier.fillMaxSize().background(bannerBrush))
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.size(90.dp)
                        .border(4.dp, AppColors.Background, CircleShape)
                        .clip(CircleShape).background(AppColors.Surface)) {
                        if (user?.avatar != null) {
                            AsyncImage(
                                model = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=256",
                                contentDescription = null, modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(AppColors.Primary),
                                contentAlignment = Alignment.Center) {
                                Text(user?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 34.sp, fontWeight = FontWeight.Black, color = AppColors.OnPrimary)
                            }
                        }
                    }
                    Button(onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.ErrorContainer, contentColor = AppColors.Error),
                        shape = RoundedCornerShape(Radius.Medium)) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (loadingUser) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
                    }
                } else if (user != null) {

                    ProfileSection("Access Token", Icons.Outlined.Fingerprint) {
                        Button(
                            onClick = {
                                (ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager)
                                    .setPrimaryClip(ClipData.newPlainText("Discord Token", token))
                                copied = true
                                addLog("INFO", "Token", "Token copied to clipboard")
                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(2000)
                                    copied = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (copied) AppColors.Success.copy(0.8f) else AppColors.Success),
                            shape = RoundedCornerShape(Radius.Token)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(if (copied) "Copied!" else "Copy Token", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVar),
                            shape = RoundedCornerShape(Radius.Token)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(text = if (showToken) token else "•".repeat(40),
                                    fontFamily = FontFamily.Monospace, fontSize = 12.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = if (showToken) Int.MAX_VALUE else 1,
                                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                IconButton(onClick = { showToken = !showToken }, modifier = Modifier.size(36.dp)) {
                                    Icon(imageVector = if (showToken) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = AppColors.Divider)
                    Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(user.globalName ?: user.username, fontSize = 26.sp,
                            fontWeight = FontWeight.Black, color = AppColors.TextPrimary)
                        if (presence != null) {
                            val sc = statusColor(presence.status)
                            Row(verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(sc.copy(0.12f), RoundedCornerShape(Radius.Badge))
                                    .border(1.dp, sc.copy(0.25f), RoundedCornerShape(Radius.Badge))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Box(modifier = Modifier.size(7.dp).background(sc, CircleShape))
                                Spacer(Modifier.width(5.dp))
                                Text(statusLabel(presence.status), fontSize = 11.sp,
                                    color = sc, fontWeight = FontWeight.SemiBold)
                            }
                        } else if (loadingPresence) {
                            Box(modifier = Modifier
                                .background(AppColors.TextMuted.copy(0.12f), RoundedCornerShape(Radius.Badge))
                                .padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("Loading...", fontSize = 11.sp, color = AppColors.TextMuted)
                            }
                        }
                    }

                    Text("@${user.username}" + if (user.discriminator != "0") "#${user.discriminator}" else "",
                        fontSize = 15.sp, color = AppColors.TextMuted, fontWeight = FontWeight.Medium)

                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(AppColors.Success.copy(0.12f), RoundedCornerShape(Radius.Small))
                            .padding(horizontal = 10.dp, vertical = 5.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = AppColors.Success, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Token obtained", fontSize = 12.sp, color = AppColors.Success, fontWeight = FontWeight.SemiBold)
                    }

                    if (presence != null && presence.platforms.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            presence.platforms.forEach { platform ->
                                Box(modifier = Modifier
                                    .background(AppColors.Primary.copy(0.10f), RoundedCornerShape(Radius.Badge))
                                    .border(1.dp, AppColors.Primary.copy(0.20f), RoundedCornerShape(Radius.Badge))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text(platform, fontSize = 10.sp, color = AppColors.Primary.copy(0.9f),
                                        fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    val badges = parseBadges(user.publicFlags, user.premiumType)
                    if (badges.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            badges.forEach { (label, color) ->
                                val imgUrl = badgeImageUrl(label)
                                Row(modifier = Modifier
                                    .background(color.copy(0.12f), RoundedCornerShape(Radius.Badge))
                                    .border(1.dp, color.copy(0.35f), RoundedCornerShape(Radius.Badge))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    if (imgUrl.isNotEmpty()) {
                                        AsyncImage(model = imgUrl, contentDescription = label,
                                            modifier = Modifier.size(16.dp).clip(CircleShape))
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Text(label, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = AppColors.Divider)
                        Spacer(Modifier.height(16.dp))
                        ProfileSection("About Me", Icons.Outlined.Notes) {
                            DiscordMarkdown(user.bio, modifier = Modifier.fillMaxWidth())
                        }
                    }

                    if (loadingPresence) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = AppColors.Divider)
                        Spacer(Modifier.height(16.dp))
                        ProfileSection("Current Activity", Icons.Outlined.Games) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Loading activity...", fontSize = 13.sp, color = AppColors.TextMuted)
                            }
                        }
                    } else if (presence != null && presence.activities.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = AppColors.Divider)
                        Spacer(Modifier.height(16.dp))
                        ProfileSection("Current Activity", Icons.Outlined.Games) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                presence.activities.forEach { activity ->
                                    ActivityCard(activity)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = AppColors.Divider)
                    Spacer(Modifier.height(16.dp))

                    ProfileSection("Account Info", Icons.Outlined.Tag) {
                        InfoRow(Icons.Outlined.Tag, "ID", user.id)
                        InfoRow(Icons.Outlined.CalendarMonth, "Created", snowflakeToDate(user.id))
                        if (!user.email.isNullOrBlank()) InfoRow(Icons.Outlined.Email, "Email", user.email)
                        if (!user.phone.isNullOrBlank()) InfoRow(Icons.Outlined.Phone, "Phone", user.phone)
                        if (!user.locale.isNullOrBlank()) InfoRow(Icons.Outlined.Language, "Locale", localeLabel(user.locale))
                        InfoRow(Icons.Outlined.Lock, "2FA",
                            if (user.mfaEnabled) "Enabled" else "Disabled",
                            if (user.mfaEnabled) AppColors.Success else AppColors.TextMuted)
                        InfoRow(Icons.Outlined.CheckCircle, "Verified",
                            if (user.verified) "Yes" else "No",
                            if (user.verified) AppColors.Success else AppColors.TextMuted)
                        if (user.premiumType > 0) InfoRow(Icons.Outlined.WorkspacePremium, "Nitro",
                            nitroLabel(user.premiumType), Color(0xFF5865F2))

                        if (loadingGuilds) {
                            InfoRow(Icons.Outlined.Groups, "Servers", "Loading...")
                        } else if (guildCount != null) {
                            InfoRow(Icons.Outlined.Groups, "Servers",
                                guildCount.toString() + if (guildCount >= 100) "+" else "")
                        }

                        if (!user.bannerColor.isNullOrBlank()) {
                            val parsedAccentColor = runCatching {
                                Color(android.graphics.Color.parseColor(user.bannerColor))
                            }.getOrNull()
                            if (parsedAccentColor != null) {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.AutoAwesome, null,
                                        tint = AppColors.TextMuted, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Accent", fontSize = 13.sp, color = AppColors.TextMuted,
                                        modifier = Modifier.width(80.dp))
                                    Box(modifier = Modifier.size(20.dp)
                                        .background(parsedAccentColor, RoundedCornerShape(4.dp))
                                        .border(1.dp, AppColors.Divider, RoundedCornerShape(4.dp)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(user.bannerColor.uppercase(), fontSize = 13.sp,
                                        color = AppColors.TextPrimary, fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = AppColors.Divider)
                    Spacer(Modifier.height(16.dp))

                    if (loadingConns) {
                        ProfileSection("Connected Accounts", Icons.Outlined.Link) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.Primary)
                                Spacer(Modifier.width(8.dp))
                                Text("Loading connections...", fontSize = 13.sp, color = AppColors.TextMuted)
                            }
                        }
                    } else if (connections != null && connections.isNotEmpty()) {
                        val visibleConns = if (expandConns) connections else connections.take(3)
                        ProfileSection("Connected Accounts (${connections.size})", Icons.Outlined.Link) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                visibleConns.forEach { conn ->
                                    val cc = connectionColor(conn.type)
                                    Row(modifier = Modifier.fillMaxWidth()
                                        .background(cc.copy(0.06f), RoundedCornerShape(Radius.Medium))
                                        .border(1.dp, cc.copy(0.20f), RoundedCornerShape(Radius.Medium))
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(30.dp)
                                            .background(cc.copy(0.18f), RoundedCornerShape(Radius.Small)),
                                            contentAlignment = Alignment.Center) {
                                            Text(connectionLabel(conn.type).first().toString(),
                                                fontSize = 14.sp, color = cc, fontWeight = FontWeight.Black)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(connectionLabel(conn.type), fontSize = 11.sp,
                                                color = cc, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                                            Text(conn.name, fontSize = 13.sp,
                                                color = AppColors.TextPrimary, fontWeight = FontWeight.Medium,
                                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        if (conn.verified) {
                                            Box(modifier = Modifier
                                                .background(AppColors.Success.copy(0.12f), RoundedCornerShape(Radius.Badge))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)) {
                                                Text("✓", fontSize = 11.sp, color = AppColors.Success, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                if (connections.size > 3) {
                                    TextButton(onClick = { expandConns = !expandConns },
                                        modifier = Modifier.fillMaxWidth()) {
                                        Icon(if (expandConns) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                            null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (expandConns) "Show less" else "Show ${connections.size - 3} more",
                                            fontSize = 12.sp, color = AppColors.Primary)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = AppColors.Divider)
                        Spacer(Modifier.height(16.dp))
                    }

                    Spacer(Modifier.height(28.dp))
                    Footer(clickCount = footerClickCount, onClick = onFooterClick)
                    Spacer(Modifier.height(10.dp))
                    GitHubButton()
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    @Composable
    fun ActivityCard(activity: PresenceActivity) {
        val isSpotify = activity.type == 2
        val accentColor = if (isSpotify) AppColors.Spotify else AppColors.Primary

        var elapsed by remember { mutableStateOf("") }
        var remaining by remember { mutableStateOf("") }
        LaunchedEffect(activity.timestamps) {
            while (true) {
                val ts = activity.timestamps
                if (ts?.start != null) elapsed = formatElapsed(ts.start)
                if (ts?.end != null) remaining = formatRemaining(ts.end)
                delay(1000)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = accentColor.copy(0.07f)),
            shape = RoundedCornerShape(Radius.Card),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.22f))) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    if (activity.albumCoverUrl != null) {
                        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(Radius.Small))) {
                            AsyncImage(model = activity.albumCoverUrl, contentDescription = null,
                                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            if (activity.assets?.smallImage != null && activity.applicationId != null) {
                                AsyncImage(
                                    model = "https://cdn.discordapp.com/app-assets/${activity.applicationId}/${activity.assets.smallImage}.png",
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp).align(Alignment.BottomEnd)
                                        .clip(CircleShape).border(1.dp, AppColors.Background, CircleShape))
                            }
                        }
                    } else {
                        Box(modifier = Modifier.size(56.dp)
                            .background(accentColor.copy(0.15f), RoundedCornerShape(Radius.Small)),
                            contentAlignment = Alignment.Center) {
                            if (activity.applicationId != null && activity.assets?.largeImage != null) {
                                AsyncImage(
                                    model = "https://cdn.discordapp.com/app-assets/${activity.applicationId}/${activity.assets.largeImage}.png",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.Small)),
                                    contentScale = ContentScale.Crop)
                            } else {
                                Icon(if (isSpotify) Icons.Outlined.MusicNote else Icons.Outlined.Games,
                                    null, tint = accentColor, modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(activityTypeLabel(activity.type), fontSize = 10.sp,
                            color = accentColor, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(activity.name, fontSize = 14.sp,
                            color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.details.isNullOrBlank())
                            Text(activity.details, fontSize = 12.sp, color = AppColors.TextSecondary,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.state.isNullOrBlank())
                            Text(activity.state, fontSize = 12.sp, color = AppColors.TextMuted,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.assets?.largeText.isNullOrBlank())
                            Text(activity.assets!!.largeText!!, fontSize = 11.sp, color = AppColors.TextMuted,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (activity.timestamps != null) {
                    Spacer(Modifier.height(8.dp))
                    if (activity.timestamps.start != null && activity.timestamps.end != null) {
                        val startMs = activity.timestamps.start
                        val endMs   = activity.timestamps.end
                        val total   = (endMs - startMs).coerceAtLeast(1L)
                        val progress = ((System.currentTimeMillis() - startMs).toFloat() / total).coerceIn(0f, 1f)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(elapsed, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.width(6.dp))
                            Box(modifier = Modifier.weight(1f).height(3.dp).background(accentColor.copy(0.2f), CircleShape)) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(progress).background(accentColor, CircleShape))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(remaining, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                        }
                    } else if (activity.timestamps.start != null) {
                        Text("$elapsed elapsed", fontSize = 10.sp, color = AppColors.TextMuted)
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                Icon(icon, null, tint = AppColors.TextMuted, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(5.dp))
                Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = AppColors.TextMuted, letterSpacing = 0.8.sp)
            }
            content()
        }
    }

    @Composable
    fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = AppColors.TextPrimary) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppColors.TextMuted, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 13.sp, color = AppColors.TextMuted, modifier = Modifier.width(80.dp))
            Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    @Composable
    fun Footer(clickCount: Int, onClick: () -> Unit) {
        val transition = rememberInfiniteTransition(label = "rgb")
        val hue by transition.animateFloat(0f, 360f,
            infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "hue")
        val c = Color.hsv(hue, 0.75f, 1f)
        val remaining = 5 - (clickCount % 5)
        Row(modifier = Modifier.fillMaxWidth().clickable { onClick() },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AutoAwesome, null, tint = c, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text("By Rhyan57", fontSize = 12.sp, color = c, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Outlined.AutoAwesome, null, tint = c, modifier = Modifier.size(13.dp))
        }
        if (clickCount > 0 && clickCount < 5) {
            Text("Tap ${remaining}x more to open console", fontSize = 10.sp,
                color = AppColors.TextMuted.copy(0.6f), textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth())
        }
    }

    @Composable
    fun GitHubButton() {
        val ctx = LocalContext.current
        OutlinedButton(onClick = {
            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-Rhyan57/GetDiscordToken")))
        }, modifier = Modifier.fillMaxWidth().height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextMuted),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider),
            shape = RoundedCornerShape(Radius.Button)) {
            Icon(Icons.Outlined.Code, null, modifier = Modifier.size(16.dp), tint = AppColors.TextMuted)
            Spacer(Modifier.width(8.dp))
            Text("Sc-Rhyan57/GetDiscordToken", fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
    }

    private suspend fun fetchUserInfo(token: String): DiscordUser = withContext(Dispatchers.IO) {
        addLog("INFO", "API", "GET /users/@me", "Authorization: ${token.take(20)}...")
        val request = Request.Builder()
            .url("https://discord.com/api/v10/users/@me")
            .header("Authorization", token)
            .build()
        val response = httpClient.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response")
        if (!response.isSuccessful) {
            addLog("ERROR", "API", "HTTP ${response.code}", body.take(200))
            throw IOException("HTTP ${response.code}")
        }
        addLog("SUCCESS", "API", "HTTP 200 /users/@me", "Content-Length: ${body.length}")
        val j = JSONObject(body)
        fun str(k: String) = j.optString(k).takeIf { it.isNotEmpty() && it != "null" }
        DiscordUser(
            id            = j.optString("id"),
            username      = j.optString("username"),
            discriminator = j.optString("discriminator", "0"),
            globalName    = str("global_name"),
            avatar        = str("avatar"),
            banner        = str("banner"),
            bannerColor   = str("banner_color"),
            bio           = str("bio"),
            email         = str("email"),
            phone         = str("phone"),
            verified      = j.optBoolean("verified", false),
            mfaEnabled    = j.optBoolean("mfa_enabled", false),
            locale        = str("locale"),
            premiumType   = j.optInt("premium_type", 0),
            publicFlags   = j.optLong("public_flags", 0L),
            accentColor   = if (!j.isNull("accent_color")) j.optInt("accent_color") else null
        )
    }

    private suspend fun fetchPresenceViaGateway(
        token: String,
        onLiveUpdate: (DiscordPresence) -> Unit
    ): DiscordPresence? = withContext(Dispatchers.IO) {
        addLog("INFO", "Gateway", "Connecting to Discord Gateway", GATEWAY_URL)
        heartbeatJob?.cancel()
        gatewayWs?.close(1000, null)
        val deferred = CompletableDeferred<DiscordPresence?>()
        gatewayToken = token

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                addLog("INFO", "Gateway", "WebSocket connected", "HTTP ${response.code}")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val op = json.optInt("op", -1)
                    val seq = json.optInt("s", -1)
                    if (seq > 0) gatewaySequence = seq

                    when (op) {
                        10 -> {
                            val interval = json.getJSONObject("d").getLong("heartbeat_interval")
                            addLog("INFO", "Gateway", "Hello received, heartbeat: ${interval}ms")
                            heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
                                delay((interval * 0.8).toLong())
                                while (true) {
                                    val seq = if (gatewaySequence > 0) gatewaySequence.toString() else "null"
                                    webSocket.send("""{"op":1,"d":$seq}""")
                                    addLog("INFO", "Gateway", "Heartbeat sent", "seq=$seq")
                                    delay(interval)
                                }
                            }
                            webSocket.send(JSONObject().apply {
                                put("op", 2)
                                put("d", JSONObject().apply {
                                    put("token", token)
                                    put("capabilities", 16381)
                                    put("properties", JSONObject().apply {
                                        put("os", "Android")
                                        put("browser", "Discord Android")
                                        put("device", "Android")
                                    })
                                    put("compress", false)
                                })
                            }.toString())
                        }
                        0 -> {
                            val t = json.optString("t")
                            addLog("INFO", "Gateway", "Event: $t")
                            when (t) {
                                "READY" -> {
                                    val data = json.getJSONObject("d")
                                    val sessions = data.optJSONArray("sessions")
                                    val (status, platforms) = resolvePresenceStatus(sessions)
                                    val activities = parseActivitiesFromSessions(sessions)
                                    val presence = DiscordPresence(status, activities, platforms)
                                    addLog("SUCCESS", "Gateway", "READY received", "status=$status activities=${activities.size}")
                                    if (!deferred.isCompleted) deferred.complete(presence)
                                    onLiveUpdate(presence)
                                }
                                "PRESENCE_UPDATE" -> {
                                    val data = json.getJSONObject("d")
                                    val status = data.optString("status", "offline")
                                    val activities = parseActivitiesFromEvent(data)
                                    val presence = DiscordPresence(status, activities, emptyList())
                                    addLog("INFO", "Gateway", "Presence update", "status=$status")
                                    onLiveUpdate(presence)
                                }
                                "SESSION_REPLACE" -> {
                                    val sessions = json.optJSONArray("d")
                                    if (sessions != null) {
                                        val (status, platforms) = resolvePresenceStatus(sessions)
                                        val activities = parseActivitiesFromSessions(sessions)
                                        onLiveUpdate(DiscordPresence(status, activities, platforms))
                                    }
                                }
                            }
                        }
                        11 -> addLog("INFO", "Gateway", "Heartbeat ACK")
                        9  -> {
                            addLog("WARN", "Gateway", "Invalid session")
                            if (!deferred.isCompleted) deferred.complete(null)
                        }
                    }
                } catch (e: Exception) {
                    addLog("ERROR", "Gateway", "Parse error: ${e.message}", e.toString())
                    if (!deferred.isCompleted) deferred.complete(null)
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                addLog("ERROR", "Gateway", "WebSocket failure: ${t.message}", response?.code?.toString())
                if (!deferred.isCompleted) deferred.complete(null)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                addLog("INFO", "Gateway", "WebSocket closed: $code $reason")
            }
        }

        val ws = httpClient.newWebSocket(Request.Builder().url(GATEWAY_URL).build(), listener)
        gatewayWs = ws
        val result = withTimeoutOrNull(15000L) { deferred.await() }
        if (!deferred.isCompleted) {
            deferred.complete(null)
            addLog("WARN", "Gateway", "Timeout waiting for READY")
        }
        result
    }

    private suspend fun fetchConnections(token: String): List<DiscordConnection> = withContext(Dispatchers.IO) {
        addLog("INFO", "API", "GET /users/@me/connections")
        try {
            val response = httpClient.newCall(
                Request.Builder().url("https://discord.com/api/v10/users/@me/connections")
                    .header("Authorization", token).build()
            ).execute()
            if (!response.isSuccessful) {
                addLog("ERROR", "API", "HTTP ${response.code} /connections")
                return@withContext emptyList()
            }
            val body = response.body?.string() ?: return@withContext emptyList()
            val arr = JSONArray(body)
            addLog("SUCCESS", "API", "HTTP 200 /connections", "count=${arr.length()}")
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(DiscordConnection(
                        type       = obj.optString("type"),
                        name       = obj.optString("name"),
                        verified   = obj.optBoolean("verified", false),
                        visibility = obj.optInt("visibility", 0)
                    ))
                }
            }
        } catch (e: Exception) {
            addLog("ERROR", "API", "Connections exception: ${e.message}")
            emptyList()
        }
    }

    private suspend fun fetchGuildCount(token: String): Int? = withContext(Dispatchers.IO) {
        addLog("INFO", "API", "GET /users/@me/guilds")
        try {
            val response = httpClient.newCall(
                Request.Builder().url("https://discord.com/api/v10/users/@me/guilds")
                    .header("Authorization", token).build()
            ).execute()
            if (!response.isSuccessful) {
                addLog("ERROR", "API", "HTTP ${response.code} /guilds")
                return@withContext null
            }
            val body = response.body?.string() ?: return@withContext null
            val count = JSONArray(body).length()
            addLog("SUCCESS", "API", "HTTP 200 /guilds", "count=$count")
            count
        } catch (e: Exception) {
            addLog("ERROR", "API", "Guilds exception: ${e.message}")
            null
        }
    }

    private suspend fun checkLatestVersion(): String? = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.newCall(
                Request.Builder().url(GITHUB_API_LATEST)
                    .header("Accept", "application/vnd.github.v3+json").build()
            ).execute()
            if (!response.isSuccessful) return@withContext null
            val body = response.body?.string() ?: return@withContext null
            JSONObject(body).optString("tag_name").takeIf { it.isNotEmpty() }
        } catch (_: Exception) { null }
    }
}
