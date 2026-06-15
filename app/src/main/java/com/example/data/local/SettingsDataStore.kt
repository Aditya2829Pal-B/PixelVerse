package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension property
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    // Keys for navigation preferences
    private val SHOW_HOME = booleanPreferencesKey("show_home")
    private val SHOW_SEARCH = booleanPreferencesKey("show_search")
    private val SHOW_ADD = booleanPreferencesKey("show_add")
    private val SHOW_SNAPLY = booleanPreferencesKey("show_snaply")
    private val SHOW_ACCOUNT = booleanPreferencesKey("show_account")

    val showHomeFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_HOME] ?: true
    }
    
    val showSearchFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SEARCH] ?: true
    }

    val showAddFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_ADD] ?: true
    }

    val showSnaplyFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SNAPLY] ?: true
    }

    val showAccountFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_ACCOUNT] ?: true
    }

    suspend fun saveShowHome(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_HOME] = show }
    }

    suspend fun saveShowSearch(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_SEARCH] = show }
    }

    suspend fun saveShowAdd(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_ADD] = show }
    }

    suspend fun saveShowSnaply(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_SNAPLY] = show }
    }

    suspend fun saveShowAccount(show: Boolean) {
        context.dataStore.edit { preferences -> preferences[SHOW_ACCOUNT] = show }
    }
}
