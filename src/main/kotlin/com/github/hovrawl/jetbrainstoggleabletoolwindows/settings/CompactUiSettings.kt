package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "CompactUISettings", storages = [Storage("compact-ui-settings.xml")])
@Service(Service.Level.APP)
class CompactUISettings : PersistentStateComponent<CompactUISettingsState> {

    private var state = CompactUISettingsState()

    override fun getState(): CompactUISettingsState = state

    override fun loadState(state: CompactUISettingsState) {
        this.state = state
    }

    companion object {
        fun getInstance(): CompactUISettings =
            ApplicationManager.getApplication().getService(CompactUISettings::class.java)
    }
}
