@echo off
REM Stop Gradle and Kotlin daemons to apply new JVM settings

echo Stopping Gradle daemon...
gradlew --stop

echo Killing any remaining Kotlin compile daemons...
taskkill /F /IM "kotlin*.exe" 2>nul

echo Cleaning build directories...
gradlew clean

echo.
echo All daemons stopped. Now run:
echo   gradlew assembleDebug
echo.
echo Or in Android Studio:
echo   File -^> Invalidate Caches -^> Invalidate and Restart
pause
