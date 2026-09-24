package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Bentuk minimal buat parse ulang errorBody() dari HttpException - backend balikin ApiResponse<T>
// penuh {statusCode,message,data} bahkan buat response error (401/403/422/dst), tapi kita cuma
// butuh "message"-nya doang di sini, gak perlu tau tipe T (data biasanya null buat error).
@Serializable
data class ApiErrorBody(
    val statusCode: Int? = null,
    val message: String? = null
)
