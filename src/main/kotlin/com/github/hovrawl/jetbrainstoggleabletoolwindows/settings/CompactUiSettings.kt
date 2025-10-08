package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "CompactUiSettings", storages = [Storage("compact-ui.xml")])
@Service(Service.Level.APP)
class CompactUiSettings : PersistentStateComponent<CompactUiSettings.State> {

    data class State(
        // Immersive Top Bar settings
        var enableAutoHideTopBar: Boolean = false,
        var revealZoneHeight: Int = 4,
        var hideDelay: Int = 700,
        var edgePadding: Int = 4,
        var applySidesAndBottom: Boolean = true,
        var hideNavigationBar: Boolean = true,
        var enableAnimation: Boolean = false, // Future feature, disabled for now
        
        // Debug logging
        var debugLogging: Boolean = false
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): CompactUiSettings = ApplicationManager.getApplication().getService(CompactUiSettings::class.java)
    }
}
