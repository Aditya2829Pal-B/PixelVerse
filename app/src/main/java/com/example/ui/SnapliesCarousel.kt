package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.Snaply
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Vibrant gradient colors representing an unviewed Snaply (story) ring.
 */
val SnaplyUnviewedGradient = Brush.linearGradient(
    listOf(
        Color(0xFFFEDA77),
        Color(0xFFF58529),
        Color(0xFFDD2A7B),
        Color(0xFF8134AF),
        Color(0xFF515BD4)
    )
)

/**
 * Subtle muted gradient colors for an already viewed Snaply.
 */
val SnaplyViewedGradient = Brush.linearGradient(
    listOf(
        Color(0xFFCCCCCC),
        Color(0xFFAAAAAA)
    )
)

/**
 * Snaplies (stories) horizontal carousel component displayed at the top of the home screen.
 * Allows users to scroll horizontally and tap on any story to view temporary visual content.
 */
@Composable
fun SnapliesCarousel(
    snaplies: List<Snaply>,
    currentUserProfilePic: String,
    viewedSnaplyIds: Set<String> = emptySet(),
    onSnaplyClick: (Snaply, Int) -> Unit,
    onAddSnaplyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .testTag("snaplies_carousel"),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Your Snaply" item with quick add button
        item(key = "your_snaply_item") {
            YourSnaplyItem(
                profilePicUrl = currentUserProfilePic,
                onClick = onAddSnaplyClick
            )
        }

        // Friends' Snaplies items
        itemsIndexed(
            items = snaplies,
            key = { _, snaply -> snaply.id }
        ) { index, snaply ->
            val isViewed = snaply.isViewed || viewedSnaplyIds.contains(snaply.id)
            SnaplyCarouselItem(
                snaply = snaply,
                isViewed = isViewed,
                onClick = { onSnaplyClick(snaply, index) }
            )
        }
    }
}

/**
 * "Your Snaply" item with an avatar and a blue '+' badge.
 */
