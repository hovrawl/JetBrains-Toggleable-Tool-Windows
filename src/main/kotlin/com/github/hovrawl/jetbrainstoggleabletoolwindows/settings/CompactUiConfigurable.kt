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

    // Immersive Top Bar controls
    private var enableImmersiveTopBarCheckBox: JBCheckBox? = null
    private var revealZoneHeightSpinner: JSpinner? = null
    private var hideDelaySpinner: JSpinner? = null
    private var edgePaddingSpinner: JSpinner? = null
    private var applySidesAndBottomCheckBox: JBCheckBox? = null
    private var hideNavigationBarCheckBox: JBCheckBox? = null

    override fun getId(): String = "compact.ui.settings"

    override fun getDisplayName(): String = "Compact UI"

    override fun createComponent(): JComponent {
        val state = CompactUISettings.getInstance().state

        enabledCheckBox = JBCheckBox("Enable Compact UI", state.enabled)

        hoverDelaySpinner = JSpinner(SpinnerNumberModel(state.hoverActivationDelayMs, 0, 5000, 50))
        autoHideDelaySpinner = JSpinner(SpinnerNumberModel(state.autoHideDelayMs, 0, 5000, 50))

        onlyWhenEditorFocusedCheckBox = JBCheckBox("Only hide when editor refocuses", state.onlyWhenEditorFocused)
        suppressWhenPinnedCheckBox = JBCheckBox("Suppress floating for pinned tool windows", state.suppressWhenPinned)
        debugLoggingCheckBox = JBCheckBox("Enable debug logging", state.debugLogging)

        // Immersive Top Bar section
        enableImmersiveTopBarCheckBox = JBCheckBox("Enable Auto-hide Top Bar", state.enableAutoHideTopBar)
        revealZoneHeightSpinner = JSpinner(SpinnerNumberModel(state.revealZoneHeight, 2, 24, 1))
        hideDelaySpinner = JSpinner(SpinnerNumberModel(state.hideDelay, 0, 5000, 50))
        edgePaddingSpinner = JSpinner(SpinnerNumberModel(state.edgePadding, 0, 32, 1))
        applySidesAndBottomCheckBox = JBCheckBox("Apply padding to sides and bottom", state.applySidesAndBottom)
        hideNavigationBarCheckBox = JBCheckBox("Hide navigation bar too", state.hideNavigationBar)

        return FormBuilder.createFormBuilder()
            // Compact UI
            .addComponent(enabledCheckBox!!)
            .addLabeledComponent(JBLabel("Hover activation delay (ms):"), hoverDelaySpinner!!)
            .addLabeledComponent(JBLabel("Auto-hide delay (ms):"), autoHideDelaySpinner!!)
            .addComponent(onlyWhenEditorFocusedCheckBox!!)
            .addComponent(suppressWhenPinnedCheckBox!!)
            .addSeparator()
            // Immersive Top Bar
            .addComponent(JBLabel("Immersive Top Bar:"))
            .addComponent(enableImmersiveTopBarCheckBox!!)
            .addLabeledComponent(JBLabel("Reveal zone height (px):"), revealZoneHeightSpinner!!)
            .addLabeledComponent(JBLabel("Hide delay (ms):"), hideDelaySpinner!!)
            .addLabeledComponent(JBLabel("Edge padding (px):"), edgePaddingSpinner!!)
            .addComponent(applySidesAndBottomCheckBox!!)
            .addComponent(hideNavigationBarCheckBox!!)
            .addSeparator()
            .addComponent(JBLabel("Advanced:"))
            .addComponent(debugLoggingCheckBox!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }

    override fun isModified(): Boolean {
        val state = CompactUISettings.getInstance().state
        return enabledCheckBox?.isSelected != state.enabled ||
                (hoverDelaySpinner?.value as? Int) != state.hoverActivationDelayMs ||
                (autoHideDelaySpinner?.value as? Int) != state.autoHideDelayMs ||
                onlyWhenEditorFocusedCheckBox?.isSelected != state.onlyWhenEditorFocused ||
                suppressWhenPinnedCheckBox?.isSelected != state.suppressWhenPinned ||
                debugLoggingCheckBox?.isSelected != state.debugLogging ||
                // Immersive Top Bar comparisons
                enableImmersiveTopBarCheckBox?.isSelected != state.enableAutoHideTopBar ||
                (revealZoneHeightSpinner?.value as? Int) != state.revealZoneHeight ||
                (hideDelaySpinner?.value as? Int) != state.hideDelay ||
                (edgePaddingSpinner?.value as? Int) != state.edgePadding ||
                applySidesAndBottomCheckBox?.isSelected != state.applySidesAndBottom ||
                hideNavigationBarCheckBox?.isSelected != state.hideNavigationBar
    }

    override fun apply() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        // Compact UI
        state.enabled = enabledCheckBox?.isSelected ?: false
        state.hoverActivationDelayMs = (hoverDelaySpinner?.value as? Int) ?: 150
        state.autoHideDelayMs = (autoHideDelaySpinner?.value as? Int) ?: 500
        state.onlyWhenEditorFocused = onlyWhenEditorFocusedCheckBox?.isSelected ?: true
        state.suppressWhenPinned = suppressWhenPinnedCheckBox?.isSelected ?: true
        state.debugLogging = debugLoggingCheckBox?.isSelected ?: false

        // Immersive Top Bar
        state.enableAutoHideTopBar = enableImmersiveTopBarCheckBox?.isSelected ?: false
        state.revealZoneHeight = (revealZoneHeightSpinner?.value as? Int) ?: 4
        state.hideDelay = (hideDelaySpinner?.value as? Int) ?: 700
        state.edgePadding = (edgePaddingSpinner?.value as? Int) ?: 4
        state.applySidesAndBottom = applySidesAndBottomCheckBox?.isSelected ?: true
        state.hideNavigationBar = hideNavigationBarCheckBox?.isSelected ?: true

        // Notify all listeners (controllers/managers) to refresh behavior
        ApplicationManager.getApplication().messageBus.syncPublisher(CompactUISettingsListener.TOPIC).settingsChanged()
    }

    override fun reset() {
        val state = CompactUISettings.getInstance().state
        // Compact UI
        enabledCheckBox?.isSelected = state.enabled
        hoverDelaySpinner?.value = state.hoverActivationDelayMs
        autoHideDelaySpinner?.value = state.autoHideDelayMs
        onlyWhenEditorFocusedCheckBox?.isSelected = state.onlyWhenEditorFocused
        suppressWhenPinnedCheckBox?.isSelected = state.suppressWhenPinned
        debugLoggingCheckBox?.isSelected = state.debugLogging
        // Immersive Top Bar
        enableImmersiveTopBarCheckBox?.isSelected = state.enableAutoHideTopBar
        revealZoneHeightSpinner?.value = state.revealZoneHeight
        hideDelaySpinner?.value = state.hideDelay
        edgePaddingSpinner?.value = state.edgePadding
        applySidesAndBottomCheckBox?.isSelected = state.applySidesAndBottom
        hideNavigationBarCheckBox?.isSelected = state.hideNavigationBar
    }
}
