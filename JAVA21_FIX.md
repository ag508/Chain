# Java 21 Incompatibility with KAPT

## Problem Identified

You're using **Java 21.0.6**, but Kotlin 1.9.20's KAPT does not support Java 21.

Error:
```
java.lang.NoSuchMethodError: 'com.sun.tools.javac.tree.JCTree$JCImport
org.jetbrains.kotlin.kapt3.javac.KaptTreeMaker.Import(...)'
```

**Root Cause:**
- Java 21 changed internal compiler API signatures
- Kotlin 1.9.20 KAPT was built for Java 17
- KAPT tries to call methods that don't exist/have different signatures in Java 21

## Solution: Force Java 17 Toolchain

I've added this to `app/build.gradle.kts`:

```kotlin
kotlin {
    jvmToolchain(17)
}
```

This tells Gradle to:
1. Download Java 17 automatically if not present
2. Use Java 17 for compilation (even though your system has Java 21)
3. Keep Java 21 for other uses

## What Happens Next

When you rebuild, Gradle will:
```
> Task :prepareKotlinBuildScriptModel
Gradle will download JDK 17 toolchain...
```

This is normal and only happens once.

## Alternative Solutions

### Option 1: Install Java 17 Manually (Faster)

Download and install JDK 17:
- Oracle: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Adoptium (recommended): https://adoptium.net/temurin/releases/?version=17

Then in Android Studio:
1. File → Project Structure → SDK Location
2. Set "Gradle JDK" to JDK 17

### Option 2: Upgrade Kotlin (Not Recommended Now)

We could upgrade to Kotlin 2.0+ which supports Java 21, but:
- May break other dependencies
- Requires testing all code
- Better to do after we get a successful build

## Recommended Action

**Just rebuild** - Gradle will download Java 17 automatically:

```bash
./gradlew clean assembleDebug
```

Or in Android Studio:
```
Build → Clean Project
Build → Rebuild Project
```

Gradle's toolchain feature will handle everything.

## Build Time Note

First build after this change will be slower:
- Downloading JDK 17 (~200MB)
- Re-indexing
- Full clean build

Subsequent builds will be normal speed.

## Verification

After successful build, you can verify:
```bash
./gradlew --version
```

You'll see:
```
JVM:          17.x.x (automatically downloaded)
```

Even though `java -version` still shows 21.

## Why This Is Better Than Downgrading System Java

- You can keep Java 21 for other projects
- Gradle manages the JDK automatically
- No system-wide changes needed
- Other projects using Java 21 won't be affected

## If This Still Fails

1. Delete Gradle cache:
   ```bash
   ./gradlew clean --no-daemon
   rm -rf ~/.gradle/caches
   ```

2. In Android Studio:
   File → Invalidate Caches → Invalidate and Restart

3. Report the exact error message

The toolchain approach is the cleanest solution for your Java 21 system.
