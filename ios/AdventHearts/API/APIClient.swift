import Foundation

enum APIClientError: LocalizedError {
    case http(String)
    case decoding
    case missingData

    var errorDescription: String? {
        switch self {
        case .http(let message): return message
        case .decoding: return "Could not read the server response."
        case .missingData: return "The server returned an empty response."
        }
    }
}

final class APIClient {
    static let shared = APIClient()
    var token: String?

    private let decoder: JSONDecoder = {
        let d = JSONDecoder()
        return d
    }()

    private init() {}

    func request<T: Decodable>(
        _ path: String,
        method: String = "GET",
        body: Encodable? = nil,
        token: String? = nil
    ) async throws -> T {
        guard let url = URL(string: "\(AppConfig.origin.absoluteString)/api/v1/\(path)") else {
            throw APIClientError.http("Invalid URL")
        }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Accept")
        if let token = token ?? self.token {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            req.setValue("application/json", forHTTPHeaderField: "Content-Type")
            req.httpBody = try JSONEncoder().encode(AnyEncodable(body))
        }
        let (data, response) = try await URLSession.shared.data(for: req)
        let env = try decoder.decode(Envelope<T>.self, from: data)
        if let http = response as? HTTPURLResponse, http.statusCode >= 400 || !env.success {
            throw APIClientError.http(env.error?.message ?? "Request failed (\( (response as? HTTPURLResponse)?.statusCode ?? 0 ))")
        }
        guard let payload = env.data else { throw APIClientError.missingData }
        return payload
    }

    func uploadPhoto(file: Data, filename: String = "photo.jpg", kind: String = "profile") async throws -> UploadResult {
        let boundary = "Boundary-\(UUID().uuidString)"
        var req = URLRequest(url: URL(string: "\(AppConfig.origin.absoluteString)/api/v1/profile/photo?kind=\(kind)")!)
        req.httpMethod = "POST"
        req.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        if let token { req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }

        var body = Data()
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"photo\"; filename=\"\(filename)\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        body.append(file)
        body.append("\r\n--\(boundary)--\r\n".data(using: .utf8)!)
        req.httpBody = body

        let (data, _) = try await URLSession.shared.data(for: req)
        let env = try decoder.decode(Envelope<UploadResult>.self, from: data)
        guard env.success, let payload = env.data else {
            throw APIClientError.http(env.error?.message ?? "Upload failed")
        }
        return payload
    }
}

private struct AnyEncodable: Encodable {
    private let encodeClosure: (Encoder) throws -> Void
    init(_ value: Encodable) {
        encodeClosure = { encoder in try value.encode(to: encoder) }
    }
    func encode(to encoder: Encoder) throws { try encodeClosure(encoder) }
}
