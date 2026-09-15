package com.example.sakuku.data.remote.dto

data class AuthSession(
    val user: CustomerMeResponse? = null,
    val accessToken: String,
    val expiresAtMillis: Long = 100_000L
) {
    fun isExpiredAt(nowMillis: Long): Boolean = nowMillis >= expiresAtMillis
}
