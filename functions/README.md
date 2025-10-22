# Budget Tracker Cloud Functions

Firebase Cloud Functions for sending push notifications for bill reminders and subscription renewals.

## 🚀 **Functions**

### 1. `sendDailyReminders` (Scheduled)
- **Runs:** Every day at 9:00 AM (America/New_York timezone)
- **Purpose:** Automatically checks all users for:
  - Essential expenses due today
  - Subscriptions renewing in 3 days or today
- **Sends:** Push notifications to users' phones

### 2. `sendTestNotification` (Callable)
- **Callable from app:** Test if notifications are working
- **Purpose:** Send a test notification to verify FCM setup

### 3. `sendExpenseReminder` (Callable)
- **Callable from app:** Send immediate reminder for a specific expense
- **Purpose:** Manual reminders for bills

---

## 📋 **Prerequisites**

1. **Node.js 18** installed
2. **Firebase CLI** installed:
   ```bash
   npm install -g firebase-tools
   ```
3. **Firebase project** initialized
4. **Blaze plan** (pay-as-you-go) for Cloud Functions

---

## 🛠️ **Setup**

### 1. Install Dependencies
```bash
cd functions
npm install
```

### 2. Login to Firebase
```bash
firebase login
```

### 3. Set Your Firebase Project
```bash
firebase use budget-tracker-app-oliver
```

### 4. Configure Timezone (Optional)
Edit `functions/index.js` and change the timezone:
```javascript
.timeZone('America/New_York') // Change to your timezone
```

**Common timezones:**
- `America/New_York` (Eastern Time)
- `America/Chicago` (Central Time)
- `America/Denver` (Mountain Time)
- `America/Los_Angeles` (Pacific Time)
- `Europe/Berlin` (Germany)

### 5. Configure Reminder Time (Optional)
Edit the cron schedule in `functions/index.js`:
```javascript
.schedule('0 9 * * *') // 9 AM daily
```

**Cron examples:**
- `0 9 * * *` - 9:00 AM daily
- `0 9,18 * * *` - 9:00 AM and 6:00 PM daily
- `0 * * * *` - Every hour
- `0 8 * * 1-5` - 8:00 AM on weekdays only

---

## 🚀 **Deployment**

### Deploy All Functions
```bash
firebase deploy --only functions
```

### Deploy Specific Function
```bash
firebase deploy --only functions:sendDailyReminders
```

### View Deployment Status
```bash
firebase functions:list
```

---

## 🧪 **Testing**

### Test Locally (Emulator)
```bash
cd functions
npm run serve
```

### Test from App
Call the `sendTestNotification` function from your app:
```kotlin
val functions = Firebase.functions
functions
    .getHttpsCallable("sendTestNotification")
    .call()
    .addOnSuccessListener {
        Log.d("FCM", "Test notification sent!")
    }
```

### View Logs
```bash
firebase functions:log
```

### Trigger Manual Reminder
```bash
firebase functions:shell
# Then run:
sendDailyReminders()
```

---

## 📊 **Cost Estimate**

**Blaze Plan Pricing:**
- **Invocations:** 2 million/month FREE, then $0.40/million
- **Compute Time:** 400,000 GB-seconds/month FREE
- **Networking:** 5 GB/month FREE

**For this app:**
- `sendDailyReminders`: 1 call/day = 30 calls/month
- Sends ~3-5 notifications per user per day
- **Estimated cost:** $0.00 (within free tier)

---

## 🔧 **Troubleshooting**

### "Deployment failed: Billing account not configured"
**Solution:** Upgrade to Blaze plan in Firebase Console → Settings → Usage and Billing

### "User has no FCM token"
**Solution:** User needs to:
1. Open app
2. Grant notification permission
3. FCM token will be saved automatically

### "Notifications not received"
**Check:**
1. User granted notification permission
2. FCM token saved to Firestore (`users/{userId}/fcmToken`)
3. Cloud Function deployed successfully
4. Check logs: `firebase functions:log`
5. Phone is online

### "Invalid FCM token"
**Solution:** Function automatically removes invalid tokens. User needs to:
1. Reinstall app OR
2. Clear app data and reopen

---

## 📱 **Notification Examples**

### Essential Expense Due
```
🏠 Rent IVY is due today!
$1,800.00 - Don't forget to pay
```

### Subscription Renewal (3 days)
```
🎵 Spotify Premium renews in 3 days
$11.99 - Monthly
```

### Subscription Renewal (Today)
```
📱 Phone Plan renews today
$150.00 - Semi-annual
```

---

## 🔒 **Security**

- ✅ Functions require authentication (except scheduled)
- ✅ Users can only send notifications to themselves
- ✅ FCM tokens are private and stored securely
- ✅ Invalid tokens are automatically removed
- ✅ Firestore security rules prevent unauthorized access

---

## 📚 **Resources**

- [Firebase Cloud Functions Docs](https://firebase.google.com/docs/functions)
- [Firebase Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [Cron Schedule Syntax](https://crontab.guru/)
- [Timezone List](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)

---

## 🎯 **Next Steps**

1. Deploy functions: `firebase deploy --only functions`
2. Test notification from app
3. Wait until 9 AM next day for automatic reminders
4. Check logs to verify: `firebase functions:log`

**Notifications are ready! 🎉**

