package com.kelompok.duidtracker.viewmodel

import androidx.lifecycle.ViewModel // Class induk untuk mengelola data UI agar tidak hilang saat rotasi layar
import androidx.lifecycle.ViewModelProvider // Alat untuk membantu pembuatan instance ViewModel
import androidx.lifecycle.viewModelScope // Lingkungan kerja (Scope) untuk menjalankan proses berat di background
import com.kelompok.duidtracker.data.repository.AuthRepository // Jembatan data autentikasi
import com.kelompok.duidtracker.data.repository.AuthResult // Bungkus hasil (Success/Error)
import kotlinx.coroutines.flow.MutableStateFlow // Pipa data yang isinya bisa kita ubah-ubah
import kotlinx.coroutines.flow.StateFlow // Pipa data versi "baca saja" (Read-only) untuk UI
import kotlinx.coroutines.flow.asStateFlow // Mengubah pipa "bisa diubah" menjadi "baca saja"
import kotlinx.coroutines.flow.update // Fungsi untuk memperbarui isi pipa data secara aman
import kotlinx.coroutines.launch // Menjalankan perintah di jalur (thread) lain agar UI tidak macet

/**
 * Data class untuk membungkus seluruh status layar Autentikasi.
 * Analogi: Seperti lampu indikator di dashboard mobil.
 */
data class AuthUiState(
    val isLoading: Boolean = false, // Lampu indikator: "Mesin sedang bekerja"
    val isSuccess: Boolean = false, // Lampu indikator: "Tujuan tercapai (Berhasil)"
    val errorMessage: String? = null, // Layar teks: Menampilkan pesan jika ada kerusakan/error
    val isLoggedIn: Boolean = false // Status: Apakah pengemudi sudah duduk (Sudah Login)
)

/**
 * ViewModel untuk mengelola logika Login dan Register.
 * Analogi: Ini adalah "Otak" yang memproses permintaan dari tombol di layar.
 */
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Membuat pipa data privat (Hanya otak yang bisa mengubah status)
    private val _uiState = MutableStateFlow(AuthUiState())
    
    // Membuka pipa data untuk dilihat oleh Mata (UI), tapi UI tidak bisa mengubahnya langsung
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Kode yang langsung jalan saat ViewModel diciptakan (Start-up)
    init {
        checkLoginStatus() // Langsung cek: "Apakah user masih login dari sesi sebelumnya?"
    }

    /**
     * Logika untuk pendaftaran akun baru.
     */
    fun register(name: String, email: String, password: String) {
        // Menjalankan tugas di jalur background agar aplikasi tidak 'Not Responding'
        viewModelScope.launch {
            // Langkah 1: Nyalakan lampu loading dan bersihkan error lama
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            
            // Langkah 2: Minta Repository (Gudang Data) melakukan pendaftaran ke Firebase
            val result = repository.register(name, email, password)
            
            // Langkah 3: Olah hasilnya (Berhasil atau Gagal)
            handleResult(result)
        }
    }

    /**
     * Logika untuk masuk ke akun lama.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Langkah 1: Beri tahu UI bahwa kita mulai bekerja (Loading)
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
            
            // Langkah 2: Kirim data ke Firebase melalui Repository
            val result = repository.login(email, password)
            
            // Langkah 3: Sampaikan hasilnya ke layar
            handleResult(result)
        }
    }

    /**
     * Fungsi internal untuk menerjemahkan hasil dari Repository ke status UI.
     * Analogi: Seperti asisten yang merangkum laporan untuk bos.
     */
    private fun handleResult(result: AuthResult<Unit>) {
        when (result) {
            // Kasus A: Jika laporannya "Sukses"
            is AuthResult.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false, // Matikan lampu loading
                        isSuccess = true,  // Nyalakan tanda berhasil
                        isLoggedIn = true  // Tandai user sekarang sudah masuk
                    )
                }
            }
            // Kasus B: Jika laporannya "Gagal/Error"
            is AuthResult.Error -> {
                _uiState.update {
                    it.copy(
                        isLoading = false, // Matikan lampu loading
                        errorMessage = result.message, // Ambil pesan error (misal: "Sinyal Lemah")
                        isSuccess = false // Pastikan tanda berhasil mati
                    )
                }
            }
            // Kasus C: Jika sedang dalam proses
            is AuthResult.Loading -> {
                _uiState.update { it.copy(isLoading = true) }
            }
        }
    }

    /**
     * Logika untuk keluar dari akun.
     */
    fun logout() {
        repository.logout() // Perintahkan Firebase untuk hapus sesi
        _uiState.update { AuthUiState(isLoggedIn = false) } // Reset seluruh indikator di dashboard UI
    }

    /**
     * Menghapus pesan error agar tidak terus-terusan muncul di layar.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Mengecek status sesi pengguna di memori HP.
     */
    fun checkLoginStatus() {
        val loggedIn = repository.isLoggedIn() // Tanya ke repository: "Ada KTP user tersimpan?"
        _uiState.update { it.copy(isLoggedIn = loggedIn) } // Update status di dashboard
    }

    /**
     * Kelas pabrik (Factory) untuk merakit AuthViewModel.
     * Analogi: Karena ViewModel butuh 'alat' (AuthRepository), kita butuh 'Perakit' untuk menyediakannya.
     */
    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Memastikan class yang dirakit benar-benar AuthViewModel
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class") // Marah jika disuruh merakit yang salah
        }
    }
}
