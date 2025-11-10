package com.budgettracker.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import java.util.Date
import com.budgettracker.features.auth.presentation.SplashScreen
import com.budgettracker.features.auth.presentation.LoginScreen
import com.budgettracker.features.auth.presentation.RegisterScreen
import com.budgettracker.features.dashboard.presentation.MobileFriendlyDashboard
import com.budgettracker.features.dashboard.presentation.ModernDashboardScreen
import com.budgettracker.features.transactions.presentation.AddTransactionScreen
import com.budgettracker.features.transactions.presentation.TransactionListScreen
import com.budgettracker.features.budget.presentation.EnhancedBudgetScreen
import com.budgettracker.features.budget.presentation.BudgetOverviewScreen
import com.budgettracker.features.budget.presentation.EnhancedBudgetOverviewScreen
import com.budgettracker.features.budget.presentation.MobileBudgetOverviewScreen
import com.budgettracker.features.budget.presentation.SubscriptionsScreen
import com.budgettracker.features.budget.presentation.RemindersScreen
import com.budgettracker.features.savings.presentation.SavingsGoalsScreen
import com.budgettracker.features.savings.presentation.AddGoalScreen
import com.budgettracker.features.savings.presentation.GoalsScreen
import com.budgettracker.features.financialgoals.presentation.FinancialGoalsMainScreen
import com.budgettracker.features.reports.presentation.ReportsScreen
import com.budgettracker.features.settings.presentation.EnhancedSettingsScreen
import com.budgettracker.features.onboarding.presentation.OnboardingFlow

/**
 * Tab routes in order for determining animation direction
 */
private val tabRoutes = listOf(
    BudgetTrackerDestinations.DASHBOARD_ROUTE,
    BudgetTrackerDestinations.TRANSACTIONS_ROUTE,
    BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE,
    BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE
)

/**
 * Get enter transition for tab navigation
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.getTabEnterTransition(): EnterTransition {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    
    val initialIndex = tabRoutes.indexOf(initialRoute)
    val targetIndex = tabRoutes.indexOf(targetRoute)
    
    // Only animate if both routes are tabs
    if (initialIndex == -1 || targetIndex == -1) {
        return fadeIn(animationSpec = tween(300))
    }
    
    return if (targetIndex > initialIndex) {
        // Moving forward (right to left)
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(400)
        ) + fadeIn(animationSpec = tween(300))
    } else {
        // Moving backward (left to right)
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(400)
        ) + fadeIn(animationSpec = tween(300))
    }
}

/**
 * Get exit transition for tab navigation
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.getTabExitTransition(): ExitTransition {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    
    val initialIndex = tabRoutes.indexOf(initialRoute)
    val targetIndex = tabRoutes.indexOf(targetRoute)
    
    // Only animate if both routes are tabs
    if (initialIndex == -1 || targetIndex == -1) {
        return fadeOut(animationSpec = tween(300))
    }
    
    return if (targetIndex > initialIndex) {
        // Moving forward (right to left)
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(400)
        ) + fadeOut(animationSpec = tween(300))
    } else {
        // Moving backward (left to right)
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(400)
        ) + fadeOut(animationSpec = tween(300))
    }
}

/**
 * Main navigation component for Budget Tracker app with animated transitions
 */
