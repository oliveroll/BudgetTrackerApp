package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for UserProfile operations
 */
@Dao
interface UserProfileDao {
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileEntity?
    
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?>
    
    @Query("SELECT * FROM user_profiles WHERE email = :email")
    suspend fun getUserProfileByEmail(email: String): UserProfileEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity): Long
    
    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)
    
    @Query("UPDATE user_profiles SET monthlyIncome = :income WHERE userId = :userId")
    suspend fun updateMonthlyIncome(userId: String, income: Double)
    
    @Query("UPDATE user_profiles SET baseSalary = :salary WHERE userId = :userId")
    suspend fun updateBaseSalary(userId: String, salary: Double)
    
    @Query("UPDATE user_profiles SET employmentStatus = :status WHERE userId = :userId")
    suspend fun updateEmploymentStatus(userId: String, status: String)
    
    @Query("UPDATE user_profiles SET company = :company WHERE userId = :userId")
    suspend fun updateCompany(userId: String, company: String)
    
    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profiles WHERE userId = :userId")
    suspend fun deleteUserProfileById(userId: String)
    
    // Sync related queries
    @Query("SELECT * FROM user_profiles WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedUserProfiles(): List<UserProfileEntity>
    
    @Query("UPDATE user_profiles SET syncStatus = :status WHERE userId = :userId")
    suspend fun updateSyncStatus(userId: String, status: String)
}

