package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth // Library untuk mengelola akun (Email/Password)
import com.google.firebase.auth.FirebaseAuthException // Class khusus untuk menangkap error dari Firebase Auth
import com.google.firebase.firestore.FirebaseFirestore // Library untuk menyimpan data profil di cloud (NoSQL)
import kotlinx.coroutines.tasks.await // Mengubah sistem callback Firebase menjadi gaya linear (Coroutines)

/**
 * Kelas pembungkus hasil operasi (State Wrapper).
 * Analogi: Seperti amplop laporan hasil kerja. Isinya bisa Berhasil (Success), Gagal (Error), atau Masih Diproses (Loading).
 */
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>() // Jika data berhasil didapat
    data class Error(val message: String) : AuthResult<Nothing>() // Jika terjadi gangguan/error
    object Loading : AuthResult<Nothing>() // Jika proses masih berjalan di background
}

/**
 * Repository untuk menangani segala urusan Autentikasi dan Profil User.
 * Analogi: Seperti "Manajer Personalia" yang mengurus pendaftaran karyawan baru dan absensi (login).
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth, // Alat untuk urusan login
    private val firestore: FirebaseFirestore // Alat untuk simpan biodata user
) {

    /**
     * Mendaftarkan pengguna baru dengan dua tahap (Double-Write).
     * 1. Buat akun di Firebase Auth.
     * 2. Simpan data profil (nama, email) ke Firestore.
     */
    suspend fun register(name: String, email: String, password: String): AuthResult<Unit> {
        return try {
            // Tahap 1: Meminta Firebase membuat akun baru. .await() menunggu sampai selesai.
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            // Mengambil ID unik (UID) yang diberikan Firebase untuk user baru ini
            val userId = result.user?.uid ?: throw Exception("Gagal mendapatkan User ID")

            // Menyiapkan data profil untuk disimpan ke database cloud
            val userMap = mapOf(
                "userId" to userId,
                "name" to name,
                "email" to email,
                "createdAt" to System.currentTimeMillis() // Mencatat waktu join
            )

            // Tahap 2: Menyimpan data profil ke koleksi "users" di Firestore dengan nama dokumen = ID user
            firestore.collection("users").document(userId).set(userMap).await()
            
            // Mengirim kabar sukses
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            // Jika Firebase menolak (misal: email sudah ada), ubah kode error jadi bahasa manusia
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            // Jika ada masalah lain (misal: internet mati)
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat pendaftaran")
        }
    }

    /**
     * Melakukan proses masuk (Sign In).
     * Analogi: Mengecek kunci (password) dan gembok (email) di gudang data cloud.
     */
    suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            // Meminta Firebase mengecek kecocokan email dan password
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            // Memberitahu user kenapa login gagal (misal: password salah)
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat login")
        }
    }

    /**
     * Menghapus sesi login (Sign Out).
     * Analogi: Menghapus "cookie" atau sesi agar orang lain tidak bisa masuk tanpa password lagi.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    /**
     * Mengambil ID unik user yang sedang login saat ini.
     * Mengembalikan null jika tidak ada user yang aktif.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Mengecek apakah ada pengguna yang sedang login di HP ini.
     * Analogi: Mengecek apakah ada "KTP" yang masih terselip di dompet aplikasi.
     */
    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Fungsi helper untuk menerjemahkan bahasa "Robot" Firebase ke Bahasa Indonesia.
     */
    private fun mapFirebaseAuthError(errorCode: String): String {
        return when (errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar"
            "ERROR_WRONG_PASSWORD" -> "Password salah"
            "ERROR_USER_NOT_FOUND" -> "Akun tidak ditemukan"
            "ERROR_INVALID_EMAIL" -> "Format email tidak valid"
            "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah (min 6 karakter)"
            "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan"
            else -> "Terjadi kesalahan: $errorCode"
        }
    }
}
