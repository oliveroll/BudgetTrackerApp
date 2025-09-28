# Budget Tracker - API Documentation

## 🏗️ Domain Models API

### Transaction Model

```kotlin
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val description: String,
    val date: Date,
    val isRecurring: Boolean = false,
    val recurringPeriod: RecurringPeriod? = null,
    val tags: List<String> = emptyList(),
    val attachmentUrl: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val isDeleted: Boolean = false
)
```

**Properties**:
- `id`: Unique transaction identifier
- `userId`: Associated user ID
- `amount`: Transaction amount (positive for income, expenses)
- `category`: Transaction category (37+ available)
- `type`: INCOME or EXPENSE
- `description`: Human-readable description
- `date`: Transaction date
- `isRecurring`: Whether transaction repeats
- `recurringPeriod`: Frequency of recurrence
- `tags`: Custom tags for categorization
- `attachmentUrl`: Receipt or document URL
- `location`: Transaction location
- `notes`: Additional notes
- `createdAt/updatedAt`: Timestamps
- `isDeleted`: Soft delete flag

### Transaction Categories

```kotlin
enum class TransactionCategory {
    // Income
    SALARY, FREELANCE, INVESTMENT_RETURNS, OTHER_INCOME,
    
    // Fixed Expenses
    RENT, UTILITIES, INTERNET, PHONE, SUBSCRIPTIONS,
    LOAN_PAYMENTS, INSURANCE,
    
    // Variable Expenses
    GROCERIES, DINING_OUT, TRANSPORTATION, ENTERTAINMENT,
    CLOTHING, PERSONAL_CARE, HEALTHCARE, EDUCATION,
    
    // Savings & Investments
    EMERGENCY_FUND, RETIREMENT_401K, RETIREMENT_ROTH_IRA,
    STOCKS_ETFS, CRYPTO, TRAVEL_FUND,
    
    // OPT/Visa Specific
    H1B_APPLICATION, VISA_FEES,
    
    // Other
    MISCELLANEOUS
}
```

### Budget Model

```kotlin
data class Budget(
    val id: String,
    val userId: String,
    val month: String, // "2024-01"
    val year: Int,
    val categories: List<CategoryBudget>,
    val totalBudget: Double,
    val totalSpent: Double,
    val totalIncome: Double,
    val createdAt: Date,
    val updatedAt: Date,
    val isTemplate: Boolean = false,
    val templateName: String? = null, // "50/30/20", "Zero-based", "OPT Student"
    val status: BudgetStatus = BudgetStatus.ON_TRACK
)

data class CategoryBudget(
    val category: TransactionCategory,
    val budgetedAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double
) {
    val utilizationPercentage: Double
        get() = if (budgetedAmount > 0) (spentAmount / budgetedAmount) * 100 else 0.0
}

enum class BudgetStatus {
    UNDER_BUDGET,    // < 70% spent
    ON_TRACK,        // 70-89% spent
    NEAR_LIMIT,      // 90-99% spent
    OVER_BUDGET      // >= 100% spent
}
```

### User Profile Model

```kotlin
data class UserProfile(
    val id: String,
    val email: String,
    val displayName: String,
    val monthlyIncome: Double = Constants.DEFAULT_MONTHLY_INCOME,
    val annualSalary: Double = Constants.DEFAULT_ANNUAL_SALARY,
    val employmentStatus: EmploymentStatus = EmploymentStatus.OPT,
    val company: String = Constants.DEFAULT_COMPANY,
    val state: String = Constants.DEFAULT_STATE,
    val emergencyFundGoal: Double = Constants.DEFAULT_MONTHLY_INCOME * Constants.EMERGENCY_FUND_MONTHS,
    val createdAt: Date,
    val updatedAt: Date,
    val preferences: UserPreferences = UserPreferences()
)

enum class EmploymentStatus {
    OPT, H1B, GREEN_CARD, CITIZEN, STUDENT, UNEMPLOYED
}

data class UserPreferences(
    val currency: String = "USD",
    val theme: String = "SYSTEM", // LIGHT, DARK, SYSTEM
    val notifications: Boolean = true,
    val biometricAuth: Boolean = false,
    val autoSync: Boolean = true
)
```

## 🔄 Use Cases API

### Transaction Use Cases

```kotlin
// Get all transactions for user
class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>>
    
    operator fun invoke(
        dateRange: DateRange? = null,
        categories: List<TransactionCategory>? = null,
        type: TransactionType? = null
    ): Flow<List<Transaction>>
}

// Create new transaction
class CreateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        category: TransactionCategory,
        type: TransactionType,
        description: String,
        date: Date = Date(),
        isRecurring: Boolean = false,
        recurringPeriod: RecurringPeriod? = null
    ): Result<Transaction>
}

// Update existing transaction
class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Transaction>
}

// Delete transaction (soft delete)
class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transactionId: String): Result<Unit>
}
```

### Budget Use Cases

```kotlin
// Get budget for specific month
class GetBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(month: String, year: Int): Budget?
    operator fun invoke(): Flow<Budget?> // Current month
}

// Create budget from template
class CreateBudgetFromTemplateUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(
        template: BudgetTemplate,
        month: String,
        year: Int,
        monthlyIncome: Double
    ): Result<Budget>
}

// Calculate budget status
class CalculateBudgetStatusUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(budgetId: String): BudgetStatus
}

enum class BudgetTemplate {
    FIFTY_THIRTY_TWENTY,  // 50% needs, 30% wants, 20% savings
    ZERO_BASED,           // Every dollar assigned
    OPT_STUDENT           // Optimized for international students
}
```

### Analytics Use Cases

