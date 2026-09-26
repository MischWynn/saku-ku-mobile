@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.screens.register

import com.example.sakuku.ui.theme.ScreenPadding
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.res.stringResource
import com.example.sakuku.R
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.data.remote.dto.WilayahItem
import com.example.sakuku.ui.components.AuthButtonGradient
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.KtpPhotoCapture
import com.example.sakuku.ui.components.OtpCodeInput
import com.example.sakuku.ui.components.RegionDropdownField
import com.example.sakuku.ui.components.RupiahVisualTransformation
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.components.TanggalLahirField
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme

// Register direstrukturisasi 17 Sept, URUTAN BARU jadi 4 step: Akun -> Verifikasi OTP ->
// Identitas (Foto KTP+NIK+Domisili+DOB) -> Data Pekerjaan. Sebelumnya NIK ada di step Akun dan
// Data Pekerjaan ada SEBELUM Foto KTP - dibalik. onRegisterSuccess sekarang baru terpicu abis
// Step 3 (Data Pekerjaan) selesai, bukan lagi Step 2 (Foto KTP).
@Composable
fun RegisterScreen(
    onRegisterSuccess: (namaLengkap: String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onBack: () -> Unit = {},
    googleEmail: String? = null,
    googleName: String? = null,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)

    LaunchedEffect(googleEmail) {
        if (googleEmail != null) viewModel.prefillFromGoogle(googleEmail, googleName)
    }

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) onRegisterSuccess(uiState.namaLengkap)
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    RegisterScreenContent(
        onGoogleSignInClick = { viewModel.signInWithGoogle(context, webClientId) },
        uiState = uiState,
        onNamaLengkapChange = viewModel::onNamaLengkapChange,
        onEmailChange = viewModel::onEmailChange,
        onNoHpChange = viewModel::onNoHpChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onAgreedToTermsChange = viewModel::onAgreedToTermsChange,
        onOtpCodeChange = viewModel::onOtpCodeChange,
        onVerifyOtp = viewModel::verifyOtp,
        onResendOtp = viewModel::resendOtp,
        onRetryAutoLogin = viewModel::retryAutoLogin,
        onNikChange = viewModel::onNikChange,
        onTanggalLahirChange = viewModel::onTanggalLahirChange,
        onProvinsiSelected = viewModel::onProvinsiSelected,
        onKotaSelected = viewModel::onKotaSelected,
        onKecamatanSelected = viewModel::onKecamatanSelected,
        onFotoKtpCaptured = viewModel::onFotoKtpCaptured,
        onRetryProvinsi = viewModel::retryFetchProvinsi,
        onSubmitIdentitas = viewModel::submitIdentitas,
        onTipePekerjaanChange = viewModel::onTipePekerjaanChange,
        onPekerjaanChange = viewModel::onPekerjaanChange,
        onPendapatanBulananChange = viewModel::onPendapatanBulananChange,
        onSubmitDataPekerjaan = viewModel::submitDataPekerjaan,
        onSubmitAccount = viewModel::submitAccount,
        onNavigateToLogin = onNavigateToLogin,
        onBack = onBack
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: RegisterUiState,
    onNamaLengkapChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAgreedToTermsChange: (Boolean) -> Unit,
    onOtpCodeChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onRetryAutoLogin: () -> Unit,
    onNikChange: (String) -> Unit,
    onTanggalLahirChange: (String) -> Unit,
    onProvinsiSelected: (WilayahItem) -> Unit,
    onKotaSelected: (WilayahItem) -> Unit,
    onKecamatanSelected: (WilayahItem) -> Unit,
    onFotoKtpCaptured: (Uri) -> Unit,
    onRetryProvinsi: () -> Unit = {},
    onSubmitIdentitas: () -> Unit,
    onTipePekerjaanChange: (TipePekerjaan) -> Unit,
    onPekerjaanChange: (String) -> Unit,
    onPendapatanBulananChange: (String) -> Unit,
    onSubmitDataPekerjaan: () -> Unit,
    onSubmitAccount: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPadding.Horizontal)
            .navigationBarsPadding()
    ) {
        // Step 0 punya tombol back (48dp) yang ngasih jarak dari status bar - step 1-3 gak punya,
        // jadi judul "Daftar" nempel ke status bar. Jarak ekstra di sini biar sejajar.
        Spacer(modifier = Modifier.height(if (uiState.currentStep == 0) 24.dp else 48.dp))

        if (uiState.currentStep == 0) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Daftar",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = when (uiState.currentStep) {
                0 -> "Mulai perjalanan finansialmu bareng kami"
                1 -> "Masukkan kode OTP yang dikirim ke ${uiState.email}"
                2 -> "Verifikasi identitas kamu - siapin KTP-mu ya"
                else -> "Terakhir, ceritain soal pekerjaanmu"
            },
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (uiState.currentStep) {
            0 -> RegisterStep1Fields(
                uiState = uiState,
                onNamaLengkapChange = onNamaLengkapChange,
                onEmailChange = onEmailChange,
                onNoHpChange = onNoHpChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onAgreedToTermsChange = onAgreedToTermsChange
            )
            1 -> RegisterStepOtp(
                uiState = uiState,
                onOtpCodeChange = onOtpCodeChange,
                onResendOtp = onResendOtp,
                onRetryAutoLogin = onRetryAutoLogin
            )
            2 -> RegisterStepIdentitas(
                uiState = uiState,
                onNikChange = onNikChange,
                onTanggalLahirChange = onTanggalLahirChange,
                onProvinsiSelected = onProvinsiSelected,
                onKotaSelected = onKotaSelected,
                onKecamatanSelected = onKecamatanSelected,
                onFotoKtpCaptured = onFotoKtpCaptured,
                onRetryProvinsi = onRetryProvinsi
            )
            else -> RegisterStepPekerjaan(
                uiState = uiState,
                onTipePekerjaanChange = onTipePekerjaanChange,
                onPekerjaanChange = onPekerjaanChange,
                onPendapatanBulananChange = onPendapatanBulananChange
            )
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
        uiState.otpInfoMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = BlobDark, fontFamily = PlusJakartaSans, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (uiState.currentStep) {
            0 -> GradientButton(
                text = "Daftar",
                onClick = onSubmitAccount,
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
            1 -> if (uiState.needsManualLoginRetry) {
                GradientButton(text = "Coba Lagi", onClick = onRetryAutoLogin, isLoading = uiState.isVerifyingOtp)
            } else {
                GradientButton(
                    text = "Verifikasi",
                    onClick = onVerifyOtp,
                    enabled = uiState.canSubmitOtp,
                    isLoading = uiState.isVerifyingOtp
                )
            }
            2 -> GradientButton(
                text = "Lanjutkan",
                onClick = onSubmitIdentitas,
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
            else -> GradientButton(
                text = "Selesai",
                onClick = onSubmitDataPekerjaan,
                enabled = !uiState.isLoading,
                isLoading = uiState.isLoading
            )
        }

        if (uiState.currentStep == 0) {
            // "Daftar dengan Google" manggil FUNGSI YANG SAMA kayak "Masuk dengan Google" di
            // LoginScreen (RegisterViewModel.signInWithGoogle(), yang di dalamnya juga manggil
            // AuthRepository.googleSignIn() yang sama) - bukan endpoint/alur terpisah. Backend
            // cuma punya 1 pintu buat ngecek "email ini udah ada akun apa belum", jadi baik dari
            // sini maupun dari Login hasilnya sama - LoggedIn (ternyata udah ada akun -> langsung
            // dianggap sukses daftar, skip form) atau NeedsRegistration (lock email, lanjut isi
            // form yang lagi kebuka ini, gak pindah layar).
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                Text(
                    text = "atau",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onGoogleSignInClick,
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(brush = AuthButtonGradient, shape = RoundedCornerShape(16.dp))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "G",
                        color = Color(0xFF4285F4),
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Daftar dengan Google",
                        color = Color.White,
                        fontFamily = PlusJakartaSans,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Punya akun? ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans
                )
                Text(
                    text = "Masuk Sekarang",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans,
                    modifier = Modifier.clickable(onClick = onNavigateToLogin)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegisterStep1Fields(
    uiState: RegisterUiState,
    onNamaLengkapChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAgreedToTermsChange: (Boolean) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    FieldLabel("Nama Lengkap")
    SakukuOutlinedField(value = uiState.namaLengkap, onValueChange = onNamaLengkapChange, keyboardType = KeyboardType.Text)

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Email")
    SakukuOutlinedField(
        value = uiState.email,
        onValueChange = onEmailChange,
        keyboardType = KeyboardType.Email,
        // Terkunci kalau datang dari "Masuk dengan Google" - email itu udah diverifikasi Google,
        // gak boleh diketik ulang jadi beda dari akun yang barusan dipakai sign-in.
        readOnly = uiState.emailLocked
    )
    if (uiState.emailLocked) {
        Text(
            text = "Email terverifikasi lewat Google",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontFamily = PlusJakartaSans,
            modifier = Modifier.padding(top = 4.dp)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Nomor Telepon")
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .height(56.dp)
                .padding(end = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+62",
                color = Color.White,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
        SakukuOutlinedField(
            value = uiState.noHp,
            onValueChange = onNoHpChange,
            keyboardType = KeyboardType.Phone,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Password")
    SakukuOutlinedField(
        value = uiState.password,
        onValueChange = onPasswordChange,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Konfirmasi Password")
    SakukuOutlinedField(
        value = uiState.confirmPassword,
        onValueChange = onConfirmPasswordChange,
        keyboardType = KeyboardType.Password,
        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                Icon(
                    imageVector = if (confirmPasswordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    )

    Spacer(modifier = Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = uiState.agreedToTerms,
            onCheckedChange = onAgreedToTermsChange,
            colors = CheckboxDefaults.colors(
                checkedColor = BlobDark,
                uncheckedColor = Color.White.copy(alpha = 0.4f),
                checkmarkColor = Color.White
            )
        )
        Text(
            text = "Saya menyetujui seluruh syarat & ketentuan yang berlaku dari saku-ku",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontFamily = PlusJakartaSans
        )
    }
}

@Composable
private fun RegisterStepOtp(
    uiState: RegisterUiState,
    onOtpCodeChange: (String) -> Unit,
    onResendOtp: () -> Unit,
    onRetryAutoLogin: () -> Unit
) {
    Column {
        OtpCodeInput(code = uiState.otpCode, onCodeChange = onOtpCodeChange)

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Belum menerima OTP? ",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans
            )
            Text(
                text = if (uiState.resendCooldown > 0) "Kirim ulang (${uiState.resendCooldown}s)" else "Kirim ulang OTP",
                color = if (uiState.resendCooldown > 0) Color.White.copy(alpha = 0.4f) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans,
                modifier = Modifier.clickable(
                    enabled = uiState.resendCooldown == 0 && !uiState.isResendingOtp,
                    onClick = onResendOtp
                )
            )
        }

        if (uiState.needsManualLoginRetry) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Kode udah terpakai - tombol di bawah cuma coba login ulang, bukan verifikasi OTP lagi.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.5.sp,
                fontFamily = PlusJakartaSans,
                modifier = Modifier.clickable(onClick = onRetryAutoLogin)
            )
        }
    }
}

// Step 2 (BARU sejak 17 Sept) - gabungan NIK + Domisili (cascading Provinsi/Kota/Kecamatan) +
// Tanggal Lahir + Foto KTP dalam 1 step. NIK/Domisili/Tanggal Lahir WAJIB (divalidasi di
// RegisterViewModel.submitIdentitas()), Foto KTP tetap opsional (kamera bisa gagal - izin
// ditolak/device tanpa kamera - gak boleh nge-block registrasi, bisa dilengkapi belakangan
// lewat menu KTP & Data Diri di Profil).
@Composable
private fun RegisterStepIdentitas(
    uiState: RegisterUiState,
    onNikChange: (String) -> Unit,
    onTanggalLahirChange: (String) -> Unit,
    onProvinsiSelected: (WilayahItem) -> Unit,
    onKotaSelected: (WilayahItem) -> Unit,
    onKecamatanSelected: (WilayahItem) -> Unit,
    onFotoKtpCaptured: (Uri) -> Unit,
    onRetryProvinsi: () -> Unit = {}
) {
    Column {
        KtpPhotoCapture(previewUri = uiState.fotoKtpPreviewUri, onCaptured = onFotoKtpCaptured)
        Text(
            text = "Bisa dilewati dulu kalau kamera gak bisa dipakai sekarang, tapi wajib dilengkapi dari Profil sebelum mengajukan pinjaman.",
            color = Color.White.copy(alpha = 0.4f),
            fontFamily = PlusJakartaSans,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        FieldLabel("NIK")
        SakukuOutlinedField(value = uiState.nik, onValueChange = onNikChange, keyboardType = KeyboardType.Number)

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Tanggal Lahir")
        TanggalLahirField(value = uiState.tanggalLahir, onValueChange = onTanggalLahirChange)

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Provinsi")
        RegionDropdownField(
            label = "Provinsi",
            selectedName = uiState.selectedProvinsi?.name,
            options = uiState.provinsiList,
            enabled = true,
            isLoading = uiState.isLoadingProvinsi,
            onSelect = onProvinsiSelected
        )
        // Gagal fetch provinsi = listnya kosong selamanya tanpa tombol ini, gak ada cara lain
        // buat coba lagi (beda dari kota/kecamatan yang bisa "dipancing" retry dengan pilih ulang
        // parent-nya).
        if (uiState.provinsiList.isEmpty() && !uiState.isLoadingProvinsi) {
            Text(
                text = "Gagal memuat daftar provinsi - Coba lagi",
                color = BlobDark,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .clickable(onClick = onRetryProvinsi)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Kota/Kabupaten")
        RegionDropdownField(
            label = "Kota/Kabupaten",
            selectedName = uiState.selectedKota?.name,
            options = uiState.kotaList,
            enabled = uiState.selectedProvinsi != null,
            isLoading = uiState.isLoadingKota,
            onSelect = onKotaSelected
        )

        Spacer(modifier = Modifier.height(16.dp))
        FieldLabel("Kecamatan")
        RegionDropdownField(
            label = "Kecamatan",
            selectedName = uiState.selectedKecamatan?.name,
            options = uiState.kecamatanList,
            enabled = uiState.selectedKota != null,
            isLoading = uiState.isLoadingKecamatan,
            onSelect = onKecamatanSelected
        )
    }
}

@Composable
private fun RegisterStepPekerjaan(
    uiState: RegisterUiState,
    onTipePekerjaanChange: (TipePekerjaan) -> Unit,
    onPekerjaanChange: (String) -> Unit,
    onPendapatanBulananChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    FieldLabel("Sektor Pekerjaan")
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = uiState.tipePekerjaan?.label ?: "",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlobDark,
                unfocusedBorderColor = BlobMid.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.White.copy(alpha = 0.03f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.03f)
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            TipePekerjaan.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onTipePekerjaanChange(option)
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Jabatan")
    SakukuOutlinedField(value = uiState.pekerjaan, onValueChange = onPekerjaanChange, keyboardType = KeyboardType.Text)

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Pendapatan Bulanan")
    SakukuOutlinedField(
        value = uiState.pendapatanBulanan,
        onValueChange = onPendapatanBulananChange,
        keyboardType = KeyboardType.Number,
        visualTransformation = RupiahVisualTransformation(),
        placeholder = "Contoh: 5000000"
    )
}

@Preview(showBackground = true)
@Composable
private fun RegisterStep1Preview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 0),
            onNamaLengkapChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onOtpCodeChange = {}, onVerifyOtp = {}, onResendOtp = {}, onRetryAutoLogin = {},
            onNikChange = {}, onTanggalLahirChange = {}, onProvinsiSelected = {}, onKotaSelected = {}, onKecamatanSelected = {},
            onFotoKtpCaptured = {}, onSubmitIdentitas = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {}, onSubmitDataPekerjaan = {},
            onSubmitAccount = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterStepOtpPreview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 1, email = "novita.sari@mail.com", otpCode = "12"),
            onNamaLengkapChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onOtpCodeChange = {}, onVerifyOtp = {}, onResendOtp = {}, onRetryAutoLogin = {},
            onNikChange = {}, onTanggalLahirChange = {}, onProvinsiSelected = {}, onKotaSelected = {}, onKecamatanSelected = {},
            onFotoKtpCaptured = {}, onSubmitIdentitas = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {}, onSubmitDataPekerjaan = {},
            onSubmitAccount = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 1200)
@Composable
private fun RegisterStepIdentitasPreview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 2),
            onNamaLengkapChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onOtpCodeChange = {}, onVerifyOtp = {}, onResendOtp = {}, onRetryAutoLogin = {},
            onNikChange = {}, onTanggalLahirChange = {}, onProvinsiSelected = {}, onKotaSelected = {}, onKecamatanSelected = {},
            onFotoKtpCaptured = {}, onSubmitIdentitas = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {}, onSubmitDataPekerjaan = {},
            onSubmitAccount = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterStepPekerjaanPreview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 3),
            onNamaLengkapChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onOtpCodeChange = {}, onVerifyOtp = {}, onResendOtp = {}, onRetryAutoLogin = {},
            onNikChange = {}, onTanggalLahirChange = {}, onProvinsiSelected = {}, onKotaSelected = {}, onKecamatanSelected = {},
            onFotoKtpCaptured = {}, onSubmitIdentitas = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {}, onSubmitDataPekerjaan = {},
            onSubmitAccount = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}
