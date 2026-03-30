package com.snb.inspect.util

import com.snb.inspect.dataClasses.MetalDetectorWithFullDetails

sealed class SerialCheckResult {
    // Cloud answers
    data class Exists(val system: MetalDetectorWithFullDetails?) : SerialCheckResult()
    object NotFound : SerialCheckResult()

    // Fuzzy match (looks like an existing one but isn't exact)
    data class FuzzyMatch(val system: MetalDetectorWithFullDetails) : SerialCheckResult()

    // Offline answers from local cache/DB
    data class ExistsLocalOffline(val system: MetalDetectorWithFullDetails?) : SerialCheckResult()
    object NotFoundLocalOffline : SerialCheckResult()

    // We tried and failed (timeout, 500s, DNS tantrums)
    data class Error(val message: String?) : SerialCheckResult()
}
