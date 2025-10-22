package com.budgettracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.budgettracker.navigation.BudgetTrackerNavigation
import com.budgettracker.navigation.BudgetTrackerBottomNavigation
import com.budgettracker.navigation.BudgetTrackerDestinations
import com.budgettracker.ui.theme.BudgetTrackerTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

/**
 * Main activity for Budget Tracker app
 */
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    // Register for notification permission result (Android 13+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
            requestFCMToken()
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Request notification permission and FCM token
        requestNotificationPermissionAndToken()
        
        setContent {
            BudgetTrackerTheme {
                BudgetTrackerApp()
            }
        }
    }
    
    /**
     * Request notification permission (Android 13+) and FCM token
     */
    private fun requestNotificationPermissionAndToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                    requestFCMToken()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show explanation to user why we need this permission
                    Log.d(TAG, "Showing permission rationale")
                    // For now, request permission anyway
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    Log.d(TAG, "Requesting notification permission")
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below don't need runtime notification permission
            Log.d(TAG, "Android < 13, notification permission not required")
            requestFCMToken()
        }
    }
    
    /**
     * Request FCM token and save to Firestore
     */
    private fun requestFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token retrieved: $token")
                saveFCMTokenToFirestore(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get FCM token", e)
            }
    }
    
    /**
     * Save FCM token to Firestore for Cloud Functions to use
     */
    private fun saveFCMTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not authenticated, cannot save FCM token")
            return
        }
        
        val db = FirebaseFirestore.getInstance()
        
        // Save FCM token to user document
        db.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved to Firestore for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save FCM token, trying to create document", e)
                
                // If document doesn't exist, create it
                db.collection("users")
                    .document(userId)
                    .set(mapOf(
                        "fcmToken" to token,
                        "createdAt" to System.currentTimeMillis()
                    ))
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token saved to new user document")
                    }
                    .addOnFailureListener { e2 ->
                        Log.e(TAG, "Failed to create user document with FCM token", e2)
                    }
            }
    }
}

/**
 * Main app composable
 */
@Composable
fun BudgetTrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Show bottom navigation only for main app screens
    val showBottomNav = currentRoute in listOf(
        BudgetTrackerDestinations.DASHBOARD_ROUTE,
        BudgetTrackerDestinations.TRANSACTIONS_ROUTE,
        BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE,
        BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE
    )
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    BudgetTrackerBottomNavigation(navController)
                }
            }
        ) { paddingValues ->
            BudgetTrackerNavigation(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                startDestination = BudgetTrackerDestinations.SPLASH_ROUTE // Start with splash screen
            )
        }
    }
}
