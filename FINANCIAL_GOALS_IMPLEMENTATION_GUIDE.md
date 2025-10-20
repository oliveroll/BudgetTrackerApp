# Financial Goals Feature - Implementation Guide

## 📋 Overview

This document provides a comprehensive guide to the **Financial Goals** feature implementation, including all 4 tabs, their architecture, Firebase integration, and usage instructions.

---

## 🎯 Feature Tabs

### 1. Freedom Debt Journey (Debt Loan Tracking)
- **Purpose:** Track and manage debt payoff journey
- **Features:**
  - Add/edit/delete multiple loans
  - Record payments with principal/interest breakdown
  - View amortization schedule (month-by-month breakdown)
  - Simulate different payment amounts
  - Calculate projected payoff dates
  - Progress bar showing % paid off

### 2. Roth IRA (Retirement Contributions)
- **Purpose:** Track IRA contributions and maximize annual limits
- **Features:**
  - Manage IRA accounts across tax years
  - Track contributions toward annual limit ($7,000 for 2024)
  - Calculate required contributions to max out by year-end
  - Biweekly and monthly contribution recommendations
  - Recurring contribution setup
  - Progress bar showing contribution progress

### 3. Emergency Fund (Safety Net Savings)
- **Purpose:** Build and track emergency fund with compound interest
- **Features:**
  - Set target goals and track progress
  - Record deposits (manual and automatic)
  - Project growth with compound interest calculations
  - APY tracking and compounding frequency
  - 12-month projection schedule
  - Calculate months to reach goal

### 4. ETF Portfolio (Investment Tracking)
- **Purpose:** Manage investment portfolios with holdings and transactions
- **Features:**
  - Multiple portfolio support (Brokerage, IRA, 401k)
  - Track individual holdings (VOO, VTI, VXUS, etc.)
  - Record buy/sell transactions
  - Portfolio allocation pie chart
  - Performance tracking (gain/loss %)
  - Rebalancing suggestions
  - Dividend income and expense ratio tracking

---

## 🏗️ Architecture

### ✅ **Data Layer (COMPLETED)**

#### Domain Models (`core/domain/model/`)
- **DebtLoan.kt**: Loan with amortization logic
- **RothIRA.kt**: IRA with contribution calculations
- **EmergencyFund.kt**: Savings with compound interest
- **ETFPortfolio.kt**: Portfolio with holdings and allocations

#### Room Entities (`core/data/local/entities/`)
- **DebtLoanEntity + DebtPaymentRecordEntity**
- **RothIRAEntity + IRAContributionEntity**
- **EmergencyFundEntity + EmergencyFundDepositEntity**
- **ETFPortfolioEntity + ETFHoldingEntity + InvestmentTransactionEntity**

#### DAOs (`core/data/local/dao/`)
- **FinancialGoalsDao.kt**: All DAOs for financial goals
  - `DebtLoanDao`, `RothIRADao`, `EmergencyFundDao`, `ETFPortfolioDao`
  - Flow-based reactive queries
  - Pending sync tracking

#### Repositories (`core/data/repository/`)
- **FinancialGoalsRepository.kt**: DebtLoan + RothIRA repositories
- **EmergencyFundRepository.kt**: Emergency fund repository
- **ETFPortfolioRepository.kt**: Portfolio + holdings repository
- **Full Firebase Firestore CRUD operations**
- **Offline-first with background sync**

### ✅ **Presentation Layer (COMPLETED)**

#### ViewModels (`features/financialgoals/presentation/`)
- **DebtJourneyViewModel.kt**: Debt management with payment simulation
- **RothIRAViewModel.kt**: IRA management with contribution calculator
- **EmergencyFundViewModel.kt**: Emergency fund with projections
- **ETFPortfolioViewModel.kt**: Portfolio with performance tracking

### 🚧 **UI Layer (IN PROGRESS)**

#### Screens (`features/financialgoals/presentation/`)
- **DebtJourneyScreen.kt**: Freedom Debt Journey UI
- **RothIRAScreen.kt**: Roth IRA management UI
- **EmergencyFundScreen.kt**: Emergency fund UI with charts
- **ETFPortfolioScreen.kt**: Portfolio UI with pie chart
- **FinancialGoalsMainScreen.kt**: Tab container

---

## 🔥 Firebase Integration

### Firestore Collections Structure

```
users/
└── {userId}/
    ├── debtLoans/
    │   └── {loanId}/
    ├── debtPayments/
    │   └── {paymentId}/
    ├── rothIRAs/
    │   └── {iraId}/
    ├── iraContributions/
    │   └── {contributionId}/
    ├── emergencyFunds/
    │   └── {fundId}/
    ├── emergencyFundDeposits/
    │   └── {depositId}/
    ├── etfPortfolios/
    │   └── {portfolioId}/
    ├── etfHoldings/
    │   └── {holdingId}/
    └── investmentTransactions/
        └── {transactionId}/
```

