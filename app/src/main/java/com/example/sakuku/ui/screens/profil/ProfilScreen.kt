package com.example.sakuku.ui.screens.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.screens.register.TipePekerjaan
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.util.LoanCalculator

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun ProfilScreen(
    onLoggedOut: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    ProfilScreenContent(
        uiState = uiState,
        onNamaLengkapChange = viewModel::onNamaLengkapChange,
        onEmailChange = viewModel::onEmailChange,
        onNoHpChange = viewModel::onNoHpChange,
        onAlamatChange = viewModel::onAlamatChange,
        onTipePekerjaanChange = viewModel::onTipePekerjaanChange,
        onPekerjaanChange = viewModel::onPekerjaanChange,
        onPendapatanChange = viewModel::onPendapatanChange,
        onSave = viewModel::save,
        onRetry = viewModel::load,
        onLogout = viewModel::logout
    )
}

@Composable
private fun ProfilScreenContent(
    uiState: ProfilUiState,
    onNamaLengkapChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
    onTipePekerjaanChange: (TipePekerjaan) -> Unit,
    onPekerjaanChange: (String) -> Unit,
    onPendapatanChange: (String) -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit
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
                .padding(top = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Profil",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
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

            RingkasanCard(uiState)

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                    "Mengubah data ini belum otomatis mengubah plafond kamu.",
                    color = Color.White.copy(alpha = 0.4f),
                    fontFamily = PlusJakartaSans,
                    fontSize = 10.sp
                )
            }

            if (uiState.successMessage != null) {
                Text(uiState.successMessage, color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }

            GradientButton(text = "Simpan Perubahan", onClick = onSave, isLoading = uiState.isSaving)

            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF28FA0)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF28FA0).copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Keluar", fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RingkasanCard(uiState: ProfilUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassFill)
            .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(uiState.namaLengkap, color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            uiState.tierPlafond?.let {
                Text(
                    it.uppercase(),
                    color = BlobDark,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(BlobDark.copy(alpha = 0.15f))
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Plafond", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
                Text(uiState.plafond?.let { LoanCalculator.formatRupiah(it) } ?: "-", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Sisa Tersedia", color = Color.White.copy(alpha = 0.45f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
                Text(uiState.sisaPlafond?.let { LoanCalculator.formatRupiah(it) } ?: "-", color = Color.White, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1300)
@Composable
private fun ProfilScreenPreview() {
    SakukuTheme {
        ProfilScreenContent(
            uiState = ProfilUiState(
                isLoading = false,
                namaLengkap = "Novita Sari",
                email = "novita.sari@mail.com",
                noHp = "081234560016",
                alamat = "Jl. Ahmad Yani No. 16, Bandung",
                tipePekerjaan = TipePekerjaan.SWASTA,
                pekerjaan = "Staff Admin",
                pendapatanBulanan = "5000000",
                plafond = 4_000_000.0,
                sisaPlafond = 200_000.0,
                tierPlafond = "Bronze"
            ),
            onNamaLengkapChange = {}, onEmailChange = {}, onNoHpChange = {}, onAlamatChange = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanChange = {},
            onSave = {}, onRetry = {}, onLogout = {}
        )
    }
}
