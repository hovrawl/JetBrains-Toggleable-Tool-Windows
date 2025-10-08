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
        if (settings.suppressWhenPinned && toolWindow.isAutoHide.not()) {
            if (debugLog()) {
                thisLogger().info("Compact UI: Skipping pinned window: ${toolWindow.id}")
            }
            return
        }

        // Skip if onlyWhenEditorFocused is true and editor is not focused
        // (This is a simplified check - actual implementation would need to check focus)
        if (settings.onlyWhenEditorFocused) {
            // TODO: Add proper editor focus check
        }
    }

    fun requestShow(windows: List<ToolWindow>) {
        if (!isCompactEnabled()) return

        val settings = CompactUISettings.getInstance().state
        
        alarm.cancelAllRequests()
        alarm.addRequest({
            windows.forEach { window ->
                showFloating(window)
            }
        }, settings.hoverActivationDelayMs)
    }

    fun requestHide(windows: List<ToolWindow>) {
        if (!isCompactEnabled()) return

        val settings = CompactUISettings.getInstance().state
        
        alarm.cancelAllRequests()
        alarm.addRequest({
            windows.forEach { window ->
                hideFloating(window)
            }
        }, settings.autoHideDelayMs)
    }

    fun forceHideAll(anchor: ToolWindowAnchor) {
        if (!isCompactEnabled()) return

        val twm = ToolWindowManager.getInstance(project)
        val windowsToHide = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == anchor && it.isVisible }

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

        // Store original type if not already stored
        if (!originalTypes.containsKey(window.id)) {
            originalTypes[window.id] = window.type
        }

        // Switch to floating mode
        ApplicationManager.getApplication().invokeLater {
            window.type = ToolWindowType.FLOATING
            window.show(null)
            floatingWindows[window.id] = window
        }
    }

    private fun hideFloating(window: ToolWindow) {
        if (debugLog()) {
            thisLogger().info("Compact UI: Hiding floating window: ${window.id}")
        }

        ApplicationManager.getApplication().invokeLater {
            window.hide(null)
            floatingWindows.remove(window.id)
        }
    }

    fun cleanup() {
        if (debugLog()) {
            thisLogger().info("Compact UI: Cleaning up controller")
        }

        alarm.cancelAllRequests()
        
        // Restore original types
        ApplicationManager.getApplication().invokeLater {
            originalTypes.forEach { (id, originalType) ->
                val window = ToolWindowManager.getInstance(project).getToolWindow(id)
                if (window != null) {
                    window.type = originalType
                    if (window.isVisible) {
                        window.hide(null)
                    }
                }
            }
            originalTypes.clear()
            floatingWindows.clear()
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
            // This would be called when Compact UI is disabled globally
            // We need to cleanup all open projects' controllers
            // For now, this is a placeholder - actual implementation would iterate through open projects
        }
    }
}
