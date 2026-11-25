package com.snb.inspect.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

// --- Legacy Enums & Data Classes for Compatibility ---

enum class CheckweigherStatus {
    PASS,
    FAIL,
    FURTHER_ASSESSMENT_REQUIRED
}

enum class ToleranceSource {
    MANUFACTURER,
    CUSTOMER,
    SNB_GUIDANCE
}

enum class TestType {
    ROUTINE,
    COMPREHENSIVE
}

data class CheckweigherCalculationResult(
    val nominalWeight: Double,
    val referenceWeight: Double,
    val effectiveReferenceWeight: Double,
    val tareOffsetApplied: Double,
    val tne: Double,
    val t1: Double,
    val t2: Double,
    val benchmark20PercentTne: Double,
    val benchmark25PercentTne: Double,
    val passCount: Int,
    val mean: Double?,
    val meanDynamicError: Double?,
    val sampleStdDev: Double?,
    val sixSigmaZoi: Double?,
    val exceeds25PercentTneLimit: Boolean,
    val additionalSetPointAllowance: Double?,
    val equipmentSuitabilityPass: Boolean,
    val overallStatus: CheckweigherStatus
)

data class OffCentreTestResult(
    val testWeight: Double?,
    val centre: Double,
    val corner1: Double,
    val corner2: Double,
    val corner3: Double,
    val corner4: Double,
    val devCorner1: Double,
    val devCorner2: Double,
    val devCorner3: Double,
    val devCorner4: Double,
    val maxDeviation: Double,
    val maxCornerName: String,
    val isPass: Boolean
)

data class RepeatabilityTestResult(
    val testWeight: Double?,
    val reading1: Double,
    val reading2: Double,
    val reading3: Double,
    val zero1: Double? = null,
    val zero2: Double? = null,
    val zero3: Double? = null,
    val mean: Double,
    val minReading: Double,
    val maxReading: Double,
    val range: Double,
    val maxZeroReturn: Double? = null,
    val isPass: Boolean
)

data class StaticTestSet(
    val id: Int = 1,
    val equipmentId: Int? = null,
    val testWeight: String = "",
    val centre: String = "",
    val corner1: String = "",
    val corner2: String = "",
    val corner3: String = "",
    val corner4: String = "",
    val repeatabilityReading1: String = "",
    val repeatabilityReading2: String = "",
    val repeatabilityReading3: String = "",
    val repeatabilityZero1: String = "",
    val repeatabilityZero2: String = "",
    val repeatabilityZero3: String = ""
)

data class EvaluatedStaticTestSet(
    val testSet: StaticTestSet,
    val offCentreResult: OffCentreTestResult?,
    val repeatabilityResult: RepeatabilityTestResult?
)

data class DynamicEccentricityTestResult(
    val testLoad: Double,
    val referenceWeight: Double,
    val frontReading: Double,
    val backReading: Double,
    val centreReading: Double?,
    val frontError: Double,
    val backError: Double,
    val maxError: Double,
    val mpe: Double,
    val scaleInterval: Double,
    val accuracyClass: String,
    val toleranceSource: String = "OIML_R51",
    val toleranceDescription: String = "",
    val isPass: Boolean
)

// --- New Structured Tolerance Modes & Workflows ---

enum class ToleranceMode(val label: String, val description: String) {
    NONE(
        label = "None (Advisory Only)",
        description = "No tolerance limits enforced. Reports weighing errors for advisory purposes only without pass/fail evaluation."
    ),

    PACKAGED_GOODS_TNE(
        label = "Packaged Goods TNE (UK Avg Weight / e-mark)",
        description = "Calculates TNE using UK Packaged Goods Regulations 2006 for nominal weight Qn"
    ),
    MANUAL_SPEC(
        label = "Manual/Customer Specification",
        description = "User enters custom Max Mean Error (g or %) and Max Std Dev (g or %)"
    ),
    DIVISION_BASED(
        label = "Division-Based",
        description = "Based on scale interval (d) with static and dynamic multipliers"
    ),

    OIML_R51_BENCHMARK(
        label = "OIML R 51",
        description = "Enforces OIML R 51 Class MPE and Maximum Permissible Standard Deviation limits"
    )
}

enum class OimlInspectionType(val label: String) {
    INITIAL_VERIFICATION("Initial Verification"),
    IN_SERVICE("In-Service Inspection")
}

data class ToleranceConfig(
    val mode: ToleranceMode = ToleranceMode.PACKAGED_GOODS_TNE,
    val manualMeanErrorValue: Double = 1.0,
    val manualMeanErrorUnit: String = "GRAMS",
    val manualStdDevValue: Double = 0.5,
    val manualStdDevUnit: String = "GRAMS",
    val staticDivisionMultiplier: Double = 1.0,
    val dynamicDivisionMultiplier: Double = 2.0,
    val nominalPackageWeightQn: Double = 0.0,
    val stdDevPercentageOfTne: Double = 10.0,
    val accuracyClass: String = "XIII",
    val designationFactorX: Double = 1.0,
    val verificationScaleIntervalE: Double = 1.0,
    val inspectionType: OimlInspectionType = OimlInspectionType.INITIAL_VERIFICATION,
    val oimlComplianceModeEnabled: Boolean = false
)

