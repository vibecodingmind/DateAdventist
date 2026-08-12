package com.example.data.remote

import com.example.data.local.MatchEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.ProfileEntity
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val country: String? = null,
    val city: String? = null,
    val relationshipIntention: String? = null
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val userId: String,
    val email: String,
    val fullName: String = "",
    val role: String? = "USER",
    val accessToken: String = "",
    val refreshToken: String = "",
    val isEmailVerified: Boolean = false
)

data class GenericAckDto(
    val message: String? = null,
    val marked: Boolean? = null,
    val blocked: Boolean? = null,
    val passed: Boolean? = null,
    val active: Boolean? = null,
    val tier: String? = null,
    val userId: String? = null,
    val status: String? = null,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val verificationStatus: String? = null
)

data class VerifyEmailRequestDto(
    val token: String
)

data class RefreshTokenRequestDto(
    val refreshToken: String
)

data class LikeRequestDto(
    val fromUserId: String? = null,
    val toUserId: String,
    val isSuperLike: Boolean = false
)

data class LikeResponseDto(
    val isMatch: Boolean = false,
    val matchId: String? = null,
    val compatibilityScore: Int = 80,
    val conversationStarter: String? = null,
    val targetUserId: String? = null
)

data class PassRequestDto(
    val toUserId: String
)

data class CheckoutRequestDto(
    val planId: String,
    val paymentProvider: String = "stripe",
    val billingCycle: String = "MONTHLY"
)

data class CheckoutResponseDto(
    val transactionId: String,
    val provider: String = "stripe",
    val status: String,
    val checkoutUrl: String
)

data class ConfirmPaymentRequestDto(
    val transactionId: String,
    val planId: String? = null
)

data class AdminLoginRequestDto(
    val email: String,
    val password: String? = null,
    val role: String? = null
)

data class AdminDashboardMetricsDto(
    val activeUsers: Int = 0,
    val totalMatches: Int = 0,
    val pendingVerifications: Int = 0,
    val openReports: Int = 0,
    val monthlyRevenue: Double = 0.0
)

data class AdminUserDto(
    val userId: String,
    val fullName: String = "",
    val email: String = "",
    val subscriptionStatus: String = "FREE",
    val isVerified: Boolean = false,
    val accountStatus: String = "ACTIVE",
    val createdAt: String? = null
)

data class AuditLogDto(
    val id: String,
    val timestamp: Long,
    val actor: String,
    val actionType: String,
    val details: String,
    val targetUserId: String? = null
)

data class AdminAnalyticsDto(
    val totalUsers: Int = 0,
    val dailyActiveUsers: Int = 0,
    val monthlyActiveUsers: Int = 0,
    val subscriptionConversionRatePct: Double = 0.0,
    val activeSubscriptions: Int = 0,
    val totalReports: Int = 0,
    val pendingVerifications: Int = 0,
    val totalRevenueUsd: Double = 0.0,
    val matchesMade: Int = 0,
    val verificationRatePercent: Int = 0
)

data class UpdateUserStatusRequestDto(
    val status: String,
    val reason: String? = null
)

data class ProfileDto(
    val userId: String,
    val fullName: String = "",
    val age: Int = 18,
    val gender: String = "",
    val country: String = "",
    val city: String = "",
    val distanceKm: Int = 12,
    val occupation: String = "",
    val education: String = "",
    val bio: String = "",
    val relationshipIntention: String = "Marriage",
    val primaryPhoto: String = "",
    val photoUrls: List<String> = emptyList(),
    val isVerified: Boolean = false,
    val verificationStatus: String = "NOT_VERIFIED",
    val verificationSelfieUri: String? = null,
    val isPremium: Boolean = false,
    val isPaused: Boolean = false,
    val lastActiveText: String = "Active today",
    val adventistAffiliation: String = "Seventh-day Adventist Member",
    val yearsAsAdventist: Int = 0,
    val localChurch: String = "",
    val isBaptized: Boolean = true,
    val faithImportance: String = "Very important",
    val churchInvolvement: String = "Active",
    val sabbathObservance: List<String> = emptyList(),
    val ministryInterests: List<String> = emptyList(),
    val personalBibleStudy: String = "Daily",
    val favoriteVerse: String = "",
    val diet: String = "Vegetarian",
    val alcohol: String = "None / Abstain",
    val smoking: String = "Never",
    val wantsChildren: String = "Yes, definitely",
    val hasChildren: Boolean = false,
    val interests: List<String> = emptyList(),
    val compatibilityScore: Int? = null
)

data class MatchDto(
    val matchId: String,
    val user1Id: String,
    val user2Id: String,
    val compatibilityScore: Int = 80,
    val conversationStarter: String = "",
    val matchedAt: String? = null,
    val otherProfile: ProfileDto? = null
)

data class MessageDto(
    val messageId: String,
    val matchId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = 0L,
    val isRead: Boolean = false
)

data class SendMessageRequestDto(
    val text: String
)

data class LikeReceivedDto(
    val likeId: String? = null,
    val fromUserId: String,
    val toUserId: String? = null,
    val isSuperLike: Boolean = false,
    val profile: ProfileDto? = null
)

data class ReportRequestDto(
    val reportedUserId: String,
    val reason: String,
    val details: String = ""
)

data class BlockRequestDto(
    val blockedUserId: String
)

data class NotificationDto(
    val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String,
    val isRead: Boolean = false,
    val timestamp: Long = 0L
)