### Firebase Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check user ownership
    function isOwner(userId) {
      return request.auth != null && request.auth.uid == userId;
    }
    
    // User data
    match /users/{userId} {
      allow read, write: if isOwner(userId);
      
      // Debt Loans
      match /debtLoans/{loanId} {
        allow read, write: if isOwner(userId);
      }
      
      match /debtPayments/{paymentId} {
        allow read, write: if isOwner(userId);
      }
      
      // Roth IRA
      match /rothIRAs/{iraId} {
        allow read, write: if isOwner(userId);
      }
      
      match /iraContributions/{contributionId} {
        allow read, write: if isOwner(userId);
      }
      
      // Emergency Fund
      match /emergencyFunds/{fundId} {
        allow read, write: if isOwner(userId);
      }
      
      match /emergencyFundDeposits/{depositId} {
        allow read, write: if isOwner(userId);
      }
      
      // ETF Portfolio
      match /etfPortfolios/{portfolioId} {
        allow read, write: if isOwner(userId);
      }
      
      match /etfHoldings/{holdingId} {
        allow read, write: if isOwner(userId);
      }
      
      match /investmentTransactions/{transactionId} {
        allow read, write: if isOwner(userId);
      }
    }
  }
}
```

---

## 📱 UI Implementation Pattern

### Example: Debt Journey Screen Structure

```kotlin
@Composable
fun DebtJourneyScreen(
    viewModel: DebtJourneyViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Freedom Debt Journey") },
                actions = {
                    IconButton(onClick = { viewModel.toggleAddDialog() }) {
                        Icon(Icons.Default.Add, "Add Loan")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Summary Card
            DebtSummaryCard(loans = uiState.loans)
            
            // Loans List
            LazyColumn {
                items(uiState.loans) { loan ->
                    DebtLoanCard(
                        loan = loan,
                        onLoanClick = { viewModel.selectLoan(loan) },
                        onEditClick = { /* Open edit dialog */ },
                        onPaymentClick = { /* Open payment dialog */ }
                    )
                }
            }
        }
        
        // Dialogs
        if (uiState.showAddDialog) {
            AddLoanDialog(
                onDismiss = { viewModel.toggleAddDialog() },
                onConfirm = { loan -> viewModel.addLoan(loan) }
            )
        }
        
        // Selected Loan Detail (Bottom Sheet or Separate Screen)
        uiState.selectedLoan?.let { loan ->
            LoanDetailBottomSheet(
                loan = loan,
                onDismiss = { viewModel.selectLoan(null) },
                onRecordPayment = { amount, principal, interest ->
                    viewModel.recordPayment(loan.id, amount, principal, interest)
                }
            )
        }
    }
}
```

### Key UI Components

1. **Summary Cards**: Aggregated metrics at the top
2. **List View**: Scrollable list of items (loans, IRAs, funds, portfolios)
3. **Detail View**: Bottom sheet or separate screen for item details
4. **Add/Edit Dialogs**: Modal forms for CRUD operations
5. **Progress Indicators**: Visual progress bars and charts
6. **Charts**: Line charts for projections, pie charts for allocations

---

## 🎨 UI Design Guidelines

### Material Design 3 Components
- `Scaffold` with `TopAppBar`
- `Card` with elevation for each item
- `LinearProgressIndicator` for progress tracking
- `FloatingActionButton` for primary actions
- `Dialog` and `ModalBottomSheet` for forms
- `LazyColumn` for lists
- `Tab` and `TabRow` for navigation between goal types

### Color Scheme
- **Primary**: Material3 Primary color
- **Success**: `Color(0xFF28a745)` - Green for positive metrics
- **Warning**: `Color(0xFFfd7e14)` - Orange for alerts
- **Error**: `Color(0xFFdc3545)` - Red for debt/losses
- **Info**: `Color(0xFF17a2b8)` - Blue for information

### Typography
- **Headline**: For titles and main headers
- **Body**: For descriptions and content
- **Label**: For form labels and metadata
- **Display**: For large numbers and metrics

---

## 🧪 Testing Approach

### Mock Data for Testing

#### Debt Loan Example
```kotlin
val mockLoan = DebtLoan(
    id = "loan-1",
    userId = "user-123",
    loanProvider = "KfW",
    loanType = "Studienkredit",
    accountNumber = "****1234",
    originalAmount = 10442.51,
    currentBalance = 10317.64,
    interestRate = 6.66,
    repaymentStartDate = Date(),
    currentMonthlyPayment = 121.37,
    adjustedMonthlyPayment = 900.0,
    nextPaymentDueDate = Date(),
    minimumPayment = 121.37,
    currency = "EUR"
)
```

#### Roth IRA Example
```kotlin
val mockIRA = RothIRA(
    id = "ira-1",
    userId = "user-123",
    brokerageName = "Vanguard",
    accountNumber = "****5678",
    annualContributionLimit = 7000.0,
    contributionsThisYear = 3500.0,
    currentBalance = 25000.0,
    taxYear = 2024,
    recurringContributionAmount = 250.0,
    recurringContributionFrequency = ContributionFrequency.BIWEEKLY
)
```

---

## 🚀 Usage Instructions

### For Users

1. **Add a Goal**: Tap the `+` button in any tab
2. **View Details**: Tap on any item to see full details
3. **Record Transactions**: Use the action buttons to record payments/contributions/deposits
4. **Track Progress**: View progress bars and projections
5. **Edit Goals**: Long-press or use edit icon to modify
6. **Delete Goals**: Use delete option in detail view

### For Developers

1. **Initialize Repository**:
   ```kotlin
   viewModel.loadLoans() // or loadIRAs(), loadFunds(), loadPortfolios()
   ```

2. **Add New Item**:
   ```kotlin
   val newLoan = DebtLoan(/* ... */)
   viewModel.addLoan(newLoan)
   ```

3. **Update Item**:
   ```kotlin
   val updatedLoan = existingLoan.copy(currentBalance = newBalance)
   viewModel.updateLoan(updatedLoan)
   ```

4. **Record Transaction**:
   ```kotlin
   viewModel.recordPayment(loanId, amount, principalPaid, interestPaid)
   ```

---

## 📊 Feature Capabilities

### Debt Journey
- ✅ Multiple loans support
- ✅ Amortization schedule (24+ months)
- ✅ Payment simulation (what-if scenarios)
- ✅ Progress tracking (% paid off)
- ✅ Payment history
- ✅ Interest vs principal breakdown

### Roth IRA
- ✅ Multi-year tracking
- ✅ Automatic contribution limit enforcement
- ✅ Contribution frequency support (weekly, biweekly, monthly)
- ✅ Calculator: required contributions to max out
- ✅ Recurring contribution setup
- ✅ Tax year management

### Emergency Fund
- ✅ Compound interest projections
- ✅ Multiple compounding frequencies (daily, monthly, annually)
- ✅ Configurable projection timelines (12-60 months)
- ✅ Goal tracking with progress bars
- ✅ APY tracking
- ✅ Automatic deposit setup

### ETF Portfolio
- ✅ Multiple portfolios support
- ✅ Holdings tracking (ticker, shares, prices)
- ✅ Buy/sell transaction recording
- ✅ Portfolio allocation tracking
- ✅ Performance metrics (gain/loss %, total value)
- ✅ Dividend income tracking
- ✅ Expense ratio tracking
- ✅ Rebalancing suggestions (future)

---

## 🔧 Integration Steps

### 1. Add to Navigation
```kotlin
sealed class Screen(val route: String) {
    object FinancialGoals : Screen("financial_goals")
}

