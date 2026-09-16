package com.example.sakuku.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : ViewModel() {
    private val _showSubtitle = MutableStateFlow(false)
    val showSubtitle = _showSubtitle.asStateFlow()

    private val _navigateToNext = MutableStateFlow(false)
    val navigateToNext = _navigateToNext.asStateFlow()

    // Null selama belum dicek - LaunchedEffect di SplashScreen nunggu ini non-null dulu
    // sebelum navigate, biar gak sempet nge-flash ke Onboarding baru lompat ke Main.
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            // Token yang ada di sini gak divalidasi expiry-nya di sini - kalau ternyata basi,
            // AuthInterceptor bakal nge-clear begitu ada request yang ke-401, dan Home sendiri
            // aman diakses guest jadi gak ada resiko nyangkut kalau ternyata token-nya gak valid.
            _isLoggedIn.value = tokenDataStore.getTokenOnce() != null
            delay(800)
            _showSubtitle.value = true
            delay(1500)
            _navigateToNext.value = true
        }
    }
}
