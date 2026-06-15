package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.presentation.settings.SettingsViewModel
import com.example.presentation.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val showHome by settingsViewModel.showHome.collectAsState()
    val showSearch by settingsViewModel.showSearch.collectAsState()
    val showAdd by settingsViewModel.showAdd.collectAsState()
    val showSnaply by settingsViewModel.showSnaply.collectAsState()
    val showAccount by settingsViewModel.showAccount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            Text(
                text = "Customize Navigation Tabs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
            
            ToggleRow(label = "Home Screen", isChecked = showHome) {
                settingsViewModel.toggleHome(it)
            }
            ToggleRow(label = "Search Screen", isChecked = showSearch) {
                settingsViewModel.toggleSearch(it)
            }
            ToggleRow(label = "Add Post Screen", isChecked = showAdd) {
                settingsViewModel.toggleAdd(it)
            }
            ToggleRow(label = "Snaply Screen", isChecked = showSnaply) {
                settingsViewModel.toggleSnaply(it)
            }
            ToggleRow(label = "Account Screen", isChecked = showAccount) {
                settingsViewModel.toggleAccount(it)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Note: Disabling a tab will remove it from the bottom navigation bar.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { authViewModel.logout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Log Out")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ToggleRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}
