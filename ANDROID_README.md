# Netflix Skip Intro - Android Auto-Click App

A Kotlin Multiplatform app that uses Android Accessibility Services to automatically detect and click buttons in other apps, specifically designed to skip intro buttons on Netflix.

## Features

- ✅ **Automatic Button Detection**: Automatically detects and clicks "Skip Intro" buttons in Netflix
- ✅ **Manual Button Click**: Click any button by text in any app
- ✅ **Button Scanner**: Scan and detect all clickable buttons on the current screen
- ✅ **Modern Compose UI**: Beautiful Material 3 design
- ✅ **Real-time Service Status**: Live monitoring of accessibility service status

## How It Works

The app uses Android's **Accessibility Service** API to:
1. Monitor window changes and UI updates in other apps
2. Search for buttons with specific text (e.g., "Skip Intro", "Skip")
3. Automatically click those buttons when detected
4. Allow manual interaction with any detected button

## Setup Instructions

### 1. Build and Install the App

```bash
./gradlew :composeApp:assembleDebug
```

Or open in Android Studio and run the app.

### 2. Enable Accessibility Service

1. Open the **Netflix Skip Intro** app
2. Tap the **"Enable"** button on the Service Status card
3. You'll be taken to **Settings → Accessibility**
4. Find **"Netflix Skip Intro"** in the list
5. Toggle it **ON**
6. Accept the permissions dialog

### 3. Use the App

#### Automatic Mode (Netflix)
1. Open Netflix
2. Start playing any show
3. When the "Skip Intro" button appears, the app will automatically click it
4. No manual intervention needed!

#### Manual Mode (Any App)
1. Open the Netflix Skip Intro app
2. Keep it in the background
3. Open any other app
4. Return to Netflix Skip Intro
5. Enter the button text you want to click
6. Tap **"Click Button"**

#### Scanner Mode
1. Open any app where you want to find buttons
2. Switch back to Netflix Skip Intro app
3. Tap **"Scan"** in the Detected Buttons section
4. View all clickable buttons from the previous screen
5. Tap **"Click"** next to any button to activate it

## Permissions Explained

### BIND_ACCESSIBILITY_SERVICE
Required to create and bind the accessibility service. This is the core permission that allows the app to interact with other apps.

### QUERY_ALL_PACKAGES
Allows the app to see and interact with all installed packages. Required to detect buttons across different apps.

### FOREGROUND_SERVICE (Optional)
Allows the service to run reliably in the background for better performance.

## Architecture

### Android Components

```
composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/
├── AutoClickAccessibilityService.kt    # Core accessibility service
├── AccessibilityViewModel.kt           # State management
├── AccessibilityScreen.kt              # Compose UI
└── MainActivity.kt                     # Entry point
```

### Key Files

1. **AutoClickAccessibilityService.kt**
   - Extends `AccessibilityService`
   - Monitors window content changes
   - Implements button finding and clicking logic
   - Supports gesture dispatching for coordinate-based clicks

2. **AccessibilityViewModel.kt**
   - Manages service state
   - Provides interface to trigger clicks
   - Handles button detection results

3. **AccessibilityScreen.kt**
   - Modern Material 3 UI
   - Service status display
   - Manual click controls
   - Detected buttons list

4. **AndroidManifest.xml**
   - Declares accessibility service
   - Requests necessary permissions
   - Links to service configuration

5. **accessibility_service_config.xml**
   - Configures service capabilities
   - Sets event types to monitor
   - Defines target packages (Netflix)

## Configuration

### Target Different Apps

To target apps other than Netflix, modify the `packageNames` in `accessibility_service_config.xml`:

```xml
<!-- For all apps -->
android:packageNames=""

<!-- For specific apps -->
android:packageNames="com.netflix.mediaclient,com.hulu.plus"
```

### Change Auto-Click Buttons

Modify the button text detection in `AutoClickAccessibilityService.kt`:

```kotlin
// Look for different button texts
findAndClickButton("Skip Intro")
findAndClickButton("Skip Ad")
findAndClickButton("Continue")
```

## Privacy & Security

⚠️ **Important**: This app has powerful permissions that allow it to see and interact with content in other apps.

- ✅ **No data collection**: The app does not collect or transmit any data
- ✅ **Open source**: All code is visible and auditable
- ✅ **Local only**: All processing happens on your device
- ✅ **No network access**: The app does not require internet permissions

## Limitations

1. **Android 7.0+**: Accessibility gesture API requires Android 7.0 (API 24) or higher
2. **Service must be enabled**: The accessibility service must be manually enabled in Settings
3. **Battery usage**: Running an accessibility service may impact battery life
4. **App-specific**: Some apps may block or detect accessibility services

## Troubleshooting

### Service Not Working

1. **Check if service is enabled**: Open Settings → Accessibility → Netflix Skip Intro
2. **Restart the app**: Force stop and reopen the app
3. **Restart the device**: Sometimes a reboot is needed after enabling the service

### Buttons Not Being Clicked

1. **Check button text**: The button text must match exactly (case-insensitive)
2. **Use Scanner**: Use the Scanner feature to see what buttons are actually detected
3. **Check package name**: Ensure the target app's package name is in the configuration

### Permission Denied

1. **QUERY_ALL_PACKAGES**: On Android 11+, this requires a declaration in the manifest
2. **Accessibility**: Must be enabled manually by the user in Settings

## Building from Source

### Prerequisites

- Android Studio Hedgehog or later
- JDK 11 or later
- Kotlin 1.9+
- Gradle 8.0+

### Build Commands

```bash
# Debug build
./gradlew :composeApp:assembleDebug

# Release build
./gradlew :composeApp:assembleRelease

# Install on device
./gradlew :composeApp:installDebug
```

## Future Enhancements

- [ ] iOS implementation using Voice Control automation
- [ ] Customizable button patterns
- [ ] Delay configuration for auto-clicks
- [ ] History of clicked buttons
- [ ] Per-app settings
- [ ] Floating overlay for quick access

## License

This project is for educational purposes. Use responsibly and in accordance with the terms of service of the apps you interact with.

## Disclaimer

This app is not affiliated with, endorsed by, or connected to Netflix or any other streaming service. It is an independent tool created for convenience and accessibility purposes.

