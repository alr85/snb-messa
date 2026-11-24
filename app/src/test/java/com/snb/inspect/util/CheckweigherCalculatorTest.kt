package com.snb.inspect.util

import org.junit.Assert.*
import org.junit.Test

class CheckweigherCalculatorTest {

    @Test
    fun testTneFromGovTable() {
        // < 50g -> 9%
        assertEquals(3.6, CheckweigherCalculator.calculateTne(40.0), 0.01)
        // 50..100g -> 4.5g
        assertEquals(4.5, CheckweigherCalculator.calculateTne(80.0), 0.01)
        // 100..200g -> 4.5%
        assertEquals(6.75, CheckweigherCalculator.calculateTne(150.0), 0.01)
        // 200..300g -> 9g
        assertEquals(9.0, CheckweigherCalculator.calculateTne(250.0), 0.01)
        // 300..500g -> 3%
        assertEquals(12.0, CheckweigherCalculator.calculateTne(400.0), 0.01)
        // 500..1000g -> 15g
        assertEquals(15.0, CheckweigherCalculator.calculateTne(800.0), 0.01)
        // 1000..10000g -> 1.5%
        assertEquals(30.0, CheckweigherCalculator.calculateTne(2000.0), 0.01)
    }

    @Test
    fun test30PassDynamicWeighingStatisticsAndPassResult() {
        val nominal = 500.0
        val staticRef = 500.0
        // 30 passes with slight random variation around 500.1g
        val passes = List(30) { i -> 500.0 + (if (i % 2 == 0) 0.1 else -0.1) }

        val res = CheckweigherCalculator.evaluate(
            nominalWeight = nominal,
            referenceWeight = staticRef,
            passes = passes
        )

        assertEquals(15.0, res.tne, 0.01)
        assertEquals(3.0, res.benchmark20PercentTne, 0.01)
        assertEquals(3.75, res.benchmark25PercentTne, 0.01)
        assertEquals(30, res.passCount)
        assertEquals(500.0, res.mean!!, 0.05)
        assertEquals(0.0, res.meanDynamicError!!, 0.05)
        assertTrue(res.sixSigmaZoi!! < res.benchmark25PercentTne)
        assertFalse(res.exceeds25PercentTneLimit)
        assertEquals(CheckweigherStatus.PASS, res.overallStatus)
    }

    @Test
    fun testTareOffsetAdjustmentWhenStaticRefIsGrossWeight() {
        val nominal = 200.0
        val grossStaticRef = 215.0 // Pack + 15g Tare container
        val netPasses = List(30) { 200.0 } // Checkweigher indicating net 200g

        val res = CheckweigherCalculator.evaluate(
            nominalWeight = nominal,
            referenceWeight = grossStaticRef,
            passes = netPasses,
            tareConfigString = "15.0",
            applyTareOffset = true
        )

        assertEquals(200.0, res.effectiveReferenceWeight, 0.01)
        assertEquals(15.0, res.tareOffsetApplied, 0.01)
        assertEquals(0.0, res.meanDynamicError!!, 0.01)
        assertEquals(CheckweigherStatus.PASS, res.overallStatus)
    }

    @Test
    fun testFurtherAssessmentRequiredWhenSixSigmaExceeds25PercentTneThreshold() {
        val nominal = 200.0 // TNE = 9.0g, 25% TNE = 2.25g
        val staticRef = 200.0
        // High variation passes causing 6s > 2.25g
        val passes = List(30) { i -> 200.0 + (if (i % 2 == 0) 1.5 else -1.5) }

        val res = CheckweigherCalculator.evaluate(
            nominalWeight = nominal,
            referenceWeight = staticRef,
            passes = passes
        )

        assertEquals(9.0, res.tne, 0.01)
        assertEquals(2.25, res.benchmark25PercentTne, 0.01)
        assertTrue(res.sixSigmaZoi!! > res.benchmark25PercentTne)
        assertTrue(res.exceeds25PercentTneLimit)
        assertNotNull(res.additionalSetPointAllowance)
        assertEquals(CheckweigherStatus.FURTHER_ASSESSMENT_REQUIRED, res.overallStatus)
    }

    @Test
    fun testGeneralRejectFailureTriggersOverallFail() {
        val nominal = 500.0
        val staticRef = 500.0
        val passes = List(30) { 500.0 }

        val res = CheckweigherCalculator.evaluate(
            nominalWeight = nominal,
            referenceWeight = staticRef,
            passes = passes,
            generalRejectPassed = false
        )

        assertEquals(CheckweigherStatus.FAIL, res.overallStatus)
    }

    @Test
    fun testEvaluateOffCentreTest() {
        val res = CheckweigherCalculator.evaluateOffCentreTest(
            testWeightStr = "100.0",
            centreStr = "100.0",
            c1Str = "100.05",
            c2Str = "99.95",
            c3Str = "100.0",
            c4Str = "100.02",
            scaleIntervalStr = "0.1"
        )

        assertNotNull(res)
        assertEquals(100.0, res!!.centre, 0.01)
        assertEquals(0.05, res.maxDeviation, 0.01)
        assertTrue(res.isPass)
    }

    @Test
    fun testEvaluateRepeatabilityTest() {
        val res = CheckweigherCalculator.evaluateRepeatabilityTest(
            testWeightStr = "200.0",
            r1Str = "200.0",
            r2Str = "200.05",
            r3Str = "199.98",
            z1Str = "0.0",
            z2Str = "0.01",
            z3Str = "-0.01",
            scaleIntervalStr = "0.1"
        )

        assertNotNull(res)
        assertEquals(200.01, res!!.mean, 0.01)
        assertEquals(0.07, res.range, 0.01)
        assertEquals(0.01, res.maxZeroReturn!!, 0.01)
        assertTrue(res.isPass)
    }

