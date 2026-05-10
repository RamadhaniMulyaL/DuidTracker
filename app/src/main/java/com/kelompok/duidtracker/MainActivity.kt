package com.kelompok.duidtracker

import android.os.Bundle // Digunakan untuk menyimpan status activity
import androidx.activity.ComponentActivity // Class dasar untuk activity yang mendukung Compose
import androidx.activity.compose.setContent // Menghubungkan layout UI Compose ke Activity
import androidx.activity.enableEdgeToEdge // Mengaktifkan tampilan layar penuh (tanpa bar sistem yang kaku)
import androidx.compose.foundation.layout.Box // Wadah untuk menumpuk elemen UI
import androidx.compose.foundation.layout.Column // Wadah untuk menyusun elemen secara vertikal
import androidx.compose.foundation.layout.fillMaxSize // Perintah agar elemen mengisi seluruh layar
import androidx.compose.foundation.layout.padding // Memberikan jarak aman di pinggir elemen
import androidx.compose.material3.Button // Tombol standar Material Design 3
import androidx.compose.material3.MaterialTheme // Tema utama (warna, font) aplikasi
import androidx.compose.material3.Scaffold // Struktur layout dasar (menyediakan slot untuk bar atas/bawah)
import androidx.compose.material3.Text // Komponen untuk menampilkan teks
import androidx.compose.runtime.Composable // Menandai fungsi sebagai komponen UI Compose
import androidx.compose.runtime.collectAsState // Mengamati perubahan data dari Flow/StateFlow
import androidx.compose.runtime.getValue // Mempermudah pengambilan nilai delegasi state
import androidx.compose.ui.Alignment // Mengatur perataan posisi elemen
import androidx.compose.ui.Modifier // Modifikator untuk mengatur tampilan dan perilaku elemen
import androidx.compose.ui.unit.dp // Satuan ukuran piksel standar Android
import androidx.lifecycle.viewmodel.compose.viewModel // Membantu membuat/mengambil ViewModel di Compose
import androidx.navigation.compose.NavHost // Wadah pusat navigasi aplikasi
import androidx.navigation.compose.composable // Mendefinisikan tujuan (screen) di dalam NavHost
import androidx.navigation.compose.rememberNavController // Mengingat posisi navigasi saat ini
import com.google.firebase.auth.FirebaseAuth // Library untuk urusan akun pengguna
import com.google.firebase.firestore.FirebaseFirestore // Library untuk database awan (cloud)
import com.kelompok.duidtracker.data.repository.AuthRepository // Penjembatan data autentikasi
import com.kelompok.duidtracker.ui.auth.LoginScreen // Layar masuk
import com.kelompok.duidtracker.ui.auth.RegisterScreen // Layar daftar
import com.kelompok.duidtracker.ui.theme.DuidTrackerTheme // Tema khusus project DuidTracker
import com.kelompok.duidtracker.viewmodel.AuthViewModel // Pengelola logika pendaftaran/masuk

/**
 * Activity Utama yang menjadi tempat tinggal seluruh tampilan aplikasi.
 * Analogi: Ini adalah "Panggung Utama" tempat semua drama (layar) dipentaskan.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- MANUAL DEPENDENCY INJECTION (Persiapan Alat) ---
        // Mengambil kunci akses ke Firebase (Analogi: Menyiapkan alat komunikasi ke server pusat)
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()
        
        // Merakit jembatan data (Analogi: Menyiapkan manajer gudang yang tahu cara ambil data)
        val authRepository = AuthRepository(auth, firestore)
        
        // Menyiapkan perakit otak (Analogi: Bengkel untuk membuat otak aplikasi/ViewModel)
        val authViewModelFactory = AuthViewModel.Factory(authRepository)

        // Mengaktifkan fitur layar penuh hingga ke pinggiran HP
        enableEdgeToEdge()
        
        // Memulai penggambaran UI menggunakan Jetpack Compose
        setContent {
            // Membungkus seluruh aplikasi dengan tema DuidTracker
            DuidTrackerTheme {
                // Membuat pengendali navigasi (Analogi: Supir yang tahu rute jalan)
                val navController = rememberNavController()
                
                // Menghubungkan UI dengan otaknya (ViewModel) menggunakan bantuan perakit (Factory)
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)

                // --- SISTEM NAVIGASI (Peta Jalan) ---
                NavHost(
                    navController = navController,
                    // Menentukan pintu masuk: Ke Home jika sudah login, ke Login jika belum.
                    startDestination = if (authRepository.isLoggedIn()) "home" else "login"
                ) {
                    // Mendefinisikan jalur ke layar "LOGIN"
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Jika sukses masuk, pindah ke Home dan hapus layar Login dari memori
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {
                                // Pindah ke layar pendaftaran jika diklik
                                navController.navigate("register")
                            }
                        )
                    }
                    
                    // Mendefinisikan jalur ke layar "REGISTER"
                    composable("register") {
                        RegisterScreen(
                            viewModel = authViewModel,
                            onRegisterSuccess = {
                                // Jika sukses daftar, pindah ke Home dan bersihkan layar-layar sebelumnya
                                navController.navigate("home") {
                                    popUpTo("login") { inclusive = true }
                                    popUpTo("register") { inclusive = true }
                                }
                            },
                            onNavigateToLogin = {
                                // Kembali ke layar login jika berubah pikiran
                                navController.popBackStack()
                            }
                        )
                    }
                    
                    // Mendefinisikan jalur ke layar "HOME" (Halaman Utama)
                    composable("home") {
                        HomeScreen(
                            onLogout = {
                                // Jalankan perintah keluar di otak aplikasi
                                authViewModel.logout()
                                // Tendang pengguna kembali ke layar Login dan hapus status Home
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Layar Sapaan Utama (Sementara).
 * Analogi: Ruang tunggu setelah Anda berhasil menunjukkan kartu akses yang benar.
 */
@Composable
fun HomeScreen(onLogout: () -> Unit) {
    // Memberikan struktur halaman bersih dengan padding sistem
    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize() // Memenuhi layar
                .padding(padding), // Hindari tertutup bar sistem
            contentAlignment = Alignment.Center // Konten di tengah-tengah
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Teks Selamat Datang
                Text(
                    text = "Selamat Datang di DuitTracker!", 
                    style = MaterialTheme.typography.headlineMedium
                )
                // Tombol untuk keluar dari aplikasi
                Button(
                    onClick = onLogout, 
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Keluar")
                }
            }
        }
    }
}
