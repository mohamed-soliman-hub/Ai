package com.aiphone.agent.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import timber.log.Timber

class AIAccessibilityService : AccessibilityService() {
    companion object {
        @Volatile var instance: AIAccessibilityService? = null
            private set
        fun isRunning() = instance != null
    }

    override fun onServiceConnected() { super.onServiceConnected(); instance = this; Timber.d("AIAccessibilityService connected") }
    override fun onDestroy() { super.onDestroy(); instance = null }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun clickOnText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val node = findNodeByText(root, text) ?: return false
        return node.performAction(AccessibilityNodeInfo.ACTION_CLICK).also { node.recycle() }
    }

    fun typeText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = findFocusedEditText(root) ?: return false
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args).also { focused.recycle() }
    }

    fun getScreenText(): String {
        val root = rootInActiveWindow ?: return ""
        val sb = StringBuilder()
        collectText(root, sb)
        return sb.toString().trim()
    }

    fun scroll(scrollDown: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val action = if (scrollDown) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        return findScrollableNode(root)?.performAction(action) ?: false
    }

    fun pressBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun pressHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val byExact = root.findAccessibilityNodeInfosByText(text)
        if (byExact.isNotEmpty()) return byExact.first()
        for (i in 0 until root.childCount) { val result = findNodeByText(root.getChild(i) ?: continue, text); if (result != null) return result }
        return null
    }

    private fun findFocusedEditText(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (root.className?.contains("EditText") == true && root.isFocused) return root
        for (i in 0 until root.childCount) { val result = findFocusedEditText(root.getChild(i) ?: continue); if (result != null) return result }
        return null
    }

    private fun findScrollableNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) { val result = findScrollableNode(node.getChild(i) ?: continue); if (result != null) return result }
        return null
    }

    private fun collectText(node: AccessibilityNodeInfo, sb: StringBuilder) {
        if (!node.text.isNullOrBlank()) sb.appendLine(node.text.toString())
        if (!node.contentDescription.isNullOrBlank()) sb.appendLine(node.contentDescription.toString())
        for (i in 0 until node.childCount) { collectText(node.getChild(i) ?: continue, sb) }
    }
}
