# Firebase Cloud Messaging (FCM) Implementation Guide

## Overview
Firebase Cloud Messaging (FCM) is fully integrated for push notifications. This guide documents the complete implementation for future reference.

---

## ✅ What's Implemented

### 1. Dependencies (build.gradle.kts)
```kotlin
// Firebase BOM
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

// Required for FCM
implementation("com.google.firebase:firebase-messaging-ktx")

// Required for calling Cloud Functions from app
implementation("com.google.firebase:firebase-functions-ktx")
```

### 2. AndroidManifest.xml Configuration
```xml
<!-- Notification Permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- FCM Service -->
<service
    android:name=".core.firebase.BudgetTrackerMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<!-- Default Notification Settings -->
<meta-data
    android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_notification" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_color"
    android:resource="@color/primary_40" />
<meta-data
    android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="budget_reminders" />
```

### 3. Notification Icon (res/drawable/ic_notification.xml)
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <path
        android:fillColor="@android:color/white"
        android:pathData="M12,22c1.1,0 2,-0.9 2,-2h-4c0,1.1 0.9,2 2,2zM18,16v-5c0,-3.07 -1.64,-5.64 -4.5,-6.32V4c0,-0.83 -0.67,-1.5 -1.5,-1.5s-1.5,0.67 -1.5,1.5v0.68C7.63,5.36 6,7.92 6,11v5l-2,2v1h16v-1l-2,-2z"/>
</vector>
```

### 4. Messaging Service (BudgetTrackerMessagingService.kt)
```kotlin
class BudgetTrackerMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "budget_reminders"
        private const val CHANNEL_NAME = "Budget Reminders"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Extract notification data
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Budget Tracker"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a reminder"
        val type = remoteMessage.data["type"]
        val amount = remoteMessage.data["amount"]
        val itemId = remoteMessage.data["itemId"]
        
        // Show notification
        showNotification(title, body, type, amount, itemId)
    }
    
    private fun showNotification(title: String, body: String, type: String?, amount: String?, itemId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for bills, subscriptions, and essential expenses"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Intent to open app
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", type)
            putExtra("item_id", itemId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Choose emoji based on type
        val emoji = when (type?.lowercase()) {
            "rent" -> "🏠"
            "utilities" -> "⚡"
            "subscription" -> "🎵"
            "phone" -> "📱"
            "insurance" -> "🛡️"
            else -> "💰"
        }
        
        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("$emoji $title")
            .setContentText(if (amount != null) "$body - $$amount" else body)
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        saveTokenToFirestore(token)
    }
    
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        
        db.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnFailureListener {
                // If document doesn't exist, create it
                db.collection("users").document(userId).set(mapOf("fcmToken" to token))
            }
    }
}
```

### 5. MainActivity - Request Permissions & Token
```kotlin
class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            requestFCMToken()
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request notification permission and FCM token
        requestNotificationPermissionAndToken()
        
        // ... rest of onCreate
    }
    
    private fun requestNotificationPermissionAndToken() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    requestFCMToken()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below don't need runtime permission
            requestFCMToken()
        }
    }
    
    private fun requestFCMToken() {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token: $token")
                
                // Save to Firestore
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token saved to Firestore for user: $userId")
                    }
                    .addOnFailureListener {
                        // Create document if it doesn't exist
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(mapOf("fcmToken" to token))
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get FCM token", e)
            }
    }
}
```

---

## 🔥 Cloud Functions (functions/index.js)

### 1. Scheduled Daily Reminders (9 AM every day)
```javascript
exports.sendDailyReminders = functions.pubsub
  .schedule('0 9 * * *')
  .timeZone('America/New_York')
  .onRun(async (context) => {
    const db = admin.firestore();
    const today = new Date();
    const todayDay = today.getDate();
    
    // Get all users
    const usersSnapshot = await db.collection('users').get();
    
    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;
      const fcmToken = userDoc.data().fcmToken;
      
      if (!fcmToken) continue;
      
      // Check Essential Expenses
      const expensesSnapshot = await db
        .collection('essentialExpenses')
        .where('userId', '==', userId)
        .where('paid', '==', false)
        .get();
      
      for (const expenseDoc of expensesSnapshot.docs) {
        const expense = expenseDoc.data();
        if (expense.dueDay === todayDay) {
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: `${expense.name} is due today!`,
              body: `$${expense.plannedAmount.toFixed(2)} - Don't forget to pay`
            },
            data: {
              type: expense.category.toLowerCase(),
              amount: expense.plannedAmount.toString(),
              itemId: expenseDoc.id,
              screen: 'budget'
            },
            android: {
              priority: 'high',
              notification: {
                channelId: 'budget_reminders',
                priority: 'high'
              }
            }
          });
        }
      }
      
      // Check Subscriptions (3 days before and on the day)
      const subscriptionsSnapshot = await db
        .collection('subscriptions')
        .where('userId', '==', userId)
        .where('active', '==', true)
        .get();
      
      for (const subDoc of subscriptionsSnapshot.docs) {
        const subscription = subDoc.data();
        const nextBilling = new Date(subscription.nextBillingDate);
        const daysUntil = Math.ceil((nextBilling - today) / (1000 * 60 * 60 * 24));
        
        if (daysUntil === 3 || daysUntil === 0) {
          const reminderText = daysUntil === 0 ? 'renews today' : 'renews in 3 days';
          
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: `${subscription.name} ${reminderText}`,
              body: `$${subscription.amount.toFixed(2)} - ${subscription.frequency || 'Monthly'}`
            },
            data: {
              type: 'subscription',
              amount: subscription.amount.toString(),
              itemId: subDoc.id,
              screen: 'budget'
            },
            android: {
              priority: 'high',
              notification: {
                channelId: 'budget_reminders'
              }
            }
          });
        }
      }
    }
  });
