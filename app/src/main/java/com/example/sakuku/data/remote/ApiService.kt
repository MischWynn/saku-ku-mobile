package com.example.sakuku.data.remote

import com.example.sakuku.data.remote.dto.ApiResponse
import com.example.sakuku.data.remote.dto.BungaTenorResponse
import com.example.sakuku.data.remote.dto.CustomerChangePasswordRequest
import com.example.sakuku.data.remote.dto.CustomerDeleteAccountRequest
import com.example.sakuku.data.remote.dto.CustomerMeResponse
import com.example.sakuku.data.remote.dto.FcmTokenRequest
import com.example.sakuku.data.remote.dto.CustomerUpdateRequest
import com.example.sakuku.data.remote.dto.ForgotPasswordRequest
import com.example.sakuku.data.remote.dto.GoogleSignInRequest
import com.example.sakuku.data.remote.dto.GoogleSignInResponseData
import com.example.sakuku.data.remote.dto.LoginRequest
import com.example.sakuku.data.remote.dto.LoginResponseData
import com.example.sakuku.data.remote.dto.NotificationResponse
import com.example.sakuku.data.remote.dto.PengajuanApiResponse
import com.example.sakuku.data.remote.dto.PengajuanHistoryResponse
import com.example.sakuku.data.remote.dto.PengajuanMeResponse
import com.example.sakuku.data.remote.dto.PengajuanRequest
import com.example.sakuku.data.remote.dto.PlafondResponse
import com.example.sakuku.data.remote.dto.RegisterApiResponse
import com.example.sakuku.data.remote.dto.RegisterRequest
import com.example.sakuku.data.remote.dto.ResendOtpRequest
import com.example.sakuku.data.remote.dto.ResetPasswordRequest
import com.example.sakuku.data.remote.dto.VerifyOtpRequest
import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("customer/login")
    suspend fun login(@Body request: LoginRequest): LoginResponseData

    @POST("customer/register")
    suspend fun register(@Body request: RegisterRequest): RegisterApiResponse

    @POST("customer/google-signin")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): ApiResponse<GoogleSignInResponseData>

    @POST("customer/verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiResponse<JsonElement>

    @POST("customer/resend-otp")
    suspend fun resendOtp(@Body request: ResendOtpRequest): ApiResponse<JsonElement>

    @POST("customer/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): ApiResponse<JsonElement>

    @POST("customer/verify-reset-otp")
    suspend fun verifyResetOtp(@Body request: VerifyOtpRequest): ApiResponse<JsonElement>

    @POST("customer/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse<JsonElement>

    @GET("plafond")
    suspend fun getPlafondTiers(): ApiResponse<List<PlafondResponse>>

    @GET("bunga-tenor")
    suspend fun getBungaTenor(): ApiResponse<List<BungaTenorResponse>>

    @GET("customer/me")
    suspend fun getCustomerMe(): ApiResponse<CustomerMeResponse>

    @PATCH("customer/me")
    suspend fun updateCustomerMe(@Body request: CustomerUpdateRequest): ApiResponse<CustomerMeResponse>

    @PATCH("customer/change-password")
    suspend fun changeCustomerPassword(@Body request: CustomerChangePasswordRequest): ApiResponse<JsonElement>

    @PATCH("customer/fcm-token")
    suspend fun updateFcmToken(@Body request: FcmTokenRequest): ApiResponse<JsonElement>

    @POST("customer/logout")
    suspend fun logout(): ApiResponse<JsonElement>

    @HTTP(method = "DELETE", path = "customer/me", hasBody = true)
    suspend fun deleteCustomerAccount(@Body request: CustomerDeleteAccountRequest): ApiResponse<JsonElement>

    @POST("pengajuan")
    suspend fun createPengajuan(@Body request: PengajuanRequest): PengajuanApiResponse

    @GET("pengajuan/me")
    suspend fun getMyPengajuan(): ApiResponse<List<PengajuanMeResponse>>

    @GET("pengajuan/{id}/history/me")
    suspend fun getPengajuanHistory(@Path("id") id: String): ApiResponse<List<PengajuanHistoryResponse>>

    @GET("notifikasi")
    suspend fun getNotifications(): ApiResponse<List<NotificationResponse>>

    @PATCH("notifikasi/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): ApiResponse<NotificationResponse>
}
