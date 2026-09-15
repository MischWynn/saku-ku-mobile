package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class LoginResponseData(
    val token: String,
    val type: String
)
