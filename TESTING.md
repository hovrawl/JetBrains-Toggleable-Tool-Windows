# Immersive Top Bar Manual Testing Checklist

This checklist covers manual verification of the Immersive Top Bar feature.

## Prerequisites
- Build and run the plugin using `./gradlew runIde`
- Open a test project in the sandbox IDE

## Basic Functionality Tests

### 1. Enable/Disable Feature
- [ ] Open Settings > Tools > Compact UI
- [ ] Enable "Auto-hide Top Bar" checkbox
- [ ] Verify main toolbar disappears
- [ ] Verify navigation bar disappears (if "Hide navigation bar too" is checked)
- [ ] Disable "Auto-hide Top Bar" checkbox
- [ ] Verify main toolbar reappears
- [ ] Verify navigation bar reappears (if it was visible before)

### 2. Reveal Zone
- [ ] Enable "Auto-hide Top Bar"
- [ ] Move mouse to the very top edge of the IDE window (top 4px by default)
- [ ] Verify toolbar appears immediately
- [ ] Move mouse away from toolbar area
- [ ] Verify toolbar hides after configured delay (default 700ms)
- [ ] Move mouse back to top edge while timer is running
- [ ] Verify hide is cancelled and toolbar remains visible

### 3. Settings Live Updates
- [ ] Enable feature
- [ ] Change "Reveal zone height" to 10px
- [ ] Verify reveal zone responds at top 10px
- [ ] Change "Hide delay" to 2000ms
- [ ] Move mouse to reveal zone, then away
- [ ] Verify toolbar hides after 2 seconds
- [ ] Change "Edge padding" to 8px
- [ ] Verify padding updates immediately around IDE content

### 4. Edge Padding
- [ ] Enable feature with default padding (4px)
- [ ] Verify a small gap appears around the IDE content
- [ ] Uncheck "Apply padding to sides and bottom"
- [ ] Verify padding only on top
- [ ] Re-check "Apply padding to sides and bottom"
- [ ] Verify padding on all sides
- [ ] Change padding to 0px
- [ ] Verify no padding
- [ ] Change padding to 16px
- [ ] Verify larger padding visible

### 5. Navigation Bar Toggle
- [ ] Enable feature with "Hide navigation bar too" checked
- [ ] Verify both toolbar and navbar hidden
- [ ] Uncheck "Hide navigation bar too"
- [ ] Verify navbar remains visible, toolbar still hides
- [ ] Move mouse to reveal zone
- [ ] Verify only toolbar shows (navbar stays visible)
- [ ] Re-check "Hide navigation bar too"
- [ ] Move mouse to reveal zone
- [ ] Verify both toolbar and navbar show

### 6. Original State Preservation
- [ ] Disable main toolbar manually: View > Appearance > Toolbar (uncheck)
- [ ] Disable navigation bar manually: View > Appearance > Navigation Bar (uncheck)
- [ ] Enable "Auto-hide Top Bar" feature
- [ ] Disable "Auto-hide Top Bar" feature
- [ ] Verify both toolbar and navbar remain hidden (original state preserved)
- [ ] Enable toolbar manually, leave navbar hidden
- [ ] Enable feature, then disable it
- [ ] Verify toolbar visible, navbar still hidden

### 7. User Manual Changes While Feature Active
- [ ] Enable "Auto-hide Top Bar"
- [ ] Enable toolbar manually via View > Appearance > Toolbar
- [ ] Wait a moment, then disable feature
- [ ] Verify toolbar remains visible (user preference remembered)

### 8. Debug Logging
- [ ] Enable "Debug logging"
- [ ] Perform various actions (enable, reveal, hide, disable)
- [ ] Open IDE log (Help > Show Log in Explorer/Finder)
- [ ] Verify log entries with prefix "hCompactUi[IMMERSIVE]" are present
- [ ] Disable "Debug logging"
- [ ] Perform actions again
- [ ] Verify no new log entries appear

## Edge Cases

### 9. Fullscreen/Presentation Mode
- [ ] Enable feature
- [ ] Enter fullscreen mode (View > Enter Full Screen, or F11 on Windows/Linux)
- [ ] Verify padding is suspended (no gap around content)
- [ ] Exit fullscreen mode
- [ ] Verify padding is restored
- [ ] Enter presentation mode (View > Enter Presentation Mode)
- [ ] Verify padding is suspended
- [ ] Exit presentation mode
- [ ] Verify padding is restored

### 10. Multiple Projects
- [ ] Enable feature
- [ ] Open a second project in a new window (File > Open, choose "New Window")
- [ ] Verify both windows have reveal zones
- [ ] Verify toolbar hides in both windows
- [ ] Move mouse to top of second window
- [ ] Verify toolbar appears in second window only
- [ ] Close second project
- [ ] Verify first project still works correctly

### 11. Settings Persistence
- [ ] Configure feature with custom settings (e.g., padding=8, delay=1500, zone height=6)
- [ ] Enable feature
- [ ] Close IDE completely
- [ ] Reopen IDE
- [ ] Open Settings > Tools > Compact UI
- [ ] Verify custom settings are preserved
- [ ] Verify feature is still enabled and working

### 12. Compatibility with Existing Actions
- [ ] Enable feature
- [ ] Test toggle island actions (Left/Right/Bottom)
- [ ] Verify tool windows still toggle correctly
- [ ] Verify no interference between features

## Performance & Stability

### 13. No Exceptions
- [ ] With debug logging disabled, use feature normally for 5 minutes
- [ ] Check IDE log for any exceptions or errors
- [ ] Verify no recurring exceptions

### 14. Resource Cleanup
- [ ] Enable feature on multiple projects
- [ ] Close all projects
- [ ] Check that no memory leaks or background threads remain
- [ ] (Advanced: use a profiler if available)

## Platform-Specific Tests

### 15. Windows
- [ ] Verify reveal zone doesn't interfere with window drag
- [ ] Verify system buttons (minimize, maximize, close) are accessible
- [ ] Test with New UI enabled (Settings > Appearance & Behavior > New UI)

### 16. macOS
- [ ] Verify menu bar is not obscured
- [ ] Verify traffic light buttons (close, minimize, maximize) work
- [ ] Test in dark mode and light mode

### 17. Linux
- [ ] Test with different window managers (GNOME, KDE, XFCE if possible)
- [ ] Verify window decorations work correctly
- [ ] Verify menu bar accessible

## Known Limitations (Expected Behavior)

- Animation is disabled in this iteration (checkbox is disabled)
- Reveal zone is always at the top edge only (no left/right triggers)
- Settings are global (not per-frame)
- Status bar auto-hide is not implemented

## Test Results Summary

Date tested: _____________
IDE version: _____________
OS: _____________
Overall result: [ ] PASS [ ] FAIL

Notes:
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
