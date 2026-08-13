# Go live checklist

This repo can be launch-hardened in code. **Going live still requires you** to host HTTPS, set secrets, and publish the apps. AdventHearts will not become a public product from this repository alone.

## What the code now enforces

- Production will **not start** without strong `JWT_SECRET` / `JWT_REFRESH_SECRET` (32+ characters, not a `change_me` placeholder) and `PUBLIC_BASE_URL`.
- Production will **not seed demo accounts** (`john.adventist@gmail.com` / `password123`) unless you set `ALLOW_DEMO_SEED=true` (staging only).
- Register responses **do not** include email verification tokens.
- When `EMAIL_API_KEY` is set in production, login requires a verified email.
- CORS in production is limited to `CORS_ORIGIN` (comma-separated). Same-origin nginx still works.
- Demo login buttons are **debug/dev only** on Android, web, and iOS.
- Release Android and iOS **block cleartext HTTP**. Point them at HTTPS.
- Terms and privacy are served at `/legal/terms` and `/legal/privacy`.

SQLite + local `uploads/` is acceptable for a **single-instance** first launch if you attach persistent volumes. Postgres and object storage can come later.

## 1. Merge the stacked pull requests

Land in order onto `main`:

1. Android + API live features
2. Web + iOS clients
3. This go-live hardening branch

Do not ship a production database that was created by `npm run seed`.

## 2. Host the API on HTTPS

Pick a host (Railway, Fly, Render, Cloud Run, a VPS). Build from `backend/Dockerfile`. Set:

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
export JWT_REFRESH_SECRET="$(openssl rand -hex 32)"
export PUBLIC_BASE_URL="https://api.yourdomain.com"
export CORS_ORIGIN="https://app.yourdomain.com"
export NODE_ENV=production
export HOST=0.0.0.0
export DATABASE_URL="file:./data/adventhearts.db"
export UPLOAD_DIR=uploads
export ALLOW_DEMO_SEED=false
```

Confirm `GET https://api.yourdomain.com/health` returns `{ "success": true, "status": "HEALTHY" }`.

Keep `adventhearts-data` and `adventhearts-uploads` volumes (or equivalent disks). Replacing the container without a volume **wipes photos and the SQLite file**.

Local Docker (not public):

```bash
export JWT_SECRET="$(openssl rand -hex 32)"
export JWT_REFRESH_SECRET="$(openssl rand -hex 32)"
export PUBLIC_BASE_URL="http://localhost:5000"
export ALLOW_HTTP=true
docker compose up --build
```

## 3. Email and payments

| Need | Variable | Action |
| :--- | :--- | :--- |
| Verification + password reset | `EMAIL_API_KEY`, `EMAIL_FROM` | Create a Resend (or compatible) key and a sending domain |
| Paid plans | `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` | Stripe Dashboard → webhook `POST https://<api>/api/v1/subscriptions/webhook` |

Without Stripe keys, checkout returns 503 in production. Without email keys, tokens are logged on the server — do not do that in public production.

## 4. Point every client at the same API

- **Android** `.env` (release): `API_BASE_URL=https://<api-host>/api/v1/`
- **Web**: `VITE_API_BASE_URL=https://<api-host>` or leave empty when nginx on port 8080 proxies `/api`
- **iOS** Info.plist `API_ORIGIN`: `https://<api-host>` (debug builds still use `http://127.0.0.1:5000`)

Create a real admin after first start (do not seed production). Register a member, then promote that row in the database, or add a one-off admin from a private staging seed.

## 5. Store listings (you must do this)

Code cannot create Google Play or App Store accounts.

- Google Play Console + upload keystore (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`)
- Apple Developer Program, signing team, TestFlight, then App Store
- Bundle / application id is `com.adventhearts.app` — change it if that id is taken
- Store privacy questionnaire should match `/legal/privacy` (no selling data, 18+, Stripe, account deletion)

## 6. Smoke test before you announce

1. Create a new account (not a demo user).
2. Verify email if `EMAIL_API_KEY` is set.
3. Upload a photo, like another real tester, chat.
4. Open Terms and Privacy from Settings on Android, web, and iOS.
5. Delete the test account from Settings.
6. Confirm demo buttons are absent on release/production builds.
