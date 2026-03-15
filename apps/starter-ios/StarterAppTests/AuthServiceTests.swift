import XCTest

// Tests for AuthService using HTTP-level interception via MockURLProtocol.
// No MockAPIClient — the real APIClient and JSON decoding path is exercised.
// Fixture JSON matches actual backend response shapes.

final class AuthServiceTests: XCTestCase {

    override func setUp() {
        super.setUp()
        URLProtocol.registerClass(MockURLProtocol.self)
        MockURLProtocol.fixtures = [:]
        MockURLProtocol.requestHandler = nil
    }

    override func tearDown() {
        URLProtocol.unregisterClass(MockURLProtocol.self)
        MockURLProtocol.fixtures = [:]
        MockURLProtocol.requestHandler = nil
        super.tearDown()
    }

    // MARK: - requestMagicLink

    func testRequestMagicLinkSuccess() async throws {
        MockURLProtocol.fixtures["auth/magic-link/request"] = (
            data: Data(#"{"message":"Code sent"}"#.utf8),
            statusCode: 201
        )
        let service = AuthService()
        try await service.requestMagicLink(email: "user@example.com")
        // No throw = success
    }

    func testRequestMagicLinkNetworkError() async {
        MockURLProtocol.requestHandler = { _ in
            throw URLError(.notConnectedToInternet)
        }
        let service = AuthService()
        do {
            try await service.requestMagicLink(email: "user@example.com")
            XCTFail("Expected error not thrown")
        } catch {
            // Expected — any error is acceptable here
        }
    }

    // MARK: - verifyMagicLink

    func testVerifyMagicLinkSuccess() async throws {
        let json = """
        {
          "accessToken": "eyJhbGciOiJIUzI1NiJ9.test.sig",
          "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
          "user": {
            "id": "abc123",
            "email": "user@example.com",
            "displayName": null
          }
        }
        """
        MockURLProtocol.fixtures["auth/magic-link/verify"] = (
            data: Data(json.utf8),
            statusCode: 200
        )
        let service = AuthService()
        let result = try await service.verifyMagicLink(
            email: "user@example.com",
            code: "12345678"
        )
        XCTAssertEqual(result.user.email, "user@example.com")
        XCTAssertEqual(result.user.id, "abc123")
        XCTAssertFalse(result.accessToken.isEmpty)
        XCTAssertFalse(result.refreshToken.isEmpty)
    }

    func testVerifyMagicLinkInvalidCode() async {
        MockURLProtocol.fixtures["auth/magic-link/verify"] = (
            data: Data(#"{"message":"Invalid email or code","statusCode":401}"#.utf8),
            statusCode: 401
        )
        let service = AuthService()
        do {
            _ = try await service.verifyMagicLink(
                email: "user@example.com",
                code: "00000000"
            )
            XCTFail("Expected APIError.unauthorized not thrown")
        } catch APIError.unauthorized {
            // Expected
        } catch {
            XCTFail("Wrong error type: \(error)")
        }
    }

    func testVerifyMagicLinkMalformedResponse() async {
        MockURLProtocol.fixtures["auth/magic-link/verify"] = (
            data: Data("not json".utf8),
            statusCode: 200
        )
        let service = AuthService()
        do {
            _ = try await service.verifyMagicLink(
                email: "user@example.com",
                code: "12345678"
            )
            XCTFail("Expected decode error not thrown")
        } catch APIError.decodingError {
            // Expected
        } catch {
            XCTFail("Wrong error type: \(error)")
        }
    }
}
