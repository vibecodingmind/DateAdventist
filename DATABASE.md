# AdventHearts - Database Specification & Migration Guide

The live schema lives in `backend/prisma/schema.prisma`. Local development uses SQLite (`file:./dev.db`) so the API can run without Docker. Production can keep the same models; swap the Prisma `provider` to `postgresql` and set `DATABASE_URL` when you attach a managed Postgres instance.

## 1. Relational Database Architecture
AdventHearts uses Prisma ORM for relational persistence.

### Key Indexing Strategy
- `User`: unique `email`, status + last active.
- `Profile`: country/city, gender, date of birth.
- `Like`: compound unique `(fromUserId, toUserId)` for idempotent swipes.
- `Match`: compound unique `(user1Id, user2Id)`.
- `Message`: `(matchId, createdAt)` for chat history.

---

## 2. Running Migrations

```bash
cd backend

npx prisma generate
npx prisma db push
npx prisma db seed
```
