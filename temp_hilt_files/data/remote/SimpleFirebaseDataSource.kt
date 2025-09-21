package com.budgettracker.core.data.remote

import com.budgettracker.core.domain.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Simple Firebase data source without dependency injection
 */
class SimpleFirebaseDataSource {
    
    private val firestore = FirebaseFirestore.getInstance()
    
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
}
