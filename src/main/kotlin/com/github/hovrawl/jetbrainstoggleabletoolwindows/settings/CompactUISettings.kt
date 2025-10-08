package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.application.ApplicationManager

@State(name = "CompactUISettings", storages = [Storage("compact-ui-settings.xml")])
@Service(Service.Level.APP)
class CompactUISettings : PersistentStateComponent<CompactUISettings.State> {

    data class State(
        var enabled: Boolean = false,
        var hoverActivationDelayMs: Int = 150,
        var autoHideDelayMs: Int = 500,
        var onlyWhenEditorFocused: Boolean = true,
        var suppressWhenPinned: Boolean = true,
        var debugLogging: Boolean = false
    )

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): CompactUISettings = ApplicationManager.getApplication().getService(CompactUISettings::class.java)
    }
}