data class EvaluatedToleranceLimits(
    val mode: ToleranceMode,
    val allowedStaticMpe: Double,
    val allowedMaxMeanError: Double,
    val allowedMaxStdDev: Double,
    val tne: Double? = null,
    val staticMpeLabel: String = "",
    val maxMeanErrorLabel: String = "",
    val maxStdDevLabel: String = "",
    val description: String = ""
)

data class StaticLinearityPointResult(
    val pointIndex: Int,
    val percentCapacity: Int,
    val targetRefWeight: Double,
    val appliedRefWeight: Double,
    val displayedReading: Double,
    val error: Double,
    val allowedMpe: Double,
    val isPass: Boolean
)

data class StaticLinearityTestResult(
    val points: List<StaticLinearityPointResult>,
    val isOverallPass: Boolean
)

data class EvaluatedOffCentreTestResult(
    val testWeight: Double,
    val centre: Double,
    val frontLeft: Double,
    val frontRight: Double,
    val rearLeft: Double,
    val rearRight: Double,
    val devFrontLeft: Double,
    val devFrontRight: Double,
    val devRearLeft: Double,
    val devRearRight: Double,
    val maxCornerDeviation: Double,
    val maxCornerName: String,
    val allowedLimit: Double,
    val isPass: Boolean
)

data class DynamicRepeatabilityResult(
    val referenceWeight: Double,
    val beltSpeed: Double,
    val beltSpeedUnit: String,
    val runCount: Int,
    val minRequiredRuns: Int,
    val readings: List<Double>,
    val dynamicMean: Double,
    val dynamicMeanError: Double,
    val sampleStdDev: Double,
    val threeSigma: Double,
    val dynamicAccuracyBand: Double,
    val allowedMaxMeanError: Double,
    val allowedMaxStdDev: Double,
    val isMeanErrorPass: Boolean,
    val isStdDevPass: Boolean,
    val isSampleCountSufficient: Boolean,
    val isOverallPass: Boolean
)

data class RejectMechanismResult(
    val underweightRejectResult: String,
    val overweightRejectResult: String,
    val timingVerificationResult: String,
    val isOverallPass: Boolean
)

data class CompleteCheckweigherEvaluation(
    val toleranceLimits: EvaluatedToleranceLimits,
    val staticLinearityResult: StaticLinearityTestResult?,
    val staticEccentricityResult: EvaluatedOffCentreTestResult?,
    val dynamicRepeatabilityResult: DynamicRepeatabilityResult?,
    val rejectMechanismResult: RejectMechanismResult?,
    val overallStatus: CheckweigherStatus
)

object CheckweigherCalculator {

    fun roundTo(x: Double, dp: Int): Double {
        val m = 10.0.pow(dp.toDouble())
        return round(x * m) / m
    }

    /**
     * TNE calculation derived from UK Weights and Measures (Packaged Goods) Regulations 2006.
     * Quantity Qn is in grams or millilitres.
     */
    fun calculateTne(quantity: Double): Double {
        if (quantity <= 0.0) return 0.0
        val tne = when {
            quantity < 5.0 -> 5.0 * 0.09
            quantity <= 50.0 -> quantity * 0.09
            quantity <= 100.0 -> 4.5
            quantity <= 200.0 -> quantity * 0.045
            quantity <= 300.0 -> 9.0
            quantity <= 500.0 -> quantity * 0.03
            quantity <= 1000.0 -> 15.0
            quantity <= 10_000.0 -> quantity * 0.015
            quantity <= 15_000.0 -> 150.0
            else -> quantity * 0.01
        }
        return roundTo(tne, 2)
    }

    fun tneRuleDescription(quantity: Double): String = when {
        quantity < 5.0 -> "Rule: Rejected (Minimum 5g)"
        quantity <= 50.0 -> "Rule: 9% of quantity"
        quantity <= 100.0 -> "Rule: 4.5 g/ml fixed"
        quantity <= 200.0 -> "Rule: 4.5% of quantity"
        quantity <= 300.0 -> "Rule: 9 g/ml fixed"
        quantity <= 500.0 -> "Rule: 3% of quantity"
        quantity <= 1000.0 -> "Rule: 15 g/ml fixed"
        quantity <= 10_000.0 -> "Rule: 1.5% of quantity"
        quantity <= 15_000.0 -> "Rule: 150 g/ml fixed"
        else -> "Rule: 1% of quantity"
    }

