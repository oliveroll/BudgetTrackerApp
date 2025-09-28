# Budget Tracker - Architecture Documentation

## 🏗️ Clean Architecture Overview

This Budget Tracker Android app follows **Clean Architecture** principles with **MVVM** pattern, ensuring separation of concerns, testability, and maintainability.

## 📱 Architecture Layers

### 1. Presentation Layer
**Location**: `features/` and `ui/`
**Responsibilities**: UI components and state management

```kotlin
// ViewModels manage UI state
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    fun loadTransactions() {
        viewModelScope.launch {
            getTransactionsUseCase().collect { transactions ->
                _uiState.value = _uiState.value.copy(transactions = transactions)
            }
        }
    }
}
```

**Components**:
- **Compose UI Screens**: Dashboard, Transactions, Budget, Analytics, Settings
- **ViewModels**: State management with StateFlow
- **Navigation**: Jetpack Navigation Compose
- **UI State**: Data classes representing screen state

### 2. Domain Layer
**Location**: `core/domain/`
**Responsibilities**: Business logic and rules

```kotlin
// Use cases contain business logic
class CreateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        amount: Double,
        category: TransactionCategory,
        type: TransactionType,
        description: String
    ): Result<Transaction> {
        // Business validation
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Amount must be positive"))
        }
        
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            amount = amount,
            category = category,
            type = type,
            description = description,
            date = Date(),
            userId = getCurrentUserId()
        )
        
        return transactionRepository.createTransaction(transaction)
    }
}
```

**Components**:
- **Models**: Core business entities (Transaction, Budget, UserProfile)
- **Use Cases**: Business operations (GetTransactions, CreateBudget, etc.)
- **Repository Interfaces**: Abstractions for data access
- **Value Objects**: TransactionCategory, BudgetStatus, etc.

### 3. Data Layer
**Location**: `core/data/`
**Responsibilities**: Data management and persistence

```kotlin
// Repository implementation coordinates data sources
class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionLocalDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    private val networkMonitor: NetworkMonitor
) : TransactionRepository {
    
    override fun getTransactions(): Flow<List<Transaction>> = flow {
        // Emit local data first (offline-first)
        emit(localDataSource.getTransactions().map { it.toDomain() })
        
        // Sync with remote if connected
        if (networkMonitor.isOnline()) {
            try {
                val remoteTransactions = remoteDataSource.getTransactions()
                localDataSource.saveTransactions(remoteTransactions.map { it.toEntity() })
                emit(localDataSource.getTransactions().map { it.toDomain() })
            } catch (e: Exception) {
                // Continue with local data if sync fails
            }
        }
    }
}
```

**Components**:
- **Room Database**: Local persistence
- **Firebase Integration**: Cloud sync and real-time updates
- **Repository Implementations**: Data source coordination
- **Data Sources**: Local (Room) and Remote (Firebase)
- **Entities**: Database representations
- **DTOs**: Network data transfer objects

## 🔄 Data Flow

### Read Operations
```
UI Screen → ViewModel → Use Case → Repository → Data Source → Database/API
                ↑                                                    ↓
            StateFlow ←─────────── Flow ←──────────── Entity ←──── Response
```

### Write Operations
```
UI Action → ViewModel → Use Case → Repository → Data Source → Database/API
              ↓                      ↓              ↓            ↓
         Validation → Business Logic → Caching → Local Storage → Cloud Sync
```

## 🎯 Key Design Patterns

### 1. Repository Pattern
Abstracts data sources and provides a clean API for domain layer.

```kotlin
interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun createTransaction(transaction: Transaction): Result<Transaction>
    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
}
```

### 2. Use Case Pattern
Encapsulates specific business operations.

```kotlin
class CalculateBudgetStatusUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(budgetId: String): BudgetStatus {
        val budget = budgetRepository.getBudget(budgetId)
        val transactions = transactionRepository.getTransactionsByBudget(budgetId)
        
        val totalSpent = transactions.filter { it.type == EXPENSE }.sumOf { it.amount }
        val totalBudget = budget.totalBudget
        
        return when {
            totalSpent >= totalBudget -> BudgetStatus.OVER_BUDGET
            totalSpent >= totalBudget * 0.9 -> BudgetStatus.NEAR_LIMIT
            totalSpent >= totalBudget * 0.7 -> BudgetStatus.ON_TRACK
            else -> BudgetStatus.UNDER_BUDGET
        }
    }
}
```

