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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.R
import com.example.sakuku.ui.theme.BgBottom
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.Teal900
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val DangerColor = Color(0xFFF28FA0)

// Overview Profil - sengaja dibikin bentuk menu (avatar+kartu limit+list section), BUKAN
// form panjang - form-nya sendiri dipindah ke EditDataDiriScreen (dibuka lewat menu
// "Data Pribadi"), biar halaman ini tetap scannable dan gak "pusing lihatnya" (permintaan user).
@Composable
fun ProfilScreen(
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToEditDataDiri: () -> Unit = {},
    onNavigateToKeamanan: () -> Unit = {},
    onNavigateToRekeningBank: () -> Unit = {},
    onNavigateToBantuan: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    ProfilScreenContent(
        uiState = uiState,
        onNavigateToKtpDataDiri = onNavigateToKtpDataDiri,
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
    )
    {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Profil Pengguna",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f).padding(bottom= 24.dp)
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlobDark)
                }
                return@Box
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp, bottom = 120.dp),
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
                    onClick = onNavigateToEditDataDiri
                )
                ProfileMenuItem(
                    icon = Icons.Rounded.Work,
                    title = "Data Pekerjaan",
                    subtitle = uiState.pekerjaan.ifBlank { "Belum diisi" },
                    onClick = onNavigateToEditDataDiri
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

private fun maskedNik(nik: String): String {
    if (nik.length < 4) return "Belum tersedia"
    return "•••• •••• ${nik.takeLast(4)}"
}

// --- KOMPONEN PENDUKUNG ---

// Kartu identitas gaya "kartu member" - nama + NIK ter-mask + tier + limit dalam 1 kartu,
// niru referensi Figma user (background hijau tua gradasi + motif batik/koi). Motif asli
// belum ada asetnya, jadi background-nya sementara pakai gradasi + pola lingkaran hasil
// Canvas (CardMotifOverlay) - begitu ada PNG/SVG koi dari Figma, tinggal ganti jadi
// Image(painterResource(...)) di posisi CardMotifOverlay tanpa ubah layout lain.
@Composable
private fun TierIdentityCard(name: String, nik: String, tier: String, plafond: Double?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(BgBottom, Teal900, BlobMid.copy(alpha = 0.35f)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(22.dp))
    ) {
        CardMotifOverlay(modifier = Modifier.matchParentSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
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

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
                        color = Color.White.copy(alpha = 0.65f),
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
private fun CardMotifOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val ringColor = Color.White.copy(alpha = 0.06f)
        // Beberapa lingkaran konsentris di kiri-atas, niru kesan motif batik/koi tanpa aset asli.
        repeat(3) { i ->
            drawCircle(
                color = ringColor,
                radius = size.minDimension * (0.35f + i * 0.18f),
                center = Offset(size.width * 0.08f, size.height * 0.15f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }
        drawCircle(
            color = BlobMid.copy(alpha = 0.18f),
            radius = size.minDimension * 0.55f,
            center = Offset(size.width * 0.95f, size.height * 1.05f)
        )
    }
    Image(
        painter = painterResource(id = R.drawable.koi_line),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alpha = 0.15f,
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
            onNavigateToEditDataDiri = {},
            onNavigateToKeamanan = {},
            onNavigateToRekeningBank = {},
            onNavigateToBantuan = {},
            onLogout = {}
        )
    }
}
