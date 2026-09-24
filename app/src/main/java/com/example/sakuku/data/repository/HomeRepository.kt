package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.data.remote.toFriendlyMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Data publik yang dipakai Beranda (guest & logged-in) - dua-duanya permitAll() di backend.
class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val json: Json
) {
    suspend fun getPlafondTiers(): Result<List<PlafondResponse>> = try {
        val response = apiService.getPlafondTiers()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat data plafond, coba lagi"), e))
    }

    suspend fun getBungaTenor(): Result<List<BungaTenorResponse>> = try {
        val response = apiService.getBungaTenor()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat data suku bunga, coba lagi"), e))
    }
}
