package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val http = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

private const val FALLBACK_SUPER_PROPS = "eyJvcyI6IkFuZHJvaWQiLCJicm93c2VyIjoiQW5kcm9pZCBNb2JpbGUiLCJkZXZpY2UiOiJBbmRyb2lkIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKEFuZHJvaWQgMTY7IE1vYmlsZTsgcnY6MTUyLjApIEdlY2tvLzE1Mi4wIEZpcmVmb3gvMTUyLjAiLCJicm93c2VyX3ZlcnNpb24iOiIxNTIuMCIsIm9zX3ZlcnNpb24iOiIxNiIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiIiLCJyZWZlcnJpbmdfZG9tYWluX2N1cnJlbnQiOiIiLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1NjUzMTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGwsImNsaWVudF9sYXVuY2hfaWQiOiI4ZWU3ZmU2My0zYTA1LTQwOGUtOTNhNi1kOWQ2MjlkZDQ4ZWUiLCJsYXVuY2hfc2lnbmF0dXJlIjoiNTc3OGFmMDMtYjYyMi00N2E1LTk1MTItM2IzY2QwYTRjM2I4IiwiY2xpZW50X2hlYXJ0YmVhdF9zZXNzaW9uX2lkIjoiOWFhNmZlMTgtM2RkNi00OTcyLWE0YjAtZWQwNGFjNThkZWQ2IiwiY2xpZW50X2FwcF9zdGF0ZSI6ImZvY3VzZWQifQ=="

private object DC {
    val Bg        = Color(0xFF0E0F13)
    val Surface   = Color(0xFF17181C)
    val Card      = Color(0xFF1E1F26)
    val CardAlt   = Color(0xFF22232B)
    val Border    = Color(0xFF2C2D35)
    val Primary   = Color(0xFF5865F2)
    val Success   = Color(0xFF23A55A)
    val Warning   = Color(0xFFFAA61A)
    val Error     = Color(0xFFED4245)
    val White     = Color(0xFFF2F3F5)
    val SubText   = Color(0xFFB5BAC1)
    val Muted     = Color(0xFF72767D)
    val OrbViolet = Color(0xFFB675F0)
    val Teal      = Color(0xFF43B581)
}

data class Region(val label: String, val flag: String, val locale: String, val timezone: String)

private val REGIONS = listOf(
    Region("Brazil", "BR", "pt-BR", "America/Sao_Paulo"),
    Region("United States", "US", "en-US", "America/New_York"),
    Region("United Kingdom", "GB", "en-GB", "Europe/London"),
    Region("France", "FR", "fr", "Europe/Paris"),
    Region("Germany", "DE", "de", "Europe/Berlin"),
    Region("Japan", "JP", "ja", "Asia/Tokyo"),
    Region("South Korea", "KR", "ko", "Asia/Seoul"),
    Region("Canada", "CA", "en-US", "America/Toronto"),
    Region("Australia", "AU", "en-AU", "Australia/Sydney"),
    Region("Poland", "PL", "pl", "Europe/Warsaw"),
    Region("Turkey", "TR", "tr", "Europe/Istanbul"),
    Region("Russia", "RU", "ru", "Europe/Moscow"),
    Region("India", "IN", "en-IN", "Asia/Kolkata"),
    Region("Mexico", "MX", "es-419", "America/Mexico_City"),
    Region("Spain", "ES", "es-ES", "Europe/Madrid")
)

data class QuestLog(val timestamp: String, val tag: String, val message: String, val detail: String? = null)
data class HttpHookLog(
    val id: Long,
    val timestamp: String,
    val method: String,
    val url: String,
    val requestHeaders: String,
    val requestBody: String?,
    val responseStatus: Int,
    val responseHeaders: String,
    val responseBody: String
)
data class ConsoleLogEntry(val timestamp: String, val level: String, val message: String)

private val questLogs = mutableStateListOf<QuestLog>()
private val httpHookLogs = mutableStateListOf<HttpHookLog>()
private val consoleLogs = mutableStateListOf<ConsoleLogEntry>()
private var hookLogIdCounter = 0L
private val pendingHttpRequests = mutableMapOf<String, HttpHookLog>()
private var capturedSuperProps = mutableStateOf("")
private var capturedBuildNumber = mutableStateOf(0)
private var webViewReady = mutableStateOf(false)
private var stopHooking = mutableStateOf(false)
private var currentToken = ""

private fun sanitizeText(text: String): String {
    if (currentToken.isEmpty()) return text
    return text.replace(currentToken, "[REDACTED BY DGT]")
        .replace("\"token\":\"[^\"]+\"".toRegex(), "\"token\":\"[REDACTED BY DGT]\"")
}

private fun addQuestLog(tag: String, message: String, detail: String? = null) {
    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    val safeMsg = sanitizeText(message)
    val safeDet = detail?.let { sanitizeText(it) }
    questLogs.add(0, QuestLog(ts, tag, safeMsg, safeDet))
    if (questLogs.size > 500) questLogs.removeAt(questLogs.size - 1)
}

private fun addHttpHookLog(isRequest: Boolean, method: String, url: String, headers: String, body: String?, status: Int = 0, respHeaders: String = "", respBody: String = "") {
    if (stopHooking.value) return
    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    val safeHeaders = sanitizeText(headers)
    val safeBody = body?.let { sanitizeText(it) }
    val safeRespHeaders = sanitizeText(respHeaders)
    val safeRespBody = sanitizeText(respBody)

    if (isRequest) {
        val id = ++hookLogIdCounter
        val log = HttpHookLog(id, ts, method, url, safeHeaders, safeBody, 0, "", "")
        val key = "$method:$url"
        pendingHttpRequests[key] = log
    } else {
        val key = "$method:$url"
        val pending = pendingHttpRequests.remove(key)
        if (pending != null) {
            val complete = pending.copy(responseStatus = status, responseHeaders = safeRespHeaders, responseBody = safeRespBody)
            httpHookLogs.add(0, complete)
        } else {
            val id = ++hookLogIdCounter
            httpHookLogs.add(0, HttpHookLog(id, ts, method, url, "", "", status, safeRespHeaders, safeRespBody))
        }
        if (httpHookLogs.size > 200) httpHookLogs.removeAt(httpHookLogs.size - 1)
    }
}

private fun addConsoleLog(level: String, message: String) {
    if (stopHooking.value && level != "SUCCESS" && level != "ERROR") return
    val ts = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())
    val safeMsg = sanitizeText(message)
    consoleLogs.add(0, ConsoleLogEntry(ts, level, safeMsg))
    if (consoleLogs.size > 300) consoleLogs.removeAt(consoleLogs.size - 1)
}

private fun saveLogsToCache(ctx: Context) {
    try {
        val dir = File(ctx.cacheDir, "quest_logs")
        dir.mkdirs()

        val httpFile = File(dir, "http_logs.json")
        val httpArray = JSONArray()
        httpHookLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("timestamp", log.timestamp)
            obj.put("method", log.method)
            obj.put("url", log.url)
            obj.put("requestHeaders", log.requestHeaders)
            obj.put("requestBody", log.requestBody ?: "")
            obj.put("responseStatus", log.responseStatus)
            obj.put("responseHeaders", log.responseHeaders)
            obj.put("responseBody", log.responseBody)
            httpArray.put(obj)
        }
        httpFile.writeText(httpArray.toString())

        val consoleFile = File(dir, "console_logs.json")
        val consoleArray = JSONArray()
        consoleLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("timestamp", log.timestamp)
            obj.put("level", log.level)
            obj.put("message", log.message)
            consoleArray.put(obj)
        }
        consoleFile.writeText(consoleArray.toString())

        val questFile = File(dir, "quest_logs.json")
        val questArray = JSONArray()
        questLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("timestamp", log.timestamp)
            obj.put("tag", log.tag)
            obj.put("message", log.message)
            log.detail?.let { obj.put("detail", it) }
            questArray.put(obj)
        }
        questFile.writeText(questArray.toString())
    } catch (_: Exception) {}
}

private fun loadLogsFromCache(ctx: Context) {
    try {
        val dir = File(ctx.cacheDir, "quest_logs")

        val httpFile = File(dir, "http_logs.json")
        if (httpFile.exists()) {
            val arr = JSONArray(httpFile.readText())
            for (i in arr.length() - 1 downTo 0) {
                val o = arr.getJSONObject(i)
                httpHookLogs.add(HttpHookLog(
                    ++hookLogIdCounter,
                    o.optString("timestamp"),
                    o.optString("method"),
                    o.optString("url"),
                    o.optString("requestHeaders"),
                    o.optString("requestBody").takeIf { it.isNotEmpty() },
                    o.optInt("responseStatus"),
                    o.optString("responseHeaders"),
                    o.optString("responseBody")
                ))
            }
        }

        val consoleFile = File(dir, "console_logs.json")
        if (consoleFile.exists()) {
            val arr = JSONArray(consoleFile.readText())
            for (i in arr.length() - 1 downTo 0) {
                val o = arr.getJSONObject(i)
                consoleLogs.add(ConsoleLogEntry(o.optString("timestamp"), o.optString("level"), o.optString("message")))
            }
        }

        val questFile = File(dir, "quest_logs.json")
        if (questFile.exists()) {
            val arr = JSONArray(questFile.readText())
            for (i in arr.length() - 1 downTo 0) {
                val o = arr.getJSONObject(i)
                questLogs.add(QuestLog(o.optString("timestamp"), o.optString("tag"), o.optString("message"), o.optString("detail").takeIf { it.isNotEmpty() }))
            }
        }
    } catch (_: Exception) {}
}

private fun clearLogsCache(ctx: Context) {
    try {
        val dir = File(ctx.cacheDir, "quest_logs")
        dir.deleteRecursively()
    } catch (_: Exception) {}
    httpHookLogs.clear()
    consoleLogs.clear()
    questLogs.clear()
    pendingHttpRequests.clear()
}

private const val HOOK_JS = """
(function() {
    try {
        var TOKEN = '__TOKEN_PLACEHOLDER__';
        try {
            var existing = localStorage.getItem('token');
            if (!existing || JSON.parse(existing) !== TOKEN) {
                localStorage.setItem('token', JSON.stringify(TOKEN));
            }
        } catch(e) {}

        function safeCall(fn) {
            try { fn(); } catch(e) {}
        }

        var origOpen = XMLHttpRequest.prototype.open;
        var origSend = XMLHttpRequest.prototype.send;
        var origSetHeader = XMLHttpRequest.prototype.setRequestHeader;

        XMLHttpRequest.prototype.open = function(method, url) {
            this.__hookMethod = method;
            this.__hookUrl = url;
            this.__hookHeaders = {};
            return origOpen.apply(this, arguments);
        };

        XMLHttpRequest.prototype.setRequestHeader = function(name, value) {
            if (this.__hookHeaders) this.__hookHeaders[name] = value;
            if (name === 'X-Super-Properties' && value) {
                safeCall(function() { AndroidHook.onSuperProps(value); });
            }
            return origSetHeader.apply(this, arguments);
        };

        XMLHttpRequest.prototype.send = function(body) {
            var self = this;
            var method = this.__hookMethod || 'GET';
            var url = this.__hookUrl || '';
            var headers = JSON.stringify(this.__hookHeaders || {});
            var bodyStr = body ? (typeof body === 'string' ? body : String(body)) : '';

            safeCall(function() { AndroidHook.onHttpRequest(method, url, headers, bodyStr); });

            this.addEventListener('load', function() {
                safeCall(function() {
                    var respHeaders = self.getAllResponseHeaders() || '';
                    var respBody = self.responseText || '';
                    AndroidHook.onHttpResponse(method, url, self.status || 0, respHeaders, respBody);
                });
            });

            this.addEventListener('error', function() {
                safeCall(function() {
                    AndroidHook.onHttpResponse(method, url, 0, '', 'Network Error');
                });
            });

            return origSend.apply(this, arguments);
        };

        var origFetch = window.fetch;
        window.fetch = function(input, init) {
            var url = typeof input === 'string' ? input : (input && input.url) || '';
            var method = (init && init.method) || (input && input.method) || 'GET';

            var headerObj = {};
            var rawHeaders = (init && init.headers) || (input && input.headers);
            if (rawHeaders) {
                if (rawHeaders instanceof Headers) {
                    rawHeaders.forEach(function(v, k) { headerObj[k] = v; });
                } else if (typeof rawHeaders === 'object') {
                    for (var k in rawHeaders) { headerObj[k] = rawHeaders[k]; }
                }
            }

            if (headerObj['X-Super-Properties']) {
                safeCall(function() { AndroidHook.onSuperProps(headerObj['X-Super-Properties']); });
            }

            var body = (init && init.body) ? String(init.body) : '';
            var headersStr = JSON.stringify(headerObj);

            safeCall(function() { AndroidHook.onHttpRequest(method, url, headersStr, body); });

            return origFetch.apply(this, arguments).then(function(response) {
                var cloned = response.clone();
                cloned.text().then(function(text) {
                    var rh = {};
                    cloned.headers.forEach(function(v, k) { rh[k] = v; });
                    safeCall(function() {
                        AndroidHook.onHttpResponse(method, url, response.status, JSON.stringify(rh), text);
                    });
                }).catch(function() {});
                return response;
            });
        };

        ['log', 'warn', 'error', 'info', 'debug'].forEach(function(level) {
            var orig = console[level];
            console[level] = function() {
                var args = Array.prototype.slice.call(arguments).map(function(a) {
                    if (typeof a === 'object') {
                        try { return JSON.stringify(a); } catch(e) { return String(a); }
                    }
                    return String(a)
                }).join(' ');
                safeCall(function() { AndroidHook.onConsoleLog(level.toUpperCase(), args); });
                return orig.apply(console, arguments);
            };
        });

        window.addEventListener('error', function(e) {
            safeCall(function() {
                AndroidHook.onConsoleLog('ERROR', (e.message || 'Unknown error') + ' (' + (e.filename || '') + ':' + (e.lineno || 0) + ')');
            });
        });

        window.addEventListener('unhandledrejection', function(e) {
            safeCall(function() {
                var reason = e.reason && e.reason.message ? e.reason.message : String(e.reason);
                AndroidHook.onConsoleLog('ERROR', 'Unhandled rejection: ' + reason);
            });
        });

        safeCall(function() { AndroidHook.onConsoleLog('INFO', 'Hooks installed successfully'); });
    } catch(e) {
        safeCall(function() { AndroidHook.onConsoleLog('ERROR', 'Hook init error: ' + e.message); });
    }
})();
"""

