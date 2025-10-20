package com.budgettracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.budgettracker.navigation.BudgetTrackerNavigation
import com.budgettracker.navigation.BudgetTrackerBottomNavigation
import com.budgettracker.navigation.BudgetTrackerDestinations
import com.budgettracker.ui.theme.BudgetTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint

/**
 * Main activity for Budget Tracker app
 */
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            BudgetTrackerTheme {
                BudgetTrackerApp()
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
