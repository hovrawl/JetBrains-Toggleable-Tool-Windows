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

A productivity plugin that makes JetBrains tool windows feel fast and minimal:
- Toggleable Islands: three actions to hide/reopen the last set of tool windows per island (left, right, bottom)
- Compact UI Mode: optional floating, hover-timed tool windows that auto-hide after a delay
- Immersive Top Bar: optional auto-hide for the main toolbar/navigation bar with a slim reveal zone and edge padding


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

The plugin offers a Compact UI mode that enables tool windows to appear as transient floating overlays when you show them via the toggle actions, and automatically hide when you move the mouse away.

Features
- Hover to Show (via actions): windows appear after a configurable delay when requested
- Auto-Hide: windows disappear after a configurable delay when you move the mouse away
- Floating Presentation: windows appear as overlays that don't affect editor layout
- Seamless Integration: works alongside the island toggle actions

Settings (Settings/Preferences > Tools > Compact UI)
- Enable Compact UI
- Hover activation delay (ms)
- Auto-hide delay (ms)
- Only hide when editor refocuses
- Suppress floating for pinned tool windows
- Enable debug logging

## Immersive Top Bar

An optional auto-hide for the IDE's main toolbar and navigation bar with a minimalist chrome effect.

How it works
- When enabled, the toolbar (and optionally the navigation bar) is hidden by default
- Move the mouse to the top edge (reveal zone) to show it temporarily
- It auto-hides after a configurable delay when you move away
- Optional edge padding can be applied for a cleaner look

Settings (same Compact UI page)
- Enable Auto-hide Top Bar
- Reveal zone height
- Hide delay
- Edge padding (+ apply to sides and bottom)
- Hide navigation bar too
- Enable debug logging

For manual testing tips, see TESTING.md.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:
  
  Settings/Preferences > Plugins > Marketplace > Search for "JetBrains-Toggleable-Tool-Windows" > Install
  
- Using JetBrains Marketplace:

  Go to JetBrains Marketplace and install it by clicking the Install to ... button in case your IDE is running.

  You can also download the latest release from JetBrains Marketplace and install it manually using
  Settings/Preferences > Plugins > ⚙️ > Install plugin from disk...

- Manually:

  Download the latest release from GitHub Releases and install it manually using
  Settings/Preferences > Plugins > ⚙️ > Install plugin from disk...

---
Plugin based on the IntelliJ Platform Plugin Template.
