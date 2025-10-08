<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# JetBrains-Toggleable-Tool-Windows Changelog

## [Unreleased]
### Added
- Compact UI Mode with floating tool windows and hover activation
- Application-level settings for Compact UI configuration
- Configurable hover activation and auto-hide delays
- Editor-focused mode option
- Pinned window suppression option
- Debug logging support
- Settings UI in Tools > Compact UI

### Changed
- Toggle actions now integrate with Compact UI Mode when enabled
- Enhanced README with comprehensive documentation

## [0.0.1] - 2025-10-08
### Added
- Initial release: toggle actions for Left/Right/Bottom tool window islands that remember and restore the last active tool window per island.
- Project-level service to store last-remembered tool window IDs.
- Basic action registrations and descriptions.