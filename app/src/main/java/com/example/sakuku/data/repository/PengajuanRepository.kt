package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.PengajuanHistoryResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.remote.dto.PengajuanRequest
import javax.inject.Inject

class PengajuanRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMyPengajuan(): Result<List<PengajuanMeResponse>> = try {
        val response = apiService.getMyPengajuan()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getHistory(id: String): Result<List<PengajuanHistoryResponse>> = try {
        val response = apiService.getPengajuanHistory(id)
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createPengajuan(request: PengajuanRequest): Result<String> = try {
        val response = apiService.createPengajuan(request)
        if (response.statusCode in 200..299) {
            Result.success(response.message)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
