# Android App Structure

## Files Created/Modified

### 1. Kotlin Source Files (androidMain)

```
composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/
│
├── AutoClickAccessibilityService.kt        ✅ NEW
│   ├── Core accessibility service
│   ├── Monitors window changes
│   ├── Finds and clicks buttons
│   └── Supports gesture dispatching
│
├── AccessibilityViewModel.kt               ✅ NEW
│   ├── Manages service state
│   ├── Provides click functions
│   └── Handles button detection
│
├── AccessibilityScreen.kt                  ✅ NEW
│   ├── Material 3 Compose UI
│   ├── Service status card
│   ├── Manual click controls
│   └── Button scanner
│
└── MainActivity.kt                         ✅ MODIFIED
    └── Uses AccessibilityScreen instead of default App
```

### 2. Android Resources

```
composeApp/src/androidMain/res/
│
├── xml/
│   └── accessibility_service_config.xml    ✅ NEW
│       ├── Service configuration
│       ├── Event types
│       ├── Capabilities
│       └── Target packages
│
└── values/
    └── strings.xml                         ✅ MODIFIED
        └── Added service description
```

### 3. Manifest & Build Files

```
composeApp/src/androidMain/
│
├── AndroidManifest.xml                     ✅ MODIFIED
│   ├── Added BIND_ACCESSIBILITY_SERVICE
│   ├── Added QUERY_ALL_PACKAGES
│   ├── Added FOREGROUND_SERVICE
│   └── Declared AutoClickAccessibilityService
│
└── build.gradle.kts                        ✅ MODIFIED
    └── Added kotlinx-coroutines dependencies
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         MainActivity                         │
│                    (Entry Point)                            │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ setContent
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     AccessibilityScreen                      │
│                   (Compose UI - Material 3)                 │
│                                                              │
│  ┌────────────────────────────────────────────────────┐   │
│  │         Service Status Card                         │   │
│  │  • Shows if service is enabled                      │   │
│  │  • "Enable" button if not active                    │   │
│  └────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌────────────────────────────────────────────────────┐   │
│  │         Instructions Card                           │   │
│  │  • How to enable service                            │   │
│  │  • Usage instructions                               │   │
│  └────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌────────────────────────────────────────────────────┐   │
│  │         Manual Button Click                         │   │
│  │  • Text input for button text                       │   │
│  │  • "Click Button" action                            │   │
│  └────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌────────────────────────────────────────────────────┐   │
│  │         Detected Buttons Scanner                    │   │
│  │  • "Scan" button                                    │   │
│  │  • List of detected buttons                         │   │
│  │  • Click individual buttons                         │   │
│  └────────────────────────────────────────────────────┘   │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ uses
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   AccessibilityViewModel                     │
│                    (State Management)                        │
│                                                              │
│  • isServiceEnabled: StateFlow<Boolean>                     │
│  • detectedButtons: StateFlow<List<ButtonInfo>>            │
│  • checkServiceStatus()                                      │
│  • openAccessibilitySettings()                              │
│  • clickButton(text: String)                                │
│  • clickAtPosition(x: Float, y: Float)                      │
│  • refreshDetectedButtons()                                  │
└────────────────────────────┬────────────────────────────────┘
                             │
                             │ communicates with
                             ▼
┌─────────────────────────────────────────────────────────────┐
│              AutoClickAccessibilityService                   │
│                (Background Service)                          │
│                                                              │
│  Lifecycle:                                                  │
│  • onServiceConnected()  → Sets instance                    │
│  • onAccessibilityEvent() → Monitors window changes         │
│  • onInterrupt()                                             │
│  • onDestroy()           → Clears instance                  │
│                                                              │
│  Core Functions:                                             │
│  • findAndClickButton(text)                                 │
│  • findAndClickNodeByText(node, text)                       │
│  • clickAtCoordinates(x, y)                                 │
│  • getAllClickableButtons()                                  │
│  • findAllClickableNodes(node, list)                        │
│                                                              │
│  Auto-detection:                                             │
│  • Listens for TYPE_WINDOW_CONTENT_CHANGED                  │
│  • Searches for "Skip Intro", "Skip" text                   │
│  • Automatically clicks when found                          │
└─────────────────────────────────────────────────────────────┘

                             │
                             │ monitors
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     Other Apps (Netflix)                     │
│                                                              │
│  The service can:                                            │
│  • Read window content                                       │
│  • Find clickable elements                                   │
│  • Perform click actions                                     │
│  • Dispatch gestures                                         │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. Service Enabling Flow

```
User taps "Enable" button
         ↓
