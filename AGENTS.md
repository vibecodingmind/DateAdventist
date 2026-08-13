# AdventHearts

Faith-first Seventh-day Adventist dating app: an **Android (Kotlin/Compose)** client in `app/` plus a **Node.js/Express/TypeScript** backend in `backend/`.

## Cursor Cloud specific instructions

### Scope in the cloud VM
- The runnable, testable service in this environment is the **backend** (`backend/`). Lint, tests, and the API server all run headlessly here.
- The **Android client** (`app/`) requires the Android SDK + emulator, which are **not** installed in the cloud VM. It cannot be built or run here; treat it as out of scope for cloud verification and rely on the backend for end-to-end checks.

### Backend (`backend/`)
- Stack: Express + Socket.IO + Prisma. Local/dev uses **SQLite** (`DATABASE_URL=file:./dev.db`), so no external Postgres/Redis is needed to run or test. The root `.env.example` shows a Postgres/production profile; the dev profile lives in `backend/.env.example` and is what to use here.
- Config is read from `backend/.env` (see `backend/src/config/index.ts`). The update script does not create this file; if `backend/.env` is missing, run `cp backend/.env.example backend/.env`.
- Standard commands (from `backend/package.json`), run inside `backend/`:
  - Run (dev, hot-reload): `npm run dev` — serves on `http://localhost:5000` (health: `GET /health`).
  - Lint / type-check: `npm run type-check`.
  - Tests: `npm test` (Jest + Supertest; uses a separate `test.db` via `src/test/setup-env.ts`).
  - DB setup: `npm run db:setup` (= `prisma generate && prisma db push && npm run seed`). The seed prints demo logins.
- Seeded demo accounts: member `john.adventist@gmail.com` / `password123`, admin `admin@adventhearts.com` / `AdminPass2026!`, super admin `superadmin@adventhearts.com` / `AdminPass2026!`.
- The SQLite `dev.db` is gitignored and persists in the VM snapshot. If it is missing/empty (e.g. login returns no data), re-run `npm run db:setup` from `backend/`. Editing `prisma/schema.prisma` requires re-running `npx prisma db push` (and `npx prisma generate`) — the dev server's hot reload does not apply schema changes.
- Core API smoke flow: `POST /api/v1/auth/login` → `GET /api/v1/discover` → `POST /api/v1/discover/like` (reciprocal like creates a match) → `GET|POST /api/v1/matches/:id/messages`.
