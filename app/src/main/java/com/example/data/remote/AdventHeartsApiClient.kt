package com.example.data.remote

import com.example.BuildConfig
import com.example.data.local.MessageEntity
import com.example.data.local.ProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AdventHeartsApiClient(
    baseUrl: String = BuildConfig.API_BASE_URL
) {
    private val service = RetrofitClient.apiService

    init {
        RetrofitClient.setBaseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
    }

    fun setAuthToken(token: String?) {
        AuthTokenManager.accessToken = token
    }

    private suspend fun <T, R> safeCall(
        call: suspend () -> Response<BackendResponseDto<T>>,
        transform: (T) -> R
    ): ApiResponse<R> = withContext(Dispatchers.IO) {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    ApiResponse.Success(transform(body.data))
                } else {
                    val err = body?.error
                    ApiResponse.Error(err?.code ?: "API_ERROR", err?.message ?: "Unknown API response error.")
                }
            } else {
                ApiResponse.Error("HTTP_${response.code()}", response.message().ifBlank { "HTTP call failed with code ${response.code()}" })
            }
        } catch (e: Exception) {
            ApiResponse.Error("NETWORK_ERROR", e.message ?: "Failed to connect to backend service.")
        }
    }

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        dateOfBirth: String,
        gender: String,
        country: String,
        city: String,
        age: Int? = null,
        relationshipIntention: String? = null
    ): ApiResponse<AuthResponseData> {
        if (email.isBlank() || password.isBlank()) {
            return ApiResponse.Error("INVALID_INPUT", "Email and password are required.")
        }

        val request = RegisterRequestDto(
            email = email,
            password = password,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            age = age,
            gender = gender,
            country = country,
            city = city,
            relationshipIntention = relationshipIntention
        )

        val result = safeCall({ service.register(request) }, ::toAuthData)
        if (result is ApiResponse.Success) {
            persistSession(result.data)
        }
        return result
    }

    suspend fun login(email: String, password: String): ApiResponse<AuthResponseData> {
        if (email.isBlank() || password.isBlank()) {
            return ApiResponse.Error("INVALID_CREDENTIALS", "Email and password cannot be empty.")
        }
        val result = safeCall({ service.login(LoginRequestDto(email, password)) }, ::toAuthData)
        if (result is ApiResponse.Success) {
            persistSession(result.data)
        }
        return result
    }

    suspend fun loginAdmin(email: String, password: String): ApiResponse<AuthResponseData> {
        val result = safeCall({ service.loginAdmin(AdminLoginRequestDto(email, password)) }, ::toAuthData)
        if (result is ApiResponse.Success) {
            persistSession(result.data)
        }
        return result
    }

    suspend fun fetchDiscoveryProfiles(): ApiResponse<List<ProfileEntity>> {
        return safeCall({ service.getDiscoveryProfiles() }) { list -> list.map { it.toEntity() } }
    }

    suspend fun fetchProfile(): ApiResponse<ProfileEntity> {
        return safeCall({ service.getProfile() }) { it.toEntity() }
    }

    suspend fun updateProfile(profile: ProfileEntity): ApiResponse<ProfileEntity> {
        val dto = profile.toDto()
        val result = safeCall({ service.updateProfile(dto) }) { it.toEntity() }
        if (result is ApiResponse.Success) {
            safeCall({ service.updateFaithProfile(dto) }) { it.toEntity() }
        }
        return result
    }

    suspend fun unmatch(matchId: String): ApiResponse<Boolean> {
        return safeCall({ service.unmatch(matchId) }) { true }
    }

    suspend fun fetchMatches(): ApiResponse<List<MatchDto>> {
        return safeCall({ service.getMatches() }) { it }
    }

    suspend fun fetchLikes(): ApiResponse<List<LikeReceivedDto>> {
        return safeCall({ service.getLikesReceived() }) { it }
    }

    suspend fun fetchMessages(matchId: String): ApiResponse<List<MessageEntity>> {
        return safeCall({ service.getMessages(matchId) }) { list -> list.map { it.toEntity() } }
    }

    suspend fun fetchNotifications(): ApiResponse<List<NotificationDto>> {
        return safeCall({ service.getNotifications() }) { it }
    }

    suspend fun sendLike(fromUserId: String, toUserId: String, isSuperLike: Boolean): ApiResponse<LikeResponseData> {
        val request = LikeRequestDto(fromUserId = fromUserId, toUserId = toUserId, isSuperLike = isSuperLike)
        return safeCall({ service.sendLike(request) }) { dto ->
            LikeResponseData(
                isMatch = dto.isMatch,
                matchId = dto.matchId,
                compatibilityScore = dto.compatibilityScore
            )
        }
    }

    suspend fun sendPass(toUserId: String): ApiResponse<Boolean> {
        return safeCall({ service.sendPass(PassRequestDto(toUserId)) }) { true }
    }

    suspend fun initiateCheckout(planId: String, paymentProvider: String = "stripe"): ApiResponse<CheckoutResponseData> {
        val request = CheckoutRequestDto(planId = planId, paymentProvider = paymentProvider)
        return safeCall({ service.initiateCheckout(request) }) { dto ->
            CheckoutResponseData(
                transactionId = dto.transactionId,
                checkoutUrl = dto.checkoutUrl,
                status = dto.status
            )
        }
    }

    suspend fun confirmPayment(transactionId: String, planId: String): ApiResponse<Boolean> {
        return safeCall({ service.confirmPayment(ConfirmPaymentRequestDto(transactionId, planId)) }) { true }
    }

    suspend fun sendMessage(matchId: String, text: String): ApiResponse<MessageEntity> {
        if (text.isBlank()) {
            return ApiResponse.Error("EMPTY_MESSAGE", "Message body cannot be empty.")
        }
        return safeCall({ service.sendMessage(matchId, SendMessageRequestDto(text)) }) { it.toEntity() }
    }

    suspend fun reportUser(reportedUserId: String, reason: String, details: String): ApiResponse<Boolean> {
        return safeCall({ service.reportUser(ReportRequestDto(reportedUserId, reason, details)) }) { true }
    }

    suspend fun uploadPhoto(bytes: ByteArray, filename: String = "photo.jpg", kind: String = "profile"): ApiResponse<ProfileEntity> {
        val body = bytes.toRequestBody("image/jpeg".toMediaType())
        val part = MultipartBody.Part.createFormData("photo", filename, body)
        return safeCall({ service.uploadPhoto(part, kind) }) { dto ->
            dto.profile?.toEntity() ?: throw IllegalStateException("Upload succeeded without a profile")
        }
    }

    suspend fun forgotPassword(email: String): ApiResponse<Boolean> {
        return safeCall({ service.forgotPassword(ForgotPasswordRequestDto(email)) }) { true }
    }

    suspend fun resetPassword(token: String, password: String): ApiResponse<Boolean> {
        return safeCall({ service.resetPassword(ResetPasswordRequestDto(token, password)) }) { true }
    }

    suspend fun deleteAccount(): ApiResponse<Boolean> {
        return safeCall({ service.deleteAccount() }) { true }
    }

    suspend fun fetchCurrentSubscription(): ApiResponse<CurrentSubscriptionDto> {
        return safeCall({ service.getCurrentSubscription() }) { it }
    }

    private fun persistSession(data: AuthResponseData) {
        setAuthToken(data.accessToken)
        AuthTokenManager.saveTokens(data.accessToken, data.refreshToken, data.userId)
    }

    private fun toAuthData(dto: AuthResponseDto) = AuthResponseData(
        userId = dto.userId,
        email = dto.email,
        fullName = dto.fullName,
        role = dto.role ?: "USER",
        accessToken = dto.accessToken,
        refreshToken = dto.refreshToken
    )
}

data class AuthResponseData(
    val userId: String,
    val email: String,
    val fullName: String,
    val accessToken: String,
    val refreshToken: String,
    val role: String = "USER"
)

data class LikeResponseData(
    val isMatch: Boolean,
    val matchId: String?,
    val compatibilityScore: Int
)

data class CheckoutResponseData(
    val transactionId: String,
    val checkoutUrl: String,
    val status: String
)
