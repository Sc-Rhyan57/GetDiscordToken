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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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

private const val JS_TOKEN =
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
            val screen = when {
                showWebView && token == null -> "webview"
                token == null -> "login"
                else -> "profile"
            }
            AnimatedContent(
                targetState = screen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
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
                modifier = Modifier.size(100.dp).scale(scale.value),
                tint = DiscordColors.Blurple
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Discord Token",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = DiscordColors.TextPrimary,
                modifier = Modifier.scale(scale.value)
            )

            Text(
                text = "EXTRACTOR",
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
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
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
                        text = "Get Your Token",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Securely extract your Discord access token",
                        fontSize = 14.sp,
                        color = DiscordColors.TextMuted,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
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
                                text = "Login with Discord",
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
                            text = "Secure & Private",
                            fontSize = 12.sp,
                            color = DiscordColors.Green,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            CreditsFooter()
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
                        if (Build.MANUFACTURER.equals("motorola", ignoreCase = true)) {
                            wv.settings.userAgentString =
                                "Mozilla/5.0 (Linux; Android 14; SM-S921U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Mobile Safari/537.36"
                        }

                        wv.webViewClient = object : WebViewClient() {
                            @Deprecated("Deprecated in Java")
                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                url: String
                            ): Boolean {
                                view.stopLoading()
                                if (url.endsWith("/app")) {
                                    view.loadUrl(JS_TOKEN)
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
                                result.confirm()
                                view.visibility = View.GONE
                                if (message.isNotBlank()) {
                                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                        currentOnTokenReceived(message)
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = DiscordColors.TextPrimary
                )
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                webViewRef.value?.apply {
                    stopLoading()
                    loadUrl("about:blank")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
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
                modifier = Modifier.fillMaxWidth().height(140.dp)
            ) {
                if (user?.banner != null) {
                    AsyncImage(
                        model = "https://cdn.discordapp.com/banners/" + user.id + "/" + user.banner + ".png?size=600",
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
                    modifier = Modifier.fillMaxWidth().offset(y = (-50).dp),
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
                                    model = "https://cdn.discordapp.com/avatars/" + user.id + "/" + user.avatar + ".png?size=256",
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
                        Text(text = "Logout", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = DiscordColors.Blurple,
                            strokeWidth = 3.dp
                        )
                    }
                } else if (user != null) {
                    val displayName = user.displayName ?: user.username
                    Text(
                        text = displayName,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = DiscordColors.TextPrimary
                    )

                    val handleText = "@" + user.username
                    Text(
                        text = handleText,
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
                                    text = user.bio,
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
                                    text = "Access Token",
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
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val tokenDisplay = if (showToken) token else "".padEnd(60, '\u2022')
                                    Text(
                                        text = tokenDisplay,
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
                                        val icon = if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                        val desc = if (showToken) "Hide" else "Show"
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = desc,
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
                                modifier = Modifier.fillMaxWidth().height(52.dp),
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
                                Text(
                                    text = "Copy to Clipboard",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/Sc-rhyan57/GetDiscordToken")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
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
                        Text(
                            text = "Sc-rhyan57/GetDiscordToken",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(Modifier.height(40.dp))
                CreditsFooter()
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
                modifier = Modifier.fillMaxWidth().padding(18.dp),
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
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        color = DiscordColors.TextMuted,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 15.sp,
                        color = DiscordColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    @Composable
    fun CreditsFooter() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                DiscordColors.Blurple.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = DiscordColors.Blurple.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Made by rhyan57",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DiscordColors.TextMuted
                    )
                }
            }
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

        if (!response.isSuccessful) throw IOException("Request failed: " + response.code)

        val segments = body.split(",")

        fun extract(key: String): String? {
            return segments.find { it.contains("\"" + key + "\"") }
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
