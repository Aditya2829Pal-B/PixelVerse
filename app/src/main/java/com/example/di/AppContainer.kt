package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase

import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.AuthRepository

interface AppContainer {
    val database: AppDatabase
    val postRepository: PostRepository
    val userRepository: UserRepository
    val authRepository: AuthRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pixelverse_database"
        ).fallbackToDestructiveMigration().build()
    }
    
    override val postRepository: PostRepository by lazy {
        PostRepository(database.postDao())
    }
    
    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepository(database.userDao(), context)
    }
}
