# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.10] - 30/04/2026
### Fixed
- Increased max length of 'Product Peak Signal (As Found)' to 25 characters and moved from the bottom of the screen to the top of the screen.
- Increased max length of 'Product Peak Signal (As Left)' to 25 characters and moved from the bottom of the screen to the top.
- Added further text normalisation to prevent special characters being encoded incorrectly
- Fixed a bug where the Large Metal Test PV Result was not being saved to the internal database
- Rebuilt the PV Pass/Fail logic for 'As Left' sensitivities - Fe, NonFe and SS
- Removed the requirement for engineers to sign off M&S Requirements in the summary page

## [1.0.9] - 21/04/2026
### Fixed
- Made the 'Product Peak Signal (As Left)' field mandatory
- Added a 'Product Peak Signal (As Left)' field to the Calibration Summary pages
- Changed the max length of System Checklist Engineer Comments to 40 characters.
- Added some text normalisation to prevent special characters being encoded incorrectly

## [1.0.8] - 08/04/2026
### Fixed
- Fixed critical errors found during the testing phase.
- Improved stability of the calibration sync process.

## [1.0.0] - 
### Added
- Initial testing release.
- Paperless calibration procedures.
- Offline data storage and cloud syncing.
