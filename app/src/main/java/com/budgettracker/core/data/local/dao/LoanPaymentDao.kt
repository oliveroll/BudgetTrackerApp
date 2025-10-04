package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.LoanPaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for LoanPayment operations
 */
@Dao
interface LoanPaymentDao {
    
    @Query("SELECT * FROM loan_payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<LoanPaymentEntity>>
    
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY paymentDate DESC")
    fun getPaymentsByLoanId(loanId: String): Flow<List<LoanPaymentEntity>>
    
    @Query("SELECT * FROM loan_payments WHERE id = :id")
    suspend fun getPaymentById(id: String): LoanPaymentEntity?
    
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId AND paymentDate >= :startDate AND paymentDate <= :endDate ORDER BY paymentDate DESC")
    fun getPaymentsByDateRange(loanId: String, startDate: Long, endDate: Long): Flow<List<LoanPaymentEntity>>
    
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId AND isExtraPayment = 1 ORDER BY paymentDate DESC")
    fun getExtraPaymentsByLoanId(loanId: String): Flow<List<LoanPaymentEntity>>
    
    @Query("SELECT SUM(amount) FROM loan_payments WHERE loanId = :loanId")
    suspend fun getTotalPaidAmount(loanId: String): Double?
    
    @Query("SELECT SUM(principalAmount) FROM loan_payments WHERE loanId = :loanId")
    suspend fun getTotalPrincipalPaid(loanId: String): Double?
    
    @Query("SELECT SUM(interestAmount) FROM loan_payments WHERE loanId = :loanId")
    suspend fun getTotalInterestPaid(loanId: String): Double?
    
    @Query("SELECT COUNT(*) FROM loan_payments WHERE loanId = :loanId")
    suspend fun getPaymentCount(loanId: String): Int
    
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY paymentDate DESC LIMIT 1")
    suspend fun getLastPayment(loanId: String): LoanPaymentEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: LoanPaymentEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<LoanPaymentEntity>)
    
    @Update
    suspend fun updatePayment(payment: LoanPaymentEntity)
    
    @Delete
    suspend fun deletePayment(payment: LoanPaymentEntity)
    
    @Query("DELETE FROM loan_payments WHERE loanId = :loanId")
    suspend fun deleteAllPaymentsForLoan(loanId: String)
    
    @Query("SELECT * FROM loan_payments WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedPayments(): List<LoanPaymentEntity>
    
    @Query("UPDATE loan_payments SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
}

