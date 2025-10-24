package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import java.util.UUID

/**
 * Enhanced Room entity for Subscription data with FCM support
 * Builds on existing SubscriptionEntity with new requirements
 */
@Entity(tableName = "enhanced_subscriptions")
@TypeConverters(Converters::class)
data class EnhancedSubscriptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val amount: Double,
    val currency: String = "USD",
    val frequency: BillingFrequency,
    val nextBillingDate: Long, // Epoch millis UTC
    val reminderDaysBefore: List<Int> = listOf(1, 3, 7), // Multiple reminder days
    val fcmReminderEnabled: Boolean = true,
    val active: Boolean = true,
    val isAutoPay: Boolean = true, // NEW: Auto-pay subscriptions roll forward automatically
    val iconEmoji: String? = null,
    val category: String = "Entertainment",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Calculate monthly cost equivalent
     */
    fun getMonthlyCost(): Double {
        return when (frequency) {
            BillingFrequency.WEEKLY -> amount * 4.33
            BillingFrequency.BI_WEEKLY -> amount * 2.17
            BillingFrequency.MONTHLY -> amount
            BillingFrequency.QUARTERLY -> amount / 3
            BillingFrequency.SEMI_ANNUAL -> amount / 6
            BillingFrequency.YEARLY -> amount / 12
        }
    }
    
    /**
     * Get days until next billing
     */
    fun getDaysUntilBilling(): Int {
        val now = System.currentTimeMillis()
        val diffInMillis = nextBillingDate - now
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * Check if subscription needs reminder
     */
    fun needsReminder(): Boolean {
        val daysUntil = getDaysUntilBilling()
        return active && fcmReminderEnabled && reminderDaysBefore.contains(daysUntil)
    }
    
    /**
     * Check if billing date has passed and needs to roll forward
     */
    fun needsRollForward(): Boolean {
        return isAutoPay && System.currentTimeMillis() > nextBillingDate
    }
    
    /**
     * Calculate the next billing date by rolling forward based on frequency
     * Returns a new entity with updated nextBillingDate
     */
    fun rollForwardBillingDate(): EnhancedSubscriptionEntity {
        if (!needsRollForward()) return this
        
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = nextBillingDate
        }
        
        // Roll forward based on frequency
        when (frequency) {
            BillingFrequency.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
            BillingFrequency.BI_WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 2)
            BillingFrequency.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
            BillingFrequency.QUARTERLY -> calendar.add(java.util.Calendar.MONTH, 3)
            BillingFrequency.SEMI_ANNUAL -> calendar.add(java.util.Calendar.MONTH, 6)
            BillingFrequency.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
        }
        
        // If still in the past (e.g., subscription was very overdue), keep rolling forward
        var newBillingDate = calendar.timeInMillis
        while (newBillingDate < System.currentTimeMillis()) {
            when (frequency) {
                BillingFrequency.WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 1)
                BillingFrequency.BI_WEEKLY -> calendar.add(java.util.Calendar.WEEK_OF_YEAR, 2)
                BillingFrequency.MONTHLY -> calendar.add(java.util.Calendar.MONTH, 1)
                BillingFrequency.QUARTERLY -> calendar.add(java.util.Calendar.MONTH, 3)
                BillingFrequency.SEMI_ANNUAL -> calendar.add(java.util.Calendar.MONTH, 6)
                BillingFrequency.YEARLY -> calendar.add(java.util.Calendar.YEAR, 1)
            }
            newBillingDate = calendar.timeInMillis
        }
        
        return copy(
            nextBillingDate = newBillingDate,
            updatedAt = System.currentTimeMillis(),
            pendingSync = true // Mark as needing sync to Firebase
        )
    }
    
    /**
     * Get status text for display
     */
    fun getStatusText(): String {
        val daysUntil = getDaysUntilBilling()
        return when {
            !isAutoPay && daysUntil < 0 -> "Overdue"
            daysUntil < 0 -> "Processing" // Auto-pay subscriptions don't show overdue
            daysUntil == 0 -> "Due today"
            daysUntil <= 7 -> "$daysUntil day${if (daysUntil > 1) "s" else ""} left"
            else -> "${daysUntil}d reminder"
        }
    }
}

/**
 * Enhanced billing frequency enum
 */
enum class BillingFrequency(val displayName: String, val monthsInterval: Double) {
    WEEKLY("Weekly", 0.23),
    BI_WEEKLY("Bi-weekly", 0.46),
    MONTHLY("Monthly", 1.0),
    QUARTERLY("Quarterly", 3.0),
    SEMI_ANNUAL("Semi-annual", 6.0),
    YEARLY("Yearly", 12.0)
}
