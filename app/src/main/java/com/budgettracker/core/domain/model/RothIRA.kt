package com.budgettracker.core.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

/**
 * Roth IRA account model for retirement savings tracking
 */
@Parcelize
data class RothIRA(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val brokerageName: String = "",
    val accountNumber: String = "",
    val annualContributionLimit: Double = 7000.0, // 2024 limit
    val contributionsThisYear: Double = 0.0,
    val currentBalance: Double = 0.0,
    val taxYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val recurringContributionAmount: Double? = null,
    val recurringContributionFrequency: ContributionFrequency = ContributionFrequency.BIWEEKLY,
    val recurringContributionStartDate: Date? = null,
    val recurringContributionDayOfMonth: Int? = null, // For monthly
    val autoIncrease: Boolean = false, // Auto-increase contributions with salary increases
    val autoIncreasePercentage: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
) : Parcelable {
    
    /**
     * Calculate remaining contribution room for the year
     */
    fun getRemainingContributionRoom(): Double {
        return (annualContributionLimit - contributionsThisYear).coerceAtLeast(0.0)
    }
    
    /**
     * Calculate progress percentage toward annual limit
     */
    fun getProgressPercentage(): Float {
        return if (annualContributionLimit > 0) {
            (contributionsThisYear / annualContributionLimit * 100).toFloat().coerceIn(0f, 100f)
        } else 0f
    }
    
    /**
     * Check if annual limit is reached
     */
    fun isLimitReached(): Boolean {
        return contributionsThisYear >= annualContributionLimit
    }
    
    /**
     * Calculate how many contributions remaining until year end
     */
    fun getContributionsRemainingThisYear(): Int {
        val now = Calendar.getInstance()
        val yearEnd = Calendar.getInstance().apply {
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }
        
        val weeksRemaining = ((yearEnd.timeInMillis - now.timeInMillis) / (1000L * 60 * 60 * 24 * 7)).toInt()
        
        return when (recurringContributionFrequency) {
            ContributionFrequency.WEEKLY -> weeksRemaining
            ContributionFrequency.BIWEEKLY -> weeksRemaining / 2
            ContributionFrequency.MONTHLY -> {
                val monthsRemaining = yearEnd.get(Calendar.MONTH) - now.get(Calendar.MONTH) + 1
                monthsRemaining.coerceAtLeast(0)
            }
            ContributionFrequency.QUARTERLY -> {
                val quartersRemaining = ((yearEnd.get(Calendar.MONTH) - now.get(Calendar.MONTH)) / 3) + 1
                quartersRemaining.coerceAtLeast(0)
            }
            ContributionFrequency.ANNUAL -> if (now.get(Calendar.MONTH) == Calendar.DECEMBER) 0 else 1
            ContributionFrequency.NONE -> 0
        }
    }
    
    /**
     * Calculate required contribution per period to max out by year end
     */
    fun getRequiredContributionToMaxOut(): Double {
        val remainingRoom = getRemainingContributionRoom()
        val contributionsRemaining = getContributionsRemainingThisYear()
        
        return if (contributionsRemaining > 0) {
            remainingRoom / contributionsRemaining
        } else remainingRoom
    }
    
    /**
     * Calculate projected contributions by year end
     */
    fun getProjectedYearEndContributions(): Double {
        val recurringAmount = recurringContributionAmount ?: 0.0
        val contributionsRemaining = getContributionsRemainingThisYear()
        
        return contributionsThisYear + (recurringAmount * contributionsRemaining)
    }
    
    /**
     * Check if on track to max out
     */
    fun isOnTrackToMaxOut(): Boolean {
        val projected = getProjectedYearEndContributions()
        return projected >= annualContributionLimit * 0.95 // Within 5%
    }
    
    /**
     * Calculate total growth (current balance - total contributions)
     */
    fun getTotalGrowth(): Double {
        return (currentBalance - contributionsThisYear).coerceAtLeast(0.0)
    }
    
    /**
     * Get days remaining in tax year
     */
    fun getDaysRemainingInYear(): Long {
        val now = Calendar.getInstance()
        val yearEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, taxYear)
            set(Calendar.MONTH, Calendar.DECEMBER)
            set(Calendar.DAY_OF_MONTH, 31)
        }
        
        return ((yearEnd.timeInMillis - now.timeInMillis) / (1000L * 60 * 60 * 24)).coerceAtLeast(0)
    }
}

/**
 * Contribution frequency options
 */
@Parcelize
enum class ContributionFrequency(val displayName: String, val perYear: Int) : Parcelable {
    WEEKLY("Weekly", 52),
    BIWEEKLY("Biweekly", 26),
    MONTHLY("Monthly", 12),
    QUARTERLY("Quarterly", 4),
    ANNUAL("Annual", 1),
    NONE("None", 0)
}

/**
 * IRA contribution record
 */
@Parcelize
data class IRAContribution(
    val id: String = UUID.randomUUID().toString(),
    val iraId: String = "",
    val amount: Double = 0.0,
    val contributionDate: Date = Date(),
    val taxYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isAutomatic: Boolean = false,
    val transactionId: String? = null, // Link to transaction if recorded
    val notes: String = ""
) : Parcelable

/**
 * IRA contribution calculator helper
 */
data class IRACalculation(
    val currentContributions: Double,
    val annualLimit: Double,
    val remainingRoom: Double,
    val contributionsRemaining: Int,
    val requiredPerContribution: Double,
    val projectedYearEnd: Double,
    val isOnTrack: Boolean,
    val daysRemaining: Long,
    val recommendedBiweeklyAmount: Double,
    val recommendedMonthlyAmount: Double
)

