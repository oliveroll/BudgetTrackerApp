# 🔥 Firebase Firestore Schema Documentation

## Overview
This document defines the complete Firestore data structure for the Budget Tracker app, ensuring **user data separation**, **security**, and **offline-first sync** with Room database.

---

## 📊 Firestore Structure

### Root Collections
```
/users                          ← Main collection (one document per user)
```

### User Document Structure
```
/users/{userId}                 ← User UID from Firebase Auth
    ├── Profile Fields          ← Stored directly in user document
    │   ├── userId: string
    │   ├── email: string
    │   ├── name: string
    │   ├── profilePictureUrl: string?
    │   ├── currency: string (default: "USD")
    │   ├── monthlyIncome: number
    │   ├── netMonthlyIncome: number
    │   ├── baseSalary: number
    │   ├── company: string
    │   ├── employmentStatus: string ("OPT", "H1B", "GreenCard", etc.)
    │   ├── state: string
    │   ├── emergencyFundTarget: number
    │   ├── fcmToken: string (for push notifications)
    │   ├── createdAt: timestamp
    │   └── updatedAt: timestamp
    │
    └── Subcollections:
        ├── /transactions/{transactionId}           ← Income & Expense records
        ├── /essentialExpenses/{expenseId}          ← Fixed monthly expenses
        ├── /subscriptions/{subscriptionId}         ← Recurring subscriptions
        ├── /goals/{goalId}                         ← Financial goals (unified)
        │   ├── /debtLoans/{loanId}                ← Debt tracking
        │   ├── /rothIRAs/{iraId}                  ← Roth IRA accounts
        │   ├── /emergencyFunds/{fundId}           ← Emergency savings
        │   └── /etfPortfolios/{portfolioId}       ← ETF investments
        ├── /paychecks/{paycheckId}                ← Paycheck tracking
        └── /balance/{balanceId}                   ← Current balance snapshot
```

---

## 📝 Collection Schemas

### 1. `/users/{userId}/transactions/{transactionId}`

**Purpose**: All income and expense transactions

```typescript
{
  id: string,                    // UUID
  userId: string,                // Firebase Auth UID (redundant but useful)
  type: "INCOME" | "EXPENSE",
  category: string,              // SALARY, GROCERIES, RENT, etc.
  amount: number,
  description: string,
  date: timestamp,
  notes: string?,
  isRecurring: boolean,
  isDeleted: boolean,            // Soft delete
  linkedGoalId: string?,         // Link to goal if applicable
  createdAt: timestamp,
  updatedAt: timestamp
}
```

**Indexes**:
- `userId` + `date` (descending)
- `userId` + `type` + `date`
- `userId` + `category`

---

### 2. `/users/{userId}/essentialExpenses/{expenseId}`

**Purpose**: Fixed monthly expenses (rent, utilities, etc.)

