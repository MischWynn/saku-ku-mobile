package com.example.sakuku.ui.screens.notifikasi

import androidx.compose.ui.unit.Dp
import com.example.sakuku.ui.theme.sakukuBlobBackgroundTop
import com.example.sakuku.ui.components.PengajuanStepper
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.CustomerMeResponse
import com.example.sakuku.data.remote.dto.NotificationPengajuanRef
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.ScreenPadding
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.screenTitleInset
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

// Dibuka dari list Notifikasi khusus buat pengajuan yang statusnya DISBURSED - ringkasan dana
// yang cair + cicilan + rekening tujuan. Pengajuan yang masih diproses/ditolak dibuka ke
// Detail Status Pengajuan (history_detail), bukan ke sini.
@Composable
fun NotifikasiDetailScreen(
    notificationId: String,
    onBack: () -> Unit = {},
    onLihatTagihan: () -> Unit = {},
    // Tinggi navbar mengambang (lihat MainScreen) - ruang ekstra di bawah konten scroll.
    bottomInset: Dp = 0.dp,
    viewModel: NotifikasiDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(notificationId) { viewModel.load(notificationId) }
    NotifikasiDetailContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onLihatTagihan = onLihatTagihan,
        bottomInset = bottomInset
    )
}

@Composable
private fun NotifikasiDetailContent(
    uiState: NotifikasiDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onLihatTagihan: () -> Unit,
    bottomInset: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackgroundTop()
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(top = 8.dp, bottom = 32.dp + bottomInset)
        ) {
            Row(modifier = Modifier.screenTitleInset(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = "Detail Pencairan",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            val pengajuan = uiState.notification?.pengajuan
            when {
                uiState.isLoading -> Box(Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlobDark)
                }
                uiState.errorMessage != null || pengajuan == null -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GlassFill)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        uiState.errorMessage ?: "Data pinjaman tidak tersedia",
                        color = Color.White.copy(alpha = 0.8f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 12.5.sp
                    )
                    Text(
                        "Coba lagi",
                        color = BlobDark,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        modifier = Modifier.clickable(onClick = onRetry)
                    )
                }
                else -> DisbursementSummary(pengajuan = pengajuan, profile = uiState.profile, onLihatTagihan = onLihatTagihan)
            }
        }
    }
}

@Composable
private fun DisbursementSummary(
    pengajuan: NotificationPengajuanRef,
    profile: CustomerMeResponse?,
    onLihatTagihan: () -> Unit
) {
    val nominal = pengajuan.nominalDisetujui ?: pengajuan.nominalPengajuan ?: 0.0
    val tenor = pengajuan.tenor
    val rate = pengajuan.interestRate
    val cicilan = if (tenor != null && tenor > 0 && rate != null) LoanCalculator.estimate(nominal, tenor, rate).cicilanBulanan else null
    val nextDue = pengajuan.tanggalPencairan?.let { LoanCalculator.nextDueDateLabel(it) }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(BlobDark.copy(alpha = 0.16f))
                .border(1.dp, BlobDark.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Verified, contentDescription = null, tint = BlobDark, modifier = Modifier.size(44.dp))
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "Dana berhasil dicairkan",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Pinjamanmu sudah dikirim ke rekening terdaftar",
            color = Color.White.copy(alpha = 0.6f),
            fontFamily = PlusJakartaSans,
            fontSize = 12.5.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            LoanCalculator.formatRupiah(nominal),
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        PengajuanStepper(status = pengajuan.status ?: "DISBURSED")

        Spacer(modifier = Modifier.height(22.dp))

        // Ringkasan 2x2 - sengaja cuma jatuh tempo BERIKUTNYA, jadwal lengkap ada di layar Bayar.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCell("Tenor", tenor?.let { "$it bulan" } ?: "-", Modifier.weight(1f))
                SummaryCell("Bunga", rate?.let { "${formatPercent(it)}%" } ?: "-", Modifier.weight(1f), alignEnd = true)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCell("Cicilan / bulan", cicilan?.let { LoanCalculator.formatRupiah(it) } ?: "-", Modifier.weight(1f))
                SummaryCell("Jatuh tempo berikutnya", nextDue ?: "-", Modifier.weight(1f), alignEnd = true)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(GlassFill)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
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
                Text("Rekening tujuan", color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
                Text(
                    text = rekeningLabel(profile),
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
                profile?.namaPemilikRekening?.takeIf { it.isNotBlank() }?.let {
                    Text("a.n. $it", color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(text = "Lihat Tagihan", onClick = onLihatTagihan)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "ID Pengajuan ${LoanCalculator.formatPengajuanRef(pengajuan.id)}",
            color = Color.White.copy(alpha = 0.4f),
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun SummaryCell(label: String, value: String, modifier: Modifier = Modifier, alignEnd: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

// Nomor rekening disamarkan, cukup 4 digit terakhir.
private fun rekeningLabel(profile: CustomerMeResponse?): String {
    val bank = profile?.namaBank?.takeIf { it.isNotBlank() }
    val nomor = profile?.nomorRekening?.takeIf { it.isNotBlank() }
    return when {
        bank != null && nomor != null -> "$bank •••• ${nomor.takeLast(4)}"
        bank != null -> bank
        else -> "Rekening terdaftar"
    }
}

// interestRate dari API itu angka persen mentah (3.0 = "3%"), bukan pecahan 0-1.
private fun formatPercent(rate: Double): String =
    if (rate == rate.toInt().toDouble()) rate.toInt().toString() else "%.1f".format(rate)

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NotifikasiDetailPreview() {
    SakukuTheme {
        NotifikasiDetailContent(
            uiState = NotifikasiDetailUiState(
                isLoading = false,
                notification = NotificationResponse(
                    id = "n1",
                    judul = "Dana pinjaman sudah cair",
                    pesan = "Dana sudah dikirim ke rekeningmu",
                    isRead = true,
                    pengajuan = NotificationPengajuanRef(
                        id = "5f2d8959-7906-4a0a-a915-40486f3d8f2d",
                        status = "DISBURSED",
                        nominalPengajuan = 8_000_000.0,
                        nominalDisetujui = 7_500_000.0,
                        tenor = 12,
                        interestRate = 8.0,
                        tanggalPencairan = "2026-09-15T10:00:00"
                    )
                ),
                profile = CustomerMeResponse(
                    id = "c1",
                    namaLengkap = "Vilia",
                    namaBank = "BCA",
                    nomorRekening = "1234567890",
                    namaPemilikRekening = "Vilia"
                )
            ),
            onBack = {}, onRetry = {}, onLihatTagihan = {}
        )
    }
}
