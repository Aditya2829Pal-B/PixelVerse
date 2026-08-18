package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "snaplies")
data class SnaplyEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val imageUrl: String,
    val timestamp: Long
)
