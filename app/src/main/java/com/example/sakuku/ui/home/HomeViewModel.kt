package com.example.sakuku.ui.home

import com.example.sakuku.data.repository.NotificationRepository
import java.time.temporal.ChronoUnit
import java.time.LocalDate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.HomeRepository
import com.example.sakuku.data.repository.PengajuanRepository
import com.example.sakuku.util.LoanCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_MAX_NOMINAL = 50_000_000.0
private const val MIN_NOMINAL = 500_000.0

// Status yang masih di pipeline review - customer punya "pengajuan aktif" yang lagi dipantau.
// Beda dari status terminal (DISBURSED/REJECTED/CANCELLED) yang gak perlu ditonjolin di Home.
private val PIPELINE_STATUSES = setOf("MARKETING_REVIEW", "BM_REVIEW", "BACKOFFICE_REVIEW")

// Preview 1 kartu tagihan buat Home - versi ringkas dari yang ditampilin penuh di BayarScreen.
data class TagihanPreview(val cicilanBulanan: Double, val dueDateLabel: String?, val daysUntilDue: Long?)

// Syarat data yang WAJIB lengkap sebelum bisa ngajuin pinjaman - sama persis dengan gerbang di
// PengajuanViewModel/PengajuanService, biar meter di Beranda gak bilang "lengkap" padahal
// pengajuannya masih ditolak.
enum class ProfileRequirement(val label: String) {
    KTP("NIK & foto KTP"),
    TANGGAL_LAHIR("Tanggal lahir"),
    PEKERJAAN("Pekerjaan & pendapatan"),
    REKENING("Rekening bank")
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val tiers: List<PlafondResponse> = emptyList(),
    val tenors: List<BungaTenorResponse> = emptyList(),
    val selectedTenorId: String? = null,
    val nominal: Double = 1_000_000.0,
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
    val customerName: String? = null,
    val tierPlafond: String? = null,
    val sisaPlafond: Double? = null,
    val plafondTotal: Double? = null,
    val activeLoan: PengajuanMeResponse? = null,
    val tagihanPreview: TagihanPreview? = null,
    val missingRequirements: List<ProfileRequirement> = emptyList(),
    val unreadNotifCount: Int = 0
) {
    val maxNominal: Double
        get() = if (isLoggedIn && sisaPlafond != null) {
            sisaPlafond
        } else {
            tiers.maxOfOrNull { it.limitMaksimal } ?: DEFAULT_MAX_NOMINAL
        }

    val selectedTenor: BungaTenorResponse?
        get() = tenors.find { it.id == selectedTenorId }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val customerRepository: CustomerRepository,
    private val pengajuanRepository: PengajuanRepository,
    private val notificationRepository: NotificationRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPublicData()
        observeLoginState()
    }

    private fun loadPublicData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val tiersResult = homeRepository.getPlafondTiers()
            val tenorResult = homeRepository.getBungaTenor()

            val tiers = tiersResult.getOrDefault(emptyList()).sortedBy { it.limitMaksimal }
            val tenors = tenorResult.getOrDefault(emptyList()).sortedBy { it.tenor }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    tiers = tiers,
                    tenors = tenors,
                    selectedTenorId = tenors.firstOrNull()?.id,
                    errorMessage = if (tiersResult.isFailure && tenorResult.isFailure) {
                        "Gagal memuat data, coba lagi"
                    } else null
                )
            }
        }
    }

    private fun observeLoginState() {
        viewModelScope.launch {
            tokenDataStore.tokenFlow.collect { token ->
                val loggedIn = token != null
                if (loggedIn) {
                    loadProfileData()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            customerName = null,
                            tierPlafond = null,
                            sisaPlafond = null,
                            plafondTotal = null,
                            activeLoan = null,
                            tagihanPreview = null,
                            missingRequirements = emptyList(),
                            unreadNotifCount = 0
                        )
                    }
                }
            }
        }
    }

    // Dipanggil tiap Beranda kebuka lagi. observeLoginState() cuma reload pas token berubah
    // (login/logout), jadi perubahan profil/plafond/pengajuan setelah itu gak kebaca sampai
    // app dibuka ulang.
    fun refresh() {
        if (_uiState.value.isLoggedIn) loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            val profileResult = customerRepository.getMe()
            val pengajuanResult = pengajuanRepository.getMyPengajuan()
            val notifResult = notificationRepository.getAll()

            val profile = profileResult.getOrNull()
            val pengajuanList = pengajuanResult.getOrDefault(emptyList())

            val activeLoan = pengajuanList.firstOrNull { it.status in PIPELINE_STATUSES }
            val disbursedLoan = pengajuanList
                .filter { it.status == "DISBURSED" }
                .maxByOrNull { it.tanggalPengajuan ?: "" }

            val tagihanPreview = disbursedLoan?.let { loan ->
                val estimate = LoanCalculator.estimate(
                    loan.nominalDisetujui ?: loan.nominalPengajuan,
                    loan.tenor,
                    loan.interestRate
                )
                val dueDate = loan.tanggalPencairan?.let { iso -> LoanCalculator.nextDueDate(iso) }
                TagihanPreview(
                    cicilanBulanan = estimate.cicilanBulanan,
                    dueDateLabel = loan.tanggalPencairan?.let { iso -> LoanCalculator.nextDueDateLabel(iso) },
                    daysUntilDue = dueDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }
                )
            }
            val newMaxNominal = profile?.sisaPlafond ?: _uiState.value.maxNominal

            _uiState.update { current ->
                current.copy(
                    isLoggedIn = true,
                    customerName = profile?.namaLengkap,
                    tierPlafond = profile?.tierPlafond,
                    sisaPlafond = profile?.sisaPlafond,
                    plafondTotal = profile?.plafond,
                    activeLoan = activeLoan,
                    tagihanPreview = tagihanPreview,
                    // Profil gagal dimuat -> jangan nampilin meter "belum lengkap" yang palsu.
                    // Gagal fetch -> badge lonceng pakai angka terakhir, bukan dipaksa 0.
                    unreadNotifCount = notifResult.getOrNull()?.count { n -> !n.isRead } ?: current.unreadNotifCount,
                    missingRequirements = profile?.let { p ->
                        buildList {
                            if (p.nik.isNullOrBlank() || !p.hasFotoKtp) add(ProfileRequirement.KTP)
                            if (p.tanggalLahir.isNullOrBlank()) add(ProfileRequirement.TANGGAL_LAHIR)
                            if (p.pekerjaan.isNullOrBlank() || p.pendapatanBulanan == null) add(ProfileRequirement.PEKERJAAN)
                            if (p.namaBank.isNullOrBlank() || p.nomorRekening.isNullOrBlank() || p.namaPemilikRekening.isNullOrBlank()) {
                                add(ProfileRequirement.REKENING)
                            }
                        }
                    } ?: emptyList(),
                    nominal = if (newMaxNominal < MIN_NOMINAL) {
                        newMaxNominal.coerceAtLeast(0.0)
                    } else {
                        current.nominal.coerceIn(MIN_NOMINAL, newMaxNominal)
                    }
                )
            }
        }
    }

    fun onNominalChange(value: Double) {
        val max = _uiState.value.maxNominal
        val min = if (max < MIN_NOMINAL) 0.0 else MIN_NOMINAL
        _uiState.update { it.copy(nominal = value.coerceIn(min, max)) }
    }

    fun onTenorSelect(tenorId: String) {
        _uiState.update { it.copy(selectedTenorId = tenorId) }
    }

    fun retry() = loadPublicData()
}
