import dotenv from 'dotenv';
import path from 'path';

dotenv.config({ path: path.resolve(__dirname, '../../.env') });
dotenv.config();

export const config = {
  env: process.env.NODE_ENV || 'development',
  port: parseInt(process.env.PORT || '5000', 10),
  jwtSecret: process.env.JWT_SECRET || 'adventhearts_dev_jwt_secret_change_me_32ch',
  jwtRefreshSecret: process.env.JWT_REFRESH_SECRET || 'adventhearts_dev_refresh_secret_change_me',
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || '15m',
  jwtRefreshExpiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '7d',
  argonMemoryCost: parseInt(process.env.ARGON_MEMORY_COST || (process.env.NODE_ENV === 'test' ? '4096' : '65536'), 10),
  argonTimeCost: parseInt(process.env.ARGON_TIME_COST || (process.env.NODE_ENV === 'test' ? '2' : '3'), 10),
  databaseUrl: process.env.DATABASE_URL || 'file:./dev.db',
  stripeSecretKey: process.env.STRIPE_SECRET_KEY || '',
  stripeWebhookSecret: process.env.STRIPE_WEBHOOK_SECRET || '',
  emailApiKey: process.env.EMAIL_API_KEY || '',
  emailFrom: process.env.EMAIL_FROM || 'AdventHearts <no-reply@adventhearts.com>',
  publicBaseUrl: process.env.PUBLIC_BASE_URL || '',
};

if (process.env.NODE_ENV === 'production') {
  const weak = ['change_me', 'adventhearts_dev', 'super_secret'];
  if (!process.env.JWT_SECRET || weak.some((w) => process.env.JWT_SECRET!.includes(w))) {
    console.warn('WARNING: Set a strong JWT_SECRET before going live.');
  }
}

export const SUBSCRIPTION_PLANS = [
  {
    id: 'plan_plus_monthly',
    name: 'AdventHearts Plus',
    tier: 'PLUS',
    priceMonthly: 9.99,
    priceAnnually: 79.99,
    currency: 'USD',
    badgeText: '',
    features: [
      'Unlimited Likes & Swipes',
      'See Who Likes You',
      '1 Super Like per week',
    ],
  },
  {
    id: 'plan_gold_monthly',
    name: 'AdventHearts Gold',
    tier: 'GOLD',
    priceMonthly: 14.99,
    priceAnnually: 119.88,
    currency: 'USD',
    badgeText: 'MOST POPULAR',
    features: [
      'Unlimited Likes & Swipes',
      'See Who Likes You',
      '5 Free Super Likes per week',
      'Advanced Faith & Lifestyle Search Filters',
      '1 Free Profile Boost per month',
      'Gold Verification Badge',
    ],
  },
  {
    id: 'plan_platinum_monthly',
    name: 'AdventHearts Platinum',
    tier: 'PLATINUM',
    priceMonthly: 24.99,
    priceAnnually: 199.88,
    currency: 'USD',
    badgeText: 'BEST VALUE',
    features: [
      'All Gold Plan Features Included',
      'Priority Messaging in Inbox',
      'Message Before Matching',
      'See Read Receipts',
      'Unlimited Rewinds',
      'Platinum Crown Badge & VIP Support',
    ],
  },
];
