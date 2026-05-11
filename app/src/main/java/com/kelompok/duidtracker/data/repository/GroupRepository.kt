package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth // Alat untuk mengecek identitas pengguna (siapa yang sedang login)
import com.google.firebase.firestore.FirebaseFirestore // Alat untuk mengakses database awan Firestore
import com.google.firebase.firestore.FieldValue // Digunakan untuk operasi khusus Firestore seperti tambah item ke array
import com.kelompok.duidtracker.data.local.dao.GroupDao // Pelayan database lokal Room untuk data Grup
import com.kelompok.duidtracker.data.local.entity.GroupEntity // Formulir data Grup
import com.google.gson.Gson // Library untuk mengubah list menjadi teks JSON
import kotlinx.coroutines.flow.Flow // Aliran data real-time
import kotlinx.coroutines.tasks.await // Menunggu proses Firebase selesai tanpa membuat HP lag

/**
 * Repository untuk mengelola data Kelompok/Grup.
 * Analogi: Ini adalah "Sekretaris Organisasi" yang mencatat pendaftaran kelompok baru dan daftar anggotanya.
 */
class GroupRepository(
    private val dao: GroupDao, // Akses ke laci penyimpanan grup di HP
    private val firestore: FirebaseFirestore, // Akses ke database grup di internet
    private val auth: FirebaseAuth // Akses ke identitas user aktif
) {
    // Alamat laci koleksi "groups" di pusat data Cloud (Firestore)
    private val groupsRef = firestore.collection("groups")

    /**
     * Membuat grup baru di Cloud dan menyimpannya di database lokal.
     * Analogi: Mendaftarkan organisasi baru ke pusat, lalu mencatatnya di buku saku sendiri.
     */
    suspend fun createGroup(group: GroupEntity): Result<Unit> {
        return try {
            // Langkah 1: Simpan ke Cloud (Firestore). Kita kirim data dalam bentuk Map hasil konversi.
            groupsRef.document(group.id).set(group.toFirestoreMap()).await()
            
            // Langkah 2: Jika cloud sukses, simpan juga di database lokal HP agar bisa diakses cepat tanpa internet
            dao.upsert(group)
            
            // Mengirim laporan keberhasilan
            Result.success(Unit)
        } catch (e: Exception) {
            // Mengirim laporan kegagalan jika ada masalah (misal: internet putus)
            Result.failure(e)
        }
    }

    /**
     * Bergabung ke grup menggunakan kode undangan (Invite Code).
     * Analogi: Mengetok pintu dengan kata sandi; jika benar, nama Anda dicatat di dalam grup tersebut.
     */
    suspend fun joinGroup(inviteCode: String): Result<Unit> {
        return try {
            // Memastikan pengetuk pintu (user) sudah memiliki ID login
            val userId = auth.currentUser?.uid ?: throw Exception("Anda harus login terlebih dahulu")

            // Langkah 1: Cari grup di Firestore yang kodenya cocok dengan input user
            val snapshot = groupsRef.whereEqualTo("invite_code", inviteCode).limit(1).get().await()
            
            // Jika tidak ada grup yang punya kode tersebut, batalkan proses
            if (snapshot.isEmpty) throw Exception("Kode undangan tidak valid")
            
            // Ambil data grup yang ditemukan
            val doc = snapshot.documents[0]
            val groupId = doc.id

            // Langkah 2: Tambahkan ID user ke daftar array 'members' di Firestore (Pusat Data)
            groupsRef.document(groupId).update("members", FieldValue.arrayUnion(userId)).await()

            // Langkah 3: Ambil data terbaru grup tersebut (setelah user resmi jadi anggota)
            val updatedDoc = groupsRef.document(groupId).get().await()
            
            // Simpan data grup terbaru ke database lokal HP agar user bisa langsung melihatnya di menu Grup
            updatedDoc.toGroupEntity()?.let { dao.upsert(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengambil aliran data (Flow) seluruh grup yang diikuti oleh pengguna dari database lokal.
     * Analogi: Memasang pipa informasi dari gudang lokal ke layar HP Anda.
     */
    fun getGroupsForUser(userId: String): Flow<List<GroupEntity>> {
        return dao.getAllGroupsForUser(userId)
    }

    /**
     * Mengambil detail satu grup berdasarkan ID-nya secara real-time.
     */
    fun getGroupById(groupId: String): Flow<GroupEntity?> {
        return dao.getGroupById(groupId)
    }

    /**
     * Menyinkronkan (download) seluruh daftar grup dari Cloud ke HP.
     * Analogi: Melakukan sinkronisasi email agar pesan di server muncul di aplikasi HP.
     */
    suspend fun syncGroups(userId: String) {
        try {
            // Mencari semua grup di Firestore yang daftar anggotanya mengandung ID user ini
            val snapshot = groupsRef.whereArrayContains("members", userId).get().await()
            
            // Memasukkan setiap grup yang ditemukan ke dalam penyimpanan lokal (Room)
            snapshot.documents.forEach { doc ->
                doc.toGroupEntity()?.let { dao.upsert(it) }
            }
        } catch (e: Exception) {
            // Jika gagal sync, catat saja tanpa mematikan aplikasi
            e.printStackTrace()
        }
    }
}

/**
 * Alat bantu (Extension) untuk menerjemahkan data Kotlin (Entity) ke format yang dimengerti Firestore (Map).
 * Alasan: Firestore menyimpan daftar anggota sebagai 'Array' sungguhan, bukan teks JSON.
 */
fun GroupEntity.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "name" to name,
        "description" to description,
        "owner_id" to ownerId,
        "members" to getMembersList(), // Membongkar JSON lokal menjadi List asli untuk dikirim ke Cloud
        "budget" to budget,
        "icon" to icon,
        "invite_code" to inviteCode,
        "created_at" to createdAt
    )
}

/**
 * Alat bantu untuk mengubah dokumen kiriman Cloud (DocumentSnapshot) kembali menjadi format formulir HP kita.
 */
private fun com.google.firebase.firestore.DocumentSnapshot.toGroupEntity(): GroupEntity? {
    return try {
        // Langkah 1: Ambil data array anggota dari Firestore
        @Suppress("UNCHECKED_CAST")
        val membersList = get("members") as? List<String> ?: emptyList()
        
        // Langkah 2: Ubah list tersebut menjadi satu baris teks JSON agar bisa disimpan di Room Database
        val membersJson = Gson().toJson(membersList)

        // Langkah 3: Masukkan semua potongan data ke dalam objek GroupEntity
        GroupEntity(
            id = id, // ID dokumen Firestore
            name = getString("name") ?: "",
            description = getString("description") ?: "",
            ownerId = getString("owner_id") ?: "",
            membersJson = membersJson, // Hasil konversi list ke teks
            budget = getDouble("budget") ?: 0.0,
            icon = getString("icon") ?: "",
            inviteCode = getString("invite_code") ?: "",
            createdAt = getLong("created_at") ?: 0L
        )
    } catch (e: Exception) {
        null // Jika data dari internet rusak, abaikan dokumen tersebut
    }
}
