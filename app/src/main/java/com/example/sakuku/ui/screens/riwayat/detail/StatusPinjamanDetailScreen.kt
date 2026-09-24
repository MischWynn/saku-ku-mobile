package com.example.sakuku.ui.screens.riwayat.detail

import com.example.sakuku.ui.theme.sakukuBlobBackgroundTop
import com.example.sakuku.ui.components.PengajuanStepper
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.PengajuanHistoryResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.ui.screens.riwayat.StatusBadge
import com.example.sakuku.ui.screens.riwayat.formatTanggal
import com.example.sakuku.ui.screens.riwayat.historyActionLabel
import com.example.sakuku.ui.screens.riwayat.tujuanLabel
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val RejectRed = Color(0xFFF28FA0)

@Composable
fun StatusPinjamanDetailScreen(
    id: String,
    onBack: () -> Unit = {},
    viewModel: StatusPinjamanDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(id) { viewModel.load(id) }

    StatusPinjamanDetailScreenContent(uiState = uiState, onRetry = viewModel::retry, onBack = onBack)
}

@Composable
private fun StatusPinjamanDetailScreenContent(
    uiState: StatusPinjamanDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackgroundTop()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(bottom = 32.dp)
        ) {
            Row(modifier = Modifier.screenTitleInset(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = "Detail Pengajuan",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlobDark)
                    }
                }
                uiState.errorMessage != null -> {
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
                }
                uiState.item != null -> {
                    val item = uiState.item
                    SummaryCard(item)

                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(GlassFill)
                            .padding(18.dp)
                    ) {
                        Text(
                            text = "Tahap Pengajuan",
                            color = Color.White.copy(alpha = 0.6f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        PengajuanStepper(status = item.status)
                    }

                    uiState.rejectionReason?.let { reason ->
                        Spacer(modifier = Modifier.height(16.dp))
                        RejectionCard(reason)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Riwayat Proses",
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.history.isEmpty()) {
                        Text(
                            "Belum ada riwayat proses",
                            color = Color.White.copy(alpha = 0.5f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 12.5.sp
                        )
                    } else {
                        Timeline(uiState.history)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(item: PengajuanMeResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    text = LoanCalculator.formatRupiah(item.nominalDisetujui ?: item.nominalPengajuan),
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "No. Pengajuan ${LoanCalculator.formatPengajuanRef(item.id)}",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                )
            }
            StatusBadge(item.status)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryField("Tenor", "${item.tenor} Bulan")
            SummaryField("Diajukan", formatTanggal(item.tanggalPengajuan))
        }
        item.tujuanPinjaman?.let {
            SummaryField("Tujuan Pinjaman", tujuanLabel(it))
        }
    }
}

@Composable
private fun SummaryField(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.5.sp)
        Text(value, color = Color.White.copy(alpha = 0.85f), fontFamily = PlusJakartaSans, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
private fun RejectionCard(reason: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RejectRed.copy(alpha = 0.10f))
            .border(1.dp, RejectRed.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ALASAN PENOLAKAN",
            color = RejectRed,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 10.5.sp
        )
        Text(
            text = reason.ifBlank { "Tidak ada catatan tambahan dari tim review." },
            color = Color.White.copy(alpha = 0.9f),
            fontFamily = PlusJakartaSans,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun Timeline(history: List<PengajuanHistoryResponse>) {
    Column {
        history.forEachIndexed { index, entry ->
            TimelineRow(entry = entry, isLast = index == history.lastIndex)
        }
    }
}

@Composable
private fun TimelineRow(entry: PengajuanHistoryResponse, isLast: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (entry.action == "REJECT") RejectRed else BlobDark)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.5.dp)
                        .height(48.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = 18.dp)) {
            Text(
                text = historyActionLabel(entry.action, entry.roleName),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Text(
                text = formatTanggal(entry.createdAt),
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp
            )
            entry.catatan?.takeIf { it.isNotBlank() }?.let { catatan ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"$catatan\"",
                    color = Color.White.copy(alpha = 0.6f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun StatusPinjamanDetailScreenPreview() {
    SakukuTheme {
        StatusPinjamanDetailScreenContent(
            uiState = StatusPinjamanDetailUiState(
                isLoading = false,
                item = PengajuanMeResponse("1", 2_000_000.0, 12, 8.0, null, "MARKETING_REJECTED", "MODAL_USAHA", "2026-09-01T09:00:00"),
                history = listOf(
                    PengajuanHistoryResponse("REJECT", "MARKETING_REVIEW", "MARKETING_REJECTED", "Dokumen pendapatan tidak sesuai", "MARKETING", "2026-09-02T10:15:00")
                )
            ),
            onRetry = {},
            onBack = {}
        )
    }
}
