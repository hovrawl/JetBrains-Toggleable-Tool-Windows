package com.github.hovrawl.jetbrainstoggleabletoolwindows.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowAnchor

@State(name = "RememberedToolWindowsService", storages = [Storage("toggleable-tool-windows.xml")])
@Service(Service.Level.PROJECT)
class RememberedToolWindowsService(private val project: Project) : PersistentStateComponent<RememberedToolWindowsService.State> {

    data class State(
        // Legacy single-remembered id fields (kept for backward compatibility with existing persisted state)
        var lastLeftId: String? = null,
        var lastRightId: String? = null,
        var lastBottomId: String? = null,
        // New multi-remembered ids per island
        var lastLeftIds: MutableList<String> = mutableListOf(),
        var lastRightIds: MutableList<String> = mutableListOf(),
        var lastBottomIds: MutableList<String> = mutableListOf(),
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
        // Simple migration: if new lists are empty but legacy single id exists, migrate it into the list
        if (this.state.lastLeftIds.isEmpty() && this.state.lastLeftId != null) {
            this.state.lastLeftIds = mutableListOf(this.state.lastLeftId!!)
        }
        if (this.state.lastRightIds.isEmpty() && this.state.lastRightId != null) {
            this.state.lastRightIds = mutableListOf(this.state.lastRightId!!)
        }
        if (this.state.lastBottomIds.isEmpty() && this.state.lastBottomId != null) {
            this.state.lastBottomIds = mutableListOf(this.state.lastBottomId!!)
        }
    }

    // Legacy single-id accessors retained for compatibility with existing code/tests
    fun getRememberedId(anchor: ToolWindowAnchor): String? = when (anchor) {
        ToolWindowAnchor.LEFT -> state.lastLeftIds.firstOrNull() ?: state.lastLeftId
        ToolWindowAnchor.RIGHT -> state.lastRightIds.firstOrNull() ?: state.lastRightId
        ToolWindowAnchor.BOTTOM -> state.lastBottomIds.firstOrNull() ?: state.lastBottomId
        else -> null
    }

    fun rememberId(anchor: ToolWindowAnchor, id: String?) {
        if (id == null) return
        when (anchor) {
            ToolWindowAnchor.LEFT -> {
                state.lastLeftIds = mutableListOf(id)
                state.lastLeftId = id
            }
            ToolWindowAnchor.RIGHT -> {
                state.lastRightIds = mutableListOf(id)
                state.lastRightId = id
            }
            ToolWindowAnchor.BOTTOM -> {
                state.lastBottomIds = mutableListOf(id)
                state.lastBottomId = id
            }
            else -> {}
        }
    }

    // New multi-id API
    fun getRememberedIds(anchor: ToolWindowAnchor): List<String> = when (anchor) {
        ToolWindowAnchor.LEFT -> if (state.lastLeftIds.isNotEmpty()) state.lastLeftIds.toList() else listOfNotNull(state.lastLeftId)
        ToolWindowAnchor.RIGHT -> if (state.lastRightIds.isNotEmpty()) state.lastRightIds.toList() else listOfNotNull(state.lastRightId)
        ToolWindowAnchor.BOTTOM -> if (state.lastBottomIds.isNotEmpty()) state.lastBottomIds.toList() else listOfNotNull(state.lastBottomId)
        else -> emptyList()
    }

    fun rememberIds(anchor: ToolWindowAnchor, ids: List<String>) {
        val unique = ids.distinct().toMutableList()
        when (anchor) {
            ToolWindowAnchor.LEFT -> {
                state.lastLeftIds = unique
                state.lastLeftId = unique.firstOrNull()
            }
            ToolWindowAnchor.RIGHT -> {
                state.lastRightIds = unique
                state.lastRightId = unique.firstOrNull()
            }
            ToolWindowAnchor.BOTTOM -> {
                state.lastBottomIds = unique
                state.lastBottomId = unique.firstOrNull()
            }
            else -> {}
        }
    }
}
