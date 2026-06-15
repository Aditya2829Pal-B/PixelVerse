package com.example.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    val showHome: StateFlow<Boolean> = dataStore.showHomeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showSearch: StateFlow<Boolean> = dataStore.showSearchFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showAdd: StateFlow<Boolean> = dataStore.showAddFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showSnaply: StateFlow<Boolean> = dataStore.showSnaplyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val showAccount: StateFlow<Boolean> = dataStore.showAccountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    fun toggleHome(show: Boolean) = viewModelScope.launch { dataStore.saveShowHome(show) }
    fun toggleSearch(show: Boolean) = viewModelScope.launch { dataStore.saveShowSearch(show) }
    fun toggleAdd(show: Boolean) = viewModelScope.launch { dataStore.saveShowAdd(show) }
    fun toggleSnaply(show: Boolean) = viewModelScope.launch { dataStore.saveShowSnaply(show) }
    fun toggleAccount(show: Boolean) = viewModelScope.launch { dataStore.saveShowAccount(show) }
}
