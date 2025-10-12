package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.SubscriptionEntity
import com.budgettracker.core.domain.model.SubscriptionFrequency
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Subscription operations
 */
@Dao
interface SubscriptionDao {
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND isActive = 1 ORDER BY nextBillingDate ASC")
    fun getActiveSubscriptions(userId: String): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscriptionById(id: String): SubscriptionEntity?
    
    @Query("SELECT * FROM subscriptions WHERE id = :id")
    fun getSubscriptionByIdFlow(id: String): Flow<SubscriptionEntity?>
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND category = :category AND isActive = 1")
    fun getSubscriptionsByCategory(userId: String, category: String): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND frequency = :frequency AND isActive = 1")
    fun getSubscriptionsByFrequency(userId: String, frequency: SubscriptionFrequency): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND nextBillingDate BETWEEN :startDate AND :endDate AND isActive = 1")
    fun getUpcomingSubscriptions(userId: String, startDate: Long, endDate: Long): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT SUM(cost) FROM subscriptions WHERE userId = :userId AND isActive = 1 AND frequency = 'MONTHLY'")
    suspend fun getTotalMonthlyCost(userId: String): Double?
    
    @Query("SELECT COUNT(*) FROM subscriptions WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveSubscriptionCount(userId: String): Int
    
    @Query("SELECT * FROM subscriptions WHERE userId = :userId AND reminderEnabled = 1 AND isActive = 1")
    fun getSubscriptionsWithReminders(userId: String): Flow<List<SubscriptionEntity>>
    
    @Query("SELECT * FROM subscriptions WHERE name LIKE '%' || :searchQuery || '%' AND userId = :userId AND isActive = 1")
    fun searchSubscriptions(userId: String, searchQuery: String): Flow<List<SubscriptionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)
    
    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)
    
    @Delete
    suspend fun deleteSubscription(subscription: SubscriptionEntity)
    
    @Query("UPDATE subscriptions SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun deactivateSubscription(id: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE subscriptions SET reminderEnabled = :enabled, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateReminderStatus(id: String, enabled: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE subscriptions SET nextBillingDate = :nextBillingDate, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateNextBillingDate(id: String, nextBillingDate: Long, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE subscriptions SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
    
    @Query("SELECT * FROM subscriptions WHERE syncStatus = 'PENDING'")
    suspend fun getUnsyncedSubscriptions(): List<SubscriptionEntity>
    
    @Query("DELETE FROM subscriptions WHERE userId = :userId")
    suspend fun deleteAllUserSubscriptions(userId: String)
}



