import SwiftUI

struct DiscoverView: View {
    @State private var profiles: [Profile] = []
    @State private var index = 0
    @State private var error: String?
    @State private var matchName: String?

    var current: Profile? {
        profiles.indices.contains(index) ? profiles[index] : nil
    }

    var body: some View {
        VStack {
            HStack {
                Text("Discover").font(.title2.bold())
                Spacer()
            }
            if let error { Text(error).foregroundStyle(.red) }
            if let matchName {
                Text("It's a match with \(matchName)!")
                    .padding()
                    .background(Color.green.opacity(0.2))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            if let current {
                AsyncImage(url: URL(string: current.primaryPhoto)) { image in
                    image.resizable().scaledToFill()
                } placeholder: {
                    Color.gray.opacity(0.3)
                }
                .frame(height: 420)
                .clipShape(RoundedRectangle(cornerRadius: 24))
                .overlay(alignment: .bottomLeading) {
                    VStack(alignment: .leading) {
                        Text("\(current.fullName), \(current.age)").font(.title.bold())
                        Text("\(current.occupation) · \(current.city)").font(.subheadline)
                        Text(current.bio).font(.footnote).lineLimit(3)
                    }
                    .padding()
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(LinearGradient(colors: [.clear, .black.opacity(0.85)], startPoint: .top, endPoint: .bottom))
                }
                .clipped()
                HStack(spacing: 20) {
                    Button("✕") { Task { await act("pass") } }
                        .font(.title)
                        .frame(width: 64, height: 64)
                        .background(Color(red: 0.09, green: 0.09, blue: 0.13))
                        .clipShape(Circle())
                    Button("♥") { Task { await act("like") } }
                        .font(.title)
                        .frame(width: 72, height: 72)
                        .background(Color(red: 1, green: 0.2, blue: 0.4))
                        .clipShape(Circle())
                }
            } else {
                Text("No more profiles right now.").foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding()
        .task { await load() }
    }

    func load() async {
        do {
            profiles = try await APIClient.shared.request("discover")
            index = 0
        } catch {
            self.error = error.localizedDescription
        }
    }

    func act(_ kind: String) async {
        guard let current else { return }
        do {
            if kind == "pass" {
                struct Body: Encodable { let toUserId: String }
                let _: MessageAck = try await APIClient.shared.request("discover/pass", method: "POST", body: Body(toUserId: current.userId))
            } else {
                struct Body: Encodable { let toUserId: String; let isSuperLike: Bool }
                let result: LikeResult = try await APIClient.shared.request("discover/like", method: "POST", body: Body(toUserId: current.userId, isSuperLike: false))
                if result.isMatch { matchName = current.fullName }
            }
            index += 1
        } catch {
            self.error = error.localizedDescription
        }
    }
}
