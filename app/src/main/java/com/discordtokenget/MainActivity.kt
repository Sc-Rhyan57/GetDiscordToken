package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

data class DiscordUser(
    val id: String,
    val username: String,
    val discriminator: String,
    val displayName: String?,
    val avatar: String?,
    val bio: String?,
    val banner: String?
)

private object AppColors {
    val Background = Color(0xFF1E1F22)
    val Surface = Color(0xFF2B2D31)
    val SurfaceVariant = Color(0xFF313338)
    val Primary = Color(0xFF5865F2)
    val OnPrimary = Color(0xFFFFFFFF)
    val Success = Color(0xFF23A55A)
    val Error = Color(0xFFED4245)
    val ErrorContainer = Color(0xFF3B1A1B)
    val OnError = Color(0xFFFFDFDE)
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFB5BAC1)
    val TextMuted = Color(0xFF80848E)
}

private val DiscordDarkColorScheme = darkColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    background = AppColors.Background,
    surface = AppColors.Surface,
    surfaceVariant = AppColors.SurfaceVariant,
    onBackground = AppColors.TextPrimary,
    onSurface = AppColors.TextPrimary,
    error = AppColors.Error,
    errorContainer = AppColors.ErrorContainer,
    onError = AppColors.OnError
)

private const val JS_TOKEN =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"

