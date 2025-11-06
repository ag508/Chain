# Sprint 6 & 7 Testing Guide
## Chain Messaging App - Media Sharing & Advanced Features

**Test Date**: 2025-11-06
**Sprints Covered**: Sprint 6 (Media Sharing) & Sprint 7 (Advanced Features)
**Platform**: Android (API 26+)

---

## 📋 Table of Contents
1. [Sprint 6: Media Sharing Tests](#sprint-6-media-sharing-tests)
2. [Sprint 7: Advanced Features Tests](#sprint-7-advanced-features-tests)
3. [Integration Tests](#integration-tests)
4. [Performance Tests](#performance-tests)
5. [Edge Cases & Error Handling](#edge-cases--error-handling)
6. [Test Results Template](#test-results-template)

---

## 🎬 Sprint 6: Media Sharing Tests

### 6.1 Photo Selection & Sending

#### Test Case 6.1.1: Open Gallery and Select Photo
**Prerequisites**: Device has photos in gallery
**Steps**:
1. Open a chat conversation
2. Tap the attachment button (paperclip icon)
3. Tap "Gallery" option
4. Select a single photo from the picker
5. Observe the chat screen

**Expected Results**:
- ✅ Attachment menu opens with opaque background (70% black)
- ✅ Android Photo Picker opens
- ✅ Selected photo appears in the message input or sends immediately
- ✅ Photo message displays in chat with proper image rendering
- ✅ Image loads using Coil with smooth loading animation

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.1.2: Select Multiple Photos
**Prerequisites**: Device has multiple photos in gallery
**Steps**:
1. Open attachment menu
2. Tap "Gallery" option
3. Select multiple photos (up to 10)
4. Confirm selection

**Expected Results**:
- ✅ Photo picker allows multiple selection
- ✅ All selected photos appear as separate messages or in a batch
- ✅ Each photo displays correctly
- ✅ Photos maintain original quality/aspect ratio

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.1.3: Photo Size Validation
**Prerequisites**: Device has photos of various sizes
**Steps**:
1. Try to send a photo larger than 10MB
2. Observe any error messages or warnings

**Expected Results**:
- ✅ If photo > 10MB, user sees error message
- ✅ Error message is clear: "Photo size exceeds 10MB limit"
- ✅ User can select a different photo
- ✅ Photos under 10MB send successfully

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.2 Camera Capture

#### Test Case 6.2.1: Take Photo with Camera
**Prerequisites**: Device has camera permission granted
**Steps**:
1. Open attachment menu
2. Tap "Camera" option
3. Take a photo using the camera
4. Confirm the photo
5. Observe the chat

**Expected Results**:
- ✅ Camera app opens
- ✅ Photo is captured and saved temporarily
- ✅ FileProvider securely shares the photo URI
- ✅ Photo appears in chat after confirmation
- ✅ Photo is stored in cache directory (images/)

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.2.2: Camera Permission Denied
**Prerequisites**: Camera permission not granted
**Steps**:
1. Open attachment menu
2. Tap "Camera" option
3. Deny camera permission when prompted

**Expected Results**:
- ✅ Permission dialog appears
- ✅ If denied, user sees helpful error message
- ✅ App suggests going to settings to enable permission
- ✅ App doesn't crash

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.3 Video Selection & Display

#### Test Case 6.3.1: Select and Send Video
**Prerequisites**: Device has videos in gallery
**Steps**:
1. Open attachment menu
2. Tap "Gallery" option
3. Select a video file
4. Send the video
5. Observe the video message display

**Expected Results**:
- ✅ Video picker opens
- ✅ Video thumbnail displays in message bubble
- ✅ Play button overlay appears on thumbnail
- ✅ Video size validated (< 100MB)
- ✅ Video message sent successfully

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.3.2: Video Size Validation
**Prerequisites**: Device has large video files
**Steps**:
1. Try to send a video larger than 100MB
2. Observe error handling

**Expected Results**:
- ✅ Error message displays: "Video size exceeds 100MB limit"
- ✅ User can select a different video
- ✅ No crash or freeze

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.4 Document Sharing

#### Test Case 6.4.1: Select and Send Document
**Prerequisites**: Device has documents (PDF, etc.)
**Steps**:
1. Open attachment menu
2. Tap "Document" option
3. Select a PDF document
4. Send the document
5. Observe the document message display

**Expected Results**:
- ✅ Document picker opens
- ✅ User can browse documents
- ✅ Selected document displays with file icon
- ✅ Document name and size shown correctly
- ✅ Download icon appears on document message

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.4.2: Document Size Validation
**Prerequisites**: Device has large documents
**Steps**:
1. Try to send a document larger than 50MB
2. Observe error handling

**Expected Results**:
- ✅ Error message displays: "Document size exceeds 50MB limit"
- ✅ User can select a different document
- ✅ Validation happens before upload

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.5 Media Message Display

#### Test Case 6.5.1: Image Message Rendering
**Prerequisites**: Chat has image messages
**Steps**:
1. Open a chat with image messages
2. Scroll through messages
3. Tap on an image message

**Expected Results**:
- ✅ Images load efficiently with Coil
- ✅ Images maintain aspect ratio
- ✅ Max height of 300dp enforced
- ✅ Images rounded with 12dp corners
- ✅ Loading state shows before image loads
- ✅ Tapping image opens full-screen view (if implemented)

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.5.2: Video Message Rendering
**Prerequisites**: Chat has video messages
**Steps**:
1. View video messages in chat
2. Observe video thumbnail and play button

**Expected Results**:
- ✅ Video thumbnail displays correctly
- ✅ Play button centered and visible (64dp, white)
- ✅ Black transparent background (10% alpha) behind thumbnail
- ✅ Max height of 300dp enforced

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.5.3: Audio Message Display
**Prerequisites**: Chat has audio messages
**Steps**:
1. View audio messages in chat
2. Observe audio player UI

**Expected Results**:
- ✅ Play/pause button visible (32dp)
- ✅ Waveform placeholder displays
- ✅ Duration formatted correctly (MM:SS)
- ✅ GlassAccent color used for icons

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.5.4: Document Message Display
**Prerequisites**: Chat has document messages
**Steps**:
1. View document messages in chat
2. Check document info display

**Expected Results**:
- ✅ File icon displays (40dp)
- ✅ File name truncates if too long
- ✅ File size formatted correctly (B, KB, MB)
- ✅ Download icon visible (24dp)
- ✅ Background uses GlassAccent with 10% alpha

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.6 Media Captions

#### Test Case 6.6.1: Add Caption to Image
**Prerequisites**: Sending an image message
**Steps**:
1. Select an image to send
2. Add a caption text
3. Send the message
4. View the sent message

**Expected Results**:
- ✅ Caption input field available (if implemented)
- ✅ Caption displays below image
- ✅ Caption uses proper text styling
- ✅ Caption stored in message metadata

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 6.7 Attachment Menu UI

#### Test Case 6.7.1: Attachment Menu Appearance
**Prerequisites**: In a chat conversation
**Steps**:
1. Tap attachment button
2. Observe the attachment menu

**Expected Results**:
- ✅ Menu slides up from bottom
- ✅ Background overlay is opaque (70% black alpha)
- ✅ Menu uses glassDialog() effect for more opacity
- ✅ All 6 options visible: Camera, Gallery, Document, Location, Contact, Poll
- ✅ Each option has colored icon and label
- ✅ Close button (X) at top-right
- ✅ Tapping overlay dismisses menu

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 6.7.2: Attachment Menu Interactions
**Prerequisites**: Attachment menu is open
**Steps**:
1. Tap each option and observe behavior
2. Try dismissing with overlay tap
3. Try dismissing with close button

**Expected Results**:
- ✅ Camera: Opens camera
- ✅ Gallery: Opens photo/video picker
- ✅ Document: Opens file picker
- ✅ Location/Contact/Poll: Shows TODO (not yet implemented)
- ✅ Overlay tap dismisses menu
- ✅ Close button dismisses menu

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

## 🚀 Sprint 7: Advanced Features Tests

### 7.1 Message Reactions

#### Test Case 7.1.1: Add Quick Reaction
**Prerequisites**: Chat has messages
**Steps**:
1. Double-tap a message
2. Observe quick reaction picker
3. Tap a reaction emoji (❤️, 👍, 😂, etc.)
4. Observe the message

**Expected Results**:
- ✅ Quick reaction picker appears (6 emojis + more button)
- ✅ Picker has glass effect with proper styling
- ✅ Reaction emoji added to message
- ✅ Reaction displays below message bubble
- ✅ Reaction count shows if multiple users react

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.1.2: Full Emoji Picker
**Prerequisites**: In a chat conversation
**Steps**:
1. Double-tap a message
2. Tap "More" (➕) button on quick reactions
3. Browse emoji categories
4. Select an emoji
5. Observe the message

**Expected Results**:
- ✅ Full emoji picker opens
- ✅ 8 categories available: Frequent, Smileys, Gestures, Hearts, Animals, Food, Activities, Objects
- ✅ Tab row for category selection
- ✅ Emoji grid displays 8 per row
- ✅ Scrollable emoji list (300dp height)
- ✅ Selected emoji added to message
- ✅ Picker dismisses after selection

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.1.3: View Reactions on Message
**Prerequisites**: Message has reactions from multiple users
**Steps**:
1. View a message with reactions
2. Observe reaction display

**Expected Results**:
- ✅ Reactions grouped by emoji type
- ✅ Count displays next to emoji (if > 1)
- ✅ Reactions in rounded container below message
- ✅ Background: surface color with 50% alpha
- ✅ Padding: 8dp horizontal, 4dp vertical

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 7.2 Message Actions

#### Test Case 7.2.1: Open Message Actions Menu
**Prerequisites**: Chat has messages
**Steps**:
1. Long-press a message
2. Observe the actions sheet

**Expected Results**:
- ✅ Message actions sheet slides up from bottom
- ✅ Sheet uses glassDialog() effect
- ✅ "Message Actions" title displays
- ✅ Quick reactions row shows 6 emojis
- ✅ Actions listed: Reply, Forward, Copy, Star, Delete, Info
- ✅ Cancel button at bottom
- ✅ Sheet dismisses when tapping outside or Cancel

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.2: Reply to Message
**Prerequisites**: In a chat conversation
**Steps**:
1. Long-press a message
2. Tap "Reply" action
3. Observe the message input bar
4. Type a reply and send

**Expected Results**:
- ✅ Actions sheet closes
- ✅ Reply banner appears above input bar
- ✅ Banner shows original message preview
- ✅ "Replying to [sender]" text displays
- ✅ Cancel (X) button on reply banner
- ✅ Sent message shows reply indicator
- ✅ Reply connects to original message

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.3: Copy Message Text
**Prerequisites**: Chat has text messages
**Steps**:
1. Long-press a text message
2. Tap "Copy" action
3. Paste in another app

**Expected Results**:
- ✅ Message text copied to clipboard
- ✅ Toast notification: "Message copied"
- ✅ Text pastes correctly in other apps

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.4: Delete Message (My Message)
**Prerequisites**: Chat has messages sent by current user
**Steps**:
1. Long-press your own message
2. Tap "Delete" action
3. Observe delete confirmation dialog
4. Tap "Delete" in dialog

**Expected Results**:
- ✅ Actions sheet closes
- ✅ Delete dialog appears
- ✅ Dialog text: "This message will be deleted for everyone in the chat."
- ✅ Red "Delete" button
- ✅ "Cancel" button
- ✅ Message removed from chat
- ✅ Other users see deletion (when implemented)

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.5: Delete Message (Other's Message)
**Prerequisites**: Chat has messages from other users
**Steps**:
1. Long-press another user's message
2. Tap "Delete" action
3. Observe delete confirmation dialog
4. Confirm deletion

**Expected Results**:
- ✅ Delete dialog appears
- ✅ Dialog text: "This message will be deleted for you only."
- ✅ Message removed from local view only
- ✅ Message still visible to others

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.6: Star/Favorite Message
**Prerequisites**: Chat has messages
**Steps**:
1. Long-press a message
2. Tap "Star" action
3. Observe message changes

**Expected Results**:
- ✅ Message marked as starred
- ✅ Star indicator appears on message (if implemented)
- ✅ Message accessible from starred messages list

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.2.7: View Message Info
**Prerequisites**: Chat has messages
**Steps**:
1. Long-press a message
2. Tap "Info" action
3. Observe message info screen

**Expected Results**:
- ✅ Message info screen appears
- ✅ Shows: Send time, delivery time, read time
- ✅ Shows delivery/read receipts
- ✅ Shows message ID and encryption status

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 7.3 Message Search

#### Test Case 7.3.1: Open Search in Chat
**Prerequisites**: In a chat conversation
**Steps**:
1. Tap three-dot menu in chat header
2. Select "Search in Chat" option
3. Observe the search bar

**Expected Results**:
- ✅ Menu closes
- ✅ Search bar appears at top of chat
- ✅ Search input field has focus
- ✅ Keyboard opens automatically
- ✅ Placeholder: "Search in conversation..."
- ✅ Close button (X) visible
- ✅ Search icon in input field

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.3.2: Search for Messages
**Prerequisites**: Chat has multiple messages
**Steps**:
1. Open search in chat
2. Type a search query
3. Observe search results

**Expected Results**:
- ✅ Results appear as you type
- ✅ Query text highlighted in results (bold, yellow background)
- ✅ Result count displays: "X of Y"
- ✅ Results show sender name, message preview, timestamp
- ✅ Up/down navigation buttons appear
- ✅ Current result highlighted

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.3.3: Navigate Search Results
**Prerequisites**: Search has multiple results
**Steps**:
1. Search for a common word
2. Tap down arrow to go to next result
3. Tap up arrow to go to previous result
4. Tap a search result item

**Expected Results**:
- ✅ Down arrow navigates to next result
- ✅ Up arrow navigates to previous result
- ✅ Navigation disabled at first/last result
- ✅ Tapping result scrolls to message in chat
- ✅ Message briefly highlights
- ✅ Search bar remains open

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.3.4: No Search Results
**Prerequisites**: In search mode
**Steps**:
1. Search for text that doesn't exist
2. Observe the UI

**Expected Results**:
- ✅ "No messages found" text displays
- ✅ Text uses GlassTextSecondary color
- ✅ No error or crash

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.3.5: Close Search
**Prerequisites**: Search bar is open
**Steps**:
1. Tap close button (X)
2. Observe the UI

**Expected Results**:
- ✅ Search bar closes
- ✅ Normal chat header returns
- ✅ Search query cleared
- ✅ Chat scrolls back to bottom
- ✅ Keyboard dismisses

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

### 7.4 Disappearing Messages

#### Test Case 7.4.1: Open Disappearing Messages Settings
**Prerequisites**: In a chat conversation
**Steps**:
1. Tap three-dot menu in chat header
2. Select "Disappearing Messages" option
3. Observe the dialog

**Expected Results**:
- ✅ Menu closes
- ✅ Disappearing messages dialog opens
- ✅ Title: "Disappearing Messages" with timer icon
- ✅ Description text explaining feature
- ✅ "Off" option listed first
- ✅ 9 timer options: 5s, 10s, 30s, 1m, 5m, 30m, 1h, 24h, 7d
- ✅ Cancel button at bottom

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.4.2: Enable Disappearing Messages
**Prerequisites**: Disappearing messages dialog open
**Steps**:
1. Select a timer option (e.g., "1 minute")
2. Observe the UI changes
3. Send a message
4. Observe the message

**Expected Results**:
- ✅ Dialog closes
- ✅ Timer indicator appears in chat header or input area
- ✅ Disappearing message badge shows timer duration
- ✅ Badge displays: Timer icon + "1 minute"
- ✅ Badge has GlassAccent color with 15% alpha background
- ✅ All new messages have disappearing timer
- ✅ Timer countdown begins after message is read

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.4.3: Disable Disappearing Messages
**Prerequisites**: Disappearing messages enabled
**Steps**:
1. Open disappearing messages dialog
2. Select "Off" option
3. Observe the changes

**Expected Results**:
- ✅ Dialog closes
- ✅ Disappearing message badge removed
- ✅ New messages don't have timer
- ✅ Existing disappearing messages still honor their timer
- ✅ Setting applies to chat immediately

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

#### Test Case 7.4.4: Change Timer Duration
**Prerequisites**: Disappearing messages enabled with one duration
**Steps**:
1. Open disappearing messages dialog
2. Select a different timer duration
3. Send a message

**Expected Results**:
- ✅ New timer duration applied
- ✅ Badge updates with new duration
- ✅ New messages use new timer
- ✅ Old messages keep original timer

**Test Status**: ⬜ Pass | ⬜ Fail | ⬜ Not Tested

---

## 🔗 Integration Tests

### INT-1: End-to-End Media Message Flow
**Test**: Send photo → Receive → Reply with video → Search conversation
**Expected**: All features work seamlessly together

### INT-2: Media + Reactions + Actions
**Test**: Send image → Add reaction → Long-press for actions → Delete
**Expected**: All actions complete successfully

### INT-3: Search + Reply + Disappearing Messages
**Test**: Enable disappearing messages → Send messages → Search → Reply
**Expected**: Search finds messages, reply works, timer activates

### INT-4: Multiple Media Types in Conversation
**Test**: Send photo, video, document, audio in sequence
**Expected**: All display correctly with proper formatting

---

## ⚡ Performance Tests

### PERF-1: Image Loading Performance
- Load chat with 50+ images
- Scroll through quickly
- **Expected**: Smooth 60fps scrolling, images load progressively

### PERF-2: Search Performance
- Search in chat with 1000+ messages
- **Expected**: Results appear within 500ms

### PERF-3: Large File Handling
- Send 10MB image
- **Expected**: No UI freeze, progress indicator shown

### PERF-4: Memory Usage
- Send 20 media messages
- **Expected**: Memory usage stays under 200MB

---

## 🐛 Edge Cases & Error Handling

### EDGE-1: No Internet Connection
**Test**: Try sending media while offline
**Expected**: Error message, queued for sending when online

### EDGE-2: Low Storage Space
**Test**: Try capturing photo with < 50MB free space
**Expected**: Error message about insufficient storage

### EDGE-3: Corrupted Media File
**Test**: Receive corrupted image/video
**Expected**: Error placeholder, doesn't crash app

### EDGE-4: Rapid Actions
**Test**: Rapidly tap attachment menu, send messages, add reactions
**Expected**: No crashes, all actions queued properly

### EDGE-5: Empty Search Query
**Test**: Search with empty text
**Expected**: No results shown, no error

### EDGE-6: Special Characters in Search
**Test**: Search with emojis, special characters
**Expected**: Search works correctly, proper escaping

### EDGE-7: Very Long Messages
**Test**: Search in chat with 1000+ character messages
**Expected**: Search works, highlights visible

---

## 📊 Test Results Template

### Test Summary
- **Total Tests**: ___
- **Passed**: ___
- **Failed**: ___
- **Not Tested**: ___
- **Pass Rate**: ___%

### Critical Bugs Found
1.
2.
3.

### Medium Priority Bugs
1.
2.
3.

### Minor Issues
1.
2.
3.

### Performance Observations
-
-
-

### Recommendations
1.
2.
3.

---

## ✅ Sign-Off

**Tested By**: ___________________
**Date**: ___________________
**Build Version**: ___________________
**Device/Emulator**: ___________________
**Android Version**: ___________________

**Sprint 6 Status**: ⬜ Ready for Production | ⬜ Needs Fixes
**Sprint 7 Status**: ⬜ Ready for Production | ⬜ Needs Fixes

---

**Notes**:
- Test on multiple devices (different screen sizes, Android versions)
- Test with different user roles (sender, receiver)
- Test with poor network conditions
- Test with large data sets
- Document any unexpected behavior with screenshots/logs
