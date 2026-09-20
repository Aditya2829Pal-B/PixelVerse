package com.example.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.entity.UserEntity
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val exploreImages: StateFlow<List<String>> = postRepository.allPosts.map { entities ->
        entities.map { it.imageUrl }.shuffled() // Shuffle for discoverability in explore
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val searchResults: StateFlow<List<UserEntity>> = combine(
        userRepository.allUsers,
        _searchQuery
    ) { users, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            users.filter { 
                it.username.contains(query, ignoreCase = true) || 
                it.bio.contains(query, ignoreCase = true)
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
                ExploreViewModel(
                    application.container.postRepository,
                    application.container.userRepository
                )
            }
        }
    }
}
