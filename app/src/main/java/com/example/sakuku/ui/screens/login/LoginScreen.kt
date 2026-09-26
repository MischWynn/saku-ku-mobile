package com.example.sakuku.ui.screens.login

import androidx.compose.foundation.layout.statusBarsPadding
import com.example.sakuku.ui.theme.ScreenPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.R
import com.example.sakuku.ui.components.AuthButtonGradient
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.sakukuBlobBackground
import com.example.sakuku.ui.theme.SakukuTheme

@Composable
fun LoginScreen(
    onLoginSuccess: (userName: String?) -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNeedsGoogleRegistration: (email: String, suggestedName: String?) -> Unit = { _, _ -> },
    onBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val webClientId = stringResource(R.string.google_web_client_id)

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess(uiState.userName)
    }

    LaunchedEffect(uiState.needsGoogleRegistration) {
        uiState.needsGoogleRegistration?.let { onNeedsGoogleRegistration(it.email, it.suggestedName) }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeToast()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onLoginClick = viewModel::login,
        onGoogleSignInClick = { viewModel.signInWithGoogle(context, webClientId) },
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgotPassword = onNavigateToForgotPassword,
        onBack = onBack
    )
}

// internal (bukan private) - biar bisa dipanggil langsung dari instrumented test
// (LoginScreenUiTest di src/androidTest) tanpa perlu Hilt/ViewModel/network sama sekali.
// onNavigateToForgotPassword dikasih default {} biar test lama (yang belum tau param ini)
// tetap kompilasi tanpa perlu diupdate.
@Composable
internal fun LoginScreenContent(
    uiState: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit = {},
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onBack: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            // Dulu cuma navigationBarsPadding - tombol kembali nabrak status bar (edge-to-edge).
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = ScreenPadding.Horizontal)
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Masuk",
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Satu langkah lagi menuju dana yang kamu butuhkan",
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = PlusJakartaSans,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        FieldLabel("Email / No HP")
        SakukuOutlinedField(
            value = uiState.identifier,
            onValueChange = onIdentifierChange,
            // Text, bukan Email - keyboard email gak nyediain angka di baris utama.
            keyboardType = KeyboardType.Text,
            placeholder = "nama@email.com atau 08xxxxxxxxxx",
            modifier = Modifier.testTag("login_email_field")
        )

        Spacer(modifier = Modifier.height(16.dp))

        FieldLabel("Password")
        SakukuOutlinedField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.testTag("login_password_field"),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        )

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = BlobDark,
                        uncheckedColor = Color.White.copy(alpha = 0.4f),
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = "Ingat username",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontFamily = PlusJakartaSans
                )
            }
            Text(
                text = "Lupa password?",
                color = Color.White,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable(onClick = onNavigateToForgotPassword)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Masuk",
            onClick = onLoginClick,
            enabled = !uiState.isLoading,
            isLoading = uiState.isLoading,
            modifier = Modifier.testTag("login_submit_button")
        )

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
                    text = "Masuk dengan Google",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Belum punya akun? ",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans
            )
            Text(
                text = "Ayo Daftar",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                fontFamily = PlusJakartaSans,
                modifier = Modifier.clickable(onClick = onNavigateToRegister)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SakukuTheme {
        LoginScreenContent(
            uiState = LoginUiState(),
            onIdentifierChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onNavigateToRegister = {},
            onBack = {}
        )
    }
}
