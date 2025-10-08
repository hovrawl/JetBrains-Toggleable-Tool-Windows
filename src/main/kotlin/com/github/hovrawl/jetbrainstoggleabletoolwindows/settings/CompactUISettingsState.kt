package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

data class CompactUISettingsState(
    var enabled: Boolean = false,
    var hoverActivationDelayMs: Int = 150,
    var autoHideDelayMs: Int = 500,
    var onlyWhenEditorFocused: Boolean = true,
    var suppressWhenPinned: Boolean = true,
    var debugLogging: Boolean = false
)
