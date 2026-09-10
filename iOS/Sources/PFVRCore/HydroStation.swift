import Foundation

public enum HydroStation: String, Codable, CaseIterable, Identifiable, Sendable {
    case baselRheinhalle = "2289"
    case rheinfelden = "2091"
    public var id: String { rawValue }
    public var label: String { self == .baselRheinhalle ? "Basel, Rheinhalle" : "Rheinfelden" }
    public var supportsTemperature: Bool { self == .rheinfelden }
    public var stationURL: URL { URL(string: "https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/\(rawValue)")! }
}
