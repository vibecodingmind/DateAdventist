# AdventHearts deployment

The Android app talks to the Node API at `API_BASE_URL`. Deploy the API first, then point the app at that HTTPS URL.

## Launch checklist

1. Host the API with a strong `JWT_SECRET` / `JWT_REFRESH_SECRET`.
2. Set `PUBLIC_BASE_URL` to the public HTTPS origin (used for photo URLs, email links, and Stripe return URLs).
3. For live payments, set `STRIPE_SECRET_KEY` and `STRIPE_WEBHOOK_SECRET`, then point Stripe webhooks at `POST /api/v1/subscriptions/webhook`.
4. For live email (verification + password reset), set `EMAIL_API_KEY` (Resend) and `EMAIL_FROM`.
5. Build the Android app with `API_BASE_URL=https://<your-host>/api/v1/`.
6. Point web (`VITE_API_BASE_URL`) and iOS (`AppConfig.origin`) at the same API host.
7. Confirm `GET https://<your-host>/health` returns `{ "success": true, "status": "HEALTHY" }`.

SQLite is enough to go live on a single instance. Photo files are stored on local disk (`uploads/`). Use a persistent volume, or photos will disappear if the container is replaced. Postgres and object storage can be added later.

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

The container listens on `0.0.0.0:5000`, creates the SQLite file if needed, and seeds demo accounts when the database is empty. Uploaded photos are stored in the `adventhearts-uploads` volume. The web client is served on port **8080**.

## 3. Cloud host (Cloud Run, Railway, Fly, Render)

Build from `backend/Dockerfile`. Set:

| Variable | Required for live | Example |
| :--- | :--- | :--- |
| `PORT` | yes | provided by the host |
| `HOST` | yes | `0.0.0.0` |
| `JWT_SECRET` | yes | long random string |
| `JWT_REFRESH_SECRET` | yes | long random string |
| `DATABASE_URL` | yes | `file:./data/adventhearts.db` |
| `NODE_ENV` | yes | `production` |
| `PUBLIC_BASE_URL` | yes | `https://api.example.com` |
| `UPLOAD_DIR` | no | `uploads` |
| `STRIPE_SECRET_KEY` | payments | `sk_live_...` |
| `STRIPE_WEBHOOK_SECRET` | payments | `whsec_...` |
| `EMAIL_API_KEY` | email | Resend API key |
| `EMAIL_FROM` | email | `AdventHearts <no-reply@example.com>` |

Without Stripe keys, checkout returns `503` in production (dev still uses a local confirm-payment path). Without `EMAIL_API_KEY`, verification and reset tokens are logged to the server console.

Then set Android `.env`:

```
API_BASE_URL=https://<your-host>/api/v1/
```

## 4. Web

Local: `cd web && npm install && npm run dev` (http://localhost:5173).

Production options:

- `docker compose up --build` and use http://localhost:8080 (nginx proxies `/api` to the Node service).
- Or host `web/dist` on Vercel/Cloudflare and set `VITE_API_BASE_URL=https://<api-host>`.

## 5. iOS

On your Mac, open `ios/AdventHearts.xcodeproj`. For TestFlight/App Store, set `AppConfig.origin` to `https://<api-host>` and archive with your Apple Developer team. Details in [ios/README.md](ios/README.md).

Backend tests (run locally before you deploy):

```bash
cd backend
npm test
```
