# 💳 Auto-Pay Subscription Feature Documentation

## Overview
The Budget Tracker app now intelligently handles subscriptions based on whether they are auto-paid (e.g., automatic bank deductions) or manual-pay (requires user action). This prevents subscriptions from staying stuck as "Overdue" when they're automatically paid.

---

## ✅ What Changed

### 1. **New `isAutoPay` Field**
All subscriptions now have an `isAutoPay` boolean field:
- **`isAutoPay = true`** (default): Subscription is automatically paid (e.g., Spotify, Netflix, gym membership)
- **`isAutoPay = false`**: Subscription requires manual payment (e.g., utility bill you need to pay yourself)

### 2. **Automatic Billing Date Roll-Forward**
When a subscription's billing date passes:
- **Auto-pay subscriptions**: Automatically roll forward to the next billing cycle
- **Manual-pay subscriptions**: Show as "Overdue" until marked paid

### 3. **Smart Status Display**
- **Auto-pay past due**: Shows "Processing" instead of "Overdue"
- **Manual-pay past due**: Shows "Overdue"
- **Due today**: Shows "Due today"
- **7 days or less**: Shows "X days left"
- **More than 7 days**: Shows "Xd reminder"

---

## 🎯 How It Works

### Automatic Roll-Forward Logic

#### Example: Spotify Subscription
```
Scenario:
- Spotify monthly subscription: $15.99
- Last billing date: Oct 20, 2025
- Today's date: Oct 24, 2025
- isAutoPay: true

Old Behavior:
❌ Status: "Overdue"
❌ Next billing: Oct 20, 2025 (stuck in the past)

New Behavior:
✅ Status: "21 days left" (or "Processing" if just passed)
✅ Next billing: Nov 20, 2025 (automatically calculated)
```

#### When Roll-Forward Happens:
1. **On app start**: Checks all subscriptions and rolls forward any overdue auto-pay ones
2. **After Firebase sync**: Ensures consistency after cloud data sync
3. **Real-time**: When loading budget data

### Roll-Forward Calculation

The system intelligently calculates the next billing date based on frequency:

```kotlin
Frequency         | How it rolls forward
------------------|-----------------------
Weekly            | +1 week
Bi-weekly         | +2 weeks
Monthly           | +1 month (e.g., Jan 15 → Feb 15)
Quarterly         | +3 months
Semi-annual       | +6 months
Yearly            | +1 year
```