```kotlin
// Get spending by category
class GetSpendingByCategoryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        dateRange: DateRange
    ): Map<TransactionCategory, Double>
}

// Get monthly trends
class GetMonthlyTrendsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        months: Int = 12
    ): List<MonthlySpending>
}

data class MonthlySpending(
    val month: String,
    val year: Int,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netAmount: Double
)
```

## 🗄️ Repository Interfaces

### Transaction Repository

```kotlin
interface TransactionRepository {
    // Read operations
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByCategory(category: TransactionCategory): Flow<List<Transaction>>
    fun getTransactionsByDateRange(start: Date, end: Date): Flow<List<Transaction>>
    suspend fun getTransaction(id: String): Transaction?
    
    // Write operations
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(id: String): Result<Unit>
    
    // Bulk operations
    suspend fun createTransactions(transactions: List<Transaction>): Result<List<Transaction>>
    suspend fun syncTransactions(): Result<Unit>
}
```

### Budget Repository

```kotlin
interface BudgetRepository {
    // Read operations
    fun getCurrentBudget(): Flow<Budget?>
    suspend fun getBudget(month: String, year: Int): Budget?
    fun getBudgetHistory(): Flow<List<Budget>>
    
    // Write operations
    suspend fun createBudget(budget: Budget): Result<Budget>
    suspend fun updateBudget(budget: Budget): Result<Budget>
    suspend fun deleteBudget(id: String): Result<Unit>
    
    // Templates
    suspend fun createBudgetFromTemplate(
        template: BudgetTemplate,
        monthlyIncome: Double,
        month: String,
        year: Int
    ): Result<Budget>
}
```

## 📊 Analytics API

### Spending Analytics

```kotlin
data class SpendingAnalytics(
    val totalSpent: Double,
    val categoryBreakdown: Map<TransactionCategory, CategorySpending>,
    val monthlyTrend: List<MonthlySpending>,
    val topCategories: List<TransactionCategory>,
    val averageTransaction: Double
)

data class CategorySpending(
    val category: TransactionCategory,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int,
    val trend: SpendingTrend
)

enum class SpendingTrend {
    INCREASING, DECREASING, STABLE
}
```

### Budget Analytics

```kotlin
data class BudgetAnalytics(
    val totalBudget: Double,
    val totalSpent: Double,
    val remainingBudget: Double,
    val utilizationPercentage: Double,
    val status: BudgetStatus,
    val categoryPerformance: List<CategoryPerformance>,
    val projectedEndOfMonth: Double
)

data class CategoryPerformance(
    val category: TransactionCategory,
    val budgeted: Double,
    val spent: Double,
    val remaining: Double,
    val onTrack: Boolean,
    val projectedSpend: Double
)
```

## 🔧 Constants API

```kotlin
object Constants {
    // Financial Defaults (OPT Student Focus)
    const val DEFAULT_MONTHLY_INCOME = 5470.0      // $5,470/month
    const val DEFAULT_ANNUAL_SALARY = 80000.0      // $80,000/year
    const val EMERGENCY_FUND_MONTHS = 6
    
    // Investment Limits (2024)
    const val ROTH_IRA_ANNUAL_LIMIT_2024 = 7000.0
    const val TRADITIONAL_401K_LIMIT_2024 = 23000.0
    
    // OPT/Visa Specific
    const val DEFAULT_COMPANY = "Ixana Quasistatics"
    const val DEFAULT_STATE = "Indiana"
    
    // Budget Templates
    const val TEMPLATE_50_30_20 = "50/30/20 Rule"
    const val TEMPLATE_ZERO_BASED = "Zero-based Budget"
    const val TEMPLATE_OPT_STUDENT = "OPT Student Budget"
    
    // Categories
    val INCOME_CATEGORIES = listOf(
        TransactionCategory.SALARY,
        TransactionCategory.FREELANCE,
        TransactionCategory.INVESTMENT_RETURNS,
        TransactionCategory.OTHER_INCOME
    )
    
    val VISA_SPECIFIC_CATEGORIES = listOf(
        TransactionCategory.H1B_APPLICATION,
        TransactionCategory.VISA_FEES
    )
}
```

## 🌐 Firebase API Integration

### Firestore Collections Structure

```kotlin
// Collection paths
object FirestoreCollections {
    const val USERS = "users"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val GOALS = "goals"
    const val LOANS = "loans"
    
    // Subcollections
    fun userTransactions(userId: String) = "$USERS/$userId/$TRANSACTIONS"
    fun userBudgets(userId: String) = "$USERS/$userId/$BUDGETS"
}

// Document structure
data class TransactionDto(
    val id: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val type: String = "",
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val isRecurring: Boolean = false,
    val tags: List<String> = emptyList(),
    val attachmentUrl: String? = null,
    val location: String? = null,
    val notes: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val isDeleted: Boolean = false
)
```

## 🧪 Testing Utilities

```kotlin
object TestData {
    fun mockTransaction(
        id: String = "test-id",
        amount: Double = 100.0,
        category: TransactionCategory = TransactionCategory.GROCERIES,
        type: TransactionType = TransactionType.EXPENSE
    ) = Transaction(
        id = id,
        userId = "test-user",
        amount = amount,
        category = category,
        type = type,
        description = "Test transaction",
        date = Date()
    )
    
    fun mockBudget(
        month: String = "2024-01",
        totalBudget: Double = 5000.0
    ) = Budget(
        id = "test-budget",
        userId = "test-user",
        month = month,
        year = 2024,
        categories = emptyList(),
        totalBudget = totalBudget,
        totalSpent = 0.0,
        totalIncome = 5470.0,
        createdAt = Date(),
        updatedAt = Date()
    )
}
```

This API documentation provides comprehensive interfaces for building and extending the Budget Tracker app, specifically designed for international students on OPT/H1B visas.
