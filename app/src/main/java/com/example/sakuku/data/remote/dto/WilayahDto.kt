package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Response nusantara.clowdlab.com (dipindah dari emsifa/api-wilayah-indonesia 18 Sept karena
// emsifa keblokir dari network user) - ignoreUnknownKeys sudah di-set global di NetworkModule,
// jadi field tambahan (capital/latitude/longitude/province_id/regency_id/dst) di tiap level
// aman diabaikan, 1 DTO cukup buat ketiga level.
@Serializable
data class WilayahItem(
    val id: String,
    val name: String
)

// nusantara.clowdlab.com membungkus tiap list response dalam { success, data } - beda dari
// emsifa yang balikin array polos.
@Serializable
data class WilayahListResponse(
    val success: Boolean = true,
    val data: List<WilayahItem> = emptyList()
)
