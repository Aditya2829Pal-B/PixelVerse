package com.example.domain.model

data class User(
    val id: String,
    val username: String,
    val name: String,
    val profilePicUrl: String,
    val followersCount: Int,
    val followingCount: Int
)

data class Post(
    val id: String,
    val user: User,
    val imageUrl: String,
    val caption: String,
    val likesCount: Int,
    val commentsCount: Int,
    val timeAgo: String,
    val isLiked: Boolean,
    val isSaved: Boolean
)

data class Comment(
    val id: String,
    val postId: String,
    val user: User,
    val text: String,
    val timeAgo: String
)

data class Message(
    val id: String,
    val sender: User,
    val text: String,
    val timeAgo: String,
    val isMine: Boolean
)

data class Snaply(
    val id: String,
    val user: User,
    val isViewed: Boolean
)
