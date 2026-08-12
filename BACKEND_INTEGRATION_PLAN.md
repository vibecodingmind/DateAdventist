# AdventHearts - Backend Integration Plan

## 1. Executive Summary
AdventHearts is a faith-centered dating and fellowship application tailored for the Seventh-day Adventist community. This document outlines the backend architecture, database schema, REST API specifications, external integrations, and frontend-to-backend migration strategy to transition AdventHearts from its current client-side Room database architecture to a production-grade multi-tier backend infrastructure.

---

## 2. Frontend Feature Inventory & Current State Analysis

| Frontend Feature Module | Existing UI Screens / Composables | Current State / Persistence | Required Backend Capabilities |
| :--- | :--- | :--- | :--- |
| **Auth & Onboarding** | `WelcomeScreen.kt`<br>`OnboardingScreen.kt` | Room `user_accounts`, `profiles` | JWT authentication, email verification, password hashing (Argon2id), session tokens. |
| **Discovery & Deck** | `DiscoverScreen.kt`<br>`DiscoveryViewModel.kt` | Room queries for profiles | Recommendation engine, geographic distance calculation, faith/lifestyle filtering, pagination. |
| **Likes & Passes** | `LikesScreen.kt` | Room `user_likes`, `user_passes` | Server-side match trigger, mutual swipe checks, super-like quotas. |
| **Matching & Compatibility** | `MatchesScreen.kt`<br>`MatchesViewModel.kt` | Room `matches` | Weighted compatibility scoring engine (`CompatibilityService`), mutual match record creation. |
| **Messaging / Chat** | `ChatDetailScreen.kt`<br>`MessagesScreen.kt` | Room `messages` | Realtime WebSocket/Socket.IO communication, read receipts, message delivery persistence. |
| **Safety & Moderation** | `SafetyCenterScreen.kt` | Room `reports`, `blocks` | Report queuing, blocking enforcement in discovery/messages, photo content moderation. |
| **Verification** | `ProfileScreen.kt` | Room `verificationStatus` | Image storage presigned URLs, admin verification queue, audit logging. |
| **Subscriptions & Billing** | `SubscriptionScreen.kt` | ViewModel state | Stripe/PayPal checkout session creation, webhook handling, entitlement verification. |
| **Admin Dashboard** | `AdminDashboardScreen.kt`<br>`AdminViewModel.kt` | Room queries & state | RBAC protected routes, aggregated analytics, system configuration endpoints. |

---

## 3. Production Architecture & Stack

```
                         CLIENT LAYER
                    ┌──────────────────┐
                    │ Android App      │
                    │ (Kotlin/Compose) │
                    └────────┬─────────┘
                             │ HTTPS / WSS
                             ▼
                         API GATEWAY
                    ┌──────────────────┐
                    │ REST API / WS    │
                    │ Node.js/Express  │
                    │ TypeScript       │
                    └────────┬─────────┘
                             │
          ┌──────────────────┼──────────────────┐
          ▼                  ▼                  ▼
   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
   │  PostgreSQL  │   │  S3 Storage  │   │ Redis Cache  │
   │   + Prisma   │   │  (Photos)    │   │ (Realtime/   │
   └──────────────┘   └──────────────┘   │  RateLimit)  │
                                         └──────────────┘
```

- **Runtime**: Node.js v20 LTS / TypeScript 5.x
- **Framework**: Express.js with Zod validation
- **ORM / Database**: Prisma ORM with PostgreSQL
- **Realtime**: Socket.IO / WebSockets with Redis Adapter
- **Storage**: AWS S3 / Cloudflare R2 object storage with Presigned URLs
- **Payments**: Stripe API + PayPal SDK + Webhooks
- **Email**: Transactional email via Resend / SendGrid

---

## 4. Prisma Database Schema Overview

