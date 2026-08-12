package com.example.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class ErrorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        when (response.code) {
            401 -> {
                Log.w("HTTP_ERROR", "401 Unauthorized encountered for ${request.url}")
                AuthTokenManager.onUnauthorized()
            }
            403 -> {
                Log.e("HTTP_ERROR", "403 Forbidden - RBAC or Permission Denied for ${request.url}")
            }
            404 -> {
                Log.w("HTTP_ERROR", "404 Not Found: ${request.url}")
            }
            500, 502, 503 -> {
                Log.e("HTTP_ERROR", "${response.code} Server Error on ${request.url}")
            }
        }

        return response
    }
}