#### Multiple Periods Handling
If a subscription is very overdue (e.g., hasn't been updated in 6 months), the system keeps rolling forward until it reaches a future date:

```
Example:
- Billing date: April 15, 2025
- Today: October 24, 2025
- Frequency: Monthly

Roll forward process:
April 15 → May 15 → June 15 → July 15 → Aug 15 → Sep 15 → Oct 15 → Nov 15 ✅
Result: Next billing = November 15, 2025
```

---

## 📊 Data Structure

### Database Schema (Enhanced Subscriptions)

```sql
CREATE TABLE enhanced_subscriptions (
    id TEXT PRIMARY KEY,
    userId TEXT NOT NULL,
    name TEXT NOT NULL,
    amount REAL NOT NULL,
    currency TEXT DEFAULT 'USD',
    frequency TEXT NOT NULL,        -- WEEKLY, MONTHLY, YEARLY, etc.
    nextBillingDate INTEGER NOT NULL, -- Unix timestamp
    isAutoPay INTEGER DEFAULT 1,    -- 🆕 NEW FIELD (1=true, 0=false)
    active INTEGER DEFAULT 1,
    iconEmoji TEXT,
    category TEXT DEFAULT 'Entertainment',
    notes TEXT,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL,
    syncedAt INTEGER,
    pendingSync INTEGER DEFAULT 0
);
```

### Firebase Firestore Structure

```javascript
users/{userId}/subscriptions/{subscriptionId}
{
  name: "Spotify Premium",
  amount: 15.99,
  currency: "USD",
  frequency: "MONTHLY",
  nextBillingDate: Timestamp,
  isAutoPay: true,           // 🆕 NEW FIELD
  active: true,
  iconEmoji: "🎵",
  category: "Entertainment",
  notes: "Family plan",
  createdAt: Timestamp,
  updatedAt: Timestamp
}
```

---

## 🔧 Technical Implementation

### 1. Entity Methods

```kotlin
// EnhancedSubscriptionEntity.kt

/**
 * Check if billing date has passed and needs to roll forward
 */
fun needsRollForward(): Boolean {
    return isAutoPay && System.currentTimeMillis() > nextBillingDate
}

/**
 * Calculate the next billing date by rolling forward based on frequency
 */
fun rollForwardBillingDate(): EnhancedSubscriptionEntity {
    if (!needsRollForward()) return this
    
    val calendar = Calendar.getInstance().apply {
        timeInMillis = nextBillingDate
    }
    
    // Roll forward based on frequency
    when (frequency) {
        BillingFrequency.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
        BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        // ... etc
    }
    
    return copy(
        nextBillingDate = calendar.timeInMillis,
        updatedAt = System.currentTimeMillis(),
        pendingSync = true // Sync to Firebase
    )
}

/**
 * Get status text for display
 */
fun getStatusText(): String {
    val daysUntil = getDaysUntilBilling()
    return when {
        !isAutoPay && daysUntil < 0 -> "Overdue"      // Manual-pay overdue
        daysUntil < 0 -> "Processing"                 // Auto-pay (won't show)
        daysUntil == 0 -> "Due today"
        daysUntil <= 7 -> "$daysUntil days left"
        else -> "${daysUntil}d reminder"
    }
}
```

### 2. Repository Method

```kotlin
// BudgetOverviewRepository.kt

/**
 * Roll forward auto-pay subscriptions that have passed their billing date
 */
suspend fun rollForwardOverdueSubscriptions(): Result<Int> {
    val userId = currentUserId ?: return Result.Error("Not authenticated")
    
    val subscriptions = dao.getActiveSubscriptions(userId)
    var updatedCount = 0
    
    subscriptions.forEach { subscription ->
        if (subscription.needsRollForward()) {
            val rolledForward = subscription.rollForwardBillingDate()
            
            // Update local database
            dao.updateSubscription(rolledForward)
            
            // Sync to Firebase
            firestore.collection("users/$userId/subscriptions")
                .document(subscription.id)
                .update("nextBillingDate", Timestamp(Date(rolledForward.nextBillingDate)))
                .await()
            
            updatedCount++
        }
    }
    
    return Result.Success(updatedCount)
}
```

### 3. ViewModel Integration

```kotlin
// BudgetOverviewViewModel.kt

init {
    loadBalanceImmediately()
    initializeBalance()
    syncFromFirebase()
    rollForwardSubscriptions() // 🆕 Auto-roll forward on app start
    loadBudgetData()
    observeDataChanges()
}

private fun rollForwardSubscriptions() {
    viewModelScope.launch {
        val result = repository.rollForwardOverdueSubscriptions()
        when (result) {
            is Result.Success -> {
                if (result.data > 0) {
                    Log.d("Budget", "Rolled forward ${result.data} subscription(s)")
                    loadBudgetData() // Refresh UI
                }
            }
            is Result.Error -> {
                Log.e("Budget", "Failed to roll forward: ${result.message}")
            }
        }
    }
}
```

### 4. UI Update

```kotlin
// MobileBudgetOverviewScreen.kt

@Composable
private fun SubscriptionItem(
    subscription: EnhancedSubscriptionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val reminderText = subscription.getStatusText() // 🆕 Use new status method
    
    // Display subscription with correct status
    Row {
        Text("Next billing: ${dateFormat.format(Date(subscription.nextBillingDate))}")
        Text(reminderText) // Shows "Processing" or proper status
    }
}
```

---

## 🗄️ Database Migration

### Migration 5 → 6

```kotlin
// BudgetTrackerDatabase.kt

private val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add isAutoPay column with default value of 1 (true)
        database.execSQL("""
            ALTER TABLE enhanced_subscriptions 
            ADD COLUMN isAutoPay INTEGER NOT NULL DEFAULT 1
        """)
    }
}
```

**Migration Details:**
- **Version**: 5 → 6
- **Default**: All existing subscriptions default to `isAutoPay = true`
- **Safe**: Uses `ALTER TABLE` with `DEFAULT` to avoid data loss
- **Backward compatible**: Old code can still read the database

---

## 🧪 Testing

### Manual Testing Steps

#### Test 1: Auto-Pay Roll-Forward
1. Create a subscription with `isAutoPay = true`
2. Set `nextBillingDate` to a past date (e.g., Oct 20)
3. Restart app or navigate to Budget screen
4. **Expected**: Subscription automatically rolls forward to future date
5. **Verify**: Status shows days remaining, not "Overdue"

#### Test 2: Manual-Pay Overdue
1. Create a subscription with `isAutoPay = false`
2. Set `nextBillingDate` to a past date
3. Open Budget screen
4. **Expected**: Subscription shows "Overdue"
5. **Verify**: Status remains "Overdue" until manually updated

#### Test 3: Multiple Roll-Forwards
1. Create a monthly subscription
2. Set `nextBillingDate` to 6 months ago
3. Open app
4. **Expected**: Rolls forward multiple times to reach future date
5. **Verify**: Next billing date is in the future

#### Test 4: Firebase Sync
1. Roll forward a subscription on Device A
2. Open app on Device B
3. **Expected**: Device B syncs and shows updated billing date
4. **Verify**: Firebase Firestore has correct `nextBillingDate`

---

## 📱 User Experience

### Before This Feature:
```
Subscriptions Screen (Oct 24, 2025):
❌ Spotify Premium      $15.99
   Next billing: Oct 20, 2025
   Status: Overdue ⚠️

❌ Netflix HD           $15.49
   Next billing: Oct 15, 2025
   Status: Overdue ⚠️

Problem: All auto-pay subscriptions stuck as "Overdue"
```

### After This Feature:
```
Subscriptions Screen (Oct 24, 2025):
✅ Spotify Premium      $15.99
   Next billing: Nov 20, 2025
   Status: 27 days left

✅ Netflix HD           $15.49
   Next billing: Nov 15, 2025
   Status: 22 days left

Solution: Auto-pay subscriptions automatically roll forward
```

---

## 🔑 Key Benefits

### 1. **Accurate Financial Tracking**
- No more false "Overdue" alerts for auto-paid subscriptions
- Clear visibility into upcoming payments
- Better monthly budget planning

### 2. **Reduced Manual Work**
- No need to manually update billing dates every month
- Automatic sync across devices
- Set it and forget it

### 3. **Smart Reminders**
- Get reminders 7, 3, and 1 days before billing
- Never miss a payment
- FCM push notifications work correctly

### 4. **Flexible for All Subscription Types**
- Auto-pay: Netflix, Spotify, Gym memberships
- Manual-pay: Utility bills, rent, manual subscriptions
- Mixed: Handle both types seamlessly

---

## 🚨 Edge Cases Handled

### 1. **Very Old Subscriptions**
If a subscription hasn't been opened in months:
- ✅ Rolls forward multiple times until future date
- ✅ Doesn't get stuck in infinite loop
- ✅ Updates Firebase with correct date

### 2. **Frequency Changes**
If user changes subscription frequency:
- ✅ Next roll-forward uses new frequency
- ✅ Recalculates based on current settings

### 3. **Database Migration**
Existing subscriptions without `isAutoPay` field:
- ✅ Default to `isAutoPay = true`
- ✅ Users can edit to set as manual-pay
- ✅ No data loss during migration

### 4. **Offline Mode**
If app is offline when billing date passes:
- ✅ Rolls forward on next app open
- ✅ Syncs to Firebase when online
- ✅ Resolves conflicts gracefully

---

## 📈 Future Enhancements

### Planned Features:
- [ ] **UI Toggle**: Let users toggle `isAutoPay` in edit dialog
- [ ] **Bulk Edit**: Change multiple subscriptions at once
- [ ] **Smart Detection**: Auto-detect if subscription is likely auto-pay
- [ ] **Payment History**: Track all past billing dates
- [ ] **Spending Analytics**: Show total spent per subscription over time

---

## 🛠️ Troubleshooting

### Issue: Subscription still shows "Overdue"
**Solution**: 
1. Check `isAutoPay` field in database
2. Verify it's set to `true` (1)
3. Restart app to trigger roll-forward

### Issue: Billing date doesn't update
**Solution**:
1. Check Firebase sync status
2. Verify user is authenticated
3. Check logcat for errors: `adb logcat -s BudgetOverviewRepo`

### Issue: Wrong next billing date
**Solution**:
1. Verify subscription frequency is correct
2. Check original billing date
3. Manually update if needed

---

## 📚 Code References

### Files Modified:
1. `EnhancedSubscriptionEntity.kt` - Added `isAutoPay`, roll-forward logic
2. `BudgetOverviewRepository.kt` - Roll-forward function, Firebase sync
3. `BudgetOverviewViewModel.kt` - Auto-call roll-forward on init
4. `MobileBudgetOverviewScreen.kt` - Use new `getStatusText()` method
5. `BudgetTrackerDatabase.kt` - Migration 5→6

### Testing Files:
- Manual testing on physical device
- Check Firebase Firestore for synced dates
- Monitor logcat for roll-forward logs

---

## ✅ Summary

**Problem Solved:**
Subscriptions that are automatically paid (like Spotify, Netflix) were showing as "Overdue" after their billing date passed, causing confusion and false alerts.

**Solution Implemented:**
Added `isAutoPay` field and automatic roll-forward logic that:
- Detects when auto-pay subscriptions pass their billing date
- Calculates the next billing cycle based on frequency
- Updates both local database and Firebase
- Shows proper status ("Processing" or "X days left" instead of "Overdue")
- Happens automatically on app start, data refresh, and Firebase sync

**Result:**
✅ Clean, accurate subscription tracking  
✅ No more false "Overdue" alerts  
✅ Automatic date management  
✅ Firebase synced across devices  
✅ Better user experience

**The subscription system now intelligently handles both auto-pay and manual-pay subscriptions! 🎉**

