# AdventHearts

Faith-first Seventh-day Adventist dating app: **Android (Kotlin/Compose)** client plus a **Node.js/Express** backend.

## What this repo contains

| Layer | Location | Role |
| :--- | :--- | :--- |
| Android app | `app/` | Compose UI for auth, discovery, likes, matches, chat, safety, subscriptions, and admin |
| Backend API | `backend/` | REST + Socket.IO API with Prisma persistence, JWT auth, matching, and RBAC |
| Local cache | Room in `app/src/main/java/com/example/data/local` | Offline demo data and cache of server responses |

The Android screens were previously backed only by a local Room database. They now call `/api/v1/*` and keep Room as an offline fallback.

## Run the backend

```bash
cd backend
cp .env.example .env
npm install
npx prisma generate
npx prisma db push
npm run seed
npm run dev
```

API: `http://localhost:5000`  
Health: `GET /health`

### Demo accounts (after seed)

| Role | Email | Password |
| :--- | :--- | :--- |
| Member | `john.adventist@gmail.com` | `password123` |
| Admin | `admin@adventhearts.com` | `AdminPass2026!` |
| Super admin | `superadmin@adventhearts.com` | `AdminPass2026!` |

### Tests

```bash
cd backend
npm test
```

## Android client

Point the app at the API with `API_BASE_URL` in `.env` (see `.env.example`).

- Android emulator: `http://10.0.2.2:5000/api/v1/`
- Physical device on the same network: `http://<your-lan-ip>:5000/api/v1/`

Open the project in Android Studio and run the `app` configuration.

If the API is unreachable, the app still runs against the local Room demo database (Joshua / Admin demo switcher on the welcome screen).

## Core API

- `POST /api/v1/auth/register` `POST /api/v1/auth/login` `GET /api/v1/auth/me`
- `GET /api/v1/discover` `POST /api/v1/discover/like` `POST /api/v1/discover/pass`
- `GET /api/v1/likes` `GET /api/v1/matches` `GET|POST /api/v1/matches/:id/messages`
- `POST /api/v1/safety/report` `POST /api/v1/safety/block`
- `GET /api/v1/subscriptions/plans` `POST /api/v1/subscriptions/checkout`
- `GET /api/v1/admin/dashboard` (moderator+) with server-side RBAC

Realtime events (Socket.IO, JWT in `auth.token`): `match:new`, `like:new`, `chat:message`.

## Docker

```bash
docker compose up --build
```

## Deploy

See [DEPLOYMENT.md](DEPLOYMENT.md). After the API is hosted, set `API_BASE_URL` to `https://<your-host>/api/v1/` in the Android `.env`.
