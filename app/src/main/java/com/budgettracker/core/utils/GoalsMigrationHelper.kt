package com.budgettracker.core.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// NOTE: Using simple return types to avoid Result class confusion
// Calling code can catch exceptions

/**
 * Helper to fix existing goals in Firestore that might be missing the `isActive` field
 * 
 * PROBLEM: Goals created before the fix might not have `isActive` field, causing them to be filtered out
 * SOLUTION: Add `isActive: true` to all existing goals that are missing this field
 */
object GoalsMigrationHelper {
    
    private const val TAG = "GoalsMigration"
    
    /**
     * Fix all existing goals for the current user by adding missing fields
     * Returns the number of goals fixed
     */
    suspend fun fixExistingGoals(): Int {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            
            if (userId == null) {
                Log.w(TAG, "No user logged in, skipping migration")
                return 0
            }
            
            Log.d(TAG, "🔧 Starting goals migration for userId: $userId")
            
            // Get ALL goals for this user (no filters)
            val snapshot = firestore.collection("savingsGoals")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            Log.d(TAG, "📦 Found ${snapshot.documents.size} goal documents")
            
            var fixedCount = 0
            
            for (doc in snapshot.documents) {
                val data = doc.data
                if (data == null) {
                    Log.w(TAG, "Document ${doc.id} has no data, skipping")
                    continue
                }
                
                val goalName = data["name"] as? String ?: "Unknown"
                val hasIsActive = data.containsKey("isActive")
                val isActiveValue = data["isActive"]
                
                Log.d(TAG, "Goal: $goalName | hasIsActive=$hasIsActive | value=$isActiveValue")
                
                // Fix missing isActive field
                if (!hasIsActive || isActiveValue == null) {
                    Log.w(TAG, "❌ Goal '$goalName' missing isActive field, fixing...")
                    
                    firestore.collection("savingsGoals")
                        .document(doc.id)
                        .update(
                            mapOf(
                                "isActive" to true,
                                "updatedAt" to com.google.firebase.Timestamp.now()
                            )
                        )
                        .await()
                    
                    Log.d(TAG, "✅ Fixed goal '$goalName'")
                    fixedCount++
                } else {
                    Log.d(TAG, "✓ Goal '$goalName' already has isActive field")
                }
            }
            
            Log.d(TAG, "✅ Migration complete! Fixed $fixedCount of ${snapshot.documents.size} goals")
            return fixedCount
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Migration failed: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Log all goals to see their current state
     * Returns list of goal info maps
     */
    suspend fun debugLogAllGoals(): List<Map<String, Any?>> {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            
            if (userId == null) {
                Log.w(TAG, "No user logged in")
                return emptyList()
            }
            
            Log.d(TAG, "🔍 Debugging all goals for userId: $userId")
            
            val snapshot = firestore.collection("savingsGoals")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            Log.d(TAG, "📦 Found ${snapshot.documents.size} goals:")
            
            val goals = mutableListOf<Map<String, Any?>>()
            
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val goalInfo = mapOf(
                    "id" to doc.id,
                    "name" to (data["name"] ?: "Unknown"),
                    "category" to (data["category"] ?: "Unknown"),
                    "isActive" to data["isActive"],
                    "isCompleted" to data["isCompleted"],
                    "currentAmount" to data["currentAmount"],
                    "targetAmount" to data["targetAmount"]
                )
                
                goals.add(goalInfo)
                
                Log.d(TAG, "  • ${goalInfo["name"]}")
                Log.d(TAG, "    - category: ${goalInfo["category"]}")
                Log.d(TAG, "    - isActive: ${goalInfo["isActive"]}")
                Log.d(TAG, "    - isCompleted: ${goalInfo["isCompleted"]}")
                Log.d(TAG, "                        - progress: ${goalInfo["currentAmount"]}/${goalInfo["targetAmount"]}")
            }
            
            return goals
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to debug goals: ${e.message}", e)
            throw e
        }
    }
}

