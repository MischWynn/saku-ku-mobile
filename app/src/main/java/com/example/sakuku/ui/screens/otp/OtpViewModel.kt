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

// Dulu dipakai bareng buat 2 alur (registrasi + reset-password) lewat OtpMode - sejak Register
// direstrukturisasi 17 Sept jadi 4 step (OTP registrasi sekarang punya UI+state sendiri di
// RegisterScreen/RegisterViewModel, gak navigate ke sini lagi), layar ini murni buat alur
// Lupa Password aja. Disederhanain, gak perlu enum mode lagi.
private const val CODE_LENGTH = 6
private const val RESEND_COOLDOWN_SECONDS = 30

data class OtpUiState(
    val email: String = "",
    val code: String = "",
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val resendCooldown: Int = 0,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    // One-shot Toast (pola sama kayak RegisterUiState.toastMessage) - teks infoMessage inline
    // sering ketutup keyboard, jadi konfirmasi resend juga dimunculin lewat Toast.
    val toastMessage: String? = null,
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

    // Dipanggil dari LaunchedEffect(email) di Screen - guard `initialized` biar recomposition
    // gak restart cooldown timer dari awal terus-terusan.
    fun init(email: String) {
        if (initialized) return
        initialized = true
        _uiState.update { it.copy(email = email) }
        startCooldown()
    }

    fun onCodeChange(value: String) {
        _uiState.update {
            it.copy(code = value.filter { c -> c.isDigit() }.take(CODE_LENGTH), errorMessage = null)
        }
    }

    // Cek beneran lewat customer/verify-reset-otp (cek doang, gak konsumsi kode) - gak bisa
    // lanjut ke Ganti Password pakai kode asal 6 digit. Konsumsi kode yang sebenarnya (ditandai
    // used) tetap kejadian sekali pas POST /customer/reset-password di-submit nanti.
    fun submit() {
        val state = _uiState.value
        if (state.code.length != CODE_LENGTH) return

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

    fun resend() {
        val state = _uiState.value
        if (state.resendCooldown > 0 || state.isResending) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResending = true, errorMessage = null, infoMessage = null) }
            // Gak ada endpoint resend terpisah buat reset password - forgot-password sendiri
            // yang generate+kirim kode baru tiap dipanggil ulang.
            authRepository.forgotPassword(state.email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            infoMessage = "Kode OTP baru sudah dikirim ke ${state.email}. Cek juga folder Spam.",
                            toastMessage = "Kode OTP baru sudah dikirim"
                        )
                    }
                    startCooldown()
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isResending = false, errorMessage = e.message ?: "Gagal mengirim ulang OTP")
                    }
                }
        }
    }

    fun consumeToast() {
        _uiState.update { it.copy(toastMessage = null) }
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
