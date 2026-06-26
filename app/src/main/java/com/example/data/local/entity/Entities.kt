package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val bio: String,
    val profilePicUrl: String,
    val followersCount: Int,
    val followingCount: Int,
    val passwordHash: String = ""
)

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val timeAgo: String = "",
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val userId: String,
    val text: String,
    val timeAgo: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timeAgo: String,
    val isMine: Boolean
)
