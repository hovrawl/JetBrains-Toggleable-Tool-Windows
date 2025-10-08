package com.github.hovrawl.jetbrainstoggleabletoolwindows.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@State(name = "RememberedToolWindowsService", storages = [Storage("toggleable-tool-windows.xml")])
@Service(Service.Level.PROJECT)
class RememberedToolWindowsService(private val project: Project) : PersistentStateComponent<RememberedToolWindowsService.State> {

    data class State(
        var lastLeftId: String? = null,
        var lastRightId: String? = null,
        var lastBottomId: String? = null,
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    fun getRememberedId(anchor: com.intellij.openapi.wm.ToolWindowAnchor): String? = when (anchor) {
        com.intellij.openapi.wm.ToolWindowAnchor.LEFT -> state.lastLeftId
        com.intellij.openapi.wm.ToolWindowAnchor.RIGHT -> state.lastRightId
        com.intellij.openapi.wm.ToolWindowAnchor.BOTTOM -> state.lastBottomId
        else -> null
    }

    fun rememberId(anchor: com.intellij.openapi.wm.ToolWindowAnchor, id: String?) {
        when (anchor) {
            com.intellij.openapi.wm.ToolWindowAnchor.LEFT -> state.lastLeftId = id
            com.intellij.openapi.wm.ToolWindowAnchor.RIGHT -> state.lastRightId = id
            com.intellij.openapi.wm.ToolWindowAnchor.BOTTOM -> state.lastBottomId = id
            else -> {}
        }
    }
}
