package com.budgettracker.core.data.local.dao

import androidx.room.*
import com.budgettracker.core.data.local.entities.LoanEntity
import com.budgettracker.core.domain.model.LoanType
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Loan operations
 */
@Dao
interface LoanDao {
    
    @Query("SELECT * FROM loans WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveLoans(): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getLoansByUserId(userId: String): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun getLoanById(id: String): LoanEntity?
    
    @Query("SELECT * FROM loans WHERE id = :id")
    fun getLoanByIdFlow(id: String): Flow<LoanEntity?>
    
    @Query("SELECT * FROM loans WHERE userId = :userId AND loanType = :loanType AND isActive = 1 ORDER BY createdAt DESC")
    fun getLoansByType(userId: String, loanType: LoanType): Flow<List<LoanEntity>>
    
    @Query("SELECT * FROM loans WHERE userId = :userId AND remainingAmount > 0 AND isActive = 1 ORDER BY estimatedPayoffDate ASC")
    fun getActiveLoansWithBalance(userId: String): Flow<List<LoanEntity>>
    
    @Query("SELECT SUM(remainingAmount) FROM loans WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalRemainingAmount(userId: String): Double?
    
    @Query("SELECT SUM(monthlyPayment) FROM loans WHERE userId = :userId AND isActive = 1")
    suspend fun getTotalMonthlyPayments(userId: String): Double?
    
    @Query("SELECT AVG(interestRate) FROM loans WHERE userId = :userId AND isActive = 1")
    suspend fun getAverageInterestRate(userId: String): Double?
    
    @Query("SELECT COUNT(*) FROM loans WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveLoanCount(userId: String): Int
    
    @Query("SELECT * FROM loans WHERE userId = :userId AND (name LIKE '%' || :searchQuery || '%' OR lender LIKE '%' || :searchQuery || '%') AND isActive = 1 ORDER BY createdAt DESC")
    fun searchLoans(userId: String, searchQuery: String): Flow<List<LoanEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoans(loans: List<LoanEntity>)
    
    @Update
    suspend fun updateLoan(loan: LoanEntity)
    
    @Query("UPDATE loans SET remainingAmount = :remainingAmount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRemainingAmount(id: String, remainingAmount: Double, updatedAt: Long = System.currentTimeMillis())
    
    @Query("UPDATE loans SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markLoanInactive(id: String, updatedAt: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deleteLoan(loan: LoanEntity)
    
    @Query("DELETE FROM loans WHERE userId = :userId")
    suspend fun deleteAllLoansForUser(userId: String)
    
    @Query("SELECT * FROM loans WHERE syncStatus != 'SYNCED' ORDER BY updatedAt ASC")
    suspend fun getUnsyncedLoans(): List<LoanEntity>
    
    @Query("UPDATE loans SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)
    
    @Query("SELECT * FROM loans WHERE userId = :userId AND isActive = 1 AND estimatedPayoffDate <= :sixMonthsFromNow ORDER BY estimatedPayoffDate ASC")
    suspend fun getLoansNearPayoff(userId: String, sixMonthsFromNow: Long): List<LoanEntity>
}

