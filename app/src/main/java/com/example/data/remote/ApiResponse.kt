package com.example.data.remote

/**
 * Consistent success and error response wrapper for all AdventHearts API calls
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val code: String, val message: String) : ApiResponse<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error

    fun getOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): Error? = this as? Error
}

data class BackendResponseDto<T>(
    val success: Boolean,
    val data: T? = null,
    val error: BackendErrorDto? = null
)

data class BackendErrorDto(
    val code: String,
    val message: String
)
