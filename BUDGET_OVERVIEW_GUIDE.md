# Mobile Budget Overview Screen - Implementation Guide

## Overview

The **Mobile Budget Overview Screen** (`MobileBudgetOverviewScreen.kt`) provides a clean, mobile-responsive interface for managing essential expenses and subscriptions. This screen matches the design specifications with month navigation, current balance tracking, and comprehensive expense/subscription management.

## Features

### 1. Month Navigation Header
- **Left/Right chevron buttons** to navigate between months
- **Current month display** (e.g., "October 2025")
- **"Budget Overview" subtitle** in gray text
- Clean white card with subtle elevation

### 2. Current Balance Card
- **Large green balance display** ($2850.75 format)
- **Two-column breakdown:**
  - Left: Expenses (negative red amount)
  - Right: Subscriptions (negative red amount)
- **Bottom row:** Remaining balance (green if positive, red if negative)
- **Edit button** in top-right corner to update balance

### 3. Essential Expenses Section
- **Purple shopping cart icon** with "Essential Expenses" header
- **Add button (+)** in top-right to add new expenses
- **Expense items show:**
  - Name (bold)
  - Category label in gray (Food, Transportation, Utilities, etc.)
  - "Fixed" badge for recurring expenses (if dueDay is set)
  - Dollar amount (right-aligned)
  - Edit icon (pencil)
  - Delete icon (trash can) in red
- Light gray background cards with rounded corners

### 4. Subscriptions Section
- **Purple subscription icon** with "Subscriptions" header
- **Add button (+)** to add new subscriptions
- **Subscription cards display:**
  - Service name (e.g., Netflix, Spotify)
  - Black "active" badge
  - Recurring icon (refresh symbol)
  - Next billing date with reminder countdown
  - Monthly price with "/monthly" label
  - Alert/bell icon (orange) for upcoming reminders
  - Edit and Delete icons
- **Bottom summary:** "Active Monthly" total
- Color-coded reminders:
  - Orange bell for upcoming (≤7 days)
  - Red text for urgent (≤3 days)

## Architecture

### Files Structure

```
app/src/main/java/com/budgettracker/
├── features/budget/presentation/
│   ├── MobileBudgetOverviewScreen.kt      # New mobile-first UI
│   ├── BudgetOverviewViewModel.kt         # State management (Hilt)
│   ├── EnhancedBudgetOverviewScreen.kt   # Alternative advanced UI
│   └── BudgetOverviewScreen.kt           # Original simple version
├── core/
│   ├── data/
│   │   ├── repository/
│   │   │   └── BudgetOverviewRepository.kt  # Business logic
│   │   └── local/
│   │       ├── dao/
│   │       │   └── BudgetOverviewDao.kt     # Database queries
│   │       └── entities/
│   │           ├── EssentialExpenseEntity.kt
│   │           ├── EnhancedSubscriptionEntity.kt
│   │           ├── PaycheckEntity.kt
│   │           └── BalanceEntity.kt
│   └── domain/model/
└── navigation/
    └── BudgetTrackerNavigation.kt          # Route: budget_overview
```

### Data Flow

1. **UI Layer** (`MobileBudgetOverviewScreen`)
   - Collects state from ViewModel
   - Displays data in cards and lists
   - Handles user interactions (add, edit, delete)
   - Shows dialogs for data entry

2. **ViewModel Layer** (`BudgetOverviewViewModel`)
   - Manages UI state (`BudgetOverviewUiState`)
   - Observes real-time data changes via Flows
   - Handles user actions (CRUD operations)
   - Coordinates with repository

3. **Repository Layer** (`BudgetOverviewRepository`)
   - Offline-first approach
   - Room database for local persistence
   - Firebase Firestore for cloud sync
   - Background sync with error handling

4. **Database Layer**
   - Room DAOs for CRUD operations
   - Flow-based reactive queries
   - Complex dashboard summary queries

## Domain Models

### EssentialExpenseEntity
```kotlin
data class EssentialExpenseEntity(
    val id: String,
    val userId: String,
    val name: String,
    val category: ExpenseCategory,
    val plannedAmount: Double,
    val actualAmount: Double? = null,
    val dueDay: Int? = null,        // Day of month (1-31)
    val paid: Boolean = false,
    val period: String,              // "2024-10" format
    val reminderDaysBefore: List<Int>? = listOf(3, 1),
    val fcmReminderEnabled: Boolean = true,
    val notes: String? = null
)

enum class ExpenseCategory {
    RENT, UTILITIES, GROCERIES, PHONE, 
    INSURANCE, TRANSPORTATION, OTHER
}
```

