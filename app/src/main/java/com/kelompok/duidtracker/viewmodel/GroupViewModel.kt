package com.kelompok.duidtracker.viewmodel

import androidx.lifecycle.ViewModel // Class induk untuk menyimpan data UI agar tahan banting saat layar berputar
import androidx.lifecycle.ViewModelProvider // Alat bantu untuk menciptakan instance ViewModel
import androidx.lifecycle.viewModelScope // Ruang kerja khusus untuk menjalankan proses di latar belakang
import com.kelompok.duidtracker.data.local.entity.GroupEntity // Formulir data untuk sebuah Kelompok/Grup
import com.kelompok.duidtracker.data.repository.GroupRepository // Manajer yang mengatur aliran data Grup
import kotlinx.coroutines.flow.MutableStateFlow // Pipa data internal yang isinya bisa kita ubah-ubah
import kotlinx.coroutines.flow.StateFlow // Pipa data publik yang hanya bisa dibaca oleh layar (UI)
import kotlinx.coroutines.flow.asStateFlow // Mengubah pipa "Bisa Diubah" menjadi "Hanya Baca"
import kotlinx.coroutines.flow.update // Fungsi untuk memperbarui isi data di dalam pipa secara aman
import kotlinx.coroutines.launch // Menjalankan perintah di jalur cepat (thread background)
import java.util.UUID // Alat pembuat ID unik (seperti nomor seri acak)

/**
 * Data class untuk merangkum seluruh kondisi tampilan pada menu Grup.
 * Analogi: Seperti layar monitor di ruang kontrol yang menampilkan daftar grup dan status sistem.
 */
data class GroupUiState(
    val groups: List<GroupEntity> = emptyList(), // Daftar grup yang diikuti pengguna
    val isLoading: Boolean = false, // Indikator jika sistem sedang sibuk (misal: saat gabung grup)
    val errorMessage: String? = null, // Pesan teks yang muncul jika ada masalah
    val joinSuccess: Boolean = false // Tanda jika proses bergabung ke grup baru berhasil
)

/**
 * Otak yang mengelola logika interaksi Grup (Buat Grup & Gabung Grup).
 * Analogi: Seperti "Sekretaris Kelompok" yang mencatat pendaftaran anggota dan membuat kelompok baru.
 */
class GroupViewModel(
    private val repository: GroupRepository, // Akses ke manajer logistik data grup
    private val userId: String // ID pengguna agar sekretaris tahu siapa yang sedang bekerja
) : ViewModel() {

    // Pipa data rahasia untuk menyimpan status terkini
    private val _uiState = MutableStateFlow(GroupUiState())
    // Pipa data yang dipasang ke layar agar UI bisa otomatis update
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()

    init {
        // Alur: Begitu aplikasi dibuka, sekretaris langsung mengambil daftar grup dari laci penyimpanan
        loadUserGroups()
    }

    /**
     * Mengambil daftar grup yang diikuti user secara real-time.
     */
    private fun loadUserGroups() {
        viewModelScope.launch {
            // Memasang kran data (Flow) ke database lokal HP
            repository.getGroupsForUser(userId).collect { list ->
                // Setiap ada perubahan data grup di HP, daftar di layar langsung diperbarui
                _uiState.update { it.copy(groups = list) }
            }
        }
    }

    /**
     * Logika untuk menciptakan kelompok baru.
     */
    fun createGroup(name: String, description: String, budget: Double) {
        viewModelScope.launch {
            // Nyalakan indikator sibuk (loading)
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Membuat identitas grup baru
            val newGroup = GroupEntity(
                id = UUID.randomUUID().toString(), // Membuat ID unik otomatis
                name = name,
                description = description,
                ownerId = userId, // Orang yang membuat grup otomatis jadi ketua (Owner)
                membersJson = "[\"$userId\"]", // Menambahkan si pembuat sebagai anggota pertama
                budget = budget,
                icon = "default_icon", // Ikon standar
                inviteCode = generateInviteCode(), // Membuat kode rahasia untuk mengajak teman
                createdAt = System.currentTimeMillis() // Mencatat waktu pembuatan
            )

            // Mengirim pendaftaran grup baru ke manajer data
            val result = repository.createGroup(newGroup)
            
            // Menangani hasil pendaftaran
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = "Gagal membuat grup: ${result.exceptionOrNull()?.message}" 
                    )
                }
            }
        }
    }

    /**
     * Logika untuk bergabung ke grup orang lain menggunakan kode unik.
     */
    fun joinGroup(inviteCode: String) {
        viewModelScope.launch {
            // Nyalakan indikator sibuk
            _uiState.update { it.copy(isLoading = true, errorMessage = null, joinSuccess = false) }
            
            // Minta manajer data untuk mendaftarkan user ke grup cloud lewat kode tersebut
            val result = repository.joinGroup(inviteCode)
            
            if (result.isSuccess) {
                // Jika sukses, nyalakan tanda berhasil agar UI bisa memberi selamat atau pindah layar
                _uiState.update { it.copy(isLoading = false, joinSuccess = true) }
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = result.exceptionOrNull()?.message ?: "Gagal bergabung" 
                    )
                }
            }
        }
    }

    /**
     * Fungsi sederhana untuk membuat kode unik 6 karakter.
     * Analogi: Seperti membuat password acak untuk kunci pintu grup.
     */
    private fun generateInviteCode(): String {
        return UUID.randomUUID().toString().take(6).uppercase()
    }

    /**
     * Menghapus pesan error agar layar kembali bersih.
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Perakit (Factory) untuk menciptakan GroupViewModel.
     * Analogi: Bengkel perakit sekretaris kelompok yang membekali sekretaris dengan alat yang tepat.
     */
    class Factory(
        private val repository: GroupRepository, 
        private val userId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupViewModel::class.java)) {
                return GroupViewModel(repository, userId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
