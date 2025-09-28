package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Fixed monthly expense model
 */
@Parcelize
data class FixedExpense(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val category: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val dueDate: String = "", // e.g., "1st", "15th", "Weekly"
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

/**
 * Income source model
 */
@Parcelize
data class IncomeSource(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val description: String = "",
    val frequency: String = "", // "Bi-weekly", "Monthly", "Weekly"
    val grossAmount: Double = 0.0,
    val taxesDeductions: Double = 0.0,
    val netAmount: Double = grossAmount - taxesDeductions,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate monthly net amount based on frequency
     */
    fun getMonthlyNetAmount(): Double {
        return when (frequency.lowercase()) {
            "bi-weekly" -> netAmount * 2.167 // 26 pay periods / 12 months
            "weekly" -> netAmount * 4.33 // 52 weeks / 12 months
            "monthly" -> netAmount
            "yearly" -> netAmount / 12
            else -> netAmount
        }
    }
}

/**
 * Variable expense category with budget allocation
 */
@Parcelize
data class VariableExpenseCategory(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val category: String = "",
    val budgetAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Get remaining budget
     */
    fun getRemainingBudget(): Double = budgetAmount - spentAmount
    
    /**
     * Get utilization percentage
     */
    fun getUtilizationPercentage(): Float {
        return if (budgetAmount > 0) {
            (spentAmount / budgetAmount * 100).toFloat()
        } else 0f
    }
}

/**
 * Account information model
 */
@Parcelize
data class Account(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val bankPlatform: String = "",
    val purpose: String = "",
    val currentBalance: Double = 0.0,
    val monthlyFlow: Double = 0.0, // Positive for inflow, negative for outflow
    val accountType: AccountType = AccountType.CHECKING,
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable

@Parcelize
enum class AccountType(val displayName: String) : Parcelable {
    CHECKING("Checking"),
    SAVINGS("Savings"),
    INVESTMENT("Investment"),
    RETIREMENT("Retirement"),
    LOAN("Loan")
}


