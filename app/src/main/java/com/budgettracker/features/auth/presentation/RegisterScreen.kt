package com.budgettracker.features.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.budgettracker.features.auth.data.HybridAuthManager
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

/**
 * Register screen for new user registration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { HybridAuthManager(context) }
    val scope = rememberCoroutineScope()
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
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
                            onNavigateToDashboard()
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
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary40, Secondary40)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo and Title
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💰",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Budget Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Create your account",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Register Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sign Up",
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
                    
                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = "Name")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
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
                    
                    // Confirm Password Field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Confirm Password")
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                            ) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword
                    )
                    
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "Passwords do not match",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sign Up Button
                    Button(
                        onClick = {
                            if (password == confirmPassword && password.length >= 6) {
                                isLoading = true
                                errorMessage = ""
                                scope.launch {
                                    val result = authManager.createUserWithEmailAndPassword(email, password, name)
                                    if (result.isSuccess) {
                                        onNavigateToDashboard()
                                    } else {
                                        errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                                    }
                                    isLoading = false
                                }
                            } else if (password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                            } else if (password.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && 
                                password.isNotBlank() && confirmPassword.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Create Account")
                        }
                    }
                    
                    // Google Sign Up Button
                    OutlinedButton(
                        onClick = {
                            // Launch real Google Sign-In with account selection
                            isLoading = true
                            errorMessage = ""
                            val signInIntent = authManager.getGoogleSignInClient().signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign up with Google")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Login Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Already have an account? ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = onNavigateToLogin
                        ) {
                            Text("Sign In")
                        }
                    }
                }
            }
        }
    }
}
