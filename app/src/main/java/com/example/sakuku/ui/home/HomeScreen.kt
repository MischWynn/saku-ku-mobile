package com.example.sakuku.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.FactCheck
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PriceChange
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.ui.components.AuthButtonGradient
import com.example.sakuku.ui.components.EditableNominalField
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator
import kotlinx.coroutines.launch

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

// Warna badge per tier - dicocokin ke nama asli dari tbl_plafond (Bronze/Silver/Gold/Platinum).
// Tier di luar 4 nama ini (kalau nanti ditambah) jatuh ke abu-abu netral, bukan crash.
private fun tierColor(namaPlafond: String): Color = when (namaPlafond.lowercase()) {
    "bronze" -> Color(0xFFC98A4B)
    "silver" -> Color(0xFFAEBCC9)
    "gold" -> Color(0xFFE7C468)
    "platinum" -> Color(0xFF8FD4E6)
    else -> Color.White.copy(alpha = 0.6f)
}

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToPlafond: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        uiState = uiState,
        onNominalChange = viewModel::onNominalChange,
        onTenorSelect = viewModel::onTenorSelect,
        onRetry = viewModel::retry,
        onNavigateToLogin = onNavigateToLogin,
        onNavigateToPlafond = onNavigateToPlafond
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToPlafond: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val simulasiAnchor = remember { BringIntoViewRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                // bottom digedein (bukan 32.dp biasa) - navbar sekarang floating beneran di atas
                // konten (lihat MainScreen), jadi butuh jarak aman biar kartu Estimasi/tombol
                // terakhir gak ketutup pill navbar.
                .padding(top = 24.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            TopBar(onNavigateToLogin = onNavigateToLogin)

            PlafondPreviewSection(tiers = uiState.tiers, isLoading = uiState.isLoading)

            QuickAccessSection(
                onDaftarMasuk = onNavigateToLogin,
                onSimulasi = { coroutineScope.launch { simulasiAnchor.bringIntoView() } },
                onCekPlafond = onNavigateToPlafond
            )

            SimulasiSection(
                modifier = Modifier.bringIntoViewRequester(simulasiAnchor),
                uiState = uiState,
                onNominalChange = onNominalChange,
                onTenorSelect = onTenorSelect,
                onRetry = onRetry,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}

@Composable
private fun TopBar(onNavigateToLogin: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(
                text = "Selamat datang di",
                color = Color.White.copy(alpha = 0.65f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp
            )
            Text(
                text = "Saku-ku",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
//        Row(
//            modifier = Modifier
//                .clip(RoundedCornerShape(999.dp))
//                .background(GlassFill)
//                .border(1.dp, GlassBorder, RoundedCornerShape(999.dp))
//                .clickable(onClick = onNavigateToLogin)
//                .padding(horizontal = 16.dp, vertical = 10.dp)
//        ) {
//            Text(
//                text = "Masuk / Daftar",
//                color = Color.White,
//                fontFamily = PlusJakartaSans,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 12.5.sp
//            )
//        }
    }
}

@Composable
private fun PlafondPreviewSection(tiers: List<PlafondResponse>, isLoading: Boolean) {
    Column {
        SectionHeader(
            title = "Plafond yang bisa didapatkan",
            subtitle = "Dihitung otomatis dari profilmu saat daftar"
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = BlobDark, strokeWidth = 2.dp)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tiers.forEach { tier -> TierMiniCard(tier) }
            }
        }
    }
}

@Composable
private fun TierMiniCard(tier: PlafondResponse) {
    val accent = tierColor(tier.namaPlafond)
    Column(
        modifier = Modifier
            // widthIn (bukan width tetap) - kartu punya lebar dasar lebih lega, tapi tetap bisa
            // "elastis" ngikutin kalau teks di dalamnya butuh ruang lebih (mis. font sistem HP
            // di-set besar via Setelan Aksesibilitas) - semua Text di sini udah pakai .sp jadi
            // otomatis ke-scale, kartu ini yang perlu ikut nyesuaiin.
            .widthIn(min = 168.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = tier.namaPlafond.uppercase(),
            color = accent,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(accent.copy(alpha = 0.18f))
                .padding(horizontal = 9.dp, vertical = 3.dp)
        )
        Column {
            Text(
                text = "Hingga",
                color = Color.White.copy(alpha = 0.45f),
                fontFamily = PlusJakartaSans,
                fontSize = 9.5.sp
            )
            Text(
                text = LoanCalculator.formatRupiah(tier.limitMaksimal),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        tier.deskripsi?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun QuickAccessSection(
    onDaftarMasuk: () -> Unit,
    onSimulasi: () -> Unit,
    onCekPlafond: () -> Unit
) {
    Column {
        SectionHeader(title = "Akses cepat")
        Spacer(modifier = Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAccessTile("Daftar / Masuk", Icons.AutoMirrored.Rounded.ArrowForward, Modifier.weight(1f), onClick = onDaftarMasuk)
            QuickAccessTile("Simulasi Pinjaman", Icons.Rounded.Calculate, Modifier.weight(1f), onClick = onSimulasi)
            QuickAccessTile("Cek Plafond", Icons.Rounded.PriceChange, Modifier.weight(1f), onClick = onCekPlafond, isNew = true)
        }
    }
}

@Composable
private fun QuickAccessTile(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isNew: Boolean = false,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = BlobDark, modifier = Modifier.size(17.dp))
            }
            Text(
                text = label,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 13.sp
            )
        }
        if (isNew) {
            Text(
                text = "BARU",
                color = Color(0xFF04160F),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Black,
                fontSize = 8.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(BlobDark)
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SimulasiSection(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(modifier = modifier) {
        SectionHeader(title = "Simulasi pinjaman")
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassFill)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(uiState.errorMessage, color = Color.White.copy(alpha = 0.8f), fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
                Text(
                    text = "Coba lagi",
                    color = BlobDark,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    modifier = Modifier.clickable(onClick = onRetry)
                )
            }
            return
        }

        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jumlah pinjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
                    Text(
                        "maks. ${LoanCalculator.formatRupiah(uiState.maxNominal)}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                EditableNominalField(
                    nominal = uiState.nominal,
                    onNominalChange = onNominalChange,
                    minNominal = 500_000.0,
                    maxNominal = uiState.maxNominal
                )
                Slider(
                    value = uiState.nominal.toFloat(),
                    onValueChange = { onNominalChange(it.toDouble()) },
                    valueRange = 500_000f..uiState.maxNominal.toFloat(),
                    steps = 0,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = BlobDark,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )
                Text(
                    text = "Geser sesuai kebutuhanmu",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp
                )
            }

            if (uiState.tenors.isNotEmpty()) {
                Column {
                    Text("Pilih tenor", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.tenors.forEach { tenor ->
                            SelectableChip(
                                modifier = Modifier.weight(1f),
                                label = "${tenor.tenor} Bulan",
                                subtitle = "${formatPercent(tenor.interestRate)}%",
                                isSelected = tenor.id == uiState.selectedTenorId,
                                onClick = { onTenorSelect(tenor.id) }
                            )
                        }
                    }
                }

                val tenor = uiState.selectedTenor
                if (tenor != null) {
                    val estimate = LoanCalculator.estimate(uiState.nominal, tenor.tenor, tenor.interestRate)
                    EstimateCard(estimate = estimate, nominal = uiState.nominal, tenor = tenor, onNavigateToLogin = onNavigateToLogin)
                }
            } else if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = BlobDark, strokeWidth = 2.dp)
            }
        }
    }
}

