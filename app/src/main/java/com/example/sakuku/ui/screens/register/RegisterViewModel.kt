package com.example.sakuku.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.RegisterRequest
import com.example.sakuku.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TipePekerjaan(val label: String, val apiValue: String) {
    ASN_TNI_POLRI("Aparatur Negara/Negeri (ASN/TNI/Polri)", "ASN_TNI_POLRI"),
    BUMN_BUMD("BUMN/BUMD", "BUMN_BUMD"),
    SWASTA("Swasta", "SWASTA"),
    WIRASWASTA("Wiraswasta/Pemilik Usaha", "WIRASWASTA"),
    NON_PROFIT("Lembaga Non-Profit (Yayasan/LSM)", "NON_PROFIT"),
    FREELANCE("Freelance/Pekerja Lepas", "FREELANCE"),
    TIDAK_BEKERJA("Belum/Tidak Bekerja", "TIDAK_BEKERJA")
}

data class RegisterUiState(
    val currentStep: Int = 0,
    // Step 1 - identitas
    val namaLengkap: String = "",
    val nik: String = "",
    val email: String = "",
    val noHp: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreedToTerms: Boolean = false,
    // Step 2 - pekerjaan
    val tipePekerjaan: TipePekerjaan? = null,
    val pekerjaan: String = "",
    val pendapatanBulanan: String = "",
    // status
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onNamaLengkapChange(value: String) {
        _uiState.value = _uiState.value.copy(namaLengkap = value, errorMessage = null)
    }

    fun onNikChange(value: String) {
        _uiState.value = _uiState.value.copy(nik = value.filter { it.isDigit() }, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onNoHpChange(value: String) {
        _uiState.value = _uiState.value.copy(noHp = value.filter { it.isDigit() }, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    fun onAgreedToTermsChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(agreedToTerms = value, errorMessage = null)
    }

    fun onTipePekerjaanChange(value: TipePekerjaan) {
        _uiState.value = _uiState.value.copy(tipePekerjaan = value, errorMessage = null)
    }

    fun onPekerjaanChange(value: String) {
        _uiState.value = _uiState.value.copy(pekerjaan = value, errorMessage = null)
    }

    fun onPendapatanBulananChange(value: String) {
        _uiState.value = _uiState.value.copy(pendapatanBulanan = value.filter { it.isDigit() }, errorMessage = null)
    }

    fun goToStep2() {
        val s = _uiState.value
        val error = when {
            s.namaLengkap.isBlank() -> "Nama lengkap wajib diisi"
            s.nik.length != 16 -> "NIK harus 16 digit"
            s.email.isBlank() -> "Email wajib diisi"
            s.noHp.isBlank() -> "Nomor telepon wajib diisi"
            s.password.length < 8 -> "Password minimal 8 karakter"
            s.password != s.confirmPassword -> "Konfirmasi password tidak sama"
            !s.agreedToTerms -> "Kamu harus menyetujui syarat & ketentuan"
            else -> null
        }
        if (error != null) {
            _uiState.value = s.copy(errorMessage = error)
        } else {
            _uiState.value = s.copy(currentStep = 1, errorMessage = null)
        }
    }

    fun goToStep1() {
        _uiState.value = _uiState.value.copy(currentStep = 0, errorMessage = null)
    }

    fun register() {
        val s = _uiState.value
        if (s.tipePekerjaan == null) {
            _uiState.value = s.copy(errorMessage = "Sektor pekerjaan wajib dipilih")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val request = RegisterRequest(
                namaLengkap = s.namaLengkap,
                nik = s.nik,
                email = s.email,
                noHp = "+62${s.noHp}",
                password = s.password,
                tipePekerjaan = s.tipePekerjaan.apiValue,
                pekerjaan = s.pekerjaan.ifBlank { null },
                pendapatanBulanan = s.pendapatanBulanan.toLongOrNull()
            )
            authRepository.register(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registrasi gagal, coba lagi"
                    )
                }
        }
    }
}
