package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.BudgetOverview
import java.util.Date
import java.util.UUID

/**
 * Room entity for Budget Overview data
 */
@Entity(tableName = "budget_overview")
@TypeConverters(Converters::class)
data class BudgetOverviewEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val monthlyIncome: Double = 5470.0, // Gross monthly income
    val netMonthlyIncome: Double = 5191.32, // After taxes
    val currentBalance: Double = 0.0,
    val nextPaycheckDate: Long,
    val nextPaycheckAmount: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): BudgetOverview {
        return BudgetOverview(
            id = id,
            userId = userId,
            monthlyIncome = monthlyIncome,
            netMonthlyIncome = netMonthlyIncome,
            currentBalance = currentBalance,
            nextPaycheckDate = Date(nextPaycheckDate),
            nextPaycheckAmount = nextPaycheckAmount,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        fun fromDomain(budget: BudgetOverview): BudgetOverviewEntity {
            return BudgetOverviewEntity(
                id = budget.id,
                userId = budget.userId,
                monthlyIncome = budget.monthlyIncome,
                netMonthlyIncome = budget.netMonthlyIncome,
                currentBalance = budget.currentBalance,
                nextPaycheckDate = budget.nextPaycheckDate.time,
                nextPaycheckAmount = budget.nextPaycheckAmount,
                createdAt = budget.createdAt.time,
                updatedAt = budget.updatedAt.time
            )
        }
    }
}



