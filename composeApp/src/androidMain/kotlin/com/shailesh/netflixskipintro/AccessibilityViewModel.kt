package com.shailesh.netflixskipintro

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccessibilityViewModel : ViewModel() {
    
    private val _isServiceEnabled = MutableStateFlow(false)
    val isServiceEnabled: StateFlow<Boolean> = _isServiceEnabled.asStateFlow()
    
    private val _detectedButtons = MutableStateFlow<List<AutoClickAccessibilityService.ButtonInfo>>(emptyList())
    val detectedButtons: StateFlow<List<AutoClickAccessibilityService.ButtonInfo>> = _detectedButtons.asStateFlow()
    
    fun checkServiceStatus() {
        _isServiceEnabled.value = AutoClickAccessibilityService.isServiceEnabled()
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
    
    fun clickAtPosition(x: Float, y: Float): Boolean {
        val service = AutoClickAccessibilityService.getInstance()
        return service?.clickAtCoordinates(x, y) ?: false
    }
    
    fun refreshDetectedButtons() {
        val service = AutoClickAccessibilityService.getInstance()
        _detectedButtons.value = service?.getAllClickableButtons() ?: emptyList()
    }
}

