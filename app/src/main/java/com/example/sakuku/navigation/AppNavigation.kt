package com.example.sakuku.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.sakuku.ui.screens.MainScreen
import com.example.sakuku.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.sakuku.ui.screens.login.LoginScreen
import com.example.sakuku.ui.screens.newpassword.NewPasswordScreen
import com.example.sakuku.ui.screens.onboarding.OnboardingScreen
import com.example.sakuku.ui.screens.otp.OtpMode
import com.example.sakuku.ui.screens.otp.OtpScreen
import com.example.sakuku.ui.screens.register.RegisterScreen
import com.example.sakuku.ui.screens.splash.SplashScreen
import com.example.sakuku.ui.screens.welcome.WelcomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(onNavigate = {
                navController.navigate("onboarding") {
                    popUpTo("splash") { inclusive = true }
                }
            })
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
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("register") {
            RegisterScreen(
                // Registrasi sekarang gak langsung ke Welcome - akun kebuat sebagai
                // PENDING_VERIFICATION di backend, harus lewat Verifikasi OTP dulu.
                onRegisterSuccess = { email, userName ->
                    navController.navigate("otp/${OtpMode.REGISTRATION.name}/$email/$userName") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate("login") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onOtpSent = { email ->
                    navController.navigate("otp/${OtpMode.RESET_PASSWORD.name}/$email/-")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "otp/{mode}/{email}/{userName}",
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType },
                navArgument("email") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val mode = if (backStackEntry.arguments?.getString("mode") == OtpMode.RESET_PASSWORD.name) {
                OtpMode.RESET_PASSWORD
            } else {
                OtpMode.REGISTRATION
            }
            val email = backStackEntry.arguments?.getString("email") ?: ""
            val userName = backStackEntry.arguments?.getString("userName") ?: ""

            OtpScreen(
                email = email,
                mode = mode,
                onVerified = { code ->
                    when (mode) {
                        // Verifikasi registrasi udah beneran divalidasi di OtpViewModel
                        // (verify-otp) - begitu sukses, lanjut Welcome kayak alur lama.
                        OtpMode.REGISTRATION -> navController.navigate("welcome/$userName") {
                            popUpTo("register") { inclusive = true }
                        }
                        // Reset password: kode BELUM divalidasi di sini (gak ada endpoint
                        // verify-only buat alur ini) - dibawa ke Ganti Password, yang
                        // ngirim email+code+newPassword sekaligus ke reset-password.
                        OtpMode.RESET_PASSWORD -> navController.navigate("new_password/$email/$code")
                    }
                },
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
            route = "welcome/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: ""
            WelcomeScreen(
                userName = userName,
                onContinue = {
                    navController.navigate("main") {
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
