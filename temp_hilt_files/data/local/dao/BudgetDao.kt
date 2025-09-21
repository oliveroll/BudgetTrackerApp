package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Budget operations
 */
@Dao
interface BudgetDao {
    
    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY year DESC, month DESC")
    fun getBudgetsByUserId(userId: String): Flow<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: String): BudgetEntity?
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    suspend fun getBudgetByMonthYear(userId: String, month: String, year: Int): BudgetEntity?
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND isTemplate = 1")
    fun getBudgetTemplates(userId: String): Flow<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets WHERE userId = :userId AND isTemplate = 1 AND templateName = :templateName")
    suspend fun getBudgetTemplate(userId: String, templateName: String): BudgetEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)
    
    @Update
    suspend fun updateBudget(budget: BudgetEntity)
    
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)
    
    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllBudgetsForUser(userId: String)
    
    // Sync related queries
    @Query("SELECT * FROM budgets WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedBudgets(): List<BudgetEntity>
    
    @Query("UPDATE budgets SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}