private const val QUEST_COMPLETE_JS = """
(function() {
    try {
        var wpRequire = webpackChunkdiscord_app.push([[Symbol()], {}, r => r]);
        webpackChunkdiscord_app.pop();

        var QuestsStore = Object.values(wpRequire.c).find(x => x?.exports?.A?.__proto__?.getQuest)?.exports.A;
        var api = Object.values(wpRequire.c).find(x => x?.exports?.Bo?.get)?.exports.Bo;
        var ChannelStore = Object.values(wpRequire.c).find(x => x?.exports?.A?.__proto__?.getAllThreadsForParent)?.exports.A;

        if (!QuestsStore) { AndroidHook.onConsoleLog('ERROR', 'QuestsStore not found'); return; }
        if (!api) { AndroidHook.onConsoleLog('ERROR', 'API module not found'); return; }

        var supportedTasks = ["WATCH_VIDEO", "PLAY_ON_DESKTOP", "STREAM_ON_DESKTOP", "PLAY_ACTIVITY", "WATCH_VIDEO_ON_MOBILE"];
        var questId = '__QUEST_ID__';

        var quest = null;
        try {
            quest = QuestsStore.quests.get(questId);
        } catch(e) {}

        if (!quest) {
            var quests = Array.from(QuestsStore.quests.values());
            for (var i = 0; i < quests.length; i++) {
                if (quests[i].id === questId) { quest = quests[i]; break; }
            }
        }

        if (!quest) { AndroidHook.onConsoleLog('ERROR', 'Quest not found: ' + questId); return; }

        var taskConfig = quest.config.taskConfig || quest.config.taskConfigV2;
        var taskName = supportedTasks.find(function(x) { return taskConfig.tasks[x] != null; });
        if (!taskName) { AndroidHook.onConsoleLog('ERROR', 'No supported task found'); return; }

        var secondsNeeded = taskConfig.tasks[taskName].target;
        var secondsDone = (quest.userStatus && quest.userStatus.progress && quest.userStatus.progress[taskName] && quest.userStatus.progress[taskName].value) || 0;

        AndroidHook.onConsoleLog('INFO', 'Starting quest: ' + questId + ' task: ' + taskName + ' needed: ' + secondsNeeded + ' done: ' + secondsDone);

        if (taskName === "WATCH_VIDEO" || taskName === "WATCH_VIDEO_ON_MOBILE") {
            var speed = 7;
            (async function() {
                while (secondsDone < secondsNeeded) {
                    var remaining = Math.min(speed, secondsNeeded - secondsDone);
                    await new Promise(function(resolve) { setTimeout(resolve, remaining * 1000); });

                    var timestamp = secondsDone + speed;
                    var res = await api.post({
                        url: '/quests/' + questId + '/video-progress',
                        body: { timestamp: Math.min(secondsNeeded, timestamp + Math.random()) }
                    });

                    var completed = res.body && res.body.completed_at != null;
                    secondsDone = Math.min(secondsNeeded, timestamp);
                    AndroidHook.onConsoleLog('INFO', 'Video progress: ' + secondsDone + '/' + secondsNeeded);

                    if (timestamp >= secondsNeeded) break;
                }

                await api.post({
                    url: '/quests/' + questId + '/video-progress',
                    body: { timestamp: secondsNeeded }
                });

                AndroidHook.onConsoleLog('SUCCESS', 'Quest completed: ' + questId);
            })();
        } else if (taskName === "PLAY_ACTIVITY") {
            if (!ChannelStore) { AndroidHook.onConsoleLog('ERROR', 'ChannelStore not found'); return; }
            var channelId = ChannelStore.getSortedPrivateChannels()[0] && ChannelStore.getSortedPrivateChannels()[0].id;
            if (!channelId) { AndroidHook.onConsoleLog('ERROR', 'No channel found'); return; }
            var streamKey = 'call:' + channelId + ':1';

            (async function() {
                while (secondsDone < secondsNeeded) {
                    var res = await api.post({
                        url: '/quests/' + questId + '/heartbeat',
                        body: { stream_key: streamKey, terminal: false }
                    });

                    var progress = res.body && res.body.progress && res.body.progress.PLAY_ACTIVITY ? res.body.progress.PLAY_ACTIVITY.value : secondsDone;
                    secondsDone = progress;
                    AndroidHook.onConsoleLog('INFO', 'Activity progress: ' + progress + '/' + secondsNeeded);

                    await new Promise(function(resolve) { setTimeout(resolve, 20 * 1000); });

                    if (progress >= secondsNeeded) {
                        await api.post({
                            url: '/quests/' + questId + '/heartbeat',
                            body: { stream_key: streamKey, terminal: true }
                        });
                        break;
                    }
                }

                AndroidHook.onConsoleLog('SUCCESS', 'Quest completed: ' + questId);
            })();
        } else {
            AndroidHook.onConsoleLog('WARN', 'Task ' + taskName + ' is not supported on mobile');
        }
    } catch(e) {
        AndroidHook.onConsoleLog('ERROR', 'Quest completion error: ' + e.message);
    }
})();
"""

class WebHookInterface(
    private val onReq: (String, String, String, String) -> Unit,
    private val onResp: (String, String, Int, String, String) -> Unit,
    private val onConsole: (String, String) -> Unit,
    private val onProps: (String) -> Unit
) {
    @JavascriptInterface
    fun onHttpRequest(method: String, url: String, headers: String, body: String) {
        onReq.invoke(method, url, headers, body)
    }

    @JavascriptInterface
    fun onHttpResponse(method: String, url: String, status: Int, headers: String, body: String) {
        onResp.invoke(method, url, status, headers, body)
    }

    @JavascriptInterface
    fun onConsoleLog(level: String, message: String) {
        onConsole.invoke(level, message)
    }

    @JavascriptInterface
    fun onSuperProps(props: String) {
        onProps.invoke(props)
    }
}

class QuestWebLoader(private val ctx: Context, private val token: String) {
    private var webView: WebView? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var onQuestData: (suspend (String) -> Unit)? = null
    var onSuperProps: ((String) -> Unit)? = null
    var onPageLoaded: (() -> Unit)? = null

    fun init() {
        mainHandler.post {
            val wv = WebView(ctx)

            wv.settings.javaScriptEnabled = true
            wv.settings.domStorageEnabled = true
            wv.settings.databaseEnabled = true
            wv.settings.userAgentString = "Mozilla/5.0 (Android 16; Mobile; rv:152.0) Gecko/152.0 Firefox/152.0"
            wv.settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

            CookieManager.getInstance().setAcceptCookie(true)
            CookieManager.getInstance().setAcceptThirdPartyCookies(wv, true)

            val hookInterface = WebHookInterface(
                onReq = { method, url, headers, body ->
                    mainHandler.post {
                        addHttpHookLog(true, method, url, headers, body.takeIf { it.isNotEmpty() })
                        addQuestLog("HTTP", "$method $url", "Headers:\n$headers" + if (body.isNotEmpty()) "\nBody: $body" else "")
                    }
                },
                onResp = { method, url, status, headers, body ->
                    mainHandler.post {
                        addHttpHookLog(false, method, url, "", "", status, headers, body)
                        addQuestLog("HTTP", "Response $status $url", body.take(2000))
                        if (url.contains("/quests/@me") || url.contains("/quests?")) {
                            if (status == 200) {
                                stopHooking.value = true
                            }
                            scope.launch {
                                onQuestData?.invoke(body)
                            }
                        }
                    }
                },
                onConsole = { level, message ->
                    mainHandler.post {
                        addConsoleLog(level, message)
                    }
                },
                onProps = { props ->
                    mainHandler.post {
                        capturedSuperProps.value = props
                        try {
                            val json = JSONObject(String(Base64.decode(props, Base64.NO_WRAP)))
                            val build = json.optInt("client_build_number", 0)
                            if (build > 0) {
                                capturedBuildNumber.value = build
                                addQuestLog("SuperProps", "Captured build: $build", "Full: $props")
                            }
                        } catch (_: Exception) {}
                        onSuperProps?.invoke(props)
                    }
                }
            )

            wv.addJavascriptInterface(hookInterface, "AndroidHook")

            var hooksInjected = false
            var tokenSet = false

            wv.webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    addQuestLog("WebView", "Loading: $url")

                    if (!hooksInjected && url != null && url.contains("discord.com")) {
                        val js = HOOK_JS.replace("__TOKEN_PLACEHOLDER__", token)
                        view?.evaluateJavascript(js, null)
                        hooksInjected = true
                        addQuestLog("WebView", "Hooks injected on $url")
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    addQuestLog("WebView", "Page finished: $url")

                    if (!hooksInjected && url != null && url.contains("discord.com")) {
                        val js = HOOK_JS.replace("__TOKEN_PLACEHOLDER__", token)
                        view?.evaluateJavascript(js, null)
                        hooksInjected = true
                    }

                    if (url != null && (url.contains("/login") || url.contains("/register"))) {
                        if (!tokenSet) {
                            val setTokenJs = """
                                try {
                                    localStorage.setItem('token', JSON.stringify("$token"));
                                    window.location.href = 'https://discord.com/quest-home';
                                } catch(e) {
                                    AndroidHook.onConsoleLog('ERROR', 'Token set failed: ' + e.message);
                                }
                            """.trimIndent()
                            view?.evaluateJavascript(setTokenJs, null)
                            tokenSet = true
                        }
                    }

                    if (url != null && url.contains("quest-home")) {
                        webViewReady.value = true
                        onPageLoaded?.invoke()
                    }
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url != null && url.contains("discord.com")) return false
                    return true
                }
            }

            wv.webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    if (consoleMessage != null) {
                        addConsoleLog(consoleMessage.messageLevel().name, "${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
                    }
                    return true
                }
            }

            wv.loadUrl("https://discord.com/quest-home")
            webView = wv
            addQuestLog("WebView", "WebView initialized, loading quest-home")
        }
    }

    fun getWebView(): WebView? = webView

    fun reload() {
        stopHooking.value = false
        mainHandler.post {
            webView?.reload()
        }
    }

    fun executeQuestJS(questId: String) {
        mainHandler.post {
            val js = QUEST_COMPLETE_JS.replace("__QUEST_ID__", questId)
            webView?.evaluateJavascript(js, null)
            addQuestLog("JS", "Injected quest completion JS for $questId")
        }
    }

    fun executeCustomJS(js: String, callback: ((String) -> Unit)? = null) {
        mainHandler.post {
            webView?.evaluateJavascript(js) { result ->
                callback?.invoke(result ?: "")
            }
        }
    }

    fun destroy() {
        scope.cancel()
        mainHandler.post {
            webView?.apply {
                stopLoading()
                loadUrl("about:blank")
                Handler(Looper.getMainLooper()).postDelayed({
                    clearHistory()
                    destroy()
                }, 300)
            }
            webView = null
            webViewReady.value = false
        }
    }
}

private fun getCachedSuperProps(ctx: Context): String {
    val sp = capturedSuperProps.value
    if (sp.isNotEmpty()) return sp
    return ctx.getSharedPreferences("quest_app_cache", Context.MODE_PRIVATE)
        .getString("super_props", FALLBACK_SUPER_PROPS) ?: FALLBACK_SUPER_PROPS
}

private fun saveCachedSuperProps(ctx: Context, props: String) {
    ctx.getSharedPreferences("quest_app_cache", Context.MODE_PRIVATE)
        .edit().putString("super_props", props).apply()
}

private fun buildReq(url: String, token: String, region: Region, superProps: String, referer: String = "https://discord.com/quest-home") =
    Request.Builder().url(url).apply {
        header("Authorization",         token)
        header("Content-Type",          "application/json")
        header("Accept",                "*/*")
        header("Accept-Language",       "${region.locale};q=0.9,en;q=0.8")
        header("User-Agent",            "Mozilla/5.0 (Android 16; Mobile; rv:152.0) Gecko/152.0 Firefox/152.0")
        header("X-Super-Properties",    superProps)
        header("X-Discord-Locale",      region.locale)
        header("X-Discord-Timezone",    region.timezone)
        header("X-Debug-Options",       "bugReporterEnabled")
        header("Referer",               referer)
        header("Origin",                "https://discord.com")
        header("Priority",              "u=1, i")
    }

private fun generateSuperPropsJson(region: Region, buildNumber: Int): String {
    val json = JSONObject()
    json.put("os", "Android")
    json.put("browser", "Android Mobile")
    json.put("device", "Android")
    json.put("system_locale", region.locale)
    json.put("has_client_mods", false)
    json.put("browser_user_agent", "Mozilla/5.0 (Android 16; Mobile; rv:152.0) Gecko/152.0 Firefox/152.0")
    json.put("browser_version", "152.0")
    json.put("os_version", "16")
    json.put("referrer", "")
    json.put("referring_domain", "")
    json.put("referrer_current", "")
    json.put("referring_domain_current", "")
    json.put("release_channel", "stable")
    json.put("client_build_number", buildNumber)
    json.put("client_event_source", JSONObject.NULL)
    json.put("client_launch_id", UUID.randomUUID().toString())
    json.put("launch_signature", UUID.randomUUID().toString())
    json.put("client_heartbeat_session_id", UUID.randomUUID().toString())
    json.put("client_app_state", "focused")
    return Base64.encodeToString(json.toString().toByteArray(), Base64.NO_WRAP)
}

