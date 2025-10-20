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
    
    @Provides
    fun provideBudgetOverviewDao(database: BudgetTrackerDatabase): BudgetOverviewDao {
        return database.budgetOverviewDao()
    }
    
    @Provides
    fun provideSubscriptionDao(database: BudgetTrackerDatabase): SubscriptionDao {
        return database.subscriptionDao()
    }
    
    @Provides
    fun provideBillReminderDao(database: BudgetTrackerDatabase): BillReminderDao {
        return database.billReminderDao()
    }
    
    // Financial Goals DAOs
    @Provides
    fun provideDebtLoanDao(database: BudgetTrackerDatabase): DebtLoanDao {
        return database.debtLoanDao()
    }
    
    @Provides
    fun provideRothIRADao(database: BudgetTrackerDatabase): RothIRADao {
        return database.rothIRADao()
    }
    
    @Provides
    fun provideEmergencyFundDao(database: BudgetTrackerDatabase): EmergencyFundDao {
        return database.emergencyFundDao()
    }
    
    @Provides
    fun provideETFPortfolioDao(database: BudgetTrackerDatabase): ETFPortfolioDao {
        return database.etfPortfolioDao()
    }
}

