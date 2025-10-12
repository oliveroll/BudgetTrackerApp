package com.budgettracker.core.data.repository

import com.budgettracker.core.domain.model.*
import com.budgettracker.core.domain.repository.SubscriptionRepository
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub implementation of SubscriptionRepository for build purposes
 */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor() : SubscriptionRepository {
    
    override fun getActiveSubscriptions(userId: String): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getSubscriptionById(id: String): Subscription? {
        return null
    }
    
    override fun getSubscriptionByIdFlow(id: String): Flow<Subscription?> {
        return flowOf(null)
    }
    
    override fun getSubscriptionsByCategory(userId: String, category: String): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override fun getSubscriptionsByFrequency(userId: String, frequency: SubscriptionFrequency): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override fun getUpcomingSubscriptions(userId: String, startDate: Date, endDate: Date): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override suspend fun getTotalMonthlyCost(userId: String): Result<Double> {
        return Result.Success(0.0)
    }
    
    override suspend fun getActiveSubscriptionCount(userId: String): Result<Int> {
        return Result.Success(0)
    }
    
    override fun getSubscriptionsWithReminders(userId: String): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override fun searchSubscriptions(userId: String, query: String): Flow<List<Subscription>> {
        return flowOf(emptyList())
    }
    
    override suspend fun createSubscription(subscription: Subscription): Result<String> {
        return Result.Success(subscription.id)
    }
    
    override suspend fun updateSubscription(subscription: Subscription): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deleteSubscription(id: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun deactivateSubscription(id: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun updateReminderStatus(id: String, enabled: Boolean): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun updateNextBillingDate(id: String, nextBillingDate: Date): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun syncSubscriptions(userId: String): Result<Unit> {
        return Result.Success(Unit)
    }
    
    override suspend fun getUnsyncedSubscriptions(): Result<List<Subscription>> {
        return Result.Success(emptyList())
    }
}
