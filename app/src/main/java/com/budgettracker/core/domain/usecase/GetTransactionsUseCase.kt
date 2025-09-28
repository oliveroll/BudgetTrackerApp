package com.budgettracker.core.domain.usecase

import com.budgettracker.core.domain.model.Transaction
import com.budgettracker.core.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving transactions
 */
class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(userId: String): Flow<List<Transaction>> {
        return repository.getAllTransactions(userId)
    }
}




