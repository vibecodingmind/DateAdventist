package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// DTO data structures
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val country: String? = null,
    val city: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val userId: String,
    val email: String,
    val fullName: String,
    val role: String? = "USER",
    val accessToken: String,
    val refreshToken: String,
    val isEmailVerified: Boolean = false
)

data class VerifyEmailRequestDto(
    val token: String
)

data class RefreshTokenRequestDto(
    val refreshToken: String
)

data class LikeRequestDto(
    val fromUserId: String,
    val toUserId: String,
    val isSuperLike: Boolean = false
)

data class LikeResponseDto(
    val isMatch: Boolean,
    val matchId: String? = null,
    val compatibilityScore: Int = 80
)

data class CheckoutRequestDto(
    val planId: String,
    val paymentProvider: String = "stripe",
    val billingCycle: String = "MONTHLY"
)

data class CheckoutResponseDto(
    val transactionId: String,
    val provider: String,
    val status: String,
    val checkoutUrl: String
)

data class ConfirmPaymentRequestDto(
    val transactionId: String
)

data class AdminLoginRequestDto(
    val email: String,
    val role: String
)

data class AdminDashboardMetricsDto(
    val activeUsers: Int,
    val totalMatches: Int,
    val pendingVerifications: Int,
    val openReports: Int,
    val monthlyRevenue: Double
)

data class AdminUserDto(
    val userId: String,
    val fullName: String,
    val email: String,
    val subscriptionStatus: String = "FREE",
    val isVerified: Boolean = false,
    val accountStatus: String = "ACTIVE", // ACTIVE, SUSPENDED, BANNED
    val createdAt: String? = null
)

data class AuditLogDto(
    val id: String,
    val timestamp: Long,
    val actor: String,
    val actionType: String, // USER_SUSPEND, USER_BAN, USER_ACTIVATE, VERIFY_APPROVE, VERIFY_REJECT, SETTINGS_UPDATE
    val details: String,
    val targetUserId: String? = null
)

data class AdminAnalyticsDto(
    val totalUsers: Int = 1248,
    val dailyActiveUsers: Int = 852,
    val monthlyActiveUsers: Int = 2100,
    val subscriptionConversionRatePct: Double = 12.4,
    val activeSubscriptions: Int = 230,
    val totalReports: Int = 14,
    val pendingVerifications: Int = 3,
    val totalRevenueUsd: Double = 14280.00
)

data class UpdateUserStatusRequestDto(
    val status: String, // ACTIVE, SUSPENDED, BANNED
    val reason: String? = null
)

interface AdventHeartsApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<BackendResponseDto<AuthResponseDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<BackendResponseDto<AuthResponseDto>>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): Response<BackendResponseDto<Map<String, String>>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): Response<BackendResponseDto<Map<String, String>>>

    @GET("auth/me")
    suspend fun getMe(): Response<BackendResponseDto<AuthResponseDto>>

    // Profile
    @GET("profile")
    suspend fun getProfile(): Response<BackendResponseDto<Map<String, Any>>>

    // Discovery
    @GET("discover")
    suspend fun getDiscoveryProfiles(): Response<BackendResponseDto<List<Map<String, Any>>>>

    @POST("discover/like")
    suspend fun sendLike(@Body request: LikeRequestDto): Response<BackendResponseDto<LikeResponseDto>>

    // Subscriptions
    @POST("subscriptions/checkout")
    suspend fun initiateCheckout(@Body request: CheckoutRequestDto): Response<BackendResponseDto<CheckoutResponseDto>>

    @POST("subscriptions/confirm-payment")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequestDto): Response<BackendResponseDto<Map<String, Any>>>

    // Admin
    @POST("admin/login")
    suspend fun loginAdmin(@Body request: AdminLoginRequestDto): Response<BackendResponseDto<AuthResponseDto>>

    @GET("admin/dashboard")
    suspend fun getAdminDashboard(): Response<BackendResponseDto<AdminDashboardMetricsDto>>

    @GET("admin/users")
    suspend fun getAdminUsers(): Response<BackendResponseDto<List<AdminUserDto>>>

    @POST("admin/users/{id}/status")
    suspend fun updateUserStatus(
        @Path("id") userId: String,
        @Body request: UpdateUserStatusRequestDto
    ): Response<BackendResponseDto<Map<String, Any>>>

    @GET("admin/audit-logs")
    suspend fun getAuditLogs(): Response<BackendResponseDto<List<AuditLogDto>>>

    @GET("admin/analytics")
    suspend fun getAdminAnalytics(): Response<BackendResponseDto<AdminAnalyticsDto>>
}
