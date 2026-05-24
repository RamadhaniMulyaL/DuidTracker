package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Sealed class untuk membungkus hasil operasi Auth.
 */
sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

/**
 * Repository untuk menangani autentikasi pengguna.
 */
class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun register(name: String, email: String, password: String): AuthResult<Unit> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = result.user?.uid ?: throw Exception("Gagal mendapatkan User ID")

            val userMap = mapOf(
                "userId" to userId,
                "name" to name,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(userId).set(userMap).await()
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat pendaftaran")
        }
    }

    suspend fun login(email: String, password: String): AuthResult<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            AuthResult.Success(Unit)
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(mapFirebaseAuthError(e.errorCode))
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Terjadi kesalahan saat login")
        }
    }

    /**
     * Keluar dari akun.
     * Membersihkan sesi Firebase Auth.
     */
    fun logout() {
        firebaseAuth.signOut()
    }

    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    private fun mapFirebaseAuthError(errorCode: String): String {
        return when (errorCode.uppercase()) {
            "ERROR_EMAIL_ALREADY_IN_USE", "EMAIL_ALREADY_IN_USE", "ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "Email sudah terdaftar"
            "ERROR_WRONG_PASSWORD", "WRONG_PASSWORD" -> "Password salah"
            "ERROR_USER_NOT_FOUND", "USER_NOT_FOUND" -> "Akun tidak ditemukan"
            "ERROR_INVALID_EMAIL", "INVALID_EMAIL" -> "Format email tidak valid"
            "ERROR_INVALID_CREDENTIAL", "INVALID_CREDENTIAL" -> "Email atau password salah"
            "ERROR_WEAK_PASSWORD", "WEAK_PASSWORD" -> "Password terlalu lemah"
            "ERROR_USER_DISABLED", "USER_DISABLED" -> "Akun ini telah dinonaktifkan"
            else -> "Terjadi kesalahan: $errorCode"
        }
    }
}
