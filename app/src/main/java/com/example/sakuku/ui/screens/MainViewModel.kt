package com.example.sakuku.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    tokenDataStore: TokenDataStore
) : ViewModel() {

    // Guest-access model: Beranda selalu bisa diakses tanpa token, tab lain (Riwayat/Ajukan/
    // Notifikasi/Profil) harus di-soft-gate ke Login - lihat CLAUDE.md project & artifact
    // "Nasabah Screen Guide".
    val isLoggedIn: StateFlow<Boolean> = tokenDataStore.tokenFlow
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
}
