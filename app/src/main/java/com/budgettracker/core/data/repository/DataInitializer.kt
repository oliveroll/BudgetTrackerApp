package com.budgettracker.core.data.repository

import com.budgettracker.core.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Data initializer to populate default financial data for new users
 */
class DataInitializer {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    suspend fun initializeAllUserData(): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
            
            // Initialize all collections with default data
            initializeFixedExpenses(userId)
            // Income sources already exist, skip for now
            initializeVariableExpenses(userId)
            initializeAccounts(userId)
            initializeSavingsGoals(userId)
            initializeTransactions(userId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun initializeFixedExpenses(userId: String) {
        val fixedExpenses = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Housing",
                "description" to "Rent",
                "amount" to 700.0,
                "dueDate" to "1st",
                "notes" to "Apartment in Indiana",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Utilities",
                "description" to "Electric/Water/Internet",
                "amount" to 100.0,
                "dueDate" to "15th",
                "notes" to "Average monthly",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Phone",
                "description" to "Mobile Plan (Prepaid)",
                "amount" to 25.0,
                "dueDate" to "-",
                "notes" to "$150/6 months (renew Feb)",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Subscription",
                "description" to "Spotify",
                "amount" to 5.99,
                "dueDate" to "10th",
                "notes" to "$11.99 after March",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Debt Payment",
                "description" to "German Student Loan",
                "amount" to 475.0,
                "dueDate" to "20th",
                "notes" to "€11,000 @ €450/month",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Transportation",
                "description" to "Public Transit/Uber Budget",
                "amount" to 100.0,
                "dueDate" to "-",
                "notes" to "No car",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Living",
                "description" to "Groceries",
                "amount" to 300.0,
                "dueDate" to "Weekly",
                "notes" to "$75/week",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
        )
        
        fixedExpenses.forEach { expense ->
            firestore.collection("fixedExpenses")
                .document(expense["id"] as String)
                .set(expense)
                .await()
        }
    }
    
    private suspend fun initializeVariableExpenses(userId: String) {
        val variableExpenses = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Dining Out",
                "budgetAmount" to 300.0,
                "spentAmount" to 0.0,
                "notes" to "Weekly limit: $75",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Entertainment",
                "budgetAmount" to 200.0,
                "spentAmount" to 0.0,
                "notes" to "Movies, events",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Personal Care",
                "budgetAmount" to 100.0,
                "spentAmount" to 0.0,
                "notes" to "Haircut, gym",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Clothing",
                "budgetAmount" to 150.0,
                "spentAmount" to 0.0,
                "notes" to "Seasonal needs",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Miscellaneous",
                "budgetAmount" to 200.0,
                "spentAmount" to 0.0,
                "notes" to "Amazon, unexpected",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "German Loan Extra",
                "budgetAmount" to 200.0,
                "spentAmount" to 0.0,
                "notes" to "Optional extra payment",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "category" to "Buffer/Overflow",
                "budgetAmount" to 614.01,
                "spentAmount" to 0.0,
                "notes" to "Unallocated flexibility",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
        )
        
