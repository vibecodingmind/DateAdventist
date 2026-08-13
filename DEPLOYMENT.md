# AdventHearts deployment

The Android, web, and iOS apps talk to the Node API. Deploy the API first, then point every client at that HTTPS origin. Full operator steps: [GO_LIVE.md](GO_LIVE.md).

## Launch checklist

1. Host the API with a strong `JWT_SECRET` / `JWT_REFRESH_SECRET` (`openssl rand -hex 32`). Production **refuses to start** on weak or missing secrets.
2. Set `PUBLIC_BASE_URL` to the public HTTPS origin (photo URLs, email links, Stripe return URLs, legal pages).
3. Leave `ALLOW_DEMO_SEED` unset/false in production. Empty databases do **not** auto-create `password123` demo users.
4. For live payments, set `STRIPE_SECRET_KEY` and `STRIPE_WEBHOOK_SECRET`, then point Stripe webhooks at `POST /api/v1/subscriptions/webhook`.
5. For live email (verification + password reset), set `EMAIL_API_KEY` (Resend) and `EMAIL_FROM`. Production with a real email key requires verified email before login.
6. Set `CORS_ORIGIN` to the web app origin if it is hosted on a different host than the API.
7. Build Android with `API_BASE_URL=https://<your-host>/api/v1/`.
8. Point web (`VITE_API_BASE_URL`) and iOS (`API_ORIGIN` in Info.plist) at the same API host.
9. Confirm `GET https://<your-host>/health` returns `{ "success": true, "status": "HEALTHY" }`.
10. Confirm `GET https://<your-host>/legal/terms` and `/legal/privacy` load.

SQLite is enough to go live on a **single instance**. Photo files are stored on local disk (`uploads/`). Use a persistent volume, or photos will disappear if the container is replaced. Postgres and object storage can be added later.

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

Demo logins after **local** seed only:

- Member: `john.adventist@gmail.com` / `password123`
- Admin: `admin@adventhearts.com` / `AdminPass2026!`

Do not run `npm run seed` against a production database.

## 2. Docker

You must export secrets first (`JWT_SECRET`, `JWT_REFRESH_SECRET`, `PUBLIC_BASE_URL`). See [GO_LIVE.md](GO_LIVE.md).

```bash
docker compose up --build
```

The API listens on `0.0.0.0:5000`. Demo accounts are **not** seeded. Uploaded photos are stored in the `adventhearts-uploads` volume. The web client is served on port **8080**.

## 3. Cloud host (Cloud Run, Railway, Fly, Render)

Build from `backend/Dockerfile`. Set:

| Variable | Required for live | Example |
| :--- | :--- | :--- |
| `PORT` | yes | provided by the host |
| `HOST` | yes | `0.0.0.0` |
| `JWT_SECRET` | yes | `openssl rand -hex 32` |
| `JWT_REFRESH_SECRET` | yes | `openssl rand -hex 32` |
| `DATABASE_URL` | yes | `file:./data/adventhearts.db` |
| `NODE_ENV` | yes | `production` |
| `PUBLIC_BASE_URL` | yes | `https://api.example.com` |
| `CORS_ORIGIN` | web on another host | `https://app.example.com` |
| `ALLOW_DEMO_SEED` | no | `false` |
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

- `docker compose up --build` and use http://localhost:8080 (nginx proxies `/api` and `/legal` to the Node service).
- Or host `web/dist` on Vercel/Cloudflare and set `VITE_API_BASE_URL=https://<api-host>`.

Demo login buttons appear only in `npm run dev`, not in production builds.

## 5. iOS

On your Mac, open `ios/AdventHearts.xcodeproj`. Debug uses `http://127.0.0.1:5000`. For TestFlight/App Store, set Info.plist `API_ORIGIN` to `https://<api-host>` and archive with your Apple Developer team. Details in [ios/README.md](ios/README.md).

Backend tests (run locally before you deploy):

```bash
cd backend
npm test
```
