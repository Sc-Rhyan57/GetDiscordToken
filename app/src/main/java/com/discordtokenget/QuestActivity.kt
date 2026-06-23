package com.discordtokenget

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Trophy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID
import kotlin.math.sin

class QuestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra("token") ?: ""
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0F0F1A)
                ) {
                    QuestScreen(token = token, onBack = { finish() })
                }
            }
        }
    }
}

data class QuestData(
    val id: String,
    val name: String,
    val description: String,
    val rewardAmount: Int,
    val rewardType: String,
    val status: String,
    val iconUrl: String,
    val applicationId: String,
    val questType: String,
    val progress: Int = 0,
    val total: Int = 1,
    val isPremium: Boolean = false,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val isTargeted: Boolean = false,
    val expiresAt: String? = null,
    val enrollUrl: String? = null
)

data class TaskBarData(
    val id: String,
    val name: String,
    val iconUrl: String,
    val rewardAmount: Int,
    val progress: Int,
    val total: Int,
    val applicationId: String,
    val status: String,
    val ctaText: String,
    val backgroundColor: String = "#1E1E3F"
)

data class CaptchaChallenge(
    val sitekey: String,
    val rqdata: String,
    val rqtoken: String,
    val sessionId: String,
    val service: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestScreen(token: String, onBack: () -> Unit) {
    var quests by remember { mutableStateOf<List<QuestData>>(emptyList()) }
    var taskBars by remember { mutableStateOf<List<TaskBarData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var initialLoad by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshing by remember { mutableStateOf(false) }
    var showCaptchaDialog by remember { mutableStateOf(false) }
    var pendingCaptchaQuest by remember { mutableStateOf<QuestData?>(null) }
    var pendingCaptchaChallenge by remember { mutableStateOf<CaptchaChallenge?>(null) }
    var pendingMethod by remember { mutableStateOf("") }
    var showMethodDialog by remember { mutableStateOf(false) }
    var methodQuest by remember { mutableStateOf<QuestData?>(null) }
    val completingQuests = remember { mutableStateMapOf<String, Boolean>() }
    val snackbarHostState = remember { SnackbarHostState() }
    var totalOrbsEarned by remember { mutableIntStateOf(0) }
    var completedCount by remember { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var refreshRotation by remember { mutableFloatStateOf(0f) }

    fun loadQuests() {
        scope.launch {
            try {
                if (initialLoad) isLoading = true
                else refreshing = true
                errorMessage = null
                val result = withContext(Dispatchers.IO) { fetchQuests(token) }
                val newQuests = result.first
                val newTaskBars = result.second
                quests = newQuests
                taskBars = newTaskBars
                completedCount = newQuests.count { it.isCompleted || it.isClaimed }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Erro ao carregar missões"
            } finally {
                isLoading = false
                initialLoad = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(token) { loadQuests() }

    fun completeQuest(quest: QuestData, method: String) {
        if (completingQuests[quest.id] == true) return
        completingQuests[quest.id] = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    when (method) {
                        "jshook" -> completeQuestJsHook(token, quest)
                        else -> completeQuestNormal(token, quest)
                    }
                }
                when (result.first) {
                    "success" -> {
                        val updated = quests.map {
                            if (it.id == quest.id) it.copy(isCompleted = true, status = "COMPLETED")
                            else it
                        }
                        quests = updated
                        totalOrbsEarned += quest.rewardAmount
                        completedCount = updated.count { it.isCompleted || it.isClaimed }
                        snackbarHostState.showSnackbar("${quest.name} concluída! +${quest.rewardAmount} Orbs")
                    }
                    "captcha_required" -> {
                        pendingCaptchaQuest = quest
                        pendingCaptchaChallenge = result.second as? CaptchaChallenge
                        pendingMethod = method
                        showCaptchaDialog = true
                    }
                    "already_completed" -> {
                        val updated = quests.map {
                            if (it.id == quest.id) it.copy(isCompleted = true, isClaimed = true, status = "CLAIMED")
                            else it
                        }
                        quests = updated
                        snackbarHostState.showSnackbar("Já foi concluída anteriormente")
                    }
                    "error" -> {
                        snackbarHostState.showSnackbar(result.second.toString())
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Erro desconhecido")
            } finally {
                completingQuests[quest.id] = false
            }
        }
    }

    fun claimWithCaptcha(captchaKey: String) {
        val quest = pendingCaptchaQuest ?: return
        val challenge = pendingCaptchaChallenge ?: return
        val method = pendingMethod
        completingQuests[quest.id] = true
        showCaptchaDialog = false
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    claimRewardWithCaptcha(token, quest, challenge, captchaKey, method)
                }
                if (result) {
                    val updated = quests.map {
                        if (it.id == quest.id) it.copy(isCompleted = true, isClaimed = true, status = "CLAIMED")
                        else it
                    }
                    quests = updated
                    totalOrbsEarned += quest.rewardAmount
                    completedCount = updated.count { it.isCompleted || it.isClaimed }
                    snackbarHostState.showSnackbar("${quest.name} recompensa resgatada! +${quest.rewardAmount} Orbs")
                } else {
                    snackbarHostState.showSnackbar("Falha ao resgatar recompensa com captcha")
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(e.message ?: "Erro ao resgatar")
            } finally {
                completingQuests[quest.id] = false
                pendingCaptchaQuest = null
                pendingCaptchaChallenge = null
                pendingMethod = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Trophy,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Missões",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!refreshing && !isLoading) {
                                refreshRotation += 360f
                                loadQuests()
                            }
                        },
                        enabled = !refreshing && !isLoading
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            tint = if (refreshing || isLoading) Color.Gray else Color.White,
                            modifier = Modifier.rotate(refreshRotation)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF12121F))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading && initialLoad -> {
                    LoadingQuestsAnimation()
                }
                errorMessage != null && quests.isEmpty() && initialLoad -> {
                    ErrorView(message = errorMessage!!, onRetry = { loadQuests() })
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        if (taskBars.isNotEmpty()) {
                            item(key = "taskbars_header") {
                                AnimatedVisibility(
                                    visible = taskBars.isNotEmpty(),
                                    enter = fadeIn(tween(600)) + slideInVertically(
                                        tween(600, easing = FastOutSlowInEasing)
                                    ) { -it },
                                    exit = fadeOut(tween(300)) + slideOutVertically { -it }
                                ) {
                                    TaskBarsSection(taskBars = taskBars)
                                }
                            }
                            item(key = "taskbars_spacer") { Spacer(modifier = Modifier.height(14.dp)) }
                        }

                        item(key = "stats_bar") {
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(500, delayMillis = 100)) + slideInHorizontally(
                                    tween(500, delayMillis = 100, easing = FastOutSlowInEasing)
                                ) { it }
                            ) {
                                StatsBar(
                                    totalOrbs = totalOrbsEarned,
                                    questCount = quests.size,
                                    completedCount = completedCount
                                )
                            }
                        }
                        item(key = "stats_spacer") { Spacer(modifier = Modifier.height(10.dp)) }

                        item(key = "quests_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Missões disponíveis",
                                    color = Color(0xFFB0B0C0),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${quests.filter { it.status != "CLAIMED" }.size} restantes",
                                    color = Color(0xFF666680),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        val currentQuests by rememberUpdatedState(quests)
                        itemsIndexed(
                            items = currentQuests,
                            key = { _, q -> q.id }
                        ) { index, quest ->
                            val isCompleting = completingQuests[quest.id] == true
                            val enterOffset by remember {
                                derivedStateOf {
                                    (index * 40).coerceAtMost(600)
                                }
                            }
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(
                                    animationSpec = tween(
                                        delayMillis = enterOffset,
                                        durationMillis = 450,
                                        easing = FastOutSlowInEasing
                                    )
                                ) + slideInVertically(
                                    animationSpec = tween(
                                        delayMillis = enterOffset,
                                        durationMillis = 450,
                                        easing = FastOutSlowInEasing
                                    )
                                ) { it / 2 },
                                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                            ) {
                                QuestCard(
                                    quest = quest,
                                    isCompleting = isCompleting,
                                    onComplete = {
                                        methodQuest = quest
                                        showMethodDialog = true
                                    },
                                    onClaim = { completeQuest(quest, "normal") }
                                )
                            }
                            if (index < currentQuests.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        item(key = "bottom_spacer") { Spacer(modifier = Modifier.height(90.dp)) }
                    }

                    if (refreshing) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .height(2.dp),
                            color = Color(0xFF00D4AA),
                            trackColor = Color.Transparent,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }
        }
    }

    if (showMethodDialog && methodQuest != null) {
        MethodSelectionDialog(
            quest = methodQuest!!,
            isCompleting = completingQuests[methodQuest!!.id] == true,
            onDismiss = {
                showMethodDialog = false
                methodQuest = null
            },
            onNormal = {
                showMethodDialog = false
                val q = methodQuest
                methodQuest = null
                if (q != null) completeQuest(q, "normal")
            },
            onJsHook = {
                showMethodDialog = false
                val q = methodQuest
                methodQuest = null
                if (q != null) completeQuest(q, "jshook")
            }
        )
    }

    if (showCaptchaDialog && pendingCaptchaChallenge != null) {
        CaptchaSolveDialog(
            challenge = pendingCaptchaChallenge!!,
            onDismiss = {
                showCaptchaDialog = false
                pendingCaptchaQuest = null
                pendingCaptchaChallenge = null
                pendingMethod = ""
            },
            onSolved = { key -> claimWithCaptcha(key) }
        )
    }
}

