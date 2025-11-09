# API Keys Setup Guide

## 🔐 Secure API Key Management

This project uses `local.properties` to store sensitive API keys securely. This file is **git-ignored** and will never be committed to version control.

---

## Quick Setup

### 1. Create `local.properties` File

If you cloned this repo, you need to create your own `local.properties` file:

```bash
cp local.properties.example local.properties
```

### 2. Add Your API Keys

Open `local.properties` and replace the placeholder values with your actual keys:

```properties
# Android SDK location (Gradle will auto-set this)
sdk.dir=/home/your-username/Android/Sdk

# PostHog Analytics
POSTHOG_API_KEY=phc_YOUR_ACTUAL_API_KEY_HERE
POSTHOG_HOST=https://us.i.posthog.com
```

### 3. Get Your PostHog API Key

1. Go to [PostHog Dashboard](https://us.i.posthog.com)
2. Navigate to **Project Settings** → **Project Variables**
3. Copy your **Project API Key**
4. Paste it into `local.properties`

### 4. Build the Project

```bash
./gradlew assembleDebug
```

The API keys will be automatically injected into `BuildConfig` during compilation.

---

## How It Works

### Architecture

```
local.properties (git-ignored)
    ↓
build.gradle.kts (reads properties)
    ↓
BuildConfig.java (generated at compile time)
    ↓
BudgetTrackerApplication.kt (uses BuildConfig)
```

### Security Features

✅ **Git-Ignored:** `local.properties` is never committed  
✅ **Compile-Time Injection:** Keys are embedded during build  
✅ **No Hardcoding:** Keys are not in source code  
✅ **Team-Friendly:** Each developer has their own keys  

---

## File Structure

```
BudgetTrackerApp/
├── local.properties.example     # ✅ Template (safe to commit)
├── local.properties              # ❌ Your actual keys (git-ignored)
├── .gitignore                    # Ensures local.properties is ignored
└── app/
    ├── build.gradle.kts          # Reads keys from local.properties
    └── src/main/java/
        └── BudgetTrackerApplication.kt  # Uses BuildConfig
```

---

## Usage in Code

The API keys are available via `BuildConfig`:

```kotlin
// In BudgetTrackerApplication.kt
val config = PostHogAndroidConfig(
    apiKey = BuildConfig.POSTHOG_API_KEY,  // Loaded from local.properties
    host = BuildConfig.POSTHOG_HOST
)
```

---

## Troubleshooting

### ❌ Build Error: "Unresolved reference: BuildConfig"

**Solution:** Make sure `local.properties` exists and contains the required keys:

```properties
POSTHOG_API_KEY=your_key_here
POSTHOG_HOST=https://us.i.posthog.com
```

Then clean and rebuild:
```bash
./gradlew clean assembleDebug
```

### ❌ Empty API Keys

If `local.properties` is missing keys, default empty values are used. Check logs:

```bash
adb logcat | grep PostHog
```

### ❌ Keys Not Loading

1. Verify `local.properties` is in the **root** directory (not `app/`)
2. Check for typos in property names
3. Restart Android Studio / resync Gradle

---

## For Team Members

### First Time Setup

1. **Clone the repo:**
   ```bash
   git clone https://github.com/yourusername/BudgetTrackerApp.git
   cd BudgetTrackerApp
   ```

2. **Copy the example file:**
   ```bash
   cp local.properties.example local.properties
   ```

3. **Get your own API key:**
   - Contact the project owner for PostHog access
   - Or create your own PostHog project at https://posthog.com

4. **Add keys to `local.properties`:**
   ```properties
   POSTHOG_API_KEY=phc_YOUR_KEY_HERE
   POSTHOG_HOST=https://us.i.posthog.com
   ```

5. **Build:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## CI/CD Setup

For continuous integration (GitHub Actions, GitLab CI, etc.), add secrets:

### GitHub Actions Example

```yaml
# .github/workflows/build.yml
- name: Create local.properties
  run: |
    echo "POSTHOG_API_KEY=${{ secrets.POSTHOG_API_KEY }}" >> local.properties
    echo "POSTHOG_HOST=https://us.i.posthog.com" >> local.properties
```

Add `POSTHOG_API_KEY` to your repository secrets:
1. Go to **Settings** → **Secrets** → **Actions**
2. Add new secret: `POSTHOG_API_KEY`

---

## Production Considerations

### Release Builds

For production releases:

1. **Use environment variables** in your CI/CD pipeline
2. **Rotate keys** if they're ever exposed
3. **Use different keys** for debug vs release builds (optional):

```kotlin
// In build.gradle.kts
buildTypes {
    debug {
        buildConfigField("String", "POSTHOG_API_KEY", "\"${localProperties.getProperty("POSTHOG_DEBUG_API_KEY")}\"")
    }
    release {
        buildConfigField("String", "POSTHOG_API_KEY", "\"${localProperties.getProperty("POSTHOG_RELEASE_API_KEY")}\"")
    }
}
```

### ProGuard/R8

API keys in `BuildConfig` are obfuscated in release builds automatically.

---

## Security Best Practices

✅ **DO:**
- Keep `local.properties` git-ignored
- Use different keys per environment (dev/prod)
- Rotate keys if compromised
- Use CI/CD secrets for automated builds

❌ **DON'T:**
- Commit API keys to git
- Share keys in Slack/email
- Hardcode keys in source code
- Use production keys for development

---

## Additional Resources

- [PostHog Documentation](https://posthog.com/docs)
- [Android Security Best Practices](https://developer.android.com/privacy-and-security/security-tips)
- [Git Secrets Prevention](https://git-secret.io/)

---

**Need Help?**  
Contact the project maintainer or open an issue on GitHub.




