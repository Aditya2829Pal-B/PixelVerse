package com.example.data.mapper

import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.UserEntity
import com.example.domain.model.Post
import com.example.domain.model.User

fun UserEntity.toDomainModel(): User {
    return User(
        id = id,
        username = username,
        name = bio,
        profilePicUrl = profilePicUrl,
        followersCount = followersCount,
        followingCount = followingCount
    )
}

fun PostEntity.toDomainModel(user: User): Post {
    return Post(
        id = id,
        user = user,
        imageUrl = imageUrl,
        caption = caption,
        likesCount = likesCount,
        commentsCount = commentsCount,
        timeAgo = timeAgo,
        isLiked = isLiked,
        isSaved = isSaved
    )
}
