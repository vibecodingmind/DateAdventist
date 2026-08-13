import SwiftUI

struct ChatView: View {
    let match: Match
    @EnvironmentObject var session: SessionStore
    @State private var messages: [ChatMessage] = []
    @State private var text = ""
    @State private var error: String?

    var body: some View {
        VStack {
            if let error { Text(error).foregroundStyle(.red).font(.footnote) }
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 8) {
                    ForEach(messages) { msg in
                        HStack {
                            if msg.senderId == session.user?.userId { Spacer() }
                            Text(msg.text)
                                .padding(10)
                                .background(msg.senderId == session.user?.userId ? Color(red: 1, green: 0.2, blue: 0.4) : Color(red: 0.09, green: 0.09, blue: 0.13))
                                .clipShape(RoundedRectangle(cornerRadius: 16))
                            if msg.senderId != session.user?.userId { Spacer() }
                        }
                    }
                }
                .padding()
            }
            HStack {
                TextField("Type a respectful message…", text: $text)
                Button("Send") { Task { await send() } }
            }
            .padding()
        }
        .navigationTitle(match.otherProfile?.fullName ?? "Chat")
        .toolbar {
            Button("Unmatch", role: .destructive) {
                Task {
                    do {
                        let _: UnmatchResult = try await APIClient.shared.request("matches/\(match.matchId)", method: "DELETE")
                    } catch {
                        self.error = error.localizedDescription
                    }
                }
            }
        }
        .task { await load() }
    }

    func load() async {
        do {
            messages = try await APIClient.shared.request("matches/\(match.matchId)/messages")
        } catch {
            self.error = error.localizedDescription
        }
    }

    func send() async {
        let body = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !body.isEmpty else { return }
        text = ""
        do {
            struct Payload: Encodable { let text: String }
            let msg: ChatMessage = try await APIClient.shared.request("matches/\(match.matchId)/messages", method: "POST", body: Payload(text: body))
            messages.append(msg)
        } catch {
            self.error = error.localizedDescription
        }
    }
}
