package com.example.mecca.util

fun capitaliseFirstChar(input: String): String {
    return input.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}
