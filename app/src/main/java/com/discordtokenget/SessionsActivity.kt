package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val sessHttp = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

private object SColors {
    val Bg      = Color(0xFF1E1F22)
    val Surface = Color(0xFF2B2D31)
    val Card    = Color(0xFF313338)
    val Primary = Color(0xFF5865F2)
    val Success = Color(0xFF23A55A)
    val Warning = Color(0xFFFAA61A)
    val Error   = Color(0xFFED4245)
    val White   = Color(0xFFF2F3F5)
    val SubText = Color(0xFFB5BAC1)
    val Muted   = Color(0xFF80848E)
    val Divider = Color(0xFF3F4147)
}

data class AuthSession(
    val idHash: String,
    val approxLastUsed: String,
    val os: String?,
    val platform: String?,
    val location: String?
)

class SessionsActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        fun start(ctx: Context, token: String) {
            ctx.startActivity(Intent(ctx, SessionsActivity::class.java).putExtra(EXTRA_TOKEN, token))
        }
    }

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = SColors.Primary, background = SColors.Bg, surface = SColors.Surface)) {
                Surface(Modifier.fillMaxSize(), color = SColors.Bg) {
                    SessionsScreen(token = token, onBack = { finish() })
                }
            }
        }
    }
}

private suspend fun fetchSessions(token: String): List<AuthSession> = withContext(Dispatchers.IO) {
    try {
        val resp = sessHttp.newCall(
            Request.Builder().url("https://discord.com/api/v10/auth/sessions")
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .build()
        ).execute()
        if (!resp.isSuccessful) return@withContext emptyList()
        val body = resp.body?.string() ?: return@withContext emptyList()
        val j = JSONObject(body)
        val arr = j.optJSONArray("user_sessions") ?: return@withContext emptyList()
        val list = mutableListOf<AuthSession>()
        for (i in 0 until arr.length()) {
            val s = arr.getJSONObject(i)
            val ci = s.optJSONObject("client_info")
            list.add(AuthSession(
                idHash         = s.optString("id_hash", ""),
                approxLastUsed = s.optString("approx_last_used_time", ""),
                os             = ci?.optString("os")?.takeIf { it.isNotEmpty() && it != "null" },
                platform       = ci?.optString("platform")?.takeIf { it.isNotEmpty() && it != "null" },
                location       = ci?.optString("location")?.takeIf { it.isNotEmpty() && it != "null" }
            ))
        }
        list
    } catch (_: Exception) { emptyList() }
}

private suspend fun revokeSessions(token: String, hashes: List<String>): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val body = JSONObject().put("session_id_hashes", JSONArray(hashes)).toString()
                .toRequestBody("application/json".toMediaType())
            val resp = sessHttp.newCall(
                Request.Builder().url("https://discord.com/api/v10/auth/sessions/logout")
                    .header("Authorization", token)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()
            ).execute()
            resp.code in 200..204
        } catch (_: Exception) { false }
    }
}

private fun parseIsoShort(s: String): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also { it.timeZone = TimeZone.getTimeZone("UTC") }
        val date = sdf.parse(s.substringBefore('.')) ?: return s
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
    } catch (_: Exception) { s.take(16) }
}

private fun platformIcon(platform: String?): ImageVector = when {
    platform == null                           -> Icons.Outlined.DeviceUnknown
    platform.contains("Android", true)        -> Icons.Outlined.Smartphone
    platform.contains("iOS", true) || platform.contains("iPhone", true) -> Icons.Outlined.PhoneIphone
    platform.contains("Windows", true)        -> Icons.Outlined.Computer
    platform.contains("Mac", true)            -> Icons.Outlined.LaptopMac
    platform.contains("Linux", true)          -> Icons.Outlined.Computer
    platform.contains("Discord Client", true) -> Icons.Outlined.DesktopWindows
    platform.contains("Browser", true) || platform.contains("Chrome", true) || platform.contains("Firefox", true) -> Icons.Outlined.Language
    else -> Icons.Outlined.Devices
}

