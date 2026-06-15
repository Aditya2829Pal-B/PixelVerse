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
import kotlinx.coroutines.flow.firstOrNull

class AddPostViewModel(
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun createPost(imageUrl: String, caption: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.firstOrNull() ?: return@launch
            val newPost = PostEntity(
                id = "post_${System.currentTimeMillis()}",
                userId = userId,
                imageUrl = imageUrl,
                caption = caption,
                likesCount = 0,
                commentsCount = 0,
                timeAgo = "Just now",
                isLiked = false,
                isSaved = false
            )
            postRepository.insertPost(newPost)
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
