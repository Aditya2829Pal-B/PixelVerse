package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Post
import com.example.data.local.entity.CommentEntity
import com.example.presentation.comments.CommentsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    post: Post,
    onDismiss: () -> Unit,
    commentsViewModel: CommentsViewModel = viewModel(factory = CommentsViewModel.Factory(post.id))
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var text by remember { mutableStateOf("") }
    
    val postComments by commentsViewModel.comments.collectAsState()
    val currentUser by commentsViewModel.currentUser.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f) // Occupy 80% of screen height
        ) {
            Text(
                "Comments",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(postComments) { comment ->
                    CommentItem(comment = comment)
                }
            }
            
            HorizontalDivider()
            
            // Comment input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = currentUser?.profilePicUrl ?: "",
                    contentDescription = "Your Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("Add a comment...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = CircleShape,
                    maxLines = 3
                )
                if (text.isNotBlank()) {
                    IconButton(onClick = {
                        commentsViewModel.addComment(text)
                        text = ""
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color(0xFF0095F6))
                    }
                }
            }
            // Add extra padding at the bottom to accommodate the navigation bar / keyboard
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CommentItem(comment: CommentEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        AsyncImage(
            model = comment.profilePicUrl,
            contentDescription = "Profile Pic",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.timeAgo,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Text(
                text = comment.text,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        
        // Delete option for own comments
        // Note: For full implementation, we'd add delete functionality in ViewModel
        // if (comment.userId == currentUser.id) { ... }
    }
}
