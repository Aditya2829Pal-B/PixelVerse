package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase

import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

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
        PostRepository(Firebase.firestore)
    }
    
    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepository(Firebase.auth, Firebase.firestore)
    }
}
