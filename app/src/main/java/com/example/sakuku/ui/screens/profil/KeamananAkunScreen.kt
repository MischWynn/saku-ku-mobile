package com.example.sakuku.ui.screens.profil

import com.example.sakuku.ui.theme.screenTitleInset
import com.example.sakuku.ui.theme.ScreenPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.ui.components.FieldLabel
import com.example.sakuku.ui.components.GradientButton
import com.example.sakuku.ui.components.SakukuOutlinedField
import com.example.sakuku.ui.theme.BlobDark
import com.example.sakuku.ui.theme.PlusJakartaSans
import com.example.sakuku.ui.theme.SakukuTheme
import com.example.sakuku.ui.theme.sakukuBlobBackground

// Ganti password saat udah login - beda dari Lupa Password (OTP-based di halaman Masuk), di
// sini butuh password lama buat verifikasi. Endpoint customer/change-password baru ada (16
// Sept), sebelumnya cuma staff yang punya.
@Composable
fun KeamananAkunScreen(
    onBack: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: KeamananAkunViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    // Popup (Toast) juga, selain pesan hijau di bawah form - pola sama kayak notif "sesi habis".
    val context = LocalContext.current
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    KeamananAkunContent(
        uiState = uiState,
        onBack = onBack,
        onOldPasswordChange = viewModel::onOldPasswordChange,
        onNewPasswordChange = viewModel::onNewPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onSubmit = viewModel::submit,
        onShowDeleteDialog = viewModel::onShowDeleteDialog,
        onDismissDeleteDialog = viewModel::onDismissDeleteDialog,
        onDeletePasswordChange = viewModel::onDeletePasswordChange,
        onConfirmDelete = viewModel::confirmDeleteAccount
    )
}

@Composable
private fun KeamananAkunContent(
    uiState: KeamananAkunUiState,
    onBack: () -> Unit,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onShowDeleteDialog: () -> Unit = {},
    onDismissDeleteDialog: () -> Unit = {},
    onDeletePasswordChange: (String) -> Unit = {},
    onConfirmDelete: () -> Unit = {}
) {
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    if (uiState.showDeleteDialog) {
        DeleteAccountDialog(
            password = uiState.deletePassword,
            isDeleting = uiState.isDeleting,
            errorMessage = uiState.deleteError,
            onPasswordChange = onDeletePasswordChange,
            onConfirm = onConfirmDelete,
            onDismiss = onDismissDeleteDialog
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .sakukuBlobBackground()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding.Horizontal)
                // bottom 120dp (bukan 32dp) - nyamain pola ProfilScreen.kt, floating
                // AnimatedBottomNavBar butuh clearance segitu biar konten paling bawah gak
                // ketutup nav bar-nya.
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
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Keamanan Akun",
                    color = Color.White,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Text(
                text = "Ganti password akun kamu. Kamu perlu memasukkan password lama sebelum bisa menyimpan yang baru.",
                color = Color.White.copy(alpha = 0.6f),
                fontFamily = PlusJakartaSans,
                fontSize = 12.5.sp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    FieldLabel("Password Lama")
                    SakukuOutlinedField(
                        value = uiState.oldPassword,
                        onValueChange = onOldPasswordChange,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (oldVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { oldVisible = !oldVisible }) {
                                Icon(
                                    imageVector = if (oldVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }

                Column {
                    FieldLabel("Password Baru")
                    SakukuOutlinedField(
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { newVisible = !newVisible }) {
                                Icon(
                                    imageVector = if (newVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }

                Column {
                    FieldLabel("Konfirmasi Password Baru")
                    SakukuOutlinedField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        keyboardType = KeyboardType.Password,
                        visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                Icon(
                                    imageVector = if (confirmVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                Text(text = message, color = Color(0xFFF28FA0), fontFamily = PlusJakartaSans, fontSize = 12.5.sp)
            }
            uiState.successMessage?.let { message ->
                Text(text = message, color = BlobDark, fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
            }

            GradientButton(text = "Ganti Password", onClick = onSubmit, isLoading = uiState.isLoading)

            // Zona Berbahaya - section TERPISAH dari form ganti password di atas (bukan nempel
            // langsung setelahnya), niru pola MenuSection di overview Profil. Sengaja dipisah
            // biar konteksnya jelas beda (destruktif vs biasa), bukan asal ditumpuk.
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ZONA BERBAHAYA",
                    color = Color(0xFFF28FA0).copy(alpha = 0.7f),
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF28FA0).copy(alpha = 0.08f))
                        .clickable(onClick = onShowDeleteDialog)
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.DeleteForever, contentDescription = null, tint = Color(0xFFF28FA0))
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Hapus Akun", color = Color(0xFFF28FA0), fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Permanen, gak bisa dibatalin", color = Color(0xFFF28FA0).copy(alpha = 0.6f), fontFamily = PlusJakartaSans, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1100)
@Composable
private fun KeamananAkunScreenPreview() {
    SakukuTheme {
        KeamananAkunContent(
            uiState = KeamananAkunUiState(),
            onBack = {},
            onOldPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {}
        )
    }
}
