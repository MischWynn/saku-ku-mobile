package com.example.sakuku.ui.screens.pengajuan

import java.time.LocalDateTime
import java.time.LocalDate
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.util.LoanCalculator
import com.example.sakuku.util.Validators
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PengajuanRequest
import com.example.sakuku.data.remote.dto.TujuanPinjaman
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.HomeRepository
import com.example.sakuku.data.repository.PengajuanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val MIN_NOMINAL = 500_000.0

data class PengajuanUiState(
    val currentStep: Int = 1,
    val isLoading: Boolean = true,
    val plafond: Double? = null,
    val sisaPlafond: Double? = null,
    val pekerjaan: String? = null,
    val pendapatanBulanan: Double? = null,
    val tanggalLahir: String? = null,
    val nik: String? = null,
    val hasFotoKtp: Boolean = false,
    val namaBank: String? = null,
    val nomorRekening: String? = null,
    val namaPemilikRekening: String? = null,
    // Estimasi total cicilan/bulan dari pinjaman lain yang udah cair & tenornya belum habis -
    // dipakai buat "total beban bulanan" di langkah 2 (flat rate, bukan data pembayaran asli).
    val existingCicilanBulanan: Double = 0.0,
    val nominal: Double = 1_000_000.0,
    val tenors: List<BungaTenorResponse> = emptyList(),
    val selectedTenorId: String? = null,
    val tujuanPinjaman: TujuanPinjaman? = null,
    val agreedToTerms: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedTenor: BungaTenorResponse?
        get() = tenors.find { it.id == selectedTenorId }

    val canProceedToStep2: Boolean
        get() = !isLoading && sisaPlafond != null && selectedTenorId != null && nominal >= MIN_NOMINAL

    // Sisa plafond customer bisa di bawah minimum pengajuan (Rp500rb) - misal udah kepakai
    // hampir abis sama pengajuan lain yang masih ketahan. UI pakai ini buat nampilin pesan
    // jelas + nyembunyiin slider, bukan biarin coerceIn di bawah nge-crash (lihat gotcha).
    val belowMinimum: Boolean
        get() = sisaPlafond != null && sisaPlafond < MIN_NOMINAL

    // pekerjaan/pendapatanBulanan opsional pas registrasi (Step 3 Android cuma wajibin
    // tipePekerjaan) tapi backend (PengajuanService.create()) sekarang NOLAK submit kalau
    // salah satunya kosong - dicek di sini juga biar UI kasih tau + arahin ke halaman lengkapi
    // profil SEBELUM customer buang waktu ngisi slider/tenor yang ujungnya ditolak backend.
    val employmentIncomplete: Boolean
        get() = !isLoading && (pekerjaan.isNullOrBlank() || pendapatanBulanan == null)

    // Tanggal lahir juga bagian dari "profil lengkap": kosong -> arahin ke KTP & Data Diri,
    // udah diisi tapi umurnya < 17 tahun -> gak bisa ngajuin sama sekali (syarat KTP).
    val tanggalLahirMissing: Boolean
        get() = !isLoading && tanggalLahir.isNullOrBlank()

    val underage: Boolean
        get() = !isLoading && !tanggalLahir.isNullOrBlank() && !Validators.isOldEnough(tanggalLahir)

    // Rekening wajib ada sebelum ngajuin - dana yang disetujui dicairin ke rekening ini.
    val rekeningMissing: Boolean
        get() = !isLoading && (namaBank.isNullOrBlank() || nomorRekening.isNullOrBlank() || namaPemilikRekening.isNullOrBlank())

    // NIK + foto KTP wajib (backend PengajuanService.create() juga nolak) - staff butuh
    // keduanya buat verifikasi identitas. Bisa kosong kalau langkah Identitas di Register dilewati.
    val ktpMissing: Boolean
        get() = !isLoading && (nik.isNullOrBlank() || !hasFotoKtp)

    val profileIncomplete: Boolean
        get() = ktpMissing || employmentIncomplete || tanggalLahirMissing || underage || rekeningMissing

    // Selisih plafond total vs sisa yang bisa dipakai sekarang - kalau > 0 berarti ada
    // pengajuan lain (masih direview atau udah cair) yang lagi "ketahan" makan jatah plafond.
    val heldAmount: Double?
        get() {
            val total = plafond ?: return null
            val sisa = sisaPlafond ?: return null
            val held = total - sisa
            return if (held > 0) held else null
        }

    // DBR (Debt Burden Ratio) = cicilan bulanan dibagi pendapatan bulanan - sama formula &
    // ambang batas (33%) kayak yang staff liat di drawer review web dashboard. Informational
    // buat bantu customer sendiri menilai kewajaran pengajuannya sebelum submit.
    fun dbrRatio(cicilanBulanan: Double): Double? {
        val income = pendapatanBulanan ?: return null
        if (income <= 0) return null
        return cicilanBulanan / income
    }

    // Estimasi cicilan/bulan buat tenor mana pun - dipakai kartu pilihan tenor (tiap kartu
    // nampilin cicilannya sendiri) dan ringkasan di bar bawah.
    fun cicilanFor(tenor: BungaTenorResponse): Double =
        LoanCalculator.estimate(nominal, tenor.tenor, tenor.interestRate).cicilanBulanan

    val canSubmitFromStep1: Boolean
        get() = !isLoading && !profileIncomplete && !belowMinimum && canProceedToStep2
}

