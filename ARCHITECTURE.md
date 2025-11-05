# Chain - Architecture Documentation

## Overview

Chain follows Clean Architecture principles with MVVM pattern for the presentation layer. The architecture is divided into three main layers: Presentation, Domain, and Data.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Composables │  │  ViewModels  │  │  Navigation      │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Use Cases   │  │  Models      │  │  Repositories    │  │
│  │              │  │              │  │  (Interfaces)    │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                        Data Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Repository  │  │  Database    │  │  Network         │  │
│  │  Impls       │  │  (Room)      │  │  (P2P + WebRTC)  │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Layer Details

### 1. Presentation Layer (`presentation/`)

**Purpose**: Handles UI and user interactions.

**Components**:
- **Composables**: Jetpack Compose UI components
- **ViewModels**: Manage UI state and business logic coordination
- **UI State**: Sealed classes representing different UI states

**Example Flow**:
```kotlin
ChatListScreen (Composable)
    ↓
ChatListViewModel (ViewModel)
    ↓
GetChatsUseCase (Domain Use Case)
```

**Key Files**:
- `presentation/chat/ChatScreen.kt` - Chat list UI
- `presentation/chat/ChatListViewModel.kt` - Chat list state management
- `presentation/MainActivity.kt` - Main activity entry point

### 2. Domain Layer (`domain/`)

**Purpose**: Contains business logic and domain models. This layer is framework-agnostic.

**Components**:
- **Models**: Pure Kotlin data classes representing business entities
- **Repository Interfaces**: Contracts for data operations
- **Use Cases**: Single-responsibility business logic units

**Example**:
```kotlin
// Domain Model
data class Message(
    val id: String,
    val content: String,
    val timestamp: Date
)

// Repository Interface
interface MessageRepository {
    suspend fun sendMessage(message: Message): Result<Message>
}

// Use Case
class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(content: String): Result<Message>
}
```

**Key Directories**:
- `domain/model/` - Business entities
- `domain/repository/` - Repository interfaces
- `domain/usecase/` - Business logic use cases

### 3. Data Layer (`data/`)

**Purpose**: Implements data operations and manages data sources.

**Components**:

#### Local Database (`data/local/`)
- **Room Database**: Encrypted with SQLCipher
- **DAOs**: Data Access Objects for database operations
- **Entities**: Database table representations
- **Converters**: Type converters for complex types

#### Encryption (`data/encryption/`)
- **SignalProtocolManager**: Manages encryption/decryption
- **SignalProtocolStore**: Stores encryption keys securely

#### Repository Implementations (`data/repository/`)
- Implement domain repository interfaces
- Coordinate between different data sources
- Handle data transformation

**Example**:
```kotlin
@Singleton
class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val encryptionRepository: EncryptionRepository
) : MessageRepository {
    override suspend fun sendMessage(message: Message): Result<Message> {
        // Implementation
    }
}
```

## Dependency Injection

Chain uses **Hilt** for dependency injection.

### Module Structure

```
di/
├── AppModule.kt           - Application-wide dependencies
├── DatabaseModule.kt      - Database and DAO providers
├── EncryptionModule.kt    - Encryption service providers
└── RepositoryModule.kt    - Repository bindings
```

### Example Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository
}
```

## Data Flow

### Sending a Message

```
User Input (Composable)
    ↓
ChatViewModel.sendMessage()
    ↓
SendMessageUseCase.invoke()
    ↓
EncryptionRepository.encryptMessage()
    ↓
MessageRepository.sendMessage()
    ↓
MessageDao.insertMessage() → Room Database
    ↓
P2PRepository.sendMessage() → Direct P2P (WebRTC Data Channel)
    ↓
If recipient offline → P2PRepository.storeForwardMessage()
```

### Receiving Messages

```
P2P Network (WebRTC Data Channel)
    ↓
P2PRepository.subscribeToMessages()
    ↓
EncryptionRepository.decryptMessage()
    ↓
MessageRepository.observeMessages()
    ↓
ChatViewModel (StateFlow)
    ↓
Composable (UI Update)
```

## Database Schema

### Tables

1. **users**
   - Primary Key: `id`
   - Columns: `id`, `publicKey`, `displayName`, `avatar`, `status`, `lastSeen`

2. **chats**
   - Primary Key: `id`
   - Columns: `id`, `type`, `name`, `participants`, `settings`, `updatedAt`

3. **messages**
   - Primary Key: `id`
   - Columns: `id`, `chatId`, `senderId`, `content`, `type`, `timestamp`, `status`
   - FTS Table: `messages_fts` for full-text search

4. **reactions**
   - Composite Key: `messageId`, `emoji`, `userId`

5. **calls**
   - Primary Key: `id`
   - Columns: `id`, `chatId`, `participants`, `type`, `status`, `startTime`

### Database Encryption

- **SQLCipher**: AES-256 encryption
- **Key Storage**: Android Keystore
- **Passphrase**: Generated and stored in EncryptedSharedPreferences

## Security Architecture

### Encryption Stack

```
Application Data
    ↓
