# Chain Messaging App - Complete UI Implementation Plan

## 📋 Overview
Transform Chain into a WhatsApp-level messaging app with glassmorphism design, persistent authentication, and complete messaging features.

---

## 🎯 Phase 1: Foundation & Authentication (Priority: CRITICAL)

### 1.1 Persistent User Authentication
**Goal**: User stays logged in after closing app

**Implementation**:
- [ ] Create `AuthState` sealed class (Authenticated, Unauthenticated, Loading)
- [ ] Store auth token in EncryptedSharedPreferences
- [ ] Store user profile in Room database
- [ ] Check auth state on app startup
- [ ] Navigate to appropriate screen based on auth state
- [ ] Add logout functionality

**Files to Create/Modify**:
- `data/local/entity/UserEntity.kt` (update)
- `data/preferences/AuthPreferences.kt` (new)
- `domain/model/AuthState.kt` (new)
- `presentation/auth/AuthViewModel.kt` (update)
- `presentation/MainActivity.kt` (update navigation logic)

---

## 🎨 Phase 2: Glassmorphism Design System (Priority: HIGH)

### 2.1 Core UI Components Library
**Goal**: Reusable glassmorphism components

**Components to Create**:
- [ ] `GlassCard` - Card with glass effect
- [ ] `GlassButton` - Button with glass effect
- [ ] `GlassTextField` - Input field with glass effect
- [ ] `GlassTopBar` - App bar with glass effect
- [ ] `GlassBottomBar` - Bottom navigation with glass effect
- [ ] `GlassDialog` - Modal dialog with glass effect
- [ ] `GlassSheet` - Bottom sheet with glass effect
- [ ] `GlassChip` - Chip/tag with glass effect

**Files to Create**:
- `presentation/components/glass/GlassCard.kt`
- `presentation/components/glass/GlassButton.kt`
- `presentation/components/glass/GlassTextField.kt`
- `presentation/components/glass/GlassTopBar.kt`
- `presentation/components/glass/GlassBottomBar.kt`
- `presentation/components/glass/GlassDialog.kt`
- `presentation/components/glass/GlassSheet.kt`
- `presentation/components/glass/GlassChip.kt`
- `presentation/theme/GlassEffects.kt`

---

## 💬 Phase 3: Chat List Screen (Priority: HIGH)

### 3.1 Enhanced Chat List UI
**Goal**: Complete chat list with search, menu, and add button

**Features**:
- [ ] Glass-styled chat list items
- [ ] Real-time search functionality
- [ ] Three-dot menu (Profile, Settings, Logout)
- [ ] Floating action button (FAB) for adding contacts/groups
- [ ] Pull-to-refresh for syncing messages
- [ ] Unread message badges
- [ ] Last message preview
- [ ] Timestamp display
- [ ] Online status indicators

**Components**:
- [ ] `ChatListItem` with glassmorphism
- [ ] `SearchBar` with live search
- [ ] `TopBarMenu` with profile/settings
- [ ] `AddContactFAB`
- [ ] `ChatListState` (Loading, Success, Error, Empty)

**Files to Create/Modify**:
- `presentation/chat/list/ChatListScreen.kt` (major update)
- `presentation/chat/list/ChatListViewModel.kt` (add search logic)
- `presentation/chat/list/components/ChatListItem.kt` (new)
- `presentation/chat/list/components/SearchBar.kt` (new)
- `presentation/chat/list/components/TopBarMenu.kt` (new)
- `presentation/chat/list/components/AddContactDialog.kt` (new)

---

## 👥 Phase 4: Add Contacts & Groups (Priority: HIGH)

### 4.1 Add Contact/Group Dialog
**Goal**: Add new contacts and create groups

**Features**:
- [ ] Search contacts by phone number
- [ ] QR code scanner for adding contacts
- [ ] Create group functionality
- [ ] Select multiple contacts for groups
- [ ] Group name and icon selection
- [ ] Invite link generation

**Components**:
- [ ] `AddContactDialog` with tabs (Add Contact, Create Group)
- [ ] `ContactSearchField`
- [ ] `QRCodeScanner`
- [ ] `GroupCreationFlow`
- [ ] `ContactSelectionList`

**Files to Create**:
- `presentation/contacts/AddContactDialog.kt`
- `presentation/contacts/ContactSearchScreen.kt`
- `presentation/contacts/QRCodeScannerScreen.kt`
- `presentation/groups/CreateGroupScreen.kt`
- `presentation/groups/GroupSetupScreen.kt`

---

## 💬 Phase 5: Complete Chat Screen (Priority: CRITICAL)

### 5.1 Chat Header
**Goal**: Header with user info and action buttons

**Features**:
- [ ] Contact/group avatar
- [ ] Contact/group name
- [ ] Online status / "Typing..." indicator
- [ ] Voice call button
- [ ] Video call button
- [ ] Three-dot menu (View Profile, Search in Chat, Mute, Block)

