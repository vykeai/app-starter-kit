import Foundation

enum APIError: Error, LocalizedError {
    case networkError(Error)
    case serverError(Int, String?, String?)
    case decodingError(Error)
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .networkError(let e): return e.localizedDescription
        case .serverError(let code, let msg, let correlationId):
            if let msg, let correlationId {
                return "\(msg) [\(correlationId)]"
            }
            return msg ?? "Server error (\(code))"
        case .decodingError: return "Failed to parse response"
        case .unauthorized: return "Session expired. Please sign in again."
        }
    }
}

private struct RefreshTokenBody: Encodable {
    let refreshToken: String
}

private struct RefreshTokenResponse: Decodable {
    let accessToken: String
    let refreshToken: String
}

private struct ErrorEnvelope: Decodable {
    struct Payload: Decodable {
        let code: String
        let message: String
        let correlationId: String?
    }

    let error: Payload
}

actor APIClient {
    static var shared = APIClient()

    private let baseURL: String
    private let session: URLSession

    // Tracks an in-flight refresh so concurrent 401s share one Task instead of
    // each triggering its own /auth/refresh call.
    private var refreshTask: Task<String, Error>?

    init(
        baseURL: String = Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String
            ?? "http://localhost:3000/api/v1",
        session: URLSession = .shared
    ) {
        self.baseURL = baseURL
        self.session = session
    }

    nonisolated static func configureShared(
        baseURL: String? = nil,
        session: URLSession? = nil
    ) {
        shared = APIClient(
            baseURL: baseURL ?? defaultBaseURL,
            session: session ?? .shared
        )
    }

    private nonisolated static var defaultBaseURL: String {
        Bundle.main.object(forInfoDictionaryKey: "API_BASE_URL") as? String
            ?? "http://localhost:3000/api/v1"
    }

    // MARK: - Public request method

    func request<T: Decodable>(
        _ endpoint: String,
        method: String = "GET",
        body: (any Encodable)? = nil
    ) async throws -> T {
        let token = KeychainHelper.shared.getAccessToken()
        let (data, http) = try await performRequest(
            endpoint: endpoint,
            method: method,
            body: body,
            accessToken: token
        )

        // Happy path.
        if (200..<300).contains(http.statusCode) {
            do {
                return try JSONDecoder().decode(T.self, from: data)
            } catch {
                throw APIError.decodingError(error)
            }
        }

        // On 401 from a normal (non-refresh) endpoint, attempt token refresh once.
        if http.statusCode == 401 {
            let freshToken = try await refreshAccessToken()
            // Replay the original request with the new token.
            let (retryData, retryHTTP) = try await performRequest(
                endpoint: endpoint,
                method: method,
                body: body,
                accessToken: freshToken
            )
            if retryHTTP.statusCode == 401 {
                throw APIError.unauthorized
            }
            guard (200..<300).contains(retryHTTP.statusCode) else {
                let payload = decodeErrorEnvelope(from: retryData)
                throw APIError.serverError(retryHTTP.statusCode, payload?.message, payload?.correlationId)
            }
            do {
                return try JSONDecoder().decode(T.self, from: retryData)
            } catch {
                throw APIError.decodingError(error)
            }
        }

        let payload = decodeErrorEnvelope(from: data)
        throw APIError.serverError(http.statusCode, payload?.message, payload?.correlationId)
    }

    // MARK: - Internal helpers

    /// Low-level URLSession call — shared by the public `request` and the refresh path.
    /// Does NOT retry on 401 to avoid infinite loops.
    private func performRequest(
        endpoint: String,
        method: String,
        body: (any Encodable)?,
        accessToken: String?
    ) async throws -> (Data, HTTPURLResponse) {
        guard let url = URL(string: "\(baseURL)/\(endpoint)") else {
            throw APIError.networkError(URLError(.badURL))
        }
        var req = URLRequest(url: url)
        req.httpMethod = method
        req.setValue("application/json", forHTTPHeaderField: "Content-Type")
        req.setValue("ios-\(UUID().uuidString)", forHTTPHeaderField: "X-Request-Id")
        req.setValue("ios", forHTTPHeaderField: "X-Client-Platform")
        req.setValue(
            Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0",
            forHTTPHeaderField: "X-Client-Version"
        )
        if let token = accessToken {
            req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        if let body {
            req.httpBody = try JSONEncoder().encode(body)
        }
        let (data, response) = try await session.data(for: req)
        guard let http = response as? HTTPURLResponse else {
            throw APIError.networkError(URLError(.badServerResponse))
        }
        return (data, http)
    }

    /// Performs (or coalesces) a single token-refresh call.
    /// If a refresh is already in-flight, all callers await the same Task result.
    private func refreshAccessToken() async throws -> String {
        // Return the existing in-flight task if one is already running.
        if let existing = refreshTask {
            return try await existing.value
        }

        guard let storedRefreshToken = KeychainHelper.shared.getRefreshToken() else {
            throw APIError.unauthorized
        }

        let task = Task<String, Error> {
            defer { refreshTask = nil }

            let (data, http) = try await performRequest(
                endpoint: "auth/refresh",
                method: "POST",
                body: RefreshTokenBody(refreshToken: storedRefreshToken),
                accessToken: nil
            )

            if http.statusCode == 401 {
                throw APIError.unauthorized
            }
            guard (200..<300).contains(http.statusCode) else {
                let payload = decodeErrorEnvelope(from: data)
                throw APIError.serverError(http.statusCode, payload?.message, payload?.correlationId)
            }

            let decoded = try JSONDecoder().decode(RefreshTokenResponse.self, from: data)
            KeychainHelper.shared.saveAccessToken(decoded.accessToken)
            KeychainHelper.shared.saveRefreshToken(decoded.refreshToken)
            return decoded.accessToken
        }

        refreshTask = task
        return try await task.value
    }

    private func decodeErrorEnvelope(from data: Data) -> ErrorEnvelope.Payload? {
        if let decoded = try? JSONDecoder().decode(ErrorEnvelope.self, from: data) {
            return decoded.error
        }
        if let messageMap = try? JSONDecoder().decode([String: String].self, from: data),
           let message = messageMap["message"] {
            return .init(code: "ERROR", message: message, correlationId: nil)
        }
        return nil
    }
}

// MARK: - SyncAPIClient

extension APIClient: SyncAPIClient {
    func syncPush(changes: [SyncChange], deviceId: String) async throws -> SyncPushResponse {
        // TODO: implement POST /sync/push
        throw APIError.serverError(501, "sync push not yet implemented", nil)
    }

    func syncPull(since: Date?, collections: [String]) async throws -> SyncPullResponse<SyncChange> {
        // TODO: implement GET /sync/pull
        throw APIError.serverError(501, "sync pull not yet implemented", nil)
    }
}
