# 🔔 Budget Tracker - Notification System Documentation

## Overview
Budget Tracker uses **Firebase Cloud Messaging (FCM)** to send push notifications for bill reminders, subscription renewals, and essential expense due dates. This system works **even when the app is offline** because notifications are sent from Firebase servers.

---

## ✅ What's Already Working

### 1. **Firebase Cloud Messaging (FCM) Integration**
- ✅ FCM token is automatically generated and saved to Firestore when app starts
- ✅ Token is stored in `users/{userId}/fcmToken` field
- ✅ `BudgetTrackerMessagingService` handles incoming notifications
- ✅ Notifications work in foreground AND background

### 2. **Notification Service** (`BudgetTrackerMessagingService.kt`)
- Receives push notifications from Firebase
- Displays notifications with:
  - Custom emojis based on expense type (🏠 Rent, ⚡ Utilities, 🎵 Subscriptions, etc.)
  - Amount and description
  - Deep links to open app when tapped
  - Vibration pattern
  - High priority for immediate delivery
- Handles foreground and background notifications

### 3. **Cloud Functions** (Deployed to Firebase)
Three functions running on Firebase servers:

#### a) `sendDailyReminders` (Scheduled - Runs daily at 9 AM)
- Checks all users' essential expenses and subscriptions
- Sends notifications for:
  - Bills due TODAY (based on `dueDay` field)
  - Subscriptions renewing TODAY
  - Subscriptions renewing in 3 DAYS (early warning)
- Automatically removes invalid FCM tokens

#### b) `sendTestNotification` (Callable)
- Sends a test notification to verify system works
- Called from the app's Settings → Developer Tools
- Perfect for debugging

#### c) `sendExpenseReminder` (Callable)
- Sends an immediate reminder for a specific expense
- Can be called from anywhere in the app
- Useful for "Remind Me Later" features

### 4. **Notification Permissions**
- ✅ Automatically requests `POST_NOTIFICATIONS` permission on Android 13+
- ✅ Handled in `MainActivity.onCreate()`
- ✅ User is prompted to allow notifications on first launch

### 5. **Notification Channel**
- ✅ Channel ID: `budget_reminders`
- ✅ Channel Name: "Budget Reminders"
- ✅ Importance: HIGH (appears as heads-up notification)
- ✅ Features: Vibration, lights, lock screen visibility

---

## 🧪 How to Test Notifications

### Method 1: Using the Test Button (Easiest)
1. Open Budget Tracker app
2. Tap **Settings** tab (bottom navigation)
3. Scroll to **Developer Tools** section (blue/purple card)
4. Tap **"🔔 Send Test Notification"** button
5. Wait 2-3 seconds
6. Check your notification tray for: **"🎉 Budget Tracker Test"**

### Method 2: Using Firebase Console
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select **Budget Tracker App** project
3. Navigate to **Cloud Messaging** in left sidebar
4. Click **"Send your first message"**
5. Fill in:
   - **Notification title**: "Test from Console"
   - **Notification text**: "This is a test"
6. Click **Next** → Select **"budget-tracker-app-oliver"** app
7. Click **Review** → **Publish**
8. Check your device notification tray

### Method 3: Simulate Real Scenario
1. Add an essential expense with a due date of TODAY:
   - Go to **Budget** tab
   - Add a bill with `dueDay = Today's date` (e.g., if today is Oct 24, set dueDay = 24)
   - Mark as NOT paid
2. Wait until 9:00 AM tomorrow (or manually trigger the Cloud Function)
3. You'll receive: **"🏠 Rent IVY is due today! - $750.00"**

---

## 📱 Notification Examples

### Bill Due Today:
```
🏠 Rent IVY is due today!
$750.00 - Don't forget to pay
```

### Subscription Renewal (Same Day):
```
🎵 Spotify Premium renews today
$15.99 - Monthly
```

### Subscription Renewal (3 Days Early):
```
📱 Verizon renews in 3 days
$80.00 - Monthly
```

### Test Notification:
```
🎉 Test Notification
Budget Tracker notifications are working! You will receive reminders for bills and subscriptions.
```

---

## 🔧 Technical Architecture

### Data Flow:
```
1. App Start → Request FCM token
2. FCM token saved to Firestore: users/{userId}/fcmToken
3. Cloud Functions run daily at 9 AM (Cron: 0 9 * * *)
4. Function checks:
   - essentialExpenses collection (dueDay = today)
   - subscriptions collection (nextBillingDate = today or in 3 days)
5. Function sends FCM message to user's token
6. Firebase delivers message to device (even if app is closed)
7. BudgetTrackerMessagingService receives message
8. Notification displayed in system tray
9. User taps notification → App opens to relevant screen
```

