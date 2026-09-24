package com.example.sakuku.ui.screens.profil

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.R
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.BgBottom
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.Teal900
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.TIER_LEVEL_COUNT
import com.example.sakuku.ui.theme.tierStyleOf
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val DangerColor = Color(0xFFF28FA0)

@Composable
fun ProfilScreen(
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToKontak: () -> Unit = {},
    onNavigateToDataPekerjaan: () -> Unit = {},
    onNavigateToEditDataDiri: () -> Unit = {},
    onNavigateToKeamanan: () -> Unit = {},
    onNavigateToRekeningBank: () -> Unit = {},
    onNavigateToBantuan: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshIfLoaded() }

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    ProfilScreenContent(
        uiState = uiState,
        onNavigateToKtpDataDiri = onNavigateToKtpDataDiri,
        onNavigateToKontak = onNavigateToKontak,
        onNavigateToDataPekerjaan = onNavigateToDataPekerjaan,
        onNavigateToEditDataDiri = onNavigateToEditDataDiri,
        onNavigateToKeamanan = onNavigateToKeamanan,
        onNavigateToRekeningBank = onNavigateToRekeningBank,
        onNavigateToBantuan = onNavigateToBantuan,
        onLogout = viewModel::logout
    )
}

@Composable
private fun ProfilScreenContent(
    uiState: ProfilUiState,
    onNavigateToKtpDataDiri: () -> Unit,
    onNavigateToKontak: () -> Unit,
    onNavigateToDataPekerjaan: () -> Unit,
    onNavigateToEditDataDiri: () -> Unit,
    onNavigateToKeamanan: () -> Unit,
    onNavigateToRekeningBank: () -> Unit,
    onNavigateToBantuan: () -> Unit,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 16.dp)
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlobDark)
            }
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Profil Pengguna",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                TierIdentityCard(
                    name = uiState.namaLengkap.ifBlank { "Pengguna" },
                    nik = uiState.nik,
                    tier = uiState.tierPlafond ?: "Bronze",
                    plafond = uiState.plafond
                )

                if (uiState.errorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DangerColor.copy(alpha = 0.12f))
                            .padding(14.dp)
                    ) {
                        Text(uiState.errorMessage, color = DangerColor, fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
                    }
                }

                SisaPlafondRow(sisaPlafond = uiState.sisaPlafond?.let { LoanCalculator.formatRupiah(it) } ?: "-")

                MenuSection(title = "Data Pribadi (e-KYC)") {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Badge,
                        title = "KTP & Data Diri",
                        subtitle = maskedNik(uiState.nik),
                        onClick = onNavigateToKtpDataDiri
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.Phone,
                        title = "Kontak",
                        subtitle = uiState.email.ifBlank { "Belum diisi" },
                        onClick = onNavigateToKontak
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.Work,
                        title = "Data Pekerjaan",
                        subtitle = uiState.pekerjaan.ifBlank { "Belum diisi" },
                        onClick = onNavigateToDataPekerjaan
                    )
                }

                MenuSection(title = "Pengaturan Akun") {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Lock,
                        title = "Keamanan Akun",
                        subtitle = "Ganti Password",
                        onClick = onNavigateToKeamanan
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.AccountBalance,
                        title = "Rekening Bank",
                        subtitle = "Untuk pencairan dana",
                        onClick = onNavigateToRekeningBank
                    )
                }

                MenuSection(title = "Lainnya") {
                    ProfileMenuItem(
                        icon = Icons.Rounded.HelpOutline,
                        title = "Pusat Bantuan (FAQ)",
                        onClick = onNavigateToBantuan
                    )
                    ProfileMenuItem(
                        icon = Icons.Rounded.Description,
                        title = "Syarat & Ketentuan",
                        onClick = onNavigateToBantuan
                    )
                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Rounded.ExitToApp,
                        title = "Keluar",
                        titleColor = DangerColor,
                        iconTint = DangerColor,
                        showChevron = false,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

// Konfirmasi password sebelum eksekusi - aksi ini gak bisa dibatalin (backend soft-delete +
// scramble NIK/email/no_hp, lihat CustomerAuthService.deleteOwnAccount), jadi jangan cukup
// modal "yakin?" doang tanpa re-auth. Sengaja BUKAN private - dipindah dipakainya ke
// KeamananAkunScreen (17 Sept, section "Zona Berbahaya"), tapi komponennya tetap di sini biar
// gak pindah-pindah file cuma buat 1 composable kecil.
@Composable
fun DeleteAccountDialog(
    password: String,
    isDeleting: Boolean,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        containerColor = Teal900,
        title = {
            Text("Hapus Akun?", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Akun dan semua data kamu bakal gak bisa dipakai lagi. Masukkan password buat konfirmasi.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.5.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                SakukuOutlinedField(
                    value = password,
                    onValueChange = onPasswordChange,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = DangerColor, fontFamily = PlusJakartaSans, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DangerColor, strokeWidth = 2.dp)
                } else {
                    Text("Hapus Akun", color = DangerColor, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text("Batal", color = Color.White.copy(alpha = 0.6f), fontFamily = PlusJakartaSans)
            }
        }
    )
}

private fun maskedNik(nik: String): String {
    if (nik.length < 4) return "Belum tersedia"
    return "•••• •••• ${nik.takeLast(4)}"
}

@Composable
private fun TierIdentityCard(name: String, nik: String, tier: String, plafond: Double?) {
    // Tiap tier punya gradient + warna accent sendiri (lihat ui/theme/TierStyle.kt), biar
    // Bronze/Silver/Gold/Platinum kebedain sekilas. Watermark koi tetap sama di semua tier.
    val style = tierStyleOf(tier)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = style.gradient,
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(1.dp, style.accent.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
    ) {
        CardMotifOverlay(accent = style.accent, modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 17.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = maskedNik(nik),
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = PlusJakartaSans,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = tier.uppercase(),
                    color = style.accent,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.25f))
                        .border(1.dp, style.accent.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Posisi tier dari 4 level (Bronze = 1 ... Platinum = 4) - biar user langsung
                // lihat masih ada tier di atasnya.
                if (style.level > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(TIER_LEVEL_COUNT) { i ->
                            Box(
                                modifier = Modifier
                                    .size(width = 18.dp, height = 4.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (i < style.level) style.accent
                                        else Color.White.copy(alpha = 0.18f)
                                    )
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${tier.replaceFirstChar { it.uppercase() }} tier",
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "limit hingga ${plafond?.let { LoanCalculator.formatRupiah(it) } ?: "-"}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontFamily = PlusJakartaSans,
                        fontStyle = FontStyle.Italic,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CardMotifOverlay(accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val ringColor = accent.copy(alpha = 0.10f)
        repeat(3) { i ->
            drawCircle(
                color = ringColor,
                radius = size.minDimension * (0.35f + i * 0.18f),
                center = Offset(size.width * 0.08f, size.height * 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }
        drawCircle(
            color = accent.copy(alpha = 0.18f),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * 0.95f, size.height * 1.05f)
        )
    }
    Image(
        painter = painterResource(id = R.drawable.koi_line),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alpha = 0.30f,
        modifier = Modifier
            .fillMaxSize()
            .offset(x=(-30).dp, y = 10.dp)
    )
}

@Composable
private fun SisaPlafondRow(sisaPlafond: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Speed, contentDescription = null, tint = BlobDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Sisa Plafond Tersedia", color = Color.White.copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Text(sisaPlafond, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun MenuSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp)),
            content = content
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector, title: String, subtitle: String? = null,
    titleColor: Color = Color.White, iconTint: Color = Color.White.copy(alpha = 0.7f),
    showChevron: Boolean = true, onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = titleColor, fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (subtitle != null) {
                Text(text = subtitle, color = Color.White.copy(alpha = 0.4f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
            }
        }
        if (showChevron) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Masuk", tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun ProfilScreenPreview() {
    SakukuTheme {
        ProfilScreenContent(
            uiState = ProfilUiState(
                isLoading = false,
                namaLengkap = "Novita Sari",
                nik = "3273010101990016",
                email = "novita.sari@mail.com",
                noHp = "081234560016",
                alamat = "Jl. Ahmad Yani No. 16, Bandung",
                pekerjaan = "Staff Admin",
                plafond = 4_000_000.0,
                sisaPlafond = 200_000.0,
                tierPlafond = "Bronze"
            ),
            onNavigateToKtpDataDiri = {},
            onNavigateToKontak = {},
            onNavigateToDataPekerjaan = {},
            onNavigateToEditDataDiri = {},
            onNavigateToKeamanan = {},
            onNavigateToRekeningBank = {},
            onNavigateToBantuan = {},
            onLogout = {}
        )
    }
}

// Bandingin keempat tier e-card sekaligus di panel Preview Android Studio.
@Preview(showBackground = true, backgroundColor = 0xFF0A1614, heightDp = 820)
@Composable
private fun TierIdentityCardAllTiersPreview() {
    SakukuTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TierIdentityCard("Novita Sari", "3201123456789012", "Bronze", 4_000_000.0)
            TierIdentityCard("Novita Sari", "3201123456789012", "Silver", 9_000_000.0)
            TierIdentityCard("Novita Sari", "3201123456789012", "Gold", 15_000_000.0)
            TierIdentityCard("Novita Sari", "3201123456789012", "Platinum", 42_000_000.0)
        }
    }
}
