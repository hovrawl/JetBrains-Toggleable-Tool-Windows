package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class CompactUIConfigurable : SearchableConfigurable {

    private var enabledCheckBox: JBCheckBox? = null
    private var hoverDelaySpinner: JSpinner? = null
    private var autoHideDelaySpinner: JSpinner? = null
    private var onlyWhenEditorFocusedCheckBox: JBCheckBox? = null
    private var suppressWhenPinnedCheckBox: JBCheckBox? = null
    private var debugLoggingCheckBox: JBCheckBox? = null

    override fun getId(): String = "compact.ui.settings"

    override fun getDisplayName(): String = "Compact UI"

    override fun createComponent(): JComponent {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        enabledCheckBox = JBCheckBox("Enable Compact UI", state.enabled)
        
        hoverDelaySpinner = JSpinner(SpinnerNumberModel(state.hoverActivationDelayMs, 0, 5000, 50))
        autoHideDelaySpinner = JSpinner(SpinnerNumberModel(state.autoHideDelayMs, 0, 5000, 50))
        
        onlyWhenEditorFocusedCheckBox = JBCheckBox("Only hide when editor refocuses", state.onlyWhenEditorFocused)
        suppressWhenPinnedCheckBox = JBCheckBox("Suppress floating for pinned tool windows", state.suppressWhenPinned)
        debugLoggingCheckBox = JBCheckBox("Enable debug logging", state.debugLogging)

        return FormBuilder.createFormBuilder()
            .addComponent(enabledCheckBox!!)
            .addLabeledComponent(JBLabel("Hover activation delay (ms):"), hoverDelaySpinner!!)
            .addLabeledComponent(JBLabel("Auto-hide delay (ms):"), autoHideDelaySpinner!!)
            .addComponent(onlyWhenEditorFocusedCheckBox!!)
            .addComponent(suppressWhenPinnedCheckBox!!)
            .addSeparator()
            .addComponent(JBLabel("Advanced:"))
            .addComponent(debugLoggingCheckBox!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val state = CompactUISettings.getInstance().state
        return enabledCheckBox?.isSelected != state.enabled ||
                hoverDelaySpinner?.value as? Int != state.hoverActivationDelayMs ||
                autoHideDelaySpinner?.value as? Int != state.autoHideDelayMs ||
                onlyWhenEditorFocusedCheckBox?.isSelected != state.onlyWhenEditorFocused ||
                suppressWhenPinnedCheckBox?.isSelected != state.suppressWhenPinned ||
                debugLoggingCheckBox?.isSelected != state.debugLogging
    }

    override fun apply() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state
        
        state.enabled = enabledCheckBox?.isSelected ?: false
        state.hoverActivationDelayMs = hoverDelaySpinner?.value as? Int ?: 150
        state.autoHideDelayMs = autoHideDelaySpinner?.value as? Int ?: 500
        state.onlyWhenEditorFocused = onlyWhenEditorFocusedCheckBox?.isSelected ?: true
        state.suppressWhenPinned = suppressWhenPinnedCheckBox?.isSelected ?: true
        state.debugLogging = debugLoggingCheckBox?.isSelected ?: false

        // Notify all open projects to refresh behavior
        ApplicationManager.getApplication().messageBus.syncPublisher(CompactUISettingsListener.TOPIC).settingsChanged()
    }

    override fun reset() {
        val state = CompactUISettings.getInstance().state
        enabledCheckBox?.isSelected = state.enabled
        hoverDelaySpinner?.value = state.hoverActivationDelayMs
        autoHideDelaySpinner?.value = state.autoHideDelayMs
        onlyWhenEditorFocusedCheckBox?.isSelected = state.onlyWhenEditorFocused
        suppressWhenPinnedCheckBox?.isSelected = state.suppressWhenPinned
        debugLoggingCheckBox?.isSelected = state.debugLogging
    }
}
