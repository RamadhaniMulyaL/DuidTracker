package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.gson.Gson
import com.kelompok.duidtracker.data.local.dao.GroupDao
import com.kelompok.duidtracker.data.local.entity.GroupEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Repository untuk mengelola data Kelompok/Grup.
 */
class GroupRepository(
    private val dao: GroupDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val groupsRef = firestore.collection("groups")
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var groupListener: ListenerRegistration? = null

    /**
     * Memantau grup di mana user menjadi anggota.
     */
    fun startFirestoreListener(userId: String) {
        stopListener()
        if (userId.isEmpty()) return

        groupListener = groupsRef
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                
                repositoryScope.launch {
                    snapshots.documents.forEach { doc ->
                        doc.toGroupEntity()?.let { dao.upsert(it) }
                    }
                }
            }
    }

    fun stopListener() {
        groupListener?.remove()
        groupListener = null
    }

    suspend fun createGroup(name: String, description: String, budget: Double, icon: String): Result<GroupEntity> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak terautentikasi")
            val groupId = UUID.randomUUID().toString()
            val inviteCode = generateInviteCode()
            val members = listOf(userId)
            
            val groupEntity = GroupEntity(
                id = groupId,
                name = name,
                description = description,
                ownerId = userId,
                membersJson = Gson().toJson(members),
                budget = budget,
                icon = icon,
                inviteCode = inviteCode,
                createdAt = System.currentTimeMillis()
            )

            groupsRef.document(groupId).set(groupEntity.toFirestoreMap(members)).await()
            dao.upsert(groupEntity)
            
            Result.success(groupEntity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun joinGroupByCode(inviteCode: String): Result<GroupEntity> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak terautentikasi")
            
            val snapshot = groupsRef.whereEqualTo("inviteCode", inviteCode).get().await()
            if (snapshot.isEmpty) return Result.failure(Exception("Kode undangan tidak valid"))

            val doc = snapshot.documents[0]
            val group = doc.toGroupEntity() ?: throw Exception("Gagal memproses data grup")

            if (group.isMember(userId)) return Result.failure(Exception("Kamu sudah bergabung"))

            groupsRef.document(group.id).update("members", FieldValue.arrayUnion(userId)).await()
            
            val updatedDoc = groupsRef.document(group.id).get().await()
            val updatedGroup = updatedDoc.toGroupEntity() ?: group
            dao.upsert(updatedGroup)

            Result.success(updatedGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllGroups(userId: String): Flow<List<GroupEntity>> = dao.getAllGroupsForUser(userId)

    fun getGroupById(groupId: String): Flow<GroupEntity?> = dao.getGroupById(groupId)

    suspend fun leaveGroup(groupId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak terautentikasi")
            groupsRef.document(groupId).update("members", FieldValue.arrayRemove(userId)).await()
            dao.deleteById(groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User tidak terautentikasi")
            val group = dao.getGroupByIdOnce(groupId) ?: throw Exception("Grup tidak ditemukan")
            if (group.ownerId != userId) throw Exception("Hanya pemilik yang bisa menghapus")

            groupsRef.document(groupId).delete().await()
            dao.delete(group)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetBudget(groupId: String, newBudget: Double): Result<Unit> {
        return try {
            groupsRef.document(groupId).update("budget", newBudget).await()
            val group = dao.getGroupByIdOnce(groupId)
            group?.let { dao.upsert(it.copy(budget = newBudget)) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }
}

fun GroupEntity.toFirestoreMap(membersList: List<String>): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "ownerId" to ownerId,
        "members" to membersList,
        "budget" to budget,
        "icon" to icon,
        "inviteCode" to inviteCode,
        "createdAt" to createdAt
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toGroupEntity(): GroupEntity? {
    return try {
        @Suppress("UNCHECKED_CAST")
        val membersList = get("members") as? List<String> ?: emptyList()
        GroupEntity(
            id = id,
            name = getString("name") ?: "",
            description = getString("description") ?: "",
            ownerId = getString("ownerId") ?: "",
            membersJson = Gson().toJson(membersList),
            budget = getDouble("budget") ?: 0.0,
            icon = getString("icon") ?: "",
            inviteCode = getString("inviteCode") ?: "",
            createdAt = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) {
        null
    }
}
