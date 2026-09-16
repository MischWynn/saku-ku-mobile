package com.example.sakuku.data.repository

import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.ForgotPasswordRequest
import com.example.sakuku.data.remote.dto.LoginRequest
import com.example.sakuku.data.remote.dto.RegisterRequest
import com.example.sakuku.data.remote.dto.ResendOtpRequest
import com.example.sakuku.data.remote.dto.ResetPasswordRequest
import com.example.sakuku.data.remote.dto.VerifyOtpRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore
) {
    // Observable session - delegate langsung ke TokenDataStore.tokenFlow (DataStore Flow yang
    // udah ada), bukan bikin abstraksi AuthSession/local-data-source baru. null = belum login.
    val session: Flow<String?> = tokenDataStore.tokenFlow

    suspend fun login(identifier: String, password: String): Result<String> {
        return try {
            // Response login customer itu LoginResponseData mentah ({token,type}), bukan
            // ApiResponse<LoginResponseData> - lihat catatan di ApiService.kt. Jadi token
            // dibaca langsung, gak ada .data/.message buat dibongkar (401 dkk masuk lewat
            // exception di bawah, HttpException dari Retrofit).
            // Nyimpen token ke TokenDataStore tetap tanggung jawab caller (LoginViewModel) -
            // sama kayak sebelumnya, biar gak double-write dari 2 tempat berbeda.
            val response = apiService.login(LoginRequest(identifier, password))
            Result.success(response.token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<String> {
        return try {
            val response = apiService.register(request)
            if (response.statusCode in 200..299) {
                Result.success(response.message)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenDataStore.clearToken()
    }

    suspend fun verifyOtp(email: String, code: String): Result<String> {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, code))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendOtp(email: String): Result<String> {
        return try {
            val response = apiService.resendOtp(ResendOtpRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dipakai juga buat "kirim ulang" di alur reset password - backend gak punya endpoint
    // resend terpisah buat ini, forgot-password sendiri yang generate+kirim kode baru tiap dipanggil.
    suspend fun verifyResetOtp(email: String, code: String): Result<String> {
        return try {
            val response = apiService.verifyResetOtp(VerifyOtpRequest(email, code))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
