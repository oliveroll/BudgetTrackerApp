package com.budgettracker.features.settings.data.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

/**
 * User Settings - Persisted in Room and synced to Firestore
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val userId: String,
    
    // Account Settings
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    
    // App Settings
    @Embedded
    val notificationSettings: NotificationSettings = NotificationSettings(),
    
    // Financial Settings
    val currency: String = "USD",
    val currencySymbol: String = "$",
    
    // Theme Settings (placeholder)
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    
    // Biometric Settings (placeholder)
    val biometricEnabled: Boolean = false,
    
    // Onboarding status
    val isOnboardingCompleted: Boolean = false,
    
    // Sync metadata
    val lastSyncedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Notification Settings
 */
data class NotificationSettings(
    // Budget Alerts
    val lowBalanceAlertEnabled: Boolean = true,
    val lowBalanceThreshold: Double = 100.0,
    val lowBalanceFrequency: NotificationFrequency = NotificationFrequency.DAILY,
    
    // Bill Reminders
    val upcomingBillReminderEnabled: Boolean = true,
    val upcomingBillDaysBefore: Int = 3,
    val upcomingBillFrequency: NotificationFrequency = NotificationFrequency.DAILY,
    
    // Subscription Reminders
    val subscriptionRenewalEnabled: Boolean = true,
    val subscriptionRenewalDaysBefore: Int = 3,
    val subscriptionRenewalFrequency: NotificationFrequency = NotificationFrequency.DAILY,
    
    // Goal Milestones
    val goalMilestoneEnabled: Boolean = true,
    val goalMilestoneFrequency: NotificationFrequency = NotificationFrequency.WEEKLY,
    
    // System notification permission
    val notificationPermissionGranted: Boolean = false
)

enum class NotificationFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER
}

/**
 * Employment Settings
 */
@Entity(tableName = "employment_settings")
data class EmploymentSettings(
    @PrimaryKey
    val id: String,
    val userId: String,
    
    // Employment details
    val optStatus: String? = null, // OPT, H1B, Green Card, Citizen, etc.
    val employmentType: String = "FULL_TIME", // FULL_TIME, PART_TIME, CONTRACT, UNEMPLOYED
    val employer: String? = null,
    val jobTitle: String? = null,
    
    // Salary details
    val annualSalary: Double = 0.0,
    val payFrequency: PayFrequency = PayFrequency.BI_WEEKLY,
    val nextPayDate: Long? = null,
    
    // Metadata
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

enum class PayFrequency {
    WEEKLY,
    BI_WEEKLY,
    SEMI_MONTHLY,
    MONTHLY,
    ANNUALLY
}

/**
 * Custom Category
 */
@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey
    val id: String,
    val userId: String,
    
    // Category details
    val name: String,
    val type: CategoryType,
    val colorHex: String = "#FF6B6B",
    val iconName: String = "category",
    
    // Budget settings
    val budgetedAmount: Double? = null,
    val budgetPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    
    // State
    val isArchived: Boolean = false,
    val isSystem: Boolean = false, // System categories can't be deleted
    val transactionCount: Int = 0,
    
    // Metadata
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long? = null
)

enum class CategoryType {
    INCOME,
    EXPENSE,
    TRANSFER
}

enum class BudgetPeriod {
    WEEKLY,
    BI_WEEKLY,
    MONTHLY,
    QUARTERLY,
    ANNUALLY
}

/**
 * Firestore mapping extensions
 */
fun UserSettings.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "userId" to userId,
        "email" to email,
        "displayName" to displayName,
        "photoUrl" to photoUrl,
        "notificationSettings" to mapOf(
            "lowBalanceAlertEnabled" to notificationSettings.lowBalanceAlertEnabled,
            "lowBalanceThreshold" to notificationSettings.lowBalanceThreshold,
            "lowBalanceFrequency" to notificationSettings.lowBalanceFrequency.name,
            "upcomingBillReminderEnabled" to notificationSettings.upcomingBillReminderEnabled,
            "upcomingBillDaysBefore" to notificationSettings.upcomingBillDaysBefore,
            "upcomingBillFrequency" to notificationSettings.upcomingBillFrequency.name,
            "subscriptionRenewalEnabled" to notificationSettings.subscriptionRenewalEnabled,
            "subscriptionRenewalDaysBefore" to notificationSettings.subscriptionRenewalDaysBefore,
            "subscriptionRenewalFrequency" to notificationSettings.subscriptionRenewalFrequency.name,
            "goalMilestoneEnabled" to notificationSettings.goalMilestoneEnabled,
            "goalMilestoneFrequency" to notificationSettings.goalMilestoneFrequency.name,
            "notificationPermissionGranted" to notificationSettings.notificationPermissionGranted
        ),
        "currency" to currency,
        "currencySymbol" to currencySymbol,
        "themeMode" to themeMode,
        "biometricEnabled" to biometricEnabled,
        "isOnboardingCompleted" to isOnboardingCompleted,
        "updatedAt" to Timestamp.now(),
        "createdAt" to Timestamp(java.util.Date(createdAt))
    )
}

fun EmploymentSettings.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "optStatus" to optStatus,
        "employmentType" to employmentType,
        "employer" to employer,
        "jobTitle" to jobTitle,
        "annualSalary" to annualSalary,
        "payFrequency" to payFrequency.name,
        "nextPayDate" to nextPayDate?.let { Timestamp(java.util.Date(it)) },
        "isActive" to isActive,
        "updatedAt" to Timestamp.now(),
        "createdAt" to Timestamp(java.util.Date(createdAt))
    )
}

fun CustomCategory.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "userId" to userId,
        "name" to name,
        "type" to type.name,
        "colorHex" to colorHex,
        "iconName" to iconName,
        "budgetedAmount" to budgetedAmount,
        "budgetPeriod" to budgetPeriod.name,
        "isArchived" to isArchived,
        "isSystem" to isSystem,
        "transactionCount" to transactionCount,
        "updatedAt" to Timestamp.now(),
        "createdAt" to Timestamp(java.util.Date(createdAt))
    )
}

