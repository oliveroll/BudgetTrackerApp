package com.budgettracker.features.settings.presentation

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.budgettracker.features.auth.data.HybridAuthManager
import com.budgettracker.features.settings.data.models.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { HybridAuthManager(context) }
    val scope = rememberCoroutineScope()
    val currentUser = authManager.getCurrentUser()
    
    val uiState by viewModel.uiState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val employment by viewModel.employment.collectAsState()
    val categories by viewModel.categories.collectAsState()
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle messages and errors
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.clearError()
        }
    }
    
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
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // User Profile Header
                item {
                    UserProfileHeader(
                        user = currentUser,
                        settings = userSettings,
                        onEditEmail = { viewModel.showEmailChangeDialog() }
                    )
                }
                
                // Account Settings
                item {
                    SettingsSection(title = "Account Settings") {
                        AccountSettingsCard(
                            settings = userSettings,
                            onEditEmail = { viewModel.showEmailChangeDialog() }
                        )
                    }
                }
                
                // App Settings - Notifications
                item {
                    SettingsSection(title = "App Settings") {
                        NotificationSettingsCard(
                            settings = userSettings?.notificationSettings ?: NotificationSettings(),
                            onSettingsChanged = { viewModel.updateNotificationSettings(it) },
                            onTestNotifications = { viewModel.testNotifications() },
                            context = context
                        )
                    }
                }
                
                // Financial Settings
                item {
                    SettingsSection(title = "Financial Settings") {
                        FinancialSettingsCard(
                            currency = userSettings?.currency ?: "USD",
                            currencySymbol = userSettings?.currencySymbol ?: "$",
                            employment = employment,
                            onCurrencyChange = { currency, symbol ->
                                viewModel.updateCurrency(currency, symbol)
                            },
                            onEmploymentEdit = { viewModel.showEmploymentDialog() }
                        )
                    }
                }
                
                // Categories
                item {
                    SettingsSection(title = "Categories") {
                        CategoriesCard(
                            categories = categories,
                            onAddCategory = { viewModel.showCategoryDialog() },
                            onEditCategory = { viewModel.showCategoryDialog(it) },
                            onArchiveCategory = { viewModel.archiveCategory(it) }
                        )
                    }
                }
                
                // Account Actions
                item {
                    SettingsSection(title = "Account Actions") {
                        AccountActionsCard(
                            onExport = {
                                viewModel.exportData { json ->
                                    saveExportToFile(context, json)
                                }
                            },
                            onSignOut = {
                                scope.launch {
                                    authManager.signOut()
                                    onNavigateToLogin()
                                }
                            },
                            onDeleteAccount = { viewModel.showDeleteAccountDialog() }
                        )
                    }
                }
                
                // Help & Info
                item {
                    SettingsSection(title = "Help & Info") {
                        HelpInfoCard()
                    }
                }
                
                // Spacer for bottom
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Dialogs
    if (uiState.showEmailChangeDialog) {
        EmailChangeDialog(
            currentEmail = userSettings?.email ?: "",
            onDismiss = { viewModel.hideEmailChangeDialog() },
            onConfirm = { password, newEmail ->
                viewModel.changeEmail(password, newEmail)
            }
        )
    }
    
    if (uiState.showEmploymentDialog) {
        EmploymentDialog(
            employment = employment,
            onDismiss = { viewModel.hideEmploymentDialog() },
            onSave = { viewModel.saveEmployment(it) }
        )
    }
    
    if (uiState.showCategoryDialog) {
        CategoryDialog(
            category = uiState.editingCategory,
            onDismiss = { viewModel.hideCategoryDialog() },
            onSave = { category ->
                if (uiState.editingCategory != null) {
                    viewModel.updateCategory(category)
                } else {
                    viewModel.addCategory(category)
                }
            }
        )
    }
    
    if (uiState.showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { viewModel.hideDeleteAccountDialog() },
            onConfirm = { password ->
                viewModel.deleteAccount(password) {
                    onNavigateToLogin()
                }
            }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun UserProfileHeader(
    user: com.budgettracker.features.auth.data.User?,
    settings: UserSettings?,
    onEditEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.name?.firstOrNull() ?: user?.email?.firstOrNull() ?: "U").toString().uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user?.name ?: "User",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onEditEmail) {
                Icon(Icons.Default.Edit, "Edit email")
            }
        }
    }
}