@Composable
fun SessionsScreen(token: String, onBack: () -> Unit) {
    val scope   = rememberCoroutineScope()
    var loading by remember { mutableStateOf(true) }
    var sessions by remember { mutableStateOf<List<AuthSession>>(emptyList()) }
    var error   by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showConfirmAll by remember { mutableStateOf(false) }
    var revoking by remember { mutableStateOf<String?>(null) }
    var toast   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(refreshKey) {
        loading = true; error = null
        try { sessions = fetchSessions(token) }
        catch (e: Exception) { error = e.message }
        loading = false
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            kotlinx.coroutines.delay(2500)
            toast = null
        }
    }

    if (showConfirmAll) {
        AlertDialog(
            onDismissRequest = { showConfirmAll = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = SColors.Warning, modifier = Modifier.size(28.dp)) },
            title = { Text("Terminate All Sessions?", color = SColors.White, fontWeight = FontWeight.Bold) },
            text = { Text("This will log you out of all devices except the current one. Your token in this app remains valid.", color = SColors.SubText, fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmAll = false
                        scope.launch {
                            val hashes = sessions.map { it.idHash }
                            val ok = revokeSessions(token, hashes)
                            toast = if (ok) "All sessions terminated" else "Failed to terminate sessions"
                            if (ok) refreshKey++
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SColors.Error),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Terminate All", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmAll = false }) { Text("Cancel", color = SColors.Muted) }
            },
            containerColor = SColors.Surface, shape = RoundedCornerShape(16.dp)
        )
    }

    Box(Modifier.fillMaxSize().background(SColors.Bg)) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1A1C3A), SColors.Bg)))
                    .padding(top = 40.dp, bottom = 0.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(SColors.Surface, RoundedCornerShape(12.dp))) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = SColors.White, modifier = Modifier.size(20.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Active Sessions", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = SColors.White)
                            if (!loading) Text("${sessions.size} device${if (sessions.size != 1) "s" else ""}", fontSize = 13.sp, color = SColors.Muted)
                        }
                        IconButton(onClick = { refreshKey++ }, modifier = Modifier.size(40.dp).background(SColors.Surface, RoundedCornerShape(12.dp))) {
                            Icon(Icons.Outlined.Refresh, null, tint = SColors.SubText, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (!loading && sessions.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth()
                                .background(SColors.Error.copy(0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, SColors.Error.copy(0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Security, null, tint = SColors.Error, modifier = Modifier.size(18.dp))
                                Column {
                                    Text("Security", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SColors.Error)
                                    Text("Revoke all if you suspect unauthorized access", fontSize = 10.sp, color = SColors.Muted)
                                }
                            }
                            OutlinedButton(
                                onClick = { showConfirmAll = true },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SColors.Error.copy(0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SColors.Error),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) { Text("Revoke All", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }

            when {
                loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = SColors.Primary, modifier = Modifier.size(40.dp), strokeWidth = 3.dp)
                        Text("Loading sessions...", color = SColors.Muted)
                    }
                }
                error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.ErrorOutline, null, tint = SColors.Error, modifier = Modifier.size(48.dp))
                        Text("Failed to load sessions", color = SColors.White, fontWeight = FontWeight.ExtraBold)
                        Text(error ?: "", color = SColors.Muted, fontSize = 11.sp)
                        Button(onClick = { refreshKey++ }, colors = ButtonDefaults.buttonColors(containerColor = SColors.Primary), shape = RoundedCornerShape(12.dp)) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                sessions.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Outlined.DeviceUnknown, null, tint = SColors.Muted, modifier = Modifier.size(56.dp))
                        Text("No active sessions found", color = SColors.White, fontWeight = FontWeight.ExtraBold)
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Spacer(Modifier.height(8.dp)) }
                    items(sessions, key = { it.idHash }) { session ->
                        SessionCard(
                            session    = session,
                            isRevoking = revoking == session.idHash,
                            onRevoke   = {
                                scope.launch {
                                    revoking = session.idHash
                                    val ok = revokeSessions(token, listOf(session.idHash))
                                    toast = if (ok) "Session terminated" else "Failed to terminate session"
                                    if (ok) refreshKey++
                                    revoking = null
                                }
                            }
                        )
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }

        if (toast != null) {
            Snackbar(
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                containerColor = SColors.Surface,
                contentColor = SColors.White,
                shape = RoundedCornerShape(12.dp)
            ) { Text(toast!!) }
        }
    }
}

@Composable
private fun SessionCard(session: AuthSession, isRevoking: Boolean, onRevoke: () -> Unit) {
    val icon = platformIcon(session.platform ?: session.os)
    val osColor = when {
        session.os?.contains("Android", true) == true -> Color(0xFF3DDC84)
        session.os?.contains("iOS", true) == true || session.os?.contains("Mac", true) == true -> Color(0xFF999999)
        session.os?.contains("Windows", true) == true -> Color(0xFF0078D4)
        else -> SColors.Primary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SColors.Card),
        shape  = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, SColors.Divider.copy(0.4f), RoundedCornerShape(14.dp))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(osColor.copy(0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, osColor.copy(0.25f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = osColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    session.platform ?: session.os ?: "Unknown Device",
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = SColors.White
                )
                if (!session.os.isNullOrBlank() && session.platform != null) {
                    Text(session.os, fontSize = 11.sp, color = SColors.Muted)
                }
                if (!session.location.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Outlined.LocationOn, null, tint = SColors.Muted, modifier = Modifier.size(11.dp))
                        Text(session.location, fontSize = 10.sp, color = SColors.Muted)
                    }
                }
                if (session.approxLastUsed.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Icon(Icons.Outlined.AccessTime, null, tint = SColors.Muted, modifier = Modifier.size(10.dp))
                        Text("Last used: ${parseIsoShort(session.approxLastUsed)}", fontSize = 10.sp, color = SColors.Muted, fontFamily = FontFamily.Monospace)
                    }
                }
            }
            if (isRevoking) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = SColors.Error, strokeWidth = 2.dp)
            } else {
                IconButton(
                    onClick = onRevoke,
                    modifier = Modifier.size(36.dp).background(SColors.Error.copy(0.10f), RoundedCornerShape(10.dp))
                        .border(1.dp, SColors.Error.copy(0.25f), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Outlined.Logout, null, tint = SColors.Error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
