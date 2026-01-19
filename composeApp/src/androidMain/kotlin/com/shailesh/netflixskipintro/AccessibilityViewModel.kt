package com.shailesh.netflixskipintro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessibilityViewModel : ViewModel() {
    
    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()
    
    private val _detectedButtons = MutableStateFlow<List<AutoClickAccessibilityService.ButtonInfo>>(emptyList())
    val detectedButtons: StateFlow<List<AutoClickAccessibilityService.ButtonInfo>> = _detectedButtons.asStateFlow()
    
    private val _buttonTexts = MutableStateFlow<List<String>>(emptyList())
    val buttonTexts: StateFlow<List<String>> = _buttonTexts.asStateFlow()
    
    private val _autoClickEnabled = MutableStateFlow(true)
    val autoClickEnabled: StateFlow<Boolean> = _autoClickEnabled.asStateFlow()
    
    private val _enabledApps = MutableStateFlow<Set<String>>(emptySet())
    val enabledApps: StateFlow<Set<String>> = _enabledApps.asStateFlow()
    
    private val _allApps = MutableStateFlow<Map<String, String>>(emptyMap())
    val allApps: StateFlow<Map<String, String>> = _allApps.asStateFlow()
    
    private val _showNotifications = MutableStateFlow(true)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()
    
    fun checkServiceStatus() {
        _isServiceEnabled.value = AutoClickAccessibilityService.isServiceEnabled()
        
        if (_isServiceEnabled.value) {
            val service = AutoClickAccessibilityService.getInstance()
            _buttonTexts.value = service?.getButtonTexts() ?: emptyList()
            _autoClickEnabled.value = service?.isAutoClickEnabled() ?: true
            _enabledApps.value = service?.getEnabledApps() ?: emptySet()
            _allApps.value = service?.getAllApps() ?: emptyMap()
            _showNotifications.value = service?.getShowNotifications() ?: true
        }
    }
    
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    fun clickButton(buttonText: String): Boolean {
        val service = AutoClickAccessibilityService.getInstance()
        return service?.findAndClickButton(buttonText) ?: false
    }
    
    fun clickButton(buttonText: String, ignoreCase: Boolean): Boolean {
        val service = AutoClickAccessibilityService.getInstance()
        return service?.findAndClickButton(buttonText, ignoreCase) ?: false
    }
    
    fun clickAtPosition(x: Float, y: Float): Boolean {
        val service = AutoClickAccessibilityService.getInstance()
        return service?.clickAtCoordinates(x, y) ?: false
    }
    
    fun refreshDetectedButtons() {
        val service = AutoClickAccessibilityService.getInstance()
        _detectedButtons.value = service?.getAllClickableButtons() ?: emptyList()
    }
    
    fun addButtonText(text: String) {
        val service = AutoClickAccessibilityService.getInstance()
        val currentTexts = service?.getButtonTexts()?.toMutableList() ?: mutableListOf()
        if (!currentTexts.contains(text)) {
            currentTexts.add(text)
            service?.saveButtonTexts(currentTexts)
            _buttonTexts.value = currentTexts
        }
    }
    
    fun removeButtonText(text: String) {
        val service = AutoClickAccessibilityService.getInstance()
        val currentTexts = service?.getButtonTexts()?.toMutableList() ?: mutableListOf()
        currentTexts.remove(text)
        service?.saveButtonTexts(currentTexts)
        _buttonTexts.value = currentTexts
    }
    
    fun updateButtonTexts(texts: List<String>) {
        val service = AutoClickAccessibilityService.getInstance()
        service?.saveButtonTexts(texts)
        _buttonTexts.value = texts
    }
    
    fun setAutoClickEnabled(enabled: Boolean) {
        val service = AutoClickAccessibilityService.getInstance()
        service?.setAutoClickEnabled(enabled)
        _autoClickEnabled.value = enabled
    }
    
    fun toggleAppEnabled(packageName: String) {
        val service = AutoClickAccessibilityService.getInstance()
        val currentApps = service?.getEnabledApps()?.toMutableSet() ?: mutableSetOf()
        
        if (currentApps.contains(packageName)) {
            currentApps.remove(packageName)
        } else {
            currentApps.add(packageName)
        }
        
        service?.saveEnabledApps(currentApps)
        _enabledApps.value = currentApps
    }
    
    fun isAppEnabled(packageName: String): Boolean {
        return _enabledApps.value.contains(packageName)
    }
    
    fun setShowNotifications(show: Boolean) {
        val service = AutoClickAccessibilityService.getInstance()
        service?.setShowNotifications(show)
        _showNotifications.value = show
    }
    
    fun addCustomApp(packageName: String, appName: String) {
        val service = AutoClickAccessibilityService.getInstance()
        service?.addCustomApp(packageName, appName)
        // Refresh all apps list
        _allApps.value = service?.getAllApps() ?: emptyMap()
    }
    
    fun removeApp(packageName: String) {
        val service = AutoClickAccessibilityService.getInstance()
        service?.removeCustomApp(packageName)
        // Refresh lists
        _allApps.value = service?.getAllApps() ?: emptyMap()
        _enabledApps.value = service?.getEnabledApps() ?: emptySet()
    }
    
    fun isCustomApp(packageName: String): Boolean {
        val service = AutoClickAccessibilityService.getInstance()
        return service?.isCustomApp(packageName) ?: false
    }
    
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permission not required for Android 12 and below
        }
    }
    
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
