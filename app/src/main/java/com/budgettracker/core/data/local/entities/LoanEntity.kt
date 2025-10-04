package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.model.LoanType
import java.util.Date

/**
 * Room entity for Loan
 */
@Entity(tableName = "loans")
@TypeConverters(Converters::class)
data class LoanEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val originalAmount: Double,
    val remainingAmount: Double,
    val interestRate: Double,
    val monthlyPayment: Double,
    val currency: String,
    val startDate: Long,
    val estimatedPayoffDate: Long,
    val loanType: LoanType,
    val lender: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String,
    val syncStatus: String = "PENDING"
) {
    fun toDomain(): Loan {
        return Loan(
            id = id,
            userId = userId,
            name = name,
            originalAmount = originalAmount,
            remainingAmount = remainingAmount,
            interestRate = interestRate,
            monthlyPayment = monthlyPayment,
            currency = currency,
            startDate = Date(startDate),
            estimatedPayoffDate = Date(estimatedPayoffDate),
            loanType = loanType,
            lender = lender,
            isActive = isActive,
            createdAt = Date(createdAt),
            updatedAt = Date(updatedAt),
            notes = notes
        )
    }
    
    companion object {
        fun fromDomain(loan: Loan): LoanEntity {
            return LoanEntity(
                id = loan.id,
                userId = loan.userId,
                name = loan.name,
                originalAmount = loan.originalAmount,
                remainingAmount = loan.remainingAmount,
                interestRate = loan.interestRate,
                monthlyPayment = loan.monthlyPayment,
                currency = loan.currency,
                startDate = loan.startDate.time,
                estimatedPayoffDate = loan.estimatedPayoffDate.time,
                loanType = loan.loanType,
                lender = loan.lender,
                isActive = loan.isActive,
                createdAt = loan.createdAt.time,
                updatedAt = loan.updatedAt.time,
                notes = loan.notes
            )
        }
    }
}

