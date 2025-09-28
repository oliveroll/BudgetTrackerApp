# Budget Tracker - Deployment Guide

## 🚀 Production Deployment

### Prerequisites
- Android Developer Account ($25 one-time fee)
- Firebase Project with production configuration
- Google Play Console access
- Signing keys generated and secured

## 📱 Build Configuration

### 1. Release Build Setup

**`app/build.gradle.kts`**:
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/budget-tracker-release.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

### 2. ProGuard Configuration

**`app/proguard-rules.pro`**:
```proguard
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep Room entities
-keep class com.budgettracker.core.data.local.entities.** { *; }

# Keep domain models
-keep class com.budgettracker.core.domain.model.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class **_HiltModules* { *; }

# Keep Kotlin coroutines
-keep class kotlinx.coroutines.** { *; }

# Keep Jetpack Compose
-keep class androidx.compose.** { *; }
```

## 🔐 Security Configuration

### 1. API Keys Protection

**`local.properties`** (not committed):
```properties
# Firebase
FIREBASE_API_KEY=your_firebase_api_key_here

# Other sensitive keys
CRASHLYTICS_API_KEY=your_crashlytics_key_here
```

**`app/build.gradle.kts`**:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "FIREBASE_API_KEY", "\"${project.findProperty("FIREBASE_API_KEY")}\"")
    }
}
```

### 2. Network Security

**`app/src/main/res/xml/network_security_config.xml`**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">firebase.google.com</domain>
        <domain includeSubdomains="true">firestore.googleapis.com</domain>
    </domain-config>
</network-security-config>
```

## 🏗️ CI/CD Pipeline

### GitHub Actions Workflow

**`.github/workflows/android.yml`**:
```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
      
    - name: Run tests
      run: ./gradlew test
      
    - name: Run lint
      run: ./gradlew lintDebug

  build:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Decode Keystore
      env:
        ENCODED_STRING: ${{ secrets.KEYSTORE_BASE64 }}
      run: |
        echo $ENCODED_STRING | base64 -di > keystore/budget-tracker-release.jks
        
    - name: Build Release APK
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew assembleRelease
      
    - name: Build Release Bundle
      env:
        KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
        KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
        KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
      run: ./gradlew bundleRelease
      
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: release-apk
        path: app/build/outputs/apk/release/app-release.apk
        
    - name: Upload Bundle
      uses: actions/upload-artifact@v3
      with:
        name: release-bundle
        path: app/build/outputs/bundle/release/app-release.aab
```

### Fastlane Configuration

**`fastlane/Fastfile`**:
```ruby
default_platform(:android)

platform :android do
  desc "Runs all the tests"
  lane :test do
    gradle(task: "test")
  end

  desc "Build debug and test APK for screenshots"
  lane :build_for_screengrab do
    gradle(
      task: 'clean'
    )
    gradle(
      task: 'assembleDebug assembleAndroidTest'
    )
  end

  desc "Deploy a new version to the Google Play"
  lane :deploy do
    gradle(task: "clean bundleRelease")
    upload_to_play_store(
      track: 'internal',
      release_status: 'draft'
    )
  end
end
```

## 🌐 Firebase Production Setup

### 1. Firestore Security Rules

**`firestore.rules`**:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // User's transactions
      match /transactions/{transactionId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // User's budgets
      match /budgets/{budgetId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      
      // User's goals
      match /goals/{goalId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

### 2. Storage Security Rules

**`storage.rules`**:
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Users can only access their own files
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Receipt attachments
    match /receipts/{userId}/{receiptId} {
      allow read, write: if request.auth != null && request.auth.uid == userId
        && resource.size < 5 * 1024 * 1024; // 5MB limit
    }
  }
}
```

### 3. Firebase Performance Monitoring

**`BudgetTrackerApplication.kt`**:
```kotlin
@HiltAndroidApp
class BudgetTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        if (!BuildConfig.DEBUG) {
            // Initialize Firebase Performance only in release builds
            FirebasePerformance.getInstance().isPerformanceCollectionEnabled = true
        }
    }
}
```

## 📊 Monitoring & Analytics

### 1. Crashlytics Setup

```kotlin
class GlobalExceptionHandler : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    
    override fun uncaughtException(thread: Thread, exception: Throwable) {
        // Log to Crashlytics
        FirebaseCrashlytics.getInstance().recordException(exception)
        
        // Set custom keys for better debugging
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("thread_name", thread.name)
            setCustomKey("user_id", getCurrentUserId() ?: "unknown")
        }
        
        defaultHandler?.uncaughtException(thread, exception)
    }
}
```

### 2. Analytics Events

```kotlin
object AnalyticsEvents {
    const val TRANSACTION_CREATED = "transaction_created"
    const val BUDGET_CREATED = "budget_created"
    const val GOAL_ACHIEVED = "goal_achieved"
    const val EXPORT_DATA = "export_data"
    
