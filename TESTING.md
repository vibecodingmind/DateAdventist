# AdventHearts - Automated Testing Guide

## Backend

```bash
cd backend
npm test
```

Jest + Supertest cover:

1. Health check
2. Member login / invalid credentials / registration
3. Profile + discovery feed
4. Reciprocal like → match
5. Chat send/list
6. Report + block
7. Subscription checkout confirmation
8. Admin RBAC (member denied, admin allowed, super-admin settings)

The Android UI still uses Room for offline demo data; network calls go through `AdventHeartsApiClient`.
