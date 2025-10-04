package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.LoanDao
import com.budgettracker.core.data.local.dao.LoanPaymentDao
import com.budgettracker.core.data.local.entities.LoanEntity
import com.budgettracker.core.data.local.entities.LoanPaymentEntity
import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.model.LoanPayment
import com.budgettracker.core.domain.model.LoanSummary
import com.budgettracker.core.domain.model.LoanType
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LoanRepository with local-first approach and Firestore sync
 */
@Singleton
class LoanRepositoryImpl @Inject constructor(
    private val loanDao: LoanDao,
    private val loanPaymentDao: LoanPaymentDao,
    private val firestore: FirebaseFirestore
) : LoanRepository {
    
    companion object {
        private const val LOANS_COLLECTION = "loans"
        private const val PAYMENTS_SUBCOLLECTION = "payments"
    }
    
    override suspend fun createLoan(loan: Loan): Result<String> {
        return try {
            val entity = LoanEntity.fromDomain(loan)
            loanDao.insertLoan(entity)
            syncLoanToFirestore(loan)
            Result.Success(loan.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create loan")
        }
    }
    
    override suspend fun updateLoan(loan: Loan): Result<Unit> {
        return try {
            val entity = LoanEntity.fromDomain(loan.copy(updatedAt = Date()))
            loanDao.updateLoan(entity)
            syncLoanToFirestore(loan)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update loan")
        }
    }
    
    override suspend fun deleteLoan(loanId: String): Result<Unit> {
        return try {
            val loan = loanDao.getLoanById(loanId)
            if (loan != null) {
                loanDao.deleteLoan(loan)
                try {
                    firestore.collection(LOANS_COLLECTION).document(loanId).delete().await()
                } catch (e: Exception) {
                    // Handle Firestore deletion error silently for offline functionality
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete loan")
        }
    }
    
    override suspend fun getLoanById(loanId: String): Result<Loan?> {
        return try {
            val entity = loanDao.getLoanById(loanId)
            val loan = entity?.toDomain()
            Result.Success(loan)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get loan")
        }
    }
    
    override fun getLoanByIdFlow(loanId: String): Flow<Loan?> {
        return loanDao.getLoanByIdFlow(loanId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override fun getLoansByUserId(userId: String): Flow<List<Loan>> {
        return loanDao.getLoansByUserId(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getLoansByType(userId: String, loanType: LoanType): Flow<List<Loan>> {
        return loanDao.getLoansByType(userId, loanType).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getActiveLoansWithBalance(userId: String): Flow<List<Loan>> {
        return loanDao.getActiveLoansWithBalance(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun addPayment(payment: LoanPayment): Result<String> {
        return try {
            val entity = LoanPaymentEntity.fromDomain(payment)
            loanPaymentDao.insertPayment(entity)
            
            val currentLoan = loanDao.getLoanById(payment.loanId)
            if (currentLoan != null) {
                val newRemainingAmount = (currentLoan.remainingAmount - payment.principalAmount).coerceAtLeast(0.0)
                loanDao.updateRemainingAmount(payment.loanId, newRemainingAmount)
            }
            
            syncPaymentToFirestore(payment)
            Result.Success(payment.id)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to add payment")
        }
    }
    
    override suspend fun updatePayment(payment: LoanPayment): Result<Unit> {
        return try {
            val entity = LoanPaymentEntity.fromDomain(payment).copy(updatedAt = System.currentTimeMillis())
            loanPaymentDao.updatePayment(entity)
            syncPaymentToFirestore(payment)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update payment")
        }
    }
    
    override suspend fun deletePayment(paymentId: String): Result<Unit> {
        return try {
            val payment = loanPaymentDao.getPaymentById(paymentId)
            if (payment != null) {
                loanPaymentDao.deletePayment(payment)
                
                val currentLoan = loanDao.getLoanById(payment.loanId)
                if (currentLoan != null) {
                    val newRemainingAmount = currentLoan.remainingAmount + payment.principalAmount
                    loanDao.updateRemainingAmount(payment.loanId, newRemainingAmount)
                }
                
                try {
                    firestore.collection(LOANS_COLLECTION)
                        .document(payment.loanId)
                        .collection(PAYMENTS_SUBCOLLECTION)
                        .document(paymentId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    // Handle silently
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete payment")
        }
    }
    
    override suspend fun getPaymentById(paymentId: String): Result<LoanPayment?> {
        return try {
            val entity = loanPaymentDao.getPaymentById(paymentId)
            Result.Success(entity?.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get payment")
        }
    }
    
    override fun getPaymentsByLoanId(loanId: String): Flow<List<LoanPayment>> {
        return loanPaymentDao.getPaymentsByLoanId(loanId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getPaymentsByDateRange(loanId: String, startDate: Date, endDate: Date): Flow<List<LoanPayment>> {
        return loanPaymentDao.getPaymentsByDateRange(loanId, startDate.time, endDate.time).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getExtraPaymentsByLoanId(loanId: String): Flow<List<LoanPayment>> {
        return loanPaymentDao.getExtraPaymentsByLoanId(loanId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getLoanSummary(userId: String): Result<LoanSummary> {
        return try {
            val totalLoans = loanDao.getActiveLoanCount(userId)
            val totalRemainingAmount = loanDao.getTotalRemainingAmount(userId) ?: 0.0
            val totalMonthlyPayments = loanDao.getTotalMonthlyPayments(userId) ?: 0.0
            val averageInterestRate = loanDao.getAverageInterestRate(userId) ?: 0.0
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 1)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val nextPaymentDate = calendar.time
            
            val sixMonthsFromNow = Calendar.getInstance().apply { 
                add(Calendar.MONTH, 6) 
            }.timeInMillis
            val loansNearPayoff = loanDao.getLoansNearPayoff(userId, sixMonthsFromNow).map { it.toDomain() }
            
            val summary = LoanSummary(
                totalLoans = totalLoans,
                totalRemainingAmount = totalRemainingAmount,
                totalMonthlyPayments = totalMonthlyPayments,
                averageInterestRate = averageInterestRate,
                nextPaymentDate = nextPaymentDate,
                nextPaymentAmount = totalMonthlyPayments,
                totalPaidThisMonth = 0.0,
                loansNearPayoff = loansNearPayoff
            )
            
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get loan summary")
        }
    }
    
    override suspend fun getTotalRemainingAmount(userId: String): Result<Double> {
        return try {
            val total = loanDao.getTotalRemainingAmount(userId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total remaining amount")
        }
    }
    
    override suspend fun getTotalMonthlyPayments(userId: String): Result<Double> {
        return try {
            val total = loanDao.getTotalMonthlyPayments(userId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total monthly payments")
        }
    }
    
    override suspend fun getTotalInterestPaid(loanId: String): Result<Double> {
        return try {
            val total = loanPaymentDao.getTotalInterestPaid(loanId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total interest paid")
        }
    }
    
    override suspend fun getTotalPrincipalPaid(loanId: String): Result<Double> {
        return try {
            val total = loanPaymentDao.getTotalPrincipalPaid(loanId) ?: 0.0
            Result.Success(total)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get total principal paid")
        }
    }
    
    override suspend fun getPaymentCount(loanId: String): Result<Int> {
        return try {
            val count = loanPaymentDao.getPaymentCount(loanId)
            Result.Success(count)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get payment count")
        }
    }
    
    override suspend fun getLastPayment(loanId: String): Result<LoanPayment?> {
        return try {
            val entity = loanPaymentDao.getLastPayment(loanId)
            Result.Success(entity?.toDomain())
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get last payment")
        }
    }
    
    override suspend fun updateRemainingAmount(loanId: String, remainingAmount: Double): Result<Unit> {
        return try {
            loanDao.updateRemainingAmount(loanId, remainingAmount)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update remaining amount")
        }
    }
    
    override suspend fun markLoanPaidOff(loanId: String): Result<Unit> {
        return try {
            loanDao.updateRemainingAmount(loanId, 0.0)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to mark loan as paid off")
        }
    }
    
    override fun searchLoans(userId: String, query: String): Flow<List<Loan>> {
        return loanDao.searchLoans(userId, query).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun syncLoansWithFirestore(userId: String): Result<Unit> {
        return try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sync loans")
        }
    }
    
    override suspend fun syncPaymentsWithFirestore(loanId: String): Result<Unit> {
        return try {
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to sync payments")
        }
    }
    
    override suspend fun getUnsyncedLoans(): Result<List<Loan>> {
        return try {
            val entities = loanDao.getUnsyncedLoans()
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get unsynced loans")
        }
    }
    
    override suspend fun getUnsyncedPayments(): Result<List<LoanPayment>> {
        return try {
            val entities = loanPaymentDao.getUnsyncedPayments()
            Result.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to get unsynced payments")
        }
    }
    
    private suspend fun syncLoanToFirestore(loan: Loan) {
        try {
            val loanData = mapOf(
                "id" to loan.id,
                "userId" to loan.userId,
                "name" to loan.name,
                "originalAmount" to loan.originalAmount,
                "remainingAmount" to loan.remainingAmount,
                "interestRate" to loan.interestRate,
                "monthlyPayment" to loan.monthlyPayment,
                "currency" to loan.currency,
                "startDate" to loan.startDate,
                "estimatedPayoffDate" to loan.estimatedPayoffDate,
                "loanType" to loan.loanType.name,
                "lender" to loan.lender,
                "isActive" to loan.isActive,
                "createdAt" to loan.createdAt,
                "updatedAt" to loan.updatedAt,
                "notes" to loan.notes
            )
            
            firestore.collection(LOANS_COLLECTION)
                .document(loan.id)
                .set(loanData)
                .await()
                
            loanDao.updateSyncStatus(loan.id, "SYNCED")
        } catch (e: Exception) {
            loanDao.updateSyncStatus(loan.id, "FAILED")
        }
    }
    
    private suspend fun syncPaymentToFirestore(payment: LoanPayment) {
        try {
            val paymentData = mapOf(
                "id" to payment.id,
                "loanId" to payment.loanId,
                "amount" to payment.amount,
                "principalAmount" to payment.principalAmount,
                "interestAmount" to payment.interestAmount,
                "paymentDate" to payment.paymentDate,
                "paymentMethod" to payment.paymentMethod,
                "notes" to payment.notes,
                "isExtraPayment" to payment.isExtraPayment,
                "exchangeRate" to payment.exchangeRate,
                "amountInUSD" to payment.amountInUSD
            )
            
            firestore.collection(LOANS_COLLECTION)
                .document(payment.loanId)
                .collection(PAYMENTS_SUBCOLLECTION)
                .document(payment.id)
                .set(paymentData)
                .await()
                
            loanPaymentDao.updateSyncStatus(payment.id, "SYNCED")
        } catch (e: Exception) {
            loanPaymentDao.updateSyncStatus(payment.id, "FAILED")
        }
    }
}

