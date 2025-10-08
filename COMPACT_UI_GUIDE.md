# Compact UI Feature - User Guide

## Introduction

The Compact UI feature transforms the way tool windows behave in your IntelliJ IDE. Instead of the traditional docked panels, Compact UI mode shows tool windows as lightweight floating panels that can automatically show and hide based on your interactions.

## When to Use Compact UI

Compact UI is ideal for:
- **Maximizing screen real estate**: Floating panels take up less permanent space
- **Distraction-free coding**: Tool windows hide automatically when not needed
- **Laptop users**: Especially beneficial on smaller screens
- **Focus mode workflows**: Keep your attention on the editor

## Getting Started

### Step 1: Enable Compact UI

1. Open **Settings/Preferences** (Ctrl+Alt+S on Windows/Linux, Cmd+, on macOS)
2. Navigate to **Tools** > **Compact UI**
3. Check **Enable Compact UI Mode**
4. Click **Apply** or **OK**

### Step 2: Configure Your Preferences

The following settings are available:

#### Hover Activation Delay
- **What it does**: Controls how long you hover before a tool window appears
- **Default**: 150ms
- **Recommendation**: 
  - 100-150ms for quick access
  - 200-300ms for less accidental activation

#### Auto-hide Delay
- **What it does**: How long before an inactive tool window automatically hides
- **Default**: 500ms
- **Recommendation**:
  - 300-500ms for quick workflows
  - 1000-2000ms for more relaxed workflows

#### Only When Editor Focused
- **What it does**: Restricts Compact UI behavior to when the editor has focus
- **Default**: Enabled
- **Use case**: Prevents interference when working in other IDE areas

#### Suppress When Pinned
- **What it does**: Ignores pinned tool windows for Compact UI behavior
- **Default**: Enabled
- **Use case**: Keep important tool windows permanently visible

#### Enable Debug Logging
- **What it does**: Logs detailed Compact UI events to the IDE log
- **Default**: Disabled
- **Use case**: Troubleshooting unexpected behavior

### Step 3: Set Up Keyboard Shortcuts

1. Open **Settings/Preferences** > **Keymap**
2. Search for:
   - "Toggle Left Island"
   - "Toggle Right Island"
   - "Toggle Bottom Island"
3. Assign shortcuts (suggested):
   - Left Island: **Alt+1**
   - Right Island: **Alt+2**
   - Bottom Island: **Alt+3**

## Using Compact UI

### Basic Workflow

1. **Show Tool Windows**:
   - Press your assigned shortcut (e.g., Alt+1 for left island)
   - Your previously visible tool windows appear as floating panels
   
2. **Hide Tool Windows**:
   - Press the same shortcut again
   - All visible tool windows on that island hide
   
3. **Auto-hide Behavior**:
   - When enabled, tool windows automatically hide after the configured delay
   - Move focus back to trigger auto-hide

### Working with Multiple Tool Windows

The plugin remembers which tool windows were visible on each island:

**Example Workflow**:
1. Open Project and Structure windows (both on left island)
2. Press Alt+1 to hide both
3. Press Alt+1 again to restore both
4. With Compact UI enabled, both appear as floating panels

### Pinning Tool Windows

You can exclude specific tool windows from Compact UI:

1. Right-click the tool window header
2. Select **Pin** (or use the pin icon)
3. Pinned windows won't be affected by Compact UI (when "Suppress When Pinned" is enabled)

## Advanced Tips

### Optimizing Delay Settings

**For Quick Access**:
```
Hover Activation Delay: 100ms
Auto-hide Delay: 300ms
```

**For Stable Viewing**:
```
Hover Activation Delay: 200ms
Auto-hide Delay: 1500ms
```

**For Minimal Distraction**:
```
Hover Activation Delay: 50ms
Auto-hide Delay: 500ms
Only When Editor Focused: Enabled
```

### Troubleshooting

#### Tool windows don't appear as floating

**Possible causes**:
1. Compact UI is not enabled
2. "Suppress When Pinned" is enabled and the window is pinned
3. The window is not part of a remembered set

**Solutions**:
- Verify Compact UI is enabled in settings
- Check if the window is pinned
- Toggle the island once to "remember" the windows

#### Auto-hide doesn't work

**Possible causes**:
1. Hover over the window resets the timer
2. The window has focus
3. Another action is interfering

**Solutions**:
- Move focus away from the tool window
- Increase the auto-hide delay
- Enable debug logging and check the IDE log

#### Floating windows appear in wrong position

**Note**: The current implementation uses the IDE's default floating position. Custom positioning is planned for a future release.

### Disabling Compact UI

To return to normal behavior:

1. Open **Settings/Preferences** > **Tools** > **Compact UI**
2. Uncheck **Enable Compact UI Mode**
3. Click **Apply**

All tool windows will automatically restore to their original types (docked/floating/windowed).

## Compatibility

### Supported IDEs

Compact UI works with all IntelliJ Platform-based IDEs:
- IntelliJ IDEA (Community & Ultimate)
- PyCharm
- WebStorm
- PhpStorm
- RubyMine
- GoLand
- Rider
- CLion
- Android Studio
- And more...

### Minimum Version

- **Since Build**: 243 (2024.3.x)

### Known Limitations

1. **Hover detection**: Not yet implemented for automatic showing
2. **Editor focus detection**: Simplified implementation; full detection coming soon
3. **Custom positioning**: Floating windows use IDE default positions
4. **Per-anchor settings**: Currently global settings; per-anchor configuration planned

## Best Practices

### 1. Start Conservative

Begin with default settings and adjust based on your workflow.

### 2. Use Pinning Strategically

Pin tool windows that you want always visible (e.g., Project tree).

### 3. Experiment with Delays

Different tasks may benefit from different delay settings. Don't be afraid to adjust.

### 4. Combine with Other IDE Features

Compact UI works well with:
- Distraction-free mode
- Presentation mode
- Custom layouts
- Quick Documentation (Ctrl+Q)

### 5. Create Custom Shortcuts

If the suggested shortcuts conflict with your workflow, choose what works best for you.

## Frequently Asked Questions

**Q: Can I have different settings for different islands?**  
A: Not yet. Per-anchor configuration is on the roadmap.

**Q: Will my settings sync across IDEs?**  
A: Yes, if you use IDE Settings Sync. CompactUISettings are stored in the IDE configuration.

**Q: Does Compact UI affect performance?**  
A: Minimal impact. The plugin uses efficient timers and event listeners.

**Q: Can I use Compact UI with remote development?**  
A: Yes, it should work with JetBrains Gateway and remote IDEs.

**Q: What happens if I close the IDE with Compact UI enabled?**  
A: Settings are preserved. Tool windows restore properly on restart.

## Getting Help

If you encounter issues:

1. **Enable debug logging** in Compact UI settings
2. **Check the IDE log**: Help > Show Log in Explorer/Finder
3. **Search for "Compact UI" entries** in the log
4. **Report issues** with relevant log excerpts on the GitHub repository

## Feedback and Contributions

We welcome your feedback and contributions!

- **GitHub Repository**: https://github.com/hovrawl/JetBrains-Toggleable-Tool-Windows
- **Issue Tracker**: Report bugs and request features
- **Pull Requests**: Contributions are always welcome

---

*Last updated: Implementation of Compact UI Feature*
