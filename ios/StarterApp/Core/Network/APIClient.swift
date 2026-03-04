import Foundation

enum APIError: Error, LocalizedError {
    case networkError(Error)
    case serverError(Int, String?)
    case decodingError(Error)
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .networkError(let e): return e.localizedDescription
        case .serverError(let code, let msg): return msg ?? "Server error (\(code))"
        case .decodingError: return "Failed to parse response"
        case .unauthorized: return "Session expired. Please sign in again."
        }
    }
}

actor APIClient {
    static let shared = APIClient()

    private let baseURL: String = {
        Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String
            ?? "http://localhost:3000/api/v1"
    }()

    private func accessToken() -> String? {
        KeychainHelper.shared.getAccessToken()
    }

    func request<T: Decodable>(
        _ endpoint: String,
        method: String = "GET",
        body: (any Encodable)? = nil
    ) async throws -> T {
        guard let url = URL(string: "\(baseURL)/\(endpoint)") else {
            throw APIError.networkError(URLError(.badURL))
        }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let token = accessToken() {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            req.httpBody = try JSONEncoder().encode(body)
        }
        let (data, response) = try await URLSession.shared.data(for: req)
        guard let http = response as? HTTPURLResponse else {
            throw APIError.networkError(URLError(.badServerResponse))
        }
        if http.statusCode == 401 { throw APIError.unauthorized }
        guard (200..<300).contains(http.statusCode) else {
            let msg = try? JSONDecoder().decode([String: String].self, from: data)
            throw APIError.serverError(http.statusCode, msg?["message"])
        }
        do {
            return try JSONDecoder().decode(T.self, from: data)
        } catch {
            throw APIError.decodingError(error)
        }
    }
}
