# JetBrains-Toggleable-Tool-Windows

![Build](https://github.com/hovrawl/JetBrains-Toggleable-Tool-Windows/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties) and [pluginName](./gradle.properties), as well as the [id](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [x] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [x] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `MARKETPLACE_ID` in the above README badges. You can obtain it once the plugin is published to JetBrains Marketplace.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.
- [ ] Configure the [CODECOV_TOKEN](https://docs.codecov.com/docs/quick-start) secret for automated test coverage reports on PRs

## Plugin-specific Implementation Tasks: Toggleable Tool Windows

- [ ] Define per-project state storage for last-remembered tool window IDs per island (left/right/bottom):
  - [ ] Create a ProjectService implementing PersistentStateComponent with fields like lastLeftId, lastRightId, lastBottomId.
  - [ ] Validate/clear IDs on project open; ensure they correspond to existing tool windows.
- [ ] Implement core toggle logic shared across islands:
  - [ ] Create an abstract ToggleIslandAction (AnAction) that determines current active tool window and its island (via ToolWindowManager.getActiveToolWindowId and ToolWindow.anchor).
  - [ ] If the active tool window is on this action’s island: hide it (toolWindow.hide(null)) and remember its ID in the ProjectService.
  - [ ] If none is active on this island: fetch remembered ID from ProjectService; if valid, activate/show it (toolWindow.activate(null, true) or show(null)).
  - [ ] Update remembered ID when a different tool window on the same island becomes active before hiding.
  - [ ] Handle floating/detached windows as belonging to their original island.
- [ ] Provide concrete actions for each island:
  - [ ] ToggleLeftIslandToolWindow
  - [ ] ToggleRightIslandToolWindow
  - [ ] ToggleBottomIslandToolWindow
- [ ] Register actions in plugin.xml with stable action IDs and presentation:
  - [ ] Add three <action> entries with text/description and group them appropriately for discoverability.
  - [ ] Optionally create a Keymap group to suggest bindings.
- [ ] Edge cases and robustness:
  - [ ] If remembered ID is invalid or tool window is unavailable (ToolWindowManager.getToolWindow(id) == null), skip safely.
  - [ ] When first invoked with no remembered ID and no active window on that island, do nothing.
  - [ ] Resolve focus ambiguities by relying on ToolWindowManager.getActiveToolWindowId; fall back to PlatformDataKeys.TOOL_WINDOW.
- [ ] Tests (optional but recommended):
  - [ ] Add BasePlatformTestCase tests to simulate action execution and assert remembered IDs and visibility changes where feasible.
- [ ] Documentation and usage:
  - [ ] Update README and plugin description to clearly state behavior and provide suggested keybindings (e.g., Alt+1/Alt+2/Alt+3 if not conflicting).
  - [ ] Add a short “How to bind keys” section: Settings/Preferences > Keymap, search by action name.
- [ ] Manual verification:
  - [ ] runIde and verify toggling for Project/Structure (left), Commit/TODO (right), Run/Debug/Terminal (bottom) behaves as expected.

<!-- Plugin description -->
Toggleable Islands adds three actions that let you quickly hide and re‑open the last active tool window on each island (left, right, bottom).

How it works:
- Each island has a dedicated toggle action: Left, Right, Bottom.
- If a tool window on that island is currently active, invoking the action hides it and remembers it as the last active for that island.
- If no tool window on that island is active, invoking the action re‑opens the last remembered tool window (if available).
- If you activate a different tool window on the same island, the remembered ID updates accordingly.

Notes:
- Works with common tool windows such as Project/Structure/Services (left), Commit/TODO/Problems (right), and Run/Debug/Terminal/Build (bottom).
- Floating/detached tool windows are treated as belonging to their original island.
- Suggested keybindings (you can customize in Keymap): Alt+1 (Left), Alt+2 (Right), Alt+3 (Bottom), adjusting to avoid conflicts.

## Compact UI Mode

The plugin offers a **Compact UI** mode that enables tool windows to appear as transient floating overlays when you hover over their stripe icons, and automatically hide when you move the mouse away. This provides a more streamlined workspace while keeping quick access to tool windows.

### Features

- **Hover to Show**: Tool windows appear automatically when you hover over their icons in the tool window stripes (left, right, bottom)
- **Auto-Hide**: Windows disappear after a configurable delay when you move the mouse away
- **Floating Presentation**: Windows appear as floating overlays that don't affect your editor layout
- **Seamless Integration**: Works alongside the existing island toggle actions

### Settings

Access Compact UI settings via **Settings/Preferences > Tools > Compact UI**:

- **Enable Compact UI**: Toggle the feature on/off (default: off)
- **Hover activation delay (ms)**: How long to wait before showing a window on hover (default: 150ms)
- **Auto-hide delay (ms)**: How long to wait before hiding a window after mouse exit (default: 500ms)
- **Only hide when editor refocuses**: Additional condition for auto-hiding (default: enabled)
- **Suppress floating for pinned tool windows**: Whether to exclude pinned windows from Compact UI behavior (default: enabled)
- **Enable debug logging**: Show detailed logs for troubleshooting (default: disabled)

### How It Works with Toggle Actions

When Compact UI is enabled:
- The toggle actions (Alt+1/2/3 or your custom bindings) still work to show/hide tool windows
- Showing a window uses the hover delay and floating presentation
- Hiding windows triggers the auto-hide mechanism
- When you disable Compact UI, all windows are restored to their original presentation and the normal toggle behavior resumes

### Manual Verification

To verify Compact UI is working correctly:
1. Enable Compact UI in settings
2. Hover over a tool window icon - it should appear after the activation delay
3. Move mouse away - it should hide after the auto-hide delay
4. Toggle the feature off - windows should restore to normal behavior instantly
5. If suppressWhenPinned is enabled, pinned windows should be ignored
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "JetBrains-Toggleable-Tool-Windows"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/hovrawl/JetBrains-Toggleable-Tool-Windows/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