```typescript
{
  id: string,
  userId: string,
  name: string,                  // "Rent IVY", "Utilities", etc.
  category: "RENT" | "UTILITIES" | "PHONE" | "INSURANCE" | "TRANSPORTATION" | "GROCERIES",
  plannedAmount: number,
  actualAmount: number?,
  dueDay: number,                // Day of month (1-31)
  paid: boolean,
  period: string,                // "2025-10" format
  reminderDaysBefore: number[],  // [1, 3, 7] days before
  fcmReminderEnabled: boolean,
  notes: string?,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Indexes**:
- `userId` + `period` + `paid`
- `userId` + `dueDay`

---

### 3. `/users/{userId}/subscriptions/{subscriptionId}`

**Purpose**: Recurring subscriptions (Spotify, Netflix, etc.)

```typescript
{
  id: string,
  userId: string,
  name: string,                  // "Spotify Premium", "Netflix HD"
  amount: number,
  currency: string,              // "USD", "EUR"
  frequency: "WEEKLY" | "BI_WEEKLY" | "MONTHLY" | "QUARTERLY" | "SEMI_ANNUAL" | "YEARLY",
  nextBillingDate: timestamp,
  isAutoPay: boolean,            // NEW: Auto-pay subscriptions roll forward
  active: boolean,
  iconEmoji: string?,            // "🎵", "📺"
  category: string,              // "Entertainment", "Utilities"
  reminderDaysBefore: number[],  // [1, 3, 7]
  fcmReminderEnabled: boolean,
  notes: string?,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Indexes**:
- `userId` + `active` + `nextBillingDate`
- `userId` + `frequency`

---

### 4. `/users/{userId}/goals/debtLoans/{loanId}`

**Purpose**: Debt tracking (student loans, car loans, etc.)

```typescript
{
  id: string,
  userId: string,
  loanProvider: string,          // "German Student Loan", "Bank of America"
  loanType: string,              // "Student", "Auto", "Personal"
  accountNumber: string?,
  originalAmount: number,
  currentBalance: number,
  interestRate: number,          // Annual percentage (6.66)
  monthlyPayment: number,
  minimumPayment: number,
  nextPaymentDate: timestamp,
  finalPaymentDate: timestamp?,
  currency: string,              // "USD", "EUR"
  isActive: boolean,
  notes: string?,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Subcollection**: `/payments/{paymentId}` - payment history

---

### 5. `/users/{userId}/goals/rothIRAs/{iraId}`

**Purpose**: Roth IRA contribution tracking

```typescript
{
  id: string,
  userId: string,
  brokerageName: string,         // "Fidelity", "Vanguard"
  accountNumber: string?,
  annualContributionLimit: number,  // 7000 (2024)
  contributionsThisYear: number,
  currentBalance: number,
  taxYear: number,               // 2025
  recurringContributionAmount: number?,
  recurringContributionFrequency: "BIWEEKLY" | "MONTHLY",
  recurringContributionStartDate: timestamp?,
  recurringContributionDayOfMonth: number?,
  autoIncrease: boolean,
  autoIncreasePercentage: number,
  notes: string?,
  isActive: boolean,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Subcollection**: `/contributions/{contributionId}` - contribution history

---

### 6. `/users/{userId}/goals/emergencyFunds/{fundId}`

**Purpose**: Emergency fund tracking

```typescript
{
  id: string,
  userId: string,
  bankName: string,              // "Ally Bank", "Marcus"
  accountType: "SAVINGS" | "MONEY_MARKET" | "CD",
  accountNumber: string?,
  currentBalance: number,
  targetGoal: number,
  apy: number,                   // Annual percentage yield
  compoundingFrequency: "DAILY" | "MONTHLY" | "QUARTERLY",
  monthlyContribution: number,
  autoDeposit: boolean,
  depositDayOfMonth: number?,
  monthsOfExpensesCovered: number,  // Calculated field
  minimumBalance: number,
  notes: string?,
  isActive: boolean,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Subcollection**: `/deposits/{depositId}` - deposit history

---

### 7. `/users/{userId}/goals/etfPortfolios/{portfolioId}`

**Purpose**: ETF/Stock portfolio tracking

```typescript
{
  id: string,
  userId: string,
  portfolioName: string,
  brokerageName: string,
  accountNumber: string?,
  totalInvested: number,
  currentValue: number,
  totalReturn: number,           // Calculated
  totalReturnPercentage: number, // Calculated
  allocationStrategy: string,    // "70/30", "S&P 500", "3-Fund"
  autoInvest: boolean,
  monthlyContribution: number,
  isActive: boolean,
  notes: string?,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

**Subcollection**: `/holdings/{holdingId}` - individual stocks/ETFs

---

### 8. `/users/{userId}/paychecks/{paycheckId}`

**Purpose**: Paycheck tracking

```typescript
{
  id: string,
  userId: string,
  date: timestamp,
  grossAmount: number,
  netAmount: number,
  deposited: boolean,
  depositedAt: timestamp?,
  payPeriodStart: timestamp,
  payPeriodEnd: timestamp,
  notes: string?,
  createdAt: timestamp,
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

---

### 9. `/users/{userId}/balance/{balanceId}`

**Purpose**: Current balance snapshot (typically one document)

```typescript
{
  userId: string,
  currentBalance: number,
  lastUpdatedBy: "user" | "transaction" | "paycheck",
  updatedAt: timestamp,
  syncedAt: timestamp?
}
```

---

## 🔐 Firebase Security Rules

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }
    
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // User documents - only owner can access
    match /users/{userId} {
      allow read, write: if isOwner(userId);
      
      // All subcollections under user - only owner can access
      match /{subcollection}/{document=**} {
        allow read, write: if isOwner(userId);
      }
    }
    
    // Legacy root collections (deprecated - migrate to user subcollections)
    match /transactions/{transactionId} {
      allow read, write: if isAuthenticated() 
        && resource.data.userId == request.auth.uid;
    }
    
    match /fixedExpenses/{expenseId} {
      allow read, write: if isAuthenticated() 
        && resource.data.userId == request.auth.uid;
    }
    
    // Deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### Deploy Security Rules:
```bash
cd /home/oliver/BudgetTrackerApp
firebase deploy --only firestore:rules
```

---

## 🔄 Data Migration Strategy

### Phase 1: Migrate Existing Data

Your current data structure has root-level collections. We need to move them under user documents.

#### Migration Steps:

1. **Transactions** (94 documents)
   ```
   FROM: /transactions/{id} (with userId field)
   TO:   /users/{userId}/transactions/{id}
   ```

2. **Essential Expenses**
   ```
   FROM: /fixedExpenses/{id} (with userId field)
   TO:   /users/{userId}/essentialExpenses/{id}
   ```

3. **Subscriptions**
   ```
   FROM: /subscriptions/{id} (with userId field)  
   TO:   /users/{userId}/subscriptions/{id}
   ```

### Migration Script:
```javascript
// Run in Firebase Console or Cloud Functions
const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

async function migrateToUserSubcollections() {
  // Migrate transactions
  const transactions = await db.collection('transactions').get();
  for (const doc of transactions.docs) {
    const data = doc.data();
    await db.collection('users')
      .doc(data.userId)
      .collection('transactions')
      .doc(doc.id)
      .set(data);
  }
  
  console.log(`Migrated ${transactions.size} transactions`);
  
  // Repeat for other collections...
}
```

---

## 💾 Room + Firestore Sync Architecture

### Sync Flow:

```
User Action (Add/Edit/Delete)
    ↓
[Repository Layer]
    ├→ Update Room Database (immediate)
    │   └→ UI updates instantly (offline-first)
    │
    └→ Sync to Firestore (background)
        └→ Other devices receive updates
```

### Key Principles:

1. **Offline-First**: Always write to Room first
2. **Optimistic UI**: Show changes immediately
3. **Background Sync**: Push to Firebase asynchronously
4. **Conflict Resolution**: Firestore timestamp wins
5. **Pending Sync Queue**: Track unsync'd changes

---

## 📱 Repository Implementation Pattern

### Example: Transaction Repository

```kotlin
@Singleton
class TransactionRepository @Inject constructor(
    private val dao: TransactionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    // Add transaction
    suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            val userId = currentUserId ?: return Result.Error("Not authenticated")
            
            // 1. Save to Room (offline cache)
            val entity = TransactionEntity.fromDomain(transaction, pendingSync = true)
            dao.insertTransaction(entity)
            
            // 2. Sync to Firestore (user subcollection)
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transaction.id)
                .set(transaction.toFirestoreMap())
                .await()
            
            // 3. Mark as synced in Room
            dao.markSynced(transaction.id, System.currentTimeMillis())
            
            Result.Success(transaction.id)
        } catch (e: Exception) {
            Result.Error("Failed to add transaction: ${e.message}")
        }
    }
    
    // Load transactions from Firestore on app start
    suspend fun syncFromFirebase() {
        try {
            val userId = currentUserId ?: return
            
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .get()
                .await()
            
            snapshot.documents.forEach { doc ->
                val entity = doc.toTransactionEntity()
                dao.insertTransaction(entity)
            }
        } catch (e: Exception) {
            // Fail silently, use local data
        }
    }
}
```

---

## 🔍 Query Examples

### Get User's Transactions for Current Month
```kotlin
firestore.collection("users")
    .document(userId)
    .collection("transactions")
    .whereGreaterThanOrEqualTo("date", startOfMonth)
    .whereLessThan("date", endOfMonth)
    .orderBy("date", Query.Direction.DESCENDING)
    .get()
