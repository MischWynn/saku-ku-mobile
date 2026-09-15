package com.example.sakuku.ui.screens.pengajuan

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.TujuanPinjaman
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.ButtonTurquoiseDeep
import com.example.sakuku.ui.theme.ButtonTurquoiseLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val CardGradient = Brush.linearGradient(listOf(ButtonTurquoiseLight, ButtonTurquoiseDeep))

@Composable
fun PengajuanScreen(
    onBack: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    viewModel: PengajuanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    PengajuanScreenContent(
        uiState = uiState,
        onNominalChange = viewModel::onNominalChange,
        onTenorSelect = viewModel::onTenorSelect,
        onTujuanPinjaman = viewModel::onTujuanPinjaman,
        onAgreedToTermsChange = viewModel::onAgreedToTermsChange,
        onNextStep = viewModel::goToStep2,
        onPrevStep = viewModel::goToStep1,
        onSubmit = viewModel::submit,
        onRetry = viewModel::retry,
        onBack = onBack,
        onDone = onSubmitSuccess
    )
}

@Composable
private fun PengajuanScreenContent(
    uiState: PengajuanUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onTujuanPinjaman: (TujuanPinjaman) -> Unit,
    onAgreedToTermsChange: (Boolean) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
    ) {
        if (uiState.submitSuccess) {
            SuccessView(onDone = onDone)
            return@Box
        }

        Column(modifier = Modifier.fillMaxSize()) {
            StepHeader(
                stepLabel = if (uiState.currentStep == 1) "Langkah 1 dari 2" else "Langkah 2 dari 2",
                progress = if (uiState.currentStep == 1) 0.5f else 1f,
                onBack = if (uiState.currentStep == 1) onBack else onPrevStep
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (uiState.currentStep == 1) {
                    Step1Content(
                        uiState = uiState,
                        onNominalChange = onNominalChange,
                        onTenorSelect = onTenorSelect,
                        onTujuanPinjaman = onTujuanPinjaman,
                        onNextStep = onNextStep,
                        onRetry = onRetry
                    )
                } else {
                    Step2Content(
                        uiState = uiState,
                        onAgreedToTermsChange = onAgreedToTermsChange,
                        onSubmit = onSubmit
                    )
                }
            }
        }
    }
}

@Composable
private fun StepHeader(stepLabel: String, progress: Float, onBack: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
            }
            Text(
                text = stepLabel,
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(CardGradient)
            )
        }
    }
}

