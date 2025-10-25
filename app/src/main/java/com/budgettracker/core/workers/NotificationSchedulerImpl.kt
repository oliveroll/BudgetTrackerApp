package com.budgettracker.core.workers

import android.content.Context
import androidx.work.*
import com.budgettracker.features.settings.data.models.NotificationFrequency
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules periodic notification workers based on user settings
 */
@Singleton
class NotificationSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule all enabled notifications based on user settings
     */
    fun scheduleAll(
        lowBalanceEnabled: Boolean,
        lowBalanceFrequency: NotificationFrequency,
        goalMilestoneEnabled: Boolean,
        goalMilestoneFrequency: NotificationFrequency
    ) {
        if (lowBalanceEnabled || goalMilestoneEnabled) {
            // Use the most frequent setting to ensure all checks happen
            val frequency = listOf(lowBalanceFrequency, goalMilestoneFrequency)
                .filter { it != NotificationFrequency.NEVER }
                .minByOrNull { it.toHours() } ?: NotificationFrequency.DAILY

            schedulePeriodicWorker(frequency)
        } else {
            cancelAllNotifications()
        }
    }

    /**
     * Schedule periodic notification checks
     */
    private fun schedulePeriodicWorker(frequency: NotificationFrequency) {
        val repeatInterval = frequency.toHours()
        if (repeatInterval == 0L) {
            cancelAllNotifications()
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<SimpleNotificationWorker>(
            repeatInterval, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                15, TimeUnit.MINUTES
            )
            .addTag("budget_notifications")
            .build()

        workManager.enqueueUniquePeriodicWork(
            SimpleNotificationWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel all scheduled notifications
     */
    fun cancelAllNotifications() {
        workManager.cancelUniqueWork(SimpleNotificationWorker.WORK_NAME)
        workManager.cancelAllWorkByTag("budget_notifications")
    }

    /**
     * Trigger an immediate notification check (for testing)
     */
    fun triggerImmediateCheck() {
        val workRequest = OneTimeWorkRequestBuilder<SimpleNotificationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueue(workRequest)
    }

    private fun NotificationFrequency.toHours(): Long {
        return when (this) {
            NotificationFrequency.DAILY -> 24
            NotificationFrequency.WEEKLY -> 168 // 7 * 24
            NotificationFrequency.MONTHLY -> 720 // 30 * 24
            NotificationFrequency.NEVER -> 0
        }
    }
}

