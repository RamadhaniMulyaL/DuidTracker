package com.kelompok.duidtracker.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kelompok.duidtracker.data.local.dao.TransactionDao
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repository untuk mengelola data Transaksi.
 */
class TransactionRepository(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val transactionsRef = firestore.collection("transactions")
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var transactionListener: ListenerRegistration? = null

    /**
     * Memulai pendengar Firestore untuk user tertentu.
     */
    fun startFirestoreListener(userId: String) {
        stopListener() 
        
        if (userId.isEmpty()) return

        transactionListener = transactionsRef
            .whereEqualTo("userId", userId) 
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    Log.e("TX_AUDIT", "Listener error: ${e?.message}")
                    return@addSnapshotListener
                }
                
                repositoryScope.launch {
                    snapshots.documents.forEach { doc ->
                        doc.toTransactionEntity()?.let { dao.upsert(it) }
                    }
                }
            }
    }

    /**
     * Menghentikan pendengar aktif.
     */
    fun stopListener() {
        transactionListener?.remove()
        transactionListener = null
    }

    /**
     * Menambah transaksi dengan Audit Log Detail.
     */
    suspend fun addTransaction(transaction: TransactionEntity): Result<Unit> {
        val payload = transaction.toMap()
        val uid = auth.currentUser?.uid

        Log.d("TX_AUDIT", "=== ATTEMPTING FIRESTORE WRITE ===")
        Log.d("TX_AUDIT", "Path: transactions/${transaction.id}")
        Log.d("TX_AUDIT", "Auth UID: $uid")
        Log.d("TX_AUDIT", "Payload: $payload")

        return try {
            transactionsRef.document(transaction.id).set(payload).await()
            Log.d("TX_AUDIT", "FIRESTORE WRITE SUCCESS")
            dao.upsert(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TX_AUDIT", "WRITE FAILED: ${e.message}")
            Log.e("TX_AUDIT", "Possible Cause: Mismatch between payload keys (userId) and Security Rules (user_id?)")
            Log.e("TX_AUDIT", "Full Stack Trace:", e)
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit> {
        Log.d("TX_AUDIT", "Attempting delete: ${transaction.id}")
        return try {
            transactionsRef.document(transaction.id).delete().await()
            dao.delete(transaction)
            Log.d("TX_AUDIT", "Delete success")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TX_AUDIT", "Delete failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun getPersonalTransactions(userId: String): Flow<List<TransactionEntity>> {
        return dao.getPersonalTransactions(userId)
    }

    fun getGroupTransactions(groupId: String): Flow<List<TransactionEntity>> {
        return dao.getGroupTransactions(groupId)
    }

    fun getPersonalTotals(userId: String): Flow<Pair<Double, Double>> {
        val incomeFlow = dao.getTotalByType(userId, TransactionEntity.TYPE_INCOME).map { it ?: 0.0 }
        val expenseFlow = dao.getTotalByType(userId, TransactionEntity.TYPE_EXPENSE).map { it ?: 0.0 }
        
        return combine(incomeFlow, expenseFlow) { income, expense ->
            Pair(income, expense)
        }
    }

    suspend fun clearLocalData() {
        dao.clearAll()
    }

    fun syncFromFirestore(userId: String) {
        repositoryScope.launch {
            try {
                val snapshot = transactionsRef.whereEqualTo("userId", userId).get().await()
                snapshot.documents.forEach { doc ->
                    doc.toTransactionEntity()?.let { dao.upsert(it) }
                }
            } catch (e: Exception) {
                Log.e("TX_AUDIT", "Sync failed: ${e.message}")
            }
        }
    }
}

/**
 * Extension untuk konversi ke Map Firestore.
 * PENTING: Pastikan field names di sini sama persis dengan yang ada di Security Rules.
 */
fun TransactionEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId, 
        "groupId" to groupId, 
        "type" to type,
        "nama" to nama,
        "nominal" to nominal,
        "kategori" to kategori,
        "tanggal" to tanggal,
        "createdAt" to createdAt
    )
}

/**
 * Konversi Dokumen Firestore ke Entity.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toTransactionEntity(): TransactionEntity? {
    return try {
        TransactionEntity(
            id = getString("id") ?: id,
            userId = getString("userId") ?: return null,
            groupId = getString("groupId"),
            type = getString("type") ?: return null,
            nama = getString("nama") ?: "",
            nominal = getDouble("nominal") ?: 0.0,
            kategori = getString("kategori") ?: "",
            tanggal = getLong("tanggal") ?: 0L,
            createdAt = getLong("createdAt") ?: 0L
        )
    } catch (e: Exception) {
        null
    }
}
