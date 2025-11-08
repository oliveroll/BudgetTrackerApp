package com.budgettracker.core.domain.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.util.Date
import java.util.UUID

/**
 * Transaction data model representing income and expense transactions
 * 
 * FIXED: Uses LocalDate for transaction date to prevent timezone bugs.
 * No more "picked Nov 24, saved as Nov 23" issues!
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val amount: Double = 0.0,
    val category: TransactionCategory = TransactionCategory.MISCELLANEOUS,
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val date: LocalDate = LocalDate.now(),
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val tags: List<String> = emptyList(),
    val attachmentUrl: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isDeleted: Boolean = false
) : Parcelable {
    
    /**
     * Get formatted amount with proper sign based on transaction type
     */
    fun getSignedAmount(): Double {
        return when (type) {
            TransactionType.INCOME -> amount
            TransactionType.EXPENSE -> -amount
        }
    }
    
    /**
     * Check if transaction is from current month
     */
    fun isCurrentMonth(): Boolean {
        val now = LocalDate.now()
        return date.month == now.month && date.year == now.year
    }
    
    // Manual Parcelable implementation for LocalDate support
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeDouble(amount)
        parcel.writeString(category.name)
        parcel.writeString(type.name)
        parcel.writeString(description)
        parcel.writeString(date.toString()) // ISO format: yyyy-MM-dd
        parcel.writeByte(if (isRecurring) 1 else 0)
        parcel.writeString(recurringPeriod?.name)
        parcel.writeStringList(tags)
        parcel.writeString(attachmentUrl)
        parcel.writeString(location)
        parcel.writeString(notes)
        parcel.writeLong(createdAt.time)
        parcel.writeLong(updatedAt.time)
        parcel.writeByte(if (isDeleted) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(
                id = parcel.readString() ?: UUID.randomUUID().toString(),
                userId = parcel.readString() ?: "",
                amount = parcel.readDouble(),
                category = TransactionCategory.valueOf(parcel.readString() ?: "MISCELLANEOUS"),
                type = TransactionType.valueOf(parcel.readString() ?: "EXPENSE"),
                description = parcel.readString() ?: "",
                date = LocalDate.parse(parcel.readString() ?: LocalDate.now().toString()),
                isRecurring = parcel.readByte() != 0.toByte(),
                recurringPeriod = parcel.readString()?.let { RecurringPeriod.valueOf(it) },
                tags = parcel.createStringArrayList() ?: emptyList(),
                attachmentUrl = parcel.readString(),
                location = parcel.readString(),
                notes = parcel.readString(),
                createdAt = Date(parcel.readLong()),
                updatedAt = Date(parcel.readLong()),
                isDeleted = parcel.readByte() != 0.toByte()
            )
        }

        override fun newArray(size: Int): Array<Transaction?> = arrayOfNulls(size)
    }
}

/**
 * Transaction type enum
 */
enum class TransactionType : Parcelable {
    INCOME,
    EXPENSE;
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<TransactionType> {
        override fun createFromParcel(parcel: Parcel): TransactionType {
            return valueOf(parcel.readString() ?: "EXPENSE")
        }

        override fun newArray(size: Int): Array<TransactionType?> = arrayOfNulls(size)
    }
}

/**
 * Transaction categories with proper grouping
 */
@Parcelize
enum class TransactionCategory(
    val displayName: String,
    val type: TransactionType,
    val icon: String,
    val color: String
) : Parcelable {
    // Income Categories
    SALARY("Salary", TransactionType.INCOME, "💰", "#28a745"),
    FREELANCE("Freelance", TransactionType.INCOME, "💼", "#17a2b8"),
    INVESTMENT_RETURN("Investment Returns", TransactionType.INCOME, "📈", "#6f42c1"),
    OTHER_INCOME("Other Income", TransactionType.INCOME, "💸", "#20c997"),
    
    // Fixed Expenses
    RENT("Rent", TransactionType.EXPENSE, "🏠", "#dc3545"),
    UTILITIES("Utilities", TransactionType.EXPENSE, "⚡", "#fd7e14"),
    INTERNET("Internet", TransactionType.EXPENSE, "🌐", "#6610f2"),
    PHONE("Phone", TransactionType.EXPENSE, "📱", "#e83e8c"),
    SUBSCRIPTIONS("Subscriptions", TransactionType.EXPENSE, "📺", "#6f42c1"),
    LOAN_PAYMENT("Loan Payment", TransactionType.EXPENSE, "🏦", "#dc3545"),
    INSURANCE("Insurance", TransactionType.EXPENSE, "🛡️", "#495057"),
    
    // Variable Expenses
    GROCERIES("Groceries", TransactionType.EXPENSE, "🛒", "#28a745"),
    DINING_OUT("Dining Out", TransactionType.EXPENSE, "🍽️", "#ffc107"),
    TRANSPORTATION("Transportation", TransactionType.EXPENSE, "🚗", "#17a2b8"),
    ENTERTAINMENT("Entertainment", TransactionType.EXPENSE, "🎬", "#e83e8c"),
    CLOTHING("Clothing", TransactionType.EXPENSE, "👕", "#6610f2"),
    PERSONAL_CARE("Personal Care", TransactionType.EXPENSE, "💄", "#fd7e14"),
    HEALTHCARE("Healthcare", TransactionType.EXPENSE, "🏥", "#dc3545"),
    EDUCATION("Education", TransactionType.EXPENSE, "📚", "#20c997"),
    
    // Savings & Investments
    EMERGENCY_FUND("Emergency Fund", TransactionType.EXPENSE, "🚨", "#28a745"),
    RETIREMENT("Retirement", TransactionType.EXPENSE, "👴", "#6f42c1"),
    STOCKS_ETFS("Stocks/ETFs", TransactionType.EXPENSE, "📊", "#17a2b8"),
    CRYPTO("Cryptocurrency", TransactionType.EXPENSE, "₿", "#ffc107"),
    TRAVEL_FUND("Travel Fund", TransactionType.EXPENSE, "✈️", "#20c997"),
    
    // OPT/Visa Specific
    H1B_APPLICATION("H1B Application", TransactionType.EXPENSE, "📄", "#495057"),
    VISA_FEES("Visa Fees", TransactionType.EXPENSE, "🛂", "#6c757d"),
    
    // Other
    MISCELLANEOUS("Miscellaneous", TransactionType.EXPENSE, "📦", "#6c757d");
    
    companion object {
        fun getIncomeCategories() = values().filter { it.type == TransactionType.INCOME }
        fun getExpenseCategories() = values().filter { it.type == TransactionType.EXPENSE }
        fun getFixedExpenseCategories() = listOf(
            RENT, UTILITIES, INTERNET, PHONE, SUBSCRIPTIONS, LOAN_PAYMENT, INSURANCE
        )
        fun getVariableExpenseCategories() = listOf(
            GROCERIES, DINING_OUT, TRANSPORTATION, ENTERTAINMENT, CLOTHING, 
            PERSONAL_CARE, HEALTHCARE, EDUCATION
        )
        fun getSavingsCategories() = listOf(
            EMERGENCY_FUND, RETIREMENT, STOCKS_ETFS, CRYPTO, TRAVEL_FUND
        )
    }
}

/**
 * Recurring period for recurring transactions
 */
@Parcelize
enum class RecurringPeriod(val displayName: String, val days: Int) : Parcelable {
    DAILY("Daily", 1),
    WEEKLY("Weekly", 7),
    BI_WEEKLY("Bi-weekly", 14),
    MONTHLY("Monthly", 30),
    QUARTERLY("Quarterly", 90),
    YEARLY("Yearly", 365)
}
