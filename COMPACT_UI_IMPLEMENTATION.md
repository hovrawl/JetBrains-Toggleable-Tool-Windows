# Compact UI Implementation Guide

## Overview

This document describes the implementation of the Compact UI feature for the JetBrains Toggleable Tool Windows plugin.

## Architecture

### Components

1. **Settings Layer** (`com.github.hovrawl.jetbrainstoggleabletoolwindows.settings`)
   - `CompactUISettingsState`: Data class holding configuration
   - `CompactUISettings`: Application-level PersistentStateComponent service
   - `CompactUIConfigurable`: Settings UI page
   - `CompactUISettingsListener`: Message bus topic for settings changes

2. **Controller Layer** (`com.github.hovrawl.jetbrainstoggleabletoolwindows.controller`)
   - `CompactUIController`: Project-level service managing tool window behavior
   - `StripeHoverDetector`: Helper for detecting hover on stripe icons

3. **Integration Layer** (`com.github.hovrawl.jetbrainstoggleabletoolwindows.actions`)
   - Modified `ToggleIslandAction` to delegate to CompactUIController when enabled

## Key Design Decisions

### 1. Application-level vs Project-level Settings

**Decision**: Settings are application-level (stored in `compact-ui-settings.xml`)

**Rationale**: The Compact UI behavior is a user preference that should apply consistently across all projects. Users typically want the same UX preferences in all their projects.

### 2. Project-level Controller

**Decision**: Controller is a project-level service

**Rationale**: Each project needs its own controller to:
- Maintain separate state for floating windows
- Listen to project-specific tool window events
- Clean up properly when project closes

### 3. Timer-based Show/Hide

**Decision**: Use IntelliJ's `Alarm` with configurable delays

**Rationale**: 
- Provides EDT-safe scheduling
- Automatic cleanup on disposal
- Configurable delays allow users to tune for their preference
- Natural feel with activation/hide delays

### 4. Floating Window Type Strategy

**Decision**: Store original `ToolWindowType` and temporarily change to `FLOATING`

**Rationale**:
- Allows restoration to original state when feature is disabled
- Provides the transient overlay UX
- Non-destructive modification

### 5. Eligibility Check

**Decision**: Check `toolWindow.isAutoHide` for pinned status

**Rationale**:
- `isAutoHide = true` → unpinned (auto-hide mode)
- `isAutoHide = false` → pinned
- When `suppressWhenPinned = true`, exclude pinned windows

### 6. Stripe Hover Detection

**Decision**: Provide infrastructure but use toggle actions as primary trigger

**Rationale**:
- Stripe components are internal IDE API (may change between versions)
- Graceful fallback ensures feature works even without direct stripe access
- Toggle actions provide reliable trigger mechanism
- Future enhancement can implement full hover detection when API stabilizes

## Data Flow

### Enabling a Tool Window in Compact UI Mode

```
User triggers toggle action
  ↓
ToggleIslandAction.actionPerformed()
  ↓
Checks CompactUISettings.state.enabled
  ↓
If enabled → handleCompactUIMode()
  ↓
CompactUIController.requestShow(id, trigger)
  ↓
Check eligibility (available, not pinned if suppressWhenPinned)
  ↓
Schedule show with Alarm (hoverActivationDelayMs)
  ↓
After delay → performShow()
  ↓
Store original ToolWindowType
  ↓
Set type to FLOATING
  ↓
toolWindow.show()
  ↓
Install mouse listener on window
```

### Auto-hide Behavior

```
Mouse exits tool window
  ↓
MouseAdapter.mouseExited()
  ↓
CompactUIController.requestHide(id, "mouse_exit")
  ↓
Schedule hide with Alarm (autoHideDelayMs)
  ↓
After delay → performHide()
  ↓
Restore original ToolWindowType
  ↓
toolWindow.hide()
```

### Disabling Compact UI

```
User unchecks "Enable Compact UI" in settings
  ↓
CompactUIConfigurable.apply()
  ↓
Publishes CompactUISettingsListener message
  ↓
CompactUIController.handleSettingsChanged()
  ↓
Detects enabled = false
  ↓
forceHideAll() + restoreAllWindowTypes()
  ↓
All floating windows restored to original state
```

## State Management

### WindowState Map

