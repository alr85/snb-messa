# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.1] - 07/07/2026
### Fixed
- An MD System cloud syncing issue has been identified and fixed

### Changed
- General tidy up of the metal detector calibration summary screen

### Removed
- Removed the PV rules for speed and backup sensors in the metal detector calibrations

## [1.2.0] - 03/07/2026
### Fixed
- A bug that prevented forward navigation on the 'Measuring Equipment' screen in a metal detector calibration

### Changed
- The colour of the forward navigation button in metal detector calibrations now reflects the current screens completion status. Green for complete, red for incomplete.
- The 'About App' screen now shows the full change log rather than the latest update

### Added
- Metal Detector Validations are now up and running.

## [1.1.1] - 29/06/2026
### Fixed
- A bug that prevented a calibration from being finished/uploaded has been found and fixed

## [1.1.0] - 26/06/2026
### Fixed
- Improved the portrait only mode lock to prevent user being logged out upon screen rotation
- Operator failsafe tests are now persistent when returning to an incomplete calibration
- Empty 'Detection Setting' slots are now default N/A
- Sensor Fitted? inputs now default to a null value
- Made the title of 'Calibration Start' screen sticky when scrolling
- Error messages when trying to download user manuals are more descriptive

### Changed
- Made the 'Reason for not calibrating' drop down a multi select list
- Moved the 'Detection Settings (As Found)' screen. This is now before the 'As Found' Sensitivities
- Reversed the reject delay and duration order in the Reject Settings screen

### Added
- Added a new navigation menu in the calibration process to enable short-cuts to specific screens
- Added 'Overhead Sweep Arm' to the list of reject devices in Metal Detector Calibrations
- Added a 30-second bin door timeout PV rule in Metal Detector Calibrations
- Added an 'OK' condition to the System Checklist in Metal Detector Calibrations
- Added a 'Notes' function to the metal detector System Details screen.
- Added a 'Report Data Issue' function to metal detector System Details screen. If you notice any discrepancies in the system details, or the notes someone has added, report it via this button.
- Added a new Codes of Practice area to the Settings menu
- Added an 'Export Database' button to the Settings menu. This will help with diagnostics/debugging
- Added buttons for upcoming features including Metal Detector Validations, and Passwords

### Removed
- Removed 'Operator Test' screen for non-PV calibrations
- Removed the 'Latched' and 'Controlled Restart' inputs for Speed Sensor failsafe test
- Removed bin door open/unlocked notification PV rules
- Removed the conveyor dimension inputs
- Removed the redundant 'Details' data row from the Calibration Summary screen



## [1.0.13] - 29/05/2026
### Fixed
- Increased max length of machine location to 30 characters
- Enabled 'N/A' toggle for Product Peak Signal fields
- Further refinement of PV logic for Reject Bin Door and Operator Test screens
- Limited the main and calibration activities to portrait orientation to prevent user being logged out on rotation.
- Improved machine cloud ID synchronisation
- Improved Operator Test logic to automatically set certificate numbers to "N/A" when the test is "N/A".
- Fixed field reset logic to use empty lists instead of lists containing empty strings for sensor results.
### Added
- Implemented a dropdown menu for "Reason for not calibrating" with an optional "Other" text field.

## [1.0.12] - 15/05/2026
### Fixed
- Further improvements to the P.V. logic, specifically for non-conveyor systems and the Fe/NonFe/SS tests.
- Add "Reject System (Other)" field to the calibration summary details screen.
- Store and persist "As Found" engineer notes for Ferrous, Non-Ferrous, and Stainless tests to prevent data loss when navigating away from calibration activities.

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
