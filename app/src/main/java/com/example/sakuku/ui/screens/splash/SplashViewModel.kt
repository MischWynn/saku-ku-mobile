package com.example.sakuku.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _showSubtitle = MutableStateFlow(false)
    val showSubtitle = _showSubtitle.asStateFlow()

    private val _navigateToNext = MutableStateFlow(false)
    val navigateToNext = _navigateToNext.asStateFlow()

    init {
        viewModelScope.launch {
            delay(800)
            _showSubtitle.value = true
            delay(1500)
            _navigateToNext.value = true
        }
    }
}