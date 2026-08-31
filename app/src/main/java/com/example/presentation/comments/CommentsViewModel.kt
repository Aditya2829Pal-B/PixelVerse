package com.example.presentation.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.entity.CommentEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.PostRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class CommentsViewModel(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository,
    private val postId: String
) : ViewModel() {

    private val _comments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val comments: StateFlow<List<CommentEntity>> = _comments.asStateFlow()

    private val _currentUser = MutableStateFlow<com.example.data.User?>(null)
    val currentUser: StateFlow<com.example.data.User?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            postRepository.getCommentsForPost(postId).collect { commentsList ->
                _comments.value = commentsList
            }
        }
        
        viewModelScope.launch {
            val uid = authRepository.currentUserId.firstOrNull()
            if (uid != null) {
                val db = Firebase.firestore
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val user = com.example.data.User(
                        id = uid,
                        username = doc.getString("username") ?: "User",
                        profilePicUrl = doc.getString("profilePicUrl") ?: "",
                        fullName = doc.getString("bio") ?: ""
                    )
                    _currentUser.value = user
                }
            }
        }
    }

    fun addComment(text: String) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val comment = CommentEntity(
                id = "comment_${System.currentTimeMillis()}",
                postId = postId,
                userId = user.id,
                username = user.username,
                profilePicUrl = user.profilePicUrl,
                text = text,
                timeAgo = "Just now",
                timestamp = System.currentTimeMillis()
            )
            postRepository.addComment(comment)
        }
    }

    companion object {
        fun Factory(postId: String) = viewModelFactory {
            initializer {
                val app = pixelVerseApplication()
                CommentsViewModel(
                    postRepository = app.container.postRepository,
                    authRepository = app.container.authRepository,
                    postId = postId
                )
            }
        }
    }
}
