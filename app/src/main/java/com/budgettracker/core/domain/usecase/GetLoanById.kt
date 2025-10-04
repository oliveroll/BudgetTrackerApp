package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.Loan
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get a loan by ID
 */
class GetLoanById @Inject constructor(
    private val loanRepository: LoanRepository
) {
    
    suspend operator fun invoke(loanId: String): Result<Loan?> {
        return try {
            loanRepository.getLoanById(loanId)
        } catch (e: Exception) {
            Result.Error("Failed to get loan: ${e.message}")
        }
    }
    
    fun asFlow(loanId: String): Flow<Loan?> {
        return loanRepository.getLoanByIdFlow(loanId)
    }
}
