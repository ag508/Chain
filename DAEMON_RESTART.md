# KAPT JDK17 Fix - Daemon Restart Required

## Problem

The KAPT module access error persists even after adding JVM args because:
1. **Gradle daemon** is still running with old JVM arguments
2. **Kotlin compiler daemon** is cached and not picking up new settings
3. **Android Studio** caches need to be cleared

## Solution - Stop All Daemons

### Option 1: Use the Script (Recommended)

**Windows:**
```bash
stop-daemons.bat
```

**Mac/Linux:**
```bash
./stop-daemons.sh
```

Then rebuild:
```bash
./gradlew assembleDebug
```

### Option 2: Manual Steps

1. **Stop Gradle Daemon:**
   ```bash
   ./gradlew --stop
   ```

2. **Kill Kotlin Daemon (Windows):**
   ```bash
   taskkill /F /IM "kotlin*.exe"
   ```

   **Kill Kotlin Daemon (Mac/Linux):**
   ```bash
   pkill -f "kotlin.*compile"
   ```

3. **Clean Build:**
   ```bash
   ./gradlew clean
   ```

4. **In Android Studio - Invalidate Caches:**
   - File → Invalidate Caches
   - Select "Invalidate and Restart"
   - Wait for indexing to complete

5. **Rebuild:**
   ```bash
   ./gradlew assembleDebug
   ```

### Option 3: If Still Failing

Close Android Studio completely, then:

```bash
# Stop everything
./gradlew --stop
pkill -f gradle
pkill -f kotlin

# Delete Gradle cache (nuclear option)
rm -rf ~/.gradle/caches
rm -rf ~/.gradle/daemon

# Delete project build folders
rm -rf .gradle
rm -rf app/build
rm -rf build

# Rebuild
./gradlew clean assembleDebug
```

## What Changed

Added to `gradle.properties`:

1. **Formatted JVM args with line continuation** for readability
2. **Added `kotlin.daemon.jvmargs`** - this was missing!
   - The Kotlin compiler has its own daemon
   - It needs the same JVM args as Gradle
   - This is why it wasn't working before

```properties
# Gradle daemon JVM args
org.gradle.jvmargs=-Xmx2048m ... --add-opens flags ...

# Kotlin daemon JVM args (CRITICAL!)
kotlin.daemon.jvmargs=-Xmx2048m ... --add-opens flags ...
```

## Why This Happens

1. **First build**: Gradle daemon starts with default JVM args
2. **You add JVM args**: Old daemon is still running
3. **Gradle doesn't restart daemon** automatically
4. **KAPT runs with old daemon** → still fails

The daemon must be **explicitly stopped** to pick up new JVM args.

## Verification

After stopping daemons and rebuilding, you should see:
```
BUILD SUCCESSFUL in Xs
```

No more KAPT module access errors.

## If Still Failing

Check your Java version:
```bash
java -version
```

If you're using JDK 20+, the `--add-opens` flags might have changed. Let me know your JDK version and I'll adjust.

## Alternative: Use JDK 11

If nothing works, the nuclear option is to use JDK 11:

1. **Download JDK 11** from Adoptium
2. **In Android Studio:**
   - File → Project Structure → SDK Location
   - Set JDK to JDK 11

JDK 11 doesn't have the strict module enforcement, so KAPT works without `--add-opens` flags.
