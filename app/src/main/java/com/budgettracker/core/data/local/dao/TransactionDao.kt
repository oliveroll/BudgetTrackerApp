package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.TransactionEntity
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Transaction operations
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions WHERE isDeleted = 0 ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY date DESC")
    fun getTransactionsByUserId(userId: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getTransactionById(id: String): TransactionEntity?
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isDeleted = 0 
        ORDER BY date DESC
    """)
    fun getTransactionsByDateRange(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND category = :category 
        AND isDeleted = 0 
        ORDER BY date DESC
    """)
    fun getTransactionsByCategory(
        userId: String,
        category: TransactionCategory
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND type = :type 
        AND isDeleted = 0 
        ORDER BY date DESC
    """)
    fun getTransactionsByType(
        userId: String,
        type: TransactionType
    ): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND type = :type 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isDeleted = 0
    """)
    suspend fun getTotalAmountByTypeAndDateRange(
        userId: String,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): Double?
    
    @Query("""
        SELECT SUM(amount) FROM transactions 
        WHERE userId = :userId 
        AND category = :category 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isDeleted = 0
    """)
    suspend fun getTotalAmountByCategoryAndDateRange(
        userId: String,
        category: TransactionCategory,
        startDate: Long,
        endDate: Long
    ): Double?
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND isRecurring = 1 
        AND isDeleted = 0 
        ORDER BY date DESC
    """)
    fun getRecurringTransactions(userId: String): Flow<List<TransactionEntity>>
    
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND (description LIKE '%' || :searchQuery || '%' OR notes LIKE '%' || :searchQuery || '%')
        AND isDeleted = 0 
        ORDER BY date DESC
    """)
    fun searchTransactions(userId: String, searchQuery: String): Flow<List<TransactionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)
    
    @Query("UPDATE transactions SET isDeleted = 1, updatedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, deletedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE isDeleted = 1")
    suspend fun deleteAllSoftDeletedTransactions()
    
    @Query("DELETE FROM transactions WHERE userId = :userId")
    suspend fun deleteAllTransactionsForUser(userId: String)
    
    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>
    
    @Query("UPDATE transactions SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
    
    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions 
        WHERE userId = :userId 
        AND type = :type 
        AND date >= :startDate 
        AND date <= :endDate 
        AND isDeleted = 0 
        GROUP BY category 
        ORDER BY total DESC
    """)
    suspend fun getCategoryTotals(
        userId: String,
        type: TransactionType,
        startDate: Long,
        endDate: Long
    ): List<CategoryTotal>
    
    @Query("""
        SELECT COUNT(*) FROM transactions 
        WHERE userId = :userId 
        AND isDeleted = 0
    """)
    suspend fun getTransactionCount(userId: String): Int
}

data class CategoryTotal(
    val category: TransactionCategory,
    val total: Double
)

