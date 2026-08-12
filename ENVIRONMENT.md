# AdventHearts - Environment Variables Reference

| Variable Name | Required | Default / Format | Description |
| :--- | :--- | :--- | :--- |
| `NODE_ENV` | Yes | `production` / `development` | Runtime environment mode |
| `PORT` | No | `5000` | HTTP Server port |
| `DATABASE_URL` | Yes | `postgresql://...` | PostgreSQL Connection String |
| `JWT_SECRET` | Yes | 32+ char random string | Secret for access token signing |
| `JWT_REFRESH_SECRET`| Yes | 32+ char random string | Secret for refresh token signing |
| `STORAGE_BUCKET` | Yes | `adventhearts-media` | S3 Object storage bucket |
| `STRIPE_SECRET_KEY` | Yes | `sk_live_...` | Stripe secret key for payment processing |
| `STRIPE_WEBHOOK_SECRET`| Yes | `whsec_...` | Stripe webhook signature secret |
| `REDIS_URL` | Yes | `redis://localhost:6379` | Redis instance for pub/sub & rate limiting |
