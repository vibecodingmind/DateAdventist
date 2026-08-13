import SwiftUI

struct ContentView: View {
    @EnvironmentObject var session: SessionStore

    var body: some View {
        if session.user == nil {
            WelcomeView()
        } else {
            TabView {
                DiscoverView()
                    .tabItem { Label("Discover", systemImage: "rectangle.stack") }
                LikesView()
                    .tabItem { Label("Likes", systemImage: "heart") }
                MatchesView()
                    .tabItem { Label("Matches", systemImage: "person.2") }
                ProfileView()
                    .tabItem { Label("Profile", systemImage: "person.crop.circle") }
            }
            .tint(Color(red: 1, green: 0.2, blue: 0.4))
        }
    }
}
