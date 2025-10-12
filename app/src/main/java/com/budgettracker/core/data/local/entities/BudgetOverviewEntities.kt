package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import java.util.UUID

/**
 * Enhanced Room entity for Balance tracking with real-time sync
 */
@Entity(tableName = "balance")
@TypeConverters(Converters::class)
data class BalanceEntity(
    @PrimaryKey
    val userId: String,
    val currentBalance: Double,
    val lastUpdatedBy: String = "user", // "user", "paycheck", "expense"
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Create updated balance
     */
    fun withNewBalance(
        newBalance: Double, 
        updatedBy: String = "user"
    ): BalanceEntity {
        return copy(
            currentBalance = newBalance,
            lastUpdatedBy = updatedBy,
            updatedAt = System.currentTimeMillis(),
            pendingSync = true,
            syncedAt = null
        )
    }
}

/**
 * Enhanced Room entity for Paycheck tracking
 */
@Entity(tableName = "paychecks")
@TypeConverters(Converters::class)
data class PaycheckEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: Long, // Epoch millis UTC
    val grossAmount: Double,
    val netAmount: Double,
    val deposited: Boolean = false,
    val depositedAt: Long? = null,
    val payPeriodStart: Long? = null,
    val payPeriodEnd: Long? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Check if paycheck is upcoming (within next 7 days)
     */
    fun isUpcoming(): Boolean {
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000)
        return date > now && date <= sevenDaysFromNow
    }
    
    /**
     * Get days until paycheck (negative if past)
     */
    fun getDaysUntilPaycheck(): Int {
        val now = System.currentTimeMillis()
        val diffInMillis = date - now
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * Mark as deposited
     */
    fun markDeposited(): PaycheckEntity {
        return copy(
            deposited = true,
            depositedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
    }
}

/**
 * Enhanced Room entity for FCM Reminders with recurring support
 */
@Entity(tableName = "fcm_reminders")
@TypeConverters(Converters::class)
data class ReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val type: ReminderType,
    val targetId: String, // ID of related subscription/essential/paycheck
    val fireAtUtc: Long, // When to send reminder (epoch millis UTC)
    val title: String,
    val message: String,
    val sent: Boolean = false,
    val sentAt: Long? = null,
    val fcmMessageId: String? = null,
    val recurringRule: String? = null, // JSON or cron-like rule for recurring
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Check if reminder should fire now
     */
    fun shouldFire(): Boolean {
        val now = System.currentTimeMillis()
        return !sent && fireAtUtc <= now
    }
    
    /**
     * Mark as sent
     */
    fun markSent(fcmId: String? = null): ReminderEntity {
        return copy(
            sent = true,
            sentAt = System.currentTimeMillis(),
            fcmMessageId = fcmId,
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
    }
    
    /**
     * Check if overdue (should have fired but didn't)
     */
    fun isOverdue(): Boolean {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - (60 * 60 * 1000)
        return !sent && fireAtUtc < oneHourAgo
    }
}

/**
 * Reminder types for different entities
 */
enum class ReminderType(val displayName: String) {
    SUBSCRIPTION("Subscription"),
    ESSENTIAL("Essential Expense"),
    PAYCHECK("Paycheck"),
    CUSTOM("Custom Reminder")
}

/**
 * Enhanced Room entity for FCM Device tokens
 */
@Entity(tableName = "fcm_devices")
@TypeConverters(Converters::class)
data class DeviceEntity(
    @PrimaryKey
    val deviceId: String, // Android ID or similar
    val userId: String,
    val fcmToken: String,
    val platform: String = "android",
    val appVersion: String,
    val notificationsEnabled: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Update token
     */
    fun updateToken(newToken: String): DeviceEntity {
        return copy(
            fcmToken = newToken,
            lastSeen = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
    }
    
    /**
     * Update last seen
     */
    fun updateLastSeen(): DeviceEntity {
        return copy(
            lastSeen = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            pendingSync = true
        )
    }
}
