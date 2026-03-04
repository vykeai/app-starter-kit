import SwiftUI

enum AppTokens {
    enum Color {
        static let primary = SwiftUI.Color(hex: "#E8570E")
        static let primaryDark = SwiftUI.Color(hex: "#F07020")
        static let background = SwiftUI.Color(hex: "#0A0A0A")
        static let surface = SwiftUI.Color(hex: "#1A1A1A")
        static let surfaceElevated = SwiftUI.Color(hex: "#242424")
        static let textPrimary = SwiftUI.Color.white
        static let textSecondary = SwiftUI.Color(white: 0.6)
        static let error = SwiftUI.Color(hex: "#E53935")
        static let success = SwiftUI.Color(hex: "#43A047")
        static let warning = SwiftUI.Color(hex: "#F57C00")
    }

    enum Spacing {
        static let xs: CGFloat = 4
        static let sm: CGFloat = 8
        static let md: CGFloat = 16
        static let lg: CGFloat = 24
        static let xl: CGFloat = 32
        static let xxl: CGFloat = 48
    }

    enum Radius {
        static let sm: CGFloat = 8
        static let md: CGFloat = 12
        static let lg: CGFloat = 16
        static let full: CGFloat = 9999
    }
}

extension SwiftUI.Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: Double
        switch hex.count {
        case 6:
            r = Double((int >> 16) & 0xFF) / 255
            g = Double((int >> 8) & 0xFF) / 255
            b = Double(int & 0xFF) / 255
        default:
            r = 1; g = 1; b = 1
        }
        self.init(red: r, green: g, blue: b)
    }
}
