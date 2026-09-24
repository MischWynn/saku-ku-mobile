package com.example.sakuku.ui.theme

import androidx.compose.ui.graphics.Color

// Gaya visual per tier plafond (e-card Profil, dan bisa dipakai layar lain). Warna accent-nya
// sama dengan tierColor() di HomeScreen, biar tier kebaca konsisten di seluruh app.
data class TierStyle(
    val gradient: List<Color>,  // background kartu, dari gelap ke terang
    val accent: Color,          // badge, ring motif, titik level
    val level: Int              // 1..4, buat indikator "posisi tier" di kartu
)

private val BronzeStyle = TierStyle(
    gradient = listOf(Color(0xFF1F130A), Color(0xFF5C3518), Color(0xFFC98A4B).copy(alpha = 0.55f)),
    accent = Color(0xFFC98A4B),
    level = 1
)

private val SilverStyle = TierStyle(
    gradient = listOf(Color(0xFF12171D), Color(0xFF3E4A57), Color(0xFFAEBCC9).copy(alpha = 0.55f)),
    accent = Color(0xFFAEBCC9),
    level = 2
)

private val GoldStyle = TierStyle(
    gradient = listOf(Color(0xFF1E1605), Color(0xFF6B5114), Color(0xFFE7C468).copy(alpha = 0.60f)),
    accent = Color(0xFFE7C468),
    level = 3
)

private val PlatinumStyle = TierStyle(
    gradient = listOf(Color(0xFF071A20), Color(0xFF1F5A6A), Color(0xFF8FD4E6).copy(alpha = 0.60f)),
    accent = Color(0xFF8FD4E6),
    level = 4
)

// Tier gak dikenal / belum ada -> gaya teal brand yang lama.
private val DefaultStyle = TierStyle(
    gradient = listOf(BgBottom, Teal900, BlobMid.copy(alpha = 0.35f)),
    accent = BlobMid,
    level = 0
)

fun tierStyleOf(tier: String?): TierStyle = when (tier?.lowercase()) {
    "bronze" -> BronzeStyle
    "silver" -> SilverStyle
    "gold" -> GoldStyle
    "platinum" -> PlatinumStyle
    else -> DefaultStyle
}

const val TIER_LEVEL_COUNT = 4
