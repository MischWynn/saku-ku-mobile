package com.example.sakuku.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    // TODO: kosongin lagi sebelum submit - ini cuma prefill buat testing di emulator.
    // Password dummy data emang pake tanda seru ("Password123!"), BUKAN "Password123" -
    // sempet salah ketik di sini, baru ketauan pas ngetes hash bcrypt yang rusak (13 Sept).
    val identifier: String = "novita.sari@mail.com",
    val password: String = "Password123!",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            tokenDataStore.getRememberedIdentifier()?.let { remembered ->
                _uiState.value = _uiState.value.copy(identifier = remembered, rememberMe = true)
            }
        }
    }

    fun onIdentifierChange(value: String) {
        _uiState.value = _uiState.value.copy(identifier = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onRememberMeChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(rememberMe = value)
    }

    fun login() {
        val current = _uiState.value
        if (current.identifier.isBlank() || current.password.isBlank()) {
            _uiState.value = current.copy(errorMessage = "Email/No HP dan password wajib diisi")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.login(current.identifier, current.password)
                .onSuccess { token ->
                    tokenDataStore.saveToken(token)
                    tokenDataStore.saveRememberedIdentifier(if (current.rememberMe) current.identifier else null)
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login gagal, coba lagi"
                    )
                }
        }
    }
}