    /**
     * OIML R 51 Class XIII(1) Maximum Permissible Standard Deviation lookup table.
     * Based on net load m in grams, multiplied by designation factor x and accuracy class factor.
     */
    fun calculateOimlR51MaxStdDev(
        m: Double,
        accuracyClass: String = "XIII",
        designationFactorX: Double = 1.0
    ): Double {
        if (m <= 0.0) return 0.0
        val baseStdDev = when {
            m <= 50.0 -> (0.48 / 100.0) * m
            m <= 100.0 -> 0.24
            m <= 200.0 -> (0.24 / 100.0) * m
            m <= 300.0 -> 0.48
            m <= 500.0 -> (0.16 / 100.0) * m
            m <= 1000.0 -> 0.8
            m <= 10_000.0 -> (0.08 / 100.0) * m
            m <= 15_000.0 -> 8.0
            else -> (0.053 / 100.0) * m
        }

        val classFactor = when {
            accuracyClass.equals("XI", ignoreCase = true) || accuracyClass.contains("(0.1)", ignoreCase = true) -> 0.1
            accuracyClass.equals("XII", ignoreCase = true) || accuracyClass.contains("(0.25)", ignoreCase = true) -> 0.25
            accuracyClass.equals("XIIII", ignoreCase = true) || accuracyClass.contains("(2)", ignoreCase = true) -> 2.0
            else -> 1.0
        }

        val effectiveFactor = if (designationFactorX > 0.0) designationFactorX else 1.0
        return roundTo(baseStdDev * effectiveFactor * classFactor, 3)
    }

    /**
     * OIML R 51 Class XIII Mean Error Maximum Permissible Error (MPE) based on m / e.
     */
    fun calculateOimlR51Mpe(
        m: Double,
        scaleIntervalE: Double,
        inspectionType: OimlInspectionType = OimlInspectionType.INITIAL_VERIFICATION,
        accuracyClass: String = "XIII",
        designationFactorX: Double = 1.0
    ): Double {
        val e = if (scaleIntervalE > 0.0) scaleIntervalE else 1.0
        val ratio = abs(m) / e

        val baseMpe = when {
            ratio <= 500.0 -> 0.5 * e
            ratio <= 2000.0 -> 1.0 * e
            else -> 1.5 * e
        }

        val inspectionMultiplier = if (inspectionType == OimlInspectionType.IN_SERVICE) 2.0 else 1.0

        val factorX = when {
            accuracyClass.contains("(0.5)", ignoreCase = true) -> 0.5
            accuracyClass.contains("(0.2)", ignoreCase = true) -> 0.2
            accuracyClass.contains("(0.1)", ignoreCase = true) -> 0.1
            accuracyClass.contains("(2)", ignoreCase = true) -> 2.0
            designationFactorX > 0.0 -> designationFactorX
            else -> 1.0
        }

        val classFactor = when {
            accuracyClass.startsWith("XI", ignoreCase = true) && !accuracyClass.startsWith("XII", ignoreCase = true) -> 0.1
            accuracyClass.startsWith("XIIII", ignoreCase = true) -> 2.0
            accuracyClass.startsWith("XII", ignoreCase = true) -> 0.25
            else -> 1.0
        }
        val totalFactor = if (accuracyClass.contains("(")) factorX else (factorX * classFactor)
        return roundTo(baseMpe * inspectionMultiplier * totalFactor, 3)
    }

    fun calculateOimlR51Mpe(
        load: Double,
        scaleInterval: Double,
        accuracyClass: String = ""
    ): Double {
        return calculateOimlR51Mpe(
            m = load,
            scaleIntervalE = scaleInterval,
            inspectionType = OimlInspectionType.INITIAL_VERIFICATION,
            accuracyClass = accuracyClass,
            designationFactorX = 1.0
        )
    }

    fun getOimlMinSampleCount(loadGram: Double): Int {
        return when {
            loadGram <= 1000.0 -> 60
            loadGram <= 10_000.0 -> 30
            loadGram <= 20_000.0 -> 20
            else -> 10
        }
    }

