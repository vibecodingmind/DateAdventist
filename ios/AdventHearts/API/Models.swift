import Foundation

struct APIErrorBody: Decodable {
    let code: String?
    let message: String?
}

struct Envelope<T: Decodable>: Decodable {
    let success: Bool
    let data: T?
    let error: APIErrorBody?
}

struct AuthUser: Codable, Identifiable {
    var id: String { userId }
    let userId: String
    let email: String
    let fullName: String
    let role: String
    let accessToken: String
    let refreshToken: String
    let isEmailVerified: Bool?

    init(userId: String, email: String, fullName: String, role: String, accessToken: String, refreshToken: String, isEmailVerified: Bool?) {
        self.userId = userId
        self.email = email
        self.fullName = fullName
        self.role = role
        self.accessToken = accessToken
        self.refreshToken = refreshToken
        self.isEmailVerified = isEmailVerified
    }

    enum CodingKeys: String, CodingKey {
        case userId, email, fullName, role, accessToken, refreshToken, isEmailVerified
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        userId = try c.decode(String.self, forKey: .userId)
        email = try c.decode(String.self, forKey: .email)
        fullName = try c.decodeIfPresent(String.self, forKey: .fullName) ?? ""
        role = try c.decodeIfPresent(String.self, forKey: .role) ?? "USER"
        accessToken = try c.decodeIfPresent(String.self, forKey: .accessToken) ?? ""
        refreshToken = try c.decodeIfPresent(String.self, forKey: .refreshToken) ?? ""
        isEmailVerified = try c.decodeIfPresent(Bool.self, forKey: .isEmailVerified)
    }
}

struct Profile: Codable, Identifiable {
    var id: String { userId }
    let userId: String
    var fullName: String
    var age: Int
    var gender: String
    var country: String
    var city: String
    var occupation: String
    var bio: String
    var relationshipIntention: String
    var primaryPhoto: String
    var photoUrls: [String]
    var isVerified: Bool
    var verificationStatus: String
    var isPremium: Bool
    var localChurch: String
    var favoriteVerse: String
    var diet: String
    var interests: [String]
    var compatibilityScore: Int?

    enum CodingKeys: String, CodingKey {
        case userId, fullName, age, gender, country, city, occupation, bio
        case relationshipIntention, primaryPhoto, photoUrls, isVerified, verificationStatus
        case isPremium, localChurch, favoriteVerse, diet, interests, compatibilityScore
    }

    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        userId = try c.decode(String.self, forKey: .userId)
        fullName = try c.decodeIfPresent(String.self, forKey: .fullName) ?? ""
        age = try c.decodeIfPresent(Int.self, forKey: .age) ?? 18
        gender = try c.decodeIfPresent(String.self, forKey: .gender) ?? ""
        country = try c.decodeIfPresent(String.self, forKey: .country) ?? ""
        city = try c.decodeIfPresent(String.self, forKey: .city) ?? ""
        occupation = try c.decodeIfPresent(String.self, forKey: .occupation) ?? ""
        bio = try c.decodeIfPresent(String.self, forKey: .bio) ?? ""
        relationshipIntention = try c.decodeIfPresent(String.self, forKey: .relationshipIntention) ?? "Marriage"
        primaryPhoto = try c.decodeIfPresent(String.self, forKey: .primaryPhoto) ?? ""
        photoUrls = try c.decodeIfPresent([String].self, forKey: .photoUrls) ?? []
        isVerified = try c.decodeIfPresent(Bool.self, forKey: .isVerified) ?? false
        verificationStatus = try c.decodeIfPresent(String.self, forKey: .verificationStatus) ?? "NOT_VERIFIED"
        isPremium = try c.decodeIfPresent(Bool.self, forKey: .isPremium) ?? false
        localChurch = try c.decodeIfPresent(String.self, forKey: .localChurch) ?? ""
        favoriteVerse = try c.decodeIfPresent(String.self, forKey: .favoriteVerse) ?? ""
        diet = try c.decodeIfPresent(String.self, forKey: .diet) ?? ""
        interests = try c.decodeIfPresent([String].self, forKey: .interests) ?? []
        compatibilityScore = try c.decodeIfPresent(Int.self, forKey: .compatibilityScore)
    }
}

struct Match: Codable, Identifiable {
    var id: String { matchId }
    let matchId: String
    let user1Id: String
    let user2Id: String
    let compatibilityScore: Int
    let conversationStarter: String
    let otherProfile: Profile?
}

struct ChatMessage: Codable, Identifiable {
    var id: String { messageId }
    let messageId: String
    let matchId: String
    let senderId: String
    let receiverId: String
    let text: String
    let timestamp: Int64
    let isRead: Bool?
}

struct LikeReceived: Codable, Identifiable {
    var id: String { fromUserId }
    let fromUserId: String
    let isSuperLike: Bool?
    let profile: Profile?
}

struct LikeResult: Codable {
    let isMatch: Bool
    let matchId: String?
    let compatibilityScore: Int?
}

struct SubscriptionStatus: Codable {
    let active: Bool
    let tier: String
}

struct CheckoutResult: Codable {
    let checkoutUrl: String
    let transactionId: String
}

struct MessageAck: Codable {
    let message: String?
}

struct UnmatchResult: Codable {
    let unmatched: Bool?
}

struct UploadResult: Codable {
    let url: String
    let profile: Profile?
}
