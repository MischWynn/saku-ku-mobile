package com.example.sakuku.ui.screens.profil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.ui.screens.register.TipePekerjaan
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfilUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val namaLengkap: String = "",
    val email: String = "",
    val noHp: String = "",
    val alamat: String = "",
    val tipePekerjaan: TipePekerjaan? = null,
    val pekerjaan: String = "",
    val pendapatanBulanan: String = "",
    val plafond: Double? = null,
    val sisaPlafond: Double? = null,
    val tierPlafond: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfilViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            customerRepository.getMe()
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            namaLengkap = profile.namaLengkap,
                            email = profile.email ?: "",
                            noHp = profile.noHp ?: "",
                            alamat = profile.alamat ?: "",
                            tipePekerjaan = TipePekerjaan.entries.find { t -> t.apiValue == profile.tipePekerjaan },
                            pekerjaan = profile.pekerjaan ?: "",
                            pendapatanBulanan = profile.pendapatanBulanan?.let { p -> p.toLong().toString() } ?: "",
                            plafond = profile.plafond,
                            sisaPlafond = profile.sisaPlafond,
                            tierPlafond = profile.tierPlafond
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat profil") }
                }
        }
    }

    fun onNamaLengkapChange(v: String) = _uiState.update { it.copy(namaLengkap = v, successMessage = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, successMessage = null) }
    fun onNoHpChange(v: String) = _uiState.update { it.copy(noHp = v, successMessage = null) }
    fun onAlamatChange(v: String) = _uiState.update { it.copy(alamat = v, successMessage = null) }
    fun onTipePekerjaanChange(v: TipePekerjaan) = _uiState.update { it.copy(tipePekerjaan = v, successMessage = null) }
    fun onPekerjaanChange(v: String) = _uiState.update { it.copy(pekerjaan = v, successMessage = null) }
    fun onPendapatanChange(v: String) = _uiState.update { it.copy(pendapatanBulanan = v.filter { c -> c.isDigit() }, successMessage = null) }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            customerRepository.updateMe(
                CustomerUpdateRequest(
                    namaLengkap = state.namaLengkap.ifBlank { null },
                    email = state.email.ifBlank { null },
                    noHp = state.noHp.ifBlank { null },
                    alamat = state.alamat.ifBlank { null },
                    tipePekerjaan = state.tipePekerjaan?.apiValue,
                    pekerjaan = state.pekerjaan.ifBlank { null },
                    pendapatanBulanan = state.pendapatanBulanan.toDoubleOrNull()
                )
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, successMessage = "Profil berhasil diperbarui") }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, errorMessage = error.message ?: "Gagal menyimpan profil") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenDataStore.clearToken()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
