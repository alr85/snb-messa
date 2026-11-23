package com.snb.inspect

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun sensitivitySpaceRemoval_removesInternalAndSurroundingSpaces() {
        val rawInput = "1. 2"
        val cleaned = rawInput.replace("\\s+".toRegex(), "")
        assertEquals("1.2", cleaned)
    }

    @Test
    fun sensitivitySpaceRemoval_handlesNaAndCleanDecimals() {
        assertEquals("1.5", " 1. 5 ".replace("\\s+".toRegex(), ""))
        assertEquals("N/A", "N / A".replace("\\s+".toRegex(), ""))
        assertEquals("2.0", "2.0".replace("\\s+".toRegex(), ""))
    }
}