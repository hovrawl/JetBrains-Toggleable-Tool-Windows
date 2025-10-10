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
        val originalAutoHide: Boolean,
        var isManagedByCompactUI: Boolean = false
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
                    // Restore any Compact UI-managed window that is no longer visible (auto-hidden by IDE)
                    windowStates.keys.toList().forEach { id ->
                        val state = windowStates[id] ?: return@forEach
                        if (!state.isManagedByCompactUI) return@forEach
                        val tw = toolWindowManager.getToolWindow(id) ?: return@forEach
                        if (!tw.isVisible) {
                            restoreWindowState(id)
                            if (isDebugLoggingEnabled()) {
                                logger.info("STATE_SYNC - Restored $id after IDE auto-hide")
                            }
                        }
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
            // Compact UI was disabled - restore all managed windows
            forceHideAll()
            restoreAllWindowTypes()
        } else {
            // Re-install hover detection when enabled to catch late UI construction
            stripeHoverDetector.install()
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
        
        // Allow pinned windows when the request originates from a toggle action
        val bypassPinnedSuppression = trigger == "toggle_action"

        if (!isEligible(toolWindow, bypassPinnedSuppression)) {
            if (isDebugLoggingEnabled()) {
                if (settings.suppressWhenPinned && !toolWindow.isAutoHide && !bypassPinnedSuppression) {
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
        if (windowState?.isManagedByCompactUI != true) return

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
            if (windowStates[id]?.isManagedByCompactUI == true) {
                performHide(id)
            }
        }
    }

    private fun performShow(id: String) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id) ?: return
        
        // Store original type and auto-hide if not already stored
        if (!windowStates.containsKey(id)) {
            windowStates[id] = WindowState(
                originalType = toolWindow.type,
                originalAutoHide = toolWindow.isAutoHide,
                isManagedByCompactUI = false
            )
        }

        // Use SLIDING + auto-hide for overlay behavior that collapses when clicking off
        ApplicationManager.getApplication().invokeLater {
//            try {
//                if (!toolWindow.isAutoHide) toolWindow.setAutoHide(true)
//            } catch (_: Throwable) {
//                // setAutoHide may not be available in some builds; ignore
//            }
            if (toolWindow.type != ToolWindowType.SLIDING) {
                toolWindow.setType(ToolWindowType.SLIDING, null)
            }
            if (!toolWindow.isVisible) {
                toolWindow.activate(null, true)
            } else {
                toolWindow.activate(null, true)
            }
        }

        windowStates[id]?.isManagedByCompactUI = true
        showRequests.remove(id)

        if (isDebugLoggingEnabled()) {
            logger.info("SHOW_COMMIT - $id (SLIDING+autoHide)")
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
                if (isDebugLoggingEnabled()) {
                    logger.info("HIDE_DEFERRED_EDITOR_FOCUS - $id")
                }
                val runnable = Runnable { performHide(id) }
                hideRequests[id]?.let { alarm.cancelRequest(it) }
                alarm.addRequest(runnable, 200)
                hideRequests[id] = runnable
                return
            }
        }

        // Restore original type and auto-hide, then hide
        ApplicationManager.getApplication().invokeLater {
            toolWindow.setType(windowState.originalType, null)
//            try {
//                toolWindow.setAutoHide(windowState.originalAutoHide)
//            } catch (_: Throwable) {
//                // Ignore if not available
//            }
            toolWindow.hide(null)
        }

        windowState.isManagedByCompactUI = false
        hideRequests.remove(id)

        if (isDebugLoggingEnabled()) {
            logger.info("HIDE_COMMIT - $id (restored type & autoHide)")
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
            if (state.isManagedByCompactUI) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id)
                toolWindow?.let {
                    ApplicationManager.getApplication().invokeLater {
                        it.setType(state.originalType, null)
//                        try {
//                            it.setAutoHide(state.originalAutoHide)
//                        } catch (_: Throwable) {
//                            // ignore if not available
//                        }
                    }
                }
            }
        }
        windowStates.clear()
    }

    private fun isEligible(toolWindow: ToolWindow, bypassPinnedSuppression: Boolean = false): Boolean {
        val settings = CompactUISettings.getInstance().state
        
        if (!settings.enabled) return false
        if (!toolWindow.isAvailable) return false
        
        // Check if pinned windows should be suppressed (unless bypassed)
        if (!bypassPinnedSuppression && settings.suppressWhenPinned && !toolWindow.isAutoHide) {
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

    private fun restoreWindowState(id: String) {
        val state = windowStates[id] ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(id) ?: return
        ApplicationManager.getApplication().invokeLater {
            toolWindow.setType(state.originalType, null)
//            try {
//                toolWindow.setAutoHide(state.originalAutoHide)
//            } catch (_: Throwable) {
//                // ignore if not available
//            }
        }
        state.isManagedByCompactUI = false
        // Also remove any mouse listener in case it was still attached
        removeWindowMouseListener(toolWindow)
    }

    companion object {
        fun getInstance(project: Project): CompactUIController {
            return project.getService(CompactUIController::class.java)
        }
    }
}
