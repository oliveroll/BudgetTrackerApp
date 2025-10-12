package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.BillReminderEntity
import com.budgettracker.core.domain.model.ReminderStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Bill Reminder operations
 */
@Dao
interface BillReminderDao {
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId ORDER BY dueDate ASC")
    fun getAllReminders(userId: String): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    suspend fun getReminderById(id: String): BillReminderEntity?
    
    @Query("SELECT * FROM bill_reminders WHERE id = :id")
    fun getReminderByIdFlow(id: String): Flow<BillReminderEntity?>
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND status = :status ORDER BY dueDate ASC")
    fun getRemindersByStatus(userId: String, status: ReminderStatus): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND dueDate BETWEEN :startDate AND :endDate ORDER BY dueDate ASC")
    fun getRemindersInDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND category = :category ORDER BY dueDate ASC")
    fun getRemindersByCategory(userId: String, category: String): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND dueDate < :currentDate AND status != 'PAID'")
    fun getOverdueReminders(userId: String, currentDate: Long): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE userId = :userId AND dueDate BETWEEN :startDate AND :endDate AND status = 'UPCOMING'")
    fun getUpcomingReminders(userId: String, startDate: Long, endDate: Long): Flow<List<BillReminderEntity>>
    
    @Query("SELECT * FROM bill_reminders WHERE reminderDate <= :currentTime AND status = 'UPCOMING'")
    suspend fun getRemindersToSend(currentTime: Long): List<BillReminderEntity>
    
    @Query("SELECT * FROM bill_reminders WHERE isRecurring = 1 AND userId = :userId")
    fun getRecurringReminders(userId: String): Flow<List<BillReminderEntity>>
    
    @Query("SELECT SUM(amount) FROM bill_reminders WHERE userId = :userId AND status != 'PAID' AND dueDate BETWEEN :startDate AND :endDate")
    suspend fun getTotalUpcomingAmount(userId: String, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT COUNT(*) FROM bill_reminders WHERE userId = :userId AND status = 'OVERDUE'")
    suspend fun getOverdueCount(userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: BillReminderEntity)
    
    @Update
    suspend fun updateReminder(reminder: BillReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: BillReminderEntity)
    
    @Query("UPDATE bill_reminders SET status = :status, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateReminderStatus(id: String, status: ReminderStatus, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE bill_reminders SET fcmMessageId = :fcmMessageId WHERE id = :id")
    suspend fun updateFcmMessageId(id: String, fcmMessageId: String)
    
    @Query("DELETE FROM bill_reminders WHERE userId = :userId")
    suspend fun deleteAllUserReminders(userId: String)
    
    @Query("DELETE FROM bill_reminders WHERE status = 'PAID' AND updatedAt < :cutoffDate")
    suspend fun deletePaidRemindersOlderThan(cutoffDate: Long)
}



