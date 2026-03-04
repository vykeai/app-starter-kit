import Foundation
import Security

final class KeychainHelper {
    static let shared = KeychainHelper()
    private init() {}

    private let service = "com.starter.app"

    func save(_ value: String, for key: String) {
        let data = Data(value.utf8)
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: key,
        ]
        SecItemDelete(query as CFDictionary)
        let attributes: [CFString: Any] = query.merging([kSecValueData: data]) { $1 }
        SecItemAdd(attributes as CFDictionary, nil)
    }

    func get(_ key: String) -> String? {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: key,
            kSecReturnData: true,
            kSecMatchLimit: kSecMatchLimitOne,
        ]
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func delete(_ key: String) {
        let query: [CFString: Any] = [
            kSecClass: kSecClassGenericPassword,
            kSecAttrService: service,
            kSecAttrAccount: key,
        ]
        SecItemDelete(query as CFDictionary)
    }

    func clearAll() {
        delete("accessToken")
        delete("refreshToken")
    }

    func saveAccessToken(_ token: String) { save(token, for: "accessToken") }
    func getAccessToken() -> String? { get("accessToken") }
    func saveRefreshToken(_ token: String) { save(token, for: "refreshToken") }
    func getRefreshToken() -> String? { get("refreshToken") }
}
