# App Logo Setup Guide

## 📁 Where to Place Your Logo

### 1. For Login/Signup Screens (UI Logo)
Place your logo file here:
```
app/src/main/res/drawable/app_logo.png
```

**File Requirements:**
- Name: `app_logo.png` (or `.jpg`, `.webp`)
- Recommended size: 512x512px or higher
- Format: PNG with transparent background (preferred)
- For best results: Square aspect ratio

### 2. For App Icon (Launcher Icon)
Place in mipmap folders for different screen densities:

```
app/src/main/res/
├── mipmap-mdpi/ic_launcher.png      (48x48px)
├── mipmap-hdpi/ic_launcher.png      (72x72px)
├── mipmap-xhdpi/ic_launcher.png     (96x96px)
├── mipmap-xxhdpi/ic_launcher.png    (144x144px)
└── mipmap-xxxhdpi/ic_launcher.png   (192x192px)
```

---

## 🚀 Quick Setup Steps

### Option A: Manual File Copy (Quick & Easy)

1. **Add UI Logo:**
   ```bash
   # Copy your logo to the drawable folder
   cp your_logo.png app/src/main/res/drawable/app_logo.png
   ```

2. **Build the app:**
   ```bash
   ./gradlew assembleDebug
   ```

3. **Done!** The logo will appear on Login and Signup screens automatically.

---

### Option B: Use Android Studio Image Asset Studio (Recommended for App Icon)

1. **Open Android Studio**
2. Right-click on `app/src/main/res` folder
3. Select: **New > Image Asset**
4. Choose:
   - **Icon Type:** Launcher Icons (Adaptive and Legacy)
   - **Asset Type:** Image (select your logo file)
   - **Name:** ic_launcher
   - **Trim:** Yes (removes unnecessary padding)
   - **Resize:** 100%
5. Click **Next** → **Finish**

Android Studio will automatically generate all required sizes!

---

## ✅ What's Already Done

The code has been updated in:
- ✅ **LoginScreen.kt** - Uses `R.drawable.app_logo`
- ✅ **RegisterScreen.kt** - Uses `R.drawable.app_logo`

Both screens will:
- Display your logo in a circular card
- Add elevation shadow for depth
- Scale properly on all devices
- Show as 100dp circular container with 80dp logo inside

---

## 🎨 Logo Design Tips

### For UI Logo (Login/Signup):
- **Transparent background** works best
- **Square aspect ratio** (1:1)
- **High resolution:** 512x512px minimum
- **Simple design:** Looks good at small sizes
- **Clear visibility:** Works on light backgrounds

### For App Icon:
- **Foreground + Background layers** (adaptive icon)
- **Safe zone:** Keep important elements in center 66%
- **No transparency in background layer**
- **Test on different devices:** Light/dark modes

---

## 📱 Testing Your Logo

1. **Check Login Screen:**
   ```bash
   ./gradlew installDebug
   # Open app → You should see your logo on login screen
   ```

2. **Check App Icon:**
   - Go to home screen
   - Find "Budget Tracker" app
   - Verify icon looks correct

3. **Test Different Screens:**
   - Small phones (< 5")
   - Tablets
   - Dark mode

---

## 🔧 Troubleshooting

### Logo doesn't appear:
1. Check file name is exactly: `app_logo.png`
2. Verify file is in: `app/src/main/res/drawable/`
3. Clean and rebuild:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

### Build error: "Resource not found":
- Ensure the file exists before building
- Check file extension (.png, .jpg, .webp)
- No spaces or special characters in filename

### Logo looks stretched/squashed:
- Use square aspect ratio image (1:1)
- Check original image isn't already distorted
- Adjust `contentScale` in code if needed

### App icon not updating:
- Uninstall old app first
- Clear Android Studio cache: **File > Invalidate Caches > Restart**
- Reinstall the app

---

## 📋 Current Logo Implementation

```kotlin
// LoginScreen.kt & RegisterScreen.kt
Card(
    modifier = Modifier.size(100.dp),
    shape = CircleShape,
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "Budget Tracker Logo",
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Fit
    )
}
```

---

## 🎯 Next Steps

1. **Add your logo file** to `app/src/main/res/drawable/app_logo.png`
2. **Generate app icons** using Android Studio Image Asset Studio
3. **Build and run** the app to test
4. (Optional) Customize logo size/shape in code if needed

---

## 📞 Need Help?

If you want to customize:
- Logo size: Change `size(100.dp)` or `size(80.dp)` values
- Logo shape: Change `CircleShape` to `RoundedCornerShape(16.dp)` for rounded square
- Logo background: Adjust `containerColor` alpha value
- Logo padding: Add `.padding(8.dp)` to Image modifier

Let me know if you need any adjustments!


