package com.example.sakuku.data.repository

import com.example.sakuku.data.local.dao.PengajuanDao
import com.example.sakuku.data.local.entity.toCacheEntity
import com.example.sakuku.data.local.entity.toResponse
import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.dto.PengajuanHistoryResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.remote.dto.PengajuanRequest
import com.example.sakuku.data.remote.toFriendlyMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject

class PengajuanRepository @Inject constructor(
    private val apiService: ApiService,
    private val json: Json,
    private val pengajuanDao: PengajuanDao
) {
    // Dipakai HomeViewModel (kartu Pengajuan Aktif/Tagihan) - one-shot, gak lewat cache Room,
    // sengaja dibiarin apa adanya biar gak nyenggol behavior Home yang udah jalan.
    suspend fun getMyPengajuan(): Result<List<PengajuanMeResponse>> = try {
        val response = apiService.getMyPengajuan()
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat riwayat pengajuan, coba lagi"), e))
    }

    // === Offline-first buat Riwayat Pengajuan (Room jadi single source of truth) ===
    // UI observe ini terus - begitu refreshPengajuanCache() nulis data baru, Flow ini emit ulang
    // sendiri. Kalau device offline dan cache udah pernah keisi sebelumnya, list lama tetap
    // kebaca normal - gak ada state "gagal load" cuma gara-gara gak ada internet.
    fun observeCachedPengajuan(): Flow<List<PengajuanMeResponse>> =
        pengajuanDao.observeAll().map { cached -> cached.map { it.toResponse() } }

    // Sinkronisasi dari network ke Room. Gagal (offline/network error) BUKAN error fatal buat
    // caller - cache lama (kalau ada) tetap valid buat ditampilin, makanya balikin Result<Unit>
    // yang cuma dipakai buat nampilin pesan "gagal sinkron" opsional, bukan buat nge-blank layar.
    suspend fun refreshPengajuanCache(): Result<Unit> = try {
        val response = apiService.getMyPengajuan()
        val items = (response.data ?: emptyList()).map { it.toCacheEntity() }
        pengajuanDao.replaceAll(items)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal sinkron riwayat pengajuan"), e))
    }

    suspend fun getHistory(id: String): Result<List<PengajuanHistoryResponse>> = try {
        val response = apiService.getPengajuanHistory(id)
        Result.success(response.data ?: emptyList())
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal memuat riwayat review, coba lagi"), e))
    }

    // toFriendlyMessage() di sini yang paling kepakai - backend nolak submit (mis. "Nominal
    // pengajuan melebihi sisa plafond") lewat status non-2xx (422/400), yang sebelumnya cuma
    // nongol sebagai "HTTP 422 " generic, gak keliatan alasan sebenarnya.
    suspend fun createPengajuan(request: PengajuanRequest): Result<String> = try {
        val response = apiService.createPengajuan(request)
        if (response.statusCode in 200..299) {
            Result.success(response.message)
        } else {
            Result.failure(Exception(response.message))
        }
    } catch (e: Exception) {
        Result.failure(Exception(e.toFriendlyMessage(json, "Gagal mengirim pengajuan, coba lagi"), e))
    }
}
