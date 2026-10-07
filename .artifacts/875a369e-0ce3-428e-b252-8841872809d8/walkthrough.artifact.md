# Walkthrough - Implement 'Also Known As' for Customers

I have successfully integrated the `alsoKnownAs` field into the application. This field is now stored locally and displayed in the customer selection screen.

## Changes Made

### Data Layer
- **[Customer.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/dataClasses/Customer.kt)**: Added `alsoKnownAs: String?` to both the API data class and the Room entity.
- **[CustomerRepository.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/repositories/CustomerRepository.kt)**: Updated the mapping logic during synchronization to save the new field.
- **[AppDatabase.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/AppDatabase.kt)**: Incremented the database version to 77.

### UI Layer
- **[ServiceSelectCustomerScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/mainmenu/ServiceSelectCustomerScreen.kt)**:
    - Updated `CustomerRow` to display the "Also Known As" name on a separate line below the primary name, formatted as `(AKA: Name)`.
    - Updated the search logic to include the `alsoKnownAs` field, allowing users to find customers by their AKA name.
    - Updated the navigation logic to pass the combined name (Name + AKA) to subsequent screens, ensuring consistency in headers and "Add New" forms.
    - Adjusted typography for the AKA line to `bodySmall` for better visual hierarchy.

## Verification
- Verified the logic for combining names: `Name (AKA)` is only shown if `alsoKnownAs` is not null or blank.
- Confirmed that the database version increment will trigger a destructive migration (as configured), which is necessary for the schema change.

> [!IMPORTANT]
> The database schema has changed. Existing local customer data will be cleared and re-synced from the API on the next app launch.