private suspend fun fetchDynamicSuperProps(ctx: Context, region: Region): String = withContext(Dispatchers.IO) {
    addQuestLog("SuperProps", "Fetching builds from discord.sale/api/builds...")
    var attempts = 0
    var resultProps = ""

    while (attempts < 3 && resultProps.isEmpty()) {
        attempts++
        try {
            val req = Request.Builder().url("https://discord.sale/api/builds").build()
            val resp = http.newCall(req).execute()
            val body = resp.body?.string() ?: ""
            if (resp.isSuccessful && body.isNotEmpty()) {
                val arr = JSONObject(body).optJSONArray("builds")
                if (arr != null && arr.length() > 0) {
                    var latestDate = 0L
                    var buildNumber = 0
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

                    for (i in 0 until arr.length()) {
                        val b = arr.getJSONObject(i)
                        val dateStr = b.optString("build_date")
                        val date = try { sdf.parse(dateStr)?.time ?: 0L } catch (_: Exception) { 0L }
                        if (date > latestDate) {
                            latestDate = date
                            buildNumber = b.optInt("build_number")
                        }
                    }

                    if (buildNumber != 0) {
                        addQuestLog("SuperProps", "Latest build found: $buildNumber")
                        resultProps = generateSuperPropsJson(region, buildNumber)
                        saveCachedSuperProps(ctx, resultProps)
                        addQuestLog("SuperProps", "Generated and cached successfully!")
                    }
                }
            } else {
                addQuestLog("SuperProps", "Attempt $attempts failed (HTTP ${resp.code})")
            }
        } catch (e: Exception) {
            addQuestLog("SuperProps", "Error on attempt $attempts: ${e.message}")
        }
    }

    if (resultProps.isEmpty()) {
        addQuestLog("SuperProps", "All attempts failed. Using local cache.")
        resultProps = getCachedSuperProps(ctx)
    }

    return@withContext resultProps
}

private val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).also {
    it.timeZone = TimeZone.getTimeZone("UTC")
}
private fun parseIso(s: String?): Long =
    if (s.isNullOrEmpty() || s == "null") 0L
    else try { isoFmt.parse(s.substringBefore('.'))?.time ?: 0L } catch (_: Exception) { 0L }

private fun fmtShort(ms: Long): String =
    try { SimpleDateFormat("d/M", Locale.getDefault()).format(Date(ms)) } catch (_: Exception) { "" }

private fun buildBannerUrl(id: String, cfg: JSONObject): String? {
    val a = cfg.optJSONObject("assets") ?: return null
    for (k in listOf("quest_bar_hero", "hero", "logotype", "game_tile")) {
        val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
        if (v.contains(".mp4") || v.contains(".m3u8") || v.contains(".webm")) continue
        return if (v.startsWith("http")) v
               else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v"
               else "https://cdn.discordapp.com/quests/$id/$v"
    }
    val appId = cfg.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" }
    return if (appId != null) "https://cdn.discordapp.com/app-assets/$appId/store/header.jpg" else null
}

private fun buildRewardIconUrl(id: String, cfg: JSONObject): String? {
    val a = cfg.optJSONObject("assets")
    if (a != null) for (k in listOf("reward_generic_android", "reward", "collectible_preview", "quest_reward", "reward_tile", "game_tile")) {
        val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
        return if (v.startsWith("http")) v else "https://cdn.discordapp.com/quests/$id/$v"
    }
    val skuId = cfg.optJSONObject("rewards_config")?.optJSONArray("rewards")
        ?.optJSONObject(0)?.optString("sku_id")?.takeIf { it.isNotEmpty() && it != "null" }
    return if (skuId != null) "https://cdn.discordapp.com/collectibles-assets/$skuId/icon.png?size=80" else null
}

private fun buildVideoUrl(id: String, cfg: JSONObject): String? {
    cfg.optJSONObject("video_metadata")?.optString("video_url", "")?.takeIf { it.isNotEmpty() && it != "null" }?.let { return it }
    val a = cfg.optJSONObject("assets")
    if (a != null) {
        for (k in listOf("quest_bar_video", "hero_video", "quest_bar_hero_video", "video", "promo_video")) {
            val v = a.optString(k, "").takeIf { it.isNotEmpty() && it != "null" } ?: continue
            return if (v.startsWith("http")) v else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v" else "https://cdn.discordapp.com/quests/$id/$v"
        }
        for (k in a.keys()) {
            val v = a.optString(k, "")
            if (v.contains(".mp4") || v.contains(".m3u8") || v.contains(".webm"))
                return if (v.startsWith("http")) v else if (v.startsWith("quests/")) "https://cdn.discordapp.com/$v" else "https://cdn.discordapp.com/quests/$id/$v"
        }
    }
    val appId = cfg.optJSONObject("application")?.optString("id")?.takeIf { it.isNotEmpty() && it != "null" } ?: return null
    return "https://cdn.discordapp.com/quests/$id/${appId}_mx480.m3u8"
}

private val SUPPORTED = listOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ACTIVITY", "PLAY_ON_DESKTOP", "PLAY_ON_DESKTOP_V2", "STREAM_ON_DESKTOP")
private val MOBILE    = setOf("WATCH_VIDEO_ON_MOBILE", "WATCH_VIDEO", "PLAY_ACTIVITY")

data class QuestItem(
    val id: String, val name: String, val reward: String, val rewardType: String,
    val expiresMs: Long, val taskName: String, val secondsNeeded: Long, val secondsDone: Long,
    val publisher: String, val bannerUrl: String?, val rewardIconUrl: String?,
    val videoUrl: String?, val questLink: String?,
    val enrolledAt: String?, val completedAt: String?, val claimedAt: String?,
    val configVersion: Int,
    val rawConfig: JSONObject
)

data class CollectibleItem(
    val skuId: String, val name: String, val summary: String,
    val type: Int, val purchaseType: Int, val purchasedAt: String,
    val expiresAt: String?, val items: List<CollectibleSub>
)
data class CollectibleSub(val type: Int, val asset: String, val label: String)

enum class RunState { IDLE, RUNNING, DONE, ERROR, NOT_ENROLLED, DESKTOP_ONLY }
data class QuestState(val quest: QuestItem, val runState: RunState = RunState.IDLE, val progress: Long = 0L, val log: String = "")

data class CaptchaData(
    val sitekey: String,
    val rqdata: String,
    val rqtoken: String,
    val sessionId: String,
    val token: String = ""
)

private fun parseQuest(q: JSONObject): QuestItem? {
    val id  = q.optString("id").takeIf { it.isNotEmpty() } ?: return null
    val cfg = q.optJSONObject("config") ?: return null
    val us  = q.optJSONObject("user_status")
    val msg = cfg.optJSONObject("messages") ?: return null
    val configVersion = cfg.optInt("config_version", cfg.optInt("configVersion", 1))
    val taskCfgObj = cfg.optJSONObject("task_config") ?: cfg.optJSONObject("taskConfig")
        ?: cfg.optJSONObject("taskConfigV2") ?: cfg.optJSONObject("task_config_v2")
    val tasks = taskCfgObj?.optJSONObject("tasks") ?: return null
    val taskName = SUPPORTED.firstOrNull { tasks.has(it) } ?: return null
    val needed   = tasks.optJSONObject(taskName)?.optLong("target", 0L) ?: 0L
    val done     = us?.optJSONObject("progress")?.optJSONObject(taskName)?.optLong("value", 0L) ?: 0L
    val rewardsArr = cfg.optJSONObject("rewards_config")?.optJSONArray("rewards")
        ?: cfg.optJSONObject("rewardsConfig")?.optJSONArray("rewards")
    var reward = ""; var rewardType = "prize"
    if (rewardsArr != null && rewardsArr.length() > 0) {
        val r = rewardsArr.optJSONObject(0)
        if (r != null) {
            rewardType = when (r.optString("type", "0")) { "4" -> "orbs"; "3" -> "decor"; "2" -> "nitro"; else -> "prize" }
            reward = when (rewardType) {
                "orbs"  -> { val qty = r.optInt("orb_quantity", r.optInt("orbQuantity", 0)); if (qty > 0) "$qty Discord Orbs" else r.optJSONObject("messages")?.optString("name_with_article", "") ?: "Orbs" }
                "decor" -> r.optJSONObject("messages")?.optString("name_with_article", "")?.removePrefix("a ")?.removePrefix("an ") ?: "Avatar Decoration"
                "nitro" -> "Nitro"
                else    -> r.optJSONObject("messages")?.optString("name_with_article", "")?.removePrefix("a ")?.removePrefix("an ") ?: "In-game Reward"
            }
        }
    }
    return QuestItem(
        id = id, name = msg.optString("quest_name", msg.optString("questName", "")), reward = reward, rewardType = rewardType,
        expiresMs = parseIso(cfg.optString("expires_at", cfg.optString("expiresAt", ""))), taskName = taskName,
        secondsNeeded = needed, secondsDone = done, publisher = msg.optString("game_publisher", msg.optString("gamePublisher", "")),
        bannerUrl = buildBannerUrl(id, cfg), rewardIconUrl = buildRewardIconUrl(id, cfg),
        videoUrl = buildVideoUrl(id, cfg),
        questLink = cfg.optJSONObject("application")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" }
                    ?: cfg.optJSONObject("cta_config")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" }
                    ?: cfg.optJSONObject("ctaConfig")?.optString("link")?.takeIf { it.isNotEmpty() && it != "null" },
        enrolledAt  = us?.optString("enrolled_at")?.takeIf  { it.isNotEmpty() && it != "null" }
                      ?: us?.optString("enrolledAt")?.takeIf  { it.isNotEmpty() && it != "null" },
        completedAt = us?.optString("completed_at")?.takeIf { it.isNotEmpty() && it != "null" }
                      ?: us?.optString("completedAt")?.takeIf { it.isNotEmpty() && it != "null" },
        claimedAt   = us?.optString("claimed_at")?.takeIf   { it.isNotEmpty() && it != "null" }
                      ?: us?.optString("claimedAt")?.takeIf   { it.isNotEmpty() && it != "null" },
        configVersion = configVersion,
        rawConfig   = cfg
    )
}

private fun parseQuestsFromJson(body: String): List<QuestItem> {
    val now = System.currentTimeMillis()
    val parsed = try { JSONObject(body) } catch (_: Exception) { JSONObject() }
    val arr = parsed.optJSONArray("quests")
        ?: try { JSONArray(body) } catch (_: Exception) { JSONArray() }
    val list = mutableListOf<QuestItem>()
    for (i in 0 until arr.length()) {
        val item = parseQuest(arr.getJSONObject(i))
        if (item != null) {
            if (item.expiresMs <= 0 || item.expiresMs >= now) {
                if (item.claimedAt == null) {
                    list.add(item)
                }
            }
        }
    }
    return list
}

private fun saveQuestsCache(ctx: Context, body: String) {
    ctx.getSharedPreferences("quest_cache", Context.MODE_PRIVATE).edit().putString("quests", body).apply()
}

private fun loadQuestsCache(ctx: Context): String? {
    return ctx.getSharedPreferences("quest_cache", Context.MODE_PRIVATE).getString("quests", null)
}

private suspend fun apiFetch(token: String, region: Region, superProps: String, ctx: Context): Pair<List<QuestItem>, Int?> = withContext(Dispatchers.IO) {
    var attempts = 0
    var list = emptyList<QuestItem>()
    var orbs: Int? = null

    var rateLimited = true
    while (rateLimited) {
        attempts++
        val req = buildReq("https://discord.com/api/v9/quests/@me", token, region, superProps, "https://discord.com/quest-home?tab=all").build()
        addQuestLog("API", "GET /quests/@me", "Headers:\n${req.headers}")
        val resp = http.newCall(req).execute()
        val body = resp.body?.string() ?: ""
        
        if (resp.code == 429) {
            val retryAfter = try { JSONObject(body).optDouble("retry_after", 5.0).toLong() } catch (_: Exception) { 5L }
            addQuestLog("WARN", "Rate Limited", "429 Too Many Requests. Retrying in $retryAfter seconds...")
            delay(retryAfter * 1000 + 500)
            continue
        }
        rateLimited = false
        
        addQuestLog("API", "Response ${resp.code}", body.take(2000))
        if (!resp.isSuccessful) throw Exception(try { JSONObject(body).optString("message", "HTTP ${resp.code}") } catch (_: Exception) { "HTTP ${resp.code}" })

        saveQuestsCache(ctx, body)
        stopHooking.value = true

        list = parseQuestsFromJson(body)

        val orbReq = buildReq("https://discord.com/api/v9/users/@me/virtual-currency/balance", token, region, superProps).build()
        val orbBody = try {
            http.newCall(orbReq).execute().body?.string() ?: "{}"
        } catch (_: Exception) { "{}" }
        orbs = try { JSONObject(orbBody).optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }
    }
    
    Pair(list, orbs)
}

private suspend fun apiFirstDm(token: String, region: Region, superProps: String): String? = withContext(Dispatchers.IO) {
    try {
        val req = buildReq("https://discord.com/api/v9/users/@me/channels", token, region, superProps).build()
        val arr = JSONArray(http.newCall(req).execute().body?.string() ?: "[]")
        if (arr.length() > 0) arr.getJSONObject(0).optString("id").takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) { null }
}

private suspend fun apiCollectibles(token: String, region: Region, superProps: String): JSONArray = withContext(Dispatchers.IO) {
    try {
        JSONArray(http.newCall(buildReq("https://discord.com/api/v9/users/@me/collectibles-purchases", token, region, superProps).build()).execute().body?.string() ?: "[]")
    } catch (_: Exception) { JSONArray() }
}

private suspend fun retryApi(maxAttempts: Int = 5, initialDelayMs: Long = 500, maxDelayMs: Long = 8_000L, block: suspend () -> JSONObject): JSONObject {
    var attempt = 0; var delay = initialDelayMs
    while (true) {
        attempt++
        try { return block() } catch (e: Exception) {
            if (attempt >= maxAttempts) throw e
            kotlinx.coroutines.delay(delay)
            delay = minOf(maxDelayMs, delay * 2)
        }
    }
}

