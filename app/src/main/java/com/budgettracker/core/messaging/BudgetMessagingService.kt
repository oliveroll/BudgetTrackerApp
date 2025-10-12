package com.budgettracker.core.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

/**
 * Simplified messaging service for handling bill reminders
 * Firebase integration can be added later
 */
class BudgetMessagingService : Service() {
    
    companion object {
        const val CHANNEL_ID = "budget_reminders"
        const val CHANNEL_NAME = "Budget Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for bill payments and subscriptions"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}