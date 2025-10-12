package com.budgettracker.core.data.repository

import com.budgettracker.core.domain.model.*
import com.budgettracker.core.domain.repository.BillReminderRepository
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of BillReminderRepository for build purposes
 */
@Singleton
class BillReminderRepositoryImpl @Inject constructor() : BillReminderRepository {
    
    override fun getAllReminders(userId: String): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getReminderById(id: String): BillReminder? {
        return null
    }
    
    override fun getReminderByIdFlow(id: String): Flow<BillReminder?> {
        return flowOf(null)
    }
    
    override fun getRemindersByStatus(userId: String, status: ReminderStatus): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override fun getRemindersInDateRange(userId: String, startDate: Date, endDate: Date): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override fun getRemindersByCategory(userId: String, category: String): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override fun getOverdueReminders(userId: String): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override fun getUpcomingReminders(userId: String): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getRemindersToSend(): Result<List<BillReminder>> {
        return Result.Success(emptyList())
    }
    
    override fun getRecurringReminders(userId: String): Flow<List<BillReminder>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getTotalUpcomingAmount(userId: String, startDate: Date, endDate: Date): Result<Double> {
        return Result.Success(0.0)
    }
    
    override suspend fun getOverdueCount(userId: String): Flow<Int> {
        return flowOf(0)
    }
    
    override suspend fun createReminder(reminder: BillReminder): Result<String> {
        return Result.Success(reminder.id)
    }
    
    override suspend fun updateReminder(reminder: BillReminder): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deleteReminder(id: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun updateReminderStatus(id: String, status: ReminderStatus): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun updateFcmMessageId(id: String, fcmMessageId: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deleteAllUserReminders(userId: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deletePaidRemindersOlderThan(cutoffDate: Date): Result<Unit> {
        return Result.Success(Unit)
    }
}
