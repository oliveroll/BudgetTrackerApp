package com.budgettracker.core.data.repository

import com.budgettracker.core.domain.model.Budget
import com.budgettracker.core.domain.repository.BudgetRepository
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of BudgetRepository for build purposes
 */
@Singleton
class BudgetRepositoryImpl @Inject constructor() : BudgetRepository {
    
    override fun getBudgetsByUserId(userId: String): Flow<List<Budget>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getBudgetById(id: String): Budget? {
        return null
    }
    
    override suspend fun getBudgetByMonthYear(userId: String, month: String, year: Int): Budget? {
        return null
    }
    
    override fun getBudgetTemplates(userId: String): Flow<List<Budget>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getBudgetTemplate(userId: String, templateName: String): Budget? {
        return null
    }
    
    override suspend fun insertBudget(budget: Budget): Result<String> {
        return Result.Success(budget.id)
    }
    
    override suspend fun updateBudget(budget: Budget): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deleteBudget(id: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deleteAllBudgets(userId: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun syncBudgets(userId: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun getUnsyncedBudgets(): List<Budget> {
        return emptyList()
    }
}
