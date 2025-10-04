package com.budgettracker.feature.loan.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.budgettracker.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanNotifications @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val NOTIFICATION_CHANNEL_BILLS = "bills_channel"
        const val NOTIFICATION_ID_LOAN_PAYMENT = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    fun showPaymentReminder(loanId: String, amount: Double, currency: String) {
        val amountUSD = amount * 1.05
        
        val intent = Intent().apply {
            putExtra("loanId", loanId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_BILLS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Loan Payment Due")
            .setContentText("$currency$amount loan payment due today (~$${"%.2f".format(amountUSD)} USD)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_payment,
                "Pay Now",
                createPaymentIntent(loanId)
            )
            .addAction(
                R.drawable.ic_snooze,
                "Snooze",
                createSnoozeIntent(loanId)
            )
            .build()
        
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID_LOAN_PAYMENT, notification)
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_BILLS,
                "Bills & Payments",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for bill payments and loan reminders"
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createPaymentIntent(loanId: String): PendingIntent {
        val intent = Intent().apply {
            putExtra("action", "pay")
            putExtra("loanId", loanId)
        }
        
        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createSnoozeIntent(loanId: String): PendingIntent {
        val intent = Intent().apply {
            putExtra("action", "snooze")
            putExtra("loanId", loanId)
        }
        
        return PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

