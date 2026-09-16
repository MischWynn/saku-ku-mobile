package com.example.sakuku.ui.screens

import androidx.compose.foundation.layout.Box
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    onNavigateToLogin: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    // Membaca rute aktif saat ini untuk menentukan tombol navbar mana yang menyala
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val hazeState = remember {HazeState()}

    Scaffold(
        bottomBar = {
            AnimatedBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // Guest-access model: cuma "home" yang boleh diakses tanpa login, tab lain
                    // (Riwayat/Ajukan/Notifikasi/Profil) di-soft-gate ke Login - lihat artifact
                    // "Nasabah Screen Guide".
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
        // "home" sengaja TIDAK dikasih bottom padding dari Scaffold di sini - background blob-nya
        // (lihat HomeScreen + sakukuBlobBackground) perlu beneran nyampe tepi layar biar ada warna
        // buat di-blend efek glass AnimatedBottomNavBar, bukan cuma nyentuh background flat Scaffold.
        // Area interaktif Beranda sendiri tetap aman lewat bottom padding besar di Column-nya.
        // Layar lain di bawah tetap dapet innerPadding penuh seperti semula, perilakunya gak diubah.
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.fillMaxSize().hazeSource(state = hazeState)
        ) {
            composable("home") {
                Box(Modifier.padding(top = innerPadding.calculateTopPadding())) {
                    HomeScreen(
                        onNavigateToLogin = onNavigateToLogin,
                        onNavigateToPlafond = { navController.navigate("plafond") }
                    )
                }
            }
            composable("plafond") {
                Box(Modifier.padding(innerPadding)) {
                    PlafondScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToRegister = onNavigateToRegister
                    )
                }
            }
            composable("history") {
                Box(Modifier.padding(innerPadding)) {
                    RiwayatScreen(
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
                Box(Modifier.padding(innerPadding)) {
                    StatusPinjamanDetailScreen(
                        id = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable("apply") {
                Box(Modifier.padding(innerPadding)) {
                    PengajuanScreen(
                        onBack = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onSubmitSuccess = {
                            navController.navigate("home") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
            composable("bayar") {
                // Belum ada entry point tombol ke sini dari Home (HomeScreen.kt sengaja gak
                // disentuh - lagi dikerjain user sendiri buat versi logged-in). Route ini
                // tinggal dipanggil navController.navigate("bayar") begitu tombol "Bayar
                // Cicilan"-nya siap ditaro di Home.
                Box(Modifier.padding(innerPadding)) {
                    BayarScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("notification") {
                Box(Modifier.padding(innerPadding)) { NotifikasiScreen() }
            }
            composable("profile") {
                Box(Modifier.padding(innerPadding)) {
                    ProfilScreen(
                        onNavigateToKtpDataDiri = { navController.navigate("ktp_data_diri") },
                        onNavigateToEditDataDiri = { navController.navigate("edit_data_diri") },
                        onNavigateToKeamanan = { navController.navigate("keamanan_akun") },
                        onNavigateToRekeningBank = { navController.navigate("rekening_bank") },
                        onNavigateToBantuan = { navController.navigate("bantuan") },
                        onLoggedOut = onLoggedOut
                    )
                }
            }
            composable("ktp_data_diri") {
                Box(Modifier.padding(innerPadding)) {
                    KtpDataDiriScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("edit_data_diri") {
                Box(Modifier.padding(innerPadding)) {
                    EditDataDiriScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("keamanan_akun") {
                Box(Modifier.padding(innerPadding)) {
                    KeamananAkunScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("rekening_bank") {
                Box(Modifier.padding(innerPadding)) {
                    RekeningBankScreen(onBack = { navController.popBackStack() })
                }
            }
            composable("bantuan") {
                Box(Modifier.padding(innerPadding)) {
                    BantuanScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}