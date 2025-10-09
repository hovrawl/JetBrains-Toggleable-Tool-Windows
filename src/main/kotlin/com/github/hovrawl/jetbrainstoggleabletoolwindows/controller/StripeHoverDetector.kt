package com.github.hovrawl.jetbrainstoggleabletoolwindows.controller

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.WindowManager
import java.awt.Component
import java.awt.Container
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.JComponent

/**
 * Helper class to detect hover events on tool window stripe icons.
 * Best-effort approach that scans the project frame for components that look like stripe buttons.
 */
class StripeHoverDetector(
    private val project: Project,
    private val controller: CompactUIController
) {
    private val logger = Logger.getInstance(StripeHoverDetector::class.java)
    private val stripeListeners = mutableMapOf<Component, MouseAdapter>()

    fun install() {
        // Re-scan and (re)install listeners on stripe components
        uninstall()
        ApplicationManager.getApplication().invokeLater {
            tryInstallStripeListeners()
        }
    }

    fun uninstall() {
        stripeListeners.forEach { (component, listener) ->
            runCatching { component.removeMouseListener(listener) }
        }
        stripeListeners.clear()
    }

    private fun tryInstallStripeListeners() {
        val frame: Window = WindowManager.getInstance().getFrame(project) ?: return

        val twm = ToolWindowManager.getInstance(project)
        val ids = twm.toolWindowIds.toList()

        // For each tool window, try to find a matching stripe button-like component
        ids.forEach { id ->
            val toolWindow = twm.getToolWindow(id) ?: return@forEach
            val stripeButton = findStripeButtonComponent(frame as Container, toolWindow)
            if (stripeButton != null) {
                installHoverListener(stripeButton, toolWindow)
            } else if (isDebugLoggingEnabled()) {
                logger.info("StripeHover: no stripe button found for ${toolWindow.id}")
            }
        }
    }

    private fun findStripeButtonComponent(root: Container, toolWindow: ToolWindow): Component? {
        val candidates = mutableListOf<Component>()
        collectCandidates(root, candidates)

        val expectedTexts = buildSet {
            add(toolWindow.stripeTitle)
            add(toolWindow.id)
        }.filter { it.isNotBlank() }

        // Score candidates: prefer class names containing "StripeButton" and matching text/tooltip
        return candidates
            .asSequence()
            .map { component ->
                val className = component.javaClass.name
                val scoreClass = if (className.contains("StripeButton", ignoreCase = true) || className.contains("ToolWindowStripe", ignoreCase = true)) 2 else 0
                val text = (component as? AbstractButton)?.text ?: component.accessibleContext?.accessibleName ?: component.name ?: ""
                val tooltip = (component as? JComponent)?.toolTipText ?: ""
                val matchesText = expectedTexts.any { t -> t.isNotBlank() && (text.contains(t) || tooltip.contains(t)) }
                val scoreText = if (matchesText) 3 else 0
                val score = scoreClass + scoreText
                component to score
            }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }
            ?.first
    }

    private fun collectCandidates(container: Container, out: MutableList<Component>) {
        for (comp in container.components) {
            out.add(comp)
            if (comp is Container) collectCandidates(comp, out)
        }
    }

    private fun installHoverListener(component: Component, toolWindow: ToolWindow) {
        // Avoid duplicate listeners
        stripeListeners[component]?.let { existing ->
            runCatching { component.removeMouseListener(existing) }
        }

        val listener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                controller.requestShow(toolWindow.id, "stripe_hover")
            }
        }

        runCatching { component.addMouseListener(listener) }
        stripeListeners[component] = listener

        if (isDebugLoggingEnabled()) {
            logger.info("StripeHover: listener installed for ${toolWindow.id} on ${component.javaClass.name}")
        }
    }

    private fun isDebugLoggingEnabled(): Boolean = CompactUISettings.getInstance().state.debugLogging
}
