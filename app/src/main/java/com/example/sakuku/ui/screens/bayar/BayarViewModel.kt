package com.example.sakuku.ui.screens.bayar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.repository.PengajuanRepository
import com.example.sakuku.util.LoanCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Item siap-tampil - perhitungan (cicilan, jatuh tempo) dilakuin sekali di sini, bukan di
// Composable, biar UI-nya tinggal render string jadi.
data class TagihanItem(
    val loan: PengajuanMeResponse,
    val cicilanBulanan: Double,
    // null kalau backend belum ngisi tanggalPencairan (data lama sebelum field ini ada,
    // atau belum ke-restart pas field baru ditambah) - UI nampilin "belum tersedia".
    val dueDateLabel: String?
)

data class BayarUiState(
    val isLoading: Boolean = true,
    // Cuma pinjaman yang statusnya DISBURSED - itu doang yang punya tagihan berjalan.
    val items: List<TagihanItem> = emptyList(),
    val errorMessage: String? = null
)

// Layar ini SENGAJA informational-only, bukan sistem pembayaran/ledger beneran (keputusan
// 11 Sept, lihat CLAUDE.md project) - gak ada tracking "udah bayar bulan ini apa belum", gak
// ada endpoint bayar. Tagihan bulanan dihitung ulang di client dari data /pengajuan/me yang
// udah ada, zero backend baru buat bagian ini.
//
// Tanggal jatuh tempo BELUM bisa ditampilin - butuh field tanggalPencairan di PengajuanEntity
// (backend ask, sama pola kayak tujuan_pinjaman) yang belum ada. Sengaja gak ditebak/di-derive
// dari tanggalPengajuan (itu tanggal APPLY, bukan tanggal CAIR - beda, nebak dari situ bisa
// nyesatin) - UI nampilin "belum tersedia" sampai field itu beneran ada.
@HiltViewModel
class BayarViewModel @Inject constructor(
    private val pengajuanRepository: PengajuanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BayarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            pengajuanRepository.getMyPengajuan()
                .onSuccess { list ->
                    val items = list.filter { it.status == "DISBURSED" }
                        .sortedByDescending { it.tanggalPengajuan ?: "" }
                        .map { loan ->
                            val estimate = LoanCalculator.estimate(
                                loan.nominalDisetujui ?: loan.nominalPengajuan,
                                loan.tenor,
                                loan.interestRate
                            )
                            TagihanItem(
                                loan = loan,
                                cicilanBulanan = estimate.cicilanBulanan,
                                dueDateLabel = loan.tanggalPencairan?.let { LoanCalculator.nextDueDateLabel(it) }
                            )
                        }
                    _uiState.update { it.copy(isLoading = false, items = items) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat tagihan") }
                }
        }
    }
}
