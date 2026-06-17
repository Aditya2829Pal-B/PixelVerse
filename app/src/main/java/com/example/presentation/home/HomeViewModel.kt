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
        }.reversed()
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
    
    fun refreshFeed(onComplete: () -> Unit) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // Simulate network delay
            val newPost = PostEntity(
                id = java.util.UUID.randomUUID().toString(),
                userId = "2", // Using an existing dummy user
                imageUrl = "https://picsum.photos/400/400?random=${System.currentTimeMillis()}",
                caption = "Just refreshed my feed! \uD83D\uDE0A",
                likesCount = (10..500).random(),
                commentsCount = (0..50).random(),
                timeAgo = "Just now",
                isLiked = false,
                isSaved = false
            )
            postRepository.insertPost(newPost)
            onComplete()
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
