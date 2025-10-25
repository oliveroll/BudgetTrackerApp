package com.budgettracker.features.settings.di

import com.budgettracker.core.data.local.database.BudgetTrackerDatabase
import com.budgettracker.features.settings.data.dao.CustomCategoryDao
import com.budgettracker.features.settings.data.dao.EmploymentSettingsDao
import com.budgettracker.features.settings.data.dao.UserSettingsDao
import com.budgettracker.features.settings.data.repository.SettingsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    
    @Provides
    @Singleton
    fun provideUserSettingsDao(database: BudgetTrackerDatabase): UserSettingsDao {
        return database.userSettingsDao()
    }
    
    @Provides
    @Singleton
    fun provideEmploymentSettingsDao(database: BudgetTrackerDatabase): EmploymentSettingsDao {
        return database.employmentSettingsDao()
    }
    
    @Provides
    @Singleton
    fun provideCustomCategoryDao(database: BudgetTrackerDatabase): CustomCategoryDao {
        return database.customCategoryDao()
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(
        userSettingsDao: UserSettingsDao,
        employmentSettingsDao: EmploymentSettingsDao,
        customCategoryDao: CustomCategoryDao,
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): SettingsRepository {
        return SettingsRepository(
            userSettingsDao,
            employmentSettingsDao,
            customCategoryDao,
            firestore,
            auth
        )
    }
}

