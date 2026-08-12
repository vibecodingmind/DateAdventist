package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AdventHeartsDao {

    // User accounts
    @Query("SELECT * FROM user_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getUserAccount(userId: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(user: UserAccountEntity)

    // Profiles
    @Query("SELECT * FROM profiles WHERE userId = :userId")
    fun getProfileFlow(userId: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE userId = :userId LIMIT 1")
    suspend fun getProfileSync(userId: String): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE userId != :currentUserId")
    fun getAllOtherProfiles(currentUserId: String): Flow<List<ProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("SELECT * FROM profiles WHERE verificationStatus = 'PENDING'")
    fun getPendingVerifications(): Flow<List<ProfileEntity>>

    // Likes & Passes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPass(pass: PassEntity)

    @Query("SELECT * FROM user_likes WHERE fromUserId = :fromUserId AND toUserId = :toUserId LIMIT 1")
    suspend fun getLikeBetween(fromUserId: String, toUserId: String): LikeEntity?

    @Query("SELECT * FROM user_likes WHERE toUserId = :toUserId")
    fun getLikesReceivedForUser(toUserId: String): Flow<List<LikeEntity>>

    @Query("SELECT * FROM user_likes WHERE fromUserId = :fromUserId")
    fun getLikesSentByUser(fromUserId: String): Flow<List<LikeEntity>>

    // Matches
    @Query("SELECT * FROM matches WHERE user1Id = :userId OR user2Id = :userId ORDER BY matchedAt DESC")
    fun getMatchesForUser(userId: String): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE matchId = :matchId LIMIT 1")
    suspend fun getMatchById(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: MatchEntity)

    @Query("DELETE FROM matches WHERE matchId = :matchId")
    suspend fun deleteMatch(matchId: String)

    // Messages
    @Query("SELECT * FROM messages WHERE matchId = :matchId ORDER BY timestamp ASC")
    fun getMessagesForMatch(matchId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE messages SET isRead = 1 WHERE matchId = :matchId AND receiverId = :currentUserId")
    suspend fun markMessagesAsRead(matchId: String, currentUserId: String)

    // Reports & Blocks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Query("SELECT * FROM reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("UPDATE reports SET status = :status WHERE reportId = :reportId")
    suspend fun updateReportStatus(reportId: String, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity)

    @Query("SELECT blockedUserId FROM blocks WHERE blockerId = :blockerId")
    fun getBlockedUserIds(blockerId: String): Flow<List<String>>

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    // System Settings
    @Query("SELECT * FROM system_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): SystemSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SystemSettingsEntity)
}
