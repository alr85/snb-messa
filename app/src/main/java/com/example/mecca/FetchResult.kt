package com.example.mecca

sealed class FetchResult {
    data class Success(val message: String) : FetchResult()
    data class Failure(val errorMessage: String) : FetchResult()
}