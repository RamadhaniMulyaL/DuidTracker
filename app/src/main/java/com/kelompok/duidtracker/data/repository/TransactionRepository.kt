package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth // Alat untuk mengecek identitas pengguna (siapa yang sedang login)
import com.google.firebase.firestore.FirebaseFirestore // Alat untuk mengakses database awan Firestore
import com.kelompok.duidtracker.data.local.dao.TransactionDao // Pelayan database lokal Room untuk data Transaksi
import com.kelompok.duidtracker.data.local.entity.TransactionEntity // Formulir data Transaksi
import kotlinx.coroutines.CoroutineScope // Ruang lingkup untuk menjalankan perintah background
import kotlinx.coroutines.Dispatchers // Pengatur jalur kerja (IO untuk database/internet)
import kotlinx.coroutines.SupervisorJob // Memastikan satu kegagalan tidak mematikan seluruh sistem sinkronisasi
import kotlinx.coroutines.flow.Flow // Aliran data real-time dari database
import kotlinx.coroutines.flow.combine // Alat untuk menggabungkan dua aliran data menjadi satu laporan
import kotlinx.coroutines.flow.map // Alat untuk mengubah isi data di dalam aliran (misal null jadi 0.0)
import kotlinx.coroutines.launch // Menjalankan perintah di jalur background (non-blocking)
import kotlinx.coroutines.tasks.await // Menunggu proses Firebase selesai tanpa menghentikan aplikasi

/**
 * Repository untuk mengelola data Transaksi (Pemasukan & Pengeluaran).
 * Analogi: Ini adalah "Manajer Logistik" yang memastikan catatan keuangan di HP dan di Cloud selalu sinkron.
 */
