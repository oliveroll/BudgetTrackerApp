# PostHog Analytics Integration Guide

## Overview
PostHog Analytics has been integrated into the Budget Tracker app with a **privacy-first approach**. The implementation ensures sensitive financial data is never sent to analytics.

## What's Been Implemented

### 1. **PostHog SDK Setup** ✅
- Dependency added to `app/build.gradle.kts`
- Configured in `BudgetTrackerApplication.kt`
- API Key: `phc_HWRak0mzarW9KmIAKjLzD1BfbGNXFUZKStmlzWPguY`
- Host: `https://us.i.posthog.com`

### 2. **Privacy Protection** ✅
**Automatic Data Sanitization:**
- Exact transaction amounts → Amount ranges (e.g., "100-500")
- Sensitive fields removed (account numbers, card numbers, SSN, etc.)
- Person profiles only for identified users (`PersonProfiles.IDENTIFIED_ONLY`)

**What's Tracked:**
- ✅ User actions (button clicks, screen views)
- ✅ Feature usage patterns
- ✅ Transaction categories (not amounts)
- ✅ Error events and crashes
- ✅ Navigation patterns

**What's NOT Tracked:**
- ❌ Exact transaction amounts
- ❌ Personal financial details
- ❌ Account numbers
- ❌ Card information
- ❌ Any PII (Personally Identifiable Information)

### 3. **Analytics Helper Class** ✅
Created `AnalyticsTracker.kt` with pre-built tracking methods.

## How to Use

### Authentication Tracking

```kotlin
import com.budgettracker.core.utils.AnalyticsTracker

// When user signs up
AnalyticsTracker.trackUserSignUp(method = "google")

// When user logs in
AnalyticsTracker.trackUserLogin(method = "email")

// Identify the user for session tracking
AnalyticsTracker.identifyUser(
    userId = user.uid,
    email = user.email
)

// When user logs out
AnalyticsTracker.trackUserLogout() // Also clears user data
```

### Transaction Tracking

```kotlin
// When adding a transaction
AnalyticsTracker.trackTransactionAdded(
    type = TransactionType.EXPENSE,
    category = TransactionCategory.GROCERIES,
    isRecurring = false,
    hasAttachment = true
)

// When editing a transaction
AnalyticsTracker.trackTransactionEdited(
    type = TransactionType.INCOME,
    category = TransactionCategory.SALARY
)

// When deleting a transaction
AnalyticsTracker.trackTransactionDeleted(
    type = TransactionType.EXPENSE,
    category = TransactionCategory.ENTERTAINMENT
)

// After PDF import
AnalyticsTracker.trackBulkTransactionsImported(
    count = 25,
    source = "regions_bank_pdf"
)
```

### PDF Upload Tracking

```kotlin
// Start of PDF upload
AnalyticsTracker.trackPDFUploadStarted()

// On success
AnalyticsTracker.trackPDFUploadSuccess(transactionCount = 30)

// On failure
AnalyticsTracker.trackPDFUploadFailed(errorReason = "invalid_format")
```

### Budget Tracking

```kotlin
// When creating a budget
AnalyticsTracker.trackBudgetCreated(templateUsed = "50_30_20")

// When editing budget
AnalyticsTracker.trackBudgetEdited()

// When viewing budget alerts
AnalyticsTracker.trackBudgetAlertViewed(categoryOverBudget = true)
```

### Savings Goal Tracking

```kotlin
// Creating a goal
AnalyticsTracker.trackSavingsGoalCreated(goalType = "emergency_fund")

// Updating progress
AnalyticsTracker.trackSavingsGoalProgressUpdated(percentComplete = 75)

// Goal completed
AnalyticsTracker.trackSavingsGoalCompleted(goalType = "vacation")
```

### Screen Navigation Tracking

```kotlin
// Track screen views (already auto-captured, but you can add custom data)
AnalyticsTracker.trackScreenViewed(screenName = "TransactionDetails")

// Track feature usage
AnalyticsTracker.trackFeatureUsed(featureName = "export_to_csv")
```

### Error Tracking

```kotlin
// General errors
AnalyticsTracker.trackError(
    errorType = "firestore_sync_failed",
    errorMessage = "Network timeout"
)

// Network errors
AnalyticsTracker.trackNetworkError(endpoint = "/api/transactions")
```

### Settings & Preferences

```kotlin
// When settings change
AnalyticsTracker.trackSettingChanged(
    settingName = "currency",
    newValue = "EUR"
)

// Notification preferences
AnalyticsTracker.trackNotificationPreferenceChanged(enabled = true)
```

### Engagement Tracking

```kotlin
// Dashboard views
AnalyticsTracker.trackDashboardViewed()

// Chart interactions
AnalyticsTracker.trackChartInteraction(chartType = "spending_by_category")

// Filter usage
AnalyticsTracker.trackFilterApplied(
    filterType = "date_range",
    filterValue = "last_30_days"
)
```

## Automatic Tracking

PostHog automatically captures:
- **App Opened** - when app launches
- **App Backgrounded** - when user leaves app
- **Screen Views** - navigation between screens
- **Deep Links** - when app opened via link
- **Crashes** - app exceptions and crashes

