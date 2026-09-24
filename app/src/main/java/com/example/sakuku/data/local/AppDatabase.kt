package com.example.sakuku.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.sakuku.data.local.dao.PengajuanDao
import com.example.sakuku.data.local.entity.PengajuanCacheEntity

@Database(entities = [PengajuanCacheEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun pengajuanDao(): PengajuanDao
}