    fun logTransactionCreated(category: String, amount: Double) {
        FirebaseAnalytics.getInstance(context).logEvent(TRANSACTION_CREATED) {
            param("category", category)
            param("amount", amount)
        }
    }
}
```

## 🎯 App Store Optimization

### 1. Store Listing

**Title**: "Budget Tracker - OPT Student Finance"

**Short Description**: 
"Personal finance app designed for international students on OPT/H1B visas"

**Full Description**:
```
Take control of your finances as an international student! Budget Tracker is specifically designed for students on OPT, H1B, and other visas to manage money while navigating US financial systems.

🎯 KEY FEATURES
• 37+ expense categories including visa-specific costs
• Budget templates: 50/30/20 rule, Zero-based, OPT Student
• Track H1B application expenses and visa fees
• Emergency fund planning for visa transitions
• Beautiful, modern interface with Material Design 3
• Offline-first with cloud sync

💰 PERFECT FOR
• OPT students managing limited income
• H1B visa holders planning finances
• International students building credit
• Anyone tracking immigration-related expenses

🏗️ BUILT WITH BEST PRACTICES
• Clean Architecture for reliability
• Offline functionality - works without internet
• Bank-level security with biometric authentication
• Automatic data backup to Google Drive

Start your journey to financial freedom! Download now and take the first step toward smart money management in the US.
```

### 2. Screenshots

Generate screenshots showing:
1. Dashboard with OPT-specific data
2. Transaction list with visa categories
3. Budget planning screen
4. Analytics with spending trends
5. Goal tracking for emergency fund

### 3. Keywords

```
budget tracker, personal finance, OPT student, H1B visa, international student, 
expense tracker, money management, financial planning, budget app, spending tracker
```

## 🔄 Release Process

### 1. Version Management

**`app/build.gradle.kts`**:
```kotlin
android {
    defaultConfig {
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### 2. Release Checklist

- [ ] All tests passing
- [ ] Code coverage > 80%
- [ ] Security audit completed
- [ ] Performance testing completed
- [ ] Accessibility testing done
- [ ] Release notes written
- [ ] Store listing updated
- [ ] Marketing materials ready

### 3. Rollout Strategy

1. **Internal Testing** (5-10 users, 1 week)
2. **Closed Alpha** (50 users, 2 weeks)
3. **Open Beta** (500 users, 1 month)
4. **Staged Rollout** (5% → 20% → 50% → 100%)

## 📈 Post-Launch Monitoring

### 1. Key Metrics

- **Adoption**: Download rate, user retention
- **Usage**: Daily/monthly active users, session length
- **Performance**: App startup time, crash rate
- **Engagement**: Feature usage, user feedback

### 2. A/B Testing

```kotlin
class FeatureFlags {
    companion object {
        const val NEW_ONBOARDING_FLOW = "new_onboarding_flow"
        const val ENHANCED_ANALYTICS = "enhanced_analytics"
        
        fun isEnabled(flag: String): Boolean {
            return FirebaseRemoteConfig.getInstance()
                .getBoolean(flag)
        }
    }
}
```

## 🚨 Emergency Response

### 1. Rollback Plan

```bash
# Rollback to previous version
./gradlew bundleRelease -Pversion=1.0.1
fastlane deploy track:production
```

### 2. Hotfix Process

1. Create hotfix branch from production tag
2. Apply minimal fix
3. Test thoroughly
4. Deploy with expedited review

This deployment guide ensures a smooth, secure, and successful launch of the Budget Tracker app for international students.
