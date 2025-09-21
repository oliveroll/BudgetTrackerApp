package com.budgettracker.core.domain.model

import java.util.Date

/**
 * User profile data model representing a user's basic information and financial setup
 */
data class UserProfile(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val monthlyIncome: Double = 5470.0, // Default based on requirements
    val currency: String = "USD",
    val employmentStatus: String = "OPT",
    val company: String = "Ixana Quasistatics",
    val baseSalary: Double = 80000.0,
    val state: String = "Indiana",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val profilePictureUrl: String? = null,
    val workAuthExpiryDate: Date? = null, // For OPT visa tracking
    val emergencyFundTarget: Double = monthlyIncome * 6, // 6 months for visa gaps
    val isOnboardingCompleted: Boolean = false
) {
    /**
     * Calculate net monthly income after taxes (rough estimate)
     */
    fun getNetMonthlyIncome(): Double {
        // Rough tax calculation for Indiana (assuming ~22% effective tax rate)
        return monthlyIncome * 0.78
    }
    
    /**
     * Get recommended emergency fund target based on employment status
     */
    fun getRecommendedEmergencyFund(): Double {
        return when (employmentStatus) {
            "OPT", "H1B" -> monthlyIncome * 6 // Higher for visa holders
            else -> monthlyIncome * 3
        }
    }
}
