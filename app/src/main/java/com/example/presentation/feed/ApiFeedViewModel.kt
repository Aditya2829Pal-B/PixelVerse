package com.example.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.Post
import com.example.data.User
import com.example.data.remote.PicsumApiService
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ApiFeedViewModel(private val apiService: PicsumApiService) : ViewModel() {
    private val _feedPosts = MutableStateFlow<List<Post>>(emptyList())
    val feedPosts: StateFlow<List<Post>> = _feedPosts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPage = 1

    init {
        loadMorePosts()
    }

    fun loadMorePosts() {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val images = apiService.getImages(page = currentPage, limit = 10)
                val newPosts = images.map { image ->
                    Post(
                        id = image.id,
                        user = User(
                            id = image.author,
                            username = image.author.replace(" ", "").lowercase(),
                            profilePicUrl = "https://picsum.photos/seed/${image.author}/150",
                            fullName = image.author
                        ),
                        imageUrl = image.download_url,
                        caption = "Beautiful capture by ${image.author} \uD83D\uDCF8",
                        likesCount = (10..500).random(),
                        commentsCount = (0..50).random(),
                        timeAgo = "${(1..24).random()}h",
                        isLiked = false,
                        isSaved = false
                    )
                }
                _feedPosts.update { current -> current + newPosts }
                currentPage++
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(postId: String, currentStatus: Boolean) {
        _feedPosts.update { current ->
            current.map { post ->
                if (post.id == postId) {
                    post.copy(
                        isLiked = !currentStatus,
                        likesCount = post.likesCount + if (!currentStatus) 1 else -1
                    )
                } else {
                    post
                }
            }
        }
    }

    fun refreshFeed(onComplete: () -> Unit) {
        currentPage = 1
        _feedPosts.value = emptyList()
        viewModelScope.launch {
            loadMorePosts()
            onComplete()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                ApiFeedViewModel(application.container.picsumApiService)
            }
        }
    }
}
