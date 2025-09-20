# Budget Tracker Android App - Project Documentation (MDC)

## Project Overview
**Repository:** https://github.com/oliveroll/BudgetTrackerApp  
**Platform:** Android  
**Language:** Kotlin  
**Architecture:** Clean Architecture with MVVM  
**UI Framework:** Jetpack Compose  
**Target SDK:** 34 (Android 14)  
**Min SDK:** 24 (Android 7.0)  

## Project Structure

```
/home/oliver/BudgetTrackerApp/
├── app/
│   ├── build.gradle.kts                    # App-level build configuration
│   └── src/main/
│       ├── java/com/budgettracker/
│       │   ├── BudgetTrackerApplication.kt # Application class with Hilt setup
│       │   └── core/
│       │       ├── data/
│       │       │   └── local/
│       │       │       └── entities/       # Room database entities
│       │       │           ├── TransactionEntity.kt
│       │       │           └── UserProfileEntity.kt
│       │       ├── domain/
│       │       │   └── model/             # Domain models (business logic)
│       │       │       ├── Budget.kt
│       │       │       ├── Loan.kt
│       │       │       ├── SavingsGoal.kt
│       │       │       ├── Transaction.kt
│       │       │       └── UserProfile.kt
│       │       └── utils/
│       │           ├── Constants.kt       # App-wide constants
│       │           └── Extensions.kt      # Utility extensions
│       └── res/                          # Android resources
│           ├── drawable/
│           ├── layout/
│           └── values/
├── build.gradle.kts                      # Project-level build configuration
├── settings.gradle.kts                   # Project settings
└── gradle/wrapper/                       # Gradle wrapper files
```

## Technology Stack

### Core Android
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern UI toolkit
- **Material Design 3** - UI components and theming
- **Navigation Compose** - Navigation framework

### Architecture & DI
- **Hilt (Dagger)** - Dependency injection
- **MVVM Pattern** - Architecture pattern
- **Clean Architecture** - Layered architecture approach

### Data Layer
- **Room Database** - Local data persistence
- **Firebase Firestore** - Cloud database
- **Firebase Auth** - Authentication
- **Firebase Storage** - File storage
- **Firebase Analytics** - App analytics
- **Firebase Crashlytics** - Crash reporting

### Networking & APIs
- **Retrofit** - HTTP client
- **OkHttp** - Network interceptor
- **Gson** - JSON serialization

### UI & Visualization
- **Vico Charts** - Chart library for financial data
- **Coil** - Image loading
- **Material Icons Extended** - Icon library

### Utilities
- **Kotlinx Coroutines** - Asynchronous programming
- **Kotlinx DateTime** - Date/time handling
- **WorkManager** - Background tasks
- **Biometric** - Fingerprint/face authentication

## Domain Models

### 1. Transaction Model
**File:** `core/domain/model/Transaction.kt`

```kotlin
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType,
    val description: String,
    val date: Date,
    val isRecurring: Boolean,
    val recurringPeriod: RecurringPeriod?,
    val tags: List<String>,
    val attachmentUrl: String?,
    val location: String?,
    val notes: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val isDeleted: Boolean
)
```

**Key Features:**
- Support for income and expense transactions
- Comprehensive category system (37+ categories)
- Recurring transaction support
- Attachment and location tracking
- Soft delete functionality

**Transaction Categories Include:**
- **Income:** Salary, Freelance, Investment Returns, Other Income
- **Fixed Expenses:** Rent, Utilities, Internet, Phone, Subscriptions, Loan Payments, Insurance
- **Variable Expenses:** Groceries, Dining Out, Transportation, Entertainment, Clothing, Personal Care, Healthcare, Education
- **Savings & Investments:** Emergency Fund, Retirement, Stocks/ETFs, Crypto, Travel Fund
- **Visa-Specific:** H1B Application, Visa Fees
- **Other:** Miscellaneous

### 2. Budget Model
**File:** `core/domain/model/Budget.kt`

```kotlin
data class Budget(
    val id: String,
    val userId: String,
    val month: String,
    val year: Int,
    val categories: List<CategoryBudget>,
    val totalBudget: Double,
    val totalSpent: Double,
    val totalIncome: Double,
    val createdAt: Date,
    val updatedAt: Date,
    val isTemplate: Boolean,
    val templateName: String?
)
```

