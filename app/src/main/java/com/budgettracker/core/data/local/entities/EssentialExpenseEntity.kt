package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import java.util.UUID

/**
 * Enhanced Room entity for Essential Expenses with monthly tracking
 * 
 * UNIQUENESS CONSTRAINT: Each user can have only ONE essential expense per category per period.
 * This prevents duplicate Rent, Groceries, etc. when rolling over to a new month.
 */
@Entity(
    tableName = "essential_expenses",
    indices = [
        androidx.room.Index(
            value = ["userId", "category", "period"], 
            unique = true,
            name = "index_essential_expenses_unique_category_period"
        )
    ]
)
@TypeConverters(Converters::class)
data class EssentialExpenseEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val name: String,
    val category: ExpenseCategory,
    val plannedAmount: Double,
    val actualAmount: Double? = null,
    val dueDay: Int? = null, // Day of month (1-31), null for flexible
    val paid: Boolean = false,
    val period: String, // "2024-10" for October 2024
    val reminderDaysBefore: List<Int>? = listOf(3, 1), // Days before due date
    val fcmReminderEnabled: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Get due date for current period
     */
    fun getDueDateMillis(): Long? {
        if (dueDay == null) return null
        
        val parts = period.split("-")
        if (parts.size != 2) return null
        
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, dueDay, 0, 0, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        
        return calendar.timeInMillis
    }
    
    /**
     * Check if expense is overdue
     */
    fun isOverdue(): Boolean {
        val dueDate = getDueDateMillis() ?: return false
        return !paid && System.currentTimeMillis() > dueDate
    }
    
    /**
     * Get days until due (negative if overdue)
     */
    fun getDaysUntilDue(): Int? {
        val dueDate = getDueDateMillis() ?: return null
        val now = System.currentTimeMillis()
        val diffInMillis = dueDate - now
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    /**
     * Check if needs reminder
     */
    fun needsReminder(): Boolean {
        val daysUntil = getDaysUntilDue() ?: return false
        return !paid && fcmReminderEnabled && 
               reminderDaysBefore?.contains(daysUntil) == true
    }
}

/**
 * Essential expense categories
 */
enum class ExpenseCategory(val displayName: String, val iconEmoji: String) {
    RENT("Rent", "🏠"),
    UTILITIES("Utilities", "⚡"),
    GROCERIES("Groceries", "🛒"),
    PHONE("Phone", "📱"),
    INSURANCE("Insurance", "🛡️"),
    TRANSPORTATION("Transportation", "🚗"),
    OTHER("Other", "💳");
    
    /**
     * Map to corresponding TransactionCategory for spending tracking
     */
    fun toTransactionCategory(): com.budgettracker.core.domain.model.TransactionCategory {
        return when (this) {
            RENT -> com.budgettracker.core.domain.model.TransactionCategory.RENT
            UTILITIES -> com.budgettracker.core.domain.model.TransactionCategory.UTILITIES
            GROCERIES -> com.budgettracker.core.domain.model.TransactionCategory.GROCERIES
            PHONE -> com.budgettracker.core.domain.model.TransactionCategory.PHONE
            INSURANCE -> com.budgettracker.core.domain.model.TransactionCategory.INSURANCE
            TRANSPORTATION -> com.budgettracker.core.domain.model.TransactionCategory.TRANSPORTATION
            OTHER -> com.budgettracker.core.domain.model.TransactionCategory.MISCELLANEOUS
        }
    }
    
    /**
     * Determine if this category should have a "Mark as Paid" button.
     * Transaction-driven categories (like Groceries) are tracked by transactions, not manual payment.
     * Fixed bills (like Rent, Phone) should have a paid button.
     */
    fun shouldShowPaidButton(): Boolean {
        return when (this) {
            // Transaction-driven categories: tracked by actual transactions, no paid button
            GROCERIES, TRANSPORTATION -> false
            
            // Fixed bills: should have a paid button
            RENT, UTILITIES, PHONE, INSURANCE, OTHER -> true
        }
    }
}
