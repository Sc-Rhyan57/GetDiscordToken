package com.discordtokenget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

private val questHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

private val QuestColors = object {
    val Background  = Color(0xFF1E1F22)
    val Surface     = Color(0xFF2B2D31)
    val SurfaceVar  = Color(0xFF313338)
    val Primary     = Color(0xFF5865F2)
    val Gold        = Color(0xFFFFD700)
    val Success     = Color(0xFF23A55A)
    val Warning     = Color(0xFFFAA61A)
    val Error       = Color(0xFFED4245)
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFB5BAC1)
    val TextMuted   = Color(0xFF80848E)
    val Divider     = Color(0xFF3F4147)
    val Orbs        = Color(0xFF7C43E0)
    val Decor       = Color(0xFF57F287)
}

data class QuestItem(
    val id: String,
    val name: String,
    val reward: String,
    val rewardType: String,
    val expiresAt: String,
    val taskName: String,
    val secondsNeeded: Long,
    val secondsDone: Long,
    val applicationId: String?,
    val applicationName: String?
)

enum class QuestStatus { IDLE, RUNNING, DONE, ERROR }

data class QuestState(
    val quest: QuestItem,
    var status: QuestStatus = QuestStatus.IDLE,
    var progress: Long = 0L,
    var log: String = ""
)

class QuestActivity : ComponentActivity() {

    companion object {
        private const val EXTRA_TOKEN = "extra_token"

        fun start(context: Context, token: String) {
            context.startActivity(Intent(context, QuestActivity::class.java).apply {
                putExtra(EXTRA_TOKEN, token)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = QuestColors.Primary,
                background = QuestColors.Background,
                surface = QuestColors.Surface
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = QuestColors.Background) {
                    QuestScreen(token = token, onBack = { finish() })
                }
            }
        }
    }

