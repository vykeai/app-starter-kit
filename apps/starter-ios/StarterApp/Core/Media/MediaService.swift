import Foundation

struct MediaService {
    func prepareUpload(
        kind: String,
        mimeType: String,
        fileName: String? = nil,
        sizeBytes: Int? = nil,
        visibility: String? = nil
    ) async throws -> MediaUploadPreparation {
        try await APIClient.shared.request(
            "media/uploads/prepare",
            method: "POST",
            body: PrepareMediaUploadBody(
                kind: kind,
                mimeType: mimeType,
                fileName: fileName,
                sizeBytes: sizeBytes,
                visibility: visibility
            )
        )
    }

    func completeUpload(
        assetId: String,
        width: Int? = nil,
        height: Int? = nil
    ) async throws -> MediaAsset {
        try await APIClient.shared.request(
            "media/uploads/complete",
            method: "POST",
            body: CompleteMediaUploadBody(assetId: assetId, width: width, height: height)
        )
    }

    func listAssets() async throws -> [MediaAsset] {
        try await APIClient.shared.request("media/assets")
    }

    func deleteAsset(assetId: String) async throws -> MessageResponse {
        try await APIClient.shared.request("media/assets/\(assetId)", method: "DELETE")
    }
}
