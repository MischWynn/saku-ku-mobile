package com.example.sakuku.ui.screens

import androidx.lifecycle.ViewModel
import com.example.sakuku.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

// Dipakai AppNavigation buat ngedengerin event "sesi habis" (AuthInterceptor -> TokenDataStore)
// dan maksa navigasi ke Login begitu itu kejadian - lihat komentar di TokenDataStore
// dan AuthInterceptor buat alasan lengkapnya.
@HiltViewModel
class SessionViewModel @Inject constructor(
    tokenDataStore: TokenDataStore
) : ViewModel() {
    val sessionExpiredEvents: SharedFlow<Unit> = tokenDataStore.sessionExpiredEvents
}
