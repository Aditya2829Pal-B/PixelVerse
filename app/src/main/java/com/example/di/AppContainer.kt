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

import com.example.data.remote.PicsumApiService
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

import com.google.firebase.storage.storage

interface AppContainer {
    val database: AppDatabase
    val postRepository: PostRepository
    val userRepository: UserRepository
    val authRepository: AuthRepository
    val picsumApiService: PicsumApiService
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
        PostRepository(Firebase.firestore, Firebase.storage)
    }
    
    override val userRepository: UserRepository by lazy {
        UserRepository(database.userDao())
    }
    
    override val authRepository: AuthRepository by lazy {
        AuthRepository(Firebase.auth, Firebase.firestore)
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://picsum.photos/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    override val picsumApiService: PicsumApiService by lazy {
        retrofit.create(PicsumApiService::class.java)
    }
}