    fun evaluateToleranceLimits(
        config: ToleranceConfig,
        scaleIntervalE: Double,
        referenceWeight: Double
    ): EvaluatedToleranceLimits {
        val e = if (scaleIntervalE > 0.0) scaleIntervalE else 1.0
        val hasRef = referenceWeight > 0.0
        val ref = if (hasRef) referenceWeight else 0.0

        val effectiveMode = if (config.oimlComplianceModeEnabled) ToleranceMode.OIML_R51_BENCHMARK else config.mode

        return when (effectiveMode) {
            ToleranceMode.NONE -> {
                EvaluatedToleranceLimits(
                    mode = ToleranceMode.NONE,
                    allowedStaticMpe = Double.MAX_VALUE,
                    allowedMaxMeanError = Double.MAX_VALUE,
                    allowedMaxStdDev = Double.MAX_VALUE,
                    staticMpeLabel = "Advisory",
                    maxMeanErrorLabel = "Advisory",
                    maxStdDevLabel = "Advisory",
                    description = "None (Advisory Only): No tolerance limits enforced; all weighing errors will be recorded for advisory purposes."
                )
            }

            ToleranceMode.MANUAL_SPEC -> {
                val meanErrGrams = if (config.manualMeanErrorUnit == "PERCENT") {
                    if (hasRef) roundTo((config.manualMeanErrorValue / 100.0) * ref, 3) else 0.0
                } else {
                    roundTo(config.manualMeanErrorValue, 3)
                }

                val stdDevGrams = if (config.manualStdDevUnit == "PERCENT") {
                    if (hasRef) roundTo((config.manualStdDevValue / 100.0) * ref, 3) else 0.0
                } else {
                    roundTo(config.manualStdDevValue, 3)
                }

                val staticMpeLabel = if (config.manualMeanErrorUnit == "PERCENT") {
                    if (hasRef) "±${"%.2f".format(config.manualMeanErrorValue)}% (±${"%.2f".format(meanErrGrams)}g)" else "±${"%.2f".format(config.manualMeanErrorValue)}% of Ref Wt"
                } else {
                    "±${"%.2f".format(meanErrGrams)}g"
                }

                val maxMeanErrorLabel = staticMpeLabel

                val maxStdDevLabel = if (config.manualStdDevUnit == "PERCENT") {
                    if (hasRef) "${"%.2f".format(config.manualStdDevValue)}% (${"%.2f".format(stdDevGrams)}g)" else "${"%.2f".format(config.manualStdDevValue)}% of Ref Wt"
                } else {
                    "${"%.2f".format(stdDevGrams)}g"
                }

                val desc = if (hasRef) {
                    "Manual Spec (@ ${"%.1f".format(ref)}g ref): Max Mean Error $staticMpeLabel, Max Std Dev $maxStdDevLabel"
                } else {
                    "Manual Spec: Max Mean Error $staticMpeLabel, Max Std Dev $maxStdDevLabel"
                }

                EvaluatedToleranceLimits(
                    mode = ToleranceMode.MANUAL_SPEC,
                    allowedStaticMpe = if (config.manualMeanErrorUnit == "PERCENT" && !hasRef) Double.MAX_VALUE else meanErrGrams,
                    allowedMaxMeanError = if (config.manualMeanErrorUnit == "PERCENT" && !hasRef) Double.MAX_VALUE else meanErrGrams,
                    allowedMaxStdDev = if (config.manualStdDevUnit == "PERCENT" && !hasRef) Double.MAX_VALUE else stdDevGrams,
                    staticMpeLabel = staticMpeLabel,
                    maxMeanErrorLabel = maxMeanErrorLabel,
                    maxStdDevLabel = maxStdDevLabel,
                    description = desc
                )
            }

            ToleranceMode.DIVISION_BASED -> {
                val staticMult = if (config.staticDivisionMultiplier > 0.0) config.staticDivisionMultiplier else 1.0
                val dynamicMult = if (config.dynamicDivisionMultiplier > 0.0) config.dynamicDivisionMultiplier else 2.0

                val staticMpe = roundTo(staticMult * e, 3)
                val maxMeanErr = roundTo(dynamicMult * e, 3)
                val maxStdDev = roundTo(dynamicMult * e, 3)

                val staticMpeLabel = "±${"%.2f".format(staticMpe)}g (${staticMult}d)"
                val maxMeanErrorLabel = "±${"%.2f".format(maxMeanErr)}g (${dynamicMult}d)"
                val maxStdDevLabel = "${"%.2f".format(maxStdDev)}g (${dynamicMult}d)"

                EvaluatedToleranceLimits(
                    mode = ToleranceMode.DIVISION_BASED,
                    allowedStaticMpe = staticMpe,
                    allowedMaxMeanError = maxMeanErr,
                    allowedMaxStdDev = maxStdDev,
                    staticMpeLabel = staticMpeLabel,
                    maxMeanErrorLabel = maxMeanErrorLabel,
                    maxStdDevLabel = maxStdDevLabel,
                    description = "Division-Based (Scale Interval d=${"%.2f".format(e)}g): Static MPE $staticMpeLabel, Dyn Mean Error $maxMeanErrorLabel, Max Std Dev $maxStdDevLabel"
                )
            }

            ToleranceMode.PACKAGED_GOODS_TNE -> {
                val qn = if (config.nominalPackageWeightQn > 0.0) config.nominalPackageWeightQn else ref
                val pct = config.stdDevPercentageOfTne.coerceIn(5.0, 20.0)

                if (qn > 0.0) {
                    val tne = calculateTne(qn)
                    val maxStdDev = roundTo((pct / 100.0) * tne, 3)
                    val maxMeanErr = roundTo(0.20 * tne, 3)
                    val staticMpe = roundTo(0.10 * tne, 3)

                    EvaluatedToleranceLimits(
                        mode = ToleranceMode.PACKAGED_GOODS_TNE,
                        allowedStaticMpe = staticMpe,
                        allowedMaxMeanError = maxMeanErr,
                        allowedMaxStdDev = maxStdDev,
                        tne = tne,
                        staticMpeLabel = "±${"%.2f".format(staticMpe)}g (10% TNE)",
                        maxMeanErrorLabel = "±${"%.2f".format(maxMeanErr)}g (20% TNE)",
                        maxStdDevLabel = "${"%.2f".format(maxStdDev)}g (${"%.0f".format(pct)}% TNE)",
                        description = "UK Packaged Goods Regs 2006 (Qn=${"%.1f".format(qn)}g, TNE=${"%.2f".format(tne)}g): Static MPE ±${"%.2f".format(staticMpe)}g, Max Mean Error ±${"%.2f".format(maxMeanErr)}g, Max Std Dev ${"%.2f".format(maxStdDev)}g"
                    )
                } else {
                    EvaluatedToleranceLimits(
                        mode = ToleranceMode.PACKAGED_GOODS_TNE,
                        allowedStaticMpe = Double.MAX_VALUE,
                        allowedMaxMeanError = Double.MAX_VALUE,
                        allowedMaxStdDev = Double.MAX_VALUE,
                        staticMpeLabel = "10% of TNE",
                        maxMeanErrorLabel = "20% of TNE",
                        maxStdDevLabel = "${"%.0f".format(pct)}% of TNE",
                        description = "UK Packaged Goods Regs 2006: Evaluates TNE based on Target Pack Weight Qn (Static MPE = 10% TNE, Dyn Mean Error = 20% TNE, Max Std Dev = ${"%.0f".format(pct)}% TNE)."
                    )
                }
            }

            ToleranceMode.OIML_R51_BENCHMARK -> {
                val evalRef = if (hasRef) ref else 100.0
                val staticMpe = calculateOimlR51Mpe(
                    m = evalRef,
                    scaleIntervalE = e,
                    inspectionType = config.inspectionType,
                    accuracyClass = config.accuracyClass,
                    designationFactorX = config.designationFactorX
                )

                val maxMeanErr = staticMpe

                val maxStdDev = calculateOimlR51MaxStdDev(
                    m = evalRef,
                    accuracyClass = config.accuracyClass,
                    designationFactorX = config.designationFactorX
                )

                val inspLabel = if (config.inspectionType == OimlInspectionType.IN_SERVICE) "In-Service" else "Initial Verification"

                val staticMpeLabel = if (hasRef) "±${"%.2f".format(staticMpe)}g" else "OIML Class MPE"
                val maxMeanErrorLabel = staticMpeLabel
                val maxStdDevLabel = if (hasRef) "${"%.2f".format(maxStdDev)}g" else "OIML Max Std Dev"

                val desc = if (hasRef) {
                    "OIML R 51 Class ${config.accuracyClass} (${config.designationFactorX}x, $inspLabel @ ${"%.1f".format(evalRef)}g ref): MPE ±${"%.2f".format(staticMpe)}g, Max Std Dev ${"%.2f".format(maxStdDev)}g"
                } else {
                    "OIML R 51 Class ${config.accuracyClass} (${config.designationFactorX}x, $inspLabel): Enforces OIML Class MPE and Max Std Dev limits based on load."
                }

                EvaluatedToleranceLimits(
                    mode = ToleranceMode.OIML_R51_BENCHMARK,
                    allowedStaticMpe = staticMpe,
                    allowedMaxMeanError = maxMeanErr,
                    allowedMaxStdDev = maxStdDev,
                    staticMpeLabel = staticMpeLabel,
                    maxMeanErrorLabel = maxMeanErrorLabel,
                    maxStdDevLabel = maxStdDevLabel,
                    description = desc
                )
            }
        }
    }

