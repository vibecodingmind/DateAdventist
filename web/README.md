# AdventHearts web

React (Vite) client for the AdventHearts API. Same accounts, matches, and chat as Android and iOS.

## Local

Start the API on port 5000, then:

```bash
cd web
npm install
npm run dev
```

Open http://localhost:5173. Vite proxies `/api`, `/uploads`, and `/socket.io` to `http://localhost:5000`.

Demo: Member (Joshua) `john.adventist@gmail.com` / `password123`.

## Production

Set `VITE_API_BASE_URL` to the public API origin if the web app is hosted on a different domain:

```
VITE_API_BASE_URL=https://api.example.com
```

If you use `docker compose up --build`, nginx serves the web app on port 8080 and proxies API calls to the Node service, so `VITE_API_BASE_URL` can stay empty.
