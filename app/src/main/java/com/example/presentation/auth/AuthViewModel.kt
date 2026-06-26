package com.example.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.repository.AuthRepository
import com.example.utils.pixelVerseApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val currentUserId: StateFlow<String?> = authRepository.currentUserId
    
    // For signaling loading / error states
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun loginWithEmail(email: String, password: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val success = authRepository.loginWithEmail(email, password)
            if (!success) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to sign in. Check credentials and google-services.json.") }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signupWithEmail(email: String, password: String, username: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val success = authRepository.signupWithEmail(email, password, username)
            if (!success) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to sign up. Check credentials and google-services.json.") }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loginWithGoogle(idToken: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val success = authRepository.loginWithGoogle(idToken)
            if (!success) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed to sign in with Google") }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = pixelVerseApplication()
                AuthViewModel(application.container.authRepository)
            }
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
