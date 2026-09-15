package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.NotificationResponse
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAll(): Result<List<NotificationResponse>> = try {
        val response = apiService.getNotifications()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun markAsRead(id: String): Result<Unit> = try {
        apiService.markNotificationRead(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
