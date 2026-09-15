package com.example.sakuku.data.remote

import com.example.sakuku.data.local.TokenDataStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

// Nempelin "Authorization: Bearer <token>" otomatis ke SEMUA request kalau token-nya ada.
// Endpoint publik (login/register/plafond/bunga-tenor) gak butuh header ini - backend gak
// peduli kalau header-nya nempel juga (Spring Security cuma cek pas endpoint-nya emang
// butuh auth), jadi aman dipasang global tanpa perlu list per-endpoint.
class AuthInterceptor @Inject constructor(
    private val tokenDataStore: TokenDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenDataStore.getTokenOnce() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
