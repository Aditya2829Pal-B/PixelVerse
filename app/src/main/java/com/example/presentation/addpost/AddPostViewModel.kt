package com.example.presentation.addpost

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.MockData
import com.example.data.local.entity.PostEntity
import com.example.data.repository.PostRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.launch

import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class AddPostViewModel(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    fun createPost(mediaUri: String, caption: String, mediaType: String = "IMAGE", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val userId = authRepository.currentUserId.firstOrNull() ?: "current_user"
                
                // Upload the media to Firebase Storage first (falls back to local uri if offline/error)
                val uploadedUrl = try {
                    postRepository.uploadImage(android.net.Uri.parse(mediaUri))
                } catch (e: Exception) {
                    e.printStackTrace()
                    mediaUri
                }
                
                val newPost = PostEntity(
                    id = "post_${System.currentTimeMillis()}",
                    userId = userId,
                    imageUrl = uploadedUrl, // we use imageUrl field for both image and video URLs for simplicity
                    caption = caption,
                    likesCount = 0,
                    commentsCount = 0,
                    timeAgo = "Just now",
                    isLiked = false,
                    isSaved = false,
                    mediaType = mediaType
                )
                postRepository.insertPost(newPost)
                onComplete()
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                AddPostViewModel(
                    application.container.postRepository,
                    application.container.authRepository
                )
            }
        }
    }
}
