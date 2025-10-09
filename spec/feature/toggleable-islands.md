## Overview

This spec documents the Toggleable Islands feature: three actions that let you quickly hide all visible tool windows on a given "island" (left, right, bottom) and re-open the last remembered set for that island.

The behavior is designed to be fast, predictable, and keyboard-friendly, while coexisting cleanly with the Compact UI mode.

## What's New

### Core Functionality

- Three user actions, one per island:
  - Toggle Left Island
  - Toggle Right Island
  - Toggle Bottom Island
- When any tool window on the target island is visible, invoking the action hides all visible windows on that island and remembers their IDs (order-preserving, unique).
- When no window on the island is visible, invoking the action re-opens the last remembered set:
  - Activates the first remembered tool window
  - Shows the rest (restoring multi-split sections when applicable)
- If nothing was remembered (first use) or remembered IDs are not available anymore, the first available tool window on that island is opened as a sensible default.

### Integration with Compact UI

- When Compact UI is enabled, toggling delegates to the Compact UI controller:
  - Hiding uses `controller.forceHideAll()`
  - Showing uses `controller.requestShow(id, "toggle_action")` for the primary remembered window
- When Compact UI is disabled, the toggling logic uses the normal Tool Window API (`show`, `hide`, `activate`).

### Per‑Project Memory

- Last-remembered tool windows are stored per project via a `PersistentStateComponent` with multi-ID support per island, including migration from legacy single-ID fields.

## User Experience

- Works out of the box with common windows like Project/Structure/Services (left), Commit/TODO/Problems (right), Run/Debug/Terminal/Build (bottom).
- Floating/detached tool windows are treated as belonging to their original anchor island.
- Suggested keybindings (customizable): Alt+1 (Left), Alt+2 (Right), Alt+3 (Bottom).

## Technical Implementation

### Actions and IDs

Registered in `src/main/resources/META-INF/plugin.xml`:
- id: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleLeftIslandToolWindow`
  - class: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleLeftIslandToolWindow`
  - text: "Toggle Left Island"
- id: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleRightIslandToolWindow`
  - class: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleRightIslandToolWindow`
  - text: "Toggle Right Island"
- id: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleBottomIslandToolWindow`
  - class: `com.github.hovrawl.jetbrainstoggleabletoolwindows.actions.ToggleBottomIslandToolWindow`
  - text: "Toggle Bottom Island"

### Classes

- `ToggleIslandAction` (abstract)
  - Location: `src/main/kotlin/.../actions/ToggleStripeAction.kt`
  - Input: target `ToolWindowAnchor` (LEFT/RIGHT/BOTTOM)
  - Behavior (Compact UI disabled):
    1. Collect visible tool windows on the target island via `ToolWindowManager`.
    2. If any are visible (or the active window is on this island):
       - Remember their IDs per island using `RememberedToolWindowsService`.
       - Hide them all (`ToolWindow.hide(null)`).
    3. Else, read remembered IDs for the island and re-open them:
       - Activate the first, then show the rest.
    4. If none are remembered/available, open the first available tool window on that island.
  - Behavior (Compact UI enabled):
    - Uses `CompactUIController.getInstance(project)` to hide/show (see Integration above).

- `ToggleLeftIslandToolWindow`, `ToggleRightIslandToolWindow`, `ToggleBottomIslandToolWindow`
  - Location: `src/main/kotlin/.../actions/*`
  - Each extends `ToggleIslandAction` with the respective `ToolWindowAnchor`.

- `RememberedToolWindowsService` (Project service)
  - Location: `src/main/kotlin/.../services/RememberedToolWindowsService.kt`
  - Persistent state file: `toggleable-tool-windows.xml`
  - State fields:
    - Legacy single IDs: `lastLeftId`, `lastRightId`, `lastBottomId`
    - New multi-ID lists: `lastLeftIds`, `lastRightIds`, `lastBottomIds`
  - Migration: If multi-ID lists are empty but legacy single ID exists, migrate it to the list on load.
  - API:
    - `rememberId(anchor, id)` — maintains both legacy and list for backward compatibility
    - `rememberIds(anchor, ids)` — stores de-duplicated, order-preserving lists per island
    - `getRememberedId(anchor)` — first remembered (or legacy) id
    - `getRememberedIds(anchor)` — full remembered set per island

### Anchor/Island Detection

- Uses `ToolWindow.anchor` to determine island membership.
- Active vs visible logic:
  - If any tool window is visible on the island, the action behaves as "hide island" regardless of focus location, to restore the last set accurately later.

### Compact UI Integration Points

- Settings check via `CompactUISettings.getInstance().state.enabled` in `ToggleIslandAction`.
- Delegate calls to `CompactUIController` (project-level) when enabled:
  - `forceHideAll()` when hiding
  - `requestShow(id, "toggle_action")` to show primary remembered window on this island

## Edge Cases & Safety

- Invalid remembered IDs or unavailable tool windows are skipped safely.
- First invocation with no remembered IDs results in no-ops unless a suitable default exists; we attempt to open the first available tool window on that island.
- Floating windows are treated by their `anchor` (original island) even if detached.
- All UI operations are run on the EDT via IntelliJ APIs (`ToolWindowManager`).

## Testing

### Manual Verification

- Hide and remember on each island:
  1. Open two tool windows on the left (e.g., Project and Structure)
  2. Invoke Toggle Left Island → both hide; reopen the action → both restore (first activates)
- Reopen remembered when none visible:
  - Ensure no left windows are visible, invoke → last remembered set reopens
- Fallback behavior:
  - Clear remembered state, ensure no left windows visible, invoke → first available left tool window opens
- Compact UI coexistence:
  - Enable Compact UI in settings, repeat the above → show/hide routed through Compact UI controller

### Automated Tests (optional)

- BasePlatformTestCase ideas:
  - Verify `RememberedToolWindowsService` persists list state and migrates legacy fields
  - Simulate action execution to assert hide/show and remembered IDs interaction (as feasible with test tooling)

## Acceptance Criteria

- Actions hide all currently visible windows on the target island and remember their IDs.
- Actions reopen the remembered set (activate first, show the rest) when none are visible on that island.
- Compact UI enabled: actions delegate to controller without breaking behavior.
- Invalid/unavailable windows are ignored without errors; no exceptions in logs under normal use.

## Files Involved

- `src/main/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/actions/ToggleStripeAction.kt`
- `src/main/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/actions/ToggleLeftStripeToolWindow.kt`
- `src/main/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/actions/ToggleRightStripeToolWindow.kt`
- `src/main/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/actions/ToggleBottomStripeToolWindow.kt`
- `src/main/kotlin/com/github/hovrawl/jetbrainstoggleabletoolwindows/services/RememberedToolWindowsService.kt`
- `src/main/resources/META-INF/plugin.xml`

## Notes / Future Enhancements

- Potential per-island preferences (e.g., prefer specific default window if none remembered)
- Optional setting to remember only the active tool window rather than all visible on the island
- Additional tests for split panes and combined tool window scenarios

