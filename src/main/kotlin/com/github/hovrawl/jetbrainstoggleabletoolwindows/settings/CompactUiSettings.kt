package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

/**
 * Facade settings service for the Compact UI feature (floating tool windows).
 * This complements CompactUiSettings (immersive top bar) and resolves naming disparities
 * between branches by providing the PascalCase `UI` variant used by existing code/tests.
 */
@State(name = "CompactUISettings", storages = [Storage("compact-ui-settings.xml")])
@Service(Service.Level.APP)
class CompactUISettings : PersistentStateComponent<CompactUISettingsState> {

    private var state = CompactUISettingsState()

    override fun getState(): CompactUISettingsState = state

    override fun loadState(state: CompactUISettingsState) {
        this.state = state
        // Notify interested components that settings changed
        ApplicationManager.getApplication()
            .messageBus
            .syncPublisher(CompactUISettingsListener.TOPIC)
            .settingsChanged()
    }

    companion object {
        @JvmStatic
        fun getInstance(): CompactUISettings =
            ApplicationManager.getApplication().getService(CompactUISettings::class.java)
    }
}
