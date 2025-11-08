# Savings Goals Data Persistence Fix

## Problem Summary

**Symptoms**: Roth IRA and Emergency Fund goals were not displaying in the UI after being created.

**Root Causes Identified**:

1. **Missing `isActive` field in Firestore**: Goals were being saved without explicitly including the `isActive` field, causing them to be filtered out by the query `.whereEqualTo("isActive", true)`

2. **Auto-serialization issues**: Using `.set(goalObject)` with Firestore auto-serialization didn't reliably include fields with default values

3. **No defensive deserialization**: Goals with missing fields would fail to deserialize or have incorrect values

4. **Local cache duplicates**: Goals were being appended to local storage without checking for duplicates

5. **No optimistic updates**: UI wouldn't update immediately after creating a goal

---

## Fixes Implemented

### 1. Explicit Field Mapping on Write (`FirebaseRepository.kt`)

**Before**:
```kotlin
firestore.collection("savingsGoals")
    .document(goal.id)
    .set(goalWithUserId)  // ❌ Auto-serialization might skip fields
    .await()
```

**After**:
```kotlin
val goalData = hashMapOf(
    "id" to goalWithUserId.id,
    "userId" to goalWithUserId.userId,
    "name" to goalWithUserId.name,
    // ... all fields explicitly mapped ...
    "isActive" to true,  // ✅ CRITICAL: Explicitly include isActive
    // ...
)

firestore.collection("savingsGoals")
    .document(goal.id)
    .set(goalData)  // ✅ Guaranteed to include all fields
    .await()
```

### 2. Defensive Deserialization with Defaults (`FirebaseRepository.kt`)

**Before**:
```kotlin
val goals = snapshot.toObjects(SavingsGoal::class.java)  // ❌ Fails if fields missing
```

**After**:
```kotlin
val goals = snapshot.documents.mapNotNull { doc ->
    try {
        val data = doc.data ?: return@mapNotNull null
        
        SavingsGoal(
            id = doc.id,
            userId = data["userId"] as? String ?: userId,
            name = data["name"] as? String ?: "Unnamed Goal",
            // ... all fields with safe extraction and defaults ...
            isActive = data["isActive"] as? Boolean ?: true,  // ✅ Default to true if missing
            // ...
        )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to parse goal: ${e.message}")
        null  // Skip malformed goals instead of crashing
    }
}
```

### 3. Removed Strict Firestore Filtering

**Before**:
```kotlin
firestore.collection("savingsGoals")
    .whereEqualTo("userId", userId)
    .whereEqualTo("isActive", true)  // ❌ Excludes goals without this field
    .orderBy("priority")
```

**After**:
```kotlin
firestore.collection("savingsGoals")
    .whereEqualTo("userId", userId)
    // REMOVED: .whereEqualTo("isActive", true)
    .orderBy("priority")

// Filter in code instead:
val activeGoals = goals.filter { it.isActive && !it.isCompleted }
```

**Reasoning**: Query-level filtering excludes documents without the field entirely. Code-level filtering allows us to apply defaults for missing fields.

### 4. Optimistic Updates (`FirebaseRepository.kt`)

**Before**:
```kotlin
// Save to Firebase first, then maybe update local cache
```

**After**:
```kotlin
// FIXED: Save locally first (optimistic update)
localDataManager.saveSavingsGoal(goalWithUserId)

// Then sync to Firebase in background
firestore.collection("savingsGoals")
    .document(goal.id)
    .set(goalData)
    .await()
```

**Benefit**: UI updates immediately, even if Firebase sync is slow or fails.

### 5. Prevent Local Cache Duplicates (`LocalDataManager.kt`)

**Before**:
```kotlin
val goals = getSavingsGoals().toMutableList()
goals.add(goal)  // ❌ Creates duplicates if goal already exists
```

**After**:
```kotlin
val goals = getSavingsGoals().toMutableList()
goals.removeAll { it.id == goal.id }  // ✅ Remove existing first
goals.add(goal)
```

---

## Migration Helper

