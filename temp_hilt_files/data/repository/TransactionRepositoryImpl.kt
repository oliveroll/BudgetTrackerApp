package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.TransactionDao
import com.budgettracker.core.data.local.entities.TransactionEntity
import com.budgettracker.core.data.remote.FirebaseDataSource
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TransactionRepository
 * Handles local-first approach with Firebase sync
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionDao,
    private val remoteDataSource: FirebaseDataSource
) : TransactionRepository {
    
    override fun getAllTransactions(userId: String): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByUserId(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override suspend fun getTransactionById(id: String): Transaction? {
        return localDataSource.getTransactionById(id)?.toDomainModel()
    }
    
    override fun getTransactionsByDateRange(
        userId: String,
        startDate: Date,
        endDate: Date
    ): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByDateRange(userId, startDate, endDate)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTransactionsByCategory(
        userId: String,
        category: TransactionCategory
    ): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByCategory(userId, category)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getTransactionsByType(
        userId: String,
        type: TransactionType
    ): Flow<List<Transaction>> {
        return localDataSource.getTransactionsByType(userId, type)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun getRecurringTransactions(userId: String): Flow<List<Transaction>> {
        return localDataSource.getRecurringTransactions(userId)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override fun searchTransactions(userId: String, query: String): Flow<List<Transaction>> {
        return localDataSource.searchTransactions(userId, query)
            .map { entities -> entities.map { it.toDomainModel() } }
    }
    
    override suspend fun insertTransaction(transaction: Transaction): Result<String> {
        return try {
            // Insert locally first
            val entity = TransactionEntity.fromDomainModel(transaction, "PENDING")
            localDataSource.insertTransaction(entity)
            
            // Try to sync to Firebase
            val remoteResult = remoteDataSource.insertTransaction(transaction)
            if (remoteResult.isSuccess) {
                localDataSource.updateSyncStatus(transaction.id, "SYNCED")
            }
            
            Result.success(transaction.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            // Update locally first
            val entity = TransactionEntity.fromDomainModel(transaction, "PENDING")
            localDataSource.updateTransaction(entity)
            
            // Try to sync to Firebase
            val remoteResult = remoteDataSource.updateTransaction(transaction)
            if (remoteResult.isSuccess) {
                localDataSource.updateSyncStatus(transaction.id, "SYNCED")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            // Soft delete locally
            localDataSource.softDeleteTransaction(id)
            
            // Try to sync to Firebase
            val remoteResult = remoteDataSource.deleteTransaction(id)
            if (remoteResult.isSuccess) {
                localDataSource.updateSyncStatus(id, "SYNCED")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAllTransactions(userId: String): Result<Unit> {
        return try {
            localDataSource.deleteAllTransactionsForUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTotalAmountByTypeAndDateRange(
        userId: String,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Double {
        return localDataSource.getTotalAmountByTypeAndDateRange(userId, type, startDate, endDate) ?: 0.0
    }
    
    override suspend fun getTotalAmountByCategoryAndDateRange(
        userId: String,
        category: TransactionCategory,
        startDate: Date,
        endDate: Date
    ): Double {
        return localDataSource.getTotalAmountByCategoryAndDateRange(userId, category, startDate, endDate) ?: 0.0
    }
    
    override suspend fun getCategoryTotals(
        userId: String,
        type: TransactionType,
        startDate: Date,
        endDate: Date
    ): Map<TransactionCategory, Double> {
        val categoryTotals = localDataSource.getCategoryTotals(userId, type, startDate, endDate)
        return categoryTotals.associate { it.category to it.total }
    }
    
    override suspend fun getTransactionCount(userId: String): Int {
        return localDataSource.getTransactionCount(userId)
    }
    
    override suspend fun syncTransactions(userId: String): Result<Unit> {
        return try {
            // Get remote transactions
            val remoteTransactions = remoteDataSource.getTransactions(userId)
            
            // Insert/update local transactions
            val entities = remoteTransactions.map { 
                TransactionEntity.fromDomainModel(it, "SYNCED") 
            }
            localDataSource.insertTransactions(entities)
            
            // Upload unsynced local transactions
            val unsyncedTransactions = localDataSource.getUnsyncedTransactions()
            unsyncedTransactions.forEach { entity ->
                val transaction = entity.toDomainModel()
                val result = remoteDataSource.insertTransaction(transaction)
                if (result.isSuccess) {
                    localDataSource.updateSyncStatus(entity.id, "SYNCED")
                } else {
                    localDataSource.updateSyncStatus(entity.id, "FAILED")
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUnsyncedTransactions(): List<Transaction> {
        return localDataSource.getUnsyncedTransactions().map { it.toDomainModel() }
    }
}

