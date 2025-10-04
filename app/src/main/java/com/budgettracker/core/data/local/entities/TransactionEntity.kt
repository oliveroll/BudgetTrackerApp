package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.RecurringPeriod
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import java.util.Date

/**
 * Room entity for Transaction
 */
@Entity(tableName = "transactions")
@TypeConverters(Converters::class)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val description: String,
    val date: Long,
    val isRecurring: Boolean,
    val recurringPeriod: RecurringPeriod?,
    val tags: List<String>,
    val attachmentUrl: String?,
    val location: String?,
    val notes: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val syncStatus: String = "PENDING"
) {
    fun toDomainModel(): Transaction {
        return Transaction(
            id = id,
            userId = userId,
            amount = amount,
            category = category,
            type = type,
            description = description,
            date = Date(date),
            isRecurring = isRecurring,
            recurringPeriod = recurringPeriod,
            tags = tags,
            attachmentUrl = attachmentUrl,
            location = location,
            notes = notes,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            isDeleted = isDeleted
        )
    }
    
    companion object {
        fun fromDomainModel(transaction: Transaction, syncStatus: String = "PENDING"): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                userId = transaction.userId,
                amount = transaction.amount,
                category = transaction.category,
                type = transaction.type,
                description = transaction.description,
                date = transaction.date.time,
                isRecurring = transaction.isRecurring,
                recurringPeriod = transaction.recurringPeriod,
                tags = transaction.tags,
                attachmentUrl = transaction.attachmentUrl,
                location = transaction.location,
                notes = transaction.notes,
                createdAt = transaction.createdAt.time,
                updatedAt = transaction.updatedAt.time,
                isDeleted = transaction.isDeleted,
                syncStatus = syncStatus
            )
        }
    }
}

