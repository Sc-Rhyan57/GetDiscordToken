package com.discordtokenget

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

private val http = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

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

data class Region(val label: String, val flag: String, val locale: String, val timezone: String, val superProps: String)

private val REGIONS = listOf(
    Region(
        "Brazil", "BR", "pt-BR", "America/Sao_Paulo",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6InB0LUJSIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "United States", "US", "en-US", "America/New_York",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVuLVVTIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "United Kingdom", "GB", "en-GB", "Europe/London",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVuLUdCIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "France", "FR", "fr", "Europe/Paris",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImZyIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Germany", "DE", "de", "Europe/Berlin",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImRlIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Japan", "JP", "ja", "Asia/Tokyo",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImphIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "South Korea", "KR", "ko", "Asia/Seoul",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImtvIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Canada", "CA", "en-US", "America/Toronto",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVuLVVTIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Australia", "AU", "en-AU", "Australia/Sydney",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVuLUFVIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Poland", "PL", "pl", "Europe/Warsaw",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6InBsIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Turkey", "TR", "tr", "Europe/Istanbul",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6InRyIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Russia", "RU", "ru", "Europe/Moscow",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6InJ1IiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "India", "IN", "en-IN", "Asia/Kolkata",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVuLUlOIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
    Region(
        "Mexico", "MX", "es-419", "America/Mexico_City",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVzLTQxOSIsImhhc19jbGllbnRfbW9kcyI6ZmFsc2UsImJyb3dzZXJfdXNlcl9hZ2VudCI6Ik1vemlsbGEvNS4wIChXaW5kb3dzIE5UIDEwLjA7IFdpbjY0OyB4NjQpIEFwcGxlV2ViS2l0LzUzNy4zNiAoS0hUTUwsIGxpa2UgR2Vja28pIENocm9tZS8xNDcuMC4wLjAgU2FmYXJpLzUzNy4zNiIsImJyb3dzZXJfdmVyc2lvbiI6IjE0Ny4wLjAuMCIsIm9zX3ZlcnNpb24iOiIxMCIsInJlZmVycmVyIjoiIiwicmVmZXJyaW5nX2RvbWFpbiI6IiIsInJlZmVycmVyX2N1cnJlbnQiOiJodHRwczovL2Rpc2NvcmQuY29tLyIsInJlZmVycmluZ19kb21haW5fY3VycmVudCI6ImRpc2NvcmQuY29tIiwicmVsZWFzZV9jaGFubmVsIjoic3RhYmxlIiwiY2xpZW50X2J1aWxkX251bWJlciI6NTM5OTUxLCJjbGllbnRfZXZlbnRfc291cmNlIjpudWxsfQ=="
    ),
    Region(
        "Spain", "ES", "es-ES", "Europe/Madrid",
        "eyJvcyI6IldpbmRvd3MiLCJicm93c2VyIjoiQ2hyb21lIiwiZGV2aWNlIjoiIiwic3lzdGVtX2xvY2FsZSI6ImVzLUVTIiwiaGFzX2NsaWVudF9tb2RzIjpmYWxzZSwiYnJvd3Nlcl91c2VyX2FnZW50IjoiTW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Ny4wLjAuMCBTYWZhcmkvNTM3LjM2IiwiYnJvd3Nlcl92ZXJzaW9uIjoiMTQ3LjAuMC4wIiwib3NfdmVyc2lvbiI6IjEwIiwicmVmZXJyZXIiOiIiLCJyZWZlcnJpbmdfZG9tYWluIjoiIiwicmVmZXJyZXJfY3VycmVudCI6Imh0dHBzOi8vZGlzY29yZC5jb20vIiwicmVmZXJyaW5nX2RvbWFpbl9jdXJyZW50IjoiZGlzY29yZC5jb20iLCJyZWxlYXNlX2NoYW5uZWwiOiJzdGFibGUiLCJjbGllbnRfYnVpbGRfbnVtYmVyIjo1Mzk5NTEsImNsaWVudF9ldmVudF9zb3VyY2UiOm51bGx9"
    ),
)

private fun buildReq(url: String, token: String, region: Region, referer: String = "https://discord.com/quest-home") =
    Request.Builder().url(url).apply {
        header("Authorization",         token)
        header("Content-Type",          "application/json")
        header("Accept",                "*/*")
        header("Accept-Language",       "${region.locale};q=0.9,en;q=0.8")
        header("Accept-Encoding",       "gzip, deflate, br, zstd")
        header("User-Agent",            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36")
        header("X-Super-Properties",    region.superProps)
        header("X-Discord-Locale",      region.locale)
        header("X-Discord-Timezone",    region.timezone)
        header("X-Debug-Options",       "bugReporterEnabled")
        header("Referer",               referer)
        header("Origin",                "https://discord.com")
        header("Sec-Ch-Ua",             "\"Google Chrome\";v=\"147\", \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"147\"")
        header("Sec-Ch-Ua-Mobile",      "?0")
        header("Sec-Ch-Ua-Platform",    "\"Windows\"")
        header("Sec-Fetch-Dest",        "empty")
        header("Sec-Fetch-Mode",        "cors")
        header("Sec-Fetch-Site",        "same-origin")
        header("Priority",              "u=1, i")
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

private suspend fun apiFetch(token: String, region: Region): Pair<List<QuestItem>, Int?> = withContext(Dispatchers.IO) {
    val now  = System.currentTimeMillis()
    val resp = http.newCall(
        buildReq("https://discord.com/api/v9/quests/@me", token, region, "https://discord.com/quest-home?tab=all").build()
    ).execute()
    val body = resp.body?.string() ?: ""
    if (!resp.isSuccessful) throw Exception(try { JSONObject(body).optString("message", "HTTP ${resp.code}") } catch (_: Exception) { "HTTP ${resp.code}" })
    val parsed = try { JSONObject(body) } catch (_: Exception) { JSONObject() }
    val arr = parsed.optJSONArray("quests")
        ?: try { JSONArray(body) } catch (_: Exception) { JSONArray() }
    val list = mutableListOf<QuestItem>()
    for (i in 0 until arr.length()) {
        val item = parseQuest(arr.getJSONObject(i)) ?: continue
        if (item.expiresMs > 0 && item.expiresMs < now) continue
        if (item.claimedAt != null) continue
        list.add(item)
    }
    val orbBody = try {
        http.newCall(
            buildReq("https://discord.com/api/v9/users/@me/virtual-currency/balance", token, region).build()
        ).execute().body?.string() ?: "{}"
    } catch (_: Exception) { "{}" }
    list to try { JSONObject(orbBody).optInt("balance", -1).takeIf { it >= 0 } } catch (_: Exception) { null }
}

private suspend fun apiGetStatus(token: String, questId: String, region: Region): JSONObject = withContext(Dispatchers.IO) {
    try {
        JSONObject(
            http.newCall(
                buildReq("https://discord.com/api/v9/quests/@me/$questId", token, region).build()
            ).execute().body?.string() ?: "{}"
        )
    } catch (_: Exception) { JSONObject() }
}

private suspend fun apiFirstDm(token: String, region: Region): String? = withContext(Dispatchers.IO) {
    try {
        val arr = JSONArray(
            http.newCall(
                buildReq("https://discord.com/api/v9/users/@me/channels", token, region).build()
            ).execute().body?.string() ?: "[]"
        )
        if (arr.length() > 0) arr.getJSONObject(0).optString("id").takeIf { it.isNotEmpty() } else null
    } catch (_: Exception) { null }
}

private suspend fun apiCollectibles(token: String, region: Region): JSONArray = withContext(Dispatchers.IO) {
    try {
        JSONArray(
            http.newCall(
                buildReq("https://discord.com/api/v9/users/@me/collectibles-purchases", token, region).build()
            ).execute().body?.string() ?: "[]"
        )
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

private suspend fun runComplete(token: String, region: Region, state: QuestState, onUpdate: (QuestState) -> Unit) {
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

                while (true) {
                    val remaining = minOf(7L, needed - done)
                    delay(remaining * 1000L)

                    val timestamp = done + 7L
                    val sendTs = minOf(needed.toDouble(), timestamp.toDouble() + Math.random())
                    val body = JSONObject().put("timestamp", sendTs)
                        .toString().toRequestBody("application/json".toMediaType())
                    val rj = try {
                        retryApi {
                            JSONObject(
                                http.newCall(
                                    buildReq(
                                        "https://discord.com/api/v9/quests/$questId/video-progress",
                                        token, region, "https://discord.com/quest-home"
                                    ).post(body).build()
                                ).execute().body?.string() ?: "{}"
                            )
                        }
                    } catch (e: Exception) {
                        upd("Error: ${e.message}", rs = RunState.ERROR)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        return
                    }

                    completed = rj.optString("completed_at", "").isNotEmpty()
                    done = minOf(needed, timestamp)

                    upd("Video: ${done}s / ${needed}s (${if (needed > 0) done * 100 / needed else 0}%)", done)
                    withContext(Dispatchers.Main) { onUpdate(cur) }

                    if (timestamp >= needed) break
                }

                if (!completed) {
                    try {
                        retryApi {
                            JSONObject(
                                http.newCall(
                                    buildReq(
                                        "https://discord.com/api/v9/quests/$questId/video-progress",
                                        token, region, "https://discord.com/quest-home"
                                    ).post(
                                        JSONObject().put("timestamp", needed.toDouble())
                                            .toString().toRequestBody("application/json".toMediaType())
                                    ).build()
                                ).execute().body?.string() ?: "{}"
                            )
                        }
                    } catch (_: Exception) {}
                }

                done = needed
                upd("Completed! Claim your reward in the Discord app.", done, RunState.DONE)
                withContext(Dispatchers.Main) { onUpdate(cur) }
            }

            "PLAY_ACTIVITY" -> {
                val channelId = apiFirstDm(token, region) ?: throw Exception("No DM channel found.")
                val streamKey = "call:$channelId:1"
                upd("Syncing activity progress...", done)
                withContext(Dispatchers.Main) { onUpdate(cur) }

                while (true) {
                    val rb = JSONObject().put("stream_key", streamKey).put("terminal", false)
                        .toString().toRequestBody("application/json".toMediaType())
                    val rj = try {
                        retryApi {
                            JSONObject(
                                http.newCall(
                                    buildReq(
                                        "https://discord.com/api/v9/quests/$questId/heartbeat",
                                        token, region
                                    ).post(rb).build()
                                ).execute().body?.string() ?: "{}"
                            )
                        }
                    } catch (e: Exception) {
                        upd("Error: ${e.message}", rs = RunState.ERROR)
                        withContext(Dispatchers.Main) { onUpdate(cur) }
                        return
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
                                JSONObject(
                                    http.newCall(
                                        buildReq(
                                            "https://discord.com/api/v9/quests/$questId/heartbeat",
                                            token, region
                                        ).post(
                                            JSONObject().put("stream_key", streamKey).put("terminal", true)
                                                .toString().toRequestBody("application/json".toMediaType())
                                        ).build()
                                    ).execute().body?.string() ?: "{}"
                                )
                            }
                        } catch (_: Exception) {}
                        break
                    }
                    delay(20_000L)
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
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val token = intent.getStringExtra(EXTRA_TOKEN) ?: run { finish(); return }
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(primary = DC.Primary, background = DC.Bg, surface = DC.Surface)) {
                Surface(Modifier.fillMaxSize(), color = DC.Bg) {
                    val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    var tosAcked by remember { mutableStateOf(prefs.getBoolean("tos_ack", false)) }
                    if (!tosAcked) TosDialog(
                        onAccept  = { prefs.edit().putBoolean("tos_ack", true).apply(); tosAcked = true },
                        onDecline = { finish() }
                    ) else QuestScreen(token, onBack = { finish() })
                }
            }
        }
    }
}

@Composable
private fun TosDialog(onAccept: () -> Unit, onDecline: () -> Unit) {
    Dialog(onDismissRequest = onDecline, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.fillMaxWidth(0.92f).clip(RoundedCornerShape(20.dp)).background(DC.Card).border(1.dp, DC.Warning.copy(0.3f), RoundedCornerShape(20.dp)).padding(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(DC.Warning.copy(0.12f), CircleShape), Alignment.Center) {
                        Icon(Icons.Outlined.Warning, null, tint = DC.Warning, modifier = Modifier.size(22.dp))
                    }
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
private fun QuestScreen(token: String, onBack: () -> Unit) {
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
    var sortMode    by remember { mutableIntStateOf(0) }
    var fOrbs       by remember { mutableStateOf(false) }
    var fDecor      by remember { mutableStateOf(false) }
    var fInGame     by remember { mutableStateOf(false) }
    var fWatch      by remember { mutableStateOf(false) }
    var fPlay       by remember { mutableStateOf(false) }
    val states      = remember { mutableStateListOf<QuestState>() }
    val ctx         = LocalContext.current

    LaunchedEffect(refreshKey, region) {
        loading = true; fetchError = null; states.clear(); orbBalance = null
        try {
            val (list, orbs) = apiFetch(token, region)
            states.addAll(list.map { q ->
                val rs = when {
                    q.completedAt != null && q.claimedAt == null -> RunState.DONE
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
        } catch (e: Exception) { fetchError = e.message }
        loading = false
    }

    var displayed = states.toList()
    if (fOrbs)   displayed = displayed.filter { it.quest.rewardType == "orbs" }
    if (fDecor)  displayed = displayed.filter { it.quest.rewardType == "decor" }
    if (fInGame) displayed = displayed.filter { it.quest.rewardType != "orbs" && it.quest.rewardType != "decor" }
    if (fWatch)  displayed = displayed.filter { it.quest.taskName.contains("WATCH") }
    if (fPlay)   displayed = displayed.filter { it.quest.taskName.contains("PLAY") || it.quest.taskName.contains("STREAM") }
    displayed = when (sortMode) {
        1    -> displayed.sortedByDescending { it.quest.expiresMs }
        2    -> displayed.sortedBy { it.quest.expiresMs }
        3    -> displayed.filter { it.quest.enrolledAt != null }
        else -> displayed
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
                                runComplete(token, region, st) { upd -> if (idx < states.size) states[idx] = upd }
                        }
                    }
                }
            )
            when {
                loading            -> LoadingPane()
                fetchError != null -> ErrorPane(fetchError!!) { refreshKey++ }
                else               -> QuestList(
                    displayed, token, region,
                    onUpdate = { upd -> val i = states.indexOfFirst { it.quest.id == upd.quest.id }; if (i >= 0) states[i] = upd },
                    onWatch  = { videoQuest = it },
                    onMore   = { moreQuest  = it }
                )
            }
        }
        if (showFilters) FiltersSheet(sortMode, fOrbs, fDecor, fInGame, fWatch, fPlay,
            onApply   = { sm, o, d, ig, w, p -> sortMode = sm; fOrbs = o; fDecor = d; fInGame = ig; fWatch = w; fPlay = p; showFilters = false },
            onReset   = { sortMode = 0; fOrbs = false; fDecor = false; fInGame = false; fWatch = false; fPlay = false; showFilters = false },
            onDismiss = { showFilters = false })
        if (showRegion) RegionSheet(region, onSelect = { region = it; showRegion = false; refreshKey++ }, onDismiss = { showRegion = false })
        if (showCollect) CollectiblesScreen(token, region, onDismiss = { showCollect = false })
        videoQuest?.let { q ->
            VideoPlayerDialog(q, token, region,
                onDismiss  = { videoQuest = null },
                onComplete = { upd -> val i = states.indexOfFirst { it.quest.id == upd.quest.id }; if (i >= 0) states[i] = upd; videoQuest = null })
        }
        moreQuest?.let { MoreMenuSheet(it, ctx, onDismiss = { moreQuest = null }) }
    }
}

@Composable
private fun QuestHeader(
    orbBalance: Int?, region: Region,
    onBack: () -> Unit, onFilter: () -> Unit, onCollect: () -> Unit,
    onRegion: () -> Unit, onRefresh: () -> Unit,
    filterCount: Int, canCompleteAll: Boolean, onCompleteAll: () -> Unit
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
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${region.flag}  ${region.label}", fontSize = 11.sp, color = DC.Muted, fontWeight = FontWeight.Medium)
                Text("Quests", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = DC.White)
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
    displayed: List<QuestState>, token: String, region: Region,
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
        items(displayed, key = { it.quest.id }) { state -> QuestCard(state, token, region, onUpdate, onWatch, onMore) }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuestCard(state: QuestState, token: String, region: Region, onUpdate: (QuestState) -> Unit, onWatch: (QuestItem) -> Unit, onMore: (QuestItem) -> Unit) {
    val ctx   = LocalContext.current
    val q     = state.quest
    val scope = rememberCoroutineScope()
    val accent = when { q.claimedAt != null -> DC.Teal; q.rewardType == "orbs" -> DC.OrbViolet; q.rewardType == "decor" || q.rewardType == "nitro" -> DC.Primary; else -> DC.Success }
    val gifLoader = remember(ctx) { ImageLoader.Builder(ctx).components { if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }
    val pct = if (q.secondsNeeded > 0) (state.progress.coerceAtLeast(q.secondsDone).toFloat() / q.secondsNeeded).coerceIn(0f, 1f) else 0f
    val pulse = rememberInfiniteTransition(label = "p"); val pAlpha by pulse.animateFloat(0.15f, 0.6f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "pa")
    val shimT = rememberInfiniteTransition(label = "s"); val shimX by shimT.animateFloat(-350f, 1500f, infiniteRepeatable(tween(1900, easing = LinearEasing), RepeatMode.Restart), label = "sx")

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
                    state.runState == RunState.DONE -> StateChip(accent, Icons.Outlined.CheckCircle, "Completed!", Modifier.weight(1f))
                    state.runState == RunState.DESKTOP_ONLY -> StateChip(DC.Warning, Icons.Outlined.Computer, "Desktop Only", Modifier.weight(1f))
                    state.runState == RunState.NOT_ENROLLED -> StateChip(DC.Warning, Icons.Outlined.Info, "Accept in Discord", Modifier.weight(1f))
                    else -> {
                        val isVideo = q.taskName.contains("WATCH")
                        PrimaryBtn("Auto Complete", accent, Icons.Outlined.PlayArrow, Modifier.weight(if (isVideo && q.videoUrl != null) 0.55f else 1f), shimX) {
                            scope.launch(Dispatchers.IO) { runComplete(token, region, state, onUpdate) }
                        }
                        if (isVideo && q.videoUrl != null) SecondaryBtn("Watch", Icons.Outlined.Videocam, Modifier.weight(0.45f)) { onWatch(q) }
                    }
                }
                SecondaryIconBtn(Icons.Outlined.MoreVert) { onMore(q) }
            }
        }
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
private fun FiltersSheet(sortMode: Int, fOrbs: Boolean, fDecor: Boolean, fInGame: Boolean, fWatch: Boolean, fPlay: Boolean,
    onApply: (Int, Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit, onReset: () -> Unit, onDismiss: () -> Unit) {
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
            CheckRow("Orbs", to) { to = !to }; HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("Avatar decoration", td) { td = !td }; HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("In-game rewards", ti) { ti = !ti }
        }
        Spacer(Modifier.height(16.dp))
        SheetSection("Quest type")
        SheetGroup {
            CheckRow("Play", tp) { tp = !tp }; HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DC.Border.copy(0.4f))
            CheckRow("Watch", tw) { tw = !tw }
        }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(26.dp), border = BorderStroke(1.dp, DC.Border)) { Text("Reset", color = DC.Muted, fontWeight = FontWeight.Bold) }
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
private fun CollectiblesScreen(token: String, region: Region, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    var loading by remember { mutableStateOf(true) }
    var items   by remember { mutableStateOf(listOf<CollectibleItem>()) }
    val gifLoader = remember(ctx) { ImageLoader.Builder(ctx).components { if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }
    LaunchedEffect(Unit) {
        val arr  = apiCollectibles(token, region)
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
            Text("Purchased ${fmtShort(parseIso(c.purchasedAt))}${if (c.expiresAt != null) "  ·  Expires ${fmtShort(parseIso(c.expiresAt))}" else ""}", fontSize = 10.sp, color = DC.Muted, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
        }
    }
}

@Composable
private fun VideoPlayerDialog(quest: QuestItem, token: String, region: Region, onDismiss: () -> Unit, onComplete: (QuestState) -> Unit) {
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
                                    while (spoofActive && spoofDone < needed) {
                                        val remaining = minOf(7L, needed - spoofDone)
                                        delay(remaining * 1000L)
                                        if (!spoofActive) break

                                        val timestamp = spoofDone + 7L
                                        val sendTs = minOf(needed.toDouble(), timestamp.toDouble() + Math.random())
                                        val body = JSONObject().put("timestamp", sendTs)
                                            .toString().toRequestBody("application/json".toMediaType())
                                        val rj = try {
                                            JSONObject(
                                                http.newCall(
                                                    buildReq(
                                                        "https://discord.com/api/v9/quests/${quest.id}/video-progress",
                                                        token, region, "https://discord.com/quest-home"
                                                    ).post(body).build()
                                                ).execute().body?.string() ?: "{}"
                                            )
                                        } catch (_: Exception) { JSONObject() }

                                        completed = rj.optString("completed_at", "").isNotEmpty()
                                        spoofDone = minOf(needed, timestamp)
                                        val p = if (needed > 0) (spoofDone * 100 / needed).toInt() else 0
                                        withContext(Dispatchers.Main) { log = "${spoofDone}s / ${needed}s ($p%)" }

                                        if (completed || spoofDone >= needed) {
                                            if (!completed) {
                                                try {
                                                    JSONObject(
                                                        http.newCall(
                                                            buildReq(
                                                                "https://discord.com/api/v9/quests/${quest.id}/video-progress",
                                                                token, region, "https://discord.com/quest-home"
                                                            ).post(
                                                                JSONObject().put("timestamp", needed.toDouble())
                                                                    .toString().toRequestBody("application/json".toMediaType())
                                                            ).build()
                                                        ).execute().body?.string() ?: "{}"
                                                    )
                                                } catch (_: Exception) {}
                                            }
                                            withContext(Dispatchers.Main) { log = "Done! Claim your reward in the Discord app."; completed = true }
                                            break
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
    val m = seconds / 60; val dur = if (m < 60) "${m}min" else "${m/60}h${if (m % 60 > 0) " ${m%60}min" else ""}"
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
}