### EnhancedSubscriptionEntity
```kotlin
data class EnhancedSubscriptionEntity(
    val id: String,
    val userId: String,
    val name: String,
    val amount: Double,
    val currency: String = "USD",
    val frequency: BillingFrequency,
    val nextBillingDate: Long,       // Epoch millis
    val reminderDaysBefore: List<Int> = listOf(1, 3, 7),
    val fcmReminderEnabled: Boolean = true,
    val active: Boolean = true,
    val iconEmoji: String? = null,
    val category: String = "Entertainment"
)

enum class BillingFrequency {
    WEEKLY, BI_WEEKLY, MONTHLY, 
    QUARTERLY, SEMI_ANNUAL, YEARLY
}
```

### BalanceEntity
```kotlin
data class BalanceEntity(
    val userId: String,
    val currentBalance: Double,
    val lastUpdatedBy: String,      // "user", "expense", "paycheck"
    val updatedAt: Long,
    val pendingSync: Boolean = true
)
```

## Key Functions

### BudgetOverviewViewModel

#### Data Loading
```kotlin
fun refreshData()                    // Reload all data
private fun loadBudgetData()         // Initial load
private fun observeDataChanges()     // Real-time updates
```

#### Balance Management
```kotlin
fun showEditBalanceDialog()
fun hideEditBalanceDialog()
fun updateBalance(newBalance: Double)
```

#### Essential Expenses
```kotlin
fun addEssentialExpense(name, category, amount, dueDay)
fun deleteEssentialExpense(expenseId)
fun markEssentialPaid(expenseId, actualAmount?)
```

#### Subscriptions
```kotlin
fun addSubscription(name, amount, frequency, nextBillingDate, category, iconEmoji)
```

#### Calculations
```kotlin
fun getHealthScore(): Int              // Financial health 0-100
fun getTotalMonthlySubscriptions(): Double
fun getNetRemaining(): Double          // Income - expenses
fun getFormattedLastUpdated(): String
```

### BudgetOverviewRepository

#### Essential Expenses
```kotlin
suspend fun getEssentialExpenses(period: String?): Result<List<EssentialExpenseEntity>>
fun getEssentialExpensesFlow(period: String?): Flow<List<EssentialExpenseEntity>>
suspend fun addEssentialExpense(name, category, plannedAmount, dueDay, reminderDaysBefore)
suspend fun updateEssentialExpense(expenseId, name?, category?, plannedAmount?, dueDay?)
suspend fun deleteEssentialExpense(expenseId): Result<Unit>
suspend fun markEssentialPaid(expenseId, actualAmount?): Result<Unit>
```

#### Subscriptions
```kotlin
suspend fun getActiveSubscriptions(): Result<List<EnhancedSubscriptionEntity>>
fun getActiveSubscriptionsFlow(): Flow<List<EnhancedSubscriptionEntity>>
suspend fun addSubscription(name, amount, frequency, nextBillingDate, category, reminderDaysBefore, iconEmoji)
```

#### Balance
```kotlin
suspend fun getCurrentBalance(): Result<Double>
fun getBalanceFlow(): Flow<Double>
suspend fun updateBalance(newBalance, updatedBy): Result<Unit>
```

#### Dashboard Queries
```kotlin
suspend fun getDashboardSummary(): Result<DashboardSummary>
suspend fun getUpcomingTimeline(limit): Result<List<TimelineItem>>
```

## UI Components

### Dialog Components

1. **EditBalanceDialog** - Update current balance
2. **AddExpenseDialog** - Create new essential expense
   - Name, category, amount fields
   - Optional: Fixed checkbox and due day
3. **AddSubscriptionDialog** - Create new subscription
   - Name, amount, frequency, icon emoji
   - Auto-calculated next billing date
4. **ConfirmDeleteDialog** - Delete confirmation with warning

### Card Components

1. **MonthNavigationHeader** - Navigate between months
2. **CurrentBalanceCard** - Balance with breakdown
3. **EssentialExpensesCard** - List of expenses
4. **SubscriptionsCard** - List of subscriptions with summary
5. **EssentialExpenseItem** - Individual expense row
6. **SubscriptionItem** - Individual subscription row with billing info

## Usage Examples

### Adding a New Expense
```kotlin
viewModel.addEssentialExpense(
    name = "Rent",
    category = ExpenseCategory.RENT,
    plannedAmount = 708.95,
    dueDay = 31                      // Fixed recurring on 31st
)
```

### Adding a Subscription
```kotlin
val nextBillingDate = Calendar.getInstance().apply {
    add(Calendar.DAY_OF_MONTH, 30)
}.timeInMillis

viewModel.addSubscription(
    name = "Netflix",
    amount = 15.99,
    frequency = BillingFrequency.MONTHLY,
    nextBillingDate = nextBillingDate,
    category = "Entertainment",
    iconEmoji = "📺"
)
```

### Updating Balance
```kotlin
viewModel.updateBalance(2850.75)
```

