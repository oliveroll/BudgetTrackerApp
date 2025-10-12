package com.budgettracker.core.data.seeder

import com.budgettracker.core.domain.model.*
import com.budgettracker.core.domain.repository.SubscriptionRepository
import com.budgettracker.core.domain.repository.BillReminderRepository
import com.budgettracker.core.utils.Result
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Seeds sample data for the Budget Overview screen
 */
@Singleton
class BudgetDataSeeder @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val billReminderRepository: BillReminderRepository
) {
    
    suspend fun seedSampleData(userId: String) {
        seedSubscriptions(userId)
        seedBillReminders(userId)
    }
    
    private suspend fun seedSubscriptions(userId: String) {
        val subscriptions = listOf(
            // Spotify subscription
            Subscription(
                userId = userId,
                name = "Spotify Premium",
                cost = 11.99,
                frequency = SubscriptionFrequency.MONTHLY,
                nextBillingDate = createDate(2025, Calendar.NOVEMBER, 20),
                category = "Entertainment",
                reminderEnabled = true,
                reminderDaysBefore = 3,
                notes = "Music streaming service"
            ),
            
            // Phone plan (6-month cycle)
            Subscription(
                userId = userId,
                name = "Phone Plan",
                cost = 150.0,
                frequency = SubscriptionFrequency.SEMI_ANNUAL,
                nextBillingDate = createDate(2025, Calendar.NOVEMBER, 20),
                category = "Utilities",
                reminderEnabled = true,
                reminderDaysBefore = 7,
                notes = "15GB/month data plan - 6 month cycle"
            ),
            
            // Netflix (example)
            Subscription(
                userId = userId,
                name = "Netflix",
                cost = 15.49,
                frequency = SubscriptionFrequency.MONTHLY,
                nextBillingDate = createDate(2025, Calendar.NOVEMBER, 15),
                category = "Entertainment",
                reminderEnabled = false,
                reminderDaysBefore = 3,
                notes = "Video streaming service"
            ),
            
            // Adobe Creative Cloud (example)
            Subscription(
                userId = userId,
                name = "Adobe Creative Cloud",
                cost = 52.99,
                frequency = SubscriptionFrequency.MONTHLY,
                nextBillingDate = createDate(2025, Calendar.NOVEMBER, 10),
                category = "Software",
                reminderEnabled = true,
                reminderDaysBefore = 5,
                notes = "Design software suite"
            ),
            
            // Amazon Prime (yearly)
            Subscription(
                userId = userId,
                name = "Amazon Prime",
                cost = 139.0,
                frequency = SubscriptionFrequency.YEARLY,
                nextBillingDate = createDate(2026, Calendar.MARCH, 15),
                category = "Shopping",
                reminderEnabled = true,
                reminderDaysBefore = 14,
                notes = "Free shipping and Prime Video"
            )
        )
        
        subscriptions.forEach { subscription ->
            subscriptionRepository.createSubscription(subscription)
        }
    }
    
    private suspend fun seedBillReminders(userId: String) {
        val reminders = listOf(
            // Rent payment (monthly on 31st)
            BillReminder(
                userId = userId,
                title = "Rent Payment",
                amount = 708.95, // $700 + $8.95 fee
                dueDate = createDate(2025, Calendar.OCTOBER, 31),
                category = "Housing",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "MONTHLY",
                reminderDate = createDate(2025, Calendar.OCTOBER, 28), // 3 days before
                notes = "Monthly rent payment including processing fee"
            ),
            
            // Utilities
            BillReminder(
                userId = userId,
                title = "Electric Bill",
                amount = 85.0,
                dueDate = createDate(2025, Calendar.NOVEMBER, 5),
                category = "Utilities",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "MONTHLY",
                reminderDate = createDate(2025, Calendar.NOVEMBER, 2),
                notes = "Monthly electricity bill"
            ),
            
            // Internet
            BillReminder(
                userId = userId,
                title = "Internet Service",
                amount = 79.99,
                dueDate = createDate(2025, Calendar.NOVEMBER, 12),
                category = "Utilities",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "MONTHLY",
                reminderDate = createDate(2025, Calendar.NOVEMBER, 9),
                notes = "High-speed internet service"
            ),
            
            // Car Insurance (quarterly)
            BillReminder(
                userId = userId,
                title = "Car Insurance",
                amount = 485.0,
                dueDate = createDate(2025, Calendar.DECEMBER, 1),
                category = "Insurance",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "QUARTERLY",
                reminderDate = createDate(2025, Calendar.NOVEMBER, 25),
                notes = "Quarterly auto insurance premium"
            ),
            
            // Credit Card Payment
            BillReminder(
                userId = userId,
                title = "Credit Card Payment",
                amount = 250.0,
                dueDate = createDate(2025, Calendar.NOVEMBER, 8),
                category = "Credit",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "MONTHLY",
                reminderDate = createDate(2025, Calendar.NOVEMBER, 5),
                notes = "Monthly credit card payment"
            ),
            
            // German Student Loan (starting Dec 2025)
            BillReminder(
                userId = userId,
                title = "German Student Loan",
                amount = 900.0,
                dueDate = createDate(2025, Calendar.DECEMBER, 1),
                category = "Debt",
                status = ReminderStatus.UPCOMING,
                isRecurring = true,
                recurringFrequency = "MONTHLY",
                reminderDate = createDate(2025, Calendar.NOVEMBER, 28),
                notes = "Accelerated monthly payment - €900 (Freedom by Nov 2026!)"
            )
        )
        
        reminders.forEach { reminder ->
            billReminderRepository.createReminder(reminder)
        }
    }
    
    private fun createDate(year: Int, month: Int, day: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
