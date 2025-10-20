package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.math.pow

/**
 * Emergency fund account model for financial safety net tracking
 */
@Parcelize
data class EmergencyFund(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val bankName: String = "",
    val accountType: String = "High-Yield Savings", // HYSA, Savings, Money Market, etc.
    val accountNumber: String = "",
    val currentBalance: Double = 0.0,
    val targetGoal: Double = 5000.0, // User-defined goal
    val apy: Double = 0.0, // Annual Percentage Yield
    val compoundingFrequency: CompoundingFrequency = CompoundingFrequency.MONTHLY,
    val monthlyContribution: Double = 0.0,
    val autoDeposit: Boolean = false,
    val depositDayOfMonth: Int? = null,
    val monthsOfExpensesCovered: Int = 6, // Recommended: 3-6 months
    val minimumBalance: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate progress percentage toward goal
     */
    fun getProgressPercentage(): Float {
        return if (targetGoal > 0) {
            (currentBalance / targetGoal * 100).toFloat().coerceIn(0f, 100f)
        } else 0f
    }
    
    /**
     * Calculate remaining amount to reach goal
     */
    fun getRemainingToGoal(): Double {
        return (targetGoal - currentBalance).coerceAtLeast(0.0)
    }
    
    /**
     * Check if goal is reached
     */
    fun isGoalReached(): Boolean {
        return currentBalance >= targetGoal
    }
    
    /**
     * Calculate projected balance after X months with compound interest
     */
    fun projectBalanceAfterMonths(months: Int): Double {
        if (apy <= 0) {
            // Simple calculation without interest
            return currentBalance + (monthlyContribution * months)
        }
        
        val periodsPerYear = compoundingFrequency.periodsPerYear
        val ratePerPeriod = apy / 100 / periodsPerYear
        val totalPeriods = (months * periodsPerYear) / 12.0
        
        // Future value with compound interest and regular contributions
        // FV = PV(1+r)^n + PMT * (((1+r)^n - 1) / r)
        val futureValueOfPrincipal = currentBalance * (1 + ratePerPeriod).pow(totalPeriods)
        
        val contributionPerPeriod = monthlyContribution * 12 / periodsPerYear
        val futureValueOfContributions = if (ratePerPeriod > 0) {
            contributionPerPeriod * (((1 + ratePerPeriod).pow(totalPeriods) - 1) / ratePerPeriod)
        } else {
            contributionPerPeriod * totalPeriods
        }
        
        return futureValueOfPrincipal + futureValueOfContributions
    }
    
    /**
     * Calculate months needed to reach goal
     */
    fun getMonthsToReachGoal(): Int {
        if (currentBalance >= targetGoal) return 0
        if (monthlyContribution <= 0) return 999
        
        val remaining = getRemainingToGoal()
        
        if (apy <= 0) {
            // Simple calculation without interest
            return (remaining / monthlyContribution).toInt()
        }
        
        // Use financial formula to calculate months with compound interest
        val periodsPerYear = compoundingFrequency.periodsPerYear
        val ratePerPeriod = apy / 100 / periodsPerYear
        val contributionPerPeriod = monthlyContribution * 12 / periodsPerYear
        
        // Solve for n in: FV = PV(1+r)^n + PMT * (((1+r)^n - 1) / r)
        // This is complex, so we'll use an iterative approach
        var months = 0
        while (months < 600) { // Max 50 years
            val projected = projectBalanceAfterMonths(months)
            if (projected >= targetGoal) {
                return months
            }
            months++
        }
        
        return 999 // Goal not reachable with current settings
    }
    
    /**
     * Calculate projected date to reach goal
     */
    fun getProjectedGoalDate(): Date {
        val monthsNeeded = getMonthsToReachGoal()
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, monthsNeeded)
        return calendar.time
    }
    
    /**
     * Calculate total interest earned
     */
    fun getTotalInterestEarned(): Double {
        // This would require tracking initial balance and contributions over time
        // For now, return a simple estimate
        return (currentBalance - (monthlyContribution * 12)).coerceAtLeast(0.0)
    }
    
    /**
     * Calculate required monthly contribution to reach goal in X months
     */
    fun getRequiredMonthlyContribution(targetMonths: Int): Double {
        if (targetMonths <= 0) return getRemainingToGoal()
        if (currentBalance >= targetGoal) return 0.0
        
        val remaining = getRemainingToGoal()
        
        if (apy <= 0) {
            return remaining / targetMonths
        }
        
        // PMT = (FV - PV(1+r)^n) * (r / ((1+r)^n - 1))
        val periodsPerYear = compoundingFrequency.periodsPerYear
        val ratePerPeriod = apy / 100 / periodsPerYear
        val totalPeriods = (targetMonths * periodsPerYear) / 12.0
        
        val futureValueOfPrincipal = currentBalance * (1 + ratePerPeriod).pow(totalPeriods)
        val numerator = (targetGoal - futureValueOfPrincipal) * ratePerPeriod
        val denominator = (1 + ratePerPeriod).pow(totalPeriods) - 1
        
        val contributionPerPeriod = if (denominator > 0) {
            numerator / denominator
        } else {
            remaining / totalPeriods
        }
        
        return (contributionPerPeriod * periodsPerYear / 12).coerceAtLeast(0.0)
    }
    
    /**
     * Generate projection schedule
     */
    fun generateProjectionSchedule(months: Int = 12): List<EmergencyFundProjection> {
        val schedule = mutableListOf<EmergencyFundProjection>()
        val calendar = Calendar.getInstance()
        
        repeat(months) { month ->
            val projectedBalance = projectBalanceAfterMonths(month + 1)
            val interestEarned = if (month == 0) {
                projectedBalance - currentBalance - monthlyContribution
            } else {
                val previousBalance = schedule[month - 1].balance
                projectedBalance - previousBalance - monthlyContribution
            }
            
            calendar.add(Calendar.MONTH, 1)
            
            schedule.add(
                EmergencyFundProjection(
                    month = month + 1,
                    date = calendar.time.clone() as Date,
                    contribution = monthlyContribution,
                    interestEarned = interestEarned.coerceAtLeast(0.0),
                    balance = projectedBalance,
                    progressPercentage = (projectedBalance / targetGoal * 100).toFloat().coerceIn(0f, 100f)
                )
            )
        }
        
        return schedule
    }
}

/**
 * Compounding frequency options
 */
@Parcelize
enum class CompoundingFrequency(val displayName: String, val periodsPerYear: Int) : Parcelable {
    DAILY("Daily", 365),
    MONTHLY("Monthly", 12),
    QUARTERLY("Quarterly", 4),
    ANNUALLY("Annually", 1)
}

/**
 * Emergency fund projection for a specific month
 */
@Parcelize
data class EmergencyFundProjection(
    val month: Int,
    val date: Date,
    val contribution: Double,
    val interestEarned: Double,
    val balance: Double,
    val progressPercentage: Float
) : Parcelable

/**
 * Emergency fund deposit record
 */
@Parcelize
data class EmergencyFundDeposit(
    val id: String = UUID.randomUUID().toString(),
    val fundId: String = "",
    val amount: Double = 0.0,
    val depositDate: Date = Date(),
    val isAutomatic: Boolean = false,
    val transactionId: String? = null,
    val notes: String = ""
) : Parcelable

