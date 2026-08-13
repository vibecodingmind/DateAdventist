import SwiftUI

struct LikesView: View {
    @State private var likes: [LikeReceived] = []
    @State private var sub: SubscriptionStatus?
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Group {
                if let error { Text(error).foregroundStyle(.red) }
                if likes.isEmpty {
                    Text("No incoming likes yet.").foregroundStyle(.secondary)
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            ForEach(likes) { like in
                                if let p = like.profile {
                                    VStack {
                                        AsyncImage(url: URL(string: p.primaryPhoto)) { img in
                                            img.resizable().scaledToFill()
                                        } placeholder: { Color.gray }
                                        .frame(height: 180)
                                        .clipped()
                                        .blur(radius: sub?.active == true ? 0 : 8)
                                        Text(sub?.active == true ? p.fullName : "Gold member")
                                            .font(.footnote.bold())
                                    }
                                    .clipShape(RoundedRectangle(cornerRadius: 16))
                                }
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Likes you")
            .toolbar {
                NavigationLink("Gold") { SubscriptionView() }
            }
            .task {
                do {
                    likes = try await APIClient.shared.request("likes")
                    sub = try await APIClient.shared.request("subscriptions/current")
                } catch {
                    self.error = error.localizedDescription
                }
            }
        }
    }
}