Signal Protocol (E2E Encryption)
    ↓
Local Storage (SQLCipher)
    ↓
Android Keystore (Key Storage)
```

### Key Components

1. **SignalProtocolStore**
   - Stores identity keys, pre-keys, session keys
   - Uses EncryptedSharedPreferences
   - Hardware-backed with Android Keystore

2. **SignalProtocolManager**
   - Handles X3DH key agreement
   - Implements Double Ratchet algorithm
   - Manages session initialization

3. **Database Encryption**
   - SQLCipher for at-rest encryption
   - Unique passphrase per device
   - Automatic key derivation

## Testing Strategy

### Unit Tests
- **Domain Layer**: Use cases and business logic
- **ViewModels**: State management and user interactions
- **Repositories**: Data operations

### Integration Tests
- **Database**: DAO operations and queries
- **Encryption**: Signal Protocol implementation
- **End-to-End**: Complete message flows

### UI Tests
- **Composables**: UI rendering and interactions
- **Navigation**: Screen transitions

## Adding New Features

### Step-by-Step Guide

1. **Define Domain Model**
   ```kotlin
   // domain/model/YourModel.kt
   data class YourModel(val id: String, ...)
   ```

2. **Create Repository Interface**
   ```kotlin
   // domain/repository/YourRepository.kt
   interface YourRepository {
       suspend fun doSomething(): Result<YourModel>
   }
   ```

3. **Implement Data Layer**
   ```kotlin
   // data/local/entity/YourEntity.kt
   @Entity(tableName = "your_table")
   data class YourEntity(...)

   // data/local/dao/YourDao.kt
   @Dao
   interface YourDao { ... }

   // data/repository/YourRepositoryImpl.kt
   class YourRepositoryImpl @Inject constructor(...) : YourRepository
   ```

4. **Create Use Case**
   ```kotlin
   // domain/usecase/YourUseCase.kt
   class YourUseCase @Inject constructor(
       private val repository: YourRepository
   ) {
       suspend operator fun invoke(...): Result<YourModel>
   }
   ```

5. **Build ViewModel**
   ```kotlin
   // presentation/your/YourViewModel.kt
   @HiltViewModel
   class YourViewModel @Inject constructor(
       private val useCase: YourUseCase
   ) : ViewModel()
   ```

6. **Create Composable**
   ```kotlin
   // presentation/your/YourScreen.kt
   @Composable
   fun YourScreen(viewModel: YourViewModel = hiltViewModel())
   ```

7. **Add Dependency Injection**
   ```kotlin
   // di/RepositoryModule.kt
   @Binds
   abstract fun bindYourRepository(
       impl: YourRepositoryImpl
   ): YourRepository
   ```

## Best Practices

1. **Separation of Concerns**
   - Keep UI logic in Composables
   - Keep business logic in Use Cases
   - Keep data logic in Repositories

2. **Immutability**
   - Use `val` over `var`
   - Use immutable data classes
   - Use StateFlow for observable state

3. **Error Handling**
   - Use `Result<T>` for operations that can fail
   - Handle errors at the presentation layer
   - Provide meaningful error messages

4. **Coroutines**
   - Use `viewModelScope` for UI-related operations
   - Use `CoroutineScope(SupervisorJob())` for app-wide operations
   - Handle cancellation properly

5. **Testing**
   - Write unit tests for business logic
   - Mock dependencies in tests
   - Test edge cases and error scenarios

## Performance Considerations

1. **Database**
   - Use indexes for frequently queried columns
   - Implement pagination for large lists
   - Use Flow for reactive updates

2. **Memory**
   - Load images efficiently with Coil
   - Clear caches when appropriate
   - Use lazy loading for lists

3. **Network**
   - Maintain P2P connections efficiently
   - Implement exponential backoff for peer reconnection
   - Cache frequently accessed peer information
   - Optimize DHT lookups

## Next Steps

1. **P2P Networking Implementation**
   - Implement WebRTC data channel manager
   - Create DHT service for global peer discovery
   - Add mDNS service for local network discovery
   - Implement store-and-forward for offline messages

2. **Authentication**
   - OAuth integration (Google/Microsoft)
   - Biometric authentication
   - Key recovery mechanisms

3. **WebRTC Calls**
   - PeerConnection management
   - STUN/TURN server integration
   - Call signaling through P2P data channels

4. **Cloud Storage**
   - Google Drive API integration
   - OneDrive API integration
   - Encrypted file upload/download

5. **UI Completion**
   - Message composer
   - Media picker
   - Settings screens
   - Profile management

## Resources

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Signal Protocol Specifications](https://signal.org/docs/)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)
