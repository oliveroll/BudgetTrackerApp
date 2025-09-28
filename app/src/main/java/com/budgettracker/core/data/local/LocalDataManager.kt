package com.budgettracker.core.data.local

import android.content.Context
import android.content.SharedPreferences
import com.budgettracker.core.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Local data manager for storing user data locally
 * This will work immediately while Firebase is being configured
 */
class LocalDataManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("budget_data", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_BUDGETS = "budgets"
        private const val KEY_SAVINGS_GOALS = "savings_goals"
        private const val KEY_USER_PROFILE = "user_profile"
    }
    
    // Transaction operations
    fun saveTransaction(transaction: Transaction): Result<String> {
        return try {
            val transactions = getTransactions().toMutableList()
            transactions.add(transaction)
            val json = gson.toJson(transactions)
            prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
            Result.success(transaction.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getTransactions(): List<Transaction> {
        return try {
            val json = prefs.getString(KEY_TRANSACTIONS, null)
            if (json != null) {
                val type = object : TypeToken<List<Transaction>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                // Return sample data for demo
                getSampleTransactions()
            }
        } catch (e: Exception) {
            getSampleTransactions()
        }
    }
    
    // Savings Goal operations
    fun saveSavingsGoal(goal: SavingsGoal): Result<String> {
        return try {
            val goals = getSavingsGoals().toMutableList()
            goals.add(goal)
            val json = gson.toJson(goals)
            prefs.edit().putString(KEY_SAVINGS_GOALS, json).apply()
            Result.success(goal.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getSavingsGoals(): List<SavingsGoal> {
        return try {
            val json = prefs.getString(KEY_SAVINGS_GOALS, null)
            if (json != null) {
                val type = object : TypeToken<List<SavingsGoal>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } else {
                getSampleGoals()
            }
        } catch (e: Exception) {
            getSampleGoals()
        }
    }
    
    // User Profile operations
    fun saveUserProfile(profile: UserProfile): Result<String> {
        return try {
            val json = gson.toJson(profile)
            prefs.edit().putString(KEY_USER_PROFILE, json).apply()
            Result.success(profile.userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getUserProfile(): UserProfile? {
        return try {
            val json = prefs.getString(KEY_USER_PROFILE, null)
            if (json != null) {
                gson.fromJson(json, UserProfile::class.java)
            } else null
        } catch (e: Exception) {
            null
        }
    }
    
    // Clear all data
    fun clearAllData(): Result<Unit> {
        return try {
            prefs.edit().clear().apply()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Sample data for demo
    private fun getSampleTransactions(): List<Transaction> {
        return listOf(
            Transaction(
                id = "1",
                userId = "demo_user",
                amount = 2523.88,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Salary Deposit - Ixana Quasistatics",
                date = Date(),
                notes = "Bi-weekly salary"
            ),
            Transaction(
                id = "2",
                userId = "demo_user",
                amount = 475.0,
                category = TransactionCategory.LOAN_PAYMENT,
                type = TransactionType.EXPENSE,
                description = "German Student Loan Payment",
                date = Date(System.currentTimeMillis() - 86400000), // Yesterday
                notes = "€450 monthly payment"
            ),
            Transaction(
                id = "3",
                userId = "demo_user",
                amount = 75.50,
                category = TransactionCategory.GROCERIES,
                type = TransactionType.EXPENSE,
                description = "Grocery Shopping - Walmart",
                date = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                notes = "Weekly groceries"
            )
        )
    }
    
    private fun getSampleGoals(): List<SavingsGoal> {
        return listOf(
            SavingsGoal(
                id = "goal_1",
                userId = "demo_user",
                name = "Emergency Fund",
                description = "6 months of expenses for OPT visa security",
                targetAmount = 16000.0,
                currentAmount = 0.0,
                deadline = Date(System.currentTimeMillis() + 15552000000L), // 6 months
                priority = Priority.CRITICAL,
                monthlyContribution = 800.0,
                category = GoalCategory.EMERGENCY_FUND
            ),
            SavingsGoal(
                id = "goal_2",
                userId = "demo_user",
                name = "Roth IRA 2025",
                description = "Tax-free retirement savings",
                targetAmount = 7000.0,
                currentAmount = 0.0,
                deadline = Date(System.currentTimeMillis() + 31104000000L), // 12 months
                priority = Priority.HIGH,
                monthlyContribution = 583.0,
                category = GoalCategory.RETIREMENT
            )
        )
    }
}


