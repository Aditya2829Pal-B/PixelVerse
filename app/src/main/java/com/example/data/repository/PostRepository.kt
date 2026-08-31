package com.example.data.repository

import android.net.Uri
import com.example.data.local.entity.PostEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

import com.example.data.local.entity.SnaplyEntity
import com.example.data.local.entity.CommentEntity

class PostRepository(private val firestore: FirebaseFirestore, private val storage: FirebaseStorage) {
    
    suspend fun uploadImage(uri: Uri): String {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("images/$fileName")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    val allSnaplies: Flow<List<SnaplyEntity>> = callbackFlow {
        val listener = firestore.collection("snaplies")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val snaplies = snapshot.documents.mapNotNull { it.toObject(SnaplyEntity::class.java) }
                    trySend(snaplies)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun insertSnaply(snaply: SnaplyEntity) {
        firestore.collection("snaplies").document(snaply.id).set(snaply).await()
    }

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

    fun getCommentsForPost(postId: String): Flow<List<CommentEntity>> = callbackFlow {
        val listener = firestore.collection("comments")
            .whereEqualTo("postId", postId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { it.toObject(CommentEntity::class.java) }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(comment: CommentEntity) {
        firestore.collection("comments").document(comment.id).set(comment).await()
        // Increment commentsCount in post
        firestore.collection("posts").document(comment.postId)
            .update("commentsCount", com.google.firebase.firestore.FieldValue.increment(1L)).await()
    }
}

