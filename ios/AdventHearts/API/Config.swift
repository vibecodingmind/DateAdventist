import Foundation

enum AppConfig {
    /// Debug builds talk to the API on your Mac. Release builds use `API_ORIGIN` from Info.plist
    /// (change that value to your live HTTPS host before App Store / TestFlight).
    static var origin: URL {
        #if DEBUG
        return URL(string: "http://127.0.0.1:5000")!
        #else
        let fromPlist = (Bundle.main.object(forInfoDictionaryKey: "API_ORIGIN") as? String)?
            .trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        if let url = URL(string: fromPlist), !fromPlist.isEmpty {
            return url
        }
        return URL(string: "https://api.adventhearts.com")!
        #endif
    }

    static var termsURL: URL {
        origin.appendingPathComponent("legal").appendingPathComponent("terms")
    }

    static var privacyURL: URL {
        origin.appendingPathComponent("legal").appendingPathComponent("privacy")
    }
}
