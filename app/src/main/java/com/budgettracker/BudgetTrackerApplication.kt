package com.budgettracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Budget Tracker app
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class BudgetTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase (automatically done via google-services plugin)
        // Initialize crash reporting
        // Initialize analytics
        
        // Set up global exception handling for better crash reporting
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Log crash to Firebase Crashlytics
            exception.printStackTrace()
            
            // Call the original default handler to let the system handle it
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