    fun calculateSampleStdDev(readings: List<Double>): Double {
        if (readings.size < 2) return 0.0
        val mean = readings.sum() / readings.size
        val sumSquaredDiffs = readings.sumOf { (it - mean) * (it - mean) }
        return sqrt(sumSquaredDiffs / (readings.size - 1))
    }

    // --- Legacy Evaluation Methods for Backwards Compatibility ---

    fun evaluate(
        nominalWeight: Double,
        referenceWeight: Double,
        passes: List<Double>,
        effectiveTne: Double? = null,
        toleranceSource: ToleranceSource = ToleranceSource.SNB_GUIDANCE,
        generalRejectPassed: Boolean = true,
        tareConfigString: String = "",
        applyTareOffset: Boolean = false
    ): CheckweigherCalculationResult {
        val tne = effectiveTne ?: calculateTne(nominalWeight)
        val t1 = roundTo(nominalWeight - tne, 2)
        val t2 = roundTo(nominalWeight - (2.0 * tne), 2)

        val b20 = roundTo(tne * 0.20, 2)
        val b25 = roundTo(tne * 0.25, 2)

        val tareValue = if (applyTareOffset) tareConfigString.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0 else 0.0
        val effectiveRefWeight = referenceWeight - tareValue

        val passCount = passes.size
        val mean = if (passCount > 0) roundTo(passes.sum() / passCount, 3) else null
        val meanDynamicError = mean?.let { roundTo(it - effectiveRefWeight, 3) }

        val sRaw = if (passCount >= 2) calculateSampleStdDev(passes) else 0.0
        val sampleStdDev = if (passCount >= 2) roundTo(sRaw, 4) else null
        val sixSigmaZoi = sampleStdDev?.let { roundTo(it * 6.0, 3) }

        val exceeds25 = sixSigmaZoi?.let { it > b25 } ?: false
        val setPointAllowance = if (exceeds25 && sixSigmaZoi != null) roundTo(sixSigmaZoi - b25, 3) else null

        val equipmentSuitabilityPass = meanDynamicError?.let { abs(it) <= b20 } ?: true

        val overallStatus = when {
            !generalRejectPassed -> CheckweigherStatus.FAIL
            !equipmentSuitabilityPass -> CheckweigherStatus.FAIL
            exceeds25 -> CheckweigherStatus.FURTHER_ASSESSMENT_REQUIRED
            else -> CheckweigherStatus.PASS
        }

        return CheckweigherCalculationResult(
            nominalWeight = nominalWeight,
            referenceWeight = referenceWeight,
            effectiveReferenceWeight = effectiveRefWeight,
            tareOffsetApplied = tareValue,
            tne = tne,
            t1 = t1,
            t2 = t2,
            benchmark20PercentTne = b20,
            benchmark25PercentTne = b25,
            passCount = passCount,
            mean = mean,
            meanDynamicError = meanDynamicError,
            sampleStdDev = sampleStdDev,
            sixSigmaZoi = sixSigmaZoi,
            exceeds25PercentTneLimit = exceeds25,
            additionalSetPointAllowance = setPointAllowance,
            equipmentSuitabilityPass = equipmentSuitabilityPass,
            overallStatus = overallStatus
        )
    }