class TransactionRepository(
    private val dao: TransactionDao, // Akses ke laci penyimpanan data di memori HP
    private val firestore: FirebaseFirestore, // Akses ke gudang data besar di internet (Cloud)
    private val auth: FirebaseAuth // Akses ke identitas digital pengguna
) {
    // Menunjuk alamat koleksi "transactions" di pusat data cloud
    private val transactionsRef = firestore.collection("transactions")
    
    // Tim pekerja khusus di balik layar untuk urusan sinkronisasi otomatis
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        // Alur: Saat aplikasi dibuka, jika pengguna sudah masuk (login)...
        auth.currentUser?.uid?.let { userId ->
            // ...langsung aktifkan walkie-talkie (listener) untuk mendengar kabar data dari Cloud
            startFirestoreListener(userId)
        }
    }

    /**
     * Memasang pendengar (listener) perubahan data di Cloud secara real-time.
     * Analogi: Seperti asisten yang selalu standby memantau papan pengumuman di pusat.
     */
    private fun startFirestoreListener(userId: String) {
        // Mencari dokumen transaksi milik user yang bersifat pribadi (tanpa grup)
        transactionsRef
            .whereEqualTo("user_id", userId)
            .whereEqualTo("group_id", null)
            .addSnapshotListener { snapshots, e ->
                // Jika koneksi internet putus atau data kosong, jangan lakukan apapun
                if (e != null || snapshots == null) return@addSnapshotListener
                
                // Jika ada perubahan (tambah/edit) di Cloud, segera perbarui database di HP
                repositoryScope.launch {
                    snapshots.documents.forEach { doc ->
                        // Ubah kiriman cloud jadi format formulir kita, lalu simpan ke database HP
                        doc.toTransactionEntity()?.let { dao.upsert(it) }
                    }
                }
            }
    }

    /**
     * Menambah transaksi baru ke dua tempat sekaligus: Cloud dan Lokal HP.
     * Alur: Kirim ke Cloud dulu, jika sukses baru simpan di HP agar data tetap aman walau HP hilang.
     */
    suspend fun addTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            // Tahap 1: Daftarkan ke Cloud. await() artinya tunggu sampai sinyal terkirim.
            transactionsRef.document(transaction.id).set(transaction.toMap()).await()
            
            // Tahap 2: Jika cloud menerima, baru simpan di database lokal HP untuk akses offline
            dao.upsert(transaction)
            
            Result.success(Unit) // Berikan laporan "Berhasil!" ke atasan (ViewModel)
        } catch (e: Exception) {
            Result.failure(e) // Berikan laporan "Gagal" beserta alasan kerusakannya
        }
    }

    /**
     * Menghapus catatan transaksi dari Cloud dan memori HP secara permanen.
     */
    suspend fun deleteTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            // Menghapus data di pusat informasi (Cloud)
            transactionsRef.document(transaction.id).delete().await()
            // Menghapus data di laci lokal (HP)
            dao.delete(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengambil aliran data transaksi pribadi.
     * Analogi: Memasang pipa air dari database langsung ke layar HP.
     */
    fun getPersonalTransactions(userId: String): Flow<List<TransactionEntity>> {
        return dao.getPersonalTransactions(userId) // Data mengalir otomatis setiap ada perubahan
    }

    /**
     * Mengambil aliran data transaksi untuk kelompok tertentu.
     */
    fun getGroupTransactions(groupId: String): Flow<List<TransactionEntity>> {
        return dao.getGroupTransactions(groupId)
    }

    /**
     * Menghitung total saldo (Pemasukan vs Pengeluaran) secara otomatis.
     * Analogi: Menggabungkan dua timbangan menjadi satu laporan keuangan lengkap.
     */
    fun getPersonalTotals(userId: String): Flow<Pair<Double, Double>> {
        // Menyiapkan kran data untuk jumlah pemasukan (jika kosong set ke 0.0)
        val incomeFlow = dao.getTotalByType(userId, TransactionEntity.TYPE_INCOME).map { it ?: 0.0 }
        // Menyiapkan kran data untuk jumlah pengeluaran
        val expenseFlow = dao.getTotalByType(userId, TransactionEntity.TYPE_EXPENSE).map { it ?: 0.0 }
        
        // Menggabungkan (combine) kedua aliran data tersebut menjadi satu paket
        return combine(incomeFlow, expenseFlow) { income, expense ->
            Pair(income, expense) // Mengembalikan pasangan: (Total Masuk, Total Keluar)
        }
    }

    /**
     * Melakukan sinkronisasi manual dari internet ke HP (Full Backup).
     * Analogi: Seperti mendownload ulang semua file dari Google Drive ke memori HP.
     */
    fun syncFromFirestore(userId: String) {
        repositoryScope.launch {
            try {
                // Mencari semua data di Cloud yang userId-nya adalah milik kita
                val snapshot = transactionsRef.whereEqualTo("user_id", userId).get().await()
                snapshot.documents.forEach { doc ->
                    // Masukkan semua data satu per satu ke database lokal HP
                    doc.toTransactionEntity()?.let { dao.upsert(it) }
                }
            } catch (e: Exception) {
                e.printStackTrace() // Catat jika ada gangguan kabel/sinyal
            }
        }
    }
}

/**
 * Alat bantu (Extension) untuk menerjemahkan data Kotlin ke bahasa Cloud Firestore (Map).
 */
fun TransactionEntity.toMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "user_id" to userId,
        "group_id" to groupId,
        "type" to type,
        "nama" to nama,
        "nominal" to nominal,
        "kategori" to kategori,
        "tanggal" to tanggal,
        "created_at" to createdAt
    )
}

/**
 * Alat bantu untuk mengubah dokumen dari Cloud kembali menjadi formulir data Kotlin.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toTransactionEntity(): TransactionEntity? {
    return try {
        TransactionEntity(
            id = getString("id") ?: id, // Ambil ID teks
            userId = getString("user_id") ?: return null, // UserID wajib ada
            groupId = getString("group_id"), // GroupID boleh kosong (Pribadi)
            type = getString("type") ?: return null, // Tipe (Masuk/Keluar) wajib ada
            nama = getString("nama") ?: "",
            nominal = getDouble("nominal") ?: 0.0, // Ambil angka uang
            kategori = getString("kategori") ?: "",
            tanggal = getLong("tanggal") ?: 0L,
            createdAt = getLong("created_at") ?: 0L
        )
    } catch (e: Exception) {
        null // Jika data dari Cloud rusak/tidak valid, abaikan dokumen tersebut
    }
}
