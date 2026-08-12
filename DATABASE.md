# AdventHearts - Database Specification & Migration Guide

## 1. Relational Database Architecture
AdventHearts utilizes PostgreSQL with Prisma ORM for relational persistence.

### Key Indexing Strategy
- `User`: Index on `email` (unique), `status`, `lastActiveAt`.
- `Profile`: Spatial/B-tree index on `(country, city)`, `adventistAffiliation`, `dateOfBirth`.
- `Like`: Compound unique index on `(fromUserId, toUserId)` to guarantee idempotent swiping and eliminate race conditions.
- `Match`: Compound index on `(user1Id, user2Id)` and B-Tree index on `matchedAt`.
- `Message`: Index on `(matchId, createdAt DESC)` for efficient paginated chat retrieval.

---

## 2. Running Migrations

```bash
# Generate Prisma client
npx prisma generate

# Execute development migration
npx prisma migrate dev --name init_schema

# Execute production migration deployment
npx prisma migrate deploy

# Seed database with initial admin and sample Adventist profiles
npx prisma db seed
```
