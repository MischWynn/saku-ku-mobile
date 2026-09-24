package com.example.sakuku.ui.screens.plafond

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.data.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlafondUiState(
    val isLoading: Boolean = true,
    val tiers: List<PlafondResponse> = emptyList(),
    val errorMessage: String? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class PlafondViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlafondUiState())
    val uiState = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            homeRepository.getPlafondTiers()
                .onSuccess { tiers ->
                    _uiState.update { it.copy(isLoading = false, tiers = tiers.sortedBy { t -> t.limitMaksimal }) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat data plafond") }
                }
        }
    }
}