private suspend fun claimReward(token: String, region: Region, superProps: String, quest: QuestItem, captcha: CaptchaData? = null): Pair<JSONObject, CaptchaData?> = withContext(Dispatchers.IO) {
    val url = "https://discord.com/api/v9/quests/${quest.id}/claim-reward"
    val traffic = quest.rawConfig.optString("traffic_metadata_sealed", "")
    val bodyStr = JSONObject().apply {
        put("platform", 0)
        put("location", 11)
        put("is_targeted", false)
        put("metadata_sealed", JSONObject.NULL)
        put("traffic_metadata_sealed", traffic)
    }.toString()
    
    val reqBody = bodyStr.toRequestBody("application/json".toMediaType())
    val req = buildReq(url, token, region, superProps).post(reqBody).apply {
        if (captcha != null && captcha.token.isNotEmpty()) {
            header("X-Captcha-Key", captcha.token)
            header("X-Captcha-Rqtoken", captcha.rqtoken)
            header("X-Captcha-Session-Id", captcha.sessionId)
        }
    }.build()
    
    val resp = http.newCall(req).execute()
    val respBody = resp.body?.string() ?: "{}"
    val json = JSONObject(respBody)
    
    if (resp.code == 400 && json.has("captcha_key")) {
        val capData = CaptchaData(
            sitekey = json.optString("captcha_sitekey"),
            rqdata = json.optString("captcha_rqdata"),
            rqtoken = json.optString("captcha_rqtoken"),
            sessionId = json.optString("captcha_session_id")
        )
        return@withContext Pair(json, capData)
    }
    
    if (!resp.isSuccessful) {
        throw Exception(json.optString("message", "HTTP ${resp.code}"))
    }
    
    return@withContext Pair(json, null)
}

private suspend fun runComplete(token: String, region: Region, superProps: String, state: QuestState, onUpdate: (QuestState) -> Unit) {
    val q = state.quest; val questId = q.id; val taskName = q.taskName; val needed = q.secondsNeeded
    var done = q.secondsDone
    var cur  = state.copy(runState = RunState.RUNNING, log = "Starting...", progress = done)
    withContext(Dispatchers.Main) { onUpdate(cur) }
    fun upd(log: String, prog: Long = done, rs: RunState = RunState.RUNNING) { cur = cur.copy(runState = rs, log = log, progress = prog) }

    if (taskName !in MOBILE) {
        upd("Requires Discord Desktop app.", rs = RunState.DESKTOP_ONLY)
        withContext(Dispatchers.Main) { onUpdate(cur) }
        return
    }

    try {
        val enrolledAt = q.enrolledAt
        if (enrolledAt == null) {
            upd("Not enrolled. Accept this quest in the Discord app first.", 0, RunState.NOT_ENROLLED)
            withContext(Dispatchers.Main) { onUpdate(cur) }
            return
        }

        when (taskName) {
            "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> {
                upd("Syncing video progress...", done)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                var completed = false
                var running = true

                while (running) {
                    val remaining = minOf(7L, needed - done)
                    delay(remaining * 1000L)

                    val timestamp = done + 7L
                    val sendTs = minOf(needed.toDouble(), timestamp.toDouble() + Math.random())
                    val bodyStr = JSONObject().put("timestamp", sendTs).toString()
                    val reqBody = bodyStr.toRequestBody("application/json".toMediaType())

                    addQuestLog("API", "POST /video-progress", "Payload: $bodyStr")

                    val rj = try {
                        retryApi {
                            val postReq = buildReq(
                                "https://discord.com/api/v9/quests/$questId/video-progress",
                                token, region, superProps, "https://discord.com/quest-home"
                            ).post(reqBody).build()
                            val postResp = http.newCall(postReq).execute()
                            val postBody = postResp.body?.string() ?: "{}"
                            addQuestLog("API", "Response ${postResp.code}", postBody.take(2000))
                            JSONObject(postBody)
                        }
                    } catch (e: Exception) {
                        addQuestLog("ERROR", "Video progress failed", e.message ?: "")
                        upd("Error: ${e.message}", rs = RunState.ERROR)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        return@runComplete
                    }

                    completed = rj.optString("completed_at", "").isNotEmpty()
                    done = minOf(needed, timestamp)

                    upd("Video: ${done}s / ${needed}s (${if (needed > 0) done * 100 / needed else 0}%)", done)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (timestamp >= needed) {
                        running = false
                    }
                }

                if (!completed) {
                    try {
                        retryApi {
                            val finalBody = JSONObject().put("timestamp", needed.toDouble()).toString()
                            addQuestLog("API", "POST /video-progress (Final)", "Payload: $finalBody")
                            val postReq = buildReq(
                                "https://discord.com/api/v9/quests/$questId/video-progress",
                                token, region, superProps, "https://discord.com/quest-home"
                            ).post(finalBody.toRequestBody("application/json".toMediaType())).build()
                            val resp = http.newCall(postReq).execute()
                            val respBody = resp.body?.string() ?: "{}"
                            addQuestLog("API", "Response ${resp.code}", respBody.take(2000))
                            JSONObject(respBody)
                        }
                    } catch (_: Exception) {}
                }

                done = needed
                upd("Completed! Claim your reward in the Discord app.", done, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ACTIVITY" -> {
                val channelId = apiFirstDm(token, region, superProps) ?: throw Exception("No DM channel found.")
                val streamKey = "call:$channelId:1"
                upd("Syncing activity progress...", done)
                withContext(Dispatchers.Main) { onUpdate(cur) }
                var running = true

                while (running) {
                    val bodyStr = JSONObject().put("stream_key", streamKey).put("terminal", false).toString()
                    val reqBody = bodyStr.toRequestBody("application/json".toMediaType())
                    addQuestLog("API", "POST /heartbeat", "Payload: $bodyStr")

                    val rj = try {
                        retryApi {
                            val postReq = buildReq(
                                "https://discord.com/api/v9/quests/$questId/heartbeat",
                                token, region, superProps
                            ).post(reqBody).build()
                            val resp = http.newCall(postReq).execute()
                            val respBody = resp.body?.string() ?: "{}"
                            addQuestLog("API", "Response ${resp.code}", respBody.take(2000))
                            JSONObject(respBody)
                        }
                    } catch (e: Exception) {
                        upd("Error: ${e.message}", rs = RunState.ERROR)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        return@runComplete
                    }

                    done = if (q.configVersion == 1)
                        rj.optJSONObject("user_status")?.optLong("stream_progress_seconds", done) ?: done
                    else
                        rj.optJSONObject("progress")?.optJSONObject("PLAY_ACTIVITY")?.optLong("value", done) ?: done

                    upd("Activity: ${done}s / ${needed}s (~${maxOf(0L, (needed - done) / 60)} min left)", done)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (done >= needed) {
                        try {
                            retryApi {
                                val finalBody = JSONObject().put("stream_key", streamKey).put("terminal", true).toString()
                                val postReq = buildReq(
                                    "https://discord.com/api/v9/quests/$questId/heartbeat",
                                    token, region, superProps
                                ).post(finalBody.toRequestBody("application/json".toMediaType())).build()
                                JSONObject(http.newCall(postReq).execute().body?.string() ?: "{}")
                            }
                        } catch (_: Exception) {}
                        running = false
                    } else {
                        delay(20_000L)
                    }
                }

                upd("Completed! Claim your reward in the Discord app.", needed, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            else -> {
                upd("Task '$taskName' not supported on Android.", rs = RunState.DESKTOP_ONLY)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }
        }
    } catch (e: Exception) {
        upd("Error: ${e.message}", rs = RunState.ERROR)
        withContext(Dispatchers.Main) { onUpdate(cur) }
    }
}

class QuestActivity : ComponentActivity() {
    companion object {
        private const val EXTRA_TOKEN = "extra_token"
        private const val PREFS       = "quest_prefs_v3"
        fun start(ctx: Context, token: String) = ctx.startActivity(Intent(ctx, QuestActivity::class.java).putExtra(EXTRA_TOKEN, token))
    }

    private var webLoader: QuestWebLoader? = null

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        currentToken = token
        loadLogsFromCache(this)
        webLoader = QuestWebLoader(this, token)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = DC.Primary, background = DC.Bg, surface = DC.Surface)) {
                Surface(Modifier.fillMaxSize(), color = DC.Bg) {
                    val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    var tosAcked by remember { mutableStateOf(prefs.getBoolean("tos_ack", false)) }
                    if (!tosAcked) TosDialog(
                        onAccept  = { prefs.edit().putBoolean("tos_ack", true).apply(); tosAcked = true },
                        onDecline = { finish() }
                    ) else QuestScreen(token, webLoader, onBack = { finish() })
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val ctx = this
        Thread {
            saveLogsToCache(ctx)
        }.start()
        webLoader?.destroy()
    }
}

@Composable
private fun TosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth(0.92f).clip(RoundedCornerShape(20.dp)).background(DC.Card).border(1.dp, DC.Warning.copy(0.3f), RoundedCornerShape(20.dp)).padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(DC.Warning.copy(0.12f), CircleShape), Alignment.Center) { Icon(Icons.Outlined.Warning, null, tint = DC.Warning, modifier = Modifier.size(22.dp)) }
                    Text("Terms of Service", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White)
                }
                HorizontalDivider(color = DC.Border)
                Text("This tool automates Discord Quest completion via the official API.\n\nUsing automation may violate Discord's Terms of Service. Your account could be suspended or banned.\n\nFor educational purposes only. Use at your own risk.", fontSize = 13.sp, color = DC.SubText, lineHeight = 20.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, DC.Error.copy(0.5f))) { Text("Decline", color = DC.Error, fontWeight = FontWeight.Bold) }
                    Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) { Text("I Understand", fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
    }
}

