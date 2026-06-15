package com.example.data

import com.example.data.local.entity.CommentEntity
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.PostEntity
import com.example.data.local.entity.UserEntity
import com.example.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LocalDataBootstrapper(private val container: AppContainer) {

    fun bootstrapDataIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = container.database
            val usersInDb = db.userDao().getAllUsers().first()
            if (usersInDb.isEmpty()) {
                val dbUsers = MockData.users.map {
                    UserEntity(
                        id = it.id,
                        username = it.username,
                        bio = it.fullName,
                        profilePicUrl = it.profilePicUrl,
                        followersCount = 1200,
                        followingCount = 300
                    )
                } + UserEntity(
                    id = MockData.currentUser.id,
                    username = MockData.currentUser.username,
                    bio = MockData.currentUser.fullName,
                    profilePicUrl = MockData.currentUser.profilePicUrl,
                    followersCount = 200,
                    followingCount = 150
                )
                db.userDao().insertUsers(dbUsers)

                val dbPosts = MockData.posts.map {
                    PostEntity(
                        id = it.id,
                        userId = it.user.id,
                        imageUrl = it.imageUrl,
                        caption = it.caption,
                        likesCount = it.likesCount,
                        commentsCount = it.commentsCount,
                        timeAgo = it.timeAgo,
                        isLiked = it.isLiked,
                        isSaved = it.isSaved
                    )
                }
                db.postDao().insertPosts(dbPosts)
                
                val dbComments = MockData.comments.map {
                    CommentEntity(
                        id = it.id,
                        postId = it.postId,
                        userId = it.user.id,
                        text = it.text,
                        timeAgo = it.timeAgo
                    )
                }
                dbComments.forEach { db.commentDao().insertComment(it) }
                                
            }
        }
    }
}