    fun evaluateOffCentreTest(
        testWeightStr: String,
        centreStr: String,
        c1Str: String,
        c2Str: String,
        c3Str: String,
        c4Str: String,
        scaleIntervalStr: String = ""
    ): OffCentreTestResult? {
        val centre = centreStr.toDoubleOrNull() ?: return null
        val c1 = c1Str.toDoubleOrNull() ?: return null
        val c2 = c2Str.toDoubleOrNull() ?: return null
        val c3 = c3Str.toDoubleOrNull() ?: return null
        val c4 = c4Str.toDoubleOrNull() ?: return null
        val testWeight = testWeightStr.toDoubleOrNull()

        val d1 = roundTo(abs(c1 - centre), 3)
        val d2 = roundTo(abs(c2 - centre), 3)
        val d3 = roundTo(abs(c3 - centre), 3)
        val d4 = roundTo(abs(c4 - centre), 3)

        val maxDev = maxOf(d1, d2, d3, d4)
        val maxCornerName = when (maxDev) {
            d1 -> "Corner 1 (Front Left)"
            d2 -> "Corner 2 (Front Right)"
            d3 -> "Corner 3 (Back Left)"
            else -> "Corner 4 (Back Right)"
        }

        val interval = scaleIntervalStr.toDoubleOrNull() ?: 0.1
        val allowedLimit = if (interval > 0.0) interval else 1.0
        val isPass = maxDev <= allowedLimit

        return OffCentreTestResult(
            testWeight = testWeight,
            centre = centre,
            corner1 = c1,
            corner2 = c2,
            corner3 = c3,
            corner4 = c4,
            devCorner1 = d1,
            devCorner2 = d2,
            devCorner3 = d3,
            devCorner4 = d4,
            maxDeviation = maxDev,
            maxCornerName = maxCornerName,
            isPass = isPass
        )
    }

    fun evaluateRepeatabilityTest(
        testWeightStr: String,
        r1Str: String,
        r2Str: String,
        r3Str: String,
        z1Str: String = "",
        z2Str: String = "",
        z3Str: String = "",
        scaleIntervalStr: String = ""
    ): RepeatabilityTestResult? {
        val r1 = r1Str.toDoubleOrNull() ?: return null
        val r2 = r2Str.toDoubleOrNull() ?: return null
        val r3 = r3Str.toDoubleOrNull() ?: return null
        val testWeight = testWeightStr.toDoubleOrNull()

        val z1 = z1Str.toDoubleOrNull()
        val z2 = z2Str.toDoubleOrNull()
        val z3 = z3Str.toDoubleOrNull()

        val mean = roundTo((r1 + r2 + r3) / 3.0, 3)
        val minVal = minOf(r1, r2, r3)
        val maxVal = maxOf(r1, r2, r3)
        val range = roundTo(maxVal - minVal, 3)

        val zeroes = listOfNotNull(z1, z2, z3)
        val maxZeroReturn = if (zeroes.isNotEmpty()) {
            roundTo(zeroes.maxOf { abs(it) }, 3)
        } else null

        val interval = scaleIntervalStr.toDoubleOrNull() ?: 0.1
        val allowedLimit = if (interval > 0.0) interval else 1.0

        val isLoadPass = range <= allowedLimit
        val isZeroPass = maxZeroReturn == null || maxZeroReturn <= allowedLimit
        val isPass = isLoadPass && isZeroPass

        return RepeatabilityTestResult(
            testWeight = testWeight,
            reading1 = r1,
            reading2 = r2,
            reading3 = r3,
            zero1 = z1,
            zero2 = z2,
            zero3 = z3,
            mean = mean,
            minReading = minVal,
            maxReading = maxVal,
            range = range,
            maxZeroReturn = maxZeroReturn,
            isPass = isPass
        )
    }

