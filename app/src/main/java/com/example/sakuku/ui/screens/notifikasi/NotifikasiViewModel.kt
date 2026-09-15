package com.example.sakuku.ui.screens.notifikasi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotifikasiUiState(
    val isLoading: Boolean = true,
    val items: List<NotificationResponse> = emptyList(),
    val errorMessage: String? = null
) {
    val unreadCount: Int
        get() = items.count { !it.isRead }
}

@HiltViewModel
class NotifikasiViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotifikasiUiState())
    val uiState = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            notificationRepository.getAll()
                .onSuccess { list ->
                    val sorted = list.sortedByDescending { it.createdAt ?: "" }
                    _uiState.update { it.copy(isLoading = false, items = sorted) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message ?: "Gagal memuat notifikasi") }
                }
        }
    }

    // Optimistic update - langsung tandai terbaca di UI, gak nunggu response API biar
    // kerasa instan; kalau API-nya gagal, badge unread cuma bakal balik lagi pas reload.
    fun markAsRead(id: String) {
        val target = _uiState.value.items.find { it.id == id } ?: return
        if (target.isRead) return

        _uiState.update { state ->
            state.copy(items = state.items.map { if (it.id == id) it.copy(isRead = true) else it })
        }
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
