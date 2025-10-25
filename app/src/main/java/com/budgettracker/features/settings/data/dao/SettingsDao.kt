package com.budgettracker.features.settings.data.dao

import androidx.room.*
import com.budgettracker.features.settings.data.models.CustomCategory
import com.budgettracker.features.settings.data.models.EmploymentSettings
import com.budgettracker.features.settings.data.models.UserSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getUserSettingsFlow(userId: String): Flow<UserSettings?>
    
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    suspend fun getUserSettings(userId: String): UserSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(settings: UserSettings)
    
    @Update
    suspend fun updateUserSettings(settings: UserSettings)
    
    @Query("DELETE FROM user_settings WHERE userId = :userId")
    suspend fun deleteUserSettings(userId: String)
}

@Dao
interface EmploymentSettingsDao {
    @Query("SELECT * FROM employment_settings WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveEmploymentFlow(userId: String): Flow<EmploymentSettings?>
    
    @Query("SELECT * FROM employment_settings WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActiveEmployment(userId: String): EmploymentSettings?
    
    @Query("SELECT * FROM employment_settings WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getAllEmploymentHistory(userId: String): List<EmploymentSettings>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployment(employment: EmploymentSettings)
    
    @Update
    suspend fun updateEmployment(employment: EmploymentSettings)
    
    @Query("UPDATE employment_settings SET isActive = 0 WHERE userId = :userId AND id != :currentId")
    suspend fun deactivateOtherEmployments(userId: String, currentId: String)
    
    @Transaction
    suspend fun setAsActive(employment: EmploymentSettings) {
        deactivateOtherEmployments(employment.userId, employment.id)
        insertEmployment(employment.copy(isActive = true))
    }
}

@Dao
interface CustomCategoryDao {
    @Query("SELECT * FROM custom_categories WHERE userId = :userId AND isArchived = 0 ORDER BY name ASC")
    fun getActiveCategoriesFlow(userId: String): Flow<List<CustomCategory>>
    
    @Query("SELECT * FROM custom_categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getAllCategories(userId: String): List<CustomCategory>
    
    @Query("SELECT * FROM custom_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CustomCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)
    
    @Update
    suspend fun updateCategory(category: CustomCategory)
    
    @Query("UPDATE custom_categories SET isArchived = 1, updatedAt = :timestamp WHERE id = :categoryId")
    suspend fun archiveCategory(categoryId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("DELETE FROM custom_categories WHERE id = :categoryId AND transactionCount = 0")
    suspend fun deleteCategory(categoryId: String): Int
    
    @Query("UPDATE custom_categories SET transactionCount = transactionCount + 1 WHERE id = :categoryId")
    suspend fun incrementTransactionCount(categoryId: String)
    
    @Query("UPDATE custom_categories SET transactionCount = transactionCount - 1 WHERE id = :categoryId AND transactionCount > 0")
    suspend fun decrementTransactionCount(categoryId: String)
}

