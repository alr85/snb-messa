package com.example.mecca

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String?): String {
    // Define the input format (without 'T')
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Define the output format (desired format)
    val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

    // Parse the input date string to a Date object
    val date = inputFormat.parse(dateString)

    // Format the Date object to the desired output format and return it
    return outputFormat.format(date!!)
}
