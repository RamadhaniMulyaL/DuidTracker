package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao // Penanda interface untuk akses database Room
import androidx.room.Delete // Perintah untuk menghapus data dari tabel
import androidx.room.Insert // Perintah untuk memasukkan data baru
import androidx.room.OnConflictStrategy // Aturan main jika ada ID data yang bentrok
import androidx.room.Query // Perintah untuk menulis kueri SQL kustom
import com.kelompok.duidtracker.data.local.entity.GroupEntity // Menghubungkan pelayan ini dengan tabel Grup
import kotlinx.coroutines.flow.Flow // Aliran data real-time yang otomatis update UI

/**
 * Interface DAO untuk mengelola data Kelompok/Grup.
 * Analogi: Ini adalah "Buku Daftar Anggota" yang dipegang oleh resepsionis untuk mengecek siapa saja yang punya grup.
 */
@Dao
interface GroupDao {

    /**
     * Menambah atau memperbarui informasi grup.
     * Analogi: Mencatat nama kelompok baru di buku besar. Jika nama sudah ada, perbarui detailnya.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Jika ID grup sudah ada, data lama diganti dengan yang paling baru
    suspend fun upsert(group: GroupEntity) // 'suspend' memastikan HP tidak 'lag' saat proses simpan data

    /**
     * Menghapus informasi grup tertentu dari memori HP.
     */
    @Delete
    suspend fun delete(group: GroupEntity)

    /**
     * Mengambil detail satu grup berdasarkan ID-nya.
     * Analogi: Mencari satu halaman spesifik di buku besar berdasarkan nomor halaman uniknya.
     */
    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupById(id: String): Flow<GroupEntity?> // 'Flow' membuat layar otomatis berubah jika nama grup diganti di tempat lain

    /**
     * Mengambil semua grup di mana pengguna tertentu menjadi anggotanya.
     * Analogi: Menyisir seluruh buku besar untuk mencari nama Anda di setiap daftar anggota kelompok.
     */
    // Menggunakan perintah LIKE untuk mencari ID user di dalam teks JSON daftar anggota
    @Query("SELECT * FROM groups WHERE members_json LIKE '%' || :userId || '%'")
    fun getAllGroupsForUser(userId: String): Flow<List<GroupEntity>>

    /**
     * Mencari grup berdasarkan kode undangan (Invite Code).
     * Analogi: Mencari pintu rahasia yang kuncinya cocok dengan kode unik yang Anda bawa.
     */
    @Query("SELECT * FROM groups WHERE invite_code = :code")
    suspend fun getGroupByInviteCode(code: String): GroupEntity? // Digunakan saat user klik "Gabung Grup"

    /**
     * Menghapus seluruh data grup dari database lokal.
     * Analogi: Membakar seluruh buku daftar anggota (Reset total).
     */
    @Query("DELETE FROM groups")
    suspend fun clearAll()
}
