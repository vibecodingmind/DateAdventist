export type AuthUser = {
  userId: string;
  email: string;
  fullName: string;
  role: string;
  accessToken: string;
  refreshToken: string;
  isEmailVerified?: boolean;
};

export type Profile = {
  userId: string;
  fullName: string;
  age: number;
  gender: string;
  country: string;
  city: string;
  distanceKm: number;
  occupation: string;
  education: string;
  bio: string;
  relationshipIntention: string;
  primaryPhoto: string;
  photoUrls: string[];
  isVerified: boolean;
  verificationStatus: string;
  isPremium: boolean;
  lastActiveText: string;
  adventistAffiliation: string;
  yearsAsAdventist: number;
  localChurch: string;
  isBaptized: boolean;
  faithImportance: string;
  churchInvolvement: string;
  sabbathObservance: string[];
  ministryInterests: string[];
  favoriteVerse: string;
  diet: string;
  alcohol: string;
  smoking: string;
  wantsChildren: string;
  hasChildren: boolean;
  interests: string[];
  compatibilityScore?: number;
};

export type Match = {
  matchId: string;
  user1Id: string;
  user2Id: string;
  compatibilityScore: number;
  conversationStarter: string;
  matchedAt?: string;
  lastMessage?: { text: string; senderId: string } | null;
  otherProfile: Profile | null;
};

export type ChatMessage = {
  messageId: string;
  matchId: string;
  senderId: string;
  receiverId: string;
  text: string;
  timestamp: number;
  isRead: boolean;
};

export type LikeReceived = {
  likeId?: string;
  fromUserId: string;
  isSuperLike: boolean;
  profile: Profile | null;
};

export type Subscription = {
  active: boolean;
  tier: string;
  plan?: string;
  status?: string;
  expiresAt?: string | null;
};

export type NotificationItem = {
  id: string;
  title: string;
  body: string;
  type: string;
  isRead: boolean;
  timestamp: number;
};
