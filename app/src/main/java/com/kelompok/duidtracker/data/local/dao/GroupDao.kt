package com.kelompok.duidtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kelompok.duidtracker.data.local.entity.GroupEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface DAO untuk mengelola data Kelompok/Grup.
 */
@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(group: GroupEntity)

    @Delete
    suspend fun delete(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupById(id: String): Flow<GroupEntity?>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupByIdOnce(id: String): GroupEntity?

    @Query("SELECT * FROM groups WHERE members_json LIKE '%' || :userId || '%'")
    fun getAllGroupsForUser(userId: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE invite_code = :code")
    suspend fun getGroupByInviteCode(code: String): GroupEntity?

    @Query("DELETE FROM groups")
    suspend fun clearAll()
}
