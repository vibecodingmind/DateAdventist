# AdventHearts

Faith-first Seventh-day Adventist dating: **one API**, three clients.

```
                 Node API  (backend/)   /api/v1 + Socket.IO
           ┌───────────────┼───────────────┐
           ▼               ▼               ▼
      Android           Web app           iOS
      app/              web/              ios/
      Kotlin/Compose    React/Vite        SwiftUI
```

| Layer | Location | Role |
| :--- | :--- | :--- |
| API | `backend/` | JWT auth, discovery, likes, matches, chat, photos, Stripe, safety, admin |
| Android | `app/` | Compose client (Room cache if the API is offline) |
| Web | `web/` | React client at http://localhost:5173 |
| iOS | `ios/` | SwiftUI Xcode project for your Mac |

## 1. Start the API

```bash
cd backend
cp .env.example .env
npm install
npx prisma generate
npx prisma db push
npm run seed
npm run dev
```

Health: `GET http://localhost:5000/health`

| Role | Email | Password |
| :--- | :--- | :--- |
| Member | `john.adventist@gmail.com` | `password123` |
| Admin | `admin@adventhearts.com` | `AdminPass2026!` |

```bash
cd backend && npm test
```

## 2. Web

```bash
cd web
npm install
npm run dev
```

http://localhost:5173 — Vite proxies `/api` to the Node server.

## 3. Android

Set `API_BASE_URL` in `.env` (see `.env.example`).

- Emulator: `http://10.0.2.2:5000/api/v1/`
- Physical device: `http://<your-lan-ip>:5000/api/v1/`

Open the repo in Android Studio and run `app`.

## 4. iOS (Mac + Xcode)

See [ios/README.md](ios/README.md). Open `ios/AdventHearts.xcodeproj`, pick your signing team, run on a simulator. The simulator uses `http://127.0.0.1:5000`.

## Docker (API + web)

```bash
docker compose up --build
```

- API: http://localhost:5000
- Web: http://localhost:8080

## Deploy

See [DEPLOYMENT.md](DEPLOYMENT.md). Point every client at the same HTTPS API:

- Android `.env`: `API_BASE_URL=https://<api-host>/api/v1/`
- Web: `VITE_API_BASE_URL=https://<api-host>` (or leave empty if nginx proxies `/api`)
- iOS: `AppConfig.origin` in `ios/AdventHearts/API/Config.swift`
