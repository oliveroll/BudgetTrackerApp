package com.budgettracker.features.auth.data

import android.content.Context
import com.budgettracker.core.data.local.TransactionDataStore
import com.budgettracker.core.data.repository.FirebaseRepository
import com.budgettracker.features.auth.data.LocalAuthManager
import com.budgettracker.features.auth.data.AuthManager
import kotlinx.coroutines.delay

/**
 * Hybrid authentication manager that tries Firebase first, falls back to local
 */
class HybridAuthManager(private val context: Context) {
    
    private val localAuthManager = LocalAuthManager(context)
    private val firebaseAuthManager = AuthManager(context)
    private val firebaseRepository = FirebaseRepository(context)
    
    /**
     * Check if user is signed in (checks both Firebase and local)
     */
    fun isSignedIn(): Boolean {
        return try {
            firebaseAuthManager.isSignedIn() || localAuthManager.isSignedIn()
        } catch (e: Exception) {
            localAuthManager.isSignedIn()
        }
    }
    
    /**
     * Get current user (tries Firebase first, then local)
     */
    fun getCurrentUser(): User? {
        return try {
            val firebaseUser = firebaseAuthManager.getCurrentUser()
            if (firebaseUser != null) {
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: ""
                )
            } else {
                localAuthManager.getCurrentUser()
            }
        } catch (e: Exception) {
            localAuthManager.getCurrentUser()
        }
    }
    
    /**
     * Sign in with email and password (tries Firebase first, falls back to local)
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
        return try {
            // Try Firebase first
            val firebaseResult = firebaseAuthManager.signInWithEmailAndPassword(email, password)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrNull()!!
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    name = firebaseUser.displayName ?: email.substringBefore("@")
                )
                // Also save to local for offline access
                localAuthManager.signInWithEmailAndPassword(email, password)
                // Initialize user data and sync
                firebaseRepository.initializeUserData(firebaseUser)
                Result.success(user)
            } else {
                // Fall back to local
                localAuthManager.signInWithEmailAndPassword(email, password)
            }
        } catch (e: Exception) {
            // Fall back to local
            localAuthManager.signInWithEmailAndPassword(email, password)
        }
    }
    
    /**
     * Create user with email and password (tries Firebase first, falls back to local)
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String, name: String): Result<User> {
        return try {
            // Try Firebase first
            val firebaseResult = firebaseAuthManager.createUserWithEmailAndPassword(email, password)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrNull()!!
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: email,
                    name = name
                )
                // Also save to local for offline access
                localAuthManager.createUserWithEmailAndPassword(email, password, name)
                // Initialize user data and sync
                firebaseRepository.initializeUserData(firebaseUser)
                Result.success(user)
            } else {
                // Fall back to local
                localAuthManager.createUserWithEmailAndPassword(email, password, name)
            }
        } catch (e: Exception) {
            // Fall back to local
            localAuthManager.createUserWithEmailAndPassword(email, password, name)
        }
    }
    
    /**
     * Sign in with Google using ID token (tries Firebase first, falls back to local)
     * Returns Pair<User, isNewUser> to indicate if this is first-time sign-in
     */
    suspend fun signInWithGoogle(idToken: String): Result<Pair<User, Boolean>> {
        return try {
            // Try Firebase Google Sign-In first
            val firebaseResult = firebaseAuthManager.signInWithGoogle(idToken)
            if (firebaseResult.isSuccess) {
                val (firebaseUser, isNewUser) = firebaseResult.getOrNull()!!
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: ""
                )
                android.util.Log.d("HybridAuthManager", "Google Sign-In: User = ${user.email}, isNewUser = $isNewUser")
                
                // Also save to local for offline access
                localAuthManager.signInWithGoogle(user.email, user.name)
                // Initialize user data and sync
                firebaseRepository.initializeUserData(firebaseUser)
                Result.success(Pair(user, isNewUser))
            } else {
                // Fall back to local with demo data (consider as existing user)
                val result = localAuthManager.signInWithGoogle("ollesch.oliver@gmail.com", "Oliver Ollesch")
                if (result.isSuccess) {
                    Result.success(Pair(result.getOrNull()!!, false))
                } else {
                    Result.failure(result.exceptionOrNull()!!)
                }
            }
        } catch (e: Exception) {
            // Fall back to local with demo data (consider as existing user)
            val result = localAuthManager.signInWithGoogle("ollesch.oliver@gmail.com", "Oliver Ollesch")
            if (result.isSuccess) {
                Result.success(Pair(result.getOrNull()!!, false))
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get Google Sign-In client
     */
    fun getGoogleSignInClient() = firebaseAuthManager.getGoogleSignInClient()
    
    /**
     * Sign out from Google to force account picker on next sign-in
     */
    suspend fun signOutFromGoogle() {
        firebaseAuthManager.signOutFromGoogle()
    }
    
    /**
     * Sign out (from both Firebase and local)
     * FIXED: Clears all local cached data on sign-out
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            android.util.Log.d("HybridAuthManager", "🚪 Signing out...")
            
            // Clear all cached transaction data FIRST (before auth logout)
            TransactionDataStore.clearLocalData()
            android.util.Log.d("HybridAuthManager", "✅ Cleared TransactionDataStore")
            
            // Sign out from Firebase (includes Google Sign-In)
            firebaseAuthManager.signOut()
            android.util.Log.d("HybridAuthManager", "✅ Signed out from Firebase")
            
            // Sign out from local
            localAuthManager.signOut()
            android.util.Log.d("HybridAuthManager", "✅ Signed out from local storage")
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("HybridAuthManager", "Error during sign-out: ${e.message}")
            // At least clear local data and sign out locally
            TransactionDataStore.clearLocalData()
            localAuthManager.signOut()
        }
    }
}
