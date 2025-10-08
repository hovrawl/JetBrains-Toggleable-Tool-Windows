package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CompactUISettingsTest : BasePlatformTestCase() {

    fun testDefaultSettings() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        // Verify default values
        assertFalse("Compact UI should be disabled by default", state.enabled)
        assertEquals("Default hover delay should be 150ms", 150, state.hoverActivationDelayMs)
        assertEquals("Default auto-hide delay should be 500ms", 500, state.autoHideDelayMs)
        assertTrue("Only when editor focused should be enabled by default", state.onlyWhenEditorFocused)
        assertTrue("Suppress when pinned should be enabled by default", state.suppressWhenPinned)
        assertFalse("Debug logging should be disabled by default", state.debugLogging)
    }

    fun testSettingsModification() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        // Modify settings
        state.enabled = true
        state.hoverActivationDelayMs = 200
        state.autoHideDelayMs = 600
        state.onlyWhenEditorFocused = false
        state.suppressWhenPinned = false
        state.debugLogging = true

        // Verify modifications
        assertTrue("Compact UI should be enabled", state.enabled)
        assertEquals("Hover delay should be 200ms", 200, state.hoverActivationDelayMs)
        assertEquals("Auto-hide delay should be 600ms", 600, state.autoHideDelayMs)
        assertFalse("Only when editor focused should be disabled", state.onlyWhenEditorFocused)
        assertFalse("Suppress when pinned should be disabled", state.suppressWhenPinned)
        assertTrue("Debug logging should be enabled", state.debugLogging)

        // Reset to defaults for other tests
        state.enabled = false
        state.hoverActivationDelayMs = 150
        state.autoHideDelayMs = 500
        state.onlyWhenEditorFocused = true
        state.suppressWhenPinned = true
        state.debugLogging = false
    }

    fun testCompactUIControllerCreation() {
        // Verify that the controller can be instantiated
        val controller = CompactUIController.getInstance(project)
        assertNotNull("Controller should be created", controller)

        // Verify it respects the enabled state
        val settings = CompactUISettings.getInstance()
        settings.state.enabled = false
        assertFalse("Controller should report Compact UI as disabled", controller.isCompactEnabled())

        settings.state.enabled = true
        assertTrue("Controller should report Compact UI as enabled", controller.isCompactEnabled())

        // Reset
        settings.state.enabled = false
    }
}
