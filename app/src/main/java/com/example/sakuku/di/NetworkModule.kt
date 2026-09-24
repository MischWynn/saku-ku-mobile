package com.example.sakuku.di

import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.AuthInterceptor
import com.example.sakuku.data.remote.WilayahApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WilayahRetrofit

private const val WILAYAH_BASE_URL = "https://nusantara.clowdlab.com/api/v1/regions/"
// Points at the deployed backend (Nginx Proxy Manager -> GCP VM, self-hosted Postgres, real
// Let's Encrypt cert on a real subdomain) - same URL the Angular frontend's environment.ts uses.
// 22 Sept 2026: swapped off the old ephemeral Cloudflare quick-tunnel URL (which broke whenever
// the VM's cloudflared container restarted) onto this stable subdomain instead.
private const val BASE_URL = "https://mysaku.morpkhai.web.id/api/v1/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    @WilayahRetrofit
    fun provideWilayahOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    @WilayahRetrofit
    fun provideWilayahRetrofit(@WilayahRetrofit okHttpClient: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(WILAYAH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideWilayahApiService(@WilayahRetrofit retrofit: Retrofit): WilayahApiService =
        retrofit.create(WilayahApiService::class.java)
}
