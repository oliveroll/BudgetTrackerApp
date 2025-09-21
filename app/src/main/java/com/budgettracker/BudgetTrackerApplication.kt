package com.budgettracker

import android.app.Application
// import dagger.hilt.android.HiltAndroidApp // Temporarily disabled

/**
 * Application class for Budget Tracker app
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
// @HiltAndroidApp // Temporarily disabled
class BudgetTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (automatically done via google-services plugin)
        // Initialize crash reporting
        // Initialize analytics
        
        // Set up global exception handling for better crash reporting
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Log crash to Firebase Crashlytics
            exception.printStackTrace()
            
            // Re-throw to let the system handle it
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, exception)
        }
    }
}
