package com.example.sakuku.ui.screens.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Verifikasi ini dipakai bareng buat 2 alur berbeda - lihat catatan di submit()/resend() soal
// bedanya masing-masing mode manggil endpoint apa.
enum class OtpMode { REGISTRATION, RESET_PASSWORD }

private const val CODE_LENGTH = 6
private const val RESEND_COOLDOWN_SECONDS = 30

data class OtpUiState(
    val email: String = "",
    val mode: OtpMode = OtpMode.REGISTRATION,
    val code: String = "",
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldown: Int = 0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val verified: Boolean = false
) {
    val canSubmit: Boolean get() = code.length == CODE_LENGTH && !isSubmitting
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState = _uiState.asStateFlow()

    private var cooldownJob: Job? = null
    private var initialized = false

    // Dipanggil dari LaunchedEffect(email, mode) di Screen - guard `initialized` biar
    // recomposition gak restart cooldown timer dari awal terus-terusan.
    fun init(email: String, mode: OtpMode) {
        if (initialized) return
        initialized = true
        _uiState.update { it.copy(email = email, mode = mode) }
        startCooldown()
    }

    fun onCodeChange(value: String) {
        _uiState.update {
            it.copy(code = value.filter { c -> c.isDigit() }.take(CODE_LENGTH), errorMessage = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.code.length != CODE_LENGTH) return

        when (state.mode) {
            OtpMode.REGISTRATION -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
                    authRepository.verifyOtp(state.email, state.code)
                        .onSuccess { _uiState.update { it.copy(isSubmitting = false, verified = true) } }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isSubmitting = false, errorMessage = e.message ?: "Kode OTP salah atau sudah kedaluwarsa")
                            }
                        }
                }
            }
            OtpMode.RESET_PASSWORD -> {
                // Sekarang beneran divalidasi di sini lewat customer/verify-reset-otp (cek
                // doang, gak konsumsi kode) - gak bisa lagi lanjut ke Ganti Password pakai
                // kode asal 6 digit. Konsumsi kode yang sebenarnya (ditandai used) tetap
                // kejadian sekali pas POST /customer/reset-password di-submit nanti.
                viewModelScope.launch {
                    _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
                    authRepository.verifyResetOtp(state.email, state.code)
                        .onSuccess { _uiState.update { it.copy(isSubmitting = false, verified = true) } }
                        .onFailure { e ->
                            _uiState.update {
                                it.copy(isSubmitting = false, errorMessage = e.message ?: "Kode OTP salah atau sudah kedaluwarsa")
                            }
                        }
                }
            }
        }
    }

    fun resend() {
        val state = _uiState.value
        if (state.resendCooldown > 0 || state.isResending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
            val result = when (state.mode) {
                OtpMode.REGISTRATION -> authRepository.resendOtp(state.email)
                // Gak ada endpoint resend terpisah buat reset password - forgot-password
                // sendiri yang generate+kirim kode baru tiap dipanggil ulang.
                OtpMode.RESET_PASSWORD -> authRepository.forgotPassword(state.email)
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isResending = false, infoMessage = "Kode OTP baru sudah dikirim") }
                    startCooldown()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isResending = false, errorMessage = e.message ?: "Gagal mengirim ulang OTP")
                    }
                }
        }
    }

    private fun startCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.update { it.copy(resendCooldown = RESEND_COOLDOWN_SECONDS) }
            while (_uiState.value.resendCooldown > 0) {
                delay(1000)
                _uiState.update { it.copy(resendCooldown = it.resendCooldown - 1) }
            }
        }
    }
}
