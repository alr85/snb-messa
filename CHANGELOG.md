# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.11] - 08/05/2026
### Fixed
- Update logic to correctly identify non-conveyor systems (gravity/pipe/pharma) and suppress product dimension requirements.
- Enable the N/A toggle for Ferrous tests within the Operator test section.
### Added
- Auto-capitalise test stick certificate numbers across all calibration screens.
- Auto-capitalise serial number input
- Added a 'General' section to the user manuals screen for inverter manuals, wiring diagrams etc
- Added a pinch to zoom feature on the user manuals. Double tap to reset the view
- Added 'Double Bag (Latched)' to the option list for Detect Notifications


## [1.0.10] - 01/05/2026
### Fixed
- Increased max length of 'Product Peak Signal (As Found)' to 25 characters and moved from the bottom of the screen to the top of the screen.
- Increased max length of 'Product Peak Signal (As Left)' to 25 characters and moved from the bottom of the screen to the top.
- Increased max length of 'Product Description' to 25 characters.
- Decreased max length of 'Product Details Engineer Notes' to 25 characters.
- Added further text normalisation to prevent special characters being encoded incorrectly
- Fixed a bug where the Large Metal Test PV Result was not being saved to the internal database
- Rebuilt the PV Pass/Fail logic for 'As Left' sensitivities - Fe, NonFe and SS
- Removed the requirement for engineers to sign off the M&S Sensitivity Requirements in the summary page
- Tidied the Summary page, including: Re-ordered to reflect the actual order of the input screens / added the As Found Sensitivity peak signals / condensed the indicator section / removed the unnecessary colons.

### Added
- Introduced a Measuring Equipment database to the app. This will be synced to the office CRM and will allow the user to select the measuring equipment (scopes/tachos/meters etc) they used to perform the calibration. This will allow full traceability of the calibration. The measuring equipment will start increasing over the coming weeks, for now - please leave this section blank

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
