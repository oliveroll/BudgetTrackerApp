package com.budgettracker.core.utils

/**
 * A generic wrapper for handling success and error states
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    
    inline fun <R> map(transform: (value: T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(message)
        }
    }
    
    inline fun onSuccess(action: (value: T) -> Unit): Result<T> {
        if (this is Success) {
            action(data)
        }
        return this
    }
    
    inline fun onError(action: (message: String) -> Unit): Result<T> {
        if (this is Error) {
            action(message)
        }
        return this
    }
}

