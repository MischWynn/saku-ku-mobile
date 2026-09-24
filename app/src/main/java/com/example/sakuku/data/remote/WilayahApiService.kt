package com.example.sakuku.data.remote

import com.example.sakuku.data.remote.dto.WilayahListResponse
import retrofit2.http.GET
import retrofit2.http.Path

// API publik terpisah dari backend Sakuku sendiri (base URL beda, lihat NetworkModule -
// @WilayahRetrofit) - gratis, no key, dipakai buat dropdown cascading Provinsi -> Kota ->
// Kecamatan di Register & Profil (KTP & Data Diri).
// Dipindah 18 Sept dari emsifa/api-wilayah-indonesia ke nusantara.clowdlab.com - emsifa
// keblokir dari network user, nusantara dicoba cold (curl, user-agent okhttp, tanpa cookie)
// dan balikin data asli, jadi aman dipakai dari Retrofit tanpa perlu browser/JS.
interface WilayahApiService {

    @GET("provinces")
    suspend fun getProvinces(): WilayahListResponse

    @GET("provinces/{provinceId}/regencies")
    suspend fun getRegencies(@Path("provinceId") provinceId: String): WilayahListResponse

    @GET("regencies/{regencyId}/districts")
    suspend fun getDistricts(@Path("regencyId") regencyId: String): WilayahListResponse
}
