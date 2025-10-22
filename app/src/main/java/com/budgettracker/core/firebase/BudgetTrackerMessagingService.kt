package com.budgettracker.core.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.budgettracker.MainActivity
import com.budgettracker.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging Service
 * Handles push notifications for bill reminders and subscription renewals
 */
class BudgetTrackerMessagingService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "FCM_Service"
        private const val CHANNEL_ID = "budget_reminders"
        private const val CHANNEL_NAME = "Budget Reminders"
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        // Extract notification data
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Budget Tracker"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You have a reminder"
        val type = remoteMessage.data["type"] // "rent", "utilities", "subscription", etc.
        val amount = remoteMessage.data["amount"]
        val itemId = remoteMessage.data["itemId"]
        
        Log.d(TAG, "Notification: $title - $body (Type: $type, Amount: $amount)")
        
        // Show notification
        showNotification(title, body, type, amount, itemId)
    }
    
    private fun showNotification(
        title: String,
        body: String,
        type: String?,
        amount: String?,
        itemId: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(CHANNEL_ID)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for bills, subscriptions, and essential expenses"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500) // Short buzz pattern
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            }
        }
        
        // Intent to open app when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add extras to navigate to specific screen based on type
            putExtra("notification_type", type)
            putExtra("item_id", itemId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(), // Unique request code
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
            "groceries" -> "🛒"
            "transportation" -> "🚗"
            else -> "💰"
        }
        
        // Format notification text with amount if available
        val formattedBody = if (amount != null) {
            "$body - $$amount"
        } else {
            body
        }
        
        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Default notification icon
            .setContentTitle("$emoji $title")
            .setContentText(formattedBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(formattedBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true) // Dismiss when tapped
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibrate pattern
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            .build()
        
        // Show notification with unique ID
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "Notification displayed: ID=$notificationId")
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        
        // Save token to Firestore so Cloud Functions can send notifications
        saveTokenToFirestore(token)
    }
    
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not authenticated, cannot save FCM token")
            return
        }
        
        val db = FirebaseFirestore.getInstance()
        
        // Save FCM token to user document
        db.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token saved to Firestore for user: $userId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to save FCM token to Firestore", e)
                
                // If document doesn't exist, create it
                db.collection("users")
                    .document(userId)
                    .set(mapOf("fcmToken" to token))
                    .addOnSuccessListener {
                        Log.d(TAG, "FCM token saved to new user document")
                    }
                    .addOnFailureListener { e2 ->
                        Log.e(TAG, "Failed to create user document with FCM token", e2)
                    }
            }
    }
    
    /**
     * Request and save FCM token (call this from MainActivity on first launch)
     */
    fun requestFCMToken() {
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token retrieved: $token")
                saveTokenToFirestore(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get FCM token", e)
            }
    }
}

