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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sakuku.ui.theme.*

@Composable
fun SplashScreen(
    onNavigate: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val showSubtitle by viewModel.showSubtitle.collectAsState()
    val navigateToNext by viewModel.navigateToNext.collectAsState()

    LaunchedEffect(navigateToNext) {
        if (navigateToNext) onNavigate()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Lapisan Background & Blob
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Background Vertikal Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(BgTop, BgBottom),
                    startY = 0f,
                    endY = h
                )
            )

            // 2. Blob Kiri Bawah / Tengah (Mensimulasikan tekstur kasar)
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

        // Lapisan Tipografi
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