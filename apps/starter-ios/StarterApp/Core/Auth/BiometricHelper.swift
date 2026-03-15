import LocalAuthentication

/// An actor-isolated helper that wraps `LAContext` for biometric authentication.
/// Using an actor ensures the `LAContext` is never accessed from multiple threads
/// simultaneously, which is a requirement of the LocalAuthentication framework.
actor BiometricHelper {
    static let shared = BiometricHelper()

    private init() {}

    // MARK: - Availability

    /// Returns `true` if the device can evaluate Face ID, Touch ID, or optic ID.
    /// Falls back to `false` silently if biometrics are not enrolled or not available.
    var isAvailable: Bool {
        let context = LAContext()
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }

    /// Returns the biometric type supported by the device (`.faceID`, `.touchID`,
    /// `.opticID`, or `.none`). Calls `canEvaluatePolicy` internally to populate
    /// the `biometryType` property on `LAContext`.
    var biometricType: LABiometryType {
        let context = LAContext()
        var error: NSError?
        // canEvaluatePolicy must be called before biometryType is meaningful.
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else {
            return .none
        }
        return context.biometryType
    }

    // MARK: - Authentication

    /// Presents the biometric prompt and returns `true` on success.
    /// - Parameter reason: The localised string shown to the user in the prompt.
    /// - Throws: An `LAError` if the user cancels, fails authentication, or if
    ///   biometrics are unavailable. Callers can inspect `LAError.Code` to
    ///   distinguish cancellation from failure.
    @discardableResult
    func authenticate(reason: String = "Confirm your identity") async throws -> Bool {
        let context = LAContext()
        context.localizedCancelTitle = "Use Passcode"

        return try await withCheckedThrowingContinuation { continuation in
            context.evaluatePolicy(
                .deviceOwnerAuthenticationWithBiometrics,
                localizedReason: reason
            ) { success, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: success)
                }
            }
        }
    }
}
