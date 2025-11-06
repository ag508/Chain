plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("kapt")
}

// Force Java 17 toolchain (required for KAPT compatibility)
// This ensures Java 17 is used for compilation even if system has Java 21+
kotlin {
    jvmToolchain(17)
}

// KSP configuration for Room schema export
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.chain.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.chain.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // NDK configuration for 16 KB page alignment
        ndk {
            // Ensure all ABIs are built with proper alignment
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Enable 16 KB page alignment for release builds
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            isDebuggable = true

            // Enable 16 KB page alignment for debug builds
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    // Kotlin 2.0 uses built-in Compose compiler, no need for kotlinCompilerExtensionVersion

    // NDK r28+ provides automatic 16 KB page size alignment for native libraries
    // Combined with AGP 8.5.1+, this ensures all .so files are properly aligned
    ndkVersion = "28.0.12674087"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/license.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/notice.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/*.kotlin_module"
        }

        // Enable 16 KB page alignment for native libraries
        // Required for Android 15+ devices with 16 KB page size
        // useLegacyPackaging=false ensures libraries are compressed and aligned properly
        jniLibs {
            useLegacyPackaging = false
        }
    }

    // Configure APK/AAB splits for better native library handling
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = true
        }
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Hilt Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // Room Database with SQLCipher
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    ksp("androidx.room:room-compiler:2.6.0")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // Signal Protocol (libsignal) - Using Android library
    implementation("org.signal:libsignal-android:0.41.0")

    // Cryptography
    // Using newer version to avoid conflicts with Web3j dependencies
    implementation("org.bouncycastle:bcprov-jdk18on:1.73")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // WebRTC - Using Stream's distribution (more reliable)
    implementation("io.getstream:stream-webrtc-android:1.1.0")

    // P2P Networking - Pure P2P approach (no blockchain)
    // DHT for global peer discovery
    implementation("de.cgrotz:kademlia:1.0.1")

    // mDNS/Bonjour for local network discovery
    implementation("org.jmdns:jmdns:3.5.8")

    // IPFS Java library for DHT bootstrap nodes (optional, for wider network)
    implementation("com.github.ipfs:java-ipfs-http-client:v1.3.3")

    // Cloud Storage SDKs
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20251019-2.0.0")
    implementation("com.microsoft.graph:microsoft-graph:5.77.0")
    implementation("com.dropbox.core:dropbox-core-sdk:5.4.5")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Serialization
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Protobuf for efficient P2P message serialization
    implementation("com.google.protobuf:protobuf-javalite:3.24.0")
    implementation("com.google.protobuf:protobuf-kotlin-lite:3.24.0")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.1.0")

    // Work Manager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore for preferences
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Resolve BouncyCastle version conflicts
configurations.all {
    resolutionStrategy {
        force("org.bouncycastle:bcprov-jdk18on:1.73")
    }
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

// 16 KB Page Size Support Configuration
// ========================================
// AGP 8.5.1+ (currently using 8.7.3) automatically aligns native libraries at 16 KB boundaries
// during APK/AAB packaging. Combined with NDK r28+, this ensures full compatibility with
// Android 15+ devices using 16 KB page sizes.
//
// No manual alignment tasks or extraction flags needed - AGP handles everything automatically.
// The configuration above (useLegacyPackaging = false) enables uncompressed libraries which
// AGP will zip-align on 16 KB boundaries during the build process.
//
// References:
// - https://developer.android.com/guide/practices/page-sizes
// - https://android-developers.googleblog.com/2025/07/transition-to-16-kb-page-sizes-android-apps-games-android-studio.html
