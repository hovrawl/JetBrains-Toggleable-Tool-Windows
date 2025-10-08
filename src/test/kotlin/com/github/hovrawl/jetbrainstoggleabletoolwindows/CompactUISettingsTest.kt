package com.github.hovrawl.jetbrainstoggleabletoolwindows

import com.github.hovrawl.jetbrainstoggleabletoolwindows.settings.CompactUISettings
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class CompactUISettingsTest : BasePlatformTestCase() {

    fun testDefaultSettings() {
        val settings = CompactUISettings.getInstance()
        val state = settings.state

        assertFalse("Default enabled should be false", state.enabled)
        assertEquals("Default hover delay should be 150ms", 150, state.hoverActivationDelayMs)
        assertEquals("Default auto-hide delay should be 500ms", 500, state.autoHideDelayMs)
        assertTrue("Default onlyWhenEditorFocused should be true", state.onlyWhenEditorFocused)
        assertTrue("Default suppressWhenPinned should be true", state.suppressWhenPinned)
        assertFalse("Default debugLogging should be false", state.debugLogging)
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

        // Verify changes
        assertTrue("Enabled should be true", state.enabled)
        assertEquals("Hover delay should be 200ms", 200, state.hoverActivationDelayMs)
        assertEquals("Auto-hide delay should be 600ms", 600, state.autoHideDelayMs)
        assertFalse("onlyWhenEditorFocused should be false", state.onlyWhenEditorFocused)
        assertFalse("suppressWhenPinned should be false", state.suppressWhenPinned)
        assertTrue("debugLogging should be true", state.debugLogging)

        // Reset for next tests
        state.enabled = false
        state.hoverActivationDelayMs = 150
        state.autoHideDelayMs = 500
        state.onlyWhenEditorFocused = true
        state.suppressWhenPinned = true
        state.debugLogging = false
    }
}