```

### Get Unpaid Essential Expenses
```kotlin
firestore.collection("users")
    .document(userId)
    .collection("essentialExpenses")
    .whereEqualTo("period", "2025-10")
    .whereEqualTo("paid", false)
    .get()
```

### Get Active Subscriptions Due Soon
```kotlin
firestore.collection("users")
    .document(userId)
    .collection("subscriptions")
    .whereEqualTo("active", true)
    .whereLessThanOrEqualTo("nextBillingDate", sevenDaysFromNow)
    .orderBy("nextBillingDate")
    .get()
```

---

## 🎯 Benefits of This Structure

### ✅ Security
- Each user can ONLY access their own data
- Firebase Security Rules enforce this at the database level
- No risk of data leakage between users

### ✅ Scalability
- Firestore scales horizontally with user count
- Each user's data is isolated
- Queries are scoped to single user (fast)

### ✅ Organization
- Clear hierarchy: `/users/{userId}/{collection}/{document}`
- Easy to understand and maintain
- Subcollections keep related data together

### ✅ Offline Support
- Room cache works seamlessly
- Firestore offline persistence enabled
- Users can work without internet

### ✅ Real-Time Sync
- Changes sync across devices instantly
- Firestore listeners for real-time updates
- Optimistic UI for snappy experience

---

## 📚 Migration Checklist

- [ ] Deploy new Firestore Security Rules
- [ ] Update all Repository classes to use subcollections
- [ ] Migrate existing data from root collections
- [ ] Test data isolation (create test user, verify no cross-access)
- [ ] Update sync logic in all ViewModels
- [ ] Test offline → online sync
- [ ] Verify Firebase indexes are created
- [ ] Delete old root-level collections (after migration verified)
- [ ] Document new structure for team
- [ ] Update API documentation

---

## 🚀 Next Steps

1. **Deploy Security Rules** → Protect user data
2. **Migrate Existing Data** → Move to user subcollections
3. **Update Repositories** → Use new structure
4. **Test Thoroughly** → Verify sync works
5. **Monitor Firebase Usage** → Check read/write costs

---

**Your Firestore database is now properly structured for multi-user, secure, and scalable budgeting! 🎉**

