package com.budgettracker.core.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.budgettracker.core.data.local.converters.Converters
import com.budgettracker.core.data.local.dao.*
import com.budgettracker.core.data.local.entities.*

/**
 * Room database for Budget Tracker app
 */
@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        UserProfileEntity::class,
        LoanEntity::class,
        LoanPaymentEntity::class,
        BudgetOverviewEntity::class,
        SubscriptionEntity::class,
        BillReminderEntity::class,
        // New enhanced entities
        EnhancedSubscriptionEntity::class,
        EssentialExpenseEntity::class,
        BalanceEntity::class,
        PaycheckEntity::class,
        ReminderEntity::class,
        DeviceEntity::class,
        // Financial Goals entities
        DebtLoanEntity::class,
        DebtPaymentRecordEntity::class,
        RothIRAEntity::class,
        IRAContributionEntity::class,
        EmergencyFundEntity::class,
        EmergencyFundDepositEntity::class,
        ETFPortfolioEntity::class,
        ETFHoldingEntity::class,
        InvestmentTransactionEntity::class,
        // Settings entities
        com.budgettracker.features.settings.data.models.UserSettings::class,
        com.budgettracker.features.settings.data.models.EmploymentSettings::class,
        com.budgettracker.features.settings.data.models.CustomCategory::class
    ],
    version = 10, // Add isOnboardingCompleted to UserSettings
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BudgetTrackerDatabase : RoomDatabase() {
    
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun loanDao(): LoanDao
    abstract fun loanPaymentDao(): LoanPaymentDao
    abstract fun budgetOverviewDao(): BudgetOverviewDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun billReminderDao(): BillReminderDao
    
    // Financial Goals DAOs
    abstract fun debtLoanDao(): DebtLoanDao
    abstract fun rothIRADao(): RothIRADao
    abstract fun emergencyFundDao(): EmergencyFundDao
    abstract fun etfPortfolioDao(): ETFPortfolioDao
    
    // Settings DAOs
    abstract fun userSettingsDao(): com.budgettracker.features.settings.data.dao.UserSettingsDao
    abstract fun employmentSettingsDao(): com.budgettracker.features.settings.data.dao.EmploymentSettingsDao
    abstract fun customCategoryDao(): com.budgettracker.features.settings.data.dao.CustomCategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: BudgetTrackerDatabase? = null
        
        private const val DATABASE_NAME = "budget_tracker_database"
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `loans` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `originalAmount` REAL NOT NULL,
                        `remainingAmount` REAL NOT NULL,
                        `interestRate` REAL NOT NULL,
                        `monthlyPayment` REAL NOT NULL,
                        `currency` TEXT NOT NULL,
                        `startDate` INTEGER NOT NULL,
                        `estimatedPayoffDate` INTEGER NOT NULL,
                        `loanType` TEXT NOT NULL,
                        `lender` TEXT NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL DEFAULT 'PENDING',
                        PRIMARY KEY(`id`)
                    )
                """)
                
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `loan_payments` (
                        `id` TEXT NOT NULL,
                        `loanId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `principalAmount` REAL NOT NULL,
                        `interestAmount` REAL NOT NULL,
                        `paymentDate` INTEGER NOT NULL,
                        `paymentMethod` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `isExtraPayment` INTEGER NOT NULL,
                        `exchangeRate` REAL NOT NULL,
                        `amountInUSD` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL DEFAULT 'PENDING',
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`loanId`) REFERENCES `loans`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_payments_loanId` ON `loan_payments` (`loanId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_loan_payments_paymentDate` ON `loan_payments` (`paymentDate`)")
            }
        }
        
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create enhanced_subscriptions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `enhanced_subscriptions` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `currency` TEXT NOT NULL DEFAULT 'USD',
                        `frequency` TEXT NOT NULL,
                        `nextBillingDate` INTEGER NOT NULL,
                        `reminderDaysBefore` TEXT NOT NULL,
                        `fcmReminderEnabled` INTEGER NOT NULL DEFAULT 1,
                        `active` INTEGER NOT NULL DEFAULT 1,
                        `iconEmoji` TEXT,
                        `category` TEXT NOT NULL DEFAULT 'Entertainment',
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Create essential_expenses table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `essential_expenses` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `plannedAmount` REAL NOT NULL,
                        `actualAmount` REAL,
                        `dueDay` INTEGER,
                        `paid` INTEGER NOT NULL DEFAULT 0,
                        `period` TEXT NOT NULL,
                        `reminderDaysBefore` TEXT,
                        `fcmReminderEnabled` INTEGER NOT NULL DEFAULT 1,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Create balance table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `balance` (
                        `userId` TEXT NOT NULL,
                        `currentBalance` REAL NOT NULL,
                        `lastUpdatedBy` TEXT NOT NULL DEFAULT 'user',
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`userId`)
                    )
                """)
                
                // Create paychecks table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `paychecks` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `grossAmount` REAL NOT NULL,
                        `netAmount` REAL NOT NULL,
                        `deposited` INTEGER NOT NULL DEFAULT 0,
                        `depositedAt` INTEGER,
                        `payPeriodStart` INTEGER,
                        `payPeriodEnd` INTEGER,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Create fcm_reminders table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fcm_reminders` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `targetId` TEXT NOT NULL,
                        `fireAtUtc` INTEGER NOT NULL,
                        `title` TEXT NOT NULL,
                        `message` TEXT NOT NULL,
                        `sent` INTEGER NOT NULL DEFAULT 0,
                        `sentAt` INTEGER,
                        `fcmMessageId` TEXT,
                        `recurringRule` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Create fcm_devices table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `fcm_devices` (
                        `deviceId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `fcmToken` TEXT NOT NULL,
                        `platform` TEXT NOT NULL DEFAULT 'android',
                        `appVersion` TEXT NOT NULL,
                        `notificationsEnabled` INTEGER NOT NULL DEFAULT 1,
                        `lastSeen` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`deviceId`)
                    )
                """)
            }
        }
        
        /**
         * Migration from version 5 to 6: Add isAutoPay field to subscriptions
         */
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isAutoPay column to enhanced_subscriptions table
                database.execSQL("""
                    ALTER TABLE `enhanced_subscriptions` 
                    ADD COLUMN `isAutoPay` INTEGER NOT NULL DEFAULT 1
                """)
            }
        }
        
        /**
         * Migration from version 6 to 7: Add Settings tables
         */
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create user_settings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `user_settings` (
                        `userId` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `displayName` TEXT,
                        `photoUrl` TEXT,
                        `lowBalanceAlertEnabled` INTEGER NOT NULL DEFAULT 1,
                        `lowBalanceThreshold` REAL NOT NULL DEFAULT 100.0,
                        `lowBalanceFrequency` TEXT NOT NULL DEFAULT 'DAILY',
                        `upcomingBillReminderEnabled` INTEGER NOT NULL DEFAULT 1,
                        `upcomingBillDaysBefore` INTEGER NOT NULL DEFAULT 3,
                        `upcomingBillFrequency` TEXT NOT NULL DEFAULT 'DAILY',
                        `subscriptionRenewalEnabled` INTEGER NOT NULL DEFAULT 1,
                        `subscriptionRenewalDaysBefore` INTEGER NOT NULL DEFAULT 3,
                        `subscriptionRenewalFrequency` TEXT NOT NULL DEFAULT 'DAILY',
                        `goalMilestoneEnabled` INTEGER NOT NULL DEFAULT 1,
                        `goalMilestoneFrequency` TEXT NOT NULL DEFAULT 'WEEKLY',
                        `notificationPermissionGranted` INTEGER NOT NULL DEFAULT 0,
                        `currency` TEXT NOT NULL DEFAULT 'USD',
                        `currencySymbol` TEXT NOT NULL DEFAULT '$',
                        `themeMode` TEXT NOT NULL DEFAULT 'SYSTEM',
                        `biometricEnabled` INTEGER NOT NULL DEFAULT 0,
                        `lastSyncedAt` INTEGER,
                        `updatedAt` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`userId`)
                    )
                """)
                
                // Create employment_settings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `employment_settings` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `optStatus` TEXT,
                        `employmentType` TEXT NOT NULL DEFAULT 'FULL_TIME',
                        `employer` TEXT,
                        `jobTitle` TEXT,
                        `annualSalary` REAL NOT NULL DEFAULT 0.0,
                        `payFrequency` TEXT NOT NULL DEFAULT 'BI_WEEKLY',
                        `nextPayDate` INTEGER,
                        `isActive` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `lastSyncedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Create custom_categories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `custom_categories` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL DEFAULT '#FF6B6B',
                        `iconName` TEXT NOT NULL DEFAULT 'category',
                        `budgetedAmount` REAL,
                        `budgetPeriod` TEXT NOT NULL DEFAULT 'MONTHLY',
                        `isArchived` INTEGER NOT NULL DEFAULT 0,
                        `isSystem` INTEGER NOT NULL DEFAULT 0,
                        `transactionCount` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `lastSyncedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )
                """)
            }
        }
        
        /**
         * Migration from version 7 to 8: Add unique constraint on essential_expenses
         * to prevent duplicate categories per user per period
         */
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Create a new table with the unique constraint
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `essential_expenses_new` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `plannedAmount` REAL NOT NULL,
                        `actualAmount` REAL,
                        `dueDay` INTEGER,
                        `paid` INTEGER NOT NULL DEFAULT 0,
                        `period` TEXT NOT NULL,
                        `reminderDaysBefore` TEXT,
                        `fcmReminderEnabled` INTEGER NOT NULL DEFAULT 1,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `syncedAt` INTEGER,
                        `pendingSync` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Step 2: Copy data, removing duplicates (keep most recent by createdAt)
                database.execSQL("""
                    INSERT INTO essential_expenses_new 
                    SELECT * FROM essential_expenses e1
                    WHERE e1.id = (
                        SELECT e2.id FROM essential_expenses e2
                        WHERE e2.userId = e1.userId 
                        AND e2.category = e1.category 
                        AND e2.period = e1.period
                        ORDER BY e2.createdAt DESC
                        LIMIT 1
                    )
                """)
                
                // Step 3: Drop old table
                database.execSQL("DROP TABLE essential_expenses")
                
                // Step 4: Rename new table to original name
                database.execSQL("ALTER TABLE essential_expenses_new RENAME TO essential_expenses")
                
                // Step 5: Create unique index
                database.execSQL("""
                    CREATE UNIQUE INDEX IF NOT EXISTS 
                    `index_essential_expenses_unique_category_period` 
                    ON `essential_expenses` (`userId`, `category`, `period`)
                """)
                
                android.util.Log.d("Migration", "Successfully migrated to version 8: Added unique constraint to essential_expenses")
            }
        }
        
        /**
         * Migration from version 8 to 9: Fix date picker timezone bug
         * Convert transaction dates from Long (millis since epoch) to LocalDate (TEXT ISO format)
         * 
         * PROBLEM: Selecting "Nov 24" saved as "Nov 23" due to timezone conversion
         * SOLUTION: Use LocalDate (yyyy-MM-dd) with no time or timezone
         */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Step 1: Create new transactions table with LocalDate (TEXT) for date column
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `transactions_new` (
                        `id` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `category` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `isRecurring` INTEGER NOT NULL,
                        `recurringPeriod` TEXT,
                        `tags` TEXT NOT NULL,
                        `attachmentUrl` TEXT,
                        `location` TEXT,
                        `notes` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `isDeleted` INTEGER NOT NULL,
                        `syncStatus` TEXT NOT NULL DEFAULT 'PENDING',
                        PRIMARY KEY(`id`)
                    )
                """)
                
                // Step 2: Copy data, converting Long millis to LocalDate ISO string (yyyy-MM-dd)
                database.execSQL("""
                    INSERT INTO transactions_new 
                    SELECT 
                        id,
                        userId,
                        amount,
                        category,
                        type,
                        description,
                        date(date / 1000, 'unixepoch') as date,
                        isRecurring,
                        recurringPeriod,
                        tags,
                        attachmentUrl,
                        location,
                        notes,
                        createdAt,
                        updatedAt,
                        isDeleted,
                        syncStatus
                    FROM transactions
                """)
                
                // Step 3: Drop old table
                database.execSQL("DROP TABLE transactions")
                
                // Step 4: Rename new table to original name
                database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                
                android.util.Log.d("Migration", "Successfully migrated to version 9: Fixed date picker timezone bug (Long → LocalDate)")
            }
        }
        
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add isOnboardingCompleted column to user_settings table
                database.execSQL("""
                    ALTER TABLE user_settings 
                    ADD COLUMN isOnboardingCompleted INTEGER NOT NULL DEFAULT 0
                """)
                
                android.util.Log.d("Migration", "Successfully migrated to version 10: Added isOnboardingCompleted to UserSettings")
            }
        }
        
        fun getDatabase(context: Context): BudgetTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BudgetTrackerDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_3_4, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10) // Add migrations
                .fallbackToDestructiveMigration() // Allow database recreation on schema changes (dev mode)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

