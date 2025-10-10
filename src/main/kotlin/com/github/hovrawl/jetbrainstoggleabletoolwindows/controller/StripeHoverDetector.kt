package com.github.hovrawl.jetbrainstoggleabletoolwindows.controller

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.github.hovrawl.jetbrainstoggleabletoolwindows.services.RememberedToolWindowsService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
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
 * Detect hover on tool window stripes to trigger Compact UI show by installing a listener
 * on the entire left, right and bottom stripe panes (icon-level listeners removed).
 */
class StripeHoverDetector(
    private val project: Project,
    private val controller: CompactUIController
) {
    private val logger = Logger.getInstance(StripeHoverDetector::class.java)
    // Left stripe state
    private var leftStripeComponent: Component? = null
    private var leftStripeListener: MouseAdapter? = null
    private var lastLeftShownId: String? = null
    // Right stripe state
    private var rightStripeComponent: Component? = null
    private var rightStripeListener: MouseAdapter? = null
    private var lastRightShownId: String? = null
    // Bottom stripe state
    private var bottomStripeComponent: Component? = null
    private var bottomStripeListener: MouseAdapter? = null
    private var lastBottomShownId: String? = null

    fun install() {
        // Clean previous state
        uninstall()
        ApplicationManager.getApplication().invokeLater {
            tryInstallLeftStripeHover()
            tryInstallRightStripeHover()
            tryInstallBottomStripeHover()
        }
    }

    fun uninstall() {
        // Remove left stripe listener if present
        leftStripeComponent?.let { comp ->
            leftStripeListener?.let { listener ->
                runCatching { comp.removeMouseListener(listener) }
            }
        }
        leftStripeComponent = null
        leftStripeListener = null
        lastLeftShownId = null
        // Remove right stripe listener if present
        rightStripeComponent?.let { comp ->
            rightStripeListener?.let { listener ->
                runCatching { comp.removeMouseListener(listener) }
            }
        }
        rightStripeComponent = null
        rightStripeListener = null
        lastRightShownId = null
        // Remove bottom stripe listener if present
        bottomStripeComponent?.let { comp ->
            bottomStripeListener?.let { listener ->
                runCatching { comp.removeMouseListener(listener) }
            }
        }
        bottomStripeComponent = null
        bottomStripeListener = null
        lastBottomShownId = null
    }

    // -------------------- Left stripe pane hover --------------------

    private fun tryInstallLeftStripeHover() {
        val frame: Window = WindowManager.getInstance().getFrame(project) ?: return
        val root = frame as? Container ?: return
        val twm = ToolWindowManager.getInstance(project)

        // Find any left-anchored tool window's stripe button, then ascend to its stripe container
        val leftToolWindows = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == ToolWindowAnchor.LEFT && it.isAvailable }

        val stripeContainer: Component? = leftToolWindows.asSequence()
            .mapNotNull { tw -> findStripeButtonComponent(root, tw) }
            .mapNotNull { btn -> ascendToStripeContainer(btn) }
            .firstOrNull()

        if (stripeContainer == null) {
            if (isDebugLoggingEnabled()) {
                logger.info("StripeHover: left stripe container not found")
            }
            return
        }

        // Install hover listener on the stripe container once
        leftStripeComponent = stripeContainer
        val listener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                if (!CompactUISettings.getInstance().state.enabled) return
                handleLeftStripeHoverEnter()
            }
            override fun mouseExited(e: MouseEvent) {
                handleLeftStripeHoverExit()
            }
        }
        leftStripeListener = listener
        runCatching { stripeContainer.addMouseListener(listener) }

        if (isDebugLoggingEnabled()) {
            logger.info("StripeHover: left stripe hover listener installed on ${stripeContainer.javaClass.name}")
        }
    }

    private fun handleLeftStripeHoverEnter() {
        val twm = ToolWindowManager.getInstance(project)
        val service = project.getService(RememberedToolWindowsService::class.java)

        // Prefer remembered windows for LEFT, else first available on LEFT
        val remembered = service.getRememberedIds(ToolWindowAnchor.LEFT)
        val chosenId = when {
            remembered.isNotEmpty() -> remembered.first()
            else -> findFirstAvailableToolWindowOnIsland(twm, ToolWindowAnchor.LEFT)?.id
        }
        if (chosenId != null) {
            lastLeftShownId = chosenId
            // Use toggle_action so controller bypasses pinned suppression and ensures SLIDING+autoHide
            controller.requestShow(chosenId, "toggle_action")
        }
    }

    private fun handleLeftStripeHoverExit() {
        val id = lastLeftShownId ?: return
        // Schedule hide; if the mouse enters the tool window, controller cancels this hide
        controller.requestHide(id, "left_stripe_exit")
    }

    // -------------------- Right stripe pane hover --------------------

    private fun tryInstallRightStripeHover() {
        val frame: Window = WindowManager.getInstance().getFrame(project) ?: return
        val root = frame as? Container ?: return
        val twm = ToolWindowManager.getInstance(project)

        val rightToolWindows = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == ToolWindowAnchor.RIGHT && it.isAvailable }

        val stripeContainer: Component? = rightToolWindows.asSequence()
            .mapNotNull { tw -> findStripeButtonComponent(root, tw) }
            .mapNotNull { btn -> ascendToStripeContainer(btn) }
            .firstOrNull()

        if (stripeContainer == null) {
            if (isDebugLoggingEnabled()) {
                logger.info("StripeHover: right stripe container not found")
            }
            return
        }

        rightStripeComponent = stripeContainer
        val listener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                if (!CompactUISettings.getInstance().state.enabled) return
                handleRightStripeHoverEnter()
            }
            override fun mouseExited(e: MouseEvent) {
                handleRightStripeHoverExit()
            }
        }
        rightStripeListener = listener
        runCatching { stripeContainer.addMouseListener(listener) }

        if (isDebugLoggingEnabled()) {
            logger.info("StripeHover: right stripe hover listener installed on ${stripeContainer.javaClass.name}")
        }
    }

    private fun handleRightStripeHoverEnter() {
        val twm = ToolWindowManager.getInstance(project)
        val service = project.getService(RememberedToolWindowsService::class.java)

        // Prefer remembered windows for RIGHT, else first available on RIGHT
        val remembered = service.getRememberedIds(ToolWindowAnchor.RIGHT)
        val chosenId = when {
            remembered.isNotEmpty() -> remembered.first()
            else -> findFirstAvailableToolWindowOnIsland(twm, ToolWindowAnchor.RIGHT)?.id
        }
        if (chosenId != null) {
            lastRightShownId = chosenId
            // Use toggle_action so controller bypasses pinned suppression and ensures SLIDING+autoHide
            controller.requestShow(chosenId, "toggle_action")
        }
    }

    private fun handleRightStripeHoverExit() {
        val id = lastRightShownId ?: return
        // Schedule hide; if the mouse enters the tool window, controller cancels this hide
        controller.requestHide(id, "right_stripe_exit")
    }

    // -------------------- Bottom stripe pane hover --------------------

    private fun tryInstallBottomStripeHover() {
        val frame: Window = WindowManager.getInstance().getFrame(project) ?: return
        val root = frame as? Container ?: return
        val twm = ToolWindowManager.getInstance(project)

        val bottomToolWindows = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == ToolWindowAnchor.BOTTOM && it.isAvailable }

        val stripeContainer: Component? = bottomToolWindows.asSequence()
            .mapNotNull { tw -> findStripeButtonComponent(root, tw) }
            .mapNotNull { btn -> ascendToStripeContainer(btn) }
            .firstOrNull()

        if (stripeContainer == null) {
            if (isDebugLoggingEnabled()) {
                logger.info("StripeHover: bottom stripe container not found")
            }
            return
        }

        bottomStripeComponent = stripeContainer
        val listener = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                if (!CompactUISettings.getInstance().state.enabled) return
                handleBottomStripeHoverEnter()
            }
            override fun mouseExited(e: MouseEvent) {
                handleBottomStripeHoverExit()
            }
        }
        bottomStripeListener = listener
        runCatching { stripeContainer.addMouseListener(listener) }

        if (isDebugLoggingEnabled()) {
            logger.info("StripeHover: bottom stripe hover listener installed on ${stripeContainer.javaClass.name}")
        }
    }

    private fun handleBottomStripeHoverEnter() {
        val twm = ToolWindowManager.getInstance(project)
        val service = project.getService(RememberedToolWindowsService::class.java)

        // Prefer remembered windows for BOTTOM, else first available on BOTTOM
        val remembered = service.getRememberedIds(ToolWindowAnchor.BOTTOM)
        val chosenId = when {
            remembered.isNotEmpty() -> remembered.first()
            else -> findFirstAvailableToolWindowOnIsland(twm, ToolWindowAnchor.BOTTOM)?.id
        }
        if (chosenId != null) {
            lastBottomShownId = chosenId
            // Use toggle_action so controller bypasses pinned suppression and ensures SLIDING+autoHide
            controller.requestShow(chosenId, "toggle_action")
        }
    }

    private fun handleBottomStripeHoverExit() {
        val id = lastBottomShownId ?: return
        // Schedule hide; if the mouse enters the tool window, controller cancels this hide
        controller.requestHide(id, "bottom_stripe_exit")
    }

    // -------------------- Shared helpers --------------------

    private fun findFirstAvailableToolWindowOnIsland(twm: ToolWindowManager, anchor: ToolWindowAnchor): ToolWindow? {
        return twm.toolWindowIds
            .asSequence()
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == anchor && it.isAvailable }
            .firstOrNull()
    }

    private fun ascendToStripeContainer(component: Component): Container? {
        var p = component.parent
        while (p != null) {
            val name = p.javaClass.name
            if (name.contains("Stripe", ignoreCase = true)) return p
            p = p.parent
        }
        return component.parent
    }

    // -------------------- Helpers to locate stripe button (for finding stripe container only) --------------------

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

    private fun isDebugLoggingEnabled(): Boolean = CompactUISettings.getInstance().state.debugLogging
}
