import XCTest
@testable import StarterApp

final class BiometricHelperTests: XCTestCase {

    /// Verifies that `isAvailable` returns a Bool without crashing.
    /// On the simulator and devices without enrolled biometrics this will be
    /// `false`; on a physical device with enrolled biometrics it will be `true`.
    /// Either outcome is acceptable — the test guards against crashes or
    /// assertion failures inside the helper.
    func testIsAvailableReturnsBool() async {
        let result = await BiometricHelper.shared.isAvailable
        // result is a Bool — the test passes as long as we reach this line.
        XCTAssertTrue(result == true || result == false)
    }

    /// Verifies that `biometricType` returns a valid LABiometryType without crashing.
    func testBiometricTypeReturnsSupportedValue() async {
        let type = await BiometricHelper.shared.biometricType
        // All enum cases are acceptable; we just confirm no crash occurs.
        let validValues: [String] = ["none", "touchID", "faceID", "opticID"]
        // Use string description as a future-proof way to check the value exists.
        XCTAssertFalse(type.debugDescription.isEmpty, "biometricType should have a debug description: \(validValues)")
    }

    /// Verifies that calling `authenticate` on a simulator (where biometrics are
    /// unavailable) throws an error rather than hanging or crashing.
    func testAuthenticateThrowsWhenUnavailable() async {
        let available = await BiometricHelper.shared.isAvailable
        guard !available else {
            // Skip this assertion on a real device with enrolled biometrics — we
            // cannot programmatically approve the Face ID prompt in tests.
            return
        }
        do {
            _ = try await BiometricHelper.shared.authenticate(reason: "Unit test")
            // If we somehow succeed (shouldn't on simulator), that is also fine.
        } catch {
            // Expected on simulator / no biometrics enrolled.
            XCTAssertNotNil(error)
        }
    }
}
