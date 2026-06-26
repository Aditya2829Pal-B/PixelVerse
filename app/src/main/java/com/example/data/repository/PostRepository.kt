package com.example.data.repository

import com.example.data.local.entity.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository(private val firestore: FirebaseFirestore) {
    val allPosts: Flow<List<PostEntity>> = callbackFlow {
        val listener = firestore.collection("posts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { it.toObject(PostEntity::class.java) }
                    trySend(posts)
                }
            }
        awaitClose { listener.remove() }
    }
    
    suspend fun insertPost(post: PostEntity) {
        firestore.collection("posts").document(post.id).set(post).await()
    }

    suspend fun insertPosts(posts: List<PostEntity>) {
        val batch = firestore.batch()
        posts.forEach { post ->
            val ref = firestore.collection("posts").document(post.id)
            batch.set(ref, post)
        }
        batch.commit().await()
    }
    
    suspend fun toggleLike(postId: String, isLiked: Boolean) {
        val increment = if (isLiked) 1L else -1L
        firestore.collection("posts").document(postId)
            .update("likesCount", com.google.firebase.firestore.FieldValue.increment(increment),
                    "isLiked", isLiked).await()
    }
}

