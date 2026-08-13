import SwiftUI
import PhotosUI

struct ProfileView: View {
    @EnvironmentObject var session: SessionStore
    @State private var profile: Profile?
    @State private var bio = ""
    @State private var church = ""
    @State private var verse = ""
    @State private var info: String?
    @State private var error: String?
    @State private var picker: PhotosPickerItem?

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    if let error { Text(error).foregroundStyle(.red) }
                    if let info { Text(info).foregroundStyle(.green) }
                    if let profile {
                        AsyncImage(url: URL(string: profile.primaryPhoto)) { img in
                            img.resizable().scaledToFill()
                        } placeholder: { Circle().fill(.gray) }
                        .frame(width: 120, height: 120)
                        .clipShape(Circle())
                        Text("\(profile.fullName), \(profile.age)").font(.title2.bold())
                        Text("\(profile.occupation) · \(profile.city)").foregroundStyle(.secondary)
                        PhotosPicker("Change photo", selection: $picker, matching: .images)
                    }
                    TextField("Bio", text: $bio, axis: .vertical).lineLimit(4...8)
                    TextField("Local church", text: $church)
                    TextField("Favorite verse", text: $verse)
                    Button("Save profile") { Task { await save() } }
                        .buttonStyle(RoseButton())
                    NavigationLink("Subscription") { SubscriptionView() }
                    NavigationLink("Settings") { SettingsView() }
                    Button("Log out", role: .destructive) { session.logout() }
                }
                .padding()
            }
            .navigationTitle("Profile")
            .task { await load() }
            .onChange(of: picker) { _, item in
                Task {
                    guard let item, let data = try? await item.loadTransferable(type: Data.self) else { return }
                    do {
                        let result = try await APIClient.shared.uploadPhoto(file: data, kind: "profile")
                        if let p = result.profile { profile = p }
                        info = "Photo updated"
                    } catch {
                        self.error = error.localizedDescription
                    }
                }
            }
        }
    }

    func load() async {
        do {
            let p: Profile = try await APIClient.shared.request("profile")
            profile = p
            bio = p.bio
            church = p.localChurch
            verse = p.favoriteVerse
        } catch {
            self.error = error.localizedDescription
        }
    }

    func save() async {
        do {
            struct Body: Encodable { let bio: String }
            struct Faith: Encodable { let localChurch: String; let favoriteVerse: String }
            let _: Profile = try await APIClient.shared.request("profile", method: "PUT", body: Body(bio: bio))
            let _: Profile = try await APIClient.shared.request("profile/faith", method: "PUT", body: Faith(localChurch: church, favoriteVerse: verse))
            info = "Profile saved"
        } catch {
            self.error = error.localizedDescription
        }
    }
}