private const val PREF_CRASH = "crash_prefs"
private const val KEY_CRASH_TRACE = "crash_trace"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupCrashHandler()

        val prefs = getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE)
        val crashTrace = prefs.getString(KEY_CRASH_TRACE, null)
        if (crashTrace != null) {
            prefs.edit().remove(KEY_CRASH_TRACE).apply()
        }

        setContent {
            MaterialTheme(colorScheme = DiscordDarkColorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppColors.Background
                ) {
                    if (crashTrace != null) {
                        CrashScreen(trace = crashTrace)
                    } else {
                        DiscordTokenApp()
                    }
                }
            }
        }
    }

    private fun setupCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                val trace = buildCrashLog(sw.toString())
                getSharedPreferences(PREF_CRASH, Context.MODE_PRIVATE)
                    .edit()
                    .putString(KEY_CRASH_TRACE, trace)
                    .commit()
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
            } catch (_: Exception) {
                defaultHandler?.uncaughtException(thread, throwable)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(2)
        }
    }

    private fun buildCrashLog(stacktrace: String): String {
        return """Discord Token — Crash Report
Manufacturer: ${Build.MANUFACTURER}
Device: ${Build.MODEL}
Android: ${Build.VERSION.RELEASE}
Stacktrace:
$stacktrace"""
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CrashScreen(trace: String) {
        val context = LocalContext.current
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.lucide_ic_triangle_alert),
                                contentDescription = null,
                                tint = AppColors.Error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "App Crashed",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = AppColors.TextPrimary
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) {
                            Text("Close", color = AppColors.Error, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
                )
            },
            containerColor = AppColors.Background,
            floatingActionButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crash Log", trace))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_ic_copy),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Copy Log", fontWeight = FontWeight.Bold, color = AppColors.OnPrimary)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "An unexpected error occurred. Copy the log below and report it to the developer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.ErrorContainer),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Text(
                                text = trace,
                                modifier = Modifier.padding(14.dp),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = AppColors.OnError,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DiscordTokenApp() {
        var token by remember { mutableStateOf<String?>(null) }
        var user by remember { mutableStateOf<DiscordUser?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        var showToken by remember { mutableStateOf(false) }
        var showWebView by remember { mutableStateOf(false) }

        LaunchedEffect(token) {
            if (token != null) {
                isLoading = true
                delay(300)
                try {
                    user = fetchUserInfo(token!!)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
        }

        val screen = when {
            showWebView && token == null -> "webview"
            token == null -> "login"
            else -> "profile"
        }

        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
            label = "screen_transition"
        ) { currentScreen ->
            when (currentScreen) {
                "webview" -> WebViewScreen(
                    onTokenReceived = {
                        token = it
                        showWebView = false
                    },
                    onBack = { showWebView = false }
                )
                "login" -> LoginScreen(onLoginClick = { showWebView = true })
                else -> UserProfileScreen(
                    user = user,
                    token = token ?: "",
                    isLoading = isLoading,
                    showToken = showToken,
                    onToggleToken = { showToken = !showToken },
                    onLogout = {
                        token = null
                        user = null
                        showToken = false
                    }
                )
            }
        }
    }

    @Composable
    fun LoginScreen(onLoginClick: () -> Unit) {
        val scale = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(AppColors.Primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_ic_key_round),
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Discord",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = AppColors.Primary,
                modifier = Modifier.scale(scale.value)
            )
            Text(
                text = "TOKEN EXTRACTOR",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextMuted,
                letterSpacing = 5.sp,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale.value),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Get Your Token",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Sign in with your Discord account to extract your access token.",
                        fontSize = 14.sp,
                        color = AppColors.TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(28.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_ic_log_in),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Sign in with Discord",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.OnPrimary
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SecurityBadge(
                            iconRes = R.drawable.lucide_ic_shield_check,
                            label = "Secure"
                        )
                        SecurityBadge(
                            iconRes = R.drawable.lucide_ic_eye_off,
                            label = "Private"
                        )
                        SecurityBadge(
                            iconRes = R.drawable.lucide_ic_smartphone,
                            label = "Local"
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Footer()
            Spacer(Modifier.height(12.dp))
            GitHubButton()
            Spacer(Modifier.height(8.dp))
        }
    }

    @Composable
    fun SecurityBadge(iconRes: Int, label: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = AppColors.Success,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = AppColors.Success,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        val currentOnTokenReceived by rememberUpdatedState(onTokenReceived)
        val currentOnBack by rememberUpdatedState(onBack)
        val webViewRef = remember { mutableStateOf<WebView?>(null) }

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).also { wv ->
                        webViewRef.value = wv
                        wv.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
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
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        view.loadUrl(JS_TOKEN)
                                    }, 500)
                                    return true
                                }
                                return false
                            }

                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                if (url.contains("/app") || url.endsWith("/channels/@me")) {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        view.loadUrl(JS_TOKEN)
                                    }, 800)
                                }
                            }
                        }

                        wv.webChromeClient = object : WebChromeClient() {
                            override fun onJsAlert(
                                view: WebView,
                                url: String,
                                message: String,
                                result: JsResult
                            ): Boolean {
                                result.confirm()
                                view.visibility = View.GONE
                                if (message.isNotBlank() && message != "undefined") {
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        currentOnTokenReceived(message.trim())
                                    }, 200)
                                }
                                return true
                            }
                        }

                        wv.loadUrl("https://discord.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 12.dp)
            ) {
                TextButton(
                    onClick = { currentOnBack() },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = AppColors.Background.copy(alpha = 0.85f),
                        contentColor = AppColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_ic_arrow_left),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Back", fontWeight = FontWeight.Bold)
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                webViewRef.value?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    Handler(Looper.getMainLooper()).postDelayed({
                        clearHistory()
                        destroy()
                    }, 500)
                }
                webViewRef.value = null
            }
        }
    }

    @Composable
    fun UserProfileScreen(
        user: DiscordUser?,
        token: String,
        isLoading: Boolean,
        showToken: Boolean,
        onToggleToken: () -> Unit,
        onLogout: () -> Unit
    ) {
        val context = LocalContext.current
        val scale = remember { Animatable(0.92f) }
        LaunchedEffect(Unit) {
            scale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                if (user?.banner != null) {
                    AsyncImage(
                        model = "https://cdn.discordapp.com/banners/${user.id}/${user.banner}.png?size=600",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        AppColors.Primary,
                                        Color(0xFF7289DA),
                                        AppColors.Background
                                    )
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .scale(scale.value)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .border(4.dp, AppColors.Background, CircleShape)
                            .clip(CircleShape)
                            .background(AppColors.Surface)
                    ) {
                        if (user?.avatar != null) {
                            AsyncImage(
                                model = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=256",
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(AppColors.Primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AppColors.OnPrimary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.ErrorContainer,
                            contentColor = AppColors.Error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_ic_log_out),
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColors.Primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                } else if (user != null) {
                    Text(
                        text = user.displayName ?: user.username,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "@${user.username}" + if (user.discriminator != "0") "#${user.discriminator}" else "",
                        fontSize = 15.sp,
                        color = AppColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(AppColors.Success.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_ic_check_circle),
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Token obtained successfully",
                            fontSize = 12.sp,
                            color = AppColors.Success,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.lucide_ic_notebook_text),
                                        contentDescription = null,
                                        tint = AppColors.TextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "ABOUT ME",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.TextMuted,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = user.bio,
                                    fontSize = 14.sp,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    InfoRow(
                        iconRes = R.drawable.lucide_ic_hash,
                        label = "ACCOUNT ID",
                        value = user.id
                    )

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(R.drawable.lucide_ic_fingerprint),
                                    contentDescription = null,
                                    tint = AppColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Access Token",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppColors.TextPrimary
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (showToken) token else "•".repeat(40),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary,
                                        maxLines = if (showToken) Int.MAX_VALUE else 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = onToggleToken,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(
                                                if (showToken) R.drawable.lucide_ic_eye_off
                                                else R.drawable.lucide_ic_eye
                                            ),
                                            contentDescription = if (showToken) "Hide" else "Reveal",
                                            tint = AppColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Discord Token", token))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.lucide_ic_clipboard_copy),
                                    contentDescription = null,
                                    modifier = Modifier.size(17.dp),
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Copy Token",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Footer()
                    Spacer(Modifier.height(10.dp))
                    GitHubButton()
                }

                Spacer(Modifier.height(28.dp))
            }
        }
    }

    @Composable
    fun InfoRow(iconRes: Int, label: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = AppColors.TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = AppColors.TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    color = AppColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }

    @Composable
    fun Footer() {
        val infiniteTransition = rememberInfiniteTransition(label = "rgb_footer")
        val hue by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "hue"
        )
        val rgbColor = Color.hsv(hue, 0.75f, 1f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_sparkles),
                contentDescription = null,
                tint = rgbColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = "made with love by rhyan57",
                fontSize = 12.sp,
                color = rgbColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.width(5.dp))
            Icon(
                painter = painterResource(R.drawable.lucide_ic_sparkles),
                contentDescription = null,
                tint = rgbColor,
                modifier = Modifier.size(13.dp)
            )
        }
    }

    @Composable
    fun GitHubButton() {
        val context = LocalContext.current
        OutlinedButton(
            onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-Rhyan57/GetDiscordToken"))
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextMuted),
            border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_ic_code_2),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AppColors.TextMuted
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Sc-Rhyan57/GetDiscordToken",
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            )
        }
    }

    private suspend fun fetchUserInfo(token: String): DiscordUser = withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://discord.com/api/v10/users/@me")
            .header("Authorization", token)
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw IOException("Empty response")

        if (!response.isSuccessful) throw IOException("Request failed: ${response.code}")

        val segments = body.split(",")

        fun extract(key: String): String? {
            return segments.find { it.contains("\"$key\"") }
                ?.substringAfter(":")
                ?.replace("\"", "")
                ?.replace("}", "")
                ?.trim()
                ?.takeIf { it != "null" && it.isNotEmpty() }
        }

        DiscordUser(
            id = extract("id") ?: "",
            username = extract("username") ?: "",
            discriminator = extract("discriminator") ?: "0",
            displayName = extract("global_name"),
            avatar = extract("avatar"),
            bio = extract("bio"),
            banner = extract("banner")
        )
    }
}
