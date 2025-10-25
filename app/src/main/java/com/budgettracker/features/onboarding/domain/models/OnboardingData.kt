package com.budgettracker.features.onboarding.domain.models

/**
 * Onboarding data model
 */
data class OnboardingData(
    val displayName: String = "",
    val employmentStatus: EmploymentStatus = EmploymentStatus.EMPLOYED_OPT,
    val currency: Currency = Currency.USD,
    val monthlyBudget: Double? = null,
    val monthlySavingsGoal: Double? = null,
    val primaryFinancialGoal: FinancialGoal? = null
)

/**
 * Employment status options
 */
enum class EmploymentStatus(
    val displayName: String,
    val icon: String
) {
    EMPLOYED_OPT("Employed (OPT)", "💼"),
    STUDENT("Student", "📚"),
    OPT_SEEKING("OPT (Seeking Employment)", "🎓"),
    SELF_EMPLOYED("Self-Employed", "💡"),
    OTHER("Other", "👤")
}

/**
 * Supported currencies with flags
 */
enum class Currency(
    val displayName: String,
    val symbol: String,
    val flag: String,
    val code: String
) {
    USD("US Dollar", "$", "🇺🇸", "USD"),
    EUR("Euro", "€", "🇪🇺", "EUR"),
    GBP("British Pound", "£", "🇬🇧", "GBP"),
    CAD("Canadian Dollar", "CA$", "🇨🇦", "CAD"),
    INR("Indian Rupee", "₹", "🇮🇳", "INR"),
    CNY("Chinese Yuan", "¥", "🇨🇳", "CNY")
}

/**
 * Primary financial goals
 */
enum class FinancialGoal(
    val displayName: String,
    val description: String,
    val icon: String
) {
    SAVE_MORE("Save More", "Build your savings and emergency fund", "💰"),
    TRACK_SPENDING("Track Spending", "Monitor where your money goes", "📊"),
    PAY_OFF_DEBT("Pay Off Debt", "Become debt-free faster", "🎯"),
    MANAGE_BILLS("Manage Bills", "Never miss a payment", "💳")
}

/**
 * Validation result for onboarding form
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
)

