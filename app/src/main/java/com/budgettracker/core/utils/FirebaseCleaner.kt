package com.budgettracker.core.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility to completely clear Firebase database
 */
object FirebaseCleaner {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Completely clear all transactions from Firebase database
     */
    suspend fun clearAllTransactions(): Result<Int> {
        return try {
            val userId = auth.currentUser?.uid ?: "demo_user"
            android.util.Log.d("FirebaseCleaner", "🗑️ Starting to clear all transactions for user: $userId")
            
            // Get all transactions for the user
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            var deletedCount = 0
            
            // Delete each transaction document
            for (document in snapshot.documents) {
                try {
                    firestore.collection("transactions")
                        .document(document.id)
                        .delete()
                        .await()
                    
                    deletedCount++
                    android.util.Log.d("FirebaseCleaner", "🗑️ Deleted transaction: ${document.id}")
                    
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseCleaner", "Error deleting transaction ${document.id}: ${e.message}")
                }
            }
            
            android.util.Log.d("FirebaseCleaner", "✅ Successfully deleted $deletedCount transactions from Firebase")
            Result.success(deletedCount)
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseCleaner", "Error clearing transactions: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Clear all collections (nuclear option)
     */
    suspend fun clearAllData(): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: "demo_user"
            android.util.Log.d("FirebaseCleaner", "🗑️ NUCLEAR OPTION: Clearing ALL data for user: $userId")
            
            val collections = listOf("transactions", "budgets", "savingsGoals", "fixedExpenses")
            var totalDeleted = 0
            
            for (collectionName in collections) {
                try {
                    val snapshot = firestore.collection(collectionName)
                        .whereEqualTo("userId", userId)
                        .get()
                        .await()
                    
                    for (document in snapshot.documents) {
                        firestore.collection(collectionName)
                            .document(document.id)
                            .delete()
                            .await()
                        totalDeleted++
                    }
                    
                    android.util.Log.d("FirebaseCleaner", "🗑️ Cleared collection: $collectionName")
                    
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseCleaner", "Error clearing collection $collectionName: ${e.message}")
                }
            }
            
            android.util.Log.d("FirebaseCleaner", "✅ NUCLEAR CLEAR COMPLETE: Deleted $totalDeleted documents")
            Result.success("Deleted $totalDeleted documents from all collections")
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseCleaner", "Error in nuclear clear: ${e.message}")
            Result.failure(e)
        }
    }
}
