import Foundation
import Combine

@MainActor
final class SessionStore: ObservableObject {
    @Published var user: AuthUser?
    @Published var errorMessage: String?

    private let userKey = "ah.user"

    init() {
        if let data = UserDefaults.standard.data(forKey: userKey),
           let saved = try? JSONDecoder().decode(AuthUser.self, from: data) {
            user = saved
            APIClient.shared.token = saved.accessToken
        }
    }

    func persist(_ user: AuthUser) {
        self.user = user
        APIClient.shared.token = user.accessToken
        if let data = try? JSONEncoder().encode(user) {
            UserDefaults.standard.set(data, forKey: userKey)
        }
    }

    func logout() {
        user = nil
        APIClient.shared.token = nil
        UserDefaults.standard.removeObject(forKey: userKey)
    }

    func login(email: String, password: String) async {
        do {
            struct Body: Encodable { let email: String; let password: String }
            let user: AuthUser = try await APIClient.shared.request("auth/login", method: "POST", body: Body(email: email, password: password))
            persist(user)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func register(fullName: String, email: String, password: String, age: Int, gender: String, city: String) async {
        do {
            struct Body: Encodable {
                let fullName: String
                let email: String
                let password: String
                let age: Int
                let gender: String
                let city: String
                let country: String
                let relationshipIntention: String
            }
            let user: AuthUser = try await APIClient.shared.request(
                "auth/register",
                method: "POST",
                body: Body(
                    fullName: fullName,
                    email: email,
                    password: password,
                    age: age,
                    gender: gender,
                    city: city,
                    country: "United States",
                    relationshipIntention: "Marriage"
                )
            )
            persist(user)
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func demoMember() async {
        await login(email: "john.adventist@gmail.com", password: "password123")
    }

    func demoAdmin() async {
        do {
            struct Body: Encodable { let email: String; let password: String }
            let user: AuthUser = try await APIClient.shared.request(
                "admin/login",
                method: "POST",
                body: Body(email: "admin@adventhearts.com", password: "AdminPass2026!")
            )
            persist(user)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
