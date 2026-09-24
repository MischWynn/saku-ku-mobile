package com.example.sakuku.data.remote

import com.example.sakuku.data.local.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// BUG NYATA yang ditemuin 15 Sept: asumsi lama di komentar ini ("backend gak peduli kalau
// header-nya nempel juga") TERNYATA SALAH - dicek langsung pakai curl, replay persis request
// yang sama: TANPA header Authorization -> login sukses 200; DENGAN header Authorization berisi
// token lama/expired -> backend balikin 401 "Token tidak valid" SEBELUM sempat ngecek
// email/password sama sekali, walau /customer/login itu permitAll(). Token JWT cuma umur 15
// menit (lihat claim exp di JwtService) - jadi siapa aja yang nyoba login/register/dst LEBIH
// dari 15 menit setelah token sebelumnya kadaluwarsa bakal SELALU 401 di sini, bukan gara-gara
// salah password.
//
// Fix: skip nempelin header buat endpoint publik (login/register/forgot-otp-reset/plafond/
// bunga-tenor) - endpoint-endpoint ini emang gak butuh token, dan backend-nya justru nolak
// kalau ada token basi yang ikut nempel.
private val PUBLIC_ENDPOINT_SUFFIXES = listOf(
    "customer/login",
    "customer/register",
    "customer/verify-otp",
    "customer/resend-otp",
    "customer/forgot-password",
    "customer/verify-reset-otp",
    "customer/reset-password",
    "/plafond",
    "/bunga-tenor"
)

class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        if (PUBLIC_ENDPOINT_SUFFIXES.any { path.endsWith(it) }) {
            return chain.proceed(original)
        }

        val token = runBlocking { tokenDataStore.getTokenOnce() }
        val request = if (token != null) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        val response = chain.proceed(request)

        // Token yang nempel ternyata ditolak backend (expired/basi) - bersihin dari
        // DataStore. Tanpa ini, MainViewModel.isLoggedIn (cuma ngecek token != null, gak
        // ngecek validitas/umur) bakal tetep true selamanya walau token-nya udah gak berguna,
        // bikin user "keliatan login" tapi tiap request ke endpoint customer/* selalu 401.
        // notifySessionExpired() sekalian ngasih tau AppNavigation buat maksa pindah ke Login -
        // tanpa ini, user cuma nyangkut di halaman yang lagi dibuka (token-nya ilang diam-diam
        // di background, gak ada yang nge-redirect dia).
        if (response.code == 401 && token != null) {
            runBlocking { tokenDataStore.clearToken() }
            tokenDataStore.notifySessionExpired()
        }

        return response
    }
}
