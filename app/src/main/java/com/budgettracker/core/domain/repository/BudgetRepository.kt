package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.Budget
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Budget operations
 */
interface BudgetRepository {
    
    // Read operations
    fun getBudgetsByUserId(userId: String): Flow<List<Budget>>
    suspend fun getBudgetById(id: String): Budget?
    suspend fun getBudgetByMonthYear(userId: String, month: String, year: Int): Budget?
    fun getBudgetTemplates(userId: String): Flow<List<Budget>>
    suspend fun getBudgetTemplate(userId: String, templateName: String): Budget?
    
    // Write operations
    suspend fun insertBudget(budget: Budget): Result<String>
    suspend fun updateBudget(budget: Budget): Result<Unit>
    suspend fun deleteBudget(id: String): Result<Unit>
    suspend fun deleteAllBudgets(userId: String): Result<Unit>
    
    // Sync operations
    suspend fun syncBudgets(userId: String): Result<Unit>
    suspend fun getUnsyncedBudgets(): List<Budget>
}


