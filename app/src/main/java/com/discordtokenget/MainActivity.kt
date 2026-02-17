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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
    val Success = Color(0xFF3BA55D)
    val Error = Color(0xFFED4245)
    val ErrorContainer = Color(0xFF3B1A1B)
    val OnError = Color(0xFFFFDFDE)
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFB5BAC1)
    val TextMuted = Color(0xFF80848E)
}

private val KizzyDarkColorScheme = darkColorScheme(
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
            MaterialTheme(colorScheme = KizzyDarkColorScheme) {
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
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val androidVersion = Build.VERSION.RELEASE
        return """Discord Token — Crash Report
Fabricante: $manufacturer
Dispositivo: $model
Android: $androidVersion
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
                        Text(
                            text = "O app crashou",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = AppColors.TextPrimary
                        )
                    },
                    actions = {
                        TextButton(onClick = { android.os.Process.killProcess(android.os.Process.myPid()) }) {
                            Text("Fechar", color = AppColors.Error, fontWeight = FontWeight.Bold)
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
                    Text("Copiar Log", fontWeight = FontWeight.Bold, color = AppColors.OnPrimary)
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
                    text = "Um erro inesperado ocorreu. Copie o log abaixo e reporte ao desenvolvedor.",
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

            Spacer(Modifier.height(56.dp))

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
                        text = "Obter Token",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Faça login com sua conta Discord para extrair o token de acesso.",
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
                        Text(
                            text = "Entrar com Discord",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.OnPrimary
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "Seguro • Privado • Local",
                        fontSize = 12.sp,
                        color = AppColors.Success,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Footer()
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
                    Text("← Voltar", fontWeight = FontWeight.Bold)
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
            Box(modifier = Modifier.fillMaxWidth().height(130.dp)) {
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
                                        AppColors.Primary.copy(alpha = 0.5f)
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
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Card(
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .width(80.dp)
                            .height(80.dp),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Background),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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

                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.Error),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AppColors.Error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Sair", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 3.dp)
                    }
                } else if (user != null) {
                    Text(
                        text = user.displayName ?: user.username,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        text = "@${user.username}",
                        fontSize = 15.sp,
                        color = AppColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = user.bio,
                                modifier = Modifier.padding(14.dp),
                                fontSize = 14.sp,
                                color = AppColors.TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    InfoRow(label = "ID da Conta", value = user.id)

                    Spacer(Modifier.height(14.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Token de Acesso",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TextPrimary
                            )

                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceVariant),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (showToken) token else "●".repeat(40),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = AppColors.TextSecondary,
                                        maxLines = if (showToken) Int.MAX_VALUE else 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(
                                        onClick = onToggleToken,
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (showToken) "Ocultar token" else "Revelar token",
                                            fontSize = 12.sp,
                                            color = AppColors.Primary,
                                            fontWeight = FontWeight.SemiBold
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
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Copiar Token",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-rhyan57/GetDiscordToken"))
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Sc-rhyan57/GetDiscordToken",
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(Modifier.height(36.dp))
                Footer()
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    @Composable
    fun InfoRow(label: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = AppColors.TextMuted,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
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
        Text(
            text = "feito com carinho por rhyan57",
            fontSize = 12.sp,
            color = AppColors.TextMuted,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
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
