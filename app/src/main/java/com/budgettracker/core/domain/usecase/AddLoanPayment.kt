package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.LoanPayment
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import javax.inject.Inject

/**
 * Use case to add a loan payment
 */
class AddLoanPayment @Inject constructor(
    private val loanRepository: LoanRepository
) {
    
    suspend operator fun invoke(payment: LoanPayment): Result<String> {
        return try {
            if (payment.amount <= 0) {
                return Result.Error("Payment amount must be greater than 0")
            }
            
            if (payment.principalAmount < 0 || payment.interestAmount < 0) {
                return Result.Error("Principal and interest amounts cannot be negative")
            }
            
            if (payment.principalAmount + payment.interestAmount != payment.amount) {
                return Result.Error("Principal + Interest must equal total payment amount")
            }
            
            loanRepository.addPayment(payment)
        } catch (e: Exception) {
            Result.Error("Failed to add loan payment: ${e.message}")
        }
    }
    
    fun calculatePaymentBreakdown(
        paymentAmount: Double,
        remainingBalance: Double,
        monthlyInterestRate: Double
    ): Pair<Double, Double> {
        val interestAmount = remainingBalance * monthlyInterestRate
        val principalAmount = (paymentAmount - interestAmount).coerceAtMost(remainingBalance)
        
        return Pair(interestAmount, principalAmount)
    }
}

