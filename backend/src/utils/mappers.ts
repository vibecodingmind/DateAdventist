import { Profile, FaithProfile, User, Subscription } from '@prisma/client';
import { ageFromDob, parseStringArray } from './json';

type UserWithRelations = User & {
  profile?: Profile | null;
  faithProfile?: FaithProfile | null;
  subscription?: Subscription | null;
};

export function toProfileDto(
  user: UserWithRelations,
  extras?: { compatibilityScore?: number }
) {
  const profile = user.profile;
  const faith = user.faithProfile;
  if (!profile) return null;

  return {
    userId: user.id,
    email: user.email,
    role: user.role,
    accountStatus: user.status,
    isEmailVerified: user.isEmailVerified,
    fullName: profile.fullName,
    age: ageFromDob(profile.dateOfBirth),
    dateOfBirth: profile.dateOfBirth.toISOString(),
    gender: profile.gender,
    country: profile.country,
    city: profile.city,
    latitude: profile.latitude,
    longitude: profile.longitude,
    distanceKm: profile.distanceKm,
    occupation: profile.occupation,
    education: profile.education,
    bio: profile.bio,
    relationshipIntention: profile.relationshipIntention,
    primaryPhoto: profile.primaryPhoto,
    photoUrls: parseStringArray(profile.photoUrls),
    isVerified: profile.isVerified,
    verificationStatus: profile.verificationStatus,
    verificationSelfieUri: profile.verificationSelfie,
    isPremium: profile.isPremium,
    isPaused: profile.isPaused,
    lastActiveText: 'Active today',
    adventistAffiliation: faith?.adventistAffiliation ?? 'Seventh-day Adventist Member',
    yearsAsAdventist: faith?.yearsAsAdventist ?? 0,
    localChurch: faith?.localChurch ?? '',
    isBaptized: faith?.isBaptized ?? true,
    faithImportance: faith?.faithImportance ?? 'Very important',
    churchInvolvement: faith?.churchInvolvement ?? 'Active',
    sabbathObservance: parseStringArray(faith?.sabbathObservance),
    ministryInterests: parseStringArray(faith?.ministryInterests),
    personalBibleStudy: faith?.personalBibleStudy ?? 'Daily',
    favoriteVerse: faith?.favoriteVerse ?? '',
    diet: profile.diet,
    alcohol: profile.alcohol,
    smoking: profile.smoking,
    wantsChildren: profile.wantsChildren,
    hasChildren: profile.hasChildren,
    interests: parseStringArray(profile.interests),
    subscriptionTier: user.subscription?.status === 'ACTIVE' ? user.subscription.plan : 'FREE',
    compatibilityScore: extras?.compatibilityScore,
  };
}

export function toAuthPayload(user: User, profileName?: string, tokens?: { accessToken: string; refreshToken: string }) {
  return {
    userId: user.id,
    email: user.email,
    fullName: profileName ?? '',
    role: user.role,
    isEmailVerified: user.isEmailVerified,
    accessToken: tokens?.accessToken ?? '',
    refreshToken: tokens?.refreshToken ?? '',
    user: {
      id: user.id,
      email: user.email,
      fullName: profileName ?? '',
      role: user.role,
      isEmailVerified: user.isEmailVerified,
    },
  };
}
