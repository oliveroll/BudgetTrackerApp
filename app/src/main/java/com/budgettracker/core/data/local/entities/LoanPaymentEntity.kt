package com.budgettracker.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverters
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.domain.model.LoanPayment
import java.util.Date

/**
 * Room entity for LoanPayment
 */
@Entity(
    tableName = "loan_payments",
    foreignKeys = [
        ForeignKey(
            entity = LoanEntity::class,
            parentColumns = ["id"],
            childColumns = ["loanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["loanId"]), Index(value = ["paymentDate"])]
)
@TypeConverters(Converters::class)
data class LoanPaymentEntity(
    @PrimaryKey
    val id: String,
    val loanId: String,
    val amount: Double,
    val principalAmount: Double,
    val interestAmount: Double,
    val paymentDate: Long,
    val paymentMethod: String,
    val notes: String,
    val isExtraPayment: Boolean,
    val exchangeRate: Double,
    val amountInUSD: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val syncStatus: String = "PENDING"
) {
    fun toDomain(): LoanPayment {
        return LoanPayment(
            id = id,
            loanId = loanId,
            amount = amount,
            principalAmount = principalAmount,
            interestAmount = interestAmount,
            paymentDate = Date(paymentDate),
            paymentMethod = paymentMethod,
            notes = notes,
            isExtraPayment = isExtraPayment,
            exchangeRate = exchangeRate,
            amountInUSD = amountInUSD
        )
    }
    
    companion object {
        fun fromDomain(payment: LoanPayment): LoanPaymentEntity {
            return LoanPaymentEntity(
                id = payment.id,
                loanId = payment.loanId,
                amount = payment.amount,
                principalAmount = payment.principalAmount,
                interestAmount = payment.interestAmount,
                paymentDate = payment.paymentDate.time,
                paymentMethod = payment.paymentMethod,
                notes = payment.notes,
                isExtraPayment = payment.isExtraPayment,
                exchangeRate = payment.exchangeRate,
                amountInUSD = payment.amountInUSD,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
