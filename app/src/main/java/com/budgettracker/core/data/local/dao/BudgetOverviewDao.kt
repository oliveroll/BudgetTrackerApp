package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced DAO for Budget Overview functionality
 */
@Dao
interface BudgetOverviewDao {
    
    // Balance operations
    @Query("SELECT * FROM balance WHERE userId = :userId")
    suspend fun getBalance(userId: String): BalanceEntity?
    
    @Query("SELECT * FROM balance WHERE userId = :userId")
    fun getBalanceFlow(userId: String): Flow<BalanceEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: BalanceEntity)
    
    @Update
    suspend fun updateBalance(balance: BalanceEntity)
    
    // Essential expenses operations
    @Query("SELECT * FROM essential_expenses WHERE userId = :userId AND period = :period ORDER BY dueDay ASC")
    suspend fun getEssentialExpenses(userId: String, period: String): List<EssentialExpenseEntity>
    
    @Query("SELECT * FROM essential_expenses WHERE userId = :userId AND period = :period ORDER BY dueDay ASC")
    fun getEssentialExpensesFlow(userId: String, period: String): Flow<List<EssentialExpenseEntity>>
    
    @Query("SELECT * FROM essential_expenses WHERE userId = :userId AND paid = 0 AND period = :period")
    suspend fun getUnpaidEssentials(userId: String, period: String): List<EssentialExpenseEntity>
    
    @Query("SELECT SUM(plannedAmount) FROM essential_expenses WHERE userId = :userId AND period = :period")
    suspend fun getTotalPlannedExpenses(userId: String, period: String): Double?
    
    @Query("SELECT SUM(actualAmount) FROM essential_expenses WHERE userId = :userId AND period = :period AND paid = 1")
    suspend fun getTotalPaidExpenses(userId: String, period: String): Double?
    
    /**
     * Insert essential expense with IGNORE strategy to prevent duplicates.
     * If a record with same (userId, category, period) exists, the insert is silently ignored.
     * This prevents duplicate expenses during month rollover.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEssentialExpense(expense: EssentialExpenseEntity): Long
    
    @Query("DELETE FROM essential_expenses WHERE id = :expenseId")
    suspend fun deleteEssentialExpense(expenseId: String)
    
    @Query("SELECT * FROM essential_expenses WHERE id = :expenseId")
    suspend fun getEssentialExpenseById(expenseId: String): EssentialExpenseEntity?
    
    @Update
    suspend fun updateEssentialExpense(expense: EssentialExpenseEntity)
    
    // Enhanced subscriptions operations
    @Query("SELECT * FROM enhanced_subscriptions WHERE userId = :userId AND active = 1 ORDER BY nextBillingDate ASC")
    suspend fun getActiveSubscriptions(userId: String): List<EnhancedSubscriptionEntity>
    
    @Query("SELECT * FROM enhanced_subscriptions WHERE userId = :userId AND active = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptionsFlow(userId: String): Flow<List<EnhancedSubscriptionEntity>>
    
    @Query("SELECT * FROM enhanced_subscriptions WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAllSubscriptions(userId: String): List<EnhancedSubscriptionEntity>
    
    @Query("SELECT * FROM enhanced_subscriptions WHERE userId = :userId ORDER BY name ASC")
    fun getAllSubscriptionsFlow(userId: String): Flow<List<EnhancedSubscriptionEntity>>
    
    @Query("SELECT SUM(amount) FROM enhanced_subscriptions WHERE userId = :userId AND active = 1 AND frequency = 'MONTHLY'")
    suspend fun getTotalMonthlySubscriptions(userId: String): Double?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: EnhancedSubscriptionEntity)
    
    @Update
    suspend fun updateSubscription(subscription: EnhancedSubscriptionEntity)
    
    @Delete
    suspend fun deleteSubscription(subscription: EnhancedSubscriptionEntity)
    
    // Paycheck operations
    @Query("SELECT * FROM paychecks WHERE userId = :userId AND date >= :fromDate ORDER BY date ASC LIMIT :limit")
    suspend fun getUpcomingPaychecks(userId: String, fromDate: Long, limit: Int = 5): List<PaycheckEntity>
    
    @Query("SELECT * FROM paychecks WHERE userId = :userId ORDER BY date DESC")
    fun getPaychecksFlow(userId: String): Flow<List<PaycheckEntity>>
    
    @Query("SELECT * FROM paychecks WHERE userId = :userId AND deposited = 0 AND date <= :currentTime")
    suspend fun getPendingPaychecks(userId: String, currentTime: Long): List<PaycheckEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaycheck(paycheck: PaycheckEntity)
    
    @Update
    suspend fun updatePaycheck(paycheck: PaycheckEntity)
    
    @Delete
    suspend fun deletePaycheck(paycheck: PaycheckEntity)
    
    // FCM Reminders operations
    @Query("SELECT * FROM fcm_reminders WHERE userId = :userId AND sent = 0 AND fireAtUtc <= :currentTime ORDER BY fireAtUtc ASC")
    suspend fun getPendingReminders(userId: String, currentTime: Long): List<ReminderEntity>
    
    @Query("SELECT * FROM fcm_reminders WHERE userId = :userId ORDER BY fireAtUtc DESC")
    fun getRemindersFlow(userId: String): Flow<List<ReminderEntity>>
    
    @Query("SELECT * FROM fcm_reminders WHERE targetId = :targetId AND type = :type")
    suspend fun getRemindersForTarget(targetId: String, type: ReminderType): List<ReminderEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity)
    
    @Update
    suspend fun updateReminder(reminder: ReminderEntity)
    
    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)
    
    @Query("DELETE FROM fcm_reminders WHERE targetId = :targetId AND type = :type")
    suspend fun deleteRemindersForTarget(targetId: String, type: ReminderType)
    
    // FCM Device tokens operations
    @Query("SELECT * FROM fcm_devices WHERE userId = :userId AND notificationsEnabled = 1")
    suspend fun getActiveDevices(userId: String): List<DeviceEntity>
    
    @Query("SELECT * FROM fcm_devices WHERE deviceId = :deviceId")
    suspend fun getDevice(deviceId: String): DeviceEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: DeviceEntity)
    
    @Update
    suspend fun updateDevice(device: DeviceEntity)
    
    @Delete
    suspend fun deleteDevice(device: DeviceEntity)
    
    // Sync operations
    @Query("SELECT * FROM balance WHERE pendingSync = 1")
    suspend fun getUnsyncedBalance(): List<BalanceEntity>
    
    @Query("SELECT * FROM essential_expenses WHERE pendingSync = 1")
    suspend fun getUnsyncedEssentials(): List<EssentialExpenseEntity>
    
    @Query("SELECT * FROM enhanced_subscriptions WHERE pendingSync = 1")
    suspend fun getUnsyncedSubscriptions(): List<EnhancedSubscriptionEntity>
    
    @Query("SELECT * FROM paychecks WHERE pendingSync = 1")
    suspend fun getUnsyncedPaychecks(): List<PaycheckEntity>
    
    @Query("SELECT * FROM fcm_reminders WHERE pendingSync = 1")
    suspend fun getUnsyncedReminders(): List<ReminderEntity>
    
    @Query("SELECT * FROM fcm_devices WHERE pendingSync = 1")
    suspend fun getUnsyncedDevices(): List<DeviceEntity>
    
    // Complex queries for dashboard
    @Query("""
        SELECT 
            (SELECT currentBalance FROM balance WHERE userId = :userId) as currentBalance,
            (SELECT SUM(plannedAmount) FROM essential_expenses WHERE userId = :userId AND period = :period) as totalPlanned,
            (SELECT SUM(actualAmount) FROM essential_expenses WHERE userId = :userId AND period = :period AND paid = 1) as totalPaid,
            (SELECT COUNT(*) FROM essential_expenses WHERE userId = :userId AND period = :period AND paid = 0) as unpaidCount
    """)
    suspend fun getDashboardSummary(userId: String, period: String): DashboardSummary?
    
    // Timeline query for upcoming bills and paychecks
    @Query("""
        SELECT 'ESSENTIAL' as type, id, name as title, plannedAmount as amount, 
               (dueDay || '-' || :period) as dateStr, paid as completed
        FROM essential_expenses 
        WHERE userId = :userId AND period = :period
        UNION ALL
        SELECT 'SUBSCRIPTION' as type, id, name as title, amount, 
               datetime(nextBillingDate/1000, 'unixepoch') as dateStr, 0 as completed
        FROM enhanced_subscriptions 
        WHERE userId = :userId AND active = 1 AND nextBillingDate >= :fromDate
        UNION ALL
        SELECT 'PAYCHECK' as type, id, 'Paycheck' as title, netAmount as amount,
               datetime(date/1000, 'unixepoch') as dateStr, deposited as completed
        FROM paychecks
        WHERE userId = :userId AND date >= :fromDate
        ORDER BY dateStr ASC
        LIMIT :limit
    """)
    suspend fun getUpcomingTimeline(
        userId: String, 
        period: String, 
        fromDate: Long, 
        limit: Int = 30
    ): List<TimelineItem>
}

/**
 * Data class for dashboard summary
 */
data class DashboardSummary(
    val currentBalance: Double?,
    val totalPlanned: Double?,
    val totalPaid: Double?,
    val unpaidCount: Int?
)

/**
 * Data class for timeline items
 */
data class TimelineItem(
    val type: String, // ESSENTIAL, SUBSCRIPTION, PAYCHECK
    val id: String,
    val title: String,
    val amount: Double,
    val dateStr: String,
    val completed: Boolean
)