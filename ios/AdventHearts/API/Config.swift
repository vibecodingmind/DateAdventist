import Foundation

enum AppConfig {
    /// Simulator talks to the API on your Mac. On a physical iPhone, change this to your Mac's LAN IP,
    /// e.g. http://192.168.1.12:5000
    static let origin = URL(string: "http://127.0.0.1:5000")!
}
