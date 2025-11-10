package com.budgettracker.features.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.budgettracker.R
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.features.auth.data.HybridAuthManager
import com.budgettracker.core.utils.AnalyticsTracker
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * Login screen for user authentication
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToRegister: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    // Track screen view
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("Login")
    }
    
    val context = LocalContext.current
    val authManager = remember { HybridAuthManager(context) }
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { idToken ->
                    scope.launch {
                        val authResult = authManager.signInWithGoogle(idToken)
                        if (authResult.isSuccess) {
                            val (user, isNewUser) = authResult.getOrNull()!!
                            android.util.Log.d("LoginScreen", "Google Sign-In success: user=${user.email}, isNewUser=$isNewUser")
                            
                            // FIXED: Initialize TransactionDataStore
                            try {
                                TransactionDataStore.initializeFromFirebase(forceReload = true)
                                android.util.Log.d("LoginScreen", "✅ Initialized data for user (Google)")
                            } catch (e: Exception) {
                                android.util.Log.e("LoginScreen", "Failed to initialize: ${e.message}")
                            }
                            
                            // FIXED: Route to onboarding for new users, dashboard for existing
                            if (isNewUser) {
                                android.util.Log.d("LoginScreen", "🆕 New user → Navigating to onboarding")
                                onNavigateToOnboarding()
                            } else {
                                android.util.Log.d("LoginScreen", "👤 Existing user → Navigating to dashboard")
                                onNavigateToDashboard()
                            }
                        } else {
                            errorMessage = "Google Sign-In failed: ${authResult.exceptionOrNull()?.message}"
                        }
                        isLoading = false
                    }
                } ?: run {
                    errorMessage = "Google Sign-In failed: No ID token received"
                    isLoading = false
                }
            } catch (e: ApiException) {
                errorMessage = "Google Sign-In failed: ${e.message}"
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Top section with logo and title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Logo and Title
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "KINOVA Logo",
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "KINOVA",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        
        // Login Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                    
                    // Error Message
                    if (errorMessage.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                    
                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sign In Button
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                val result = authManager.signInWithEmailAndPassword(email, password)
                                if (result.isSuccess) {
                                    // FIXED: Initialize TransactionDataStore for new user
                                    try {
                                        TransactionDataStore.initializeFromFirebase(forceReload = true)
                                        android.util.Log.d("LoginScreen", "✅ Initialized data for user")
                                    } catch (e: Exception) {
                                        android.util.Log.e("LoginScreen", "Failed to initialize: ${e.message}")
                                    }
                                    onNavigateToDashboard()
                                } else {
                                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Sign In")
                        }
                    }
                    
                    // Google Sign In Button
                    OutlinedButton(
                        onClick = {
                            // Launch real Google Sign-In with account selection
                            isLoading = true
                            errorMessage = ""
                            scope.launch {
                                // Sign out from Google first to force account picker
                                authManager.signOutFromGoogle()
                                // Now launch sign-in
                                val signInIntent = authManager.getGoogleSignInClient().signInIntent
                                googleSignInLauncher.launch(signInIntent)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign in with Google")
                    }
                    
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Register Link - Outside card for better visibility
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onNavigateToRegister,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
        
        // Demo credentials info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = "Demo: Enter any email and password to continue",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

