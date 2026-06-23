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
    var newLocation: String = ""
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
    var sampleCertAsFoundFerrous: String = ""
    var detectRejectAsFoundFerrousLeading: String = ""
    var peakSignalAsFoundFerrousLeading: String = ""
    var detectRejectAsFoundFerrousMiddle: String = ""
    var peakSignalAsFoundFerrousMiddle: String = ""
    var detectRejectAsFoundFerrousTrailing: String = ""
    var peakSignalAsFoundFerrousTrailing: String = ""
    var notesAsFoundFerrous: String = ""

    var sensitivityAsFoundNonFerrous: String = ""
    var sampleCertAsFoundNonFerrous: String = ""
    var detectRejectAsFoundNonFerrousLeading: String = ""
    var peakSignalAsFoundNonFerrousLeading: String = ""
    var detectRejectAsFoundNonFerrousMiddle: String = ""
    var peakSignalAsFoundNonFerrousMiddle: String = ""
    var detectRejectAsFoundNonFerrousTrailing: String = ""
    var peakSignalAsFoundNonFerrousTrailing: String = ""
    var notesAsFoundNonFerrous: String = ""

    var sensitivityAsFoundStainless: String = ""
    var sampleCertAsFoundStainless: String = ""
    var detectRejectAsFoundStainlessLeading: String = ""
    var peakSignalAsFoundStainlessLeading: String = ""
    var detectRejectAsFoundStainlessMiddle: String = ""
    var peakSignalAsFoundStainlessMiddle: String = ""
    var detectRejectAsFoundStainlessTrailing: String = ""
    var peakSignalAsFoundStainlessTrailing: String = ""
    var notesAsFoundStainless: String = ""

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
    var notesAsFoundDetectionSettings: String = ""
    var productPeakSignalAsFound: String = ""

    // VALIDATION (30 passes)
    var validationTest1Description: String = "" // e.g. "Ferrous 1.0mm" or "Stainless 1.2mm"
    var val1LeadingPasses: Int = 0
    var val1LeadingSuccesses: Int = 0
    var val1MiddlePasses: Int = 0
    var val1MiddleSuccesses: Int = 0
    var val1TrailingPasses: Int = 0
    var val1TrailingSuccesses: Int = 0
    
    var validationTest2Description: String = ""
    var val2LeadingPasses: Int = 0
    var val2LeadingSuccesses: Int = 0
    var val2MiddlePasses: Int = 0
    var val2MiddleSuccesses: Int = 0
    var val2TrailingPasses: Int = 0
    var val2TrailingSuccesses: Int = 0
    
    var validationTest3Description: String = ""
    var val3LeadingPasses: Int = 0
    var val3LeadingSuccesses: Int = 0
    var val3MiddlePasses: Int = 0
    var val3MiddleSuccesses: Int = 0
    var val3TrailingPasses: Int = 0
    var val3TrailingSuccesses: Int = 0

    // AS LEFT
    var sensitivityAsLeftFerrous: String = ""
    var sampleCertAsLeftFerrous: String = ""
    var minSignalAsLeftFerrousLeading: String = ""
    var minSignalAsLeftFerrousMiddle: String = ""
    var minSignalAsLeftFerrousTrailing: String = ""
    var notesAsLeftFerrous: String = ""

    var sensitivityAsLeftNonFerrous: String = ""
    var sampleCertAsLeftNonFerrous: String = ""
    var minSignalAsLeftNonFerrousLeading: String = ""
    var minSignalAsLeftNonFerrousMiddle: String = ""
    var minSignalAsLeftNonFerrousTrailing: String = ""
    var notesAsLeftNonFerrous: String = ""

    var sensitivityAsLeftStainless: String = ""
    var sampleCertAsLeftStainless: String = ""
    var minSignalAsLeftStainlessLeading: String = ""
    var minSignalAsLeftStainlessMiddle: String = ""
    var minSignalAsLeftStainlessTrailing: String = ""
    var notesAsLeftStainless: String = ""

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
    var notesAsLeftDetectionSettings: String = ""
    var productPeakSignalAsLeft: String = ""

    // COMMENTS
    var systemComments: String = "" // Performance restrictions (metal free area, vibration etc)
    var productComments: String = "" // Performance restrictions (conductivity, size changes etc)
    var optimisationNotes: String = "" // Notes on the optimisation process

    // SIGN OFF
    var engineerSignature: String = ""
    var customerSignature: String = ""
    var customerName: String = ""

    // NEW VALIDATION
    var packValidationPassed: Boolean = false
    
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
