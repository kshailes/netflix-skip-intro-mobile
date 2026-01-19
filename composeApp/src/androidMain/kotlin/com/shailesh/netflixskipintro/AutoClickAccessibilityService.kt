package com.shailesh.netflixskipintro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoClickService"
        private const val PREFS_NAME = "AutoClickPrefs"
        private const val KEY_BUTTON_TEXTS = "button_texts"
        private const val KEY_AUTO_CLICK_ENABLED = "auto_click_enabled"
        private const val KEY_ENABLED_APPS = "enabled_apps"
        private const val KEY_SHOW_NOTIFICATIONS = "show_notifications"
        private const val KEY_CUSTOM_APPS = "custom_apps"
        private const val KEY_CUSTOM_APP_NAMES = "custom_app_names"
        
        private const val NOTIFICATION_CHANNEL_ID = "auto_click_channel"
        private const val NOTIFICATION_ID = 1001
        
        private var instance: AutoClickAccessibilityService? = null

        fun getInstance(): AutoClickAccessibilityService? = instance

        fun isServiceEnabled(): Boolean = instance != null
        
        // Default button texts to search for
        private val DEFAULT_BUTTON_TEXTS = listOf(
            "Skip Intro",
            "Skip",
            "SKIP INTRO",
            "SKIP",
            "Skip Ad",
            "Skip Ads"
        )
        
        // Packages to ignore - never interact with these apps
        private val IGNORED_PACKAGES = setOf(
            "com.android.settings",
            "com.google.android.settings",
            "com.samsung.android.settings",
            "com.android.systemui",
            "com.android.launcher",
            "com.google.android.launcher",
            "com.samsung.android.launcher"
        )
        
        // Default apps to enable
        val DEFAULT_ENABLED_APPS = mapOf(
            "com.netflix.mediaclient" to "Netflix",
            "com.hulu.plus" to "Hulu",
            "com.amazon.avod.thirdpartyclient" to "Prime Video",
            "com.disney.disneyplus" to "Disney+",
            "com.hbo.hbonow" to "HBO Max",
            "com.google.android.youtube" to "YouTube",
            "tv.twitch.android.app" to "Twitch"
        )
    }
    
    private var autoClickEnabled = true
    private var buttonTextsToSearch = mutableListOf<String>()
    private var enabledApps = mutableSetOf<String>()
    private var customApps = mutableMapOf<String, String>() // packageName to appName
    private var showNotifications = true
    private var lastClickTime = 0L
    private var currentForegroundApp: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        loadPreferences()
        createNotificationChannel()
        Log.d(TAG, "Accessibility Service Connected")
        Log.d(TAG, "Auto-click enabled: $autoClickEnabled")
        Log.d(TAG, "Button texts: $buttonTextsToSearch")
        Log.d(TAG, "Enabled apps: $enabledApps")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (!autoClickEnabled) return
            
            // IMPORTANT: Ignore events from our own app
            if (event.packageName == packageName) {
                return
            }
            
            // IMPORTANT: Ignore system apps
            val eventPackage = event.packageName?.toString() ?: ""
            if (IGNORED_PACKAGES.contains(eventPackage) || 
                eventPackage.contains("settings", ignoreCase = true) ||
                eventPackage.contains("launcher", ignoreCase = true)) {
                return
            }
            
            // Check if this app is in the enabled list
            // If enabled list is not empty, only process apps in the list
            if (!enabledApps.contains(eventPackage)) {
                Log.v(TAG, "Skipping $eventPackage - not in enabled apps list")
                return
            }
            
            // Detect app coming to foreground
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (currentForegroundApp != eventPackage) {
                    currentForegroundApp = eventPackage
                    showAppForegroundNotification(eventPackage)
                }
            }
            
            Log.d(TAG, "Event: ${it.eventType}, Package: ${it.packageName}")
            
            // Auto-detect and click buttons
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                // Debounce
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime < 1000) {
                    return
                }
                
                // Try to find and click configured button texts
                for (buttonText in buttonTextsToSearch) {
                    if (findAndClickButton(buttonText)) {
                        lastClickTime = currentTime
                        Log.d(TAG, "Successfully clicked button: $buttonText in $eventPackage")
                        showButtonClickedNotification(buttonText, eventPackage)
                        break
                    }
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Accessibility Service Destroyed")
    }

    /**
     * Create notification channel for Android O+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Auto-Click Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications when auto-click service is active"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification when enabled app comes to foreground
     */
    private fun showAppForegroundNotification(packageName: String) {
        if (!showNotifications) return
        
        // Check custom apps first, then default apps
        val appName = customApps[packageName] ?: DEFAULT_ENABLED_APPS[packageName] ?: packageName
        
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Auto-Click Active")
            .setContentText("Monitoring $appName for buttons")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(5000) // Auto-dismiss after 5 seconds
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show notification when button is clicked
     */
    private fun showButtonClickedNotification(buttonText: String, packageName: String) {
        if (!showNotifications) return
        
        // Check custom apps first, then default apps
        val appName = customApps[packageName] ?: DEFAULT_ENABLED_APPS[packageName] ?: packageName
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Button Clicked")
            .setContentText("Clicked '$buttonText' in $appName")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setTimeoutAfter(3000)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    /**
     * Load preferences
     */
    private fun loadPreferences() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        autoClickEnabled = prefs.getBoolean(KEY_AUTO_CLICK_ENABLED, true)
        showNotifications = prefs.getBoolean(KEY_SHOW_NOTIFICATIONS, true)
        
        val savedTexts = prefs.getStringSet(KEY_BUTTON_TEXTS, null)
        buttonTextsToSearch.clear()
        if (savedTexts != null && savedTexts.isNotEmpty()) {
            buttonTextsToSearch.addAll(savedTexts)
        } else {
            buttonTextsToSearch.addAll(DEFAULT_BUTTON_TEXTS)
        }
        
        // Load custom apps
        val savedCustomApps = prefs.getStringSet(KEY_CUSTOM_APPS, null)
        val savedCustomAppNames = prefs.getStringSet(KEY_CUSTOM_APP_NAMES, null)
        customApps.clear()
        if (savedCustomApps != null && savedCustomAppNames != null) {
            // Reconstruct the map from saved sets
            savedCustomApps.forEachIndexed { index, packageName ->
                val appName = savedCustomAppNames.elementAtOrNull(index) ?: packageName
                customApps[packageName] = appName
            }
        }
        
        val savedApps = prefs.getStringSet(KEY_ENABLED_APPS, null)
        enabledApps.clear()
        if (savedApps != null && savedApps.isNotEmpty()) {
            enabledApps.addAll(savedApps)
        } else {
            enabledApps.addAll(DEFAULT_ENABLED_APPS.keys)
        }
    }

    /**
     * Save button texts
     */
    fun saveButtonTexts(texts: List<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_BUTTON_TEXTS, texts.toSet()).apply()
        buttonTextsToSearch.clear()
        buttonTextsToSearch.addAll(texts)
        Log.d(TAG, "Saved button texts: $buttonTextsToSearch")
    }
    
    /**
     * Get current button texts
     */
    fun getButtonTexts(): List<String> = buttonTextsToSearch.toList()
    
    /**
     * Save enabled apps
     */
    fun saveEnabledApps(apps: Set<String>) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_ENABLED_APPS, apps).apply()
        enabledApps.clear()
        enabledApps.addAll(apps)
        Log.d(TAG, "Saved enabled apps: $enabledApps")
    }
    
    /**
     * Get enabled apps
     */
    fun getEnabledApps(): Set<String> = enabledApps.toSet()
    
    /**
     * Get all apps (default + custom)
     */
    fun getAllApps(): Map<String, String> {
        val allApps = mutableMapOf<String, String>()
        allApps.putAll(DEFAULT_ENABLED_APPS)
        allApps.putAll(customApps)
        return allApps
    }
    
    /**
     * Add custom app
     */
    fun addCustomApp(packageName: String, appName: String) {
        customApps[packageName] = appName
        saveCustomApps()
        Log.d(TAG, "Added custom app: $appName ($packageName)")
    }
    
    /**
     * Remove app (from custom apps)
     */
    fun removeCustomApp(packageName: String) {
        customApps.remove(packageName)
        // Also remove from enabled apps
        enabledApps.remove(packageName)
        saveCustomApps()
        saveEnabledApps(enabledApps)
        Log.d(TAG, "Removed custom app: $packageName")
    }
    
    /**
     * Check if app is custom (not default)
     */
    fun isCustomApp(packageName: String): Boolean {
        return customApps.containsKey(packageName)
    }
    
    /**
     * Save custom apps to preferences
     */
    private fun saveCustomApps() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putStringSet(KEY_CUSTOM_APPS, customApps.keys)
            putStringSet(KEY_CUSTOM_APP_NAMES, customApps.values.toSet())
            apply()
        }
    }
    
    /**
     * Set auto-click enabled
     */
    fun setAutoClickEnabled(enabled: Boolean) {
        autoClickEnabled = enabled
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_CLICK_ENABLED, enabled).apply()
        Log.d(TAG, "Auto-click enabled: $autoClickEnabled")
    }
    
    /**
     * Get auto-click enabled
     */
    fun isAutoClickEnabled(): Boolean = autoClickEnabled
    
    /**
     * Set show notifications
     */
    fun setShowNotifications(show: Boolean) {
        showNotifications = show
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_SHOW_NOTIFICATIONS, show).apply()
        Log.d(TAG, "Show notifications: $showNotifications")
    }
    
    /**
     * Get show notifications
     */
    fun getShowNotifications(): Boolean = showNotifications

    /**
     * Find and click a button by text
     */
    fun findAndClickButton(buttonText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodePackage = rootNode.packageName?.toString() ?: ""
        
        // Check own app
        if (nodePackage == packageName) {
            return false
        }
        
        // Check system apps
        if (IGNORED_PACKAGES.contains(nodePackage) || 
            nodePackage.contains("settings", ignoreCase = true) ||
            nodePackage.contains("launcher", ignoreCase = true)) {
            return false
        }
        
        // Check if app is enabled (only for auto-click, manual clicks can bypass this)
        // But we still log it for debugging
        if (!enabledApps.contains(nodePackage)) {
            Log.v(TAG, "Note: Clicking in $nodePackage which is not in enabled apps list")
        }
        
        return findAndClickNodeByText(rootNode, buttonText, ignoreCase = true)
    }
    
    /**
     * Find and click with case sensitivity
     */
    fun findAndClickButton(buttonText: String, ignoreCase: Boolean): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodePackage = rootNode.packageName?.toString() ?: ""
        
        // Check own app
        if (nodePackage == packageName) {
            return false
        }
        
        // Check system apps
        if (IGNORED_PACKAGES.contains(nodePackage) || 
            nodePackage.contains("settings", ignoreCase = true) ||
            nodePackage.contains("launcher", ignoreCase = true)) {
            return false
        }
        
        // Note: Manual clicks are allowed even if app is not in enabled list
        if (!enabledApps.contains(nodePackage)) {
            Log.v(TAG, "Note: Manually clicking in $nodePackage which is not in enabled apps list")
        }
        
        return findAndClickNodeByText(rootNode, buttonText, ignoreCase)
    }

    private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String, ignoreCase: Boolean): Boolean {
        try {
            // Check text
            if (node.text?.toString()?.contains(text, ignoreCase = ignoreCase) == true) {
                if (node.isClickable) {
                    val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) {
                        Log.d(TAG, "✓ Clicked node with text: ${node.text}")
                        return true
                    }
                } else {
                    val parent = node.parent
                    if (parent != null && parent.isClickable) {
                        val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        if (clicked) return true
                    }
                }
            }

            // Check content description
            if (node.contentDescription?.toString()?.contains(text, ignoreCase = ignoreCase) == true) {
                if (node.isClickable) {
                    val clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    if (clicked) {
                        Log.d(TAG, "✓ Clicked node with desc: ${node.contentDescription}")
                        return true
                    }
                } else {
                    val parent = node.parent
                    if (parent != null && parent.isClickable) {
                        val clicked = parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        parent.recycle()
                        if (clicked) return true
                    }
                }
            }

            // Recursively search children
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                if (findAndClickNodeByText(child, text, ignoreCase)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while searching node: ${e.message}")
        }

        return false
    }

    /**
     * Click at coordinates
     */
    fun clickAtCoordinates(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                Log.d(TAG, "Gesture cancelled")
            }
        }, null)
    }

    /**
     * Get all clickable buttons
     */
    fun getAllClickableButtons(): List<ButtonInfo> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val buttons = mutableListOf<ButtonInfo>()
        findAllClickableNodes(rootNode, buttons)
        return buttons
    }

    private fun findAllClickableNodes(node: AccessibilityNodeInfo, buttons: MutableList<ButtonInfo>) {
        try {
            val text = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            val className = node.className?.toString() ?: ""
            val bounds = Rect()
            node.getBoundsInScreen(bounds)
            
            if ((node.isClickable || node.parent?.isClickable == true) && 
                (text.isNotEmpty() || contentDesc.isNotEmpty())) {
                buttons.add(ButtonInfo(
                    text = text,
                    contentDescription = contentDesc,
                    className = className,
                    isClickable = node.isClickable,
                    bounds = "${bounds.left},${bounds.top},${bounds.right},${bounds.bottom}"
                ))
            }

            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findAllClickableNodes(child, buttons)
                child.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error finding clickable nodes: ${e.message}")
        }
    }

    data class ButtonInfo(
        val text: String,
        val contentDescription: String,
        val className: String,
        val isClickable: Boolean = true,
        val bounds: String = ""
    )
}
