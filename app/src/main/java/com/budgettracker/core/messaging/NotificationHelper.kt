package com.budgettracker.core.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.budgettracker.MainActivity
import com.budgettracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for showing local notifications
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID_BILLS = "budget_bills"
        const val CHANNEL_ID_SUBSCRIPTIONS = "budget_subscriptions"
        const val CHANNEL_ID_DEBT = "budget_debt"
        const val CHANNEL_ID_PAYCHECK = "budget_paycheck"
        
        const val NOTIFICATION_ID_BILL_BASE = 1000
        const val NOTIFICATION_ID_SUBSCRIPTION_BASE = 2000
        const val NOTIFICATION_ID_DEBT = 3000
        const val NOTIFICATION_ID_PAYCHECK = 4000
    }
    
    init {
        createNotificationChannels()
    }
    
    fun showBillReminderNotification(data: Map<String, String>) {
        val title = data["title"] ?: "Bill Reminder"
        val amount = data["amount"]?.toDoubleOrNull() ?: 0.0
        val category = data["category"] ?: "Bill"
        val dueDate = data["due_date"] ?: "soon"
        val reminderId = data["reminder_id"] ?: ""
        
        val content = if (amount > 0) {
            "$category due $dueDate - $${String.format("%.2f", amount)}"
        } else {
            "$category due $dueDate"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "bill_reminder")
            putExtra("reminder_id", reminderId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            reminderId.hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_BILLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(createMarkAsPaidAction(reminderId))
            .addAction(createSnoozeAction(reminderId))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_BILL_BASE + reminderId.hashCode(), notification)
        }
    }
    
    fun showSubscriptionReminderNotification(data: Map<String, String>) {
        val subscriptionName = data["subscription_name"] ?: "Subscription"
        val amount = data["amount"]?.toDoubleOrNull() ?: 0.0
        val billingDate = data["billing_date"] ?: "soon"
        val frequency = data["frequency"] ?: "Monthly"
        val subscriptionId = data["subscription_id"] ?: ""
        
        val title = "$subscriptionName Renewal Reminder"
        val content = "Your $frequency $subscriptionName subscription ($${String.format("%.2f", amount)}) renews $billingDate"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", "subscription_reminder")
            putExtra("subscription_id", subscriptionId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            subscriptionId.hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SUBSCRIPTIONS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_SUBSCRIPTION_BASE + subscriptionId.hashCode(), notification)
        }
    }
    
    fun showDebtProgressNotification(data: Map<String, String>) {
        val remainingPayments = data["remaining_payments"]?.toIntOrNull() ?: 0
        val remainingAmount = data["remaining_amount"]?.toDoubleOrNull() ?: 0.0
        val monthlyPayment = data["monthly_payment"]?.toDoubleOrNull() ?: 900.0
        
        val title = when {
            remainingPayments <= 3 -> "🎉 Almost Debt-Free!"
            remainingPayments <= 6 -> "💪 Keep Going!"
            remainingPayments <= 12 -> "📈 Great Progress!"
            else -> "🎯 Debt Freedom Journey"
        }
        
        val content = when {
            remainingPayments <= 3 -> "Only $remainingPayments payments left! You're almost debt-free! 🎊"
            remainingPayments <= 6 -> "$remainingPayments payments remaining. You're doing amazing! 💪"
            remainingPayments <= 12 -> "€${String.format("%.0f", remainingAmount)} remaining. Keep up the great work!"
            else -> "Your €${String.format("%.0f", monthlyPayment)} monthly payment is building your financial freedom! 🚀"
        }
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_debt_progress", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_ID_DEBT, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_DEBT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_DEBT, notification)
        }
    }
    
    fun showPaycheckReminderNotification(data: Map<String, String>) {
        val amount = data["amount"]?.toDoubleOrNull() ?: 0.0
        val date = data["date"] ?: "soon"
        
        val title = "💰 Paycheck Incoming!"
        val content = "Your paycheck of $${String.format("%.2f", amount)} arrives $date. Time to budget! 📊"
        
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("show_budget_overview", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_ID_PAYCHECK, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PAYCHECK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_PAYCHECK, notification)
        }
    }
    
    private fun createMarkAsPaidAction(reminderId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "MARK_AS_PAID"
            putExtra("reminder_id", reminderId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            "mark_paid_$reminderId".hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_payment,
            "Mark as Paid",
            pendingIntent
        ).build()
    }
    
    private fun createSnoozeAction(reminderId: String): NotificationCompat.Action {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE_REMINDER"
            putExtra("reminder_id", reminderId)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context, 
            "snooze_$reminderId".hashCode(), 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Action.Builder(
            R.drawable.ic_snooze,
            "Snooze 1h",
            pendingIntent
        ).build()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_BILLS,
                    "Bill Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for upcoming bills and payments"
                    enableLights(true)
                    enableVibration(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_SUBSCRIPTIONS,
                    "Subscription Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for subscription renewals"
                    enableLights(true)
                },
                
                NotificationChannel(
                    CHANNEL_ID_DEBT,
                    "Debt Progress",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Motivational notifications for debt payoff progress"
                },
                
                NotificationChannel(
                    CHANNEL_ID_PAYCHECK,
                    "Paycheck Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications about incoming paychecks"
                    enableLights(true)
                }
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
