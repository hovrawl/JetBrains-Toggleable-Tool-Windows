package com.github.hovrawl.jetbrainstoggleabletoolwindows.controller

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

/**
 * Helper class to detect hover events on tool window stripe icons.
 * This uses a best-effort approach to find stripe components in the UI hierarchy.
 */
class StripeHoverDetector(
    private val project: Project,
    private val controller: CompactUIController
) {
    private val logger = Logger.getInstance(StripeHoverDetector::class.java)
    private val stripeListeners = mutableMapOf<Component, MouseAdapter>()

    fun install() {
        // Try to find and install listeners on stripe components
        ApplicationManager.getApplication().invokeLater {
            tryInstallStripeListeners()
        }
    }

    fun uninstall() {
        stripeListeners.forEach { (component, listener) ->
            component.removeMouseListener(listener)
        }
        stripeListeners.clear()
    }

    private fun tryInstallStripeListeners() {
        try {
            val twm = ToolWindowManager.getInstance(project)
            
            // For each tool window, try to find its stripe button component
            twm.toolWindowIds.forEach { id ->
                val toolWindow = twm.getToolWindow(id)
                toolWindow?.let {
                    tryInstallListenerForToolWindow(it)
                }
            }
        } catch (e: Exception) {
            if (isDebugLoggingEnabled()) {
                logger.info("Could not install stripe listeners (this is expected with internal API changes): ${e.message}")
            }
        }
    }

    private fun tryInstallListenerForToolWindow(toolWindow: ToolWindow) {
        try {
            // This is a best-effort attempt. The stripe button components are not public API.
            // We try to access them through reflection or by searching the UI hierarchy.
            
            // Attempt 1: Try to get the stripe button through the component hierarchy
            val stripeButton = findStripeButtonComponent(toolWindow)
            
            if (stripeButton != null) {
                installHoverListener(stripeButton, toolWindow)
            } else if (isDebugLoggingEnabled()) {
                logger.info("Could not find stripe button for ${toolWindow.id}")
            }
        } catch (e: Exception) {
            if (isDebugLoggingEnabled()) {
                logger.info("Error installing listener for ${toolWindow.id}: ${e.message}")
            }
        }
    }

    private fun findStripeButtonComponent(toolWindow: ToolWindow): Component? {
        // This is a placeholder implementation.
        // The actual implementation would need to:
        // 1. Get the ToolWindowManagerImpl instance
        // 2. Access the ToolWindowsPane
        // 3. Find the stripe component for the tool window's anchor
        // 4. Locate the specific button for this tool window
        
        // Since this requires internal API access which may not be stable,
        // we return null for now and rely on the toggle action integration.
        return null
    }

    private fun installHoverListener(component: Component, toolWindow: ToolWindow) {
        // Remove existing listener if any
        stripeListeners[component]?.let {
            component.removeMouseListener(it)
        }

        val listener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                controller.requestShow(toolWindow.id, "stripe_hover")
            }

            override fun mouseExited(e: MouseEvent) {
                // Don't immediately hide - the mouse might be moving to the window
                // The window's own mouse listener will handle the hide
            }
        }

        component.addMouseListener(listener)
        stripeListeners[component] = listener

        if (isDebugLoggingEnabled()) {
            logger.info("Installed hover listener for ${toolWindow.id}")
        }
    }

    private fun isDebugLoggingEnabled(): Boolean {
        return CompactUISettings.getInstance().state.debugLogging
    }
}
