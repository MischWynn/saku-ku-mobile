package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.CustomerChangePasswordRequest
import com.example.sakuku.data.remote.dto.CustomerDeleteAccountRequest
import com.example.sakuku.data.remote.dto.CustomerMeResponse
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import com.example.sakuku.data.remote.dto.FcmTokenRequest
import com.example.sakuku.data.remote.toFriendlyMessage
import kotlinx.serialization.json.Json
import javax.inject.Inject

// Dipakai bareng oleh Pengajuan (butuh sisaPlafond) dan Profil (lihat/edit profil) - satu
// sumber buat semua yang berhubungan sama "customer/me", bukan diduplikasi tiap fitur.
class CustomerRepository @Inject constructor(
    private val apiService: ApiService,
    private val json: Json
) {
    suspend fun getMe(): Result<CustomerMeResponse> = try {
        val response = apiService.getCustomerMe()
        val data = response.data
        if (data != null) Result.success(data) else Result.failure(Exception(response.message))
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat profil, coba lagi"), e))
    }

    suspend fun updateMe(request: CustomerUpdateRequest): Result<CustomerMeResponse> = try {
        val response = apiService.updateCustomerMe(request)
        val data = response.data
        if (data != null) Result.success(data) else Result.failure(Exception(response.message))
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal menyimpan profil, coba lagi"), e))
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> = try {
        val response = apiService.changeCustomerPassword(CustomerChangePasswordRequest(oldPassword, newPassword))
        Result.success(response.message)
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal mengganti password, coba lagi"), e))
    }

    // Best-effort - dipanggil dari PushMessagingService (bisa gagal kalau belum login, gak
    // ada AuthInterceptor Bearer yang nempel) dan abis login sukses (lihat AuthRepository/
    // LoginViewModel/RegisterViewModel). Gagal di sini gak boleh nge-block flow manapun.
    suspend fun updateFcmToken(token: String): Result<Unit> = try {
        apiService.updateFcmToken(FcmTokenRequest(token))
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(password: String): Result<String> = try {
        val response = apiService.deleteCustomerAccount(CustomerDeleteAccountRequest(password))
        Result.success(response.message)
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal menghapus akun, coba lagi"), e))
    }
}
