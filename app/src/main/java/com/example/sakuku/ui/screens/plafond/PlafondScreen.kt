package com.example.sakuku.ui.screens.plafond

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
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

private fun tierColor(namaPlafond: String): Color = when (namaPlafond.lowercase()) {
    "bronze" -> Color(0xFFC98A4B)
    "silver" -> Color(0xFFAEBCC9)
    "gold" -> Color(0xFFE7C468)
    "platinum" -> Color(0xFF8FD4E6)
    else -> Color.White.copy(alpha = 0.6f)
}

@Composable
fun PlafondScreen(
    onBack: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    viewModel: PlafondViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    PlafondScreenContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
private fun PlafondScreenContent(
    uiState: PlafondUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onNavigateToRegister: () -> Unit
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
                .padding(top = 20.dp, bottom = 32.dp)
        ) {
            Row(modifier = Modifier.screenTitleInset(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Daftar Plafond",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Limit dihitung otomatis dari profilmu saat daftar.",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

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
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        uiState.tiers.forEach { tier -> TierFullCard(tier) }
                    }

                    if (!uiState.isLoggedIn) {
                        Spacer(modifier = Modifier.height(22.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(GlassFill)
                                .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Daftar sekarang & lengkapi profilmu untuk lihat plafond milikmu sendiri.",
                                color = Color.White.copy(alpha = 0.75f),
                                fontFamily = PlusJakartaSans,
                                fontSize = 12.5.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 17.sp
                            )
                            GradientButton(
                                text = "Daftar untuk cek plafondmu",
                                onClick = onNavigateToRegister
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierFullCard(tier: PlafondResponse) {
    val accent = tierColor(tier.namaPlafond)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tier.namaPlafond.take(1).uppercase(),
                        color = accent,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(tier.namaPlafond, color = accent, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(
                text = "hingga ${LoanCalculator.formatRupiah(tier.limitMaksimal)}",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        tier.deskripsi?.let {
            Text(it, color = Color.White.copy(alpha = 0.55f), fontFamily = PlusJakartaSans, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PlafondScreenPreview() {
    SakukuTheme {
        PlafondScreenContent(
            uiState = PlafondUiState(
                isLoading = false,
                tiers = listOf(
                    PlafondResponse("1", "Bronze", "Tier minimum", "REGULER", 2_000_000.0, "ACTIVE"),
                    PlafondResponse("2", "Silver", "Tier menengah bawah", "REGULER", 7_500_000.0, "ACTIVE"),
                    PlafondResponse("3", "Gold", "Tier menengah atas", "REGULER", 15_000_000.0, "ACTIVE"),
                    PlafondResponse("4", "Platinum", "Tier tertinggi", "REGULER", 50_000_000.0, "ACTIVE")
                )
            ),
            onBack = {},
            onRetry = {},
            onNavigateToRegister = {}
        )
    }
}
