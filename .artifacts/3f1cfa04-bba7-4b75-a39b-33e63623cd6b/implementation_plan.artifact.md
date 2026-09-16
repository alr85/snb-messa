# Azure Cloud Status Monitor Implementation Plan

Add a comprehensive Azure Cloud Status Monitor feature to the MECCA app, allowing users and service engineers to inspect the health, reachability, latency, and endpoint availability of the Azure backend (`https://snb-mea-web-apiapi.azure-api.net/api/` and Azure App Service).

## User Review Required

> [!IMPORTANT]
> The Azure Cloud Status Monitor will perform live diagnostic checks against configured Azure API endpoints (e.g., Users, Customers, MdSystems, and WeekendRota) on demand and optionally on screen load.

## Open Questions

- Are there any specific additional Azure endpoints or health-check URLs you would like monitored alongside `Users`, `Customers`, `MdSystems`, and `WeekendRota`?
- Would you like periodic background checks with notifications when Azure status changes, or is on-demand inspection via Settings sufficient?

## Proposed Changes

### Network & ViewModel Layer

#### [NEW] [AzureStatusManager.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/network/AzureStatusManager.kt)
- Create a diagnostic utility/repository that tests connectivity and response times for key Azure API endpoints:
  - Base API Gateway (`https://snb-mea-web-apiapi.azure-api.net/api/Users`)
  - Weekend Rota API (`https://snb-mea-web-api20240909215557.azurewebsites.net/api/WeekendRota`)
- Measures round-trip latency (ms), HTTP status codes, and determines overall cloud health status (Healthy, Degraded, Unreachable).

#### [NEW] [AzureCloudStatusViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/menu/AzureCloudStatusViewModel.kt)
- ViewModel to manage state for health checks, loading status, endpoint results, and refresh actions.

---

### UI & Navigation Layer

#### [NEW] [AzureCloudStatusScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/menu/AzureCloudStatusScreen.kt)
- Jetpack Compose screen showing:
  - Overall Azure Cloud Status header card with status badge (Healthy / Degraded / Offline) and last checked time.
  - Quick action to "Run Diagnostics" / "Refresh".
  - List of monitored endpoints displaying name, URL path, HTTP status code, and latency in milliseconds.
  - Helpful technical diagnostics (e.g., active base URL, network connectivity state).

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/mainmenu/SettingsScreen.kt)
- Add a new menu item under "App Management" for **Azure Cloud Status** with cloud icon pointing to route `"azureCloudStatus"`.

#### [MODIFY] [AppChromeViewModel.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/AppChromeViewModel.kt)
- Add top bar configuration for route `"azureCloudStatus"` (Title: "Azure Cloud Status", showBack = true).

#### [MODIFY] [Navigation.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/ui/theme/Navigation.kt)
- Register `composable("azureCloudStatus")` route in `AppNavGraph`.

## Verification Plan

### Automated Tests
- Build verification via Gradle (`gradle_build("app:assembleDebug")`) to ensure no compilation errors.

### Manual Verification
- Launch app, navigate to Settings -> App Management -> Azure Cloud Status.
- Verify real-time diagnostic checks ping Azure endpoints, display response times in ms, HTTP status codes, and overall health status.
- Test "Refresh / Run Diagnostics" button.
