package com.budgettracker.core.data.local

import android.content.Context
import android.content.SharedPreferences
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Singleton data store with Firebase persistence
 */
object TransactionDataStore {
    
    private var _transactions = mutableListOf<Transaction>()
    private val parsedDocuments = mutableSetOf<String>()
    private var isInitialized = false
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "demo_user"
    }
    
    /**
     * Initialize and load transactions from Firebase
     */
    suspend fun initializeFromFirebase() {
        if (isInitialized) return
        
        try {
            val userId = getCurrentUserId()
            android.util.Log.d("TransactionDataStore", "Loading transactions from Firebase for user: $userId")
            
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
            
            val firebaseTransactions = snapshot.documents.mapNotNull { doc ->
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
                    android.util.Log.e("TransactionDataStore", "Error parsing Firebase transaction: ${e.message}")
                    null
                }
            }
            
            if (firebaseTransactions.isNotEmpty()) {
                _transactions.clear()
                _transactions.addAll(firebaseTransactions)
                android.util.Log.d("TransactionDataStore", "Loaded ${firebaseTransactions.size} transactions from Firebase")
            } else {
                android.util.Log.d("TransactionDataStore", "No Firebase transactions found, initializing with demo data")
                initializeWithDemoData()
                // Save demo data to Firebase
                saveDemoDataToFirebase()
            }
            
            isInitialized = true
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error loading from Firebase: ${e.message}")
            initializeWithDemoData()
            isInitialized = true
        }
    }
    
    /**
     * Get all transactions sorted by date (newest first)
     */
    fun getTransactions(): List<Transaction> {
        return _transactions.sortedByDescending { it.date }
    }
    
    /**
     * Add a single transaction and save to Firebase
     */
    fun addTransaction(transaction: Transaction) {
        _transactions.add(transaction)
        android.util.Log.d("TransactionDataStore", "Added transaction: ${transaction.description} - ${transaction.amount}")
        
        // Save to Firebase in background
        CoroutineScope(Dispatchers.IO).launch {
            saveTransactionToFirebase(transaction)
        }
    }
    
    /**
     * Add multiple transactions (from PDF parsing)
     */
    fun addTransactions(transactions: List<Transaction>, documentHash: String? = null): Int {
        // Check if document was already parsed
        if (documentHash != null && parsedDocuments.contains(documentHash)) {
            android.util.Log.d("TransactionDataStore", "Document already parsed: $documentHash")
            return 0
        }
        
        var addedCount = 0
        for (transaction in transactions) {
            // Check for duplicates based on description, amount, and date
            val isDuplicate = _transactions.any { existing ->
                existing.description == transaction.description &&
                existing.amount == transaction.amount &&
                kotlin.math.abs(existing.date.time - transaction.date.time) < 86400000 // Within 24 hours
            }
            
            if (!isDuplicate) {
                _transactions.add(transaction)
                addedCount++
            }
        }
        
        // Mark document as parsed
        if (documentHash != null) {
            parsedDocuments.add(documentHash)
        }
        
        // Save new transactions to Firebase in background
        if (addedCount > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                saveTransactionsToFirebase(transactions.takeLast(addedCount))
            }
        }
        
        android.util.Log.d("TransactionDataStore", "Added $addedCount new transactions out of ${transactions.size}")
        return addedCount
    }
    
    /**
     * Update existing transaction and save to Firebase
     */
    fun updateTransaction(updatedTransaction: Transaction) {
        val index = _transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            _transactions[index] = updatedTransaction
            android.util.Log.d("TransactionDataStore", "Updated transaction: ${updatedTransaction.description}")
            
            // Update in Firebase
            CoroutineScope(Dispatchers.IO).launch {
                updateTransactionInFirebase(updatedTransaction)
            }
        }
    }
    
    /**
     * Delete transaction and remove from Firebase
     */
    fun deleteTransaction(transactionId: String) {
        _transactions.removeAll { it.id == transactionId }
        android.util.Log.d("TransactionDataStore", "Deleted transaction: $transactionId")
        
        // Delete from Firebase
        CoroutineScope(Dispatchers.IO).launch {
            deleteTransactionFromFirebase(transactionId)
        }
    }
    
    /**
     * Get transactions for specific month/year sorted by date (newest first)
     */
    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return _transactions.filter { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }.sortedByDescending { it.date }
    }
    
    /**
     * Check if document was already parsed
     */
    fun isDocumentParsed(documentHash: String): Boolean {
        return parsedDocuments.contains(documentHash)
    }
    
    /**
     * Clear all data (for testing)
     */
    fun clearAllData() {
        _transactions.clear()
        parsedDocuments.clear()
        android.util.Log.d("TransactionDataStore", "Cleared all data")
    }
    
    /**
     * Initialize with demo data
     */
    private fun initializeWithDemoData() {
        _transactions.addAll(
            listOf(
                Transaction(
                    id = "demo_1",
                    userId = "demo_user",
                    amount = 2523.88,
                    category = TransactionCategory.SALARY,
                    type = TransactionType.INCOME,
                    description = "Salary Deposit - Ixana Quasistatics",
                    date = Date(),
                    notes = "Bi-weekly salary deposit"
                ),
                Transaction(
                    id = "demo_2",
                    userId = "demo_user",
                    amount = 475.0,
                    category = TransactionCategory.LOAN_PAYMENT,
                    type = TransactionType.EXPENSE,
                    description = "German Student Loan Payment",
                    date = Date(System.currentTimeMillis() - 86400000),
                    notes = "€450 monthly payment"
                ),
                Transaction(
                    id = "demo_3",
                    userId = "demo_user",
                    amount = 75.50,
                    category = TransactionCategory.GROCERIES,
                    type = TransactionType.EXPENSE,
                    description = "Grocery Shopping - Walmart",
                    date = Date(System.currentTimeMillis() - 3600000),
                    notes = "Weekly groceries"
                ),
                Transaction(
                    id = "demo_4",
                    userId = "demo_user",
                    amount = 1200.0,
                    category = TransactionCategory.RENT,
                    type = TransactionType.EXPENSE,
                    description = "Monthly Rent Payment",
                    date = Date(System.currentTimeMillis() - 172800000),
                    notes = "Apartment rent"
                ),
                // Add some from previous months for testing
                Transaction(
                    id = "demo_5",
                    userId = "demo_user",
                    amount = 2523.88,
                    category = TransactionCategory.SALARY,
                    type = TransactionType.INCOME,
                    description = "Salary Deposit - August",
                    date = Date(System.currentTimeMillis() - 86400000L * 30),
                    notes = "Previous month salary"
                ),
                Transaction(
                    id = "demo_6",
                    userId = "demo_user",
                    amount = 150.0,
                    category = TransactionCategory.INSURANCE,
                    type = TransactionType.EXPENSE,
                    description = "Car Insurance - August",
                    date = Date(System.currentTimeMillis() - 86400000L * 32),
                    notes = "Monthly car insurance"
                )
            )
        )
        android.util.Log.d("TransactionDataStore", "Initialized with ${_transactions.size} demo transactions")
    }
    
    /**
     * Save single transaction to Firebase
     */
    private suspend fun saveTransactionToFirebase(transaction: Transaction) {
        try {
            val transactionData = mapOf(
                "userId" to getCurrentUserId(),
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
            
            firestore.collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()
            
            android.util.Log.d("TransactionDataStore", "Saved transaction to Firebase: ${transaction.id}")
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error saving transaction to Firebase: ${e.message}")
        }
    }
    
    /**
     * Save multiple transactions to Firebase
     */
    private suspend fun saveTransactionsToFirebase(transactions: List<Transaction>) {
        for (transaction in transactions) {
            saveTransactionToFirebase(transaction)
        }
    }
    
    /**
     * Update transaction in Firebase
     */
    private suspend fun updateTransactionInFirebase(transaction: Transaction) {
        try {
            val updateData = mapOf(
                "amount" to transaction.amount,
                "category" to transaction.category.name,
                "type" to transaction.type.name,
                "description" to transaction.description,
                "notes" to transaction.notes,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection("transactions")
                .document(transaction.id)
                .update(updateData)
                .await()
            
            android.util.Log.d("TransactionDataStore", "Updated transaction in Firebase: ${transaction.id}")
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error updating transaction in Firebase: ${e.message}")
        }
    }
    
    /**
     * Delete transaction from Firebase (soft delete)
     */
    private suspend fun deleteTransactionFromFirebase(transactionId: String) {
        try {
            firestore.collection("transactions")
                .document(transactionId)
                .update(
                    mapOf(
                        "isDeleted" to true,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            android.util.Log.d("TransactionDataStore", "Deleted transaction from Firebase: $transactionId")
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error deleting transaction from Firebase: ${e.message}")
        }
    }
    
    /**
     * Save demo data to Firebase on first run
     */
    private suspend fun saveDemoDataToFirebase() {
        try {
            android.util.Log.d("TransactionDataStore", "Saving demo data to Firebase")
            saveTransactionsToFirebase(_transactions)
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error saving demo data to Firebase: ${e.message}")
        }
    }
}