@Composable
private fun QuestScreen(token: String, webLoader: QuestWebLoader?, onBack: () -> Unit) {
    var loading     by remember { mutableStateOf(true) }
    var fetchError  by remember { mutableStateOf<String?>(null) }
    var orbBalance  by remember { mutableStateOf<Int?>(null) }
    var refreshKey  by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    var showCollect by remember { mutableStateOf(false) }
    var showRegion  by remember { mutableStateOf(false) }
    var videoQuest  by remember { mutableStateOf<QuestItem?>(null) }
    var moreQuest   by remember { mutableStateOf<QuestItem?>(null) }
    var region      by remember { mutableStateOf(REGIONS[0]) }
    val ctx         = LocalContext.current
    var superProps  by remember { mutableStateOf(getCachedSuperProps(ctx)) }
    var sortMode    by remember { mutableIntStateOf(0) }
    var fOrbs       by remember { mutableStateOf(false) }
    var fDecor      by remember { mutableStateOf(false) }
    var fInGame     by remember { mutableStateOf(false) }
    var fWatch      by remember { mutableStateOf(false) }
    var fPlay       by remember { mutableStateOf(false) }
    val states      = remember { mutableStateListOf<QuestState>() }
    var showDebug   by remember { mutableStateOf(false) }
    var showWebView by remember { mutableStateOf(false) }

    var titleClicks by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    val capturedProps by capturedSuperProps
    val capturedBuild by capturedBuildNumber
    val wvReady by webViewReady

    LaunchedEffect(capturedBuild) {
        if (capturedBuild > 0) {
            val prefs = ctx.getSharedPreferences("quest_app_cache", Context.MODE_PRIVATE)
            val lastBuild = prefs.getInt("last_build", 0)
            if (lastBuild != 0 && lastBuild != capturedBuild) {
                addQuestLog("UPDATE", "Discord Updated!", "The client updated! Version b: $lastBuild --> version a: $capturedBuild")
            }
            if (lastBuild != capturedBuild) {
                prefs.edit().putInt("last_build", capturedBuild).apply()
            }
        }
    }

    LaunchedEffect(Unit) {
        val cached = loadQuestsCache(ctx)
        if (cached != null) {
            try {
                val list = withContext(Dispatchers.Default) { parseQuestsFromJson(cached) }
                states.clear()
                states.addAll(list.map { q ->
                    val rs = when {
                        q.completedAt != null || (q.secondsNeeded > 0 && q.secondsDone >= q.secondsNeeded) -> RunState.DONE
                        q.taskName !in MOBILE -> RunState.DESKTOP_ONLY
                        q.enrolledAt == null  -> RunState.NOT_ENROLLED
                        else -> RunState.IDLE
                    }
                    QuestState(
                        quest = q, runState = rs, progress = q.secondsDone,
                        log = when (rs) {
                            RunState.DONE         -> "Completed! Claim in the Discord app."
                            RunState.DESKTOP_ONLY -> "Requires Desktop app."
                            RunState.NOT_ENROLLED -> "Accept the quest in the Discord app first."
                            else -> ""
                        }
                    )
                })
                loading = false
                addQuestLog("Cache", "Loaded quests from cache")
            } catch (_: Exception) {}
        }

        webLoader?.onQuestData = { body ->
            try {
                val list = withContext(Dispatchers.Default) { parseQuestsFromJson(body) }
                withContext(Dispatchers.Main) {
                    states.clear()
                    states.addAll(list.map { q ->
                        val rs = when {
                            q.completedAt != null || (q.secondsNeeded > 0 && q.secondsDone >= q.secondsNeeded) -> RunState.DONE
                            q.taskName !in MOBILE -> RunState.DESKTOP_ONLY
                            q.enrolledAt == null  -> RunState.NOT_ENROLLED
                            else -> RunState.IDLE
                        }
                        QuestState(
                            quest = q, runState = rs, progress = q.secondsDone,
                            log = when (rs) {
                                RunState.DONE         -> "Completed! Claim in the Discord app."
                                RunState.DESKTOP_ONLY -> "Requires Desktop app."
                                RunState.NOT_ENROLLED -> "Accept the quest in the Discord app first."
                                else -> ""
                            }
                        )
                    })
                    loading = false
                    addQuestLog("WebView", "Quests parsed from hooked response: ${list.size}")
                }
            } catch (e: Exception) {
                addQuestLog("ERROR", "Parse hooked quests", e.message ?: "")
            }
        }

        webLoader?.onSuperProps = { props ->
            if (props.isNotEmpty()) {
                superProps = props
                CoroutineScope(Dispatchers.IO).launch {
                    saveCachedSuperProps(ctx, props)
                }
            }
        }

        webLoader?.init()
    }

    LaunchedEffect(refreshKey, region) {
        if (refreshKey > 0 || loading) {
            stopHooking.value = false
            loading = true; fetchError = null; states.clear(); orbBalance = null
            try {
                var (list, orbs) = apiFetch(token, region, superProps, ctx)

                if (list.isEmpty()) {
                    addQuestLog("WARN", "Quests Empty", "Trying to fetch new SuperProps...")
                    val newProps = fetchDynamicSuperProps(ctx, region)
                    if (newProps.isNotEmpty() && newProps != superProps) {
                        superProps = newProps
                        val retryResult = apiFetch(token, region, newProps, ctx)
                        list = retryResult.first
                        orbs = retryResult.second
                    }
                }

                states.addAll(list.map { q ->
                    val rs = when {
                        q.completedAt != null || (q.secondsNeeded > 0 && q.secondsDone >= q.secondsNeeded) -> RunState.DONE
                        q.taskName !in MOBILE -> RunState.DESKTOP_ONLY
                        q.enrolledAt == null  -> RunState.NOT_ENROLLED
                        else -> RunState.IDLE
                    }
                    QuestState(
                        quest = q, runState = rs, progress = q.secondsDone,
                        log = when (rs) {
                            RunState.DONE         -> "Completed! Claim in the Discord app."
                            RunState.DESKTOP_ONLY -> "Requires Desktop app."
                            RunState.NOT_ENROLLED -> "Accept the quest in the Discord app first."
                            else -> ""
                        }
                    )
                })
                orbBalance = orbs
            } catch (e: Exception) {
                fetchError = e.message
                addQuestLog("ERROR", "Fetch Error", e.message ?: "Unknown")
            }
            loading = false
        }
    }

    val displayed = remember(states.toList(), sortMode, fOrbs, fDecor, fInGame, fWatch, fPlay) {
        var d = states.toList()
        if (fOrbs)   d = d.filter { it.quest.rewardType == "orbs" }
        if (fDecor)  d = d.filter { it.quest.rewardType == "decor" }
        if (fInGame) d = d.filter { it.quest.rewardType != "orbs" && it.quest.rewardType != "decor" }
        if (fWatch)  d = d.filter { it.quest.taskName.contains("WATCH") }
        if (fPlay)   d = d.filter { it.quest.taskName.contains("PLAY") || it.quest.taskName.contains("STREAM") }
        when (sortMode) {
            1    -> d.sortedByDescending { it.quest.expiresMs }
            2    -> d.sortedBy { it.quest.expiresMs }
            3    -> d.filter { it.quest.enrolledAt != null }
            else -> d
        }
    }
    val filterCount = listOf(fOrbs, fDecor, fInGame, fWatch, fPlay).count { it }

    Box(Modifier.fillMaxSize().background(DC.Bg)) {
        Column(Modifier.fillMaxSize()) {
            QuestHeader(
                orbBalance = orbBalance, region = region, onBack = onBack,
                onFilter = { showFilters = true }, onCollect = { showCollect = true },
                onRegion = { showRegion = true }, onRefresh = { refreshKey++ },
                filterCount = filterCount,
                canCompleteAll = states.any { it.runState == RunState.IDLE },
                onCompleteAll = {
                    CoroutineScope(Dispatchers.IO).launch {
                        states.forEachIndexed { idx, st ->
                            if (st.runState == RunState.IDLE)
                                runComplete(token, region, superProps, st) { upd -> if (idx < states.size) states[idx] = upd }
                        }
                    }
                },
                onShowWebView = { showWebView = true },
                webViewReady = wvReady,
                capturedBuild = capturedBuild,
                onTitleClick = {
                    val now = System.currentTimeMillis()
                    if (now - lastClickTime > 2000) titleClicks = 0
                    lastClickTime = now
                    titleClicks++
                    if (titleClicks >= 4) {
                        titleClicks = 0
                        showDebug = true
                    }
                }
            )
            when {
                loading            -> LoadingPane()
                fetchError != null -> ErrorPane(fetchError!!) { refreshKey++ }
                else               -> QuestList(
                    displayed, token, region, superProps, webLoader,
                    onUpdate = { upd -> val i = states.indexOfFirst { it.quest.id == upd.quest.id }; if (i >= 0) states[i] = upd },
                    onWatch  = { videoQuest = it },
                    onMore   = { moreQuest  = it }
                )
            }
        }
        if (showFilters) FiltersSheet(
            sortMode = sortMode, fOrbs = fOrbs, fDecor = fDecor, fInGame = fInGame, fWatch = fWatch, fPlay = fPlay,
            onApply   = { sm, o, d, ig, w, p -> sortMode = sm; fOrbs = o; fDecor = d; fInGame = ig; fWatch = w; fPlay = p; showFilters = false },
            onReset   = { sortMode = 0; fOrbs = false; fDecor = false; fInGame = false; fWatch = false; fPlay = false },
            onDismiss = { showFilters = false }
        )
        if (showRegion) RegionSheet(region, onSelect = { region = it; showRegion = false; refreshKey++ }, onDismiss = { showRegion = false })
        if (showCollect) CollectiblesScreen(token, region, superProps, onDismiss = { showCollect = false })
        videoQuest?.let { q ->
            VideoPlayerDialog(q, token, region, superProps,
                onDismiss  = { videoQuest = null },
                onComplete = { upd -> val i = states.indexOfFirst { it.quest.id == upd.quest.id }; if (i >= 0) states[i] = upd; videoQuest = null })
        }
        moreQuest?.let { MoreMenuSheet(it, LocalContext.current, onDismiss = { moreQuest = null }) }

        if (showDebug) {
            DebugScreen(token, region, superProps, onClose = { showDebug = false }, onPropsUpdated = { superProps = it })
        }

        if (showWebView) {
            WebViewDialog(webLoader, onDismiss = { showWebView = false })
        }
    }
}

