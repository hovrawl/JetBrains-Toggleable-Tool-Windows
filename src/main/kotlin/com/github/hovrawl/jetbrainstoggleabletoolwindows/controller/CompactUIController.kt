package com.github.hovrawl.jetbrainstoggleabletoolwindows.controller

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettingsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ToolWindowType
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.Alarm
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class CompactUIController(private val project: Project) : Disposable {

    private val logger = Logger.getInstance(CompactUIController::class.java)
    private val alarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val stripeHoverDetector = StripeHoverDetector(project, this)
    
    // State tracking
    private data class WindowState(
        val originalType: ToolWindowType,
        var isFloatingByCompactUI: Boolean = false
    )
    
    private val windowStates = mutableMapOf<String, WindowState>()
    private val showRequests = mutableMapOf<String, Runnable>()
    private val hideRequests = mutableMapOf<String, Runnable>()
    private val mouseListeners = mutableMapOf<Component, MouseAdapter>()

    init {
        setupListeners()
        // Try to install stripe hover detection
        stripeHoverDetector.install()
    }

    private fun setupListeners() {
        // Listen for tool window registration changes
        project.messageBus.connect(this).subscribe(
            ToolWindowManagerListener.TOPIC,
            object : ToolWindowManagerListener {
                override fun stateChanged(toolWindowManager: ToolWindowManager) {
                    // Tool window state changed - we might need to refresh
                    if (isDebugLoggingEnabled()) {
                        logger.info("Tool window state changed")
                    }
                }
            }
        )

        // Listen for settings changes
        project.messageBus.connect(this).subscribe(
            CompactUISettingsListener.TOPIC,
            object : CompactUISettingsListener {
                override fun settingsChanged() {
                    handleSettingsChanged()
                }
            }
        )
    }

    private fun handleSettingsChanged() {
        val settings = CompactUISettings.getInstance().state
        if (!settings.enabled) {
            // Compact UI was disabled - restore all floating windows
            forceHideAll()
            restoreAllWindowTypes()
        }
        if (isDebugLoggingEnabled()) {
            logger.info("Settings changed - enabled: ${settings.enabled}")
        }
    }

    fun requestShow(id: String, trigger: String) {
        val settings = CompactUISettings.getInstance().state
        if (!settings.enabled) {
            if (isDebugLoggingEnabled()) {
                logger.info("DISABLED_GLOBAL - ignoring show request for $id")
            }
            return
        }

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id) ?: return
        
        if (!isEligible(toolWindow)) {
            if (isDebugLoggingEnabled()) {
                if (settings.suppressWhenPinned && !toolWindow.isAutoHide) {
                    logger.info("SUPPRESSED_PINNED - $id is pinned (isAutoHide=false)")
                } else {
                    logger.info("NOT_ELIGIBLE - $id")
                }
            }
            return
        }

        // Cancel any pending hide request
        hideRequests[id]?.let {
            alarm.cancelRequest(it)
            hideRequests.remove(id)
            if (isDebugLoggingEnabled()) {
                logger.info("HIDE_CANCELLED - $id")
            }
        }

        if (isDebugLoggingEnabled()) {
            logger.info("SHOW_REQUEST - $id, trigger: $trigger")
        }

        // Schedule show after hover delay
        val runnable = Runnable { performShow(id) }
        alarm.addRequest(runnable, settings.hoverActivationDelayMs)
        showRequests[id] = runnable
    }

    fun requestHide(id: String, reason: String) {
        val settings = CompactUISettings.getInstance().state
        if (!settings.enabled) return

        // Cancel any pending show request
        showRequests[id]?.let {
            alarm.cancelRequest(it)
            showRequests.remove(id)
        }

        val windowState = windowStates[id]
        
        // Only hide if we're managing this window
        if (windowState?.isFloatingByCompactUI != true) return

        if (isDebugLoggingEnabled()) {
            logger.info("HIDE_SCHEDULED - $id, reason: $reason, delay: ${settings.autoHideDelayMs}ms")
        }

        // Schedule hide after auto-hide delay
        val runnable = Runnable { performHide(id) }
        alarm.addRequest(runnable, settings.autoHideDelayMs)
        hideRequests[id] = runnable
    }

    fun forceHideAll() {
        // Cancel all pending requests
        showRequests.values.forEach { alarm.cancelRequest(it) }
        hideRequests.values.forEach { alarm.cancelRequest(it) }
        showRequests.clear()
        hideRequests.clear()

        // Hide all windows we're managing
        windowStates.keys.toList().forEach { id ->
            if (windowStates[id]?.isFloatingByCompactUI == true) {
                performHide(id)
            }
        }
    }

    private fun performShow(id: String) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id) ?: return
        
        // Store original type if not already stored
        if (!windowStates.containsKey(id)) {
            windowStates[id] = WindowState(
                originalType = toolWindow.type,
                isFloatingByCompactUI = false
            )
        }

        // Change to floating type if not already
        if (toolWindow.type != ToolWindowType.FLOATING) {
            ApplicationManager.getApplication().invokeLater {
                toolWindow.setType(ToolWindowType.FLOATING, null)
            }
        }

        // Show the window
        ApplicationManager.getApplication().invokeLater {
            if (!toolWindow.isVisible) {
                toolWindow.show(null)
            }
        }

        windowStates[id]?.isFloatingByCompactUI = true
        showRequests.remove(id)

        if (isDebugLoggingEnabled()) {
            logger.info("SHOW_COMMIT - $id")
        }

        // Install mouse listener on the tool window to detect when mouse leaves
        installWindowMouseListener(toolWindow)
    }

    private fun performHide(id: String) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id) ?: return
        val windowState = windowStates[id] ?: return

        // Enforce 'only hide when editor refocuses' if enabled
        val settings = CompactUISettings.getInstance().state
        if (settings.onlyWhenEditorFocused) {
            if (!isEditorFocused()) {
                // Re-schedule until editor regains focus
                if (isDebugLoggingEnabled()) {
                    logger.info("HIDE_DEFERRED_EDITOR_FOCUS - $id")
                }
                val runnable = Runnable { performHide(id) }
                // Replace existing hide request with a short retry
                hideRequests[id]?.let { alarm.cancelRequest(it) }
                alarm.addRequest(runnable, 200)
                hideRequests[id] = runnable
                return
            }
        }

        // Restore original type and hide
        ApplicationManager.getApplication().invokeLater {
            toolWindow.setType(windowState.originalType, null)
            toolWindow.hide(null)
        }

        windowState.isFloatingByCompactUI = false
        hideRequests.remove(id)

        if (isDebugLoggingEnabled()) {
            logger.info("HIDE_COMMIT - $id")
        }

        // Remove mouse listener
        removeWindowMouseListener(toolWindow)
    }

    private fun isEditorFocused(): Boolean {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return false
        val editorComponent = editor.component
        val focusOwner: Component = IdeFocusManager.getInstance(project).focusOwner ?: return false
        return SwingUtilities.isDescendingFrom(focusOwner, editorComponent)
    }

    private fun restoreAllWindowTypes() {
        windowStates.forEach { (id, state) ->
            if (state.isFloatingByCompactUI) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id)
                toolWindow?.let {
                    ApplicationManager.getApplication().invokeLater {
                        it.setType(state.originalType, null)
                    }
                }
            }
        }
        windowStates.clear()
    }

    private fun isEligible(toolWindow: ToolWindow): Boolean {
        val settings = CompactUISettings.getInstance().state
        
        if (!settings.enabled) return false
        if (!toolWindow.isAvailable) return false
        
        // Check if pinned windows should be suppressed
        // In IntelliJ: isAutoHide = true means unpinned, isAutoHide = false means pinned
        if (settings.suppressWhenPinned && !toolWindow.isAutoHide) {
            return false
        }
        
        return true
    }

    private fun installWindowMouseListener(toolWindow: ToolWindow) {
        val component = toolWindow.component

        // Remove existing listener if any
        removeWindowMouseListener(toolWindow)
        
        val listener = object : MouseAdapter() {
            override fun mouseExited(e: MouseEvent) {
                // Check if mouse actually left the component bounds
                val point = SwingUtilities.convertPoint(e.component, e.point, component)
                if (!component.contains(point)) {
                    requestHide(toolWindow.id, "mouse_exit_window")
                }
            }
            
            override fun mouseEntered(e: MouseEvent) {
                // Cancel any pending hide when mouse re-enters
                hideRequests[toolWindow.id]?.let {
                    alarm.cancelRequest(it)
                    hideRequests.remove(toolWindow.id)
                    if (isDebugLoggingEnabled()) {
                        logger.info("HIDE_CANCELLED - ${toolWindow.id} (mouse re-entered)")
                    }
                }
            }
        }
        
        component.addMouseListener(listener)
        mouseListeners[component] = listener
    }

    private fun removeWindowMouseListener(toolWindow: ToolWindow) {
        val component = toolWindow.component
        mouseListeners[component]?.let {
            component.removeMouseListener(it)
            mouseListeners.remove(component)
        }
    }

    private fun isDebugLoggingEnabled(): Boolean {
        return CompactUISettings.getInstance().state.debugLogging
    }

    override fun dispose() {
        // Cancel all pending requests
        showRequests.values.forEach { alarm.cancelRequest(it) }
        hideRequests.values.forEach { alarm.cancelRequest(it) }
        showRequests.clear()
        hideRequests.clear()

        // Remove all mouse listeners
        mouseListeners.forEach { (component, listener) ->
            component.removeMouseListener(listener)
        }
        mouseListeners.clear()

        // Uninstall stripe hover detection
        stripeHoverDetector.uninstall()

        // Restore all window types
        restoreAllWindowTypes()
    }

    companion object {
        fun getInstance(project: Project): CompactUIController {
            return project.getService(CompactUIController::class.java)
        }
    }
}
