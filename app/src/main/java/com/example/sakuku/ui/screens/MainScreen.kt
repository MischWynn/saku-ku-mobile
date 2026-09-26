package com.example.sakuku.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sakuku.ui.components.AnimatedBottomNavBar
import com.example.sakuku.ui.home.HomeScreen
import com.example.sakuku.ui.screens.bayar.BayarScreen
import com.example.sakuku.ui.screens.notifikasi.NotifikasiScreen
import com.example.sakuku.ui.screens.notifikasi.NotifikasiDetailScreen
import com.example.sakuku.ui.screens.pengajuan.PengajuanScreen
import com.example.sakuku.ui.screens.plafond.PlafondScreen
import com.example.sakuku.ui.screens.profil.BantuanScreen
import com.example.sakuku.ui.screens.profil.EditDataDiriScreen
import com.example.sakuku.ui.screens.profil.KeamananAkunScreen
import com.example.sakuku.ui.screens.profil.KtpDataDiriScreen
import com.example.sakuku.ui.screens.profil.ProfilScreen
import com.example.sakuku.ui.screens.profil.RekeningBankScreen
import com.example.sakuku.ui.screens.riwayat.RiwayatScreen
import com.example.sakuku.ui.screens.riwayat.detail.StatusPinjamanDetailScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.sakuku.ui.screens.profil.KontakScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import com.example.sakuku.ui.screens.profil.DataPekerjaanScreen
import com.example.sakuku.ui.screens.simulasi.SimulasiScreen

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val hazeState = remember {HazeState()}

    Scaffold(
        bottomBar = {
            AnimatedBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != "home" && !isLoggedIn) {
                        onNavigateToLogin()
                    } else {
                        navController.navigate(route) {
                            // Menghindari penumpukan halaman saat berpindah-pindah tab
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                hazeState = hazeState
            )
        }
    ) { innerPadding ->
        // Aturan inset buat SEMUA layar di sini:
        // - Atas: jarak status bar dipasang sekali di sini (screenArea) lalu di-consume, jadi
        //   layar yang juga pakai statusBarsPadding() sendiri gak dapet jarak dobel.
        // - Bawah: layar digambar sampai tepi bawah layar (di belakang navbar mengambang) biar
        //   background-nya gak kepotong. Konten yang harus kelihatan digeser naik sendiri oleh
        //   tiap layar - lewat parameter bottomInset, atau (sub-layar Profil & Bantuan) clearance
        //   120dp + navigationBarsPadding() yang udah ada di layarnya.
        val statusBarTop = innerPadding.calculateTopPadding()
        val bottomInset = innerPadding.calculateBottomPadding()
        val screenArea = Modifier
            .padding(top = statusBarTop)
            .consumeWindowInsets(PaddingValues(top = statusBarTop))

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)
        ) {
            composable("home") {
                Box(screenArea) {
                    HomeScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToRegister = onNavigateToRegister,
                        onNavigateToPlafond = { navController.navigate("plafond") },
                        onNavigateToSimulasi = { navController.navigate("simulasi") },
                        onNavigateToApply = {
                            navController.navigate("apply") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToRiwayat = {
                            navController.navigate("history") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onNavigateToRekening = { navController.navigate("rekening_bank") },
                        onNavigateToBayar = { navController.navigate("bayar") },
                        onNavigateToBantuan = { navController.navigate("bantuan") },
                        onNavigateToStatusDetail = { id -> navController.navigate("history_detail/$id") },
                        onNavigateToKtpDataDiri = { navController.navigate("ktp_data_diri") },
                        onNavigateToDataPekerjaan = { navController.navigate("data_pekerjaan") },
                        onNavigateToNotifikasi = { navController.navigate("notification") }
                    )
                }
            }
            composable("plafond") {
                Box(screenArea) {
                    PlafondScreen(
                        bottomInset = bottomInset,
                        onBack = { navController.popBackStack() },
                        onNavigateToRegister = onNavigateToRegister
                    )
                }
            }
            composable("history") {
                Box(screenArea) {
                    RiwayatScreen(
                        bottomInset = bottomInset,
                        onNavigateToApply = {
                            navController.navigate("apply") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onItemClick = { id -> navController.navigate("history_detail/$id") }
                    )
                }
            }
            composable(
                route = "history_detail/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                Box(screenArea) {
                    StatusPinjamanDetailScreen(
                        bottomInset = bottomInset,
                        id = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("apply") {
                Box(screenArea) {
                    PengajuanScreen(
                        bottomInset = bottomInset,
                        onBack = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSubmitSuccess = {
//                            navController.navigate("home") {
//                                popUpTo(navController.graph.startDestinationId) { saveState = true }
//                                launchSingleTop = true
//                                restoreState = true
                            navController.popBackStack()
//                            }
                        },
                        onNavigateToCompleteProfile = { navController.navigate("data_pekerjaan") },
                        onNavigateToKtpDataDiri = { navController.navigate("ktp_data_diri") },
                        onNavigateToRekening = { navController.navigate("rekening_bank") }
                    )
                }
            }
            composable("bayar") {
                // Belum ada entry point tombol ke sini dari Home (HomeScreen.kt sengaja gak
                // disentuh - lagi dikerjain user sendiri buat versi logged-in). Route ini
                // tinggal dipanggil navController.navigate("bayar") begitu tombol "Bayar
                // Cicilan"-nya siap ditaro di Home.
                Box(screenArea) {
                    BayarScreen(onBack = { navController.popBackStack() }, bottomInset = bottomInset)
                }
            }
            composable("notification") {
                Box(screenArea) {
                    NotifikasiScreen(
                        bottomInset = bottomInset,
                        onBack = { navController.popBackStack() },
                        onOpenDisbursement = { notifId -> navController.navigate("notification_detail/$notifId") },
                        onOpenStatus = { pengajuanId -> navController.navigate("history_detail/$pengajuanId") }
                    )
                }
            }
            composable("notification_detail/{id}") { backStackEntry ->
                val notifId = backStackEntry.arguments?.getString("id") ?: ""
                Box(screenArea) {
                    NotifikasiDetailScreen(
                        bottomInset = bottomInset,
                        notificationId = notifId,
                        onBack = { navController.popBackStack() },
                        onLihatTagihan = { navController.navigate("bayar") }
                    )
                }
            }
            composable("profile") {
                Box(screenArea) {
                    ProfilScreen(
                        onNavigateToKtpDataDiri = { navController.navigate("ktp_data_diri") },
                        onNavigateToEditDataDiri = { navController.navigate("edit_data_diri") },
                        onNavigateToKontak = {navController.navigate("kontak")},
                        onNavigateToDataPekerjaan = { navController.navigate("data_pekerjaan") },
                        onNavigateToKeamanan = { navController.navigate("keamanan_akun") },
                        onNavigateToRekeningBank = { navController.navigate("rekening_bank") },
                        onNavigateToBantuan = { navController.navigate("bantuan") },
                        onLoggedOut = onLoggedOut
                    )
                }
            }
            composable("ktp_data_diri") {
                Box(screenArea) {
                    KtpDataDiriScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("edit_data_diri") {
                Box(screenArea) {
                    EditDataDiriScreen(onBack = { navController.popBackStack() })
                }
            }

            composable("kontak") {
                Box(screenArea) {
                    KontakScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("data_pekerjaan") {
                Box(screenArea) {
                    DataPekerjaanScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("keamanan_akun") {
                Box(screenArea) {
                    KeamananAkunScreen(onBack = { navController.popBackStack() }, onLoggedOut = onLoggedOut)
                }
            }
            composable("rekening_bank") {
                Box(screenArea) {
                    RekeningBankScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("simulasi") {
                Box(screenArea) {
                    SimulasiScreen(
                        bottomInset = bottomInset,
                        onBack = { navController.popBackStack() },
                        onNavigateToLogin = onNavigateToLogin,
                    )
                }
            }
            composable("bantuan") {
                Box(screenArea) {
                    BantuanScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}