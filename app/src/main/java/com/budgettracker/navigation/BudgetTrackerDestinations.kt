package com.budgettracker.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * Destinations used in the Budget Tracker app
 */
object BudgetTrackerDestinations {
    
    // Auth Flow
    const val SPLASH_ROUTE = "splash"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val ONBOARDING_ROUTE = "onboarding"
    
    // Main App Flow
    const val DASHBOARD_ROUTE = "dashboard"
    
    // Transactions
    const val TRANSACTIONS_ROUTE = "transactions"
    const val ADD_TRANSACTION_ROUTE = "add_transaction"
    const val EDIT_TRANSACTION_ROUTE = "edit_transaction"
    
    // Budget
    const val BUDGET_OVERVIEW_ROUTE = "budget_overview"
    const val BUDGET_SETUP_ROUTE = "budget_setup"
    const val SUBSCRIPTIONS_ROUTE = "subscriptions"
    const val REMINDERS_ROUTE = "reminders"
    
    // Savings Goals
    const val SAVINGS_GOALS_ROUTE = "savings_goals"
    const val ADD_GOAL_ROUTE = "add_goal"
    const val EDIT_GOAL_ROUTE = "edit_goal"
    
    // Financial Goals (New Feature)
    const val FINANCIAL_GOALS_ROUTE = "financial_goals"
    
    // Investments & Loans
    const val INVESTMENTS_ROUTE = "investments"
    const val LOANS_ROUTE = "loans"
    
    // Reports & Settings
    const val REPORTS_ROUTE = "reports"
    const val SETTINGS_ROUTE = "settings"
    const val PROFILE_ROUTE = "profile"
    
    // Navigation arguments
    val editTransactionArguments: List<NamedNavArgument> = listOf(
        navArgument("transactionId") {
            type = NavType.StringType
        }
    )
    
    val editGoalArguments: List<NamedNavArgument> = listOf(
        navArgument("goalId") {
            type = NavType.StringType
        }
    )
}




