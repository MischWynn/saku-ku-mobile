package com.example.sakuku.ui.screens.newpassword

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.R
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground

@Composable
fun NewPasswordScreen(
    email: String,
    code: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit = {},
    onNeedNewOtp: () -> Unit = {},
    viewModel: NewPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(email, code) { viewModel.init(email, code) }
    LaunchedEffect(uiState.success) { if (uiState.success) onSuccess() }

    NewPasswordScreenContent(
        uiState = uiState,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::submit,
        onBack = onBack,
        onNeedNewOtp = onNeedNewOtp
    )
}

@Composable
private fun NewPasswordScreenContent(
    uiState: NewPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onNeedNewOtp: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Kembali",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.password),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Masukkan Password Baru",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pastikan password barumu mudah diingat, tapi tetap aman",
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        FieldLabel("Password Baru")
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

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Ganti Password",
            onClick = onSubmit,
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Kode salah atau kedaluwarsa? ",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans
            )
            Text(
                text = "Minta OTP baru",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans,
                modifier = Modifier.clickable(onClick = onNeedNewOtp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun NewPasswordScreenPreview() {
    SakukuTheme {
        NewPasswordScreenContent(
            uiState = NewPasswordUiState(),
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onBack = {},
            onNeedNewOtp = {}
        )
    }
}
