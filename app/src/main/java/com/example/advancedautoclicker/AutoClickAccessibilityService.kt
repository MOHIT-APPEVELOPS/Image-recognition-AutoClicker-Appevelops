package com.example.advancedautoclicker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class AutoClickAccessibilityService : AccessibilityService() {

    // delayMs वेरिएबल जोड़ दिया गया है ताकि unresolved reference एरर न आए
    var delayMs: Long = 100L

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handle accessibility events
    }

    override fun onInterrupt() {
        // Required override for AccessibilityService
    }
}
