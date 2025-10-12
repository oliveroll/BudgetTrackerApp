package com.budgettracker.core.messaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.budgettracker.core.data.repository.BudgetOverviewRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Enhanced Notification Action Receiver for handling notification actions
 * Integrates with BudgetOverviewRepository for data updates
 */
@AndroidEntryPoint
class EnhancedNotificationActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var repository: BudgetOverviewRepository
    
    override fun onReceive(context: Context, intent: Intent) {
        val targetId = intent.getStringExtra("target_id") ?: return
        val type = intent.getStringExtra("type") ?: return
        
        when (intent.action) {
            "MARK_AS_PAID" -> {
                val amount = intent.getStringExtra("amount")?.toDoubleOrNull()
                markItemAsPaid(context, targetId, type, amount)
            }
            "SNOOZE_REMINDER" -> {
                val snoozeDays = intent.getIntExtra("snooze_days", 1)
                snoozeReminder(context, targetId, type, snoozeDays)
            }
        }
        
        // Dismiss the notification
        val notificationId = when (type) {
            "SUBSCRIPTION" -> EnhancedBudgetMessagingService.SUBSCRIPTION_NOTIFICATION_ID
            "ESSENTIAL" -> EnhancedBudgetMessagingService.ESSENTIAL_NOTIFICATION_ID
            "PAYCHECK" -> EnhancedBudgetMessagingService.PAYCHECK_NOTIFICATION_ID
            else -> EnhancedBudgetMessagingService.CUSTOM_NOTIFICATION_ID
        }
        NotificationManagerCompat.from(context).cancel(notificationId)
    }
    
    private fun markItemAsPaid(context: Context, targetId: String, type: String, amount: Double?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (type) {
                    "ESSENTIAL" -> {
                        repository.markEssentialPaid(targetId, amount)
                        showSuccessToast(context, "Essential expense marked as paid")
                    }
                    "SUBSCRIPTION" -> {
                        // For subscriptions, we might want to update the next billing date
                        // This would require additional repository methods
                        showSuccessToast(context, "Subscription payment recorded")
                    }
                    "PAYCHECK" -> {
                        repository.markPaycheckDeposited(targetId)
                        showSuccessToast(context, "Paycheck marked as deposited")
                    }
                }
            } catch (e: Exception) {
                showErrorToast(context, "Failed to update: ${e.message}")
            }
        }
    }
    
    private fun snoozeReminder(context: Context, targetId: String, type: String, days: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create new reminder for future date
                val futureDate = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, days)
                }.timeInMillis
                
                // This would require additional repository methods to reschedule reminders
                // For now, just show confirmation
                showSuccessToast(context, "Reminder snoozed for $days day(s)")
                
            } catch (e: Exception) {
                showErrorToast(context, "Failed to snooze reminder: ${e.message}")
            }
        }
    }
    
    private fun showSuccessToast(context: Context, message: String) {
        // Implementation would show a toast or send a local broadcast
        // to update the UI if the app is open
    }
    
    private fun showErrorToast(context: Context, message: String) {
        // Implementation would show an error toast
    }
}