```

### 2. Test Notification (Callable)
```javascript
exports.sendTestNotification = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const userId = context.auth.uid;
  const userDoc = await admin.firestore().collection('users').doc(userId).get();
  const fcmToken = userDoc.data()?.fcmToken;
  
  if (!fcmToken) {
    throw new functions.https.HttpsError('not-found', 'FCM token not found');
  }
  
  await admin.messaging().send({
    token: fcmToken,
    notification: {
      title: '🎉 Test Notification',
      body: 'Budget Tracker notifications are working!'
    },
    data: {
      type: 'test',
      screen: 'dashboard'
    },
    android: {
      priority: 'high',
      notification: {
        channelId: 'budget_reminders'
      }
    }
  });
  
  return { success: true };
});
```

### 3. Manual Expense Reminder (Callable)
```javascript
exports.sendExpenseReminder = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
  }
  
  const { expenseId, expenseName, amount } = data;
  const userId = context.auth.uid;
  const userDoc = await admin.firestore().collection('users').doc(userId).get();
  const fcmToken = userDoc.data()?.fcmToken;
  
  if (!fcmToken) {
    throw new functions.https.HttpsError('not-found', 'FCM token not found');
  }
  
  await admin.messaging().send({
    token: fcmToken,
    notification: {
      title: `Reminder: ${expenseName}`,
      body: `$${parseFloat(amount).toFixed(2)} - Payment reminder`
    },
    data: {
      type: 'expense_reminder',
      amount: amount.toString(),
      itemId: expenseId,
      screen: 'budget'
    },
    android: {
      priority: 'high',
      notification: {
        channelId: 'budget_reminders'
      }
    }
  });
  
  return { success: true };
});
```

---

## 🧪 Testing Implementation

### 1. Test Button in Settings Screen
```kotlin
// Add to SettingsScreen.kt
@Composable
private fun DeveloperToolsCard(scope: kotlinx.coroutines.CoroutineScope) {
    val context = LocalContext.current
    var isTestingSending by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Developer Tools", style = MaterialTheme.typography.titleMedium)
            
            Button(
                onClick = {
                    scope.launch {
                        isTestingSending = true
                        try {
                            Firebase.functions
                                .getHttpsCallable("sendTestNotification")
                                .call()
                                .await()
                            
                            Toast.makeText(context, "✅ Test notification sent!", Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isTestingSending = false
                        }
                    }
                },
                enabled = !isTestingSending
            ) {
                if (isTestingSending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sending...")
                } else {
                    Text("🔔 Send Test Notification")
                }
            }
        }
    }
}
```

### 2. Deploy Cloud Functions
```bash
cd functions
npm install
firebase deploy --only functions
```

### 3. Test from Firebase Console
1. Firebase Console → Cloud Messaging
2. "Send your first message"
3. Use FCM token from Firestore `users/{userId}/fcmToken`

---

## 📊 Data Structure

### Firestore Schema
```javascript
users/{userId}
  - fcmToken: string
  - email: string
  - name: string
  // ... other user fields

essentialExpenses/{expenseId}
  - userId: string
  - name: string
  - plannedAmount: number
  - dueDay: number (1-31)
  - paid: boolean
  - category: string

subscriptions/{subscriptionId}
  - userId: string
  - name: string
  - amount: number
  - nextBillingDate: timestamp
  - frequency: string
  - active: boolean
```

---

## 🐛 Common Issues & Solutions

### Issue: "Unresolved reference 'functions'"
**Solution**: Add `implementation("com.google.firebase:firebase-functions-ktx")` to build.gradle.kts

### Issue: Notifications not appearing
**Solution**: 
1. Check notification permissions granted
2. Verify FCM token saved to Firestore
3. Check logcat: `adb logcat -s FCM_Service`
4. Ensure notification channel created (Android 8+)

### Issue: Cloud Function fails
**Solution**:
1. Check function logs: `firebase functions:log`
2. Verify user has valid FCM token in Firestore
3. Ensure Firebase project on Blaze (pay-as-you-go) plan

---

## 🔑 Key Points to Remember

1. **Always request notification permissions** on Android 13+ in MainActivity.onCreate()
2. **FCM token must be saved to Firestore** so Cloud Functions can send messages
3. **Create notification channel** on Android 8+ before showing notifications
4. **Use IMPORTANCE_HIGH** for notification channel to show as heads-up
5. **Cloud Functions require Blaze plan** for scheduled functions
6. **Test button is essential** for quick verification during development
7. **Handle onNewToken()** to update token if it changes

---

## 📚 Documentation Files

- `NOTIFICATION_SYSTEM.md` - Complete user and developer documentation
- `functions/README.md` - Cloud Functions setup guide
- This file - Implementation reference for AI

---

## ✅ Verification Checklist

When implementing FCM in a new project:
- [ ] Add firebase-messaging-ktx dependency
- [ ] Add firebase-functions-ktx dependency
- [ ] Create BudgetTrackerMessagingService
- [ ] Add service to AndroidManifest.xml
- [ ] Add notification permissions to manifest
- [ ] Create notification icon drawable
- [ ] Request notification permissions in MainActivity
- [ ] Request and save FCM token
- [ ] Create Cloud Functions (test, scheduled, manual)
- [ ] Deploy Cloud Functions
- [ ] Add test button in Settings
- [ ] Test notification flow end-to-end
- [ ] Document in NOTIFICATION_SYSTEM.md

---

**Last Updated**: October 24, 2025
**Status**: ✅ Fully Implemented and Tested

