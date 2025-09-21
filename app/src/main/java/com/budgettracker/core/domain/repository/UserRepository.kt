package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for User operations
 */
interface UserRepository {
    
    // Read operations
    suspend fun getUserProfile(userId: String): UserProfile?
    fun getUserProfileFlow(userId: String): Flow<UserProfile?>
    suspend fun getUserProfileByEmail(email: String): UserProfile?
    
    // Write operations
    suspend fun insertUserProfile(profile: UserProfile): Result<String>
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit>
    suspend fun updateMonthlyIncome(userId: String, income: Double): Result<Unit>
    suspend fun updateBaseSalary(userId: String, salary: Double): Result<Unit>
    suspend fun updateEmploymentStatus(userId: String, status: String): Result<Unit>
    suspend fun updateCompany(userId: String, company: String): Result<Unit>
    suspend fun deleteUserProfile(userId: String): Result<Unit>
    
    // Authentication related
    suspend fun createUserProfile(
        userId: String,
        email: String,
        name: String
    ): Result<UserProfile>
    
    // Sync operations
    suspend fun syncUserProfile(userId: String): Result<Unit>
    suspend fun getUnsyncedUserProfiles(): List<UserProfile>
}


