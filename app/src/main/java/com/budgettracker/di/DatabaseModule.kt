package com.budgettracker.di

import android.content.Context
import androidx.room.Room
import com.budgettracker.core.data.local.dao.*
import com.budgettracker.core.data.local.database.BudgetTrackerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideBudgetTrackerDatabase(
        @ApplicationContext context: Context
    ): BudgetTrackerDatabase {
        return BudgetTrackerDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideTransactionDao(database: BudgetTrackerDatabase): TransactionDao {
        return database.transactionDao()
    }
    
    @Provides
    fun provideBudgetDao(database: BudgetTrackerDatabase): BudgetDao {
        return database.budgetDao()
    }
    
    @Provides
    fun provideSavingsGoalDao(database: BudgetTrackerDatabase): SavingsGoalDao {
        return database.savingsGoalDao()
    }
    
    @Provides
    fun provideUserProfileDao(database: BudgetTrackerDatabase): UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    fun provideLoanDao(database: BudgetTrackerDatabase): LoanDao {
        return database.loanDao()
    }
    
    @Provides
    fun provideLoanPaymentDao(database: BudgetTrackerDatabase): LoanPaymentDao {
        return database.loanPaymentDao()
    }
}
