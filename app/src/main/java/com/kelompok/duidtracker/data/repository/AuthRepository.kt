package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth // Import library untuk mengelola pendaftaran dan login akun
import com.google.firebase.auth.FirebaseAuthException // Import untuk menangkap error khusus dari sistem login Firebase
import com.google.firebase.firestore.FirebaseFirestore // Import library database cloud untuk menyimpan profil user
import kotlinx.coroutines.tasks.await // Import alat untuk membuat proses Firebase yang tadinya ribet jadi simpel (linear)

/**
 * Kelas pembungkus hasil (State Wrapper).
 * Analogi: Seperti amplop hasil diagnosa dokter; isinya bisa Berhasil, Gagal, atau sedang diperiksa (Loading).
 */
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>() // Kotak yang berisi data jika operasi berhasil
    data class Error(val message: String) : AuthResult<Nothing>() // Kotak berisi pesan jika terjadi kesalahan
    object Loading : AuthResult<Nothing>() // Penanda bahwa aplikasi sedang bekerja di latar belakang
}

/**
 * Repository untuk urusan Autentikasi.
 * Analogi: Ini adalah "Manajer Personalia" yang mengurus pembuatan ID Card karyawan (User) dan absensi masuk.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth, // Alat komunikasi ke bagian login cloud
    private val firestore: FirebaseFirestore // Alat komunikasi ke bagian gudang profil cloud
) {

    /**
     * Mendaftarkan pengguna baru (Registrasi).
     * Alur: Buat akun login dulu -> Jika sukses, buat dokumen profil (Nama, dll).
     */
    suspend fun register(name: String, email: String, password: String): AuthResult<Unit> {
        return try {
            // Langkah 1: Meminta Firebase membuat akun email & password. .await() artinya tunggu sampai selesai.
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            
            // Langkah 2: Mengambil ID unik (UID) yang diberikan Firebase untuk user baru ini
            val userId = result.user?.uid ?: throw Exception("Gagal mendapatkan User ID")

            // Langkah 3: Menyiapkan formulir profil untuk disimpan di database Firestore
            val userMap = mapOf(
                "userId" to userId, // Mencatat ID unik user
                "name" to name, // Mencatat nama lengkap yang diketik di layar
                "email" to email, // Mencatat alamat email
                "createdAt" to System.currentTimeMillis() // Mencatat waktu detik saat pendaftaran
            )

            // Langkah 4: Masuk ke koleksi "users", buat laci bernama ID user, lalu taruh formulir tadi di sana
            firestore.collection("users").document(userId).set(userMap).await()
            
            // Mengembalikan status Sukses
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            // Jika Firebase menolak (misal: email sudah ada), terjemahkan kodenya ke Bahasa Indonesia
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            // Jika ada masalah lain (misal: internet mati), tampilkan pesan umum
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat pendaftaran")
        }
    }

    /**
     * Logika untuk masuk ke akun (Login).
     */
    suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            // Mencocokkan email dan password di server pusat Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            // Jika cocok, berikan status sukses
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            // Jika tidak cocok, beri tahu alasannya (misal: "Password salah")
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat login")
        }
    }

    /**
     * Logika untuk keluar (Logout).
     * Analogi: Membuang kartu akses sementara agar orang lain tidak bisa masuk HP ini.
     */
    fun logout() {
        firebaseAuth.signOut() // Memerintahkan Firebase untuk menghapus sesi aktif
    }

    /**
     * Mengambil ID unik pengguna yang sedang aktif saat ini.
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid // Jika tidak ada yang login, hasilnya adalah null
    }

    /**
     * Mengecek apakah ada pengguna yang masih login di HP ini.
     */
    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null // True jika ada user aktif, False jika kosong
    }

    /**
     * Fungsi rahasia untuk menerjemahkan bahasa "Mesin" Firebase ke bahasa manusia.
     */
    private fun mapFirebaseAuthError(errorCode: String): String {
        return when (errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar, silakan gunakan email lain"
            "ERROR_WRONG_PASSWORD" -> "Kata sandi yang Anda masukkan salah"
            "ERROR_USER_NOT_FOUND" -> "Akun dengan email ini tidak ditemukan"
            "ERROR_INVALID_EMAIL" -> "Format email yang Anda masukkan tidak valid"
            "ERROR_WEAK_PASSWORD" -> "Kata sandi terlalu lemah (minimal 6 karakter)"
            "ERROR_USER_DISABLED" -> "Maaf, akun ini telah dinonaktifkan oleh sistem"
            else -> "Terjadi gangguan sistem: $errorCode" // Pesan cadangan jika ada error asing
        }
    }
}
