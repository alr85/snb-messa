package com.snb.inspect.dataClasses

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "SensitivityOptimisationValidations")
class SensitivityOptimisationValidationLocal(
    @PrimaryKey val sovId: String
) {
    // SETUP
    var mapVersion : String = "SOV1.0"
    var systemId: Int = 0
    var cloudSystemId: Int = 0
    var serialNumber: String = ""
    var lastLocation: String = ""
    var engineerId: Int = 0
    var customerId: Int = 0
    var startDate: String = LocalDateTime.now().toString()
    var endDate: String = ""
    var isSynced: Boolean = false

    // PRODUCT DETAILS
    var productDescription: String = ""
    var productLibraryReference: String = ""
    var productLibraryNumber: String = ""
    var beltSpeed: String = ""

    // AS FOUND
    var sensitivityAsFoundFerrous: String = ""
    var sensitivityAsFoundNonFerrous: String = ""
    var sensitivityAsFoundStainless: String = ""
    var sensitivityAsFoundOther1: String = ""
    var sensitivityAsFoundOther2: String = ""
    
    var detectionSettingAsFound1: String = ""
    var detectionSettingAsFound2: String = ""
    var detectionSettingAsFound3: String = ""
    var detectionSettingAsFound4: String = ""
    var detectionSettingAsFound5: String = ""
    var detectionSettingAsFound6: String = ""
    var detectionSettingAsFound7: String = ""
    var detectionSettingAsFound8: String = ""

    // VALIDATION (30 passes)
    var validationTest1Description: String = "" // e.g. "Ferrous 1.0mm" or "Stainless 1.2mm"
    var validationTest1Passes: Int = 0
    var validationTest1Successes: Int = 0
    
    var validationTest2Description: String = ""
    var validationTest2Passes: Int = 0
    var validationTest2Successes: Int = 0
    
    var validationTest3Description: String = ""
    var validationTest3Passes: Int = 0
    var validationTest3Successes: Int = 0

    // AS LEFT
    var sensitivityAsLeftFerrous: String = ""
    var sensitivityAsLeftNonFerrous: String = ""
    var sensitivityAsLeftStainless: String = ""
    var sensitivityAsLeftOther1: String = ""
    var sensitivityAsLeftOther2: String = ""

    var detectionSettingAsLeft1: String = ""
    var detectionSettingAsLeft2: String = ""
    var detectionSettingAsLeft3: String = ""
    var detectionSettingAsLeft4: String = ""
    var detectionSettingAsLeft5: String = ""
    var detectionSettingAsLeft6: String = ""
    var detectionSettingAsLeft7: String = ""
    var detectionSettingAsLeft8: String = ""

    // COMMENTS
    var systemComments: String = "" // Performance restrictions (metal free area, vibration etc)
    var productComments: String = "" // Performance restrictions (conductivity, size changes etc)

    // SIGN OFF
    var engineerSignature: String = ""
    var customerSignature: String = ""
    var customerName: String = ""
    
    // Labels for detection settings (carried from machine model)
    var detectionSetting1label: String = ""
    var detectionSetting2label: String = ""
    var detectionSetting3label: String = ""
    var detectionSetting4label: String = ""
    var detectionSetting5label: String = ""
    var detectionSetting6label: String = ""
    var detectionSetting7label: String = ""
    var detectionSetting8label: String = ""
}
