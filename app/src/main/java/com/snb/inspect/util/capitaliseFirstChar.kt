package com.snb.inspect.util

fun capitaliseFirstChar(input: String): String {
    return input.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase() else char.toString()
    }
}
