package com.example.mecca.util

sealed class SerialCheckResult {
    // Cloud answers
    object Exists : SerialCheckResult()
    object NotFound : SerialCheckResult()

    // Offline answers from local cache/DB
    object ExistsLocalOffline : SerialCheckResult()
    object NotFoundLocalOffline : SerialCheckResult()

    // We tried and failed (timeout, 500s, DNS tantrums)
    data class Error(val message: String?) : SerialCheckResult()
}

