package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object AuthTokenManager {
    private const val PREFS_NAME = "adventhearts_secure_prefs"
    private const val KEY_ACCESS_TOKEN = "jwt_access_token"
    private const val KEY_REFRESH_TOKEN = "jwt_refresh_token"
    private const val KEY_CURRENT_USER_ID = "current_user_id"

    private var prefs: SharedPreferences? = null

    @Volatile
    var accessToken: String? = null

    @Volatile
    var refreshToken: String? = null

    @Volatile
    var currentUserId: String? = null

    private var onUnauthorizedListener: (() -> Unit)? = null

    fun init(context: Context) {
        if (prefs == null) {
            synchronized(this) {
                if (prefs == null) {
                    prefs = try {
                        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                        EncryptedSharedPreferences.create(
                            PREFS_NAME,
                            masterKeyAlias,
                            context.applicationContext,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.values()[0],
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.values()[0]
                        )
                    } catch (e: Exception) {
                        Log.e("AuthTokenManager", "Failed to initialize EncryptedSharedPreferences, falling back to private prefs: ${e.message}")
                        context.applicationContext.getSharedPreferences("${PREFS_NAME}_fallback", Context.MODE_PRIVATE)
                    }

                    accessToken = prefs?.getString(KEY_ACCESS_TOKEN, null)
                    refreshToken = prefs?.getString(KEY_REFRESH_TOKEN, null)
                    currentUserId = prefs?.getString(KEY_CURRENT_USER_ID, null)
                }
            }
        }
    }

    fun saveTokens(token: String, refresh: String? = null, userId: String? = null) {
        accessToken = token
        refreshToken = refresh ?: refreshToken
        if (userId != null) {
            currentUserId = userId
        }

        prefs?.edit()?.apply {
            putString(KEY_ACCESS_TOKEN, token)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            if (userId != null) {
                putString(KEY_CURRENT_USER_ID, userId)
            }
            apply()
        }
    }

    fun setOnUnauthorizedListener(listener: () -> Unit) {
        this.onUnauthorizedListener = listener
    }

    fun onUnauthorized() {
        clearTokens()
        onUnauthorizedListener?.invoke()
    }

    fun clearTokens() {
        accessToken = null
        refreshToken = null
        currentUserId = null

        prefs?.edit()?.apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_CURRENT_USER_ID)
            apply()
        }
    }
}
