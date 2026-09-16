package com.example.sakuku.ui.screens.profil

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.screens.register.TipePekerjaan
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground

// Form beneran (nama/kontak/pekerjaan) - dipindah ke sini dari ProfilScreen biar overview-nya
// gak "fully form". Dibuka dari 3 menu item Data Pribadi (e-KYC) sekaligus (KTP, Kontak, Data
// Pekerjaan) - datanya disimpan bareng lewat 1 PATCH customer/me, jadi sengaja 1 form gabungan
// bukan dipecah 3 layar terpisah.
@Composable
fun EditDataDiriScreen(
    onBack: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    EditDataDiriContent(
        uiState = uiState,
        onBack = onBack,
        onNamaLengkapChange = viewModel::onNamaLengkapChange,
        onEmailChange = viewModel::onEmailChange,
        onNoHpChange = viewModel::onNoHpChange,
        onAlamatChange = viewModel::onAlamatChange,
        onTipePekerjaanChange = viewModel::onTipePekerjaanChange,
        onPekerjaanChange = viewModel::onPekerjaanChange,
        onPendapatanChange = viewModel::onPendapatanChange,
        onSave = viewModel::save
    )
}

@Composable
private fun EditDataDiriContent(
    uiState: ProfilUiState,
    onBack: () -> Unit,
    onNamaLengkapChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
    onTipePekerjaanChange: (TipePekerjaan) -> Unit,
    onPekerjaanChange: (String) -> Unit,
    onPendapatanChange: (String) -> Unit,
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
                .padding(horizontal = 20.dp)
                // bottom 120dp (bukan 32dp) - nyamain pola ProfilScreen.kt, floating
                // AnimatedBottomNavBar butuh clearance segitu biar konten paling bawah gak
                // ketutup nav bar-nya.
                .padding(top = 24.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Data Pribadi", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

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

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                FieldLabel("NIK")
                SakukuOutlinedField(value = uiState.nik, onValueChange = {}, keyboardType = KeyboardType.Number, modifier = Modifier)
                Text(
                    text = "NIK tidak dapat diubah.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp
                )

                FieldLabel("Nama Lengkap")
                SakukuOutlinedField(value = uiState.namaLengkap, onValueChange = onNamaLengkapChange, keyboardType = KeyboardType.Text)

                FieldLabel("Email")
                SakukuOutlinedField(value = uiState.email, onValueChange = onEmailChange, keyboardType = KeyboardType.Email)

                FieldLabel("Nomor HP")
                SakukuOutlinedField(value = uiState.noHp, onValueChange = onNoHpChange, keyboardType = KeyboardType.Phone)

                FieldLabel("Alamat")
                SakukuOutlinedField(value = uiState.alamat, onValueChange = onAlamatChange, keyboardType = KeyboardType.Text)

                Column {
                    FieldLabel("Sektor Pekerjaan")
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TipePekerjaan.entries.forEach { t ->
                            SelectableChip(
                                label = t.label,
                                isSelected = t == uiState.tipePekerjaan,
                                onClick = { onTipePekerjaanChange(t) }
                            )
                        }
                    }
                }

                FieldLabel("Jabatan / Pekerjaan")
                SakukuOutlinedField(value = uiState.pekerjaan, onValueChange = onPekerjaanChange, keyboardType = KeyboardType.Text)

                FieldLabel("Pendapatan Bulanan")
                SakukuOutlinedField(value = uiState.pendapatanBulanan, onValueChange = onPendapatanChange, keyboardType = KeyboardType.Number)

                Text(
                    text = "Mengubah data ini belum otomatis mengubah plafond kamu.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp
                )
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage, color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            GradientButton(text = "Simpan Perubahan", onClick = onSave, isLoading = uiState.isSaving)
        }
    }
}

@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun EditDataDiriScreenPreview() {
    SakukuTheme {
        EditDataDiriContent(
            uiState = ProfilUiState(
                isLoading = false,
                namaLengkap = "Novita Sari",
                nik = "3273010101990016",
                email = "novita.sari@mail.com",
                noHp = "081234560016",
                alamat = "Jl. Ahmad Yani No. 16, Bandung",
                tipePekerjaan = TipePekerjaan.SWASTA,
                pekerjaan = "Staff Admin",
                pendapatanBulanan = "5000000"
            ),
            onBack = {},
            onNamaLengkapChange = {},
            onEmailChange = {},
            onNoHpChange = {},
            onAlamatChange = {},
            onTipePekerjaanChange = {},
            onPekerjaanChange = {},
            onPendapatanChange = {},
            onSave = {}
        )
    }
}
