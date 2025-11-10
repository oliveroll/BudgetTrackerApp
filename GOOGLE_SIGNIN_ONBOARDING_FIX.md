# Google Sign-In Onboarding Fix

## Issue Fixed ✅

**Problem:** When signing in with Google, new users skipped the onboarding flow and went directly to the dashboard.

**Expected Behavior:** 
- New users (first-time Google Sign-In) → Onboarding flow
- Existing users (returning Google Sign-In) → Dashboard

---

## Solution

### 1. Detect New vs Existing Users

**Modified:** `AuthManager.kt`
- Changed return type from `Result<FirebaseUser>` to `Result<Pair<FirebaseUser, Boolean>>`
- Use Firebase's `additionalUserInfo.isNewUser` property to detect first-time sign-ins
- Returns both the user object and a boolean flag indicating if it's a new user

```kotlin
suspend fun signInWithGoogle(idToken: String): Result<Pair<FirebaseUser, Boolean>> {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    val result = firebaseAuth.signInWithCredential(credential).await()
    val user = result.user!!
    val isNewUser = result.additionalUserInfo?.isNewUser ?: false
    return Result.success(Pair(user, isNewUser))
}
```

### 2. Propagate New User Flag

**Modified:** `HybridAuthManager.kt`
- Updated to handle the new return type
- Propagates `isNewUser` flag through the auth chain
- Fallback to local auth returns `isNewUser = false` (existing user)

```kotlin
suspend fun signInWithGoogle(idToken: String): Result<Pair<User, Boolean>> {
    val (firebaseUser, isNewUser) = firebaseResult.getOrNull()!!
    // ... create User object
    return Result.success(Pair(user, isNewUser))
}
```

### 3. Route Based on User Status

**Modified:** `LoginScreen.kt`
- Added `onNavigateToOnboarding` callback parameter
- Check `isNewUser` flag after successful Google Sign-In
- Route new users to onboarding, existing users to dashboard

```kotlin
val (user, isNewUser) = authResult.getOrNull()!!

if (isNewUser) {
    onNavigateToOnboarding()  // New user → Onboarding
} else {
    onNavigateToDashboard()   // Existing user → Dashboard
}
```

**Modified:** `BudgetTrackerNavigation.kt`
- Added onboarding navigation route for LoginScreen
- Properly clears navigation backstack to prevent back-button issues

```kotlin
onNavigateToOnboarding = {
    navController.navigate(BudgetTrackerDestinations.ONBOARDING_ROUTE) {
        popUpTo(BudgetTrackerDestinations.LOGIN_ROUTE) { inclusive = true }
    }
}
```

### 4. Register Screen Handling

**Modified:** `RegisterScreen.kt`
- Always routes to onboarding (since users came through register flow)
- Even if technically not new (edge case), they get onboarding experience

---

## Technical Details

### Firebase AdditionalUserInfo

Firebase's `AuthResult.additionalUserInfo` provides:
- `isNewUser: Boolean` - true if first sign-in, false if returning user
- Works for all auth methods (Google, Email, etc.)
- Reliable way to detect new users

### Navigation Flow

**New User (First-time Google Sign-In):**
```
Google Sign-In → LoginScreen → Onboarding → Dashboard
```

**Existing User (Returning):**
```
Google Sign-In → LoginScreen → Dashboard
```

**Email/Password Registration:**
```
RegisterScreen → Onboarding → Dashboard
```

---

## Files Changed

1. ✅ `app/src/main/java/com/budgettracker/features/auth/data/AuthManager.kt`
2. ✅ `app/src/main/java/com/budgettracker/features/auth/data/HybridAuthManager.kt`
3. ✅ `app/src/main/java/com/budgettracker/features/auth/presentation/LoginScreen.kt`
4. ✅ `app/src/main/java/com/budgettracker/features/auth/presentation/RegisterScreen.kt`
5. ✅ `app/src/main/java/com/budgettracker/navigation/BudgetTrackerNavigation.kt`

---

## Testing Instructions

### Test Case 1: New Google User
1. Uninstall app completely (clear Firebase auth cache)
2. Reinstall app
3. Click "Sign in with Google" on **Login** screen
4. Select Google account that has **never** signed in before
5. ✅ Expected: Goes to **Onboarding** flow

### Test Case 2: Existing Google User
1. After completing onboarding, sign out
2. Click "Sign in with Google" again
3. Select same Google account
4. ✅ Expected: Goes directly to **Dashboard**

### Test Case 3: Register Screen
1. Click "Sign in with Google" on **Register** screen
2. Any Google account (new or existing)
3. ✅ Expected: Goes to **Onboarding** (since they came through register)

### Test Case 4: Email/Password Registration
1. Register with email/password
2. ✅ Expected: Goes to **Onboarding**

---

## Build Status

✅ **BUILD SUCCESSFUL**
- No linter errors
- Compiled successfully
- APK generated: `app/build/outputs/apk/debug/app-debug.apk`

---

## Deployment

To install on phone:
```bash
# Connect phone via USB
adb devices

# Install APK
cd /home/oliver/BudgetTrackerApp
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Related Issues Fixed

This fix also ensures proper user scoping (from previous fix):
- Each Google user has isolated data
- No cross-account data leaks
- TransactionDataStore properly initialized per user
- Data cleared on sign-out

---

**Status:** ✅ COMPLETE
**Ready for:** User testing
**Next Steps:** Connect phone and install APK

