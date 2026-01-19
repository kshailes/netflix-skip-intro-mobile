package com.shailesh.netflixskipintro

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AutoClickAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "AutoClickService"
        private var instance: AutoClickAccessibilityService? = null

        fun getInstance(): AutoClickAccessibilityService? = instance

        fun isServiceEnabled(): Boolean = instance != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Accessibility Service Connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events if needed
        event?.let {
            Log.d(TAG, "Event: ${it.eventType}, Package: ${it.packageName}")
            
            // Auto-detect and click buttons with specific text
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                
                // Look for Netflix skip intro button
                findAndClickButton("Skip Intro")
                findAndClickButton("Skip")
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
     * Find and click a button by text
     */
    fun findAndClickButton(buttonText: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        return findAndClickNodeByText(rootNode, buttonText)
    }

    private fun findAndClickNodeByText(node: AccessibilityNodeInfo, text: String): Boolean {
        // Check if current node matches
        if (node.isClickable && node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            Log.d(TAG, "Found clickable node with text: ${node.text}")
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // Check content description
        if (node.isClickable && node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true) {
            Log.d(TAG, "Found clickable node with description: ${node.contentDescription}")
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

        // Recursively search children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            if (findAndClickNodeByText(child, text)) {
                child.recycle()
                return true
            }
            child.recycle()
        }

        return false
    }

    /**
     * Click at specific coordinates
     */
    fun clickAtCoordinates(x: Float, y: Float): Boolean {
        val path = Path()
        path.moveTo(x, y)
        
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        return dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                Log.d(TAG, "Gesture completed at ($x, $y)")
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.d(TAG, "Gesture cancelled")
            }
        }, null)
    }

    /**
     * Find all clickable buttons in current screen
     */
    fun getAllClickableButtons(): List<ButtonInfo> {
        val rootNode = rootInActiveWindow ?: return emptyList()
        val buttons = mutableListOf<ButtonInfo>()
        findAllClickableNodes(rootNode, buttons)
        return buttons
    }

    private fun findAllClickableNodes(node: AccessibilityNodeInfo, buttons: MutableList<ButtonInfo>) {
        if (node.isClickable) {
            val text = node.text?.toString() ?: ""
            val contentDesc = node.contentDescription?.toString() ?: ""
            val className = node.className?.toString() ?: ""
            
            if (text.isNotEmpty() || contentDesc.isNotEmpty()) {
                buttons.add(ButtonInfo(
                    text = text,
                    contentDescription = contentDesc,
                    className = className
                ))
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findAllClickableNodes(child, buttons)
            child.recycle()
        }
    }

    data class ButtonInfo(
        val text: String,
        val contentDescription: String,
        val className: String
    )
}

