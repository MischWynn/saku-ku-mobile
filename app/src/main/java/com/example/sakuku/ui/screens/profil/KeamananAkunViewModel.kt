package com.example.sakuku.ui.screens.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val successMessage: String? = null
)

@HiltViewModel
class KeamananAkunViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
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
            s.newPassword.length < 8 -> "Password baru minimal 8 karakter"
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
}
