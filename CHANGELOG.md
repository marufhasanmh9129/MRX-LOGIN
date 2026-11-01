# Changelog

## 1.0.0

- Set project and plugin version to 1.0.0.
- Added failed-login attempt counting and in-memory temporary lockout.
- Added account-per-IP registration enforcement.
- Added weak-password denylist and optional registration confirmation.
- Added unauthenticated inventory, interaction, damage and item-drop restrictions.
- Added optional first-join spawn and last-location coordinate saving.
- Added `/ml admin unregister <player>`.
- Added explicit configuration validation for modules that are not implemented in this release.
- Added JUnit password hashing tests and retained GitHub Actions packaging workflow.
- Documented platform and feature limitations rather than claiming universal compatibility.
