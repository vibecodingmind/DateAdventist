import SwiftUI

struct MatchesView: View {
    @State private var matches: [Match] = []
    @State private var error: String?

    var body: some View {
        NavigationStack {
            Group {
                if let error { Text(error).foregroundStyle(.red) }
                if matches.isEmpty {
                    Text("No matches yet. Like members in Discover.").foregroundStyle(.secondary).padding()
                } else {
                    ScrollView {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                            ForEach(matches) { match in
                                if let p = match.otherProfile {
                                    NavigationLink {
                                        ChatView(match: match)
                                    } label: {
                                        VStack {
                                            AsyncImage(url: URL(string: p.primaryPhoto)) { img in
                                                img.resizable().scaledToFill()
                                            } placeholder: { Color.gray }
                                            .frame(height: 180)
                                            .clipped()
                                            Text("\(p.fullName), \(p.age) · \(match.compatibilityScore)%")
                                                .font(.footnote.bold())
                                                .foregroundStyle(.white)
                                        }
                                        .clipShape(RoundedRectangle(cornerRadius: 16))
                                    }
                                }
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Matches")
            .task {
                do { matches = try await APIClient.shared.request("matches") }
                catch { self.error = error.localizedDescription }
            }
        }
    }
}
