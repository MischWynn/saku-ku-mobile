package com.example.sakuku.ui.screens.profil

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val GlassFill = Color.White.copy(alpha = 0.05f)
private val GlassBorder = Color.White.copy(alpha = 0.12f)
private val ID_LOCALE = Locale.Builder().setLanguage("id").setRegion("ID").build()
private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy", ID_LOCALE)

// Contoh pola buat 3 layar Data Pribadi yang dipisah dari "EditDataDiriScreen" gabungan lama
// (KTP & Data Diri / Kontak / Data Pekerjaan) - screen ini KTP & Data Diri-nya, jadi acuan buat
// 2 layar lain (dibikin manual, sengaja gak sekalian biar bisa dicoba sendiri). Tetap 1
// ViewModel (ProfilViewModel) & 1 PATCH customer/me yang sama - cuma UI-nya yang dipecah per
// kategori biar tiap layar scannable, bukan 1 form panjang gabung semua.
@Composable
fun KtpDataDiriScreen(
    onBack: () -> Unit = {},
    viewModel: ProfilViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    KtpDataDiriContent(
        uiState = uiState,
        onBack = onBack,
        onNamaLengkapChange = viewModel::onNamaLengkapChange,
        onTanggalLahirChange = viewModel::onTanggalLahirChange,
        onAlamatChange = viewModel::onAlamatChange,
        onSave = viewModel::save
    )
}

@Composable
private fun KtpDataDiriContent(
    uiState: ProfilUiState,
    onBack: () -> Unit,
    onNamaLengkapChange: (String) -> Unit,
    onTanggalLahirChange: (String) -> Unit,
    onAlamatChange: (String) -> Unit,
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
                Text(
                    text = "KTP & Data Diri",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontSize = 20.sp
                )
            }

            // Strip identitas - NIK masked + badge "Terverifikasi", nempel di atas biar konteks
            // "ini data e-KYC kamu" langsung kebaca sebelum masuk ke field yang bisa diedit.
            NikIdentityStrip(nik = uiState.nik)

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
                    FieldLabel("Nama Lengkap")
                    SakukuOutlinedField(value = uiState.namaLengkap, onValueChange = onNamaLengkapChange, keyboardType = KeyboardType.Text)
                }

                Column {
                    FieldLabel("Tanggal Lahir")
                    TanggalLahirField(value = uiState.tanggalLahir, onValueChange = onTanggalLahirChange)
                }

                Column {
                    FieldLabel("Domisili")
                    SakukuOutlinedField(
                        value = uiState.alamat,
                        onValueChange = onAlamatChange,
                        keyboardType = KeyboardType.Text,
                        singleLine = false,
                        leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White.copy(alpha = 0.5f)) },
                        modifier = Modifier.height(96.dp)
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

@Composable
private fun NikIdentityStrip(nik: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BlobMid.copy(alpha = 0.12f))
            .border(1.dp, BlobDark.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = BlobDark, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text("NIK", color = Color.White.copy(alpha = 0.5f), fontFamily = PlusJakartaSans, fontSize = 10.sp)
                Text(
                    text = if (nik.length >= 4) "•••• •••• ${nik.takeLast(4)}" else "Belum tersedia",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
        // Badge "Terverifikasi" cuma muncul kalau NIK beneran ada datanya - sebelumnya statis
        // selalu nyala walau NIK masih "Belum tersedia", nyesatin (KTP-nya sendiri belum ada
        // proses verifikasi apapun, cuma data NIK polos dari registrasi).
        if (nik.length >= 4) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = BlobDark, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Terverifikasi", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
            }
        }
    }
}

// Field tanggal lahir - tampil kayak outlined field biasa (konsisten sama field lain), tapi
// read-only + clickable buat buka DatePickerDialog Material3, bukan keyboard manual. Value
// disimpen/dikirim sebagai ISO "yyyy-MM-dd" (format yang diterima CustomerUpdateRequest
// backend), ditampilin dalam format lokal "14 Mei 1998" (DISPLAY_DATE_FORMATTER).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TanggalLahirField(value: String, onValueChange: (String) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val parsedDate = remember(value) { value.toLocalDateOrNull() }
    val displayText = parsedDate?.format(DISPLAY_DATE_FORMATTER) ?: ""

    // OutlinedTextField (bahkan readOnly=true) nyerep touch event-nya sendiri buat gesture
    // cursor/text-selection, jadi Modifier.clickable yang ditumpuk di atasnya sering gak
    // kepanggil. Fix: field-nya di-disable total (gak pernah dapet fokus/gesture internal) +
    // Box transparan di atasnya yang beneran nangkep klik, warnanya di-override manual biar
    // gak keliatan "disabled" (abu-abu redup) kayak default Material3.
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            enabled = false,
            placeholder = { Text("Pilih tanggal lahir", color = Color.White.copy(alpha = 0.35f), fontFamily = PlusJakartaSans) },
            trailingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = BlobMid.copy(alpha = 0.5f),
                disabledTextColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.03f),
                disabledPlaceholderColor = Color.White.copy(alpha = 0.35f),
                disabledTrailingIconColor = Color.White.copy(alpha = 0.6f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = parsedDate?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        onValueChange(picked.toString())
                    }
                    showPicker = false
                }) {
                    Text("Pilih", color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f), fontFamily = PlusJakartaSans)
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = try {
    if (isBlank()) null else LocalDate.parse(this)
} catch (e: Exception) {
    null
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun KtpDataDiriScreenPreview() {
    SakukuTheme {
        KtpDataDiriContent(
            uiState = ProfilUiState(
                isLoading = false,
                namaLengkap = "Novita Sari",
                nik = "3273010101990016",
                tanggalLahir = "1999-01-01",
                alamat = "Jl. Ahmad Yani No. 16, Bandung"
            ),
            onBack = {},
            onNamaLengkapChange = {},
            onTanggalLahirChange = {},
            onAlamatChange = {},
            onSave = {}
        )
    }
}