@Composable
fun TaskBarsSection(taskBars: List<TaskBarData>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Recompensas",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "GANHE ORBS",
                color = Color(0xFF00D4AA),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .background(
                        color = Color(0x2200D4AA),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            itemsIndexed(taskBars, key = { _, t -> t.id }) { index, taskBar ->
                val infiniteTransition = rememberInfiniteTransition(label = "tb")
                val glowAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "glow"
                )
                val progress by animateFloatAsState(
                    targetValue = if (taskBar.total > 0) taskBar.progress.toFloat() / taskBar.total.toFloat() else 0f,
                    animationSpec = tween(800, delayMillis = index * 150, easing = FastOutSlowInEasing),
                    label = "tbprogress"
                )
                Card(
                    modifier = Modifier
                        .width(200.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF00D4AA).copy(alpha = glowAlpha * 0.3f),
                            spotColor = Color(0xFF00D4AA).copy(alpha = glowAlpha * 0.2f)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A30)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = taskBar.iconUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            1.dp,
                                            Color(0xFF2A2A45),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        taskBar.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "+${taskBar.rewardAmount} Orbs",
                                        color = Color(0xFF00D4AA),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0xFF2A2A45))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progress)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFF00D4AA),
                                                    Color(0xFF00B4D8)
                                                )
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${taskBar.progress}/${taskBar.total}",
                                    color = Color(0xFF888898),
                                    fontSize = 10.sp
                                )
                                Text(
                                    taskBar.ctaText,
                                    color = Color(0xFF6C63FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        if (taskBar.status == "COMPLETED" || taskBar.progress >= taskBar.total) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x55000000))
                                    .clip(RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00D4AA),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsBar(totalOrbs: Int, questCount: Int, completedCount: Int) {
    val animatedOrbs by animateIntAsState(
        targetValue = totalOrbs,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "orbs"
    )
    val animatedCompleted by animateIntAsState(
        targetValue = completedCount,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f),
        label = "completed"
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color(0xFF6C63FF).copy(alpha = 0.15f),
                spotColor = Color(0xFF6C63FF).copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16162A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Trophy,
                iconColor = Color(0xFFFFD700),
                label = "Orbs",
                value = "$animatedOrbs"
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFF2A2A45))
            )
            StatItem(
                icon = Icons.Default.PlayArrow,
                iconColor = Color(0xFF6C63FF),
                label = "Total",
                value = "$questCount"
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFF2A2A45))
            )
            StatItem(
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF00D4AA),
                label = "Concluídas",
                value = "$animatedCompleted"
            )
        }
    }
}

