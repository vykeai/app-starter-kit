import Foundation

struct PendingAuthLink: Equatable {
    let email: String?
    let code: String?
    let linkToken: String?

    var hasAuthPayload: Bool {
        !(email?.isEmpty ?? true) || !(code?.isEmpty ?? true) || !(linkToken?.isEmpty ?? true)
    }
}

enum PendingAppRoute: Equatable {
    case auth(PendingAuthLink)
    case home
}

@Observable
class AppState {
    var isAuthenticated: Bool = false
    var currentUser: AppUser? = nil
    var syncEngine: SyncEngine?
    var pendingRoute: PendingAppRoute? = nil
    let forceUpdateChecker = ForceUpdateChecker()
    let networkMonitor = NetworkMonitor()

    /// Guards against double-tap or concurrent logout calls.
    private(set) var isLoggingOut: Bool = false

    init() {
        configureRuntimeNetworking()
    }

    var pendingAuthLink: PendingAuthLink? {
        get {
            guard case .auth(let link) = pendingRoute else {
                return nil
            }
            return link
        }
        set {
            pendingRoute = newValue.map(PendingAppRoute.auth)
        }
    }

    @MainActor
    func logout() {
        guard !isLoggingOut else { return }
        isLoggingOut = true
        KeychainHelper.shared.clearAll()
        isAuthenticated = false
        currentUser = nil
        pendingRoute = nil
        isLoggingOut = false
    }

