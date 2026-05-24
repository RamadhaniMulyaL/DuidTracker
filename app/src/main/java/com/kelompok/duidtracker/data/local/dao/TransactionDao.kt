package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO untuk mengelola operasi CRUD pada tabel transaksi.
 * Sesuai dengan persyaratan Step F1.3:
 * - Operasi tulis menggunakan 'suspend'
 * - Operasi baca (Flow) tidak menggunakan 'suspend'
 * - Strategi Insert menggunakan REPLACE (Upsert)
 */
@Dao
interface TransactionDao {

    /**
     * 1. Menambah atau memperbarui transaksi (Upsert).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    /**
     * 2. Menghapus objek transaksi tertentu.
     */
    @Delete
    suspend fun delete(transaction: TransactionEntity)

    /**
     * 3. Menghapus transaksi berdasarkan ID spesifik.
     */
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 4. Mengambil daftar transaksi pribadi (personal).
     * Diurutkan dari yang terbaru (tanggal DESC), di mana group_id NULL.
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL ORDER BY tanggal DESC")
    fun getPersonalTransactions(userId: String): Flow<List<TransactionEntity>>

    /**
     * 5. Mengambil daftar transaksi milik grup tertentu.
     * Diurutkan dari yang terbaru (tanggal DESC).
     */
    @Query("SELECT * FROM transactions WHERE group_id = :groupId ORDER BY tanggal DESC")
    fun getGroupTransactions(groupId: String): Flow<List<TransactionEntity>>

    /**
     * 6. Mengambil transaksi pribadi berdasarkan kategori tertentu.
     */
    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL AND kategori = :kategori ORDER BY tanggal DESC")
    fun getPersonalByCategory(userId: String, kategori: String): Flow<List<TransactionEntity>>

    /**
     * 7. Menghitung total nominal berdasarkan tipe (income/expense) untuk transaksi pribadi.
     */
    @Query("SELECT SUM(nominal) FROM transactions WHERE user_id = :userId AND group_id IS NULL AND type = :type")
    fun getTotalByType(userId: String, type: String): Flow<Double?>

    /**
     * 8. Menghitung total nominal berdasarkan tipe untuk grup tertentu.
     */
    @Query("SELECT SUM(nominal) FROM transactions WHERE group_id = :groupId AND type = :type")
    fun getGroupTotalByType(groupId: String, type: String): Flow<Double?>

    /**
     * 9. Menghapus seluruh data dari tabel transaksi.
     */
    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
