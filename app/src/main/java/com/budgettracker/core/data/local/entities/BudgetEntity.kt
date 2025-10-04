package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.Budget
import com.budgettracker.core.domain.model.CategoryBudget
import java.util.Date

/**
 * Room entity for Budget
 */
@Entity(tableName = "budgets")
@TypeConverters(Converters::class)
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val month: String,
    val year: Int,
    val categories: List<CategoryBudget>,
    val totalBudget: Double,
    val totalSpent: Double,
    val totalIncome: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val isTemplate: Boolean,
    val templateName: String?,
    val syncStatus: String = "PENDING"
) {
    fun toDomainModel(): Budget {
        return Budget(
            id = id,
            userId = userId,
            month = month,
            year = year,
            categories = categories,
            totalBudget = totalBudget,
            totalSpent = totalSpent,
            totalIncome = totalIncome,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            isTemplate = isTemplate,
            templateName = templateName
        )
    }
    
    companion object {
        fun fromDomainModel(budget: Budget, syncStatus: String = "PENDING"): BudgetEntity {
            return BudgetEntity(
                id = budget.id,
                userId = budget.userId,
                month = budget.month,
                year = budget.year,
                categories = budget.categories,
                totalBudget = budget.totalBudget,
                totalSpent = budget.totalSpent,
                totalIncome = budget.totalIncome,
                createdAt = budget.createdAt.time,
                updatedAt = budget.updatedAt.time,
                isTemplate = budget.isTemplate,
                templateName = budget.templateName,
                syncStatus = syncStatus
            )
        }
    }
}

