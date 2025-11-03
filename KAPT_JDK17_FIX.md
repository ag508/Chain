# KAPT and BouncyCastle Conflict Fix

## Issues Fixed

### 1. BouncyCastle Duplicate Classes

**Problem:**
```
Duplicate class org.bouncycastle.* found in modules:
- bcprov-jdk15on-1.70 (explicitly added)
- bcprov-jdk18on-1.73 (transitive from Web3j)
```

**Root Cause:**
- We explicitly added `bcprov-jdk15on:1.70` for cryptography
- Web3j depends on `bcprov-jdk18on:1.73`
- Both versions contain identical package names, causing conflict

**Solution:**
1. Updated explicit dependency to `bcprov-jdk18on:1.73`
2. Added `resolutionStrategy` to force version 1.73 across all dependencies
3. This ensures only one BouncyCastle version is in the classpath

```kotlin
// Use newer version
implementation("org.bouncycastle:bcprov-jdk18on:1.73")

// Force this version everywhere
configurations.all {
    resolutionStrategy {
        force("org.bouncycastle:bcprov-jdk18on:1.73")
    }
}
```

### 2. KAPT Module Access Error on JDK 17+

**Problem:**
```
java.lang.IllegalAccessError: superclass access check failed:
class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler
cannot access class com.sun.tools.javac.main.JavaCompiler
because module jdk.compiler does not export com.sun.tools.javac.main
```

**Root Cause:**
- JDK 9+ introduced the Java Platform Module System (JPMS)
- JDK 17 enforces stronger encapsulation
- KAPT needs access to internal compiler APIs in `jdk.compiler` module
- By default, these packages are not exported/opened to unnamed modules
- KAPT runs in an unnamed module and can't access the internal APIs

**Solution:**
Added JVM arguments to `gradle.properties` to open necessary packages:

```properties
org.gradle.jvmargs=
  -Xmx2048m
  -Dfile.encoding=UTF-8
  --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.jvm=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
  --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

These `--add-opens` flags tell JDK to open internal compiler packages to ALL-UNNAMED modules (including KAPT).

## Technical Details

### JDK Module System
- **JDK 8 and earlier**: All internal APIs accessible
- **JDK 9-16**: Module system introduced, but some backward compatibility
- **JDK 17+**: Stronger encapsulation, internal APIs restricted by default

### KAPT Requirements
KAPT (Kotlin Annotation Processing Tool) needs to:
1. Create Java stubs from Kotlin code
2. Run Java annotation processors (Hilt, Room, etc.)
3. Access internal Java compiler APIs to do this

### Why Not KSP?
We're using both:
- **KSP** for Room (newer, faster, Kotlin-native)
- **KAPT** for Hilt (doesn't support KSP yet fully)

Once Hilt fully supports KSP, we can remove KAPT entirely.

## Alternative Solutions

If the JVM args don't work, alternatives are:

1. **Use JDK 11**:
   ```bash
   # Set in gradle.properties
   org.gradle.java.home=/path/to/jdk-11
   ```

2. **Wait for Hilt KSP support**:
   - Hilt is working on full KSP support
   - Would eliminate need for KAPT

3. **Use a custom Java toolchain**:
   ```kotlin
   kotlin {
       jvmToolchain(11)
   }
   ```

## Verification

After these changes:

```bash
# Clean previous build
./gradlew clean

# Rebuild
./gradlew assembleDebug

# Expected: BUILD SUCCESSFUL
```

## References
- [JEP 261: Module System](https://openjdk.org/jeps/261)
- [KAPT JDK 17 issues](https://youtrack.jetbrains.com/issue/KT-45545)
- [Gradle JVM args](https://docs.gradle.org/current/userguide/build_environment.html#sec:configuring_jvm_memory)
