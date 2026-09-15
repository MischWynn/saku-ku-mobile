package com.example.sakuku.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_MAX_NOMINAL = 50_000_000.0
private const val MIN_NOMINAL = 500_000.0

data class HomeUiState(
    val isLoading: Boolean = true,
    val tiers: List<PlafondResponse> = emptyList(),
    val tenors: List<BungaTenorResponse> = emptyList(),
    val selectedTenorId: String? = null,
    val nominal: Double = 1_000_000.0,
    val errorMessage: String? = null
) {
    val maxNominal: Double
        get() = tiers.maxOfOrNull { it.limitMaksimal } ?: DEFAULT_MAX_NOMINAL

    val selectedTenor: BungaTenorResponse?
        get() = tenors.find { it.id == selectedTenorId }
}

// Dipakai bareng buat Beranda (tamu maupun login) - guest gak punya plafond personal, jadi
// slider di-cap ke tier tertinggi yang ada (bukan ke sisaPlafond customer, itu baru relevan
// begitu user login - lihat CLAUDE.md project soal sisaPlafond).
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
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

    fun onNominalChange(value: Double) {
        _uiState.update { it.copy(nominal = value.coerceIn(MIN_NOMINAL, it.maxNominal)) }
    }

    fun onTenorSelect(tenorId: String) {
        _uiState.update { it.copy(selectedTenorId = tenorId) }
    }

    fun retry() = loadData()
}
