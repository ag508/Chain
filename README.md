# Chain - Decentralized Messaging Platform

Chain is a revolutionary decentralized messaging platform that eliminates the need for central servers by leveraging blockchain technology and peer-to-peer communication. The platform provides all the features users expect from modern messaging apps while ensuring complete privacy, censorship resistance, and user data ownership.

## Overview

Chain combines blockchain technology, peer-to-peer networking, and end-to-end encryption to create a censorship-resistant communication system. The architecture eliminates central servers by using participants' devices as blockchain nodes, with messages transmitted as encrypted transactions that are automatically pruned after delivery.

### Key Features

- **End-to-End Encryption**: Uses Signal Protocol for military-grade encryption
- **Decentralized Architecture**: No central servers, peer-to-peer communication
- **Blockchain-Based**: Messages as blockchain transactions with automatic pruning
- **Multi-Platform**: Android, iOS, Windows, Mac, Linux, and Docker
- **Rich Messaging**: Text, images, videos, voice messages, documents
- **Group Chats**: Support for up to 100,000 members per group
- **Voice & Video Calls**: WebRTC-based real-time communication
- **Cloud Storage Integration**: Google Drive, OneDrive, iCloud, Dropbox
- **Disappearing Messages**: Configurable self-destructing messages
- **No Phone Numbers**: Authentication via OAuth, passkeys, or biometrics

## Architecture

### Technology Stack

- **Android**: Kotlin, Jetpack Compose, Coroutines, Flow
- **Dependency Injection**: Hilt/Dagger
- **Database**: Room with SQLCipher encryption
- **Encryption**: Signal Protocol (libsignal)
- **Blockchain**: Web3j for Ethereum-compatible chains
- **P2P Networking**: libp2p
- **WebRTC**: Google WebRTC for voice/video calls
- **Cloud Storage**: Google Drive, OneDrive, iCloud, Dropbox APIs

### Clean Architecture Layers

```
presentation/       - UI layer (Jetpack Compose)
├── theme/         - Theme and styling
├── screens/       - Screen composables
└── viewmodels/    - ViewModels

domain/            - Business logic layer
├── model/         - Domain models
├── repository/    - Repository interfaces
└── usecase/       - Use cases

data/              - Data layer
├── local/         - Room database
│   ├── entity/    - Database entities
│   ├── dao/       - Data access objects
│   └── converter/ - Type converters
├── encryption/    - Signal Protocol implementation
├── blockchain/    - Blockchain integration
├── p2p/          - P2P networking
├── webrtc/       - WebRTC implementation
└── cloud/        - Cloud storage integration
```

## Current Implementation Status

### ✅ Completed

1. **Project Setup**
   - Gradle configuration with all dependencies
   - Android manifest with required permissions
   - ProGuard rules for release builds

2. **Architecture Foundation**
   - Clean Architecture structure (domain, data, presentation)
   - Hilt dependency injection setup
   - Application class with notification channels

3. **Domain Layer**
   - Core domain models (User, Message, Chat, Call, etc.)
   - Repository interfaces for all major features
   - Blockchain and encryption models

4. **Data Layer - Local Storage**
   - Room database entities for all models
   - DAOs with comprehensive CRUD operations
   - Full-text search support for messages
   - Type converters for complex types
   - SQLCipher integration for encrypted database

5. **Encryption**
   - Signal Protocol integration (libsignal)
   - SignalProtocolStore implementation with Android Keystore
   - SignalProtocolManager for encryption/decryption
   - Pre-key bundle generation
   - Safety number verification
   - Secure key storage with EncryptedSharedPreferences

6. **Preferences**
   - DataStore-based user preferences
   - Secure storage for user settings

7. **UI Foundation**
   - Material 3 theme with dynamic colors
   - Basic MainActivity with Compose
   - Color scheme for light/dark modes

### 🚧 In Progress

- Repository implementations (connecting domain interfaces to data sources)
- Blockchain manager and P2P networking
- WebRTC call management
- Authentication system

### 📋 Planned

- Cloud storage integration
- Message synchronization
- Group encryption
- Call features
- UI components and screens
- Comprehensive testing

## Building the Project

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34
- Gradle 8.2

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/Chain.git
cd Chain
```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Build and run:
```bash
./gradlew assembleDebug
```

## Security

### Encryption

- **Signal Protocol**: Industry-standard end-to-end encryption
- **Perfect Forward Secrecy**: Each message uses unique keys
- **Post-Compromise Security**: Key rotation limits breach impact
- **SQLCipher**: Database encryption at rest
- **Android Keystore**: Hardware-backed key storage

### Privacy

- **No Phone Numbers**: No PII required for registration
- **Metadata Protection**: Minimal exposure through P2P routing
- **Local Data Control**: Users own their data
- **No Tracking**: No analytics or telemetry

## Project Structure

```
Chain/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/chain/app/
│           │   ├── ChainApplication.kt
│           │   ├── domain/
│           │   │   ├── model/
│           │   │   └── repository/
│           │   ├── data/
│           │   │   ├── local/
│           │   │   ├── encryption/
│           │   │   └── preferences/
│           │   ├── di/
│           │   └── presentation/
│           └── res/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

## Contributing

This is currently a development project. Contribution guidelines will be added as the project matures.

## License

TBD

## Acknowledgments

- Signal Protocol for encryption architecture
- Ethereum/Web3j for blockchain integration
- WebRTC for real-time communication
- Android Jetpack for modern Android development

---

**Note**: This project is under active development. The implementation follows the comprehensive design document and requirements specification for building a production-ready decentralized messaging platform.
