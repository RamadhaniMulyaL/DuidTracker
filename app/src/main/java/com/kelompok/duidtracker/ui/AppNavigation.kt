package com.kelompok.duidtracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kelompok.duidtracker.ui.auth.LoginScreen
import com.kelompok.duidtracker.ui.auth.RegisterScreen
import com.kelompok.duidtracker.viewmodel.AuthViewModel

/**
 * Kelas Screen untuk mendefinisikan rute navigasi di aplikasi.
 * Analogi: Seperti daftar alamat tujuan di peta navigasi (GPS).
 */
sealed class Screen(val route: String) {
    object Login : Screen("login") // Alamat untuk layar masuk
    object Register : Screen("register") // Alamat untuk layar daftar
    object Main : Screen("main") // Alamat untuk layar utama (setelah masuk)
    object GroupDetail : Screen("group_detail/{groupId}") { // Alamat detail grup dengan parameter ID
        // Fungsi untuk membuat alamat spesifik dengan ID grup tertentu
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
}

/**
 * Komponen Navigasi Utama Aplikasi.
 * Analogi: Ini adalah "Pusat Kendali Lalu Lintas" yang mengatur perpindahan antar layar.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel, // Memberikan akses ke status login pengguna
    navController: NavHostController = rememberNavController() // Alat pengendali navigasi
) {
    // Menentukan layar pertama yang muncul: Jika sudah login masuk ke Main, jika belum ke Login.
    val startDestination = if (authViewModel.uiState.value.isLoggedIn) Screen.Main.route else Screen.Login.route

    // Wadah utama yang menampung semua layar aplikasi
    NavHost(
        navController = navController,
        startDestination = startDestination // Menetapkan pintu masuk utama
    ) {
        // Mendefinisikan rute layar Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Jika login sukses, pindah ke layar Main dan hapus riwayat layar Login dari tumpukan
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Pindah ke layar pendaftaran
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Mendefinisikan rute layar Register
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    // Jika daftar sukses, pindah ke layar Main dan bersihkan riwayat navigasi sebelumnya
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Kembali ke layar login (hapus layar register dari tumpukan)
                    navController.popBackStack()
                }
            )
        }

        // Mendefinisikan rute layar Utama (Main)
        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                onNavigateToGroupDetail = { groupId ->
                    // Navigasi ke detail grup dengan mengirimkan ID grup sebagai argumen
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onLogout = {
                    // Jalankan fungsi logout di ViewModel dan tendang user kembali ke layar Login
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // Mendefinisikan rute layar Detail Grup yang membutuhkan parameter 'groupId'
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Mengambil ID grup dari data navigasi yang dikirim
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            // Menampilkan layar detail grup
            GroupDetailScreen(groupId = groupId, onNavigateBack = { navController.popBackStack() })
        }
    }
}

/**
 * Layar Placeholder untuk Detail Grup.
 * Analogi: Ruangan khusus yang hanya bisa diakses dengan kunci (ID) tertentu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(groupId: String, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            // Bilah navigasi atas dengan tombol kembali
            TopAppBar(
                title = { Text("Detail Grup: $groupId") }, // Judul sesuai ID grup yang dibuka
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            // Ikon panah kembali yang otomatis menyesuaikan arah baca (LRT/RTL)
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Konten tengah layar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text("Layar Detail Grup untuk $groupId\nSegera Hadir")
        }
    }
}
