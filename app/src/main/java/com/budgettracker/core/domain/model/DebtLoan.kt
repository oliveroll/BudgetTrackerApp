package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.pow

/**
 * Debt loan model for Freedom Debt Journey tracking
 */
@Parcelize
data class DebtLoan(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val loanProvider: String = "",
    val loanType: String = "", // e.g., "KfW-Studienkredit", "Student Loan", "Credit Card"
    val accountNumber: String = "",
    val originalAmount: Double = 0.0,
    val currentBalance: Double = 0.0,
    val interestRate: Double = 0.0, // Annual percentage rate
    val repaymentStartDate: Date = Date(),
    val currentMonthlyPayment: Double = 0.0,
    val adjustedMonthlyPayment: Double? = null, // User can adjust payment
    val nextPaymentDueDate: Date = Date(),
    val minimumPayment: Double = 0.0,
    val currency: String = "USD",
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Get the effective monthly payment (adjusted or current)
     */
    fun getEffectiveMonthlyPayment(): Double {
        return adjustedMonthlyPayment ?: currentMonthlyPayment
    }
    
    /**
     * Calculate progress percentage (amount paid off)
     */
    fun getProgressPercentage(): Float {
        return if (originalAmount > 0) {
            ((originalAmount - currentBalance) / originalAmount * 100).toFloat().coerceIn(0f, 100f)
        } else 0f
    }
    
    /**
     * Calculate remaining balance
     */
    fun getRemainingBalance(): Double = currentBalance
    
    /**
     * Calculate total interest paid so far
     */
    fun getTotalInterestPaid(): Double {
        return (originalAmount - currentBalance) - getEffectiveMonthlyPayment() * getMonthsPaid()
    }
    
    /**
     * Calculate months already paid
     */
    fun getMonthsPaid(): Long {
        val now = Date()
        val diff = now.time - repaymentStartDate.time
        return (diff / (1000L * 60 * 60 * 24 * 30)).coerceAtLeast(0)
    }
    
    /**
     * Calculate estimated months remaining based on current payment
     */
    fun getEstimatedMonthsRemaining(): Int {
        val monthlyPayment = getEffectiveMonthlyPayment()
        if (monthlyPayment <= 0 || currentBalance <= 0) return 0
        
        val monthlyInterestRate = interestRate / 100 / 12
        if (monthlyInterestRate <= 0) {
            return (currentBalance / monthlyPayment).toInt()
        }
        
        // Use amortization formula: n = -log(1 - (r * P / A)) / log(1 + r)
        // where P = principal, A = monthly payment, r = monthly interest rate
        val numerator = 1 - (monthlyInterestRate * currentBalance / monthlyPayment)
        if (numerator <= 0) return 999 // Payment too low to cover interest
        
        return (-kotlin.math.ln(numerator) / kotlin.math.ln(1 + monthlyInterestRate)).toInt()
    }
    
    /**
     * Calculate projected payoff date
     */
    fun getProjectedPayoffDate(): Date {
        val monthsRemaining = getEstimatedMonthsRemaining()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthsRemaining)
        return calendar.time
    }
    
    /**
     * Generate amortization schedule
     */
    fun generateAmortizationSchedule(months: Int = 24): List<AmortizationPayment> {
        val schedule = mutableListOf<AmortizationPayment>()
        var balance = currentBalance
        val monthlyPayment = getEffectiveMonthlyPayment()
        val monthlyInterestRate = interestRate / 100 / 12
        
        val calendar = Calendar.getInstance()
        calendar.time = nextPaymentDueDate
        
        repeat(months) { index ->
            if (balance <= 0) return@repeat
            
            val interestCharge = balance * monthlyInterestRate
            val principalPayment = (monthlyPayment - interestCharge).coerceAtLeast(0.0)
            val actualPayment = if (balance < monthlyPayment) balance + interestCharge else monthlyPayment
            
            schedule.add(
                AmortizationPayment(
                    month = index + 1,
                    date = calendar.time.clone() as Date,
                    payment = actualPayment,
                    principal = principalPayment.coerceAtMost(balance),
                    interest = interestCharge,
                    remainingBalance = (balance - principalPayment).coerceAtLeast(0.0)
                )
            )
            
            balance -= principalPayment
            calendar.add(Calendar.MONTH, 1)
        }
        
        return schedule
    }
    
    /**
     * Simulate different payment amount
     */
    fun simulatePayment(newMonthlyPayment: Double): PaymentSimulation {
        val monthlyInterestRate = interestRate / 100 / 12
        var balance = currentBalance
        var monthCount = 0
        var totalInterest = 0.0
        
        while (balance > 0 && monthCount < 600) { // Max 50 years
            val interestCharge = balance * monthlyInterestRate
            val principalPayment = (newMonthlyPayment - interestCharge).coerceAtLeast(0.0)
            
            if (principalPayment <= 0) {
                // Payment doesn't cover interest
                return PaymentSimulation(
                    monthlyPayment = newMonthlyPayment,
                    monthsToPayoff = 999,
                    totalInterest = 999999.0,
                    totalPaid = 999999.0,
                    payoffDate = Date(Long.MAX_VALUE),
                    isViable = false
                )
            }
            
            totalInterest += interestCharge
            balance -= principalPayment
            monthCount++
        }
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthCount)
        
        return PaymentSimulation(
            monthlyPayment = newMonthlyPayment,
            monthsToPayoff = monthCount,
            totalInterest = totalInterest,
            totalPaid = currentBalance + totalInterest,
            payoffDate = calendar.time,
            isViable = true
        )
    }
}

/**
 * Single payment entry in amortization schedule
 */
@Parcelize
data class AmortizationPayment(
    val month: Int,
    val date: Date,
    val payment: Double,
    val principal: Double,
    val interest: Double,
    val remainingBalance: Double
) : Parcelable

/**
 * Payment simulation result
 */
@Parcelize
data class PaymentSimulation(
    val monthlyPayment: Double,
    val monthsToPayoff: Int,
    val totalInterest: Double,
    val totalPaid: Double,
    val payoffDate: Date,
    val isViable: Boolean
) : Parcelable

/**
 * Payment history record
 */
@Parcelize
data class DebtPaymentRecord(
    val id: String = UUID.randomUUID().toString(),
    val loanId: String = "",
    val amount: Double = 0.0,
    val paymentDate: Date = Date(),
    val principalPaid: Double = 0.0,
    val interestPaid: Double = 0.0,
    val balanceAfter: Double = 0.0,
    val notes: String = ""
) : Parcelable

