package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.entity.PostEntity
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.combine
import com.example.data.Post
import com.example.data.User

class HomeViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val feedPosts: StateFlow<List<Post>> = combine(
        postRepository.allPosts,
        userRepository.allUsers
    ) { posts, users ->
        posts.map { entity ->
            val uEntity = users.find { it.id == entity.userId }
            val u = if (uEntity != null) {
                User(
                    id = uEntity.id,
                    username = uEntity.username,
                    profilePicUrl = uEntity.profilePicUrl,
                    fullName = uEntity.bio
                )
            } else {
                User("0", "unknown", "https://picsum.photos/150", "Unknown")
            }
            Post(
                id = entity.id,
                user = u,
                imageUrl = entity.imageUrl,
                caption = entity.caption,
                likesCount = entity.likesCount,
                commentsCount = entity.commentsCount,
                timeAgo = entity.timeAgo,
                isLiked = entity.isLiked,
                isSaved = entity.isSaved
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
        
    fun toggleLike(postId: String, currentLikeStatus: Boolean) {
        viewModelScope.launch {
            postRepository.toggleLike(postId, !currentLikeStatus)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                HomeViewModel(
                    application.container.postRepository,
                    application.container.userRepository
                )
            }
        }
    }
}