AccessibilityScreen → openAccessibilitySettings()
         ↓
AccessibilityViewModel → openAccessibilitySettings(context)
         ↓
Opens Android Settings → Accessibility
         ↓
User enables "Netflix Skip Intro" service
         ↓
AutoClickAccessibilityService → onServiceConnected()
         ↓
instance = this
         ↓
User returns to app
         ↓
AccessibilityScreen → checkServiceStatus()
         ↓
Service status updates → UI shows "Service is active"
```

### 2. Auto-Click Flow (Automatic)

```
Netflix plays video with "Skip Intro" button
         ↓
AutoClickAccessibilityService receives TYPE_WINDOW_CONTENT_CHANGED
         ↓
onAccessibilityEvent(event) called
         ↓
findAndClickButton("Skip Intro")
         ↓
rootInActiveWindow → AccessibilityNodeInfo
         ↓
findAndClickNodeByText(rootNode, "Skip Intro")
         ↓
Recursively search for node with matching text
         ↓
Found clickable node with text "Skip Intro"
         ↓
node.performAction(ACTION_CLICK)
         ↓
Button clicked! Intro skipped! 🎉
```

### 3. Manual Click Flow

```
User enters "Skip" in text field
         ↓
User taps "Click Button"
         ↓
AccessibilityScreen → clickButton("Skip")
         ↓
AccessibilityViewModel → clickButton("Skip")
         ↓
AutoClickAccessibilityService.getInstance()
         ↓
service.findAndClickButton("Skip")
         ↓
Searches current screen for button
         ↓
If found → clicks it
         ↓
Returns true/false result
```

### 4. Scanner Flow

```
User opens another app (e.g., Netflix)
         ↓
User switches back to Netflix Skip Intro app
         ↓
User taps "Scan" button
         ↓
AccessibilityScreen → refreshDetectedButtons()
         ↓
AccessibilityViewModel → refreshDetectedButtons()
         ↓
AutoClickAccessibilityService → getAllClickableButtons()
         ↓
Traverses rootInActiveWindow node tree
         ↓
Collects all clickable nodes with text/description
         ↓
Returns List<ButtonInfo>
         ↓
Updates detectedButtons StateFlow
         ↓
UI shows list of buttons
         ↓
User can tap "Click" on any button
```

## Key Components Explained

### AccessibilityNodeInfo
- Represents a node in the UI hierarchy
- Contains information about views (text, clickable, bounds, etc.)
- Allows performing actions (click, focus, etc.)

### GestureDescription
- Used for coordinate-based clicking
- Creates touch gestures programmatically
- Requires `canPerformGestures="true"` in config

### StateFlow
- Kotlin coroutines reactive state holder
- Emits updates to all collectors
- Used for real-time UI updates

### Material 3 Components Used
- Scaffold (app structure)
- TopAppBar (header)
- Card (content containers)
- Button/FilledTonalButton (actions)
- Icon (visual indicators)
- LazyColumn (scrollable list)

## Permission Flow

```
App Installation
         ↓
Manifest declares permissions
         ↓
BIND_ACCESSIBILITY_SERVICE → Required for service binding
         ↓
QUERY_ALL_PACKAGES → See all installed apps
         ↓
FOREGROUND_SERVICE → Reliable background operation
         ↓
User must manually enable in Settings
         ↓
Android grants accessibility capabilities
         ↓
Service can now interact with other apps
```

## Testing the App

1. **Build**: `./gradlew :composeApp:assembleDebug`
2. **Install**: Install APK on Android device (API 24+)
3. **Enable Service**: Settings → Accessibility → Enable "Netflix Skip Intro"
4. **Test Auto-mode**: Open Netflix, play show with intro
5. **Test Manual-mode**: Use app to click specific buttons
6. **Test Scanner**: Scan buttons on any screen

