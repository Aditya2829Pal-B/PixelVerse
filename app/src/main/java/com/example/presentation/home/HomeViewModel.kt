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

import com.example.data.Snaply
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import com.example.data.local.entity.SnaplyEntity

class HomeViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val feedSnaplies: StateFlow<List<Snaply>> = combine(
        postRepository.allSnaplies,
        userRepository.allUsers
    ) { snaplies, users ->
        snaplies.map { entity ->
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
            Snaply(
                id = entity.id,
                user = u,
                isViewed = false,
                imageUrl = entity.imageUrl
            )
        }.sortedByDescending { it.id } // simple sort
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
                isSaved = entity.isSaved,
                mediaType = entity.mediaType
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
            onComplete()
        }
    }
    
    fun uploadSnaply(uriString: String) {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.firstOrNull() ?: return@launch
            
            try {
                val uploadedUrl = postRepository.uploadImage(android.net.Uri.parse(uriString))
                val newSnaply = SnaplyEntity(
                    id = "snaply_${System.currentTimeMillis()}",
                    userId = userId,
                    imageUrl = uploadedUrl,
                    timestamp = System.currentTimeMillis()
                )
                postRepository.insertSnaply(newSnaply)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                HomeViewModel(
                    application.container.postRepository,
                    application.container.userRepository,
                    application.container.authRepository
                )
            }
        }
    }
}