### Marking Expense as Paid
```kotlin
viewModel.markEssentialPaid(
    expenseId = "expense_id_123",
    actualAmount = 708.95           // Or null to use planned
)
```

## Styling & Design

### Color Scheme
- **Primary Purple:** `#6f42c1` (icons, headers)
- **Green:** `#28a745` (positive amounts, balance)
- **Red:** `#dc3545` (negative amounts, delete)
- **Orange:** `#ff9800` (warnings, reminders)
- **Gray backgrounds:** `#F5F5F5` (page), `#F8F9FA` (cards)
- **White:** Card backgrounds

### Typography
- **Large balance:** 42sp, Bold
- **Section headers:** TitleMedium, Bold
- **Body text:** BodyLarge/Medium
- **Secondary text:** BodySmall, Gray

### Spacing
- **Card padding:** 20dp
- **Section spacing:** 20dp vertical
- **Item spacing:** 12dp vertical
- **Icon sizes:** 18-24dp

### Elevation
- **Cards:** 4dp
- **Dialogs:** 8dp

## Firebase Integration

### Firestore Collections Structure
```
users/{userId}/
  ├── balance/current
  ├── essentials/{expenseId}
  ├── subscriptions/{subscriptionId}
  ├── paychecks/{paycheckId}
  ├── reminders/{reminderId}
  └── devices/{deviceId}
```

### Sync Strategy
1. **Write:** Local first (Room), then Firebase
2. **Read:** Room + Flow for real-time updates
3. **Conflict Resolution:** Server timestamp wins
4. **Offline Support:** Queue pending syncs

## Testing

### Manual Testing Checklist
- [ ] Month navigation works forward/backward
- [ ] Current balance displays correctly
- [ ] Expenses/subscriptions breakdown calculates accurately
- [ ] Add expense dialog saves and displays
- [ ] Add subscription dialog saves with correct billing date
- [ ] Edit balance updates immediately
- [ ] Delete confirmation prevents accidental deletion
- [ ] Fixed badge appears for expenses with dueDay
- [ ] Reminder indicators show for upcoming subscriptions
- [ ] Active Monthly total is correct

### Sample Data
Use `viewModel.seedSampleData()` to populate test data:
- Rent: $708.95 (due 31st)
- Electric: $85.00 (due 15th)
- Groceries: $400.00 (flexible)
- Spotify: $11.99 (monthly)
- Phone Plan: $150.00 (semi-annual)
- Initial balance: $393.97
- Upcoming paychecks

## Navigation Integration

The screen is accessible via:
- **Route:** `BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE`
- **Bottom Navigation:** "Budget" tab (3rd position)
- **Icon:** PieChart icon

```kotlin
// In BudgetTrackerNavigation.kt
composable(BudgetTrackerDestinations.BUDGET_OVERVIEW_ROUTE) {
    MobileBudgetOverviewScreen()
}
```

## Performance Considerations

1. **Flow-based Updates:** Automatic UI refresh on data changes
2. **Lazy Loading:** LazyColumn for efficient scrolling
3. **Minimal Recomposition:** StateFlow with distinct values
4. **Background Sync:** Firebase operations off main thread
5. **Error Handling:** Graceful fallbacks for network issues

## Future Enhancements

### Planned Features
- [ ] Expense categories customization
- [ ] Recurring expense templates
- [ ] Subscription pause/resume
- [ ] Export to CSV/PDF
- [ ] Budget vs. actual charts
- [ ] Multi-currency support
- [ ] Shared budgets (family mode)
- [ ] Bill splitting
- [ ] Receipt attachments
- [ ] Auto-categorization with ML

### UI Improvements
- [ ] Swipe actions for edit/delete
- [ ] Drag-and-drop reordering
- [ ] Dark mode support
- [ ] Tablet/landscape layouts
- [ ] Animated transitions
- [ ] Pull-to-refresh
- [ ] Search and filter

## Troubleshooting

### Common Issues

**Issue:** Balance not updating after marking expense paid
- **Solution:** Check Firebase auth state and network connectivity

**Issue:** Subscriptions not showing reminder icons
- **Solution:** Verify `nextBillingDate` is in the future and `fcmReminderEnabled` is true

**Issue:** Fixed badge not appearing
- **Solution:** Ensure `dueDay` is not null (1-31)

**Issue:** Navigation not working
- **Solution:** Verify Hilt ViewModel injection is working correctly

## References

- [Room Database Documentation](https://developer.android.com/training/data-storage/room)
- [Firebase Firestore Guide](https://firebase.google.com/docs/firestore)
- [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Flows](https://kotlinlang.org/docs/flow.html)

## Support

For questions or issues:
1. Check existing Budget Overview screens for patterns
2. Review ViewModel and Repository implementations
3. Test with sample data using `seedSampleData()`
4. Verify Firebase configuration in `google-services.json`

