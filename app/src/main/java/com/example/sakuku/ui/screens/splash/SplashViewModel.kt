package com.example.sakuku.ui.screens.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.security.RootChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    @ApplicationContext private val appContext: Context
) : ViewModel() {
    private val _showSubtitle = MutableStateFlow(false)
    val showSubtitle = _showSubtitle.asStateFlow()

    private val _navigateToNext = MutableStateFlow(false)
    val navigateToNext = _navigateToNext.asStateFlow()

    // Null selama belum dicek - LaunchedEffect di SplashScreen nunggu ini non-null dulu
    // sebelum navigate, biar gak sempet nge-flash ke Onboarding baru lompat ke Main.
    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    // App-level security gate (Saku-Ku Checkpoint, "Technical direction - decided 19 Sept"):
    // checked once here at startup, not per-screen. Default false so a slow check never
    // accidentally blocks a legitimate device before it resolves.
    private val _isRooted = MutableStateFlow(false)
    val isRooted = _isRooted.asStateFlow()

    init {
        viewModelScope.launch {
            // File/PackageManager/Runtime.exec checks touch disk and IPC - off the main thread.
            val rooted = withContext(Dispatchers.IO) { RootChecker.isDeviceRooted(appContext) }
            _isRooted.value = rooted
            if (rooted) {
                // Skip the login check and splash animation entirely - go straight to blocking,
                // don't reveal any app content (not even the splash subtitle) on a rooted device.
                _navigateToNext.value = true
                return@launch
            }

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
