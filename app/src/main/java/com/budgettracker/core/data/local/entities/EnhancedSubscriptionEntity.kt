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
