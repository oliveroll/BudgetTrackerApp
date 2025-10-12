package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID

/**
 * Domain model for Budget Overview
 */
@Parcelize
data class BudgetOverview(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val monthlyIncome: Double = 5470.0,
    val netMonthlyIncome: Double = 5191.32,
    val currentBalance: Double = 0.0,
    val nextPaycheckDate: Date = Date(),
    val nextPaycheckAmount: Double = 2735.66, // Bi-weekly
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate total essential expenses
     */
    fun getTotalEssentialExpenses(): Double {
        return 700.0 + 150.0 + 200.0 + 11.99 + 100.0 // Rent + Phone + Groceries + Spotify + Utilities
    }
    
    /**
     * Calculate remaining after essentials
     */
    fun getRemainingAfterEssentials(): Double {
        return netMonthlyIncome - getTotalEssentialExpenses()
    }
    
    /**
     * Calculate debt freedom progress (0-100%)
     */
    fun getDebtFreedomProgress(): Float {
        val originalDebt = 11550.0 // Original loan amount
        val currentDebt = 10317.64 // Current remaining
        return ((originalDebt - currentDebt) / originalDebt * 100).toFloat()
    }
}

/**
 * Domain model for Subscriptions
 */
@Parcelize
data class Subscription(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "",
    val cost: Double = 0.0,
    val frequency: SubscriptionFrequency = SubscriptionFrequency.MONTHLY,
    val nextBillingDate: Date = Date(),
    val category: String = "Entertainment",
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3,
    val notes: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate monthly cost equivalent
     */
    fun getMonthlyCost(): Double {
        return when (frequency) {
            SubscriptionFrequency.WEEKLY -> cost * 4.33
            SubscriptionFrequency.BI_WEEKLY -> cost * 2.17
            SubscriptionFrequency.MONTHLY -> cost
            SubscriptionFrequency.QUARTERLY -> cost / 3
            SubscriptionFrequency.SEMI_ANNUAL -> cost / 6
            SubscriptionFrequency.YEARLY -> cost / 12
        }
    }
    
    /**
     * Get days until next billing
     */
    fun getDaysUntilBilling(): Int {
        val now = Date()
        val diffInMillis = nextBillingDate.time - now.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
}

/**
 * Subscription frequency enum
 */
@Parcelize
enum class SubscriptionFrequency(val displayName: String, val monthsInterval: Double) : Parcelable {
    WEEKLY("Weekly", 0.23),
    BI_WEEKLY("Bi-weekly", 0.46),
    MONTHLY("Monthly", 1.0),
    QUARTERLY("Quarterly", 3.0),
    SEMI_ANNUAL("Semi-annual", 6.0),
    YEARLY("Yearly", 12.0)
}

/**
 * Domain model for Bill Reminders
 */
@Parcelize
data class BillReminder(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val dueDate: Date = Date(),
    val category: String = "",
    val status: ReminderStatus = ReminderStatus.UPCOMING,
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null,
    val reminderDate: Date = Date(),
    val fcmMessageId: String? = null,
    val notes: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Check if reminder is overdue
     */
    fun isOverdue(): Boolean {
        return Date().after(dueDate) && status != ReminderStatus.PAID
    }
    
    /**
     * Get status color for UI
     */
    fun getStatusColor(): String {
        return when (status) {
            ReminderStatus.UPCOMING -> "#FFA726" // Orange
            ReminderStatus.OVERDUE -> "#EF5350" // Red
            ReminderStatus.PAID -> "#66BB6A" // Green
        }
    }
}

/**
 * Reminder status enum
 */
@Parcelize
enum class ReminderStatus(val displayName: String) : Parcelable {
    UPCOMING("Upcoming"),
    OVERDUE("Overdue"),
    PAID("Paid")
}

/**
 * Budget overview summary for dashboard
 */
@Parcelize
data class BudgetSummary(
    val totalIncome: Double,
    val totalExpenses: Double,
    val totalSubscriptions: Double,
    val upcomingBills: List<BillReminder>,
    val debtProgress: Float,
    val debtRemaining: Double,
    val debtFreedomDate: Date,
    val nextPaycheckAmount: Double,
    val nextPaycheckDate: Date
) : Parcelable {
    
    /**
     * Calculate net remaining
     */
    fun getNetRemaining(): Double {
        return totalIncome - totalExpenses - totalSubscriptions
    }
    
    /**
     * Get financial health score (0-100)
     */
    fun getHealthScore(): Int {
        val expenseRatio = (totalExpenses + totalSubscriptions) / totalIncome
        return when {
            expenseRatio <= 0.5 -> 100 // Excellent
            expenseRatio <= 0.7 -> 80  // Good
            expenseRatio <= 0.85 -> 60 // Fair
            else -> 30 // Needs attention
        }
    }
}



