# JetBrains-Toggleable-Tool-Windows Changelog

## [Unreleased]
### Added
- **Compact UI Mode**: New feature that enables tool windows to appear as transient floating overlays with auto-hide functionality
  - Application-level settings accessible via Settings > Tools > Compact UI
  - Configurable hover activation delay (default: 150ms)
  - Configurable auto-hide delay (default: 500ms)
  - Option to suppress floating for pinned tool windows
  - Option to only hide when editor refocuses
  - Debug logging for troubleshooting
  - Seamless integration with existing toggle actions
  - Automatic cleanup and state restoration on project close or feature disable

## [0.0.1] - 2025-10-08
### Added
- Initial release: toggle actions for Left/Right/Bottom tool window islands that remember and restore the last active tool window per island.
- Project-level service to store last-remembered tool window IDs.
- Basic action registrations and descriptions.