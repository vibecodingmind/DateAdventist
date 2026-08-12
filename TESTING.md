# AdventHearts - Automated Testing Guide

## 1. Test Architecture
The backend testing suite includes Unit, Integration, and End-to-End (E2E) tests executed via **Jest** and **Supertest**.

```bash
# Run unit tests
npm run test:unit

# Run integration tests against test PostgreSQL container
npm run test:integration

# Run full E2E user & admin journey tests
npm run test:e2e
```

---

## 2. Tested Key User & Admin Journeys

### User Journey Coverage
1. `User Registration` -> `Email Verification` -> `Profile Completion` -> `Selfie Upload`.
2. `Discover Profiles` -> `Send Like` -> `Reciprocal Like Event` -> `Match Record Created`.
3. `Open Chat` -> `Send Message` -> `WebSocket Broadcast` -> `Read Receipt`.
4. `Subscribe Premium` -> `Stripe Checkout Session` -> `Webhook Trigger` -> `isPremium Granted`.

### Admin Journey Coverage
1. `Admin Authentication` -> `RBAC Middleware Pass`.
2. `Fetch Pending Verifications` -> `Approve Selfie` -> `Verified Badge Granted`.
3. `Update Payment Settings` -> `SystemSettings Updated in Database`.
