package com.example.sakuku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val SakukuColorScheme = darkColorScheme(
    primary = BlobDark,
    onPrimary = Color.White,
    secondary = BlobMid,
    tertiary = BlobLight,
    background = BgBottom,
    onBackground = Color.White,
    surface = BgTop,
    onSurface = Color.White
)

private val SakukuTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)

@Composable
fun SakukuTheme(content: @Composable () -> Unit) {
    // Ukuran font sistem tetap dihormati, tapi dibatasi MAX_FONT_SCALE. HP Samsung dkk sering
    // diset font/"zoom layar" gede (bisa sampai 2x), dan banyak baris/kartu di app ini lebarnya
    // pas-pasan (tenor, nominal+badge, e-card Profil) - tanpa batas, teksnya numpuk/kepotong.
    val density = LocalDensity.current
    val cappedDensity = Density(
        density = density.density,
        fontScale = density.fontScale.coerceAtMost(MAX_FONT_SCALE)
    )
    CompositionLocalProvider(LocalDensity provides cappedDensity) {
        MaterialTheme(
            colorScheme = SakukuColorScheme,
            typography = SakukuTypography,
            content = content
        )
    }
}

private const val MAX_FONT_SCALE = 1.15f
