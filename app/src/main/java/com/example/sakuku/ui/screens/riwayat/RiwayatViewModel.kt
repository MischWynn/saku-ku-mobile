package com.example.sakuku.ui.screens.riwayat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.repository.PengajuanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RiwayatUiState(
    val isLoading: Boolean = true,
    val items: List<PengajuanMeResponse> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class RiwayatViewModel @Inject constructor(
    private val pengajuanRepository: PengajuanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RiwayatUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            pengajuanRepository.getMyPengajuan()
                .onSuccess { list ->
                    // String ISO ("yyyy-MM-dd...") sortable lexicographically - terbaru duluan
                    val sorted = list.sortedByDescending { it.tanggalPengajuan ?: "" }
                    _uiState.update { it.copy(isLoading = false, items = sorted) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat riwayat pengajuan") }
                }
        }
    }
}
