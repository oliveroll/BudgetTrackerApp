# Essential Expenses Duplication Fix

## Problem Description

### Symptom
When a new month starts, essential expenses (Rent, Groceries, Utilities, WiFi/Power, etc.) were being duplicated. After several months, the Budget screen would show multiple copies of the same essential expense:
- Rent (2 entries)
- Rent (2 entries)
- Groceries (3 entries)
- etc.

### Root Cause
The issue was in `BudgetOverviewRepository.kt` in the `ensureFixedExpensesForPeriod()` function:

1. **Weak duplication check**: The function only checked if ANY expenses existed for the period, not if specific categories already existed
2. **No database constraint**: There was no uniqueness constraint preventing duplicate `(userId, category, period)` combinations
3. **Race conditions**: Multiple calls to the function (from different screens, sync operations, etc.) could create duplicates
4. **Non-idempotent Firestore writes**: Using `.set()` without merge options

## Solution Overview

The fix implements a **multi-layered anti-duplication strategy**:

### 1. Database-Level Protection (Primary Defense)
**File**: `EssentialExpenseEntity.kt`

Added a unique composite index on `(userId, category, period)`:

```kotlin
@Entity(
    tableName = "essential_expenses",
    indices = [
        androidx.room.Index(
            value = ["userId", "category", "period"], 
            unique = true,
            name = "index_essential_expenses_unique_category_period"
        )
    ]
)
```

**Effect**: The database now enforces that each user can have only ONE essential expense per category per period. Attempts to insert duplicates will be rejected at the database level.

### 2. Database Migration (Data Cleanup)
**File**: `BudgetTrackerDatabase.kt` - `MIGRATION_7_8`

The migration:
1. Creates a new table with the unique constraint
2. **Removes existing duplicates** by keeping only the most recent entry (by `createdAt`)
3. Drops the old table and renames the new one
4. Creates the unique index

**SQL Logic**:
```sql
-- Keep only the most recent duplicate for each (userId, category, period)
INSERT INTO essential_expenses_new 
SELECT * FROM essential_expenses e1
WHERE e1.id = (
    SELECT e2.id FROM essential_expenses e2
    WHERE e2.userId = e1.userId 
    AND e2.category = e1.category 
    AND e2.period = e1.period
    ORDER BY e2.createdAt DESC
    LIMIT 1
)
```

**Result**: All existing duplicates are removed during the migration.

### 3. DAO-Level Protection
**File**: `BudgetOverviewDao.kt`

Changed insert strategy from `REPLACE` to `IGNORE`:

```kotlin
@Insert(onConflict = OnConflictStrategy.IGNORE)
suspend fun insertEssentialExpense(expense: EssentialExpenseEntity): Long
```

**Effect**: 
- If a duplicate is attempted, the insert is silently ignored (returns -1)
- No exception is thrown
- The existing expense is preserved
- Returns the row ID on success (> 0)

### 4. Application-Level Logic (Smart Rollover)
**File**: `BudgetOverviewRepository.kt` - `ensureFixedExpensesForPeriod()`

Enhanced the month rollover logic:

#### Before (Buggy):
```kotlin
// Check if ANY expenses exist
val existingExpenses = dao.getEssentialExpenses(userId, targetPeriod)
if (existingExpenses.isNotEmpty()) {
    return // Stop completely
}

// Copy ALL fixed expenses blindly
fixedExpenses.forEach { template ->
    dao.insertEssentialExpense(newExpense) // Could create duplicates!
}
```

#### After (Fixed):
```kotlin
// Get existing expenses and their categories
val existingExpenses = dao.getEssentialExpenses(userId, targetPeriod)
val existingCategories = existingExpenses.map { it.category }.toSet()

fixedExpenses.forEach { template ->
    // Skip if category already exists
    if (existingCategories.contains(template.category)) {
        skippedCount++
        return@forEach
    }
    
    // Try to insert
    val insertResult = dao.insertEssentialExpense(newExpense)
    
    if (insertResult > 0) {
        createdCount++
        // Success - sync to Firebase
    } else {
        skippedCount++
        // Duplicate detected, skip
    }
}
```

**Key Improvements**:
- Checks each category individually
- Skips categories that already exist
- Logs creation and skip counts for debugging
- Only creates missing categories

### 5. Firestore Idempotency
**File**: `BudgetOverviewRepository.kt`

Changed all Firestore writes to use `SetOptions.merge()`:

```kotlin
// Before
.set(expense.toFirestoreMap())

// After
.set(expense.toFirestoreMap(), SetOptions.merge())
```

