package com.budgettracker.core.data.remote

import com.budgettracker.core.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase data source for remote operations
 */
@Singleton
class FirebaseDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Collections
    private val usersCollection = firestore.collection("users")
    private val transactionsCollection = firestore.collection("transactions")
    private val budgetsCollection = firestore.collection("budgets")
    private val savingsGoalsCollection = firestore.collection("savingsGoals")
    
    // Transaction operations
    suspend fun getTransactions(userId: String): List<Transaction> {
        return try {
            transactionsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Transaction::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getTransaction(id: String): Transaction? {
        return try {
            transactionsCollection
                .document(id)
                .get()
                .await()
                .toObject(Transaction::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun insertTransaction(transaction: Transaction): Result<String> {
        return try {
            transactionsCollection
                .document(transaction.id)
                .set(transaction)
                .await()
            Result.success(transaction.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionsCollection
                .document(transaction.id)
                .set(transaction)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            transactionsCollection
                .document(id)
                .update("isDeleted", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Budget operations
    suspend fun getBudgets(userId: String): List<Budget> {
        return try {
            budgetsCollection
                .whereEqualTo("userId", userId)
                .orderBy("year", Query.Direction.DESCENDING)
                .orderBy("month", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Budget::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getBudget(id: String): Budget? {
        return try {
            budgetsCollection
                .document(id)
                .get()
                .await()
                .toObject(Budget::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun insertBudget(budget: Budget): Result<String> {
        return try {
            budgetsCollection
                .document(budget.id)
                .set(budget)
                .await()
            Result.success(budget.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateBudget(budget: Budget): Result<Unit> {
        return try {
            budgetsCollection
                .document(budget.id)
                .set(budget)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Savings Goal operations
    suspend fun getSavingsGoals(userId: String): List<SavingsGoal> {
        return try {
            savingsGoalsCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .await()
                .toObjects(SavingsGoal::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getSavingsGoal(id: String): SavingsGoal? {
        return try {
            savingsGoalsCollection
                .document(id)
                .get()
                .await()
                .toObject(SavingsGoal::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun insertSavingsGoal(goal: SavingsGoal): Result<String> {
        return try {
            savingsGoalsCollection
                .document(goal.id)
                .set(goal)
                .await()
            Result.success(goal.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateSavingsGoal(goal: SavingsGoal): Result<Unit> {
        return try {
            savingsGoalsCollection
                .document(goal.id)
                .set(goal)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // User Profile operations
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            usersCollection
                .document(userId)
                .get()
                .await()
                .toObject(UserProfile::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun insertUserProfile(profile: UserProfile): Result<String> {
        return try {
            usersCollection
                .document(profile.userId)
                .set(profile)
                .await()
            Result.success(profile.userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            usersCollection
                .document(profile.userId)
                .set(profile)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

