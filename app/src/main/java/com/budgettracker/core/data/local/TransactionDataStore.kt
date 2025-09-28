package com.budgettracker.core.data.local

import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import java.util.*

/**
 * Singleton data store for transactions that persists across navigation
 */
object TransactionDataStore {
    
    private var _transactions = mutableListOf<Transaction>()
    private val parsedDocuments = mutableSetOf<String>()
    
    /**
     * Get all transactions
     */
    fun getTransactions(): List<Transaction> {
        if (_transactions.isEmpty()) {
            initializeWithDemoData()
        }
        return _transactions.toList()
    }
    
    /**
     * Add a single transaction
     */
    fun addTransaction(transaction: Transaction) {
        _transactions.add(transaction)
        android.util.Log.d("TransactionDataStore", "Added transaction: ${transaction.description} - ${transaction.amount}")
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
        
        android.util.Log.d("TransactionDataStore", "Added $addedCount new transactions out of ${transactions.size}")
        return addedCount
    }
    
    /**
     * Update existing transaction
     */
    fun updateTransaction(updatedTransaction: Transaction) {
        val index = _transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            _transactions[index] = updatedTransaction
            android.util.Log.d("TransactionDataStore", "Updated transaction: ${updatedTransaction.description}")
        }
    }
    
    /**
     * Delete transaction
     */
    fun deleteTransaction(transactionId: String) {
        _transactions.removeAll { it.id == transactionId }
        android.util.Log.d("TransactionDataStore", "Deleted transaction: $transactionId")
    }
    
    /**
     * Get transactions for specific month/year
     */
    fun getTransactionsForMonth(month: Int, year: Int): List<Transaction> {
        return _transactions.filter { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.date }
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
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
}
