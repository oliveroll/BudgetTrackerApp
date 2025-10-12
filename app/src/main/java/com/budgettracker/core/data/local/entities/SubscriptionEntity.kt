package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.Subscription
import com.budgettracker.core.domain.model.SubscriptionFrequency
import java.util.Date
import java.util.UUID

/**
 * Room entity for Subscription data
 */
@Entity(tableName = "subscriptions")
@TypeConverters(Converters::class)
data class SubscriptionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val cost: Double,
    val frequency: SubscriptionFrequency,
    val nextBillingDate: Long,
    val category: String,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true,
    val reminderDaysBefore: Int = 3, // Days before billing to send reminder
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
) {
    fun toDomain(): Subscription {
        return Subscription(
            id = id,
            userId = userId,
            name = name,
            cost = cost,
            frequency = frequency,
            nextBillingDate = Date(nextBillingDate),
            category = category,
            isActive = isActive,
            reminderEnabled = reminderEnabled,
            reminderDaysBefore = reminderDaysBefore,
            notes = notes,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        fun fromDomain(subscription: Subscription): SubscriptionEntity {
            return SubscriptionEntity(
                id = subscription.id,
                userId = subscription.userId,
                name = subscription.name,
                cost = subscription.cost,
                frequency = subscription.frequency,
                nextBillingDate = subscription.nextBillingDate.time,
                category = subscription.category,
                isActive = subscription.isActive,
                reminderEnabled = subscription.reminderEnabled,
                reminderDaysBefore = subscription.reminderDaysBefore,
                notes = subscription.notes,
                createdAt = subscription.createdAt.time,
                updatedAt = subscription.updatedAt.time
            )
        }
    }
}



