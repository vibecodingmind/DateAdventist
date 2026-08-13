import SwiftUI

struct WelcomeView: View {
    @EnvironmentObject var session: SessionStore
    @State private var mode = "home"
    @State private var email = ""
    @State private var password = ""
    @State private var fullName = ""
    @State private var age = 25
    @State private var gender = "Female"
    @State private var city = "Silver Spring"
    @State private var busy = false

    var body: some View {
        VStack(spacing: 16) {
            Spacer()
            Text("♥")
                .font(.system(size: 52))
                .foregroundStyle(Color(red: 1, green: 0.2, blue: 0.4))
            Text("AdventHearts").font(.largeTitle.bold())
            Text("Faith. Connection. Purpose.")
                .foregroundStyle(Color(red: 0.92, green: 0.7, blue: 0.03))
            Text("Faith-first Seventh-day Adventist dating for intentional, marriage-oriented relationships.")
                .font(.footnote)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal)

            if let error = session.errorMessage {
                Text(error).foregroundStyle(.red).font(.footnote)
            }

            if mode == "home" {
                Button("Create AdventHearts Account") { mode = "register" }
                    .buttonStyle(RoseButton())
                Button("I Already Have an Account") { mode = "login" }
                    .buttonStyle(GhostButton())
                HStack {
                    Button("Member (Joshua)") { Task { busy = true; await session.demoMember(); busy = false } }
                    Button("Admin") { Task { busy = true; await session.demoAdmin(); busy = false } }
                }
                .font(.footnote)
            } else if mode == "login" {
                TextField("Email", text: $email).textInputAutocapitalization(.never)
                SecureField("Password", text: $password)
                Button("Sign In") {
                    Task { busy = true; await session.login(email: email, password: password); busy = false }
                }
                .buttonStyle(RoseButton())
                .disabled(busy)
                Button("Back") { mode = "home" }
            } else {
                TextField("Full name", text: $fullName)
                TextField("Email", text: $email).textInputAutocapitalization(.never)
                SecureField("Password", text: $password)
                Stepper("Age \(age)", value: $age, in: 18...80)
                Picker("Gender", selection: $gender) { Text("Female").tag("Female"); Text("Male").tag("Male") }
                TextField("City", text: $city)
                Button("Create account") {
                    Task {
                        busy = true
                        await session.register(fullName: fullName, email: email, password: password, age: age, gender: gender, city: city)
                        busy = false
                    }
                }
                .buttonStyle(RoseButton())
                Button("Back") { mode = "home" }
            }
            Spacer()
        }
        .padding()
        .background(Color(red: 0.04, green: 0.04, blue: 0.05).ignoresSafeArea())
        .foregroundStyle(.white)
    }
}

struct RoseButton: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color(red: 1, green: 0.2, blue: 0.4))
            .foregroundStyle(.white)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}

struct GhostButton: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .frame(maxWidth: .infinity)
            .padding()
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.white.opacity(0.2)))
            .opacity(configuration.isPressed ? 0.8 : 1)
    }
}
