package com.example.data.repository

import android.content.Context
import com.example.data.local.AdventHeartsDao
import com.example.data.local.AdventHeartsDatabase
import com.example.data.local.BlockEntity
import com.example.data.local.LikeEntity
import com.example.data.local.MatchEntity
import com.example.data.local.MessageEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PassEntity
import com.example.data.local.ProfileEntity
import com.example.data.local.ReportEntity
import com.example.data.local.UserAccountEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class AdventHeartsRepository(private val dao: AdventHeartsDao) {

    companion object {
        @Volatile
        private var INSTANCE: AdventHeartsRepository? = null

        fun getInstance(context: Context): AdventHeartsRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AdventHeartsDatabase.getInstance(context)
                val repo = AdventHeartsRepository(db.dao())
                INSTANCE = repo
                repo
            }
        }
    }

    // Auth & Accounts
    suspend fun getUserAccount(userId: String): UserAccountEntity? = dao.getUserAccount(userId)
    suspend fun getUserAccountByEmail(email: String): UserAccountEntity? = dao.getUserAccountByEmail(email)
    suspend fun registerUser(user: UserAccountEntity, profile: ProfileEntity) {
        dao.insertUserAccount(user)
        dao.insertProfile(profile)
    }

    // Profiles
    fun getProfileFlow(userId: String): Flow<ProfileEntity?> = dao.getProfileFlow(userId)
    suspend fun getProfileSync(userId: String): ProfileEntity? = dao.getProfileSync(userId)
    fun getAllOtherProfiles(currentUserId: String): Flow<List<ProfileEntity>> = dao.getAllOtherProfiles(currentUserId)
    suspend fun updateProfile(profile: ProfileEntity) = dao.updateProfile(profile)
    fun getPendingVerifications(): Flow<List<ProfileEntity>> = dao.getPendingVerifications()

    // Likes, Passes & Matching Logic
    suspend fun sendLike(fromUserId: String, toUserId: String, isSuperLike: Boolean): Boolean {
        dao.insertLike(LikeEntity(fromUserId = fromUserId, toUserId = toUserId, isSuperLike = isSuperLike))
        
        // Check if reciprocal like exists (Mutual Match!)
        val reciprocalLike = dao.getLikeBetween(toUserId, fromUserId)
        if (reciprocalLike != null) {
            val fromProfile = dao.getProfileSync(fromUserId)
            val toProfile = dao.getProfileSync(toUserId)
            val score = calculateCompatibility(fromProfile, toProfile)
            val matchId = "match_${System.currentTimeMillis()}_${(1000..9999).random()}"
            val starter = generateConversationStarter(fromProfile, toProfile)
            
            dao.insertMatch(
                MatchEntity(
                    matchId = matchId,
                    user1Id = fromUserId,
                    user2Id = toUserId,
                    compatibilityScore = score,
                    conversationStarter = starter
                )
            )

            // Notify both users
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = fromUserId,
                    title = "It's a Match! ❤️",
                    body = "You and ${toProfile?.fullName ?: "someone"} liked each other!",
                    type = "MATCH"
                )
            )
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = toUserId,
                    title = "It's a Match! ❤️",
                    body = "You and ${fromProfile?.fullName ?: "someone"} liked each other!",
                    type = "MATCH"
                )
            )
            return true // Is Match
        } else {
            // Notify receiver if they are premium or standard like notification
            val fromProfile = dao.getProfileSync(fromUserId)
            dao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    userId = toUserId,
                    title = if (isSuperLike) "⭐ Super Like Received!" else "Someone Liked You!",
                    body = "${fromProfile?.fullName ?: "An Adventist single"} sent you a ${if (isSuperLike) "Super Like" else "Like"}.",
                    type = if (isSuperLike) "SUPERLIKE" else "LIKE"
                )
            )
            return false
        }
    }

    suspend fun sendPass(fromUserId: String, toUserId: String) {
        dao.insertPass(PassEntity(fromUserId = fromUserId, toUserId = toUserId))
    }

    fun getLikesReceived(userId: String): Flow<List<LikeEntity>> = dao.getLikesReceivedForUser(userId)
    fun getLikesSent(userId: String): Flow<List<LikeEntity>> = dao.getLikesSentByUser(userId)

    // Matches
    fun getMatches(userId: String): Flow<List<MatchEntity>> = dao.getMatchesForUser(userId)
    suspend fun getMatchById(matchId: String): MatchEntity? = dao.getMatchById(matchId)

    // Messaging
    fun getMessages(matchId: String): Flow<List<MessageEntity>> = dao.getMessagesForMatch(matchId)
    suspend fun sendMessage(matchId: String, senderId: String, receiverId: String, text: String) {
        val msg = MessageEntity(
            messageId = UUID.randomUUID().toString(),
            matchId = matchId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        dao.insertMessage(msg)
    }

    suspend fun markMessagesAsRead(matchId: String, currentUserId: String) {
        dao.markMessagesAsRead(matchId, currentUserId)
    }

    // Safety & Moderation
    suspend fun reportUser(reporterId: String, reportedUserId: String, reason: String, details: String) {
        val report = ReportEntity(
            reportId = "rep_${UUID.randomUUID()}",
            reporterId = reporterId,
            reportedUserId = reportedUserId,
            reason = reason,
            details = details
        )
        dao.insertReport(report)
    }

    fun getAllReports(): Flow<List<ReportEntity>> = dao.getAllReports()
    suspend fun updateReportStatus(reportId: String, status: String) = dao.updateReportStatus(reportId, status)

    suspend fun blockUser(blockerId: String, blockedUserId: String) {
        dao.insertBlock(BlockEntity(blockerId = blockerId, blockedUserId = blockedUserId))
    }

    fun getBlockedUserIds(blockerId: String): Flow<List<String>> = dao.getBlockedUserIds(blockerId)

    // Notifications
    fun getNotifications(userId: String): Flow<List<NotificationEntity>> = dao.getNotificationsForUser(userId)
    suspend fun markNotificationsRead(userId: String) = dao.markAllNotificationsAsRead(userId)

    // Compatibility Calculation Engine (Faith 25%, Intention 20%, Lifestyle 15%, Family 15%, Location 10%, Interests 5%, Age 5%, Personality 5%)
    fun calculateCompatibility(p1: ProfileEntity?, p2: ProfileEntity?): Int {
        if (p1 == null || p2 == null) return 75
        var score = 50

        // Faith compatibility (25 points max)
        if (p1.adventistAffiliation == p2.adventistAffiliation) score += 10
        if (p1.faithImportance == p2.faithImportance) score += 8
        if (p1.isBaptized == p2.isBaptized) score += 7

        // Relationship Intention (20 points max)
        if (p1.relationshipIntention == p2.relationshipIntention) {
            score += 20
        } else if (p1.relationshipIntention.contains("Marriage") && p2.relationshipIntention.contains("Serious")) {
            score += 15
        } else {
            score += 5
        }

        // Lifestyle (15 points max)
        if (p1.diet == p2.diet) score += 8
        if (p1.alcohol == p2.alcohol) score += 7

        // Family goals (15 points max)
        if (p1.wantsChildren == p2.wantsChildren) score += 15

        // Location & Distance (10 points max)
        if (p1.country == p2.country) score += 6
        if (p1.city == p2.city) score += 4

        // Common Interests (10 points max)
        val commonInterests = p1.interests.intersect(p2.interests.toSet())
        score += (commonInterests.size * 3).coerceAtMost(10)

        return score.coerceIn(60, 99)
    }

    private fun generateConversationStarter(p1: ProfileEntity?, p2: ProfileEntity?): String {
        if (p1 == null || p2 == null) return "What's your favorite way to celebrate Sabbath?"
        
        val commonMinistry = p1.ministryInterests.intersect(p2.ministryInterests.toSet())
        if (commonMinistry.isNotEmpty()) {
            return "Both of you share a passion for ${commonMinistry.first()} Ministry! Ask how they got started."
        }
        val commonInterests = p1.interests.intersect(p2.interests.toSet())
        if (commonInterests.isNotEmpty()) {
            return "You both enjoy ${commonInterests.first()}! Great topic for a conversation starter."
        }
        if (p1.localChurch.isNotBlank() && p2.localChurch.isNotBlank()) {
            return "Both of you are active members in your local Seventh-day Adventist churches!"
        }
        return "You share strong faith values and marriage intentions! Ask about their favorite Sabbath activity."
    }

    // Seed database with realistic Adventist single profiles and sample admin/moderator accounts
    suspend fun seedDatabaseIfEmpty() {
        val adminAcc = dao.getUserAccount("usr_admin")
        if (adminAcc != null) return // Already seeded

        // Accounts
        val accounts = listOf(
            UserAccountEntity("usr_me", "john.adventist@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_sarah", "sarah.m@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_david", "david.k@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_hannah", "hannah.t@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_elena", "elena.r@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_marcus", "marcus.b@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_miriam", "miriam.s@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_samuel", "samuel.a@gmail.com", "password123", role = "USER", isEmailVerified = true),
            UserAccountEntity("usr_admin", "admin@adventhearts.com", "admin123", role = "ADMIN", isEmailVerified = true),
            UserAccountEntity("usr_mod", "moderator@adventhearts.com", "mod123", role = "MODERATOR", isEmailVerified = true)
        )
        accounts.forEach { dao.insertUserAccount(it) }

        // Profiles
        val myProfile = ProfileEntity(
            userId = "usr_me",
            fullName = "Joshua Miller",
            age = 28,
            gender = "Male",
            country = "United States",
            city = "Berrien Springs, MI",
            distanceKm = 5,
            occupation = "Software Engineer & Youth Mentor",
            education = "Andrews University (B.S. Computer Science)",
            bio = "Devoted Adventist Christian passionate about technology, youth ministry, and nature photography. Looking for a godly partner with whom to share Sabbath peace, family life, and ministry.",
            relationshipIntention = "Marriage",
            primaryPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            verificationStatus = "VERIFIED",
            isPremium = true,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 28,
            localChurch = "Pioneer Memorial Church",
            isBaptized = true,
            faithImportance = "Central to everything I do",
            churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "Nature Walks", "AY Youth Fellowship"),
            ministryInterests = listOf("Youth", "Evangelism", "Technology"),
            personalBibleStudy = "Daily",
            favoriteVerse = "Jeremiah 29:11",
            diet = "Vegetarian",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Yes, definitely",
            hasChildren = false,
            interests = listOf("Sabbath Nature Walks", "Youth Ministry", "A cappella Music", "Camping", "Reading")
        )
        dao.insertProfile(myProfile)

        val sarahProfile = ProfileEntity(
            userId = "usr_sarah",
            fullName = "Sarah Moretz",
            age = 26,
            gender = "Female",
            country = "United States",
            city = "Silver Spring, MD",
            distanceKm = 15,
            occupation = "Registered Nurse & Pathfinders Leader",
            education = "Loma Linda University (B.S. Nursing)",
            bio = "Active Adventist nurse who loves health ministry, Pathfinder camping, and baking healthy plant-based treats. I value kindness, Sabbath rest, and living with purpose.",
            relationshipIntention = "Marriage",
            primaryPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            verificationStatus = "VERIFIED",
            isPremium = false,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 26,
            localChurch = "Sligo Seventh-day Adventist Church",
            isBaptized = true,
            faithImportance = "Central to everything I do",
            churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "Nature Walks", "Community Service"),
            ministryInterests = listOf("Youth", "Health", "Music"),
            personalBibleStudy = "Daily",
            favoriteVerse = "Philippians 4:13",
            diet = "Plant-Based",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Yes, definitely",
            hasChildren = false,
            interests = listOf("Pathfinders", "Health Cooking", "Sabbath Nature Walks", "A cappella Music")
        )
        dao.insertProfile(sarahProfile)

        val hannahProfile = ProfileEntity(
            userId = "usr_hannah",
            fullName = "Hannah Tesfaye",
            age = 25,
            gender = "Female",
            country = "Tanzania",
            city = "Arusha",
            distanceKm = 42,
            occupation = "Architect & Choir Director",
            education = "University of Dar es Salaam",
            bio = "Adventist architect passionate about designing sustainable community spaces and leading gospel choir ministry. Seeking a faith-driven partner ready to build a Christian home grounded in love.",
            relationshipIntention = "Marriage",
            primaryPhoto = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            verificationStatus = "VERIFIED",
            isPremium = true,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 18,
            localChurch = "Central SDA Church Arusha",
            isBaptized = true,
            faithImportance = "Central to everything I do",
            churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "AY Youth Fellowship", "Sunset to Sunset Rest"),
            ministryInterests = listOf("Music", "Youth", "Community Service"),
            personalBibleStudy = "Daily",
            favoriteVerse = "Proverbs 3:5-6",
            diet = "Vegetarian",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Yes, definitely",
            hasChildren = false,
            interests = listOf("A cappella Music", "Architecture", "Bible Study", "Volunteering")
        )
        dao.insertProfile(hannahProfile)

        val elenaProfile = ProfileEntity(
            userId = "usr_elena",
            fullName = "Elena Rostova",
            age = 27,
            gender = "Female",
            country = "United Kingdom",
            city = "London",
            distanceKm = 120,
            occupation = "Primary School Teacher",
            education = "Newbold College of Higher Education",
            bio = "Christian educator who believes in teaching children with love and patience. I enjoy Sabbath afternoon walks in London parks, classical music, and Bible study groups.",
            relationshipIntention = "Serious Relationship",
            primaryPhoto = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = false,
            verificationStatus = "PENDING",
            isPremium = false,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 10,
            localChurch = "Stanborough Park SDA Church",
            isBaptized = true,
            faithImportance = "Very important",
            churchInvolvement = "Active",
            sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "Nature Walks"),
            ministryInterests = listOf("Education", "Children"),
            personalBibleStudy = "Several times a week",
            favoriteVerse = "Psalm 46:10",
            diet = "Vegetarian",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Open to children",
            hasChildren = false,
            interests = listOf("Reading", "Education", "Sabbath Nature Walks", "Piano")
        )
        dao.insertProfile(elenaProfile)

        val miriamProfile = ProfileEntity(
            userId = "usr_miriam",
            fullName = "Miriam Santos",
            age = 29,
            gender = "Female",
            country = "Brazil",
            city = "São Paulo",
            distanceKm = 85,
            occupation = "Nutritionist & Health Evangelist",
            education = "Centro Universitário Adventista de São Paulo (UNASP)",
            bio = "Dedicated to practical health ministry and helping families thrive through wholesome lifestyle choices. Looking for a grounded, godly man with a warm heart.",
            relationshipIntention = "Marriage",
            primaryPhoto = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            verificationStatus = "VERIFIED",
            isPremium = false,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 29,
            localChurch = "Igreja Adventista do Unasp-SP",
            isBaptized = true,
            faithImportance = "Central to everything I do",
            churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "Community Service"),
            ministryInterests = listOf("Health", "Evangelism", "Bible Study"),
            personalBibleStudy = "Daily",
            favoriteVerse = "3 John 1:2",
            diet = "Vegan",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Yes, definitely",
            hasChildren = false,
            interests = listOf("Health Cooking", "Plant-Based Living", "Mission Trips", "Reading")
        )
        dao.insertProfile(miriamProfile)

        val davidProfile = ProfileEntity(
            userId = "usr_david",
            fullName = "David Kiarie",
            age = 30,
            gender = "Male",
            country = "Kenya",
            city = "Nairobi",
            distanceKm = 30,
            occupation = "Civil Engineer & Pathfinder Master Guide",
            education = "University of Eastern Africa, Baraton",
            bio = "Pathfinder Master Guide and civil engineer. I love outdoor camping, leadership mentoring, and Sabbath afternoon fellowship. Excited to connect with Adventist singles who value faith and family.",
            relationshipIntention = "Marriage",
            primaryPhoto = "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80"
            ),
            isVerified = true,
            verificationStatus = "VERIFIED",
            isPremium = false,
            adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 30,
            localChurch = "Nairobi Central SDA Church (Maxwell)",
            isBaptized = true,
            faithImportance = "Central to everything I do",
            churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "AY Youth Fellowship"),
            ministryInterests = listOf("Youth", "Leadership", "Community Service"),
            personalBibleStudy = "Daily",
            favoriteVerse = "Joshua 24:15",
            diet = "Vegetarian",
            alcohol = "None / Abstain",
            smoking = "Never",
            wantsChildren = "Yes, definitely",
            hasChildren = false,
            interests = listOf("Pathfinders", "Camping", "Leadership", "Hiking")
        )
        dao.insertProfile(davidProfile)

        // Seed mutual likes & matches between "usr_me" and "usr_sarah", "usr_hannah"
        dao.insertLike(LikeEntity(fromUserId = "usr_sarah", toUserId = "usr_me", isSuperLike = false))
        dao.insertLike(LikeEntity(fromUserId = "usr_me", toUserId = "usr_sarah", isSuperLike = false))
        val matchSarah = MatchEntity(
            matchId = "match_sarah_joshua",
            user1Id = "usr_me",
            user2Id = "usr_sarah",
            compatibilityScore = 92,
            conversationStarter = "Both of you value Sabbath worship, Pathfinders, and healthy living!"
        )
        dao.insertMatch(matchSarah)

        dao.insertMessage(
            MessageEntity(
                messageId = "msg_1",
                matchId = "match_sarah_joshua",
                senderId = "usr_sarah",
                receiverId = "usr_me",
                text = "Happy Sabbath Joshua! I saw that you studied at Andrews University. How do you usually spend your Sabbath afternoons in Michigan?",
                timestamp = System.currentTimeMillis() - 3600000 * 5,
                isRead = true
            )
        )
        dao.insertMessage(
            MessageEntity(
                messageId = "msg_2",
                matchId = "match_sarah_joshua",
                senderId = "usr_me",
                receiverId = "usr_sarah",
                text = "Happy Sabbath Sarah! Usually after church at Pioneer, we go for nature walks around Lake Michigan or join AY fellowship. How about at Sligo?",
                timestamp = System.currentTimeMillis() - 3600000 * 2,
                isRead = true
            )
        )

        // Likes received for "usr_me" from Hannah & Miriam (for "Likes You" screen)
        dao.insertLike(LikeEntity(fromUserId = "usr_hannah", toUserId = "usr_me", isSuperLike = true))
        dao.insertLike(LikeEntity(fromUserId = "usr_miriam", toUserId = "usr_me", isSuperLike = false))

        // Initial Notification
        dao.insertNotification(
            NotificationEntity(
                id = "notif_welcome",
                userId = "usr_me",
                title = "Welcome to AdventHearts! 🌸",
                body = "Your Seventh-day Adventist profile is live. Start discovering faith-compatible singles!",
                type = "SYSTEM"
            )
        )
    }
}
