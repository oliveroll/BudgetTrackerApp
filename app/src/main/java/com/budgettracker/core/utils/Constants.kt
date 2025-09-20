package com.budgettracker.core.utils

/**
 * App-wide constants
 */
object Constants {
    
    // Firebase Collections
    const val USERS_COLLECTION = "users"
    const val TRANSACTIONS_COLLECTION = "transactions"
    const val BUDGETS_COLLECTION = "budgets"
    const val GOALS_COLLECTION = "goals"
    const val LOANS_COLLECTION = "loans"
    const val RECURRING_COLLECTION = "recurring"
    
    // SharedPreferences Keys
    const val PREFS_NAME = "budget_tracker_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_IS_ONBOARDED = "is_onboarded"
    const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
    const val PREF_CURRENCY = "currency"
    const val PREF_THEME = "theme"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    
    // Date Formats
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val DATE_FORMAT_API = "yyyy-MM-dd"
    const val DATE_FORMAT_MONTH_YEAR = "yyyy-MM"
    const val TIME_FORMAT_DISPLAY = "h:mm a"
    
    // Currency Formats
    const val CURRENCY_USD = "USD"
    const val CURRENCY_EUR = "EUR"
    
    // Default Values
    const val DEFAULT_MONTHLY_INCOME = 5470.0
    const val DEFAULT_BASE_SALARY = 80000.0
    const val DEFAULT_EMERGENCY_FUND_MONTHS = 6
    const val DEFAULT_LOAN_AMOUNT = 11550.0
    const val DEFAULT_LOAN_PAYMENT = 475.0
    
    // Budget Alert Thresholds
    const val BUDGET_ALERT_THRESHOLD_50 = 0.5f
    const val BUDGET_ALERT_THRESHOLD_80 = 0.8f
    const val BUDGET_ALERT_THRESHOLD_100 = 1.0f
    
    // Notification Channels
    const val NOTIFICATION_CHANNEL_BUDGET = "budget_alerts"
    const val NOTIFICATION_CHANNEL_BILLS = "bill_reminders"
    const val NOTIFICATION_CHANNEL_GOALS = "goal_updates"
    
    // Request Codes
    const val REQUEST_CODE_BIOMETRIC = 1001
    const val REQUEST_CODE_CAMERA = 1002
    const val REQUEST_CODE_GALLERY = 1003
    
    // OPT Specific
    const val OPT_EMPLOYMENT_STATUS = "OPT"
    const val H1B_EMPLOYMENT_STATUS = "H1B"
    const val DEFAULT_COMPANY = "Ixana Quasistatics"
    const val DEFAULT_STATE = "Indiana"
    
    // Investment Limits
    const val ROTH_IRA_ANNUAL_LIMIT = 7000.0 // 2024 limit
    const val TRADITIONAL_401K_LIMIT = 23000.0 // 2024 limit
    
    // Exchange Rates (Default - should be fetched from API)
    const val EUR_TO_USD_RATE = 1.1
    const val USD_TO_EUR_RATE = 0.91
    
    // Chart Colors
    val CHART_COLORS = listOf(
        "#667eea", "#764ba2", "#f093fb", "#f5576c",
        "#4facfe", "#00f2fe", "#43e97b", "#38f9d7",
        "#ffecd2", "#fcb69f", "#a8edea", "#fed6e3",
        "#ff9a9e", "#fecfef", "#ffeaa7", "#fab1a0"
    )
    
    // Animation Durations
    const val ANIMATION_DURATION_SHORT = 150L
    const val ANIMATION_DURATION_MEDIUM = 300L
    const val ANIMATION_DURATION_LONG = 500L
    
    // Pagination
    const val DEFAULT_PAGE_SIZE = 20
    const val TRANSACTION_PAGE_SIZE = 50
    
    // File Upload
    const val MAX_FILE_SIZE_MB = 10L
    const val ALLOWED_IMAGE_TYPES = "image/*"
    
    // Regex Patterns
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    const val PHONE_PATTERN = "^[\\+]?[1-9]?[0-9]{7,12}$"
    
    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_UNKNOWN = "An unknown error occurred. Please try again."
    const val ERROR_INVALID_EMAIL = "Please enter a valid email address."
    const val ERROR_INVALID_AMOUNT = "Please enter a valid amount."
    const val ERROR_EMPTY_FIELD = "This field is required."
    
    // Success Messages
    const val SUCCESS_TRANSACTION_SAVED = "Transaction saved successfully!"
    const val SUCCESS_BUDGET_UPDATED = "Budget updated successfully!"
    const val SUCCESS_GOAL_CREATED = "Savings goal created successfully!"
    const val SUCCESS_PROFILE_UPDATED = "Profile updated successfully!"
}
