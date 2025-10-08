package com.github.hovrawl.jetbrainstoggleabletoolwindows.immersive

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUiSettings
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.openapi.wm.impl.IdeGlassPaneImpl
import com.intellij.util.Alarm
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.border.EmptyBorder

class ImmersiveTopBarFrameDelegate(
    private val frame: Window,
    private val project: Project,
    private val manager: ImmersiveTopBarManager
) {

    private var revealZone: JComponent? = null
    private var hideAlarm: Alarm? = null
    private var isToolbarVisible = false
    private var previousBorder: javax.swing.border.Border? = null
    private var contentPane: JComponent? = null

    fun enable() {
        manager.log("ENABLED", "Enabling immersive mode for frame")
        
        ApplicationManager.getApplication().invokeLater {
            if (!isInFullscreenOrPresentationMode()) {
                createRevealZone()
                applyEdgePadding()
            }
            hideToolbars()
        }
    }

    fun disable() {
        manager.log("DISABLED", "Disabling immersive mode for frame")
        
        ApplicationManager.getApplication().invokeLater {
            removeRevealZone()
            removeEdgePadding()
            // Don't restore toolbars here - manager will handle that
        }
    }

    fun dispose() {
        manager.log("DISPOSE", "Disposing frame delegate")
        disable()
        hideAlarm?.dispose()
        hideAlarm = null
    }

    private fun isInFullscreenOrPresentationMode(): Boolean {
        // Try to detect fullscreen mode
        val windowManager = WindowManagerEx.getInstanceEx()
        val frameHelper = windowManager.getFrameHelper(project)
        
        // Check if frame is in fullscreen (GraphicsDevice.isFullScreenSupported)
        val isFullscreen = try {
            val device = frame.graphicsConfiguration?.device
            device?.fullScreenWindow == frame
        } catch (e: Exception) {
            false
        }
        
        // Check presentation mode via UISettings
        val isPresentationMode = UISettings.getInstance().presentationMode
        
        return isFullscreen || isPresentationMode
    }

    private fun createRevealZone() {
        removeRevealZone()
        
        // Don't create reveal zone in fullscreen/presentation mode
        if (isInFullscreenOrPresentationMode()) {
            manager.log("REVEAL_ZONE_SKIPPED", "Skipping reveal zone in fullscreen/presentation mode")
            return
        }
        
        val settings = CompactUiSettings.getInstance().state
        val height = settings.revealZoneHeight
        
        // Find glass pane
        val glassPane = findGlassPane() ?: return
        
        // Create transparent reveal zone
        val zone = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                // Transparent, no painting needed
            }
        }
        zone.isOpaque = false
        zone.preferredSize = Dimension(glassPane.width, height)
        zone.bounds = Rectangle(0, 0, glassPane.width, height)
        
        // Add mouse listeners
        val mouseAdapter = object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                manager.log("SHOW_REQUEST", "Mouse entered reveal zone")
                cancelHideTimer()
                showToolbars()
            }
            
            override fun mouseExited(e: MouseEvent) {
                // Schedule hide with delay
                manager.log("HIDE_SCHEDULED", "Mouse exited reveal zone, scheduling hide")
                scheduleHide()
            }
        }
        
        zone.addMouseListener(mouseAdapter)
        zone.addMouseMotionListener(object : java.awt.event.MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                // Cancel hide if mouse is moving in reveal zone
                cancelHideTimer()
            }
        })
        
        // Add to glass pane
        glassPane.add(zone)
        glassPane.revalidate()
        glassPane.repaint()
        
        revealZone = zone
        manager.log("REVEAL_ZONE_CREATED", "Reveal zone created with height=$height")
    }

    private fun removeRevealZone() {
        revealZone?.let { zone ->
            val glassPane = findGlassPane()
            glassPane?.remove(zone)
            glassPane?.revalidate()
            glassPane?.repaint()
            revealZone = null
            manager.log("REVEAL_ZONE_REMOVED", "Reveal zone removed")
        }
    }

    private fun findGlassPane(): JComponent? {
        return when (frame) {
            is JFrame -> {
                val glassPane = frame.glassPane
                if (glassPane is IdeGlassPaneImpl) {
                    glassPane as JComponent
                } else null
            }
            else -> null
        }
    }

    private fun applyEdgePadding() {
        // Don't apply padding in fullscreen/presentation mode
        if (isInFullscreenOrPresentationMode()) {
            manager.log("PADDING_SKIPPED", "Skipping padding in fullscreen/presentation mode")
            return
        }
        
        val settings = CompactUiSettings.getInstance().state
        val padding = settings.edgePadding
        val applySides = settings.applySidesAndBottom
        
        // Find root content pane
        contentPane = when (frame) {
            is JFrame -> frame.contentPane as? JComponent
            else -> null
        }
        
        contentPane?.let { pane ->
            // Store previous border
            previousBorder = pane.border
            
            // Apply new border
            val top = padding
            val left = if (applySides) padding else 0
            val bottom = if (applySides) padding else 0
            val right = if (applySides) padding else 0
            
            pane.border = EmptyBorder(top, left, bottom, right)
            pane.revalidate()
            pane.repaint()
            
            manager.log("PADDING_APPLIED", "Padding applied: $padding px, sides=$applySides")
        }
    }

    private fun removeEdgePadding() {
        contentPane?.let { pane ->
            pane.border = previousBorder
            pane.revalidate()
            pane.repaint()
            previousBorder = null
            manager.log("PADDING_REMOVED", "Edge padding removed")
        }
    }

    private fun showToolbars() {
        if (isToolbarVisible) return
        
        val uiSettings = UISettings.getInstance()
        uiSettings.showMainToolbar = true
        
        val settings = CompactUiSettings.getInstance().state
        if (settings.hideNavigationBar) {
            uiSettings.showNavigationBar = true
        }
        
        uiSettings.fireUISettingsChanged()
        isToolbarVisible = true
        
        manager.log("SHOW_APPLIED", "Toolbars shown")
    }

    private fun hideToolbars() {
        if (!isToolbarVisible) {
            // Force initial hide
            val uiSettings = UISettings.getInstance()
            uiSettings.showMainToolbar = false
            
            val settings = CompactUiSettings.getInstance().state
            if (settings.hideNavigationBar) {
                uiSettings.showNavigationBar = false
            }
            
            uiSettings.fireUISettingsChanged()
            manager.log("HIDE_APPLIED", "Toolbars initially hidden")
        } else {
            val uiSettings = UISettings.getInstance()
            uiSettings.showMainToolbar = false
            
            val settings = CompactUiSettings.getInstance().state
            if (settings.hideNavigationBar) {
                uiSettings.showNavigationBar = false
            }
            
            uiSettings.fireUISettingsChanged()
            isToolbarVisible = false
            
            manager.log("HIDE_APPLIED", "Toolbars hidden")
        }
    }

    private fun scheduleHide() {
        cancelHideTimer()
        
        val settings = CompactUiSettings.getInstance().state
        val delay = settings.hideDelay
        
        if (hideAlarm == null) {
            hideAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD)
        }
        
        hideAlarm?.addRequest({
            hideToolbars()
        }, delay)
        
        manager.log("HIDE_SCHEDULED", "Hide scheduled with delay=$delay ms")
    }

    private fun cancelHideTimer() {
        hideAlarm?.cancelAllRequests()
        manager.log("HIDE_CANCELLED", "Hide timer cancelled")
    }
}
