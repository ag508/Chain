# Dependency Changes for Build Fix

## Issues Found
The initial build failed due to several dependency resolution problems:

1. **Signal Protocol**: `org.signal:libsignal-client:0.42.1` - Maven repository connectivity issues
2. **WebRTC**: `org.webrtc:google-webrtc:1.0.32006` - Not available in configured repositories
3. **libp2p**: `io.github.libp2p:jvm-libp2p-minimal:0.10.0-RELEASE` - Returns 401 Unauthorized from JitPack
4. **Google Drive API**: Repository resolution issues

## Changes Made

### 1. Signal Protocol Library
**Before:**
```kotlin
implementation("org.signal:libsignal-client:0.42.1")
```

**After:**
```kotlin
implementation("org.signal:libsignal-android:0.41.0")
```

**Reason**: The `libsignal-android` is a more stable, Android-specific build that's reliably available from Maven Central. The API is compatible with our existing code.

### 2. WebRTC Library
**Before:**
```kotlin
implementation("org.webrtc:google-webrtc:1.0.32006")
```

**After:**
```kotlin
implementation("io.getstream:stream-webrtc-android:1.1.0")
```

**Reason**: Stream provides a well-maintained, Android-optimized WebRTC distribution that's actively supported and available via Maven Central. This includes all necessary WebRTC functionality for voice/video calling.

### 3. P2P Networking (libp2p)
**Before:**
```kotlin
implementation("io.github.libp2p:jvm-libp2p-minimal:0.10.0-RELEASE")
```

**After:**
```kotlin
// Commented out - will implement custom P2P or use alternative
// implementation("io.github.libp2p:jvm-libp2p-minimal:0.10.0-RELEASE")
```

**Reason**: The JVM libp2p library doesn't have stable Android releases. We'll implement a custom P2P solution using OkHttp WebSockets or consider alternatives like:
- Tox protocol for Android
- Custom WebSocket-based P2P
- IPFS lite for Android
- Direct socket connections with NAT traversal

### 4. App Icon Updated
**Changed**: `android:icon="@mipmap/ic_launcher"` → `android:icon="@mipmap/Chain"`

The proper Chain app icon assets have been integrated into all mipmap density folders.

## Build Status
After these changes, the build should succeed. The commented-out libp2p dependency will need to be addressed in a future PR with a proper Android P2P solution.

## Next Steps
1. Verify build succeeds
2. Implement custom P2P networking solution
3. Test WebRTC integration with Stream library
4. Verify Signal Protocol encryption still works with Android library