**Effect**: 
- Multiple writes of the same expense are safe
- Existing data is preserved and merged
- No accidental overwrites

## How It Works: Month Rollover Flow

### Scenario: User enters December 2025

1. **ensureFixedExpensesForPeriod("2025-12")** is called
2. Function checks: "What expenses already exist for 2025-12?"
   - Result: `[Rent, Groceries]` (already exist)
3. Function gets fixed expenses from November 2025: `[Rent, Groceries, Utilities, Phone]`
4. For each template:
   - **Rent**: Skip (already exists)
   - **Groceries**: Skip (already exists)
   - **Utilities**: Create new expense for 2025-12 ✅
   - **Phone**: Create new expense for 2025-12 ✅
5. **Result**: Only missing categories are created, no duplicates

### Multi-Device Sync Protection

**Scenario**: User has app open on phone and tablet, both trigger rollover

**Phone**:
1. Checks existing expenses: `[]` (empty)
2. Creates: Rent, Groceries, Utilities, Phone ✅

**Tablet** (runs simultaneously):
1. Checks existing expenses: `[]` (empty - might not see phone's changes yet)
2. Attempts to create: Rent, Groceries, Utilities, Phone
3. **Database rejects all** (unique constraint violation)
4. DAO returns -1 for each attempt
5. Repository skips all (already logged)

**Final Result**: Database contains exactly 4 expenses, no duplicates ✅

## Benefits

### 1. **Database Integrity**
- Impossible to have duplicate categories per period
- Constraint enforced at the lowest level

### 2. **Idempotent Operations**
- Function can be called multiple times safely
- Firestore writes can retry without issues
- Multi-device sync doesn't create duplicates

### 3. **Backward Compatible**
- Migration cleans up existing duplicates
- New code prevents future duplicates
- No user intervention required

### 4. **Performance**
- Unique index improves query performance
- Early exits when categories exist
- Logs help with debugging

### 5. **Maintainability**
- Clear intent with comments
- Logging for troubleshooting
- Defensive programming at multiple layers

## Testing Checklist

### Manual Testing
- [ ] Install app with migration 7 → 8
- [ ] Verify existing duplicates are removed
- [ ] Advance to new month
- [ ] Verify no duplicates created
- [ ] Try creating same expense manually twice
- [ ] Verify second attempt shows error
- [ ] Check Firestore data consistency

### Edge Cases
- [ ] First month (no previous expenses to copy)
- [ ] Multiple devices syncing simultaneously
- [ ] Network failure during Firestore sync
- [ ] App restart mid-rollover
- [ ] Manual expense creation during rollover

### Database Verification
```sql
-- Check for duplicates (should return 0 rows)
SELECT userId, category, period, COUNT(*) as count
FROM essential_expenses
GROUP BY userId, category, period
HAVING count > 1;

-- Verify unique index exists
SELECT name FROM sqlite_master 
WHERE type='index' 
AND name='index_essential_expenses_unique_category_period';
```

## Rollback Plan

If issues arise, rollback steps:

1. **Emergency Fix**: Disable auto-rollover temporarily:
   ```kotlin
   // In ensureFixedExpensesForPeriod()
   return // Temporary disable
   ```

2. **Database Rollback**: Revert to version 7:
   - Change `version = 8` back to `version = 7`
   - Remove `MIGRATION_7_8` from migrations list
   - Use `.fallbackToDestructiveMigration()`

3. **User Communication**: 
   - "We're fixing a rare issue with monthly expenses"
   - "Your data is safe, we'll resolve this shortly"

## Future Enhancements

### Potential Improvements
1. **UI Feedback**: Show toast when duplicate is prevented
2. **Manual Deduplication Tool**: Settings option to remove duplicates
3. **Sync Conflict Resolution**: Handle Firestore→Room sync conflicts
4. **Analytics**: Track duplication attempts for monitoring

### Code Cleanup
- Consider removing `OnConflictStrategy.REPLACE` from other DAOs
- Audit all Firebase writes for idempotency
- Add unit tests for `ensureFixedExpensesForPeriod()`

## Summary

This fix implements a **defense-in-depth** strategy:
- ✅ Database constraint (primary protection)
- ✅ DAO conflict strategy (secondary protection)
- ✅ Application logic (tertiary protection)
- ✅ Idempotent Firestore writes (sync protection)
- ✅ Data migration (cleanup existing issues)

**Result**: Essential expenses cannot be duplicated, even under race conditions or multi-device sync scenarios.

