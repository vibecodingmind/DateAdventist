# AdventHearts - Backend Architecture Specification

## 1. Architectural Overview
The AdventHearts backend is built as a layered TypeScript service application operating over Node.js with PostgreSQL, Prisma ORM, Redis, and Socket.IO.

```
                      +-------------------+
                      |   Client Layer    |
                      | (Android App/Web) |
                      +---------+---------+
                                |
                                v
                      +-------------------+
                      |   Express API     |
                      | (REST Gateway)    |
                      +---------+---------+
                                |
             +------------------+------------------+
             |                  |                  |
             v                  v                  v
    +-----------------+ +---------------+ +-----------------+
    | Services Layer  | | Auth & RBAC   | | Realtime Engine |
    | (Business Logic)| | Middleware    | | (Socket.IO)     |
    +--------+--------+ +-------+-------+ +--------+--------+
             |                  |                  |
             +------------------+------------------+
                                |
                                v
                      +-------------------+
                      |   Prisma ORM      |
                      +---------+---------+
                                |
                                v
                      +-------------------+
                      |   PostgreSQL      |
                      +-------------------+
```

---

## 2. Directory & Module Structure

```
backend/
├── src/
│   ├── config/             # App, DB, Redis, and Security constants
│   ├── controllers/        # Request handlers & response formatting
│   ├── middleware/         # Auth, RBAC, Rate Limiter, Error Handler
│   ├── routes/             # REST route definitions (/api/v1/*)
│   ├── services/           # Core business logic (Matching, Billing, Compatibility)
│   ├── repositories/       # Prisma database query abstractions
│   ├── validators/         # Zod schema definitions
│   ├── realtime/           # Socket.IO event handlers and adapter
│   ├── storage/            # S3 presigned URL generator & thumbnail utility
│   ├── email/              # Email templates & transactional sender
│   └── server.ts           # Application entrypoint & HTTP server
├── prisma/
│   ├── schema.prisma       # Complete database schema
│   ├── migrations/         # SQL migration history
│   └── seed.ts             # Development and initial admin seeding script
├── package.json
└── tsconfig.json
```

---

## 3. Core Business Logic Engines

### A. Compatibility Engine (`CompatibilityService`)
Calculates a 0-100% compatibility score based on weighted Adventist faith and lifestyle attributes:
- **Adventist Faith Affiliation & Importance** (25%)
- **Relationship Intention & Goals** (20%)
- **Lifestyle & Diet (Vegetarian/Plant-Based, Abstinence)** (15%)
- **Family & Children Intentions** (15%)
- **Geographic Distance** (10%)
- **Church Involvement & Ministry Interests** (10%)
- **Age Proximity** (5%)

### B. Matching Engine (`MatchingService`)
1. Atomically inserts a `Like` record with a database-level `[fromUserId, toUserId]` unique constraint.
2. Checks for a reciprocal `Like` record from `toUserId` to `fromUserId`.
3. If reciprocal like exists:
   - Instantiates a `Match` record with initial conversation starter prompts.
   - Triggers real-time notification to both connected client sockets.
   - Returns mutual match metadata to caller.

### C. Payment & Subscription Abstraction (`PaymentProvider`)
Encapsulates payment processing logic behind a provider interface:
- `StripeProvider`: Manages Stripe Checkout Sessions, Customer Portal, and Webhook signature verification (`customer.subscription.updated`, `checkout.session.completed`).
- `PayPalProvider`: Handles PayPal Orders API and Subscription webhooks.
- Both providers update the user's `Subscription` model state and toggle `isPremium` status.

---

## 4. Realtime Architecture
- **Adapter**: Redis Pub/Sub Adapter for horizontal scaling.
- **Authentication**: Socket connection handshake verifies JWT bearer token.
- **Channels**:
  - `user:{userId}`: Private channel for matches, likes, and system alerts.
  - `chat:{matchId}`: Room-based socket channel for instantaneous peer-to-peer chat, typing indicators, and read confirmations.
