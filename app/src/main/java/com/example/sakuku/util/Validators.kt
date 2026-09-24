package com.example.sakuku.util

import java.time.LocalDate
import java.time.Period

// Aturan validasi yang dipakai bareng di beberapa layar (Register, Password Baru, Keamanan Akun,
// KTP & Data Diri, Ajukan Pinjaman) - biar pesannya seragam dan gak ada layar yang ketinggalan
// aturan lama.
object Validators {

    // Umur minimal punya KTP.
    const val MIN_AGE = 17

    // null = valid. Sengaja gak wajib simbol - cukup huruf besar + huruf kecil + angka.
    fun passwordError(password: String): String? = when {
        password.length < 8 -> "Password minimal 8 karakter"
        password.none { it.isUpperCase() } -> "Password harus mengandung minimal 1 huruf besar"
        password.none { it.isLowerCase() } -> "Password harus mengandung minimal 1 huruf kecil"
        password.none { it.isDigit() } -> "Password harus mengandung minimal 1 angka"
        else -> null
    }

    // Tanggal paling muda yang masih boleh dipilih (hari ini dikurangi 17 tahun).
    fun latestAllowedBirthDate(today: LocalDate = LocalDate.now()): LocalDate =
        today.minusYears(MIN_AGE.toLong())

    // isoDate format "yyyy-MM-dd" (format yang dikirim/diterima backend). Tanggal gak kebaca = false.
    fun isOldEnough(isoDate: String?, today: LocalDate = LocalDate.now()): Boolean {
        val birthDate = isoDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return false
        return Period.between(birthDate, today).years >= MIN_AGE
    }

    fun tanggalLahirError(isoDate: String): String? = when {
        isoDate.isBlank() -> "Tanggal lahir wajib diisi"
        !isOldEnough(isoDate) -> "Usia minimal $MIN_AGE tahun (sesuai syarat kepemilikan KTP)"
        else -> null
    }
}
