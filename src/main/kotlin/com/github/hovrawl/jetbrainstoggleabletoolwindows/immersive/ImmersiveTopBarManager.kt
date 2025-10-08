package com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUiSettings
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.wm.WindowManager
import java.awt.Window
import javax.swing.SwingUtilities

@Service(Service.Level.APP)
class ImmersiveTopBarManager : ProjectManagerListener {

    private val frameDelegates = mutableMapOf<Window, ImmersiveTopBarFrameDelegate>()
    private var originalShowMainToolbar: Boolean? = null
    private var originalShowNavigationBar: Boolean? = null
    private var userPreferenceShowMainToolbar: Boolean = true
    private var userPreferenceShowNavigationBar: Boolean = true

    init {
        log("INIT", "ImmersiveTopBarManager initialized")
        ApplicationManager.getApplication().messageBus.connect()
            .subscribe(ProjectManager.TOPIC, this)
    }

    override fun projectOpened(project: Project) {
        log("INIT_FRAME", "Project opened: ${project.name}")
        SwingUtilities.invokeLater {
            val frame = WindowManager.getInstance().getFrame(project) ?: return@invokeLater
            initializeFrameDelegate(frame, project)
        }
    }

    override fun projectClosed(project: Project) {
        log("INIT_FRAME", "Project closed: ${project.name}")
        SwingUtilities.invokeLater {
            val frame = WindowManager.getInstance().getFrame(project) ?: return@invokeLater
            disposeFrameDelegate(frame)
        }
    }

    private fun initializeFrameDelegate(frame: Window, project: Project) {
        if (frameDelegates.containsKey(frame)) return
        
        val delegate = ImmersiveTopBarFrameDelegate(frame, project, this)
        frameDelegates[frame] = delegate
        
        val settings = CompactUiSettings.getInstance().state
        if (settings.enableAutoHideTopBar) {
            delegate.enable()
        }
    }

    private fun disposeFrameDelegate(frame: Window) {
        frameDelegates.remove(frame)?.dispose()
    }

    fun onSettingsChanged() {
        log("SETTINGS_CHANGED", "Settings changed, updating all frames")
        val settings = CompactUiSettings.getInstance().state
        
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
                frameDelegates.values.forEach { it.enable() }
            } else {
                // Disabling feature
                frameDelegates.values.forEach { it.disable() }
                restoreOriginalState()
            }
        }
    }

    fun updateUserPreferences() {
        val uiSettings = UISettings.getInstance()
        userPreferenceShowMainToolbar = uiSettings.showMainToolbar
        userPreferenceShowNavigationBar = uiSettings.showNavigationBar
        log("USER_PREF_UPDATED", "User preferences updated: toolbar=$userPreferenceShowMainToolbar, navbar=$userPreferenceShowNavigationBar")
    }

    private fun restoreOriginalState() {
        if (originalShowMainToolbar == null) return
        
        val uiSettings = UISettings.getInstance()
        uiSettings.showMainToolbar = userPreferenceShowMainToolbar
        if (CompactUiSettings.getInstance().state.hideNavigationBar) {
            uiSettings.showNavigationBar = userPreferenceShowNavigationBar
        }
        uiSettings.fireUISettingsChanged()
        
        log("STATE_RESTORED", "Restored user preferences: toolbar=$userPreferenceShowMainToolbar, navbar=$userPreferenceShowNavigationBar")
        
        // Reset captured state
        originalShowMainToolbar = null
        originalShowNavigationBar = null
    }

    fun log(tag: String, message: String) {
        if (CompactUiSettings.getInstance().state.debugLogging) {
            thisLogger().info("hCompactUi[IMMERSIVE][$tag]: $message")
        }
    }

    companion object {
        fun getInstance(): ImmersiveTopBarManager =
            ApplicationManager.getApplication().getService(ImmersiveTopBarManager::class.java)
    }
}