        variableExpenses.forEach { expense ->
            firestore.collection("variableExpenseCategories")
                .document(expense["id"] as String)
                .set(expense)
                .await()
        }
    }
    
    private suspend fun initializeAccounts(userId: String) {
        val accounts = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Bills & Thrills Checking",
                "bankPlatform" to "Your Bank",
                "purpose" to "Daily expenses & bills",
                "currentBalance" to 0.0,
                "monthlyFlow" to 3470.0,
                "accountType" to "CHECKING",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Emergency Savings",
                "bankPlatform" to "High-Yield (Ally/Marcus)",
                "purpose" to "3-4 months expenses",
                "currentBalance" to 0.0,
                "monthlyFlow" to 800.0,
                "accountType" to "SAVINGS",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Roth IRA",
                "bankPlatform" to "Vanguard/Fidelity",
                "purpose" to "Tax-free retirement",
                "currentBalance" to 0.0,
                "monthlyFlow" to 583.0,
                "accountType" to "RETIREMENT",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Robinhood",
                "bankPlatform" to "Robinhood",
                "purpose" to "ETFs & fun investing",
                "currentBalance" to 0.0,
                "monthlyFlow" to 500.0,
                "accountType" to "INVESTMENT",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Travel Savings",
                "bankPlatform" to "Your Bank",
                "purpose" to "Vacation fund",
                "currentBalance" to 0.0,
                "monthlyFlow" to 117.0,
                "accountType" to "SAVINGS",
                "isActive" to true,
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
        )
        
        accounts.forEach { account ->
            firestore.collection("accounts")
                .document(account["id"] as String)
                .set(account)
                .await()
        }
    }
    
    private suspend fun initializeSavingsGoals(userId: String) {
        val goals = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Emergency Fund",
                "description" to "6 months of expenses for OPT visa security",
                "targetAmount" to 16000.0,
                "currentAmount" to 0.0,
                "deadline" to Date(System.currentTimeMillis() + 15552000000L), // 6 months
                "priority" to "CRITICAL",
                "monthlyContribution" to 800.0,
                "category" to "EMERGENCY_FUND",
                "isCompleted" to false,
                "isActive" to true,
                "color" to "#dc3545",
                "icon" to "🚨",
                "createdAt" to Date(),
                "updatedAt" to Date()
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "name" to "Roth IRA 2025",
                "description" to "Tax-free retirement savings",
                "targetAmount" to 7000.0,
                "currentAmount" to 0.0,
                "deadline" to Date(System.currentTimeMillis() + 31104000000L), // 12 months
                "priority" to "HIGH",
                "monthlyContribution" to 583.0,
                "category" to "RETIREMENT",
                "isCompleted" to false,
                "isActive" to true,
                "color" to "#6f42c1",
                "icon" to "👴",
                "createdAt" to Date(),
                "updatedAt" to Date()
            )
        )
        
        goals.forEach { goal ->
            firestore.collection("savingsGoals")
                .document(goal["id"] as String)
                .set(goal)
                .await()
        }
    }
    
    private suspend fun initializeTransactions(userId: String) {
        val transactions = listOf(
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "amount" to 2523.88,
                "category" to "SALARY",
                "type" to "INCOME",
                "description" to "Salary Deposit - Ixana Quasistatics",
                "date" to Date(),
                "isRecurring" to false,
                "recurringPeriod" to null,
                "tags" to emptyList<String>(),
                "attachmentUrl" to null,
                "location" to null,
                "notes" to "Bi-weekly salary",
                "createdAt" to Date(),
                "updatedAt" to Date(),
                "isDeleted" to false
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "amount" to 475.0,
                "category" to "LOAN_PAYMENT",
                "type" to "EXPENSE",
                "description" to "German Student Loan Payment",
                "date" to Date(System.currentTimeMillis() - 86400000), // Yesterday
                "isRecurring" to true,
                "recurringPeriod" to "MONTHLY",
                "tags" to listOf("loan", "germany"),
                "attachmentUrl" to null,
                "location" to null,
                "notes" to "€450 monthly payment",
                "createdAt" to Date(),
                "updatedAt" to Date(),
                "isDeleted" to false
            ),
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "userId" to userId,
                "amount" to 75.50,
                "category" to "GROCERIES",
                "type" to "EXPENSE",
                "description" to "Grocery Shopping - Walmart",
                "date" to Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                "isRecurring" to false,
                "recurringPeriod" to null,
                "tags" to listOf("groceries", "food"),
                "attachmentUrl" to null,
                "location" to null,
                "notes" to "Weekly groceries",
                "createdAt" to Date(),
                "updatedAt" to Date(),
                "isDeleted" to false
            )
        )
        
        transactions.forEach { transaction ->
            firestore.collection("transactions")
                .document(transaction["id"] as String)
                .set(transaction)
                .await()
        }
    }
}