@Composable
fun StatItem(icon: ImageVector, iconColor: Color, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            color = Color(0xFF888898),
            fontSize = 10.sp
        )
    }
}

@Composable
fun QuestCard(
    quest: QuestData,
    isCompleting: Boolean,
    onComplete: () -> Unit,
    onClaim: () -> Unit
) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isCompleting) 1.02f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val borderGlow by animateFloatAsState(
        targetValue = if (isCompleting) 1f else 0f,
        animationSpec = tween(400),
        label = "borderglow"
    )
    val statusColor by animateColorAsState(
        targetValue = when {
            quest.isClaimed -> Color(0xFF00D4AA)
            quest.isCompleted -> Color(0xFF00B4D8)
            else -> Color(0xFF6C63FF)
        },
        animationSpec = tween(500),
        label = "statuscolor"
    )
    val progress by animateFloatAsState(
        targetValue = if (quest.total > 0) quest.progress.toFloat() / quest.total.toFloat() else 0f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "qprogress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pulseScale)
            .then(
                if (borderGlow > 0f) {
                    Modifier.border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF6C63FF).copy(alpha = borderGlow),
                                Color(0xFF00D4AA).copy(alpha = borderGlow)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16162A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = quest.iconUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF2A2A45), RoundedCornerShape(12.dp))
                        .shadow(4.dp, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            quest.name,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (quest.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "PREMIUM",
                                color = Color(0xFFFFD700),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier
                                    .background(
                                        Color(0x33FFD700),
                                        RoundedCornerShape(3.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        quest.description,
                        color = Color(0xFF888898),
                        fontSize = 11.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2A2A45))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(statusColor, statusColor.copy(alpha = 0.7f))
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (quest.total > 1) "${quest.progress}/${quest.total}" else when {
                        quest.isClaimed -> "Resgatado"
                        quest.isCompleted -> "Concluído"
                        else -> "Pendente"
                    },
                    color = if (quest.isClaimed || quest.isCompleted) statusColor else Color(0xFF888898),
                    fontSize = 10.sp,
                    fontWeight = if (quest.isClaimed || quest.isCompleted) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF222240), thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Trophy,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "+${quest.rewardAmount} ${quest.rewardType}",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                when {
                    quest.isClaimed -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1A00D4AA))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00D4AA),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Resgatado",
                                color = Color(0xFF00D4AA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    quest.isCompleted -> {
                        Button(
                            onClick = onClaim,
                            enabled = !isCompleting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00D4AA),
                                disabledContainerColor = Color(0xFF00D4AA).copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            if (isCompleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                "Resgatar",
                                color = Color(0xFF0F0F1A),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    else -> {
                        Button(
                            onClick = onComplete,
                            enabled = !isCompleting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6C63FF),
                                disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            if (isCompleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                "Complete task",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MethodSelectionDialog(
    quest: QuestData,
    isCompleting: Boolean,
    onDismiss: () -> Unit,
    onNormal: () -> Unit,
    onJsHook: () -> Unit
) {
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    val infiniteTransition = rememberInfiniteTransition(label = "methodglow")
    val glowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowoffset"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF6C63FF).copy(alpha = 0.2f),
                    spotColor = Color(0xFF6C63FF).copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A30))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    "Selecionar método",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    quest.name,
                    color = Color(0xFF888898),
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(16.dp))
                MethodOption(
                    icon = Icons.Default.PlayArrow,
                    title = "Método Normal",
                    description = "Completa a missão diretamente pela API",
                    color = Color(0xFF6C63FF),
                    isSelected = selectedMethod == "normal",
                    glowOffset = glowOffset,
                    enabled = !isCompleting,
                    onClick = { selectedMethod = "normal" }
                )
                Spacer(modifier = Modifier.height(10.dp))
                MethodOption(
                    icon = Icons.Default.Code,
                    title = "Método JS Hook",
                    description = "Simula interação JavaScript com o aplicativo",
                    color = Color(0xFF00D4AA),
                    isSelected = selectedMethod == "jshook",
                    glowOffset = glowOffset,
                    enabled = !isCompleting,
                    onClick = { selectedMethod = "jshook" }
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isCompleting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(0xFF3A3A55)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFB0B0C0)
                        )
                    ) {
                        Text("Cancelar", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = {
                            when (selectedMethod) {
                                "normal" -> onNormal()
                                "jshook" -> onJsHook()
                            }
                        },
                        enabled = selectedMethod != null && !isCompleting,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedMethod) {
                                "jshook" -> Color(0xFF00D4AA)
                                else -> Color(0xFF6C63FF)
                            },
                            disabledContainerColor = Color(0xFF3A3A55)
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (isCompleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Confirmar",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MethodOption(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    isSelected: Boolean,
    glowOffset: Float,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val selectedScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "selScale"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else Color(0xFF2A2A45),
        animationSpec = tween(300),
        label = "selBorder"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.15f else 0f,
        animationSpec = tween(300),
        label = "selBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(selectedScale)
            .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = bgAlpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = if (isSelected) 0.2f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(description, color = Color(0xFF888898), fontSize = 11.sp, lineHeight = 14.sp)
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CaptchaSolveDialog(
    challenge: CaptchaChallenge,
    onDismiss: () -> Unit,
    onSolved: (String) -> Unit
) {
    var solvingState by remember { mutableStateOf("loading") }
    var solvedToken by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = solvingState != "solving",
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(380.dp)
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF00D4AA).copy(alpha = 0.15f),
                    spotColor = Color(0xFF00D4AA).copy(alpha = 0.1f)
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF12121F))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color(0xFFFFD700),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Verificação necessária",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Resolva o captcha para continuar",
                        color = Color(0xFF888898),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A30))
                    ) {
                        if (solvingState == "loading") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = Color(0xFF6C63FF))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Carregando captcha...", color = Color(0xFF888898), fontSize = 12.sp)
                                }
                            }
                        } else if (solvingState == "solving") {
                            AndroidView(
                                factory = { context ->
                                    WebView(context).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.loadWithOverviewMode = true
                                        settings.useWideViewPort = true
                                        webViewClient = WebViewClient()
                                        webChromeClient = WebChromeClient()
                                        addJavascriptInterface(object {
                                            @JavascriptInterface
                                            fun onCaptchaSolved(token: String) {
                                                solvedToken = token
                                                solvingState = "solved"
                                            }
                                            @JavascriptInterface
                                            fun onCaptchaError(error: String) {
                                                solvingState = "error"
                                            }
                                        }, "AndroidBridge")
                                        val html = buildCaptchaHtml(challenge.sitekey, challenge.rqdata)
                                        loadDataWithBaseURL(
                                            "https://discord.com",
                                            html,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (solvingState == "solved" && solvedToken != null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val checkScale by animateFloatAsState(
                                        targetValue = 1f,
                                        animationSpec = spring(dampingRatio = 0.3f, stiffness = 200f),
                                        label = "check"
                                    )
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF00D4AA),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .scale(checkScale)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Captcha resolvido!", color = Color(0xFF00D4AA), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                                LaunchedEffect(solvedToken) {
                                    delay(800)
                                    onSolved(solvedToken!!)
                                }
                            }
                        } else if (solvingState == "error") {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.ErrorOutline,
                                        contentDescription = null,
                                        tint = Color(0xFFFF4444),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Erro ao carregar captcha", color = Color(0xFFFF4444), fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { solvingState = "loading" },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6C63FF))
                                    ) {
                                        Text("Tentar novamente", color = Color(0xFF6C63FF), fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                if (solvingState == "solving") {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Text("Cancelar", color = Color(0xFF888898), fontSize = 12.sp)
                    }
                }
            }
        }
    }

    LaunchedEffect(solvingState) {
        if (solvingState == "loading") {
            delay(500)
            solvingState = "solving"
        }
    }
}

