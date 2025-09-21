package com.budgettracker.di

import com.budgettracker.core.data.repository.TransactionRepositoryImpl
import com.budgettracker.core.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository
    
    // TODO: Add other repository bindings as they are implemented
    // @Binds
    // @Singleton
    // abstract fun bindBudgetRepository(
    //     budgetRepositoryImpl: BudgetRepositoryImpl
    // ): BudgetRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindSavingsGoalRepository(
    //     savingsGoalRepositoryImpl: SavingsGoalRepositoryImpl
    // ): SavingsGoalRepository
    
    // @Binds
    // @Singleton
    // abstract fun bindUserRepository(
    //     userRepositoryImpl: UserRepositoryImpl
    // ): UserRepository
}

