package com.example.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.presentation.auth.AuthViewModel
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PixelVerse",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        val credentialManager = CredentialManager.create(context)
                        
                        // Generate a nonce
                        val rawNonce = UUID.randomUUID().toString()
                        val bytes = rawNonce.toByteArray()
                        val md = MessageDigest.getInstance("SHA-256")
                        val digest = md.digest(bytes)
                        val hashedNonce = digest.joinToString("") { "%02x".format(it) }

                        // Note: Replace with your actual Web Client ID configured in Google Cloud Console
                        val webClientId = "123456789012-dummyclientid.apps.googleusercontent.com"

                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .setNonce(hashedNonce)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(
                            request = request,
                            context = context as Activity
                        )

                        val credential = result.credential
                        if (credential is CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            authViewModel.loginWithGoogle(googleIdTokenCredential.idToken)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Sign in with Google")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Please configure your google-services.json and Web Client ID in the code for the integration to work correctly.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}
