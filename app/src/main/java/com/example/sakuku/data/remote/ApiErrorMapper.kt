package com.example.sakuku.data.remote

import com.example.sakuku.data.remote.dto.ApiErrorBody
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException

// Backend selalu balikin body ApiResponse<T> {statusCode,message,data} bahkan buat error
// (401/403/422/dst), tapi Retrofit ngelempar HttpException duluan buat status non-2xx SEBELUM
// body-nya sempet di-parse ke tipe yang diminta - jadi kalau repository cuma pake `e.message`
// polos di catch block, yang keluar cuma bawaan Retrofit sendiri (mis. "HTTP 401 "), bukan pesan
// asli dari backend ("Email/No HP atau password salah", "Nominal pengajuan melebihi sisa
// plafond", dst). Fungsi ini baca ulang errorBody() manual buat ambil message asli backend;
// kalau gagal di-parse (bukan JSON ApiResponse, atau network putus total), baru fallback ke
// pesan generik per kategori.
fun Throwable.toFriendlyMessage(json: Json, fallback: String = "Terjadi kesalahan, coba lagi"): String {
    return when (this) {
        is HttpException -> {
            val backendMessage = response()?.errorBody()?.string()
                ?.let { body -> runCatching { json.decodeFromString(ApiErrorBody.serializer(), body) }.getOrNull() }
                ?.message
                ?.takeIf { it.isNotBlank() }

            backendMessage ?: when (code()) {
                401 -> "Email/No HP atau password salah"
                403 -> "Kamu gak punya akses buat aksi ini"
                404 -> "Data yang dicari gak ketemu"
                in 500..599 -> "Server lagi bermasalah, coba lagi nanti"
                else -> fallback
            }
        }
        is IOException -> "Gagal terhubung ke server. Cek koneksi internetmu, lalu coba lagi."
        else -> message?.takeIf { it.isNotBlank() } ?: fallback
    }
}
