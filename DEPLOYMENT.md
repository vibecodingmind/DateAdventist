# AdventHearts deployment

The Android app talks to the Node API at `API_BASE_URL`. Deploy the API first, then point the app at that HTTPS URL.

## 1. Run locally

```bash
cd backend
cp .env.example .env
npm install
npx prisma generate
npx prisma db push
npm run seed
npm run dev
```

Health check: `GET http://localhost:5000/health`

Demo logins after seed:

- Member: `john.adventist@gmail.com` / `password123`
- Admin: `admin@adventhearts.com` / `AdminPass2026!`

## 2. Docker

```bash
docker compose up --build
```

The container listens on `0.0.0.0:5000`, creates the SQLite file if needed, and seeds demo accounts when the database is empty.

## 3. Cloud host (Cloud Run, Railway, Fly, Render)

Build from `backend/Dockerfile`. Set:

| Variable | Example |
| :--- | :--- |
| `PORT` | provided by the host (Cloud Run sets this) |
| `HOST` | `0.0.0.0` |
| `JWT_SECRET` | long random string |
| `JWT_REFRESH_SECRET` | long random string |
| `DATABASE_URL` | `file:./data/adventhearts.db` or a Postgres URL later |
| `NODE_ENV` | `production` |

Confirm `GET https://<your-host>/health` returns `{ "success": true, "status": "HEALTHY" }`.

Then set Android `.env`:

```
API_BASE_URL=https://<your-host>/api/v1/
```

Backend tests (run locally before you deploy):

```bash
cd backend
npm test
```