Created `GoalsMigrationHelper.kt` to fix existing goals in Firestore that might be missing the `isActive` field.

### Usage:

```kotlin
// In your Settings screen or debug menu:
import com.budgettracker.core.utils.GoalsMigrationHelper

Button(onClick = {
    scope.launch {
        val result = GoalsMigrationHelper.fixExistingGoals()
        result.onSuccess { count ->
            Toast.makeText(context, "Fixed $count goals", Toast.LENGTH_SHORT).show()
        }
    }
}) {
    Text("Fix Missing Goals")
}
```

### What it does:
1. Queries ALL goals for the current user (no filters)
2. Checks each goal for missing `isActive` field
3. Adds `isActive: true` to goals that are missing it
4. Logs all operations for debugging

### Debug helper:

```kotlin
GoalsMigrationHelper.debugLogAllGoals()
```

This will log all goals with their full state to help diagnose issues.

---

## Testing the Fix

### 1. Test New Goal Creation

```kotlin
// In AddGoalScreen or any screen:
val goal = SavingsGoal(
    name = "Test Goal",
    targetAmount = 10000.0,
    category = GoalCategory.EMERGENCY_FUND,
    priority = Priority.HIGH
)

repository.saveSavingsGoal(goal)
```

**Expected**:
- ✅ Goal appears in UI immediately (optimistic update)
- ✅ Goal persists after app restart
- ✅ Goal has `isActive: true` in Firestore

**Verify in logcat**:
```
D/FirebaseRepository: ✅ Saved goal: Test Goal with isActive=true
D/LocalDataManager: 💾 Saved goal locally: Test Goal (isActive=true)
```

### 2. Test Existing Goals Recovery

1. **Check current state**:
   ```kotlin
   GoalsMigrationHelper.debugLogAllGoals()
   ```
   Look for goals with `isActive: null` or missing.

2. **Run migration**:
   ```kotlin
   GoalsMigrationHelper.fixExistingGoals()
   ```

3. **Verify**:
   - Check logcat for "Fixed X goals"
   - Goals should now appear in UI
   - Firebase Console should show `isActive: true` on all goals

### 3. Test Goal Display

**In SavingsGoalsScreen**:
```kotlin
val savingsGoals by repository.getSavingsGoalsFlow().collectAsState(initial = emptyList())

// Should display all active goals
```

**Expected logcat**:
```
D/FirebaseRepository: 📊 Loading savings goals for userId: abc123
D/FirebaseRepository: 📦 Firestore returned 2 goal documents
D/FirebaseRepository: ✅ Loaded 2 total goals, 2 active
D/FirebaseRepository:   Goal: Roth IRA | isActive=true | currentAmount=0.0
D/FirebaseRepository:   Goal: Emergency Fund | isActive=true | currentAmount=0.0
```

---

## Firestore Schema

### Correct Goal Document Structure

```json
{
  "/savingsGoals/{goalId}": {
    "id": "uuid-string",
    "userId": "firebase-auth-uid",
    "name": "Roth IRA",
    "description": "Retirement savings account",
    "targetAmount": 7000.0,
    "currentAmount": 0.0,
    "deadline": Timestamp,
    "priority": "HIGH",
    "monthlyContribution": 583.0,
    "category": "RETIREMENT",
    "isCompleted": false,
    "isActive": true,           // ✅ CRITICAL: Must be present and true
    "color": "#6f42c1",
    "icon": "🎯",
    "createdAt": Timestamp,
    "updatedAt": Timestamp
  }
}
```

### Fields that MUST be present:
- ✅ `isActive: true` - Otherwise goal won't show
- ✅ `userId` - For query filtering
- ✅ All enum fields as strings (e.g., `"category": "RETIREMENT"`)

---

## Common Issues & Solutions

### Issue: "Goals still not showing after fix"

**Check**:
1. Is user logged in? Goals require auth
   ```kotlin
   FirebaseAuth.getInstance().currentUser?.uid
   ```

2. Are goals in Firestore? Check Firebase Console → Firestore → `savingsGoals` collection

