package com.example.sakuku.ui.screens.notifikasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.CustomerMeResponse
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.data.repository.CustomerRepository
import com.example.sakuku.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotifikasiDetailUiState(
    val isLoading: Boolean = true,
    val notification: NotificationResponse? = null,
    // Rekening tujuan pencairan diambil dari profil (customer/me) - notifikasi gak bawa data ini.
    val profile: CustomerMeResponse? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class NotifikasiDetailViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotifikasiDetailUiState())
    val uiState = _uiState.asStateFlow()

    private var lastId: String? = null

    // Gak ada endpoint GET /notifikasi/{id}, jadi ambil list-nya (kecil) lalu cari by id.
    fun load(notificationId: String) {
        lastId = notificationId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val notifResult = notificationRepository.getAll()
            val profileResult = customerRepository.getMe()
            val notification = notifResult.getOrNull()?.find { it.id == notificationId }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    notification = notification,
                    profile = profileResult.getOrNull(),
                    errorMessage = when {
                        notifResult.isFailure -> notifResult.exceptionOrNull()?.message ?: "Gagal memuat notifikasi"
                        notification == null -> "Notifikasi tidak ditemukan"
                        else -> null
                    }
                )
            }
        }
    }

    fun retry() {
        lastId?.let { load(it) }
    }
}
