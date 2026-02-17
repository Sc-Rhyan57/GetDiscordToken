package com.discordtokenget

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.*
import okhttp3.*
import java.io.IOException

data class DiscordUser(
    val id: String,
    val username: String,
    val discriminator: String,
    val displayName: String?,
    val avatar: String?,
    val bio: String?,
    val banner: String?
)

object DiscordColors {
    val Background = Color(0xFF1E1F22)
    val Secondary = Color(0xFF2B2D31)
    val Tertiary = Color(0xFF313338)
    val Blurple = Color(0xFF5865F2)
    val Green = Color(0xFF3BA55D)
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFB5BAC1)
    val TextMuted = Color(0xFF80848E)
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkPermissions()

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DiscordTokenApp()
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.INTERNET)
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

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DiscordColors.Background
        ) {
            AnimatedContent(
                targetState = when {
                    showWebView && token == null -> "webview"
                    token == null -> "login"
                    else -> "profile"
                },
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    "webview" -> WebViewScreen(
                        onTokenReceived = {
                            token = it
                            showWebView = false
                        },
                        onBack = { showWebView = false }
                    )
                    "login" -> LoginScreen(onLoginClick = { showWebView = true })
                    "profile" -> UserProfileScreen(
                        user = user,
                        token = token!!,
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.ChatBubble,
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale.value),
                tint = DiscordColors.Blurple
            )

            Spacer(Modifier.height(24.dp))

            Text(
                "Discord Token",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = DiscordColors.TextPrimary,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                "EXTRACTOR",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordColors.TextMuted,
                letterSpacing = 6.sp,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(Modifier.height(60.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.scale(scale.value)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = DiscordColors.Blurple
                    )

                    Spacer(Modifier.height(24.dp))

                    Text(
                        "Get Your Token",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Securely extract your Discord access token",
                        fontSize = 14.sp,
                        color = DiscordColors.TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.Blurple),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Login with Discord",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = DiscordColors.Green
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Secure & Private",
                            fontSize = 12.sp,
                            color = DiscordColors.Green,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            RainbowText()
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        val isPageLoading = remember { mutableStateOf(true) }
        val progress = remember { mutableFloatStateOf(0f) }
        val currentOnTokenReceived by rememberUpdatedState(onTokenReceived)
        val currentOnBack by rememberUpdatedState(onBack)
        val webViewRef = remember { mutableStateOf<WebView?>(null) }

        Column(Modifier.fillMaxSize()) {
            Surface(
                color = DiscordColors.Secondary,
                shadowElevation = 4.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentOnBack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = DiscordColors.TextPrimary
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Discord Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DiscordColors.TextPrimary
                        )

                        Spacer(Modifier.weight(1f))

                        AnimatedVisibility(
                            visible = isPageLoading.value,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DiscordColors.Blurple,
                                strokeWidth = 2.5.dp
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = progress.floatValue > 0f && progress.floatValue < 1f,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        LinearProgressIndicator(
                            progress = { progress.floatValue },
                            modifier = Modifier.fillMaxWidth(),
                            color = DiscordColors.Blurple,
                            trackColor = DiscordColors.Tertiary
                        )
                    }
                }
            }

            AndroidView(
                factory = { ctx ->
                    WebView(ctx).also { wv ->
                        webViewRef.value = wv

                        wv.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        wv.setBackgroundColor(android.graphics.Color.parseColor("#1E1F22"))

                        wv.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        wv.webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                progress.floatValue = newProgress / 100f
                                isPageLoading.value = newProgress < 100
                            }
                        }

                        wv.webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isPageLoading.value = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isPageLoading.value = false

                                view?.postDelayed({
                                    view.evaluateJavascript("""
                                        (function() {
                                            try {
                                                let token = null;
                                                
                                                const iframe = document.createElement('iframe');
                                                iframe.style.display = 'none';
                                                document.body.appendChild(iframe);
                                                
                                                try {
                                                    const iframeLocalStorage = iframe.contentWindow.localStorage;
                                                    const storedToken = iframeLocalStorage.getItem('token');
                                                    if (storedToken) {
                                                        token = storedToken.replace(/"/g, '');
                                                    }
                                                } catch(e) {}
                                                
                                                document.body.removeChild(iframe);
                                                
                                                if (!token && typeof webpackChunkdiscord_app !== 'undefined') {
                                                    webpackChunkdiscord_app.push([
                                                        [Symbol()],
                                                        {},
                                                        req => {
                                                            for (let mod of Object.values(req.c)) {
                                                                try {
                                                                    if (mod?.exports?.default?.getToken) {
                                                                        token = mod.exports.default.getToken();
                                                                    }
                                                                    if (mod?.exports?.getToken) {
                                                                        token = mod.exports.getToken();
                                                                    }
                                                                } catch(e) {}
                                                            }
                                                        }
                                                    ]);
                                                    webpackChunkdiscord_app.pop();
                                                }
                                                
                                                return token || null;
                                            } catch(err) {
                                                return null;
                                            }
                                        })();
                                    """.trimIndent()) { result ->
                                        if (result != null && result != "null" && result != "\"null\"") {
                                            val cleanToken = result.replace("\"", "").trim()
                                            if (cleanToken.isNotEmpty() && cleanToken != "null" && cleanToken.length > 20) {
                                                currentOnTokenReceived(cleanToken)
                                            }
                                        }
                                    }
                                }, 1000)
                            }
                        }

                        wv.loadUrl("https://discord.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        DisposableEffect(Unit) {
            onDispose {
                webViewRef.value?.apply {
                    stopLoading()
                    clearHistory()
                    destroy()
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
        val scale = remember { Animatable(0.8f) }

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
                        contentDescription = "Banner",
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
                                        DiscordColors.Blurple,
                                        DiscordColors.Blurple.copy(alpha = 0.7f)
                                    )
                                )
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .scale(scale.value)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-50).dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Card(
                        shape = CircleShape,
                        modifier = Modifier.size(96.dp),
                        colors = CardDefaults.cardColors(containerColor = DiscordColors.Background),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (user?.avatar != null) {
                                AsyncImage(
                                    model = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=256",
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(DiscordColors.Blurple),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFED4245)),
                        border = BorderStroke(1.5.dp, Color(0xFFED4245)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Logout", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = DiscordColors.Blurple,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (user != null) {
                    Text(
                        user.displayName ?: user.username,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = DiscordColors.TextPrimary
                    )

                    Text(
                        "@${user.username}",
                        fontSize = 16.sp,
                        color = DiscordColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = DiscordColors.TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    user.bio,
                                    fontSize = 14.sp,
                                    color = DiscordColors.TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    InfoCard(title = "User ID", value = user.id)

                    Spacer(Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = DiscordColors.Blurple
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Access Token",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DiscordColors.TextPrimary
                                )
                            }

                            Spacer(Modifier.height(16.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DiscordColors.Tertiary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (showToken) token else "•".repeat(60),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 13.sp,
                                        color = DiscordColors.TextSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = if (showToken) Int.MAX_VALUE else 1
                                    )
                                    IconButton(
                                        onClick = onToggleToken,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showToken) "Hide" else "Show",
                                            tint = DiscordColors.TextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Discord Token", token))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.Green),
                                shape = RoundedCornerShape(12.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Copy to Clipboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-rhyan57/GetDiscordToken"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordColors.TextPrimary),
                        border = BorderStroke(1.5.dp, DiscordColors.Tertiary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Sc-rhyan57/GetDiscordToken", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(40.dp))

                RainbowText()

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    @Composable
    fun InfoCard(title: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = DiscordColors.Blurple
                )
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, fontSize = 13.sp, color = DiscordColors.TextMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(4.dp))
                    Text(value, fontSize = 15.sp, color = DiscordColors.TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @Composable
    fun RainbowText() {
        val infiniteTransition = rememberInfiniteTransition(label = "rainbow")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "offset"
        )

        val colors = listOf(
            Color(0xFFFF0000), Color(0xFFFF7F00), Color(0xFFFFFF00),
            Color(0xFF00FF00), Color(0xFF0000FF), Color(0xFF4B0082),
            Color(0xFF9400D3), Color(0xFFFF0000)
        )

        val brush = Brush.linearGradient(
            colors = colors,
            start = androidx.compose.ui.geometry.Offset(offset * 1000f, 0f),
            end = androidx.compose.ui.geometry.Offset(offset * 1000f + 500f, 0f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "With",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(brush = brush)
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFF1744)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "By rhyan57",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                style = androidx.compose.ui.text.TextStyle(brush = brush)
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
        val body = response.body?.string() ?: throw IOException("Empty")

        if (!response.isSuccessful) throw IOException("Failed: ${response.code}")

        val lines = body.split(",")

        fun extract(key: String): String? {
            return lines.find { it.contains("\"$key\"") }
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
