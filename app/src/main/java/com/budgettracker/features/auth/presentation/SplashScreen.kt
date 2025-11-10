package com.budgettracker.features.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.budgettracker.features.auth.data.HybridAuthManager
import com.budgettracker.ui.theme.Primary40
import com.budgettracker.ui.theme.Secondary40
import kotlinx.coroutines.delay

/**
 * Splash screen shown at app startup
 */
@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToOnboarding: () -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { HybridAuthManager(context) }
    
    // Check authentication status and navigate accordingly
    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        
        if (authManager.isSignedIn()) {
            // Wait for Firebase Auth to fully initialize
            var retries = 0
            val maxRetries = 10
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            
            android.util.Log.d("SplashScreen", "User reported as signed in, waiting for Firebase Auth...")
            
            // Wait until Firebase Auth currentUser is available
            while (auth.currentUser == null && retries < maxRetries) {
                android.util.Log.d("SplashScreen", "Firebase Auth not ready yet, waiting... (attempt ${retries + 1})")
                delay(200) // Wait 200ms between checks
                retries++
            }
            
            if (auth.currentUser == null) {
                android.util.Log.e("SplashScreen", "❌ Firebase Auth failed to initialize after ${maxRetries} attempts")
                onNavigateToLogin()
                return@LaunchedEffect
            }
            
            android.util.Log.d("SplashScreen", "✅ Firebase Auth ready! User: ${auth.currentUser?.uid}")
            
            // FIXED: Check if onboarding is completed
            val database = com.budgettracker.core.data.local.database.BudgetTrackerDatabase.getDatabase(context)
            val settingsRepository = com.budgettracker.features.settings.data.repository.SettingsRepository(
                userSettingsDao = database.userSettingsDao(),
                employmentSettingsDao = database.employmentSettingsDao(),
                customCategoryDao = database.customCategoryDao(),
                firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                auth = auth
            )
            
            val isOnboardingComplete = settingsRepository.isOnboardingCompleted()
            android.util.Log.d("SplashScreen", "User signed in. Onboarding complete: $isOnboardingComplete")
            
            if (isOnboardingComplete) {
                onNavigateToDashboard()
            } else {
                // User is authenticated but hasn't completed onboarding
                onNavigateToOnboarding()
            }
        } else {
            onNavigateToLogin()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary40, Secondary40)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon placeholder
            Card(
                modifier = Modifier.size(100.dp),
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
                        fontSize = 48.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Budget Tracker",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Smart Financial Management",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

