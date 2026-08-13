import SwiftUI

struct SubscriptionView: View {
    @State private var sub: SubscriptionStatus?
    @State private var error: String?
    @State private var info: String?
    @State private var planId = "plan_gold_monthly"

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Membership").font(.title2.bold())
            if let error { Text(error).foregroundStyle(.red) }
            if let info { Text(info).foregroundStyle(.green) }
            Text("Current: \(sub?.tier ?? "FREE")")
            ForEach(["plan_plus_monthly", "plan_gold_monthly", "plan_platinum_monthly"], id: \.self) { id in
                Button(id.replacingOccurrences(of: "plan_", with: "").replacingOccurrences(of: "_monthly", with: "").capitalized) {
                    planId = id
                }
                .padding()
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(planId == id ? Color.yellow.opacity(0.3) : Color.white.opacity(0.06))
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
            Button("Checkout with Stripe") {
                Task {
                    do {
                        struct Body: Encodable { let planId: String; let paymentProvider: String; let billingCycle: String }
                        let result: CheckoutResult = try await APIClient.shared.request(
                            "subscriptions/checkout",
                            method: "POST",
                            body: Body(planId: planId, paymentProvider: "stripe", billingCycle: "MONTHLY")
                        )
                        if let url = URL(string: result.checkoutUrl) {
                            await MainActor.run { UIApplication.shared.open(url) }
                        }
                    } catch {
                        self.error = error.localizedDescription
                    }
                }
            }
            .buttonStyle(RoseButton())
            Button("I've finished paying — refresh") { Task { await load() } }
            Spacer()
        }
        .padding()
        .task { await load() }
    }

    func load() async {
        do { sub = try await APIClient.shared.request("subscriptions/current") }
        catch { self.error = error.localizedDescription }
    }
}
