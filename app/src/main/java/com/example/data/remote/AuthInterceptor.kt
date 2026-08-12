package com.example.data.remote

import android.util.Log
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")

        AuthTokenManager.accessToken?.let { token ->
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        val response = chain.proceed(builder.build())

        // Handle 401 Unauthorized responses
        if (response.code == 401 && !originalRequest.url.encodedPath.contains("/auth/")) {
            response.close()
            val refreshToken = AuthTokenManager.refreshToken
            if (!refreshToken.isNullOrBlank()) {
                val newAccessToken = attemptRefreshToken(chain, refreshToken)
                if (newAccessToken != null) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newAccessToken")
                        .build()
                    return chain.proceed(retryRequest)
                }
            }
            // Refresh failed or missing refresh token -> clear local secure storage & navigate back to login
            AuthTokenManager.onUnauthorized()
        }

        return response
    }

    private fun attemptRefreshToken(chain: Interceptor.Chain, refreshToken: String): String? {
        return try {
            val json = JSONObject().apply {
                put("refreshToken", refreshToken)
            }
            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val refreshUrl = chain.request().url.newBuilder()
                .encodedPath("/api/v1/auth/refresh-token")
                .build()

            val refreshRequest = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .build()

            val refreshResponse = chain.proceed(refreshRequest)
            if (refreshResponse.isSuccessful) {
                val bodyStr = refreshResponse.body?.string() ?: ""
                val jsonRes = JSONObject(bodyStr)
                if (jsonRes.optBoolean("success")) {
                    val dataObj = jsonRes.optJSONObject("data")
                    val newAccess = dataObj?.optString("accessToken")
                    val newRefresh = dataObj?.optString("refreshToken")
                    if (!newAccess.isNullOrBlank()) {
                        AuthTokenManager.saveTokens(newAccess, newRefresh ?: refreshToken)
                        return newAccess
                    }
                }
            }
            null
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Failed to refresh token: ${e.message}")
            null
        }
    }
}
