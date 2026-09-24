package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Field persis NotificationEntity backend - "isRead" dicross-check langsung ke compiled
// class (getIsRead(), bukan isRead()) karena field-nya Boolean boxed bukan boolean primitif -
// itu bikin Jackson tetep pake nama "isRead", bukan "read" (yang biasanya jadi konvensi kalau
// primitif). Jangan diubah tanpa cross-check ulang.
@Serializable
data class NotificationResponse(
    val id: String,
    val judul: String,
    val pesan: String,
    val isRead: Boolean,
    val createdAt: String? = null,
    // Nested entity - backend serialize NotificationEntity mentah (bukan DTO), field "pengajuan"
    // itu PengajuanEntity utuh, tapi kita cuma butuh id-nya buat nampilin nomor referensi
    // pendek di kartu (lihat LoanCalculator.formatPengajuanRef). ignoreUnknownKeys global udah
    // di-set, jadi field lain di object itu (customer/bungaTenor/dst) aman diabaikan. Nullable -
    // teorinya semua notifikasi yang dibuat lewat NotificationService.create() selalu terkait
    // 1 pengajuan, tapi dijaga null-safe kalau ada baris lama/lain yang enggak.
    val pengajuan: NotificationPengajuanRef? = null
)

// Backend nge-serialize PengajuanEntity utuh di sini, jadi field ringkasan pinjaman ikut
// kebawa - dipakai layar Detail Pencairan (NotifikasiDetailScreen) tanpa perlu request tambahan.
// Semua opsional biar notifikasi lama/aneh gak bikin parse gagal.
@Serializable
data class NotificationPengajuanRef(
    val id: String,
    val status: String? = null,
    val nominalPengajuan: Double? = null,
    val nominalDisetujui: Double? = null,
    val tenor: Int? = null,
    val interestRate: Double? = null,
    val tanggalPencairan: String? = null
)
