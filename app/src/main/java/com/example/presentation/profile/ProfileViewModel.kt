package com.example.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.MockData
import com.example.data.Post
import com.example.data.local.entity.PostEntity
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.domain.model.User
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val currentUser: StateFlow<User?> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(null)
        else {
            userRepository.allUsers.map { users ->
                val entity = users.find { it.id == userId }
                entity?.let {
                    User(
                        id = it.id,
                        username = it.username,
                        name = it.bio,
                        profilePicUrl = it.profilePicUrl,
                        followersCount = it.followersCount,
                        followingCount = it.followingCount
                    )
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userPosts: StateFlow<List<Post>> = authRepository.currentUserId.flatMapLatest { userId ->
        if (userId == null) flowOf(emptyList())
        else {
            combine(postRepository.allPosts, userRepository.allUsers) { posts, users ->
                val currentEntity = users.find { it.id == userId }
                posts.filter { it.userId == userId }.map { entity ->
                    val u = if (currentEntity != null) {
                        com.example.data.User(currentEntity.id, currentEntity.username, currentEntity.profilePicUrl, currentEntity.bio)
                    } else {
                        com.example.data.User(userId, "unknown", "", "Unknown")
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
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                ProfileViewModel(
                    application.container.userRepository,
                    application.container.postRepository,
                    application.container.authRepository
                )
            }
        }
    }
}