    fun calculateDynamicEccentricityLimit(
        toleranceSource: String,
        testLoad: Double,
        scaleInterval: Double,
        accuracyClass: String,
        manualType: String,
        manualValueStr: String,
        divisionMultiplierStr: String,
        tnePackWeightStr: String,
        oimlClassStr: String
    ): Pair<Double, String> {
        val e = if (scaleInterval > 0.0) scaleInterval else 1.0
        return when (toleranceSource) {
            "MANUAL_SPEC" -> {
                val valNum = manualValueStr.toDoubleOrNull() ?: 1.0
                if (manualType.equals("PERCENT", ignoreCase = true)) {
                    val limit = roundTo((valNum / 100.0) * testLoad, 3)
                    Pair(limit, "Manual Spec (±${valNum}% = ±${"%.2f".format(limit)}g)")
                } else {
                    val limit = roundTo(valNum, 3)
                    Pair(limit, "Manual Spec (±${"%.2f".format(limit)}g)")
                }
            }
            "DIVISION_BASED" -> {
                val mult = divisionMultiplierStr.toDoubleOrNull() ?: 1.0
                val limit = roundTo(mult * e, 3)
                val multStr = if (mult % 1.0 == 0.0) "${mult.toInt()}" else "$mult"
                Pair(limit, "Division-Based (±${multStr}d = ±${"%.2f".format(limit)}g)")
            }
            "PACKAGED_GOODS" -> {
                val packWeight = tnePackWeightStr.toDoubleOrNull() ?: testLoad
                val tne = calculateTne(packWeight)
                val limit = roundTo(0.10 * tne, 3)
                Pair(limit, "Packaged Goods (10% TNE = ±${"%.2f".format(limit)}g)")
            }
            else -> {
                val effectiveClass = oimlClassStr.ifBlank { accuracyClass }
                val limit = calculateOimlR51Mpe(testLoad, e, effectiveClass)
                val classLabel = if (effectiveClass.contains("XIII(2)", ignoreCase = true)) "XIII(2) In-Service" else "XIII(1)"
                Pair(limit, "OIML R 51 ($classLabel MPE = ±${"%.2f".format(limit)}g)")
            }
        }
    }

    fun evaluateDynamicEccentricityTest(
        testLoadStr: String,
        frontReadingStr: String,
        backReadingStr: String,
        centreReadingStr: String = "",
        scaleIntervalStr: String = "",
        accuracyClass: String = "",
        toleranceSource: String = "OIML_R51",
        manualType: String = "GRAMS",
        manualValueStr: String = "",
        divisionMultiplierStr: String = "1",
        tnePackWeightStr: String = "",
        oimlClassStr: String = "XIII(1)",
        allowedMpeOverride: Double? = null,
        toleranceDescriptionOverride: String? = null
    ): DynamicEccentricityTestResult? {
        val testLoad = testLoadStr.toDoubleOrNull() ?: return null
        val frontReading = frontReadingStr.toDoubleOrNull() ?: return null
        val backReading = backReadingStr.toDoubleOrNull() ?: return null
        val centreReading = centreReadingStr.toDoubleOrNull()
        val scaleInterval = scaleIntervalStr.toDoubleOrNull() ?: 1.0

        val baseline = centreReading ?: testLoad

        val frontError = roundTo(frontReading - baseline, 3)
        val backError = roundTo(backReading - baseline, 3)
        val maxError = roundTo(maxOf(abs(frontError), abs(backError)), 3)

        val (mpe, toleranceDescription) = if (allowedMpeOverride != null && toleranceDescriptionOverride != null) {
            Pair(allowedMpeOverride, toleranceDescriptionOverride)
        } else {
            calculateDynamicEccentricityLimit(
                toleranceSource = toleranceSource,
                testLoad = testLoad,
                scaleInterval = scaleInterval,
                accuracyClass = accuracyClass,
                manualType = manualType,
                manualValueStr = manualValueStr,
                divisionMultiplierStr = divisionMultiplierStr,
                tnePackWeightStr = tnePackWeightStr,
                oimlClassStr = oimlClassStr
            )
        }

        val isPass = maxError <= mpe

        return DynamicEccentricityTestResult(
            testLoad = testLoad,
            referenceWeight = baseline,
            frontReading = frontReading,
            backReading = backReading,
            centreReading = centreReading,
            frontError = frontError,
            backError = backError,
            maxError = maxError,
            mpe = mpe,
            scaleInterval = scaleInterval,
            accuracyClass = accuracyClass,
            toleranceSource = toleranceSource,
            toleranceDescription = toleranceDescription,
            isPass = isPass
        )
    }

