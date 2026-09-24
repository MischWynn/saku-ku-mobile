package com.example.sakuku.ui.screens.register

import com.example.sakuku.util.Validators
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import com.example.sakuku.data.remote.dto.RegisterRequest
import com.example.sakuku.data.remote.dto.WilayahItem
import com.example.sakuku.data.repository.AuthRepository
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.GoogleSignInOutcome
import com.example.sakuku.data.repository.WilayahRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

private const val OTP_LENGTH = 6
private const val RESEND_COOLDOWN_SECONDS = 30

data class RegisterUiState(
    // 0=Akun, 1=Verifikasi OTP, 2=Identitas (Foto KTP+NIK+Domisili+DOB), 3=Data Pekerjaan
    val currentStep: Int = 0,
    // Step 0 - akun (NIK sengaja gak di sini lagi sejak 17 Sept, pindah ke Step 2)
    val namaLengkap: String = "",
    val email: String = "",
    // true kalau email ini datang dari Google Sign-In (sudah diverifikasi Google) - field-nya
    // di-readOnly-kan di UI biar gak ke-edit jadi beda sama akun Google yang barusan dipakai.
    val emailLocked: Boolean = false,
    val noHp: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val agreedToTerms: Boolean = false,
    // Step 1 - OTP
    val otpCode: String = "",
    val isVerifyingOtp: Boolean = false,
    val isResendingOtp: Boolean = false,
    val resendCooldown: Int = 0,
    val otpInfoMessage: String? = null,
    val needsManualLoginRetry: Boolean = false,
    // Step 2 - identitas: NIK + Domisili (cascading Provinsi/Kota/Kecamatan) + Tanggal Lahir +
    // Foto KTP (foto tetap opsional, sisanya wajib - lihat submitIdentitas())
    val nik: String = "",
    val tanggalLahir: String = "",
    val fotoKtpPreviewUri: Uri? = null,
    val provinsiList: List<WilayahItem> = emptyList(),
    val kotaList: List<WilayahItem> = emptyList(),
    val kecamatanList: List<WilayahItem> = emptyList(),
    val selectedProvinsi: WilayahItem? = null,
    val selectedKota: WilayahItem? = null,
    val selectedKecamatan: WilayahItem? = null,
    val isLoadingProvinsi: Boolean = false,
    val isLoadingKota: Boolean = false,
    val isLoadingKecamatan: Boolean = false,
    // Step 3 - pekerjaan (step TERAKHIR sekarang, sukses di sini = registerSuccess)
    val tipePekerjaan: TipePekerjaan? = null,
    val pekerjaan: String = "",
    val pendapatanBulanan: String = "",
    // status
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false,
    // One-shot Toast - sama pola kayak LoginUiState.toastMessage, dipakai buat kasus Google
    // Sign-In di-cancel user (bukan error beneran, jadi jangan tampil sebagai banner merah).
    val toastMessage: String? = null
) {
    val canSubmitOtp: Boolean get() = otpCode.length == OTP_LENGTH && !isVerifyingOtp
}

