package com.budgettracker.core.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.budgettracker.MainActivity
import com.budgettracker.R
import com.budgettracker.core.data.repository.BudgetOverviewRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Enhanced Firebase Cloud Messaging Service for Budget Reminders
 * Handles FCM token registration and notification display
 */
@AndroidEntryPoint
class EnhancedBudgetMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var repository: BudgetOverviewRepository
    
    companion object {
        const val CHANNEL_ID = "budget_reminders"
        const val CHANNEL_NAME = "Budget Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for bill payments and subscriptions"
        
        // Notification IDs
        const val SUBSCRIPTION_NOTIFICATION_ID = 1001
        const val ESSENTIAL_NOTIFICATION_ID = 1002
        const val PAYCHECK_NOTIFICATION_ID = 1003
        const val CUSTOM_NOTIFICATION_ID = 1004
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        
        // Register token with repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = Settings.Secure.getString(
                    contentResolver,
                    Settings.Secure.ANDROID_ID
                )
                
                repository.registerFcmToken(
                    deviceId = deviceId,
                    token = token,
                    appVersion = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
                )
            } catch (e: Exception) {
                // Log error but don't crash
            }
        }
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Extract notification data
        val title = remoteMessage.notification?.title ?: "Budget Reminder"
        val body = remoteMessage.notification?.body ?: ""
        val data = remoteMessage.data
        
        // Determine notification type and show appropriate notification
        val type = data["type"] ?: "CUSTOM"
        val targetId = data["targetId"] ?: ""
        val amount = data["amount"] ?: "0.00"
        
        when (type) {
            "SUBSCRIPTION" -> showSubscriptionNotification(title, body, targetId, amount)
            "ESSENTIAL" -> showEssentialNotification(title, body, targetId, amount)
            "PAYCHECK" -> showPaycheckNotification(title, body, targetId, amount)
            else -> showCustomNotification(title, body)
        }
        
        // Mark reminder as sent in database
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminderId = data["reminderId"]
                if (reminderId != null) {
                    // Update reminder status in local database
                    // This would be implemented with proper repository method
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    private fun showSubscriptionNotification(title: String, body: String, targetId: String, amount: String) {
        val intent = createMainActivityIntent().apply {
            putExtra("navigate_to", "subscriptions")
            putExtra("target_id", targetId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, SUBSCRIPTION_NOTIFICATION_ID, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_payment,
                "Mark as Paid",
                createMarkPaidIntent(targetId, "SUBSCRIPTION", amount)
            )
            .addAction(
                R.drawable.ic_snooze,
                "Snooze 1 Day",
                createSnoozeIntent(targetId, "SUBSCRIPTION", 1)
            )
            .build()
        
        NotificationManagerCompat.from(this).notify(SUBSCRIPTION_NOTIFICATION_ID, notification)
    }
    
    private fun showEssentialNotification(title: String, body: String, targetId: String, amount: String) {
        val intent = createMainActivityIntent().apply {
            putExtra("navigate_to", "budget_overview")
            putExtra("target_id", targetId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, ESSENTIAL_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_payment,
                "Mark as Paid",
                createMarkPaidIntent(targetId, "ESSENTIAL", amount)
            )
            .addAction(
                R.drawable.ic_snooze,
                "Remind Later",
                createSnoozeIntent(targetId, "ESSENTIAL", 3)
            )
            .build()
        
        NotificationManagerCompat.from(this).notify(ESSENTIAL_NOTIFICATION_ID, notification)
    }
    
    private fun showPaycheckNotification(title: String, body: String, targetId: String, amount: String) {
        val intent = createMainActivityIntent().apply {
            putExtra("navigate_to", "budget_overview")
            putExtra("target_id", targetId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, PAYCHECK_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(this).notify(PAYCHECK_NOTIFICATION_ID, notification)
    }
    
    private fun showCustomNotification(title: String, body: String) {
        val intent = createMainActivityIntent()
        
        val pendingIntent = PendingIntent.getActivity(
            this, CUSTOM_NOTIFICATION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        NotificationManagerCompat.from(this).notify(CUSTOM_NOTIFICATION_ID, notification)
    }
    
    private fun createMainActivityIntent(): Intent {
        return Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
    
    private fun createMarkPaidIntent(targetId: String, type: String, amount: String): PendingIntent {
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "MARK_AS_PAID"
            putExtra("target_id", targetId)
            putExtra("type", type)
            putExtra("amount", amount)
        }
        
        return PendingIntent.getBroadcast(
            this, targetId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createSnoozeIntent(targetId: String, type: String, days: Int): PendingIntent {
        val intent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE_REMINDER"
            putExtra("target_id", targetId)
            putExtra("type", type)
            putExtra("snooze_days", days)
        }
        
        return PendingIntent.getBroadcast(
            this, (targetId + "snooze").hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
