package com.example.data.repository

import com.example.data.local.dao.PostDao
import com.example.data.local.entity.PostEntity
import kotlinx.coroutines.flow.Flow

class PostRepository(private val postDao: PostDao) {
    val allPosts: Flow<List<PostEntity>> = postDao.getAllPosts()
    
    suspend fun insertPost(post: PostEntity) {
        postDao.insertPost(post)
    }

    suspend fun insertPosts(posts: List<PostEntity>) {
        postDao.insertPosts(posts)
    }
    
    suspend fun toggleLike(postId: String, isLiked: Boolean) {
        val increment = if (isLiked) 1 else -1
        postDao.updatePostLikeStatus(postId, isLiked, increment)
    }
}
