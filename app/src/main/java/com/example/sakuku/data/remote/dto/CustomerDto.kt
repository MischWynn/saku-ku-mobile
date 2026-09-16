package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Field persis CustomerResponseDTO backend (cross-check 13 Sept) - termasuk sisaPlafond &
// tierPlafond yang baru ditambahin sesi ini. tanggalLahir dibiarin String? (bukan LocalDate),
// gak dipakai buat kalkulasi apa pun di sini.
@Serializable
data class CustomerMeResponse(
    val id: String,
    val namaLengkap: String,
    val nik: String? = null,
    val noHp: String? = null,
    val email: String? = null,
    val alamat: String? = null,
    val plafond: Double? = null,
    val status: String? = null,
    val tanggalLahir: String? = null,
    val tipePekerjaan: String? = null,
    val pekerjaan: String? = null,
    val lamaBekerjaBulan: Int? = null,
    val pendapatanBulanan: Double? = null,
    val utangBerjalan: Double? = null,
    val sisaPlafond: Double? = null,
    val tierPlafond: String? = null
)

// Field persis CustomerUpdateRequest.java backend - semua opsional (partial update, null =
// gak diubah). NIK & password sengaja gak ada di sini (bukan bug, memang gak bisa diedit
// lewat jalur ini). tanggalLahir dikirim string ISO "yyyy-MM-dd", Jackson backend nerima
// format itu langsung buat LocalDate.
@Serializable
data class CustomerUpdateRequest(
    val namaLengkap: String? = null,
    val email: String? = null,
    val noHp: String? = null,
    val alamat: String? = null,
    val tanggalLahir: String? = null,
    val tipePekerjaan: String? = null,
    val pekerjaan: String? = null,
    val lamaBekerjaBulan: Int? = null,
    val pendapatanBulanan: Double? = null,
    val utangBerjalan: Double? = null
)

// Field persis CustomerChangePasswordRequest.java backend (16 Sept) - beda dari forgot/reset
// password (OTP-based), ini ganti password saat udah login, butuh password lama buat verifikasi.
@Serializable
data class CustomerChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
