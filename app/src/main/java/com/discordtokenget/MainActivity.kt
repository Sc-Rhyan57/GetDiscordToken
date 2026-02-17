package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
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
    val Background = Color(0xFF23272A)
    val Secondary = Color(0xFF2C2F33)
    val Tertiary = Color(0xFF36393F)
    val Blurple = Color(0xFF5865F2)
    val Green = Color(0xFF3BA55D)
    val TextPrimary = Color(0xFFDCDDDE)
    val TextSecondary = Color(0xFFB9BBBE)
    val TextMuted = Color(0xFF72767D)
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Column(Modifier.fillMaxSize()) {
                if (showWebView && token == null) {
                    WebViewScreen(
                        onTokenReceived = { 
                            token = it
                            showWebView = false
                        },
                        onBack = { showWebView = false }
                    )
                } else if (token == null) {
                    LoginScreen(onLoginClick = { showWebView = true })
                } else {
                    UserProfileScreen(
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = DiscordColors.Blurple
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Discord",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = DiscordColors.Blurple
            )
            
            Text(
                "TOKEN GET",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DiscordColors.TextSecondary,
                letterSpacing = 4.sp
            )

            Spacer(Modifier.height(60.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Rocket,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = DiscordColors.Blurple
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        "Get Your Discord Token",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "Login with your Discord account to retrieve your token",
                        fontSize = 13.sp,
                        color = DiscordColors.TextMuted,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(Modifier.height(32.dp))
                    
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.Blurple),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Gamepad,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Log in with Discord",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            
            RainbowText()
        }
    }

    @Composable
    fun WebViewScreen(onTokenReceived: (String) -> Unit, onBack: () -> Unit) {
        var isLoading by remember { mutableStateOf(true) }
        
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DiscordColors.Secondary)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
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
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = DiscordColors.Blurple,
                        strokeWidth = 2.dp
                    )
                }
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }
                            
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                
                                if (url?.contains("discord.com") == true) {
                                    view?.evaluateJavascript("""
                                        (function() {
                                            try {
                                                let token = null;
                                                
                                                const iframe = document.createElement('iframe');
                                                document.body.appendChild(iframe);
                                                const ls = iframe.contentWindow.localStorage;
                                                const tkn = ls.getItem('token');
                                                if (tkn) {
                                                    token = JSON.parse(tkn);
                                                    document.body.removeChild(iframe);
                                                    return token;
                                                }
                                                document.body.removeChild(iframe);
                                                
                                                if (typeof webpackChunkdiscord_app !== 'undefined') {
                                                    webpackChunkdiscord_app.push([[Symbol()], {}, o => {
                                                        for (let e of Object.values(o.c)) {
                                                            try {
                                                                if (!e.exports || e.exports === window) continue;
                                                                if (e.exports?.getToken) {
                                                                    token = e.exports.getToken();
                                                                }
                                                                for (let prop in e.exports) {
                                                                    if (e.exports?.[prop]?.getToken && 
                                                                        "IntlMessagesProxy" !== e.exports[prop][Symbol.toStringTag]) {
                                                                        token = e.exports[prop].getToken();
                                                                    }
                                                                }
                                                            } catch {}
                                                        }
                                                    }]);
                                                    webpackChunkdiscord_app.pop();
                                                }
                                                
                                                return token;
                                            } catch (e) {
                                                return null;
                                            }
                                        })();
                                    """.trimIndent()) { result ->
                                        if (result != null && result != "null" && result != "\"null\"" && result.length > 10) {
                                            val cleanToken = result.replace("\"", "").trim()
                                            if (cleanToken.isNotEmpty() && cleanToken != "null") {
                                                onTokenReceived(cleanToken)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        webChromeClient = WebChromeClient()
                        
                        loadUrl("https://discord.com/login")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
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
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (user?.banner != null) {
                AsyncImage(
                    model = "https://cdn.discordapp.com/banners/${user.id}/${user.banner}.png?size=600",
                    contentDescription = "Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(DiscordColors.Blurple)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    if (user?.avatar != null) {
                        AsyncImage(
                            model = "https://cdn.discordapp.com/avatars/${user.id}/${user.avatar}.png?size=128",
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .offset(y = (-40).dp)
                                .clip(CircleShape)
                                .border(6.dp, DiscordColors.Background, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .offset(y = (-40).dp)
                                .clip(CircleShape)
                                .background(DiscordColors.Blurple)
                                .border(6.dp, DiscordColors.Background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onLogout,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFED4245)),
                        border = BorderStroke(1.dp, Color(0xFFED4245)),
                        shape = RoundedCornerShape(8.dp)
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
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.CenterHorizontally),
                        color = DiscordColors.Blurple
                    )
                } else if (user != null) {
                    Text(
                        user.displayName ?: user.username,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = DiscordColors.TextPrimary
                    )

                    Text(
                        "@${user.username}",
                        fontSize = 16.sp,
                        color = DiscordColors.TextMuted
                    )

                    if (!user.bio.isNullOrBlank()) {
                        Spacer(Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                user.bio,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 14.sp,
                                color = DiscordColors.TextSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    InfoCard(title = "User ID", value = user.id)

                    Spacer(Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = DiscordColors.Blurple
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    "Access Token",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DiscordColors.TextPrimary
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DiscordColors.Tertiary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (showToken) token else "*".repeat(60),
                                        modifier = Modifier.weight(1f),
                                        fontSize = 12.sp,
                                        color = DiscordColors.TextSecondary,
                                        maxLines = if (showToken) Int.MAX_VALUE else 1
                                    )
                                    IconButton(
                                        onClick = onToggleToken,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (showToken) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showToken) "Hide" else "Show",
                                            tint = DiscordColors.TextMuted
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Discord Token", token))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.Green),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Copy to Clipboard", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Sc-rhyan57/GetDiscordToken"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DiscordColors.TextPrimary),
                        border = BorderStroke(1.dp, DiscordColors.Tertiary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Sc-rhyan57/GetDiscordToken", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Spacer(Modifier.height(32.dp))
                
                RainbowText()
            }
        }
    }

    @Composable
    fun InfoCard(title: String, value: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DiscordColors.Secondary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Badge,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = DiscordColors.Blurple
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontSize = 12.sp, color = DiscordColors.TextMuted, fontWeight = FontWeight.Medium)
                    Text(value, fontSize = 14.sp, color = DiscordColors.TextPrimary, fontWeight = FontWeight.Bold)
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
            Color(0xFFFF0000),
            Color(0xFFFF7F00),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF0000FF),
            Color(0xFF4B0082),
            Color(0xFF9400D3),
            Color(0xFFFF0000)
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
                tint = Color.Red
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
        val body = response.body?.string() ?: throw IOException("Empty response")
        
        if (!response.isSuccessful) {
            throw IOException("Failed: ${response.code}")
        }

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
