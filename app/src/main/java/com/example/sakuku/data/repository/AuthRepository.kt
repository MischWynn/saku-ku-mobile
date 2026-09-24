package com.example.sakuku.data.repository

import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.GoogleSignInClient
import com.example.sakuku.data.remote.dto.ForgotPasswordRequest
import com.example.sakuku.data.remote.dto.GoogleSignInRequest
import com.example.sakuku.data.remote.dto.LoginRequest
import com.example.sakuku.data.remote.dto.RegisterRequest
import com.example.sakuku.data.remote.dto.ResendOtpRequest
import com.example.sakuku.data.remote.dto.ResetPasswordRequest
import com.example.sakuku.data.remote.dto.VerifyOtpRequest
import com.example.sakuku.data.remote.toFriendlyMessage
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Dua hasil yang mungkin dari googleSignIn() - dibedain di sini (bukan cuma Result<String>)
// karena "sukses" beneran ada 2 arti berbeda buat caller: udah py akun (dikasih token, lanjut
// kayak login biasa) vs belum py akun (Android arahkan ke Register step 1, email dikunci).
sealed interface GoogleSignInOutcome {
    data class LoggedIn(val token: String, val suggestedName: String?) : GoogleSignInOutcome
    data class NeedsRegistration(val email: String, val suggestedName: String?) : GoogleSignInOutcome
}

class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore,
    private val googleSignInClient: GoogleSignInClient,
    private val json: Json
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
            Result.failure(Exception(e.toFriendlyMessage(json, "Login gagal, coba lagi"), e))
        }
    }

    // activityContext dipakai SEKALI di sini buat munculin account picker Credential Manager -
    // gak disimpen di mana pun (bukan constructor property), jadi gak ada resiko leak ke luar
    // masa hidup pemanggilan ini. webClientId datang dari caller (strings.xml, lihat catatan di
    // sana) biar repository ini gak nge-hardcode resource Android.
    suspend fun googleSignIn(activityContext: Context, webClientId: String): Result<GoogleSignInOutcome> {
        val firebaseIdToken = googleSignInClient.signIn(activityContext, webClientId).getOrElse {
            return Result.failure(it)
        }

        return try {
            val response = apiService.googleSignIn(GoogleSignInRequest(firebaseIdToken))
            val data = response.data ?: return Result.failure(Exception("Respons server tidak lengkap"))
            if (data.isLoginSuccess) {
                val token = data.token
                    ?: return Result.failure(Exception("Token tidak diterima dari server"))
                Result.success(GoogleSignInOutcome.LoggedIn(token, data.namaLengkap))
            } else {
                Result.success(GoogleSignInOutcome.NeedsRegistration(data.email, data.namaLengkap))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Masuk dengan Google gagal, coba lagi"), e))
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
            Result.failure(Exception(e.toFriendlyMessage(json, "Registrasi gagal, coba lagi"), e))
        }
    }

    // Server-side logout ditambahin 19 Sept - sebelumnya cuma clearToken() lokal, token JWT-nya
    // sendiri MASIH VALID di backend sampai 15 menit alami habis (lihat TokenBlacklistService,
    // backend). Best-effort: kalau call server-nya gagal (network putus, dst), logout LOKAL
    // tetap harus jalan - user gak boleh ke-jebak gak bisa logout gara-gara gak ada internet.
    suspend fun logout() {
        try {
            apiService.logout()
        } catch (e: Exception) {
            // Sengaja diabaikan - lihat komentar di atas.
        }
        tokenDataStore.clearToken()
    }

    suspend fun verifyOtp(email: String, code: String): Result<String> {
        return try {
            val response = apiService.verifyOtp(VerifyOtpRequest(email, code))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Verifikasi OTP gagal, coba lagi"), e))
        }
    }

    suspend fun resendOtp(email: String): Result<String> {
        return try {
            val response = apiService.resendOtp(ResendOtpRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Gagal mengirim ulang OTP, coba lagi"), e))
        }
    }

    // Dipakai juga buat "kirim ulang" di alur reset password - backend gak punya endpoint
    // resend terpisah buat ini, forgot-password sendiri yang generate+kirim kode baru tiap dipanggil.
    suspend fun verifyResetOtp(email: String, code: String): Result<String> {
        return try {
            val response = apiService.verifyResetOtp(VerifyOtpRequest(email, code))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Verifikasi kode gagal, coba lagi"), e))
        }
    }

    suspend fun forgotPassword(email: String): Result<String> {
        return try {
            val response = apiService.forgotPassword(ForgotPasswordRequest(email))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Gagal mengirim kode, coba lagi"), e))
        }
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String): Result<String> {
        return try {
            val response = apiService.resetPassword(ResetPasswordRequest(email, code, newPassword))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(Exception(e.toFriendlyMessage(json, "Gagal mengganti password, coba lagi"), e))
        }
    }
}
