@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.screens.simulasi

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.unit.Dp
import com.example.sakuku.ui.theme.screenTitleInset
import com.example.sakuku.ui.theme.ScreenPadding
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.ui.components.EditableNominalField
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.RoundSliderThumb
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun SimulasiScreen(
    onBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    // Tinggi navbar mengambang (lihat MainScreen) - ruang ekstra di bawah konten scroll.
    bottomInset: Dp = 0.dp,
    viewModel: SimulasiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    SimulasiScreenContent(
        uiState = uiState,
        onBack = onBack,
        onNominalChange = viewModel::onNominalChange,
        onTenorSelect = viewModel::onTenorSelect,
        onRetry = viewModel::retry,
        onNavigateToLogin = onNavigateToLogin,
        bottomInset = bottomInset
    )
}

@Composable
private fun SimulasiScreenContent(
    uiState: SimulasiUiState,
    onBack: () -> Unit,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onRetry: () -> Unit,
    onNavigateToLogin: () -> Unit,
    bottomInset: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(top = 20.dp, bottom = 32.dp + bottomInset)
        ) {
            Row(modifier = Modifier.screenTitleInset(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Simulasi Pinjaman",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

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
                        "Coba lagi",
                        color = BlobDark,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        modifier = Modifier.clickable(onClick = onRetry)
                    )
                }
                return@Column
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

                    val effectiveMinNominal = if (uiState.maxNominal < 500_000.0) 0.0 else 500_000.0
                    EditableNominalField(
                        nominal = uiState.nominal,
                        onNominalChange = onNominalChange,
                        minNominal = effectiveMinNominal,
                        maxNominal = uiState.maxNominal
                    )
                    Slider(
                        value = uiState.nominal.toFloat(),
                        onValueChange = { onNominalChange(it.toDouble()) },
                        valueRange = effectiveMinNominal.toFloat()..uiState.maxNominal.toFloat(),
                        steps = 0,
                        thumb = { RoundSliderThumb() },
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
                        // Chip dibagi rata kalau muat (min ~84dp/chip biar "12 Bulan" gak kepecah
                        // 2 baris). Layar sempit (mis. HP 360dp) / font gede -> geser horizontal.
                        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                            val count = uiState.tenors.size
                            val fitsEvenly = count > 0 && (maxWidth - 8.dp * (count - 1)) / count >= 84.dp
                            Row(
                                modifier = if (fitsEvenly) Modifier.fillMaxWidth()
                                else Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.tenors.forEach { tenor ->
                                    SelectableChip(
                                        modifier = if (fitsEvenly) Modifier.weight(1f) else Modifier,
                                        label = "${tenor.tenor} Bulan",
                                        subtitle = "${formatPercent(tenor.interestRate)}%",
                                        isSelected = tenor.id == uiState.selectedTenorId,
                                        onClick = { onTenorSelect(tenor.id) }
                                    )
                                }
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
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
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
    // Label weight(1f): nilai tetap utuh di kanan, label yang ngalah wrap di layar sempit.
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.5.sp, modifier = Modifier.weight(1f).padding(end = 12.dp))
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, textAlign = TextAlign.End)
    }
}

private fun formatPercent(rate: Double): String =
    if (rate == rate.toInt().toDouble()) rate.toInt().toString() else "%.1f".format(rate)


// Preview buat screenshot - pakai data contoh (tenor & bunga persis data backend: 6/12/18/24
// bulan, bunga flat per tenor), gak butuh Hilt/network. heightDp dibikin tinggi biar seluruh
// layar kebaca dalam 1 gambar; ganti ke 800 kalau mau ukuran 1 layar HP.
@Preview(name = "Simulasi Pinjaman", showBackground = true, backgroundColor = 0xFF0A1614, widthDp = 390, heightDp = 1000)
@Composable
private fun SimulasiScreenPreview() {
    SakukuTheme {
        SimulasiScreenContent(
            uiState = SimulasiUiState(
                isLoading = false,
                tenors = listOf(
                    BungaTenorResponse("t1", 6, 3.0, "ACTIVE"),
                    BungaTenorResponse("t2", 12, 8.0, "ACTIVE"),
                    BungaTenorResponse("t3", 18, 10.0, "ACTIVE"),
                    BungaTenorResponse("t4", 24, 12.0, "ACTIVE")
                ),
                selectedTenorId = "t2",
                nominal = 10_000_000.0,
                maxNominal = 50_000_000.0
            ),
            onBack = {},
            onNominalChange = {},
            onTenorSelect = {},
            onRetry = {},
            onNavigateToLogin = {}
        )
    }
}
