package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.Subscription
import com.budgettracker.core.domain.model.SubscriptionFrequency
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for Subscription operations
 */
interface SubscriptionRepository {
    
    // Read operations
    fun getActiveSubscriptions(userId: String): Flow<List<Subscription>>
    suspend fun getSubscriptionById(id: String): Subscription?
    fun getSubscriptionByIdFlow(id: String): Flow<Subscription?>
    fun getSubscriptionsByCategory(userId: String, category: String): Flow<List<Subscription>>
    fun getSubscriptionsByFrequency(userId: String, frequency: SubscriptionFrequency): Flow<List<Subscription>>
    fun getUpcomingSubscriptions(userId: String, startDate: Date, endDate: Date): Flow<List<Subscription>>
    suspend fun getTotalMonthlyCost(userId: String): Result<Double>
    suspend fun getActiveSubscriptionCount(userId: String): Result<Int>
    fun getSubscriptionsWithReminders(userId: String): Flow<List<Subscription>>
    fun searchSubscriptions(userId: String, query: String): Flow<List<Subscription>>
    
    // Write operations
    suspend fun createSubscription(subscription: Subscription): Result<String>
    suspend fun updateSubscription(subscription: Subscription): Result<Unit>
    suspend fun deleteSubscription(id: String): Result<Unit>
    suspend fun deactivateSubscription(id: String): Result<Unit>
    suspend fun updateReminderStatus(id: String, enabled: Boolean): Result<Unit>
    suspend fun updateNextBillingDate(id: String, nextBillingDate: Date): Result<Unit>
    
    // Sync operations
    suspend fun syncSubscriptions(userId: String): Result<Unit>
    suspend fun getUnsyncedSubscriptions(): Result<List<Subscription>>
}

/**
 * Repository interface for Bill Reminder operations
 */
interface BillReminderRepository {
    
    // Read operations
    fun getAllReminders(userId: String): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    suspend fun getReminderById(id: String): com.budgettracker.core.domain.model.BillReminder?
    fun getReminderByIdFlow(id: String): Flow<com.budgettracker.core.domain.model.BillReminder?>
    fun getRemindersByStatus(userId: String, status: com.budgettracker.core.domain.model.ReminderStatus): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    fun getRemindersInDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    fun getRemindersByCategory(userId: String, category: String): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    fun getOverdueReminders(userId: String): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    fun getUpcomingReminders(userId: String): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    suspend fun getRemindersToSend(): Result<List<com.budgettracker.core.domain.model.BillReminder>>
    fun getRecurringReminders(userId: String): Flow<List<com.budgettracker.core.domain.model.BillReminder>>
    suspend fun getTotalUpcomingAmount(userId: String, startDate: Date, endDate: Date): Result<Double>
    suspend fun getOverdueCount(userId: String): Flow<Int>
    
    // Write operations
    suspend fun createReminder(reminder: com.budgettracker.core.domain.model.BillReminder): Result<String>
    suspend fun updateReminder(reminder: com.budgettracker.core.domain.model.BillReminder): Result<Unit>
    suspend fun deleteReminder(id: String): Result<Unit>
    suspend fun updateReminderStatus(id: String, status: com.budgettracker.core.domain.model.ReminderStatus): Result<Unit>
    suspend fun updateFcmMessageId(id: String, fcmMessageId: String): Result<Unit>
    
    // Cleanup operations
    suspend fun deleteAllUserReminders(userId: String): Result<Unit>
    suspend fun deletePaidRemindersOlderThan(cutoffDate: Date): Result<Unit>
}
