package com.snb.inspect

sealed class FetchResult {
    data class Success(val message: String) : FetchResult()
    data class Failure(val errorMessage: String) : FetchResult()
}