package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import java.awt.Insets

class CompactUIConfigurable : SearchableConfigurable {
    private var panel: JPanel? = null
    private var enabledCheckbox: JCheckBox? = null
    private var hoverDelaySpinner: JSpinner? = null
    private var autoHideDelaySpinner: JSpinner? = null
    private var onlyWhenEditorFocusedCheckbox: JCheckBox? = null
    private var suppressWhenPinnedCheckbox: JCheckBox? = null
    private var debugLoggingCheckbox: JCheckBox? = null

    override fun getId(): String = "com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUIConfigurable"

    override fun getDisplayName(): String = "Compact UI"

    override fun createComponent(): JComponent {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = Insets(5, 5, 5, 5)

        var row = 0

        // Enabled checkbox
        enabledCheckbox = JCheckBox("Enable Compact UI Mode", state.enabled)
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 2
        panel!!.add(enabledCheckbox!!, gbc)

        gbc.gridwidth = 1

        // Hover activation delay
        gbc.gridx = 0
        gbc.gridy = row
        panel!!.add(JLabel("Hover activation delay (ms):"), gbc)

        hoverDelaySpinner = JSpinner(SpinnerNumberModel(state.hoverActivationDelayMs, 0, 5000, 50))
        gbc.gridx = 1
        panel!!.add(hoverDelaySpinner!!, gbc)
        row++

        // Auto-hide delay
        gbc.gridx = 0
        gbc.gridy = row
        panel!!.add(JLabel("Auto-hide delay (ms):"), gbc)

        autoHideDelaySpinner = JSpinner(SpinnerNumberModel(state.autoHideDelayMs, 0, 5000, 50))
        gbc.gridx = 1
        panel!!.add(autoHideDelaySpinner!!, gbc)
        row++

        // Only when editor focused
        onlyWhenEditorFocusedCheckbox = JCheckBox("Only when editor focused", state.onlyWhenEditorFocused)
        gbc.gridx = 0
        gbc.gridy = row++
        gbc.gridwidth = 2
        panel!!.add(onlyWhenEditorFocusedCheckbox!!, gbc)

        // Suppress when pinned
        suppressWhenPinnedCheckbox = JCheckBox("Suppress when pinned", state.suppressWhenPinned)
        gbc.gridx = 0
        gbc.gridy = row++
        panel!!.add(suppressWhenPinnedCheckbox!!, gbc)

        // Debug logging
        debugLoggingCheckbox = JCheckBox("Enable debug logging", state.debugLogging)
        gbc.gridx = 0
        gbc.gridy = row++
        panel!!.add(debugLoggingCheckbox!!, gbc)

        // Add filler at the bottom to push everything to the top
        gbc.gridx = 0
        gbc.gridy = row
        gbc.gridwidth = 2
        gbc.weighty = 1.0
        panel!!.add(JPanel(), gbc)

        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        return enabledCheckbox?.isSelected != state.enabled ||
                (hoverDelaySpinner?.value as? Int) != state.hoverActivationDelayMs ||
                (autoHideDelaySpinner?.value as? Int) != state.autoHideDelayMs ||
                onlyWhenEditorFocusedCheckbox?.isSelected != state.onlyWhenEditorFocused ||
                suppressWhenPinnedCheckbox?.isSelected != state.suppressWhenPinned ||
                debugLoggingCheckbox?.isSelected != state.debugLogging
    }

    override fun apply() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        val wasEnabled = state.enabled
        
        state.enabled = enabledCheckbox?.isSelected ?: false
        state.hoverActivationDelayMs = (hoverDelaySpinner?.value as? Int) ?: 150
        state.autoHideDelayMs = (autoHideDelaySpinner?.value as? Int) ?: 500
        state.onlyWhenEditorFocused = onlyWhenEditorFocusedCheckbox?.isSelected ?: true
        state.suppressWhenPinned = suppressWhenPinnedCheckbox?.isSelected ?: true
        state.debugLogging = debugLoggingCheckbox?.isSelected ?: false

        // If Compact UI was just disabled, notify all controllers to cleanup
        if (wasEnabled && !state.enabled) {
            CompactUIController.notifyAllControllersToCleanup()
        }
    }

    override fun reset() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        enabledCheckbox?.isSelected = state.enabled
        hoverDelaySpinner?.value = state.hoverActivationDelayMs
        autoHideDelaySpinner?.value = state.autoHideDelayMs
        onlyWhenEditorFocusedCheckbox?.isSelected = state.onlyWhenEditorFocused
        suppressWhenPinnedCheckbox?.isSelected = state.suppressWhenPinned
        debugLoggingCheckbox?.isSelected = state.debugLogging
    }

    override fun disposeUIResources() {
        panel = null
        enabledCheckbox = null
        hoverDelaySpinner = null
        autoHideDelaySpinner = null
        onlyWhenEditorFocusedCheckbox = null
        suppressWhenPinnedCheckbox = null
        debugLoggingCheckbox = null
    }
}
