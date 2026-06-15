package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class AuthRepository(
    private val userDao: UserDao,
    private val context: Context
) {
    private val loggedInUserIdKey = stringPreferencesKey("logged_in_user_id")

    val currentUserId: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[loggedInUserIdKey]
    }

    suspend fun login(username: String, passwordHash: String): Boolean {
        val user = userDao.getUserByUsername(username)
        if (user != null && user.passwordHash == passwordHash) {
            context.dataStore.edit { prefs ->
                prefs[loggedInUserIdKey] = user.id
            }
            return true
        }
        return false
    }

    suspend fun signup(username: String, passwordHash: String): Boolean {
        if (userDao.getUserByUsername(username) != null) return false
        
        val newId = UUID.randomUUID().toString()
        val user = UserEntity(
            id = newId,
            username = username,
            bio = "New User",
            profilePicUrl = "https://picsum.photos/seed/$newId/150/150",
            followersCount = 0,
            followingCount = 0,
            passwordHash = passwordHash
        )
        userDao.insertUser(user)
        context.dataStore.edit { prefs ->
            prefs[loggedInUserIdKey] = newId
        }
        return true
    }

    suspend fun logout() {
        context.dataStore.edit { prefs ->
            prefs.remove(loggedInUserIdKey)
        }
    }
}
