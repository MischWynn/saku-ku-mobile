package com.example.sakuku.ui.screens.profil

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

// Rekening tujuan pencairan dana - sebelumnya "coming soon" (ProfilPlaceholderScreens.kt),
// sekarang beneran nyambung ke customer/me. Pola sama persis kayak KontakScreen.kt: 1
// ViewModel (ProfilViewModel) & 1 PATCH customer/me yang sama, cuma UI-nya section sendiri.
@Composable
fun RekeningBankScreen(
    onBack: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RekeningBankContent(
        uiState = uiState,
        onBack = onBack,
        onNamaBankChange = viewModel::onNamaBankChange,
        onNomorRekeningChange = viewModel::onNomorRekeningChange,
        onNamaPemilikRekeningChange = viewModel::onNamaPemilikRekeningChange,
        onSave = viewModel::save
    )
}

@Composable
private fun RekeningBankContent(
    uiState: ProfilUiState,
    onBack: () -> Unit,
    onNamaBankChange: (String) -> Unit,
    onNomorRekeningChange: (String) -> Unit,
    onNamaPemilikRekeningChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlobDark)
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.screenTitleInset().fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Rekening Bank",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Rekening ini yang bakal dipakai buat pencairan dana pinjaman kamu.",
                color = Color.White.copy(alpha = 0.5f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.sp
            )

            if (uiState.errorMessage != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFEF5876).copy(alpha = 0.12f))
                        .padding(14.dp)
                ) {
                    Text(uiState.errorMessage, color = Color(0xFFF28FA0), fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassFill)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    FieldLabel("Nama Bank")
                    SakukuOutlinedField(
                        value = uiState.namaBank,
                        onValueChange = onNamaBankChange,
                        keyboardType = KeyboardType.Text,
                        placeholder = "Contoh: BCA, BRI, Mandiri"
                    )
                }
                Column {
                    FieldLabel("Nomor Rekening")
                    SakukuOutlinedField(
                        value = uiState.nomorRekening,
                        onValueChange = onNomorRekeningChange,
                        keyboardType = KeyboardType.Number
                    )
                }
                Column {
                    FieldLabel("Nama Pemilik Rekening")
                    SakukuOutlinedField(
                        value = uiState.namaPemilikRekening,
                        onValueChange = onNamaPemilikRekeningChange,
                        keyboardType = KeyboardType.Text,
                        placeholder = "Sesuai buku tabungan/kartu ATM"
                    )
                }
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage, color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))
            GradientButton(text = "Simpan Perubahan", onClick = onSave, isLoading = uiState.isSaving)
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun RekeningBankScreenPreview() {
    SakukuTheme {
        RekeningBankContent(
            uiState = ProfilUiState(
                isLoading = false,
                namaBank = "BCA",
                nomorRekening = "1234567890",
                namaPemilikRekening = "Novita Sari"
            ),
            onBack = {},
            onNamaBankChange = {},
            onNomorRekeningChange = {},
            onNamaPemilikRekeningChange = {},
            onSave = {}
        )
    }
}