    fun evaluateStaticLinearityTest(
        maxCapacity: Double,
        appliedRefWeights: List<Double>,
        displayedReadings: List<Double>,
        allowedStaticMpe: Double
    ): StaticLinearityTestResult {
        val percentages = listOf(0, 25, 50, 75, 100)
        val points = mutableListOf<StaticLinearityPointResult>()
        var overallPass = true

        for (i in 0..4) {
            val targetRef = roundTo((percentages[i] / 100.0) * maxCapacity, 3)
            val appliedRef = appliedRefWeights.getOrNull(i) ?: targetRef
            val disp = displayedReadings.getOrNull(i) ?: 0.0

            val err = roundTo(disp - appliedRef, 3)
            val pass = abs(err) <= allowedStaticMpe

            if (!pass) overallPass = false

            points.add(
                StaticLinearityPointResult(
                    pointIndex = i + 1,
                    percentCapacity = percentages[i],
                    targetRefWeight = targetRef,
                    appliedRefWeight = appliedRef,
                    displayedReading = disp,
                    error = err,
                    allowedMpe = allowedStaticMpe,
                    isPass = pass
                )
            )
        }

        return StaticLinearityTestResult(
            points = points,
            isOverallPass = overallPass
        )
    }

    fun evaluateStaticEccentricityTest(
        testWeight: Double,
        centre: Double,
        frontLeft: Double,
        frontRight: Double,
        rearLeft: Double,
        rearRight: Double,
        allowedStaticMpe: Double
    ): EvaluatedOffCentreTestResult {
        val dFL = roundTo(abs(frontLeft - centre), 3)
        val dFR = roundTo(abs(frontRight - centre), 3)
        val dRL = roundTo(abs(rearLeft - centre), 3)
        val dRR = roundTo(abs(rearRight - centre), 3)

        val maxDev = maxOf(dFL, dFR, dRL, dRR)
        val maxCornerName = when (maxDev) {
            dFL -> "Front Left"
            dFR -> "Front Right"
            dRL -> "Rear Left"
            else -> "Rear Right"
        }

        val isPass = maxDev <= allowedStaticMpe

        return EvaluatedOffCentreTestResult(
            testWeight = testWeight,
            centre = centre,
            frontLeft = frontLeft,
            frontRight = frontRight,
            rearLeft = rearLeft,
            rearRight = rearRight,
            devFrontLeft = dFL,
            devFrontRight = dFR,
            devRearLeft = dRL,
            devRearRight = dRR,
            maxCornerDeviation = maxDev,
            maxCornerName = maxCornerName,
            allowedLimit = allowedStaticMpe,
            isPass = isPass
        )
    }

    fun evaluateDynamicRepeatability(
        referenceWeight: Double,
        beltSpeed: Double,
        beltSpeedUnit: String = "m/min",
        readings: List<Double>,
        allowedMaxMeanError: Double,
        allowedMaxStdDev: Double,
        isOimlComplianceMode: Boolean = false
    ): DynamicRepeatabilityResult {
        val count = readings.size
        val minRequired = if (isOimlComplianceMode) getOimlMinSampleCount(referenceWeight) else 10

        val mean = if (count > 0) roundTo(readings.sum() / count, 3) else 0.0
        val meanError = roundTo(mean - referenceWeight, 3)
        val s = if (count >= 2) roundTo(calculateSampleStdDev(readings), 4) else 0.0
        val threeSigma = roundTo(3.0 * s, 3)
        val accuracyBand = roundTo(abs(meanError) + threeSigma, 3)

        val isMeanErrorPass = abs(meanError) <= allowedMaxMeanError
        val isStdDevPass = s <= allowedMaxStdDev
        val isSampleSufficient = count >= minRequired

        val overallPass = isMeanErrorPass && isStdDevPass && isSampleSufficient

        return DynamicRepeatabilityResult(
            referenceWeight = referenceWeight,
            beltSpeed = beltSpeed,
            beltSpeedUnit = beltSpeedUnit,
            runCount = count,
            minRequiredRuns = minRequired,
            readings = readings,
            dynamicMean = mean,
            dynamicMeanError = meanError,
            sampleStdDev = s,
            threeSigma = threeSigma,
            dynamicAccuracyBand = accuracyBand,
            allowedMaxMeanError = allowedMaxMeanError,
            allowedMaxStdDev = allowedMaxStdDev,
            isMeanErrorPass = isMeanErrorPass,
            isStdDevPass = isStdDevPass,
            isSampleCountSufficient = isSampleSufficient,
            isOverallPass = overallPass
        )
    }

    fun evaluateRejectMechanism(
        underweightResult: String,
        overweightResult: String,
        timingResult: String
    ): RejectMechanismResult {
        val passUnder = underweightResult.equals("PASS", ignoreCase = true) || underweightResult.equals("N/A", ignoreCase = true)
        val passOver = overweightResult.equals("PASS", ignoreCase = true) || overweightResult.equals("N/A", ignoreCase = true)
        val passTiming = timingResult.equals("PASS", ignoreCase = true) || timingResult.equals("N/A", ignoreCase = true)

        val isOverallPass = passUnder && passOver && passTiming

        return RejectMechanismResult(
            underweightRejectResult = underweightResult,
            overweightRejectResult = overweightResult,
            timingVerificationResult = timingResult,
            isOverallPass = isOverallPass
        )
    }
}
