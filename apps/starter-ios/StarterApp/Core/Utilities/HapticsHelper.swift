import UIKit

/// Thin wrapper around UIKit feedback generators.
/// All methods are safe to call from any thread — UIKit generators are internally
/// dispatched to the main thread when required.
enum HapticsHelper {
    /// Triggers an impact feedback (physical press sensation).
    /// - Parameter style: `.light`, `.medium`, `.heavy`, `.soft`, or `.rigid`.
    static func impact(_ style: UIImpactFeedbackGenerator.FeedbackStyle = .medium) {
        let generator = UIImpactFeedbackGenerator(style: style)
        generator.prepare()
        generator.impactOccurred()
    }

    /// Triggers a notification feedback (success / warning / error).
    static func notification(_ type: UINotificationFeedbackGenerator.FeedbackType) {
        let generator = UINotificationFeedbackGenerator()
        generator.prepare()
        generator.notificationOccurred(type)
    }

    /// Triggers a selection-change feedback (light tick, useful for toggles and pickers).
    static func selection() {
        let generator = UISelectionFeedbackGenerator()
        generator.prepare()
        generator.selectionChanged()
    }
}
