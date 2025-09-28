# Budget Tracker Android App

[![GitMCP](https://img.shields.io/endpoint?url=https://gitmcp.io/badge/oliveroll/BudgetTrackerApp&label=GitMCP&color=aquamarine)](https://gitmcp.io/oliveroll/BudgetTrackerApp)

A comprehensive Android budget tracking application designed specifically for international students on OPT/H1B visas to manage their finances effectively.

## 🚀 Features

### Core Functionality
- **📊 Transaction Management**: Add, edit, delete transactions with 37+ categories
- **💰 Budget Planning**: Monthly budgets with templates (50/30/20, Zero-based, OPT Student)
- **🎯 Financial Goals**: Savings goals and emergency fund tracking  
- **📈 Analytics**: Spending analysis and trend visualization
- **🛂 OPT/Visa Features**: H1B expenses, visa fee tracking

### OPT/Visa Student Specific
- Visa application expense tracking
- Income tracking for OPT compliance
- Budget templates for student finances
- Emergency fund planning for visa transitions

## 🏗️ Architecture

**Clean Architecture with MVVM Pattern**
- **Presentation Layer**: Jetpack Compose UI + ViewModels
- **Domain Layer**: Business logic, use cases, entities
- **Data Layer**: Room (local) + Firebase (cloud sync)

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **DI**: Hilt (Dagger)
- **Database**: Room + Firebase Firestore
- **Authentication**: Firebase Auth
- **Charts**: Vico Charts
- **Background Tasks**: WorkManager
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

## 📱 Transaction Categories

### Income
- Salary, Freelance, Investment Returns, Other Income

### Fixed Expenses  
- Rent, Utilities, Internet, Phone, Subscriptions, Loan Payments, Insurance

### Variable Expenses
- Groceries, Dining Out, Transportation, Entertainment, Clothing, Personal Care, Healthcare, Education

### Savings & Investments
- Emergency Fund, Retirement (401K, Roth IRA), Stocks/ETFs, Crypto, Travel Fund

### Visa-Specific
- H1B Application, Visa Fees

## 🏛️ Domain Models

### Transaction
```kotlin
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Double,
    val category: TransactionCategory,
    val type: TransactionType, // INCOME, EXPENSE
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
    val isDeleted: Boolean // Soft delete
)
```

### Budget
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
    val templateName: String? // "50/30/20", "Zero-based", "OPT Student"
)
```

## 📂 Project Structure

```
app/src/main/java/com/budgettracker/
├── BudgetTrackerApplication.kt     # Application class with Hilt
├── core/
│   ├── data/
│   │   └── local/entities/         # Room entities
│   ├── domain/
│   │   └── model/                  # Domain models
│   └── utils/                      # Constants & extensions
├── features/                       # Feature modules
│   ├── transaction/
│   ├── budget/
│   ├── analytics/
│   └── profile/
└── ui/                            # Compose components
```

## 🔧 Setup

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.9+
- Android SDK 24+ (target 34)
- Firebase project

### Installation
1. Clone the repository
```bash
git clone https://github.com/oliveroll/BudgetTrackerApp.git
cd BudgetTrackerApp
```

2. Set up Firebase
   - Create a Firebase project
   - Add `google-services.json` to `app/` directory
   - Enable Firestore, Auth, and Storage

3. Configure Android SDK
   - Update `local.properties` with your Android SDK path
   ```
   sdk.dir=/path/to/your/Android/Sdk
   ```

4. Sync and Run
   - Open project in Android Studio
   - Sync Gradle dependencies
   - Run on device/emulator

## 💡 Default Configuration (OPT Student)

```kotlin
// Financial defaults for international students
const val DEFAULT_MONTHLY_INCOME = 5470.0 // $5,470/month
const val DEFAULT_ANNUAL_SALARY = 80000.0 // $80,000/year
const val EMERGENCY_FUND_MONTHS = 6
const val ROTH_IRA_ANNUAL_LIMIT_2024 = 7000.0
const val TRADITIONAL_401K_LIMIT_2024 = 23000.0
```

## 📊 Budget Templates

### 50/30/20 Rule
- 50% Needs (rent, utilities, groceries)
- 30% Wants (entertainment, dining out) 
- 20% Savings & debt payment

### Zero-based Budget
- Every dollar assigned a purpose
- Income - expenses = $0

### OPT Student Budget
- Emergency fund priority
- Visa expense allocation
- Income compliance tracking

## 🔄 Development Status

### ✅ Completed
- Project structure setup
- Domain models (Transaction, Budget, UserProfile, etc.)
- Room entities with type converters
- Constants and configuration
- Build configuration with dependencies
- Application class with Hilt setup

### 🔄 In Progress
- Repository layer implementation
- Use cases/Interactors
- ViewModels with state management

### ❌ TODO
- Compose UI screens
- Navigation setup
- Firebase integration
- Room database DAOs
- Dependency injection modules
- Testing framework

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🎯 Target Users

International students and professionals on:
- OPT (Optional Practical Training)
- H1B visa
- Other work visas in the US

Managing finances while navigating visa requirements and building financial stability in the United States.

---

**Built with ❤️ for the international student community**

## 📚 External Resources

### Technology Documentation
- **Kotlin**: https://kotlinlang.org/docs/
- **Android Development**: https://developer.android.com/
- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **Material Design 3**: https://m3.material.io/
- **Firebase**: https://firebase.google.com/docs
- **Room Database**: https://developer.android.com/training/data-storage/room
- **Hilt Dependency Injection**: https://developer.android.com/training/dependency-injection/hilt-android

### Financial Resources for International Students
- **OPT Guidelines**: https://www.uscis.gov/working-in-the-united-states/students-and-exchange-visitors/optional-practical-training-opt-for-f-1-students
- **H1B Information**: https://www.uscis.gov/working-in-the-united-states/temporary-workers/h-1b-specialty-occupations
- **Tax Information for International Students**: https://www.irs.gov/individuals/international-taxpayers/students-and-scholars
- **Personal Finance Basics**: https://www.investopedia.com/personal-finance-4427760
