package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Budget data model for monthly budget planning
 */
@Parcelize
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val month: String = "", // Format: "2025-01"
    val year: Int = 2025,
    val categories: List<CategoryBudget> = emptyList(),
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalIncome: Double = 0.0,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isTemplate: Boolean = false,
    val templateName: String? = null
) : Parcelable {
    
    /**
     * Calculate remaining budget
     */
    fun getRemainingBudget(): Double = totalBudget - totalSpent
    
    /**
     * Calculate budget utilization percentage
     */
    fun getBudgetUtilization(): Float {
        return if (totalBudget > 0) {
            (totalSpent / totalBudget * 100).toFloat()
        } else 0f
    }
    
    /**
     * Check if budget is over limit
     */
    fun isOverBudget(): Boolean = totalSpent > totalBudget
    
    /**
     * Get budget status
     */
    fun getBudgetStatus(): BudgetStatus {
        val utilization = getBudgetUtilization()
        return when {
            utilization >= 100f -> BudgetStatus.OVER_BUDGET
            utilization >= 80f -> BudgetStatus.NEAR_LIMIT
            utilization >= 50f -> BudgetStatus.ON_TRACK
            else -> BudgetStatus.UNDER_BUDGET
        }
    }
    
    /**
     * Get surplus or deficit
     */
    fun getSurplusDeficit(): Double = totalIncome - totalSpent
}

/**
 * Category budget for individual spending categories
 */
@Parcelize
data class CategoryBudget(
    val category: TransactionCategory,
    val budgetAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val color: String = category.color,
    val isEnabled: Boolean = true,
    val alertThreshold: Float = 80f // Alert when 80% of budget is used
) : Parcelable {
    
    /**
     * Calculate remaining amount for this category
     */
    fun getRemainingAmount(): Double = budgetAmount - spentAmount
    
    /**
     * Calculate utilization percentage for this category
     */
    fun getUtilization(): Float {
        return if (budgetAmount > 0) {
            (spentAmount / budgetAmount * 100).toFloat()
        } else 0f
    }
    
    /**
     * Check if category is over budget
     */
    fun isOverBudget(): Boolean = spentAmount > budgetAmount
    
    /**
     * Check if category should trigger alert
     */
    fun shouldAlert(): Boolean = getUtilization() >= alertThreshold
    
    /**
     * Get category budget status
     */
    fun getStatus(): BudgetStatus {
        val utilization = getUtilization()
        return when {
            utilization >= 100f -> BudgetStatus.OVER_BUDGET
            utilization >= alertThreshold -> BudgetStatus.NEAR_LIMIT
            utilization >= 50f -> BudgetStatus.ON_TRACK
            else -> BudgetStatus.UNDER_BUDGET
        }
    }
}

/**
 * Budget status enum
 */
@Parcelize
enum class BudgetStatus(val displayName: String, val color: String) : Parcelable {
    UNDER_BUDGET("Under Budget", "#28a745"),
    ON_TRACK("On Track", "#17a2b8"),
    NEAR_LIMIT("Near Limit", "#ffc107"),
    OVER_BUDGET("Over Budget", "#dc3545")
}

/**
 * Budget template for common budget allocations
 */
@Parcelize
data class BudgetTemplate(
    val name: String,
    val description: String,
    val categoryAllocations: Map<TransactionCategory, Float> // Percentage allocation
) : Parcelable {
    
    companion object {
        /**
         * 50/30/20 Budget Rule Template
         */
        val FIFTY_THIRTY_TWENTY = BudgetTemplate(
            name = "50/30/20 Rule",
            description = "50% needs, 30% wants, 20% savings",
            categoryAllocations = mapOf(
                // Needs (50%)
                TransactionCategory.RENT to 25f,
                TransactionCategory.GROCERIES to 10f,
                TransactionCategory.UTILITIES to 5f,
                TransactionCategory.TRANSPORTATION to 5f,
                TransactionCategory.HEALTHCARE to 3f,
                TransactionCategory.PHONE to 2f,
                
                // Wants (30%)
                TransactionCategory.DINING_OUT to 10f,
                TransactionCategory.ENTERTAINMENT to 8f,
                TransactionCategory.CLOTHING to 5f,
                TransactionCategory.SUBSCRIPTIONS to 4f,
                TransactionCategory.PERSONAL_CARE to 3f,
                
                // Savings (20%)
                TransactionCategory.EMERGENCY_FUND to 10f,
                TransactionCategory.RETIREMENT to 5f,
                TransactionCategory.STOCKS_ETFS to 5f
            )
        )
        
        /**
         * Zero-Based Budget Template
         */
        val ZERO_BASED = BudgetTemplate(
            name = "Zero-Based Budget",
            description = "Every dollar has a purpose",
            categoryAllocations = mapOf(
                TransactionCategory.RENT to 30f,
                TransactionCategory.GROCERIES to 12f,
                TransactionCategory.TRANSPORTATION to 8f,
                TransactionCategory.UTILITIES to 6f,
                TransactionCategory.DINING_OUT to 8f,
                TransactionCategory.ENTERTAINMENT to 6f,
                TransactionCategory.EMERGENCY_FUND to 15f,
                TransactionCategory.RETIREMENT to 10f,
                TransactionCategory.MISCELLANEOUS to 5f
            )
        )
        
        /**
         * OPT Student Budget Template
         */
        val OPT_STUDENT = BudgetTemplate(
            name = "OPT Student Budget",
            description = "Budget optimized for OPT visa holders",
            categoryAllocations = mapOf(
                TransactionCategory.RENT to 28f,
                TransactionCategory.GROCERIES to 10f,
                TransactionCategory.TRANSPORTATION to 6f,
                TransactionCategory.UTILITIES to 5f,
                TransactionCategory.PHONE to 2f,
                TransactionCategory.DINING_OUT to 6f,
                TransactionCategory.ENTERTAINMENT to 4f,
                TransactionCategory.EMERGENCY_FUND to 20f, // Higher for visa holders
                TransactionCategory.H1B_APPLICATION to 3f,
                TransactionCategory.RETIREMENT to 8f,
                TransactionCategory.STOCKS_ETFS to 5f,
                TransactionCategory.MISCELLANEOUS to 3f
            )
        )
    }
}
