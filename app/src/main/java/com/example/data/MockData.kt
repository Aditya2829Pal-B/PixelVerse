package com.example.data

data class User(
    val id: String,
    val username: String,
    val profilePicUrl: String,
    val fullName: String = ""
)

data class Snaply(
    val id: String,
    val user: User,
    val isViewed: Boolean = false
)

data class Post(
    val id: String,
    val user: User,
    val imageUrl: String,
    val caption: String,
    val likesCount: Int,
    val commentsCount: Int,
    val timeAgo: String,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false
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
    val text: String = "",
    val imageUrl: String? = null,
    val timeAgo: String
)

data class Chat(
    val id: String,
    val participants: List<User>,
    val messages: List<Message>,
    val unreadCount: Int = 0
)

object MockData {
    val users = listOf(
        User("1", "zuck", "https://picsum.photos/seed/zuck/150/150", "Mark Zuckerberg"),
        User("2", "mosseri", "https://picsum.photos/seed/mosseri/150/150", "Adam Mosseri"),
        User("3", "natgeo", "https://picsum.photos/seed/natgeo/150/150", "National Geographic"),
        User("4", "nasa", "https://picsum.photos/seed/nasa/150/150", "NASA"),
        User("5", "cristiano", "https://picsum.photos/seed/cristiano/150/150", "Cristiano Ronaldo"),
        User("6", "therock", "https://picsum.photos/seed/therock/150/150", "Dwayne Johnson")
    )
    
    val currentUser = User("0", "instaclone_user", "https://picsum.photos/seed/me/150/150", "InstaClone User")

    val snaplies = users.mapIndexed { index, user ->
        Snaply(
            id = "snaply_$index",
            user = user,
            isViewed = index % 3 == 0
        )
    }

    val posts = androidx.compose.runtime.mutableStateListOf<Post>().apply {
        addAll(users.mapIndexed { index, user ->
            Post(
                id = "post_$index",
                user = user,
                imageUrl = "https://picsum.photos/seed/post$index/600/600",
                caption = listOf("What a beautiful day! ☀️", "Exploring the world \uD83C\uDF0D", "Coding time \uD83D\uDCBB", "Just relaxing...", "Check this out!", "Amazing view \uD83D\uDE0D").random(),
                likesCount = (100..10000).random(),
                commentsCount = (5..500).random(),
                timeAgo = "${(1..23).random()}h",
                isLiked = index % 2 == 0,
                isSaved = index % 4 == 0
            )
        }.let { it + it + it }.mapIndexed { i, p -> p.copy(id = "post_dup_$i", imageUrl = "https://picsum.photos/seed/dup$i/600/600") })
    }
    
    val comments = androidx.compose.runtime.mutableStateListOf<Comment>().apply {
        addAll(posts.take(10).mapIndexed { i, p ->
            Comment("comment_$i", p.id, users.random(), "Great post! 🔥", "1h")
        })
    }
    
    val exploreImages = (1..30).map { "https://picsum.photos/seed/explore$it/400/400" }
    val profileImages = androidx.compose.runtime.mutableStateListOf<String>().apply {
        addAll((1..15).map { "https://picsum.photos/seed/profile$it/400/400" })
    }

    val chats = listOf(
        Chat(
            id = "chat_0",
            participants = listOf(users[0]),
            messages = listOf(
                Message("m1", users[0], "Hey, how are you?", null, "2h"),
                Message("m2", currentUser, "Good, you?", null, "1h"),
                Message("m3", users[0], "Doing great! Check this out", "https://picsum.photos/seed/chat0/400/400", "30m")
            ),
            unreadCount = 1
        ),
        Chat(
            id = "chat_1",
            participants = listOf(users[1]),
            messages = listOf(
                Message("m4", users[1], "New features are coming soon!", null, "5h")
            ),
            unreadCount = 0
        ),
        Chat(
            id = "chat_2",
            participants = listOf(users[2], users[3]), // group chat
            messages = listOf(
                Message("m5", users[2], "Beautiful nature shot!", null, "1d"),
                Message("m6", users[3], "Indeed, looks like mars.", null, "23h")
            ),
            unreadCount = 2
        )
    )
}
