package com.github.hovrawl.jetbrainstoggleabletoolwindows.actions

import com.github.hovrawl.jetbrainstoggleabletoolwindows.services.RememberedToolWindowsService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager

abstract class ToggleStripeAction(private val targetAnchor: ToolWindowAnchor) : AnAction(), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val twm = ToolWindowManager.getInstance(project)

        val activeId = twm.activeToolWindowId
            ?: e.getData(CommonDataKeys.PROJECT)?.let { null } // explicit null; we'll rely on remembered id

        val activeWindow: ToolWindow? = activeId?.let { twm.getToolWindow(it) }
        val activeAnchor: ToolWindowAnchor? = activeWindow?.anchor

        val service = project.getService(RememberedToolWindowsService::class.java)

        // Collect all visible tool windows on the target stripe (supports split top/bottom)
        val visibleOnStripe: List<ToolWindow> = twm.toolWindowIds
            .mapNotNull { twm.getToolWindow(it) }
            .filter { it.anchor == targetAnchor && it.isVisible }

        val activeOnStripe = activeWindow != null && activeAnchor == targetAnchor

        // Close behavior: if an active tool window exists on this stripe OR there are any visible on this stripe (even if focus is elsewhere), hide them all.
        if (activeOnStripe || (activeWindow == null && visibleOnStripe.isNotEmpty())) {
            // Remember all currently visible tool windows on this stripe (so we can reopen them together)
            val idsToRemember = visibleOnStripe.map { it.id }
            if (idsToRemember.isNotEmpty()) {
                service.rememberIds(targetAnchor, idsToRemember)
            } else if (activeOnStripe) {
                // Fallback: remember the active one if somehow list is empty
                service.rememberId(targetAnchor, activeWindow!!.id)
            }
            visibleOnStripe.forEach { it.hide(null) }
            return
        }

        // None active on this stripe: try to activate remembered (possibly multiple)
        val rememberedIds = service.getRememberedIds(targetAnchor)
        val windows = rememberedIds.mapNotNull { twm.getToolWindow(it) }
        if (windows.isNotEmpty()) {
            // Activate the first and show the rest to restore both sections
            windows.first().activate(null, true)
            windows.drop(1).forEach { it.show(null) }
            return
        }

        // If nothing remembered or invalid, open the first available tool window at the top of the configured stripe
        val top = findFirstAvailableToolWindowOnStripe(twm, targetAnchor)
        top?.activate(null, true)
    }

    private fun findFirstAvailableToolWindowOnStripe(twm: ToolWindowManager, anchor: ToolWindowAnchor): ToolWindow? {
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
