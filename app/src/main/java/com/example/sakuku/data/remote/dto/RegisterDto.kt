package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RegisterRequest(
    val namaLengkap: String,
    val nik: String,
    val email: String,
    val noHp: String,
    val password: String,
    val tipePekerjaan: String? = null,
    val pekerjaan: String? = null,
    val pendapatanBulanan: Long? = null
)

// data dibiarkan generik (JsonElement) karena bentuk pasti response register belum
// dikonfirmasi ke backend - yang penting statusCode/message buat tau sukses/gagal
@Serializable
data class RegisterApiResponse(
    val statusCode: Int,
    val message: String,
    val data: JsonElement? = null
)
