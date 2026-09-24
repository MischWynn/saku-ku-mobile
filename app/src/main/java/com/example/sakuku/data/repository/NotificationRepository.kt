package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.data.remote.toFriendlyMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val apiService: ApiService,
    private val json: Json
) {
    suspend fun getAll(): Result<List<NotificationResponse>> = try {
        val response = apiService.getNotifications()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat notifikasi, coba lagi"), e))
    }

    suspend fun markAsRead(id: String): Result<Unit> = try {
        apiService.markNotificationRead(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal menandai notifikasi, coba lagi"), e))
    }
}
