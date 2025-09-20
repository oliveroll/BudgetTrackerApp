package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Savings goal data model for tracking financial goals
 */
@Parcelize
data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val description: String = "",
    val targetAmount: Double = 0.0,
    val currentAmount: Double = 0.0,
    val deadline: Date = Date(),
    val priority: Priority = Priority.MEDIUM,
    val monthlyContribution: Double = 0.0,
    val category: GoalCategory = GoalCategory.OTHER,
    val isCompleted: Boolean = false,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val color: String = category.color,
    val icon: String = category.icon,
    val isActive: Boolean = true
) : Parcelable {
    
    /**
     * Calculate progress percentage
     */
    fun getProgressPercentage(): Float {
        return if (targetAmount > 0) {
            ((currentAmount / targetAmount) * 100).toFloat().coerceAtMost(100f)
        } else 0f
    }
    
    /**
     * Calculate remaining amount needed
     */
    fun getRemainingAmount(): Double = (targetAmount - currentAmount).coerceAtLeast(0.0)
    
    /**
     * Calculate days remaining until deadline
     */
    fun getDaysRemaining(): Long {
        val now = Date()
        return if (deadline.after(now)) {
            TimeUnit.DAYS.convert(deadline.time - now.time, TimeUnit.MILLISECONDS)
        } else 0L
    }
    
    /**
     * Calculate months remaining until deadline
     */
    fun getMonthsRemaining(): Long {
        return (getDaysRemaining() / 30).coerceAtLeast(1L)
    }
    
    /**
     * Calculate required monthly contribution to meet deadline
     */
    fun getRequiredMonthlyContribution(): Double {
        val monthsRemaining = getMonthsRemaining()
        return if (monthsRemaining > 0) {
            getRemainingAmount() / monthsRemaining
        } else getRemainingAmount()
    }
    
    /**
     * Check if goal is on track based on current progress and timeline
     */
    fun isOnTrack(): Boolean {
        val totalDays = TimeUnit.DAYS.convert(deadline.time - createdAt.time, TimeUnit.MILLISECONDS)
        val daysPassed = TimeUnit.DAYS.convert(Date().time - createdAt.time, TimeUnit.MILLISECONDS)
        
        if (totalDays <= 0) return isCompleted
        
        val expectedProgress = (daysPassed.toFloat() / totalDays.toFloat()) * 100f
        val actualProgress = getProgressPercentage()
        
        return actualProgress >= (expectedProgress * 0.9f) // 90% of expected progress
    }
    
    /**
     * Get goal status
     */
    fun getStatus(): GoalStatus {
        return when {
            isCompleted -> GoalStatus.COMPLETED
            getDaysRemaining() <= 0 && !isCompleted -> GoalStatus.OVERDUE
            getDaysRemaining() <= 30 -> GoalStatus.URGENT
            isOnTrack() -> GoalStatus.ON_TRACK
            else -> GoalStatus.BEHIND
        }
    }
    
    /**
     * Calculate projected completion date based on current monthly contribution
     */
    fun getProjectedCompletionDate(): Date {
        if (monthlyContribution <= 0) return Date(Long.MAX_VALUE)
        
        val monthsNeeded = (getRemainingAmount() / monthlyContribution).toLong()
        val millisecondsToAdd = monthsNeeded * 30L * 24L * 60L * 60L * 1000L
        
        return Date(Date().time + millisecondsToAdd)
    }
}

/**
 * Goal priority levels
 */
@Parcelize
enum class Priority(val displayName: String, val color: String) : Parcelable {
    LOW("Low", "#28a745"),
    MEDIUM("Medium", "#ffc107"),
    HIGH("High", "#fd7e14"),
    CRITICAL("Critical", "#dc3545")
}

/**
 * Savings goal categories
 */
@Parcelize
enum class GoalCategory(
    val displayName: String,
    val icon: String,
    val color: String,
    val description: String
) : Parcelable {
    EMERGENCY_FUND(
        "Emergency Fund",
        "🚨",
        "#dc3545",
        "Essential safety net for unexpected expenses"
    ),
    RETIREMENT(
        "Retirement",
        "👴",
        "#6f42c1",
        "Long-term savings for retirement"
    ),
    TRAVEL(
        "Travel",
        "✈️",
        "#20c997",
        "Vacation and travel expenses"
    ),
    DEBT_PAYOFF(
        "Debt Payoff",
        "💳",
        "#fd7e14",
        "Paying off loans and credit card debt"
    ),
    INVESTMENT(
        "Investment",
        "📈",
        "#17a2b8",
        "Building investment portfolio"
    ),
    HOME_PURCHASE(
        "Home Purchase",
        "🏠",
        "#28a745",
        "Down payment for buying a home"
    ),
    EDUCATION(
        "Education",
        "📚",
        "#6610f2",
        "Educational expenses and courses"
    ),
    WEDDING(
        "Wedding",
        "💒",
        "#e83e8c",
        "Wedding and celebration expenses"
    ),
    CAR_PURCHASE(
        "Car Purchase",
        "🚗",
        "#495057",
        "Buying a new or used vehicle"
    ),
    BUSINESS(
        "Business",
        "💼",
        "#343a40",
        "Starting or investing in a business"
    ),
    H1B_EXPENSES(
        "H1B Expenses",
        "📄",
        "#6c757d",
        "H1B application and related visa costs"
    ),
    OTHER(
        "Other",
        "📦",
        "#6c757d",
        "Other savings goals"
    );
    
    companion object {
        /**
         * Get recommended goals for OPT visa holders
         */
        fun getRecommendedForOPT(): List<GoalCategory> {
            return listOf(
                EMERGENCY_FUND,
                H1B_EXPENSES,
                RETIREMENT,
                INVESTMENT
            )
        }
    }
}

/**
 * Goal status tracking
 */
@Parcelize
enum class GoalStatus(val displayName: String, val color: String) : Parcelable {
    ON_TRACK("On Track", "#28a745"),
    BEHIND("Behind", "#ffc107"),
    URGENT("Urgent", "#fd7e14"),
    OVERDUE("Overdue", "#dc3545"),
    COMPLETED("Completed", "#6f42c1")
}

/**
 * Goal contribution record for tracking individual contributions
 */
@Parcelize
data class GoalContribution(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String = "",
    val amount: Double = 0.0,
    val date: Date = Date(),
    val description: String = "",
    val source: String = "Manual" // Manual, Automatic, Found Money, etc.
) : Parcelable
