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
import android.view.View
import android.view.ViewGroup
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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

private const val JS_SNIPPET =
    "javascript:(function()%7Bvar%20i%3Ddocument.createElement('iframe')%3Bdocument.body.appendChild(i)%3Balert(i.contentWindow.localStorage.token.slice(1,-1))%7D)()"

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.INTERNET)
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                DiscordTokenApp()
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

                    Row(verticalAlignment = Alignment.CenterVertically) {
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

            Credits()
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        val currentOnTokenReceived by rememberUpdatedState(onTokenReceived)
        val currentOnBack by rememberUpdatedState(onBack)
        val webViewRef = remember { mutableStateOf<WebView?>(null) }

        Box(Modifier.fillMaxSize()) {
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
                            if (android.os.Build.MANUFACTURER.equals("motorola", ignoreCase = true)) {
                                userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S921U; Build/UP1A.231005.007) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.363"
                            }
                        }

                        wv.webViewClient = object : WebViewClient() {
                            @Deprecated("Deprecated in Java")
                            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                                view.stopLoading()
                                if (url.endsWith("/app")) {
                                    view.loadUrl(JS_SNIPPET)
                                    view.visibility = View.GONE
                                }
                                return false
                            }
                        }

                        wv.webChromeClient = object : WebChromeClient() {
                            override fun onJsAlert(
                                view: WebView,
                                url: String,
                                message: String,
                                result: JsResult
                            ): Boolean {
                                if (message.isNotBlank()) {
                                    currentOnTokenReceived(message)
                                }
                                view.visibility = View.GONE
                                result.confirm()
                                return true
                            }
                        }

                        wv.loadUrl("https://discord.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { currentOnBack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 36.dp, start = 8.dp)
                    .background(
                        color = DiscordColors.Background.copy(alpha = 0.75f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DiscordColors.TextPrimary
                )
            }
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
            scale.animateTo(
                1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
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
          
