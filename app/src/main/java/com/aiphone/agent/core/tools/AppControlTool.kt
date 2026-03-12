package com.aiphone.agent.core.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class OpenAppTool @Inject constructor(@ApplicationContext private val context: Context) : BaseTool() {
    override val name = "open_app"
    override val description = "Open an installed app by its name (e.g., WhatsApp, Settings, Camera)"
    override val parameters = mapOf("app_name" to ToolParameter("Name of the app to open"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        val appName = param(params, "app_name").lowercase()
        val pm = context.packageManager
        return try {
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val app = packages.firstOrNull { pm.getApplicationLabel(it).toString().lowercase().contains(appName) }
            if (app != null) {
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    ToolExecutionResult(true, "Opened ${pm.getApplicationLabel(app)}", durationMs = System.currentTimeMillis() - start)
                } else ToolExecutionResult(false, "", "App cannot be launched", System.currentTimeMillis() - start)
            } else ToolExecutionResult(false, "", "App not found: $appName", System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class ListAppsTool @Inject constructor(@ApplicationContext private val context: Context) : BaseTool() {
    override val name = "list_apps"
    override val description = "List all installed launchable apps on the device"
    override val parameters = emptyMap<String, ToolParameter>()
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return try {
            val pm = context.packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map { pm.getApplicationLabel(it).toString() }.sorted().take(100)
            ToolExecutionResult(true, apps.joinToString(", "), durationMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class ClickOnTextTool @Inject constructor() : BaseTool() {
    override val name = "click_on_text"
    override val description = "Click a UI element by visible text (requires Accessibility Service)"
    override val parameters = mapOf("text" to ToolParameter("Text visible on screen to click"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        val text = param(params, "text")
        return try {
            val success = com.aiphone.agent.core.accessibility.AIAccessibilityService.instance?.clickOnText(text) ?: false
            if (success) ToolExecutionResult(true, "Clicked on: $text", durationMs = System.currentTimeMillis() - start)
            else ToolExecutionResult(false, "", "Text not found. Enable Accessibility Service in Settings.", System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class TypeTextTool @Inject constructor() : BaseTool() {
    override val name = "type_text"
    override val description = "Type text into the focused text field (requires Accessibility Service)"
    override val parameters = mapOf("text" to ToolParameter("Text to type"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        val text = param(params, "text")
        return try {
            val success = com.aiphone.agent.core.accessibility.AIAccessibilityService.instance?.typeText(text) ?: false
            if (success) ToolExecutionResult(true, "Typed: $text", durationMs = System.currentTimeMillis() - start)
            else ToolExecutionResult(false, "", "Failed to type. Enable Accessibility Service.", System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class GetScreenTextTool @Inject constructor() : BaseTool() {
    override val name = "get_screen_text"
    override val description = "Get all visible text from the current screen (requires Accessibility Service)"
    override val parameters = emptyMap<String, ToolParameter>()
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return try {
            val text = com.aiphone.agent.core.accessibility.AIAccessibilityService.instance?.getScreenText() ?: ""
            if (text.isNotBlank()) ToolExecutionResult(true, text.take(3000), durationMs = System.currentTimeMillis() - start)
            else ToolExecutionResult(false, "", "No text found. Enable Accessibility Service.", System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class ScrollTool @Inject constructor() : BaseTool() {
    override val name = "scroll"
    override val description = "Scroll the current screen. Direction: up or down"
    override val parameters = mapOf("direction" to ToolParameter("up or down"))
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        val direction = param(params, "direction").lowercase()
        return try {
            val success = com.aiphone.agent.core.accessibility.AIAccessibilityService.instance?.scroll(direction == "down") ?: false
            ToolExecutionResult(success, if (success) "Scrolled $direction" else "", error = if (!success) "Scroll failed" else null, durationMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}

class PressBackTool @Inject constructor() : BaseTool() {
    override val name = "press_back"
    override val description = "Press the Android Back button"
    override val parameters = emptyMap<String, ToolParameter>()
    override suspend fun execute(params: Map<String, String>): ToolExecutionResult {
        val start = System.currentTimeMillis()
        return try {
            com.aiphone.agent.core.accessibility.AIAccessibilityService.instance?.pressBack()
            ToolExecutionResult(true, "Pressed back", durationMs = System.currentTimeMillis() - start)
        } catch (e: Exception) { ToolExecutionResult(false, "", e.message, System.currentTimeMillis() - start) }
    }
}
