package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.model.LoanPayment
import com.budgettracker.core.domain.model.LoanType
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to initialize the German Student Loan with exact payoff schedule
 */
class InitializeGermanLoanPlan @Inject constructor(
    private val loanRepository: LoanRepository
) {
    
    suspend operator fun invoke(userId: String): Result<String> {
        return try {
            val loan = createGermanStudentLoan(userId)
            
            when (val createResult = loanRepository.createLoan(loan)) {
                is Result.Success -> {
                    val scheduleResult = createPaymentSchedule(loan.id)
                    when (scheduleResult) {
                        is Result.Success -> Result.Success(loan.id)
                        is Result.Error -> Result.Error("Loan created but failed to create schedule: ${scheduleResult.message}")
                    }
                }
                is Result.Error -> Result.Error("Failed to create loan: ${createResult.message}")
            }
        } catch (e: Exception) {
            Result.Error("Failed to initialize German loan plan: ${e.message}")
        }
    }
    
    private fun createGermanStudentLoan(userId: String): Loan {
        val calendar = Calendar.getInstance()
        
        calendar.set(2025, Calendar.DECEMBER, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.time
        
        calendar.set(2026, Calendar.NOVEMBER, 30, 0, 0, 0)
        val payoffDate = calendar.time
        
        return Loan(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = "German Student Loan",
            originalAmount = 10439.01,
            remainingAmount = 10317.64,
            interestRate = 6.66,
            monthlyPayment = 900.0,
            currency = "EUR",
            startDate = startDate,
            estimatedPayoffDate = payoffDate,
            loanType = LoanType.STUDENT_LOAN,
            lender = "German Government",
            isActive = true,
            createdAt = Date(),
            updatedAt = Date(),
            notes = "Accelerated payoff plan starting Dec 2025. Original plan was €121.37/month for 117 months."
        )
    }
    
    private suspend fun createPaymentSchedule(loanId: String): Result<Unit> {
        return try {
            val payments = createExactPaymentSchedule(loanId)
            
            for (payment in payments) {
                when (val result = loanRepository.addPayment(payment)) {
                    is Result.Error -> return Result.Error("Failed to add payment: ${result.message}")
                    is Result.Success -> { /* Continue */ }
                }
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to create payment schedule: ${e.message}")
        }
    }
    
    private fun createExactPaymentSchedule(loanId: String): List<LoanPayment> {
        val payments = mutableListOf<LoanPayment>()
        val calendar = Calendar.getInstance()
        
        data class PaymentData(val monthYear: String, val amount: Double, val interest: Double, val principal: Double)
        
        val scheduleData = listOf(
            PaymentData("Dec 2025", 900.0, 57.26, 842.74),
            PaymentData("Jan 2026", 900.0, 52.58, 847.42),
            PaymentData("Feb 2026", 900.0, 47.88, 852.12),
            PaymentData("Mar 2026", 900.0, 43.16, 856.84),
            PaymentData("Apr 2026", 900.0, 38.40, 861.60),
            PaymentData("May 2026", 900.0, 33.62, 866.38),
            PaymentData("Jun 2026", 900.0, 28.81, 871.19),
            PaymentData("Jul 2026", 900.0, 23.98, 876.02),
            PaymentData("Aug 2026", 900.0, 19.11, 880.89),
            PaymentData("Sep 2026", 900.0, 14.22, 885.78),
            PaymentData("Oct 2026", 900.0, 9.31, 890.69),
            PaymentData("Nov 2026", 790.33, 4.36, 785.97)
        )
        
        for (paymentData in scheduleData) {
            val parts = paymentData.monthYear.split(" ")
            val month = when (parts[0]) {
                "Dec" -> Calendar.DECEMBER
                "Jan" -> Calendar.JANUARY
                "Feb" -> Calendar.FEBRUARY
                "Mar" -> Calendar.MARCH
                "Apr" -> Calendar.APRIL
                "May" -> Calendar.MAY
                "Jun" -> Calendar.JUNE
                "Jul" -> Calendar.JULY
                "Aug" -> Calendar.AUGUST
                "Sep" -> Calendar.SEPTEMBER
                "Oct" -> Calendar.OCTOBER
                "Nov" -> Calendar.NOVEMBER
                else -> Calendar.DECEMBER
            }
            val year = parts[1].toInt()
            
            calendar.set(year, month, 1, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val paymentDate = calendar.time
            
            val payment = LoanPayment(
                id = UUID.randomUUID().toString(),
                loanId = loanId,
                amount = paymentData.amount,
                principalAmount = paymentData.principal,
                interestAmount = paymentData.interest,
                paymentDate = paymentDate,
                paymentMethod = "Bank Transfer",
                notes = "Scheduled payment for ${paymentData.monthYear}",
                isExtraPayment = false,
                exchangeRate = 1.05,
                amountInUSD = paymentData.amount * 1.05
            )
            
            payments.add(payment)
        }
        
        return payments
    }
}
