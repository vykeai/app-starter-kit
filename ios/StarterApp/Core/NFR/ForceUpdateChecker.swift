import Foundation

@Observable
class ForceUpdateChecker {
    var isHardUpdateRequired: Bool = false
    var isSoftUpdateAvailable: Bool = false
    var isSoftBannerDismissed: Bool = false
    var latestVersion: String = ""

    func checkForUpdate() async {
        let currentVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0.0"
        do {
            let response: VersionCheckResponse = try await APIClient.shared.request(
                "app/version-check?platform=ios&version=\(currentVersion)"
            )
            isHardUpdateRequired = response.isUpdateRequired
            isSoftUpdateAvailable = response.isUpdateRecommended
            latestVersion = response.latestVersion
        } catch {
            // Silent failure — never block the user due to a version check failure
        }
    }
}

struct VersionCheckResponse: Decodable {
    let isUpdateRequired: Bool
    let isUpdateRecommended: Bool
    let minimumVersion: String
    let latestVersion: String
}
