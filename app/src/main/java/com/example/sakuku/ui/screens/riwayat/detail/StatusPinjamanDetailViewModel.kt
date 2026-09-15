package com.example.sakuku.ui.screens.riwayat.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.PengajuanHistoryResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.repository.PengajuanRepository
import com.example.sakuku.ui.screens.riwayat.isRejectedStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatusPinjamanDetailUiState(
    val isLoading: Boolean = true,
    val item: PengajuanMeResponse? = null,
    val history: List<PengajuanHistoryResponse> = emptyList(),
    val errorMessage: String? = null
) {
    // Alasan penolakan yang WAJIB ditonjolkan (bukan cuma badge status) kalau statusnya
    // ditolak - diambil dari entry riwayat terakhir yang statusTo-nya persis status sekarang.
    val rejectionReason: String?
        get() {
            val status = item?.status ?: return null
            if (!isRejectedStatus(status)) return null
            return history.lastOrNull { it.statusTo == status }?.catatan
        }
}

// GET /pengajuan/me gak punya versi "by id", jadi summary item diambil dari list yang sama
// yang dipakai RiwayatScreen (find by id) - dipanggil bareng sama fetch history, gak nambah
// endpoint baru buat ini.
@HiltViewModel
class StatusPinjamanDetailViewModel @Inject constructor(
    private val pengajuanRepository: PengajuanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusPinjamanDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var currentId: String? = null

    fun load(id: String) {
        if (currentId == id) return
        currentId = id
        fetch(id)
    }

    fun retry() {
        currentId?.let { fetch(it) }
    }

    private fun fetch(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val meResult = pengajuanRepository.getMyPengajuan()
            val historyResult = pengajuanRepository.getHistory(id)

            val item = meResult.getOrNull()?.find { it.id == id }
            val history = historyResult.getOrDefault(emptyList()).sortedBy { it.createdAt ?: "" }

            val error = when {
                meResult.isFailure -> meResult.exceptionOrNull()?.message ?: "Gagal memuat data pengajuan"
                historyResult.isFailure -> historyResult.exceptionOrNull()?.message ?: "Gagal memuat riwayat proses"
                item == null -> "Pengajuan tidak ditemukan"
                else -> null
            }

            _uiState.update {
                it.copy(isLoading = false, item = item, history = history, errorMessage = error)
            }
        }
    }
}
