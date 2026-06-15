package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun getUserById(id: String): UserEntity? {
        return userDao.getUserById(id)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
}
