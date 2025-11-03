# Build Verification Checklist

## Pre-Build Setup

### 1. System Requirements
- [ ] Android Studio Hedgehog (2023.1.1) or later installed
- [ ] JDK 17 installed and configured
- [ ] Android SDK 34 installed
- [ ] Minimum 8GB RAM available

### 2. Clone and Setup
```bash
git clone <your-repo-url>
cd Chain
git checkout claude/chain-messaging-platform-design-011CUmDBfoupxn6nTooKuVSV
```

## Build Steps

### 1. Gradle Sync
```bash
./gradlew --version  # Verify Gradle works
./gradlew clean      # Clean build
```

**Expected Issues:**
- Gradle wrapper download (first time only)
- Dependency resolution (may take 5-10 minutes first time)

### 2. Compile Check
```bash
./gradlew compileDebugKotlin
```

**Expected Issues:**
- Missing imports - fix with Alt+Enter in Android Studio
- KSP/KAPT annotation processor setup
- Room schema location warning (safe to ignore)

### 3. Full Build
```bash
./gradlew assembleDebug
```

**Success Output:**
```
BUILD SUCCESSFUL in XXs
```

**If Build Fails:**
1. Check the error message carefully
2. Look for missing dependencies
3. Check Gradle version compatibility
4. Verify JDK version

### 4. Run Tests (if any)
```bash
./gradlew test
```

## Known Potential Issues

### Issue 1: KSP Configuration
**Error:** "Could not resolve com.google.devtools.ksp"

**Fix:** Ensure you have internet connection for first build. KSP plugin will download.

### Issue 2: Room Schema Export
**Warning:** "Schema export directory is not provided"

**Fix:** Already configured in build.gradle.kts - safe to ignore warning.

### Issue 3: SQLCipher Dependency
**Error:** "Could not find net.zetetic:android-database-sqlcipher"

**Fix:** Ensure `mavenCentral()` is in repositories.

### Issue 4: Signal Protocol
**Error:** "Could not find org.signal:libsignal-client"

**Fix:** This is a valid dependency. Check internet connection.

### Issue 5: Web3j Dependency
**Error:** Large dependency download

**Fix:** Normal - Web3j is ~30MB. Be patient.

### Issue 6: Missing Launcher Icon
**Error:** "Resource not found: ic_launcher"

**Fix:** Use placeholder icons for now. See APP_ICON_TODO.md.

## Android Studio Specific Checks

### 1. Open Project
- Open Android Studio
- File → Open → Select Chain folder
- Wait for Gradle sync

### 2. Check for Errors
- Look at "Build" tab at bottom
- Check "Problems" tab
- Resolve any red underlines in code

### 3. Verify Dependencies
- Open Project Structure (Ctrl+Alt+Shift+S)
- Check SDK is set to API 34
- Check JDK is version 17

### 4. Build Menu
- Build → Make Project (Ctrl+F9)
- Should complete without errors

## After Successful Build

### Next Steps:
1. ✅ Fix any compilation warnings
2. ✅ Replace placeholder launcher icons with proper assets
3. ✅ Verify database schema generation in `app/schemas/`
4. ✅ Check ProGuard rules don't break release build:
   ```bash
   ./gradlew assembleRelease
   ```

### Then Report Back:
- Build time
- Any warnings (even if build succeeds)
- Any dependency conflicts
- Memory usage during build

## If Build Succeeds

We can proceed with:
1. Implementing blockchain integration
2. Adding authentication flows
3. Building WebRTC calling features
4. Creating UI components

## If Build Fails

Provide:
1. Full error message
2. Gradle version: `./gradlew --version`
3. JDK version: `java -version`
4. Android Studio version
5. Operating system

---

**Important**: Don't try to run the app yet - it will crash because:
- No main activity navigation set up
- No authentication flow
- No blockchain connection
- Placeholder UI only

We'll implement these features after build verification!