@HiltViewModel
class PengajuanViewModel @Inject constructor(
    private val pengajuanRepository: PengajuanRepository,
    private val homeRepository: HomeRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PengajuanUiState())
    val uiState = _uiState.asStateFlow()

    init {
//        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val profileResult = customerRepository.getMe()
            val tenorResult = homeRepository.getBungaTenor()
            val pengajuanResult = pengajuanRepository.getMyPengajuan()

            val profile = profileResult.getOrNull()
            val sisaPlafond = profile?.sisaPlafond
            val tenors = tenorResult.getOrDefault(emptyList()).sortedBy { it.tenor }

            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    plafond = profile?.plafond,
                    sisaPlafond = sisaPlafond,
                    pekerjaan = profile?.pekerjaan,
                    pendapatanBulanan = profile?.pendapatanBulanan,
                    tanggalLahir = profile?.tanggalLahir,
                    nik = profile?.nik,
                    hasFotoKtp = profile?.hasFotoKtp ?: false,
                    namaBank = profile?.namaBank,
                    nomorRekening = profile?.nomorRekening,
                    namaPemilikRekening = profile?.namaPemilikRekening,
                    // Gagal fetch riwayat -> anggap 0 (cuma bikin estimasi beban lebih rendah,
                    // gak nge-block pengajuan).
                    existingCicilanBulanan = activeCicilanBulanan(pengajuanResult.getOrDefault(emptyList())),
                    tenors = tenors,
                    selectedTenorId = tenors.firstOrNull()?.id,
                    // BUG: coerceIn(min, max) LEMPAR EXCEPTION (bukan clamp) kalau max < min -
                    // ini penyebab asli crash "auto-shutdown" yang dilaporin user pas buka tab
                    // Ajukan. Kejadian kalau sisaPlafond customer < Rp500rb (MIN_NOMINAL) -
                    // misal sisa 200rb doang. Fix: kalau sisaPlafond di bawah minimum, gak ada
                    // "nominal valid" yang bisa dipilih sama sekali - langsung set ke sisaPlafond
                    // apa adanya (UI nanti nyembunyiin slider + nampilin pesan lewat belowMinimum).
                    nominal = when {
                        sisaPlafond == null -> current.nominal
                        sisaPlafond < MIN_NOMINAL -> sisaPlafond
                        else -> current.nominal.coerceIn(MIN_NOMINAL, sisaPlafond)
                    },
                    errorMessage = when {
                        profileResult.isFailure -> "Gagal memuat plafond kamu, coba lagi"
                        tenorResult.isFailure -> "Gagal memuat pilihan tenor, coba lagi"
                        else -> null
                    }
                )
            }
        }
    }

    fun retry() = loadData()

    // Pinjaman yang udah cair dan belum lewat tenornya. tanggalPencairan kosong (data lama)
    // tetap dihitung - lebih aman melebihkan beban daripada nyembunyiin cicilan yang mungkin masih jalan.
    private fun activeCicilanBulanan(list: List<PengajuanMeResponse>, today: LocalDate = LocalDate.now()): Double =
        list.filter { it.status == "DISBURSED" }
            .filter { loan ->
                val cair = loan.tanggalPencairan?.let { runCatching { LocalDateTime.parse(it).toLocalDate() }.getOrNull() }
                cair == null || !cair.plusMonths(loan.tenor.toLong()).isBefore(today)
            }
            .sumOf { loan ->
                LoanCalculator.estimate(loan.nominalDisetujui ?: loan.nominalPengajuan, loan.tenor, loan.interestRate).cicilanBulanan
            }

    fun onNominalChange(value: Double) {
        val max = _uiState.value.sisaPlafond ?: return
        // Sama kayak fix di loadData() - kalau sisa plafond di bawah MIN_NOMINAL, slider harusnya
        // udah disabled di UI, tapi jaga-jaga tetep gak boleh coerceIn(500rb, <500rb) yang crash.
        val min = if (max < MIN_NOMINAL) 0.0 else MIN_NOMINAL
        _uiState.update { it.copy(nominal = value.coerceIn(min, max)) }
    }

    fun onTenorSelect(tenorId: String) {
        _uiState.update { it.copy(selectedTenorId = tenorId) }
    }

    fun onTujuanPinjaman(tujuan: TujuanPinjaman) {
        _uiState.update { it.copy(tujuanPinjaman = tujuan) }
    }

    fun onAgreedToTermsChange(value: Boolean) {
        _uiState.update { it.copy(agreedToTerms = value) }
    }

    fun goToStep2() {
        if (_uiState.value.canProceedToStep2) {
            _uiState.update { it.copy(currentStep = 2) }
        }
    }

    fun goToStep1() {
        _uiState.update { it.copy(currentStep = 1, errorMessage = null) }
    }

    fun submit() {
        val state = _uiState.value
        val tenor = state.selectedTenor ?: return
        if (!state.agreedToTerms) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            pengajuanRepository.createPengajuan(
                PengajuanRequest(
                    idBungaTenor = tenor.id,
                    nominalPengajuan = state.nominal,
                    tujuanPinjaman = state.tujuanPinjaman?.apiValue
                )
            ).onSuccess {
                _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = error.message ?: "Gagal mengirim pengajuan, coba lagi")
                }
            }
        }
    }
}
