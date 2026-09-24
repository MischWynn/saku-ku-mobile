package com.example.sakuku.ui.screens.bayar

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobLight
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun BayarScreen(
    onBack: () -> Unit = {},
    viewModel: BayarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    BayarScreenContent(uiState = uiState, onRetry = viewModel::load, onBack = onBack)
}

@Composable
private fun BayarScreenContent(
    uiState: BayarUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = ScreenPadding.Horizontal).padding(top = 8.dp)) {
            Row(modifier = Modifier.screenTitleInset(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Text(
                    text = "Tagihan",
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
                uiState.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Belum ada pinjaman aktif yang dicairkan",
                            color = Color.White.copy(alpha = 0.6f),
                            fontFamily = PlusJakartaSans,
                            fontSize = 13.sp
                        )
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.items) { item -> TagihanCard(item) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagihanCard(item: TagihanItem) {
    val loan = item.loan

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(
                    text = "TAGIHAN BULAN INI",
                    color = Color.White.copy(alpha = 0.45f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = LoanCalculator.formatRupiah(item.cicilanBulanan),
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Jatuh Tempo", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
                Text(
                    text = item.dueDateLabel ?: "Belum tersedia",
                    color = if (item.dueDateLabel != null) BlobLight else Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Pinjaman Pokok", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.5.sp)
                Text(
                    LoanCalculator.formatRupiah(loan.nominalDisetujui ?: loan.nominalPengajuan),
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Tenor", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.5.sp)
                Text(
                    "${loan.tenor} Bulan",
                    color = Color.White.copy(alpha = 0.85f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            }
        }

        Text(
            text = "*Estimasi, bukan pembukuan resmi. Dihitung otomatis dari data pinjamanmu.",
            color = Color.White.copy(alpha = 0.3f),
            fontFamily = PlusJakartaSans,
            fontSize = 9.5.sp,
            lineHeight = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun BayarScreenPreview() {
    SakukuTheme {
        BayarScreenContent(
            uiState = BayarUiState(
                isLoading = false,
                items = listOf(
                    TagihanItem(
                        loan = PengajuanMeResponse("1", 5_000_000.0, 12, 8.0, 5_000_000.0, "DISBURSED", "MODAL_USAHA", "2026-08-01T09:00:00", "2026-08-05T09:00:00"),
                        cicilanBulanan = 450_000.0,
                        dueDateLabel = "5 Okt 2026"
                    )
                )
            ),
            onRetry = {},
            onBack = {}
        )
    }
}
