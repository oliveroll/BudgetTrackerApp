# PostHog Screen Tracking - Implementation Summary

## ✅ Screens with Tracking ADDED

### Main App Screens
1. ✅ **Dashboard** - `MobileFriendlyDashboard.kt`
2. ✅ **Transactions List** - `TransactionListScreen.kt`
3. ✅ **Add Transaction** - `AddTransactionScreen.kt`
4. ✅ **Budget Overview** - `MobileBudgetOverviewScreen.kt`
5. ✅ **Settings** - `EnhancedSettingsScreen.kt`
6. ✅ **Login** - `LoginScreen.kt`

### Financial Goals
7. ✅ **Roth IRA** - `RothIRAScreen.kt`
8. ✅ **Emergency Fund** - `EmergencyFundScreen.kt`
9. ✅ **ETF Portfolio** - `ETFPortfolioScreen.kt`
10. ✅ **Debt Journey** - `DebtJourneyScreen.kt`
11. ✅ **Financial Goals Main** - `FinancialGoalsMainScreen.kt`

### Savings & Goals
12. ✅ **Savings Goals** - `SavingsGoalsScreen.kt`

### Onboarding
13. ✅ **Personal Info** - `PersonalInfoScreen.kt`
14. ✅ **All Set** - `AllSetScreen.kt`
15. ✅ **Onboarding Financial Goals** - `FinancialGoalsScreen.kt`

### Reports
16. ✅ **Reports** - `ReportsScreen.kt`

---

## ⏳ Screens NEEDING Manual Addition

Add this code to the following screens:

### 1. RegisterScreen.kt

```kotlin
// Add import
import com.budgettracker.core.utils.AnalyticsTracker

// Add after function declaration in RegisterScreen()
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Register")
    AnalyticsTracker.trackUserSignUp("email") // When user completes registration
}
```

### 2. SplashScreen.kt

```kotlin
// Add import
import com.budgettracker.core.utils.AnalyticsTracker

// Add after function declaration
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Splash")
}
```

### 3. AddGoalScreen.kt

```kotlin
// Add import
import com.budgettracker.core.utils.AnalyticsTracker

// Add after function declaration
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("AddGoal")
}
```

### 4. SubscriptionsScreen.kt

```kotlin
// Add import
import com.budgettracker.core.utils.AnalyticsTracker

// Add after function declaration
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Subscriptions")
}
```

### 5. RemindersScreen.kt

```kotlin
// Add import
import com.budgettracker.core.utils.AnalyticsTracker

// Add after function declaration
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Reminders")
}
```

---

## 🔔 Dialogs/Modals NEEDING Tracking

### Financial Goals Dialogs

#### 1. AddRothIRADialog.kt
```kotlin
// At the top of AddRothIRADialog composable
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddRothIRA")
}
```

#### 2. EditIRADialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_EditIRA")
}
```

#### 3. AddContributionDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddContribution")
}
```

#### 4. AddEmergencyFundDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddEmergencyFund")
}
```

#### 5. EditEmergencyFundDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_EditEmergencyFund")
}
```

#### 6. AddLoanDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddLoan")
}
```

#### 7. EditLoanDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_EditLoan")
}
```

#### 8. RecordPaymentDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_RecordPayment")
}
```

#### 9. AddETFPortfolioDialog.kt
```kotlin
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddETFPortfolio")
}
```

### Settings Dialogs

#### 10. SettingsDialogs.kt
Add tracking to each dialog function:

```kotlin
// In EditCategoryDialog
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_EditCategory")
}

// In AddCustomCategoryDialog
LaunchedEffect(Unit) {
    AnalyticsTracker.trackScreenViewed("Dialog_AddCustomCategory")
}

