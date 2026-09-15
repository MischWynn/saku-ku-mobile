package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.PlafondResponse
import javax.inject.Inject

// Data publik yang dipakai Beranda (guest & logged-in) - dua-duanya permitAll() di backend.
class HomeRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getPlafondTiers(): Result<List<PlafondResponse>> = try {
        val response = apiService.getPlafondTiers()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getBungaTenor(): Result<List<BungaTenorResponse>> = try {
        val response = apiService.getBungaTenor()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }
}
