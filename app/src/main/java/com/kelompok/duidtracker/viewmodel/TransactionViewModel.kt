package com.kelompok.duidtracker.viewmodel

import androidx.lifecycle.ViewModel // Class dasar untuk menyimpan data UI agar tidak hilang saat layar berputar
import androidx.lifecycle.ViewModelProvider // Alat bantu perakit ViewModel
import androidx.lifecycle.viewModelScope // Ruang kerja background agar proses hitung tidak bikin HP macet
import com.kelompok.duidtracker.data.local.entity.TransactionEntity // Formulir data transaksi
import com.kelompok.duidtracker.data.repository.TransactionRepository // Jembatan ke gudang data
import kotlinx.coroutines.flow.MutableStateFlow // Pipa data internal yang isinya bisa kita ubah-ubah
import kotlinx.coroutines.flow.StateFlow // Pipa data publik yang hanya bisa dibaca oleh layar (UI)
import kotlinx.coroutines.flow.asStateFlow // Mengubah pipa "Bisa Diubah" menjadi "Hanya Baca"
import kotlinx.coroutines.flow.update // Fungsi untuk memperbarui isi data di dalam pipa secara aman
import kotlinx.coroutines.launch // Menjalankan perintah di jalur background
import java.util.UUID // Alat pembuat nomor seri (ID) unik otomatis

/**
 * Data class untuk merangkum seluruh kondisi tampilan di layar Transaksi.
 * Analogi: Seperti "Papan Skor" digital yang menampilkan sisa saldo dan daftar belanja.
 */
data class TransactionUiState(
    val transactions: List<TransactionEntity> = emptyList(), // Daftar struk belanja yang akan dipajang di layar
    val totalIncome: Double = 0.0, // Total uang masuk yang sudah dijumlahkan
    val totalExpense: Double = 0.0, // Total uang keluar yang sudah dijumlahkan
    val isLoading: Boolean = false, // Lampu indikator: "Sistem sedang menghitung/mengambil data"
    val errorMessage: String? = null // Teks peringatan jika ada masalah sistem
)

/**
 * Otak yang mengelola logika Transaksi (Pribadi & Grup).
 * Analogi: Seperti "Akuntan Pribadi" yang mencatat, menjumlahkan, dan merapikan struk belanja Anda.
 */
class TransactionViewModel(
    private val repository: TransactionRepository, // Akses ke manajer logistik data (Repository)
    private val userId: String // ID KTP pengguna agar akuntan tahu buku siapa yang sedang dikerjakan
) : ViewModel() {

    // Membuat pipa data rahasia (Hanya akuntan yang bisa menulis/mengubah skor)
    private val _uiState = MutableStateFlow(TransactionUiState())
    // Membuka pipa data untuk dilihat oleh layar (UI), tapi layar tidak bisa mengubah isinya
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        // Alur: Begitu akuntan ini disewa (ViewModel diciptakan), dia langsung buka buku catatan lama
        loadPersonalTransactions()
    }

    /**
     * Menghubungkan layar dengan aliran data transaksi dari database secara real-time.
     */
    private fun loadPersonalTransactions() {
        // Menjalankan tugas pengamatan di jalur background
        viewModelScope.launch {
            // Langkah 1: Pasang pipa (Flow) ke daftar transaksi yang ada di memori HP
            repository.getPersonalTransactions(userId).collect { list ->
                // Setiap ada belanjaan baru masuk ke database, daftar di layar langsung diperbarui otomatis
                _uiState.update { it.copy(transactions = list) }
            }
        }
        
        viewModelScope.launch {
            // Langkah 2: Pasang pipa ke kalkulator total (Pemasukan vs Pengeluaran)
            repository.getPersonalTotals(userId).collect { totals ->
                // Memperbarui angka total di papan skor (Dashboard UI)
                _uiState.update { 
                    it.copy(
                        totalIncome = totals.first, // Angka pemasukan
                        totalExpense = totals.second // Angka pengeluaran
                    )
                }
            }
        }
    }

    /**
     * Logika untuk mencatat pengeluaran atau pemasukan baru.
     * Analogi: Seperti menulis struk belanja baru dan memasukkannya ke laci.
     */
    fun addTransaction(nama: String, nominal: Double, kategori: String, type: String, groupId: String? = null) {
        viewModelScope.launch {
            // Tahap 1: Membuat formulir transaksi lengkap
            val newTransaction = TransactionEntity(
                id = UUID.randomUUID().toString(), // Membuat nomor struk unik secara otomatis
                userId = userId, // Pemiliknya adalah user yang sedang login
                groupId = groupId, // Bisa milik pribadi (null) atau milik kelompok
                type = type, // "income" atau "expense"
                nama = nama, // Misal: "Beli Kopi"
                nominal = nominal, // Misal: 25000.0
                kategori = kategori, // Misal: "Gaya Hidup"
                tanggal = System.currentTimeMillis(), // Mencatat waktu kejadian sekarang
                createdAt = System.currentTimeMillis() // Mencatat waktu pembuatan data
            )

            // Tahap 2: Serahkan formulir ke manajer data (Repository) untuk disimpan ke HP & Cloud
            val result = repository.addTransaction(newTransaction)
            
            // Tahap 3: Jika manajer lapor gagal (misal: memori penuh), tampilkan pesan error
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = "Gagal menyimpan transaksi") }
            }
        }
    }

    /**
     * Logika untuk menghapus catatan yang salah.
     */
    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            // Meminta manajer data untuk membakar/menghapus struk ini dari HP & Cloud
            repository.deleteTransaction(transaction)
            // Hasil: Karena kita pakai sistem 'Flow', daftar di layar akan hilang sendiri tanpa perlu refresh
        }
    }

    /**
     * Bengkel perakit (Factory) untuk menciptakan TransactionViewModel.
     * Analogi: Karena Akuntan butuh 'alat' (Repository) dan 'KTP User', kita butuh bengkel khusus untuk merakitnya.
     */
    class Factory(
        private val repository: TransactionRepository, 
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            // Memastikan bengkel merakit tipe akuntan yang benar
            return TransactionViewModel(repository, userId) as T
        }
    }
}
