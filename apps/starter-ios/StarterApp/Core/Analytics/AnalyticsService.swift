import Foundation

struct AnalyticsEvent: Encodable {
    let name: String
    let category: String?
    let properties: [String: String]?
}

struct AnalyticsSummary: Codable, Equatable {
    struct TopEvent: Codable, Equatable {
        let name: String
        let count: Int
    }

    let totalEvents: Int
    let uniqueEvents: Int
    let topEvents: [TopEvent]
    let lastEventAt: String?
}

struct AnalyticsService {
    func track(name: String, category: String? = nil, properties: [String: String]? = nil) async throws {
        let _: TrackAcceptedResponse = try await APIClient.shared.request(
            "analytics/events",
            method: "POST",
            body: AnalyticsEvent(name: name, category: category, properties: properties)
        )
    }

    func trackAuthenticated(
        name: String,
        category: String? = nil,
        properties: [String: String]? = nil
    ) async throws {
        let _: TrackAcceptedResponse = try await APIClient.shared.request(
            "analytics/events/authenticated",
            method: "POST",
            body: AnalyticsEvent(name: name, category: category, properties: properties)
        )
    }

    func fetchSummary() async throws -> AnalyticsSummary {
        try await APIClient.shared.request("analytics/summary")
    }
}

private struct TrackAcceptedResponse: Decodable {
    let accepted: Bool
}
