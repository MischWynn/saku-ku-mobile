package com.example.sakuku

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.sakuku.navigation.AppNavigation
import com.example.sakuku.ui.theme.SakukuTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // App ini selalu tema gelap, jadi status bar & navigation bar (tombol home/back/gesture
        // pill) dipaksa transparan + ikon TERANG - tanpa ini, kalau HP-nya mode terang, Android
        // ngasih ikon gelap di atas background app yang gelap (nyaris gak kelihatan) atau nempel
        // scrim putih di bawah layar.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            SakukuTheme {
                Surface {
                    RequestNotificationPermission()
                    AppNavigation()
                }
            }
        }
    }
}

// Android 13+ (TIRAMISU) wajib minta izin POST_NOTIFICATIONS eksplisit di runtime - beda dari
// permission lain yang otomatis granted dari install. Tanpa ini, AndroidAppNotifier.canPost()
// selalu false dan push FCM yang diterima gak pernah nongol di tray notifikasi (onMessageReceived
// tetap jalan, cuma notifikasi sistemnya yang di-skip diam-diam). Diminta sekali di awal app
// dibuka, bukan nunggu 1 fitur spesifik - notifikasi relevan buat semua role/state (guest
// maupun logged-in).
@Composable
private fun RequestNotificationPermission() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
