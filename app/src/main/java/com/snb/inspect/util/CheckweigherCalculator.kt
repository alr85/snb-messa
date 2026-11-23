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
    val mean: Double,
    val minReading: Double,
    val maxReading: Double,
    val range: Double,
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
    val repeatabilityReading3: String = ""
)

data class EvaluatedStaticTestSet(
    val testSet: StaticTestSet,
    val offCentreResult: OffCentreTestResult?,
    val repeatabilityResult: RepeatabilityTestResult?
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
        scaleIntervalStr: String = ""
    ): RepeatabilityTestResult? {
        val r1 = r1Str.toDoubleOrNull() ?: return null
        val r2 = r2Str.toDoubleOrNull() ?: return null
        val r3 = r3Str.toDoubleOrNull() ?: return null
        val testWeight = testWeightStr.toDoubleOrNull()

        val mean = roundTo((r1 + r2 + r3) / 3.0, 3)
        val minVal = minOf(r1, r2, r3)
        val maxVal = maxOf(r1, r2, r3)
        val range = roundTo(maxVal - minVal, 3)

        val interval = scaleIntervalStr.toDoubleOrNull() ?: 0.1
        val allowedLimit = if (interval > 0.0) interval else 1.0
        val isPass = range <= allowedLimit

        return RepeatabilityTestResult(
            testWeight = testWeight,
            reading1 = r1,
            reading2 = r2,
            reading3 = r3,
            mean = mean,
            minReading = minVal,
            maxReading = maxVal,
            range = range,
            isPass = isPass
        )
    }
}