```kotlin
private val windowStates = mutableMapOf<String, WindowState>()

data class WindowState(
    val originalType: ToolWindowType,
    var isFloatingByCompactUI: Boolean = false
)
```

**Purpose**: Track which windows we're managing and their original types for restoration

### Timer Maps

```kotlin
private val showRequests = mutableMapOf<String, Alarm.Request>()
private val hideRequests = mutableMapOf<String, Alarm.Request>()
```

**Purpose**: Track pending show/hide operations to allow cancellation when user changes mind (e.g., mouse re-enters)

### Mouse Listener Map

```kotlin
private val mouseListeners = mutableMapOf<Component, MouseAdapter>()
```

**Purpose**: Track installed listeners for proper cleanup on disposal

## Cleanup Strategy

### On Project Close
1. Cancel all pending show/hide requests
2. Remove all mouse listeners
3. Restore all window types to original state
4. Clear all maps

### On Settings Disable
1. Call `forceHideAll()` to hide all managed windows
2. Restore window types
3. Clear state

## Testing Strategy

### Unit Tests
- `CompactUISettingsTest`: Verify settings defaults and modification

### Manual Testing Checklist
1. Enable Compact UI → verify settings persist
2. Trigger toggle action → verify window appears as floating after delay
3. Move mouse away → verify auto-hide after delay
4. Move mouse back → verify hide is cancelled
5. Disable Compact UI → verify windows restore to normal
6. Pin a window → verify it's excluded when suppressWhenPinned is true
7. Enable debug logging → verify log messages appear

## Future Enhancements

### Phase 2: Direct Stripe Hover
- Implement `StripeHoverDetector.findStripeButtonComponent()` using reflection or UI search
- Handle internal API changes gracefully
- Provide richer hover experience

### Phase 3: Per-Tool Customization
- Allow users to exclude specific tool windows
- Per-tool delay overrides
- Requires UI for per-tool settings

### Phase 4: Advanced Focus Heuristics
- Smarter detection of editor focus
- Handle complex multi-window scenarios
- Consider multi-monitor setups

## Known Limitations

1. **Stripe Hover**: Currently uses toggle actions as primary trigger
2. **Focus Detection**: Basic mouse-based detection only
3. **No Per-Tool Overrides**: All eligible windows treated the same
4. **Single Anchor Support**: No cross-anchor window management

## Dependencies

- IntelliJ Platform SDK (Tool Window Manager APIs)
- Kotlin Standard Library
- IntelliJ UI components (JBCheckBox, FormBuilder, etc.)

## File Structure

```
src/main/kotlin/.../
  settings/
    CompactUISettingsState.kt       # Configuration data class
    CompactUISettings.kt            # Persistent state component
    CompactUIConfigurable.kt        # Settings UI
    CompactUISettingsListener.kt    # Message bus topic
  controller/
    CompactUIController.kt          # Main controller logic
    StripeHoverDetector.kt          # Hover detection helper
  actions/
    ToggleStripeAction.kt           # Modified to integrate with controller

src/test/kotlin/.../
  CompactUISettingsTest.kt          # Settings unit tests
```

## Configuration Schema

```xml
<!-- compact-ui-settings.xml -->
<application>
  <component name="CompactUISettings">
    <option name="enabled" value="false" />
    <option name="hoverActivationDelayMs" value="150" />
    <option name="autoHideDelayMs" value="500" />
    <option name="onlyWhenEditorFocused" value="true" />
    <option name="suppressWhenPinned" value="true" />
    <option name="debugLogging" value="false" />
  </component>
</application>
```

## Extension Points

The implementation provides extension points for future customization:

1. **CompactUISettingsListener**: Other components can listen for settings changes
2. **CompactUIController API**: Public methods for programmatic control
3. **StripeHoverDetector**: Can be enhanced with actual stripe component access
4. **Eligibility Rules**: Can be extended with custom predicates

## Performance Considerations

1. **Timer Usage**: Alarms are lightweight and EDT-safe
2. **Mouse Listeners**: Minimal overhead, only installed on visible windows
3. **State Maps**: Small memory footprint (only tracks active floating windows)
4. **Settings Access**: Singleton service, no repeated lookups

## Security Considerations

- No sensitive data stored
- No external network access
- Read-only access to tool window components
- Safe reflection usage with exception handling