@Composable
fun YourSnaplyItem(
    profilePicUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("your_snaply_button")
    ) {
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            // Profile image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePicUrl.ifEmpty { "https://picsum.photos/150" })
                    .crossfade(true)
                    .build(),
                contentDescription = "Your Snaply",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(66.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
            )

            // Blue Add Badge at bottom-right
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0095F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Snaply",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Your Snaply",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Individual Snaply item in the horizontal carousel with an authentic gradient ring.
 */
@Composable
fun SnaplyCarouselItem(
    snaply: Snaply,
    isViewed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ringBrush = if (isViewed) SnaplyViewedGradient else SnaplyUnviewedGradient

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(72.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .testTag("snaply_item_${snaply.id}")
    ) {
        // Gradient Ring Box
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(ringBrush)
                .padding(2.5.dp) // Ring thickness
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(2.dp), // Inner gap
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(snaply.user.profilePicUrl.ifEmpty { "https://picsum.photos/150" })
                    .crossfade(true)
                    .build(),
                contentDescription = "${snaply.user.username}'s Snaply",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = snaply.user.username,
            style = MaterialTheme.typography.labelSmall,
            color = if (isViewed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Full-screen interactive viewer for temporary visual content (Snaplies / Stories).
 *
 * Supports:
 * - Multi-segment progress bar reflecting the active story index.
 * - Auto-advancing timed playback (5 seconds per story).
 * - Tap navigation: Left third to go back, Right two-thirds to advance.
 * - Press & hold to pause story timer.
 * - Interactive quick reaction bar at bottom with floating emoji animations.
 * - Marks current story as viewed.
 */
@Composable
fun SnaplyStoryViewer(
    snaplies: List<Snaply>,
    initialIndex: Int = 0,
    onDismiss: () -> Unit,
    onSnaplyViewed: (String) -> Unit = {}
) {
    if (snaplies.isEmpty()) {
        onDismiss()
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, snaplies.size - 1)) }
        val currentSnaply = snaplies[currentIndex]

        var isPaused by remember { mutableStateOf(false) }
        var currentProgress by remember { mutableFloatStateOf(0f) }

        // Floating reaction animations
        var floatingReaction by remember { mutableStateOf<String?>(null) }
        val floatingReactionScale = remember { Animatable(0f) }
        val coroutineScope = rememberCoroutineScope()

        var replyText by remember { mutableStateOf("") }

        // Notify that the active Snaply has been viewed
        LaunchedEffect(currentSnaply.id) {
            onSnaplyViewed(currentSnaply.id)
        }

        // Story timer loop
        LaunchedEffect(currentIndex, isPaused) {
            if (isPaused) return@LaunchedEffect

            val totalDurationMs = 5000L
            val intervalMs = 20L
            val stepFraction = intervalMs.toFloat() / totalDurationMs

            while (currentProgress < 1f) {
                delay(intervalMs)
                if (!isPaused) {
                    currentProgress = (currentProgress + stepFraction).coerceAtMost(1f)
                }
            }

            // Auto advance when finished
            if (currentIndex < snaplies.size - 1) {
                currentIndex++
                currentProgress = 0f
            } else {
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("snaply_story_viewer")
        ) {
            // Fullscreen Story Visual Content
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(currentSnaply.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Snaply Visual Content",
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.8f),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Touch Zones for Navigation & Hold to Pause
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentIndex) {
                        detectTapGestures(
                            onPress = {
                                isPaused = true
                                val released = tryAwaitRelease()
                                isPaused = false
                            },
                            onTap = { offset ->
                                val screenWidth = size.width
                                if (offset.x < screenWidth * 0.33f) {
                                    // Tap left -> Previous story
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        currentProgress = 0f
                                    } else {
                                        currentProgress = 0f
                                    }
                                } else {
                                    // Tap right -> Next story
                                    if (currentIndex < snaplies.size - 1) {
                                        currentIndex++
                                        currentProgress = 0f
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }
            )

            // Top Overlay: Progress bars & User info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                        )
                    )
                    .padding(top = 40.dp, bottom = 20.dp)
            ) {
                // Multi-Segment Story Progress Bars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    snaplies.forEachIndexed { index, _ ->
                        val progressValue = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> currentProgress
                            else -> 0f
                        }

                        LinearProgressIndicator(
                            progress = { progressValue },
                            modifier = Modifier
                                .weight(1f)
                                .height(2.5.dp)
                                .clip(CircleShape),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.35f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Author Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(currentSnaply.user.profilePicUrl.ifEmpty { "https://picsum.photos/150" })
                            .crossfade(true)
                            .build(),
                        contentDescription = "${currentSnaply.user.username}'s profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = currentSnaply.user.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "• 2h",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("close_snaply_viewer")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Snaply",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Overlay: Quick reply field and reaction emojis
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                // Quick emoji reactions row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val emojis = listOf("❤️", "🔥", "👏", "😂", "😍", "🎉")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    floatingReaction = emoji
                                    coroutineScope.launch {
                                        floatingReactionScale.snapTo(0f)
                                        floatingReactionScale.animateTo(
                                            targetValue = 1.4f,
                                            animationSpec = tween(200)
                                        )
                                        delay(400)
                                        floatingReactionScale.animateTo(0f, animationSpec = tween(200))
                                        floatingReaction = null
                                    }
                                }
                                .padding(6.dp)
                        )
                    }
                }

                // Send message input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "Send message...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            floatingReaction = "❤️"
                            coroutineScope.launch {
                                floatingReactionScale.snapTo(0f)
                                floatingReactionScale.animateTo(1.5f, tween(200))
                                delay(300)
                                floatingReactionScale.animateTo(0f, tween(200))
                                floatingReaction = null
                            }
                        },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Like Snaply",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = { /* Share */ },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Share Snaply",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Floating reaction animated pop-up
            AnimatedVisibility(
                visible = floatingReaction != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                if (floatingReaction != null) {
                    Text(
                        text = floatingReaction!!,
                        fontSize = 72.sp
                    )
                }
            }
        }
    }
}
