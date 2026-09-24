package com.example.sakuku.data.repository

import com.example.sakuku.data.remote.WilayahApiService
import com.example.sakuku.data.remote.dto.WilayahItem
import javax.inject.Inject

class WilayahRepository @Inject constructor(
    private val wilayahApiService: WilayahApiService
) {
    suspend fun getProvinces(): Result<List<WilayahItem>> = try {
        Result.success(wilayahApiService.getProvinces().data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getRegencies(provinceId: String): Result<List<WilayahItem>> = try {
        Result.success(wilayahApiService.getRegencies(provinceId).data)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDistricts(regencyId: String): Result<List<WilayahItem>> = try {
        Result.success(wilayahApiService.getDistricts(regencyId).data)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