fun ProfileDto.toEntity(): ProfileEntity = ProfileEntity(
    userId = userId,
    fullName = fullName,
    age = age,
    gender = gender,
    country = country,
    city = city,
    distanceKm = distanceKm,
    occupation = occupation,
    education = education,
    bio = bio,
    relationshipIntention = relationshipIntention,
    primaryPhoto = primaryPhoto,
    photoUrls = photoUrls,
    isVerified = isVerified,
    verificationStatus = verificationStatus,
    verificationSelfieUri = verificationSelfieUri,
    isPremium = isPremium,
    isPaused = isPaused,
    lastActiveText = lastActiveText,
    adventistAffiliation = adventistAffiliation,
    yearsAsAdventist = yearsAsAdventist,
    localChurch = localChurch,
    isBaptized = isBaptized,
    faithImportance = faithImportance,
    churchInvolvement = churchInvolvement,
    sabbathObservance = sabbathObservance,
    ministryInterests = ministryInterests,
    personalBibleStudy = personalBibleStudy,
    favoriteVerse = favoriteVerse,
    diet = diet,
    alcohol = alcohol,
    smoking = smoking,
    wantsChildren = wantsChildren,
    hasChildren = hasChildren,
    interests = interests
)

fun MatchDto.toEntity(): MatchEntity = MatchEntity(
    matchId = matchId,
    user1Id = user1Id,
    user2Id = user2Id,
    compatibilityScore = compatibilityScore,
    conversationStarter = conversationStarter
)

fun MessageDto.toEntity(): MessageEntity = MessageEntity(
    messageId = messageId,
    matchId = matchId,
    senderId = senderId,
    receiverId = receiverId,
    text = text,
    timestamp = if (timestamp == 0L) System.currentTimeMillis() else timestamp,
    isRead = isRead
)

fun NotificationDto.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    userId = userId,
    title = title,
    body = body,
    type = type,
    isRead = isRead,
    timestamp = if (timestamp == 0L) System.currentTimeMillis() else timestamp
)

interface AdventHeartsApiService {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): Response<BackendResponseDto<AuthResponseDto>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<BackendResponseDto<AuthResponseDto>>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequestDto): Response<BackendResponseDto<GenericAckDto>>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequestDto): Response<BackendResponseDto<GenericAckDto>>

    @GET("auth/me")
    suspend fun getMe(): Response<BackendResponseDto<AuthResponseDto>>

    @GET("profile")
    suspend fun getProfile(): Response<BackendResponseDto<ProfileDto>>

    @PUT("profile")
    suspend fun updateProfile(@Body profile: ProfileDto): Response<BackendResponseDto<ProfileDto>>

    @GET("discover")
    suspend fun getDiscoveryProfiles(
        @Query("minAge") minAge: Int? = null,
        @Query("maxAge") maxAge: Int? = null,
        @Query("gender") gender: String? = null,
        @Query("q") query: String? = null
    ): Response<BackendResponseDto<List<ProfileDto>>>

    @POST("discover/like")
    suspend fun sendLike(@Body request: LikeRequestDto): Response<BackendResponseDto<LikeResponseDto>>

    @POST("discover/pass")
    suspend fun sendPass(@Body request: PassRequestDto): Response<BackendResponseDto<GenericAckDto>>

    @GET("likes")
    suspend fun getLikesReceived(): Response<BackendResponseDto<List<LikeReceivedDto>>>

    @GET("matches")
    suspend fun getMatches(): Response<BackendResponseDto<List<MatchDto>>>

    @GET("matches/{matchId}/messages")
    suspend fun getMessages(@Path("matchId") matchId: String): Response<BackendResponseDto<List<MessageDto>>>

    @POST("matches/{matchId}/messages")
    suspend fun sendMessage(
        @Path("matchId") matchId: String,
        @Body request: SendMessageRequestDto
    ): Response<BackendResponseDto<MessageDto>>

    @POST("matches/{matchId}/read")
    suspend fun markMessagesRead(@Path("matchId") matchId: String): Response<BackendResponseDto<GenericAckDto>>

    @POST("safety/report")
    suspend fun reportUser(@Body request: ReportRequestDto): Response<BackendResponseDto<GenericAckDto>>

    @POST("safety/block")
    suspend fun blockUser(@Body request: BlockRequestDto): Response<BackendResponseDto<GenericAckDto>>

    @GET("notifications")
    suspend fun getNotifications(): Response<BackendResponseDto<List<NotificationDto>>>

    @GET("subscriptions/plans")
    suspend fun getSubscriptionPlans(): Response<BackendResponseDto<GenericAckDto>>

    @POST("subscriptions/checkout")
    suspend fun initiateCheckout(@Body request: CheckoutRequestDto): Response<BackendResponseDto<CheckoutResponseDto>>

    @POST("subscriptions/confirm-payment")
    suspend fun confirmPayment(@Body request: ConfirmPaymentRequestDto): Response<BackendResponseDto<GenericAckDto>>

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
    ): Response<BackendResponseDto<GenericAckDto>>

    @GET("admin/audit-logs")
    suspend fun getAuditLogs(): Response<BackendResponseDto<List<AuditLogDto>>>

    @GET("admin/analytics")
    suspend fun getAdminAnalytics(): Response<BackendResponseDto<AdminAnalyticsDto>>

    @POST("admin/verifications/{id}/approve")
    suspend fun approveVerification(@Path("id") userId: String): Response<BackendResponseDto<GenericAckDto>>
}
