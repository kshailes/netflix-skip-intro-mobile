# Customization Guide

## Common Customizations

### 1. Change Target Apps

**File**: `composeApp/src/androidMain/res/xml/accessibility_service_config.xml`

```xml
<!-- Target ALL apps (leave empty) -->
<accessibility-service
    ...
    android:packageNames="" />

<!-- Target specific apps (comma-separated) -->
<accessibility-service
    ...
    android:packageNames="com.netflix.mediaclient,com.hulu.plus,com.amazon.avod.thirdpartyclient" />

<!-- Popular streaming app package names: -->
<!-- Netflix: com.netflix.mediaclient -->
<!-- Hulu: com.hulu.plus -->
<!-- Prime Video: com.amazon.avod.thirdpartyclient -->
<!-- Disney+: com.disney.disneyplus -->
<!-- HBO Max: com.hbo.hbonow -->
<!-- YouTube: com.google.android.youtube -->
```

### 2. Add More Auto-Click Button Texts

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    event?.let {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            // Add more button texts here
            findAndClickButton("Skip Intro")
            findAndClickButton("Skip")
            findAndClickButton("Skip Ad")           // NEW
            findAndClickButton("Skip Ads")          // NEW
            findAndClickButton("Continue Watching") // NEW
            findAndClickButton("Next Episode")      // NEW
            findAndClickButton("Play")              // NEW
        }
    }
}
```

### 3. Add Click Delay

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
import kotlinx.coroutines.*

class AutoClickAccessibilityService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                // Click after 2 second delay
                serviceScope.launch {
                    delay(2000) // 2 seconds
                    findAndClickButton("Skip Intro")
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        instance = null
    }
}
```

### 4. Case-Sensitive Button Matching

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
    // Change from ignoreCase = true to ignoreCase = false
    if (node.isClickable && node.text?.toString()?.contains(text, ignoreCase = false) == true) {
        Log.d(TAG, "Found clickable node with text: ${node.text}")
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    if (node.isClickable && node.contentDescription?.toString()?.contains(text, ignoreCase = false) == true) {
        Log.d(TAG, "Found clickable node with description: ${node.contentDescription}")
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    // Recursively search children...
}
```

### 5. Exact Text Matching (Not Contains)

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
    // Change from contains() to equals()
    if (node.isClickable && node.text?.toString()?.equals(text, ignoreCase = true) == true) {
        Log.d(TAG, "Found clickable node with text: ${node.text}")
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    if (node.isClickable && node.contentDescription?.toString()?.equals(text, ignoreCase = true) == true) {
        Log.d(TAG, "Found clickable node with description: ${node.contentDescription}")
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    // Recursively search children...
}
```

### 6. Add Notification When Button is Clicked

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class AutoClickAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val CHANNEL_ID = "auto_click_channel"
        private const val NOTIFICATION_ID = 1
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        createNotificationChannel()
        Log.d(TAG, "Accessibility Service Connected")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Auto Click Notifications",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun showNotification(buttonText: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Button Clicked")
            .setContentText("Clicked: $buttonText")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.isClickable && node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            Log.d(TAG, "Found clickable node with text: ${node.text}")
            val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            if (clicked) {
                showNotification(node.text.toString()) // NEW
            }
            return clicked
        }
        // ... rest of the function
    }
}
```

Don't forget to add notification permission in AndroidManifest.xml:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 7. Filter Buttons by Class Name

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
    // Only click if it's a Button or ImageButton
    val isButton = node.className?.toString()?.contains("Button") == true
    
    if (node.isClickable && isButton && 
        node.text?.toString()?.contains(text, ignoreCase = true) == true) {
        Log.d(TAG, "Found clickable node with text: ${node.text}")
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }
    
    // ... rest of the function
}
```

### 8. Change UI Theme Colors

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AccessibilityScreen.kt`

Add a custom color scheme:

```kotlin
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

private val NetflixRed = Color(0xFFE50914)
private val NetflixBlack = Color(0xFF141414)

