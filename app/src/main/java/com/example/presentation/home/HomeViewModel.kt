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

import com.example.data.MockData
import com.example.data.Snaply
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import com.example.data.local.entity.SnaplyEntity

class HomeViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _viewedSnaplyIds = MutableStateFlow<Set<String>>(emptySet())
    val viewedSnaplyIds: StateFlow<Set<String>> = _viewedSnaplyIds.asStateFlow()

    fun markSnaplyAsViewed(snaplyId: String) {
        _viewedSnaplyIds.update { it + snaplyId }
    }

    val feedSnaplies: StateFlow<List<Snaply>> = combine(
        postRepository.allSnaplies,
        userRepository.allUsers
    ) { snaplies, users ->
        val firestoreList = snaplies.map { entity ->
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
        }.sortedByDescending { it.id }

        if (firestoreList.isEmpty()) {
            MockData.snaplies
        } else {
            firestoreList + MockData.snaplies.filter { mock -> firestoreList.none { it.id == mock.id } }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MockData.snaplies
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

    fun seedSamplePosts() {
        viewModelScope.launch {
            val samplePosts = listOf(
                PostEntity(
                    id = "post_sample_vid_1",
                    userId = "1",
                    imageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    caption = "Incredible tech showcase video! 🎥 Watch in full HD #tech #exoplayer",
                    likesCount = 1240,
                    commentsCount = 89,
                    timeAgo = "2h",
                    isLiked = false,
                    isSaved = false,
                    mediaType = "VIDEO"
                ),
                PostEntity(
                    id = "post_sample_img_1",
                    userId = "2",
                    imageUrl = "https://picsum.photos/seed/travel_post/800/800",
                    caption = "Golden hour vibes in the mountains 🏔️✨ #wanderlust #sunset",
                    likesCount = 3450,
                    commentsCount = 142,
                    timeAgo = "4h",
                    isLiked = true,
                    isSaved = false,
                    mediaType = "IMAGE"
                ),
                PostEntity(
                    id = "post_sample_vid_2",
                    userId = "3",
                    imageUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    caption = "3D open-source animation masterpiece! 🐘🎬 Turn sound on! 🔊",
                    likesCount = 2890,
                    commentsCount = 210,
                    timeAgo = "6h",
                    isLiked = false,
                    isSaved = true,
                    mediaType = "VIDEO"
                ),
                PostEntity(
                    id = "post_sample_img_2",
                    userId = "4",
                    imageUrl = "https://picsum.photos/seed/architecture/800/800",
                    caption = "Futuristic urban architecture in Tokyo 🏙️ Captured at dusk.",
                    likesCount = 954,
                    commentsCount = 37,
                    timeAgo = "8h",
                    isLiked = false,
                    isSaved = false,
                    mediaType = "IMAGE"
                )
            )
            postRepository.insertPosts(samplePosts)
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
