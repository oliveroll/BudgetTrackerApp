package com.budgettracker.features.auth.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Local authentication manager for demo purposes
 * This bypasses Firebase Auth issues and provides working login/logout
 */
class LocalAuthManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_ID = "user_id"
    }
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    
    /**
     * Sign in with email and password (demo - accepts any valid email)
     */
    fun signInWithEmailAndPassword(email: String, password: String): Result<User> {
        return try {
            if (email.contains("@") && password.length >= 6) {
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                )
                saveUserSession(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password too short"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create user with email and password (demo)
     */
    fun createUserWithEmailAndPassword(email: String, password: String, name: String): Result<User> {
        return try {
            if (email.contains("@") && password.length >= 6 && name.isNotBlank()) {
                val user = User(
                    id = "user_${System.currentTimeMillis()}",
                    email = email,
                    name = name
                )
                saveUserSession(user)
                Result.success(user)
            } else {
                Result.failure(Exception("Please check your input: valid email, password (6+ chars), and name required"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with Google (demo)
     */
    fun signInWithGoogle(email: String, name: String): Result<User> {
        return try {
            val user = User(
                id = "google_user_${System.currentTimeMillis()}",
                email = email,
                name = name
            )
            saveUserSession(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): User? {
        return if (isSignedIn()) {
            User(
                id = prefs.getString(KEY_USER_ID, "") ?: "",
                email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
                name = prefs.getString(KEY_USER_NAME, "") ?: ""
            )
        } else null
    }
    
    /**
     * Sign out
     */
    fun signOut(): Result<Unit> {
        return try {
            prefs.edit()
                .clear()
                .apply()
            // Also clear any cached Google sign-in state
            clearGoogleSignInState()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clear Google Sign-In state to force account selection
     */
    private fun clearGoogleSignInState() {
        // This will be used to clear Google sign-in cache
        // For now, we'll handle it in the UI by resetting the state
    }
    
    /**
     * Save user session
     */
    private fun saveUserSession(user: User) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_ID, user.id)
            .apply()
    }
}

/**
 * Simple user data class
 */
data class User(
    val id: String,
    val email: String,
    val name: String
)
