package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.*
import com.budgettracker.core.data.local.entities.*
import com.budgettracker.core.domain.model.*
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
 * Repository for Debt Loan operations with Firebase sync
 */
@Singleton
class DebtLoanRepository @Inject constructor(
    private val debtLoanDao: DebtLoanDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    /**
     * Get all active loans as Flow
     */
    fun getActiveLoansFlow(): Flow<List<DebtLoan>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return debtLoanDao.getActiveLoansFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get all active loans
     */
    suspend fun getActiveLoans(): Result<List<DebtLoan>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val loans = debtLoanDao.getActiveLoans(userId).map { it.toDomain() }
            Result.Success(loans)
        } catch (e: Exception) {
            Result.Error("Failed to get loans: ${e.message}")
        }
    }
    
    /**
     * Get loan by ID
     */
    suspend fun getLoanById(loanId: String): Result<DebtLoan> {
        return try {
            val loan = debtLoanDao.getLoanById(loanId)?.toDomain()
                ?: return Result.Error("Loan not found")
            Result.Success(loan)
        } catch (e: Exception) {
            Result.Error("Failed to get loan: ${e.message}")
        }
    }
    
    /**
     * Add new loan
     */
    suspend fun addLoan(loan: DebtLoan): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val loanWithUser = loan.copy(userId = userId)
            val entity = DebtLoanEntity.fromDomain(loanWithUser, pendingSync = true)
            
            // Save to local database
            debtLoanDao.insertLoan(entity)
            
            // Sync to Firebase
            syncLoanToFirebase(entity)
            
            Result.Success(loan.id)
        } catch (e: Exception) {
            Result.Error("Failed to add loan: ${e.message}")
        }
    }
    
    /**
     * Update existing loan
     */
    suspend fun updateLoan(loan: DebtLoan): Result<Unit> {
        return try {
            val entity = DebtLoanEntity.fromDomain(loan, pendingSync = true)
            debtLoanDao.updateLoan(entity)
            
            // Sync to Firebase
            syncLoanToFirebase(entity)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update loan: ${e.message}")
        }
    }
    
    /**
     * Delete loan
     */
    suspend fun deleteLoan(loanId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Delete from local database
            debtLoanDao.deleteLoanById(loanId)
            
            // Delete from Firebase
            firestore.collection("users")
                .document(userId)
                .collection("debtLoans")
                .document(loanId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete loan: ${e.message}")
        }
    }
    
    /**
     * Record a payment
     */
    suspend fun recordPayment(
        loanId: String,
        amount: Double,
        principalPaid: Double,
        interestPaid: Double
    ): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val loan = debtLoanDao.getLoanById(loanId) ?: return Result.Error("Loan not found")
            
            // Create payment record
            val paymentRecord = DebtPaymentRecordEntity(
                loanId = loanId,
                amount = amount,
                principalPaid = principalPaid,
                interestPaid = interestPaid,
                balanceAfter = loan.currentBalance - principalPaid,
                pendingSync = true
            )
            debtLoanDao.insertPaymentRecord(paymentRecord)
            
            // Update loan balance
            val updatedLoan = loan.copy(
                currentBalance = (loan.currentBalance - principalPaid).coerceAtLeast(0.0),
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            debtLoanDao.updateLoan(updatedLoan)
            
            // Sync to Firebase
            firestore.collection("users")
                .document(userId)
                .collection("debtPayments")
                .document(paymentRecord.id)
                .set(paymentRecord.toFirestoreMap())
                .await()
            
            syncLoanToFirebase(updatedLoan)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to record payment: ${e.message}")
        }
    }
    
    /**
     * Get payment records for a loan
     */
    fun getPaymentRecordsFlow(loanId: String): Flow<List<DebtPaymentRecordEntity>> {
        return debtLoanDao.getPaymentRecordsFlow(loanId)
    }
    
    /**
     * Initialize from Firebase
     */
    suspend fun initializeFromFirebase() {
        try {
            val userId = currentUserId ?: return
            
            // Fetch loans from Firebase
            val loansSnapshot = firestore.collection("users")
                .document(userId)
                .collection("debtLoans")
                .get()
                .await()
            
            loansSnapshot.documents.forEach { doc ->
                val entity = doc.toDebtLoanEntity()
                debtLoanDao.insertLoan(entity)
            }
        } catch (e: Exception) {
            // Fail silently, use local data
        }
    }
    
    private suspend fun syncLoanToFirebase(loan: DebtLoanEntity) {
        try {
            val userId = currentUserId ?: return
            firestore.collection("users")
                .document(userId)
                .collection("debtLoans")
                .document(loan.id)
                .set(loan.toFirestoreMap())
                .await()
            
            debtLoanDao.markSynced(loan.id, System.currentTimeMillis())
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
}

/**
 * Repository for Roth IRA operations with Firebase sync
 */
@Singleton
class RothIRARepository @Inject constructor(
    private val rothIRADao: RothIRADao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    fun getActiveIRAsFlow(): Flow<List<RothIRA>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return rothIRADao.getActiveIRAsFlow(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getActiveIRAs(): Result<List<RothIRA>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val iras = rothIRADao.getActiveIRAs(userId).map { it.toDomain() }
            Result.Success(iras)
        } catch (e: Exception) {
            Result.Error("Failed to get IRAs: ${e.message}")
        }
    }
    
    suspend fun getIRAForYear(taxYear: Int): Result<RothIRA?> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val ira = rothIRADao.getIRAForYear(userId, taxYear)?.toDomain()
            Result.Success(ira)
        } catch (e: Exception) {
            Result.Error("Failed to get IRA: ${e.message}")
        }
    }
    
    suspend fun addIRA(ira: RothIRA): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val iraWithUser = ira.copy(userId = userId)
            val entity = RothIRAEntity.fromDomain(iraWithUser, pendingSync = true)
            
            rothIRADao.insertIRA(entity)
            syncIRAToFirebase(entity)
            
            Result.Success(ira.id)
        } catch (e: Exception) {
            Result.Error("Failed to add IRA: ${e.message}")
        }
    }
    
    suspend fun updateIRA(ira: RothIRA): Result<Unit> {
        return try {
            val entity = RothIRAEntity.fromDomain(ira, pendingSync = true)
            rothIRADao.updateIRA(entity)
            syncIRAToFirebase(entity)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update IRA: ${e.message}")
        }
    }
    
    suspend fun recordContribution(iraId: String, amount: Double, taxYear: Int): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val ira = rothIRADao.getIRAById(iraId) ?: return Result.Error("IRA not found")
            
            // Create contribution record
            val contribution = IRAContributionEntity(
                iraId = iraId,
                amount = amount,
                taxYear = taxYear,
                pendingSync = true
            )
            rothIRADao.insertContribution(contribution)
            
            // Update IRA contributions total
            val updatedIRA = ira.copy(
                contributionsThisYear = ira.contributionsThisYear + amount,
                currentBalance = ira.currentBalance + amount,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            rothIRADao.updateIRA(updatedIRA)
            
            // Sync to Firebase
            firestore.collection("users")
                .document(userId)
                .collection("iraContributions")
                .document(contribution.id)
                .set(contribution.toFirestoreMap())
                .await()
            
            syncIRAToFirebase(updatedIRA)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to record contribution: ${e.message}")
        }
    }
    
    suspend fun initializeFromFirebase() {
        try {
            val userId = currentUserId ?: return
            
            val irasSnapshot = firestore.collection("users")
                .document(userId)
                .collection("rothIRAs")
                .get()
                .await()
            
            irasSnapshot.documents.forEach { doc ->
                val entity = doc.toRothIRAEntity()
                rothIRADao.insertIRA(entity)
            }
        } catch (e: Exception) {
            // Fail silently
        }
    }
    
    private suspend fun syncIRAToFirebase(ira: RothIRAEntity) {
        try {
            val userId = currentUserId ?: return
            firestore.collection("users")
                .document(userId)
                .collection("rothIRAs")
                .document(ira.id)
                .set(ira.toFirestoreMap())
                .await()
            
            rothIRADao.markSynced(ira.id, System.currentTimeMillis())
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
}

// Extension functions for Firestore mapping
private fun DebtLoanEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "loanProvider" to loanProvider,
    "loanType" to loanType,
    "accountNumber" to accountNumber,
    "originalAmount" to originalAmount,
    "currentBalance" to currentBalance,
    "interestRate" to interestRate,
    "repaymentStartDate" to com.google.firebase.Timestamp(Date(repaymentStartDate)),
    "currentMonthlyPayment" to currentMonthlyPayment,
    "adjustedMonthlyPayment" to adjustedMonthlyPayment,
    "nextPaymentDueDate" to com.google.firebase.Timestamp(Date(nextPaymentDueDate)),
    "minimumPayment" to minimumPayment,
    "currency" to currency,
    "notes" to notes,
    "isActive" to isActive,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun RothIRAEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "brokerageName" to brokerageName,
    "accountNumber" to accountNumber,
    "annualContributionLimit" to annualContributionLimit,
    "contributionsThisYear" to contributionsThisYear,
    "currentBalance" to currentBalance,
    "taxYear" to taxYear,
    "recurringContributionAmount" to recurringContributionAmount,
    "recurringContributionFrequency" to recurringContributionFrequency,
    "recurringContributionStartDate" to recurringContributionStartDate?.let { com.google.firebase.Timestamp(Date(it)) },
    "recurringContributionDayOfMonth" to recurringContributionDayOfMonth,
    "autoIncrease" to autoIncrease,
    "autoIncreasePercentage" to autoIncreasePercentage,
    "notes" to notes,
    "isActive" to isActive,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun DebtPaymentRecordEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "loanId" to loanId,
    "amount" to amount,
    "paymentDate" to com.google.firebase.Timestamp(Date(paymentDate)),
    "principalPaid" to principalPaid,
    "interestPaid" to interestPaid,
    "balanceAfter" to balanceAfter,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp()
)

