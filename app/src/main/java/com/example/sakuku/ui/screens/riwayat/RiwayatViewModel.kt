package com.example.sakuku.ui.screens.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.repository.PengajuanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiwayatUiState(
    // isLoading cuma true sebelum cache lokal pertama kali kebaca (biasanya sekejap) - BUKAN
    // nunggu network, beda dari pola loading di layar lain yang nunggu response API.
    val isLoading: Boolean = true,
    val items: List<PengajuanMeResponse> = emptyList(),
    val errorMessage: String? = null
)

// Offline-first - Room (via PengajuanRepository.observeCachedPengajuan()) jadi single source of
// truth buat UI, bukan hasil network langsung. load() cuma trigger sinkronisasi di background;
// begitu sinkron sukses, Flow dari Room otomatis emit ulang dan UI ke-update sendiri. Kalau
// sinkron gagal (offline dll), item yang udah ke-cache dari sesi sebelumnya tetap kebaca normal -
// errorMessage cuma informational, gak nge-blank list yang udah ada.
@HiltViewModel
class RiwayatViewModel @Inject constructor(
    private val pengajuanRepository: PengajuanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        pengajuanRepository.observeCachedPengajuan()
            .onEach { list ->
                // String ISO ("yyyy-MM-dd...") sortable lexicographically - terbaru duluan
                val sorted = list.sortedByDescending { it.tanggalPengajuan ?: "" }
                _uiState.update { it.copy(isLoading = false, items = sorted) }
            }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            pengajuanRepository.refreshPengajuanCache()
                .onFailure { error ->
                    // Cache lama (kalau ada) tetap ditampilin - cuma numpangin pesan error,
                    // gak nge-reset items ke kosong.
                    _uiState.update { it.copy(errorMessage = error.message ?: "Gagal memuat riwayat pengajuan") }
                }
                .onSuccess {
                    _uiState.update { it.copy(errorMessage = null) }
                }
        }
    }
}
