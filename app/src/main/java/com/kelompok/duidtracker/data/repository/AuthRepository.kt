package com.kelompok.duidtracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

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
        return when (errorCode) {
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Email sudah terdaftar"
            "ERROR_WRONG_PASSWORD" -> "Password salah"
            "ERROR_USER_NOT_FOUND" -> "Akun tidak ditemukan"
            "ERROR_INVALID_EMAIL" -> "Format email tidak valid"
            "ERROR_WEAK_PASSWORD" -> "Password terlalu lemah"
            "ERROR_USER_DISABLED" -> "Akun ini telah dinonaktifkan"
            else -> "Terjadi kesalahan: $errorCode"
        }
    }
}
