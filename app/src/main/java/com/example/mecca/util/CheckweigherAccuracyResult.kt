package com.example.mecca.util

import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt

enum class PassFail { Pass, Fail }

data class CheckweigherAccuracyResult(
    val tNom: Double,
    val tne: Double,
    val t1: Double,
    val t2: Double,

    val sd: Double?,
    val zoi: Double?,
    val zoiPassFail: PassFail?,

    val knownMassPassFail: PassFail?
)

object CheckweigherAccuracyCalculator {

    private fun roundTo(x: Double, dp: Int): Double {
        val m = Math.pow(10.0, dp.toDouble())
        return round(x * m) / m
    }

    /**
     * Government table: TNE is either % of quantity or fixed grams/ml, depending on range.
     * Assumes tNom is in grams or millilitres (same numeric rules).
     */
    fun tneFromGovTable(quantity: Double): Double {
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

        return roundTo(tne, 1)
    }

    fun tneRuleText(quantity: Double): String = when {
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

    private fun sampleStdDev(values: List<Double>): Double? {
        if (values.size < 2) return null
        val mean = values.sum() / values.size
        val variance = values.sumOf { (it - mean) * (it - mean) } / (values.size - 1)
        return sqrt(variance)
    }

    fun calculate(
        tNom: Double,
        passes: List<Double?>,
        staticScale: Double?,
        checkweigher: Double?
    ): CheckweigherAccuracyResult {

        val tne = tneFromGovTable(tNom)
        val t1 = roundTo(tNom - tne, 1)
        val t2 = roundTo(tNom - 2.0 * tne, 1)

        val cleanPasses = passes.filterNotNull()
        
        // Logic Change: Require exactly 10 valid passes for ZOI result
        val sdRaw = if (cleanPasses.size == 10) sampleStdDev(cleanPasses) else null
        val sd = sdRaw?.let { roundTo(it, 3) }
        val zoi = sd?.let { it * 6.0 }

        val zoiPassFail = zoi?.let {
            val threshold = roundTo(tne * 0.25, 3)
            if (it <= threshold) PassFail.Pass else PassFail.Fail
        }

        val knownMassPassFail =
            if (staticScale != null && checkweigher != null) {
                val threshold = roundTo(tne / 5.0, 3)
                if (abs(staticScale - checkweigher) <= threshold) PassFail.Pass else PassFail.Fail
            } else null

        return CheckweigherAccuracyResult(
            tNom = tNom,
            tne = tne,
            t1 = t1,
            t2 = t2,
            sd = sd,
            zoi = zoi,
            zoiPassFail = zoiPassFail,
            knownMassPassFail = knownMassPassFail
        )
    }
}
