package com.example.sakuku.ui.screens.riwayat

import com.example.sakuku.ui.theme.sakukuBlobBackgroundTop
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.PengajuanButtonGradient
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun RiwayatScreen(
    onNavigateToApply: () -> Unit = {},
    onItemClick: (id: String) -> Unit = {},
    // Tinggi navbar mengambang - layar digambar sampai belakang navbar (background gak kepotong),
    // list-nya dikasih ruang segini di bawah biar kartu terakhir gak ketutup. Lihat MainScreen.
    bottomInset: Dp = 0.dp,
    viewModel: RiwayatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(
        Unit
    ) {
        viewModel.load()
    }
    RiwayatScreenContent(
        uiState = uiState,
        onRetry = viewModel::load,
        onNavigateToApply = onNavigateToApply,
        onItemClick = onItemClick,
        bottomInset = bottomInset
    )
}

@Composable
private fun RiwayatScreenContent(
    uiState: RiwayatUiState,
    onRetry: () -> Unit,
    onNavigateToApply: () -> Unit,
    onItemClick: (id: String) -> Unit,
    bottomInset: Dp = 0.dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackgroundTop()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(top = 24.dp)
        ) {
            Text(
                text = "Riwayat Pengajuan",
                modifier = Modifier.screenTitleInset(),
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(20.dp))

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlobDark)
                    }
                }
                // items.isNotEmpty() dicek DULUAN, sebelum errorMessage - offline-first: kalau
                // sinkronisasi background gagal (mis. gak ada internet) tapi cache Room udah ada
                // isinya dari sesi sebelumnya, list itu tetap harus keliatan, bukan ketutup kartu
                // error kayak sebelumnya (dulu errorMessage != null selalu menang duluan).
                uiState.items.isNotEmpty() -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (uiState.errorMessage != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GlassFill)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Gagal sinkron, nampilin data tersimpan",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontFamily = PlusJakartaSans,
                                    fontSize = 11.5.sp
                                )
                                Text(
                                    "Coba lagi",
                                    color = BlobDark,
                                    fontFamily = PlusJakartaSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.clickable(onClick = onRetry)
                                )
                            }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = bottomInset + 24.dp)
                        ) {
                            items(uiState.items) { item ->
                                RiwayatCard(item, onClick = { onItemClick(item.id) })
                            }
                        }
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
                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Belum ada pengajuan",
                            color = Color.White.copy(alpha = 0.6f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        GradientButton(text = "Ajukan Pinjaman", onClick = onNavigateToApply, gradient = PengajuanButtonGradient)
                    }
                }
            }
        }
    }
}

@Composable
private fun RiwayatCard(item: PengajuanMeResponse, onClick: () -> Unit) {
    val estimate = LoanCalculator.estimate(item.nominalDisetujui ?: item.nominalPengajuan, item.tenor, item.interestRate)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            // weight(1f) biar kolom nominal+ID gak kejepit sama badge status di kanan.
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = LoanCalculator.formatRupiah(item.nominalPengajuan),
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LoanCalculator.formatPengajuanRef(item.id),
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 11.sp
                )
            }
            StatusBadge(item.status)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${item.tenor} Bulan${item.tujuanPinjaman?.let { " - ${tujuanLabel(it)}" } ?: ""}",
                color = Color.White.copy(alpha = 0.55f),
                fontFamily = PlusJakartaSans,
                fontSize = 11.5.sp,
                // weight: tujuan pinjaman yang panjang wrap sendiri, tanggal gak kejepit.
                modifier = Modifier.weight(1f).padding(end = 12.dp)
            )
            Text(
                formatTanggal(item.tanggalPengajuan),
                color = Color.White.copy(alpha = 0.4f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp
            )
        }
        if (item.status == "DISBURSED" || item.status == "BACKOFFICE_REVIEW") {
            Text(
                "Cicilan estimasi Rp/bulan: ${LoanCalculator.formatRupiah(estimate.cicilanBulanan)}",
                color = Color.White.copy(alpha = 0.45f),
                fontFamily = PlusJakartaSans,
                fontSize = 10.5.sp
            )
        }
    }
}

@Composable
internal fun StatusBadge(status: String) {
    val color = statusColor(status)
    Text(
        text = statusLabel(status),
        color = color,
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun RiwayatScreenPreview() {
    SakukuTheme {
        RiwayatScreenContent(
            uiState = RiwayatUiState(
                isLoading = false,
                items = listOf(
                    PengajuanMeResponse("1", 3_800_000.0, 6, 3.0, 3_800_000.0, "BACKOFFICE_REVIEW", null, "2026-08-28T11:07:40"),
                    PengajuanMeResponse("2", 2_000_000.0, 12, 8.0, null, "MARKETING_REVIEW", "MODAL_USAHA", "2026-09-01T09:00:00")
                )
            ),
            onRetry = {},
            onNavigateToApply = {},
            onItemClick = {}
        )
    }
}