@Composable
private fun EstimateCard(
    estimate: com.example.sakuku.util.LoanEstimate,
    nominal: Double,
    tenor: BungaTenorResponse,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ESTIMASI CICILAN BULANAN",
            color = Color.White.copy(alpha = 0.45f),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
        Text(
            text = LoanCalculator.formatRupiah(estimate.cicilanBulanan),
            color = BlobLight,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            EstimateRow("Suku bunga (flat)", "${formatPercent(tenor.interestRate)}% / tenor")
            EstimateRow("Total pinjaman pokok", LoanCalculator.formatRupiah(nominal))
            EstimateRow("Total pembayaran", LoanCalculator.formatRupiah(estimate.totalPembayaran))
        }

        Spacer(modifier = Modifier.height(2.dp))
        GradientButton(text = "Masuk untuk ajukan", onClick = onNavigateToLogin)
        Text(
            text = "*Nominal dapat berubah sesuai kebijakan yang berlaku. Suku bunga flat ",
            color = Color.White.copy(alpha = 0.35f),
            fontFamily = PlusJakartaSans,
            fontSize = 9.sp,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun EstimateRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.5.sp)
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            text = title,
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
        subtitle?.let {
            Text(
                text = it,
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp
            )
        }
    }
}

// interestRate dari API itu angka persen mentah (3.0 = "3%"), bukan pecahan 0-1.
private fun formatPercent(rate: Double): String =
    if (rate == rate.toInt().toDouble()) rate.toInt().toString() else "%.1f".format(rate)

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, heightDp = 1400)
@Composable
private fun HomeScreenPreview() {
    SakukuTheme {
        HomeScreenContent(
            uiState = HomeUiState(
                isLoading = false,
                tiers = listOf(
                    PlafondResponse("1", "Bronze", "Tier minimum", "REGULER", 2_000_000.0, "ACTIVE"),
                    PlafondResponse("2", "Silver", "Tier menengah bawah", "REGULER", 7_500_000.0, "ACTIVE"),
                    PlafondResponse("3", "Gold", "Tier menengah atas", "REGULER", 15_000_000.0, "ACTIVE")
                ),
                tenors = listOf(
                    BungaTenorResponse("t1", 6, 3.0, "ACTIVE"),
                    BungaTenorResponse("t2", 12, 8.0, "ACTIVE"),
                    BungaTenorResponse("t3", 18, 10.0, "ACTIVE")
                ),
                selectedTenorId = "t1",
                nominal = 1_000_000.0
            ),
            onNominalChange = {},
            onTenorSelect = {},
            onRetry = {},
            onNavigateToLogin = {},
            onNavigateToPlafond = {}
        )
    }
}