fun buildCaptchaHtml(sitekey: String, rqdata: String): String {
    val encodedRqdata = rqdata.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { 
                background: #1a1a30; 
                display: flex; 
                justify-content: center; 
                align-items: center; 
                min-height: 100vh;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            }
            .container { text-align: center; padding: 20px; }
            .hcaptcha-container { 
                display: flex; 
                justify-content: center; 
                margin-top: 16px;
                transform: scale(1.1);
            }
            .loading-text { color: #888898; font-size: 13px; margin-bottom: 12px; }
        </style>
    </head>
    <body>
        <div class="container">
            <div class="loading-text">Resolva o captcha abaixo</div>
            <div class="hcaptcha-container" id="hcaptcha-container"></div>
        </div>
        <script src="https://js.hcaptcha.com/1/api.js?render=explicit&hl=pt-BR" async defer></script>
        <script>
            function renderCaptcha() {
                if (typeof hcaptcha !== 'undefined') {
                    try {
                        hcaptcha.render('hcaptcha-container', {
                            sitekey: '$sitekey',
                            rqdata: '$encodedRqdata',
                            theme: 'dark',
                            size: 'normal',
                            callback: function(token) {
                                try { AndroidBridge.onCaptchaSolved(token); } catch(e) {}
                            },
                            'error-callback': function(error) {
                                try { AndroidBridge.onCaptchaError(JSON.stringify(error)); } catch(e) {}
                            },
                            'expired-callback': function() {
                                try { AndroidBridge.onCaptchaError('expired'); } catch(e) {}
                            }
                        });
                    } catch(e) {
                        try { AndroidBridge.onCaptchaError(e.message); } catch(ex) {}
                    }
                } else {
                    setTimeout(renderCaptcha, 200);
                }
            }
            window.onload = function() { setTimeout(renderCaptcha, 300); };
        </script>
    </body>
    </html>
    """.trimIndent()
}

@Composable
fun LoadingQuestsAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val rotation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "loadrot"
            )
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .rotate(rotation)
                    .drawBehind {
                        val brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF6C63FF),
                                Color(0xFF00D4AA),
                                Color(0xFF6C63FF).copy(alpha = 0.1f)
                            )
                        )
                        drawArc(
                            brush = brush,
                            startAngle = 0f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Carregando missões...",
                color = Color(0xFF888898),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val bounce by rememberInfiniteTransition().animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bounce"
            )
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFFF4444),
                modifier = Modifier
                    .size(56.dp)
                    .offset(y = bounce.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                message,
                color = Color(0xFFB0B0C0),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tentar novamente", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun AndroidView(factory: (android.content.Context) -> WebView, modifier: Modifier = Modifier) {
    androidx.compose.ui.viewinterop.AndroidView(factory = factory, modifier = modifier)
}

private fun rememberUpdatedState(state: List<QuestData>) = androidx.compose.runtime.rememberUpdatedState(state)

suspend fun fetchQuests(token: String): Pair<List<QuestData>, List<TaskBarData>> {
    val conn = URL("https://discord.com/api/v9/quests?with_task_bars=true").openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty("Authorization", token)
    conn.setRequestProperty("X-Super-Properties", generateSuperProperties())
    conn.setRequestProperty("X-Discord-Locale", "pt-BR")
    conn.setRequestProperty("X-Discord-Timezone", "America/Sao_Paulo")
    conn.connectTimeout = 15000
    conn.readTimeout = 15000
    try {
        val responseCode = conn.responseCode
        if (responseCode != 200) {
            val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: ""
            throw Exception("Erro ${responseCode}: ${extractErrorMessage(errorBody)}")
        }
        val body = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(body)
        val questArray = json.optJSONArray("quests") ?: JSONArray()
        val taskBarArray = json.optJSONArray("task_bars") ?: JSONArray()

        val questsList = mutableListOf<QuestData>()
        for (i in 0 until questArray.length()) {
            val q = questArray.getJSONObject(i)
            val userStatus = q.optJSONObject("user_status") ?: JSONObject()
            val questState = userStatus.optString("quest_state", "NOT_STARTED")
            val isCompleted = questState == "COMPLETED" || questState == "CAN_CLAIM"
            val isClaimed = questState == "CLAIMED"
            val claimsObj = userStatus.optJSONObject("claims") ?: JSONObject()
            val progress = claimsObj.optInt("count", if (isCompleted) 1 else 0)
            val total = claimsObj.optInt("goal", 1)
            val rewards = q.optJSONArray("rewards") ?: JSONArray()
            var rewardAmount = 0
            var rewardType = "Orbs"
            if (rewards.length() > 0) {
                val reward = rewards.getJSONObject(0)
                rewardAmount = reward.optInt("amount", 0)
                val rewardDesc = reward.optString("description", "")
                rewardType = if (rewardDesc.contains("Orb", true)) "Orbs" else rewardDesc
            }
            val config = q.optJSONObject("config") ?: JSONObject()
            val appIcon = config.optString("app_icon", "")
            val appName = config.optString("app_name", q.optString("name", "Missão"))
            val appDesc = config.optString("app_description", q.optString("description", ""))
            val isPremium = q.optBoolean("is_premium", false)
            val isTargeted = q.optBoolean("is_targeted", false)
            val expiresAt = q.optString("expires_at", null)
            val applicationId = config.optString("application_id", "")
            val enrollUrl = config.optString("enroll_url", null)
            val questType = config.optString("type", "APP_DOWNLOAD")

            questsList.add(
                QuestData(
                    id = q.getString("id"),
                    name = appName,
                    description = appDesc,
                    rewardAmount = rewardAmount,
                    rewardType = rewardType,
                    status = questState,
                    iconUrl = appIcon,
                    applicationId = applicationId,
                    questType = questType,
                    progress = progress,
                    total = total,
                    isPremium = isPremium,
                    isCompleted = isCompleted,
                    isClaimed = isClaimed,
                    isTargeted = isTargeted,
                    expiresAt = if (expiresAt == "null" || expiresAt.isEmpty()) null else expiresAt,
                    enrollUrl = if (enrollUrl == "null" || enrollUrl.isEmpty()) null else enrollUrl
                )
            )
        }

        val taskBarsList = mutableListOf<TaskBarData>()
        for (i in 0 until taskBarArray.length()) {
            val tb = taskBarArray.getJSONObject(i)
            val config = tb.optJSONObject("config") ?: JSONObject()
            val claimsObj = tb.optJSONObject("claims") ?: JSONObject()
            val userStatus = tb.optJSONObject("user_status") ?: JSONObject()
            val questState = userStatus.optString("quest_state", "NOT_STARTED")
            taskBarsList.add(
                TaskBarData(
                    id = tb.getString("id"),
                    name = config.optString("app_name", tb.optString("name", "")),
                    iconUrl = config.optString("app_icon", ""),
                    rewardAmount = (tb.optJSONArray("rewards")?.optJSONObject(0)?.optInt("amount", 0) ?: 0),
                    progress = claimsObj.optInt("count", 0),
                    total = claimsObj.optInt("goal", 1),
                    applicationId = config.optString("application_id", ""),
                    status = questState,
                    ctaText = when (questState) {
                        "CAN_CLAIM" -> "Resgatar"
                        "COMPLETED" -> "Concluída"
                        "CLAIMED" -> "Resgatado"
                        else -> "Começar agora"
                    },
                    backgroundColor = config.optString("background_color", "#1E1E3F")
                )
            )
        }

        return Pair(questsList, taskBarsList)
    } finally {
        conn.disconnect()
    }
}

suspend fun completeQuestNormal(token: String, quest: QuestData): Pair<String, Any?> {
    return attemptClaimReward(token, quest, null, null)
}

suspend fun completeQuestJsHook(token: String, quest: QuestData): Pair<String, Any?> {
    val enrollUrl = quest.enrollUrl
    if (!enrollUrl.isNullOrEmpty()) {
        try {
            val enrollConn = URL(enrollUrl).openConnection() as HttpURLConnection
            enrollConn.requestMethod = "POST"
            enrollConn.setRequestProperty("Authorization", token)
            enrollConn.setRequestProperty("X-Super-Properties", generateSuperProperties())
            enrollConn.setRequestProperty("Content-Type", "application/json")
            enrollConn.setRequestProperty("X-Discord-Locale", "pt-BR")
            enrollConn.setRequestProperty("X-Discord-Timezone", "America/Sao_Paulo")
            enrollConn.connectTimeout = 10000
            enrollConn.readTimeout = 10000
            enrollConn.outputStream.write("{}".toByteArray())
            enrollConn.responseCode
            enrollConn.disconnect()
            delay(2000)
        } catch (_: Exception) {
            delay(1500)
        }
    }
    val acceptConn = URL("https://discord.com/api/v9/quests/${quest.id}/accept").openConnection() as HttpURLConnection
    acceptConn.requestMethod = "PUT"
    acceptConn.setRequestProperty("Authorization", token)
    acceptConn.setRequestProperty("X-Super-Properties", generateSuperProperties())
    acceptConn.setRequestProperty("Content-Type", "application/json")
    acceptConn.setRequestProperty("X-Discord-Locale", "pt-BR")
    acceptConn.setRequestProperty("X-Discord-Timezone", "America/Sao_Paulo")
    acceptConn.connectTimeout = 10000
    acceptConn.readTimeout = 10000
    try {
        acceptConn.outputStream.write("{}".toByteArray())
        acceptConn.responseCode
    } catch (_: Exception) {}
    acceptConn.disconnect()
    delay(3000)
    return attemptClaimReward(token, quest, null, null)
}

suspend fun attemptClaimReward(
    token: String,
    quest: QuestData,
    captchaKey: String?,
    challenge: CaptchaChallenge?
): Pair<String, Any?> {
    val trafficMetadata = generateTrafficMetadata()
    val body = JSONObject().apply {
        put("platform", 0)
        put("location", 11)
        put("is_targeted", quest.isTargeted)
        put("metadata_sealed", JSONObject.NULL)
        put("traffic_metadata_sealed", trafficMetadata)
    }
    val conn = URL("https://discord.com/api/v9/quests/${quest.id}/claim-reward").openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Authorization", token)
    conn.setRequestProperty("X-Super-Properties", generateSuperProperties())
    conn.setRequestProperty("Content-Type", "application/json")
    conn.setRequestProperty("X-Discord-Locale", "pt-BR")
    conn.setRequestProperty("X-Discord-Timezone", "America/Sao_Paulo")
    conn.setRequestProperty("X-Debug-Options", "bugReporterEnabled")
    if (captchaKey != null && challenge != null) {
        conn.setRequestProperty("X-Captcha-Key", captchaKey)
        conn.setRequestProperty("X-Captcha-Rqtoken", challenge.rqtoken)
        conn.setRequestProperty("X-Captcha-Session-Id", challenge.sessionId)
    }
    conn.connectTimeout = 15000
    conn.readTimeout = 15000
    conn.doOutput = true
    try {
        val writer = OutputStreamWriter(conn.outputStream)
        writer.write(body.toString())
        writer.flush()
        writer.close()
        val responseCode = conn.responseCode
        val responseBody = if (responseCode >= 400) {
            conn.errorStream?.bufferedReader()?.readText() ?: ""
        } else {
            conn.inputStream?.bufferedReader()?.readText() ?: ""
        }
        when (responseCode) {
            200 -> {
                val respJson = JSONObject(responseBody)
                val errors = respJson.optJSONArray("errors")
                if (errors != null && errors.length() > 0) {
                    return Pair("error", errors.getJSONObject(0).optString("message", "Erro desconhecido"))
                }
                return Pair("success", null)
            }
            400 -> {
                val respJson = JSONObject(responseBody)
                val captchaKeyArr = respJson.optJSONArray("captcha_key")
                if (captchaKeyArr != null) {
                    val captchaError = captchaKeyArr.optString(0, "")
                    if (captchaError == "captcha-required") {
                        val sitekey = respJson.optString("captcha_sitekey", "")
                        val rqdata = respJson.optString("captcha_rqdata", "")
                        val rqtoken = respJson.optString("captcha_rqtoken", "")
                        val sessionId = respJson.optString("captcha_session_id", "")
                        val service = respJson.optString("captcha_service", "hcaptcha")
                        if (sitekey.isNotEmpty() && rqtoken.isNotEmpty()) {
                            return Pair(
                                "captcha_required",
                                CaptchaChallenge(
                                    sitekey = sitekey,
                                    rqdata = rqdata,
                                    rqtoken = rqtoken,
                                    sessionId = sessionId,
                                    service = service
                                )
                            )
                        }
                    }
                    return Pair("error", "Captcha necessário mas dados inválidos")
                }
                val message = respJson.optString("message", "Erro 400")
                return if (message.contains("already", true) || message.contains("já", true)) {
                    Pair("already_completed", null)
                } else {
                    Pair("error", message)
                }
            }
            401 -> return Pair("error", "Token inválido ou expirado")
            403 -> return Pair("error", "Acesso negado")
            404 -> return Pair("error", "Missão não encontrada")
            429 -> {
                val retryAfter = conn.getHeaderField("Retry-After")?.toIntOrNull() ?: 5
                delay((retryAfter * 1000L).coerceAtMost(30000L))
                return attemptClaimReward(token, quest, captchaKey, challenge)
            }
            else -> return Pair("error", "Erro $responseCode: ${extractErrorMessage(responseBody)}")
        }
    } finally {
        conn.disconnect()
    }
}

suspend fun claimRewardWithCaptcha(
    token: String,
    quest: QuestData,
    challenge: CaptchaChallenge,
    captchaKey: String,
    method: String
): Boolean {
    return try {
        val result = attemptClaimReward(token, quest, captchaKey, challenge)
        result.first == "success"
    } catch (e: Exception) {
        false
    }
}

fun generateSuperProperties(): String {
    val props = JSONObject().apply {
        put("os", "Android")
        put("browser", "Android Mobile")
        put("device", "Android")
        put("system_locale", "pt-BR")
        put("has_client_mods", false)
        put("browser_user_agent", "Mozilla/5.0 (Linux; Android 16) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/152.0.0.0 Mobile Safari/537.36")
        put("browser_version", "152.0")
        put("os_version", "16")
        put("referrer", "")
        put("referring_domain", "")
        put("referring_domain_current", "")
        put("release_channel", "stable")
        put("client_build_number", 565311)
        put("client_event_source", JSONObject.NULL)
        put("client_launch_id", UUID.randomUUID().toString())
        put("launch_signature", UUID.randomUUID().toString())
        put("client_heartbeat_session_id", UUID.randomUUID().toString())
        put("client_app_state", "focused")
    }
    return android.util.Base64.encodeToString(
        props.toString().toByteArray(Charsets.UTF_8),
        android.util.Base64.NO_WRAP
    ).replace("\n", "")
}

fun generateTrafficMetadata(): String {
    val fingerprint = UUID.randomUUID().toString().replace("-", "").take(8)
    val payloadBytes = ByteArray(64) { (Math.random() * 256).toInt().toByte() }
    val payload = android.util.Base64.encodeToString(payloadBytes, android.util.Base64.NO_WRAP).replace("\n", "")
    val obj = JSONObject().apply {
        put("key_fingerprint", fingerprint)
        put("payload", payload)
    }
    return android.util.Base64.encodeToString(
        obj.toString().toByteArray(Charsets.UTF_8),
        android.util.Base64.NO_WRAP
    ).replace("\n", "")
}

fun extractErrorMessage(body: String): String {
    return try {
        val json = JSONObject(body)
        json.optString("message", body.take(200))
    } catch (_: Exception) {
        body.take(200)
    }
}
