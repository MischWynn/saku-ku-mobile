package com.example.sakuku.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String
)

@Serializable
data class LoginResponseData(
    val token: String,
    val type: String
)

// idToken di sini WAJIB Firebase ID token (dari FirebaseAuth.signInWithCredential), bukan raw
// Google ID token dari Credential Manager - lihat GoogleSignInClient.kt untuk alasannya.
@Serializable
data class GoogleSignInRequest(
    val idToken: String
)

// Beda dari LoginResponseData: backend bungkus ini dalam ApiResponse<> karena ada 2 hasil
// mungkin (status), bukan cuma 1 bentuk kayak /customer/login.
@Serializable
data class GoogleSignInResponseData(
    val status: String,
    val token: String?,
    val type: String?,
    val email: String,
    val namaLengkap: String?
) {
    val isLoginSuccess: Boolean get() = status == "LOGIN_SUCCESS"
}
