package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.CustomerMeResponse
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import javax.inject.Inject

// Dipakai bareng oleh Pengajuan (butuh sisaPlafond) dan Profil (lihat/edit profil) - satu
// sumber buat semua yang berhubungan sama "customer/me", bukan diduplikasi tiap fitur.
class CustomerRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getMe(): Result<CustomerMeResponse> = try {
        val response = apiService.getCustomerMe()
        val data = response.data
        if (data != null) Result.success(data) else Result.failure(Exception(response.message))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateMe(request: CustomerUpdateRequest): Result<CustomerMeResponse> = try {
        val response = apiService.updateCustomerMe(request)
        val data = response.data
        if (data != null) Result.success(data) else Result.failure(Exception(response.message))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