    @Test
    fun testCalculateOimlR51Mpe() {
        // Load 300g, e = 1g -> n = 300 <= 500 -> baseMpe = 0.5g
        // Class XIII(1) -> multiplier x = 1.0 -> MPE = 0.5g
        val mpe1 = CheckweigherCalculator.calculateOimlR51Mpe(300.0, 1.0, "XIII(1)")
        assertEquals(0.5, mpe1, 0.001)

        // Class XIII(0.5) -> multiplier x = 0.5 -> MPE = 0.25g
        val mpe2 = CheckweigherCalculator.calculateOimlR51Mpe(300.0, 1.0, "XIII(0.5)")
        assertEquals(0.25, mpe2, 0.001)

        // Load 800g, e = 1g -> 500 < n <= 2000 -> baseMpe = 1.0g
        // Class XII(2) -> multiplier x = 2.0 -> MPE = 2.0g
        val mpe3 = CheckweigherCalculator.calculateOimlR51Mpe(800.0, 1.0, "XII(2)")
        assertEquals(2.0, mpe3, 0.001)
    }

    @Test
    fun testEvaluateDynamicEccentricityTestPassAndFail() {
        // Test load 500g (1/3 Max + Tare representative pack), e = 1g, MPE = 0.5g
        // Front pass = 500.2g (error +0.2g), Back pass = 499.7g (error -0.3g) -> max error 0.3g <= 0.5g -> PASS
        val passRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "500.2",
            backReadingStr = "499.7",
            scaleIntervalStr = "1.0",
            accuracyClass = "XIII(1)"
        )

        assertNotNull(passRes)
        assertEquals(500.0, passRes!!.referenceWeight, 0.01)
        assertEquals(0.2, passRes.frontError, 0.01)
        assertEquals(-0.3, passRes.backError, 0.01)
        assertEquals(0.3, passRes.maxError, 0.01)
        assertEquals(0.5, passRes.mpe, 0.01)
        assertTrue(passRes.isPass)

        // Failing case: Back pass = 501.2g (error +1.2g > 0.5g MPE) -> FAIL
        val failRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "500.1",
            backReadingStr = "501.2",
            scaleIntervalStr = "1.0",
            accuracyClass = "XIII(1)"
        )

        assertNotNull(failRes)
        assertEquals(1.2, failRes!!.maxError, 0.01)
        assertFalse(failRes.isPass)
    }

    @Test
    fun testDynamicEccentricityToleranceSources() {
        // 1. Manual / Customer Spec (±Grams: ±1.5g)
        val manualGramsRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "501.2",
            backReadingStr = "498.8",
            toleranceSource = "MANUAL_SPEC",
            manualType = "GRAMS",
            manualValueStr = "1.5"
        )
        assertNotNull(manualGramsRes)
        assertEquals(1.5, manualGramsRes!!.mpe, 0.01)
        assertEquals(1.2, manualGramsRes.maxError, 0.01)
        assertTrue(manualGramsRes.isPass)

        // 2. Manual / Customer Spec (±Percent: ±1% of 500g = ±5.0g)
        val manualPercentRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "503.0",
            backReadingStr = "498.0",
            toleranceSource = "MANUAL_SPEC",
            manualType = "PERCENT",
            manualValueStr = "1.0"
        )
        assertNotNull(manualPercentRes)
        assertEquals(5.0, manualPercentRes!!.mpe, 0.01)
        assertEquals(3.0, manualPercentRes.maxError, 0.01)
        assertTrue(manualPercentRes.isPass)

        // 3. Division-Based (±2d where d = 0.5g -> limit = 1.0g)
        val divRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "500.8",
            backReadingStr = "499.3",
            scaleIntervalStr = "0.5",
            toleranceSource = "DIVISION_BASED",
            divisionMultiplierStr = "2"
        )
        assertNotNull(divRes)
        assertEquals(1.0, divRes!!.mpe, 0.01)
        assertEquals(0.8, divRes.maxError, 0.01)
        assertTrue(divRes.isPass)

        // 4. Packaged Goods TNE Rule (10% of TNE for 500g = 10% of 15g = 1.5g)
        val tneRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "500.0",
            frontReadingStr = "501.2",
            backReadingStr = "498.9",
            toleranceSource = "PACKAGED_GOODS",
            tnePackWeightStr = "500.0"
        )
        assertNotNull(tneRes)
        assertEquals(1.5, tneRes!!.mpe, 0.01)
        assertEquals(1.2, tneRes.maxError, 0.01)
        assertTrue(tneRes.isPass)

        // 5. OIML R 51 Benchmark Class XIII(2) In-Service
        val oimlInServiceRes = CheckweigherCalculator.evaluateDynamicEccentricityTest(
            testLoadStr = "300.0",
            frontReadingStr = "300.8",
            backReadingStr = "299.4",
            scaleIntervalStr = "1.0",
            toleranceSource = "OIML_R51",
            oimlClassStr = "XIII(2)"
        )
        assertNotNull(oimlInServiceRes)
        assertEquals(1.0, oimlInServiceRes!!.mpe, 0.01)
        assertEquals(0.8, oimlInServiceRes.maxError, 0.01)
        assertTrue(oimlInServiceRes.isPass)
    }
}