// Add similar for other dialogs in this file
```

---

## 📊 What Will Be Tracked in PostHog

Once complete, your PostHog dashboard will show:

### Screen Views Breakdown
```
Dashboard               ████████████ 45%
Transactions           ██████████   30%
BudgetOverview         ████████     20%
RothIRA                ████         10%
EmergencyFund          ███          8%
Settings               ██           5%
AddTransaction         ██           5%
ETFPortfolio          █            3%
SavingsGoals          █            3%
Dialog_AddRothIRA     █            2%
... and more
```

### User Journey Tracking
- Login → Dashboard → Transactions → AddTransaction
- Dashboard → Financial Goals → RothIRA → Dialog_AddRothIRA
- Dashboard → Budget → Subscriptions

---

## 🚀 Quick Add Script for Remaining Files

Run this to add tracking to remaining screens:

```bash
cd /home/oliver/BudgetTrackerApp

# RegisterScreen
sed -i '/^import kotlinx.coroutines.launch$/a import com.budgettracker.core.utils.AnalyticsTracker' \
  app/src/main/java/com/budgettracker/features/auth/presentation/RegisterScreen.kt

# SplashScreen  
sed -i '/^import kotlinx.coroutines.launch$/a import com.budgettracker.core.utils.AnalyticsTracker' \
  app/src/main/java/com/budgettracker/features/auth/presentation/SplashScreen.kt

# AddGoalScreen
sed -i '/^import kotlinx.coroutines.launch$/a import com.budgettracker.core.utils.AnalyticsTracker' \
  app/src/main/java/com/budgettracker/features/savings/presentation/AddGoalScreen.kt

# SubscriptionsScreen
sed -i '/^import kotlinx.coroutines.launch$/a import com.budgettracker.core.utils.AnalyticsTracker' \
  app/src/main/java/com/budgettracker/features/budget/presentation/SubscriptionsScreen.kt

# RemindersScreen
sed -i '/^import kotlinx.coroutines.launch$/a import com.budgettracker.core.utils.AnalyticsTracker' \
  app/src/main/java/com/budgettracker/features/budget/presentation/RemindersScreen.kt
```

Then manually add the `LaunchedEffect` tracking code at the start of each function.

---

## 📝 Pattern to Follow

For **every remaining screen and dialog**:

### Step 1: Add Import
```kotlin
import com.budgettracker.core.utils.AnalyticsTracker
```

### Step 2: Add Tracking at Start of Composable
```kotlin
@Composable
fun YourScreen() {
    // Track screen view - ADD THIS
    LaunchedEffect(Unit) {
        AnalyticsTracker.trackScreenViewed("ScreenName")
    }
    
    // Rest of your code...
}
```

### Step 3: For Dialogs, use "Dialog_" prefix
```kotlin
AnalyticsTracker.trackScreenViewed("Dialog_AddRothIRA")
```

---

## ✅ Benefits

Once all tracking is added, you'll be able to:

1. **See user flow**: Which screens users visit most
2. **Identify drop-offs**: Where users leave the app
3. **Measure feature adoption**: Are users using Roth IRA features?
4. **Optimize UX**: Which dialogs confuse users?
5. **Track engagement**: Daily/weekly screen views per user

---

## 🎯 Priority Order for Remaining Work

1. **High Priority** (User-facing screens):
   - RegisterScreen
   - AddGoalScreen
   - SubscriptionsScreen

2. **Medium Priority** (Dialogs):
   - AddRothIRADialog
   - AddEmergencyFundDialog
   - EditIRADialog

3. **Low Priority** (Internal):
   - SplashScreen
   - RemindersScreen

---

## 📊 View Your Data

Once tracking is complete:

1. **Dashboard**: https://us.posthog.com/project/246643/dashboard/641107
2. **Insight**: https://us.posthog.com/project/246643/insights/mITITE6I
3. **All Events**: https://us.posthog.com/project/246643/events

---

**Status**: 16 screens ✅ completed, ~5 screens + 10 dialogs remaining

**Estimated time to complete**: 15-20 minutes of manual additions