```prisma
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

enum Role {
  USER
  MODERATOR
  ADMIN
  SUPER_ADMIN
}

enum AccountStatus {
  ACTIVE
  PAUSED
  SUSPENDED
  BANNED
  DELETED
}

enum VerificationStatus {
  NOT_VERIFIED
  PENDING
  VERIFIED
  REJECTED
}

model User {
  id                String            @id @default(uuid())
  email             String            @unique
  passwordHash      String
  role              Role              @default(USER)
  status            AccountStatus     @default(ACTIVE)
  isEmailVerified   Boolean           @default(false)
  createdAt         DateTime          @default(now())
  updatedAt         DateTime          @updatedAt
  lastActiveAt      DateTime          @default(now())

  profile           Profile?
  sentLikes         Like[]            @relation("LikesSent")
  receivedLikes     Like[]            @relation("LikesReceived")
  sentPasses        Pass[]            @relation("PassesSent")
  user1Matches      Match[]           @relation("User1Matches")
  user2Matches      Match[]           @relation("User2Matches")
  sentMessages      Message[]         @relation("SentMessages")
  receivedMessages  Message[]         @relation("ReceivedMessages")
  notifications     Notification[]
  reportsFiled      Report[]          @relation("Reporter")
  reportsReceived   Report[]          @relation("ReportedUser")
  blockedUsers      Block[]           @relation("Blocker")
  blockedBy         Block[]           @relation("Blocked")
  subscription      Subscription?
  auditLogs         AuditLog[]
}

model Profile {
  id                    String             @id @default(uuid())
  userId                String             @unique
  user                  User               @relation(fields: [userId], references: [id], onDelete: Cascade)
  fullName              String
  dateOfBirth           DateTime
  gender                String
  country               String
  city                  String
  latitude              Float?
  longitude             Float?
  occupation            String
  education             String
  bio                   String             @db.Text
  relationshipIntention String
  primaryPhoto          String
  photoUrls             String[]
  isVerified            Boolean            @default(false)
  verificationStatus    VerificationStatus @default(NOT_VERIFIED)
  verificationSelfie    String?
  isPremium             Boolean            @default(false)
  isPaused              Boolean            @default(false)

  // Faith Profile
  adventistAffiliation  String
  yearsAsAdventist      Int
  localChurch           String
  isBaptized            Boolean            @default(true)
  faithImportance       String
  churchInvolvement     String
  sabbathObservance     String[]
  ministryInterests     String[]
  personalBibleStudy    String
  favoriteVerse         String?

  // Lifestyle Profile
  diet                  String
  alcohol               String
  smoking               String
  wantsChildren         String
  hasChildren           Boolean            @default(false)
  interests             String[]
}

model Like {
  id          String   @id @default(uuid())
  fromUserId  String
  toUserId    String
  isSuperLike Boolean  @default(false)
  createdAt   DateTime @default(now())

  fromUser    User     @relation("LikesSent", fields: [fromUserId], references: [id])
  toUser      User     @relation("LikesReceived", fields: [toUserId], references: [id])

  @@unique([fromUserId, toUserId])
}

model Pass {
  id          String   @id @default(uuid())
  fromUserId  String
  toUserId    String
  createdAt   DateTime @default(now())

  fromUser    User     @relation("PassesSent", fields: [fromUserId], references: [id])

  @@unique([fromUserId, toUserId])
}

model Match {
  id                  String    @id @default(uuid())
  user1Id             String
  user2Id             String
  compatibilityScore  Int
  conversationStarter String
  matchedAt           DateTime  @default(now())

  user1               User      @relation("User1Matches", fields: [user1Id], references: [id])
  user2               User      @relation("User2Matches", fields: [user2Id], references: [id])
  messages            Message[]

  @@unique([user1Id, user2Id])
}

model Message {
  id          String   @id @default(uuid())
  matchId     String
  senderId    String
  receiverId  String
  text        String   @db.Text
  isRead      Boolean  @default(false)
  createdAt   DateTime @default(now())

  match       Match    @relation(fields: [matchId], references: [id], onDelete: Cascade)
  sender      User     @relation("SentMessages", fields: [senderId], references: [id])
  receiver    User     @relation("ReceivedMessages", fields: [receiverId], references: [id])
}

model Notification {
  id        String   @id @default(uuid())
  userId    String
  title     String
  body      String
  type      String
  isRead    Boolean  @default(false)
  createdAt DateTime @default(now())

  user      User     @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model Report {
  id             String   @id @default(uuid())
  reporterId     String
  reportedUserId String
  reason         String
  details        String   @db.Text
  status         String   @default("OPEN")
  createdAt      DateTime @default(now())

  reporter       User     @relation("Reporter", fields: [reporterId], references: [id])
  reportedUser   User     @relation("ReportedUser", fields: [reportedUserId], references: [id])
}

model Block {
  id            String   @id @default(uuid())
  blockerId     String
  blockedUserId String
  createdAt     DateTime @default(now())

  blocker       User     @relation("Blocker", fields: [blockerId], references: [id])
  blocked       User     @relation("Blocked", fields: [blockedUserId], references: [id])

  @@unique([blockerId, blockedUserId])
}

model Subscription {
  id                   String   @id @default(uuid())
  userId               String   @unique
  plan                 String   // MONTHLY, ANNUAL, LIFETIME
  stripeSubscriptionId String?
  status               String   // ACTIVE, CANCELED, PAST_DUE
  currentPeriodEnd     DateTime
  createdAt            DateTime @default(now())

  user                 User     @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model SystemSettings {
  key       String   @id
  value     String
  updatedAt DateTime @updatedAt
}

model AuditLog {
  id        String   @id @default(uuid())
  actorId   String
  action    String
  target    String
  details   String?
  ipAddress String?
  createdAt DateTime @default(now())

  actor     User     @relation(fields: [actorId], references: [id])
}
```

