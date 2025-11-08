# ETF Portfolio Feature Status

## Current State: ⚠️ IN DEVELOPMENT

The ETF Portfolio feature is **planned but not yet implemented**. Users who navigate to this tab will see a clean "In Development" placeholder screen.

---

## What Users See

When navigating to **Financial Goals → ETF Portfolio**, users see:

- 📈 **Icon**: Chart/graph icon indicating investment tracking
- **Title**: "ETF Portfolio"
- **Message**: "This feature is still in development"
- **Description**: Brief explanation of planned functionality
- **Badge**: "Coming Soon" indicator

---

## Technical Implementation

### 1. **Placeholder Screen**
**File**: `app/src/main/java/com/budgettracker/features/financialgoals/presentation/ETFPortfolioScreen.kt`

```kotlin
@Composable
fun ETFPortfolioScreen() {
    ETFPortfolioPlaceholder()
}
```

**Design**:
- Material Design 3 card with elevated surface
- Centered layout responsive to all screen sizes
- Dark/light theme support via MaterialTheme
- Matches app's existing design language

### 2. **Stubbed ViewModel**
**File**: `app/src/main/java/com/budgettracker/features/financialgoals/presentation/ETFPortfolioViewModel.kt`

**Status**: Fully stubbed with empty implementations

**Key Points**:
- No Firebase/Firestore operations attempted
- Returns empty UI state by default
- All methods are no-ops (safe to call, do nothing)
- No dependency on `ETFPortfolioRepository` constructor injection

**Purpose**: Prevents crashes if other code accidentally tries to use the ViewModel.

### 3. **Navigation**
**Route**: `BudgetTrackerDestinations.FINANCIAL_GOALS_ROUTE` → Tab 3

**Access Path**:
1. User taps "Goals" in bottom navigation
2. `FinancialGoalsMainScreen` loads with 4 tabs
3. User taps "ETF Portfolio" tab (index 3)
4. Placeholder screen renders

**Safety**: Navigation is fully safe - no data loading or Firebase calls occur.

---

## Planned Features (Future Implementation)

When ETF Portfolio is implemented, it will support:

### Core Functionality
- **Portfolio Creation**: Link brokerage accounts (Fidelity, Vanguard, Schwab, etc.)
- **Holdings Tracking**: Monitor ETF positions (ticker, shares, cost basis)
- **Performance Metrics**: 
  - Total portfolio value
  - Gain/loss ($ and %)
  - Dividend income tracking
  - Expense ratios
- **Asset Allocation**: View diversification across holdings
- **Rebalancing Alerts**: Notify when allocation drifts from target

### Advanced Features
- **Auto-Investment Planning**: Set recurring investment schedules
- **Tax Loss Harvesting**: Identify opportunities to offset gains
- **Dividend Reinvestment Tracking**: DRIP management
- **Transaction History**: Buy/sell records with cost basis updates
- **Multi-Portfolio Support**: Separate portfolios (taxable, IRA, 401k, etc.)

---

## Data Models (Already Defined)

The domain models exist but are **not actively used**:

### ETFPortfolio
**File**: `app/src/main/java/com/budgettracker/core/domain/model/ETFPortfolio.kt`

```kotlin
data class ETFPortfolio(
    val id: String,
    val userId: String,
    val brokerageName: String,
    val accountNumber: String,
    val accountType: String, // Brokerage, IRA, 401k
    val targetMonthlyInvestment: Double,
    val recurringInvestmentFrequency: InvestmentFrequency,
    val autoInvest: Boolean,
    val targetAllocation: Map<String, Double>, // ticker -> %
    val rebalanceThreshold: Double,
    val investmentHorizon: InvestmentHorizon,
    val riskTolerance: RiskTolerance,
    // ...
)
```

### ETFHolding
```kotlin
data class ETFHolding(
    val id: String,
    val portfolioId: String,
    val ticker: String, // VOO, VTI, etc.
    val name: String,
    val sharesOwned: Double,
    val averageCostBasis: Double,
    val currentPrice: Double,
    val expenseRatio: Double,
    // ...
)
```

