package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Post
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Checks if a given media item is a video based on its declared type or file extension.
 */
fun isVideoMedia(mediaType: String, url: String): Boolean {
    val cleanUrl = url.lowercase()
    return mediaType.equals("VIDEO", ignoreCase = true) ||
            cleanUrl.endsWith(".mp4") ||
            cleanUrl.endsWith(".mov") ||
            cleanUrl.endsWith(".mkv") ||
            cleanUrl.endsWith(".webm") ||
            cleanUrl.contains("/video")
}

/**
 * A vertical scrolling feed component that displays posts from Firestore,
 * supporting both images (via Coil) and video playback (via ExoPlayer / Media3).
 *
 * Includes:
 * - Double-tap to like gesture with animated pop-up heart.
 * - Single-tap video play/pause and mute/unmute audio control.
 * - Loading placeholder and error recovery via Coil SubcomposeAsyncImage.
 * - Real-time optimistic like/unlike state synchronization.
 * - Empty state with quick sample seeding for immediate testing.
 */
@Composable
fun VerticalFeedComponent(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    headerContent: (@Composable () -> Unit)? = null,
    onLikeToggle: (postId: String, currentLiked: Boolean) -> Unit = { _, _ -> },
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {},
    onBookmarkClick: (Post) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onSeedSamplePosts: (() -> Unit)? = null,
    isLoading: Boolean = false
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("vertical_feed_list"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Optional top header (e.g. Snaplies / Stories bar)
        if (headerContent != null) {
            item(key = "feed_header") {
                headerContent()
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    thickness = 0.5.dp
                )
            }
        }

        // Loading state
        if (isLoading && posts.isEmpty()) {
            item(key = "feed_loading") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Empty state
        if (!isLoading && posts.isEmpty()) {
            item(key = "feed_empty") {
                FeedEmptyState(
                    onSeedSamplePosts = onSeedSamplePosts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 48.dp)
                )
            }
        }

        // Feed items
        items(
            items = posts,
            key = { it.id }
        ) { post ->
            FeedPostCard(
                post = post,
                onLikeToggle = onLikeToggle,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick,
                onBookmarkClick = onBookmarkClick,
                onUserClick = onUserClick
            )
        }
    }
}

/**
 * Individual post card rendered in the vertical scrolling feed.
 */
@Composable
fun FeedPostCard(
    post: Post,
    modifier: Modifier = Modifier,
    onLikeToggle: (String, Boolean) -> Unit = { _, _ -> },
    onCommentClick: (Post) -> Unit = {},
    onShareClick: (Post) -> Unit = {},
    onBookmarkClick: (Post) -> Unit = {},
    onUserClick: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Local optimistic like state
    var isLiked by remember(post.id, post.isLiked) { mutableStateOf(post.isLiked) }
    var likesCount by remember(post.id, post.likesCount) { mutableIntStateOf(post.likesCount) }
    var isSaved by remember(post.id, post.isSaved) { mutableStateOf(post.isSaved) }

    // Heart animations
    var showBigHeart by remember { mutableStateOf(false) }
    val bigHeartScale = remember { Animatable(0f) }
    val likeButtonScale = remember { Animatable(1f) }

    val handleLikeAction = {
        val currentStatus = isLiked
        isLiked = !currentStatus
        likesCount += if (isLiked) 1 else -1
        coroutineScope.launch {
            likeButtonScale.animateTo(
                targetValue = 1.35f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            )
            likeButtonScale.animateTo(1f, animationSpec = tween(120))
        }
        onLikeToggle(post.id, currentStatus)
    }

    val triggerDoubleTapHeart = {
        if (!isLiked) {
            handleLikeAction()
        }
        coroutineScope.launch {
            showBigHeart = true
            bigHeartScale.snapTo(0f)
            bigHeartScale.animateTo(
                targetValue = 1.25f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            )
            delay(350)
            bigHeartScale.animateTo(0f, animationSpec = tween(200))
            showBigHeart = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feed_post_card_${post.id}")
    ) {
        // Post Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(post.user.profilePicUrl.ifEmpty { "https://picsum.photos/150" })
                    .crossfade(true)
                    .build(),
                contentDescription = "${post.user.username}'s avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onUserClick(post.user.id) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUserClick(post.user.id) }
            ) {
                Text(
                    text = post.user.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (post.timeAgo.isNotEmpty()) {
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { /* More options */ },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More post options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Post Media (Image via Coil or Video via ExoPlayer)
        val isVideo = remember(post.mediaType, post.imageUrl) {
            isVideoMedia(post.mediaType, post.imageUrl)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.Black)
                .pointerInput(post.id) {
                    detectTapGestures(
                        onDoubleTap = { triggerDoubleTapHeart() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (isVideo) {
                VideoPlayer(
                    videoUrl = post.imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    autoPlay = true,
                    initiallyMuted = true
                )
            } else {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = post.caption.ifEmpty { "Post Image" },
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.5.dp
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unable to load media",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Animated double-tap heart overlay
            androidx.compose.animation.AnimatedVisibility(
                visible = showBigHeart,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Double tap like animation",
                    tint = Color.White.copy(alpha = 0.95f),
                    modifier = Modifier
                        .size(110.dp)
                        .graphicsLayer {
                            scaleX = bigHeartScale.value
                            scaleY = bigHeartScale.value
                        }
                )
            }
        }

        // Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Like button
                IconButton(
                    onClick = { handleLikeAction() },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("post_like_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike post" else "Like post",
                        tint = if (isLiked) Color(0xFFED4956) else MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(26.dp)
                            .graphicsLayer {
                                scaleX = likeButtonScale.value
                                scaleY = likeButtonScale.value
                            }
                    )
                }

                // Comment button
                IconButton(
                    onClick = { onCommentClick(post) },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("post_comment_button_${post.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "View comments",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = { onShareClick(post) },
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Share post",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Bookmark button
            IconButton(
                onClick = {
                    isSaved = !isSaved
                    onBookmarkClick(post)
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (isSaved) "Remove from saved" else "Save post",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Likes count
        if (likesCount > 0) {
            Text(
                text = "${"%,d".format(likesCount)} likes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )
        }

        // Caption
        if (post.caption.isNotBlank()) {
            val captionText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    append("${post.user.username} ")
                }
                withStyle(
                    style = SpanStyle(color = MaterialTheme.colorScheme.onBackground)
                ) {
                    append(post.caption)
                }
            }

            Text(
                text = captionText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
            )
        }

        // View comments link
        if (post.commentsCount > 0) {
            Text(
                text = "View all ${post.commentsCount} comments",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 3.dp)
                    .clickable { onCommentClick(post) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}

/**
 * Empty state displayed when no posts exist in Firestore.
 */
@Composable
fun FeedEmptyState(
    modifier: Modifier = Modifier,
    onSeedSamplePosts: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircleOutline,
                    contentDescription = "Feed empty",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your Feed is Empty",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Share photos or videos with your camera and gallery to see them here in full HD.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (onSeedSamplePosts != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onSeedSamplePosts,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Load Sample Posts (Images & Videos)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