@Composable
private fun Step1Content(
    uiState: PengajuanUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onTujuanPinjaman: (TujuanPinjaman) -> Unit,
    onNextStep: () -> Unit,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = "Input Pengajuan",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Text(
            text = "Silakan tentukan nominal dan durasi pinjaman sesuai kebutuhanmu",
            color = Color.White.copy(alpha = 0.55f),
            fontFamily = PlusJakartaSans,
            fontSize = 12.5.sp,
            lineHeight = 17.sp
        )
    }

    if (uiState.errorMessage != null) {
        ErrorBanner(uiState.errorMessage, onRetry)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlobDark)
        }
        return
    }

    uiState.sisaPlafond?.let { sisa ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "PLAFOND TERSEDIA ANDA",
                color = Color.White.copy(alpha = 0.45f),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = LoanCalculator.formatRupiah(sisa),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            uiState.heldAmount?.let { held ->
                Text(
                    text = "${LoanCalculator.formatRupiah(held)} sedang dipakai pengajuan lain",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }

    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Jumlah Pinjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = LoanCalculator.formatRupiah(uiState.nominal),
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp
        )
        val maxNominal = uiState.sisaPlafond ?: 0.0
        Slider(
            value = uiState.nominal.toFloat(),
            onValueChange = { onNominalChange(it.toDouble()) },
            valueRange = 500_000f..maxNominal.toFloat().coerceAtLeast(500_001f),
            enabled = uiState.sisaPlafond != null,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = BlobDark,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Rp500rb", color = Color.White.copy(alpha = 0.4f), fontFamily = PlusJakartaSans, fontSize = 10.5.sp)
            Text(
                uiState.sisaPlafond?.let { LoanCalculator.formatRupiah(it) } ?: "-",
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp
            )
        }
    }

    if (uiState.tenors.isNotEmpty()) {
        Column {
            Text("Durasi Peminjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
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
    }

    Column {
        Text("Tujuan Pinjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
        Text(
            "Opsional - bantu kami menilai pengajuanmu lebih akurat",
            color = Color.White.copy(alpha = 0.4f),
            fontFamily = PlusJakartaSans,
            fontSize = 10.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TujuanPinjaman.entries.forEach { tujuan ->
                SelectableChip(
                    label = tujuan.label,
                    isSelected = tujuan == uiState.tujuanPinjaman,
                    onClick = { onTujuanPinjaman(tujuan) }
                )
            }
        }
    }


    Spacer(modifier = Modifier.height(4.dp))
    GradientButton(
        text = "Lihat Simulasi",
        onClick = onNextStep,
        enabled = uiState.canProceedToStep2
    )
}

@Composable
private fun Step2Content(
    uiState: PengajuanUiState,
    onAgreedToTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val tenor = uiState.selectedTenor
    val estimate = tenor?.let { LoanCalculator.estimate(uiState.nominal, it.tenor, it.interestRate) }

    Text(
        text = "Detail Simulasi",
        color = Color.White,
        fontFamily = PlusJakartaSans,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )

    if (tenor != null && estimate != null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardGradient)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("TOTAL PINJAMAN", color = Color(0xFF04160F).copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(
                LoanCalculator.formatRupiah(uiState.nominal),
                color = Color(0xFF04160F),
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Tenor", color = Color(0xFF04160F).copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
                    Text("${tenor.tenor} Bulan", color = Color(0xFF04160F), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Cicilan / Bulan", color = Color(0xFF04160F).copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
                    Text(LoanCalculator.formatRupiah(estimate.cicilanBulanan), color = Color(0xFF04160F), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        Text(
            text = "Dana diterima penuh sesuai nominal pinjaman - tanpa potongan biaya administrasi.",
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Suku Bunga & Biaya", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            SummaryRow("Bunga flat", "${formatPercent(tenor.interestRate)}% / tenor")
            SummaryRow("Total bunga", LoanCalculator.formatRupiah(estimate.totalBunga))
            SummaryRow("Total pembayaran", LoanCalculator.formatRupiah(estimate.totalPembayaran))
            uiState.tujuanPinjaman?.let { SummaryRow("Tujuan pinjaman", it.label) }

            uiState.dbrRatio(estimate.cicilanBulanan)?.let { dbr ->
                val isSafe = dbr <= 0.33
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DBR (cicilan vs pendapatan)", color = Color.White.copy(alpha = 0.5f), fontFamily = PlusJakartaSans, fontSize = 10.5.sp)
                    Text(
                        text = "${(dbr * 100).let { if (it == it.toInt().toDouble()) it.toInt().toString() else "%.0f".format(it) }}%${if (!isSafe) " ⚠️" else ""}",
                        color = if (isSafe) Color(0xFF5FE3AB) else Color(0xFFF28FA0),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background((if (isSafe) Color(0xFF5FE3AB) else Color(0xFFF28FA0)).copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                if (!isSafe) {
                    Text(
                        text = "Cicilan ini lebih dari sepertiga pendapatan bulananmu - pertimbangkan nominal atau tenor yang lebih ringan.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    if (uiState.errorMessage != null) {
        ErrorBanner(uiState.errorMessage, onRetry = null)
    }

    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(top = 4.dp)) {
        Checkbox(
            checked = uiState.agreedToTerms,
            onCheckedChange = onAgreedToTermsChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BlobDark,
                uncheckedColor = Color.White.copy(alpha = 0.4f),
                checkmarkColor = Color.White
            )
        )
        Text(
            text = "Saya menyatakan bahwa data yang saya berikan adalah benar dan setuju dengan Syarat & Ketentuan yang berlaku pada layanan Saku-ku.",
            color = Color.White.copy(alpha = 0.75f),
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }

    Spacer(modifier = Modifier.height(4.dp))
    GradientButton(
        text = "Ajukan Sekarang",
        onClick = onSubmit,
        enabled = uiState.agreedToTerms && !uiState.isSubmitting,
        isLoading = uiState.isSubmitting
    )
    Text(
        text = "Pengajuanmu akan direview bertahap oleh tim Marketing, Branch Manager, lalu Back Office sebelum dicairkan.",
        color = Color.White.copy(alpha = 0.4f),
        fontFamily = PlusJakartaSans,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.5.sp)
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEF5876).copy(alpha = 0.12f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(message, color = Color(0xFFF28FA0), fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
        onRetry?.let {
            Text(
                "Coba lagi",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                modifier = Modifier.clickable(onClick = it)
            )
        }
    }
}

@Composable
private fun SuccessView(onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BlobDark.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BlobDark, modifier = Modifier.size(44.dp))
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Pengajuan Terkirim!",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Pengajuanmu sedang direview tim Marketing. Kamu bisa pantau statusnya di halaman Riwayat.",
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(28.dp))
        GradientButton(text = "Kembali ke Beranda", onClick = onDone)
    }
}

// interestRate dari API itu angka persen mentah (3.0 = "3%"), bukan pecahan 0-1 - lihat
// catatan di LoanCalculator.kt.
private fun formatPercent(rate: Double): String =
    if (rate == rate.toInt().toDouble()) rate.toInt().toString() else "%.1f".format(rate)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun PengajuanScreenStep1Preview() {
    SakukuTheme {
        PengajuanScreenContent(
            uiState = PengajuanUiState(
                currentStep = 1,
                isLoading = false,
                sisaPlafond = 15_000_000.0,
                nominal = 5_000_000.0,
                tenors = listOf(
                    BungaTenorResponse("t1", 6, 3.0, "ACTIVE"),
                    BungaTenorResponse("t2", 12, 8.0, "ACTIVE"),
                    BungaTenorResponse("t3", 18, 10.0, "ACTIVE")
                ),
                selectedTenorId = "t2"
            ),
            onNominalChange = {}, onTenorSelect = {}, onTujuanPinjaman = {},
            onAgreedToTermsChange = {}, onNextStep = {}, onPrevStep = {},
            onSubmit = {}, onRetry = {}, onBack = {}, onDone = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun PengajuanScreenStep2Preview() {
    SakukuTheme {
        PengajuanScreenContent(
            uiState = PengajuanUiState(
                currentStep = 2,
                isLoading = false,
                sisaPlafond = 15_000_000.0,
                nominal = 5_000_000.0,
                tenors = listOf(BungaTenorResponse("t2", 12, 8.0, "ACTIVE")),
                selectedTenorId = "t2",
                tujuanPinjaman = TujuanPinjaman.MODAL_USAHA,
                agreedToTerms = true
            ),
            onNominalChange = {}, onTenorSelect = {}, onTujuanPinjaman = {},
            onAgreedToTermsChange = {}, onNextStep = {}, onPrevStep = {},
            onSubmit = {}, onRetry = {}, onBack = {}, onDone = {}
        )
    }
}
