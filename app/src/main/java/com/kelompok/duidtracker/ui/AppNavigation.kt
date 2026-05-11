package com.kelompok.duidtracker.ui

import androidx.compose.foundation.layout.Box // Wadah untuk menumpuk elemen atau mengatur posisi tunggal
import androidx.compose.foundation.layout.fillMaxSize // Perintah agar komponen mengisi seluruh ukuran layar
import androidx.compose.foundation.layout.padding // Memberikan jarak aman di pinggir elemen
import androidx.compose.material.icons.Icons // Koleksi ikon standar Material
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Ikon panah kembali yang mendukung arah baca
import androidx.compose.material3.* // Komponen UI Material Design 3
import androidx.compose.runtime.Composable // Menandai fungsi sebagai komponen UI Compose
import androidx.compose.ui.Alignment // Mengatur perataan posisi elemen
import androidx.compose.ui.Modifier // Alat pemodifikasi tampilan
import androidx.navigation.NavHostController // Alat pengendali navigasi
import androidx.navigation.NavType // Definisi tipe data untuk argumen navigasi
import androidx.navigation.compose.NavHost // Wadah pusat navigasi
import androidx.navigation.compose.composable // Mendefinisikan rute layar di NavHost
import androidx.navigation.compose.rememberNavController // Mengingat posisi navigasi saat ini
import androidx.navigation.navArgument // Mendefinisikan parameter yang dikirim antar layar
import com.kelompok.duidtracker.ui.auth.LoginScreen // Layar masuk
import com.kelompok.duidtracker.ui.auth.RegisterScreen // Layar daftar
import com.kelompok.duidtracker.viewmodel.AuthViewModel // Otak login
import com.kelompok.duidtracker.viewmodel.GroupViewModel // Otak kelompok
import com.kelompok.duidtracker.viewmodel.TransactionViewModel // Otak keuangan

/**
 * Kelas Screen untuk mendefinisikan rute navigasi di aplikasi.
 * Analogi: Seperti daftar alamat tujuan di peta navigasi (GPS).
 */
sealed class Screen(val route: String) {
    object Login : Screen("login") // Alamat layar masuk
    object Register : Screen("register") // Alamat layar daftar
    object Main : Screen("main") // Alamat layar dashboard utama
    object GroupDetail : Screen("group_detail/{groupId}") { // Alamat detail grup dengan ID sebagai kunci
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
}

/**
 * Komponen Navigasi Utama Aplikasi.
 * Analogi: Ini adalah "Pusat Kendali Lalu Lintas" yang mengatur perpindahan antar layar.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel, // Memberikan akses status login
    transactionViewModel: TransactionViewModel, // Memberikan akses data keuangan
    groupViewModel: GroupViewModel, // Memberikan akses data kelompok
    navController: NavHostController = rememberNavController() // Supir navigasi
) {
    // Menentukan pintu masuk pertama: Jika sudah login masuk ke Main, jika belum ke Login.
    val startDestination = if (authViewModel.uiState.value.isLoggedIn) Screen.Main.route else Screen.Login.route

    // Wadah utama yang menampung semua layar aplikasi
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- JALUR LAYAR LOGIN ---
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    // Jika sukses masuk, pindah ke layar Main dan buang riwayat Login
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

        // --- JALUR LAYAR DAFTAR ---
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    // Jika daftar sukses, pindah ke layar Utama dan bersihkan memori navigasi
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Kembali ke layar login (pop tumpukan)
                    navController.popBackStack()
                }
            )
        }

        // --- JALUR LAYAR UTAMA (DASHBOARD) ---
        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                groupViewModel = groupViewModel,
                onNavigateToGroupDetail = { groupId ->
                    // Navigasi ke detail grup dengan membawa parameter ID
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onLogout = {
                    // Hapus sesi di otak aplikasi dan tendang balik ke Login
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // --- JALUR LAYAR DETAIL GRUP ---
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Mengambil kiriman ID dari proses navigasi sebelumnya
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            // Tampilkan layar detail sesuai ID
            GroupDetailScreen(groupId = groupId, onNavigateBack = { navController.popBackStack() })
        }
    }
}

/**
 * Layar Placeholder untuk Detail Grup.
 * Analogi: Ruangan khusus yang hanya bisa dibuka dengan kunci ID tertentu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(groupId: String, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Grup: $groupId") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali"
                        )
                    }
                }
            )
        }
    ) { padding ->
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
