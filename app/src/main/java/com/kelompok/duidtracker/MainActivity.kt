package com.kelompok.duidtracker

import android.os.Bundle // Digunakan untuk menyimpan status activity
import androidx.activity.ComponentActivity // Class dasar untuk activity yang mendukung Compose
import androidx.activity.compose.setContent // Menghubungkan layout UI Compose ke Activity
import androidx.activity.enableEdgeToEdge // Mengaktifkan tampilan layar penuh (tanpa bar sistem yang kaku)
import androidx.compose.material3.MaterialTheme // Tema utama (warna, font) aplikasi
import androidx.compose.runtime.Composable // Menandai fungsi sebagai komponen UI Compose
import androidx.lifecycle.viewmodel.compose.viewModel // Membantu membuat/mengambil ViewModel di Compose
import com.google.firebase.auth.FirebaseAuth // Library untuk urusan akun pengguna
import com.google.firebase.firestore.FirebaseFirestore // Library untuk database awan (cloud)
import com.kelompok.duidtracker.data.local.database.AppDatabase // Gedung pusat data lokal (Room)
import com.kelompok.duidtracker.data.repository.AuthRepository // Penjembatan data autentikasi
import com.kelompok.duidtracker.data.repository.GroupRepository // Penjembatan data grup
import com.kelompok.duidtracker.data.repository.TransactionRepository // Penjembatan data transaksi
import com.kelompok.duidtracker.ui.AppNavigation // Pusat kendali navigasi (jalur antar layar)
import com.kelompok.duidtracker.ui.theme.DuidTrackerTheme // Tema khusus project DuidTracker
import com.kelompok.duidtracker.viewmodel.AuthViewModel // Pengelola logika login
import com.kelompok.duidtracker.viewmodel.GroupViewModel // Pengelola logika kelompok
import com.kelompok.duidtracker.viewmodel.TransactionViewModel // Pengelola logika keuangan

/**
 * Activity Utama yang menjadi tempat tinggal seluruh tampilan aplikasi.
 * Analogi: Ini adalah "Panggung Utama" tempat semua drama (layar) dipentaskan.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- MANUAL DEPENDENCY INJECTION (Persiapan Alat & Gudang) ---
        
        // 1. Persiapan Firebase (Gudang Cloud)
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // 2. Persiapan Room (Gudang Lokal di HP)
        val database = AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val groupDao = database.groupDao()

        // 3. Merakit Jembatan Data (Repository)
        // Analogi: Menunjuk manajer gudang yang tahu cara ambil barang dari HP dan dari Cloud
        val authRepository = AuthRepository(auth, firestore)
        val transactionRepository = TransactionRepository(transactionDao, firestore, auth)
        val groupRepository = GroupRepository(groupDao, firestore, auth)

        // 4. Persiapan Perakit Otak (ViewModel Factories)
        // Kita butuh ini karena ViewModel tidak bisa menerima data langsung tanpa bantuan perakit
        val userId = auth.currentUser?.uid ?: ""
        
        val authViewModelFactory = AuthViewModel.Factory(authRepository)
        val transactionViewModelFactory = TransactionViewModel.Factory(transactionRepository, userId)
        val groupViewModelFactory = GroupViewModel.Factory(groupRepository, userId)

        // Mengaktifkan fitur layar penuh hingga ke pinggiran HP
        enableEdgeToEdge()

        // Memulai penggambaran UI menggunakan Jetpack Compose
        setContent {
            // Membungkus seluruh aplikasi dengan tema DuidTracker
            DuidTrackerTheme {
                // Membuat/mengambil instance ViewModel menggunakan bantuan perakit
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                val transactionViewModel: TransactionViewModel = viewModel(factory = transactionViewModelFactory)
                val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                // Menjalankan Pusat Kendali Navigasi
                // Analogi: Menghidupkan GPS yang akan memandu user ke layar yang tepat
                AppNavigation(
                    authViewModel = authViewModel,
                    transactionViewModel = transactionViewModel,
                    groupViewModel = groupViewModel
                )
            }
        }
    }
}
