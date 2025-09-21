package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.GoalCategory
import com.budgettracker.core.domain.model.Priority
import com.budgettracker.core.domain.model.SavingsGoal
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for SavingsGoal operations
 */
interface SavingsGoalRepository {
    
    // Read operations
    fun getSavingsGoalsByUserId(userId: String): Flow<List<SavingsGoal>>
    suspend fun getSavingsGoalById(id: String): SavingsGoal?
    fun getSavingsGoalsByCategory(userId: String, category: GoalCategory): Flow<List<SavingsGoal>>
    fun getSavingsGoalsByPriority(userId: String, priority: Priority): Flow<List<SavingsGoal>>
    suspend fun getOverdueGoals(userId: String): List<SavingsGoal>
    suspend fun getCompletedGoals(userId: String): List<SavingsGoal>
    suspend fun getTotalRemainingAmount(userId: String): Double
    suspend fun getTotalMonthlyContributions(userId: String): Double
    
    // Write operations
    suspend fun insertSavingsGoal(goal: SavingsGoal): Result<String>
    suspend fun updateSavingsGoal(goal: SavingsGoal): Result<Unit>
    suspend fun updateGoalAmount(id: String, newAmount: Double): Result<Unit>
    suspend fun deleteSavingsGoal(id: String): Result<Unit>
    suspend fun deleteAllSavingsGoals(userId: String): Result<Unit>
    
    // Sync operations
    suspend fun syncSavingsGoals(userId: String): Result<Unit>
    suspend fun getUnsyncedSavingsGoals(): List<SavingsGoal>
}


