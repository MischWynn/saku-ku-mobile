@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.screens.pengajuan

import com.example.sakuku.ui.theme.sakukuBlobBackgroundTop
import com.example.sakuku.ui.theme.ScreenPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.rounded.AccountBalance
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.TujuanPinjaman
import com.example.sakuku.ui.components.EditableNominalField
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.PengajuanButtonGradient
import com.example.sakuku.ui.components.RoundSliderThumb
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BgBottom
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.ButtonTurquoiseDeep
import com.example.sakuku.ui.theme.ButtonTurquoiseLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator
import com.example.sakuku.util.Validators

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val CardGradient = Brush.linearGradient(listOf(ButtonTurquoiseLight, ButtonTurquoiseDeep))
private val DarkInk = Color(0xFF04160F)
private val Amber = Color(0xFFF2C25F)
private val Rose = Color(0xFFF28FA0)
private val Mint = Color(0xFF5FE3AB)
private const val MIN_NOMINAL = 500_000.0

@Composable
fun PengajuanScreen(
    onBack: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToRekening: () -> Unit = {},
    viewModel: PengajuanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

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
        onDone = onSubmitSuccess,
        onNavigateToCompleteProfile = onNavigateToCompleteProfile,
        onNavigateToKtpDataDiri = onNavigateToKtpDataDiri,
        onNavigateToRekening = onNavigateToRekening
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
    onDone: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToRekening: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackgroundTop()
            .navigationBarsPadding()
    ) {
        if (uiState.submitSuccess) {
            SuccessView(onDone = onDone)
            return@Box
        }

        val showStickyBar = uiState.currentStep == 1 && uiState.canSubmitFromStep1

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
                    .padding(horizontal = ScreenPadding.Horizontal)
                    // Ruang ekstra di bawah biar konten terakhir gak ketutup bar estimasi.
                    .padding(bottom = if (showStickyBar) 120.dp else 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (uiState.currentStep == 1) {
                    Step1Content(
                        uiState = uiState,
                        onNominalChange = onNominalChange,
                        onTenorSelect = onTenorSelect,
                        onTujuanPinjaman = onTujuanPinjaman,
                        onRetry = onRetry,
                        onNavigateToCompleteProfile = onNavigateToCompleteProfile,
                        onNavigateToKtpDataDiri = onNavigateToKtpDataDiri,
                        onNavigateToRekening = onNavigateToRekening
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

        if (showStickyBar) {
            StickyEstimateBar(
                uiState = uiState,
                onNextStep = onNextStep,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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

// ======================= LANGKAH 1: bantu user milih =======================

@Composable
private fun Step1Content(
    uiState: PengajuanUiState,
    onNominalChange: (Double) -> Unit,
    onTenorSelect: (String) -> Unit,
    onTujuanPinjaman: (TujuanPinjaman) -> Unit,
    onRetry: () -> Unit,
    onNavigateToCompleteProfile: () -> Unit = {},
    onNavigateToKtpDataDiri: () -> Unit = {},
    onNavigateToRekening: () -> Unit = {}
) {
    Text(
        text = "Ajukan Pinjaman",
        color = Color.White,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )

    if (uiState.errorMessage != null) {
        ErrorBanner(uiState.errorMessage, onRetry)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BlobDark)
        }
        return
    }

    if (uiState.profileIncomplete) {
        // Backend (PengajuanService.create()) nolak submit kalau profil belum lengkap - dicek di
        // sini juga biar customer gak buang waktu ngisi slider/tenor dulu baru ditolak pas submit.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Amber.copy(alpha = 0.10f))
                .border(1.dp, Amber.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (uiState.underage) "BELUM MEMENUHI SYARAT USIA" else "LENGKAPI PROFIL DULU",
                color = Amber,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )
            Text(
                text = when {
                    uiState.underage ->
                        "Pengajuan pinjaman hanya untuk nasabah berusia minimal ${Validators.MIN_AGE} tahun (sesuai syarat kepemilikan KTP)."
                    uiState.tanggalLahirMissing ->
                        "Tanggal lahirmu belum diisi. Lengkapi dulu di KTP & Data Diri sebelum bisa mengajukan pinjaman."
                    uiState.employmentIncomplete ->
                        "Data pekerjaan dan pendapatan bulananmu belum lengkap. Lengkapi dulu di profil sebelum bisa mengajukan pinjaman."
                    else ->
                        "Rekening bank belum diisi. Dana pinjaman yang disetujui akan dicairkan ke rekening ini, jadi lengkapi dulu sebelum mengajukan."
                },
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
            // Umur < 17 gak ada yang bisa dilengkapi - tombolnya disembunyiin.
            if (!uiState.underage) {
                GradientButton(
                    text = "Lengkapi Profil",
                    onClick = when {
                        uiState.tanggalLahirMissing -> onNavigateToKtpDataDiri
                        uiState.employmentIncomplete -> onNavigateToCompleteProfile
                        else -> onNavigateToRekening
                    }
                )
            }
        }
        return
    }

    if (uiState.belowMinimum) {
        // Sisa plafond di bawah minimum pengajuan (Rp500rb) - gak ada nominal valid yang bisa
        // dipilih sama sekali, jadi berhenti di sini dengan pesan jelas.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Rose.copy(alpha = 0.10f))
                .border(1.dp, Rose.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "SISA PLAFOND TIDAK CUKUP",
                color = Rose,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp
            )
            Text(
                text = "Sisa plafondmu di bawah minimum pengajuan (Rp500.000). Selesaikan atau tunggu pengajuan yang masih berjalan sebelum mengajukan pinjaman baru.",
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
        }
        return
    }

    // 1. Hasil duluan: cicilan/bulan langsung berubah tiap slider/tenor diganti.
    EstimateHero(uiState)

    // 4. Nominal: bisa diketik, digeser, atau pilih cepat.
    val maxNominal = uiState.sisaPlafond ?: MIN_NOMINAL
    Column {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                "Jumlah Pinjaman",
                color = Color.White.copy(alpha = 0.75f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            uiState.sisaPlafond?.let {
                Text(
                    "Tersedia ${LoanCalculator.formatRupiah(it)}",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.5.sp
                )
            }
        }
        uiState.heldAmount?.let { held ->
            Text(
                text = "${LoanCalculator.formatRupiah(held)} sedang dipakai pengajuan lain",
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        EditableNominalField(
            nominal = uiState.nominal,
            onNominalChange = onNominalChange,
            minNominal = MIN_NOMINAL,
            maxNominal = maxNominal
        )
        Slider(
            value = uiState.nominal.toFloat(),
            onValueChange = { onNominalChange(it.toDouble()) },
            valueRange = MIN_NOMINAL.toFloat()..maxNominal.toFloat(),
            enabled = uiState.sisaPlafond != null,
            thumb = { RoundSliderThumb() },
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = BlobDark,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
        QuickAmountChips(current = uiState.nominal, max = maxNominal, onPick = onNominalChange)
    }

    // 3. Tiap pilihan tenor langsung nampilin cicilannya - bisa bandingin tanpa klik satu-satu.
    if (uiState.tenors.isNotEmpty()) {
        Column {
            Text("Durasi Peminjaman", color = Color.White.copy(alpha = 0.75f), fontFamily = PlusJakartaSans, fontSize = 12.sp)
            Text(
                "Tenor panjang = cicilan lebih ringan, tapi total bunga lebih besar",
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                uiState.tenors.forEach { tenor ->
                    TenorOptionCard(
                        tenor = tenor,
                        cicilan = uiState.cicilanFor(tenor),
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
}

@Composable
private fun EstimateHero(uiState: PengajuanUiState) {
    val tenor = uiState.selectedTenor
    val cicilan = tenor?.let { uiState.cicilanFor(it) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(CardGradient)
            .padding(20.dp)
    ) {
        Text("ESTIMASI CICILAN / BULAN", color = DarkInk.copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cicilan?.let { LoanCalculator.formatRupiah(it) } ?: "-",
            color = DarkInk,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "untuk ${LoanCalculator.formatRupiah(uiState.nominal)}" + (tenor?.let { " · ${it.tenor} bulan · bunga ${formatPercent(it.interestRate)}%" } ?: ""),
            color = DarkInk.copy(alpha = 0.7f),
            fontFamily = PlusJakartaSans,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun QuickAmountChips(current: Double, max: Double, onPick: (Double) -> Unit) {
    val presets = listOf(1_000_000.0, 5_000_000.0, 10_000_000.0).filter { it < max }
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        presets.forEach { amount ->
            SelectableChip(
                label = LoanCalculator.formatRupiahShort(amount),
                isSelected = current == amount,
                onClick = { onPick(amount) }
            )
        }
        SelectableChip(label = "Maks", isSelected = current == max, onClick = { onPick(max) })
    }
}

@Composable
private fun TenorOptionCard(tenor: BungaTenorResponse, cicilan: Double, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(shape)
            .background(if (isSelected) BlobDark.copy(alpha = 0.16f) else GlassFill)
            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) BlobDark else GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Text("${tenor.tenor} bulan", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            LoanCalculator.formatRupiahShort(cicilan),
            color = if (isSelected) BlobDark else Color.White.copy(alpha = 0.85f),
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        Text("/bulan", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text("bunga ${formatPercent(tenor.interestRate)}%", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
    }
}

// 2. Estimasi + tombol Lanjut dalam satu bar yang nempel di bawah.
@Composable
private fun StickyEstimateBar(uiState: PengajuanUiState, onNextStep: () -> Unit, modifier: Modifier = Modifier) {
    val tenor = uiState.selectedTenor ?: return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, BgBottom.copy(alpha = 0.95f), BgBottom)))
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F2420))
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${LoanCalculator.formatRupiah(uiState.cicilanFor(tenor))}/bln",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                "${LoanCalculator.formatRupiahShort(uiState.nominal)} · ${tenor.tenor} bulan",
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 11.sp
            )
        }
        GradientButton(
            text = "Lanjut",
            onClick = onNextStep,
            enabled = uiState.canProceedToStep2,
            modifier = Modifier.width(120.dp)
        )
    }
}

// ======================= LANGKAH 2: review sebelum kirim =======================

@Composable
private fun Step2Content(
    uiState: PengajuanUiState,
    onAgreedToTermsChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    val tenor = uiState.selectedTenor
    val estimate = tenor?.let { LoanCalculator.estimate(uiState.nominal, it.tenor, it.interestRate) }

    Text(
        text = "Ringkasan Pengajuan",
        color = Color.White,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    )

    if (tenor != null && estimate != null) {
        // 1. Total pelunasan + pembagian pokok vs bunga.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(CardGradient)
                .padding(20.dp)
        ) {
            Text("TOTAL PELUNASAN", color = DarkInk.copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            Text(
                LoanCalculator.formatRupiah(estimate.totalPembayaran),
                color = DarkInk,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp
            )
            Text(
                "${LoanCalculator.formatRupiah(estimate.cicilanBulanan)}/bulan selama ${tenor.tenor} bulan",
                color = DarkInk.copy(alpha = 0.7f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            val pokokFraction = (uiState.nominal / estimate.totalPembayaran).toFloat().coerceIn(0f, 1f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
            ) {
                Box(modifier = Modifier.weight(pokokFraction.coerceAtLeast(0.01f)).fillMaxHeight().background(DarkInk))
                Box(modifier = Modifier.weight((1f - pokokFraction).coerceAtLeast(0.01f)).fillMaxHeight().background(Color.White.copy(alpha = 0.7f)))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                LegendItem(DarkInk, "Pokok", LoanCalculator.formatRupiah(uiState.nominal))
                LegendItem(Color.White.copy(alpha = 0.7f), "Bunga", LoanCalculator.formatRupiah(estimate.totalBunga), alignEnd = true)
            }
        }

        Text(
            text = "Dana diterima penuh sesuai nominal pinjaman - tanpa potongan biaya administrasi.",
            color = Color.White.copy(alpha = 0.5f),
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        // Rincian
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Rincian", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            SummaryRow("Nominal pinjaman", LoanCalculator.formatRupiah(uiState.nominal))
            SummaryRow("Tenor", "${tenor.tenor} bulan")
            SummaryRow("Bunga flat", "${formatPercent(tenor.interestRate)}% / tenor")
            SummaryRow("Cicilan / bulan", LoanCalculator.formatRupiah(estimate.cicilanBulanan))
            uiState.tujuanPinjaman?.let { SummaryRow("Tujuan pinjaman", it.label) }
        }

        // 3. Total beban bulanan (cicilan baru + cicilan yang udah berjalan) vs pendapatan.
        MonthlyBurdenCard(uiState = uiState, cicilanBaru = estimate.cicilanBulanan)

        // 4. Rekening tujuan pencairan.
        RekeningTujuanCard(uiState)
    }

    if (uiState.errorMessage != null) {
        ErrorBanner(uiState.errorMessage, onRetry = null)
    }

    // 5. Syarat & ketentuan tepat sebelum tombol kirim.
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

    GradientButton(
        text = "Ajukan Sekarang",
        onClick = onSubmit,
        enabled = uiState.agreedToTerms && !uiState.isSubmitting,
        isLoading = uiState.isSubmitting,
        gradient = PengajuanButtonGradient
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
private fun LegendItem(color: Color, label: String, value: String, alignEnd: Boolean = false) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = DarkInk.copy(alpha = 0.65f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
        }
        Text(value, color = DarkInk, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
    }
}

@Composable
private fun MonthlyBurdenCard(uiState: PengajuanUiState, cicilanBaru: Double) {
    val existing = uiState.existingCicilanBulanan
    val total = cicilanBaru + existing
    val dbr = uiState.dbrRatio(total)
    // Zona: <= 33% aman (ambang yang sama dengan badge DBR staff), <= 50% waspada, > 50% berat.
    val zoneColor = when {
        dbr == null -> Color.White.copy(alpha = 0.5f)
        dbr <= 0.33 -> Mint
        dbr <= 0.50 -> Amber
        else -> Rose
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Beban Cicilan Bulanan", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        SummaryRow("Cicilan pengajuan ini", LoanCalculator.formatRupiah(cicilanBaru))
        if (existing > 0) {
            SummaryRow("Cicilan yang sedang berjalan", LoanCalculator.formatRupiah(existing))
        }
        SummaryRow("Total per bulan", LoanCalculator.formatRupiah(total))

        if (dbr != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Dari pendapatan bulananmu",
                    color = Color.White.copy(alpha = 0.55f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp,
                    modifier = Modifier.weight(1f)
                )
                Text("${(dbr * 100).toInt()}%", color = zoneColor, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White.copy(alpha = 0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(dbr.toFloat().coerceIn(0.02f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(999.dp))
                        .background(zoneColor)
                )
            }
            Text(
                text = when {
                    dbr <= 0.33 -> "Aman - total cicilan masih di bawah sepertiga pendapatanmu."
                    dbr <= 0.50 -> "Perlu dipertimbangkan - total cicilan lebih dari sepertiga pendapatanmu."
                    else -> "Berat - lebih dari separuh pendapatanmu. Coba kurangi nominal atau pilih tenor lebih panjang."
                },
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
        Text(
            "Estimasi, berdasarkan bunga flat & pinjaman yang sudah cair.",
            color = Color.White.copy(alpha = 0.35f),
            fontFamily = PlusJakartaSans,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RekeningTujuanCard(uiState: PengajuanUiState) {
    val bank = uiState.namaBank?.takeIf { it.isNotBlank() }
    val nomor = uiState.nomorRekening?.takeIf { it.isNotBlank() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BlobDark.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.AccountBalance, contentDescription = null, tint = BlobDark, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Dana akan dicairkan ke", color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
            Text(
                text = when {
                    bank != null && nomor != null -> "$bank •••• ${nomor.takeLast(4)}"
                    else -> "Rekening terdaftar"
                },
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp
            )
            uiState.namaPemilikRekening?.takeIf { it.isNotBlank() }?.let {
                Text("a.n. $it", color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
            }
        }
    }
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
        Text(message, color = Rose, fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
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

private val previewTenors = listOf(
    BungaTenorResponse("t1", 6, 3.0, "ACTIVE"),
    BungaTenorResponse("t2", 12, 8.0, "ACTIVE"),
    BungaTenorResponse("t3", 18, 10.0, "ACTIVE"),
    BungaTenorResponse("t4", 24, 12.0, "ACTIVE")
)

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun PengajuanScreenStep1Preview() {
    SakukuTheme {
        PengajuanScreenContent(
            uiState = PengajuanUiState(
                currentStep = 1,
                isLoading = false,
                plafond = 15_000_000.0,
                sisaPlafond = 12_000_000.0,
                pekerjaan = "Staff Admin",
                pendapatanBulanan = 5_000_000.0,
                tanggalLahir = "1998-05-12",
                namaBank = "BCA",
                nomorRekening = "1234567890",
                namaPemilikRekening = "Vilia",
                nominal = 5_000_000.0,
                tenors = previewTenors,
                selectedTenorId = "t2"
            ),
            onNominalChange = {}, onTenorSelect = {}, onTujuanPinjaman = {},
            onAgreedToTermsChange = {}, onNextStep = {}, onPrevStep = {},
            onSubmit = {}, onRetry = {}, onBack = {}, onDone = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1500)
@Composable
private fun PengajuanScreenStep2Preview() {
    SakukuTheme {
        PengajuanScreenContent(
            uiState = PengajuanUiState(
                currentStep = 2,
                isLoading = false,
                plafond = 15_000_000.0,
                sisaPlafond = 12_000_000.0,
                pekerjaan = "Staff Admin",
                pendapatanBulanan = 5_000_000.0,
                tanggalLahir = "1998-05-12",
                namaBank = "BCA",
                nomorRekening = "1234567890",
                namaPemilikRekening = "Vilia",
                existingCicilanBulanan = 500_000.0,
                nominal = 5_000_000.0,
                tenors = previewTenors,
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