### InvestmentTransaction
```kotlin
data class InvestmentTransaction(
    val id: String,
    val holdingId: String,
    val type: TransactionType, // BUY, SELL, DIVIDEND, SPLIT
    val shares: Double,
    val pricePerShare: Double,
    val totalAmount: Double,
    val fees: Double,
    val date: Date
)
```

---

## Database Schema (Defined, Not Used)

### Room Entities
**File**: `app/src/main/java/com/budgettracker/core/data/local/entities/ETFPortfolioEntity.kt`

**Tables** (exist in schema but not actively queried):
- `etf_portfolios`
- `etf_holdings`
- `investment_transactions`

### Firebase Collections
**Structure** (defined but not syncing):
```
/users/{userId}/
    ├── portfolios/{portfolioId}
    ├── holdings/{holdingId}
    └── investment_transactions/{transactionId}
```

---

## Why Not Implemented Yet

### Priority Considerations
1. **Core Features First**: Dashboard, Transactions, Budget, and Savings Goals take priority
2. **Complexity**: ETF tracking requires:
   - Real-time price data integration (API costs)
   - Complex allocation calculations
   - Multi-account management
   - Tax reporting considerations
3. **User Research**: Need to validate demand and specific use cases
4. **External Dependencies**: 
   - Stock price APIs (Alpha Vantage, IEX Cloud, etc.)
   - Brokerage integrations (Plaid for account linking)

### Development Roadmap
- **Phase 1** (Current): Other Financial Goals tabs (Debt, Roth IRA, Emergency Fund)
- **Phase 2**: ETF Portfolio basic tracking (manual entry)
- **Phase 3**: Price API integration for live updates
- **Phase 4**: Brokerage account linking (if feasible)

---

## Testing the Placeholder

### Manual Testing Steps
1. ✅ Build and install app
2. ✅ Navigate to **Goals** tab (bottom navigation)
3. ✅ Tap "ETF Portfolio" tab
4. ✅ Verify placeholder screen displays:
   - Chart icon
   - "ETF Portfolio" title
   - "This feature is still in development" message
   - "Coming Soon" badge
5. ✅ Verify no crashes or errors
6. ✅ Test in both light and dark themes
7. ✅ Test on different screen sizes (phone, tablet)

### Expected Behavior
- ✅ Smooth navigation with no lag
- ✅ No Firebase queries or network calls
- ✅ Clean, professional placeholder UI
- ✅ Consistent with app's design system
- ✅ Responsive layout

---

## Future Implementation Checklist

When ready to implement ETF Portfolio:

### Backend
- [ ] Implement `ETFPortfolioRepository` with Firebase sync
- [ ] Set up price data API integration
- [ ] Create DAOs for Room database queries
- [ ] Add background sync for price updates

### Frontend
- [ ] Replace placeholder with full `ETFPortfolioScreen`
- [ ] Implement add/edit portfolio dialogs
- [ ] Create holding management UI
- [ ] Build transaction recording interface
- [ ] Design performance charts (gain/loss over time)
- [ ] Add asset allocation pie chart

### Data
- [ ] Seed sample portfolios for testing
- [ ] Set up Firestore security rules
- [ ] Implement offline caching strategy

### Testing
- [ ] Unit tests for repository
- [ ] ViewModel tests
- [ ] UI tests for main flows
- [ ] Integration tests with Firebase

---

## Related Files

### Active Files (Placeholder Implementation)
- `ETFPortfolioScreen.kt` - Placeholder UI
- `ETFPortfolioViewModel.kt` - Stubbed ViewModel
- `FinancialGoalsMainScreen.kt` - Tab navigation

### Defined But Inactive
- `ETFPortfolio.kt` - Domain model
- `ETFHolding.kt` - Holding model
- `ETFPortfolioEntity.kt` - Room entity
- `ETFPortfolioRepository.kt` - Repository interface
- `AddETFPortfolioDialog.kt` - Add portfolio dialog (not shown)

---

## Summary

✅ **Safe for users**: Clean placeholder, no broken UI  
✅ **Safe for developers**: Stubbed code won't crash  
⏳ **Coming soon**: Full implementation planned for future release  

The placeholder provides a professional user experience while the feature is under development, maintaining app quality and preventing user confusion.

