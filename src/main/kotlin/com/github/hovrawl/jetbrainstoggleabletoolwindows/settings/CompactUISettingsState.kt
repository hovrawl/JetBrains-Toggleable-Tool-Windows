package com.github.hovrawl.jetbrainstoggleabletoolwindows.settings

data class CompactUISettingsState(
    // Compact UI (floating tool windows)
    var enabled: Boolean = false,
    var hoverActivationDelayMs: Int = 150,
    var autoHideDelayMs: Int = 500,
    var onlyWhenEditorFocused: Boolean = true,
    var suppressWhenPinned: Boolean = true,

    // Immersive Top Bar settings
    var enableAutoHideTopBar: Boolean = false,
    var revealZoneHeight: Int = 4,
    var hideDelay: Int = 700,
    var edgePadding: Int = 4,
    var applySidesAndBottom: Boolean = true,
    var hideNavigationBar: Boolean = true,
    var enableAnimation: Boolean = false,

    // Shared
    var debugLogging: Boolean = false
)
