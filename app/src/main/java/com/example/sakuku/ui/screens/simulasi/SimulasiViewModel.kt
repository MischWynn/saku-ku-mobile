package com.example.sakuku.ui.screens.simulasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import javax.inject.Inject

private const val DEFAULT_MAX_NOMINAL = 50_000_000.0
private const val MIN_NOMINAL = 500_000.0

data class SimulasiUiState(
        val isLoading: Boolean = true,
        val tenors: List<BungaTenorResponse> = emptyList(),
        val selectedTenorId: String? = null,
        val nominal: Double = 1_000_000.0,
        val maxNominal: Double = DEFAULT_MAX_NOMINAL,
        val errorMessage: String? = null,
    ) {
    val selectedTenor: BungaTenorResponse?
            get() = tenors.find { it.id == selectedTenorId }
    }

    @HiltViewModel
    class SimulasiViewModel @Inject constructor(
        private val homeRepository: HomeRepository

    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SimulasiUiState())
        val uiState = _uiState.asStateFlow()

        init {
            load()
        }
        private fun load() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }

                val tiersResult = homeRepository.getPlafondTiers()
                val tenorResult = homeRepository.getBungaTenor()
                val maxNominal = tiersResult.getOrDefault(emptyList())
                    .maxOfOrNull { it.limitMaksimal } ?: DEFAULT_MAX_NOMINAL
                val tenors = tenorResult.getOrDefault(emptyList()).sortedBy { it.tenor }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        maxNominal = maxNominal,
                        nominal = it.nominal.coerceIn(MIN_NOMINAL, maxNominal),
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
            val max = _uiState.value.maxNominal
            val min = if (max < MIN_NOMINAL) 0.0 else MIN_NOMINAL
            _uiState.update { it.copy(nominal = value.coerceIn(min, max)) }
        }


        fun onTenorSelect(tenorId: String) {
            _uiState.update { it.copy(selectedTenorId = tenorId) }
        }

        fun retry() = load()
    }