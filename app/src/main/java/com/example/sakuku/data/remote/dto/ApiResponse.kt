package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val statusCode: Int,
    val message: String,
    val data: T? = null
)
