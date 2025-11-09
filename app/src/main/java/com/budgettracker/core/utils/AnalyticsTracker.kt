package com.budgettracker.core.utils

import com.posthog.PostHog
import com.budgettracker.core.domain.model.TransactionCategory
import com.budgettracker.core.domain.model.TransactionType

/**
 * Helper class for tracking analytics events in the Budget Tracker app
 * 
 * Privacy-first approach:
 * - No exact amounts tracked (only ranges)
 * - No personal identifiable information
 * - Category-level insights only
 */
object AnalyticsTracker {
    
    // ========== User Events ==========
    
    fun trackUserSignUp(method: String) {
        PostHog.capture(
            event = "user_signed_up",
            properties = mapOf(
                "auth_method" to method // "email", "google", etc.
            )
        )
    }
    
    fun trackUserLogin(method: String) {
        PostHog.capture(
            event = "user_logged_in",
            properties = mapOf(
                "auth_method" to method
            )
        )
    }
    
    fun trackUserLogout() {
        PostHog.capture(event = "user_logged_out")
        PostHog.reset() // Clear user data
    }
    
    fun identifyUser(userId: String, email: String? = null) {
        val properties = mutableMapOf<String, Any>("platform" to "android")
        email?.let { properties["email"] = it }
        
        PostHog.identify(
            distinctId = userId,
            userProperties = properties
        )
    }
    
    // ========== Transaction Events ==========
    
    fun trackTransactionAdded(
        type: TransactionType,
        category: TransactionCategory,
        isRecurring: Boolean = false,
        hasAttachment: Boolean = false
    ) {
        PostHog.capture(
            event = "transaction_added",
            properties = mapOf(
                "transaction_type" to type.name,
                "category" to category.displayName,
                "is_recurring" to isRecurring,
                "has_attachment" to hasAttachment,
                "entry_method" to "manual"
            )
        )
    }
    
    fun trackTransactionEdited(
        type: TransactionType,
        category: TransactionCategory
    ) {
        PostHog.capture(
            event = "transaction_edited",
            properties = mapOf(
                "transaction_type" to type.name,
                "category" to category.displayName
            )
        )
    }
    
    fun trackTransactionDeleted(
        type: TransactionType,
        category: TransactionCategory
    ) {
        PostHog.capture(
            event = "transaction_deleted",
            properties = mapOf(
                "transaction_type" to type.name,
                "category" to category.displayName
            )
        )
    }
    
    fun trackBulkTransactionsImported(count: Int, source: String) {
        PostHog.capture(
            event = "transactions_imported",
            properties = mapOf(
                "transaction_count" to count,
                "import_source" to source // "pdf", "csv", "bank_statement"
            )
        )
    }
    
    // ========== PDF Upload Events ==========
    
    fun trackPDFUploadStarted() {
        PostHog.capture(event = "pdf_upload_started")
    }
    
    fun trackPDFUploadSuccess(transactionCount: Int) {
        PostHog.capture(
            event = "pdf_upload_success",
            properties = mapOf(
                "transactions_extracted" to transactionCount
            )
        )
    }
    
    fun trackPDFUploadFailed(errorReason: String) {
        PostHog.capture(
            event = "pdf_upload_failed",
            properties = mapOf(
                "error_reason" to errorReason
            )
        )
    }
    
    // ========== Budget Events ==========
    
    fun trackBudgetCreated(templateUsed: String? = null) {
        val properties = mutableMapOf<String, Any>()
        templateUsed?.let { properties["template"] = it }
        
        PostHog.capture(
            event = "budget_created",
            properties = properties.ifEmpty { null }
        )
    }
    
    fun trackBudgetEdited() {
        PostHog.capture(event = "budget_edited")
    }
    
    fun trackBudgetAlertViewed(categoryOverBudget: Boolean) {
        PostHog.capture(
            event = "budget_alert_viewed",
            properties = mapOf(
                "category_over_budget" to categoryOverBudget
            )
        )
    }
    
    // ========== Savings Goal Events ==========
    
    fun trackSavingsGoalCreated(goalType: String) {
        PostHog.capture(
            event = "savings_goal_created",
            properties = mapOf(
                "goal_type" to goalType // "emergency_fund", "custom", etc.
            )
        )
    }
    
    fun trackSavingsGoalProgressUpdated(percentComplete: Int) {
        PostHog.capture(
            event = "savings_goal_progress_updated",
            properties = mapOf(
                "percent_complete" to percentComplete
            )
        )
    }
    
    fun trackSavingsGoalCompleted(goalType: String) {
        PostHog.capture(
            event = "savings_goal_completed",
            properties = mapOf(
                "goal_type" to goalType
            )
        )
    }
    
    // ========== Navigation Events ==========
    
    fun trackScreenViewed(screenName: String) {
        PostHog.screen(
            screenTitle = screenName,
            properties = mapOf(
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
    
    fun trackFeatureUsed(featureName: String) {
        PostHog.capture(
            event = "feature_used",
            properties = mapOf(
                "feature" to featureName
            )
        )
    }
    
    // ========== Settings & Preferences ==========
    
    fun trackSettingChanged(settingName: String, newValue: String) {
        PostHog.capture(
            event = "setting_changed",
            properties = mapOf(
                "setting" to settingName,
                "value" to newValue
            )
        )
    }
    
    fun trackNotificationPreferenceChanged(enabled: Boolean) {
        PostHog.capture(
            event = "notification_preference_changed",
            properties = mapOf(
                "enabled" to enabled
            )
        )
    }
    
    // ========== Error Events ==========
    
    fun trackError(errorType: String, errorMessage: String? = null) {
        val properties = mutableMapOf<String, Any>("error_type" to errorType)
        errorMessage?.let { properties["error_message"] = it }
        
        PostHog.capture(
            event = "error_occurred",
            properties = properties
        )
    }
    
    fun trackNetworkError(endpoint: String) {
        PostHog.capture(
            event = "network_error",
            properties = mapOf(
                "endpoint" to endpoint
            )
        )
    }
    
    // ========== Engagement Events ==========
    
    fun trackDashboardViewed() {
        PostHog.capture(event = "dashboard_viewed")
    }
    
    fun trackChartInteraction(chartType: String) {
        PostHog.capture(
            event = "chart_interaction",
            properties = mapOf(
                "chart_type" to chartType // "spending_by_category", "income_vs_expenses", etc.
            )
        )
    }
    
    fun trackFilterApplied(filterType: String, filterValue: String) {
        PostHog.capture(
            event = "filter_applied",
            properties = mapOf(
                "filter_type" to filterType,
                "filter_value" to filterValue
            )
        )
    }
    
    // ========== Subscription & Payment Events ==========
    
    fun trackSubscriptionAdded(category: String) {
        PostHog.capture(
            event = "subscription_added",
            properties = mapOf(
                "category" to category
            )
        )
    }
    
    fun trackSubscriptionCancelled(category: String) {
        PostHog.capture(
            event = "subscription_cancelled",
            properties = mapOf(
                "category" to category
            )
        )
    }
    
    // ========== Helper Functions ==========
    
    /**
     * Convert exact amount to privacy-friendly range
     */
    private fun amountToRange(amount: Double): String {
        return when {
            amount < 10 -> "0-10"
            amount < 50 -> "10-50"
            amount < 100 -> "50-100"
            amount < 500 -> "100-500"
            amount < 1000 -> "500-1000"
            amount < 5000 -> "1000-5000"
            else -> "5000+"
        }
    }
    
    /**
     * Flush queued events immediately
     * Useful before user logs out or app closes
     */
    fun flush() {
        PostHog.flush()
    }
}

