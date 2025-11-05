### Glassmorphism Components Usage Guide

This guide shows how to use the new glassmorphism components in Chain.

---

## 🎨 Available Components

### 1. **GlassCard**
Glass-styled card for containers and content groups.

```kotlin
@Composable
fun Example() {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* Optional click handler */ }
    ) {
        Text("Card Content")
        Text("More content...")
    }
}
```

### 2. **GlassButton**
Glass-styled button for actions.

```kotlin
@Composable
fun Example() {
    GlassButton(
        onClick = { /* Handle click */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.Default.Send, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Send Message")
    }
}
```

### 3. **GlassTextField**
Glass-styled text input field.

```kotlin
@Composable
fun Example() {
    var text by remember { mutableStateOf("") }

    GlassTextField(
        value = text,
        onValueChange = { text = it },
        label = "Username",
        placeholder = "Enter your username",
        leadingIcon = {
            Icon(Icons.Default.Person, contentDescription = null)
        },
        supportingText = "This will be your display name"
    )
}
```

### 4. **GlassIconButton**
Glass-styled icon button.

```kotlin
@Composable
fun Example() {
    GlassIconButton(
        onClick = { /* Handle click */ }
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search")
    }
}
```

### 5. **GlassFAB**
Glass-styled floating action button.

```kotlin
@Composable
fun Example() {
    GlassFAB(
        onClick = { /* Handle click */ }
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}
```

---

## 🖼️ Complete Screen Example

Here's a complete example of a chat list screen using glassmorphism:

```kotlin
@Composable
fun ChatListScreen() {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // Glass top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassTopBar(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chain",
                    style = MaterialTheme.typography.headlineMedium
                )

                Row {
                    GlassIconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }

                    Spacer(Modifier.width(8.dp))

                    GlassIconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            }
        },
        floatingActionButton = {
            GlassFAB(
                onClick = { /* Add contact/group */ }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search chats...",
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Chat list
            LazyColumn {
                items(chats) { chat ->
                    ChatListItem(chat = chat)
                }
            }
        }
    }
}

@Composable
fun ChatListItem(chat: Chat) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { /* Navigate to chat */ }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .glassCard(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.first().toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(Modifier.width(12.dp))

            // Chat info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Timestamp and unread badge
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = chat.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                if (chat.unreadCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .glassButton(CircleShape)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
```

---

## 🎯 Applying Glassmorphism Effects Manually

You can also apply glass effects directly to any composable:

```kotlin
@Composable
fun CustomComponent() {
    Box(
        modifier = Modifier
            .size(200.dp)
            .glassCard(RoundedCornerShape(20.dp)) // Pre-configured glass card effect
            // OR use custom glass effect:
            .glass(
                shape = RoundedCornerShape(20.dp),
                blurRadius = 16.dp,
                alpha = 0.15f,
                borderAlpha = 0.3f,
                elevation = 6.dp
            )
    ) {
        // Content
    }
}
```

---

## 🎨 Pre-configured Glass Effects

- `.glassCard(shape)` - For cards and containers
- `.glassButton(shape)` - For buttons and interactive elements
- `.glassTextField(shape)` - For input fields
- `.glassDialog(shape)` - For dialogs and modals
- `.glassTopBar(shape)` - For app bars
- `.glassBottomBar(shape)` - For bottom navigation

---

## 💡 Tips

1. **Layer Order**: Apply glass effects before padding/content modifiers
2. **Dark Mode**: Glass effects automatically adjust for dark theme
3. **Performance**: Blur effects can be GPU-intensive, use sparingly
4. **Consistency**: Use the same corner radius throughout your app (16.dp recommended)
5. **Contrast**: Ensure sufficient contrast between glass elements and content

---

## 🚀 Next Steps

Now you can:
1. Replace existing UI components with glass versions
2. Create custom glass components following the same pattern
3. Adjust glass parameters (alpha, blur, etc.) to match your design
4. Implement the complete chat list and chat screens using these components

See `UI_IMPLEMENTATION_PLAN.md` for the complete roadmap!