**Files to Create**:
- `presentation/chat/detail/components/ChatHeader.kt`
- `presentation/chat/detail/components/CallButton.kt`

### 5.2 Message List
**Goal**: WhatsApp-style message display

**Features**:
- [ ] Message bubbles (sent/received)
- [ ] Timestamp display
- [ ] Read receipts (✓ sent, ✓✓ delivered, ✓✓ read in blue)
- [ ] Message status (Sending, Sent, Delivered, Read, Failed)
- [ ] Reply preview in messages
- [ ] Forwarded message indicator
- [ ] Reactions (emoji)
- [ ] Long-press menu (Reply, Forward, Copy, Delete, Star)
- [ ] Swipe to reply gesture
- [ ] Date separators
- [ ] System messages (group events)

**Message Types to Support**:
- [ ] Text messages
- [ ] Image messages (with preview)
- [ ] Video messages (with thumbnail)
- [ ] Audio messages (with waveform)
- [ ] Document messages (with icon and size)
- [ ] Location messages
- [ ] Contact cards
- [ ] Polls
- [ ] Voice notes (recording UI)

**Files to Create**:
- `presentation/chat/detail/components/MessageBubble.kt`
- `presentation/chat/detail/components/MessageStatus.kt`
- `presentation/chat/detail/components/ReadReceipts.kt`
- `presentation/chat/detail/components/ReplyPreview.kt`
- `presentation/chat/detail/components/MessageActions.kt`
- `presentation/chat/detail/components/TextMessage.kt`
- `presentation/chat/detail/components/ImageMessage.kt`
- `presentation/chat/detail/components/VideoMessage.kt`
- `presentation/chat/detail/components/AudioMessage.kt`
- `presentation/chat/detail/components/DocumentMessage.kt`
- `presentation/chat/detail/components/LocationMessage.kt`
- `presentation/chat/detail/components/VoiceNoteRecorder.kt`

### 5.3 Message Input Bar
**Goal**: Complete message composition with media

**Features**:
- [ ] Text input field (multiline, auto-expand)
- [ ] Emoji picker button
- [ ] Attachment menu button
- [ ] Voice recording button (hold to record)
- [ ] Send button (appears when text entered)
- [ ] Reply banner (when replying to a message)
- [ ] Edit banner (when editing a message)
- [ ] Typing indicator (sent to other users)

**Attachment Options**:
- [ ] Camera (take photo)
- [ ] Gallery (select photos/videos)
- [ ] Documents (file picker)
- [ ] Location (map picker)
- [ ] Contact (contact picker)
- [ ] Poll (create poll)

**Files to Create**:
- `presentation/chat/detail/components/MessageInputBar.kt`
- `presentation/chat/detail/components/EmojiPicker.kt`
- `presentation/chat/detail/components/AttachmentMenu.kt`
- `presentation/chat/detail/components/VoiceRecordButton.kt`
- `presentation/chat/detail/components/ReplyBanner.kt`
- `presentation/chat/detail/components/CameraCapture.kt`
- `presentation/chat/detail/components/MediaPicker.kt`
- `presentation/chat/detail/components/DocumentPicker.kt`
- `presentation/chat/detail/components/LocationPicker.kt`

---

## 👤 Phase 6: Profile & Settings (Priority: MEDIUM)

### 6.1 User Profile Screen
**Goal**: View and edit user profile

**Features**:
- [ ] Profile photo (tap to view full screen)
- [ ] Name
- [ ] Phone number
- [ ] About/status
- [ ] QR code for adding
- [ ] Share profile link

**Files to Create**:
- `presentation/profile/ProfileScreen.kt`
- `presentation/profile/ProfileViewModel.kt`
- `presentation/profile/EditProfileScreen.kt`

### 6.2 Settings Screen
**Goal**: App settings and preferences

**Features**:
- [ ] Account settings
- [ ] Privacy settings (read receipts, online status)
- [ ] Notifications settings
- [ ] Chats settings (backup, wallpaper)
- [ ] Data and storage settings
- [ ] Help and support
- [ ] About Chain

**Files to Create**:
- `presentation/settings/SettingsScreen.kt`
- `presentation/settings/AccountSettingsScreen.kt`
- `presentation/settings/PrivacySettingsScreen.kt`
- `presentation/settings/NotificationSettingsScreen.kt`

---

## 📞 Phase 7: Calls UI (Priority: MEDIUM)

### 7.1 Call Screens
**Goal**: Voice and video call interface

**Features**:
- [ ] Incoming call screen (fullscreen)
- [ ] Outgoing call screen
- [ ] Active call screen (voice)
- [ ] Active video call screen
- [ ] Call controls (mute, speaker, end, switch camera)
- [ ] Picture-in-picture for video calls

**Files to Create**:
- `presentation/call/IncomingCallScreen.kt`
- `presentation/call/OutgoingCallScreen.kt`
- `presentation/call/VoiceCallScreen.kt`
- `presentation/call/VideoCallScreen.kt`
- `presentation/call/CallViewModel.kt`

