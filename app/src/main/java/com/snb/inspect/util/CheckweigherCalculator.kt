package com.snb.inspect.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt

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
    val benchmark20PercentTne: Double, // 20% TNE Equipment suitability threshold
    val benchmark25PercentTne: Double, // 25% TNE Zone of Indecision threshold
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

object CheckweigherCalculator {

    private fun roundTo(x: Double, dp: Int): Double {
        val m = 10.0.pow(dp.toDouble())
        return round(x * m) / m
    }

    /**
     * TNE calculation derived from UK Weights and Measures (Packaged Goods) Regulations 2006.
     * Quantity is in grams or millilitres.
     */
    fun calculateTne(quantity: Double): Double {
        val tne = when {
            quantity < 50.0 -> quantity * 0.09
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
     * Calculates sample standard deviation using N-1 formula.
     */
    fun calculateSampleStdDev(readings: List<Double>): Double? {
        if (readings.size < 2) return null
        val mean = readings.sum() / readings.size
        val sumSquaredDiffs = readings.sumOf { (it - mean) * (it - mean) }
        return sqrt(sumSquaredDiffs / (readings.size - 1))
    }

    /**
     * Evaluates dynamic weighing readings against reference weight and tolerance specifications.
     */
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

        val sRaw = if (passCount >= 2) calculateSampleStdDev(passes) else null
        val sampleStdDev = sRaw?.let { roundTo(it, 4) }
        val sixSigmaZoi = sampleStdDev?.let { roundTo(it * 6.0, 3) }

        val exceeds25 = sixSigmaZoi?.let { it > b25 } ?: false
        val setPointAllowance = if (exceeds25 && sixSigmaZoi != null) roundTo(sixSigmaZoi - b25, 3) else null

        // Equipment suitability check: Mean dynamic error should not exceed 20% TNE benchmark
        val equipmentSuitabilityPass = meanDynamicError?.let { abs(it) <= b20 } ?: true

        val overallStatus = when {
            !generalRejectPassed -> CheckweigherStatus.FAIL
            !equipmentSuitabilityPass -> CheckweigherStatus.FAIL
            exceeds25 -> CheckweigherStatus.FURTHER_ASSESSMENT_REQUIRED
            passCount >= 30 -> CheckweigherStatus.PASS
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

    /**
     * Evaluates static off-centre loading test (4 corners vs centre).
     */
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

    /**
     * Evaluates static 3-time repeatability test.
     */
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

    /**
     * Calculates Maximum Permissible Error (MPE) according to OIML R 51-1.
     * Scale verification interval 'e' and accuracy class designation multiplier 'x' are applied.
     */
    fun calculateOimlR51Mpe(
        load: Double,
        scaleInterval: Double,
        accuracyClass: String = ""
    ): Double {
        val e = if (scaleInterval > 0.0) scaleInterval else 1.0
        val n = abs(load) / e

        val baseMpe = when {
            n <= 500.0 -> 0.5 * e
            n <= 2000.0 -> 1.0 * e
            n <= 10000.0 -> 1.5 * e
            else -> 2.5 * e
        }

        val x = when {
            accuracyClass.contains("(0.5)", ignoreCase = true) -> 0.5
            accuracyClass.contains("(2)", ignoreCase = true) -> 2.0
            accuracyClass.contains("(1)", ignoreCase = true) -> 1.0
            accuracyClass.contains("Y(b)", ignoreCase = true) -> 1.5
            accuracyClass.contains("Y(a)", ignoreCase = true) -> 1.0
            else -> 1.0
        }

        return roundTo(baseMpe * x, 3)
    }

    /**
     * Calculates the dynamic eccentricity permissible error limit (MPE/Threshold) and description based on selected tolerance source.
     * Sources:
     * - MANUAL_SPEC: Custom ±g or ±% specification.
     * - DIVISION_BASED: Multiplier X * scaleInterval (±Xd).
     * - PACKAGED_GOODS: 10% of TNE based on target pack weight.
     * - OIML_R51: OIML R 51 MPE benchmark.
     */
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
            else -> { // "OIML_R51"
                val effectiveClass = oimlClassStr.ifBlank { accuracyClass }
                val limit = calculateOimlR51Mpe(testLoad, e, effectiveClass)
                val classLabel = if (effectiveClass.contains("XIII(2)", ignoreCase = true)) "XIII(2) In-Service" else "XIII(1)"
                Pair(limit, "OIML R 51 ($classLabel MPE = ±${"%.2f".format(limit)}g)")
            }
        }
    }

    /**
     * Evaluates Dynamic Eccentricity (Off-Centre Weighing) test passes (Front and Back edge passes).
     * Compares maximum deviation against permissible error limit according to selected tolerance source.
     */
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
        oimlClassStr: String = "XIII(1)"
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

        val (mpe, toleranceDescription) = calculateDynamicEccentricityLimit(
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
}
