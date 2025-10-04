package com.budgettracker.core.domain.repository

import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.model.LoanPayment
import com.budgettracker.core.domain.model.LoanSummary
import com.budgettracker.core.domain.model.LoanType
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for loan operations
 */
interface LoanRepository {
    
    // Loan CRUD operations
    suspend fun createLoan(loan: Loan): Result<String>
    suspend fun updateLoan(loan: Loan): Result<Unit>
    suspend fun deleteLoan(loanId: String): Result<Unit>
    suspend fun getLoanById(loanId: String): Result<Loan?>
    fun getLoanByIdFlow(loanId: String): Flow<Loan?>
    fun getLoansByUserId(userId: String): Flow<List<Loan>>
    fun getLoansByType(userId: String, loanType: LoanType): Flow<List<Loan>>
    fun getActiveLoansWithBalance(userId: String): Flow<List<Loan>>
    
    // Loan payment operations
    suspend fun addPayment(payment: LoanPayment): Result<String>
    suspend fun updatePayment(payment: LoanPayment): Result<Unit>
    suspend fun deletePayment(paymentId: String): Result<Unit>
    suspend fun getPaymentById(paymentId: String): Result<LoanPayment?>
    fun getPaymentsByLoanId(loanId: String): Flow<List<LoanPayment>>
    fun getPaymentsByDateRange(loanId: String, startDate: Date, endDate: Date): Flow<List<LoanPayment>>
    fun getExtraPaymentsByLoanId(loanId: String): Flow<List<LoanPayment>>
    
    // Loan summary and analytics
    suspend fun getLoanSummary(userId: String): Result<LoanSummary>
    suspend fun getTotalRemainingAmount(userId: String): Result<Double>
    suspend fun getTotalMonthlyPayments(userId: String): Result<Double>
    suspend fun getTotalInterestPaid(loanId: String): Result<Double>
    suspend fun getTotalPrincipalPaid(loanId: String): Result<Double>
    suspend fun getPaymentCount(loanId: String): Result<Int>
    suspend fun getLastPayment(loanId: String): Result<LoanPayment?>
    
    // Loan management
    suspend fun updateRemainingAmount(loanId: String, remainingAmount: Double): Result<Unit>
    suspend fun markLoanPaidOff(loanId: String): Result<Unit>
    fun searchLoans(userId: String, query: String): Flow<List<Loan>>
    
    // Sync operations
    suspend fun syncLoansWithFirestore(userId: String): Result<Unit>
    suspend fun syncPaymentsWithFirestore(loanId: String): Result<Unit>
    suspend fun getUnsyncedLoans(): Result<List<Loan>>
    suspend fun getUnsyncedPayments(): Result<List<LoanPayment>>
}

