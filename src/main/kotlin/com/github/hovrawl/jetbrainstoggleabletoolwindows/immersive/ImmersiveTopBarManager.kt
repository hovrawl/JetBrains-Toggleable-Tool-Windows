package com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettingsListener
import com.intellij.ide.ui.UISettings
import com.intellij.ide.ui.UISettingsListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import java.awt.Window
import javax.swing.SwingUtilities

@Service(Service.Level.APP)
class ImmersiveTopBarManager {

    private val frameDelegates = mutableMapOf<Window, ImmersiveTopBarFrameDelegate>()
    private var originalShowMainToolbar: Boolean? = null
    private var originalShowNavigationBar: Boolean? = null
    private var userPreferenceShowMainToolbar: Boolean = true
    private var userPreferenceShowNavigationBar: Boolean = true
    private var isTrackingUISettingsChanges = false

    init {
        log("INIT", "ImmersiveTopBarManager initialized")

        // Subscribe to UISettings changes to track user manual changes
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(UISettingsListener.TOPIC, UISettingsListener { uiSettings ->
                onUISettingsChanged(uiSettings)
            })

        // Subscribe to Compact UI settings changes to enable/disable immersive behavior
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(CompactUISettingsListener.TOPIC, object : CompactUISettingsListener {
                override fun settingsChanged() {
                    onSettingsChanged()
                }
            })
    }

    fun initializeForProject(project: Project) {
        log("INIT_FRAME", "Initialize for project: ${project.name}")
        SwingUtilities.invokeLater {
            val frame = WindowManager.getInstance().getFrame(project) ?: return@invokeLater
            initializeFrameDelegate(frame, project)
        }
    }

    private fun onUISettingsChanged(uiSettings: UISettings) {
        // Only track manual changes when feature is enabled and we're not the ones changing it
        if (isTrackingUISettingsChanges && CompactUISettings.getInstance().state.enableAutoHideTopBar) {
            // User manually changed toolbar/navbar settings while feature is active
            // Update our stored preferences
            SwingUtilities.invokeLater {
                userPreferenceShowMainToolbar = uiSettings.showMainToolbar
                userPreferenceShowNavigationBar = uiSettings.showNavigationBar
                log("USER_PREF_UPDATED", "User preferences updated via manual change: toolbar=$userPreferenceShowMainToolbar, navbar=$userPreferenceShowNavigationBar")
            }
        }
    }

    private fun initializeFrameDelegate(frame: Window, project: Project) {
        if (frameDelegates.containsKey(frame)) return
        
        val delegate = ImmersiveTopBarFrameDelegate(frame, project, this)
        frameDelegates[frame] = delegate
        
        val settings = CompactUISettings.getInstance().state
        if (settings.enableAutoHideTopBar) {
            delegate.enable()
        }
    }

    private fun disposeFrameDelegate(frame: Window) {
        frameDelegates.remove(frame)?.dispose()
    }

    fun onSettingsChanged() {
        log("SETTINGS_CHANGED", "Settings changed, updating all frames")
        val settings = CompactUISettings.getInstance().state
        
        SwingUtilities.invokeLater {
            if (settings.enableAutoHideTopBar) {
                // Enabling feature
                if (originalShowMainToolbar == null) {
                    // First time enabling, capture original state
                    val uiSettings = UISettings.getInstance()
                    originalShowMainToolbar = uiSettings.showMainToolbar
                    originalShowNavigationBar = uiSettings.showNavigationBar
                    userPreferenceShowMainToolbar = uiSettings.showMainToolbar
                    userPreferenceShowNavigationBar = uiSettings.showNavigationBar
                    log("STATE_PRESERVED", "Original states: toolbar=$originalShowMainToolbar, navbar=$originalShowNavigationBar")
                }
                isTrackingUISettingsChanges = true
                frameDelegates.values.forEach { it.enable() }
            } else {
                // Disabling feature
                isTrackingUISettingsChanges = false
                frameDelegates.values.forEach { it.disable() }
                restoreOriginalState()
            }
        }
    }

    private fun restoreOriginalState() {
        if (originalShowMainToolbar == null) return
        
        val uiSettings = UISettings.getInstance()
        uiSettings.showMainToolbar = userPreferenceShowMainToolbar
        if (CompactUISettings.getInstance().state.hideNavigationBar) {
            uiSettings.showNavigationBar = userPreferenceShowNavigationBar
        }
        uiSettings.fireUISettingsChanged()
        
        log("STATE_RESTORED", "Restored user preferences: toolbar=$userPreferenceShowMainToolbar, navbar=$userPreferenceShowNavigationBar")
        
        // Reset captured state
        originalShowMainToolbar = null
        originalShowNavigationBar = null
    }

    fun log(tag: String, message: String) {
        if (CompactUISettings.getInstance().state.debugLogging) {
            thisLogger().info("hCompactUi[IMMERSIVE][$tag]: $message")
        }
    }

    companion object {
        fun getInstance(): ImmersiveTopBarManager =
            ApplicationManager.getApplication().getService(ImmersiveTopBarManager::class.java)
    }
}
