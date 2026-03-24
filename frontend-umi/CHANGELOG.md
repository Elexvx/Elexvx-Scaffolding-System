# Changelog

All notable changes to `@elexvx/admin-frontend-umi` will be documented in this file.

The format is inspired by Keep a Changelog, and this project follows Semantic Versioning.

## [0.3.0] - 2026-03-24

### Added
- Added missing migrated routes/pages for `announcement/cards`, `announcement/table`, `message/send`, `notification/table`, `console/download`, and `user/index`.
- Added complete result pages: `browser-incompatible`, `fail`, `maintenance`, `network-error`, and `success` under `src/pages/result/*`.
- Added example capability pages for `example/goods` and `example/order` to keep demo routes available.
- Added full migration matrix in `MIGRATION.md` covering all pages from `frontend/src/pages`.

### Changed
- Expanded dynamic `componentRegistry` with complete menu key aliases including `MessageSend`, `AnnouncementCards`, `SystemDict`, `SystemModules`, `SystemMonitorOnlineUser`, `SystemMonitorRedis`, `SystemMonitorServer`, `SystemPersonalize`, `SystemSecurity`, `SystemStorage`, `SystemVerification`, and `SystemWatermark`.
- Reworked system config page into sectioned mode (personalize / verification / security / storage) while preserving merged-route compatibility.
- Updated Umi route config to include explicit compatibility routes for migrated legacy paths.
- Bumped package version to `0.3.0`.

## [0.2.0] - 2026-03-24

### Added
- Added formal migrated pages for dictionary, module management, sensitive words, watermark settings, online-user monitor, Redis monitor, and server monitor.
- Added service adapters and domain types for the newly migrated system/monitor capabilities.
- Added a full old-to-new page migration mapping table in `MIGRATION.md`, including merged, pending, and deprecated entries.

### Changed
- Extended dynamic component registry so backend real menus can resolve all newly migrated formal pages.
- Expanded merged-route aliases (`announcement`, `message/send`, `notification/table`, `system/config` family, `user`) to keep compatibility with legacy menu resources.

## [0.1.0] - 2026-03-23

### Added
- Added a stable Umi Max runtime bootstrap for login, current-user loading, menu loading, and guarded redirects.
- Added centralized page scaffold, mobile filter drawer, mobile action bar, and responsive list/table fallback for major admin lists.
- Added `CHANGELOG.md` and documented SemVer strategy in the frontend README.

### Changed
- Removed duplicate root layout mounting so the project now relies on the Umi Max layout plugin only.
- Normalized service request paths to work with the shared `/api` prefix without producing `/api/api/...`.
- Reworked backend menu adaptation, component whitelist mapping, and permission code alignment against real backend route names.
- Normalized array and paged API responses for user, role, menu, org, announcement, message, notification, and log modules.

### Fixed
- Fixed blank-page startup issues caused by layout conflicts, menu envelope parsing, expired token handling, and fragile initialization flow.
- Fixed login/session fallback so invalid or expired tokens are cleared before redirecting back to `/login`.
- Fixed responsive list pages so mobile devices no longer receive dense desktop tables directly.
