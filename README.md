# Toggleable Tool Windows

![Build](https://github.com/hovrawl/JetBrains-Toggleable-Tool-Windows/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

## Overview

Toggleable Tool Windows is an IntelliJ Platform plugin that provides smart toggle actions for tool window islands (left, right, bottom), allowing you to quickly hide and restore your workspace layout with keyboard shortcuts.

## Features

- **Island-based Toggle Actions**: Dedicated toggle actions for Left, Right, and Bottom tool window islands
- **Multi-window Support**: Remember and restore multiple tool windows per island simultaneously
- **Compact UI Mode**: Advanced floating tool window mode with hover activation and auto-hide
- **Persistent State**: Remembers your last active tool windows across IDE sessions

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
<!-- Plugin description end -->

## Compact UI Mode

Compact UI Mode is an advanced feature that transforms tool windows into floating panels with smart hover activation and auto-hide behavior:

### Features
- **Floating Tool Windows**: Tool windows appear as floating panels instead of docked
- **Hover Activation**: Automatically show tool windows when hovering over the stripe area
- **Auto-hide**: Tool windows automatically hide after a configurable delay
- **Editor-focused**: Optional mode to only activate when editor has focus
- **Pinned Window Support**: Option to suppress behavior for pinned tool windows
- **Debug Logging**: Built-in logging for troubleshooting

### Configuration

Access Compact UI settings via: **Settings/Preferences** > **Tools** > **Compact UI**

Available settings:
- **Enable Compact UI Mode**: Toggle the feature on/off
- **Hover activation delay (ms)**: Delay before showing tool windows on hover (default: 150ms)
- **Auto-hide delay (ms)**: Delay before hiding inactive tool windows (default: 500ms)
- **Only when editor focused**: Restrict activation to when editor has focus (default: enabled)
- **Suppress when pinned**: Don't apply Compact UI to pinned tool windows (default: enabled)
- **Enable debug logging**: Log Compact UI events for troubleshooting (default: disabled)

## Usage

### Setting Up Keybindings

1. Open **Settings/Preferences** > **Keymap**
2. Search for "Toggle Left Island", "Toggle Right Island", or "Toggle Bottom Island"
3. Assign keyboard shortcuts (suggested: Alt+1 for Left, Alt+2 for Right, Alt+3 for Bottom)
4. Adjust as needed to avoid conflicts with existing shortcuts

### Basic Workflow

1. Work with your tool windows normally (Project, Terminal, Debug, etc.)
2. Press your assigned shortcut to hide all visible tool windows on that island
3. Press the same shortcut again to restore your previously visible tool windows
4. The plugin remembers which windows were visible per island

### With Compact UI Mode

1. Enable Compact UI Mode in settings
2. Toggle actions now show tool windows as floating panels
3. Tool windows auto-hide based on your configured delay settings
4. Hover behavior is automatically managed by the plugin

## Installation

- Using the IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Toggleable Tool Windows"</kbd> >
  <kbd>Install</kbd>
  
- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/hovrawl/JetBrains-Toggleable-Tool-Windows/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Roadmap

- [ ] Enhance hover detection for Compact UI Mode
- [ ] Add support for top tool window anchor
- [ ] Improve editor focus detection
- [ ] Add animation options for floating windows
- [ ] Support custom window placement in Compact UI Mode

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

## License

This project is licensed under the terms specified in the repository.

## Acknowledgments

- Built with the [IntelliJ Platform Plugin Template][template]
- Inspired by workspace management needs of IntelliJ Platform users

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
