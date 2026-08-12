package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val userId: String,
    val email: String,
    val passwordHash: String,
    val role: String = "USER", // USER, MODERATOR, ADMIN
    val isEmailVerified: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val age: Int,
    val gender: String, // Female, Male
    val country: String,
    val city: String,
    val distanceKm: Int = 12,
    val occupation: String,
    val education: String,
    val bio: String,
    val relationshipIntention: String, // Marriage, Serious Relationship, Getting to know someone, Friendship first
    val primaryPhoto: String,
    val photoUrls: List<String>,
    val isVerified: Boolean = false,
    val verificationStatus: String = "NOT_VERIFIED", // NOT_VERIFIED, PENDING, VERIFIED, REJECTED
    val verificationSelfieUri: String? = null,
    val isPremium: Boolean = false,
    val isPaused: Boolean = false,
    val lastActiveText: String = "Active today",
    
    // Faith Profile
    val adventistAffiliation: String,
    val yearsAsAdventist: Int,
    val localChurch: String,
    val isBaptized: Boolean,
    val faithImportance: String, // Central to everything, Very important, Important, Still growing
    val churchInvolvement: String, // Very active, Active, Occasionally involved, Looking for church
    val sabbathObservance: List<String>,
    val ministryInterests: List<String>,
    val personalBibleStudy: String,
    val favoriteVerse: String,
    
    // Lifestyle
    val diet: String, // Vegetarian, Vegan, Plant-Based, Standard
    val alcohol: String, // None / Abstain, Rarely, Never
    val smoking: String, // Never
    val wantsChildren: String, // Yes, definitely, Open to children, Not sure, No
    val hasChildren: Boolean,
    val interests: List<String>
)

@Entity(tableName = "user_likes")
data class LikeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUserId: String,
    val toUserId: String,
    val isSuperLike: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_passes")
data class PassEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromUserId: String,
    val toUserId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val user1Id: String,
    val user2Id: String,
    val compatibilityScore: Int,
    val conversationStarter: String,
    val matchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val messageId: String,
    val matchId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val reportId: String,
    val reporterId: String,
    val reportedUserId: String,
    val reason: String,
    val details: String,
    val status: String = "OPEN", // OPEN, UNDER_REVIEW, RESOLVED, DISMISSED
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "blocks")
data class BlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockerId: String,
    val blockedUserId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val type: String, // MATCH, LIKE, SUPERLIKE, VERIFICATION, SYSTEM
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)