private val LightColorScheme = lightColorScheme(
    primary = NetflixRed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD6),
    onPrimaryContainer = NetflixBlack
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilityScreen(viewModel: AccessibilityViewModel = viewModel()) {
    MaterialTheme(
        colorScheme = LightColorScheme  // Use custom colors
    ) {
        // ... rest of the screen
    }
}
```

### 9. Add Logging for Debugging

**File**: `composeApp/src/androidMain/kotlin/com/shailesh/netflixskipintro/AutoClickAccessibilityService.kt`

```kotlin
override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    event?.let {
        // Log all events
        Log.d(TAG, """
            Event Type: ${event.eventType}
            Package: ${event.packageName}
            Class: ${event.className}
            Text: ${event.text}
            Content Description: ${event.contentDescription}
            Time: ${System.currentTimeMillis()}
        """.trimIndent())
        
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            
            // Log the entire node tree
            logNodeTree(rootInActiveWindow, 0)
            
            findAndClickButton("Skip Intro")
        }
    }
}

private fun logNodeTree(node: AccessibilityNodeInfo?, depth: Int) {
    node ?: return
    val indent = "  ".repeat(depth)
    Log.d(TAG, "$indent${node.className} - Text: ${node.text}, " +
            "ContentDesc: ${node.contentDescription}, " +
            "Clickable: ${node.isClickable}")
    
    for (i in 0 until node.childCount) {
        logNodeTree(node.getChild(i), depth + 1)
    }
}
```

### 10. Change App Name and Icon

**App Name**:
**File**: `composeApp/src/androidMain/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">Auto Clicker Pro</string>  <!-- Change this -->
    <string name="accessibility_service_description">...</string>
</resources>
```

**App Icon**:
Replace the launcher icons in:
- `composeApp/src/androidMain/res/mipmap-*/ic_launcher.png`
- `composeApp/src/androidMain/res/mipmap-*/ic_launcher_round.png`

Or use Android Studio's Image Asset tool:
1. Right-click on `res` folder
2. New → Image Asset
3. Configure your icon
4. Click "Next" → "Finish"

## Testing Your Changes

After making changes:

1. **Clean build**:
```bash
./gradlew clean
./gradlew :composeApp:assembleDebug
```

2. **Reinstall app**:
```bash
./gradlew :composeApp:installDebug
```

3. **Re-enable service**:
   - Settings → Accessibility
   - Toggle OFF then ON again

4. **Check logs**:
```bash
adb logcat | grep "AutoClickService"
```

## Advanced: Button Click with Bounds

If you need to click based on screen position:

```kotlin
fun findAndClickButtonInRegion(text: String, minY: Int, maxY: Int): Boolean {
    val rootNode = rootInActiveWindow ?: return false
    return findAndClickNodeInRegion(rootNode, text, minY, maxY)
}

private fun findAndClickNodeInRegion(
    node: AccessibilityNodeInfo, 
    text: String, 
    minY: Int, 
    maxY: Int
): Boolean {
    if (node.isClickable && node.text?.toString()?.contains(text, ignoreCase = true) == true) {
        val bounds = android.graphics.Rect()
        node.getBoundsInScreen(bounds)
        
        // Only click if button is in the specified Y range
        if (bounds.centerY() in minY..maxY) {
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }
    
    for (i in 0 until node.childCount) {
        val child = node.getChild(i) ?: continue
        if (findAndClickNodeInRegion(child, text, minY, maxY)) {
            child.recycle()
            return true
        }
        child.recycle()
    }
    
    return false
}
```

## Performance Tips

1. **Limit event processing**: Only process specific event types you need
2. **Use debouncing**: Prevent clicking the same button multiple times
3. **Recycle nodes**: Always call `node.recycle()` when done
4. **Avoid deep recursion**: Limit tree depth if performance is an issue
5. **Use coroutines**: For delayed operations, use coroutines instead of Thread.sleep()

## Security Best Practices

1. **Minimize package scope**: Only target apps you need
2. **Explain permissions**: Be transparent about what the app does
3. **No data transmission**: Don't send accessibility data over network
4. **Open source**: Keep code visible for audit
5. **Regular updates**: Keep dependencies up to date

