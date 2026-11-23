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
            scaleIntervalStr = "0.1"
        )

        assertNotNull(res)
        assertEquals(200.01, res!!.mean, 0.01)
        assertEquals(0.07, res.range, 0.01)
        assertTrue(res.isPass)
    }
}