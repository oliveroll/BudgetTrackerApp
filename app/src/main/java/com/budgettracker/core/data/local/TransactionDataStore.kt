package com.budgettracker.core.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.budgettracker.core.data.local.entities.EssentialExpenseEntity
import com.budgettracker.core.data.local.entities.ExpenseCategory
import com.budgettracker.core.data.repository.BudgetOverviewRepository
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import com.budgettracker.core.utils.Result as RepositoryResult

/**
 * Singleton data store with Firebase persistence
 */
object TransactionDataStore {
    
    private var _transactions = mutableListOf<Transaction>()
    private val parsedDocuments = mutableSetOf<String>()
    private var isInitialized = false
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Repository for auto-marking fixed expenses as paid
    private var budgetRepository: BudgetOverviewRepository? = null
    
    /**
     * Set the budget repository for auto-pay functionality
     */
    fun setBudgetRepository(repository: BudgetOverviewRepository) {
        budgetRepository = repository
    }
    
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "demo_user"
    }
    
    /**
     * Initialize and load transactions from Firebase
     * Set forceReload = true to bypass initialization check
     * 
     * MIGRATION STRATEGY: Read from BOTH legacy root collection AND new user subcollection
     */
    suspend fun initializeFromFirebase(forceReload: Boolean = false) {
        if (isInitialized && !forceReload) return
        
        try {
            val userId = getCurrentUserId()
            android.util.Log.d("TransactionDataStore", "Loading transactions from Firebase for user: $userId")
            
            // 1. Load from NEW user subcollection (preferred)
            val userSubcollectionSnapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
            
            android.util.Log.d("TransactionDataStore", "Found ${userSubcollectionSnapshot.size()} transactions in user subcollection")
            
            // 2. Load from LEGACY root collection (for backward compatibility)
            val legacySnapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()
            
            android.util.Log.d("TransactionDataStore", "Found ${legacySnapshot.size()} transactions in legacy root collection")
            
            // Combine both sources (use Set to deduplicate by ID)
            val allDocuments = (userSubcollectionSnapshot.documents + legacySnapshot.documents)
                .distinctBy { it.id }
            
            val firebaseTransactions = allDocuments.mapNotNull { doc ->
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
                android.util.Log.d("TransactionDataStore", "Loaded ${firebaseTransactions.size} transactions total (merged from both locations)")
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
     * Also automatically marks matching fixed expenses as paid
     */
    fun addTransaction(transaction: Transaction) {
        _transactions.add(transaction)
        Log.d("TransactionDataStore", "Added transaction: ${transaction.description} - ${transaction.amount}")
        
        // Save to Firebase in background
        CoroutineScope(Dispatchers.IO).launch {
            saveTransactionToFirebase(transaction)
            
            // Auto-mark matching fixed expense as paid (only for expenses)
            if (transaction.type == TransactionType.EXPENSE) {
                autoMarkFixedExpensePaid(transaction)
            }
        }
    }
    
    /**
     * Add multiple transactions (from PDF parsing) with enhanced duplicate detection
     */
    fun addTransactions(transactions: List<Transaction>, documentHash: String? = null): Int {
        // Check if document was already parsed
        if (documentHash != null && parsedDocuments.contains(documentHash)) {
            android.util.Log.d("TransactionDataStore", "Document already parsed: $documentHash")
            return 0
        }
        
        android.util.Log.d("TransactionDataStore", "Checking ${transactions.size} new transactions for duplicates against ${_transactions.size} existing transactions")
        
        var addedCount = 0
        val duplicatesSkipped = mutableListOf<String>()
        val transactionsToAdd = mutableListOf<Transaction>()
        
        for (transaction in transactions) {
            // Enhanced duplicate detection
            val isDuplicate = checkForDuplicate(transaction)
            
            if (!isDuplicate) {
                // Also check against other transactions in this same batch
                val isDuplicateInBatch = transactionsToAdd.any { batchTransaction ->
                    isSimilarTransaction(transaction, batchTransaction)
                }
                
                if (!isDuplicateInBatch) {
                    transactionsToAdd.add(transaction)
                    addedCount++
                    android.util.Log.d("TransactionDataStore", "✅ Will add: ${transaction.description} - $${transaction.amount}")
                } else {
                    duplicatesSkipped.add("${transaction.description} ($${transaction.amount}) - duplicate in batch")
                    android.util.Log.d("TransactionDataStore", "⚠️ Skipping batch duplicate: ${transaction.description}")
                }
            } else {
                duplicatesSkipped.add("${transaction.description} ($${transaction.amount}) - already exists")
                android.util.Log.d("TransactionDataStore", "⚠️ Skipping existing duplicate: ${transaction.description}")
            }
        }
        
        // Add non-duplicate transactions
        _transactions.addAll(transactionsToAdd)
        
        // Mark document as parsed
        if (documentHash != null) {
            parsedDocuments.add(documentHash)
        }
        
        // Save new transactions to Firebase in background
        if (addedCount > 0) {
            CoroutineScope(Dispatchers.IO).launch {
                saveTransactionsToFirebase(transactionsToAdd)
            }
        }
        
        android.util.Log.d("TransactionDataStore", "✅ Added $addedCount new transactions, skipped ${duplicatesSkipped.size} duplicates")
        duplicatesSkipped.forEach { android.util.Log.d("TransactionDataStore", "   Skipped: $it") }
        
        return addedCount
    }
    
    /**
     * Enhanced duplicate detection
     */
    private fun checkForDuplicate(newTransaction: Transaction): Boolean {
        return _transactions.any { existing ->
            isSimilarTransaction(newTransaction, existing)
        }
    }
    
    /**
     * Check if two transactions are similar (likely duplicates)
     */
    private fun isSimilarTransaction(transaction1: Transaction, transaction2: Transaction): Boolean {
        // Exact amount match
        val sameAmount = kotlin.math.abs(transaction1.amount - transaction2.amount) < 0.01
        
        // Date within 2 days
        val dateRange = kotlin.math.abs(transaction1.date.time - transaction2.date.time) < 172800000 // 48 hours
        
        // Similar description (normalize and compare)
        val desc1 = normalizeDescription(transaction1.description)
        val desc2 = normalizeDescription(transaction2.description)
        val similarDescription = desc1 == desc2 || 
                                desc1.contains(desc2) || 
                                desc2.contains(desc1) ||
                                calculateSimilarity(desc1, desc2) > 0.8
        
        val isDuplicate = sameAmount && dateRange && similarDescription
        
        if (isDuplicate) {
            android.util.Log.d("TransactionDataStore", "🔍 Duplicate detected: ${transaction1.description} ($${transaction1.amount}) vs ${transaction2.description} ($${transaction2.amount})")
        }
        
        return isDuplicate
    }
    
    /**
     * Normalize description for comparison
     */
    private fun normalizeDescription(description: String): String {
        return description
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
            .replace(Regex("\\s+"), " ") // Normalize whitespace
            .replace(Regex("\\d{10,}"), "") // Remove long numbers
            .trim()
    }
    
    /**
     * Calculate similarity between two strings
     */
    private fun calculateSimilarity(str1: String, str2: String): Double {
        val maxLength = maxOf(str1.length, str2.length)
        if (maxLength == 0) return 1.0
        
        val distance = levenshteinDistance(str1, str2)
        return (maxLength - distance) / maxLength.toDouble()
    }
    
    /**
     * Calculate Levenshtein distance
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length
        
        val matrix = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) matrix[i][0] = i
        for (j in 0..len2) matrix[0][j] = j
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                matrix[i][j] = minOf(
                    matrix[i - 1][j] + 1,      // deletion
                    matrix[i][j - 1] + 1,      // insertion
                    matrix[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return matrix[len1][len2]
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
     * Clear all data locally and from Firebase
     */
    fun clearAllData() {
        _transactions.clear()
        parsedDocuments.clear()
        isInitialized = false
        android.util.Log.d("TransactionDataStore", "Cleared all local data")
        
        // Also clear Firebase data
        CoroutineScope(Dispatchers.IO).launch {
            clearAllFirebaseData()
        }
    }
    
    /**
     * Completely clear all transactions from Firebase
     */
    private suspend fun clearAllFirebaseData() {
        try {
            val userId = getCurrentUserId()
            android.util.Log.d("TransactionDataStore", "🗑️ Clearing ALL Firebase data for user: $userId")
            
            // Get all transactions for the user
            val snapshot = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            var deletedCount = 0
            
            // Delete each transaction document completely
            for (document in snapshot.documents) {
                try {
                    firestore.collection("transactions")
                        .document(document.id)
                        .delete()
                        .await()
                    
                    deletedCount++
                    android.util.Log.d("TransactionDataStore", "🗑️ Permanently deleted: ${document.id}")
                    
                } catch (e: Exception) {
                    android.util.Log.e("TransactionDataStore", "Error deleting ${document.id}: ${e.message}")
                }
            }
            
            android.util.Log.d("TransactionDataStore", "✅ FIREBASE CLEARED: Deleted $deletedCount transactions")
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error clearing Firebase: ${e.message}")
        }
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
     * Automatically mark a matching fixed expense as paid when a transaction is created
     * Matches based on: amount (within 1% tolerance), category, and period (YYYY-MM)
     */
    private suspend fun autoMarkFixedExpensePaid(transaction: Transaction) {
        try {
            val repository = budgetRepository
            if (repository == null) {
                Log.w("TransactionDataStore", "BudgetRepository not set, skipping auto-pay")
                return
            }
            
            // Get the period from the transaction date (YYYY-MM format)
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based
            val period = String.format("%04d-%02d", year, month)
            
            Log.d("AutoPay", "=== AUTO-PAY CHECK ===")
            Log.d("AutoPay", "Transaction: ${transaction.description} | $${transaction.amount} | ${transaction.category.displayName}")
            Log.d("AutoPay", "Period: $period")
            
            // Get all unpaid fixed expenses for this period
            val expensesResult = repository.getEssentialExpenses(period)
            if (expensesResult is RepositoryResult.Success) {
                val unpaidFixedExpenses: List<EssentialExpenseEntity> = expensesResult.data.filter { 
                    it.dueDay != null && !it.paid 
                }
                
                Log.d("AutoPay", "Found ${unpaidFixedExpenses.size} unpaid fixed expenses")
                
                // Try to find a matching expense
                val matchingExpense: EssentialExpenseEntity? = unpaidFixedExpenses.find { expense ->
                    // Check if category matches (map TransactionCategory to ExpenseCategory)
                    val categoryMatches = doesCategoryMatch(transaction.category, expense.category)
                    
                    // Check if amount matches (within 1% tolerance to handle rounding)
                    val amountDiff = kotlin.math.abs(transaction.amount - expense.plannedAmount)
                    val tolerance = expense.plannedAmount * 0.01 // 1% tolerance
                    val amountMatches = amountDiff <= tolerance
                    
                    Log.d("AutoPay", "  Checking: ${expense.name} | $${expense.plannedAmount} | ${expense.category}")
                    Log.d("AutoPay", "    Category match: $categoryMatches | Amount match: $amountMatches (diff: $$amountDiff, tolerance: $$tolerance)")
                    
                    categoryMatches && amountMatches
                }
                
                if (matchingExpense != null) {
                    Log.d("AutoPay", "✅ MATCH FOUND: ${matchingExpense.name}")
                    Log.d("AutoPay", "   Marking as paid with actual amount: $${transaction.amount}")
                    
                    // Mark as paid with the actual transaction amount
                    val result = repository.markEssentialPaid(matchingExpense.id, transaction.amount)
                    
                    if (result is RepositoryResult.Success) {
                        Log.d("AutoPay", "✅ Successfully auto-marked fixed expense as paid!")
                    } else if (result is RepositoryResult.Error) {
                        Log.e("AutoPay", "❌ Failed to mark as paid: ${result.message}")
                    }
                } else {
                    Log.d("AutoPay", "❌ No matching fixed expense found")
                }
            }
            
            Log.d("AutoPay", "=== AUTO-PAY CHECK END ===\n")
            
        } catch (e: Exception) {
            Log.e("AutoPay", "Error in auto-pay: ${e.message}", e)
        }
    }
    
    /**
     * Map TransactionCategory to ExpenseCategory for matching
     */
    private fun doesCategoryMatch(transactionCategory: TransactionCategory, expenseCategory: ExpenseCategory): Boolean {
        return when (transactionCategory) {
            TransactionCategory.RENT -> expenseCategory == ExpenseCategory.RENT
            TransactionCategory.UTILITIES -> expenseCategory == ExpenseCategory.UTILITIES
            TransactionCategory.GROCERIES -> expenseCategory == ExpenseCategory.GROCERIES
            TransactionCategory.PHONE -> expenseCategory == ExpenseCategory.PHONE
            TransactionCategory.INSURANCE -> expenseCategory == ExpenseCategory.INSURANCE
            TransactionCategory.TRANSPORTATION -> expenseCategory == ExpenseCategory.TRANSPORTATION
            else -> false // Don't auto-match other categories
        }
    }
    
    /**
     * Save single transaction to Firebase
     */
    /**
     * Save transaction to Firebase USER SUBCOLLECTION (new structure)
     * Also deletes from legacy root collection if exists (automatic migration)
     */
    private suspend fun saveTransactionToFirebase(transaction: Transaction) {
        try {
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
            
            // 1. Save to NEW user subcollection
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id)
                .set(transactionData)
                .await()
            
            android.util.Log.d("TransactionDataStore", "✓ Saved transaction to user subcollection: ${transaction.id}")
            
            // 2. Delete from LEGACY root collection if exists (automatic migration)
            try {
                firestore.collection("transactions")
                    .document(transaction.id)
                    .delete()
                    .await()
                android.util.Log.d("TransactionDataStore", "✓ Deleted legacy transaction: ${transaction.id}")
            } catch (e: Exception) {
                // Ignore if doesn't exist in legacy location
            }
            
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
    /**
     * Update transaction in Firebase USER SUBCOLLECTION (new structure)
     * Also updates in legacy location if exists (automatic migration)
     */
    private suspend fun updateTransactionInFirebase(transaction: Transaction) {
        try {
            val userId = getCurrentUserId()
            val updateData = mapOf(
                "amount" to transaction.amount,
                "category" to transaction.category.name,
                "type" to transaction.type.name,
                "description" to transaction.description,
                "notes" to transaction.notes,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            // 1. Update in NEW user subcollection
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id)
                .update(updateData)
                .await()
            
            android.util.Log.d("TransactionDataStore", "✓ Updated transaction in user subcollection: ${transaction.id}")
            
            // 2. Also update in LEGACY root collection if exists (automatic migration)
            try {
                firestore.collection("transactions")
                    .document(transaction.id)
                    .update(updateData)
                    .await()
                android.util.Log.d("TransactionDataStore", "✓ Updated legacy transaction: ${transaction.id}")
            } catch (e: Exception) {
                // Ignore if doesn't exist in legacy location
            }
            
        } catch (e: Exception) {
            android.util.Log.e("TransactionDataStore", "Error updating transaction in Firebase: ${e.message}")
        }
    }
    
    /**
     * Delete transaction from Firebase USER SUBCOLLECTION (new structure)
     * Also deletes from legacy location if exists (automatic migration)
     */
    private suspend fun deleteTransactionFromFirebase(transactionId: String) {
        try {
            val userId = getCurrentUserId()
            
            // 1. Delete from NEW user subcollection
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId)
                .delete()
                .await()
            
            android.util.Log.d("TransactionDataStore", "✓ Permanently deleted transaction from user subcollection: $transactionId")
            
            // 2. Also delete from LEGACY root collection if exists (automatic migration)
            try {
                firestore.collection("transactions")
                    .document(transactionId)
                    .delete()
                    .await()
                android.util.Log.d("TransactionDataStore", "✓ Deleted legacy transaction: $transactionId")
            } catch (e: Exception) {
                // Ignore if doesn't exist in legacy location
            }
            
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
