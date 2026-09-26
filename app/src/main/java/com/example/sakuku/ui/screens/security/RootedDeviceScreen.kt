package com.example.sakuku.ui.screens.security

import androidx.compose.foundation.layout.systemBarsPadding
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground

// Terminal screen - the only route left on the back stack once Splash detects root (popUpTo
// splash inclusive), so there's nothing meaningful to navigate back to. BackHandler swallows the
// back gesture instead of letting it fall through to whatever Activity default would apply,
// so the only way out is the explicit "Tutup Aplikasi" button below.
@Composable
fun RootedDeviceScreen() {
    val context = LocalContext.current

    BackHandler(enabled = true) { /* swallow back - no bypass via the back gesture */ }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = Color(0xFFF87171),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Perangkat Tidak Aman Terdeteksi",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Saku-ku tidak dapat dijalankan di perangkat yang sudah di-root (rooted). " +
                    "Ini untuk melindungi data finansial dan keamanan akunmu dari aplikasi lain " +
                    "yang berpotensi berbahaya di perangkat ini.",
                color = Color.White.copy(alpha = 0.75f),
                fontFamily = PlusJakartaSans,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = "Coba jalankan Saku-ku di perangkat lain yang tidak di-root, atau hubungi " +
                    "cs@saku-ku.id jika menurutmu ini keliru.",
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            GradientButton(
                text = "Tutup Aplikasi",
                onClick = { (context as? Activity)?.finishAndRemoveTask() },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }
}
