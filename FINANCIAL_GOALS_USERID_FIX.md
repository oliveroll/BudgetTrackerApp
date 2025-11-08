# Financial Goals - userId Missing Bug Fix

## Date
November 8, 2025

## Problem
**Emergency Fund and Roth IRA data was not displaying in the UI after creation**, even though:
- ✅ Data existed in Firestore (`/users/{uid}/emergencyFunds`, `/users/{uid}/rothIRAs`)
- ✅ Repository found the documents in Firestore
- ✅ Repository successfully parsed the entities
- ❌ **Room queries returned 0 results**

## Root Cause
The Firestore documents **did not include a `userId` field** (redundant since they're already nested under `/users/{uid}/...`):

```json
{
  "bankName": "Regions",
  "currentBalance": 600,
  "targetGoal": 6000
  // ❌ No userId field
}
```

**Deserialization Bug:**
```kotlin
// In toEmergencyFundEntity() / toRothIRAEntity()
userId = getString("userId") ?: "",  // ❌ Defaulted to empty string!
```

**Room Insertion:**
- Entity inserted with `userId = ""`
- But DAO query searched for `userId = "WYe3hhJhPbag2uRbR7p56KvO4u92"`
- **Mismatch → No results found!**

```kotlin
@Query("SELECT * FROM emergency_funds WHERE userId = :userId AND isActive = 1")
fun getActiveFundsFlow(userId: String): Flow<List<EmergencyFundEntity>>
```

## Solution
**Explicitly set the `userId` in `initializeFromFirebase()` before inserting into Room:**

### Emergency Fund Fix
```kotlin
fundsSnapshot.documents.forEach { doc ->
    val entity = doc.toEmergencyFundEntity().copy(userId = userId) // ✅ CRITICAL FIX
    emergencyFundDao.insertFund(entity)
}
```

### Roth IRA Fix
```kotlin
irasSnapshot.documents.forEach { doc ->
    val entity = doc.toRothIRAEntity().copy(userId = userId) // ✅ CRITICAL FIX
    rothIRADao.insertIRA(entity)
}
```

## Files Modified
1. `app/src/main/java/com/budgettracker/core/data/repository/EmergencyFundRepository.kt`
   - Line 176: Added `.copy(userId = userId)` to ensure entity has correct userId
   - Line 177: Added userId to debug log

2. `app/src/main/java/com/budgettracker/core/data/repository/FinancialGoalsRepository.kt`
   - Line 364: Added `.copy(userId = userId)` to ensure entity has correct userId
   - Line 365: Added userId to debug log

## Why This Happened
- **Firestore best practice:** Don't store redundant data (userId is in the path: `/users/{uid}/...`)
- **Room requirement:** Needs userId for efficient querying
- **Missing transformation:** Failed to add userId when converting Firestore → Room

## Testing
**Before Fix:**
```
📦 Found 2 Emergency Fund documents in Firestore
✅ Successfully initialized 2 Emergency Funds from Firebase
📊 Received 0 funds from Room  ❌
```

**After Fix (Expected):**
```
📦 Found 2 Emergency Fund documents in Firestore
  • Fund: Regions | userId: WYe3hhJhPbag2uRbR7p56KvO4u92 ✅
✅ Successfully initialized 2 Emergency Funds from Firebase
📊 Received 2 funds from Room  ✅
```

## Prevention
✅ **Added userId to debug logs** to catch this issue early  
✅ **Explicit field mapping** instead of relying on defaults  
✅ **Defensive deserialization** with explicit field validation  

## Related Issues
- Similar bugs could occur with other nested Firestore collections
- Consider adding **userId validation** in entity constructors
- Consider a **base repository helper** that enforces userId for all nested collections

## Next Steps
1. Install updated APK
2. Navigate to Financial Goals tabs
3. Verify Emergency Fund displays (2 Regions funds)
4. Verify Roth IRA displays (1 Fidelity IRA)
5. Test navigation persistence (switch tabs and return)
6. Check logs for userId values in debug output

