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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.Notes
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
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Refresh
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val flags: Long,
    val accentColor: Int?,
    val avatarDecorationAsset: String?,
    val nameplateAsset: String?,
    val nameplateLabel: String?,
    val primaryGuildTag: String?,
    val primaryGuildBadge: String?,
    val primaryGuildId: String?,
    val hasLegacyUsername: Boolean,
    val hasQuestBadge: Boolean,
    val hasOrbsBadge: Boolean
)

data class CustomStatus(
    val text: String?,
    val emojiName: String?,
    val emojiId: String?,
    val emojiAnimated: Boolean
)

data class RichTimestamps(val start: Long?, val end: Long?)

data class RichAssets(
    val largeImage: String?,
    val largeText: String?,
    val smallImage: String?,
    val smallText: String?
)

data class ActivityButton(val label: String, val url: String)

data class PresenceActivity(
    val type: Int,
    val name: String,
    val details: String?,
    val state: String?,
    val applicationId: String?,
    val timestamps: RichTimestamps?,
    val assets: RichAssets?,
    val syncId: String?,
    val largeImageUrl: String?,
    val smallImageUrl: String?,
    val buttons: List<ActivityButton>
)

data class DiscordPresence(
    val status: String,
    val activities: List<PresenceActivity>,
    val platforms: List<String>,
    val customStatus: CustomStatus?
)

data class DiscordConnection(
    val type: String,
    val name: String,
    val verified: Boolean,
    val visibility: Int
)

data class BadgeInfo(
    val id: String,
    val label: String,
    val color: Color,
    val cdnUrl: String,
    val description: String,
    val link: String? = null
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
private const val KEY_RISK_ACCEPTED  = "risk_accepted"
private const val DISCORD_EPOCH     = 1420070400000L
private const val CURRENT_VERSION   = "1.0.27"
private const val GITHUB_API_LATEST = "https://api.github.com/repos/Sc-Rhyan57/GetDiscordToken/releases/latest"
private const val GATEWAY_URL       = "wss://gateway.discord.gg/?v=10&encoding=json"
private const val GATEWAY_RECONNECT_DELAY_MS = 3000L
private const val GATEWAY_MAX_RECONNECT_ATTEMPTS = 5

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

private val appLogs = mutableStateListOf<AppLog>()
private val logsEnabled = mutableStateOf(true)

private fun addLog(level: String, tag: String, message: String, detail: String? = null) {
    if (!logsEnabled.value) return
    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    appLogs.add(0, AppLog(ts, level, tag, message, detail))
    if (appLogs.size > 300) appLogs.removeAt(appLogs.size - 1)
}

private fun snowflakeToDate(id: String): String = try {
    val ts = (id.toLong() ushr 22) + DISCORD_EPOCH
    SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.US).format(Date(ts))
} catch (_: Exception) { "Unknown" }

private fun snowflakeToTimestamp(id: String): Long = try {
    (id.toLong() ushr 22) + DISCORD_EPOCH
} catch (_: Exception) { 0L }

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
    0 -> "Playing"; 1 -> "Streaming"; 2 -> "Listening to"
    3 -> "Watching"; 5 -> "Competing in"; else -> "Activity"
}

private fun connectionLabel(type: String): String = when (type) {
    "spotify"         -> "Spotify";      "steam"           -> "Steam"
    "github"          -> "GitHub";       "twitch"          -> "Twitch"
    "youtube"         -> "YouTube";      "twitter"         -> "X (Twitter)"
    "reddit"          -> "Reddit";       "playstation"     -> "PlayStation"
    "xbox"            -> "Xbox";         "battlenet"       -> "Battle.net"
    "instagram"       -> "Instagram";    "tiktok"          -> "TikTok"
    "facebook"        -> "Facebook";     "epicgames"       -> "Epic Games"
    "leagueoflegends" -> "League of Legends"; "riotgames"  -> "Riot Games"
    "crunchyroll"     -> "Crunchyroll";  "domain"          -> "Domain"
    "paypal"          -> "PayPal";       "skype"           -> "Skype"
    "ebay"            -> "eBay";         "soundcloud"      -> "SoundCloud"
    else              -> type.replaceFirstChar { it.uppercase() }
}

private fun connectionColor(type: String): Color = when (type) {
    "spotify"         -> Color(0xFF1DB954); "steam"      -> Color(0xFF66C0F4)
    "github"          -> Color(0xFF8B949E); "twitch"     -> Color(0xFF9147FF)
    "youtube"         -> Color(0xFFFF0000); "twitter"    -> Color(0xFF1D9BF0)
    "reddit"          -> Color(0xFFFF4500); "playstation" -> Color(0xFF0070D1)
    "xbox"            -> Color(0xFF107C10); "battlenet"  -> Color(0xFF009AE4)
    "instagram"       -> Color(0xFFE1306C); "tiktok"     -> Color(0xFFEE1D52)
    "epicgames"       -> Color(0xFF2ECC40); "leagueoflegends" -> Color(0xFFC89B3C)
    "riotgames"       -> Color(0xFFFF4655); "soundcloud" -> Color(0xFFFF5500)
    else              -> AppColors.Primary
}

private fun connectionProfileUrl(type: String, name: String): String? = when (type) {
    "spotify"         -> "https://open.spotify.com/user/$name"
    "steam"           -> "https://steamcommunity.com/id/$name"
    "github"          -> "https://github.com/$name"
    "twitch"          -> "https://twitch.tv/$name"
    "youtube"         -> "https://youtube.com/@$name"
    "twitter"         -> "https://x.com/$name"
    "reddit"          -> "https://reddit.com/u/$name"
    "instagram"       -> "https://instagram.com/$name"
    "tiktok"          -> "https://tiktok.com/@$name"
    "facebook"        -> "https://facebook.com/$name"
    "soundcloud"      -> "https://soundcloud.com/$name"
    "leagueoflegends" -> "https://www.op.gg/summoners/br/$name"
    "riotgames"       -> "https://tracker.gg/valorant/profile/riot/$name"
    else              -> null
}

private fun badgeColorFromId(id: String): Color = when {
    id.contains("staff")              -> Color(0xFF5865F2)
    id.contains("partner")            -> Color(0xFF7289DA)
    id.contains("hypesquad_events")   -> Color(0xFFFB923C)
    id.contains("bug_hunter_level_2") -> Color(0xFFF59E0B)
    id.contains("bug_hunter")         -> Color(0xFF22C55E)
    id.contains("bravery")            -> Color(0xFFA855F7)
    id.contains("brilliance")         -> Color(0xFFEF4444)
    id.contains("balance")            -> Color(0xFF22D3EE)
    id.contains("early_supporter")    -> Color(0xFF8B5CF6)
    id.contains("verified_developer") -> Color(0xFF6366F1)
    id.contains("moderator_programs_alumni") -> Color(0xFF5865F2)
    id.contains("active_developer")   -> Color(0xFF10B981)
    id.contains("premium") || id.contains("nitro") -> Color(0xFF5865F2)
    id.contains("guild_booster")      -> Color(0xFFFF73FA)
    id.contains("legacy_username")    -> Color(0xFF8B949E)
    id.contains("quest")              -> Color(0xFF5865F2)
    else                              -> AppColors.Primary
}

private fun resolvePresenceFromSessions(sessions: JSONArray?): Triple<String, List<String>, CustomStatus?> {
    if (sessions == null || sessions.length() == 0) return Triple("offline", emptyList(), null)
    val priority = mapOf("dnd" to 3, "online" to 2, "idle" to 1, "invisible" to 0, "offline" to -1)
    var bestStatus = "offline"
    val platforms = mutableListOf<String>()
    var customStatus: CustomStatus? = null
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
        if (customStatus == null) {
            val acts = s.optJSONArray("activities") ?: continue
            for (j in 0 until acts.length()) {
                val a = acts.getJSONObject(j)
                if (a.optInt("type", -1) == 4) {
                    val emoji = a.optJSONObject("emoji")
                    customStatus = CustomStatus(
                        text          = a.optString("state").takeIf { it.isNotEmpty() && it != "null" },
                        emojiName     = emoji?.optString("name")?.takeIf { it.isNotEmpty() },
                        emojiId       = emoji?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" },
                        emojiAnimated = emoji?.optBoolean("animated", false) ?: false
                    )
                    break
                }
            }
        }
    }
    return Triple(bestStatus, platforms, customStatus)
}

private fun resolveActivityImageUrl(image: String, appId: String?): String? = when {
    image.startsWith("spotify:") ->
        "https://i.scdn.co/image/${image.removePrefix("spotify:")}"
    image.startsWith("mp:external/") ->
        "https://media.discordapp.net/external/${image.removePrefix("mp:external/")}"
    image.startsWith("mp:") ->
        "https://media.discordapp.net/${image.removePrefix("mp:")}"
    image.isEmpty() || image == "null" -> null
    appId != null -> "https://cdn.discordapp.com/app-assets/$appId/$image.png"
    else -> null
}

private fun resolveSmallImageUrl(image: String, appId: String?): String? = when {
    image.startsWith("mp:external/") ->
        "https://media.discordapp.net/external/${image.removePrefix("mp:external/")}"
    image.startsWith("mp:") ->
        "https://media.discordapp.net/${image.removePrefix("mp:")}"
    image.startsWith("spotify:") ->
        "https://i.scdn.co/image/${image.removePrefix("spotify:")}"
    image.isEmpty() || image == "null" -> null
    appId != null -> "https://cdn.discordapp.com/app-assets/$appId/$image.png"
    else -> null
}