### 3. Observer Pattern
UI observes data changes through StateFlow/Flow.

```kotlin
@Composable
fun TransactionScreen(viewModel: TransactionViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadTransactions()
    }
    
    when (uiState.loadingState) {
        is Loading -> LoadingIndicator()
        is Success -> TransactionList(uiState.transactions)
        is Error -> ErrorMessage(uiState.error)
    }
}
```

## 🏛️ Module Structure

### Core Modules
```
core/
├── data/
│   ├── local/
│   │   ├── entities/           # Room entities
│   │   ├── dao/               # Data Access Objects
│   │   └── database/          # Database configuration
│   ├── remote/
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── api/               # API interfaces
│   │   └── datasource/        # Remote data sources
│   └── repository/            # Repository implementations
├── domain/
│   ├── model/                 # Domain models
│   ├── repository/            # Repository interfaces
│   └── usecase/              # Business use cases
└── utils/
    ├── Constants.kt          # App constants
    ├── Extensions.kt         # Utility extensions
    └── Result.kt            # Result wrapper
```

### Feature Modules
```
features/
├── transaction/
│   ├── data/                 # Transaction-specific data layer
│   ├── domain/               # Transaction business logic
│   └── presentation/         # Transaction UI
├── budget/
├── analytics/
└── profile/
```

## 🔧 Dependency Injection (Hilt)

### Application Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideTransactionRepository(
        localDataSource: TransactionLocalDataSource,
        remoteDataSource: TransactionRemoteDataSource
    ): TransactionRepository = TransactionRepositoryImpl(localDataSource, remoteDataSource)
}
```

### Database Module
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "budget_tracker_database"
        ).build()
    }
    
    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao = database.transactionDao()
}
```

## 🌐 Firebase Integration

### Firestore Collections
```
/users/{userId}/
├── profile/                  # User profile data
├── settings/                 # User preferences
├── transactions/             # User transactions
├── budgets/                 # Budget information
├── goals/                   # Savings goals
└── loans/                   # Loan tracking
```

### Real-time Sync
```kotlin
class FirebaseTransactionDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TransactionRemoteDataSource {
    
    override fun getTransactionsFlow(): Flow<List<TransactionDto>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        
        val listener = firestore.collection("users")
            .document(userId)
            .collection("transactions")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<TransactionDto>()
                } ?: emptyList()
                
                trySend(transactions)
            }
        
        awaitClose { listener.remove() }
    }
}
```

## 🧪 Testing Strategy

### Unit Tests
- **ViewModels**: Test state management and UI logic
- **Use Cases**: Test business logic
- **Repositories**: Test data coordination

### Integration Tests
- **Database**: Test Room operations
- **API**: Test Firebase integration
- **End-to-End**: Test complete user flows

### Test Structure
```kotlin
class TransactionViewModelTest {
    
    @Mock private lateinit var getTransactionsUseCase: GetTransactionsUseCase
    @Mock private lateinit var createTransactionUseCase: CreateTransactionUseCase
    
    private lateinit var viewModel: TransactionViewModel
    
    @Test
    fun `when loading transactions, should update ui state`() = runTest {
        // Given
        val transactions = listOf(mockTransaction())
        whenever(getTransactionsUseCase()).thenReturn(flowOf(transactions))
        
        // When
        viewModel.loadTransactions()
        
        // Then
        assertEquals(transactions, viewModel.uiState.value.transactions)
    }
}
```

## 🚀 Performance Considerations

### Offline-First Architecture
- Local database serves as single source of truth
- UI remains responsive without network
- Background sync when connected

### Lazy Loading
```kotlin
@Composable
fun TransactionList(
    transactions: LazyPagingItems<Transaction>
) {
    LazyColumn {
        items(transactions) { transaction ->
            if (transaction != null) {
                TransactionItem(transaction)
            }
        }
    }
}
```

### Memory Management
- Use Flow for reactive data streams
- Proper lifecycle-aware observers
- Efficient image loading with Coil

This architecture ensures scalability, maintainability, and excellent user experience for international students managing their finances on OPT/H1B visas.
