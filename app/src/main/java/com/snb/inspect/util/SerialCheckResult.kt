package com.snb.inspect.util

sealed class SerialCheckResult<out T> {
    // Cloud answers
    data class Exists<T>(val system: T?) : SerialCheckResult<T>()
    object NotFound : SerialCheckResult<Nothing>()

    // Fuzzy match (looks like an existing one but isn't exact)
    data class FuzzyMatch<T>(val system: T) : SerialCheckResult<T>()

    // Offline answers from local cache/DB
    data class ExistsLocalOffline<T>(val system: T?) : SerialCheckResult<T>()
    object NotFoundLocalOffline : SerialCheckResult<Nothing>()

    // We tried and failed (timeout, 500s, DNS tantrums)
    data class Error(val message: String?) : SerialCheckResult<Nothing>()
}
