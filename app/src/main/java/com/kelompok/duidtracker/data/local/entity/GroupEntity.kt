package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "owner_id")
    val ownerId: String,

    @ColumnInfo(name = "members_json")
    val membersJson: String, // JSON array of userIds

    @ColumnInfo(name = "budget")
    val budget: Double,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "invite_code")
    val inviteCode: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    /**
     * Parses the [membersJson] string into a list of user IDs.
     */
    fun getMembersList(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(membersJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Checks if a user is a member of this group.
     */
    fun isMember(userId: String): Boolean {
        return getMembersList().contains(userId)
    }
}
