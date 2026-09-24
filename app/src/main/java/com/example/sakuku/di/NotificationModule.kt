package com.example.sakuku.di

import android.content.Context
import com.example.sakuku.MainActivity
import com.example.sakuku.core.notification.AndroidAppNotifier
import com.example.sakuku.core.notification.AppNotifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Binding yang belum pernah ada - AndroidAppNotifier butuh Context + Activity target buat
// deep-link notifikasi (dibuka lewat TaskStackBuilder pas notifikasi di-tap), MainActivity
// dipakai karena itu satu-satunya entry point Activity di app ini (single-activity + Compose
// Navigation). Tanpa module ini, PushMessagingService.notifier (@Inject) gagal di-resolve Hilt
// ("[Dagger/MissingBinding] AppNotifier cannot be provided").
@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideAppNotifier(@ApplicationContext context: Context): AppNotifier =
        AndroidAppNotifier(context, MainActivity::class.java)
}