    private func configureRuntimeNetworking() {
        let processInfo = ProcessInfo.processInfo
        let arguments = processInfo.arguments
        let environment = processInfo.environment
        let overrideBaseURL = environment["APP_API_BASE_URL"]
        let shouldUseFixtureTransport =
            arguments.contains("-UITestMode") ||
            arguments.contains("-RuntimeFixtureMode") ||
            environment["APP_RUNTIME_FIXTURE_MODE"] == "1"

        guard shouldUseFixtureTransport else {
            APIClient.configureShared(baseURL: overrideBaseURL)
            return
        }

        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [RuntimeMockURLProtocol.self]
        let session = URLSession(configuration: configuration)

        RuntimeMockURLProtocol.fixtures = [:]
        RuntimeMockURLProtocol.requestHandler = { request in
            guard let url = request.url else {
                throw URLError(.badURL)
            }

            let path = url.path
            let method = request.httpMethod?.uppercased() ?? "GET"
            let requestId = request.value(forHTTPHeaderField: "X-Request-Id")
                ?? "fixture-\(UUID().uuidString)"

            let statusCode: Int
            let body: Data

            switch (method, path) {
            case ("POST", let route) where route.hasSuffix("/auth/magic-link/request"):
                statusCode = 201
                body = """
                {
                  "message": "Code sent",
                  "deliveryMode": "disabled",
                  "email": "test@example.com",
                  "code": "12345678",
                  "linkToken": "fixture-link-token",
                  "linkUrl": "appstarterkit://auth/verify?email=test%40example.com&code=12345678&linkToken=fixture-link-token"
                }
                """.data(using: .utf8) ?? Data()
            case ("POST", let route) where route.hasSuffix("/auth/magic-link/verify"),
                 ("POST", let route) where route.hasSuffix("/auth/social"):
                statusCode = 201
                body = """
                {
                  "accessToken": "fixture-access-token",
                  "refreshToken": "fixture-refresh-token",
                  "user": {
                    "id": "fixture-user",
                    "email": "test@example.com",
                    "displayName": "Starter Test User"
                  }
                }
                """.data(using: .utf8) ?? Data()
            case ("POST", let route) where route.hasSuffix("/auth/refresh"):
                statusCode = 201
                body = """
                {
                  "accessToken": "fixture-access-token",
                  "refreshToken": "fixture-refresh-token"
                }
                """.data(using: .utf8) ?? Data()
            case ("DELETE", let route) where route.hasSuffix("/auth/session"):
                statusCode = 200
                body = #"{"message":"Signed out"}"#.data(using: .utf8) ?? Data()
            case ("GET", let route) where route.hasSuffix("/user/me"),
                 ("PATCH", let route) where route.hasSuffix("/user/me"):
                statusCode = 200
                body = """
                {
                  "id": "fixture-user",
                  "email": "test@example.com",
                  "displayName": "Starter Test User",
                  "preferences": {
                    "theme": "moss",
                    "pushMarketingEnabled": false,
                    "pushActivityEnabled": true,
                    "pushTransactionalEnabled": true,
                    "pushSystemEnabled": true,
                    "emailNotificationsEnabled": true
                  }
                }
                """.data(using: .utf8) ?? Data()
            case ("GET", let route) where route.hasSuffix("/notifications/preferences"),
                 ("PATCH", let route) where route.hasSuffix("/notifications/preferences"):
                statusCode = 200
                body = """
                {
                  "theme": "moss",
                  "pushMarketingEnabled": false,
                  "pushActivityEnabled": true,
                  "pushTransactionalEnabled": true,
                  "pushSystemEnabled": true,
                  "emailNotificationsEnabled": true,
                  "pushEnabled": true,
                  "emailEnabled": true,
                  "enabledCategories": ["activity", "transactional"],
                  "quietHoursEnabled": true,
                  "quietHoursStart": "22:00",
                  "quietHoursEnd": "07:00",
                  "urgentBreaksQuietHours": true,
                  "batchSoonNotifications": false,
                  "updatedAt": "2026-03-15T00:00:00.000Z"
                }
                """.data(using: .utf8) ?? Data()
            case ("GET", let route) where route.hasSuffix("/billing/entitlements"):
                statusCode = 200
                body = """
                {
                  "tier": "tracker",
                  "source": "ios",
                  "features": ["auth-core", "notifications-core", "billing-core"],
                  "renewsAt": "2026-04-14T12:00:00Z"
                }
                """.data(using: .utf8) ?? Data()
            case ("GET", let route) where route.hasSuffix("/health"):
                statusCode = 200
                body = #"{"status":"ok","timestamp":"2026-03-15T00:00:00.000Z"}"#.data(using: .utf8) ?? Data()
            default:
                statusCode = 404
                body = #"{"message":"No fixture mapped for runtime mock transport"}"#.data(using: .utf8) ?? Data()
            }

            let response = HTTPURLResponse(
                url: url,
                statusCode: statusCode,
                httpVersion: nil,
                headerFields: [
                    "Content-Type": "application/json",
                    "X-Request-Id": requestId
                ]
            )!
            return (response, body)
        }

        APIClient.configureShared(
            baseURL: overrideBaseURL ?? "https://fixture.invalid/api/v1",
            session: session
        )
    }
}

private final class RuntimeMockURLProtocol: URLProtocol {
    static var fixtures: [String: (data: Data, statusCode: Int)] = [:]
    static var requestHandler: ((URLRequest) throws -> (HTTPURLResponse, Data))?

    override class func canInit(with request: URLRequest) -> Bool { true }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    override func startLoading() {
        guard let url = request.url else {
            client?.urlProtocol(self, didFailWithError: URLError(.badURL))
            return
        }

        if let handler = Self.requestHandler {
            do {
                let (response, data) = try handler(request)
                client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
                client?.urlProtocol(self, didLoad: data)
            } catch {
                client?.urlProtocol(self, didFailWithError: error)
            }
        } else if let fixture = Self.fixtures.first(where: { url.absoluteString.contains($0.key) })?.value {
            let response = HTTPURLResponse(
                url: url,
                statusCode: fixture.statusCode,
                httpVersion: nil,
                headerFields: nil
            )!
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: fixture.data)
        } else {
            let response = HTTPURLResponse(url: url, statusCode: 404, httpVersion: nil, headerFields: nil)!
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: Data())
        }

        client?.urlProtocolDidFinishLoading(self)
    }

    override func stopLoading() {}
}
