package com.budgettracker

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Utility to force FCM token refresh and save to Firestore
 * Call this from Settings to manually trigger token save
 */
object ForceTokenRefresh {
    private const val TAG = "ForceTokenRefresh"
    
    fun refreshAndSaveToken(onComplete: (Boolean, String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        
        if (userId == null) {
            onComplete(false, "User not authenticated")
            return
        }
        
        Log.d(TAG, "Requesting FCM token for user: $userId")
        
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                Log.d(TAG, "FCM token retrieved: $token")
                
                // Save to Firestore with merge to preserve existing data
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update(mapOf("fcmToken" to token, "fcmTokenUpdatedAt" to System.currentTimeMillis()))
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ FCM token saved to Firestore!")
                        onComplete(true, "FCM token saved successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Update failed, trying set with merge", e)
                        
                        // Fallback: use set with merge
                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(userId)
                            .set(mapOf("fcmToken" to token, "fcmTokenUpdatedAt" to System.currentTimeMillis()), 
                                com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                Log.d(TAG, "✅ FCM token saved with merge!")
                                onComplete(true, "FCM token saved successfully!")
                            }
                            .addOnFailureListener { e2 ->
                                Log.e(TAG, "❌ Failed to save FCM token", e2)
                                onComplete(false, "Failed to save token: ${e2.message}")
                            }
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Failed to get FCM token", e)
                onComplete(false, "Failed to get token: ${e.message}")
            }
    }
}

