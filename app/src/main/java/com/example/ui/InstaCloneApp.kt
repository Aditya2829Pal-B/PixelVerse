package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.data.MockData
import com.example.data.Post
import com.example.data.Snaply
import com.example.presentation.home.HomeViewModel
import com.example.presentation.profile.ProfileViewModel
import com.example.presentation.explore.ExploreViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.settings.SettingsViewModel

import com.example.presentation.auth.AuthViewModel

@Composable
fun InstaCloneApp(
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val currentUserId by authViewModel.currentUserId.collectAsState()

    if (currentUserId == null) {
        AuthScreen(authViewModel)
    } else {
        val navController = rememberNavController()
        
        val showHome by settingsViewModel.showHome.collectAsState()
        val showSearch by settingsViewModel.showSearch.collectAsState()
        val showAdd by settingsViewModel.showAdd.collectAsState()
        val showSnaply by settingsViewModel.showSnaply.collectAsState()
        val showAccount by settingsViewModel.showAccount.collectAsState()
        
        // Check if the current route is a root route (for bottom bar visibility)
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isRootRoute = currentRoute in listOf("home", "search", "add", "snaply", "account")

        Scaffold(
            bottomBar = { 
                if (isRootRoute) {
                    BottomNavigationBar(
                        navController = navController,
                        showHome = showHome,
                        showSearch = showSearch,
                        showAdd = showAdd,
                        showSnaply = showSnaply,
                        showAccount = showAccount
                    ) 
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") { HomeFeedScreen(navController) }
                composable("search") { ExploreScreen() }
                composable("add") { AddPostScreen(navController) }
                composable("snaply") { SnaplyScreen() }
                composable("account") { ProfileScreen(navController) }
                composable("settings") { SettingsScreen(navController) }
                composable("messages") { MessagesScreen(navController) }
                composable(
                    "chat/{chatId}",
                    arguments = listOf(navArgument("chatId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val chatId = backStackEntry.arguments?.getString("chatId")
                    if (chatId != null) {
                        ChatScreen(navController, chatId)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavController,
    showHome: Boolean,
    showSearch: Boolean,
    showAdd: Boolean,
    showSnaply: Boolean,
    showAccount: Boolean
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        modifier = Modifier.border(width = 0.5.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val items = listOf(
            Triple("home", Icons.Filled.Home, Icons.Outlined.Home),
            Triple("search", Icons.Filled.Search, Icons.Outlined.Search),
            Triple("add", Icons.Filled.AddBox, Icons.Outlined.AddBox),
            Triple("snaply", Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow),
            Triple("account", Icons.Filled.Person, Icons.Outlined.Person)
        ).filter { (route) ->
            when (route) {
                "home" -> showHome
                "search" -> showSearch
                "add" -> showAdd
                "snaply" -> showSnaply
                "account" -> showAccount
                else -> true
            }
        }
        
        items.forEach { (route, selectedIcon, unselectedIcon) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) selectedIcon else unselectedIcon,
                        contentDescription = route
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val posts by homeViewModel.feedPosts.collectAsState()
    
    var isRefreshing by remember { mutableStateOf(false) }
    
    val onRefresh: () -> Unit = {
        isRefreshing = true
        homeViewModel.refreshFeed {
            isRefreshing = false
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("InstaClone", fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp)) 
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.FavoriteBorder, contentDescription = "Likes")
                    }
                    IconButton(onClick = { navController.navigate("messages") }) {
                        Icon(Icons.Outlined.Send, contentDescription = "Messages")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    SnapliesRow(snaplies = MockData.snaplies)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                }
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onLikeToggle = { id, currentLikeStatus ->
                            homeViewModel.toggleLike(id, currentLikeStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SnapliesRow(snaplies: List<Snaply>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // "Your Snaply" item
        item {
            SnaplyItem(
                imageUrl = MockData.currentUser.profilePicUrl,
                username = "Your Snaply",
                isViewed = true,
                isAddSnaply = true
            )
        }
        items(snaplies) { snaply ->
            SnaplyItem(
                imageUrl = snaply.user.profilePicUrl,
                username = snaply.user.username,
                isViewed = snaply.isViewed
            )
        }
    }
}

@Composable
fun SnaplyItem(imageUrl: String, username: String, isViewed: Boolean, isAddSnaply: Boolean = false) {
    val gradientColors = if (isViewed) {
        listOf(Color.LightGray, Color.LightGray)
    } else {
        listOf(
            Color(0xFFFEDA77),
            Color(0xFFF58529),
            Color(0xFFDD2A7B),
            Color(0xFF8134AF),
            Color(0xFF515BD4)
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(gradientColors))
                .padding(if (isAddSnaply) 0.dp else 2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(2.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Snaply from $username",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
            if (isAddSnaply) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(1.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0095F6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Snaply", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = username,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PostItem(post: Post, onLikeToggle: (String, Boolean) -> Unit = { _, _ -> }) {
    var showComments by remember { mutableStateOf(false) }
    
    val isLiked = post.isLiked
    val likesCount = post.likesCount
    val isSaved = post.isSaved
    
    val dynamicCommentsCount = MockData.comments.count { it.postId == post.id }
    
    val scope = rememberCoroutineScope()
    var showHeart by remember { mutableStateOf(false) }
    val heartScale = remember { Animatable(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = post.user.profilePicUrl,
                contentDescription = "Profile Pic",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = post.user.username,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }

        // Image and Heart Animation
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Square image
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                onLikeToggle(post.id, post.isLiked)
                                scope.launch {
                                    showHeart = true
                                    heartScale.snapTo(0f)
                                    heartScale.animateTo(
                                        targetValue = 1.2f,
                                        animationSpec = tween(200)
                                    )
                                    delay(200)
                                    heartScale.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(200)
                                    )
                                    showHeart = false
                                }
                            }
                        )
                    }
            )
            
            if (showHeart) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Large Like",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer {
                            scaleX = heartScale.value
                            scaleY = heartScale.value
                        }
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onLikeToggle(post.id, post.isLiked) }) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(onClick = { showComments = true }) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comment")
                }
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.Send, contentDescription = "Share")
                }
            }
            IconButton(onClick = { 
                val index = MockData.posts.indexOfFirst { it.id == post.id }
                if (index != -1) {
                    val p = MockData.posts[index]
                    MockData.posts[index] = p.copy(isSaved = !p.isSaved)
                }
            }) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save"
                )
            }
        }

        // Likes
        Text(
            text = "$likesCount likes",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Caption
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(
                text = "${post.user.username} ",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = post.caption,
                fontSize = 14.sp
            )
        }

        // Comments
        Text(
            text = "View all $dynamicCommentsCount comments",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 2.dp)
                .clickable { showComments = true }
        )
        
        // Time ago
        Text(
            text = "${post.timeAgo} ago",
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
        )
        
        if (showComments) {
            CommentsBottomSheet(post = post, onDismiss = { showComments = false })
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ExploreScreen(
    exploreViewModel: ExploreViewModel = viewModel(factory = ExploreViewModel.Factory)
) {
    val images by exploreViewModel.exploreImages.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(images) { imageUrl ->
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    val user by profileViewModel.currentUser.collectAsState()
    val posts by profileViewModel.userPosts.collectAsState()
    
    // Fallback if not loaded
    val displayUser = user ?: return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(displayUser.username, fontWeight = FontWeight.Bold) 
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.AddBox, contentDescription = "Create")
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Profile Header
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        AsyncImage(
                            model = displayUser.profilePicUrl,
                            contentDescription = "Profile Pic",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.width(32.dp))
                        
                        // Stats
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProfileStat(count = posts.size.toString(), label = "Posts")
                            ProfileStat(count = displayUser.followersCount.toString(), label = "Followers")
                            ProfileStat(count = displayUser.followingCount.toString(), label = "Following")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = displayUser.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "Android Developer \uD83D\uDCBB\nKeep composing!", fontSize = 14.sp)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Edit profile", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Text("Share profile", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { },
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onBackground),
                            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Discover people", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                
                // Snaply Highlights (dummy)
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(5) { i ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color.LightGray, CircleShape)
                                    .padding(4.dp)
                            ) {
                                AsyncImage(
                                    model = "https://picsum.photos/seed/highlight$i/150/150",
                                    contentDescription = "Highlight",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Highlight", fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Grid tabs (Posts vs Tags)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = "Grid")
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(width = 0.5.dp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonOutline, contentDescription = "Tags", tint = Color.Gray)
                    }
                }
            }
            
            // Grid
            val rows = posts.map { it.imageUrl }.chunked(3)
            items(rows) { rowImages ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (imageUrl in rowImages) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                        )
                    }
                    // pad with empty boxes if row < 3 items
                    for (i in 0 until (3 - rowImages.size)) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, fontSize = 14.sp)
    }
}
