package com.budgettracker

import android.app.Application
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Budget Tracker app
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class BudgetTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize PostHog Analytics (API keys loaded from local.properties)
        initializePostHog()
        
        // Initialize Firebase (automatically done via google-services plugin)
        // Initialize crash reporting
        // Initialize analytics
        
        // Set up global exception handling for better crash reporting
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Log crash to Firebase Crashlytics
            exception.printStackTrace()
            
            // Track exception in PostHog (best effort - don't fail if PostHog isn't ready)
            try {
                PostHog.capture(
                    event = "app_crashed",
                    properties = mapOf(
                        "error_message" to (exception.message ?: "Unknown error"),
                        "error_type" to exception.javaClass.simpleName
                    )
                )
            } catch (e: Exception) {
                // Ignore PostHog errors during crash handling
            }
            
            // Call the original default handler to let the system handle it
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
    
    private fun initializePostHog() {
        // API keys are securely loaded from local.properties (git-ignored)
        val config = PostHogAndroidConfig(
            apiKey = BuildConfig.POSTHOG_API_KEY,
            host = BuildConfig.POSTHOG_HOST
        ).apply {
            // Capture app lifecycle events (app opened, backgrounded, etc.)
            captureApplicationLifecycleEvents = true
            
            // Capture screen views automatically
            captureScreenViews = true
            
            // Capture deep links
            captureDeepLinks = true
            
            // Flush events after 20 events
            flushAt = 20
            
            // Flush every 30 seconds
            flushIntervalSeconds = 30
            
            // Enable debug mode in debug builds (check if app is debuggable)
            debug = applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
            
            // Send feature flag events
            sendFeatureFlagEvent = true
            
            // Preload feature flags
            preloadFeatureFlags = true
        }
        
        PostHogAndroid.setup(this, config)
        
        // Capture app started event
        PostHog.capture(event = "app_started")
    }
}
