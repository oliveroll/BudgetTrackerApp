package com.budgettracker.feature.loan.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.budgettracker.feature.loan.notifications.LoanNotifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LoanReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val loanNotifications: LoanNotifications
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val loanId = inputData.getString("loanId") ?: return Result.failure()
            val amount = inputData.getDouble("amount", 900.0)
            val currency = inputData.getString("currency") ?: "EUR"
            
            loanNotifications.showPaymentReminder(loanId, amount, currency)
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
