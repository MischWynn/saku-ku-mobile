package com.example.sakuku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.sakuku.data.local.entity.PengajuanCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PengajuanDao {

    // Flow - UI observe ini langsung, bukan one-shot fetch. Emit ulang otomatis tiap kali
    // refreshFromNetwork() nulis data baru, dan tetap ngasih data lama pas offline (gak pernah
    // emit list kosong cuma gara-gara network gagal).
    @Query("SELECT * FROM pengajuan_cache ORDER BY tanggalPengajuan DESC")
    fun observeAll(): Flow<List<PengajuanCacheEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PengajuanCacheEntity>)

    @Query("DELETE FROM pengajuan_cache")
    suspend fun clearAll()

    // Replace-all dalam 1 transaction - refresh penuh dari network (bukan sinkronisasi
    // incremental), jadi pengajuan yang somehow udah gak ada di response terbaru ikut kehapus
    // dari cache juga, bukan nyangkut selamanya.
    @Transaction
    suspend fun replaceAll(items: List<PengajuanCacheEntity>) {
        clearAll()
        upsertAll(items)
    }
}
