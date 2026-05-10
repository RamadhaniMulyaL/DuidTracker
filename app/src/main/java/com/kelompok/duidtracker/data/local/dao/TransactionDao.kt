package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL ORDER BY tanggal DESC")
    fun getPersonalTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE group_id = :groupId ORDER BY tanggal DESC")
    fun getGroupTransactions(groupId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE user_id = :userId AND group_id IS NULL AND kategori = :kategori ORDER BY tanggal DESC")
    fun getPersonalByCategory(userId: String, kategori: String): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(nominal) FROM transactions WHERE user_id = :userId AND group_id IS NULL AND type = :type")
    fun getTotalByType(userId: String, type: String): Flow<Double?>

    @Query("SELECT SUM(nominal) FROM transactions WHERE group_id = :groupId AND type = :type")
    fun getGroupTotalByType(groupId: String, type: String): Flow<Double?>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