@Composable
private fun DebugScreen(token: String, region: Region, activeProps: String, onClose: () -> Unit, onPropsUpdated: (String) -> Unit) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    var currentActiveProps by remember { mutableStateOf(activeProps) }
    var customBuildInput by remember { mutableStateOf("") }
    var generatedProps by remember { mutableStateOf("") }
    var testResult by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }
    var copiedField by remember { mutableStateOf("") }
    var logTab by remember { mutableIntStateOf(0) }
    val capturedBuild by capturedBuildNumber
    val capturedProps by capturedSuperProps

    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(DC.Bg)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(DC.Surface).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White) }
                    Text("Debug & Logs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DC.White)
                    Spacer(Modifier.weight(1f))
                    if (capturedBuild > 0) {
                        Box(Modifier.background(DC.Success.copy(0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Build: $capturedBuild", fontSize = 10.sp, color = DC.Success, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    IconButton(onClick = {
                        clearLogsCache(ctx)
                    }) { Icon(Icons.Outlined.DeleteSweep, null, tint = DC.Error, modifier = Modifier.size(20.dp)) }
                }

                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DebugTab("HTTP (${httpHookLogs.size})", logTab == 0) { logTab = 0 }
                    DebugTab("Console (${consoleLogs.size})", logTab == 1) { logTab = 1 }
                    DebugTab("System (${questLogs.size})", logTab == 2) { logTab = 2 }
                    DebugTab("SuperProps", logTab == 3) { logTab = 3 }
                }

                when (logTab) {
                    0 -> {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val sb = StringBuilder()
                                httpHookLogs.forEach { log ->
                                    sb.appendLine("${log.timestamp} ${log.method} ${log.url}")
                                    sb.appendLine("Status: ${log.responseStatus}")
                                    if (log.requestHeaders.isNotEmpty()) sb.appendLine("Request Headers:\n${log.requestHeaders}")
                                    if (log.requestBody != null) sb.appendLine("Request Body: ${log.requestBody}")
                                    if (log.responseHeaders.isNotEmpty()) sb.appendLine("Response Headers:\n${log.responseHeaders}")
                                    if (log.responseBody.isNotEmpty()) sb.appendLine("Response Body:\n${log.responseBody.take(5000)}")
                                    sb.appendLine("---")
                                }
                                if (sb.length > 400_000) {
                                    sb.setLength(400_000)
                                    sb.append("\n\n... LOGS TRUNCATED DUE TO SIZE LIMIT ...")
                                }
                                clipboard.setPrimaryClip(ClipData.newPlainText("HTTP Logs", sb.toString()))
                                copiedField = "http"
                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.CardAlt)) { Text("Copy All") }
                            Button(onClick = {
                                saveLogsToCache(ctx)
                                copiedField = "saved"
                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Save Cache") }
                        }
                        if (copiedField == "http") Text("HTTP logs copied!", color = DC.Success, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                        if (copiedField == "saved") Text("Logs saved to cache!", color = DC.Success, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))

                        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(httpHookLogs) { log ->
                                var expanded by remember { mutableStateOf(false) }
                                val statusColor = when {
                                    log.responseStatus in 200..299 -> DC.Success
                                    log.responseStatus in 400..599 -> DC.Error
                                    log.responseStatus == 0 -> DC.Muted
                                    else -> DC.Warning
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DC.Card),
                                    modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                                ) {
                                    Column(Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.background(statusColor.copy(0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Text(log.method, fontSize = 9.sp, color = statusColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(Modifier.width(6.dp))
                                            if (log.responseStatus > 0) {
                                                Text("${log.responseStatus}", fontSize = 10.sp, color = statusColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                Spacer(Modifier.width(6.dp))
                                            }
                                            Text(log.url.take(80), fontSize = 10.sp, color = DC.SubText, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                            Text(log.timestamp, fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                                            Spacer(Modifier.width(4.dp))
                                            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null, tint = DC.Muted, modifier = Modifier.size(14.dp))
                                        }
                                        if (expanded) {
                                            Spacer(Modifier.height(8.dp))
                                            if (log.requestHeaders.isNotEmpty()) {
                                                Text("Request Headers:", fontSize = 9.sp, color = DC.Primary, fontWeight = FontWeight.Bold)
                                                Text(log.requestHeaders, fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace, maxLines = 30, overflow = TextOverflow.Ellipsis)
                                                Spacer(Modifier.height(4.dp))
                                            }
                                            if (log.requestBody != null && log.requestBody.isNotEmpty()) {
                                                Text("Request Body:", fontSize = 9.sp, color = DC.Warning, fontWeight = FontWeight.Bold)
                                                Text(log.requestBody, fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace, maxLines = 20, overflow = TextOverflow.Ellipsis)
                                                Spacer(Modifier.height(4.dp))
                                            }
                                            if (log.responseHeaders.isNotEmpty()) {
                                                Text("Response Headers:", fontSize = 9.sp, color = DC.Success, fontWeight = FontWeight.Bold)
                                                Text(log.responseHeaders, fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace, maxLines = 30, overflow = TextOverflow.Ellipsis)
                                                Spacer(Modifier.height(4.dp))
                                            }
                                            if (log.responseBody.isNotEmpty()) {
                                                Text("Response Body:", fontSize = 9.sp, color = DC.Teal, fontWeight = FontWeight.Bold)
                                                Text(log.responseBody.take(5000), fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace, maxLines = 50, overflow = TextOverflow.Ellipsis)
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                TextButton(onClick = {
                                                    val full = buildString {
                                                        appendLine("${log.timestamp} ${log.method} ${log.url}")
                                                        appendLine("Status: ${log.responseStatus}")
                                                        if (log.requestHeaders.isNotEmpty()) appendLine("Request Headers:\n${log.requestHeaders}")
                                                        if (log.requestBody != null) appendLine("Request Body: ${log.requestBody}")
                                                        if (log.responseHeaders.isNotEmpty()) appendLine("Response Headers:\n${log.responseHeaders}")
                                                        if (log.responseBody.isNotEmpty()) appendLine("Response Body:\n${log.responseBody}")
                                                    }
                                                    clipboard.setPrimaryClip(ClipData.newPlainText("HTTP Log", full))
                                                }, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                                    Text("Copy", fontSize = 10.sp, color = DC.Primary)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val sb = StringBuilder()
                                consoleLogs.forEach { log -> sb.appendLine("[${log.timestamp}] ${log.level}: ${log.message}") }
                                if (sb.length > 400_000) {
                                    sb.setLength(400_000)
                                    sb.append("\n\n... LOGS TRUNCATED DUE TO SIZE LIMIT ...")
                                }
                                clipboard.setPrimaryClip(ClipData.newPlainText("Console Logs", sb.toString()))
                                copiedField = "console"
                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.CardAlt)) { Text("Copy All") }
                            Button(onClick = { saveLogsToCache(ctx) }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Save Cache") }
                        }
                        if (copiedField == "console") Text("Console logs copied!", color = DC.Success, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(consoleLogs) { log ->
                                val levelColor = when (log.level) {
                                    "ERROR" -> DC.Error
                                    "WARN" -> DC.Warning
                                    "SUCCESS" -> DC.Success
                                    "INFO" -> DC.Primary
                                    else -> DC.Muted
                                }
                                Card(colors = CardDefaults.cardColors(containerColor = DC.Card), modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(log.timestamp, fontSize = 9.sp, color = DC.Muted, fontFamily = FontFamily.Monospace)
                                            Spacer(Modifier.width(5.dp))
                                            Box(Modifier.background(levelColor.copy(0.15f), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp, vertical = 1.dp)) {
                                                Text(log.level, fontSize = 8.sp, color = levelColor, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(Modifier.width(5.dp))
                                            Text(log.message, fontSize = 10.sp, color = DC.SubText, fontFamily = FontFamily.Monospace, maxLines = 5, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                val sb = StringBuilder()
                                questLogs.forEach { log ->
                                    sb.appendLine("[${log.timestamp}] ${log.tag}: ${log.message}")
                                    log.detail?.let { sb.appendLine("  Detail: $it") }
                                }
                                if (sb.length > 400_000) {
                                    sb.setLength(400_000)
                                    sb.append("\n\n... LOGS TRUNCATED DUE TO SIZE LIMIT ...")
                                }
                                clipboard.setPrimaryClip(ClipData.newPlainText("System Logs", sb.toString()))
                                copiedField = "system"
                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.CardAlt)) { Text("Copy All") }
                            Button(onClick = { saveLogsToCache(ctx) }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Save Cache") }
                        }
                        if (copiedField == "system") Text("System logs copied!", color = DC.Success, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                        LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(questLogs) { log ->
                                Card(colors = CardDefaults.cardColors(containerColor = DC.Card), modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(8.dp)) {
                                        Text("[${log.timestamp}] ${log.tag}: ${log.message}", color = DC.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        if (log.detail != null) {
                                            Text(log.detail, color = DC.Muted, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 10, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (capturedProps.isNotEmpty()) {
                                Text("Captured from WebView (Build: $capturedBuild):", color = DC.Success, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Card(colors = CardDefaults.cardColors(containerColor = DC.Card)) {
                                    Text(capturedProps, color = DC.SubText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(8.dp).fillMaxWidth())
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = {
                                        clipboard.setPrimaryClip(ClipData.newPlainText("SuperProps", capturedProps))
                                        copiedField = "captured"
                                    }, colors = ButtonDefaults.buttonColors(containerColor = DC.CardAlt)) { Text("Copy Captured") }
                                    Button(onClick = {
                                        currentActiveProps = capturedProps
                                        onPropsUpdated(capturedProps)
                                        saveCachedSuperProps(ctx, capturedProps)
                                        addQuestLog("SuperProps", "Applied captured props from WebView")
                                    }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Use Captured") }
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Text("Active Super Properties in Cache:", color = DC.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Card(colors = CardDefaults.cardColors(containerColor = DC.Card)) {
                                Text(currentActiveProps, color = DC.SubText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(8.dp).fillMaxWidth())
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("SuperProps", currentActiveProps))
                                    copiedField = "ativo"
                                }, colors = ButtonDefaults.buttonColors(containerColor = DC.CardAlt)) { Text("Copy Current") }
                                Button(onClick = {
                                    scope.launch {
                                        currentActiveProps = fetchDynamicSuperProps(ctx, region)
                                        onPropsUpdated(currentActiveProps)
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Fetch from API") }
                            }

                            if (copiedField.isNotEmpty()) {
                                Text("Copied to clipboard!", color = DC.Success, fontSize = 12.sp)
                            }

                            HorizontalDivider(color = DC.Border, modifier = Modifier.padding(vertical = 8.dp))

                            Text("Custom Super Properties Generator:", color = DC.Warning, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            OutlinedTextField(
                                value = customBuildInput,
                                onValueChange = { customBuildInput = it.filter { c -> c.isDigit() } },
                                label = { Text("Client Build Number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = {
                                    scope.launch {
                                        val build = customBuildInput.toIntOrNull()
                                        if (build != null) {
                                            generatedProps = generateSuperPropsJson(region, build)
                                            addQuestLog("SuperProps", "Generated custom for build $build")
                                        }
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary)) { Text("Generate") }
                                Button(onClick = { showWarning = true }, colors = ButtonDefaults.buttonColors(containerColor = DC.Warning)) { Text("Set as Cache") }
                            }

                            if (generatedProps.isNotEmpty()) {
                                Text("Generated Props:", color = DC.Success, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Card(colors = CardDefaults.cardColors(containerColor = DC.Card)) {
                                    Text(generatedProps, color = DC.SubText, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(8.dp).fillMaxWidth())
                                }
                            }

                            if (showWarning) {
                                Card(colors = CardDefaults.cardColors(containerColor = DC.Error.copy(0.2f)), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text("Warning", color = DC.Error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Custom Super Properties may cause Discord to block requests from outdated builds. Continue anyway?", color = DC.SubText, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = {
                                                saveCachedSuperProps(ctx, generatedProps)
                                                currentActiveProps = generatedProps
                                                onPropsUpdated(generatedProps)
                                                showWarning = false
                                                addQuestLog("SuperProps", "Cache replaced with custom value!")
                                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.Warning)) { Text("Set anyway") }
                                            OutlinedButton(onClick = { showWarning = false }) { Text("Cancel") }
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = DC.Border, modifier = Modifier.padding(vertical = 8.dp))

                            Text("Test Request:", color = DC.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        val (list, orbs) = apiFetch(token, region, currentActiveProps, ctx)
                                        testResult = "Success! Quests: ${list.size}, Orbs: $orbs"
                                    } catch (e: Exception) {
                                        testResult = "Error: ${e.message}"
                                    }
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = DC.Success), modifier = Modifier.fillMaxWidth()) { Text("Test Quest Fetch") }
                            Text("Result: $testResult", color = DC.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DebugTab(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) DC.Primary.copy(0.2f) else DC.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = if (selected) DC.Primary else DC.Muted, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun WebViewDialog(webLoader: QuestWebLoader?, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(DC.Bg)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White) }
                    Text("Discord Quest Page", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DC.White, modifier = Modifier.weight(1f))
                    IconButton(onClick = { webLoader?.reload() }) { Icon(Icons.Outlined.Refresh, null, tint = DC.White, modifier = Modifier.size(20.dp)) }
                }
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    webLoader?.getWebView()?.let { wv ->
                        AndroidView(
                            factory = { wv },
                            modifier = Modifier.fillMaxSize()
                        )
                    } ?: run {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                CircularProgressIndicator(color = DC.Primary)
                                Text("Initializing WebView...", color = DC.Muted, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptchaDialog(sitekey: String, rqdata: String, onTokenReceived: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxSize().background(DC.Bg)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White) }
                    Text("Captcha Verification", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DC.White, modifier = Modifier.weight(1f))
                }
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    AndroidView(factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            addJavascriptInterface(object {
                                @JavascriptInterface
                                fun onSuccess(token: String) {
                                    onTokenReceived(token)
                                }
                            }, "AndroidCaptcha")
                            
                            val html = """
                                <html>
                                <head>
                                    <script src="https://js.hcaptcha.com/1/api.js?render=explicit" async defer></script>
                                    <style>body{margin:0;padding:0;display:flex;justify-content:center;align-items:center;height:100vh;background:#1e1f22;}</style>
                                </head>
                                <body>
                                    <div id="hcaptcha-container"></div>
                                    <script>
                                        hcaptcha.render('hcaptcha-container', {
                                            sitekey: '$sitekey',
                                            rqdata: '$rqdata',
                                            callback: function(token) {
                                                AndroidCaptcha.onSuccess(token);
                                            }
                                        });
                                    </script>
                                </body>
                                </html>
                            """.trimIndent()
                            loadDataWithBaseURL("https://discord.com", html, "text/html", "UTF-8", null)
                        }
                    }, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun QuestHeader(
    orbBalance: Int?, region: Region,
    onBack: () -> Unit, onFilter: () -> Unit, onCollect: () -> Unit,
    onRegion: () -> Unit, onRefresh: () -> Unit,
    filterCount: Int, canCompleteAll: Boolean, onCompleteAll: () -> Unit,
    onShowWebView: () -> Unit, webViewReady: Boolean, capturedBuild: Int,
    onTitleClick: () -> Unit
) {
    Box(Modifier.fillMaxWidth().height(240.dp)) {
        AndroidView(
            factory = { ctx ->
                SurfaceView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    holder.addCallback(object : SurfaceHolder.Callback {
                        private var mp: MediaPlayer? = null
                        override fun surfaceCreated(h: SurfaceHolder) {
                            val player = MediaPlayer()
                            mp = player
                            try {
                                player.setDataSource("https://discord.com/assets/7ba7fcf2c4710bb7.webm")
                                player.setDisplay(h); player.isLooping = true; player.setVolume(0f, 0f)
                                player.setOnPreparedListener { it.start() }; player.prepareAsync()
                            } catch (_: Exception) {}
                        }
                        override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hi: Int) {}
                        override fun surfaceDestroyed(h: SurfaceHolder) { mp?.release(); mp = null }
                    })
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(colorStops = arrayOf(0f to Color.Black.copy(0.25f), 0.5f to Color.Black.copy(0.5f), 1f to DC.Bg))))
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp).padding(top = 40.dp, bottom = 16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp).background(Color.Black.copy(0.5f), CircleShape).border(1.dp, Color.White.copy(0.1f), CircleShape)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White, modifier = Modifier.size(18.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    TopBarBtn(Icons.Outlined.Language, onClick = onRegion)
                    Box {
                        TopBarBtn(Icons.Outlined.Tune, onClick = onFilter)
                        if (filterCount > 0) Badge(Modifier.align(Alignment.TopEnd).offset(2.dp, (-2).dp)) { Text("$filterCount") }
                    }
                    TopBarBtn(Icons.Outlined.Inventory2, onClick = onCollect)
                    TopBarBtn(Icons.Outlined.Refresh, onClick = onRefresh)
                    TopBarBtn(
                        if (webViewReady) Icons.Outlined.Visibility else Icons.Outlined.Web,
                        onClick = onShowWebView
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${region.flag}  ${region.label}", fontSize = 11.sp, color = DC.Muted, fontWeight = FontWeight.Medium)
                Text(
                    "Quests",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = DC.White,
                    modifier = Modifier.clickable { onTitleClick() }
                )
                if (capturedBuild > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(6.dp).background(DC.Success, CircleShape))
                        Text("WebView Hooked (Build $capturedBuild)", fontSize = 11.sp, color = DC.Success, fontWeight = FontWeight.Bold)
                    }
                }
                if (orbBalance != null && orbBalance > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Box(Modifier.size(6.dp).background(DC.OrbViolet, CircleShape))
                        Text("$orbBalance Orbs", fontSize = 12.sp, color = DC.OrbViolet, fontWeight = FontWeight.Bold)
                    }
                }
                if (canCompleteAll) {
                    Spacer(Modifier.height(2.dp))
                    Button(onClick = onCompleteAll, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp), modifier = Modifier.height(36.dp)) {
                        Icon(Icons.Outlined.DoneAll, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Complete All", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBarBtn(icon: ImageVector, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp).background(Color.Black.copy(0.5f), RoundedCornerShape(12.dp)).border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))) {
        Icon(icon, null, tint = DC.White, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun QuestList(
    displayed: List<QuestState>, token: String, region: Region, superProps: String, webLoader: QuestWebLoader?,
    onUpdate: (QuestState) -> Unit, onWatch: (QuestItem) -> Unit, onMore: (QuestItem) -> Unit
) {
    if (displayed.isEmpty()) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(40.dp)) {
                Box(Modifier.size(80.dp).background(DC.Muted.copy(0.08f), CircleShape), Alignment.Center) { Icon(Icons.Outlined.SportsEsports, null, tint = DC.Muted, modifier = Modifier.size(38.dp)) }
                Text("No quests available", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("Try a different region or adjust your filters.", color = DC.Muted, fontSize = 13.sp)
            }
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(Modifier.height(10.dp)) }
        items(displayed, key = { it.quest.id }) { state -> QuestCard(state, token, region, superProps, webLoader, onUpdate, onWatch, onMore) }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuestCard(state: QuestState, token: String, region: Region, superProps: String, webLoader: QuestWebLoader?, onUpdate: (QuestState) -> Unit, onWatch: (QuestItem) -> Unit, onMore: (QuestItem) -> Unit) {
    val ctx   = LocalContext.current
    val q     = state.quest
    val scope = rememberCoroutineScope()
    val accent = when { q.claimedAt != null -> DC.Teal; q.rewardType == "orbs" -> DC.OrbViolet; q.rewardType == "decor" || q.rewardType == "nitro" -> DC.Primary; else -> DC.Success }
    val gifLoader = remember(ctx) { ImageLoader.Builder(ctx).components { if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }
    val pct = if (q.secondsNeeded > 0) (state.progress.coerceAtLeast(q.secondsDone).toFloat() / q.secondsNeeded).coerceIn(0f, 1f) else 0f
    val pulse = rememberInfiniteTransition(label = "p"); val pAlpha by pulse.animateFloat(0.15f, 0.6f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pa")
    val shimT = rememberInfiniteTransition(label = "s"); val shimX by shimT.animateFloat(-350f, 1500f, infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart), label = "sx")
    
    var showCaptcha by remember { mutableStateOf<CaptchaData?>(null) }

    Card(colors = CardDefaults.cardColors(containerColor = DC.Card), shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, accent.copy(if (state.runState == RunState.RUNNING) pAlpha else 0.15f), RoundedCornerShape(18.dp))) {
        Column {
            Box(Modifier.fillMaxWidth().height(150.dp)) {
                if (q.bannerUrl != null) {
                    AsyncImage(ImageRequest.Builder(ctx).data(q.bannerUrl).crossfade(true).build(), null, gifLoader, Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)), contentScale = ContentScale.Crop)
                } else {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)).background(Brush.linearGradient(listOf(accent.copy(0.25f), DC.CardAlt))))
                }
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, DC.Card.copy(0.45f), DC.Card), startY = 55f)))
                if (state.runState == RunState.RUNNING) Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color.Transparent, accent.copy(0.2f), Color.Transparent), startX = shimX, endX = shimX + 400f)))
                if (q.publisher.isNotEmpty()) {
                    Row(Modifier.align(Alignment.TopStart).padding(10.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Outlined.Verified, null, tint = DC.Success, modifier = Modifier.size(9.dp))
                        Text(q.publisher, fontSize = 9.sp, color = DC.White, fontWeight = FontWeight.Bold)
                    }
                }
                Text("Ends ${fmtShort(q.expiresMs)}", modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).background(Color.Black.copy(0.6f), RoundedCornerShape(20.dp)).padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 9.sp, color = DC.White, fontWeight = FontWeight.Bold)
                Box(Modifier.align(Alignment.BottomStart).offset(14.dp, 28.dp).size(60.dp)) {
                    if (q.rewardIconUrl != null) {
                        AsyncImage(ImageRequest.Builder(ctx).data(q.rewardIconUrl).crossfade(true).build(), null, gifLoader, Modifier.size(48.dp).align(Alignment.Center).clip(CircleShape).border(2.dp, DC.Card, CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(Modifier.size(48.dp).align(Alignment.Center).background(DC.CardAlt, CircleShape).border(2.dp, DC.Card, CircleShape)) { Icon(Icons.Outlined.CardGiftcard, null, tint = accent, modifier = Modifier.align(Alignment.Center).size(22.dp)) }
                    }
                    Canvas(Modifier.fillMaxSize()) {
                        val sw = 3.5f; val r = size.minDimension / 2f - sw / 2f
                        drawCircle(color = DC.Border.copy(0.8f), radius = r, style = Stroke(sw))
                        if (pct > 0f) drawArc(color = accent, startAngle = -90f, sweepAngle = 360f * pct, useCenter = false, style = Stroke(sw, cap = StrokeCap.Round))
                    }
                }
            }
            Column(Modifier.padding(start = 14.dp, end = 14.dp, top = 32.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("QUEST: ${q.name.uppercase()}", fontSize = 9.sp, color = accent, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.07.sp)
                Text(q.reward, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(describeTask(q.taskName, q.secondsNeeded), fontSize = 12.sp, color = DC.SubText, lineHeight = 17.sp)
            }
            if (q.secondsNeeded > 0) {
                Column(Modifier.padding(horizontal = 14.dp).padding(top = 6.dp, bottom = 2.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress", fontSize = 9.sp, color = DC.Muted, fontWeight = FontWeight.Medium)
                        Text("${(pct * 100).toInt()}%", fontSize = 9.sp, color = accent, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DC.Border)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct).background(Brush.horizontalGradient(listOf(accent.copy(0.7f), accent)), RoundedCornerShape(2.dp)))
                    }
                }
            }
            if (state.log.isNotBlank() && state.runState != RunState.IDLE) {
                val logColor = when (state.runState) { RunState.ERROR, RunState.NOT_ENROLLED -> DC.Error; RunState.DONE -> accent; RunState.DESKTOP_ONLY -> DC.Warning; else -> DC.Muted }
                Text(state.log, fontSize = 10.sp, color = logColor, fontFamily = FontFamily.Monospace, lineHeight = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp).background(logColor.copy(0.07f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 8.dp))
            }
            HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = DC.Border.copy(0.4f))
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                when {
                    state.runState == RunState.RUNNING -> {
                        Box(Modifier.weight(1f).height(46.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(0.12f)).border(1.dp, accent.copy(pAlpha), RoundedCornerShape(12.dp))
                            .drawWithContent { drawContent(); drawRect(Brush.horizontalGradient(listOf(Color.Transparent, accent.copy(0.22f), Color.Transparent), startX = shimX, endX = shimX + 300f), size = size) }, Alignment.Center) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(13.dp), color = accent, strokeWidth = 2.dp)
                                Text(state.log.lines().lastOrNull()?.take(28) ?: "Running...", fontSize = 11.sp, color = accent, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    state.runState == RunState.DONE -> {
                        StateChip(accent, Icons.Outlined.CheckCircle, "Completed!", Modifier.weight(1f))
                        if (q.claimedAt == null) {
                            SecondaryBtn("Claim", Icons.Outlined.Redeem, Modifier.weight(0.5f)) {
                                scope.launch {
                                    try {
                                        val (res, cap) = claimReward(token, region, superProps, q)
                                        if (cap != null) {
                                            showCaptcha = cap
                                        } else {
                                            onUpdate(state.copy(runState = RunState.DONE, quest = q.copy(claimedAt = res.optString("claimed_at")), log = "Reward claimed successfully!"))
                                        }
                                    } catch (e: Exception) {
                                        onUpdate(state.copy(log = "Claim error: ${e.message}"))
                                    }
                                }
                            }
                        }
                    }
                    state.runState == RunState.DESKTOP_ONLY -> StateChip(DC.Warning, Icons.Outlined.Computer, "Desktop Only", Modifier.weight(1f))
                    state.runState == RunState.NOT_ENROLLED -> StateChip(DC.Warning, Icons.Outlined.Info, "Accept in Discord", Modifier.weight(1f))
                    else -> {
                        val isVideo = q.taskName.contains("WATCH")
                        var showCompleteMenu by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(if (isVideo && q.videoUrl != null) 0.55f else 1f)) {
                            PrimaryBtn("Auto Complete", accent, Icons.Outlined.PlayArrow, Modifier.fillMaxSize(), shimX) {
                                showCompleteMenu = true
                            }
                            DropdownMenu(
                                expanded = showCompleteMenu,
                                onDismissRequest = { showCompleteMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Native API") },
                                    onClick = {
                                        showCompleteMenu = false
                                        scope.launch(Dispatchers.IO) { runComplete(token, region, superProps, state, onUpdate) }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("JS Hook (WebView)") },
                                    enabled = webLoader != null && webViewReady.value,
                                    onClick = {
                                        showCompleteMenu = false
                                        webLoader?.let { loader ->
                                            loader.executeQuestJS(q.id)
                                            addQuestLog("JS", "Quest completion JS injected for ${q.id}")
                                            onUpdate(state.copy(runState = RunState.RUNNING, log = "JS Hook running..."))
                                            
                                            scope.launch {
                                                while (isActive) {
                                                    delay(5000)
                                                    loader.executeCustomJS("""
                                                        (function() {
                                                            try {
                                                                var wpRequire = webpackChunkdiscord_app.push([[Symbol()], {}, r => r]);
                                                                webpackChunkdiscord_app.pop();
                                                                var QuestsStore = Object.values(wpRequire.c).find(x => x?.exports?.A?.__proto__?.getQuest)?.exports.A;
                                                                var quest = QuestsStore.quests.get('${q.id}');
                                                                if (!quest) return JSON.stringify({error: "not found"});
                                                                var taskConfig = quest.config.taskConfig || quest.config.taskConfigV2;
                                                                var taskName = Object.keys(taskConfig.tasks)[0];
                                                                var prog = quest.userStatus && quest.userStatus.progress && quest.userStatus.progress[taskName] ? quest.userStatus.progress[taskName].value : 0;
                                                                var completed = quest.userStatus && quest.userStatus.completed_at != null;
                                                                return JSON.stringify({progress: prog, completed: completed, needed: taskConfig.tasks[taskName].target});
                                                            } catch(e) { return JSON.stringify({error: e.message}); }
                                                        })();
                                                    """.trimIndent()) { res ->
                                                        if (res != "null" && res.isNotEmpty()) {
                                                            try {
                                                                val cleanRes = res.removeSurrounding("\"").replace("\\\"", "\"").replace("\\\\", "\\")
                                                                if (cleanRes != "null" && cleanRes.isNotEmpty()) {
                                                                    val json = JSONObject(cleanRes)
                                                                    if (json.has("progress")) {
                                                                        val p = json.getLong("progress")
                                                                        val n = json.getLong("needed")
                                                                        val c = json.getBoolean("completed")
                                                                        if (c || p >= n) {
                                                                            onUpdate(state.copy(runState = RunState.DONE, progress = n, log = "Completed via JS!"))
                                                                            this.cancel()
                                                                        } else {
                                                                            onUpdate(state.copy(progress = p, log = "JS Progress: $p/$n s"))
                                                                        }
                                                                    }
                                                                }
                                                            } catch (_: Exception) {}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        if (isVideo && q.videoUrl != null) SecondaryBtn("Watch", Icons.Outlined.Videocam, Modifier.weight(0.45f)) { onWatch(q) }
                    }
                }
                SecondaryIconBtn(Icons.Outlined.MoreVert) { onMore(q) }
            }
        }
    }
    
    showCaptcha?.let { cap ->
        CaptchaDialog(
            sitekey = cap.sitekey,
            rqdata = cap.rqdata,
            onTokenReceived = { tokenCaptcha ->
                scope.launch {
                    try {
                        val (res, _) = claimReward(token, region, superProps, q, cap.copy(token = tokenCaptcha))
                        if (res.has("claimed_at")) {
                            withContext(Dispatchers.Main) {
                                showCaptcha = null
                                onUpdate(state.copy(runState = RunState.DONE, quest = q.copy(claimedAt = res.optString("claimed_at")), log = "Reward claimed successfully!"))
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            showCaptcha = null
                            onUpdate(state.copy(log = "Claim captcha error: ${e.message}"))
                        }
                    }
                }
            },
            onDismiss = { showCaptcha = null }
        )
    }
}

@Composable private fun PrimaryBtn(label: String, color: Color, icon: ImageVector, mod: Modifier, shimX: Float = 0f, onClick: () -> Unit) {
    Box(mod.height(46.dp).clip(RoundedCornerShape(12.dp)).background(Brush.horizontalGradient(listOf(color, color.copy(0.82f)))).drawWithContent { drawContent(); drawRect(Brush.horizontalGradient(listOf(Color.Transparent, Color.White.copy(0.12f), Color.Transparent), startX = shimX, endX = shimX + 240f), size = size) }.clickable(onClick = onClick), Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp)); Text(label, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 12.sp) }
    }
}
@Composable private fun SecondaryBtn(label: String, icon: ImageVector, mod: Modifier, onClick: () -> Unit) {
    Box(mod.height(46.dp).clip(RoundedCornerShape(12.dp)).background(DC.CardAlt).border(1.dp, DC.Border, RoundedCornerShape(12.dp)).clickable(onClick = onClick), Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { Icon(icon, null, tint = DC.SubText, modifier = Modifier.size(13.dp)); Text(label, fontWeight = FontWeight.Bold, color = DC.SubText, fontSize = 12.sp) }
    }
}
@Composable private fun SecondaryIconBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(DC.CardAlt).border(1.dp, DC.Border, RoundedCornerShape(12.dp)).clickable(onClick = onClick), Alignment.Center) { Icon(icon, null, tint = DC.Muted, modifier = Modifier.size(16.dp)) }
}
@Composable private fun StateChip(color: Color, icon: ImageVector, label: String, mod: Modifier) {
    Box(mod.height(46.dp).clip(RoundedCornerShape(12.dp)).background(color.copy(0.1f)).border(1.dp, color.copy(0.2f), RoundedCornerShape(12.dp)), Alignment.Center) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Icon(icon, null, tint = color, modifier = Modifier.size(14.dp)); Text(label, fontWeight = FontWeight.ExtraBold, color = color, fontSize = 13.sp) }
    }
}

@Composable
private fun FiltersSheet(
    sortMode: Int, fOrbs: Boolean, fDecor: Boolean, fInGame: Boolean, fWatch: Boolean, fPlay: Boolean,
    onApply: (Int, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var ts by remember { mutableIntStateOf(sortMode) }; var to by remember { mutableStateOf(fOrbs) }; var td by remember { mutableStateOf(fDecor) }
    var ti by remember { mutableStateOf(fInGame) }; var tw by remember { mutableStateOf(fWatch) }; var tp by remember { mutableStateOf(fPlay) }

    BottomSheet(onDismiss) {
        SheetHandle()
        Text("Filters", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 20.dp))
        SheetSection("Sort by")
        SheetGroup { listOf("Suggestions","Most recent","Expiring soon","Active").forEachIndexed { i, lbl -> RadioRow(lbl, ts == i) { ts = i }; if (i < 3) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f)) } }
        Spacer(Modifier.height(16.dp))
        SheetSection("Rewards")
        SheetGroup {
            CheckRow("Orbs", to) { to = !to }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("Avatar decoration", td) { td = !td }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("In-game rewards", ti) { ti = !ti }
        }
        Spacer(Modifier.height(16.dp))
        SheetSection("Quest type")
        SheetGroup {
            CheckRow("Play", tp) { tp = !tp }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("Watch", tw) { tw = !tw }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { onReset() },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, DC.Border)
            ) { Text("Reset", color = DC.Muted, fontWeight = FontWeight.Bold) }

            Button(onClick = { onApply(ts, to, td, ti, tw, tp) }, modifier = Modifier.weight(2f).height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(26.dp)) { Text("Done", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) }
        }
    }
}

@Composable
private fun RegionSheet(current: Region, onSelect: (Region) -> Unit, onDismiss: () -> Unit) {
    BottomSheet(onDismiss) {
        SheetHandle()
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(40.dp).background(DC.Primary.copy(0.12f), CircleShape), Alignment.Center) { Icon(Icons.Outlined.Language, null, tint = DC.Primary, modifier = Modifier.size(20.dp)) }
            Column { Text("Region", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = DC.White); Text("Quest availability varies by region", fontSize = 11.sp, color = DC.Muted) }
        }
        SheetGroup {
            REGIONS.forEachIndexed { idx, reg ->
                Row(Modifier.fillMaxWidth().clickable { onSelect(reg) }.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(reg.flag, fontSize = 14.sp, color = DC.SubText, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                    Text(reg.label, color = DC.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    if (reg.locale == current.locale && reg.timezone == current.timezone) Icon(Icons.Outlined.Check, null, tint = DC.Primary, modifier = Modifier.size(16.dp))
                }
                if (idx < REGIONS.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun BottomSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.55f)).clickable(onClick = onDismiss)) {
        Column(Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.BottomCenter)
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).background(Color(0xFF1E1F26))
            .clickable(onClick = {}).padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 28.dp),
            content = content)
    }
}

@Composable private fun ColumnScope.SheetHandle() { Box(Modifier.width(40.dp).height(4.dp).background(DC.Muted.copy(0.4f), RoundedCornerShape(2.dp)).align(Alignment.CenterHorizontally).padding(bottom = 0.dp)); Spacer(Modifier.height(14.dp)) }
@Composable private fun SheetSection(label: String) { Text(label.uppercase(), fontSize = 10.sp, color = DC.Muted, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.08.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)) }
@Composable private fun SheetGroup(content: @Composable ColumnScope.() -> Unit) { Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFF17181C)), content = content) }
@Composable private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = DC.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(20.dp).border(2.dp, if (selected) DC.Primary else DC.Muted.copy(0.4f), CircleShape)) { if (selected) Box(Modifier.size(10.dp).background(DC.Primary, CircleShape).align(Alignment.Center)) }
    }
}
@Composable private fun CheckRow(label: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = DC.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(20.dp).clip(RoundedCornerShape(5.dp)).background(if (checked) DC.Primary else Color.Transparent).border(2.dp, if (checked) DC.Primary else DC.Muted.copy(0.4f), RoundedCornerShape(5.dp)), Alignment.Center) {
            if (checked) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(13.dp))
        }
    }
}