    @Composable
    fun QuestScreen(token: String, onBack: () -> Unit) {
        var loading by remember { mutableStateOf(false) }
        var fetchError by remember { mutableStateOf<String?>(null) }
        val questStates = remember { mutableStateListOf<QuestState>() }
        var refreshKey by remember { mutableStateOf(0) }

        LaunchedEffect(refreshKey) {
            loading = true
            fetchError = null
            questStates.clear()
            try {
                val quests = fetchQuests(token)
                questStates.addAll(quests.map { QuestState(it) })
            } catch (e: Exception) {
                fetchError = e.message ?: "Unknown error"
            }
            loading = false
        }

        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QuestColors.Surface)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = QuestColors.TextPrimary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Outlined.Star, null, tint = QuestColors.Gold, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Auto Quest Completer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = QuestColors.TextPrimary)
                }
                IconButton(onClick = { refreshKey++ }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Refresh, null, tint = QuestColors.Primary, modifier = Modifier.size(20.dp))
                }
            }

            HorizontalDivider(color = QuestColors.Divider)

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = QuestColors.Gold, strokeWidth = 3.dp, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Fetching quests...", color = QuestColors.TextMuted, fontSize = 14.sp)
                    }
                }
            } else if (fetchError != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.Warning, null, tint = QuestColors.Error, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Failed to load quests", color = QuestColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(fetchError ?: "", color = QuestColors.TextMuted, fontSize = 13.sp)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { refreshKey++ }, colors = ButtonDefaults.buttonColors(containerColor = QuestColors.Primary), shape = RoundedCornerShape(12.dp)) {
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (questStates.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Icon(Icons.Outlined.CheckCircle, null, tint = QuestColors.Success, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No active quests!", color = QuestColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("You have no incomplete quests right now.", color = QuestColors.TextMuted, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(questStates, key = { it.quest.id }) { state ->
                        QuestCard(
                            state = state,
                            token = token,
                            onStateChange = { idx ->
                                val i = questStates.indexOfFirst { it.quest.id == state.quest.id }
                                if (i >= 0) questStates[i] = idx
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    @Composable
    fun QuestCard(
        state: QuestState,
        token: String,
        onStateChange: (QuestState) -> Unit
    ) {
        val rewardColor = when (state.quest.rewardType) {
            "orbs"  -> QuestColors.Orbs
            "decor" -> QuestColors.Decor
            else    -> QuestColors.Primary
        }

        val shimmerT = rememberInfiniteTransition(label = "shimmer")
        val shimmerAlpha by shimmerT.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "sa")

        Card(
            colors = CardDefaults.cardColors(containerColor = QuestColors.Surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, rewardColor.copy(0.25f), RoundedCornerShape(16.dp))
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp)
                            .background(rewardColor.copy(0.15f), CircleShape)
                            .border(1.dp, rewardColor.copy(0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (state.quest.rewardType == "orbs") Icons.Outlined.AutoAwesome else Icons.Outlined.Star,
                            null, tint = rewardColor, modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(state.quest.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = QuestColors.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(state.quest.reward, fontSize = 12.sp, color = rewardColor, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        Modifier.background(rewardColor.copy(0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, rewardColor.copy(0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(state.quest.rewardType.uppercase(), fontSize = 9.sp, color = rewardColor, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoChip("Task", state.quest.taskName.replace("_", " "), QuestColors.TextMuted)
                    InfoChip("Expires", state.quest.expiresAt, QuestColors.Warning)
                    if (state.quest.applicationName != null) {
                        InfoChip("App", state.quest.applicationName, QuestColors.Primary)
                    }
                }

                if (state.status == QuestStatus.RUNNING || state.status == QuestStatus.DONE) {
                    Spacer(Modifier.height(10.dp))
                    val prog = (state.progress.toFloat() / state.quest.secondsNeeded.toFloat()).coerceIn(0f, 1f)
                    Box(Modifier.fillMaxWidth().height(4.dp).background(QuestColors.SurfaceVar, CircleShape)) {
                        Box(Modifier.fillMaxWidth(prog).height(4.dp).background(
                            Brush.horizontalGradient(listOf(rewardColor, rewardColor.copy(0.6f))), CircleShape
                        ))
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${state.progress}s / ${state.quest.secondsNeeded}s",
                        fontSize = 10.sp, color = QuestColors.TextMuted, fontFamily = FontFamily.Monospace
                    )
                }

                if (state.log.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(state.log, fontSize = 11.sp, color = when (state.status) {
                        QuestStatus.ERROR -> QuestColors.Error
                        QuestStatus.DONE  -> QuestColors.Success
                        else              -> QuestColors.TextMuted
                    }, fontFamily = FontFamily.Monospace, lineHeight = 15.sp)
                }

                Spacer(Modifier.height(12.dp))

                when (state.status) {
                    QuestStatus.IDLE -> {
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    completeQuest(token, state, onStateChange)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = rewardColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.PlayArrow, null, modifier = Modifier.size(16.dp), tint = Color.Black)
                            Spacer(Modifier.width(6.dp))
                            Text("Complete Quest", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                    QuestStatus.RUNNING -> {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(QuestColors.SurfaceVar, RoundedCornerShape(10.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = rewardColor.copy(shimmerAlpha),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("Completing...", fontWeight = FontWeight.Bold, color = rewardColor, fontSize = 13.sp)
                        }
                    }
                    QuestStatus.DONE -> {
                        Row(
                            Modifier.fillMaxWidth()
                                .background(QuestColors.Success.copy(0.12f), RoundedCornerShape(10.dp))
                                .border(1.dp, QuestColors.Success.copy(0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.CheckCircle, null, tint = QuestColors.Success, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Quest Completed!", fontWeight = FontWeight.Bold, color = QuestColors.Success, fontSize = 13.sp)
                        }
                    }
                    QuestStatus.ERROR -> {
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    completeQuest(token, state.copy(status = QuestStatus.IDLE, progress = 0L, log = ""), onStateChange)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = QuestColors.Error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Retry", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoChip(label: String, value: String, color: Color) {
        Row(
            Modifier
                .background(color.copy(0.08f), RoundedCornerShape(6.dp))
                .border(1.dp, color.copy(0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$label: ", fontSize = 9.sp, color = color.copy(0.7f), fontWeight = FontWeight.Bold)
            Text(value, fontSize = 9.sp, color = color, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }

    private suspend fun fetchQuests(token: String): List<QuestItem> = withContext(Dispatchers.IO) {
        val resp = questHttpClient.newCall(
            Request.Builder()
                .url("https://discord.com/api/v10/users/@me/quests?with_userquest=true&with_orb_balance=false&include_dismissed=false&user_quest_completions=false")
                .header("Authorization", token)
                .build()
        ).execute()

        if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
        val body = resp.body?.string() ?: throw Exception("Empty response")
        val json = JSONObject(body)
        val questsArr = json.optJSONArray("quests") ?: JSONArray()
        val result = mutableListOf<QuestItem>()

        for (i in 0 until questsArr.length()) {
            val q = questsArr.getJSONObject(i)
            val userStatus = q.optJSONObject("user_status") ?: continue
            if (userStatus.has("completed_at") && !userStatus.isNull("completed_at")) continue
            if (!userStatus.has("enrolled_at") || userStatus.isNull("enrolled_at")) continue

            val config = q.optJSONObject("config") ?: continue
            val expiresAt = config.optString("expires_at", "")
            val now = System.currentTimeMillis()
            try {
                val exp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
                    .parse(expiresAt.substringBefore('.'))?.time ?: 0L
                if (exp < now) continue
            } catch (_: Exception) { continue }

            val taskConfig = config.optJSONObject("taskConfig") ?: config.optJSONObject("taskConfigV2") ?: continue
            val tasks = taskConfig.optJSONObject("tasks") ?: continue
            val taskName = listOf("WATCH_VIDEO", "PLAY_ON_DESKTOP", "STREAM_ON_DESKTOP", "PLAY_ACTIVITY", "WATCH_VIDEO_ON_MOBILE")
                .firstOrNull { tasks.has(it) } ?: continue
            val taskObj = tasks.optJSONObject(taskName) ?: continue
            val secondsNeeded = taskObj.optLong("target", 0L)
            val secondsDone = userStatus.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L

            val application = config.optJSONObject("application")
            val appId = application?.optString("id")
            val appName = application?.optString("name")

            val messages = config.optJSONObject("messages")
            val questName = messages?.optString("questName") ?: appName ?: "Quest #${q.optString("id").take(8)}"

            val rewardStr = messages?.optString("rewardPreview") ?: messages?.optString("rewardDescription") ?: ""
            val rewardType = when {
                rewardStr.contains("orb", ignoreCase = true) -> "orbs"
                rewardStr.contains("avatar decoration", ignoreCase = true) || rewardStr.contains("decoration", ignoreCase = true) -> "decor"
                else -> "other"
            }

            val expDisplay = try {
                val exp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
                    .parse(expiresAt.substringBefore('.'))
                java.text.SimpleDateFormat("MM/dd", java.util.Locale.US).format(exp!!)
            } catch (_: Exception) { "?" }

            result.add(QuestItem(
                id = q.optString("id"),
                name = questName,
                reward = rewardStr.ifBlank { "Unknown Reward" },
                rewardType = rewardType,
                expiresAt = expDisplay,
                taskName = taskName,
                secondsNeeded = secondsNeeded,
                secondsDone = secondsDone,
                applicationId = appId,
                applicationName = appName
            ))
        }
        result
    }

    private suspend fun completeQuest(
        token: String,
        initialState: QuestState,
        onStateChange: (QuestState) -> Unit
    ) {
        val questId = initialState.quest.id
        val taskName = initialState.quest.taskName
        val secondsNeeded = initialState.quest.secondsNeeded
        var secondsDone = initialState.quest.secondsDone

        var current = initialState.copy(status = QuestStatus.RUNNING, log = "Starting...")
        withContext(Dispatchers.Main) { onStateChange(current) }

        try {
            when (taskName) {
                "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                    val enrolledAtResp = questHttpClient.newCall(
                        Request.Builder()
                            .url("https://discord.com/api/v10/users/@me/quests?with_userquest=true&with_orb_balance=false&include_dismissed=false&user_quest_completions=false")
                            .header("Authorization", token)
                            .build()
                    ).execute()

                    val enrolledAt = try {
                        val body = JSONObject(enrolledAtResp.body?.string() ?: "{}")
                        val quests = body.optJSONArray("quests") ?: JSONArray()
                        var ea = System.currentTimeMillis() - 30000L
                        for (i in 0 until quests.length()) {
                            val q = quests.getJSONObject(i)
                            if (q.optString("id") == questId) {
                                val us = q.optJSONObject("user_status")
                                val str = us?.optString("enrolled_at") ?: ""
                                if (str.isNotEmpty()) {
                                    ea = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                                        .also { it.timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                        .parse(str.substringBefore('.'))?.time ?: ea
                                }
                                break
                            }
                        }
                        ea
                    } catch (_: Exception) { System.currentTimeMillis() - 30000L }

                    val maxFuture = 10L
                    val speed = 7L
                    val interval = 1000L
                    var completed = false

                    while (secondsDone < secondsNeeded) {
                        val maxAllowed = ((System.currentTimeMillis() - enrolledAt) / 1000L) + maxFuture
                        val diff = maxAllowed - secondsDone
                        val timestamp = secondsDone + speed

                        if (diff >= speed) {
                            val body = JSONObject().apply { put("timestamp", minOf(secondsNeeded, timestamp).toDouble() + (Math.random() * 0.5)) }
                            val resp = questHttpClient.newCall(
                                Request.Builder()
                                    .url("https://discord.com/api/v10/quests/$questId/video-progress")
                                    .header("Authorization", token)
                                    .header("Content-Type", "application/json")
                                    .post(body.toString().toRequestBody("application/json".toMediaType()))
                                    .build()
                            ).execute()
                            val respBody = resp.body?.string() ?: ""
                            completed = try { JSONObject(respBody).has("completed_at") && !JSONObject(respBody).isNull("completed_at") } catch (_: Exception) { false }
                            secondsDone = minOf(secondsNeeded, timestamp)
                        }

                        current = current.copy(progress = secondsDone, log = "Video progress: ${secondsDone}s / ${secondsNeeded}s")
                        withContext(Dispatchers.Main) { onStateChange(current) }

                        if (secondsDone >= secondsNeeded) break
                        delay(interval)
                    }

                    if (!completed) {
                        val body = JSONObject().apply { put("timestamp", secondsNeeded.toDouble()) }
                        questHttpClient.newCall(
                            Request.Builder()
                                .url("https://discord.com/api/v10/quests/$questId/video-progress")
                                .header("Authorization", token)
                                .header("Content-Type", "application/json")
                                .post(body.toString().toRequestBody("application/json".toMediaType()))
                                .build()
                        ).execute()
                    }

                    current = current.copy(status = QuestStatus.DONE, progress = secondsNeeded, log = "Quest completed!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                "PLAY_ACTIVITY" -> {
                    val channelResp = questHttpClient.newCall(
                        Request.Builder()
                            .url("https://discord.com/api/v10/users/@me/channels")
                            .header("Authorization", token)
                            .build()
                    ).execute()
                    val channelId = try {
                        val arr = JSONArray(channelResp.body?.string() ?: "[]")
                        if (arr.length() > 0) arr.getJSONObject(0).optString("id") else "1"
                    } catch (_: Exception) { "1" }

                    val streamKey = "call:$channelId:1"

                    while (secondsDone < secondsNeeded) {
                        val body = JSONObject().apply {
                            put("stream_key", streamKey)
                            put("terminal", false)
                        }
                        val resp = questHttpClient.newCall(
                            Request.Builder()
                                .url("https://discord.com/api/v10/quests/$questId/heartbeat")
                                .header("Authorization", token)
                                .header("Content-Type", "application/json")
                                .post(body.toString().toRequestBody("application/json".toMediaType()))
                                .build()
                        ).execute()

                        val respBody = resp.body?.string() ?: "{}"
                        val progress = try {
                            JSONObject(respBody).optJSONObject("progress")?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", secondsDone) ?: secondsDone
                        } catch (_: Exception) { secondsDone }

                        secondsDone = progress
                        current = current.copy(progress = secondsDone, log = "Activity heartbeat: ${secondsDone}s / ${secondsNeeded}s")
                        withContext(Dispatchers.Main) { onStateChange(current) }

                        if (secondsDone >= secondsNeeded) break
                        delay(20000)
                    }

                    val termBody = JSONObject().apply {
                        put("stream_key", "call:$channelId:1")
                        put("terminal", true)
                    }
                    questHttpClient.newCall(
                        Request.Builder()
                            .url("https://discord.com/api/v10/quests/$questId/heartbeat")
                            .header("Authorization", token)
                            .header("Content-Type", "application/json")
                            .post(termBody.toString().toRequestBody("application/json".toMediaType()))
                            .build()
                    ).execute()

                    current = current.copy(status = QuestStatus.DONE, progress = secondsNeeded, log = "Quest completed!")
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }

                else -> {
                    current = current.copy(
                        status = QuestStatus.ERROR,
                        log = "Task '$taskName' requires Discord desktop app and cannot be completed on mobile."
                    )
                    withContext(Dispatchers.Main) { onStateChange(current) }
                }
            }
        } catch (e: Exception) {
            current = current.copy(status = QuestStatus.ERROR, log = "Error: ${e.message}")
            withContext(Dispatchers.Main) { onStateChange(current) }
        }
    }
}
