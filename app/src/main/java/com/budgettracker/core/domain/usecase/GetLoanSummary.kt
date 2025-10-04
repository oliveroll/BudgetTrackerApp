package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.LoanSummary
import com.budgettracker.core.domain.repository.LoanRepository
import com.budgettracker.core.utils.Result
import javax.inject.Inject

/**
 * Use case to get loan summary for dashboard
 */
class GetLoanSummary @Inject constructor(
    private val loanRepository: LoanRepository
) {
    
    suspend operator fun invoke(userId: String): Result<LoanSummary> {
        return try {
            loanRepository.getLoanSummary(userId)
        } catch (e: Exception) {
            Result.Error("Failed to get loan summary: ${e.message}")
        }
    }
}