@Composable
private fun CollectiblesScreen(token: String, region: Region, superProps: String, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var items   by remember { mutableStateOf(listOf<CollectibleItem>()) }
    val gifLoader = remember(ctx) { ImageLoader.Builder(ctx).components { if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }
    LaunchedEffect(Unit) {
        val arr  = apiCollectibles(token, region, superProps)
        val list = mutableListOf<CollectibleItem>()
        for (i in 0 until arr.length()) {
            try {
                val o = arr.getJSONObject(i); val subs = mutableListOf<CollectibleSub>()
                val ia = o.optJSONArray("items"); if (ia != null) for (j in 0 until ia.length()) { val it = ia.getJSONObject(j); subs.add(CollectibleSub(it.optInt("type"), it.optString("asset"), it.optString("label"))) }
                list.add(CollectibleItem(o.optString("sku_id"), o.optString("name"), o.optString("summary"), o.optInt("type"), o.optInt("purchase_type"), o.optString("purchased_at"), o.optString("expires_at").takeIf { it.isNotEmpty() && it != "null" }, subs))
            } catch (_: Exception) {}
        }
        items = list; loading = false
    }
    Box(Modifier.fillMaxSize().background(DC.Bg)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().background(DC.Surface).padding(horizontal = 16.dp, vertical = 14.dp).padding(top = 32.dp)) {
                IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = DC.White) }
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Collectibles", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = DC.White)
                    if (!loading) Text("${items.size} item${if (items.size != 1) "s" else ""}", fontSize = 11.sp, color = DC.Muted)
                }
            }
            HorizontalDivider(color = DC.Border)
            when {
                loading -> LoadingPane()
                items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Outlined.Inventory2, null, tint = DC.Muted, modifier = Modifier.size(48.dp)); Text("No collectibles yet", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp) } }
                else -> LazyColumn(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items, key = { it.skuId }) { c -> CollectibleCard(c, gifLoader, ctx) }
                    item { Spacer(Modifier.height(28.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CollectibleCard(c: CollectibleItem, gifLoader: ImageLoader, ctx: Context) {
    val typeColor = when (c.type) { 0 -> DC.OrbViolet; 1 -> DC.Primary; 2 -> DC.Warning; 3000 -> DC.Success; else -> DC.Muted }
    val typeName  = when (c.type) { 0 -> "Avatar Decoration"; 1 -> "Profile Effect"; 2 -> "Bundle"; 3000 -> "Badge"; else -> "Collectible" }
    val purchName = when (c.purchaseType) { 1 -> "Direct Purchase"; 10 -> "Quest Reward"; else -> "Gift / Other" }
    val iconUrl   = c.items.firstOrNull()?.let { sub -> val a = sub.asset.takeIf { it.isNotEmpty() && it != "null" } ?: return@let null; when { a.startsWith("http") -> a; sub.type == 0 -> "https://cdn.discordapp.com/avatar-decoration-presets/$a.png?size=160&passthrough=true"; sub.type == 1 -> "https://cdn.discordapp.com/profile-effects/$a.png"; else -> "https://cdn.discordapp.com/collectibles/$a.png" } }
    Card(colors = CardDefaults.cardColors(containerColor = DC.Card), shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().border(1.dp, typeColor.copy(0.16f), RoundedCornerShape(14.dp))) {
        Column {
            Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(56.dp).background(DC.CardAlt, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp))) {
                    if (iconUrl != null) AsyncImage(ImageRequest.Builder(ctx).data(iconUrl).crossfade(true).build(), null, gifLoader, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else Icon(Icons.Outlined.Inventory2, null, tint = DC.Muted, modifier = Modifier.align(Alignment.Center).size(24.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(c.name, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DC.White)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(typeName, fontSize = 10.sp, color = typeColor, fontWeight = FontWeight.Bold, modifier = Modifier.background(typeColor.copy(0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 5.dp, vertical = 2.dp))
                        Text(purchName, fontSize = 10.sp, color = DC.Muted)
                    }
                }
            }
            if (c.summary.isNotEmpty()) { HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = DC.Border); Text(c.summary, fontSize = 11.sp, color = DC.SubText, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) }
            HorizontalDivider(Modifier.padding(horizontal = 14.dp), color = DC.Border)
            
            val purchasedDate = fmtShort(parseIso(c.purchasedAt))
            val expiresDate = if (c.expiresAt != null) fmtShort(parseIso(c.expiresAt)) else null
            val dateText = if (expiresDate != null) "Purchased $purchasedDate  ·  Expires $expiresDate" else "Purchased $purchasedDate"
            Text(dateText, fontSize = 10.sp, color = DC.Muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
        }
    }
}

@Composable
private fun VideoPlayerDialog(quest: QuestItem, token: String, region: Region, superProps: String, onDismiss: () -> Unit, onComplete: (QuestState) -> Unit) {
    val scope = rememberCoroutineScope(); val needed = quest.secondsNeeded
    var spoofDone   by remember { mutableLongStateOf(quest.secondsDone) }
    var log         by remember { mutableStateOf("Preparing video...") }
    var spoofActive by remember { mutableStateOf(false) }
    var completed   by remember { mutableStateOf(false) }
    val pct = if (needed > 0) (spoofDone.toFloat() / needed).coerceIn(0f, 1f) else 0f
    val pulse = rememberInfiniteTransition(label = "vp"); val pA by pulse.animateFloat(0.4f, 1f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "vpa")
    DisposableEffect(Unit) { onDispose { spoofActive = false } }

    Dialog(onDismissRequest = { spoofActive = false; onDismiss() }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth(0.96f).clip(RoundedCornerShape(22.dp)).background(DC.Card).border(1.dp, DC.Border, RoundedCornerShape(22.dp))) {
            Column {
                Box(Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)).background(Color.Black)) {
                    AndroidView(factory = { ctx ->
                        android.widget.VideoView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            setMediaController(android.widget.MediaController(ctx).also { it.setAnchorView(this) })
                            setVideoURI(Uri.parse(quest.videoUrl))
                            setOnPreparedListener { mp ->
                                mp.isLooping = true; mp.start(); log = "Watching & syncing..."; spoofActive = true
                                scope.launch(Dispatchers.IO) {
                                    val ea = quest.enrolledAt
                                    if (ea == null) {
                                        withContext(Dispatchers.Main) { log = "Accept the quest in the Discord app first." }
                                        return@launch
                                    }
                                    var running = true
                                    while (running && spoofActive && spoofDone < needed) {
                                        val remaining = minOf(7L, needed - spoofDone)
                                        delay(remaining * 1000L)
                                        if (!spoofActive) { running = false }

                                        if (running) {
                                            val timestamp = spoofDone + 7L
                                            val sendTs = minOf(needed.toDouble(), timestamp.toDouble() + Math.random())
                                            val bodyStr = JSONObject().put("timestamp", sendTs).toString()
                                            val reqBody = bodyStr.toRequestBody("application/json".toMediaType())

                                            addQuestLog("API", "POST /video-progress (Dialog)", "Payload: $bodyStr")

                                            val rj = try {
                                                val postReq = buildReq(
                                                    "https://discord.com/api/v9/quests/${quest.id}/video-progress",
                                                    token, region, superProps, "https://discord.com/quest-home"
                                                ).post(reqBody).build()
                                                val resp = http.newCall(postReq).execute()
                                                val respBody = resp.body?.string() ?: "{}"
                                                addQuestLog("API", "Response ${resp.code} (Dialog)", respBody.take(2000))
                                                JSONObject(respBody)
                                            } catch (_: Exception) { JSONObject() }

                                            completed = rj.optString("completed_at", "").isNotEmpty()
                                            spoofDone = minOf(needed, timestamp)
                                            if (spoofDone >= needed) completed = true
                                            val p = if (needed > 0) (spoofDone * 100 / needed).toInt() else 0
                                            withContext(Dispatchers.Main) { log = "${spoofDone}s / ${needed}s ($p%)" }

                                            if (completed || spoofDone >= needed) {
                                                if (!completed) {
                                                    try {
                                                        val finalBody = JSONObject().put("timestamp", needed.toDouble()).toString()
                                                        val postReq = buildReq(
                                                            "https://discord.com/api/v9/quests/${quest.id}/video-progress",
                                                            token, region, superProps, "https://discord.com/quest-home"
                                                        ).post(finalBody.toRequestBody("application/json".toMediaType())).build()
                                                        JSONObject(http.newCall(postReq).execute().body?.string() ?: "{}")
                                                    } catch (_: Exception) {}
                                                }
                                                withContext(Dispatchers.Main) { log = "Done! Claim your reward in the Discord app."; completed = true }
                                                running = false
                                            }
                                        }
                                    }
                                }
                            }
                            setOnErrorListener { _, _, _ -> log = "Video unavailable. Use Auto Complete."; false }
                        }
                    }, modifier = Modifier.fillMaxSize())
                }
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(quest.reward, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DC.White)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (spoofActive && !completed) CircularProgressIndicator(modifier = Modifier.size(12.dp).graphicsLayer { alpha = pA }, color = DC.Primary, strokeWidth = 2.dp)
                        else if (completed) Icon(Icons.Outlined.CheckCircle, null, tint = DC.Success, modifier = Modifier.size(13.dp))
                        Text(log, fontSize = 11.sp, color = if (completed) DC.Success else DC.SubText, fontFamily = FontFamily.Monospace)
                    }
                    Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(DC.Border)) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(pct).background(Brush.horizontalGradient(listOf(DC.Primary.copy(0.7f), DC.Primary)), RoundedCornerShape(2.dp)))
                    }
                    OutlinedButton(
                        onClick = { spoofActive = false; onDismiss() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DC.Border)
                    ) {
                        Text("Close", color = DC.Muted, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreMenuSheet(quest: QuestItem, ctx: Context, onDismiss: () -> Unit) {
    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.45f)).clickable(onClick = onDismiss)) {
        Box(Modifier.fillMaxWidth(0.78f).wrapContentHeight().align(Alignment.BottomEnd).offset((-14).dp, (-90).dp)
            .clip(RoundedCornerShape(16.dp)).background(Color(0xFF2B2D35)).clickable(onClick = {})) {
            Column(Modifier.padding(4.dp)) {
                if (quest.questLink != null) { MoreItem("Play now", Icons.Outlined.OpenInBrowser) { try { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(quest.questLink)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }) } catch (_: Exception) {}; onDismiss() }; HorizontalDivider(Modifier.padding(horizontal = 8.dp), color = DC.Border.copy(0.4f)) }
                MoreItem("Why am I seeing this?", Icons.Outlined.HelpOutline) { onDismiss() }
                HorizontalDivider(Modifier.padding(horizontal = 8.dp), color = DC.Border.copy(0.4f))
                MoreItem("Copy link", Icons.Outlined.ContentCopy) { clipboard.setPrimaryClip(ClipData.newPlainText("quest", quest.questLink ?: quest.name)); onDismiss() }
            }
        }
    }
}