---

## 5. API Endpoint Specifications

### Auth Endpoints (`/api/v1/auth`)
- `POST /register`: Registers user account and profile with Argon2id hashed password.
- `POST /login`: Authenticates user, returns JWT Access Token & Refresh Token.
- `POST /refresh-token`: Rotates JWT refresh token.
- `POST /logout`: Invalidates session.

### Discovery & Matches (`/api/v1/discover`, `/api/v1/matches`)
- `GET /discover`: Fetches candidate profiles matching age, gender, faith, distance, and excluding liked/passed/blocked users.
- `POST /discover/:targetUserId/like`: Records like. Server checks reciprocal like. If found, automatically executes `MatchingService.createMatch()`, generates compatibility breakdown, creates notifications, and emits WebSocket event `match:new`.
- `POST /discover/:targetUserId/pass`: Records pass.
- `GET /matches`: Returns all active mutual matches for authenticated user.

### Chat & Realtime (`/api/v1/conversations`)
- `GET /conversations/:matchId/messages`: Fetches paginated chat message history.
- `POST /conversations/:matchId/messages`: Sends message via REST (fallback) and broadcasts to WebSocket channel `chat:message`.
- WebSocket events: `chat:typing`, `chat:read_receipt`, `user:online_status`.

### Admin & System Config (`/api/v1/admin`)
- `GET /admin/analytics`: Provides aggregated user counts, conversion rates, and revenue metrics.
- `GET /admin/verifications`: Retrieves pending photo verification requests.
- `POST /admin/verifications/:userId/approve`: Approves selfie verification, issues verified badge.
- `PUT /admin/settings`: Saves payment provider keys (Stripe/PayPal), price tiers, and platform rules into `SystemSettings`.

---

## 6. Frontend Integration Strategy (Android Client)

1. **Repository Dual-Source**:
   `AdventHeartsRepository` maintains local Room caching while syncing with `AdventHeartsApiService` (Ktor/Retrofit client).
2. **Offline-First Resilience**:
   When offline, user actions (likes, messages) are queued locally and synchronized once network connectivity resumes.
3. **Security Compliance**:
   No authorization decisions or pricing determinations are executed on the client; the Android app strictly acts as a render target for backend state.

---

## 7. Next Moves & Roadmap
1. Deploy Node.js/TypeScript backend service with Prisma migration scripts.
2. Configure S3 bucket presigned upload policies for profile photos and verification selfies.
3. Hook Stripe Webhooks (`/api/v1/webhooks/stripe`) to automatically grant premium benefits upon successful payment.
