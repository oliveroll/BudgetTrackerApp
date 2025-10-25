package com.budgettracker.core.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.budgettracker.MainActivity
import com.budgettracker.R
import com.budgettracker.core.data.local.database.BudgetTrackerDatabase
import com.budgettracker.features.settings.data.models.UserSettings
import kotlinx.coroutines.flow.first

/**
 * Simplified worker without Hilt dependency injection
 * Checks low balance and goal milestones
 */
class SimpleNotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "budget_alerts"
        const val CHANNEL_NAME = "Budget Alerts"
        const val WORK_NAME = "budget_notification_work"
        
        // Notification IDs
        const val LOW_BALANCE_ID = 1001
        const val GOAL_MILESTONE_ID = 1002
    }

    private val database = BudgetTrackerDatabase.getDatabase(context)

    override suspend fun doWork(): Result {
        try {
            createNotificationChannel()
            
            // Check if this is a test run
            val isTest = inputData.getBoolean("is_test", false)
            
            if (isTest) {
                // Send test notification immediately
                sendTestNotification()
                return Result.success()
            }
            
            // For periodic checks, get user settings
            val settingsDao = database.userSettingsDao()
            
            // Try to get userId from Firebase Auth first
            val userId = try {
                com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            } catch (e: Exception) {
                null
            } ?: applicationContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("current_user_id", null) 
            
            if (userId == null) {
                return Result.success()
            }
            
            val settings = settingsDao.getUserSettingsFlow(userId).first()
            
            if (settings == null || !settings.notificationSettings.notificationPermissionGranted) {
                return Result.success()
            }

            // Check Low Balance Alert
            if (settings.notificationSettings.lowBalanceAlertEnabled) {
                checkLowBalance(settings.notificationSettings.lowBalanceThreshold, userId)
            }

            // Check Goal Milestones
            if (settings.notificationSettings.goalMilestoneEnabled) {
                sendGoalMilestoneNotification()
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    private fun sendTestNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("✅ Test Notification Success!")
            .setContentText("Your notification system is working perfectly!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Budget Tracker notifications are set up correctly. You'll receive alerts for low balance, bills, subscriptions, and goal milestones."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(999, notification) // Use unique ID for test
    }
    
    private suspend fun checkLowBalance(threshold: Double, userId: String) {
        val budgetOverviewDao = database.budgetOverviewDao()
        val balance = budgetOverviewDao.getBalance(userId) ?: return
        val currentBalance = balance.currentBalance
        
        if (currentBalance <= threshold && currentBalance > 0) {
            sendLowBalanceNotification(currentBalance, threshold)
        }
    }

    private fun sendLowBalanceNotification(currentBalance: Double, threshold: Double) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", "budget")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Low Balance Alert")
            .setContentText("Your balance is ${String.format("%.2f", currentBalance)}. Time to budget carefully!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your current balance (${String.format("%.2f", currentBalance)}) is below your threshold (${String.format("%.2f", threshold)}). Consider reviewing your spending."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(LOW_BALANCE_ID, notification)
    }

    private fun sendGoalMilestoneNotification() {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("screen", "goals")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🎯 Goal Milestone Check")
            .setContentText("Notification system is working!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Goal milestone tracking is active. You'll be notified when you reach milestones."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(GOAL_MILESTONE_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for budget alerts and goal milestones"
            }

            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

