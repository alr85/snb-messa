package com.snb.inspect

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatDate(dateString: String?): String {
    if (dateString.isNullOrBlank()) return "N/A"

    // 1. Try ISO-8601 LocalDateTime (e.g., 2023-10-27T10:00:00.123)
    try {
        val ldt = LocalDateTime.parse(dateString)
        return ldt.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"))
    } catch (_: Exception) {}

    // 2. Try ISO-8601 OffsetDateTime (e.g., 2023-10-27T10:00:00Z or with offset)
    try {
        val odt = OffsetDateTime.parse(dateString)
        return odt.format(DateTimeFormatter.ofPattern("dd/MM/yy HH:mm"))
    } catch (_: Exception) {}

    // 3. Fallback to SimpleDateFormat for common legacy formats
    val formats = listOf(
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in formats) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val date = sdf.parse(dateString)
            if (date != null) {
                return SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(date)
            }
        } catch (_: Exception) {}
    }

    // 4. Final attempt: basic cleanup of common ISO-like strings
    // If it's something like "2023-10-27T10:00:00.1234567" and LocalDateTime.parse failed
    // (e.g. due to too many nano digits or other platform quirks)
    if (dateString.length >= 19 && dateString[10] == 'T') {
        try {
            val cleaned = dateString.substring(0, 19).replace('T', ' ')
            val sdfInput = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdfInput.parse(cleaned)
            if (date != null) {
                return SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(date)
            }
        } catch (_: Exception) {}
    }

    return dateString // Return raw if all else fails
}
