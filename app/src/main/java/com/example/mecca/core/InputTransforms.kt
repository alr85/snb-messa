package com.example.mecca.core

object InputTransforms {

    // Good for mm fields etc: accepts digits + one decimal separator
    val decimal: (String) -> String = { raw ->
        var cleaned = raw.replace(',', '.')

        // Keep only digits and dots
        cleaned = cleaned.filter { it.isDigit() || it == '.' }

        // Allow only one dot
        val firstDot = cleaned.indexOf('.')
        if (firstDot != -1) {
            val before = cleaned.substring(0, firstDot + 1)
            val after = cleaned.substring(firstDot + 1).replace(".", "")
            cleaned = before + after
        }

        cleaned
    }

    // Optional: digits only
    val digitsOnly: (String) -> String = { raw ->
        raw.filter { it.isDigit() }
    }
}