private fun buildActivity(a: JSONObject): PresenceActivity {
    val type  = a.optInt("type", 0)
    val name  = a.optString("name")
    val appId = a.optString("application_id").takeIf { it.isNotEmpty() && it != "null" }
    val ts    = a.optJSONObject("timestamps")
    val richTs = if (ts != null) RichTimestamps(
        start = if (!ts.isNull("start")) ts.optLong("start") else null,
        end   = if (!ts.isNull("end"))   ts.optLong("end")   else null
    ) else null
    val as_ = a.optJSONObject("assets")
    val richAs = if (as_ != null) RichAssets(
        largeImage = as_.optString("large_image").takeIf { it.isNotEmpty() && it != "null" },
        largeText  = as_.optString("large_text").takeIf { it.isNotEmpty() && it != "null" },
        smallImage = as_.optString("small_image").takeIf { it.isNotEmpty() && it != "null" },
        smallText  = as_.optString("small_text").takeIf  { it.isNotEmpty() && it != "null" }
    ) else null
    val largeImageUrl = richAs?.largeImage?.let { resolveActivityImageUrl(it, appId) }
    val smallImageUrl = richAs?.smallImage?.let { resolveSmallImageUrl(it, appId) }
    val btns = mutableListOf<ActivityButton>()
    val btnsArr = a.optJSONArray("buttons")
    if (btnsArr != null) {
        for (bi in 0 until btnsArr.length()) {
            val b = btnsArr.optJSONObject(bi)
            if (b != null) {
                val lbl = b.optString("label").takeIf { it.isNotEmpty() }
                val url = b.optString("url").takeIf  { it.isNotEmpty() }
                if (lbl != null && url != null) btns.add(ActivityButton(lbl, url))
            }
        }
    }
    return PresenceActivity(
        type          = type,
        name          = name,
        details       = a.optString("details").takeIf { it.isNotEmpty() && it != "null" },
        state         = a.optString("state").takeIf   { it.isNotEmpty() && it != "null" },
        applicationId = appId,
        timestamps    = richTs,
        assets        = richAs,
        syncId        = a.optString("sync_id").takeIf { it.isNotEmpty() && it != "null" },
        largeImageUrl = largeImageUrl,
        smallImageUrl = smallImageUrl,
        buttons       = btns
    )
}

private fun activitiesFromArray(acts: JSONArray?): List<PresenceActivity> {
    if (acts == null) return emptyList()
    val list = mutableListOf<PresenceActivity>()
    for (j in 0 until acts.length()) {
        val a = acts.getJSONObject(j)
        if (a.optInt("type", -1) == 4) continue
        val name = a.optString("name").takeIf { it.isNotEmpty() } ?: continue
        val type = a.optInt("type", 0)
        if (list.none { it.name == name && it.type == type }) list.add(buildActivity(a))
    }
    return list
}

private fun activitiesFromSessions(sessions: JSONArray?): List<PresenceActivity> {
    if (sessions == null) return emptyList()
    val list = mutableListOf<PresenceActivity>()
    for (i in 0 until sessions.length()) {
        val s = sessions.getJSONObject(i)
        val acts = s.optJSONArray("activities") ?: continue
        for (j in 0 until acts.length()) {
            val a = acts.getJSONObject(j)
            if (a.optInt("type", -1) == 4) continue
            val name = a.optString("name").takeIf { it.isNotEmpty() } ?: continue
            val type = a.optInt("type", 0)
            if (list.none { it.name == name && it.type == type }) list.add(buildActivity(a))
        }
    }
    return list
}

private fun isNewerVersion(latest: String, current: String): Boolean {
    val l = latest.trimStart('v').split(".").mapNotNull { it.toIntOrNull() }
    val c = current.trimStart('v').split(".").mapNotNull { it.toIntOrNull() }
    for (i in 0 until maxOf(l.size, c.size)) {
        val lv = l.getOrElse(i) { 0 }; val cv = c.getOrElse(i) { 0 }
        if (lv > cv) return true; if (lv < cv) return false
    }
    return false
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
           caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}

private fun formatElapsed(startMs: Long): String {
    val elapsed = (System.currentTimeMillis() - startMs) / 1000
    val h = elapsed / 3600; val m = (elapsed % 3600) / 60; val s = elapsed % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

private fun formatRemaining(endMs: Long): String {
    val rem = (endMs - System.currentTimeMillis()) / 1000
    if (rem <= 0) return "0:00"
    val h = rem / 3600; val m = (rem % 3600) / 60; val s = rem % 60
    return if (h > 0) "%d:%02d:%02d left".format(h, m, s) else "%d:%02d left".format(m, s)
}

private fun accountAgeString(createdMs: Long): String {
    val days  = (System.currentTimeMillis() - createdMs) / 86400000L
    val years = days / 365; val months = (days % 365) / 30; val rem = days % 30
    return buildString {
        if (years  > 0) append("${years}y ")
        if (months > 0) append("${months}mo ")
        if (rem    > 0 && years == 0L) append("${rem}d")
        if (isEmpty()) append("Today")
    }.trim()
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
    val lines = raw.lines(); var i = 0
    while (i < lines.size) {
        val line = lines[i]
        when {
            line.startsWith("```") -> {
                val lang = line.removePrefix("```").trim(); val sb = StringBuilder(); i++
                while (i < lines.size && !lines[i].trimEnd().endsWith("```")) { if (sb.isNotEmpty()) sb.append('\n'); sb.append(lines[i]); i++ }
                blocks.add(MdBlock.CodeBlock(sb.toString(), lang))
            }
            line.startsWith("### ") -> blocks.add(MdBlock.Heading(line.removePrefix("### "), 3))
            line.startsWith("## ")  -> blocks.add(MdBlock.Heading(line.removePrefix("## "),  2))
            line.startsWith("# ")   -> blocks.add(MdBlock.Heading(line.removePrefix("# "),   1))
            line.startsWith("-# ")  -> blocks.add(MdBlock.SubText(line.removePrefix("-# ")))
            line.startsWith("> ")   -> blocks.add(MdBlock.Blockquote(line.removePrefix("> ")))
            line.startsWith(">") && line.length > 1 -> blocks.add(MdBlock.Blockquote(line.drop(1)))
            else -> {
                val pLines = mutableListOf(line)
                while (i + 1 < lines.size) {
                    val next = lines[i + 1]
                    if (next.startsWith("```") || next.startsWith("#") || next.startsWith(">") || next.startsWith("-#")) break
                    i++; pLines.add(lines[i])
                }
                if (pLines.any { it.isNotBlank() }) blocks.add(MdBlock.Paragraph(pLines.joinToString("\n")))
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
                val cs = i + p.marker.length; val ci = text.indexOf(p.marker, cs)
                if (ci > cs) {
                    val inner = text.substring(cs, ci)
                    withStyle(p.style) { if (p.isCode) append(inner) else appendStyled(inner, baseColor) }
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
                is MdBlock.Heading -> Text(parseInline(block.text, AppColors.TextPrimary),
                    fontSize = when (block.level) { 1 -> 22.sp; 2 -> 18.sp; else -> 16.sp },
                    fontWeight = FontWeight.Bold,
                    lineHeight = when (block.level) { 1 -> 28.sp; 2 -> 24.sp; else -> 22.sp })
                is MdBlock.SubText -> Text(parseInline(block.text, AppColors.TextMuted), fontSize = 11.sp, lineHeight = 16.sp)
                is MdBlock.Blockquote -> Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)
                    .background(AppColors.Surface.copy(0.5f), RoundedCornerShape(Radius.Small)).padding(vertical = 6.dp)) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(AppColors.TextMuted.copy(0.6f), RoundedCornerShape(2.dp)))
                    Spacer(Modifier.width(10.dp))
                    Text(parseInline(block.text, AppColors.TextSecondary), fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(end = 8.dp))
                }
                is MdBlock.CodeBlock -> Box(Modifier.fillMaxWidth().background(AppColors.CodeBg, RoundedCornerShape(Radius.Medium)).padding(12.dp)) {
                    Column {
                        if (block.lang.isNotEmpty()) Text(block.lang, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 4.dp))
                        Text(block.code, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = Color(0xFFE8739C), lineHeight = 18.sp)
                    }
                }
                is MdBlock.Paragraph -> Text(parseInline(block.text, AppColors.TextSecondary), fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun ParticleBackground(speedMultiplier: Float = 1f, modifier: Modifier = Modifier) {
    data class Particle(val x: Float, val y: Float, val speed: Float, val size: Float, val alpha: Float, val phase: Float, val wobble: Float)
    data class Glob(val x: Float, val y: Float, val r: Float, val color: Color, val phase: Float)
    val particles = remember { List(90) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.5f + 0.1f, Random.nextFloat() * 2.5f + 0.5f, Random.nextFloat() * 0.4f + 0.06f, Random.nextFloat() * 6.28f, Random.nextFloat() * 0.08f + 0.02f) } }
    val globs = remember { listOf(Glob(0.15f, 0.25f, 420f, Color(0xFF5865F2), 0.47f), Glob(0.80f, 0.60f, 350f, Color(0xFF7B5EA7), 2.51f), Glob(0.50f, 0.85f, 280f, Color(0xFF3B4EC8), 1.57f), Glob(0.70f, 0.10f, 220f, Color(0xFF5865F2), 3.14f), Glob(0.30f, 0.70f, 180f, Color(0xFF9B59B6), 0.92f)) }
    val transition = rememberInfiniteTransition(label = "bg")
    val baseDuration = (80000f / speedMultiplier).toInt().coerceAtLeast(3000)
    val time by transition.animateFloat(0f, 10000f, infiniteRepeatable(tween(baseDuration, easing = LinearEasing)), label = "t")
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        globs.forEach { g ->
            val gx = g.x * w + sin(time * 0.00025f * speedMultiplier + g.phase) * 80f
            val gy = g.y * h + cos(time * 0.0003f  * speedMultiplier + g.phase) * 70f
            drawCircle(color = g.color.copy(alpha = 0.09f), radius = g.r, center = Offset(gx, gy))
        }
        particles.forEach { p ->
            val px = ((p.x + sin(time * 0.0015f * p.speed * speedMultiplier + p.phase) * p.wobble).mod(1f) + 1f).mod(1f) * w
            val py = ((p.y + time * p.speed * speedMultiplier * 0.00032f).mod(1f)) * h
            drawCircle(color = Color.White.copy(alpha = p.alpha), radius = p.size, center = Offset(px, py))
        }
    }
}

