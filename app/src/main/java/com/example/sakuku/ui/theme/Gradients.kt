package com.example.sakuku.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

// Padanan bg_gradient_dark.xml (KotlinTest) - angle 245 (kanan-atas -> kiri-bawah)
val SakukuPageGradient = Brush.linearGradient(
    colors = listOf(Teal900, OverlayDark50),
    start = Offset(Float.POSITIVE_INFINITY, 0f),
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

// Background senada SplashScreen (gradient BgTop->BgBottom + blob glow) biar kartu-kartu
// glassmorphism di atasnya (Home, dst) kelihatan efek kacanya - beda dari SakukuPageGradient
// yang gradient polos gak ada tekstur buat di-blend. Cuma 1 blob di tengah-bawah (bukan 2 blob
// kiri-atas/kanan-bawah kayak splash), sesuai keputusan biar gak numpuk sama konten yang discroll.
fun Modifier.sakukuBlobBackground(): Modifier = this.drawBehind {
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
                BlobMid.copy(alpha = 0.20f),
                BlobDark.copy(alpha = 0.10f),
                BlobDark.copy(alpha = 0f)
            ),
            center = Offset(w * 0.5f, h),
            radius = w * 1.1f
        ),
        center = Offset(w * 0.5f, h),
        radius = w * 1.1f
    )
}