3. Run debug helper:
   ```kotlin
   GoalsMigrationHelper.debugLogAllGoals()
   ```

4. Check logcat for:
   ```
   D/FirebaseRepository: 📦 Firestore returned X goal documents
   ```
   
   If `X = 0`, no goals exist in Firestore for this user.

### Issue: "Goals show briefly then disappear"

**Likely cause**: Goals are being filtered out after initial load

**Fix**: Check `isActive` and `isCompleted` fields:
```kotlin
val activeGoals = goals.filter { it.isActive && !it.isCompleted }
```

### Issue: "Duplicate goals appearing"

**Cause**: Fixed! Goals are now de-duplicated by ID in local cache.

**Verify**:
```kotlin
// LocalDataManager now does:
goals.removeAll { it.id == goal.id }  // Remove existing before adding
```

### Issue: "Goals not syncing across devices"

**Check**:
1. Same user logged in on both devices?
2. Network connectivity
3. Check Firestore rules allow read/write

**Firestore Rules** (should look like):
```javascript
match /savingsGoals/{goalId} {
  allow read, write: if request.auth != null && 
                       request.auth.uid == resource.data.userId;
}
```

---

## Summary of Changes

| File | Change | Purpose |
|------|--------|---------|
| `FirebaseRepository.kt` | Explicit field mapping on write | Ensure `isActive` is always included |
| `FirebaseRepository.kt` | Defensive deserialization | Handle missing fields gracefully |
| `FirebaseRepository.kt` | Remove query filter | Get all goals, filter in code |
| `FirebaseRepository.kt` | Optimistic local updates | UI updates immediately |
| `LocalDataManager.kt` | De-duplicate by ID | Prevent duplicate goals in cache |
| `GoalsMigrationHelper.kt` | Migration utility | Fix existing goals in Firestore |

---

## Before & After

### Before:
- ❌ Goals created but not showing in UI
- ❌ Firestore query filtering out goals without `isActive` field
- ❌ No way to recover "lost" goals
- ❌ UI only updates after Firebase sync completes

### After:
- ✅ Goals show immediately after creation (optimistic update)
- ✅ All fields explicitly saved to Firestore
- ✅ Defensive deserialization handles missing fields
- ✅ Migration helper can recover existing goals
- ✅ Comprehensive logging for debugging

---

## Next Steps

1. **Immediate**: Run `GoalsMigrationHelper.fixExistingGoals()` to recover your Roth IRA and Emergency Fund

2. **Verify**: Check that goals now appear in `SavingsGoalsScreen`

3. **Create new goals**: Test that new goals persist correctly

4. **Monitor**: Watch logcat for any error messages during goal operations

5. **Optional**: Add a "Fix Goals" button to Settings screen for easy access to migration helper

---

## Logging Guide

**When creating a goal**:
```
D/LocalDataManager: 💾 Saved goal locally: Roth IRA (isActive=true)
D/FirebaseRepository: ✅ Saved goal: Roth IRA with isActive=true
```

**When loading goals**:
```
D/FirebaseRepository: 📊 Loading savings goals for userId: abc123
D/FirebaseRepository: 📦 Firestore returned 2 goal documents
D/FirebaseRepository: ✅ Loaded 2 total goals, 2 active
D/FirebaseRepository:   Goal: Roth IRA | isActive=true | currentAmount=0.0
D/FirebaseRepository:   Goal: Emergency Fund | isActive=true | currentAmount=5200.0
```

**When migration runs**:
```
D/GoalsMigration: 🔧 Starting goals migration for userId: abc123
D/GoalsMigration: 📦 Found 2 goal documents
D/GoalsMigration: ❌ Goal 'Roth IRA' missing isActive field, fixing...
D/GoalsMigration: ✅ Fixed goal 'Roth IRA'
D/GoalsMigration: ✅ Migration complete! Fixed 1 of 2 goals
```

---

## Contact & Support

If goals still don't appear after running the migration:
1. Check logcat for error messages
2. Verify Firebase Console shows goals with `isActive: true`
3. Ensure user is authenticated
4. Try clearing app data and recreating goals

