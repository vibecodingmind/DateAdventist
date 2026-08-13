import { Profile, FaithProfile } from '@prisma/client';
import { parseStringArray } from '../utils/json';

type Scoreable = {
  adventistAffiliation?: string | null;
  faithImportance?: string | null;
  isBaptized?: boolean | null;
  relationshipIntention?: string | null;
  diet?: string | null;
  alcohol?: string | null;
  wantsChildren?: string | null;
  country?: string | null;
  city?: string | null;
  interests?: string[] | null;
  ministryInterests?: string[] | null;
  dateOfBirth?: Date | null;
};

function flatten(profile?: Profile | null, faith?: FaithProfile | null): Scoreable {
  if (!profile) return {};
  return {
    adventistAffiliation: faith?.adventistAffiliation,
    faithImportance: faith?.faithImportance,
    isBaptized: faith?.isBaptized,
    relationshipIntention: profile.relationshipIntention,
    diet: profile.diet,
    alcohol: profile.alcohol,
    wantsChildren: profile.wantsChildren,
    country: profile.country,
    city: profile.city,
    interests: parseStringArray(profile.interests),
    ministryInterests: parseStringArray(faith?.ministryInterests),
    dateOfBirth: profile.dateOfBirth,
  };
}

export class CompatibilityService {
  static score(
    a: { profile?: Profile | null; faithProfile?: FaithProfile | null } | Scoreable,
    b: { profile?: Profile | null; faithProfile?: FaithProfile | null } | Scoreable
  ): number {
    const p1 = 'profile' in a ? flatten(a.profile, a.faithProfile) : (a as Scoreable);
    const p2 = 'profile' in b ? flatten(b.profile, b.faithProfile) : (b as Scoreable);

    let score = 50;

    if (p1.adventistAffiliation && p1.adventistAffiliation === p2.adventistAffiliation) score += 10;
    if (p1.faithImportance && p1.faithImportance === p2.faithImportance) score += 8;
    if (p1.isBaptized === p2.isBaptized) score += 7;

    if (p1.relationshipIntention && p1.relationshipIntention === p2.relationshipIntention) {
      score += 20;
    } else if (
      (p1.relationshipIntention || '').includes('Marriage') &&
      (p2.relationshipIntention || '').includes('Serious')
    ) {
      score += 15;
    } else {
      score += 5;
    }

    if (p1.diet && p1.diet === p2.diet) score += 8;
    if (p1.alcohol && p1.alcohol === p2.alcohol) score += 7;
    if (p1.wantsChildren && p1.wantsChildren === p2.wantsChildren) score += 15;
    if (p1.country && p1.country === p2.country) score += 6;
    if (p1.city && p1.city === p2.city) score += 4;

    const commonInterests = (p1.interests || []).filter((i) => (p2.interests || []).includes(i));
    score += Math.min(commonInterests.length * 3, 10);

    return Math.max(60, Math.min(99, score));
  }

  static conversationStarter(
    a: { profile?: Profile | null; faithProfile?: FaithProfile | null },
    b: { profile?: Profile | null; faithProfile?: FaithProfile | null }
  ): string {
    const p1 = flatten(a.profile, a.faithProfile);
    const p2 = flatten(b.profile, b.faithProfile);

    const commonMinistry = (p1.ministryInterests || []).filter((i) => (p2.ministryInterests || []).includes(i));
    if (commonMinistry.length > 0) {
      return `Both of you share a passion for ${commonMinistry[0]} Ministry! Ask how they got started.`;
    }
    const commonInterests = (p1.interests || []).filter((i) => (p2.interests || []).includes(i));
    if (commonInterests.length > 0) {
      return `You both enjoy ${commonInterests[0]}! Great topic for a conversation starter.`;
    }
    return 'You share strong faith values and marriage intentions! Ask about their favorite Sabbath activity.';
  }
}
