# Implement 'Also Known As' field for Customers

The goal is to update the application to handle a new `alsoKnownAs` field provided by the customer API. This involves updating the data models, database storage, and the UI.

## Proposed Changes

### Data Layer

#### [MODIFY] [Customer.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/dataClasses/Customer.kt)
- Add `val alsoKnownAs: String?` to the `Customer` data class.
- Add `val alsoKnownAs: String?` to the `CustomerLocal` entity class.

#### [MODIFY] [CustomerRepository.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/repositories/CustomerRepository.kt)
- Update `fetchAndStoreCustomers` to map the `alsoKnownAs` field from the API response to the local database entity.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/AppDatabase.kt)
- Increment `version` to 77.

### UI Layer

#### [MODIFY] [ServiceSelectCustomerScreen.kt](file:///C:/Users/Adam Robson/StudioProjects/snb-messa/app/src/main/java/com/snb/inspect/screens/mainmenu/ServiceSelectCustomerScreen.kt)
- Update `CustomerRow` to display `alsoKnownAs` next to the customer name if it's not null or blank (e.g., "Customer Name (AKA Name)").

## Verification Plan

### Manual Verification
- Deploy the app to a device or emulator.
- Go to the "Select a Customer" screen.
- Perform a "Refresh" to fetch the latest customer data (including the new field).
- Verify that the "Also Known As" field appears correctly in the customer list for relevant customers.
