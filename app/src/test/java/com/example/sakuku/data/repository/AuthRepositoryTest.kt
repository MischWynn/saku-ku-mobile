package com.example.sakuku.data.repository

import com.example.sakuku.data.local.TokenDataStore
import com.example.sakuku.data.remote.ApiService
import com.example.sakuku.data.remote.GoogleSignInClient
import com.example.sakuku.data.remote.dto.LoginResponseData
import com.example.sakuku.data.remote.dto.RegisterApiResponse
import com.example.sakuku.data.remote.dto.RegisterRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith

private const val TOKEN = "header.payload.signature"
private const val EMAIL = "novita.sari@mail.com"
private const val PASSWORD = "Password123!"
private val TEST_JSON = Json { ignoreUnknownKeys = true }

@RunWith(Enclosed::class)
class AuthRepositoryTest {

    class Login {
        private val apiService = mockk<ApiService>()
        private val tokenDataStore = mockk<TokenDataStore> {
            every { tokenFlow } returns flowOf(null)
        }
        private val repository = AuthRepository(apiService, tokenDataStore, mockk<GoogleSignInClient>(), TEST_JSON)

        @Test
        fun `returns token on successful login`() = runTest {
            // Response login customer itu LoginResponseData mentah ({token,type}), bukan dibungkus ApiResponse<> - lihat catatan di ApiService.kt/AuthRepository.kt.
            coEvery { apiService.login(any()) } returns LoginResponseData(token = TOKEN, type = "CUSTOMER")

            val result = repository.login(EMAIL, PASSWORD)

            assertTrue(result.isSuccess)
            assertEquals(TOKEN, result.getOrNull())
        }

        @Test
        fun `does not persist token itself - that stays LoginViewModel's job`() = runTest {
            // AuthRepository.login() sengaja gak manggil tokenDataStore.saveToken() - biar cuma 1 tempat (LoginViewModel) yang nulis token, gak dobel-write dari 2 sisi berbeda.
            coEvery { apiService.login(any()) } returns LoginResponseData(token = TOKEN, type = "CUSTOMER")

            repository.login(EMAIL, PASSWORD)

            coVerify(exactly = 0) { tokenDataStore.saveToken(any()) }
        }

        @Test
        fun `returns failure when apiService throws`() = runTest {
            val error = RuntimeException("Email/No HP atau password salah")
            coEvery { apiService.login(any()) } throws error

            val result = repository.login(EMAIL, PASSWORD)

            assertTrue(result.isFailure)
            assertEquals("Email/No HP atau password salah", result.exceptionOrNull()?.message)
            assertEquals(error, result.exceptionOrNull()?.cause)
        }
    }

    class Register {
        private val apiService = mockk<ApiService>()
        private val tokenDataStore = mockk<TokenDataStore> {
            every { tokenFlow } returns flowOf(null)
        }
        private val repository = AuthRepository(apiService, tokenDataStore, mockk<GoogleSignInClient>(), TEST_JSON)
        private val request = RegisterRequest(
            namaLengkap = "Novita Sari",
            nik = "3201010101010001",
            email = EMAIL,
            noHp = "081234560001",
            password = PASSWORD
        )

        @Test
        fun `returns message when backend responds with success status code`() = runTest {
            coEvery { apiService.register(any()) } returns RegisterApiResponse(
                statusCode = 201,
                message = "Registrasi berhasil, cek email untuk kode OTP verifikasi"
            )

            val result = repository.register(request)

            assertTrue(result.isSuccess)
            assertEquals("Registrasi berhasil, cek email untuk kode OTP verifikasi", result.getOrNull())
        }

        @Test
        fun `returns failure when backend responds with non-2xx status code`() = runTest {
            coEvery { apiService.register(any()) } returns RegisterApiResponse(
                statusCode = 422,
                message = "Email sudah terdaftar"
            )

            val result = repository.register(request)

            assertTrue(result.isFailure)
            assertEquals("Email sudah terdaftar", result.exceptionOrNull()?.message)
        }

        @Test
        fun `returns failure when apiService throws`() = runTest {
            val error = RuntimeException("Network error")
            coEvery { apiService.register(any()) } throws error

            val result = repository.register(request)

            assertTrue(result.isFailure)
            assertEquals("Network error", result.exceptionOrNull()?.message)
            assertEquals(error, result.exceptionOrNull()?.cause)
        }
    }

    class Session {
        private val apiService = mockk<ApiService>()
        private val tokenDataStore = mockk<TokenDataStore>()

        @Test
        fun `emits token when a session is stored`() = runTest {
            every { tokenDataStore.tokenFlow } returns flowOf(TOKEN)
            val repository = AuthRepository(apiService, tokenDataStore, mockk<GoogleSignInClient>(), TEST_JSON)

            val result = repository.session.first()

            assertEquals(TOKEN, result)
        }

        @Test
        fun `emits null when logged out (no token stored)`() = runTest {
            every { tokenDataStore.tokenFlow } returns flowOf(null)
            val repository = AuthRepository(apiService, tokenDataStore, mockk<GoogleSignInClient>(), TEST_JSON)

            val result = repository.session.first()

            assertNull(result)
        }

        @Test
        fun `reflects every emission from TokenDataStore in order`() = runTest {
            every { tokenDataStore.tokenFlow } returns flowOf(TOKEN, null)
            val repository = AuthRepository(apiService, tokenDataStore, mockk<GoogleSignInClient>(), TEST_JSON)

            val emissions = repository.session.toList()

            assertEquals(listOf(TOKEN, null), emissions)
        }
    }
}
