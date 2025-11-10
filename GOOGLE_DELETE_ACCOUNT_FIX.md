# Google Sign-In Delete Account Fix

## Issue Fixed ✅

**Problem:** Users who signed up with Google couldn't delete their account because the delete function required a password, but Google users don't have passwords.

**Error:** When Google users tried to delete their account, they were blocked by the password requirement.

---

## Solution

### How It Works Now

The app detects which authentication method the user signed in with and adjusts the delete account flow accordingly:

#### Email/Password Users
1. Click "Delete Account"
2. Type "DELETE" to confirm
3. **Enter password** for verification
4. Account deleted

#### Google Sign-In Users  
1. Click "Delete Account"
2. Type "DELETE" to confirm
3. **No password needed** (message: "You signed in with Google. No password required.")
4. Account deleted

---

## Technical Implementation

### 1. Detect Sign-In Provider

**Added:** `SettingsRepository.getSignInProvider()`
```kotlin
fun getSignInProvider(): String? {
    val user = auth.currentUser ?: return null
    return user.providerData.firstOrNull()?.providerId
    // Returns: "password" or "google.com"
}
```

### 2. Separate Delete Methods

**Updated:** `SettingsRepository`

**For Email/Password users:**
```kotlin
suspend fun deleteAccount(password: String): Result<Unit> {
    // Re-authenticate with email + password
    val credential = EmailAuthProvider.getCredential(email, password)
    user.reauthenticate(credential).await()
    
    // Delete data and account
    deleteUserDataFromFirestore(userId)
    userSettingsDao.deleteUserSettings(userId)
    user.delete().await()
}
```

**For Google users:**
```kotlin
suspend fun deleteAccountWithoutPassword(): Result<Unit> {
    // No re-authentication needed if recently signed in
    // Delete data and account
    deleteUserDataFromFirestore(userId)
    userSettingsDao.deleteUserSettings(userId)
    user.delete().await()
}
```

### 3. Updated ViewModel

**Added:** `SettingsViewModel`
```kotlin
fun getSignInProvider(): String? {
    return repository.getSignInProvider()
}

fun deleteAccountGoogle(onSuccess: () -> Unit) {
    // Calls deleteAccountWithoutPassword()
}
```

### 4. Smart UI Dialog

**Updated:** `DeleteAccountDialog`
- Accepts `isGoogleUser: Boolean` parameter
- Shows/hides password field based on auth method
- Shows info message for Google users
- Calls appropriate callback

```kotlin
@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (password: String) -> Unit,
    onConfirmGoogle: () -> Unit,
    isGoogleUser: Boolean
) {
    // Password field only shown if !isGoogleUser
    // Info message shown if isGoogleUser
}
```

### 5. Integration in Settings Screen

**Updated:** `EnhancedSettingsScreen.kt`
```kotlin
if (uiState.showDeleteAccountDialog) {
    val signInProvider = viewModel.getSignInProvider()
    val isGoogleUser = signInProvider == "google.com"
    
    DeleteAccountDialog(
        onDismiss = { viewModel.hideDeleteAccountDialog() },
        onConfirm = { password ->
            viewModel.deleteAccount(password) {
                onNavigateToLogin()
            }
        },
        onConfirmGoogle = {
            viewModel.deleteAccountGoogle {
                onNavigateToLogin()
            }
        },
        isGoogleUser = isGoogleUser
    )
}
```

---

## User Experience

### Email/Password User Flow
```
Settings → Delete Account → Enter "DELETE" → Enter Password → Confirm
```

### Google Sign-In User Flow
```
Settings → Delete Account → Enter "DELETE" → Confirm (no password!)
```

Both flows:
- ✅ Delete all Firestore data
- ✅ Delete local Room database data
- ✅ Delete Firebase Authentication account
- ✅ Redirect to login screen

---

## Files Changed

1. ✅ `SettingsRepository.kt`
   - Added `getSignInProvider()`
   - Added `deleteAccountWithoutPassword()`
   - Updated `deleteAccount()` documentation

2. ✅ `SettingsViewModel.kt`
   - Added `getSignInProvider()`
   - Added `deleteAccountGoogle()`

3. ✅ `SettingsDialogs.kt`
   - Updated `DeleteAccountDialog()` signature
   - Added conditional password field
   - Added info message for Google users

4. ✅ `EnhancedSettingsScreen.kt`
   - Detect sign-in provider
   - Pass correct callbacks to dialog

---

## Security Considerations

### Why No Password for Google Users?

**Google Sign-In uses OAuth 2.0:**
- Google itself authenticates the user
- No password is stored in your app
- Google's authentication IS the security layer

### Re-Authentication

**Email/Password users:**
- Must enter password to confirm deletion
- Firebase's `reauthenticate()` ensures they know the password

**Google users:**
- Recently signed in with Google (OAuth token is fresh)
- If token expired, Firebase will show "requires-recent-login" error
- User must sign out and sign in again with Google

### Data Deletion

Both methods delete:
- All Firestore subcollections (transactions, goals, etc.)
- All local Room database data
- Firebase Authentication user account

**This is permanent and irreversible!**

---

## Testing Instructions

### Test Case 1: Email/Password User Delete
1. [ ] Sign in with email/password
2. [ ] Go to Settings → Delete Account
3. [ ] Dialog shows password field
4. [ ] Type "DELETE"
5. [ ] Enter password
6. [ ] Click "Delete Account"
7. [ ] ✅ Account deleted, redirected to login

### Test Case 2: Google User Delete
1. [ ] Sign in with Google
2. [ ] Go to Settings → Delete Account
3. [ ] Dialog shows info: "You signed in with Google. No password required."
4. [ ] **No password field shown**
5. [ ] Type "DELETE"
6. [ ] Click "Delete Account"
7. [ ] ✅ Account deleted, redirected to login

### Test Case 3: Wrong Password (Email User)
1. [ ] Email user tries to delete
2. [ ] Enter wrong password
3. [ ] ✅ Error: "Incorrect password."

### Test Case 4: Recent Login Required
1. [ ] Sign in (wait 30+ min for token to expire)
2. [ ] Try to delete account
3. [ ] ✅ Error: "Please sign out and sign in again before deleting your account."

---

## Edge Cases Handled

### 1. Mixed Authentication (Rare)
If user has multiple auth providers linked:
- Detects primary provider
- Shows appropriate UI

### 2. Token Expiration
If Google OAuth token expired:
- Firebase throws "requires-recent-login"
- User must sign out and sign back in with Google
- Then try delete again

### 3. Offline Mode
- Delete requires internet connection
- Proper error message shown if offline

---

## Build Status

✅ **BUILD SUCCESSFUL**
- No linter errors
- Compiled successfully
- APK installed on device

---

## Related Fixes

This fix complements the authentication improvements:
1. **Google Sign-In Onboarding Fix** - New users go through onboarding
2. **Onboarding Persistence Fix** - Onboarding completion tracked
3. **Google Delete Account Fix** - Google users can delete without password

Together, these ensure a complete auth experience for both email and Google users!

---

**Status:** ✅ COMPLETE  
**Ready for:** User testing  
**Next Steps:** Test on device with both auth methods