@Composable
fun AccountSettingsCard(
    settings: UserSettings?,
    onEditEmail: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsItem(
                icon = Icons.Default.Email,
                title = "Email",
                subtitle = settings?.email ?: "Not set",
                onClick = onEditEmail
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.Security,
                title = "Biometric Authentication",
                subtitle = "Coming soon",
                enabled = false
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme",
                subtitle = "System default • In development",
                enabled = false
            )
        }
    }
}

@Composable
fun NotificationSettingsCard(
    settings: NotificationSettings,
    onSettingsChanged: (NotificationSettings) -> Unit,
    onTestNotifications: () -> Unit,
    context: Context
) {
    // Request permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onSettingsChanged(settings.copy(notificationPermissionGranted = true))
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Permission banner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !settings.notificationPermissionGranted) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Notifications disabled",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        ) {
                            Text("Enable")
                        }
                    }
                }
            }
            
            NotificationToggleItem(
                title = "Low Balance Alert",
                subtitle = "Get notified when balance is low",
                enabled = settings.lowBalanceAlertEnabled,
                frequency = settings.lowBalanceFrequency,
                onToggle = { onSettingsChanged(settings.copy(lowBalanceAlertEnabled = it)) },
                onFrequencyChange = { onSettingsChanged(settings.copy(lowBalanceFrequency = it)) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            NotificationToggleItem(
                title = "Upcoming Bill Reminder",
                subtitle = "${settings.upcomingBillDaysBefore} days before due",
                enabled = settings.upcomingBillReminderEnabled,
                frequency = settings.upcomingBillFrequency,
                onToggle = { onSettingsChanged(settings.copy(upcomingBillReminderEnabled = it)) },
                onFrequencyChange = { onSettingsChanged(settings.copy(upcomingBillFrequency = it)) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            NotificationToggleItem(
                title = "Subscription Renewal",
                subtitle = "${settings.subscriptionRenewalDaysBefore} days before renewal",
                enabled = settings.subscriptionRenewalEnabled,
                frequency = settings.subscriptionRenewalFrequency,
                onToggle = { onSettingsChanged(settings.copy(subscriptionRenewalEnabled = it)) },
                onFrequencyChange = { onSettingsChanged(settings.copy(subscriptionRenewalFrequency = it)) }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            NotificationToggleItem(
                title = "Goal Milestone",
                subtitle = "Celebrate your progress",
                enabled = settings.goalMilestoneEnabled,
                frequency = settings.goalMilestoneFrequency,
                onToggle = { onSettingsChanged(settings.copy(goalMilestoneEnabled = it)) },
                onFrequencyChange = { onSettingsChanged(settings.copy(goalMilestoneFrequency = it)) }
            )
            
            // Test Notifications Button
            Divider(modifier = Modifier.padding(vertical = 12.dp))
            
            OutlinedButton(
                onClick = onTestNotifications,
                modifier = Modifier.fillMaxWidth(),
                enabled = settings.notificationPermissionGranted
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Notifications Now")
            }
            
            if (!settings.notificationPermissionGranted) {
                Text(
                    "Enable notifications to test",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            // Fix Firebase Notifications Button
            Spacer(modifier = Modifier.height(8.dp))
            
            var isRefreshingToken by remember { mutableStateOf(false) }
            
            Button(
                onClick = {
                    isRefreshingToken = true
                    com.budgettracker.ForceTokenRefresh.refreshAndSaveToken { success, message ->
                        isRefreshingToken = false
                        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isRefreshingToken
            ) {
                if (isRefreshingToken) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fixing...")
                } else {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Fix Firebase Notifications (9 AM Reminders)")
                }
            }
            
            Text(
                "Click if you're not receiving 9 AM bill/subscription reminders",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            // Test Firebase Notification Button
            Spacer(modifier = Modifier.height(8.dp))
            
            var isTestingFirebase by remember { mutableStateOf(false) }
            
            OutlinedButton(
                onClick = {
                    isTestingFirebase = true
                    // Call Firebase Cloud Function
                    com.google.firebase.functions.FirebaseFunctions.getInstance()
                        .getHttpsCallable("sendTestNotification")
                        .call()
                        .addOnSuccessListener {
                            isTestingFirebase = false
                            android.widget.Toast.makeText(
                                context,
                                "✅ Firebase notification sent! Check your notification tray.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            isTestingFirebase = false
                            android.widget.Toast.makeText(
                                context,
                                "❌ Error: ${e.message}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingFirebase && settings.notificationPermissionGranted
            ) {
                if (isTestingFirebase) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Firebase 9 AM Notifications")
                }
            }
            
            Text(
                "Tests if Firebase Cloud Functions can send you notifications",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun NotificationToggleItem(
    title: String,
    subtitle: String,
    enabled: Boolean,
    frequency: NotificationFrequency,
    onToggle: (Boolean) -> Unit,
    onFrequencyChange: (NotificationFrequency) -> Unit
) {
    var showFrequencyMenu by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onToggle
            )
        }
        
        if (enabled) {
            Box {
                TextButton(
                    onClick = { showFrequencyMenu = true },
                    modifier = Modifier.padding(start = 0.dp, top = 4.dp)
                ) {
                    Text("Frequency: ${frequency.name}")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                
                DropdownMenu(
                    expanded = showFrequencyMenu,
                    onDismissRequest = { showFrequencyMenu = false }
                ) {
                    NotificationFrequency.values().forEach { freq ->
                        DropdownMenuItem(
                            text = { Text(freq.name) },
                            onClick = {
                                onFrequencyChange(freq)
                                showFrequencyMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FinancialSettingsCard(
    currency: String,
    currencySymbol: String,
    employment: EmploymentSettings?,
    onCurrencyChange: (String, String) -> Unit,
    onEmploymentEdit: () -> Unit
) {
    var showCurrencyPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsItem(
                icon = Icons.Default.AttachMoney,
                title = "Currency",
                subtitle = "$currency ($currencySymbol)",
                onClick = { showCurrencyPicker = true }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.Work,
                title = "Employment",
                subtitle = employment?.employer ?: "Not set",
                onClick = onEmploymentEdit
            )
        }
    }
    
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCurrency = currency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { code, symbol ->
                onCurrencyChange(code, symbol)
                showCurrencyPicker = false
            }
        )
    }
}

@Composable
fun CategoriesCard(
    categories: List<CustomCategory>,
    onAddCategory: () -> Unit,
    onEditCategory: (CustomCategory) -> Unit,
    onArchiveCategory: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Custom Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddCategory) {
                    Icon(Icons.Default.Add, "Add category")
                }
            }
            
            if (categories.isEmpty()) {
                Text(
                    "No custom categories yet",
                    modifier = Modifier.padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categories.forEach { category ->
                    CategoryItem(
                        category = category,
                        onEdit = { onEditCategory(category) },
                        onArchive = { onArchiveCategory(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryItem(
    category: CustomCategory,
    onEdit: () -> Unit,
    onArchive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(category.colorHex)))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(category.name, fontWeight = FontWeight.Medium)
            Text(
                "${category.type.name} • ${category.transactionCount} transactions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit")
        }
        
        IconButton(onClick = onArchive) {
            Icon(Icons.Default.Archive, "Archive")
        }
    }
}

@Composable
fun AccountActionsCard(
    onExport: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsItem(
                icon = Icons.Default.FileDownload,
                title = "Export Data",
                subtitle = "Download all your data as JSON",
                onClick = onExport
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.Logout,
                title = "Sign Out",
                subtitle = "Sign out of your account",
                onClick = onSignOut
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.DeleteForever,
                title = "Delete Account",
                subtitle = "Permanently delete your account",
                onClick = onDeleteAccount,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun HelpInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = "1.0.0 (Beta) • Build 7",
                onClick = {}
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.Help,
                title = "Help & Support",
                subtitle = "Get help or contact us",
                onClick = { /* TODO: Open help */ }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingsItem(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                subtitle = "Read our privacy policy",
                onClick = { /* TODO: Open privacy policy */ }
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) tint else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant 
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        
        if (enabled) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Utility function to save export
fun saveExportToFile(context: Context, json: String) {
    try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val filename = "BudgetTracker_Export_$timestamp.json"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(json.toByteArray())
                }
                Toast.makeText(context, "Exported to Downloads/$filename", Toast.LENGTH_LONG).show()
            }
        } else {
            val file = java.io.File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                filename
            )
            file.writeText(json)
            Toast.makeText(context, "Exported to ${file.absolutePath}", Toast.LENGTH_LONG).show()
        }
        
        // Share intent
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, json)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share export"))
        
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

