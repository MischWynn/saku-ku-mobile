package com.example.sakuku.ui.screens.newpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NewPasswordUiState(
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class NewPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun init(email: String, code: String) {
        _uiState.update { it.copy(email = email, code = code) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun submit() {
        val s = _uiState.value
        val error = when {
            s.password.length < 8 -> "Password minimal 8 karakter"
            s.password != s.confirmPassword -> "Konfirmasi password tidak sama"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Kode OTP baru beneran divalidasi di sini (satu-satunya endpoint yang ngecek
            // email+code+newPassword sekaligus) - kalau kodenya salah/kedaluwarsa, error dari
            // backend muncul di sini, bukan di layar Verifikasi sebelumnya.
            authRepository.resetPassword(s.email, s.code, s.password)
                .onSuccess { _uiState.update { it.copy(isLoading = false, success = true) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "Kode OTP salah/kedaluwarsa, atau gagal reset password"
                        )
                    }
                }
        }
    }
}
