package com.budgettracker.core.data.repository

import com.budgettracker.core.data.local.dao.BudgetOverviewDao
import com.budgettracker.core.data.local.dao.DashboardSummary
import com.budgettracker.core.data.local.dao.TimelineItem
import com.budgettracker.core.data.local.entities.*
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.domain.model.*
import com.budgettracker.core.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data class to hold essential expense with actual spending
 */
data class EssentialExpenseWithSpending(
    val expense: EssentialExpenseEntity,
    val actualSpending: Double,
    val remainingBudget: Double
) {
    val spendingPercentage: Float
        get() = if (expense.plannedAmount > 0) {
            ((actualSpending / expense.plannedAmount) * 100).toFloat()
        } else 0f
    
    val isOverBudget: Boolean
        get() = actualSpending > expense.plannedAmount
}

/**
 * Repository for Budget Overview functionality with offline-first approach
 * Integrates with existing Firebase setup and Room database
 */
@Singleton
class BudgetOverviewRepository @Inject constructor(
    private val dao: BudgetOverviewDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    // Balance operations
    suspend fun getCurrentBalance(): Result<Double> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val balanceEntity = dao.getBalance(userId)
            
            // Initialize balance if it doesn't exist
            if (balanceEntity == null) {
                // Initialize with monthly income or a default value
                val initialBalance = 5191.32 // Monthly income default
                initializeBalance(initialBalance)
                Result.Success(initialBalance)
            } else {
                Result.Success(balanceEntity.currentBalance)
            }
        } catch (e: Exception) {
            Result.Error("Failed to get balance: ${e.message}")
        }
    }
    
    suspend fun initializeBalance(initialAmount: Double = 5191.32): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Check if balance already exists
            val existing = dao.getBalance(userId)
            if (existing == null) {
                updateBalance(initialAmount, "system_init")
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to initialize balance: ${e.message}")
        }
    }
    
    fun getBalanceFlow(): Flow<Double> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(0.0)
        return dao.getBalanceFlow(userId).map { it?.currentBalance ?: 0.0 }
    }
    
    suspend fun updateBalance(newBalance: Double, updatedBy: String = "user"): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Update local database
            val balanceEntity = BalanceEntity(
                userId = userId,
                currentBalance = newBalance,
                lastUpdatedBy = updatedBy,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            dao.insertBalance(balanceEntity)
            
            // Sync to Firebase in background
            syncBalanceToFirebase(balanceEntity)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update balance: ${e.message}")
        }
    }
    
    private suspend fun syncBalanceToFirebase(balance: BalanceEntity) {
        try {
            firestore.collection("users")
                .document(balance.userId)
                .collection("balance")
                .document("current")
                .set(
                    mapOf(
                        "currentBalance" to balance.currentBalance,
                        "lastUpdatedBy" to balance.lastUpdatedBy,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
            
            // Mark as synced
            dao.updateBalance(balance.copy(syncedAt = System.currentTimeMillis(), pendingSync = false))
        } catch (e: Exception) {
            // Keep as pending sync for retry
        }
    }
    
    // Essential expenses operations
    suspend fun getEssentialExpenses(period: String? = null): Result<List<EssentialExpenseEntity>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val currentPeriod = period ?: getCurrentPeriod()
            
            // Auto-create fixed expenses for new period if they don't exist
            ensureFixedExpensesForPeriod(userId, currentPeriod)
            
            val expenses = dao.getEssentialExpenses(userId, currentPeriod)
            Result.Success(expenses)
        } catch (e: Exception) {
            Result.Error("Failed to get essential expenses: ${e.message}")
        }
    }
    
    /**
     * Automatically create fixed expenses for a new period based on previous month's fixed expenses
     */
    private suspend fun ensureFixedExpensesForPeriod(userId: String, targetPeriod: String) {
        try {
            // Check if expenses already exist for this period
            val existingExpenses = dao.getEssentialExpenses(userId, targetPeriod)
            if (existingExpenses.isNotEmpty()) {
                return // Already has expenses for this period
            }
            
            // Get previous month's period
            val (year, month) = targetPeriod.split("-").let {
                it[0].toInt() to it[1].toInt()
            }
            val previousMonthCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 2) // month is 1-indexed, Calendar.MONTH is 0-indexed
            }
            val previousPeriod = "${previousMonthCal.get(Calendar.YEAR)}-${(previousMonthCal.get(Calendar.MONTH) + 1).toString().padStart(2, '0')}"
            
            // Get fixed expenses from previous month (expenses with dueDay set)
            val previousExpenses = dao.getEssentialExpenses(userId, previousPeriod)
            val fixedExpenses = previousExpenses.filter { it.dueDay != null }
            
            if (fixedExpenses.isEmpty()) {
                return // No fixed expenses to copy
            }
            
            // Create new instances for current period
            fixedExpenses.forEach { template ->
                val newExpense = template.copy(
                    id = UUID.randomUUID().toString(),
                    period = targetPeriod,
                    paid = false, // Reset paid status
                    actualAmount = null, // Clear actual amount
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    syncedAt = null,
                    pendingSync = true
                )
                
                // Insert into local database
                dao.insertEssentialExpense(newExpense)
                
                // Sync to Firebase
                try {
                    firestore.collection("users")
                        .document(userId)
                        .collection("essentials")
                        .document(newExpense.id)
                        .set(newExpense.toFirestoreMap())
                        .await()
                } catch (e: Exception) {
                    // Keep as pending sync for retry
                }
            }
        } catch (e: Exception) {
            // Don't throw - allow getting expenses to continue even if auto-creation fails
        }
    }
    
    fun getEssentialExpensesFlow(period: String? = null): Flow<List<EssentialExpenseEntity>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        val currentPeriod = period ?: getCurrentPeriod()
        return dao.getEssentialExpensesFlow(userId, currentPeriod)
    }
    
    /**
     * Get essential expenses with actual spending from transactions
     */
    suspend fun getEssentialExpensesWithSpending(period: String? = null): Result<List<EssentialExpenseWithSpending>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val currentPeriod = period ?: getCurrentPeriod()
            
            // Auto-create fixed expenses for new period if they don't exist
            ensureFixedExpensesForPeriod(userId, currentPeriod)
            
            val expenses = dao.getEssentialExpenses(userId, currentPeriod)
            
            // Parse period to get month and year
            val (year, month) = currentPeriod.split("-").let {
                it[0].toInt() to it[1].toInt() - 1 // Calendar months are 0-indexed
            }
            
            // Get all transactions for the period
            val transactions = TransactionDataStore.getTransactionsForMonth(month, year)
            
            // Calculate actual spending for each expense
            val expensesWithSpending = expenses.map { expense ->
                val transactionCategory = expense.category.toTransactionCategory()
                val actualSpending = transactions
                    .filter { it.type == TransactionType.EXPENSE && it.category == transactionCategory }
                    .sumOf { it.amount }
                
                val remaining = expense.plannedAmount - actualSpending
                
                EssentialExpenseWithSpending(
                    expense = expense,
                    actualSpending = actualSpending,
                    remainingBudget = remaining
                )
            }
            
            Result.Success(expensesWithSpending)
        } catch (e: Exception) {
            Result.Error("Failed to get essential expenses with spending: ${e.message}")
        }
    }
    
    suspend fun addEssentialExpense(
        name: String,
        category: ExpenseCategory,
        plannedAmount: Double,
        dueDay: Int? = null,
        reminderDaysBefore: List<Int>? = listOf(3, 1)
    ): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            val expense = EssentialExpenseEntity(
                userId = userId,
                name = name,
                category = category,
                plannedAmount = plannedAmount,
                dueDay = dueDay,
                period = getCurrentPeriod(),
                reminderDaysBefore = reminderDaysBefore,
                pendingSync = true
            )
            
            dao.insertEssentialExpense(expense)
            
            // Sync to Firestore
            firestore.collection("users")
                .document(userId)
                .collection("essentials")
                .document(expense.id)
                .set(expense.toFirestoreMap())
                .await()
            
            Result.Success(expense.id)
        } catch (e: Exception) {
            Result.Error("Failed to add essential expense: ${e.message}")
        }
    }
    
    suspend fun deleteEssentialExpense(expenseId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Delete from local database
            dao.deleteEssentialExpense(expenseId)
            
            // Delete from Firestore
            firestore.collection("users")
                .document(userId)
                .collection("essentials")
                .document(expenseId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete essential expense: ${e.message}")
        }
    }
    
    suspend fun updateEssentialExpense(
        expenseId: String,
        name: String,
        category: ExpenseCategory,
        plannedAmount: Double,
        dueDay: Int? // This can be explicitly null to unmark as fixed
    ): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Get existing expense
            val existingExpense = dao.getEssentialExpenseById(expenseId)
                ?: return Result.Error("Expense not found")
            
            // Update all fields (dueDay can be explicitly set to null to remove fixed status)
            val updatedExpense = existingExpense.copy(
                name = name,
                category = category,
                plannedAmount = plannedAmount,
                dueDay = dueDay, // Explicitly set, can be null
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            
            dao.updateEssentialExpense(updatedExpense)
            
            // Sync to Firestore
            firestore.collection("users")
                .document(userId)
                .collection("essentials")
                .document(expenseId)
                .set(updatedExpense.toFirestoreMap())
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update essential expense: ${e.message}")
        }
    }
    
    // Helper method to add Firestore mapping for EssentialExpenseEntity
    private fun EssentialExpenseEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "category" to category.name,
        "plannedAmount" to plannedAmount,
        "actualAmount" to actualAmount,
        "dueDay" to dueDay,
        "paid" to paid,
        "period" to period,
        "reminderDaysBefore" to reminderDaysBefore,
        "fcmReminderEnabled" to fcmReminderEnabled,
        "notes" to notes,
        "createdAt" to Timestamp(Date(createdAt)),
        "updatedAt" to Timestamp(Date(updatedAt))
    )
    
    suspend fun markEssentialPaid(expenseId: String, actualAmount: Double? = null): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val expense = dao.getEssentialExpenses(userId, getCurrentPeriod())
                .find { it.id == expenseId }
                ?: return Result.Error("Expense not found")
            
            // IDEMPOTENCY CHECK: If already paid, return success without updating balance
            if (expense.paid) {
                return Result.Success(Unit) // Already paid, don't deduct balance again
            }
            
            val amountToDeduct = actualAmount ?: expense.plannedAmount
            
            val updatedExpense = expense.copy(
                paid = true,
                actualAmount = amountToDeduct,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            
            dao.updateEssentialExpense(updatedExpense)
            
            // Update balance - only if not already paid
            val currentBalance = dao.getBalance(userId)?.currentBalance ?: 0.0
            val newBalance = currentBalance - amountToDeduct
            updateBalance(newBalance, "expense")
            
            // Sync to Firebase
            syncEssentialToFirebase(updatedExpense)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to mark expense as paid: ${e.message}")
        }
    }
    
    // Subscriptions operations
    suspend fun getActiveSubscriptions(): Result<List<EnhancedSubscriptionEntity>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val subscriptions = dao.getActiveSubscriptions(userId)
            Result.Success(subscriptions)
        } catch (e: Exception) {
            Result.Error("Failed to get subscriptions: ${e.message}")
        }
    }
    
    fun getActiveSubscriptionsFlow(): Flow<List<EnhancedSubscriptionEntity>> {
        val userId = currentUserId ?: return kotlinx.coroutines.flow.flowOf(emptyList())
        return dao.getActiveSubscriptionsFlow(userId)
    }
    
    suspend fun addSubscription(
        name: String,
        amount: Double,
        frequency: BillingFrequency,
        nextBillingDate: Long,
        category: String = "Entertainment",
        reminderDaysBefore: List<Int> = listOf(1, 3, 7),
        iconEmoji: String? = null
    ): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            val subscription = EnhancedSubscriptionEntity(
                userId = userId,
                name = name,
                amount = amount,
                frequency = frequency,
                nextBillingDate = nextBillingDate,
                category = category,
                reminderDaysBefore = reminderDaysBefore,
                iconEmoji = iconEmoji,
                pendingSync = true
            )
            
            dao.insertSubscription(subscription)
            
            // Create FCM reminders
            createRemindersForSubscription(subscription)
            
            // Sync to Firebase
            syncSubscriptionToFirebase(subscription)
            
            Result.Success(subscription.id)
        } catch (e: Exception) {
            Result.Error("Failed to add subscription: ${e.message}")
        }
    }
    
    suspend fun updateSubscription(
        subscriptionId: String,
        name: String,
        amount: Double,
        frequency: BillingFrequency,
        nextBillingDate: Long,
        iconEmoji: String?
    ): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val subscriptions = dao.getActiveSubscriptions(userId)
            val existing = subscriptions.find { it.id == subscriptionId }
                ?: return Result.Error("Subscription not found")
            
            val updated = existing.copy(
                name = name,
                amount = amount,
                frequency = frequency,
                nextBillingDate = nextBillingDate,
                iconEmoji = iconEmoji,
                updatedAt = System.currentTimeMillis(),
                pendingSync = true
            )
            
            dao.updateSubscription(updated)
            
            // Sync to Firebase
            syncSubscriptionToFirebase(updated)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to update subscription: ${e.message}")
        }
    }
    
    suspend fun deleteSubscription(subscriptionId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val subscriptions = dao.getActiveSubscriptions(userId)
            val subscription = subscriptions.find { it.id == subscriptionId }
                ?: return Result.Error("Subscription not found")
            
            // Delete from local database
            dao.deleteSubscription(subscription)
            
            // Delete from Firestore
            firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .document(subscriptionId)
                .delete()
                .await()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to delete subscription: ${e.message}")
        }
    }
    
    // Paycheck operations
    suspend fun getUpcomingPaychecks(limit: Int = 5): Result<List<PaycheckEntity>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val fromDate = System.currentTimeMillis()
            val paychecks = dao.getUpcomingPaychecks(userId, fromDate, limit)
            Result.Success(paychecks)
        } catch (e: Exception) {
            Result.Error("Failed to get upcoming paychecks: ${e.message}")
        }
    }
    
    suspend fun addPaycheck(
        date: Long,
        grossAmount: Double,
        netAmount: Double,
        payPeriodStart: Long? = null,
        payPeriodEnd: Long? = null
    ): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            val paycheck = PaycheckEntity(
                userId = userId,
                date = date,
                grossAmount = grossAmount,
                netAmount = netAmount,
                payPeriodStart = payPeriodStart,
                payPeriodEnd = payPeriodEnd,
                pendingSync = true
            )
            
            dao.insertPaycheck(paycheck)
            
            // Create reminder 1 day before
            createReminderForPaycheck(paycheck)
            
            // Sync to Firebase
            syncPaycheckToFirebase(paycheck)
            
            Result.Success(paycheck.id)
        } catch (e: Exception) {
            Result.Error("Failed to add paycheck: ${e.message}")
        }
    }
    
    suspend fun markPaycheckDeposited(paycheckId: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val paychecks = dao.getUpcomingPaychecks(userId, 0, 100)
            val paycheck = paychecks.find { it.id == paycheckId }
                ?: return Result.Error("Paycheck not found")
            
            val updatedPaycheck = paycheck.markDeposited()
            dao.updatePaycheck(updatedPaycheck)
            
            // Update balance
            val currentBalance = dao.getBalance(userId)?.currentBalance ?: 0.0
            val newBalance = currentBalance + paycheck.netAmount
            updateBalance(newBalance, "paycheck")
            
            // Sync to Firebase
            syncPaycheckToFirebase(updatedPaycheck)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to mark paycheck as deposited: ${e.message}")
        }
    }
    
    // Timeline and dashboard operations
    suspend fun getUpcomingTimeline(limit: Int = 30): Result<List<TimelineItem>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val fromDate = System.currentTimeMillis()
            val timeline = dao.getUpcomingTimeline(userId, getCurrentPeriod(), fromDate, limit)
            Result.Success(timeline)
        } catch (e: Exception) {
            Result.Error("Failed to get timeline: ${e.message}")
        }
    }
    
    suspend fun getDashboardSummary(): Result<DashboardSummary> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val summary = dao.getDashboardSummary(userId, getCurrentPeriod())
                ?: DashboardSummary(0.0, 0.0, 0.0, 0)
            Result.Success(summary)
        } catch (e: Exception) {
            Result.Error("Failed to get dashboard summary: ${e.message}")
        }
    }
    
    // FCM and reminder operations
    suspend fun registerFcmToken(deviceId: String, token: String, appVersion: String): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            val device = DeviceEntity(
                deviceId = deviceId,
                userId = userId,
                fcmToken = token,
                appVersion = appVersion,
                pendingSync = true
            )
            
            dao.insertDevice(device)
            
            // Sync to Firebase
            syncDeviceToFirebase(device)
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to register FCM token: ${e.message}")
        }
    }
    
    suspend fun getPendingReminders(): Result<List<ReminderEntity>> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val currentTime = System.currentTimeMillis()
            val reminders = dao.getPendingReminders(userId, currentTime)
            Result.Success(reminders)
        } catch (e: Exception) {
            Result.Error("Failed to get pending reminders: ${e.message}")
        }
    }
    
    // Private helper methods
    private fun getCurrentPeriod(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        return "$year-${month.toString().padStart(2, '0')}"
    }
    
    private suspend fun createRemindersForEssential(expense: EssentialExpenseEntity) {
        val dueDate = expense.getDueDateMillis() ?: return
        expense.reminderDaysBefore?.forEach { daysBefore ->
            val reminderTime = dueDate - (daysBefore * 24 * 60 * 60 * 1000)
            if (reminderTime > System.currentTimeMillis()) {
                val reminder = ReminderEntity(
                    userId = expense.userId,
                    type = ReminderType.ESSENTIAL,
                    targetId = expense.id,
                    fireAtUtc = reminderTime,
                    title = "${expense.name} Due ${if (daysBefore == 0) "Today" else "in $daysBefore day(s)"}",
                    message = "${expense.category.iconEmoji} ${expense.name} payment of $${String.format("%.2f", expense.plannedAmount)} ${if (daysBefore == 0) "is due today" else "due in $daysBefore day(s)"}",
                    pendingSync = true
                )
                dao.insertReminder(reminder)
            }
        }
    }
    
    private suspend fun createRemindersForSubscription(subscription: EnhancedSubscriptionEntity) {
        subscription.reminderDaysBefore.forEach { daysBefore ->
            val reminderTime = subscription.nextBillingDate - (daysBefore * 24 * 60 * 60 * 1000)
            if (reminderTime > System.currentTimeMillis()) {
                val reminder = ReminderEntity(
                    userId = subscription.userId,
                    type = ReminderType.SUBSCRIPTION,
                    targetId = subscription.id,
                    fireAtUtc = reminderTime,
                    title = "${subscription.name} Due ${if (daysBefore == 0) "Today" else "in $daysBefore day(s)"}",
                    message = "${subscription.iconEmoji ?: "💳"} ${subscription.name} bill of $${String.format("%.2f", subscription.amount)} ${if (daysBefore == 0) "is due today" else "due in $daysBefore day(s)"}",
                    pendingSync = true
                )
                dao.insertReminder(reminder)
            }
        }
    }
    
    private suspend fun createReminderForPaycheck(paycheck: PaycheckEntity) {
        val reminderTime = paycheck.date - (24 * 60 * 60 * 1000) // 1 day before
        if (reminderTime > System.currentTimeMillis()) {
            val reminder = ReminderEntity(
                userId = paycheck.userId,
                type = ReminderType.PAYCHECK,
                targetId = paycheck.id,
                fireAtUtc = reminderTime,
                title = "💰 Payday Tomorrow!",
                message = "$${String.format("%.2f", paycheck.netAmount)} hits your account tomorrow",
                pendingSync = true
            )
            dao.insertReminder(reminder)
        }
    }
    
    // Firebase sync methods
    private suspend fun syncEssentialToFirebase(expense: EssentialExpenseEntity) {
        try {
            firestore.collection("users")
                .document(expense.userId)
                .collection("essentials")
                .document(expense.id)
                .set(expense.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
    
    private suspend fun syncSubscriptionToFirebase(subscription: EnhancedSubscriptionEntity) {
        try {
            firestore.collection("users")
                .document(subscription.userId)
                .collection("subscriptions")
                .document(subscription.id)
                .set(subscription.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
    
    private suspend fun syncPaycheckToFirebase(paycheck: PaycheckEntity) {
        try {
            firestore.collection("users")
                .document(paycheck.userId)
                .collection("paychecks")
                .document(paycheck.id)
                .set(paycheck.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
    
    private suspend fun syncDeviceToFirebase(device: DeviceEntity) {
        try {
            firestore.collection("users")
                .document(device.userId)
                .collection("devices")
                .document(device.deviceId)
                .set(device.toFirestoreMap())
                .await()
        } catch (e: Exception) {
            // Keep as pending sync
        }
    }
    
    /**
     * Sync essential expenses FROM Firebase to local database
     */
    suspend fun syncEssentialExpensesFromFirebase(period: String? = null): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            val currentPeriod = period ?: getCurrentPeriod()
            
            // Fetch from Firebase
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("essentials")
                .whereEqualTo("period", currentPeriod)
                .get()
                .await()
            
            // Convert to entities and insert
            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                
                val entity = EssentialExpenseEntity(
                    id = doc.id,
                    userId = userId,
                    name = data["name"] as? String ?: "",
                    category = ExpenseCategory.valueOf(data["category"] as? String ?: "OTHER"),
                    plannedAmount = (data["plannedAmount"] as? Number)?.toDouble() ?: 0.0,
                    actualAmount = (data["actualAmount"] as? Number)?.toDouble(),
                    dueDay = (data["dueDay"] as? Number)?.toInt(),
                    paid = data["paid"] as? Boolean ?: false,
                    period = data["period"] as? String ?: currentPeriod,
                    reminderDaysBefore = (data["reminderDaysBefore"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: listOf(3, 1),
                    fcmReminderEnabled = data["fcmReminderEnabled"] as? Boolean ?: true,
                    notes = data["notes"] as? String,
                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    syncedAt = System.currentTimeMillis(),
                    pendingSync = false
                )
                
                dao.insertEssentialExpense(entity)
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to sync essential expenses from Firebase: ${e.message}")
        }
    }
    
    /**
     * Sync subscriptions FROM Firebase to local database
     */
    suspend fun syncSubscriptionsFromFirebase(): Result<Unit> {
        return try {
            val userId = currentUserId ?: return Result.Error("User not authenticated")
            
            // Fetch from Firebase
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("subscriptions")
                .whereEqualTo("active", true)
                .get()
                .await()
            
            // Convert to entities and insert
            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                
                val entity = EnhancedSubscriptionEntity(
                    id = doc.id,
                    userId = userId,
                    name = data["name"] as? String ?: "",
                    amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                    currency = data["currency"] as? String ?: "USD",
                    frequency = try {
                        BillingFrequency.valueOf(data["frequency"] as? String ?: "MONTHLY")
                    } catch (e: Exception) {
                        BillingFrequency.MONTHLY
                    },
                    nextBillingDate = (data["nextBillingDate"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    reminderDaysBefore = (data["reminderDaysBefore"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() } ?: listOf(1, 3, 7),
                    fcmReminderEnabled = data["fcmReminderEnabled"] as? Boolean ?: true,
                    active = data["active"] as? Boolean ?: true,
                    iconEmoji = data["iconEmoji"] as? String ?: "",
                    category = data["category"] as? String ?: "Other",
                    notes = data["notes"] as? String,
                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                    syncedAt = System.currentTimeMillis(),
                    pendingSync = false
                )
                
                dao.insertSubscription(entity)
            }
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to sync subscriptions from Firebase: ${e.message}")
        }
    }
    
    /**
     * Sync all budget data from Firebase (call this after database migration or on app start)
     */
    suspend fun syncAllFromFirebase(): Result<Unit> {
        return try {
            // Sync current period's essential expenses
            syncEssentialExpensesFromFirebase()
            
            // Sync subscriptions
            syncSubscriptionsFromFirebase()
            
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error("Failed to sync all data from Firebase: ${e.message}")
        }
    }
}

// Extension functions for Firestore mapping
private fun EssentialExpenseEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "category" to category.name,
    "plannedAmount" to plannedAmount,
    "actualAmount" to actualAmount,
    "dueDay" to dueDay,
    "paid" to paid,
    "period" to period,
    "reminderDaysBefore" to reminderDaysBefore,
    "fcmReminderEnabled" to fcmReminderEnabled,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun EnhancedSubscriptionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "amount" to amount,
    "currency" to currency,
    "frequency" to frequency.name,
    "nextBillingDate" to Timestamp(Date(nextBillingDate)),
    "reminderDaysBefore" to reminderDaysBefore,
    "fcmReminderEnabled" to fcmReminderEnabled,
    "active" to active,
    "iconEmoji" to iconEmoji,
    "category" to category,
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun PaycheckEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "date" to Timestamp(Date(date)),
    "grossAmount" to grossAmount,
    "netAmount" to netAmount,
    "deposited" to deposited,
    "depositedAt" to depositedAt?.let { Timestamp(Date(it)) },
    "payPeriodStart" to payPeriodStart?.let { Timestamp(Date(it)) },
    "payPeriodEnd" to payPeriodEnd?.let { Timestamp(Date(it)) },
    "notes" to notes,
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)

private fun DeviceEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "fcmToken" to fcmToken,
    "platform" to platform,
    "appVersion" to appVersion,
    "notificationsEnabled" to notificationsEnabled,
    "lastSeen" to FieldValue.serverTimestamp(),
    "createdAt" to FieldValue.serverTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp()
)
