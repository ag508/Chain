# Build Fix - Jetifier and Service Errors

## Issues Found

### 1. Jetifier Error with Java 19 Libraries
**Error Message:**
```
Failed to transform jackson-core-2.15.0.jar using Jetifier.
Reason: IllegalArgumentException, message: Unsupported class file major version 63.
```

**Root Cause:**
- Class file major version 63 = Java 19
- Jetifier (AndroidX migration tool) cannot process Java 19+ bytecode
- `jackson-core` and `fastdoubleparser` are transitive dependencies from Web3j compiled with Java 19
- Jetifier was enabled in `gradle.properties`

**Solution:**
- **Disabled Jetifier** by setting `android.enableJetifier=false`
- All our dependencies are already AndroidX compatible, so Jetifier is not needed
- This allows Java 19 compiled libraries to be used without transformation

### 2. Non-existent Services in AndroidManifest
**Error:**
Services declared but not yet implemented:
- `.data.blockchain.BlockchainNodeService`
- `.data.webrtc.CallService`

**Solution:**
- **Commented out** service declarations
- Added TODO comments
- Will be implemented in future PRs when blockchain and WebRTC features are added

## Changes Made

### gradle.properties
```kotlin
// Before
android.enableJetifier=true

// After
android.enableJetifier=false
```

**Why this works:**
- Jetifier is only needed for migrating old support library dependencies to AndroidX
- All our direct dependencies are already AndroidX compatible:
  - Room, Hilt, Compose, Coroutines, etc. are all AndroidX
  - Web3j, Signal Protocol, Stream WebRTC don't need Jetifier
- Jetifier was blocking Java 19+ compiled transitive dependencies

### AndroidManifest.xml
```xml
<!-- Before -->
<service android:name=".data.blockchain.BlockchainNodeService" ... />
<service android:name=".data.webrtc.CallService" ... />

<!-- After -->
<!-- TODO: Implement these services later -->
<!--
<service android:name=".data.blockchain.BlockchainNodeService" ... />
<service android:name=".data.webrtc.CallService" ... />
-->
```

## Build Status
After these changes, the build should succeed without errors.

## Technical Details

### About Jetifier
- **Purpose**: Migrates old Android Support Library to AndroidX
- **When needed**: Only if you have dependencies still using `android.support.*`
- **Our case**: Not needed - all dependencies use AndroidX or are pure Java/Kotlin

### Java Version Compatibility
- **Java 19 (major version 63)**: Modern libraries are compiled with newer Java
- **Android**: Supports newer Java bytecode in dependencies
- **Jetifier limitation**: Only supports up to Java 11 bytecode

### Services to Implement Later
1. **BlockchainNodeService**
   - Will manage blockchain node connectivity
   - Handles P2P networking
   - Runs as foreground service for reliability

2. **CallService**
   - Will manage WebRTC voice/video calls
   - Maintains call sessions
   - Runs as foreground service during active calls

## Verification Steps

After pulling these changes:

```bash
# Clean previous build
./gradlew clean

# Rebuild
./gradlew assembleDebug

# Expected result: BUILD SUCCESSFUL
```

## Future Work

When implementing blockchain and calling features:
1. Create `BlockchainNodeService.kt` in `app/src/main/java/com/chain/app/data/blockchain/`
2. Create `CallService.kt` in `app/src/main/java/com/chain/app/data/webrtc/`
3. Uncomment service declarations in AndroidManifest.xml
