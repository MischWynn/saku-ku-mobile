package com.example.sakuku.ui.theme

import androidx.compose.ui.graphics.Color

val BgTop = Color(0xFF08120F)
val BgBottom = Color(0xFF0A1614)

val BlobLight = Color(0xFFD1FFAE)
val BlobMid = Color(0xFF59B910)
val BlobDark = Color(0xFF10B981)

// Cyan-turquoise buat tombol - persis kode warna dari Figma user (13 Sept), dipisah dari
// BlobMid/BlobDark (yang tetep dipakai di checkbox/border/focus-ring field) biar gak ikut
// kesenggol pas cuma minta ganti warna tombol.
val ButtonTurquoiseLight = Color(0xFF10B981)
val ButtonTurquoiseDeep = Color(0xFF22D3EE)

// Diambil dari referensi KotlinTest (res/drawable/bg_gradient_dark.xml)
val Teal900 = Color(0xFF004D40)
val OverlayDark50 = Color(0x80000000)