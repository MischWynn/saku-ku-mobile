package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Field persis nama JSON asli backend (PlafondEntity/BungaTenorEntity) - jangan diubah tanpa
// cross-check ulang ke response asli, banyak kejadian mismatch nama field di project ini.
@Serializable
data class PlafondResponse(
    val idPlafond: String,
    val namaPlafond: String,
    val deskripsi: String? = null,
    val tipe: String? = null,
    val limitMaksimal: Double,
    val status: String
)

@Serializable
data class BungaTenorResponse(
    val id: String,
    val tenor: Int,
    val interestRate: Double,
    val status: String
)