private fun IRAContributionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "iraId" to iraId,
    "amount" to amount,
    "contributionDate" to com.google.firebase.Timestamp(Date(contributionDate)),
    "taxYear" to taxYear,
    "isAutomatic" to isAutomatic,
    "transactionId" to transactionId,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp()
)

// Firestore document to entity conversion
private fun com.google.firebase.firestore.DocumentSnapshot.toDebtLoanEntity(): DebtLoanEntity {
    return DebtLoanEntity(
        id = id,
        userId = getString("userId") ?: "",
        loanProvider = getString("loanProvider") ?: "",
        loanType = getString("loanType") ?: "",
        accountNumber = getString("accountNumber") ?: "",
        originalAmount = getDouble("originalAmount") ?: 0.0,
        currentBalance = getDouble("currentBalance") ?: 0.0,
        interestRate = getDouble("interestRate") ?: 0.0,
        repaymentStartDate = getTimestamp("repaymentStartDate")?.toDate()?.time ?: System.currentTimeMillis(),
        currentMonthlyPayment = getDouble("currentMonthlyPayment") ?: 0.0,
        adjustedMonthlyPayment = getDouble("adjustedMonthlyPayment"),
        nextPaymentDueDate = getTimestamp("nextPaymentDueDate")?.toDate()?.time ?: System.currentTimeMillis(),
        minimumPayment = getDouble("minimumPayment") ?: 0.0,
        currency = getString("currency") ?: "USD",
        notes = getString("notes") ?: "",
        isActive = getBoolean("isActive") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
        syncedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}

private fun com.google.firebase.firestore.DocumentSnapshot.toRothIRAEntity(): RothIRAEntity {
    return RothIRAEntity(
        id = id,
        userId = getString("userId") ?: "",
        brokerageName = getString("brokerageName") ?: "",
        accountNumber = getString("accountNumber") ?: "",
        annualContributionLimit = getDouble("annualContributionLimit") ?: 7000.0,
        contributionsThisYear = getDouble("contributionsThisYear") ?: 0.0,
        currentBalance = getDouble("currentBalance") ?: 0.0,
        taxYear = getLong("taxYear")?.toInt() ?: Calendar.getInstance().get(Calendar.YEAR),
        recurringContributionAmount = getDouble("recurringContributionAmount"),
        recurringContributionFrequency = getString("recurringContributionFrequency") ?: "BIWEEKLY",
        recurringContributionStartDate = getTimestamp("recurringContributionStartDate")?.toDate()?.time,
        recurringContributionDayOfMonth = getLong("recurringContributionDayOfMonth")?.toInt(),
        autoIncrease = getBoolean("autoIncrease") ?: false,
        autoIncreasePercentage = getDouble("autoIncreasePercentage") ?: 0.0,
        notes = getString("notes") ?: "",
        isActive = getBoolean("isActive") ?: true,
        createdAt = getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
        updatedAt = getTimestamp("updatedAt")?.toDate()?.time ?: System.currentTimeMillis(),
        syncedAt = System.currentTimeMillis(),
        pendingSync = false
    )
}

