#!/bin/bash
# Stop Gradle and Kotlin daemons to apply new JVM settings

echo "Stopping Gradle daemon..."
./gradlew --stop

echo "Killing any remaining Kotlin compile daemons..."
pkill -f "kotlin.*compile" 2>/dev/null || true

echo "Cleaning build directories..."
./gradlew clean

echo "All daemons stopped. Now run:"
echo "  ./gradlew assembleDebug"
echo ""
echo "Or in Android Studio:"
echo "  File → Invalidate Caches → Invalidate and Restart"
