package com.budgettracker.features.auth.data

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Authentication manager for Firebase Auth and Google Sign-In
 */
class AuthManager(private val context: Context) {
    
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient
    
    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("188414997520-kjop4b2p05e5fndbvk4fla2kqn0467t0.apps.googleusercontent.com")
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser
    
    /**
     * Check if user is signed in
     */
    fun isSignedIn(): Boolean = getCurrentUser() != null
    
    /**
     * Sign in with email and password
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Create user with email and password
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with Google credential
     * Returns Pair<FirebaseUser, isNewUser> to indicate if this is first-time sign-in
     */
    suspend fun signInWithGoogle(idToken: String): Result<Pair<FirebaseUser, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user!!
            // Check if this is a new user (first time signing in)
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            android.util.Log.d("AuthManager", "Google Sign-In: isNewUser = $isNewUser")
            Result.success(Pair(user, isNewUser))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get Google Sign-In client for activity result
     */
    fun getGoogleSignInClient(): GoogleSignInClient = googleSignInClient
    
    /**
     * Sign out from Google to force account picker on next sign-in
     */
    suspend fun signOutFromGoogle() {
        try {
            googleSignInClient.signOut().await()
            android.util.Log.d("AuthManager", "✅ Signed out from Google - account picker will show")
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Failed to sign out from Google: ${e.message}")
        }
    }
    
    /**
     * Sign out
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            googleSignInClient.signOut().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete account
     */
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            getCurrentUser()?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