// Register direstrukturisasi 17 Sept jadi 4 step, URUTAN BARU: Akun -> OTP -> Identitas (Foto
// KTP+NIK+Domisili+DOB) -> Data Pekerjaan. Sebelumnya NIK ada di step Akun dan Data Pekerjaan
// ada SEBELUM Foto KTP - dibalik supaya "isi identitas sambil liat KTP di tangan" jadi 1 step
// yang sama, dan Data Pekerjaan/finansial jadi step penutup.
//
// Konsekuensi backend: NIK sekarang opsional pas POST /customer/register (sebelumnya wajib),
// dan CustomerUpdateRequest (PATCH customer/me) sekarang BISA bawa nik - tapi cuma efektif
// SEKALI, backend nolak kalau NIK udah keisi sebelumnya (identitas permanen).
//
// Kunci desainnya TETAP sama seperti sebelumnya: begitu OTP (Step 1) sukses, langsung
// auto-login diam-diam pakai email+password yang masih ada di state (dari Step 0) - verify-otp
// sendiri gak nerbitin token, padahal Step 2/3 butuh token buat PATCH customer/me.
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val customerRepository: CustomerRepository,
    private val wilayahRepository: WilayahRepository,
    private val tokenDataStore: TokenDataStore,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        fetchProvinsi()
    }

    fun onNamaLengkapChange(value: String) {
        _uiState.value = _uiState.value.copy(namaLengkap = value, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    // Dipanggil sekali dari RegisterScreen kalau layar ini dibuka lewat alur "Masuk dengan
    // Google" tapi emailnya belum terdaftar (lihat LoginViewModel.GooglePrefill / AppNavigation).
    // Password TETAP wajib diisi manual di step ini - akun Google-only gak dibuatkan otomatis,
    // cuma email-nya yang udah terverifikasi jadi gak perlu (dan gak boleh) diketik ulang.
    fun prefillFromGoogle(email: String, suggestedName: String?) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailLocked = true,
            namaLengkap = suggestedName ?: _uiState.value.namaLengkap
        )
    }

    // Tombol "Daftar dengan Google" di Step 0 manggil INI, bukan endpoint terpisah - backend
    // cuma punya 1 pintu (POST /customer/google-signin) yang sekaligus ngecek "email ini udah
    // ada akun apa belum". Dua kemungkinan hasilnya:
    // - LoggedIn: ternyata email Google ini UDAH punya akun -> daripada nyuruh user isi form
    //   dari awal lagi, langsung dianggap "berhasil daftar" (reuse registerSuccess yang sama
    //   kayak alur normal, ujungnya sama-sama ke Welcome).
    // - NeedsRegistration: email belum ada akun (kasus paling umum kalau tombol ini dipencet
    //   dari Register) -> lock email-nya (prefillFromGoogle, method di atas) dan user lanjut
    //   ngisi form yang lagi kebuka ini, gak ada navigasi apa-apa.
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
                            val name = outcome.suggestedName ?: customerRepository.getMe().getOrNull()?.namaLengkap ?: ""
                            _uiState.value = _uiState.value.copy(isLoading = false, namaLengkap = name, registerSuccess = true)
                        }
                        is GoogleSignInOutcome.NeedsRegistration -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            prefillFromGoogle(outcome.email, outcome.suggestedName)
                        }
                    }
                }
                .onFailure { error ->
                    // Sama persis pola LoginViewModel.signInWithGoogle() - cancel dianggap
                    // normal (Toast ringan), bukan error (banner merah).
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

    fun onOtpCodeChange(value: String) {
        _uiState.value = _uiState.value.copy(
            otpCode = value.filter { it.isDigit() }.take(OTP_LENGTH),
            errorMessage = null
        )
    }

    fun onNikChange(value: String) {
        _uiState.value = _uiState.value.copy(nik = value.filter { it.isDigit() }.take(16), errorMessage = null)
    }

    fun onTanggalLahirChange(value: String) {
        _uiState.value = _uiState.value.copy(tanggalLahir = value, errorMessage = null)
    }

    fun onFotoKtpCaptured(uri: Uri) {
        _uiState.value = _uiState.value.copy(fotoKtpPreviewUri = uri, errorMessage = null)
    }

    fun onProvinsiSelected(item: WilayahItem) {
        _uiState.value = _uiState.value.copy(
            selectedProvinsi = item,
            selectedKota = null,
            selectedKecamatan = null,
            kotaList = emptyList(),
            kecamatanList = emptyList(),
            errorMessage = null
        )
        fetchKota(item.id)
    }

    fun onKotaSelected(item: WilayahItem) {
        _uiState.value = _uiState.value.copy(
            selectedKota = item,
            selectedKecamatan = null,
            kecamatanList = emptyList(),
            errorMessage = null
        )
        fetchKecamatan(item.id)
    }

    fun onKecamatanSelected(item: WilayahItem) {
        _uiState.value = _uiState.value.copy(selectedKecamatan = item, errorMessage = null)
    }

    // Ketiga fetch di bawah dulu diem-diem gagal (cuma matiin isLoading, gak ada errorMessage
    // sama sekali) - dropdown-nya keliatan normal tapi isinya 0 item, gak keluar apa-apa pas
    // diklik. Sekarang gagal manapun langsung keisi errorMessage (banner generik yang udah ada
    // di RegisterScreenContent otomatis nampilinnya), plus retryFetchProvinsi() buat coba lagi
    // dari titik paling awal (provinsi) - tanpa provinsi ke-load, kota/kecamatan gak akan pernah
    // bisa dicoba sama sekali.
    private fun fetchProvinsi() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProvinsi = true, errorMessage = null)
            wilayahRepository.getProvinces()
                .onSuccess { list -> _uiState.value = _uiState.value.copy(provinsiList = list, isLoadingProvinsi = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingProvinsi = false,
                        errorMessage = "Gagal memuat daftar provinsi. Cek koneksi internetmu, lalu coba lagi."
                    )
                }
        }
    }

    fun retryFetchProvinsi() = fetchProvinsi()

    private fun fetchKota(provinceId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingKota = true, errorMessage = null)
            wilayahRepository.getRegencies(provinceId)
                .onSuccess { list -> _uiState.value = _uiState.value.copy(kotaList = list, isLoadingKota = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingKota = false,
                        errorMessage = "Gagal memuat daftar kota/kabupaten. Pilih ulang provinsinya buat coba lagi."
                    )
                }
        }
    }

    private fun fetchKecamatan(regencyId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingKecamatan = true, errorMessage = null)
            wilayahRepository.getDistricts(regencyId)
                .onSuccess { list -> _uiState.value = _uiState.value.copy(kecamatanList = list, isLoadingKecamatan = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingKecamatan = false,
                        errorMessage = "Gagal memuat daftar kecamatan. Pilih ulang kota/kabupatennya buat coba lagi."
                    )
                }
        }
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

    // Step 0 -> panggil register() beneran (NIK & field pekerjaan dikirim null, diisi belakangan
    // di Step 2/3 lewat PATCH), lanjut ke Step 1 (OTP) kalau sukses.
    fun submitAccount() {
        val s = _uiState.value
        val error = when {
            s.namaLengkap.isBlank() -> "Nama lengkap wajib diisi"
            s.email.isBlank() -> "Email wajib diisi"
            s.noHp.isBlank() -> "Nomor telepon wajib diisi"
            Validators.passwordError(s.password) != null -> Validators.passwordError(s.password)
            s.password != s.confirmPassword -> "Konfirmasi password tidak sama"
            !s.agreedToTerms -> "Kamu harus menyetujui syarat & ketentuan"
            else -> null
        }
        if (error != null) {
            _uiState.value = s.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val request = RegisterRequest(
                namaLengkap = s.namaLengkap,
                nik = null,
                email = s.email,
                noHp = "+62${s.noHp}",
                password = s.password,
                tipePekerjaan = null,
                pekerjaan = null,
                pendapatanBulanan = null
            )
            authRepository.register(request)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isLoading = false, currentStep = 1)
                    startResendCooldown()
                }
                .onFailure { error2 ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error2.message ?: "Registrasi gagal, coba lagi"
                    )
                }
        }
    }

    fun verifyOtp() {
        val s = _uiState.value
        if (!s.canSubmitOtp) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingOtp = true, errorMessage = null)
            authRepository.verifyOtp(s.email, s.otpCode)
                .onSuccess { attemptAutoLogin() }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isVerifyingOtp = false,
                        errorMessage = error.message ?: "Kode OTP salah atau sudah kedaluwarsa"
                    )
                }
        }
    }

    // Kode OTP cuma bisa dipakai sekali (backend nandain used=true begitu verify sukses), jadi
    // kalau login di sini gagal, gak bisa "verify-otp lagi" - tombol retry di UI cuma manggil ini
    // ulang (bukan verifyOtp() lagi).
    fun retryAutoLogin() {
        viewModelScope.launch { attemptAutoLogin() }
    }

    private suspend fun attemptAutoLogin() {
        val s = _uiState.value
        authRepository.login(s.email, s.password)
            .onSuccess { token ->
                tokenDataStore.saveToken(token)
                // Sinkron ulang FCM token yang mungkin udah kecache SEBELUM login (lihat
                // catatan sama di LoginViewModel) - best-effort, gak nge-block registrasi.
                tokenDataStore.getFcmTokenOnce()?.let { fcmToken ->
                    customerRepository.updateFcmToken(fcmToken)
                }
                _uiState.value = _uiState.value.copy(
                    isVerifyingOtp = false,
                    needsManualLoginRetry = false,
                    currentStep = 2
                )
            }
            .onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isVerifyingOtp = false,
                    needsManualLoginRetry = true,
                    errorMessage = error.message ?: "Akun terverifikasi, tapi auto-login gagal. Coba lagi."
                )
            }
    }

    fun resendOtp() {
        val s = _uiState.value
        if (s.resendCooldown > 0 || s.isResendingOtp) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isResendingOtp = true, errorMessage = null, otpInfoMessage = null)
            authRepository.resendOtp(s.email)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(isResendingOtp = false, otpInfoMessage = "Kode OTP baru sudah dikirim")
                    startResendCooldown()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isResendingOtp = false,
                        errorMessage = error.message ?: "Gagal mengirim ulang OTP"
                    )
                }
        }
    }

    private fun startResendCooldown() {
        cooldownJob?.cancel()
        cooldownJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(resendCooldown = RESEND_COOLDOWN_SECONDS)
            while (_uiState.value.resendCooldown > 0) {
                delay(1000)
                _uiState.value = _uiState.value.copy(resendCooldown = _uiState.value.resendCooldown - 1)
            }
        }
    }

    // Step 2 -> NIK/Tanggal Lahir/Domisili wajib, Foto KTP opsional (kamera bisa gagal - izin
    // ditolak, device tanpa kamera - jadi gak boleh nge-block lanjut, bisa dilengkapi belakangan
    // lewat menu KTP & Data Diri di Profil). Semua dikirim dalam 1 PATCH customer/me, lanjut ke
    // Step 3 (Data Pekerjaan) kalau sukses.
    fun submitIdentitas() {
        val s = _uiState.value
        val error = when {
            s.nik.length != 16 -> "NIK harus 16 digit"
            Validators.tanggalLahirError(s.tanggalLahir) != null -> Validators.tanggalLahirError(s.tanggalLahir)
            s.selectedProvinsi == null -> "Provinsi wajib dipilih"
            s.selectedKota == null -> "Kota/Kabupaten wajib dipilih"
            s.selectedKecamatan == null -> "Kecamatan wajib dipilih"
            else -> null
        }
        if (error != null) {
            _uiState.value = s.copy(errorMessage = error)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val fotoBase64 = s.fotoKtpPreviewUri?.let { uri ->
                try {
                    appContext.contentResolver.openInputStream(uri)?.use { input ->
                        Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            customerRepository.updateMe(
                CustomerUpdateRequest(
                    nik = s.nik,
                    tanggalLahir = s.tanggalLahir,
                    provinsi = s.selectedProvinsi?.name,
                    kota = s.selectedKota?.name,
                    kecamatan = s.selectedKecamatan?.name,
                    fotoKtp = fotoBase64
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, currentStep = 3)
            }.onFailure { error2 ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error2.message ?: "Gagal menyimpan data identitas"
                )
            }
        }
    }

    // Step 3 (TERAKHIR) -> PATCH customer/me (sektor/jabatan/pendapatan). Sukses di sini =
    // registerSuccess (sinyal akhir buat navigasi ke Welcome).
    fun submitDataPekerjaan() {
        val s = _uiState.value
        if (s.tipePekerjaan == null) {
            _uiState.value = s.copy(errorMessage = "Sektor pekerjaan wajib dipilih")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            customerRepository.updateMe(
                CustomerUpdateRequest(
                    tipePekerjaan = s.tipePekerjaan.apiValue,
                    pekerjaan = s.pekerjaan.ifBlank { null },
                    pendapatanBulanan = s.pendapatanBulanan.toDoubleOrNull()
                )
            ).onSuccess {
                _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Gagal menyimpan data pekerjaan"
                )
            }
        }
    }
}
