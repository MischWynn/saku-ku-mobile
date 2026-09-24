package com.example.sakuku.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.sakuku.ui.theme.*

@Composable
fun SplashScreen(
    onNavigate: (loggedIn: Boolean) -> Unit,
    onRootedDetected: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val showSubtitle by viewModel.showSubtitle.collectAsState()
    val navigateToNext by viewModel.navigateToNext.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isRooted by viewModel.isRooted.collectAsState()

    LaunchedEffect(navigateToNext, isLoggedIn, isRooted) {
        if (navigateToNext) {
            if (isRooted) {
                onRootedDetected()
            } else {
                isLoggedIn?.let { onNavigate(it) }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(BgTop, BgBottom),
                    startY = 0f,
                    endY = h
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BlobLight.copy(alpha = 0.15f),
                        BlobMid.copy(alpha = 0.1f),
                        BlobDark.copy(alpha = 0.0f) // Fade out halus
                    ),
                    center = Offset(w * 0f, h * 0.7f),
                    radius = w * 0.8f
                ),
                center = Offset(w * 0f, h * 0.7f),
                radius = w * 0.8f
            )

            // 3. Blob Kanan Atas
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        BlobMid.copy(alpha = 0.15f),
                        BlobDark.copy(alpha = 0f)
                    ),
                    center = Offset(w * 0.9f, h * 0.1f),
                    radius = w * 0.6f
                ),
                center = Offset(w * 0.9f, h * 0.1f),
                radius = w * 0.6f
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Saku-ku",
                color = Color.White,
                fontSize = 42.sp,
                fontFamily = PlusJakartaSans,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(animationSpec = tween(1000))
            ) {
                Text(
                    text = "Selalu ada",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    fontFamily = PlusJakartaSans,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}