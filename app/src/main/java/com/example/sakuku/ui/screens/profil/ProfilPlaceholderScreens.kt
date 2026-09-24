package com.example.sakuku.ui.screens.profil

import com.example.sakuku.ui.theme.ScreenPadding
import com.example.sakuku.ui.theme.screenTitleInset
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground

private const val SUPPORT_EMAIL = "cs@saku-ku.id"
private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

// Kontak Customer Service sekarang beneran actionable (tap -> buka aplikasi email lewat
// mailto:), bukan cuma teks placeholder lagi. FAQ/Syarat & Ketentuan tetap "coming soon" -
// belum ada konten resmi buat diisi, pola sama kayak card Ganti Kata Sandi di Settings staff
// Angular sebelum endpoint-nya ada, biar gak ada menu item yang nge-dead-end tanpa layar.
@Composable
fun BantuanScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                // bottom 120dp - nyamain pola ProfilScreen.kt, floating AnimatedBottomNavBar
                // butuh clearance segitu.
                .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.screenTitleInset().fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Bantuan",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Ada kendala atau pertanyaan? Tim Saku-Ku siap bantu lewat email.",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassFill)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL"))
                        context.startActivity(intent)
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BlobDark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = null, tint = BlobDark, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Customer Service", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text(SUPPORT_EMAIL, color = Color.White.copy(alpha = 0.5f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(BlobDark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Rounded.HelpOutline, contentDescription = null, tint = BlobDark, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.padding(top = 14.dp))
                Text(
                    text = "Pusat bantuan (FAQ) dan Syarat & Ketentuan sedang disiapkan.",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}