class MainActivity : ComponentActivity() {

    private var gatewayWs: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private var gatewaySequence = -1
    private var gatewaySessionId: String = ""
    private var ownUserId: String = ""
    private var onPresenceLive: ((DiscordPresence) -> Unit)? = null
    private var gatewayToken: String = ""
    private var gatewayReconnectAttempts = 0
    private var currentPresence: DiscordPresence? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCrashHandler()
        val crashPrefs = getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE)
        val crashTrace = crashPrefs.getString(KEY_CRASH_TRACE, null)
        if (crashTrace != null) crashPrefs.edit().remove(KEY_CRASH_TRACE).apply()
        val savedToken = getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE).getString(KEY_TOKEN, null)?.takeIf { it.isNotEmpty() }
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
        reconnectJob?.cancel()
        gatewayWs?.close(1000, null)
    }

    private fun saveToken(token: String) = getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE).edit().putString(KEY_TOKEN, token).apply()
    private fun clearToken() {
        getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE).edit().remove(KEY_TOKEN).apply()
        CookieManager.getInstance().removeAllCookies(null); CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
    }

    private fun setupCrashHandler() {
        val def = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
                val log = "Discord Token — Crash Report\nManufacturer: ${Build.MANUFACTURER}\nDevice: ${Build.MODEL}\nAndroid: ${Build.VERSION.RELEASE}\nStacktrace:\n$sw"
                getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE).edit().putString(KEY_CRASH_TRACE, log).commit()
                startActivity(Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK) })
            } catch (_: Exception) { def?.uncaughtException(t, e) }
            android.os.Process.killProcess(android.os.Process.myPid()); System.exit(2)
        }
    }

    private fun scheduleReconnect(token: String) {
        if (gatewayReconnectAttempts >= GATEWAY_MAX_RECONNECT_ATTEMPTS) {
            addLog("WARN", "Gateway", "Max reconnect attempts reached ($GATEWAY_MAX_RECONNECT_ATTEMPTS), giving up")
            return
        }
        val delay = GATEWAY_RECONNECT_DELAY_MS * (1L shl gatewayReconnectAttempts.coerceAtMost(4))
        gatewayReconnectAttempts++
        addLog("INFO", "Gateway", "Scheduling reconnect in ${delay}ms (attempt $gatewayReconnectAttempts/$GATEWAY_MAX_RECONNECT_ATTEMPTS)")
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            delay(delay)
            val canResume = gatewaySessionId.isNotEmpty() && gatewaySequence > 0
            addLog("INFO", "Gateway", "Reconnecting… canResume=$canResume sessionId=$gatewaySessionId seq=$gatewaySequence")
            connectGatewayInternal(token, canResume) { updated ->
                currentPresence = updated
                onPresenceLive?.invoke(updated)
            }
        }
    }

    private fun buildIdentifyPayload(token: String): String = JSONObject().apply {
        put("op", 2)
        put("d", JSONObject().apply {
            put("token", token)
            put("capabilities", 16381)
            put("properties", JSONObject().apply {
                put("os", "Android")
                put("browser", "Discord Android")
                put("device", "Android")
                put("system_locale", Locale.getDefault().toLanguageTag())
                put("browser_version", "")
                put("os_version", Build.VERSION.RELEASE)
                put("referrer", "")
                put("referring_domain", "")
                put("release_channel", "stable")
                put("client_build_number", 0)
            })
            put("compress", false)
            put("presence", JSONObject().apply {
                put("status", "unknown")
                put("since", 0)
                put("activities", JSONArray())
                put("afk", false)
            })
        })
    }.toString()

    private fun buildResumePayload(token: String): String = JSONObject().apply {
        put("op", 6)
        put("d", JSONObject().apply {
            put("token", token)
            put("session_id", gatewaySessionId)
            put("seq", gatewaySequence)
        })
    }.toString()

    private fun connectGatewayInternal(token: String, tryResume: Boolean, onUpdate: (DiscordPresence) -> Unit) {
        heartbeatJob?.cancel()
        gatewayWs?.close(1000, null)
        onPresenceLive = onUpdate

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                addLog("INFO", "Gateway", "WebSocket opened", "HTTP ${response.code}")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val json = JSONObject(text)
                    val op   = json.optInt("op", -1)
                    val seq  = json.optInt("s", -1)
                    if (seq > 0) gatewaySequence = seq

                    when (op) {
                        10 -> {
                            val interval = json.getJSONObject("d").getLong("heartbeat_interval")
                            addLog("INFO", "Gateway", "Hello OP10 — heartbeat every ${interval}ms")
                            heartbeatJob = CoroutineScope(Dispatchers.IO).launch {
                                delay((interval * Random.nextFloat() * 0.5f).toLong())
                                while (true) {
                                    val s = if (gatewaySequence > 0) gatewaySequence.toString() else "null"
                                    webSocket.send("""{"op":1,"d":$s}""")
                                    addLog("INFO", "Gateway", "♥ Heartbeat sent", "seq=$s")
                                    delay(interval)
                                }
                            }
                            if (tryResume && gatewaySessionId.isNotEmpty() && gatewaySequence > 0) {
                                webSocket.send(buildResumePayload(token))
                                addLog("INFO", "Gateway", "Resume OP6 sent", "sessionId=$gatewaySessionId seq=$gatewaySequence")
                            } else {
                                webSocket.send(buildIdentifyPayload(token))
                                addLog("INFO", "Gateway", "Identify OP2 sent")
                            }
                        }

                        11 -> addLog("INFO", "Gateway", "♥ Heartbeat ACK received")

                        7 -> {
                            addLog("WARN", "Gateway", "Reconnect requested OP7 — scheduling reconnect")
                            webSocket.close(4000, "Reconnect requested")
                            scheduleReconnect(token)
                        }

                        9 -> {
                            val resumable = json.optBoolean("d", false)
                            addLog("WARN", "Gateway", "Invalid session OP9 — resumable=$resumable")
                            if (!resumable) {
                                gatewaySessionId = ""
                                gatewaySequence = -1
                            }
                            webSocket.close(4000, "Invalid session")
                            scheduleReconnect(token)
                        }

                        0 -> {
                            val ev = json.optString("t")
                            when (ev) {
                                "READY" -> {
                                    gatewayReconnectAttempts = 0
                                    val data     = json.getJSONObject("d")
                                    gatewaySessionId = data.optString("session_id", "")
                                    val selfUser = data.optJSONObject("user")
                                    val selfId   = selfUser?.optString("id") ?: ownUserId
                                    if (ownUserId.isEmpty()) ownUserId = selfId
                                    val sessions = data.optJSONArray("sessions")
                                    val (status, platforms, customStatus) = resolvePresenceFromSessions(sessions)
                                    val activities = activitiesFromSessions(sessions)
                                    val presence   = DiscordPresence(status, activities, platforms, customStatus)
                                    currentPresence = presence
                                    addLog("SUCCESS", "Gateway", "READY — userId=$selfId status=$status platforms=${platforms.joinToString()} acts=${activities.size}")
                                    onPresenceLive?.invoke(presence)
                                }

                                "RESUMED" -> {
                                    gatewayReconnectAttempts = 0
                                    addLog("SUCCESS", "Gateway", "RESUMED — session restored seq=$gatewaySequence")
                                    currentPresence?.let { onPresenceLive?.invoke(it) }
                                }

                                "PRESENCE_UPDATE" -> {
                                    val data   = json.getJSONObject("d")
                                    val userId = data.optJSONObject("user")?.optString("id") ?: ""
                                    if (userId.isNotEmpty() && ownUserId.isNotEmpty() && userId != ownUserId) return
                                    val status     = data.optString("status", "offline")
                                    val acts       = data.optJSONArray("activities")
                                    val activities = activitiesFromArray(acts)
                                    var cs: CustomStatus? = null
                                    if (acts != null) {
                                        for (i in 0 until acts.length()) {
                                            val a = acts.getJSONObject(i)
                                            if (a.optInt("type", -1) == 4) {
                                                val em = a.optJSONObject("emoji")
                                                cs = CustomStatus(
                                                    text          = a.optString("state").takeIf { it.isNotEmpty() && it != "null" },
                                                    emojiName     = em?.optString("name")?.takeIf { it.isNotEmpty() },
                                                    emojiId       = em?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" },
                                                    emojiAnimated = em?.optBoolean("animated", false) ?: false
                                                )
                                                break
                                            }
                                        }
                                    }
                                    val updated = DiscordPresence(status, activities, currentPresence?.platforms ?: emptyList(), cs)
                                    currentPresence = updated
                                    addLog("INFO", "Gateway", "PRESENCE_UPDATE uid=$userId", "status=$status acts=${activities.size} customStatus=${cs?.text}")
                                    onPresenceLive?.invoke(updated)
                                }

                                "SESSION_REPLACE" -> {
                                    val sessions = json.optJSONArray("d") ?: return
                                    val (status, platforms, cs) = resolvePresenceFromSessions(sessions)
                                    val activities = activitiesFromSessions(sessions)
                                    addLog("INFO", "Gateway", "SESSION_REPLACE", "sessions=${sessions.length()} status=$status acts=${activities.size}")
                                    val updated = DiscordPresence(status, activities, platforms, cs)
                                    currentPresence = updated
                                    onPresenceLive?.invoke(updated)
                                }

                                "READY_SUPPLEMENTAL" -> addLog("INFO", "Gateway", "READY_SUPPLEMENTAL received")
                                else -> if (ev.isNotEmpty()) addLog("INFO", "Gateway", "Event: $ev")
                            }
                        }
                    }
                } catch (e: Exception) {
                    addLog("ERROR", "Gateway", "Parse error: ${e.message}", e.stackTraceToString().take(400))
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                addLog("ERROR", "Gateway", "Failure: ${t.message}", "code=${response?.code}")
                scheduleReconnect(token)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                addLog("INFO", "Gateway", "Closed: $code", reason.ifBlank { "no reason" })
                if (code != 1000 && code != 4004) {
                    scheduleReconnect(token)
                }
            }
        }

        val ws = httpClient.newWebSocket(Request.Builder().url(GATEWAY_URL).build(), listener)
        gatewayWs = ws
        gatewayToken = token
    }

    @Composable
    fun CrashScreen(trace: String) {
        val ctx = LocalContext.current
        Column(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, null, tint = AppColors.Error, modifier = Modifier.size(22.dp)); Spacer(Modifier.width(10.dp))
                    Text("App Crashed", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold), color = AppColors.TextPrimary)
                }
                TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) { Text("Close", color = AppColors.Error, fontWeight = FontWeight.Bold) }
            }
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                Text("An unexpected error occurred. Copy the log below and report it.", style = MaterialTheme.typography.bodyMedium, color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 12.dp))
                Button(onClick = { (ctx.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Crash Log", trace)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), shape = RoundedCornerShape(Radius.Button)) {
                    Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(8.dp)); Text("Copy Log", fontWeight = FontWeight.Bold, color = AppColors.OnPrimary)
                }
                Card(modifier = Modifier.fillMaxSize(), colors = CardDefaults.cardColors(containerColor = AppColors.ErrorContainer), shape = RoundedCornerShape(Radius.Card)) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) { item { Text(trace, modifier = Modifier.padding(14.dp), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = AppColors.OnError, lineHeight = 18.sp) } }
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
        var badges           by remember { mutableStateOf<List<BadgeInfo>>(emptyList()) }
        var guildCount       by remember { mutableStateOf<Int?>(null) }
        var loadingUser      by remember { mutableStateOf(false) }
        var loadingPresence  by remember { mutableStateOf(false) }
        var loadingConns     by remember { mutableStateOf(false) }
        var loadingBadges    by remember { mutableStateOf(false) }
        var loadingGuilds    by remember { mutableStateOf(false) }
        var showWebView      by remember { mutableStateOf(false) }
        var updateTag        by remember { mutableStateOf<String?>(null) }
        var noInternet       by remember { mutableStateOf(false) }
        var showLogs         by remember { mutableStateOf(false) }
        var footerClicks     by remember { mutableIntStateOf(0) }
        var logEnabled       by remember { mutableStateOf(true) }
        var refreshTick      by remember { mutableIntStateOf(0) }

        logsEnabled.value = logEnabled

        LaunchedEffect(Unit) {
            if (!isNetworkAvailable(ctx)) { noInternet = true; return@LaunchedEffect }
            try { val v = checkLatestVersion(); if (v != null && isNewerVersion(v, CURRENT_VERSION)) updateTag = v } catch (_: Exception) {}
        }

        LaunchedEffect(token, refreshTick) {
            val t = token ?: return@LaunchedEffect
            addLog("INFO", "Session", "Fetching all user data (tick=$refreshTick)", "token=${t.take(20)}...")

            loadingUser = true
            val fetched: DiscordUser? = try {
                fetchUserInfo(t).also { u ->
                    ownUserId = u.id
                    addLog("SUCCESS", "API", "User: ${u.username} (id=${u.id})", "flags=${u.publicFlags} nitro=${u.premiumType}")
                }
            } catch (e: Exception) {
                addLog("ERROR", "API", "User fetch failed: ${e.message}", e.stackTraceToString().take(600))
                if (initialToken != null && user == null) { clearToken(); token = null }
                loadingUser = false; return@LaunchedEffect
            }
            user = fetched
            loadingUser = false

            loadingPresence = true
            launch {
                try {
                    gatewayReconnectAttempts = 0
                    gatewaySessionId = ""
                    gatewaySequence = -1
                    currentPresence = null
                    connectGatewayInternal(t) { updated -> presence = updated }
                    var waited = 0
                    while (presence == null && waited < 15000) { delay(100); waited += 100 }
                    if (presence == null) addLog("WARN", "Gateway", "Timed out waiting for READY (${waited}ms)")
                } catch (e: Exception) {
                    addLog("ERROR", "Gateway", "Gateway init failed: ${e.message}")
                }
                loadingPresence = false
            }

            loadingBadges = true
            launch {
                badges = try {
                    val uid = fetched?.id ?: ""
                    if (uid.isNotEmpty()) fetchUserBadges(t, uid) else emptyList()
                } catch (e: Exception) {
                    addLog("ERROR", "API", "Badges: ${e.message}")
                    emptyList()
                }
                loadingBadges = false
            }

            loadingConns = true
            launch {
                connections = try { fetchConnections(t) } catch (e: Exception) { addLog("ERROR", "API", "Connections: ${e.message}"); emptyList() }
                loadingConns = false
            }

            loadingGuilds = true
            launch {
                guildCount = try { fetchGuildCount(t) } catch (e: Exception) { addLog("ERROR", "API", "Guilds: ${e.message}"); null }
                loadingGuilds = false
            }
        }

        if (noInternet) {
            AlertDialog(onDismissRequest = { noInternet = false },
                icon = { Icon(Icons.Outlined.WifiOff, null, tint = AppColors.Error, modifier = Modifier.size(32.dp)) },
                title = { Text("No Internet Connection", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("This app requires internet to function.", color = AppColors.TextSecondary) },
                confirmButton = { Button(onClick = { noInternet = false; if (!isNetworkAvailable(ctx)) noInternet = true }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), shape = RoundedCornerShape(Radius.Button)) { Text("Retry", color = AppColors.OnPrimary, fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = { noInternet = false }) { Text("Dismiss", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card))
        }

        if (updateTag != null) {
            AlertDialog(onDismissRequest = { updateTag = null },
                icon = { Icon(Icons.Outlined.Update, null, tint = AppColors.Primary, modifier = Modifier.size(32.dp)) },
                title = { Text("Update Available", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("v${updateTag!!.trimStart('v')} is available. You are on v$CURRENT_VERSION.", color = AppColors.TextSecondary) },
                confirmButton = { Button(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-Rhyan57/GetDiscordToken/releases/tag/$updateTag"))); updateTag = null }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary), shape = RoundedCornerShape(Radius.Button)) { Text("Update", color = AppColors.OnPrimary, fontWeight = FontWeight.Bold) } },
                dismissButton = { TextButton(onClick = { updateTag = null }) { Text("Later", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card))
        }

        if (showLogs) {
            LogsScreen(onClose = { showLogs = false }, logsEnabled = logEnabled, onToggle = { logEnabled = it; logsEnabled.value = it })
        }

        val screen = when {
            showWebView && token == null -> "webview"
            token == null               -> "login"
            else                        -> "profile"
        }

        AnimatedContent(targetState = screen, transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) }, label = "nav") { s ->
            when (s) {
                "webview" -> WebViewScreen(
                    onTokenReceived = { t -> addLog("SUCCESS", "Token", "Token extracted!", "length=${t.length}"); token = t; saveToken(t); showWebView = false },
                    onBack = { showWebView = false })
                "login"   -> LoginScreen(onLoginClick = { showWebView = true }, footerClicks = footerClicks,
                    onFooterClick = { footerClicks++; if (footerClicks >= 5) { showLogs = true; footerClicks = 0 } })
                else      -> UserProfileScreen(
                    user = user, token = token ?: "", loadingUser = loadingUser, loadingPresence = loadingPresence,
                    loadingConns = loadingConns, loadingGuilds = loadingGuilds, loadingBadges = loadingBadges,
                    presence = presence, connections = connections, guildCount = guildCount, badges = badges,
                    footerClicks = footerClicks,
                    onFooterClick = { footerClicks++; if (footerClicks >= 5) { showLogs = true; footerClicks = 0 } },
                    onRefresh = {
                        addLog("INFO", "Session", "Manual refresh triggered")
                        user = null; presence = null; connections = null; guildCount = null; badges = emptyList()
                        heartbeatJob?.cancel(); reconnectJob?.cancel(); gatewayWs?.close(1000, null)
                        gatewaySessionId = ""; gatewaySequence = -1; gatewayReconnectAttempts = 0
                        refreshTick++
                    },
                    onLogout = {
                        addLog("INFO", "Session", "Logout — clearing state")
                        heartbeatJob?.cancel(); reconnectJob?.cancel(); gatewayWs?.close(1000, null)
                        gatewaySessionId = ""; gatewaySequence = -1; gatewayReconnectAttempts = 0; ownUserId = ""
                        clearToken(); token = null; user = null; presence = null; connections = null; guildCount = null; badges = emptyList()
                    })
            }
        }
    }

    @Composable
    fun LogsScreen(onClose: () -> Unit, logsEnabled: Boolean, onToggle: (Boolean) -> Unit) {
        Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxWidth().background(AppColors.Surface).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Terminal, null, tint = AppColors.Primary, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                            Text("App Console", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary); Spacer(Modifier.width(8.dp))
                            Box(Modifier.background(AppColors.Primary.copy(0.15f), RoundedCornerShape(Radius.Badge)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text("${appLogs.size}", fontSize = 10.sp, color = AppColors.Primary, fontWeight = FontWeight.Bold) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Logging", fontSize = 11.sp, color = AppColors.TextMuted); Spacer(Modifier.width(6.dp))
                            Switch(checked = logsEnabled, onCheckedChange = onToggle, modifier = Modifier.height(24.dp),
                                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Primary, checkedTrackColor = AppColors.Primary.copy(0.3f)))
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = { appLogs.clear() }) { Text("Clear", color = AppColors.Error, fontSize = 11.sp) }
                            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Close, null, tint = AppColors.TextMuted, modifier = Modifier.size(18.dp)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().background(AppColors.Surface.copy(0.4f)).padding(horizontal = 16.dp, vertical = 5.dp)) {
                        Text("Tap any entry to expand details. ${appLogs.size} entries.", fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                    }
                    if (appLogs.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.Terminal, null, tint = AppColors.TextMuted, modifier = Modifier.size(40.dp)); Spacer(Modifier.height(8.dp))
                                Text("No logs yet", color = AppColors.TextMuted, fontSize = 14.sp)
                            }
                        }
                    } else {
                        LazyColumn(Modifier.fillMaxSize().padding(8.dp)) {
                            items(appLogs) { log ->
                                var expanded by remember { mutableStateOf(false) }
                                val levelColor = when (log.level) { "SUCCESS" -> AppColors.Success; "ERROR" -> AppColors.Error; "WARN" -> AppColors.Warning; else -> AppColors.Primary }
                                Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                    .background(AppColors.Surface.copy(0.5f), RoundedCornerShape(Radius.Small))
                                    .clickable { expanded = !expanded }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(log.timestamp, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                                        Spacer(Modifier.width(5.dp))
                                        Box(Modifier.background(levelColor.copy(0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) { Text(log.level, fontSize = 9.sp, color = levelColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                                        Spacer(Modifier.width(5.dp))
                                        Text("[${log.tag}]", fontSize = 10.sp, color = AppColors.Primary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.width(4.dp))
                                        Text(log.message, fontSize = 11.sp, color = AppColors.TextSecondary, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f), maxLines = if (expanded) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis)
                                        Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = AppColors.TextMuted, modifier = Modifier.size(14.dp))
                                    }
                                    if (expanded && log.detail != null) { Spacer(Modifier.height(4.dp)); Text(log.detail, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace, lineHeight = 14.sp, modifier = Modifier.padding(start = 4.dp)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LoginScreen(onLoginClick: () -> Unit, footerClicks: Int, onFooterClick: () -> Unit) {
        val ctx = LocalContext.current
        val scale = remember { Animatable(0f) }
        LaunchedEffect(Unit) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
        var keyClicks by remember { mutableIntStateOf(0) }
        var speed     by remember { mutableStateOf(1f) }
        var bgGreen   by remember { mutableStateOf(false) }
        val shimmerT = rememberInfiniteTransition(label = "sh")
        val shimmer  by shimmerT.animateFloat(-1.5f, 1.5f, infiniteRepeatable(tween(2000, easing = LinearEasing)), label = "shv")
        val glowT    = rememberInfiniteTransition(label = "gl")
        val glow     by glowT.animateFloat(0.25f, 0.75f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse), label = "glv")

        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxSize().background(if (bgGreen)
                Brush.verticalGradient(listOf(Color(0xFF0A2A0A), Color(0xFF010F01), Color.Black))
            else Brush.verticalGradient(listOf(AppColors.LoginBg1, AppColors.LoginBg2, AppColors.LoginBg3))))
            ParticleBackground(speedMultiplier = speed, modifier = Modifier.fillMaxSize())
            Column(Modifier.fillMaxSize().padding(28.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier.size(96.dp)
                        .background(AppColors.Primary.copy(0.18f), CircleShape)
                        .border(1.5.dp, AppColors.Primary.copy(0.4f), CircleShape)
                        .clickable {
                            keyClicks++
                            speed = 1f + keyClicks * 1.2f
                            if (keyClicks >= 5) bgGreen = true
                            if (keyClicks >= 10) {
                                val url = if (Random.nextBoolean()) "https://youtu.be/iik25wqIuFo" else "https://youtu.be/OWSlGSnupeY"
                                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                keyClicks = 0; speed = 1f; bgGreen = false
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Key, null, tint = AppColors.Primary,
                        modifier = Modifier.size(48.dp).scale(if (keyClicks > 5) 1f + (keyClicks - 5) * 0.06f else 1f))
                }
                Spacer(Modifier.height(14.dp))
                Text("v$CURRENT_VERSION", fontSize = 10.sp, color = AppColors.TextMuted.copy(0.5f), fontFamily = FontFamily.Monospace)
                Spacer(Modifier.height(36.dp))
                Card(Modifier.fillMaxWidth().scale(scale.value),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Surface.copy(0.9f)),
                    shape = RoundedCornerShape(Radius.XLarge), elevation = CardDefaults.cardElevation(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Primary.copy(0.2f))) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Sign in with your Discord account to obtain your token.", fontSize = 13.sp, color = AppColors.TextMuted, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(22.dp))
                        Box(Modifier.fillMaxWidth()) {
                            Box(Modifier.fillMaxWidth().height(58.dp)
                                .graphicsLayer { shadowElevation = 28f; shape = RoundedCornerShape(Radius.Button); clip = false }
                                .background(AppColors.Primary.copy(glow * 0.4f), RoundedCornerShape(Radius.Button + 2.dp)))
                            Box(Modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(Radius.Button))
                                .background(AppColors.Primary).clickable { onLoginClick() },
                                contentAlignment = Alignment.Center) {
                                Box(Modifier.fillMaxSize().background(Brush.linearGradient(
                                    listOf(Color.Transparent, Color.White.copy(0.28f), Color.Transparent),
                                    start = Offset(shimmer * 400f + 200f, 0f), end = Offset(shimmer * 400f + 350f, 80f))))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Outlined.Login, null, modifier = Modifier.size(19.dp), tint = Color.White)
                                    Spacer(Modifier.width(10.dp))
                                    Text("Sign in with Discord", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            SecurityBadge(Icons.Outlined.Security, "Secure"); SecurityBadge(Icons.Outlined.VisibilityOff, "Private"); SecurityBadge(Icons.Outlined.Smartphone, "Local")
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Footer(clicks = footerClicks, onClick = onFooterClick)
                Spacer(Modifier.height(12.dp)); GitHubButton(); Spacer(Modifier.height(8.dp))
            }
        }
    }

    @Composable
    fun SecurityBadge(icon: ImageVector, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = AppColors.Success, modifier = Modifier.size(18.dp)); Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = AppColors.Success, fontWeight = FontWeight.SemiBold)
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        val currentReceived by rememberUpdatedState(onTokenReceived)
        val currentBack     by rememberUpdatedState(onBack)
        val webRef = remember { mutableStateOf<WebView?>(null) }
        Box(Modifier.fillMaxSize()) {
            AndroidView(factory = { ctx ->
                WebView(ctx).also { wv ->
                    webRef.value = wv
                    wv.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    wv.setBackgroundColor(android.graphics.Color.parseColor("#1E1F22"))
                    wv.settings.javaScriptEnabled = true; wv.settings.domStorageEnabled = true
                    wv.settings.userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S921U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36"
                    wv.webViewClient = object : WebViewClient() {
                        @Deprecated("Deprecated") override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                            if (url.contains("/app") || url.endsWith("/channels/@me")) { view.stopLoading(); Handler(Looper.getMainLooper()).postDelayed({ view.loadUrl(JS_TOKEN) }, 500); return true }
                            return false
                        }
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            addLog("INFO", "WebView", "Page loaded", url)
                            if (url.contains("/app") || url.endsWith("/channels/@me")) Handler(Looper.getMainLooper()).postDelayed({ view.loadUrl(JS_TOKEN) }, 800)
                        }
                    }
                    wv.webChromeClient = object : WebChromeClient() {
                        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
                            result.confirm(); view.visibility = View.GONE
                            if (message.isNotBlank() && message != "undefined")
                                Handler(Looper.getMainLooper()).postDelayed({ currentReceived(message.trim()) }, 200)
                            return true
                        }
                    }
                    wv.loadUrl("https://discord.com/login")
                }
            }, modifier = Modifier.fillMaxSize())
            Box(Modifier.align(Alignment.TopStart).padding(top = 40.dp, start = 12.dp)) {
                TextButton(onClick = { currentBack() }, colors = ButtonDefaults.textButtonColors(containerColor = AppColors.Background.copy(0.85f), contentColor = AppColors.TextPrimary), shape = RoundedCornerShape(Radius.Medium)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("Back", fontWeight = FontWeight.Bold)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                webRef.value?.apply { stopLoading(); loadUrl("about:blank"); Handler(Looper.getMainLooper()).postDelayed({ clearHistory(); destroy() }, 500) }
                webRef.value = null
            }
        }
    }

    @Composable
    fun UserProfileScreen(
        user: DiscordUser?, token: String, loadingUser: Boolean, loadingPresence: Boolean,
        loadingConns: Boolean, loadingGuilds: Boolean, loadingBadges: Boolean,
        presence: DiscordPresence?, connections: List<DiscordConnection>?,
        guildCount: Int?, badges: List<BadgeInfo>,
        footerClicks: Int, onFooterClick: () -> Unit, onRefresh: () -> Unit, onLogout: () -> Unit
    ) {
        val ctx = LocalContext.current
        val ctx2            = LocalContext.current
        var showToken       by remember { mutableStateOf(false) }
        var copied          by remember { mutableStateOf(false) }
        var expandConns     by remember { mutableStateOf(false) }
        var selectedBadge   by remember { mutableStateOf<BadgeInfo?>(null) }
        var showTokenWarning by remember { mutableStateOf(false) }
        var pendingAction   by remember { mutableStateOf<String?>(null) }
        val riskPrefs       = remember { ctx2.getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE) }
        val riskAccepted    = remember { mutableStateOf(riskPrefs.getBoolean(KEY_RISK_ACCEPTED, false)) }
        fun triggerTokenAction(action: String) {
            if (riskAccepted.value) {
                when (action) {
                    "copy" -> { (ctx2.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("Token", token)); copied = true; CoroutineScope(Dispatchers.Main).launch { delay(2000); copied = false } }
                    "show" -> { showToken = !showToken }
                }
            } else { pendingAction = action; showTokenWarning = true }
        }
        val scale = remember { Animatable(0.95f) }
        LaunchedEffect(user?.id) { scale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy)) }

        if (showTokenWarning) {
            AlertDialog(
                onDismissRequest = { showTokenWarning = false; pendingAction = null },
                icon = { Icon(Icons.Outlined.Warning, null, tint = AppColors.Warning, modifier = Modifier.size(32.dp)) },
                title = { Text("Security Warning", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Your access token is like a password. Never share it with anyone!", color = AppColors.Warning, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("This app is not responsible for any consequences arising from the misuse or sharing of your token, including account loss, bans, or unauthorized access.", color = AppColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                        Text("By continuing, you confirm that you understand the risks.", color = AppColors.TextMuted, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        riskPrefs.edit().putBoolean(KEY_RISK_ACCEPTED, true).apply()
                        riskAccepted.value = true
                        showTokenWarning = false
                        val act = pendingAction; pendingAction = null
                        if (act != null) triggerTokenAction(act)
                    }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Warning), shape = RoundedCornerShape(Radius.Button)) {
                        Text("I understand, continue", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                dismissButton = { TextButton(onClick = { showTokenWarning = false; pendingAction = null }) { Text("Cancel", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card))
        }

        if (selectedBadge != null) {
            AlertDialog(
                onDismissRequest = { selectedBadge = null },
                icon = { AsyncImage(model = selectedBadge!!.cdnUrl, contentDescription = null, modifier = Modifier.size(52.dp)) },
                title = { Text(selectedBadge!!.label, color = selectedBadge!!.color, fontWeight = FontWeight.Bold) },
                text = { Text(selectedBadge!!.description, color = AppColors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp) },
                confirmButton = { TextButton(onClick = { selectedBadge = null }) { Text("Close", color = AppColors.TextMuted) } },
                containerColor = AppColors.Surface, shape = RoundedCornerShape(Radius.Card))
        }

        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                if (user?.banner != null) {
                    val ext = if (user.banner.startsWith("a_")) "gif" else "png"
                    AsyncImage("https://cdn.discordapp.com/banners/${user.id}/${user.banner}.$ext?size=600", null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    val brush = if (user?.accentColor != null) { val c = Color(0xFF000000 or user.accentColor.toLong()); Brush.verticalGradient(listOf(c, c.copy(0.4f), AppColors.Background)) }
                    else Brush.verticalGradient(listOf(AppColors.Primary, Color(0xFF7289DA), AppColors.Background))
                    Box(Modifier.fillMaxSize().background(brush))
                }
            }

            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(Modifier.size(90.dp).border(4.dp, AppColors.Background, CircleShape).clip(CircleShape).background(AppColors.Surface)) {
                        if (user?.avatar != null) {
                            val ext = if (user.avatar.startsWith("a_")) "gif" else "png"
                            AsyncImage("https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.$ext?size=256", null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Box(Modifier.fillMaxSize().background(AppColors.Primary), contentAlignment = Alignment.Center) {
                                Text(user?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?", fontSize = 34.sp, fontWeight = FontWeight.Black, color = AppColors.OnPrimary)
                            }
                        }
                        if (user?.avatarDecorationAsset != null) {
                            AsyncImage("https://cdn.discordapp.com/avatar-decoration-presets/${user.avatarDecorationAsset}.png?size=96&passthrough=true", "decoration", Modifier.fillMaxSize().padding((-8).dp))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp).background(AppColors.Surface, RoundedCornerShape(Radius.Small))) {
                            Icon(Icons.Outlined.Refresh, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                        }
                        Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = AppColors.ErrorContainer, contentColor = AppColors.Error), shape = RoundedCornerShape(Radius.Medium)) {
                            Icon(Icons.AutoMirrored.Outlined.Logout, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)); Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (loadingUser) {
                    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 3.dp, modifier = Modifier.size(40.dp)) }
                } else if (user != null) {

                    ProfileSection("Access Token", Icons.Outlined.Fingerprint) {
                        Button(onClick = { triggerTokenAction("copy") }, modifier = Modifier.fillMaxWidth().height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (copied) AppColors.Success.copy(0.8f) else AppColors.Success),
                            shape = RoundedCornerShape(Radius.Token)) {
                            Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(16.dp), tint = Color.White); Spacer(Modifier.width(8.dp))
                            Text(if (copied) "Copied!" else "Copy Token", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }
                        Spacer(Modifier.height(8.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVar), shape = RoundedCornerShape(Radius.Token)) {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(if (showToken) token else "•".repeat(token.length),
                                    fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = AppColors.TextSecondary,
                                    maxLines = if (showToken) Int.MAX_VALUE else 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                IconButton(onClick = { triggerTokenAction("show") }, modifier = Modifier.size(36.dp)) {
                                    Icon(if (showToken) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = AppColors.Primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(user.globalName ?: user.username, fontSize = 26.sp, fontWeight = FontWeight.Black, color = AppColors.TextPrimary)
                        if (presence != null) {
                            val sc = statusColor(presence.status)
                            Row(Modifier.background(sc.copy(0.12f), RoundedCornerShape(Radius.Badge)).border(1.dp, sc.copy(0.25f), RoundedCornerShape(Radius.Badge)).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(7.dp).background(sc, CircleShape)); Spacer(Modifier.width(5.dp))
                                Text(statusLabel(presence.status), fontSize = 11.sp, color = sc, fontWeight = FontWeight.SemiBold)
                            }
                        } else if (loadingPresence) {
                            Box(Modifier.background(AppColors.TextMuted.copy(0.12f), RoundedCornerShape(Radius.Badge)).padding(horizontal = 8.dp, vertical = 4.dp)) { Text("Loading...", fontSize = 11.sp, color = AppColors.TextMuted) }
                        }
                    }

                    Text("@${user.username}" + if (user.discriminator != "0") "#${user.discriminator}" else "", fontSize = 15.sp, color = AppColors.TextMuted, fontWeight = FontWeight.Medium)

                    if (user.primaryGuildTag != null) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.background(AppColors.Primary.copy(0.10f), RoundedCornerShape(Radius.Badge))
                                .border(1.dp, AppColors.Primary.copy(0.25f), RoundedCornerShape(Radius.Badge))
                                .padding(horizontal = 8.dp, vertical = 3.dp)) {
                            if (user.primaryGuildBadge != null && user.primaryGuildId != null) {
                                AsyncImage("https://cdn.discordapp.com/clan-badges/${user.primaryGuildId}/${user.primaryGuildBadge}.png?size=16", null, Modifier.size(14.dp).clip(CircleShape))
                                Spacer(Modifier.width(4.dp))
                            }
                            Text(user.primaryGuildTag, fontSize = 10.sp, color = AppColors.Primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (presence?.customStatus != null) {
                        Spacer(Modifier.height(8.dp))
                        val cs = presence.customStatus
                        Row(Modifier.background(AppColors.Surface, RoundedCornerShape(Radius.Medium)).border(1.dp, AppColors.Divider, RoundedCornerShape(Radius.Medium)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            if (cs.emojiId != null) {
                                val ext = if (cs.emojiAnimated) "gif" else "png"
                                AsyncImage("https://cdn.discordapp.com/emojis/${cs.emojiId}.$ext?size=32", null, Modifier.size(20.dp))
                                if (cs.text != null) Spacer(Modifier.width(6.dp))
                            } else if (cs.emojiName != null) {
                                Text(cs.emojiName, fontSize = 18.sp)
                                if (cs.text != null) Spacer(Modifier.width(6.dp))
                            }
                            if (!cs.text.isNullOrBlank()) {
                                Text(cs.text, fontSize = 13.sp, color = AppColors.TextSecondary, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    if (presence?.platforms?.isNotEmpty() == true) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            presence.platforms.forEach { p ->
                                Box(Modifier.background(AppColors.Primary.copy(0.10f), RoundedCornerShape(Radius.Badge)).border(1.dp, AppColors.Primary.copy(0.20f), RoundedCornerShape(Radius.Badge)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text(p, fontSize = 10.sp, color = AppColors.Primary.copy(0.9f), fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (loadingBadges) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(12.dp), color = AppColors.Primary, strokeWidth = 1.5.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Loading badges...", fontSize = 11.sp, color = AppColors.TextMuted)
                        }
                    } else if (badges.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            badges.forEach { badge ->
                                Row(Modifier.background(badge.color.copy(0.12f), RoundedCornerShape(Radius.Badge))
                                    .border(1.dp, badge.color.copy(0.35f), RoundedCornerShape(Radius.Badge))
                                    .clickable { selectedBadge = badge }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(badge.cdnUrl, badge.label, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(badge.label, fontSize = 11.sp, color = badge.color, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))
                        ProfileSection("About Me", Icons.AutoMirrored.Outlined.Notes) { DiscordMarkdown(user.bio, Modifier.fillMaxWidth()) }
                    }

                    if (loadingPresence) {
                        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))
                        ProfileSection("Current Activity", Icons.Outlined.Games) {
                            Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(16.dp), color = AppColors.Primary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("Loading activity...", fontSize = 13.sp, color = AppColors.TextMuted) }
                        }
                    } else if (presence?.activities?.isNotEmpty() == true) {
                        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))
                        ProfileSection("Current Activity", Icons.Outlined.Games) { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { presence.activities.forEach { ActivityCard(it) } } }
                    }

                    Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))

                    val createdMs = snowflakeToTimestamp(user.id)
                    ProfileSection("Account Info", Icons.Outlined.Tag) {
                        InfoRow(Icons.Outlined.Tag, "User ID", user.id)
                        InfoRow(Icons.Outlined.CalendarMonth, "Created", snowflakeToDate(user.id))
                        InfoRow(Icons.Outlined.Update, "Account Age", accountAgeString(createdMs))
                        if (!user.email.isNullOrBlank())  InfoRow(Icons.Outlined.Email,    "Email",    user.email)
                        if (!user.phone.isNullOrBlank())  InfoRow(Icons.Outlined.Phone,    "Phone",    user.phone)
                        if (!user.locale.isNullOrBlank()) InfoRow(Icons.Outlined.Language, "Locale",   localeLabel(user.locale))
                        InfoRow(Icons.Outlined.Lock,  "2FA",      if (user.mfaEnabled)  "Enabled"  else "Disabled",  if (user.mfaEnabled)  AppColors.Success else AppColors.TextMuted)
                        InfoRow(Icons.Outlined.CheckCircle, "Verified", if (user.verified) "Yes"  else "No",          if (user.verified)    AppColors.Success else AppColors.TextMuted)
                        if (user.premiumType > 0) InfoRow(Icons.Outlined.WorkspacePremium, "Nitro", nitroLabel(user.premiumType), Color(0xFF5865F2))
                        if (loadingGuilds) InfoRow(Icons.Outlined.Groups, "Servers", "Loading...")
                        else if (guildCount != null) InfoRow(Icons.Outlined.Groups, "Servers", guildCount.toString() + if (guildCount >= 100) "+" else "")
                        InfoRow(Icons.Outlined.Security, "Public Flags", "0x${user.publicFlags.toString(16).uppercase()} (${user.publicFlags})")
                        if (badges.isNotEmpty()) InfoRow(Icons.Outlined.WorkspacePremium, "Badges", "${badges.size} active")
                        if (user.avatarDecorationAsset != null) InfoRow(Icons.Outlined.AutoAwesome, "Decoration", "Active ✓", AppColors.Success)
                        if (user.nameplateAsset != null) InfoRow(Icons.Outlined.AutoAwesome, "Nameplate", if (!user.nameplateLabel.isNullOrBlank()) user.nameplateLabel else "Active ✓", AppColors.Success)
                        if (!user.bannerColor.isNullOrBlank()) {
                            val c = runCatching { Color(android.graphics.Color.parseColor(user.bannerColor)) }.getOrNull()
                            if (c != null) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.AutoAwesome, null, tint = AppColors.TextMuted, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(12.dp))
                                    Text("Accent", fontSize = 13.sp, color = AppColors.TextMuted, modifier = Modifier.width(90.dp))
                                    Box(Modifier.size(20.dp).background(c, RoundedCornerShape(4.dp)).border(1.dp, AppColors.Divider, RoundedCornerShape(4.dp))); Spacer(Modifier.width(8.dp))
                                    Text(user.bannerColor.uppercase(), fontSize = 13.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                        if (user.primaryGuildTag != null) InfoRow(Icons.Outlined.Groups, "Clan Tag", user.primaryGuildTag, AppColors.Primary)
                    }

                    Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))

                    if (loadingConns) {
                        ProfileSection("Connected Accounts", Icons.Outlined.Link) { Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(16.dp), color = AppColors.Primary, strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("Loading connections...", fontSize = 13.sp, color = AppColors.TextMuted) } }
                        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))
                    } else if (!connections.isNullOrEmpty()) {
                        val visible = if (expandConns) connections else connections.take(3)
                        ProfileSection("Connected Accounts (${connections.size})", Icons.Outlined.Link) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                visible.forEach { conn ->
                                    val cc = connectionColor(conn.type)
                                    val profileUrl = connectionProfileUrl(conn.type, conn.name)
                                    Row(Modifier.fillMaxWidth()
                                        .background(cc.copy(0.06f), RoundedCornerShape(Radius.Medium))
                                        .border(1.dp, cc.copy(0.20f), RoundedCornerShape(Radius.Medium))
                                        .then(if (profileUrl != null) Modifier.clickable { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(profileUrl))) } else Modifier)
                                        .padding(horizontal = 12.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(32.dp).background(cc.copy(0.18f), RoundedCornerShape(Radius.Small)), contentAlignment = Alignment.Center) {
                                            Text(connectionLabel(conn.type).first().toString(), fontSize = 14.sp, color = cc, fontWeight = FontWeight.Black)
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column(Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(connectionLabel(conn.type), fontSize = 11.sp, color = cc, fontWeight = FontWeight.Bold, letterSpacing = 0.3.sp)
                                                if (profileUrl != null) { Spacer(Modifier.width(4.dp)); Icon(Icons.Outlined.Link, null, tint = cc.copy(0.6f), modifier = Modifier.size(10.dp)) }
                                            }
                                            Text(conn.name, fontSize = 13.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        if (conn.verified) Box(Modifier.background(AppColors.Success.copy(0.12f), RoundedCornerShape(Radius.Badge)).padding(horizontal = 7.dp, vertical = 2.dp)) { Text("✓", fontSize = 11.sp, color = AppColors.Success, fontWeight = FontWeight.Bold) }
                                    }
                                }
                                if (connections.size > 3) {
                                    TextButton(onClick = { expandConns = !expandConns }, Modifier.fillMaxWidth()) {
                                        Icon(if (expandConns) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                                        Text(if (expandConns) "Show less" else "Show ${connections.size - 3} more", fontSize = 12.sp, color = AppColors.Primary)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp)); HorizontalDivider(color = AppColors.Divider); Spacer(Modifier.height(16.dp))
                    }

                    Spacer(Modifier.height(28.dp)); Footer(clicks = footerClicks, onClick = onFooterClick); Spacer(Modifier.height(10.dp)); GitHubButton()
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    @Composable
    fun ActivityCard(activity: PresenceActivity) {
        val isSpotify = activity.type == 2
        val isStreaming = activity.type == 1
        val accent = when {
            isSpotify   -> AppColors.Spotify
            isStreaming  -> Color(0xFF9147FF)
            else         -> AppColors.Primary
        }
        var elapsed   by remember { mutableStateOf("") }
        var remaining by remember { mutableStateOf("") }
        var progFraction by remember { mutableStateOf(0f) }
        LaunchedEffect(activity.name, activity.timestamps?.start, activity.timestamps?.end) {
            while (true) {
                val ts = activity.timestamps
                if (ts?.start != null) elapsed = formatElapsed(ts.start)
                if (ts?.end != null) {
                    remaining = formatRemaining(ts.end)
                    val total = (ts.end - (ts.start ?: ts.end)).coerceAtLeast(1L)
                    progFraction = ((System.currentTimeMillis() - (ts.start ?: ts.end)).toFloat() / total).coerceIn(0f, 1f)
                }
                delay(1000)
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = accent.copy(0.07f)), shape = RoundedCornerShape(Radius.Card),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.22f))) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    val largeUrl = activity.largeImageUrl
                    if (largeUrl != null) {
                        Box(Modifier.size(60.dp)) {
                            AsyncImage(largeUrl, null, Modifier.fillMaxSize().clip(RoundedCornerShape(Radius.Small)), contentScale = ContentScale.Crop)
                            val smallUrl = activity.smallImageUrl
                            if (smallUrl != null) {
                                AsyncImage(smallUrl, null,
                                    Modifier.size(22.dp).align(Alignment.BottomEnd).clip(CircleShape).border(2.dp, AppColors.Background, CircleShape),
                                    contentScale = ContentScale.Crop)
                            }
                        }
                    } else if (activity.applicationId != null) {
                        AsyncImage(
                            model = "https://cdn.discordapp.com/app-icons/${activity.applicationId}/icon.png",
                            contentDescription = null,
                            modifier = Modifier.size(60.dp).clip(RoundedCornerShape(Radius.Small)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(Modifier.size(60.dp).background(accent.copy(0.15f), RoundedCornerShape(Radius.Small)), contentAlignment = Alignment.Center) {
                            Icon(if (isSpotify) Icons.Outlined.MusicNote else Icons.Outlined.Games, null, tint = accent, modifier = Modifier.size(28.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(activityTypeLabel(activity.type), fontSize = 10.sp, color = accent, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Text(activity.name, fontSize = 14.sp, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.details.isNullOrBlank()) Text(activity.details, fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.state.isNullOrBlank())   Text(activity.state,   fontSize = 12.sp, color = AppColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.assets?.largeText.isNullOrBlank()) Text(activity.assets!!.largeText!!, fontSize = 11.sp, color = AppColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (!activity.assets?.smallText.isNullOrBlank()) Text(activity.assets!!.smallText!!, fontSize = 11.sp, color = AppColors.TextMuted.copy(0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                val ts = activity.timestamps
                if (ts != null) {
                    Spacer(Modifier.height(8.dp))
                    if (ts.start != null && ts.end != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(elapsed, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.weight(1f).height(3.dp).background(accent.copy(0.2f), CircleShape)) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth(progFraction).background(accent, CircleShape))
                            }
                            Spacer(Modifier.width(6.dp))
                            Text(remaining, fontSize = 10.sp, color = AppColors.TextMuted, fontFamily = FontFamily.Monospace)
                        }
                    } else if (ts.start != null) {
                        Text("$elapsed elapsed", fontSize = 10.sp, color = AppColors.TextMuted)
                    }
                }
                if (activity.buttons.isNotEmpty()) {
                    val btnCtx = LocalContext.current
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        activity.buttons.forEach { btn ->
                            OutlinedButton(
                                onClick = { runCatching { btnCtx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(btn.url))) } },
                                modifier = Modifier.weight(1f).height(32.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(0.5f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                            ) {
                                Text(btn.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ProfileSection(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                Icon(icon, null, tint = AppColors.TextMuted, modifier = Modifier.size(13.dp)); Spacer(Modifier.width(5.dp))
                Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AppColors.TextMuted, letterSpacing = 0.8.sp)
            }
            content()
        }
    }

    @Composable
    fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color = AppColors.TextPrimary) {
        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AppColors.TextMuted, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(12.dp))
            Text(label, fontSize = 13.sp, color = AppColors.TextMuted, modifier = Modifier.width(90.dp))
            Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    @Composable
    fun Footer(clicks: Int, onClick: () -> Unit) {
        val t = rememberInfiniteTransition(label = "rgb")
        val hue by t.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart), label = "h")
        val c = Color.hsv(hue, 0.75f, 1f)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.clickable { onClick() }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = c, modifier = Modifier.size(13.dp)); Spacer(Modifier.width(6.dp))
                Text("By Rhyan57", fontSize = 12.sp, color = c, fontWeight = FontWeight.Bold); Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.AutoAwesome, null, tint = c, modifier = Modifier.size(13.dp))
            }
            if (clicks in 1..4) Text("${5 - clicks}x more to open console", fontSize = 10.sp, color = AppColors.TextMuted.copy(0.5f))
        }
    }

    @Composable
    fun GitHubButton() {
        val ctx = LocalContext.current
        OutlinedButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-Rhyan57/GetDiscordToken"))) },
            modifier = Modifier.fillMaxWidth().height(46.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextMuted),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Divider), shape = RoundedCornerShape(Radius.Button)) {
            Icon(Icons.Outlined.Code, null, modifier = Modifier.size(16.dp), tint = AppColors.TextMuted); Spacer(Modifier.width(8.dp))
            Text("Sc-Rhyan57/GetDiscordToken", fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
        }
    }

    private suspend fun fetchUserInfo(token: String): DiscordUser = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        addLog("INFO", "API", "GET /users/@me", "Authorization: ${token.take(20)}...")
        val req  = Request.Builder().url("https://discord.com/api/v10/users/@me").header("Authorization", token).build()
        val resp = httpClient.newCall(req).execute()
        val body = resp.body?.string() ?: throw IOException("Empty body")
        val ms   = System.currentTimeMillis() - startMs
        if (!resp.isSuccessful) {
            addLog("ERROR", "API", "HTTP ${resp.code} /users/@me (${ms}ms)", "Body: ${body.take(300)}")
            throw IOException("HTTP ${resp.code}")
        }
        addLog("SUCCESS", "API", "HTTP 200 /users/@me (${ms}ms)", "size=${body.length} bytes")
        val j = JSONObject(body)
        fun str(k: String) = j.optString(k).takeIf { it.isNotEmpty() && it != "null" }
        val dec = j.optJSONObject("avatar_decoration_data")
        val col = j.optJSONObject("collectibles")
        val np  = col?.optJSONObject("nameplate")
        val pg  = j.optJSONObject("primary_guild")
        DiscordUser(
            id = j.optString("id"), username = j.optString("username"),
            discriminator = j.optString("discriminator", "0"), globalName = str("global_name"),
            avatar = str("avatar"), banner = str("banner"), bannerColor = str("banner_color"),
            bio = str("bio"), email = str("email"), phone = str("phone"),
            verified = j.optBoolean("verified", false), mfaEnabled = j.optBoolean("mfa_enabled", false),
            locale = str("locale"), premiumType = j.optInt("premium_type", 0),
            publicFlags = j.optLong("public_flags", 0L),
            flags = j.optLong("flags", j.optLong("public_flags", 0L)),
            accentColor = if (!j.isNull("accent_color")) j.optInt("accent_color") else null,
            avatarDecorationAsset = dec?.optString("asset")?.takeIf { it.isNotEmpty() },
            nameplateAsset        = np?.optString("asset")?.takeIf { it.isNotEmpty() },
            nameplateLabel        = np?.optString("label")?.takeIf { it.isNotEmpty() },
            primaryGuildTag       = pg?.optString("tag")?.takeIf { it.isNotEmpty() },
            primaryGuildBadge     = pg?.optString("badge")?.takeIf { it.isNotEmpty() },
            primaryGuildId        = pg?.optString("identity_guild_id")?.takeIf { it.isNotEmpty() },
            hasLegacyUsername     = j.has("legacy_username") || j.optJSONObject("legacy_username") != null,
            hasQuestBadge         = (j.optLong("public_flags", 0L) and 549755813888L != 0L),
            hasOrbsBadge          = false
        )
    }

    private suspend fun fetchUserBadges(token: String, userId: String): List<BadgeInfo> = withContext(Dispatchers.IO) {
        val startMs = System.currentTimeMillis()
        addLog("INFO", "API", "GET /users/$userId/profile")
        try {
            val resp = httpClient.newCall(
                Request.Builder()
                    .url("https://discord.com/api/v10/users/$userId/profile?with_mutual_guilds=false&with_mutual_friends_count=false")
                    .header("Authorization", token)
                    .build()
            ).execute()
            if (!resp.isSuccessful) {
                addLog("WARN", "API", "HTTP ${resp.code} /profile — badges unavailable")
                return@withContext emptyList()
            }
            val body = resp.body?.string() ?: return@withContext emptyList()
            val ms = System.currentTimeMillis() - startMs
            val json = JSONObject(body)
            val badgesArr = json.optJSONArray("badges")
            if (badgesArr == null || badgesArr.length() == 0) {
                addLog("INFO", "API", "HTTP 200 /profile (${ms}ms) — no badges")
                return@withContext emptyList()
            }
            val result = mutableListOf<BadgeInfo>()
            for (i in 0 until badgesArr.length()) {
                val b = badgesArr.getJSONObject(i)
                val id   = b.optString("id", "")
                val icon = b.optString("icon", "").takeIf { it.isNotEmpty() } ?: continue
                val desc = b.optString("description", id)
                val link = b.optString("link").takeIf { it.isNotEmpty() && it != "null" }
                val label = badgeLabelFromId(id, desc)
                result.add(BadgeInfo(
                    id          = id,
                    label       = label,
                    color       = badgeColorFromId(id),
                    cdnUrl      = "https://cdn.discordapp.com/badge-icons/$icon.png",
                    description = desc,
                    link        = link
                ))
            }
            addLog("SUCCESS", "API", "HTTP 200 /profile (${ms}ms) — ${result.size} badges", result.map { it.id }.joinToString())
            result
        } catch (e: Exception) {
            addLog("ERROR", "API", "fetchUserBadges failed: ${e.message}")
            emptyList()
        }
    }

    private fun badgeLabelFromId(id: String, fallback: String): String = when {
        id == "staff"                      -> "Discord Staff"
        id == "partner"                    -> "Partner"
        id == "hypesquad"                  -> "HypeSquad Events"
        id == "bug_hunter_level_1"         -> "Bug Hunter"
        id == "bug_hunter_level_2"         -> "Bug Hunter Lv.2"
        id == "hypesquad_house_1"          -> "HypeSquad Bravery"
        id == "hypesquad_house_2"          -> "HypeSquad Brilliance"
        id == "hypesquad_house_3"          -> "HypeSquad Balance"
        id == "early_supporter"            -> "Early Supporter"
        id == "verified_developer"         -> "Early Dev"
        id == "moderator_programs_alumni"  -> "Mod Alumni"
        id == "active_developer"           -> "Active Dev"
        id == "legacy_username"            -> "Legacy Username"
        id.startsWith("premium_tenure")    -> {
            val months = id.filter { it.isDigit() }.take(4).toIntOrNull()
            if (months != null) "Nitro ${months}mo" else "Nitro Tenure"
        }
        id.startsWith("guild_booster")     -> {
            val lvl = id.lastOrNull { it.isDigit() }?.toString()
            if (lvl != null) "Booster Lv.$lvl" else "Server Booster"
        }
        id == "premium"                    -> "Nitro"
        id.contains("quest")               -> "Quest"
        else                               -> fallback.take(18)
    }

    private suspend fun connectGateway(token: String, onUpdate: (DiscordPresence) -> Unit): DiscordPresence? = withContext(Dispatchers.IO) {
        addLog("INFO", "Gateway", "Starting gateway connection…", GATEWAY_URL)
        connectGatewayInternal(token, false, onUpdate)
        var waited = 0
        while (currentPresence == null && waited < 15000) { delay(100); waited += 100 }
        if (currentPresence == null) addLog("WARN", "Gateway", "Timed out waiting for READY (${waited}ms)")
        currentPresence
    }

    private suspend fun fetchConnections(token: String): List<DiscordConnection> = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        addLog("INFO", "API", "GET /users/@me/connections")
        val resp = httpClient.newCall(Request.Builder().url("https://discord.com/api/v10/users/@me/connections").header("Authorization", token).build()).execute()
        if (!resp.isSuccessful) {
            addLog("ERROR", "API", "HTTP ${resp.code} /connections (${System.currentTimeMillis()-start}ms)")
            return@withContext emptyList()
        }
        val body = resp.body?.string() ?: return@withContext emptyList()
        val arr = JSONArray(body)
        addLog("SUCCESS", "API", "HTTP 200 /connections (${System.currentTimeMillis()-start}ms)", "count=${arr.length()}")
        buildList { for (i in 0 until arr.length()) { val o = arr.getJSONObject(i); add(DiscordConnection(o.optString("type"), o.optString("name"), o.optBoolean("verified", false), o.optInt("visibility", 0))) } }
    }

    private suspend fun fetchGuildCount(token: String): Int? = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        addLog("INFO", "API", "GET /users/@me/guilds")
        val resp = httpClient.newCall(Request.Builder().url("https://discord.com/api/v10/users/@me/guilds").header("Authorization", token).build()).execute()
        if (!resp.isSuccessful) { addLog("ERROR", "API", "HTTP ${resp.code} /guilds"); return@withContext null }
        val count = JSONArray(resp.body?.string() ?: return@withContext null).length()
        addLog("SUCCESS", "API", "HTTP 200 /guilds (${System.currentTimeMillis()-start}ms)", "count=$count")
        count
    }

    private suspend fun checkLatestVersion(): String? = withContext(Dispatchers.IO) {
        try {
            val resp = httpClient.newCall(Request.Builder().url(GITHUB_API_LATEST).header("Accept", "application/vnd.github.v3+json").build()).execute()
            if (!resp.isSuccessful) return@withContext null
            JSONObject(resp.body?.string() ?: return@withContext null).optString("tag_name").takeIf { it.isNotEmpty() }
        } catch (_: Exception) { null }
    }
}
