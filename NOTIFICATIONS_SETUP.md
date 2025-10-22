# 🔔 Push Notifications Setup Guide

## ✅ **What's Already Done**

All the code is implemented! Here's what's ready:

### Android App
- ✅ Firebase Cloud Messaging (FCM) SDK integrated
- ✅ `BudgetTrackerMessagingService` created (handles incoming notifications)
- ✅ Notification icon and channels configured
- ✅ Permission request in `MainActivity` (automatic on app start)
- ✅ FCM token saved to Firestore automatically
- ✅ App compiled successfully

### Cloud Functions
- ✅ `sendDailyReminders` - Scheduled function (runs at 9 AM daily)
- ✅ `sendTestNotification` - Test notifications from app
- ✅ `sendExpenseReminder` - Manual reminders
- ✅ All dependencies installed
- ✅ Firebase configuration updated

---

## 🚀 **What You Need to Do**

### Step 1: Upgrade to Blaze Plan (Required)

Cloud Functions require the **Blaze (pay-as-you-go) plan**. Don't worry - it's **FREE** for your usage!

**Visit:**
```
https://console.firebase.google.com/project/budget-tracker-app-oliver/usage/details
```

**Click:** "Upgrade Project" → "Select Blaze Plan"

**Why it's FREE:**
- 2 million function calls/month FREE
- You'll use ~30 calls/month (1 per day)
- Estimated cost: **$0.00/month**

---

### Step 2: Deploy Cloud Functions

After upgrading to Blaze plan:

```bash
cd /home/oliver/BudgetTrackerApp
firebase deploy --only functions
```

You'll see:
```
✔  functions[sendDailyReminders]: Successful create operation
✔  functions[sendTestNotification]: Successful create operation
✔  functions[sendExpenseReminder]: Successful create operation

✔  Deploy complete!
```

---

### Step 3: Deploy App to Your Phone

```bash
# Connect your phone via USB
adb devices

# Install app
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### Step 4: Grant Notification Permission

1. Open the app
2. You'll see a permission dialog: **"Allow Budget Tracker to send notifications?"**
3. Tap **"Allow"**

Your FCM token will be saved automatically to Firestore!

---

### Step 5: Test Notifications

You can test in 3 ways:

#### Option A: From Firebase Console
1. Go to: [Firebase Console → Cloud Messaging](https://console.firebase.google.com/project/budget-tracker-app-oliver/notification)
2. Click "Send your first message"
3. Enter:
   - Title: "Test Notification"
   - Body: "Budget Tracker notifications working!"
   - Select your app
4. Click "Review" → "Publish"

#### Option B: Test Function (From App - Coming Soon)
```kotlin
// Add this to your settings screen:
Firebase.functions
    .getHttpsCallable("sendTestNotification")
    .call()
    .addOnSuccessListener {
        Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
    }
```

#### Option C: Manual Trigger (Terminal)
```bash
firebase functions:shell
# Then type:
sendDailyReminders()
```

---

## 📱 **How It Works**

### Daily Reminders (Automatic)
**Every day at 9:00 AM**, Firebase Cloud Functions will:
1. Check your Firestore for unpaid essential expenses
2. Check for subscriptions renewing today or in 3 days
3. Send push notifications to your phone

### Example Notifications

**Essential Expense Due:**
```
🏠 Rent IVY is due today!
$1,800.00 - Don't forget to pay
```

**Subscription Renewal (3 days):**
```
🎵 Spotify Premium renews in 3 days
$11.99 - Monthly
```

**Subscription Renewal (Today):**
```
📱 Phone Plan renews today
$150.00 - Semi-annual
```

---

## 🔧 **Customization**

### Change Reminder Time
Edit `functions/index.js`:
```javascript
.schedule('0 9 * * *') // Change to your preferred time
```

**Examples:**
- `0 8 * * *` - 8:00 AM
- `0 9,18 * * *` - 9:00 AM and 6:00 PM
- `0 7 * * 1-5` - 7:00 AM on weekdays only

Then redeploy:
```bash
firebase deploy --only functions
```

### Change Timezone
Edit `functions/index.js`:
```javascript
.timeZone('America/New_York') // Your timezone
```

**Common timezones:**
- `America/New_York` (Eastern)
- `America/Chicago` (Central)
- `America/Denver` (Mountain)
- `America/Los_Angeles` (Pacific)
- `Europe/Berlin` (Germany)

---

## 📊 **Verify Setup**

### Check if FCM Token is Saved
1. Open Firebase Console
2. Go to Firestore
3. Navigate to `users/{your-user-id}`
4. You should see field: `fcmToken: "dXMp..." `

### Check Function Deployment
```bash
firebase functions:list
```

You should see:
```
sendDailyReminders (scheduled)
sendTestNotification (callable)
sendExpenseReminder (callable)
```

### View Function Logs
```bash
firebase functions:log
```

---

## 🐛 **Troubleshooting**

### "Notification permission denied"
**Solution:** 
1. Open phone Settings
2. Apps → Budget Tracker → Notifications
3. Enable "Allow notifications"
4. Reopen app

### "FCM token not saved"
**Solution:**
1. Check Firestore security rules allow writes to `users` collection
2. Verify user is authenticated
3. Check logs: `adb logcat -s MainActivity`

### "Functions not deploying"
**Solution:**
1. Verify Blaze plan is active
2. Check Firebase project ID: `firebase use budget-tracker-app-oliver`
3. Ensure you're in the project root directory

### "Notifications not received"
**Check:**
1. ✅ Notification permission granted
2. ✅ FCM token saved to Firestore
3. ✅ Functions deployed successfully
4. ✅ Phone is online
5. ✅ App is installed (doesn't need to be open)

---

## 💰 **Cost Breakdown**

**Blaze Plan (Pay-as-you-go)**

**What's FREE:**
- ✅ 2 million function invocations/month
- ✅ 400,000 GB-seconds compute time/month
- ✅ 5 GB network egress/month
- ✅ Unlimited FCM notifications

**Your Usage:**
- 📅 `sendDailyReminders`: 1 call/day = 30/month
- 📱 Sends ~3-5 notifications per day
- 💻 Runs for ~2 seconds per day

**Estimated Monthly Cost: $0.00** (well within free tier!)

**Only charged if you exceed free tier.**

---

## 🎯 **Next Steps**

1. ✅ Code is ready (all done!)
2. ⏳ **Upgrade to Blaze plan** (required)
3. ⏳ **Deploy functions** (`firebase deploy --only functions`)
4. ⏳ **Install app on phone** (`adb install -r ...`)
5. ⏳ **Grant notification permission**
6. ⏳ **Test notification**

---

## 📚 **Resources**

- [Firebase Cloud Functions Docs](https://firebase.google.com/docs/functions)
- [Firebase Cloud Messaging Docs](https://firebase.google.com/docs/cloud-messaging)
- [Cron Schedule Syntax](https://crontab.guru/)
- [Firebase Pricing Calculator](https://firebase.google.com/pricing)

---

## 🎉 **You're Almost There!**

All the code is implemented. Just:
1. Upgrade to Blaze plan (free for your usage)
2. Deploy functions
3. Install app
4. Wait for 9 AM tomorrow for automatic reminders!

**Questions? Check `functions/README.md` for detailed documentation.**

