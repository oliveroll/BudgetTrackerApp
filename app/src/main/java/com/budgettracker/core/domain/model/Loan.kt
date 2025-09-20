package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.pow

/**
 * Loan data model for tracking debt payments
 */
@Parcelize
data class Loan(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val name: String = "German Student Loan", // Default as per requirements
    val originalAmount: Double = 11550.0, // €11,000 in USD
    val remainingAmount: Double = originalAmount,
    val interestRate: Double = 0.0, // Annual interest rate as percentage
    val monthlyPayment: Double = 475.0, // Default monthly payment
    val currency: String = "EUR",
    val startDate: Date = Date(),
    val estimatedPayoffDate: Date = calculatePayoffDate(remainingAmount, monthlyPayment, interestRate),
    val loanType: LoanType = LoanType.STUDENT_LOAN,
    val lender: String = "German Government",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val paymentHistory: List<LoanPayment> = emptyList(),
    val notes: String = ""
) : Parcelable {
    
    /**
     * Calculate total interest paid so far
     */
    fun getTotalInterestPaid(): Double {
        return paymentHistory.sumOf { it.interestAmount }
    }
    
    /**
     * Calculate total principal paid so far
     */
    fun getTotalPrincipalPaid(): Double {
        return originalAmount - remainingAmount
    }
    
    /**
     * Calculate progress percentage
     */
    fun getProgressPercentage(): Float {
        return if (originalAmount > 0) {
            ((getTotalPrincipalPaid() / originalAmount) * 100).toFloat()
        } else 0f
    }
    
    /**
     * Calculate months remaining for payoff
     */
    fun getMonthsRemaining(): Long {
        return if (monthlyPayment > 0 && interestRate == 0.0) {
            // Simple calculation for 0% interest
            (remainingAmount / monthlyPayment).toLong()
        } else if (monthlyPayment > 0 && interestRate > 0) {
            // Complex calculation with interest
            calculateMonthsToPayoff(remainingAmount, monthlyPayment, interestRate / 12 / 100)
        } else 0L
    }
    
    /**
     * Calculate total amount that will be paid over the life of the loan
     */
    fun getTotalAmountToPay(): Double {
        val monthsRemaining = getMonthsRemaining()
        return (monthsRemaining * monthlyPayment) + getTotalInterestPaid()
    }
    
    /**
     * Calculate amount saved by making extra payments
     */
    fun calculateSavingsFromExtraPayment(extraAmount: Double): ExtraPaymentCalculation {
        val currentMonths = getMonthsRemaining()
        val currentTotalPayment = getTotalAmountToPay()
        
        val newMonthlyPayment = monthlyPayment + extraAmount
        val newMonths = if (interestRate == 0.0) {
            (remainingAmount / newMonthlyPayment).toLong()
        } else {
            calculateMonthsToPayoff(remainingAmount, newMonthlyPayment, interestRate / 12 / 100)
        }
        
        val newTotalPayment = newMonths * newMonthlyPayment
        
        return ExtraPaymentCalculation(
            monthsSaved = currentMonths - newMonths,
            interestSaved = currentTotalPayment - newTotalPayment,
            newPayoffDate = Date(Date().time + (newMonths * 30L * 24L * 60L * 60L * 1000L))
        )
    }
    
    /**
     * Get next payment due date
     */
    fun getNextPaymentDate(): Date {
        val lastPayment = paymentHistory.maxByOrNull { it.paymentDate }
        return if (lastPayment != null) {
            Date(lastPayment.paymentDate.time + (30L * 24L * 60L * 60L * 1000L)) // Add 30 days
        } else {
            Date() // Today if no payments made yet
        }
    }
    
    /**
     * Check if payment is overdue
     */
    fun isPaymentOverdue(): Boolean {
        val nextPaymentDate = getNextPaymentDate()
        return Date().after(nextPaymentDate)
    }
    
    /**
     * Convert amount to USD if loan is in different currency
     */
    fun getAmountInUSD(amount: Double, exchangeRate: Double = 1.1): Double {
        return if (currency == "USD") {
            amount
        } else {
            amount * exchangeRate // Default EUR to USD rate
        }
    }
    
    companion object {
        /**
         * Calculate estimated payoff date
         */
        private fun calculatePayoffDate(
            remainingAmount: Double,
            monthlyPayment: Double,
            interestRate: Double
        ): Date {
            if (monthlyPayment <= 0) return Date(Long.MAX_VALUE)
            
            val months = if (interestRate == 0.0) {
                (remainingAmount / monthlyPayment).toLong()
            } else {
                calculateMonthsToPayoff(remainingAmount, monthlyPayment, interestRate / 12 / 100)
            }
            
            val millisecondsToAdd = months * 30L * 24L * 60L * 60L * 1000L
            return Date(Date().time + millisecondsToAdd)
        }
        
        /**
         * Calculate months to payoff with interest
         */
        private fun calculateMonthsToPayoff(
            principal: Double,
            monthlyPayment: Double,
            monthlyInterestRate: Double
        ): Long {
            if (monthlyInterestRate == 0.0) {
                return (principal / monthlyPayment).toLong()
            }
            
            val numerator = kotlin.math.ln(1 + (principal * monthlyInterestRate) / monthlyPayment)
            val denominator = kotlin.math.ln(1 + monthlyInterestRate)
            
            return (numerator / denominator).toLong().coerceAtLeast(1L)
        }
    }
}

/**
 * Loan types
 */
@Parcelize
enum class LoanType(val displayName: String, val icon: String) : Parcelable {
    STUDENT_LOAN("Student Loan", "🎓"),
    MORTGAGE("Mortgage", "🏠"),
    CAR_LOAN("Car Loan", "🚗"),
    PERSONAL_LOAN("Personal Loan", "💰"),
    CREDIT_CARD("Credit Card", "💳"),
    OTHER("Other", "📋")
}

/**
 * Individual loan payment record
 */
@Parcelize
data class LoanPayment(
    val id: String = UUID.randomUUID().toString(),
    val loanId: String = "",
    val amount: Double = 0.0,
    val principalAmount: Double = 0.0,
    val interestAmount: Double = 0.0,
    val paymentDate: Date = Date(),
    val paymentMethod: String = "Bank Transfer",
    val notes: String = "",
    val isExtraPayment: Boolean = false,
    val exchangeRate: Double = 1.0, // For currency conversion
    val amountInUSD: Double = amount * exchangeRate
) : Parcelable

/**
 * Extra payment calculation result
 */
@Parcelize
data class ExtraPaymentCalculation(
    val monthsSaved: Long,
    val interestSaved: Double,
    val newPayoffDate: Date
) : Parcelable

/**
 * Loan summary for dashboard display
 */
@Parcelize
data class LoanSummary(
    val totalLoans: Int,
    val totalRemainingAmount: Double,
    val totalMonthlyPayments: Double,
    val averageInterestRate: Double,
    val nextPaymentDate: Date?,
    val nextPaymentAmount: Double,
    val totalPaidThisMonth: Double,
    val loansNearPayoff: List<Loan> // Loans with < 6 months remaining
) : Parcelable
