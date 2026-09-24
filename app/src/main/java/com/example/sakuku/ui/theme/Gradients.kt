package com.example.sakuku.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

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

// Varian background buat layar-layar seputar pengajuan (Ajukan, Riwayat, Notifikasi, Detail):
// sama kayak sakukuBlobBackground() (gradient + blob bawah), ditambah blob hijau-cyan di atas
// dan tekstur titik halus - biar ada warna di area judul, gak flat.
fun Modifier.sakukuBlobBackgroundTop(): Modifier = this
    .sakukuBlobBackground()
    .drawBehind {
        val w = size.width
        val h = size.height

        // Blob atas: hijau di kiri atas, cyan di kanan atas - saling tumpang jadi gradient.
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(BlobMid.copy(alpha = 0.22f), BlobDark.copy(alpha = 0f)),
                center = Offset(w * 0.15f, 0f),
                radius = w * 0.9f
            ),
            center = Offset(w * 0.15f, 0f),
            radius = w * 0.9f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF22D3EE).copy(alpha = 0.14f), Color(0xFF22D3EE).copy(alpha = 0f)),
                center = Offset(w * 0.95f, h * 0.08f),
                radius = w * 0.75f
            ),
            center = Offset(w * 0.95f, h * 0.08f),
            radius = w * 0.75f
        )

        // Tekstur titik (dot grid) tipis, cuma di sepertiga atas biar fokus ke area judul.
        val spacing = 22.dp.toPx()
        val dotRadius = 1.dp.toPx()
        val dotColor = Color.White.copy(alpha = 0.05f)
        var y = spacing / 2
        while (y < h * 0.35f) {
            var x = spacing / 2
            while (x < w) {
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
                x += spacing
            }
            y += spacing
        }
    }