@Composable
fun BudgetTrackerNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = BudgetTrackerDestinations.SPLASH_ROUTE,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth Flow
        composable(BudgetTrackerDestinations.SPLASH_ROUTE) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(BudgetTrackerDestinations.LOGIN_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    // FIXED: Resume onboarding if incomplete
                    navController.navigate(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        composable(BudgetTrackerDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(BudgetTrackerDestinations.REGISTER_ROUTE)
                },
                onNavigateToOnboarding = {
                    // FIXED: New Google users go to onboarding
                    navController.navigate(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        composable(BudgetTrackerDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateToDashboard = {
                    // Navigate to onboarding for new users
                    navController.navigate(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
            OnboardingFlow(
                onComplete = {
                    navController.navigate(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.ONBOARDING_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        // Main App Flow - Tab screens with animations
        composable(
            route = BudgetTrackerDestinations.DASHBOARD_ROUTE,
            enterTransition = { getTabEnterTransition() },
            exitTransition = { getTabExitTransition() }
        ) {
            ModernDashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(BudgetTrackerDestinations.ADD_TRANSACTION_ROUTE)
                },
                onNavigateToTransactions = {
                    navController.navigate(BudgetTrackerDestinations.TRANSACTIONS_ROUTE)
                },
                onNavigateToBudget = {
                    navController.navigate(BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE)
                },
                onNavigateToGoals = {
                    navController.navigate(BudgetTrackerDestinations.SAVINGS_GOALS_ROUTE)
                },
                onNavigateToFinancialGoals = {
                    navController.navigate(BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE)
                },
                onNavigateToSettings = {
                    navController.navigate(BudgetTrackerDestinations.SETTINGS_ROUTE)
                }
            )
        }
        
        composable(
            route = BudgetTrackerDestinations.TRANSACTIONS_ROUTE,
            enterTransition = { getTabEnterTransition() },
            exitTransition = { getTabExitTransition() }
        ) {
            TransactionListScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(BudgetTrackerDestinations.ADD_TRANSACTION_ROUTE)
                }
            )
        }
        
        composable(BudgetTrackerDestinations.ADD_TRANSACTION_ROUTE) {
            AddTransactionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveTransaction = { amount, description, category, type, notes ->
                    // Create transaction and save to Firebase
                    val transaction = Transaction(
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        description = description,
                        category = TransactionCategory.values().find { it.displayName == category } ?: TransactionCategory.MISCELLANEOUS,
                        type = TransactionType.valueOf(type),
                        notes = notes.ifBlank { null },
                        date = java.time.LocalDate.now()
                    )
                    // This will be handled by the screen's repository
                }
            )
        }
        
        composable(
            route = "${BudgetTrackerDestinations.EDIT_TRANSACTION_ROUTE}/{transactionId}",
            arguments = BudgetTrackerDestinations.editTransactionArguments
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            // TODO: EditTransactionScreen(transactionId)
        }
        
        composable(
            route = BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE,
            enterTransition = { getTabEnterTransition() },
            exitTransition = { getTabExitTransition() }
        ) {
            MobileBudgetOverviewScreen()
        }
        
        composable(BudgetTrackerDestinations.BUDGET_SETUP_ROUTE) {
            // TODO: BudgetSetupScreen()
        }
        
        composable(BudgetTrackerDestinations.SUBSCRIPTIONS_ROUTE) {
            SubscriptionsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BudgetTrackerDestinations.REMINDERS_ROUTE) {
            RemindersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BudgetTrackerDestinations.SAVINGS_GOALS_ROUTE) {
            GoalsScreen(
                onNavigateToAddGoal = {
                    navController.navigate(BudgetTrackerDestinations.ADD_GOAL_ROUTE)
                }
            )
        }
        
        composable(BudgetTrackerDestinations.ADD_GOAL_ROUTE) {
            AddGoalScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSaveGoal = { name, target, monthly, description, category, priority ->
                    // TODO: Save goal to database
                    // For now, just navigate back
                }
            )
        }
        
        composable(
            route = "${BudgetTrackerDestinations.EDIT_GOAL_ROUTE}/{goalId}",
            arguments = BudgetTrackerDestinations.editGoalArguments
        ) { backStackEntry ->
            val goalId = backStackEntry.arguments?.getString("goalId") ?: ""
            // TODO: EditGoalScreen(goalId)
        }
        
        // Financial Goals - New Feature
        composable(
            route = BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE,
            enterTransition = { getTabEnterTransition() },
            exitTransition = { getTabExitTransition() }
        ) {
            FinancialGoalsMainScreen()
        }
        
        composable(BudgetTrackerDestinations.INVESTMENTS_ROUTE) {
            // TODO: InvestmentTrackerScreen()
        }
        
        composable(BudgetTrackerDestinations.LOANS_ROUTE) {
            // TODO: LoanTrackerScreen()
        }
        
        composable(BudgetTrackerDestinations.REPORTS_ROUTE) {
            ReportsScreen()
        }
        
        composable(BudgetTrackerDestinations.SETTINGS_ROUTE) {
            EnhancedSettingsScreen(
                onNavigateToLogin = {
                    navController.navigate(BudgetTrackerDestinations.LOGIN_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.DASHBOARD_ROUTE) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BudgetTrackerDestinations.PROFILE_ROUTE) {
            // TODO: ProfileScreen()
        }
    }
}
