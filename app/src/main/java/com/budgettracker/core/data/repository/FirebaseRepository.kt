package com.budgettracker.core.data.repository

import android.content.Context
import com.budgettracker.core.data.local.LocalDataManager
import com.budgettracker.core.domain.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*

/**
 * Firebase repository with local-first architecture and real-time sync
 */
class FirebaseRepository(context: Context) {
    
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val localDataManager = LocalDataManager(context)
    
    private fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // Transaction operations
    suspend fun saveTransaction(transaction: Transaction): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val transactionWithUserId = transaction.copy(userId = userId)
                
                // Save locally first (for offline support)
                localDataManager.saveTransaction(transactionWithUserId)
                
                // Sync to Firebase
                firestore.collection("transactions")
                    .document(transaction.id)
                    .set(transactionWithUserId)
                    .await()
                
                Result.success(transaction.id)
            } else {
                // Not authenticated, save only locally
                localDataManager.saveTransaction(transaction)
            }
        } catch (e: Exception) {
            // Always save locally as fallback
            localDataManager.saveTransaction(transaction)
            Result.failure(e)
        }
    }
    
    fun getTransactionsFlow(): Flow<List<Transaction>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            // Listen to Firestore changes
            val listener = firestore.collection("transactions")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isDeleted", false)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Fallback to local data
                        trySend(localDataManager.getTransactions())
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val transactions = snapshot.toObjects(Transaction::class.java)
                        // Update local cache
                        transactions.forEach { localDataManager.saveTransaction(it) }
                        trySend(transactions)
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            // Not authenticated, use local data
            trySend(localDataManager.getTransactions())
            awaitClose { }
        }
    }
    
    // Savings Goal operations
    suspend fun saveSavingsGoal(goal: SavingsGoal): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val goalWithUserId = goal.copy(userId = userId)
                
                // Save locally first
                localDataManager.saveSavingsGoal(goalWithUserId)
                
                // Sync to Firebase
                firestore.collection("savingsGoals")
                    .document(goal.id)
                    .set(goalWithUserId)
                    .await()
                
                Result.success(goal.id)
            } else {
                // Not authenticated, save only locally
                localDataManager.saveSavingsGoal(goal)
            }
        } catch (e: Exception) {
            // Always save locally as fallback
            localDataManager.saveSavingsGoal(goal)
            Result.failure(e)
        }
    }
    
    fun getSavingsGoalsFlow(): Flow<List<SavingsGoal>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            // Listen to Firestore changes
            val listener = firestore.collection("savingsGoals")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .orderBy("priority")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Fallback to local data
                        trySend(localDataManager.getSavingsGoals())
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val goals = snapshot.toObjects(SavingsGoal::class.java)
                        // Update local cache
                        goals.forEach { localDataManager.saveSavingsGoal(it) }
                        trySend(goals)
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            // Not authenticated, use local data
            trySend(localDataManager.getSavingsGoals())
            awaitClose { }
        }
    }
    
    // User Profile operations
    suspend fun saveUserProfile(profile: UserProfile): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val profileWithUserId = profile.copy(userId = userId)
                
                // Save locally first
                localDataManager.saveUserProfile(profileWithUserId)
                
                // Sync to Firebase
                firestore.collection("users")
                    .document(userId)
                    .set(profileWithUserId)
                    .await()
                
                Result.success(userId)
            } else {
                // Not authenticated, save only locally
                localDataManager.saveUserProfile(profile)
            }
        } catch (e: Exception) {
            // Always save locally as fallback
            localDataManager.saveUserProfile(profile)
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(): UserProfile? {
        val userId = getCurrentUserId()
        return if (userId != null) {
            try {
                // Try to get from Firebase first
                val doc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                if (doc.exists()) {
                    val profile = doc.toObject(UserProfile::class.java)
                    // Cache locally
                    profile?.let { localDataManager.saveUserProfile(it) }
                    profile
                } else {
                    // Fallback to local
                    localDataManager.getUserProfile()
                }
            } catch (e: Exception) {
                // Fallback to local
                localDataManager.getUserProfile()
            }
        } else {
            // Not authenticated, use local
            localDataManager.getUserProfile()
        }
    }
    
    // Sync operations
    suspend fun syncAllData(): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Not authenticated"))
            
            // Sync transactions
            val localTransactions = localDataManager.getTransactions()
            localTransactions.forEach { transaction ->
                if (transaction.userId.isEmpty()) {
                    saveTransaction(transaction.copy(userId = userId))
                }
            }
            
            // Sync goals
            val localGoals = localDataManager.getSavingsGoals()
            localGoals.forEach { goal ->
                if (goal.userId.isEmpty()) {
                    saveSavingsGoal(goal.copy(userId = userId))
                }
            }
            
            // Sync profile
            val localProfile = localDataManager.getUserProfile()
            if (localProfile != null && localProfile.userId.isEmpty()) {
                saveUserProfile(localProfile.copy(userId = userId))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete operations
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                // Soft delete in Firebase
                firestore.collection("transactions")
                    .document(transactionId)
                    .update("isDeleted", true, "updatedAt", Date())
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                // Set as inactive in Firebase
                firestore.collection("savingsGoals")
                    .document(goalId)
                    .update("isActive", false, "updatedAt", Date())
                    .await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Fixed Expenses operations
    suspend fun saveFixedExpense(expense: FixedExpense): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val expenseWithUserId = expense.copy(userId = userId)
                
                firestore.collection("fixedExpenses")
                    .document(expense.id)
                    .set(expenseWithUserId)
                    .await()
                
                Result.success(expense.id)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getFixedExpensesFlow(): Flow<List<FixedExpense>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val listener = firestore.collection("fixedExpenses")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getDefaultFixedExpenses(userId))
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val expenses = snapshot.toObjects(FixedExpense::class.java)
                        if (expenses.isEmpty()) {
                            // Create default data and return it
                            val defaultExpenses = getDefaultFixedExpenses(userId)
                            trySend(defaultExpenses)
                            // Save to Firebase in background
                            defaultExpenses.forEach { expense ->
                                firestore.collection("fixedExpenses")
                                    .document(expense.id)
                                    .set(expense)
                            }
                        } else {
                            trySend(expenses)
                        }
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            trySend(getDefaultFixedExpenses(""))
            awaitClose { }
        }
    }
    
    // Income Sources operations
    suspend fun saveIncomeSource(income: IncomeSource): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val incomeWithUserId = income.copy(userId = userId)
                
                firestore.collection("incomeSources")
                    .document(income.id)
                    .set(incomeWithUserId)
                    .await()
                
                Result.success(income.id)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getIncomeSourcesFlow(): Flow<List<IncomeSource>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val listener = firestore.collection("incomeSources")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getDefaultIncomeSources(userId))
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val incomes = snapshot.toObjects(IncomeSource::class.java)
                        if (incomes.isEmpty()) {
                            // Create default data and return it
                            val defaultIncomes = getDefaultIncomeSources(userId)
                            trySend(defaultIncomes)
                            // Save to Firebase in background
                            defaultIncomes.forEach { income ->
                                firestore.collection("incomeSources")
                                    .document(income.id)
                                    .set(income)
                            }
                        } else {
                            trySend(incomes)
                        }
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            trySend(getDefaultIncomeSources(""))
            awaitClose { }
        }
    }
    
    // Variable Expenses operations
    suspend fun saveVariableExpenseCategory(category: VariableExpenseCategory): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val categoryWithUserId = category.copy(userId = userId)
                
                firestore.collection("variableExpenseCategories")
                    .document(category.id)
                    .set(categoryWithUserId)
                    .await()
                
                Result.success(category.id)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getVariableExpenseCategoriesFlow(): Flow<List<VariableExpenseCategory>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val listener = firestore.collection("variableExpenseCategories")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getDefaultVariableExpenses(userId))
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val categories = snapshot.toObjects(VariableExpenseCategory::class.java)
                        if (categories.isEmpty()) {
                            // Create default data and return it
                            val defaultCategories = getDefaultVariableExpenses(userId)
                            trySend(defaultCategories)
                            // Save to Firebase in background
                            defaultCategories.forEach { category ->
                                firestore.collection("variableExpenseCategories")
                                    .document(category.id)
                                    .set(category)
                            }
                        } else {
                            trySend(categories)
                        }
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            trySend(getDefaultVariableExpenses(""))
            awaitClose { }
        }
    }
    
    // Accounts operations
    suspend fun saveAccount(account: Account): Result<String> {
        return try {
            val userId = getCurrentUserId()
            if (userId != null) {
                val accountWithUserId = account.copy(userId = userId)
                
                firestore.collection("accounts")
                    .document(account.id)
                    .set(accountWithUserId)
                    .await()
                
                Result.success(account.id)
            } else {
                Result.failure(Exception("Not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getAccountsFlow(): Flow<List<Account>> = callbackFlow {
        val userId = getCurrentUserId()
        
        if (userId != null) {
            val listener = firestore.collection("accounts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(getDefaultAccounts(userId))
                        return@addSnapshotListener
                    }
                    
                    if (snapshot != null) {
                        val accounts = snapshot.toObjects(Account::class.java)
                        if (accounts.isEmpty()) {
                            // Create default data and return it
                            val defaultAccounts = getDefaultAccounts(userId)
                            trySend(defaultAccounts)
                            // Save to Firebase in background
                            defaultAccounts.forEach { account ->
                                firestore.collection("accounts")
                                    .document(account.id)
                                    .set(account)
                            }
                        } else {
                            trySend(accounts)
                        }
                    }
                }
            
            awaitClose { listener.remove() }
        } else {
            trySend(getDefaultAccounts(""))
            awaitClose { }
        }
    }
    
    // Initialize user data on first sign-in
    suspend fun initializeUserData(user: com.google.firebase.auth.FirebaseUser): Result<Unit> {
        return try {
            val profile = UserProfile(
                userId = user.uid,
                email = user.email ?: "",
                name = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                createdAt = Date(),
                updatedAt = Date()
            )
            
            saveUserProfile(profile)
            
            // Initialize default financial data
            // This will be done when the flows are first accessed
            
            syncAllData()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Default data based on your HTML design
    private fun getDefaultFixedExpenses(userId: String): List<FixedExpense> {
        return listOf(
            FixedExpense(userId = userId, category = "Housing", description = "Rent", amount = 700.0, dueDate = "1st", notes = "Apartment in Indiana"),
            FixedExpense(userId = userId, category = "Utilities", description = "Electric/Water/Internet", amount = 100.0, dueDate = "15th", notes = "Average monthly"),
            FixedExpense(userId = userId, category = "Phone", description = "Mobile Plan (Prepaid)", amount = 25.0, dueDate = "-", notes = "$150/6 months (renew Feb)"),
            FixedExpense(userId = userId, category = "Subscription", description = "Spotify", amount = 5.99, dueDate = "10th", notes = "$11.99 after March"),
            FixedExpense(userId = userId, category = "Debt Payment", description = "German Student Loan", amount = 475.0, dueDate = "20th", notes = "€11,000 @ €450/month"),
            FixedExpense(userId = userId, category = "Transportation", description = "Public Transit/Uber Budget", amount = 100.0, dueDate = "-", notes = "No car"),
            FixedExpense(userId = userId, category = "Living", description = "Groceries", amount = 300.0, dueDate = "Weekly", notes = "$75/week")
        )
    }
    
    private fun getDefaultIncomeSources(userId: String): List<IncomeSource> {
        return listOf(
            IncomeSource(
                userId = userId,
                description = "Base Salary - Ixana Quasistatics",
                frequency = "Bi-weekly",
                grossAmount = 3076.92,
                taxesDeductions = 553.04,
                netAmount = 2523.88
            )
        )
    }
    
    private fun getDefaultVariableExpenses(userId: String): List<VariableExpenseCategory> {
        return listOf(
            VariableExpenseCategory(userId = userId, category = "Dining Out", budgetAmount = 300.0, notes = "Weekly limit: $75"),
            VariableExpenseCategory(userId = userId, category = "Entertainment", budgetAmount = 200.0, notes = "Movies, events"),
            VariableExpenseCategory(userId = userId, category = "Personal Care", budgetAmount = 100.0, notes = "Haircut, gym"),
            VariableExpenseCategory(userId = userId, category = "Clothing", budgetAmount = 150.0, notes = "Seasonal needs"),
            VariableExpenseCategory(userId = userId, category = "Miscellaneous", budgetAmount = 200.0, notes = "Amazon, unexpected"),
            VariableExpenseCategory(userId = userId, category = "German Loan Extra", budgetAmount = 200.0, notes = "Optional extra payment"),
            VariableExpenseCategory(userId = userId, category = "Buffer/Overflow", budgetAmount = 614.01, notes = "Unallocated flexibility")
        )
    }
    
    private fun getDefaultAccounts(userId: String): List<Account> {
        return listOf(
            Account(userId = userId, name = "Bills & Thrills Checking", bankPlatform = "Your Bank", purpose = "Daily expenses & bills", monthlyFlow = 3470.0, accountType = AccountType.CHECKING),
            Account(userId = userId, name = "Emergency Savings", bankPlatform = "High-Yield (Ally/Marcus)", purpose = "3-4 months expenses", monthlyFlow = 800.0, accountType = AccountType.SAVINGS),
            Account(userId = userId, name = "Roth IRA", bankPlatform = "Vanguard/Fidelity", purpose = "Tax-free retirement", monthlyFlow = 583.0, accountType = AccountType.RETIREMENT),
            Account(userId = userId, name = "Robinhood", bankPlatform = "Robinhood", purpose = "ETFs & fun investing", monthlyFlow = 500.0, accountType = AccountType.INVESTMENT),
            Account(userId = userId, name = "Travel Savings", bankPlatform = "Your Bank", purpose = "Vacation fund", monthlyFlow = 117.0, accountType = AccountType.SAVINGS)
        )
    }
}
