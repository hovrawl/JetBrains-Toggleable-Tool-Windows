package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive.ImmersiveTopBarManager
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.*

class CompactUiConfigurable : Configurable {

    private var enableAutoHideCheckBox: JBCheckBox? = null
    private var revealZoneHeightSpinner: JSpinner? = null
    private var hideDelaySpinner: JSpinner? = null
    private var edgePaddingSpinner: JSpinner? = null
    private var applySidesAndBottomCheckBox: JBCheckBox? = null
    private var hideNavigationBarCheckBox: JBCheckBox? = null
    private var enableAnimationCheckBox: JBCheckBox? = null
    private var debugLoggingCheckBox: JBCheckBox? = null

    private var mainPanel: JPanel? = null

    override fun getDisplayName(): String = "Compact UI"

    override fun createComponent(): JComponent {
        enableAutoHideCheckBox = JBCheckBox("Enable Auto-hide Top Bar")
        revealZoneHeightSpinner = JSpinner(SpinnerNumberModel(4, 2, 24, 1))
        hideDelaySpinner = JSpinner(SpinnerNumberModel(700, 0, 5000, 50))
        edgePaddingSpinner = JSpinner(SpinnerNumberModel(4, 0, 32, 1))
        applySidesAndBottomCheckBox = JBCheckBox("Apply padding to sides and bottom", true)
        hideNavigationBarCheckBox = JBCheckBox("Hide navigation bar too", true)
        enableAnimationCheckBox = JBCheckBox("Enable animation (fade/slide)")
        debugLoggingCheckBox = JBCheckBox("Enable debug logging")

        // Disable animation checkbox (future feature)
        enableAnimationCheckBox?.isEnabled = false

        val formBuilder = FormBuilder.createFormBuilder()
            .addComponent(enableAutoHideCheckBox!!)
            .addLabeledComponent(JBLabel("Reveal zone height (px):"), revealZoneHeightSpinner!!)
            .addLabeledComponent(JBLabel("Hide delay (ms):"), hideDelaySpinner!!)
            .addLabeledComponent(JBLabel("Edge padding (px):"), edgePaddingSpinner!!)
            .addComponent(applySidesAndBottomCheckBox!!)
            .addComponent(hideNavigationBarCheckBox!!)
            .addComponent(enableAnimationCheckBox!!)
            .addSeparator()
            .addComponent(debugLoggingCheckBox!!)
            .addComponentFillVertically(JPanel(), 0)

        mainPanel = JPanel(BorderLayout())
        mainPanel?.add(formBuilder.panel, BorderLayout.NORTH)

        // Add listener to enable/disable dependent components
        enableAutoHideCheckBox?.addActionListener {
            updateComponentStates()
        }

        return mainPanel!!
    }

    private fun updateComponentStates() {
        val enabled = enableAutoHideCheckBox?.isSelected == true
        revealZoneHeightSpinner?.isEnabled = enabled
        hideDelaySpinner?.isEnabled = enabled
        edgePaddingSpinner?.isEnabled = enabled
        applySidesAndBottomCheckBox?.isEnabled = enabled
        hideNavigationBarCheckBox?.isEnabled = enabled
        // Animation checkbox stays disabled
    }

    override fun isModified(): Boolean {
        val settings = CompactUiSettings.getInstance().state
        return enableAutoHideCheckBox?.isSelected != settings.enableAutoHideTopBar ||
                (revealZoneHeightSpinner?.value as? Int) != settings.revealZoneHeight ||
                (hideDelaySpinner?.value as? Int) != settings.hideDelay ||
                (edgePaddingSpinner?.value as? Int) != settings.edgePadding ||
                applySidesAndBottomCheckBox?.isSelected != settings.applySidesAndBottom ||
                hideNavigationBarCheckBox?.isSelected != settings.hideNavigationBar ||
                enableAnimationCheckBox?.isSelected != settings.enableAnimation ||
                debugLoggingCheckBox?.isSelected != settings.debugLogging
    }

    override fun apply() {
        val settings = CompactUiSettings.getInstance()
        settings.state.enableAutoHideTopBar = enableAutoHideCheckBox?.isSelected ?: false
        settings.state.revealZoneHeight = revealZoneHeightSpinner?.value as? Int ?: 4
        settings.state.hideDelay = hideDelaySpinner?.value as? Int ?: 700
        settings.state.edgePadding = edgePaddingSpinner?.value as? Int ?: 4
        settings.state.applySidesAndBottom = applySidesAndBottomCheckBox?.isSelected ?: true
        settings.state.hideNavigationBar = hideNavigationBarCheckBox?.isSelected ?: true
        settings.state.enableAnimation = enableAnimationCheckBox?.isSelected ?: false
        settings.state.debugLogging = debugLoggingCheckBox?.isSelected ?: false

        // Notify manager of settings change
        ImmersiveTopBarManager.getInstance().onSettingsChanged()
    }

    override fun reset() {
        val settings = CompactUiSettings.getInstance().state
        enableAutoHideCheckBox?.isSelected = settings.enableAutoHideTopBar
        revealZoneHeightSpinner?.value = settings.revealZoneHeight
        hideDelaySpinner?.value = settings.hideDelay
        edgePaddingSpinner?.value = settings.edgePadding
        applySidesAndBottomCheckBox?.isSelected = settings.applySidesAndBottom
        hideNavigationBarCheckBox?.isSelected = settings.hideNavigationBar
        enableAnimationCheckBox?.isSelected = settings.enableAnimation
        debugLoggingCheckBox?.isSelected = settings.debugLogging
        updateComponentStates()
    }

    override fun disposeUIResources() {
        mainPanel = null
        enableAutoHideCheckBox = null
        revealZoneHeightSpinner = null
        hideDelaySpinner = null
        edgePaddingSpinner = null
        applySidesAndBottomCheckBox = null
        hideNavigationBarCheckBox = null
        enableAnimationCheckBox = null
        debugLoggingCheckBox = null
    }
}
