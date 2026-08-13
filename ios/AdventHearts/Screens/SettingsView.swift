import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var session: SessionStore
    @State private var error: String?

    var body: some View {
        VStack(spacing: 16) {
            Text("Settings").font(.title2.bold())
            if let error { Text(error).foregroundStyle(.red) }
            Text("Delete is permanent and removes your AdventHearts profile from the server.")
                .font(.footnote)
                .foregroundStyle(.secondary)
            Button("Log out") { session.logout() }
            Button("Delete account", role: .destructive) {
                Task {
                    do {
                        let _: MessageAck = try await APIClient.shared.request("auth/delete-account", method: "POST", body: Empty())
                        session.logout()
                    } catch {
                        self.error = error.localizedDescription
                    }
                }
            }
            Spacer()
        }
        .padding()
    }
}

private struct Empty: Encodable {}
