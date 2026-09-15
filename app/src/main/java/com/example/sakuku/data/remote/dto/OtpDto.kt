package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

// Field persis 4 request DTO backend (VerifyOtpRequest/ResendOtpRequest/
// CustomerForgotPasswordRequest/CustomerResetPasswordRequest, cross-check 15 Sept) - semua
// camelCase polos, gak ada quirk penamaan kayak beberapa DTO lain di project ini.

@Serializable
data class VerifyOtpRequest(
    val email: String,
    val code: String
)

@Serializable
data class ResendOtpRequest(
    val email: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String
)

@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String
)
