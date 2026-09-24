package com.example.sakuku.ui.screens.profil

import com.example.sakuku.ui.theme.screenTitleInset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.RupiahVisualTransformation
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.components.SelectableChip
import com.example.sakuku.ui.screens.register.TipePekerjaan
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)

@Composable
fun DataPekerjaanScreen(
    onBack: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    DataPekerjaanContent(
        uiState = uiState,
        onBack = onBack,
        onTipePekerjaanChange = viewModel::onTipePekerjaanChange,
        onPekerjaanChange = viewModel::onPekerjaanChange,
        onPendapatanChange = viewModel::onPendapatanChange,
        onSave = viewModel::save
    )
}

@Composable
private fun DataPekerjaanContent(
    uiState: ProfilUiState,
    onBack: () -> Unit,
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
        // PERBAIKAN 1: Menggunakan if-else untuk loading state
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BlobDark)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding.Horizontal)
                    .padding(top = 24.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header (Tombol Back & Judul)
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
                        text = "Data Pekerjaan",
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                // PERBAIKAN 2: Glass Container (Menambahkan kurung kurawal pembuka { )
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

                    // Label dan Input dibungkus Column agar tidak renggang terpisah oleh spacedBy(16.dp)
                    Column {
                        FieldLabel("Jabatan / Pekerjaan")
                        SakukuOutlinedField(
                            value = uiState.pekerjaan,
                            onValueChange = onPekerjaanChange,
                            keyboardType = KeyboardType.Text
                        )
                    }

                    Column {
                        FieldLabel("Pendapatan Bulanan")
                        SakukuOutlinedField(
                            value = uiState.pendapatanBulanan,
                            onValueChange = onPendapatanChange,
                            keyboardType = KeyboardType.Number,
                            visualTransformation = RupiahVisualTransformation(),
                            placeholder = "Contoh: 5000000"
                        )
                    }

                    Text(
                        text = "Mengubah data ini belum otomatis mengubah plafond kamu.",
                        color = Color.White.copy(alpha = 0.4f),
                        fontFamily = PlusJakartaSans,
                        fontSize = 10.sp
                    )
                }
                if (uiState.successMessage != null) {
                    Text(
                        text = uiState.successMessage,
                        color = BlobDark,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                GradientButton(
                    text = "Simpan Perubahan",
                    onClick = onSave,
                    isLoading = uiState.isSaving
                )
            }
        }
    }
}

