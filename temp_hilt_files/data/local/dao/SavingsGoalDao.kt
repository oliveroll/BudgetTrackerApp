package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.SavingsGoalEntity
import com.budgettracker.core.domain.model.GoalCategory
import com.budgettracker.core.domain.model.Priority
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for SavingsGoal operations
 */
@Dao
interface SavingsGoalDao {
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY priority ASC, deadline ASC")
    fun getSavingsGoalsByUserId(userId: String): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE id = :id")
    suspend fun getSavingsGoalById(id: String): SavingsGoalEntity?
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND category = :category ORDER BY priority ASC")
    fun getSavingsGoalsByCategory(userId: String, category: GoalCategory): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND priority = :priority ORDER BY deadline ASC")
    fun getSavingsGoalsByPriority(userId: String, priority: Priority): Flow<List<SavingsGoalEntity>>
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND deadline <= :date AND currentAmount < targetAmount")
    suspend fun getOverdueGoals(userId: String, date: Date): List<SavingsGoalEntity>
    
    @Query("SELECT * FROM savings_goals WHERE userId = :userId AND currentAmount >= targetAmount")
    suspend fun getCompletedGoals(userId: String): List<SavingsGoalEntity>
    
    @Query("SELECT SUM(targetAmount - currentAmount) FROM savings_goals WHERE userId = :userId AND currentAmount < targetAmount")
    suspend fun getTotalRemainingAmount(userId: String): Double?
    
    @Query("SELECT SUM(monthlyContribution) FROM savings_goals WHERE userId = :userId AND currentAmount < targetAmount")
    suspend fun getTotalMonthlyContributions(userId: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoals(goals: List<SavingsGoalEntity>)
    
    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)
    
    @Query("UPDATE savings_goals SET currentAmount = :newAmount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateGoalAmount(id: String, newAmount: Double, updatedAt: Date = Date())
    
    @Delete
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity)
    
    @Query("DELETE FROM savings_goals WHERE userId = :userId")
    suspend fun deleteAllSavingsGoalsForUser(userId: String)
    
    // Sync related queries
    @Query("SELECT * FROM savings_goals WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedSavingsGoals(): List<SavingsGoalEntity>
    
    @Query("UPDATE savings_goals SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}

