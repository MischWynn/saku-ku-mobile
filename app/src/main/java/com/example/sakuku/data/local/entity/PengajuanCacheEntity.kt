package com.example.sakuku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.sakuku.data.remote.dto.PengajuanMeResponse

// Cache lokal Riwayat Pengajuan - field-nya niru PengajuanMeResponse persis, dipisah biar
// perubahan bentuk DTO API gak otomatis ngubah skema tabel lokal begitu aja.
@Entity(tableName = "pengajuan_cache")
data class PengajuanCacheEntity(
    @PrimaryKey val id: String,
    val nominalPengajuan: Double,
    val tenor: Int,
    val interestRate: Double,
    val nominalDisetujui: Double?,
    val status: String,
    val tujuanPinjaman: String?,
    val tanggalPengajuan: String?,
    val tanggalPencairan: String?
)

fun PengajuanMeResponse.toCacheEntity() = PengajuanCacheEntity(
    id = id,
    nominalPengajuan = nominalPengajuan,
    tenor = tenor,
    interestRate = interestRate,
    nominalDisetujui = nominalDisetujui,
    status = status,
    tujuanPinjaman = tujuanPinjaman,
    tanggalPengajuan = tanggalPengajuan,
    tanggalPencairan = tanggalPencairan
)

fun PengajuanCacheEntity.toResponse() = PengajuanMeResponse(
    id = id,
    nominalPengajuan = nominalPengajuan,
    tenor = tenor,
    interestRate = interestRate,
    nominalDisetujui = nominalDisetujui,
    status = status,
    tujuanPinjaman = tujuanPinjaman,
    tanggalPengajuan = tanggalPengajuan,
    tanggalPencairan = tanggalPencairan
)
