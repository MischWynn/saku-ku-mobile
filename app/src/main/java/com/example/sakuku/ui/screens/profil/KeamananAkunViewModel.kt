package com.example.sakuku.ui.screens.profil

import com.example.sakuku.util.Validators
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KeamananAkunUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Hapus Akun - dipindah ke sini (17 Sept) dari overview Profil, ditaro sebagai section
    // "Zona Berbahaya" terpisah di bawah form ganti password (lihat KeamananAkunScreen).
    val showDeleteDialog: Boolean = false,
    val deletePassword: String = "",
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class KeamananAkunViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(KeamananAkunUiState())
    val uiState = _uiState.asStateFlow()

    fun onOldPasswordChange(v: String) = _uiState.update { it.copy(oldPassword = v, errorMessage = null, successMessage = null) }
    fun onNewPasswordChange(v: String) = _uiState.update { it.copy(newPassword = v, errorMessage = null, successMessage = null) }
    fun onConfirmPasswordChange(v: String) = _uiState.update { it.copy(confirmPassword = v, errorMessage = null, successMessage = null) }

    fun submit() {
        val s = _uiState.value
        val error = when {
            s.oldPassword.isBlank() -> "Password lama wajib diisi"
            Validators.passwordError(s.newPassword) != null -> Validators.passwordError(s.newPassword)
            s.newPassword != s.confirmPassword -> "Konfirmasi password baru tidak sama"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            customerRepository.changePassword(s.oldPassword, s.newPassword)
                .onSuccess {
                    // Field dikosongin abis sukses - user tetap di layar ini (bukan auto-navigate
                    // kayak alur reset-password OTP), biar bisa lihat pesan sukses dulu, pola sama
                    // kayak Settings staff Angular.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Password berhasil diubah",
                            oldPassword = "",
                            newPassword = "",
                            confirmPassword = ""
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Gagal mengubah password, cek password lama kamu")
                    }
                }
        }
    }

    fun onShowDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = true, deleteError = null, deletePassword = "") }
    fun onDismissDeleteDialog() = _uiState.update { it.copy(showDeleteDialog = false, deleteError = null, deletePassword = "") }
    fun onDeletePasswordChange(v: String) = _uiState.update { it.copy(deletePassword = v, deleteError = null) }

    // Minta password lagi (dicek di backend) biar gak kepencet gak sengaja - aksi ini gak bisa
    // dibatalin. Sukses -> clear token, ProfilScreen & KeamananAkunScreen dua-duanya punya
    // LaunchedEffect(loggedOut) sendiri, tapi cuma yang lagi kebuka yang bakal trigger navigasi.
    fun confirmDeleteAccount() {
        val password = _uiState.value.deletePassword
        if (password.isBlank()) {
            _uiState.update { it.copy(deleteError = "Password wajib diisi") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deleteError = null) }
            customerRepository.deleteAccount(password)
                .onSuccess {
                    tokenDataStore.clearToken()
                    _uiState.update { it.copy(isDeleting = false, showDeleteDialog = false, loggedOut = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isDeleting = false, deleteError = error.message ?: "Gagal menghapus akun") }
                }
        }
    }
}
