package com.budgettracker.features.auth.data

import android.content.Context
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
     */
    suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            // Try Firebase Google Sign-In first
            val firebaseResult = firebaseAuthManager.signInWithGoogle(idToken)
            if (firebaseResult.isSuccess) {
                val firebaseUser = firebaseResult.getOrNull()!!
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: ""
                )
                // Also save to local for offline access
                localAuthManager.signInWithGoogle(user.email, user.name)
                // Initialize user data and sync
                firebaseRepository.initializeUserData(firebaseUser)
                Result.success(user)
            } else {
                // Fall back to local with demo data
                localAuthManager.signInWithGoogle("ollesch.oliver@gmail.com", "Oliver Ollesch")
            }
        } catch (e: Exception) {
            // Fall back to local with demo data
            localAuthManager.signInWithGoogle("ollesch.oliver@gmail.com", "Oliver Ollesch")
        }
    }
    
    /**
     * Get Google Sign-In client
     */
    fun getGoogleSignInClient() = firebaseAuthManager.getGoogleSignInClient()
    
    /**
     * Sign out (from both Firebase and local)
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            // Sign out from Firebase (includes Google Sign-In)
            firebaseAuthManager.signOut()
            // Sign out from local
            localAuthManager.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            // At least sign out locally
            localAuthManager.signOut()
        }
    }
}
