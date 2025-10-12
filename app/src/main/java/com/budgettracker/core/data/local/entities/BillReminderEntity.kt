package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.BillReminder
import com.budgettracker.core.domain.model.ReminderStatus
import java.util.Date
import java.util.UUID

/**
 * Room entity for Bill Reminders
 */
@Entity(tableName = "bill_reminders")
@TypeConverters(Converters::class)
data class BillReminderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val title: String,
    val amount: Double,
    val dueDate: Long,
    val category: String,
    val status: ReminderStatus = ReminderStatus.UPCOMING,
    val isRecurring: Boolean = false,
    val recurringFrequency: String? = null, // "MONTHLY", "WEEKLY", etc.
    val reminderDate: Long, // When to send the notification
    val fcmMessageId: String? = null, // FCM message ID for tracking
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): BillReminder {
        return BillReminder(
            id = id,
            userId = userId,
            title = title,
            amount = amount,
            dueDate = Date(dueDate),
            category = category,
            status = status,
            isRecurring = isRecurring,
            recurringFrequency = recurringFrequency,
            reminderDate = Date(reminderDate),
            fcmMessageId = fcmMessageId,
            notes = notes,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        fun fromDomain(reminder: BillReminder): BillReminderEntity {
            return BillReminderEntity(
                id = reminder.id,
                userId = reminder.userId,
                title = reminder.title,
                amount = reminder.amount,
                dueDate = reminder.dueDate.time,
                category = reminder.category,
                status = reminder.status,
                isRecurring = reminder.isRecurring,
                recurringFrequency = reminder.recurringFrequency,
                reminderDate = reminder.reminderDate.time,
                fcmMessageId = reminder.fcmMessageId,
                notes = reminder.notes,
                createdAt = reminder.createdAt.time,
                updatedAt = reminder.updatedAt.time
            )
        }
    }
}



