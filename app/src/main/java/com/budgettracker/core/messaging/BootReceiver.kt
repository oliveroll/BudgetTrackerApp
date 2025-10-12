package com.budgettracker.core.messaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.budgettracker.core.domain.repository.BillReminderRepository
import com.budgettracker.core.domain.repository.SubscriptionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receiver to reschedule notifications after device reboot
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            
            // Reschedule all notifications
            CoroutineScope(Dispatchers.IO).launch {
                rescheduleNotifications()
            }
        }
    }
    
    private suspend fun rescheduleNotifications() {
        try {
            // Simple implementation without repository injection for now
            // This would be implemented with proper repository pattern
            // to reschedule all active notifications after reboot
        } catch (e: Exception) {
            // Handle error silently
        }
    }
}
