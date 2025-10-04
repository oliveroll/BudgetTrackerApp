package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.RecurringPeriod
import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType
import com.budgettracker.core.domain.repository.TransactionRepository
import com.budgettracker.core.utils.Result
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to create a recurring transaction for loan payments
 */
class CreateRecurringLoanTransaction @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    
    suspend operator fun invoke(
        userId: String,
        loanName: String,
        monthlyPaymentAmount: Double,
        currency: String = "EUR"
    ): Result<String> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(2025, Calendar.DECEMBER, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.time
            
            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                userId = userId,
                amount = monthlyPaymentAmount,
                category = TransactionCategory.LOAN_PAYMENTS,
                type = TransactionType.EXPENSE,
                description = "$loanName Payment",
                date = startDate,
                isRecurring = true,
                recurringPeriod = RecurringPeriod.MONTHLY,
                tags = listOf("loan", "german-student-loan", "accelerated-payoff"),
                attachmentUrl = null,
                location = null,
                notes = "Accelerated monthly payment for $loanName. Amount: $monthlyPaymentAmount $currency (~$${String.format("%.2f", monthlyPaymentAmount * 1.05)} USD)",
                createdAt = Date(),
                updatedAt = Date(),
                isDeleted = false
            )
            
            transactionRepository.createTransaction(transaction)
        } catch (e: Exception) {
            Result.Error("Failed to create recurring loan transaction: ${e.message}")
        }
    }
}
