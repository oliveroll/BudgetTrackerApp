package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.budgettracker.core.domain.model.DebtLoan
import java.util.*

/**
 * Room entity for debt loan data with Firebase sync support
 */
@Entity(tableName = "debt_loans")
data class DebtLoanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val loanProvider: String = "",
    val loanType: String = "",
    val accountNumber: String = "",
    val originalAmount: Double = 0.0,
    val currentBalance: Double = 0.0,
    val interestRate: Double = 0.0,
    val repaymentStartDate: Long = System.currentTimeMillis(),
    val currentMonthlyPayment: Double = 0.0,
    val adjustedMonthlyPayment: Double? = null,
    val nextPaymentDueDate: Long = System.currentTimeMillis(),
    val minimumPayment: Double = 0.0,
    val currency: String = "USD",
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
    fun toDomain(): DebtLoan {
        return DebtLoan(
            id = id,
            userId = userId,
            loanProvider = loanProvider,
            loanType = loanType,
            accountNumber = accountNumber,
            originalAmount = originalAmount,
            currentBalance = currentBalance,
            interestRate = interestRate,
            repaymentStartDate = Date(repaymentStartDate),
            currentMonthlyPayment = currentMonthlyPayment,
            adjustedMonthlyPayment = adjustedMonthlyPayment,
            nextPaymentDueDate = Date(nextPaymentDueDate),
            minimumPayment = minimumPayment,
            currency = currency,
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
        fun fromDomain(loan: DebtLoan, pendingSync: Boolean = true): DebtLoanEntity {
            return DebtLoanEntity(
                id = loan.id,
                userId = loan.userId,
                loanProvider = loan.loanProvider,
                loanType = loan.loanType,
                accountNumber = loan.accountNumber,
                originalAmount = loan.originalAmount,
                currentBalance = loan.currentBalance,
                interestRate = loan.interestRate,
                repaymentStartDate = loan.repaymentStartDate.time,
                currentMonthlyPayment = loan.currentMonthlyPayment,
                adjustedMonthlyPayment = loan.adjustedMonthlyPayment,
                nextPaymentDueDate = loan.nextPaymentDueDate.time,
                minimumPayment = loan.minimumPayment,
                currency = loan.currency,
                notes = loan.notes,
                isActive = loan.isActive,
                createdAt = loan.createdAt.time,
                updatedAt = loan.updatedAt.time,
                pendingSync = pendingSync
            )
        }
    }
}

/**
 * Room entity for debt payment records
 */
@Entity(tableName = "debt_payment_records")
data class DebtPaymentRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val loanId: String = "",
    val amount: Double = 0.0,
    val paymentDate: Long = System.currentTimeMillis(),
    val principalPaid: Double = 0.0,
    val interestPaid: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val pendingSync: Boolean = false
)

