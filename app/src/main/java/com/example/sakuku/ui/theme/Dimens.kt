package com.example.sakuku.ui.theme

import androidx.compose.ui.unit.sp

// Skala ukuran teks niru Tailwind (text-xs..text-9xl), 1rem dipetain ke 16sp - biar gak ada lagi
// angka .sp acak yang beda-beda tipis antar layar (10.5sp, 17sp, 22sp, dst). Line-height-nya juga
// dituruni dari rasio Tailwind asli (mis. text-base rasio 1.5 -> 16sp*1.5=24sp), bukan ditebak.
object TextSize {
    val xs = 12.sp    // text-xs
    val sm = 14.sp    // text-sm
    val base = 16.sp  // text-base
    val lg = 18.sp    // text-lg
    val xl = 20.sp    // text-xl
    val xl2 = 24.sp   // text-2xl
    val xl3 = 30.sp   // text-3xl
    val xl4 = 36.sp   // text-4xl
    val xl5 = 48.sp   // text-5xl
    val xl6 = 60.sp   // text-6xl
    val xl7 = 72.sp   // text-7xl
    val xl8 = 96.sp   // text-8xl
    val xl9 = 128.sp  // text-9xl
}

object TextLineHeight {
    val xs = 16.sp
    val sm = 20.sp
    val base = 24.sp
    val lg = 28.sp
    val xl = 28.sp
    val xl2 = 32.sp
    val xl3 = 36.sp
    val xl4 = 40.sp
    val xl5 = 48.sp
    val xl6 = 60.sp
    val xl7 = 72.sp
    val xl8 = 96.sp
    val xl9 = 128.sp
}
