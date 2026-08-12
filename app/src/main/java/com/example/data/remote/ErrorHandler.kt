package com.example.data.remote

import android.util.Log
import retrofit2.Response

object ErrorHandler {
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<BackendResponseDto<T>>): ApiResponse<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    ApiResponse.Success(body.data)
                } else if (body?.error != null) {
                    ApiResponse.Error(body.error.code, body.error.message)
                } else {
                    ApiResponse.Error("UNEXPECTED_EMPTY_BODY", "Server returned empty or null response body")
                }
            } else {
                val errorMsg = parseErrorMessage(response)
                ApiResponse.Error("HTTP_${response.code()}", errorMsg)
            }
        } catch (e: Exception) {
            Log.e("ErrorHandler", "API Exception encountered: ${e.message}", e)
            ApiResponse.Error("NETWORK_ERROR", e.localizedMessage ?: "Network connection error")
        }
    }

    private fun parseErrorMessage(response: Response<*>): String {
        return when (response.code()) {
            400 -> "Bad Request: Invalid request parameters."
            401 -> "Unauthorized: Please log in again."
            403 -> "Forbidden: You do not have permission to perform this action."
            404 -> "Not Found: The requested resource was not found."
            429 -> "Too Many Requests: Rate limit exceeded."
            500, 502, 503 -> "Server Error: Please try again later."
            else -> "HTTP Error ${response.code()}"
        }
    }
}
