import { prisma } from './db/prisma';
import { AuthService } from './auth/auth.service';
import { stringifyStringArray, dobFromAge } from './utils/json';
import { config } from './config';

type SeedUser = {
  id: string;
  email: string;
  password: string;
  role: 'USER' | 'MODERATOR' | 'ADMIN' | 'SUPER_ADMIN';
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
  verificationStatus: 'NOT_VERIFIED' | 'PENDING' | 'VERIFIED' | 'REJECTED';
  isPremium: boolean;
  adventistAffiliation: string;
  yearsAsAdventist: number;
  localChurch: string;
  faithImportance: string;
  churchInvolvement: string;
  sabbathObservance: string[];
  ministryInterests: string[];
  personalBibleStudy: string;
  favoriteVerse: string;
  diet: string;
  interests: string[];
};

const users: SeedUser[] = [
  {
    id: 'usr_me',
    email: 'john.adventist@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'Joshua Miller',
    age: 28,
    gender: 'Male',
    country: 'United States',
    city: 'Berrien Springs, MI',
    distanceKm: 5,
    occupation: 'Software Engineer & Youth Mentor',
    education: 'Andrews University (B.S. Computer Science)',
    bio: 'Devoted Adventist Christian passionate about technology, youth ministry, and nature photography. Looking for a godly partner with whom to share Sabbath peace, family life, and ministry.',
    relationshipIntention: 'Marriage',
    primaryPhoto: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80',
    photoUrls: [
      'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=800&q=80',
    ],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: true,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 28,
    localChurch: 'Pioneer Memorial Church',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service', 'Sunset to Sunset Rest', 'Nature Walks', 'AY Youth Fellowship'],
    ministryInterests: ['Youth', 'Evangelism', 'Technology'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Jeremiah 29:11',
    diet: 'Vegetarian',
    interests: ['Sabbath Nature Walks', 'Youth Ministry', 'A cappella Music', 'Camping', 'Reading'],
  },
  {
    id: 'usr_sarah',
    email: 'sarah.m@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'Sarah Moretz',
    age: 26,
    gender: 'Female',
    country: 'United States',
    city: 'Silver Spring, MD',
    distanceKm: 15,
    occupation: 'Registered Nurse & Pathfinders Leader',
    education: 'Loma Linda University (B.S. Nursing)',
    bio: 'Active Adventist nurse who loves health ministry, Pathfinder camping, and baking healthy plant-based treats. I value kindness, Sabbath rest, and living with purpose.',
    relationshipIntention: 'Marriage',
    primaryPhoto: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
    photoUrls: [
      'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80',
      'https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80',
    ],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: false,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 26,
    localChurch: 'Sligo Seventh-day Adventist Church',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service', 'Sunset to Sunset Rest', 'Nature Walks', 'Community Service'],
    ministryInterests: ['Youth', 'Health', 'Music'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Philippians 4:13',
    diet: 'Plant-Based',
    interests: ['Pathfinders', 'Health Cooking', 'Sabbath Nature Walks', 'A cappella Music'],
  },
  {
    id: 'usr_hannah',
    email: 'hannah.t@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'Hannah Tesfaye',
    age: 25,
    gender: 'Female',
    country: 'Tanzania',
    city: 'Arusha',
    distanceKm: 42,
    occupation: 'Architect & Choir Director',
    education: 'University of Dar es Salaam',
    bio: 'Adventist architect passionate about designing sustainable community spaces and leading gospel choir ministry. Seeking a faith-driven partner ready to build a Christian home grounded in love.',
    relationshipIntention: 'Marriage',
    primaryPhoto: 'https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80',
    photoUrls: ['https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?auto=format&fit=crop&w=800&q=80'],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: true,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 18,
    localChurch: 'Central SDA Church Arusha',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service', 'AY Youth Fellowship', 'Sunset to Sunset Rest'],
    ministryInterests: ['Music', 'Youth', 'Community Service'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Proverbs 3:5-6',
    diet: 'Vegetarian',
    interests: ['A cappella Music', 'Architecture', 'Bible Study', 'Volunteering'],
  },
  {
    id: 'usr_elena',
    email: 'elena.r@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'Elena Rostova',
    age: 27,
    gender: 'Female',
    country: 'United Kingdom',
    city: 'London',
    distanceKm: 120,
    occupation: 'Primary School Teacher',
    education: 'Newbold College of Higher Education',
    bio: 'Christian educator who believes in teaching children with love and patience. I enjoy Sabbath afternoon walks in London parks, classical music, and Bible study groups.',
    relationshipIntention: 'Serious Relationship',
    primaryPhoto: 'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80',
    photoUrls: ['https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80'],
    isVerified: false,
    verificationStatus: 'PENDING',
    isPremium: false,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 10,
    localChurch: 'Stanborough Park SDA Church',
    faithImportance: 'Very important',
    churchInvolvement: 'Active',
    sabbathObservance: ['Church Service', 'Sunset to Sunset Rest', 'Nature Walks'],
    ministryInterests: ['Education', 'Children'],
    personalBibleStudy: 'Several times a week',
    favoriteVerse: 'Psalm 46:10',
    diet: 'Vegetarian',
    interests: ['Reading', 'Education', 'Sabbath Nature Walks', 'Piano'],
  },
  {
    id: 'usr_miriam',
    email: 'miriam.s@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'Miriam Santos',
    age: 29,
    gender: 'Female',
    country: 'Brazil',
    city: 'São Paulo',
    distanceKm: 85,
    occupation: 'Nutritionist & Health Evangelist',
    education: 'Centro Universitário Adventista de São Paulo (UNASP)',
    bio: 'Dedicated to practical health ministry and helping families thrive through wholesome lifestyle choices. Looking for a grounded, godly man with a warm heart.',
    relationshipIntention: 'Marriage',
    primaryPhoto: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80',
    photoUrls: ['https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=800&q=80'],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: false,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 29,
    localChurch: 'Igreja Adventista do Unasp-SP',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service', 'Sunset to Sunset Rest', 'Community Service'],
    ministryInterests: ['Health', 'Evangelism', 'Bible Study'],
    personalBibleStudy: 'Daily',
    favoriteVerse: '3 John 1:2',
    diet: 'Vegan',
    interests: ['Health Cooking', 'Plant-Based Living', 'Mission Trips', 'Reading'],
  },
  {
    id: 'usr_david',
    email: 'david.k@gmail.com',
    password: 'password123',
    role: 'USER',
    fullName: 'David Kiarie',
    age: 30,
    gender: 'Male',
    country: 'Kenya',
    city: 'Nairobi',
    distanceKm: 30,
    occupation: 'Civil Engineer & Pathfinder Master Guide',
    education: 'University of Eastern Africa, Baraton',
    bio: 'Pathfinder Master Guide and civil engineer. I love outdoor camping, leadership mentoring, and Sabbath afternoon fellowship.',
    relationshipIntention: 'Marriage',
    primaryPhoto: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80',
    photoUrls: ['https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=800&q=80'],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: false,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 30,
    localChurch: 'Nairobi Central SDA Church (Maxwell)',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service', 'Sunset to Sunset Rest', 'AY Youth Fellowship'],
    ministryInterests: ['Youth', 'Leadership', 'Community Service'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Joshua 24:15',
    diet: 'Vegetarian',
    interests: ['Pathfinders', 'Camping', 'Leadership', 'Hiking'],
  },
  {
    id: 'usr_admin',
    email: 'admin@adventhearts.com',
    password: 'AdminPass2026!',
    role: 'ADMIN',
    fullName: 'AdventHearts Operations Admin',
    age: 40,
    gender: 'Male',
    country: 'United States',
    city: 'Silver Spring, MD',
    distanceKm: 0,
    occupation: 'Platform Operations',
    education: 'Andrews University',
    bio: 'AdventHearts administrator.',
    relationshipIntention: 'Friendship first',
    primaryPhoto: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=800&q=80',
    photoUrls: [],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: true,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 40,
    localChurch: 'GC Headquarters',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service'],
    ministryInterests: ['Administration'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Micah 6:8',
    diet: 'Vegetarian',
    interests: ['Community'],
  },
  {
    id: 'usr_moderator',
    email: 'moderator@adventhearts.com',
    password: 'AdminPass2026!',
    role: 'MODERATOR',
    fullName: 'AdventHearts Community Moderator',
    age: 34,
    gender: 'Female',
    country: 'United States',
    city: 'Loma Linda, CA',
    distanceKm: 0,
    occupation: 'Community Safety',
    education: 'Loma Linda University',
    bio: 'Community moderator.',
    relationshipIntention: 'Friendship first',
    primaryPhoto: '',
    photoUrls: [],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: true,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 34,
    localChurch: 'Loma Linda University Church',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service'],
    ministryInterests: ['Safety'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Psalm 91:1',
    diet: 'Vegetarian',
    interests: ['Moderation'],
  },
  {
    id: 'usr_super_admin',
    email: 'superadmin@adventhearts.com',
    password: 'AdminPass2026!',
    role: 'SUPER_ADMIN',
    fullName: 'AdventHearts Executive Super Admin',
    age: 45,
    gender: 'Male',
    country: 'United States',
    city: 'Silver Spring, MD',
    distanceKm: 0,
    occupation: 'Executive',
    education: 'Andrews University',
    bio: 'Platform super administrator.',
    relationshipIntention: 'Friendship first',
    primaryPhoto: '',
    photoUrls: [],
    isVerified: true,
    verificationStatus: 'VERIFIED',
    isPremium: true,
    adventistAffiliation: 'Seventh-day Adventist Member',
    yearsAsAdventist: 45,
    localChurch: 'GC Headquarters',
    faithImportance: 'Central to everything I do',
    churchInvolvement: 'Very active',
    sabbathObservance: ['Church Service'],
    ministryInterests: ['Leadership'],
    personalBibleStudy: 'Daily',
    favoriteVerse: 'Proverbs 3:5-6',
    diet: 'Vegetarian',
    interests: ['Leadership'],
  },
];

export async function seedDatabase() {
  if (config.env === 'production' && !config.allowDemoSeed) {
    throw new Error('Demo seed is disabled in production. Set ALLOW_DEMO_SEED=true only on private staging.');
  }
  for (const item of users) {
    const passwordHash = await AuthService.hashPassword(item.password);
    await prisma.user.upsert({
      where: { id: item.id },
      update: {
        email: item.email,
        passwordHash,
        role: item.role,
        status: 'ACTIVE',
        isEmailVerified: true,
      },
      create: {
        id: item.id,
        email: item.email,
        passwordHash,
        role: item.role,
        status: 'ACTIVE',
        isEmailVerified: true,
      },
    });

    await prisma.profile.upsert({
      where: { userId: item.id },
      update: {
        fullName: item.fullName,
        dateOfBirth: dobFromAge(item.age),
        gender: item.gender,
        country: item.country,
        city: item.city,
        distanceKm: item.distanceKm,
        occupation: item.occupation,
        education: item.education,
        bio: item.bio,
        relationshipIntention: item.relationshipIntention,
        primaryPhoto: item.primaryPhoto,
        photoUrls: stringifyStringArray(item.photoUrls),
        isVerified: item.isVerified,
        verificationStatus: item.verificationStatus,
        isPremium: item.isPremium,
        diet: item.diet,
        interests: stringifyStringArray(item.interests),
      },
      create: {
        userId: item.id,
        fullName: item.fullName,
        dateOfBirth: dobFromAge(item.age),
        gender: item.gender,
        country: item.country,
        city: item.city,
        distanceKm: item.distanceKm,
        occupation: item.occupation,
        education: item.education,
        bio: item.bio,
        relationshipIntention: item.relationshipIntention,
        primaryPhoto: item.primaryPhoto,
        photoUrls: stringifyStringArray(item.photoUrls),
        isVerified: item.isVerified,
        verificationStatus: item.verificationStatus,
        isPremium: item.isPremium,
        diet: item.diet,
        interests: stringifyStringArray(item.interests),
      },
    });

    await prisma.faithProfile.upsert({
      where: { userId: item.id },
      update: {
        adventistAffiliation: item.adventistAffiliation,
        yearsAsAdventist: item.yearsAsAdventist,
        localChurch: item.localChurch,
        faithImportance: item.faithImportance,
        churchInvolvement: item.churchInvolvement,
        sabbathObservance: stringifyStringArray(item.sabbathObservance),
        ministryInterests: stringifyStringArray(item.ministryInterests),
        personalBibleStudy: item.personalBibleStudy,
        favoriteVerse: item.favoriteVerse,
      },
      create: {
        userId: item.id,
        adventistAffiliation: item.adventistAffiliation,
        yearsAsAdventist: item.yearsAsAdventist,
        localChurch: item.localChurch,
        faithImportance: item.faithImportance,
        churchInvolvement: item.churchInvolvement,
        sabbathObservance: stringifyStringArray(item.sabbathObservance),
        ministryInterests: stringifyStringArray(item.ministryInterests),
        personalBibleStudy: item.personalBibleStudy,
        favoriteVerse: item.favoriteVerse,
      },
    });

    await prisma.preference.upsert({
      where: { userId: item.id },
      update: {},
      create: { userId: item.id },
    });
  }

  await prisma.subscription.upsert({
    where: { userId: 'usr_me' },
    update: { plan: 'GOLD', status: 'ACTIVE', currentPeriodEnd: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000) },
    create: {
      userId: 'usr_me',
      plan: 'GOLD',
      status: 'ACTIVE',
      currentPeriodEnd: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
    },
  });

  await prisma.like.upsert({
    where: { fromUserId_toUserId: { fromUserId: 'usr_sarah', toUserId: 'usr_me' } },
    update: {},
    create: { fromUserId: 'usr_sarah', toUserId: 'usr_me' },
  });
  await prisma.like.upsert({
    where: { fromUserId_toUserId: { fromUserId: 'usr_me', toUserId: 'usr_sarah' } },
    update: {},
    create: { fromUserId: 'usr_me', toUserId: 'usr_sarah' },
  });
  await prisma.match.upsert({
    where: { user1Id_user2Id: { user1Id: 'usr_me', user2Id: 'usr_sarah' } },
    update: {},
    create: {
      id: 'match_sarah_joshua',
      user1Id: 'usr_me',
      user2Id: 'usr_sarah',
      compatibilityScore: 92,
      conversationStarter: 'Both of you value Sabbath worship, Pathfinders, and healthy living!',
    },
  });

  const existingMsg = await prisma.message.findFirst({ where: { id: 'msg_1' } });
  if (!existingMsg) {
    await prisma.message.createMany({
      data: [
        {
          id: 'msg_1',
          matchId: 'match_sarah_joshua',
          senderId: 'usr_sarah',
          receiverId: 'usr_me',
          text: 'Happy Sabbath Joshua! I saw that you studied at Andrews University. How do you usually spend your Sabbath afternoons in Michigan?',
          isRead: true,
        },
        {
          id: 'msg_2',
          matchId: 'match_sarah_joshua',
          senderId: 'usr_me',
          receiverId: 'usr_sarah',
          text: 'Happy Sabbath Sarah! Usually after church at Pioneer, we go for nature walks around Lake Michigan or join AY fellowship. How about at Sligo?',
          isRead: true,
        },
      ],
    });
  }

  await prisma.like.upsert({
    where: { fromUserId_toUserId: { fromUserId: 'usr_hannah', toUserId: 'usr_me' } },
    update: { isSuperLike: true },
    create: { fromUserId: 'usr_hannah', toUserId: 'usr_me', isSuperLike: true },
  });
  await prisma.like.upsert({
    where: { fromUserId_toUserId: { fromUserId: 'usr_miriam', toUserId: 'usr_me' } },
    update: {},
    create: { fromUserId: 'usr_miriam', toUserId: 'usr_me' },
  });

  await prisma.notification.upsert({
    where: { id: 'notif_welcome' },
    update: {},
    create: {
      id: 'notif_welcome',
      userId: 'usr_me',
      title: 'Welcome to AdventHearts! 🌸',
      body: 'Your Seventh-day Adventist profile is live. Start discovering faith-compatible singles!',
      type: 'SYSTEM',
    },
  });

  await prisma.systemSettings.upsert({
    where: { key: 'provider' },
    update: { value: 'Stripe' },
    create: { key: 'provider', value: 'Stripe' },
  });
}

async function main() {
  console.log('--- AdventHearts database seed ---');
  await seedDatabase();
  console.log('Seed complete. Demo logins:');
  console.log('  Member: john.adventist@gmail.com / password123');
  console.log('  Admin:  admin@adventhearts.com / AdminPass2026!');
  console.log('  Super:  superadmin@adventhearts.com / AdminPass2026!');
}

if (require.main === module) {
  main()
    .catch((err) => {
      console.error(err);
      process.exit(1);
    })
    .finally(async () => {
      await prisma.$disconnect();
    });
}
