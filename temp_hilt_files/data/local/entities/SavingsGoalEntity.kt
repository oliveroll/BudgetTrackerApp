package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.GoalCategory
import com.budgettracker.core.domain.model.Priority
import com.budgettracker.core.domain.model.SavingsGoal
import java.util.Date

/**
 * Room entity for SavingsGoal
 */
@Entity(tableName = "savings_goals")
@TypeConverters(Converters::class)
data class SavingsGoalEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: Long,
    val priority: Priority,
    val monthlyContribution: Double,
    val category: GoalCategory,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val color: String,
    val icon: String,
    val isActive: Boolean,
    val syncStatus: String = "PENDING" // PENDING, SYNCED, FAILED
) {
    /**
     * Convert entity to domain model
     */
    fun toDomainModel(): SavingsGoal {
        return SavingsGoal(
            id = id,
            userId = userId,
            name = name,
            description = description,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            deadline = Date(deadline),
            priority = priority,
            monthlyContribution = monthlyContribution,
            category = category,
            isCompleted = isCompleted,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            color = color,
            icon = icon,
            isActive = isActive
        )
    }
    
    companion object {
        /**
         * Convert domain model to entity
         */
        fun fromDomainModel(goal: SavingsGoal, syncStatus: String = "PENDING"): SavingsGoalEntity {
            return SavingsGoalEntity(
                id = goal.id,
                userId = goal.userId,
                name = goal.name,
                description = goal.description,
                targetAmount = goal.targetAmount,
                currentAmount = goal.currentAmount,
                deadline = goal.deadline.time,
                priority = goal.priority,
                monthlyContribution = goal.monthlyContribution,
                category = goal.category,
                isCompleted = goal.isCompleted,
                createdAt = goal.createdAt.time,
                updatedAt = goal.updatedAt.time,
                color = goal.color,
                icon = goal.icon,
                isActive = goal.isActive,
                syncStatus = syncStatus
            )
        }
    }
}