### Key Files:
- **`BudgetTrackerMessagingService.kt`**: Handles incoming FCM messages
- **`MainActivity.kt`**: Requests notification permissions and FCM token
- **`functions/index.js`**: Cloud Functions for scheduled reminders
- **`AndroidManifest.xml`**: Declares FCM service and permissions

### Firebase Collections Used:
- `users/{userId}` - Stores FCM token
- `essentialExpenses/{expenseId}` - Fixed bills with due dates
- `subscriptions/{subscriptionId}` - Recurring subscriptions

---

## 🚀 Scheduled Reminders (Automatic)

The `sendDailyReminders` Cloud Function runs **every day at 9:00 AM EST** and checks:

### Essential Expenses:
```kotlin
WHERE userId == currentUser
  AND paid == false
  AND dueDay == TODAY
```
Sends notification: **"💰 [Name] is due today! - $[Amount]"**

### Subscriptions:
```kotlin
WHERE userId == currentUser
  AND active == true
  AND (nextBillingDate == TODAY OR nextBillingDate == TODAY + 3 days)
```
Sends notification: **"🎵 [Name] renews [today/in 3 days] - $[Amount]"**

---

## 🐛 Troubleshooting

### Not Receiving Notifications?

#### Check 1: Notification Permissions
```bash
# Check if permission is granted
adb shell dumpsys package com.budgettracker | grep POST_NOTIFICATIONS
```
**Fix**: Open app → Allow notifications when prompted

#### Check 2: FCM Token Saved?
```bash
# Check Firestore for your FCM token
# Go to Firebase Console → Firestore → users → [your_userId]
# Look for "fcmToken" field
```
**Fix**: Restart app → Token is fetched and saved on `onCreate()`

#### Check 3: Cloud Functions Deployed?
```bash
cd functions
firebase functions:list
```
Expected output:
```
sendDailyReminders   | scheduled | us-central1
sendTestNotification | callable  | us-central1
sendExpenseReminder  | callable  | us-central1
```
**Fix**: `firebase deploy --only functions`

#### Check 4: App Logs
```bash
adb logcat -s FCM_Service MainActivity FirebaseMessaging
```
Look for:
- `FCM token saved to Firestore for user: [userId]`
- `Message received from: [sender]`
- `Notification displayed: ID=[notificationId]`

---

## 🎯 Adding Notification Triggers in Your Code

### Example: Remind Me Button
```kotlin
// In any screen/composable
Button(onClick = {
    scope.launch {
        try {
            Firebase.functions
                .getHttpsCallable("sendExpenseReminder")
                .call(hashMapOf(
                    "expenseId" to expense.id,
                    "expenseName" to expense.name,
                    "amount" to expense.amount
                ))
                .await()
            
            Toast.makeText(context, "Reminder sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Reminder", "Failed", e)
        }
    }
}) {
    Text("Remind Me")
}
```

### Example: Snooze Reminder
```kotlin
// Schedule a reminder for 1 hour from now
// (Would require adding a new Cloud Function with delayed execution)
```

---

## 📊 Monitoring

### View Cloud Function Logs:
```bash
firebase functions:log
```

### View FCM Delivery Stats:
1. Firebase Console → Cloud Messaging
2. See delivery reports, impressions, and errors

### Test Notification Delivery:
Use the **Developer Tools** button in Settings for instant testing!

---

## 🔒 Security

- ✅ Only authenticated users can send notifications (Cloud Functions check `context.auth`)
- ✅ FCM tokens are private and stored securely in Firestore
- ✅ Cloud Functions run on Firebase servers (no client-side secrets)
- ✅ Notification channels are configured with appropriate importance levels

---

## 💡 Future Enhancements

- [ ] Custom notification sounds per category
- [ ] Notification actions (e.g., "Mark as Paid" button)
- [ ] Quiet hours (don't send notifications between 10 PM - 7 AM)
- [ ] Notification preferences (enable/disable specific categories)
- [ ] Weekly summary notifications
- [ ] Notification snooze feature
- [ ] Notification history in app

---

## 📝 Summary

✅ **Firebase Cloud Messaging is fully integrated and working**  
✅ **Notifications work even when app is closed**  
✅ **Scheduled reminders run daily at 9 AM**  
✅ **Test button available in Settings for easy verification**  
✅ **All components tested and deployed**  

**Your notification system is production-ready! 🎉**

