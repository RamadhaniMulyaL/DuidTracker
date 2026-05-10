package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao // Menandai interface ini sebagai Data Access Object
import androidx.room.Delete // Digunakan untuk perintah hapus data
import androidx.room.Insert // Digunakan untuk perintah tambah data
import androidx.room.OnConflictStrategy // Strategi jika ada data yang sama (ID duplikat)
import androidx.room.Query // Digunakan untuk menulis perintah SQL kustom
import com.kelompok.duidtracker.data.local.entity.TransactionEntity // Menghubungkan dengan tabel Transaksi
import kotlinx.coroutines.flow.Flow // Stream data yang selalu update jika ada perubahan di DB

/**
 * Interface DAO untuk mengelola operasi CRUD pada tabel transaksi.
 * Analogi: Ini adalah "Pelayan Restoran" yang mencatat dan mengambil pesanan dari "Dapur" (Database).
 */
@Dao
interface TransactionDao {

    /**
     * Menambah atau memperbarui transaksi.
     * Analogi: Menaruh catatan di papan pengumuman; jika judulnya sama, ganti isinya.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Jika ID sama, data lama ditimpa data baru
    suspend fun upsert(transaction: TransactionEntity) // suspend agar tidak mengganggu kelancaran UI

    /**
     * Menghapus objek transaksi tertentu dari database.
     */
    @Delete
    suspend fun delete(transaction: TransactionEntity)

    /**
     * Menghapus transaksi berdasarkan ID spesifik.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Mengambil daftar transaksi pribadi (yang tidak memiliki groupId).
     * Diurutkan dari yang terbaru ke terlama.
     * Analogi: Mengambil tumpukan struk belanja pribadi dari laci.
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL ORDER BY tanggal DESC")
    fun getPersonalTransactions(userId: String): Flow<List<TransactionEntity>> // Flow memastikan UI update otomatis

    /**
     * Mengambil daftar transaksi milik grup tertentu.
     */
    @Query("SELECT * FROM transactions WHERE group_id = :groupId ORDER BY tanggal DESC")
    fun getGroupTransactions(groupId: String): Flow<List<TransactionEntity>>

    /**
     * Mengambil transaksi pribadi berdasarkan kategori tertentu (misal: "Makanan").
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL AND kategori = :kategori ORDER BY tanggal DESC")
    fun getPersonalByCategory(userId: String, kategori: String): Flow<List<TransactionEntity>>

    /**
     * Menghitung total uang berdasarkan tipe (pemasukan/pengeluaran) untuk transaksi pribadi.
     * Analogi: Meminta kalkulator menghitung jumlah seluruh struk di satu kotak.
     */
    @Query("SELECT SUM(nominal) FROM transactions WHERE user_id = :userId AND group_id IS NULL AND type = :type")
    fun getTotalByType(userId: String, type: String): Flow<Double?> // Mengembalikan nominal total atau null jika kosong

    /**
     * Menghitung total uang berdasarkan tipe untuk grup tertentu.
     */
    @Query("SELECT SUM(nominal) FROM transactions WHERE group_id = :groupId AND type = :type")
    fun getGroupTotalByType(groupId: String, type: String): Flow<Double?>

    /**
     * Menghapus seluruh data dari tabel transaksi.
     * Analogi: Membakar seluruh arsip transaksi (Reset data).
     */
    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
