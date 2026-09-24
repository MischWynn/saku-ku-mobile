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
    val tierPlafond: String? = null,
    // Boolean doang, bukan data Base64 mentah - backend sengaja gak balikin foto KTP-nya
    // sendiri (keputusan produk: gak ditampilin ke UI, cuma "sudah difoto apa belum").
    val hasFotoKtp: Boolean = false,
    // Domisili cascading dropdown (Provinsi/Kota/Kecamatan) + Rekening Bank - ditambah 17 Sept
    val provinsi: String? = null,
    val kota: String? = null,
    val kecamatan: String? = null,
    val namaBank: String? = null,
    val nomorRekening: String? = null,
    val namaPemilikRekening: String? = null
)

// Field persis CustomerUpdateRequest.java backend - semua opsional (partial update, null =
// gak diubah). Password sengaja gak ada di sini (jalur ganti password sendiri). NIK BOLEH
// dikirim di sini sejak 17 Sept, tapi cuma efektif SEKALI - backend nolak kalau NIK udah
// keisi sebelumnya (identitas permanen, lihat CustomerAuthService.updateOwnProfile).
// tanggalLahir dikirim string ISO "yyyy-MM-dd", Jackson backend nerima format itu langsung
// buat LocalDate. provinsi/kota/kecamatan disimpen nama-string langsung (bukan kode wilayah).
@Serializable
data class CustomerUpdateRequest(
    val namaLengkap: String? = null,
    val nik: String? = null,
    val email: String? = null,
    val noHp: String? = null,
    val alamat: String? = null,
    val tanggalLahir: String? = null,
    val tipePekerjaan: String? = null,
    val pekerjaan: String? = null,
    val lamaBekerjaBulan: Int? = null,
    val pendapatanBulanan: Double? = null,
    val utangBerjalan: Double? = null,
    // Base64, dikirim pas customer capture/retake foto KTP.
    val fotoKtp: String? = null,
    val provinsi: String? = null,
    val kota: String? = null,
    val kecamatan: String? = null,
    val namaBank: String? = null,
    val nomorRekening: String? = null,
    val namaPemilikRekening: String? = null
)

// Dikirim abis login/dapet token baru dari PushMessagingService.onRegistered - endpoint
// terpisah dari CustomerUpdateRequest sengaja, ini side-effect device bukan data profil.
@Serializable
data class FcmTokenRequest(
    val fcmToken: String
)

// Konfirmasi password sebelum hapus akun beneran - dipanggil dari Profil.
@Serializable
data class CustomerDeleteAccountRequest(
    val password: String
)

// Field persis CustomerChangePasswordRequest.java backend (16 Sept) - beda dari forgot/reset
// password (OTP-based), ini ganti password saat udah login, butuh password lama buat verifikasi.
@Serializable
data class CustomerChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)
