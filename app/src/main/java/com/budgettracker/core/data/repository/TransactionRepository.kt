package com.budgettracker.core.data.repository

import android.util.Log
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing transactions with Firebase Firestore
 */
@Singleton
class TransactionRepository @Inject constructor() {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "demo_user"
    }
    
    /**
     * Get all transactions as a Flow
     */
    fun getTransactionsFlow(): Flow<List<Transaction>> = callbackFlow {
        val userId = getCurrentUserId()
        
        Log.d("TransactionRepository", "Setting up transactions listener for user: $userId")
        
        val listener = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isDeleted", false)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepository", "Error listening to transactions: ${error.message}")
                    // Send demo data as fallback
                    trySend(getDemoTransactions(userId))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    try {
                        val transactions = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                Transaction(
                                    id = doc.id,
                                    userId = data["userId"] as? String ?: userId,
                                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                                    category = TransactionCategory.valueOf(
                                        data["category"] as? String ?: "MISCELLANEOUS"
                                    ),
                                    type = TransactionType.valueOf(
                                        data["type"] as? String ?: "EXPENSE"
                                    ),
                                    description = data["description"] as? String ?: "",
                                    date = (data["date"] as? com.google.firebase.Timestamp)?.toDate() ?: Date(),
                                    notes = data["notes"] as? String,
                                    isRecurring = data["isRecurring"] as? Boolean ?: false
                                )
                            } catch (e: Exception) {
                                Log.e("TransactionRepository", "Error parsing transaction: ${e.message}")
                                null
                            }
                        }
                        
                        Log.d("TransactionRepository", "Received ${transactions.size} transactions from Firestore")
                        
                        // If no transactions in Firestore, initialize with demo data
                        if (transactions.isEmpty()) {
                            Log.d("TransactionRepository", "No transactions found, initializing with demo data")
                            val demoTransactions = getDemoTransactions(userId)
                            trySend(demoTransactions)
                            // Note: Demo data will be saved on first save operation
                        } else {
                            trySend(transactions)
                        }
                        
                    } catch (e: Exception) {
                        Log.e("TransactionRepository", "Error processing transactions: ${e.message}")
                        trySend(getDemoTransactions(userId))
                    }
                } else {
                    Log.d("TransactionRepository", "Snapshot is null, sending demo data")
                    trySend(getDemoTransactions(userId))
                }
            }
        
        awaitClose { 
            Log.d("TransactionRepository", "Removing transactions listener")
            listener.remove() 
        }
    }
    
    /**
     * Save transaction to Firebase
     */
    suspend fun saveTransaction(transaction: Transaction): Result<String> {
        return try {
            val userId = getCurrentUserId()
            val transactionData = mapOf(
                "userId" to userId,
                "amount" to transaction.amount,
                "category" to transaction.category.name,
                "type" to transaction.type.name,
                "description" to transaction.description,
                "date" to com.google.firebase.Timestamp(transaction.date),
                "notes" to transaction.notes,
                "isRecurring" to transaction.isRecurring,
                "isDeleted" to false,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            Log.d("TransactionRepository", "Saving transaction: ${transaction.description}")
            
            firestore.collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()
            
            Log.d("TransactionRepository", "Successfully saved transaction: ${transaction.id}")
            Result.success(transaction.id)
            
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error saving transaction: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Update existing transaction
     */
    suspend fun updateTransaction(transaction: Transaction): Result<String> {
        return try {
            val transactionData = mapOf(
                "amount" to transaction.amount,
                "category" to transaction.category.name,
                "type" to transaction.type.name,
                "description" to transaction.description,
                "notes" to transaction.notes,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            Log.d("TransactionRepository", "Updating transaction: ${transaction.id}")
            
            firestore.collection("transactions")
                .document(transaction.id)
                .update(transactionData)
                .await()
            
            Log.d("TransactionRepository", "Successfully updated transaction: ${transaction.id}")
            Result.success(transaction.id)
            
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transaction: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Delete transaction (soft delete)
     */
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            Log.d("TransactionRepository", "Deleting transaction: $transactionId")
            
            firestore.collection("transactions")
                .document(transactionId)
                .update(
                    mapOf(
                        "isDeleted" to true,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            Log.d("TransactionRepository", "Successfully deleted transaction: $transactionId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transaction: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Save multiple transactions (for PDF uploads)
     */
    suspend fun saveTransactions(transactions: List<Transaction>): Result<Int> {
        return try {
            var savedCount = 0
            
            Log.d("TransactionRepository", "Saving ${transactions.size} transactions to Firebase")
            
            for (transaction in transactions) {
                val result = saveTransaction(transaction)
                if (result.isSuccess) {
                    savedCount++
                }
            }
            
            Log.d("TransactionRepository", "Successfully saved $savedCount out of ${transactions.size} transactions")
            Result.success(savedCount)
            
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error saving transactions: ${e.message}")
            Result.failure(e)
        }
    }
    
    
    /**
     * Get demo transactions
     */
    private fun getDemoTransactions(userId: String): List<Transaction> {
        return listOf(
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 2523.88,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Salary Deposit - Ixana Quasistatics",
                date = Date(),
                notes = "Bi-weekly salary deposit"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 475.0,
                category = TransactionCategory.LOAN_PAYMENT,
                type = TransactionType.EXPENSE,
                description = "German Student Loan Payment",
                date = Date(System.currentTimeMillis() - 86400000),
                notes = "€450 monthly payment"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 75.50,
                category = TransactionCategory.GROCERIES,
                type = TransactionType.EXPENSE,
                description = "Grocery Shopping - Walmart",
                date = Date(System.currentTimeMillis() - 3600000),
                notes = "Weekly groceries"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 1200.0,
                category = TransactionCategory.RENT,
                type = TransactionType.EXPENSE,
                description = "Monthly Rent Payment",
                date = Date(System.currentTimeMillis() - 172800000),
                notes = "Apartment rent"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 45.0,
                category = TransactionCategory.PHONE,
                type = TransactionType.EXPENSE,
                description = "Phone Plan - Verizon",
                date = Date(System.currentTimeMillis() - 259200000),
                notes = "Monthly phone bill"
            ),
            // Add some transactions from previous months for testing
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 2523.88,
                category = TransactionCategory.SALARY,
                type = TransactionType.INCOME,
                description = "Salary Deposit - August",
                date = Date(System.currentTimeMillis() - 86400000L * 30), // Last month
                notes = "Previous month salary"
            ),
            Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = 150.0,
                category = TransactionCategory.INSURANCE,
                type = TransactionType.EXPENSE,
                description = "Car Insurance - August",
                date = Date(System.currentTimeMillis() - 86400000L * 32), // Last month
                notes = "Monthly car insurance"
            )
        )
    }
}
