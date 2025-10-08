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

        if (activeWindow != null && activeAnchor == targetAnchor) {
            // remember and hide
            service.rememberId(targetAnchor, activeWindow.id)
            activeWindow.hide(null)
            return
        }

        // None active on this stripe: try to activate remembered
        val rememberedId = service.getRememberedId(targetAnchor)
        if (rememberedId != null) {
            val toOpen = twm.getToolWindow(rememberedId)
            if (toOpen != null) {
                // Ensure visibility
                toOpen.activate(null, true)
                return
            }
        }
        // If nothing remembered or invalid, do nothing.
    }

    override fun update(e: AnActionEvent) {
        val project: Project? = e.project
        e.presentation.isEnabled = project != null
    }
}
