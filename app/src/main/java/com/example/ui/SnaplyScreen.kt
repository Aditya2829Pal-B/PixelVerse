package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MockData

@Composable
fun SnaplyScreen() {
    val snaplies = remember { MockData.exploreImages.shuffled() } // Simulate snaply content

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        items(snaplies) { imageUrl ->
            SnaplyVideoItem(
                imageUrl = imageUrl,
                modifier = Modifier.fillParentMaxHeight()
            )
        }
    }
}

@Composable
fun SnaplyVideoItem(imageUrl: String, modifier: Modifier = Modifier) {
    var isLiked by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Snaply Video",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = { isLiked = !isLiked }) {
                Icon(
                    if (isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) Color.Red else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text("12k", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Comment",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text("456", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(onClick = { }) {
                Icon(
                    Icons.Outlined.Send,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // User info overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "@pixel_creator",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text("Follow", fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Loving the vibes today! 🌅✨ #PixelVerse",
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}
