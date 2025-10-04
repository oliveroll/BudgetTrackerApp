package com.budgettracker.feature.loan.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.budgettracker.feature.loan.notifications.LoanNotifications

class LoanReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    private val loanNotifications = LoanNotifications(context)
    
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
