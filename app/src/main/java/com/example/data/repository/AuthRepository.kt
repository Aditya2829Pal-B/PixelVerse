package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    private val _currentUserId = MutableStateFlow<String?>(auth.currentUser?.uid)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserId.value = firebaseAuth.currentUser?.uid
        }
    }

    suspend fun loginWithGoogle(idToken: String): Boolean {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                // Ensure user exists in Firestore
                val userRef = firestore.collection("users").document(user.uid)
                val snapshot = userRef.get().await()
                if (!snapshot.exists()) {
                    val newUser = mapOf(
                        "id" to user.uid,
                        "username" to (user.displayName ?: "User"),
                        "profilePicUrl" to (user.photoUrl?.toString() ?: ""),
                        "bio" to "New User",
                        "followersCount" to 0,
                        "followingCount" to 0
                    )
                    userRef.set(newUser).await()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}
