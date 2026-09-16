package com.example.sakuku.ui.home

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
data class TagihanPreview(val cicilanBulanan: Double, val dueDateLabel: String?)

data class HomeUiState(
    val isLoading: Boolean = true,
    val tiers: List<PlafondResponse> = emptyList(),
    val tenors: List<BungaTenorResponse> = emptyList(),
    val selectedTenorId: String? = null,
    val nominal: Double = 1_000_000.0,
    val errorMessage: String? = null,
    // --- Personalisasi logged-in - semua null/false kalau guest ---
    val isLoggedIn: Boolean = false,
    val customerName: String? = null,
    val tierPlafond: String? = null,
    val sisaPlafond: Double? = null,
    val plafondTotal: Double? = null,
    // Pengajuan yang masih direview - null kalau gak ada yang lagi jalan.
    val activeLoan: PengajuanMeResponse? = null,
    // Preview tagihan dari pinjaman DISBURSED terbaru - null kalau gak ada pinjaman aktif.
    val tagihanPreview: TagihanPreview? = null
) {
    // Guest di-cap ke tier tertinggi yang ada (belum py plafond personal). Customer login
    // di-cap ke sisaPlafond ASLI-nya, bukan tier tertinggi - itu batas yang beneran berlaku.
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
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadPublicData()
        observeLoginState()
    }

    // Katalog tier/tenor itu publik (permitAll di backend) - selalu di-fetch terlepas dari
    // status login, dipakai bareng buat widget Simulasi & strip tier baik versi guest maupun
    // logged-in.
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

    // Reaktif terhadap TokenDataStore - kalau user login/logout sementara Home ini masih di
    // memory (jarang kejadian karena nav biasanya re-create Home, tapi lebih aman gini
    // daripada one-shot check), profil ke-refresh/dikosongin otomatis.
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
                            tagihanPreview = null
                        )
                    }
                }
            }
        }
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            val profileResult = customerRepository.getMe()
            val pengajuanResult = pengajuanRepository.getMyPengajuan()

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
                TagihanPreview(
                    cicilanBulanan = estimate.cicilanBulanan,
                    dueDateLabel = loan.tanggalPencairan?.let { iso -> LoanCalculator.nextDueDateLabel(iso) }
                )
            }

            // sisaPlafond baru ini yang bakal jadi maxNominal begitu isLoggedIn=true - dihitung
            // eksplisit di sini (bukan baca it.maxNominal di dalam update{}, yang masih baca
            // state LAMA sebelum copy diterapin). Sama pola aman kayak fix crash Ajukan
            // Pinjaman: kalau sisaPlafond di bawah MIN_NOMINAL, jangan coerceIn (bisa nge-throw
            // "max < min"), langsung pakai sisaPlafond apa adanya.
            val newMaxNominal = profile?.sisaPlafond ?: _uiState.value.maxNominal

            _uiState.update { current ->
                current.copy(
                    isLoggedIn = true,
                    customerName = profile?.namaLengkap,
                    tierPlafond = profile?.tierPlafond,
                    sisaPlafond = profile?.sisaPlafond,
                    plafondTotal = profile?.plafond,
                    // Kalau ada pengajuan yang masih direview, itu yang ditonjolin duluan -
                    // baru kalau gak ada, tagihan pinjaman yang udah cair ditampilin.
                    activeLoan = activeLoan,
                    tagihanPreview = if (activeLoan == null) tagihanPreview else null,
                    // Nominal simulasi ikut nyesuain begitu sisaPlafond keisi, biar gak nyangkut
                    // di atas batas yang beneran berlaku buat customer ini.
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
