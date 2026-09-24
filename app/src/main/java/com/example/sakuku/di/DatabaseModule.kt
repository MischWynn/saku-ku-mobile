package com.example.sakuku.di

import android.content.Context
import androidx.room.Room
import com.example.sakuku.data.local.AppDatabase
import com.example.sakuku.data.local.dao.PengajuanDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "sakuku.db").build()

    @Provides
    @Singleton
    fun providePengajuanDao(database: AppDatabase): PengajuanDao = database.pengajuanDao()
}
