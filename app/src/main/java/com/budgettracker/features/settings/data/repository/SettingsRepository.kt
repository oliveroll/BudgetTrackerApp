package com.budgettracker.features.settings.data.repository

import android.util.Log
import com.budgettracker.core.utils.Result
import com.budgettracker.features.settings.data.dao.CustomCategoryDao
import com.budgettracker.features.settings.data.dao.EmploymentSettingsDao
import com.budgettracker.features.settings.data.dao.UserSettingsDao
import com.budgettracker.features.settings.data.models.*
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    private val employmentSettingsDao: EmploymentSettingsDao,
    private val customCategoryDao: CustomCategoryDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val TAG = "SettingsRepository"
    }
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    // ============================================
    // USER SETTINGS
    // ============================================
    
    fun getUserSettingsFlow(): Flow<UserSettings?> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(null)
        return userSettingsDao.getUserSettingsFlow(userId)
    }
    
    suspend fun initializeUserSettings(): Result<UserSettings> {
        return try {
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            val user = auth.currentUser ?: return Result.Error("No user")
            
            // Check if settings exist
            val existing = userSettingsDao.getUserSettings(userId)
            if (existing != null) {
                return Result.Success(existing)
            }
            
            // Create default settings
            val settings = UserSettings(
                userId = userId,
                email = user.email ?: "",
                displayName = user.displayName,
                photoUrl = user.photoUrl?.toString()
            )
            
            // Save to Room
            userSettingsDao.insertUserSettings(settings)
            
            // Sync to Firestore
            syncUserSettingsToFirestore(settings)
            
            Result.Success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize user settings", e)
            Result.Error("Failed to initialize settings: ${e.message}")
        }
    }
    
    suspend fun updateUserSettings(settings: UserSettings): Result<Unit> {
        return try {
            // Update Room
            userSettingsDao.updateUserSettings(settings.copy(updatedAt = System.currentTimeMillis()))
            
            // Sync to Firestore
            syncUserSettingsToFirestore(settings)
            
            // Schedule/update notifications based on new settings
            notificationScheduler.scheduleAll(
                lowBalanceEnabled = settings.notificationSettings.lowBalanceAlertEnabled,
                lowBalanceFrequency = settings.notificationSettings.lowBalanceFrequency,
                goalMilestoneEnabled = settings.notificationSettings.goalMilestoneEnabled,
                goalMilestoneFrequency = settings.notificationSettings.goalMilestoneFrequency
            )
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update user settings", e)
            Result.Error("Failed to update settings: ${e.message}")
        }
    }
    
    private suspend fun syncUserSettingsToFirestore(settings: UserSettings) {
        try {
            val userId = currentUserId ?: return
            firestore.collection("users")
                .document(userId)
                .set(settings.toFirestoreMap())
                .await()
            
            // Update sync timestamp
            userSettingsDao.updateUserSettings(
                settings.copy(lastSyncedAt = System.currentTimeMillis())
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync settings to Firestore", e)
        }
    }
    
    // ============================================
    // EMAIL CHANGE (requires re-authentication)
    // ============================================
    
    suspend fun changeEmail(
        currentPassword: String,
        newEmail: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.Error("Not authenticated")
            val currentEmail = user.email ?: return Result.Error("No email")
            
            // Re-authenticate first
            val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)
            user.reauthenticate(credential).await()
            
            // Update email in Firebase Auth
            user.updateEmail(newEmail).await()
            
            // Update in Firestore and Room
            val userId = user.uid
            val settings = userSettingsDao.getUserSettings(userId)
            if (settings != null) {
                updateUserSettings(settings.copy(email = newEmail))
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to change email", e)
            val errorMessage = when {
                e.message?.contains("requires-recent-login") == true -> 
                    "This operation requires recent authentication. Please sign out and sign in again."
                e.message?.contains("email-already-in-use") == true -> 
                    "This email is already in use by another account."
                e.message?.contains("invalid-email") == true -> 
                    "Invalid email format."
                e.message?.contains("wrong-password") == true -> 
                    "Incorrect password."
                else -> "Failed to change email: ${e.message}"
            }
            Result.Error(errorMessage)
        }
    }
    
    // ============================================
    // EMPLOYMENT SETTINGS
    // ============================================
    
    fun getActiveEmploymentFlow(): Flow<EmploymentSettings?> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(null)
        return employmentSettingsDao.getActiveEmploymentFlow(userId)
    }
    
    suspend fun saveEmploymentSettings(employment: EmploymentSettings): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            
            // Set as active (deactivates others)
            employmentSettingsDao.setAsActive(employment.copy(userId = userId))
            
            // Sync to Firestore
            firestore.collection("users")
                .document(userId)
                .collection("employment")
                .document(employment.id)
                .set(employment.toFirestoreMap())
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save employment settings", e)
            Result.Error("Failed to save employment settings: ${e.message}")
        }
    }
    
    // ============================================
    // CUSTOM CATEGORIES
    // ============================================
    
    fun getActiveCategoriesFlow(): Flow<List<CustomCategory>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return customCategoryDao.getActiveCategoriesFlow(userId)
    }
    
    suspend fun addCategory(category: CustomCategory): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            
            // Save to Room
            customCategoryDao.insertCategory(category.copy(userId = userId))
            
            // Sync to Firestore
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(category.id)
                .set(category.toFirestoreMap())
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add category", e)
            Result.Error("Failed to add category: ${e.message}")
        }
    }
    
    suspend fun updateCategory(category: CustomCategory): Result<Unit> {
        return try {
            // Update Room
            customCategoryDao.updateCategory(category.copy(updatedAt = System.currentTimeMillis()))
            
            // Sync to Firestore
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(category.id)
                .set(category.toFirestoreMap())
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update category", e)
            Result.Error("Failed to update category: ${e.message}")
        }
    }
    
    suspend fun archiveCategory(categoryId: String): Result<Unit> {
        return try {
            // Archive in Room
            customCategoryDao.archiveCategory(categoryId)
            
            // Update Firestore
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(categoryId)
                .update("isArchived", true, "updatedAt", com.google.firebase.Timestamp.now())
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to archive category", e)
            Result.Error("Failed to archive category: ${e.message}")
        }
    }
    
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            // Check if category has transactions
            val category = customCategoryDao.getCategoryById(categoryId)
            if (category != null && category.transactionCount > 0) {
                return Result.Error("Cannot delete category with existing transactions. Archive it instead.")
            }
            
            // Delete from Room
            val deletedRows = customCategoryDao.deleteCategory(categoryId)
            if (deletedRows == 0) {
                return Result.Error("Category not found or has transactions")
            }
            
            // Delete from Firestore
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(categoryId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete category", e)
            Result.Error("Failed to delete category: ${e.message}")
        }
    }
    
    // ============================================
    // ACCOUNT ACTIONS
    // ============================================
    
    suspend fun exportUserData(): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            
            // Collect all data
            val settings = userSettingsDao.getUserSettings(userId)
            val employment = employmentSettingsDao.getActiveEmployment(userId)
            val categories = customCategoryDao.getAllCategories(userId)
            
            // Fetch from Firestore for complete data
            val transactions = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()
                .documents.map { it.data }
            
            val goals = firestore.collection("users")
                .document(userId)
                .collection("debtLoans")
                .get()
                .await()
                .documents.map { it.data }
            
            val subscriptions = firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .get()
                .await()
                .documents.map { it.data }
            
            // Build JSON
            val exportData = mapOf(
                "schemaVersion" to "1.0",
                "exportedAt" to System.currentTimeMillis(),
                "userId" to userId,
                "settings" to settings,
                "employment" to employment,
                "categories" to categories,
                "transactions" to transactions,
                "goals" to goals,
                "subscriptions" to subscriptions
            )
            
            val json = com.google.gson.Gson().toJson(exportData)
            Result.Success(json)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export data", e)
            Result.Error("Failed to export data: ${e.message}")
        }
    }
    
    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.Error("Not authenticated")
            val email = user.email ?: return Result.Error("No email")
            val userId = user.uid
            
            // Re-authenticate first
            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()
            
            // Delete Firestore data
            deleteUserDataFromFirestore(userId)
            
            // Delete Room data
            userSettingsDao.deleteUserSettings(userId)
            
            // Delete Firebase Auth user
            user.delete().await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete account", e)
            val errorMessage = when {
                e.message?.contains("wrong-password") == true -> 
                    "Incorrect password."
                e.message?.contains("requires-recent-login") == true -> 
                    "Please sign out and sign in again before deleting your account."
                else -> "Failed to delete account: ${e.message}"
            }
            Result.Error(errorMessage)
        }
    }
    
    private suspend fun deleteUserDataFromFirestore(userId: String) {
        try {
            // Delete subcollections
            val collections = listOf(
                "transactions", "essentials", "subscriptions", 
                "debtLoans", "rothIRAs", "emergencyFunds", 
                "etfPortfolios", "employment", "categories", "balance"
            )
            
            for (collection in collections) {
                val docs = firestore.collection("users")
                    .document(userId)
                    .collection(collection)
                    .get()
                    .await()
                
                docs.documents.forEach { it.reference.delete().await() }
            }
            
            // Delete user document
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete Firestore data", e)
            throw e
        }
    }
}

