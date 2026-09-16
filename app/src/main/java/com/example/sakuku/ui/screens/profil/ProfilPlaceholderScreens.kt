package com.example.sakuku.ui.screens.profil

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground

// 2 destinasi menu Profil yang belum ada endpoint/field backend-nya (nomor rekening buat
// disburse - nunggu user cek skema DB dulu, FAQ/T&C statis). Sengaja dibikin "coming soon" dulu
// (pola sama kayak card Ganti Kata Sandi di Settings staff Angular sebelum endpoint-nya ada)
// biar gak ada menu item yang nge-dead-end tanpa layar. "Keamanan Akun" udah gak di sini lagi -
// lihat KeamananAkunScreen.kt, sekarang beneran nyambung ke customer/change-password.
@Composable
fun RekeningBankScreen(onBack: () -> Unit = {}) {
    ProfilPlaceholderScreen(
        title = "Rekening Bank",
        icon = Icons.Rounded.AccountBalance,
        message = "Fitur rekening tujuan pencairan dana sedang disiapkan.",
        onBack = onBack
    )
}

@Composable
fun BantuanScreen(onBack: () -> Unit = {}) {
    ProfilPlaceholderScreen(
        title = "Bantuan",
        icon = Icons.Rounded.HelpOutline,
        message = "Pusat bantuan dan syarat & ketentuan sedang disiapkan. Ada kendala? Hubungi tim Saku-Ku lewat email.",
        onBack = onBack
    )
}

@Composable
private fun ProfilPlaceholderScreen(title: String, icon: ImageVector, message: String, onBack: () -> Unit) {
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
                .padding(horizontal = 20.dp)
                // bottom 120dp - nyamain pola ProfilScreen.kt, floating AnimatedBottomNavBar
                // butuh clearance segitu. Sebelumnya gak ada verticalScroll/bottom padding sama
                // sekali - kalau device pendek/teks panjang, konten bisa ketutup nav bar
                // permanen tanpa bisa di-scroll buat lihatnya.
                .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                Text(text = title, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BlobDark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = BlobDark, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.padding(top = 16.dp))
                Text(
                    text = message,
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RekeningBankScreenPreview() {
    SakukuTheme { RekeningBankScreen() }
}