// In NavHost
composable(Screen.FinancialGoals.route) {
    FinancialGoalsMainScreen()
}
```

### 2. Add Bottom Navigation Item (Optional)
```kotlin
BottomNavigationItem(
    icon = { Icon(Icons.Default.TrendingUp, "Goals") },
    label = { Text("Goals") },
    selected = selectedRoute == Screen.FinancialGoals.route,
    onClick = { navController.navigate(Screen.FinancialGoals.route) }
)
```

### 3. Deploy Firebase Rules
```bash
firebase deploy --only firestore:rules
```

### 4. Initialize Data on First Launch
```kotlin
viewModelScope.launch {
    debtLoanRepository.initializeFromFirebase()
    rothIRARepository.initializeFromFirebase()
    emergencyFundRepository.initializeFromFirebase()
    etfPortfolioRepository.initializeFromFirebase()
}
```

---

## 📝 Next Steps

1. ✅ **Data Layer**: Complete (models, entities, DAOs, repositories)
2. ✅ **ViewModels**: Complete (all 4 tabs with state management)
3. 🚧 **UI Screens**: In progress (need to implement all 4 tab UIs)
4. ⏳ **Firebase Rules**: Ready to deploy
5. ⏳ **Navigation Integration**: Need to add to app navigation
6. ⏳ **Testing**: Unit tests for ViewModels and repositories
7. ⏳ **Documentation**: User guide and API documentation

---

## 🤝 Contributing

When implementing UI screens, follow these patterns:
1. Use `hiltViewModel()` to inject ViewModels
2. Collect UI state with `collectAsState()`
3. Use `remember` for local UI state
4. Implement proper error handling and loading states
5. Follow Material Design 3 guidelines
6. Ensure accessibility (content descriptions, touch targets)

---

## 📚 References

- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Material Design 3](https://m3.material.io/)
- [Firebase Firestore](https://firebase.google.com/docs/firestore)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

