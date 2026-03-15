import Foundation

struct PrepareMediaUploadBody: Encodable {
    let kind: String
    let mimeType: String
    let fileName: String?
    let sizeBytes: Int?
    let visibility: String?
}

struct CompleteMediaUploadBody: Encodable {
    let assetId: String
    let width: Int?
    let height: Int?
}

struct MediaUploadPreparation: Codable, Equatable {
    let assetId: String
    let storageKey: String
    let uploadUrl: String
    let publicUrl: String?
    let headers: [String: String]
    let expiresInSeconds: Int
}

struct MediaAsset: Codable, Equatable {
    let id: String
    let ownerId: String
    let kind: String
    let status: String
    let storageKey: String
    let publicUrl: String?
    let mimeType: String
    let fileName: String?
    let sizeBytes: Int?
    let width: Int?
    let height: Int?
    let visibility: String
    let createdAt: String
}
