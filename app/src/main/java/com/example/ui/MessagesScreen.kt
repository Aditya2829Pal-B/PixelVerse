package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.data.Chat
import com.example.data.MockData
import com.example.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewMessageModal by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(MockData.currentUser.username, fontWeight = FontWeight.Bold, fontSize = 20.sp) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showNewMessageModal = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New Message")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                singleLine = true
            )
            
            Text(
                "Messages", 
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                val filteredChats = if (searchQuery.isBlank()) {
                    MockData.chats
                } else {
                    MockData.chats.filter { chat -> 
                        chat.participants.any { it.username.contains(searchQuery, ignoreCase = true) }
                    }
                }
                
                items(filteredChats) { chat ->
                    ChatItem(chat = chat) {
                        navController.navigate("chat/${chat.id}")
                    }
                }
            }
        }
    }
    
    if (showNewMessageModal) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showNewMessageModal = false },
            sheetState = sheetState
        ) {
            NewMessageSheetContent(navController) { showNewMessageModal = false }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageSheetContent(navController: NavController, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val filteredUsers = MockData.users.filter { 
        it.username.contains(query, ignoreCase = true) || it.fullName.contains(query, ignoreCase = true) 
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.8f)) {
        Text("New Message", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
        
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("To...") },
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            singleLine = true
        )

        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) {
            items(filteredUsers) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val existingChatId = MockData.chats.firstOrNull { 
                                it.participants.size == 1 && it.participants[0].id == user.id 
                            }?.id
                            val chatIdToNav = existingChatId ?: "new_chat_${user.id}"
                            onDismiss()
                            navController.navigate("chat/$chatIdToNav")
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user.profilePicUrl, 
                        contentDescription = null, 
                        contentScale = ContentScale.Crop, 
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(user.username, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(user.fullName, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onClick: () -> Unit) {
    val displayNames = chat.participants.joinToString(", ") { it.username }
    val isGroup = chat.participants.size > 1
    val lastMessage = chat.messages.lastOrNull()
    val isUnread = chat.unreadCount > 0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        if (isGroup) {
            Box(modifier = Modifier.size(56.dp)) {
                AsyncImage(
                    model = chat.participants[0].profilePicUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp).clip(CircleShape).align(Alignment.TopStart)
                )
                AsyncImage(
                    model = chat.participants[1].profilePicUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(40.dp).clip(CircleShape).align(Alignment.BottomEnd)
                )
            }
        } else {
            AsyncImage(
                model = chat.participants.firstOrNull()?.profilePicUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Text part
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayNames,
                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = lastMessage?.let { if (it.imageUrl != null) "Sent an image" else it.text } ?: "",
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal,
                    color = if (isUnread) MaterialTheme.colorScheme.onBackground else Color.Gray,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (lastMessage != null) {
                    Text(
                        text = " · ${lastMessage.timeAgo}",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        if (isUnread) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0095F6))
            )
        } else {
            IconButton(onClick = { /* open camera */ }) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = "Camera", tint = Color.Gray)
            }
        }
    }
}