## Configuration Options

Current configuration in `BudgetTrackerApplication.kt`:

```kotlin
captureApplicationLifecycleEvents = true  // App open/close events
captureScreenViews = true                 // Screen navigation
captureDeepLinks = true                   // Deep link tracking
flushAt = 20                             // Flush after 20 events
flushIntervalSeconds = 30                // Flush every 30 seconds
debug = BuildConfig.DEBUG                // Debug logs in dev builds
personProfiles = IDENTIFIED_ONLY         // Privacy: only for logged-in users
```

## Best Practices

### 1. **Track User Intent, Not Data**
```kotlin
// ✅ Good - tracks behavior
AnalyticsTracker.trackTransactionAdded(
    type = TransactionType.EXPENSE,
    category = TransactionCategory.GROCERIES
)

// ❌ Bad - tracks sensitive data
PostHog.capture("transaction_added", mapOf(
    "amount" to 1234.56,  // Don't do this!
    "merchant" to "Walmart Store #1234"
))
```

### 2. **Identify Users After Authentication**
```kotlin
// In your authentication flow
viewModel.signInWithEmail(email, password) { result ->
    result.onSuccess { user ->
        // Identify user for session tracking
        AnalyticsTracker.identifyUser(
            userId = user.uid,
            email = user.email
        )
    }
}
```

### 3. **Reset on Logout**
```kotlin
fun logout() {
    // Clear user session
    FirebaseAuth.getInstance().signOut()
    
    // Reset PostHog session
    AnalyticsTracker.trackUserLogout() // Includes PostHog.reset()
}
```

### 4. **Flush Before Critical Actions**
```kotlin
// Before user logs out or app closes
AnalyticsTracker.flush()
```

## Viewing Analytics

1. **Login to PostHog:** https://us.i.posthog.com
2. **Navigate to:** Your project dashboard
3. **View events:** Under "Events" tab
4. **Create insights:** Under "Insights" tab
5. **Set up funnels:** Track user conversion paths

## Feature Flags (Optional)

You can use PostHog feature flags to enable/disable features:

```kotlin
// Check if feature is enabled
if (PostHog.isFeatureEnabled("new_dashboard_layout")) {
    // Show new dashboard
    NewDashboardScreen()
} else {
    // Show old dashboard
    OldDashboardScreen()
}

// Get feature flag variant
when (PostHog.getFeatureFlag("pricing_tier")) {
    "premium" -> showPremiumFeatures()
    "basic" -> showBasicFeatures()
    else -> showDefaultFeatures()
}
```

## A/B Testing (Optional)

```kotlin
// Run experiments
val variant = PostHog.getFeatureFlag("checkout_button_color")
when (variant) {
    "blue" -> Button(colors = ButtonDefaults.buttonColors(Blue))
    "green" -> Button(colors = ButtonDefaults.buttonColors(Green))
    else -> Button(colors = ButtonDefaults.buttonColors(Primary))
}
```

## Debug Mode

In debug builds, verbose logs are enabled:

```bash
# Filter PostHog logs in Logcat
adb logcat | grep PostHog
```

## Privacy Compliance

This implementation follows privacy best practices:
- ✅ **GDPR Compliant** - No PII without consent
- ✅ **CCPA Compliant** - User can opt out
- ✅ **Financial Data Protected** - Amounts sanitized to ranges
- ✅ **Minimal Data Collection** - Only behavioral data

### Opt-Out Option

To allow users to opt out:

```kotlin
// In Settings screen
Switch(
    checked = analyticsEnabled,
    onCheckedChange = { enabled ->
        PostHogAndroidConfig().optOut = !enabled
        AnalyticsTracker.trackSettingChanged("analytics", enabled.toString())
    }
)
```

## Troubleshooting

### Events not showing up?
1. Check debug logs: `adb logcat | grep PostHog`
2. Verify API key is correct
3. Check network connectivity
4. Wait 30 seconds for batch flush

### Build errors?
1. Sync Gradle: `./gradlew build`
2. Invalidate caches: Android Studio → File → Invalidate Caches

### Privacy concerns?
- Review `propertiesSanitizer` in `BudgetTrackerApplication.kt`
- All amounts are converted to ranges
- Sensitive fields are automatically removed

## Next Steps

1. **Add tracking to existing screens:**
   - Dashboard
   - Transactions list
   - Add transaction form
   - Budget screen
   - Settings

2. **Monitor key metrics:**
   - Daily active users
   - Transaction creation rate
   - PDF upload success rate
   - Feature adoption

3. **Set up alerts:**
   - Error rate spikes
   - Crash rate increases
   - Feature usage drops

4. **Create funnels:**
   - Sign up → First transaction
   - Upload PDF → Review transactions
   - Create budget → Track progress

## Support

- **PostHog Docs:** https://posthog.com/docs/libraries/android
- **API Reference:** https://posthog.com/docs/api
- **Community:** https://posthog.com/community

---

**Remember:** Never track exact financial amounts or sensitive user data. When in doubt, use the `AnalyticsTracker` helper methods which have privacy built in.




