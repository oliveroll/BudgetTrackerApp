package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.EmergencyFundDao
import com.budgettracker.core.data.local.entities.EmergencyFundEntity
import com.budgettracker.core.data.local.entities.EmergencyFundDepositEntity
import com.budgettracker.core.domain.model.EmergencyFund
import com.budgettracker.core.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Emergency Fund operations with Firebase sync
 */
@Singleton
class EmergencyFundRepository @Inject constructor(
    private val emergencyFundDao: EmergencyFundDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    fun getActiveFundsFlow(): Flow<List<EmergencyFund>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return emergencyFundDao.getActiveFundsFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getActiveFunds(): Result<List<EmergencyFund>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val funds = emergencyFundDao.getActiveFunds(userId).map { it.toDomain() }
            Result.Success(funds)
        } catch (e: Exception) {
            Result.Error("Failed to get emergency funds: ${e.message}")
        }
    }
    
    suspend fun getFundById(fundId: String): Result<EmergencyFund> {
        return try {
            val fund = emergencyFundDao.getFundById(fundId)?.toDomain()
                ?: return Result.Error("Fund not found")
            Result.Success(fund)
        } catch (e: Exception) {
            Result.Error("Failed to get fund: ${e.message}")
        }
    }
    
    suspend fun addFund(fund: EmergencyFund): Result<String> {
        return try {
            val userId = currentUserId ?: run {
                android.util.Log.e("EmergencyFundRepo", "❌ Cannot add fund: User not authenticated")
                return Result.Error("User not authenticated")
            }
            
            android.util.Log.d("EmergencyFundRepo", "➕ Adding Emergency Fund: ${fund.bankName} | Balance: ${fund.currentBalance} | Goal: ${fund.targetGoal}")
            
            val fundWithUser = fund.copy(userId = userId)
            val entity = EmergencyFundEntity.fromDomain(fundWithUser, pendingSync = true)
            
            // Save to Room database
            emergencyFundDao.insertFund(entity)
            android.util.Log.d("EmergencyFundRepo", "💾 Saved to local database")
            
            // Sync to Firebase
            syncFundToFirebase(entity)
            android.util.Log.d("EmergencyFundRepo", "☁️ Synced to Firebase at /users/$userId/emergencyFunds/${fund.id}")
            
            Result.Success(fund.id)
        } catch (e: Exception) {
            android.util.Log.e("EmergencyFundRepo", "❌ Failed to add fund: ${e.message}", e)
            Result.Error("Failed to add fund: ${e.message}")
        }
    }
    
    suspend fun updateFund(fund: EmergencyFund): Result<Unit> {
        return try {
            val entity = EmergencyFundEntity.fromDomain(fund, pendingSync = true)
            emergencyFundDao.updateFund(entity)
            syncFundToFirebase(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update fund: ${e.message}")
        }
    }
    
    suspend fun deleteFund(fundId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val fund = emergencyFundDao.getFundById(fundId) ?: return Result.Error("Fund not found")
            
            emergencyFundDao.deleteFund(fund)
            
            firestore.collection("users")
                .document(userId)
                .collection("emergencyFunds")
                .document(fundId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete fund: ${e.message}")
        }
    }
    
    suspend fun recordDeposit(fundId: String, amount: Double, isAutomatic: Boolean = false): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val fund = emergencyFundDao.getFundById(fundId) ?: return Result.Error("Fund not found")
            
            // Create deposit record
            val deposit = EmergencyFundDepositEntity(
                fundId = fundId,
                amount = amount,
                isAutomatic = isAutomatic,
                pendingSync = true
            )
            emergencyFundDao.insertDeposit(deposit)
            
            // Update fund balance
            val updatedFund = fund.copy(
                currentBalance = fund.currentBalance + amount,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            emergencyFundDao.updateFund(updatedFund)
            
            // Sync to Firebase
            firestore.collection("users")
                .document(userId)
                .collection("emergencyFundDeposits")
                .document(deposit.id)
                .set(deposit.toFirestoreMap())
                .await()
            
            syncFundToFirebase(updatedFund)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to record deposit: ${e.message}")
        }
    }
    
    fun getDepositsFlow(fundId: String): Flow<List<EmergencyFundDepositEntity>> {
        return emergencyFundDao.getDepositsFlow(fundId)
    }
    
    suspend fun initializeFromFirebase() {
        try {
            val userId = currentUserId ?: run {
                android.util.Log.w("EmergencyFundRepo", "❌ No user ID, cannot initialize from Firebase")
                return
            }
            
            android.util.Log.d("EmergencyFundRepo", "🔍 Initializing Emergency Funds from Firebase for user: $userId")
            
            val fundsSnapshot = firestore.collection("users")
                .document(userId)
                .collection("emergencyFunds")
                .get()
                .await()
            
            android.util.Log.d("EmergencyFundRepo", "📦 Found ${fundsSnapshot.documents.size} Emergency Fund documents in Firestore")
            
            fundsSnapshot.documents.forEach { doc ->
                val entity = doc.toEmergencyFundEntity().copy(userId = userId) // CRITICAL FIX: Set userId explicitly
                android.util.Log.d("EmergencyFundRepo", "  • Fund: ${entity.bankName} | Balance: ${entity.currentBalance} | Goal: ${entity.targetGoal} | userId: ${entity.userId} | isActive: ${entity.isActive}")
                emergencyFundDao.insertFund(entity)
            }
            
            android.util.Log.d("EmergencyFundRepo", "✅ Successfully initialized ${fundsSnapshot.documents.size} Emergency Funds from Firebase")
        } catch (e: Exception) {
            android.util.Log.e("EmergencyFundRepo", "❌ Failed to initialize from Firebase: ${e.message}", e)
        }
    }
    
    private suspend fun syncFundToFirebase(fund: EmergencyFundEntity) {
        try {
            val userId = currentUserId ?: return
            firestore.collection("users")
                .document(userId)
                .collection("emergencyFunds")
                .document(fund.id)
                .set(fund.toFirestoreMap())
                .await()
            
            emergencyFundDao.markSynced(fund.id, System.currentTimeMillis())
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
}

// Extension functions for Firestore mapping
private fun EmergencyFundEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "bankName" to bankName,
    "accountType" to accountType,
    "accountNumber" to accountNumber,
    "currentBalance" to currentBalance,
    "targetGoal" to targetGoal,
    "apy" to apy,
    "compoundingFrequency" to compoundingFrequency,
    "monthlyContribution" to monthlyContribution,
    "autoDeposit" to autoDeposit,
    "depositDayOfMonth" to depositDayOfMonth,
    "monthsOfExpensesCovered" to monthsOfExpensesCovered,
    "minimumBalance" to minimumBalance,
    "notes" to notes,
    "isActive" to isActive,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun EmergencyFundDepositEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "fundId" to fundId,
    "amount" to amount,
    "depositDate" to com.google.firebase.Timestamp(Date(depositDate)),
    "isAutomatic" to isAutomatic,
    "transactionId" to transactionId,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp()
)

private fun com.google.firebase.firestore.DocumentSnapshot.toEmergencyFundEntity(): EmergencyFundEntity {
    return EmergencyFundEntity(
        id = id,
        userId = getString("userId") ?: "",
        bankName = getString("bankName") ?: "",
        accountType = getString("accountType") ?: "High-Yield Savings",
        accountNumber = getString("accountNumber") ?: "",
        currentBalance = getDouble("currentBalance") ?: 0.0,
        targetGoal = getDouble("targetGoal") ?: 5000.0,
        apy = getDouble("apy") ?: 0.0,
        compoundingFrequency = getString("compoundingFrequency") ?: "MONTHLY",
        monthlyContribution = getDouble("monthlyContribution") ?: 0.0,
        autoDeposit = getBoolean("autoDeposit") ?: false,
        depositDayOfMonth = getLong("depositDayOfMonth")?.toInt(),
        monthsOfExpensesCovered = getLong("monthsOfExpensesCovered")?.toInt() ?: 6,
        minimumBalance = getDouble("minimumBalance") ?: 0.0,
        notes = getString("notes") ?: "",
        isActive = getBoolean("isActive") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
        syncedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}

