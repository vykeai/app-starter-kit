import Foundation

struct AppUser: Codable {
    let id: String
    let email: String
    let displayName: String?

    init(id: String, email: String, displayName: String?) {
        self.id = id
        self.email = email
        self.displayName = displayName
    }

    init(profile: UserProfile) {
        self.init(id: profile.id, email: profile.email, displayName: profile.displayName)
    }
}
