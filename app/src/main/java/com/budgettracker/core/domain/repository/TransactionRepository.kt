package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for Transaction operations
 */
interface TransactionRepository {
    
    // Read operations
    fun getAllTransactions(userId: String): Flow<List<Transaction>>
    suspend fun getTransactionById(id: String): Transaction?
    fun getTransactionsByDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>>
    fun getTransactionsByCategory(
        userId: String,
        category: TransactionCategory
    ): Flow<List<Transaction>>
    fun getTransactionsByType(
        userId: String,
        type: TransactionType
    ): Flow<List<Transaction>>
    fun getRecurringTransactions(userId: String): Flow<List<Transaction>>
    fun searchTransactions(userId: String, query: String): Flow<List<Transaction>>
    
    // Write operations
    suspend fun insertTransaction(transaction: Transaction): Result<String>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(id: String): Result<Unit>
    suspend fun deleteAllTransactions(userId: String): Result<Unit>
    
    // Analytics operations
    suspend fun getTotalAmountByTypeAndDateRange(
        userId: String,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Double
    suspend fun getTotalAmountByCategoryAndDateRange(
        userId: String,
        category: TransactionCategory,
        startDate: Date,
        endDate: Date
    ): Double
    suspend fun getCategoryTotals(
        userId: String,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Map<TransactionCategory, Double>
    suspend fun getTransactionCount(userId: String): Int
    
    // Sync operations
    suspend fun syncTransactions(userId: String): Result<Unit>
    suspend fun getUnsyncedTransactions(): List<Transaction>
}


