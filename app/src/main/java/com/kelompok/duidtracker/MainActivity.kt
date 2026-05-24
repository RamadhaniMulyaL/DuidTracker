package com.kelompok.duidtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok.duidtracker.data.local.database.AppDatabase
import com.kelompok.duidtracker.data.repository.AuthRepository
import com.kelompok.duidtracker.data.repository.GroupRepository
import com.kelompok.duidtracker.data.repository.TransactionRepository
import com.kelompok.duidtracker.ui.AppNavigation
import com.kelompok.duidtracker.ui.theme.DuidTrackerTheme
import com.kelompok.duidtracker.viewmodel.AuthViewModel
import com.kelompok.duidtracker.viewmodel.GroupViewModel
import com.kelompok.duidtracker.viewmodel.TransactionViewModel

/**
 * Activity Utama (Panggung Utama).
 * Menginisialisasi seluruh kebutuhan data (Room, Firebase, Repository)
 * dan menyediakan ViewModel untuk seluruh aplikasi.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Firebase
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // 2. Inisialisasi Room Database & DAOs
        val database = AppDatabase.getDatabase(this)
        val transactionDao = database.transactionDao()
        val groupDao = database.groupDao()

        // 3. Inisialisasi Repositories
        val authRepo = AuthRepository(auth, firestore)
        val transactionRepo = TransactionRepository(transactionDao, firestore, auth)
        val groupRepo = GroupRepository(groupDao, firestore, auth)

        // 4. Inisialisasi ViewModel Factories
        val authViewModelFactory = AuthViewModel.Factory(authRepo)
        val transactionViewModelFactory = TransactionViewModel.Factory(transactionRepo, authRepo)
        val groupViewModelFactory = GroupViewModel.Factory(groupRepo, transactionRepo, authRepo)

        // Aktifkan tampilan layar penuh
        enableEdgeToEdge()

        setContent {
            DuidTrackerTheme {
                // Membuat instance ViewModel menggunakan Factories
                val authViewModel: AuthViewModel = viewModel(factory = authViewModelFactory)
                val transactionViewModel: TransactionViewModel = viewModel(factory = transactionViewModelFactory)
                val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                val authState by authViewModel.uiState.collectAsStateWithLifecycle()

                // Safety adjustment: Tangani transisi session (Issue 1 & 3)
                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        // User masuk: Mulai sinkronisasi data
                        transactionViewModel.refreshData()
                        groupViewModel.refreshData()
                    } else {
                        // User keluar: Bersihkan data di layar dan hentikan listeners
                        transactionViewModel.clearState()
                        groupViewModel.clearState()
                    }
                }

                // Jalankan Navigasi Utama dengan seluruh ViewModel yang dibutuhkan
                AppNavigation(
                    authViewModel = authViewModel,
                    transactionViewModel = transactionViewModel,
                    groupViewModel = groupViewModel
                )
            }
        }
    }
}
