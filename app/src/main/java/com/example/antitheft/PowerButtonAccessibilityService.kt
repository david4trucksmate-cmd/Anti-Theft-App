class PowerButtonAccessibilityService : AccessibilityService() {
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // Detect power button long press (power menu)
            if (event.className?.contains("PowerMenu") == true || 
                event.className?.contains("GlobalActions") == true) {
                
                // Kill power menu and show fake shutdown
                performGlobalAction(GLOBAL_ACTION_BACK)
                performGlobalAction(GLOBAL_ACTION_HOME)
                
                val intent = Intent(this, FakePowerOffActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
    }
}
