package com.github.hovrawl.jetbrainstoggleabletoolwindows.actions

import com.github.hovrawl.jetbrainstoggleabletoolwindows.controller.CompactUIController
import com.github.hovrawl.jetbrainstoggleabletoolwindows.services.RememberedToolWindowsService
import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager

abstract class ToggleIslandAction(private val targetAnchor: ToolWindowAnchor) : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val twm = ToolWindowManager.getInstance(project)

        // Check if Compact UI is enabled
        val compactUISettings = CompactUISettings.getInstance().state
        if (compactUISettings.enabled) {
            handleCompactUIMode(project, twm)
            return
        }

        // Original behavior when Compact UI is disabled
        val activeId = twm.activeToolWindowId
            ?: e.getData(CommonDataKeys.PROJECT)?.let { null } // explicit null; we'll rely on remembered id

        val activeWindow: ToolWindow? = activeId?.let { twm.getToolWindow(it) }
        val activeAnchor: ToolWindowAnchor? = activeWindow?.anchor

        val service = project.getService(RememberedToolWindowsService::class.java)

        // Collect all visible tool windows on the target island (supports split top/bottom)
        val visibleOnIsland: List<ToolWindow> = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == targetAnchor && it.isVisible }

        val activeOnIsland = activeWindow != null && activeAnchor == targetAnchor

        // Close behavior: if an active tool window exists on this island OR there are any visible on this island (even if focus is elsewhere), hide them all.
        if (activeOnIsland || visibleOnIsland.isNotEmpty()) {
            // Remember all currently visible tool windows on this island (so we can reopen them together)
            val idsToRemember = visibleOnIsland.map { it.id }
            if (idsToRemember.isNotEmpty()) {
                service.rememberIds(targetAnchor, idsToRemember)
            } else if (activeOnIsland) {
                // Fallback: remember the active one if somehow list is empty
                service.rememberId(targetAnchor, activeWindow!!.id)
            }
            visibleOnIsland.forEach { it.hide(null) }
            return
        }

        // None active on this island: try to activate remembered (possibly multiple)
        val rememberedIds = service.getRememberedIds(targetAnchor)
        val windows = rememberedIds.mapNotNull { twm.getToolWindow(it) }
        if (windows.isNotEmpty()) {
            // Activate the first and show the rest to restore both sections
            windows.first().activate(null, true)
            windows.drop(1).forEach { it.show(null) }
            return
        }

        // If nothing remembered or invalid, open the first available tool window at the top of the configured island
        val top = findFirstAvailableToolWindowOnIsland(twm, targetAnchor)
        top?.activate(null, true)
    }

    private fun handleCompactUIMode(project: Project, twm: ToolWindowManager) {
        val controller = CompactUIController.getInstance(project)
        val service = project.getService(RememberedToolWindowsService::class.java)

        // Collect all visible tool windows on the target island
        val visibleOnIsland: List<ToolWindow> = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == targetAnchor && it.isVisible }

        if (visibleOnIsland.isNotEmpty()) {
            // Remember and hide all visible windows on this island using standard API (controller manages only floating windows)
            service.rememberIds(targetAnchor, visibleOnIsland.map { it.id })
            visibleOnIsland.forEach { it.hide(null) }
            return
        }

        // Show remembered windows (use controller for floating presentation)
        val rememberedIds = service.getRememberedIds(targetAnchor)
        if (rememberedIds.isNotEmpty()) {
            controller.requestShow(rememberedIds.first(), "toggle_action")
        } else {
            val firstWindow = findFirstAvailableToolWindowOnIsland(twm, targetAnchor)
            firstWindow?.let {
                controller.requestShow(it.id, "toggle_action")
            }
        }
    }

    private fun findFirstAvailableToolWindowOnIsland(twm: ToolWindowManager, anchor: ToolWindowAnchor): ToolWindow? {
        // Best-effort approximation of "top of the configured": pick the first available tool window with the given anchor.
        return twm.toolWindowIds
            .asSequence()
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == anchor && it.isAvailable }
            .firstOrNull()
    }

    override fun update(e: AnActionEvent) {
        val project: Project? = e.project
        e.presentation.isEnabled = project != null
    }
}
