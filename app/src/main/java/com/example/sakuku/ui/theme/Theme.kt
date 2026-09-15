package com.example.sakuku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
    MaterialTheme(
        colorScheme = SakukuColorScheme,
        typography = SakukuTypography,
        content = content
    )
}
