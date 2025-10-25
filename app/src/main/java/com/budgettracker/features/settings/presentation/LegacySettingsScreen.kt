package com.budgettracker.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.budgettracker.features.auth.data.HybridAuthManager
import com.budgettracker.core.data.repository.DataInitializer
import com.google.firebase.ktx.Firebase
import com.google.firebase.functions.ktx.functions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.widget.Toast

/**
 * Settings screen with user profile and logout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { HybridAuthManager(context) }
    val scope = rememberCoroutineScope()
    val currentUser = authManager.getCurrentUser()
    
    var initializationStatus by remember { mutableStateOf("") }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "⚙️ Settings",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // User Profile Section
            item {
                UserProfileCard(
                    user = currentUser,
                    onEditProfile = { /* TODO: Navigate to edit profile */ }
                )
            }
            
            // App Settings
            item {
                AppSettingsCard()
            }
            
            // Developer Tools (Test Notifications)
            item {
                DeveloperToolsCard(scope = scope)
            }
            
            // Financial Settings
            item {
                FinancialSettingsCard()
            }
            
            // Account Actions
            item {
                LegacyAccountActionsCard(
                    onLogout = { showLogoutDialog = true },
                    onExportData = { /* TODO: Export data */ },
                    onDeleteAccount = { /* TODO: Delete account */ }
                )
            }
            
            // App Information
            item {
                AppInfoCard()
            }
        }
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            authManager.signOut()
                            showLogoutDialog = false
                            onNavigateToLogin()
                        }
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserProfileCard(
    user: com.budgettracker.features.auth.data.User?,
    onEditProfile: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enhanced Profile Picture
                Card(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.name?.firstOrNull()?.toString()?.uppercase() ?: "U",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(20.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = user?.name ?: "User",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = user?.email ?: "No email",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = "Ixana Quasistatics • OPT Status • $80,000",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Card(
                    onClick = onEditProfile,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AppSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "App Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Budget alerts and reminders",
                onClick = { /* TODO: Notification settings */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Security",
                subtitle = "Biometric authentication",
                onClick = { /* TODO: Security settings */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = "Dark mode, colors",
                onClick = { /* TODO: Theme settings */ }
            )
        }
    }
}

@Composable
private fun DeveloperToolsCard(scope: kotlinx.coroutines.CoroutineScope) {
    val context = LocalContext.current
    var isTestingSending by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "Developer Tools",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Developer Tools",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Test Notification Button
            Button(
                onClick = {
                    scope.launch {
                        isTestingSending = true
                        try {
                            // Call Cloud Function to send test notification
                            val result = Firebase.functions
                                .getHttpsCallable("sendTestNotification")
                                .call()
                                .await()
                            
                            Toast.makeText(
                                context,
                                "✅ Test notification sent! Check your notification tray.",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "❌ Error: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isTestingSending = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingSending,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                if (isTestingSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Icon(
                        imageVector = Icons.Default.NotificationAdd,
                        contentDescription = "Test Notification"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🔔 Send Test Notification")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "This will send a test push notification to verify Firebase Cloud Messaging is working.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FinancialSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Financial Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsItem(
                icon = Icons.Default.AttachMoney,
                title = "Currency",
                subtitle = "USD - United States Dollar",
                onClick = { /* TODO: Currency settings */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Work,
                title = "Employment",
                subtitle = "OPT Status • $80,000 salary",
                onClick = { /* TODO: Employment settings */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Category,
                title = "Categories",
                subtitle = "Manage transaction categories",
                onClick = { /* TODO: Category settings */ }
            )
        }
    }
}

@Composable
private fun LegacyAccountActionsCard(
    onLogout: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsItem(
                icon = Icons.Default.GetApp,
                title = "Export Data",
                subtitle = "Download your financial data",
                onClick = onExportData
            )
            
            SettingsItem(
                icon = Icons.Default.Sync,
                title = "Initialize Financial Data",
                subtitle = "Set up your complete financial profile",
                onClick = {
                    // Will be populated via Firebase Console for now
                }
            )
            
            SettingsItem(
                icon = Icons.Default.ExitToApp,
                title = "Sign Out",
                subtitle = "Sign out of your account",
                onClick = onLogout,
                textColor = Color(0xFFdc3545)
            )
            
            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                subtitle = "Permanently delete your account",
                onClick = onDeleteAccount,
                textColor = Color(0xFFdc3545)
            )
        }
    }
}

@Composable
private fun AppInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "App Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            SettingsItem(
                icon = Icons.Default.Info,
                title = "Version",
                subtitle = "1.0.0 (Beta)",
                onClick = { /* TODO: About */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help with the app",
                onClick = { /* TODO: Help */ }
            )
            
            SettingsItem(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                subtitle = "How we protect your data",
                onClick = { /* TODO: Privacy policy */ }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
