package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.Alarm
import com.intellij.util.messages.MessageBusConnection
import java.util.concurrent.ConcurrentHashMap

@Service(Service.Level.PROJECT)
class CompactUIController(private val project: Project) : Disposable {

    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val originalTypes = ConcurrentHashMap<String, ToolWindowType>()
    private val floatingWindows = ConcurrentHashMap<String, ToolWindow>()
    private var connection: MessageBusConnection? = null
    
    init {
        if (isCompactEnabled()) {
            startListening()
        }
    }

    private fun startListening() {
        if (connection != null) return
        
        connection = project.messageBus.connect(this)
        connection?.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun toolWindowShown(toolWindow: ToolWindow) {
                if (debugLog()) {
                    thisLogger().info("Compact UI: Tool window shown: ${toolWindow.id}")
                }
                handleToolWindowShown(toolWindow)
            }

            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                if (debugLog()) {
                    thisLogger().info("Compact UI: Tool window manager state changed")
                }
            }
        })
    }

    private fun stopListening() {
        connection?.disconnect()
        connection = null
    }

    private fun handleToolWindowShown(toolWindow: ToolWindow) {
        val settings = CompactUISettings.getInstance().state
        
        // Skip if pinned and suppressWhenPinned is true
        if (settings.suppressWhenPinned && !toolWindow.isAutoHide) {
            if (debugLog()) {
                thisLogger().info("Compact UI: Skipping pinned window: ${toolWindow.id}")
            }
            return
        }

        // Skip if onlyWhenEditorFocused is true and editor is not focused
        // (This is a simplified check - actual implementation would need to check focus)
        if (settings.onlyWhenEditorFocused) {
            // TODO: Add proper editor focus check using FileEditorManager
            // For now, we skip this check
        }
    }

    fun requestShow(windows: List<ToolWindow>) {
        if (!isCompactEnabled()) return

        val settings = CompactUISettings.getInstance().state
        
        // Filter out pinned windows if suppressWhenPinned is enabled
        val windowsToShow = if (settings.suppressWhenPinned) {
            windows.filter { it.isAutoHide }
        } else {
            windows
        }
        
        if (windowsToShow.isEmpty()) return
        
        alarm.cancelAllRequests()
        alarm.addRequest({
            windowsToShow.forEach { window ->
                showFloating(window)
            }
        }, settings.hoverActivationDelayMs)
    }

    fun requestHide(windows: List<ToolWindow>) {
        if (!isCompactEnabled()) return

        val settings = CompactUISettings.getInstance().state
        
        // Filter out pinned windows if suppressWhenPinned is enabled
        val windowsToHide = if (settings.suppressWhenPinned) {
            windows.filter { it.isAutoHide }
        } else {
            windows
        }
        
        if (windowsToHide.isEmpty()) return
        
        alarm.cancelAllRequests()
        alarm.addRequest({
            windowsToHide.forEach { window ->
                hideFloating(window)
            }
        }, settings.autoHideDelayMs)
    }

    fun forceHideAll(anchor: ToolWindowAnchor) {
        if (!isCompactEnabled()) return

        val settings = CompactUISettings.getInstance().state
        val twm = ToolWindowManager.getInstance(project)
        val windowsToHide = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == anchor && it.isVisible }
            .filter { !settings.suppressWhenPinned || it.isAutoHide }

        windowsToHide.forEach { hideFloating(it) }
    }

    fun forceHideAllStripes() {
        if (!isCompactEnabled()) return

        ToolWindowAnchor.values().forEach { anchor ->
            forceHideAll(anchor)
        }
    }

    private fun showFloating(window: ToolWindow) {
        if (debugLog()) {
            thisLogger().info("Compact UI: Showing floating window: ${window.id}")
        }

        try {
            // Store original type if not already stored
            if (!originalTypes.containsKey(window.id)) {
                originalTypes[window.id] = window.type
            }

            // Switch to floating mode
            ApplicationManager.getApplication().invokeLater {
                try {
                    window.type = ToolWindowType.FLOATING
                    window.show(null)
                    floatingWindows[window.id] = window
                } catch (e: Exception) {
                    if (debugLog()) {
                        thisLogger().warn("Compact UI: Error showing floating window ${window.id}", e)
                    }
                }
            }
        } catch (e: Exception) {
            if (debugLog()) {
                thisLogger().warn("Compact UI: Error in showFloating for ${window.id}", e)
            }
        }
    }

    private fun hideFloating(window: ToolWindow) {
        if (debugLog()) {
            thisLogger().info("Compact UI: Hiding floating window: ${window.id}")
        }

        try {
            ApplicationManager.getApplication().invokeLater {
                try {
                    window.hide(null)
                    floatingWindows.remove(window.id)
                } catch (e: Exception) {
                    if (debugLog()) {
                        thisLogger().warn("Compact UI: Error hiding floating window ${window.id}", e)
                    }
                }
            }
        } catch (e: Exception) {
            if (debugLog()) {
                thisLogger().warn("Compact UI: Error in hideFloating for ${window.id}", e)
            }
        }
    }

    fun cleanup() {
        if (debugLog()) {
            thisLogger().info("Compact UI: Cleaning up controller")
        }

        alarm.cancelAllRequests()
        
        // Restore original types
        ApplicationManager.getApplication().invokeLater {
            try {
                val twm = ToolWindowManager.getInstance(project)
                originalTypes.forEach { (id, originalType) ->
                    try {
                        val window = twm.getToolWindow(id)
                        if (window != null) {
                            window.type = originalType
                            if (window.isVisible) {
                                window.hide(null)
                            }
                        }
                    } catch (e: Exception) {
                        if (debugLog()) {
                            thisLogger().warn("Compact UI: Error restoring window $id", e)
                        }
                    }
                }
                originalTypes.clear()
                floatingWindows.clear()
            } catch (e: Exception) {
                if (debugLog()) {
                    thisLogger().warn("Compact UI: Error during cleanup", e)
                }
            }
        }
        
        stopListening()
    }

    fun isCompactEnabled(): Boolean = CompactUISettings.getInstance().state.enabled

    private fun debugLog(): Boolean = CompactUISettings.getInstance().state.debugLogging

    override fun dispose() {
        cleanup()
    }

    companion object {
        fun getInstance(project: Project): CompactUIController =
            project.getService(CompactUIController::class.java)

        fun notifyAllControllersToCleanup() {
            // This is called when Compact UI is disabled globally
            // Cleanup all open projects' controllers
            com.intellij.openapi.project.ProjectManager.getInstance().openProjects.forEach { project ->
                val controller = getInstance(project)
                controller.cleanup()
            }
        }
    }
}
