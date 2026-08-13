# AdventHearts iOS (SwiftUI)

Native iPhone/iPad client for the same Node API used by Android and web.

## Open on your Mac

1. Start the API on your Mac (or keep it running in Docker):

```bash
cd backend
cp .env.example .env
npm install
npx prisma generate && npx prisma db push && npm run seed
npm run dev
```

2. Open `ios/AdventHearts.xcodeproj` in Xcode 15+.
3. Select the **AdventHearts** scheme and an iPhone simulator.
4. In Signing & Capabilities, choose your Apple ID / team (required even for simulator on some Xcode versions; required for a physical device).
5. Press Run.

The simulator talks to `http://127.0.0.1:5000` (debug `AppConfig.origin`). Release builds read `API_ORIGIN` from `Info.plist`.

## Physical iPhone

1. Put your Mac and iPhone on the same Wi-Fi.
2. Change `AppConfig.origin` to your Mac’s LAN IP, for example `http://192.168.1.12:5000`.
3. Plug in the device, pick your Development Team, Run.

HTTP to a local IP is allowed by `NSAllowsLocalNetworking` in `Info.plist`. Release builds do **not** allow arbitrary HTTP. Set `API_ORIGIN` to `https://api.yourdomain.com` before TestFlight / App Store.

## App Store

- Bundle ID: `com.adventhearts.app` (change it if that ID is taken).
- Apple Developer Program ($99/year).
- Archive in Xcode → Distribute to App Store Connect.
- Camera / photo library usage strings are already in `Info.plist`.

Debug builds include demo login buttons (`john.adventist@gmail.com` / `password123` after a local seed). Release builds do not.
