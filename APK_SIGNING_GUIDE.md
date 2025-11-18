# Chain App - APK Signing Guide for Beta Testing

This guide explains how to generate signed APKs for distributing Chain to beta testers.

## Table of Contents
- [Quick Start](#quick-start)
- [One-Time Setup: Create Release Keystore](#one-time-setup-create-release-keystore)
- [Build Signed APKs](#build-signed-apks)
- [Distribution Options](#distribution-options)
- [Security Best Practices](#security-best-practices)
- [Troubleshooting](#troubleshooting)

---

## Quick Start

If you haven't set up signing yet, follow these steps:

### Option 1: Debug APK (For Quick Testing)
```bash
# In Android Studio:
# Build > Build Bundle(s) / APK(s) > Build APK(s)

# Or via command line:
./gradlew assembleDebug

# APK location: app/build/outputs/apk/debug/app-debug.apk
```

**Note:** Debug APKs use the default Android debug keystore. They work for testing but should NOT be used for production or public beta releases.

### Option 2: Release APK (Recommended for Beta Testing)
Requires one-time keystore setup (see below).

---

## One-Time Setup: Create Release Keystore

### Step 1: Generate a Keystore

Open a terminal in the project root directory and run:

```bash
keytool -genkey -v -keystore chain-release-key.keystore \
  -alias chain-release-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

You will be prompted to enter:

1. **Keystore password** - Choose a strong password (SAVE THIS!)
2. **Re-enter password** - Confirm your password
3. **First and last name** - Your name or company name
4. **Organizational unit** - (e.g., "Development" or press Enter to skip)
5. **Organization** - (e.g., "Chain" or press Enter to skip)
6. **City/Locality** - Your city
7. **State/Province** - Your state
8. **Country code** - Two-letter country code (e.g., "US")
9. **Confirm details** - Type "yes"
10. **Key password** - Press Enter to use the same password as keystore, or enter a different one

**IMPORTANT:**
- **SAVE YOUR PASSWORDS** - You cannot recover them!
- **BACKUP** the `chain-release-key.keystore` file to a secure location
- **NEVER** commit the keystore file or passwords to Git
- Without this keystore, you cannot update your app in the future!

### Step 2: Create keystore.properties

Copy the template file:

```bash
cp keystore.properties.template keystore.properties
```

Edit `keystore.properties` and update with your passwords:

```properties
storeFile=chain-release-key.keystore
storePassword=YOUR_ACTUAL_KEYSTORE_PASSWORD
keyAlias=chain-release-key
keyPassword=YOUR_ACTUAL_KEY_PASSWORD
```

**Note:** `keystore.properties` is automatically ignored by Git for security.

---

## Build Signed APKs

### Method 1: Android Studio (Recommended)

1. Open the project in Android Studio
2. **Build** → **Generate Signed Bundle / APK**
3. Select **APK**
4. Click **Next**
5. Choose **Create new...** or select existing keystore
6. Fill in keystore details:
   - Key store path: Browse to `chain-release-key.keystore`
   - Key store password: Your keystore password
   - Key alias: `chain-release-key`
   - Key password: Your key password
7. Click **Next**
8. Select **release** build variant
9. Check both **V1 (Jar Signature)** and **V2 (Full APK Signature)**
10. Click **Finish**

Output: `app/release/app-release.apk`

### Method 2: Command Line

If you've set up `keystore.properties`:

```bash
# Build release APK (automatically signed)
./gradlew assembleRelease

# Output location:
# app/build/outputs/apk/release/app-release.apk
```

### Build Multiple APK Variants

The project is configured to generate split APKs for different CPU architectures:

```bash
./gradlew assembleRelease
```

This creates:
- `app-armeabi-v7a-release.apk` - For older ARM devices (32-bit)
- `app-arm64-v8a-release.apk` - For modern ARM devices (64-bit) **← Most common**
- `app-x86-release.apk` - For Intel devices (32-bit)
- `app-x86_64-release.apk` - For Intel devices (64-bit)
- `app-universal-release.apk` - Works on all devices (larger file size)

**For beta testing, use `app-universal-release.apk`** - it works on all devices.

---

## Distribution Options

### Option 1: Direct APK Distribution

1. Build the release APK (see above)
2. Upload `app-universal-release.apk` to:
   - Google Drive
   - Dropbox
   - Firebase App Distribution
   - Your own server

3. Share the download link with beta testers

4. Testers must enable "Install from unknown sources" on their devices:
   - Settings → Security → Unknown Sources (enable)
   - Or: Settings → Apps → Special access → Install unknown apps → (Select browser/file manager)

### Option 2: Google Play Console (Internal Testing)

1. Build an Android App Bundle instead of APK:
   ```bash
   ./gradlew bundleRelease
   # Output: app/build/outputs/bundle/release/app-release.aab
   ```

2. Upload to Google Play Console → Internal Testing
3. Add tester email addresses
4. Testers install via Play Store

**Advantage:** Automatic signing with Google Play App Signing, easier updates

### Option 3: Firebase App Distribution

1. Install Firebase CLI: `npm install -g firebase-tools`
2. Build release APK
3. Upload to Firebase:
   ```bash
   firebase appdistribution:distribute app/build/outputs/apk/release/app-universal-release.apk \
     --app YOUR_FIREBASE_APP_ID \
     --groups beta-testers
   ```

---

## Security Best Practices

### DO:
✅ **Backup your keystore file** to multiple secure locations
✅ **Use strong passwords** (minimum 12 characters with letters, numbers, symbols)
✅ **Keep keystore.properties out of Git** (already configured in .gitignore)
✅ **Store passwords in a password manager** (1Password, LastPass, Bitwarden)
✅ **Use the same keystore for all app updates** (required by Android)
✅ **Consider Google Play App Signing** for production releases

### DON'T:
❌ **Don't commit keystore files to Git**
❌ **Don't share your keystore with untrusted parties**
❌ **Don't lose your keystore** (you cannot update the app without it!)
❌ **Don't use weak passwords**
❌ **Don't email your keystore or passwords**
❌ **Don't use debug keys for production releases**

---

## Version Management

Each release should increment the version:

Edit `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2        // Increment for each release
    versionName = "1.0.1"  // User-visible version
}
```

**versionCode** - Integer that MUST increase with each release
**versionName** - String shown to users (e.g., "1.0.0", "1.1.0", "2.0.0-beta")

---

## Troubleshooting

### Problem: "keystore.properties not found" warning

**Solution:** This is expected if you haven't created the release keystore yet. The build will use debug signing as a fallback. For beta testing, create the release keystore (see above).

### Problem: Build fails with signing error

**Solutions:**
1. Check that `keystore.properties` exists in project root
2. Verify passwords in `keystore.properties` are correct
3. Ensure `chain-release-key.keystore` exists at the path specified
4. Try building with debug signing: `./gradlew assembleDebug`

### Problem: "Keystore was tampered with, or password incorrect"

**Solution:**
- Double-check your keystore password in `keystore.properties`
- Make sure you're using the correct keystore file
- If you've lost the password, you'll need to create a new keystore (and release as a new app)

### Problem: APK won't install on device

**Solutions:**
1. Uninstall any existing version of Chain first
2. Enable "Install from unknown sources" in Settings
3. Make sure the APK is signed (not just assembled)
4. Try the universal APK instead of architecture-specific ones

### Problem: "App not installed" error

**Causes:**
- Device architecture doesn't match APK (use universal APK)
- Signature conflict (uninstall old version first)
- Insufficient storage space
- Corrupted APK download (re-download)

---

## Testing Checklist

Before distributing to beta testers:

- [ ] Build signed release APK
- [ ] Install on your own device
- [ ] Test core features (user registration, messaging, etc.)
- [ ] Verify no debug features are visible
- [ ] Check app version number is correct
- [ ] Test on at least 2 different devices
- [ ] Prepare release notes for testers
- [ ] Set up feedback collection method (email, form, etc.)

---

## Current Build Configuration

- **Version Code:** 1
- **Version Name:** 1.0.0
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 34 (Android 14)
- **Signing:** Configured with fallback to debug
- **ProGuard:** Enabled for release builds
- **16KB Page Size:** Supported (Android 15+)

---

## Quick Reference Commands

```bash
# Build debug APK (quick testing)
./gradlew assembleDebug

# Build release APK (beta distribution)
./gradlew assembleRelease

# Build Android App Bundle (Play Store)
./gradlew bundleRelease

# Clean build cache
./gradlew clean

# Clean + rebuild
./gradlew clean assembleRelease

# View all build tasks
./gradlew tasks
```

---

## Support

If you encounter issues:

1. Check this guide's Troubleshooting section
2. Review Android Studio's Build Output window
3. Check `app/build/outputs/logs/` for detailed logs
4. Consult Android documentation: https://developer.android.com/studio/publish/app-signing

---

**Last Updated:** 2025-11-17
**Chain Version:** 1.0.0
**Build System:** Gradle 8.7.3 / AGP 8.7.3
