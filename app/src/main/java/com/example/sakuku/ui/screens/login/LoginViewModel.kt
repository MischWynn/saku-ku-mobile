package com.example.sakuku.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.repository.AuthRepository
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.GoogleSignInOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sinyal one-shot buat LoginScreen: "email ini valid di Google, tapi belum ada akun Saku-Ku"
// -> navigate ke Register step 1 dengan email di-prefill+dikunci (lihat RegisterViewModel).
data class GooglePrefill(val email: String, val suggestedName: String?)

data class LoginUiState(
    // Email ATAU nomor HP - backend (AppCustomerDetailsService) nerima 08../62../+62.. sama aja.
    val identifier: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccess: Boolean = false,
    // Diisi dari customer/me abis token kesimpen - login sendiri gak bawa nama (respon cuma
    // {token,type}). Null berarti gak kebaca (gagal fetch atau belum sempat) - LoginScreen skip
    // Welcome dan langsung ke Main kalau ini null, gak nge-block alur login cuma gara-gara
    // fetch profil kedua gagal.
    val userName: String? = null,
    val needsGoogleRegistration: GooglePrefill? = null,
    // One-shot Toast text (e.g. "Login dibatalkan oleh pengguna" when the user backs out of the
    // Google account picker) - LoginScreen shows it then calls consumeToast() to reset this to
    // null, same one-shot pattern as needsGoogleRegistration below.
    val toastMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
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
                    // Sinkron ulang FCM token yang mungkin udah kecache SEBELUM login (app ini
                    // guest-access, PushMessagingService.onNewToken bisa fire duluan sebelum ada
                    // sesi) - best-effort, gak nge-block login kalau gagal/gak ada token dicache.
                    tokenDataStore.getFcmTokenOnce()?.let { fcmToken ->
                        customerRepository.updateFcmToken(fcmToken)
                    }
                    // Best-effort - kalau gagal, tetap lanjut login sukses tanpa nama (Welcome
                    // di-skip), bukan gagalin seluruh proses login.
                    val name = customerRepository.getMe().getOrNull()?.namaLengkap
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true, userName = name)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login gagal, coba lagi"
                    )
                }
        }
    }

    // activityContext dipakai sekali buat munculin account picker Credential Manager, gak
    // disimpen (lihat AuthRepository.googleSignIn - alasan sama, dijelaskan di sana).
    fun signInWithGoogle(activityContext: Context, webClientId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.googleSignIn(activityContext, webClientId)
                .onSuccess { outcome ->
                    when (outcome) {
                        is GoogleSignInOutcome.LoggedIn -> {
                            tokenDataStore.saveToken(outcome.token)
                            tokenDataStore.getFcmTokenOnce()?.let { fcmToken ->
                                customerRepository.updateFcmToken(fcmToken)
                            }
                            val name = outcome.suggestedName ?: customerRepository.getMe().getOrNull()?.namaLengkap
                            _uiState.value = _uiState.value.copy(isLoading = false, loginSuccess = true, userName = name)
                        }
                        is GoogleSignInOutcome.NeedsRegistration -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                needsGoogleRegistration = GooglePrefill(outcome.email, outcome.suggestedName)
                            )
                        }
                    }
                }
                .onFailure { error ->
                    // Credential Manager ngelempar exception juga kalau user nge-cancel account
                    // picker (atau Play Services gagal reauth - device tertentu, mis. MIUI,
                    // ngelempar exception yang sama persis) - itu perilaku normal, bukan "error"
                    // aplikasi, jadi ditampilin sebagai Toast ringan, bukan banner merah.
                    // androidx.credentials.exceptions punya beberapa subclass beda per alasan
                    // (NoCredentialException, GetCredentialCancellationException, dst), deteksi
                    // dari nama class-nya daripada nge-import semua satu-satu di sini.
                    val isCancelled = error.javaClass.simpleName.contains("Cancel", ignoreCase = true)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = if (isCancelled) null else (error.message ?: "Masuk dengan Google gagal, coba lagi"),
                        toastMessage = if (isCancelled) "Login dibatalkan oleh pengguna" else null
                    )
                }
        }
    }

    fun consumeToast() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}
