package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// Field persis PengajuanRequest.java backend - jangan ubah nama tanpa cross-check ulang.
@Serializable
data class PengajuanRequest(
    val idBungaTenor: String,
    val nominalPengajuan: Double,
    val tujuanPinjaman: String? = null
)

// Backend balikin PengajuanEntity mentah (bukan DTO khusus) - kita cuma butuh tau sukses
// atau enggak + pesannya, jadi data dibiarin lenient (JsonElement), sama pola kayak
// RegisterApiResponse.
@Serializable
data class PengajuanApiResponse(
    val statusCode: Int,
    val message: String,
    val data: JsonElement? = null
)

// Response GET /pengajuan/me - dicross-check langsung ke JSON asli (13 Sept), field diambil
// cuma yang perlu (nominalPengajuan/tenor/interestRate/nominalDisetujui/status/tujuanPinjaman/
// tanggalPengajuan) - customer & bungaTenor nested sengaja gak di-declare, ignoreUnknownKeys
// di Json config bakal skip otomatis (gak perlu parse hibernateLazyInitializer dkk).
@Serializable
data class PengajuanMeResponse(
    val id: String,
    val nominalPengajuan: Double,
    val tenor: Int,
    val interestRate: Double,
    val nominalDisetujui: Double? = null,
    val status: String,
    val tujuanPinjaman: String? = null,
    val tanggalPengajuan: String? = null,
    // Diisi backend (PengajuanService.disburse()) pas status pindah ke DISBURSED - field baru
    // 15 Sept 2026, jadi data lama yang udah DISBURSED sebelum field ini ada bakal null.
    // Dipakai buat ngitung jatuh tempo tagihan (BayarViewModel), bukan cuma ditampilin mentah.
    val tanggalPencairan: String? = null
)

// Response GET /pengajuan/{id}/history/me - field persis PengajuanHistoryCustomerDTO.java
// backend (cross-check 15 Sept). Sengaja gak ada field `user` (identitas staff internal) -
// itu memang dihilangkan backend, beda dari endpoint staff /{id}/history yang balikin
// ReviewLogEntity mentah. createdAt dibiarin String? (LocalDateTime backend), bukan di-parse
// ke tipe date Kotlin - pola sama kayak tanggalPengajuan/tanggalLahir di tempat lain.
@Serializable
data class PengajuanHistoryResponse(
    val action: String,
    val statusFrom: String? = null,
    val statusTo: String? = null,
    val catatan: String? = null,
    val roleName: String? = null,
    val createdAt: String? = null
)

// 6 kategori resmi tujuanPinjaman di backend (PengajuanEntity.tujuanPinjaman) - pola
// (label, apiValue) sama kayak enum TipePekerjaan di RegisterViewModel.kt.
enum class TujuanPinjaman(val label: String, val apiValue: String) {
    MODAL_USAHA("Modal Usaha", "MODAL_USAHA"),
    KONSUMTIF("Konsumtif", "KONSUMTIF"),
    PENDIDIKAN("Pendidikan", "PENDIDIKAN"),
    KESEHATAN("Kesehatan", "KESEHATAN"),
    RENOVASI("Renovasi", "RENOVASI"),
    LAINNYA("Lainnya", "LAINNYA")
}
