@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.sakuku.ui.screens.register

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.ui.components.AuthButtonGradient
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.BlobMid
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme

@Composable
fun RegisterScreen(
    // Sekarang bawa email juga (bukan cuma namaLengkap) - dibutuhin buat lanjut ke layar
    // Verifikasi OTP (POST /customer/verify-otp butuh email, bukan nama).
    onRegisterSuccess: (email: String, namaLengkap: String) -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) onRegisterSuccess(uiState.email, uiState.namaLengkap)
    }

    RegisterScreenContent(
        uiState = uiState,
        onNamaLengkapChange = viewModel::onNamaLengkapChange,
        onNikChange = viewModel::onNikChange,
        onEmailChange = viewModel::onEmailChange,
        onNoHpChange = viewModel::onNoHpChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onAgreedToTermsChange = viewModel::onAgreedToTermsChange,
        onTipePekerjaanChange = viewModel::onTipePekerjaanChange,
        onPekerjaanChange = viewModel::onPekerjaanChange,
        onPendapatanBulananChange = viewModel::onPendapatanBulananChange,
        onNextStep = viewModel::goToStep2,
        onPrevStep = viewModel::goToStep1,
        onSubmit = viewModel::register,
        onNavigateToLogin = onNavigateToLogin,
        onBack = onBack
    )
}

@Composable
private fun RegisterScreenContent(
    uiState: RegisterUiState,
    onNamaLengkapChange: (String) -> Unit,
    onNikChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNoHpChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onAgreedToTermsChange: (Boolean) -> Unit,
    onTipePekerjaanChange: (TipePekerjaan) -> Unit,
    onPekerjaanChange: (String) -> Unit,
    onPendapatanBulananChange: (String) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = if (uiState.currentStep == 0) onBack else onPrevStep) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Daftar",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (uiState.currentStep == 0) {
                "Mulai perjalanan finansialmu bareng kami"
            } else {
                "Sebelum mulai, kami ingin mengenalmu lebih jauh!"
            },
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (uiState.currentStep == 0) {
            RegisterStep1Fields(
                uiState = uiState,
                onNamaLengkapChange = onNamaLengkapChange,
                onNikChange = onNikChange,
                onEmailChange = onEmailChange,
                onNoHpChange = onNoHpChange,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
                onAgreedToTermsChange = onAgreedToTermsChange
            )
        } else {
            RegisterStep2Fields(
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

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Daftar",
            onClick = if (uiState.currentStep == 0) onNextStep else onSubmit,
            enabled = !uiState.isLoading,
            isLoading = uiState.currentStep == 1 && uiState.isLoading
        )

        if (uiState.currentStep == 0) {
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
                onClick = { /* TODO: Daftar dengan Google - belum ada backend-nya */ },
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
    onNikChange: (String) -> Unit,
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
    FieldLabel("NIK")
    SakukuOutlinedField(value = uiState.nik, onValueChange = onNikChange, keyboardType = KeyboardType.Number)

    Spacer(modifier = Modifier.height(16.dp))
    FieldLabel("Email")
    SakukuOutlinedField(value = uiState.email, onValueChange = onEmailChange, keyboardType = KeyboardType.Email)

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
private fun RegisterStep2Fields(
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
        keyboardType = KeyboardType.Number
    )
}

@Preview(showBackground = true)
@Composable
private fun RegisterStep1Preview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 0),
            onNamaLengkapChange = {}, onNikChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {},
            onNextStep = {}, onPrevStep = {}, onSubmit = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterStep2Preview() {
    SakukuTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(currentStep = 1),
            onNamaLengkapChange = {}, onNikChange = {}, onEmailChange = {}, onNoHpChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {}, onAgreedToTermsChange = {},
            onTipePekerjaanChange = {}, onPekerjaanChange = {}, onPendapatanBulananChange = {},
            onNextStep = {}, onPrevStep = {}, onSubmit = {}, onNavigateToLogin = {}, onBack = {}
        )
    }
}
