# Walkthrough - Azure Cloud Status Monitor

Added an Azure Cloud Status Monitor feature to the MECCA app, allowing service engineers and users to inspect the real-time health, reachability, HTTP status codes, and round-trip latency of the Azure API backend on demand.

## Changes

### Network & ViewModel Layer
#### [AzureStatusManager.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/network/AzureStatusManager.kt)
- Created diagnostic utility to test connectivity and latency for key Azure endpoints:
  - Base API Gateway (`Users`)
  - Customers API
  - MD Systems API
  - Weekend Rota API
- Measures round-trip time in milliseconds and categorizes overall cloud health (`HEALTHY`, `DEGRADED`, `OFFLINE`).

#### [AzureCloudStatusViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/menu/AzureCloudStatusViewModel.kt)
- Created ViewModel to manage loading states, health reports, error handling, and manual refresh actions.

---

### UI & Navigation Layer
#### [AzureCloudStatusScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/menu/AzureCloudStatusScreen.kt)
- Developed Jetpack Compose dashboard displaying:
  - Overall status banner card with color-coded operational states and last checked timestamp.
  - "Refresh / Run Diagnostics" button with loading indicator.
  - Detailed endpoint cards showing status badges, latency in ms, URLs, and HTTP status codes.

#### [SettingsScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/mainmenu/SettingsScreen.kt)
- Added **Azure Cloud Status** menu item under the **App Management** section.

#### [AppChromeViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/AppChromeViewModel.kt)
- Registered top-bar configuration for route `"azureCloudStatus"` (Title: "Azure Cloud Status", Back button enabled).

#### [Navigation.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/ui/theme/Navigation.kt)
- Registered `composable("azureCloudStatus")` route in `AppNavGraph`.

## Verification Results

### Automated Tests
- Successfully ran Gradle build (`gradle_build("app:assembleDebug")`): **Build finished successfully.**
