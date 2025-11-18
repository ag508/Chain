# Debugging Release Build Crashes

This guide helps you debug crashes that occur in release builds but not in debug builds.

## Table of Contents
- [Quick Solution: Use Beta Build Variant](#quick-solution-use-beta-build-variant)
- [Get Crash Logs from Device](#get-crash-logs-from-device)
- [Common Crash Causes](#common-crash-causes)
- [Testing Release Builds in Android Studio](#testing-release-builds-in-android-studio)
- [Advanced Debugging](#advanced-debugging)

---

## Quick Solution: Use Beta Build Variant

The **beta** build variant is configured exactly like release (with R8/ProGuard enabled) but is **debuggable**.

### Build and Run Beta Variant in Android Studio

1. **Select Beta Build Variant:**
   - Click **Build Variants** tab (bottom-left in Android Studio)
   - Select **beta** from the dropdown
   - Or: **Build** → **Select Build Variant** → Choose **beta**

2. **Run on Device/Emulator:**
   - Click the **Run** button (green play icon)
   - Or: **Run** → **Run 'app'**
   - The app installs as `com.chain.app.beta` (separate from debug build)

3. **View Crash Logs:**
   - Crashes will appear in **Logcat** window
   - Filter by your package name: `com.chain.app`
   - Look for red error messages and stack traces

### Build Beta APK via Command Line

```bash
./gradlew assembleBeta

# APK location: app/build/outputs/apk/beta/app-beta.apk
```

**Benefits:**
- ✅ Same minification as release (catches R8/ProGuard issues)
- ✅ Debuggable (shows crash logs)
- ✅ Debug signed (no keystore needed)
- ✅ Can install alongside debug build (different package name)
- ✅ Runs in Android Studio with full debugging support

---

## Get Crash Logs from Device

### Method 1: Android Studio Logcat (Easiest)

1. **Connect your device via USB**
2. **Enable USB Debugging** on device:
   - Settings → About Phone → Tap "Build Number" 7 times
   - Settings → Developer Options → Enable "USB Debugging"
3. **Open Logcat in Android Studio:**
   - View → Tool Windows → Logcat
4. **Install the release APK:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-universal-release.apk
   ```
5. **Run the app** and watch Logcat for crashes

### Method 2: ADB Command Line

```bash
# Clear previous logs
adb logcat -c

# Start logging and run your app
adb logcat -v time | grep "AndroidRuntime\|chain"

# Or save to file
adb logcat -v time > crash-log.txt
```

Then run your app and trigger the crash.

### Method 3: Device Log Extraction

```bash
# Get full crash log after crash occurs
adb logcat -d > full-crash-log.txt

# Or filter for crashes only
adb logcat -d *:E > errors-only.txt
```

### Method 4: On-Device Crash Report

If you can't access ADB:

1. Go to **Settings** → **System** → **Developer Options**
2. Enable **Show notification on ANR/crash**
3. When app crashes, tap the notification to see stack trace
4. Take a screenshot of the error

---

## Common Crash Causes

### 1. ProGuard/R8 Removing Required Classes

**Symptom:**
```
java.lang.ClassNotFoundException: com.chain.app.SomeClass
java.lang.NoSuchMethodError: No virtual method xyz()
```

**Fix:**
Add to `app/proguard-rules.pro`:
```proguard
-keep class com.chain.app.** { *; }
-keep class com.yourpackage.** { *; }
```

### 2. Reflection Not Working

**Symptom:**
```
java.lang.IllegalAccessException: Class X cannot access member of class Y
```

**Fix:**
Keep classes used via reflection:
```proguard
-keep class com.chain.app.data.model.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
```

### 3. Kotlin Coroutines Issues

**Symptom:**
```
Exception in thread "DefaultDispatcher-worker-1"
```

**Already Fixed** in `proguard-rules.pro`:
```proguard
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

### 4. Hilt/Dagger Injection Failure

**Symptom:**
```
dagger.internal.MissingBinding: Cannot provide X
java.lang.IllegalStateException: Hilt Activity must be attached to an @AndroidEntryPoint Application
```

**Already Fixed** in `proguard-rules.pro`:
```proguard
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
```

### 5. Jetpack Compose Issues

**Symptom:**
- Blank screen
- UI elements not showing
- ComposeView crashes

**Already Fixed** in `proguard-rules.pro`:
```proguard
-keep class androidx.compose.** { *; }
```

### 6. Navigation Component Issues

**Symptom:**
```
IllegalArgumentException: navigation destination is unknown to this NavController
```

**Already Fixed** in `proguard-rules.pro`:
```proguard
-keep class * extends androidx.navigation.NavArgs
```

### 7. Room Database Issues

**Symptom:**
```
java.lang.IllegalArgumentException: Cannot find implementation for database
```

**Already Fixed** in `proguard-rules.pro`:
```proguard
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
```

---

## Testing Release Builds in Android Studio

### Option 1: Beta Variant (Recommended)

**Best for:** Testing R8/ProGuard with debugging enabled

```bash
# Build and run
./gradlew assembleBeta
adb install -r app/build/outputs/apk/beta/app-beta.apk

# Or in Android Studio:
# Build Variants → Select "beta" → Run
```

### Option 2: Release Variant with ADB

**Best for:** Testing actual release APK

1. **Build release APK:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Install on device:**
   ```bash
   adb install -r app/build/outputs/apk/release/app-universal-release.apk
   ```

3. **Monitor with Logcat:**
   - Open Logcat in Android Studio
   - Launch app from device
   - Watch for crashes

### Option 3: Profile Release Build

**Best for:** Performance testing

1. **Build → Select Build Variant → release**
2. **Run → Profile 'app'**
3. Select device and click OK
4. Android Studio will show performance profiler

---

## Advanced Debugging

### Enable R8 Mapping File (For De-obfuscation)

By default, R8 creates a mapping file at:
```
app/build/outputs/mapping/release/mapping.txt
```

Use this to de-obfuscate crash logs:

```bash
# Using retrace (comes with Android SDK)
retrace.bat app/build/outputs/mapping/release/mapping.txt crash-log.txt
```

### Temporarily Disable Minification

For debugging, you can disable R8 to isolate the issue:

**In `app/build.gradle.kts`:**
```kotlin
buildTypes {
    release {
        isMinifyEnabled = false  // Changed from true
        // ...
    }
}
```

If the crash goes away, it's a ProGuard/R8 issue. Add appropriate keep rules.

### Check ProGuard Output

After building, check what R8 removed:

```
app/build/outputs/mapping/release/usage.txt  - Lists removed code
app/build/outputs/mapping/release/seeds.txt  - Lists kept code
```

### Add Debug Logging to Release

**In `app/proguard-rules.pro`**, temporarily comment out:

```proguard
# Comment this out to keep logs in release
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
```

---

## Checklist: Before Filing a Bug Report

If you still can't fix the crash:

- [ ] Collected full crash log from Logcat
- [ ] Noted which screen/action causes the crash
- [ ] Checked ProGuard rules for missing keeps
- [ ] Tested beta variant to see if it's R8-related
- [ ] Checked `mapping.txt` for obfuscated class names
- [ ] Tried disabling minification to isolate the issue
- [ ] Searched for similar crashes in ProGuard documentation

---

## Quick Reference Commands

```bash
# Build beta (debuggable release)
./gradlew assembleBeta

# Build release
./gradlew assembleRelease

# Install APK
adb install -r app/build/outputs/apk/beta/app-beta.apk

# Clear logs and monitor
adb logcat -c && adb logcat -v time

# Get crash logs
adb logcat -d *:E > crash-log.txt

# Uninstall app
adb uninstall com.chain.app
adb uninstall com.chain.app.beta

# List installed packages
adb shell pm list packages | grep chain
```

---

## How to Share Crash Logs

When asking for help, include:

1. **Full crash stack trace** from Logcat
2. **Build variant** (release, beta, debug)
3. **Steps to reproduce** the crash
4. **Device info:** Android version, manufacturer
5. **What you tried:** ProGuard changes, etc.

**Example crash log to include:**

```
2025-11-17 10:23:45.123 12345-12345/com.chain.app E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.chain.app, PID: 12345
    java.lang.RuntimeException: Unable to start activity ComponentInfo{...}
        at android.app.ActivityThread.performLaunchActivity(...)
        at ...
    Caused by: java.lang.NullPointerException: Attempt to invoke virtual method...
        at com.chain.app.MainActivity.onCreate(MainActivity.kt:45)
        at ...
```

---

## Summary

**For quick debugging:**
1. Use **beta build variant** in Android Studio
2. View crashes in Logcat window
3. Add ProGuard keep rules as needed

**For production release:**
1. Build with `./gradlew assembleRelease`
2. Test on physical device with `adb install`
3. Monitor with `adb logcat`

**Most crashes are ProGuard/R8 removing classes.** Check the improved rules in `proguard-rules.pro` which should prevent common issues.

---

**Last Updated:** 2025-11-17
**Chain Version:** 1.0.0-beta
