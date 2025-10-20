package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budgettracker.core.domain.model.CompoundingFrequency
import com.budgettracker.core.domain.model.EmergencyFund
import java.util.*

/**
 * Room entity for emergency fund account data with Firebase sync support
 */
@Entity(tableName = "emergency_funds")
data class EmergencyFundEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val bankName: String = "",
    val accountType: String = "High-Yield Savings",
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val targetGoal: Double = 5000.0,
    val apy: Double = 0.0,
    val compoundingFrequency: String = CompoundingFrequency.MONTHLY.name,
    val monthlyContribution: Double = 0.0,
    val autoDeposit: Boolean = false,
    val depositDayOfMonth: Int? = null,
    val monthsOfExpensesCovered: Int = 6,
    val minimumBalance: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
) {
    
    /**
     * Convert to domain model
     */
    fun toDomain(): EmergencyFund {
        return EmergencyFund(
            id = id,
            userId = userId,
            bankName = bankName,
            accountType = accountType,
            accountNumber = accountNumber,
            currentBalance = currentBalance,
            targetGoal = targetGoal,
            apy = apy,
            compoundingFrequency = CompoundingFrequency.valueOf(compoundingFrequency),
            monthlyContribution = monthlyContribution,
            autoDeposit = autoDeposit,
            depositDayOfMonth = depositDayOfMonth,
            monthsOfExpensesCovered = monthsOfExpensesCovered,
            minimumBalance = minimumBalance,
            notes = notes,
            isActive = isActive,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt)
        )
    }
    
    companion object {
        /**
         * Create entity from domain model
         */
        fun fromDomain(fund: EmergencyFund, pendingSync: Boolean = true): EmergencyFundEntity {
            return EmergencyFundEntity(
                id = fund.id,
                userId = fund.userId,
                bankName = fund.bankName,
                accountType = fund.accountType,
                accountNumber = fund.accountNumber,
                currentBalance = fund.currentBalance,
                targetGoal = fund.targetGoal,
                apy = fund.apy,
                compoundingFrequency = fund.compoundingFrequency.name,
                monthlyContribution = fund.monthlyContribution,
                autoDeposit = fund.autoDeposit,
                depositDayOfMonth = fund.depositDayOfMonth,
                monthsOfExpensesCovered = fund.monthsOfExpensesCovered,
                minimumBalance = fund.minimumBalance,
                notes = fund.notes,
                isActive = fund.isActive,
                createdAt = fund.createdAt.time,
                updatedAt = fund.updatedAt.time,
                pendingSync = pendingSync
            )
        }
    }
}

/**
 * Room entity for emergency fund deposit records
 */
@Entity(tableName = "emergency_fund_deposits")
data class EmergencyFundDepositEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fundId: String = "",
    val amount: Double = 0.0,
    val depositDate: Long = System.currentTimeMillis(),
    val isAutomatic: Boolean = false,
    val transactionId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
)

