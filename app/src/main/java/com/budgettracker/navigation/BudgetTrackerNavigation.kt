package com.budgettracker.navigation

import androidx.compose.runtime.Composable
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

/**
 * Main navigation component for Budget Tracker app
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
                }
            )
        }
        
        composable(BudgetTrackerDestinations.REGISTER_ROUTE) {
            RegisterScreen(
                onNavigateToDashboard = {
                    navController.navigate(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
                        popUpTo(BudgetTrackerDestinations.REGISTER_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
            // TODO: OnboardingScreen()
        }
        
        // Main App Flow
        composable(BudgetTrackerDestinations.DASHBOARD_ROUTE) {
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
        
        composable(BudgetTrackerDestinations.TRANSACTIONS_ROUTE) {
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
                        date = Date()
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
        
        composable(BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE) {
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
        composable(BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE) {
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
