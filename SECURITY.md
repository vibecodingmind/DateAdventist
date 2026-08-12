# AdventHearts - Security & Data Protection Policies

## 1. Cryptography & Authentication
- **Password Storage**: Passwords are hashed using **Argon2id** (memory cost: 64MB, iterations: 3, parallelism: 1). Plaintext passwords are never logged or stored.
- **JWT Architecture**:
  - Access Token: RS256 / HS256 signed, 15-minute lifespan.
  - Refresh Token: Cryptographically secure random token stored as SHA-256 hash in database with automatic rotation.

---

## 2. Authorization & RBAC
Endpoints enforce mandatory server-side middleware:
- `requireAuth()`: Validates JWT signature and ensures account status is `ACTIVE`.
- `requireRole(['ADMIN', 'SUPER_ADMIN'])`: Validates user role.
- `requirePermission('moderation:write')`: Checks fine-grained RBAC permission matrix.

---

## 3. Input Sanitization & Threat Protection
- **Injection Prevention**: Prisma ORM uses parameterized queries exclusively.
- **XSS & HTML Injection**: User bio and messages are stripped of raw HTML/script tags via Zod validators.
- **Rate Limiting**:
  - Auth Login: 5 requests per minute per IP.
  - Swiping / Likes: 100 requests per hour (Free Users) / Unlimited (Premium Users).
  - Messages: 60 per minute.
- **Audit Logging**: All administrative actions (user bans, verification approvals, payment refunds, settings changes) record an indelible `AuditLog` entry.
