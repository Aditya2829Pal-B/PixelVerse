package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    val allUsers: Flow<List<UserEntity>> = callbackFlow {
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val users = snapshot.documents.mapNotNull { it.toObject(UserEntity::class.java) }
                    trySend(users)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun getUserById(id: String): UserEntity? {
        return try {
            val doc = firestore.collection("users").document(id).get().await()
            doc.toObject(UserEntity::class.java)
        } catch (e: Exception) {
            userDao.getUserById(id)
        }
    }

    suspend fun insertUser(user: UserEntity) {
        firestore.collection("users").document(user.id).set(user).await()
    }
    
    suspend fun toggleFollowUser(currentUserId: String, targetUserId: String, isFollowing: Boolean) {
        val incrementCurrent = if (isFollowing) -1L else 1L
        val incrementTarget = if (isFollowing) -1L else 1L
        
        // Use batch to update both users atomically
        val batch = firestore.batch()
        
        val currentUserRef = firestore.collection("users").document(currentUserId)
        batch.update(currentUserRef, "followingCount", FieldValue.increment(incrementCurrent))
        
        val targetUserRef = firestore.collection("users").document(targetUserId)
        batch.update(targetUserRef, "followersCount", FieldValue.increment(incrementTarget))
        
        // In a real app we would also manage a 'followers' subcollection
        
        batch.commit().await()
    }

    suspend fun updateUserProfile(userId: String, username: String, bio: String, picUrl: String) {
        val userRef = firestore.collection("users").document(userId)
        userRef.update(
            mapOf(
                "username" to username,
                "bio" to bio,
                "profilePicUrl" to picUrl
            )
        ).await()
    }
}
