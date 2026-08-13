package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val DEFAULT_BASE_URL: String
        get() {
            val configured = try {
                com.example.BuildConfig.API_BASE_URL
            } catch (_: Throwable) {
                "http://10.0.2.2:5000/api/v1/"
            }
            return if (configured.endsWith("/")) configured else "$configured/"
        }

    @Volatile
    private var baseUrl: String = DEFAULT_BASE_URL

    fun setBaseUrl(newUrl: String) {
        baseUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        rebuildService()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(ErrorInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Volatile
    private var instance: AdventHeartsApiService? = null

    val apiService: AdventHeartsApiService
        get() = instance ?: synchronized(this) {
            instance ?: createService().also { instance = it }
        }

    private fun createService(): AdventHeartsApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AdventHeartsApiService::class.java)
    }

    private fun rebuildService() {
        synchronized(this) {
            instance = createService()
        }
    }
}
