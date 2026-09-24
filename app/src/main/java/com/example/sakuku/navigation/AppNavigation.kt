package com.example.sakuku.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sakuku.ui.screens.MainScreen
import com.example.sakuku.ui.screens.SessionViewModel
import com.example.sakuku.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.sakuku.ui.screens.login.LoginScreen
import com.example.sakuku.ui.screens.newpassword.NewPasswordScreen
import com.example.sakuku.ui.screens.onboarding.OnboardingScreen
import com.example.sakuku.ui.screens.otp.OtpScreen
import com.example.sakuku.ui.screens.register.RegisterScreen
import com.example.sakuku.ui.screens.security.RootedDeviceScreen
import com.example.sakuku.ui.screens.splash.SplashScreen
import com.example.sakuku.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionViewModel: SessionViewModel = hiltViewModel()

    // Sesi habis (401 dari mana pun, lihat AuthInterceptor) -> paksa balik ke Login, ke mana
    // pun user lagi berada, beda dari logout manual (onLoggedOut di bawah, yang baliknya ke
    // "main" sebagai tamu) - sesi habis harus ke Login supaya user ngerti perlu login ulang,
    // bukan didiemin di Beranda tamu seolah gak ada apa-apa.
    LaunchedEffect(Unit) {
        sessionViewModel.sessionExpiredEvents.collect {
            Toast.makeText(context, "Sesi Anda telah berakhir, silakan masuk kembali", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigate = { loggedIn ->
                // Udah login (token masih ada di DataStore, gak dicek expiry-nya di sini) ->
                // langsung ke Home logged-in, gak perlu ngelewatin Onboarding lagi tiap buka
                // app. Belum login -> alur lama (Onboarding dulu). Restriction "belum login
                // cuma bisa akses Home" tetap dipegang MainScreen.kt punya bottom-nav guard,
                // gak berubah - splash cuma nentuin titik masuk awal.
                val destination = if (loggedIn) "main" else "onboarding"
                navController.navigate(destination) {
                    popUpTo("splash") { inclusive = true }
                }
            }, onRootedDetected = {
                // Terminal state - popUpTo splash inclusive means "rooted_device" is left as the
                // ONLY back-stack entry, so there's no route back into the app via back button.
                navController.navigate("rooted_device") {
                    popUpTo("splash") { inclusive = true }
                }
            })
        }

        composable("rooted_device") {
            RootedDeviceScreen()
        }

        composable("onboarding") {
            OnboardingScreen(
                // "Lewati" -> langsung ke Home sebagai tamu (guest-access model), BUKAN ke Login.
                // Home tetap bisa diakses tanpa akun; fitur lain di-soft-gate ke Login pas ditap.
                onSkip = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                // Login (beda dari register) langsung dapet token, jadi abis Welcome lanjut ke
                // "main" beneran logged-in - bukan ke "login" lagi kayak alur register. Kalau
                // nama gagal ke-fetch (userName null/kosong), skip Welcome, langsung ke Home -
                // gak nge-block user cuma gara-gara 1 fetch profil kedua gagal.
                onLoginSuccess = { userName ->
                    if (!userName.isNullOrBlank()) {
                        navController.navigate("welcome/$userName/main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                // Google verifikasi emailnya, tapi belum ada akun Saku-Ku buat email itu -
                // lanjut ke Register step 1 dengan email di-prefill+dikunci (lihat
                // RegisterViewModel.prefillFromGoogle). URL-encode karena email/nama lewat
                // sebagai bagian path nav argument, bisa mengandung karakter spesial ('@', spasi).
                onNeedsGoogleRegistration = { email, suggestedName ->
                    val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
                    val route = if (!suggestedName.isNullOrBlank()) {
                        val encodedName = java.net.URLEncoder.encode(suggestedName, "UTF-8")
                        "register?googleEmail=$encodedEmail&googleName=$encodedName"
                    } else {
                        "register?googleEmail=$encodedEmail"
                    }
                    navController.navigate(route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "register?googleEmail={googleEmail}&googleName={googleName}",
            arguments = listOf(
                navArgument("googleEmail") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("googleName") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            RegisterScreen(
                // Register sekarang 4 step (Akun->OTP->Data Pekerjaan->Foto KTP) SEMUANYA di
                // dalam RegisterScreen/RegisterViewModel sendiri - OTP gak lagi navigate ke
                // layar terpisah kayak sebelumnya (biar RegisterViewModel bisa auto-login pakai
                // password yang masih di state-nya begitu OTP kevalidasi, tanpa perlu nge-pass
                // password lewat nav argument). onRegisterSuccess baru kepanggil abis Step 3
                // (Foto KTP) selesai/di-skip - titik itu user UDAH beneran logged-in (auto-login
                // kejadian abis OTP), jadi abis Welcome lanjut ke "main", bukan "login" lagi.
                onRegisterSuccess = { userName ->
                    navController.navigate("welcome/$userName/main") {
                        // popUpTo matches by the route PATTERN as registered below (with the
                        // optional-args suffix), not by the plain "register" string used to
                        // navigate INTO this screen - a literal "register" here would silently
                        // fail to match and leave this screen sitting in the back stack.
                        popUpTo("register?googleEmail={googleEmail}&googleName={googleName}") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") },
                onBack = { navController.popBackStack() },
                googleEmail = backStackEntry.arguments?.getString("googleEmail"),
                googleName = backStackEntry.arguments?.getString("googleName")
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onOtpSent = { email ->
                    navController.navigate("otp/$email")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Sebelumnya dipakai bareng registrasi juga (via OtpMode) - Register sekarang punya
        // step OTP-nya sendiri (lihat composable("register") di atas), jadi layar ini sekarang
        // murni buat alur Lupa Password.
        composable(
            route = "otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            OtpScreen(
                email = email,
                onVerified = { code -> navController.navigate("new_password/$email/$code") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "new_password/{email}/{code}",
            arguments = listOf(
                navArgument("email") { type = NavType.StringType },
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val code = backStackEntry.arguments?.getString("code") ?: ""

            NewPasswordScreen(
                email = email,
                code = code,
                onSuccess = {
                    // Pola sama kayak notif "sesi habis" di atas - Toast, lalu balik ke Login.
                    Toast.makeText(context, "Password berhasil diubah, silakan masuk dengan password baru", Toast.LENGTH_LONG).show()
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() },
                // Kode salah/kedaluwarsa - balik ke layar Verifikasi (masih di back stack),
                // bukan ngulang dari Lupa Password lagi.
                onNeedNewOtp = { navController.popBackStack() }
            )
        }

        composable(
            // nextRoute: "login" (abis register - belum punya token) atau "main" (abis login -
            // udah beneran logged-in). Welcome sekarang dipakai 2 tempat, tujuan abisnya beda,
            // jadi diparametrize di path-nya sendiri daripada di-hardcode di sini.
            route = "welcome/{userName}/{nextRoute}",
            arguments = listOf(
                navArgument("userName") { type = NavType.StringType },
                navArgument("nextRoute") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            val nextRoute = backStackEntry.arguments?.getString("nextRoute") ?: "login"
            WelcomeScreen(
                userName = userName,
                onContinue = {
                    navController.navigate(nextRoute) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("main") {
            MainScreen(
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToRegister = { navController.navigate("register") },
                // Logout balik ke "main" (Beranda sebagai tamu), BUKAN ke "login" - app ini
                // guest-access, jadi abis logout harusnya bisa lanjut browsing tanpa akun, bukan
                // ke-dead-end di Login gak bisa ke mana-mana (back stack ke-clear total gara-gara
                // popUpTo splash inclusive, jadi tombol back di Login gak ada tujuan lagi).
                onLoggedOut = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
    }
}
