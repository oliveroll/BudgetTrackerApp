package com.budgettracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
// import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Bottom navigation bar for main app screens
 */
@Composable
fun BudgetTrackerBottomNavigation(
    navController: NavController,
    items: List<BottomNavItem> = bottomNavItems
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    ) 
                },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    // Simple navigation - just navigate to the route
                    navController.navigate(item.route) {
                        // Clear back stack to avoid building up destinations
                        popUpTo(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
                            inclusive = item.route == BudgetTrackerDestinations.DASHBOARD_ROUTE
                        }
                        // Avoid multiple copies
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/**
 * Bottom navigation item data class
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation items (Settings moved to header)
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = BudgetTrackerDestinations.DASHBOARD_ROUTE,
        icon = Icons.Default.Dashboard,
        label = "Home"
    ),
    BottomNavItem(
        route = BudgetTrackerDestinations.TRANSACTIONS_ROUTE,
        icon = Icons.Default.Receipt,
        label = "Transactions"
    ),
    BottomNavItem(
        route = BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE,
        icon = Icons.Default.PieChart,
        label = "Budget"
    ),
    BottomNavItem(
        route = BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE,
        icon = Icons.Default.Savings,
        label = "Goals"
    )
)