---

## 📊 Phase 8: Additional Features (Priority: LOW)

### 8.1 Advanced Features
- [ ] Message search within chat
- [ ] Starred messages
- [ ] Media gallery view
- [ ] Group info screen
- [ ] Group admin controls
- [ ] Broadcast lists
- [ ] Status/Stories (WhatsApp-style)
- [ ] Archive chats
- [ ] Pin chats

---

## 🎨 Design Specifications

### Glassmorphism Style Guide
```kotlin
// Glass card effect
- Background: White/Black with 10-20% opacity
- Blur: 10-20dp backdrop blur
- Border: 1dp white/gray with 20% opacity
- Shadow: Soft elevation shadow
- Corner radius: 16dp

// Glass button
- Background: White with 15% opacity
- Blur: 12dp
- Border: 1dp white with 30% opacity
- Pressed state: 25% opacity
- Corner radius: 12dp

// Glass input field
- Background: White with 10% opacity
- Blur: 15dp
- Border: 1dp white with 20% opacity
- Focused border: Primary color
- Corner radius: 16dp
```

### Color Scheme
```kotlin
Primary: #007AFF (Blue)
Secondary: #5856D6 (Purple)
Success: #34C759 (Green)
Warning: #FF9500 (Orange)
Error: #FF3B30 (Red)

Background Gradient:
- Light: #F0F4FF → #E8EEFF
- Dark: #1C1C1E → #2C2C2E

Glass Overlay:
- Light: White @ 10-20%
- Dark: Black @ 30-40%
```

---

## 📱 Screen Flow

```
App Launch
    ↓
┌─ Check Auth State ─┐
│                     │
│ Authenticated?      │
└─────────┬───────────┘
          │
    ┌─────┴─────┐
    │           │
   Yes         No
    │           │
    ↓           ↓
Chat List   Welcome Screen
    │           ↓
    │       Phone Auth
    │           ↓
    │       OTP Verify
    │           ↓
    │       Profile Setup
    │           ↓
    └───────→ Chat List ←─────┐
                 ↓             │
           ┌─────┴─────┐      │
           │           │      │
      Select Chat  Add Contact/Group
           │           │
           ↓           │
      Chat Screen      │
           │           │
      ┌────┴────┐      │
      │         │      │
   Messages  Profile   │
      │         │      │
   Actions  Settings ──┘
      │
   Reply/Forward/Call
```

---

## 🚀 Implementation Order (Recommended)

1. **Phase 1**: Persistent Auth (Critical - prevents re-login)
2. **Phase 2**: Glassmorphism Components (Foundation for all UI)
3. **Phase 3**: Chat List Screen (Main entry point)
4. **Phase 5.1-5.2**: Chat Screen - Messages (Core functionality)
5. **Phase 5.3**: Message Input Bar (Essential for sending)
6. **Phase 4**: Add Contacts/Groups (User acquisition)
7. **Phase 6**: Profile & Settings (User management)
8. **Phase 7**: Calls UI (Advanced feature)
9. **Phase 8**: Additional Features (Polish)

---

## 📦 Dependencies to Add

```kotlin
// Image loading
implementation("io.coil-kt:coil-compose:2.5.0") // Already added

// Camera & Media
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")

// QR Code
implementation("com.google.mlkit:barcode-scanning:17.2.0")
implementation("com.google.zxing:core:3.5.2")

// Maps (for location sharing)
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.0.1")

// Audio recording
implementation("com.github.lincollincol:amplituda:2.2.2")

// Emoji support
implementation("androidx.emoji2:emoji2:1.4.0")
implementation("androidx.emoji2:emoji2-views:1.4.0")
```

---

## ✅ Success Criteria

Each phase is complete when:
- [ ] All UI components render correctly
- [ ] Glassmorphism effect is properly applied
- [ ] Functionality works as expected
- [ ] No crashes or ANRs
- [ ] Smooth animations (60fps)
- [ ] Accessible (TalkBack compatible)
- [ ] Works on different screen sizes
- [ ] Dark mode supported
- [ ] Memory efficient (no leaks)

---

## 📝 Testing Checklist

- [ ] Sign up new user → stays logged in after restart
- [ ] Search chats by name/message content
- [ ] Add contact by phone number
- [ ] Create group with multiple contacts
- [ ] Send text message → shows read receipts
- [ ] Send image → displays correctly
- [ ] Send video → plays inline
- [ ] Record voice note → sends and plays
- [ ] Reply to message → shows context
- [ ] Forward message → selects recipients
- [ ] Make voice call → connects via WebRTC
- [ ] Make video call → video streams correctly
- [ ] Edit profile → saves changes
- [ ] Change settings → persists preferences
- [ ] Delete message → removes for all
- [ ] Block contact → stops receiving messages

---

This plan provides a complete roadmap to build a WhatsApp-level messaging app with Chain's P2P backend!
