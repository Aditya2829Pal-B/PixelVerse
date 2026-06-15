package com.example.presentation.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.PostRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel(private val postRepository: PostRepository) : ViewModel() {

    val exploreImages: StateFlow<List<String>> = postRepository.allPosts.map { entities ->
        entities.map { it.imageUrl }.shuffled() // Shuffle for discoverability in explore
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                ExploreViewModel(application.container.postRepository)
            }
        }
    }
}
