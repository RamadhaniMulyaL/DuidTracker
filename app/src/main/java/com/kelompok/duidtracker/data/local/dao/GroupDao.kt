package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao // Penanda interface untuk akses database Room
import androidx.room.Delete // Perintah untuk menghapus data
import androidx.room.Insert // Perintah untuk memasukkan data
import androidx.room.OnConflictStrategy // Aturan jika ada data kembar
import androidx.room.Query // Perintah SQL kustom
import com.kelompok.duidtracker.data.local.entity.GroupEntity // Hubungan ke tabel Grup
import kotlinx.coroutines.flow.Flow // Aliran data real-time

/**
 * Interface DAO untuk mengelola data Kelompok/Grup.
 * Analogi: "Buku Daftar Anggota" yang dipegang oleh resepsionis untuk mengecek siapa saja yang punya grup.
 */
@Dao
interface GroupDao {

    /**
     * Menambah atau memperbarui informasi grup.
     * Analogi: Mencatat nama kelompok baru di buku besar. Jika nama sudah ada, perbarui detailnya.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Jika ID grup sama, data lama akan diganti yang baru
    suspend fun upsert(group: GroupEntity) // suspend agar proses tulis tidak membuat HP lag

    /**
     * Menghapus grup dari database lokal.
     */
    @Delete
    suspend fun delete(group: GroupEntity)

    /**
     * Mengambil detail satu grup berdasarkan ID-nya.
     * Analogi: Mencari satu halaman spesifik di buku besar berdasarkan nomor halaman.
     */
    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupById(id: String): Flow<GroupEntity?> // Flow agar layar otomatis update jika nama grup diubah

    /**
     * Mengambil semua grup di mana pengguna tertentu menjadi anggotanya.
     * Analogi: Menyisir seluruh buku besar untuk mencari nama Anda di setiap daftar anggota kelompok.
     */
    @Query("SELECT * FROM groups WHERE members_json LIKE '%' || :userId || '%'")
    fun getAllGroupsForUser(userId: String): Flow<List<GroupEntity>> // Mencari ID user di dalam teks JSON anggota

    /**
     * Mencari grup berdasarkan kode undangan (Invite Code).
     * Analogi: Mencari pintu rahasia yang kuncinya cocok dengan kode yang Anda bawa.
     */
    @Query("SELECT * FROM groups WHERE invite_code = :code")
    suspend fun getGroupByInviteCode(code: String): GroupEntity? // Digunakan saat fitur "Gabung Grup"

    /**
     * Menghapus semua data grup (Reset).
     */
    @Query("DELETE FROM groups")
    suspend fun clearAll()
}