@Composable
private fun MoreItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = DC.SubText, modifier = Modifier.size(16.dp))
        Text(label, fontSize = 14.sp, color = DC.White, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LoadingPane() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val inf = rememberInfiniteTransition(label = "ld"); val rot by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(900, easing = LinearEasing)), label = "lr")
            Box(Modifier.size(48.dp).rotate(rot).border(2.5.dp, Brush.sweepGradient(listOf(DC.Primary, Color.Transparent)), CircleShape))
            Text("Loading quests...", color = DC.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ErrorPane(msg: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(64.dp).background(DC.Error.copy(0.1f), CircleShape), Alignment.Center) { Icon(Icons.Outlined.ErrorOutline, null, tint = DC.Error, modifier = Modifier.size(32.dp)) }
            Text("Failed to load quests", color = DC.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            Text(msg, color = DC.Muted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = DC.Primary), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Outlined.Refresh, null, modifier = Modifier.size(15.dp)); Spacer(Modifier.width(6.dp)); Text("Try Again", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        }
    }
}

private fun describeTask(name: String, seconds: Long): String {
    val dur = if (seconds < 60) "${seconds}s" else {
        val m = seconds / 60
        if (m < 60) "${m}min" else "${m/60}h${if (m % 60 > 0) " ${m%60}min" else ""}"
    }
    return when (name) {
        "WATCH_VIDEO", "WATCH_VIDEO_ON_MOBILE" -> "Watch a video for $dur"
        "PLAY_ACTIVITY"                        -> "Play an activity for $dur"
        "PLAY_ON_DESKTOP", "PLAY_ON_DESKTOP_V2"-> "Play on Desktop for $dur"
        "STREAM_ON_DESKTOP"                    -> "Stream on Desktop for $dur"
        "PLAY_ON_XBOX"                         -> "Play on Xbox for $dur"
        "PLAY_ON_PLAYSTATION"                  -> "Play on PlayStation for $dur"
        else -> name
    }
}