**Key Features:**
- Monthly budget planning
- Category-wise budget allocation
- Budget utilization tracking
- Template support (50/30/20 rule, Zero-based, OPT Student)
- Budget status monitoring (Under Budget, On Track, Near Limit, Over Budget)

### 3. Other Models
- **UserProfile:** User information and preferences
- **SavingsGoal:** Financial goal tracking
- **Loan:** Debt management

## Data Layer

### Room Entities
**TransactionEntity** (`core/data/local/entities/TransactionEntity.kt`):
- Local database representation of transactions
- Includes sync status for offline functionality
- Type converters for complex data types

### Firebase Collections
- `users` - User profiles
- `transactions` - Transaction records
- `budgets` - Budget data
- `goals` - Savings goals
- `loans` - Loan information
- `recurring` - Recurring transactions

## Application Configuration

### Build Configuration
- **Application ID:** `com.budgettracker`
- **Version Code:** 1
- **Version Name:** "1.0"
- **Compile SDK:** 34
- **Target SDK:** 34
- **Min SDK:** 24

### Key Features Enabled
- Compose UI
- Kotlin Parcelize
- KAPT for annotation processing
- Proguard optimization for release builds
- Vector drawable support

## Constants and Configuration

**File:** `core/utils/Constants.kt`

### Default Values
- Monthly Income: $5,470
- Base Salary: $80,000
- Emergency Fund: 6 months
- Default Loan: $11,550 with $475 payments

### OPT/Visa Specific
- Employment statuses: OPT, H1B
- Default Company: "Ixana Quasistatics"
- Default State: "Indiana"

### Investment Limits
- Roth IRA Annual Limit: $7,000 (2024)
- Traditional 401K Limit: $23,000 (2024)

## Development Status

### Completed Components
✅ Project structure setup  
✅ Domain models with comprehensive business logic  
✅ Room entity definitions  
✅ Constants and configuration  
✅ Build configuration with all dependencies  
✅ Git repository setup  
✅ GitHub integration  

### Missing Components (To Be Implemented)
❌ Repository layer  
❌ Use cases/Interactors  
❌ ViewModels  
❌ Compose UI screens  
❌ Navigation setup  
❌ Firebase configuration  
❌ Room database setup (DAOs, Database class)  
❌ Dependency injection modules  
❌ Testing framework  

## Architecture Layers

### 1. Presentation Layer (To Be Implemented)
- Compose UI screens
- ViewModels
- Navigation
- UI state management

### 2. Domain Layer (Partially Complete)
- ✅ Business models
- ❌ Use cases
- ❌ Repository interfaces

### 3. Data Layer (Partially Complete)
- ✅ Room entities
- ❌ DAOs
- ❌ Repository implementations
- ❌ Remote data sources
- ❌ Local data sources

## Target User Features

### Core Functionality
1. **Transaction Management**
   - Add/edit/delete transactions
   - Categorization with 37+ categories
   - Recurring transaction support
   - Receipt attachment

2. **Budget Planning**
   - Monthly budget creation
   - Category-wise allocation
   - Budget templates (50/30/20, Zero-based, OPT Student)
   - Real-time tracking and alerts

3. **Financial Goals**
   - Savings goal creation and tracking
   - Emergency fund planning
   - Investment tracking

4. **Analytics & Reporting**
   - Spending analysis
   - Category breakdowns
   - Trend visualization
   - Budget vs actual reports

5. **OPT/Visa Specific Features**
   - H1B application expense tracking
   - Visa fee management
   - Employment status tracking

## Next Development Steps

1. **Database Setup**
   - Create Room DAOs
   - Set up Database class
   - Implement type converters

2. **Repository Layer**
   - Local and remote data sources
   - Repository implementations
   - Offline sync logic

3. **Use Cases**
   - Transaction management use cases
   - Budget calculation use cases
   - Goal tracking use cases

4. **UI Implementation**
   - Main navigation
   - Transaction screens
   - Budget planning screens
   - Analytics dashboard

5. **Firebase Integration**
   - Authentication setup
   - Firestore integration
   - Cloud storage setup

This documentation serves as the single source of truth for the Budget Tracker Android project structure and prevents hallucinations about non-existent components.
