import Foundation

/// Ports Android's documented thresholds; this is an orientation derived from BAFU raw data.
public enum RhineNavigation {
    public enum Stage: String, CaseIterable, Codable, Sendable {
        case unknown, normal, hwmI, hwmIIb, hwmIIa
        public var shortLabel: String {
            switch self {
            case .unknown: return "Lage unklar"
            case .normal: return "Normal"
            case .hwmI: return "HWM I"
            case .hwmIIb: return "Sperre IIb"
            case .hwmIIa: return "Sperre IIa"
            }
        }
        public var detail: String {
            switch self {
            case .unknown: return "Basel-Pegel fehlt oder ist älter als 60 Minuten. Massgebend sind die Schweizerischen Rheinhäfen."
            case .normal: return "Unter Hochwassermarke I (< 700 cm)."
            case .hwmI: return "Voralarm ab 700 cm Pegel Basel-Rheinhalle."
            case .hwmIIb: return "Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt."
            case .hwmIIa: return "Schifffahrt Rheinfelden–Kembs gesperrt."
            }
        }
    }
    public static let maximumCurrentAge: TimeInterval = 3600
    public static let officialThresholdStages: [Stage] = [.hwmI, .hwmIIb, .hwmIIa]

    public static func stage(gaugeCentimetres: Double?) -> Stage {
        guard let gauge = gaugeCentimetres, gauge.isFinite else { return .unknown }
        // Conversion from 248.20 m yields 819.9999999999989 cm in binary floating point.
        // A 1e-9 cm arithmetic tolerance preserves the exact source threshold without visible rounding.
        let comparable = gauge + 1e-9
        if comparable >= 820 { return .hwmIIa }
        if comparable >= 790 { return .hwmIIb }
        if comparable >= 700 { return .hwmI }
        return .normal
    }
    public static func isCurrent(measurement: Date?, cacheUpdated: Date?, now: Date = Date()) -> Bool {
        guard let measurement, let cacheUpdated,
              measurement.timeIntervalSince1970 > 0, cacheUpdated.timeIntervalSince1970 > 0,
              now.timeIntervalSince1970 > 0 else { return false }
        let measurementAge = now.timeIntervalSince(measurement)
        let cacheAge = now.timeIntervalSince(cacheUpdated)
        return measurementAge.isFinite && cacheAge.isFinite
            && (0...maximumCurrentAge).contains(measurementAge)
            && (0...maximumCurrentAge).contains(cacheAge)
    }
    public static func currentStage(gaugeCentimetres: Double?, measurement: Date?, cacheUpdated: Date?, now: Date = Date()) -> Stage {
        isCurrent(measurement: measurement, cacheUpdated: cacheUpdated, now: now) ? stage(gaugeCentimetres: gaugeCentimetres) : .unknown
    }
    public static func thresholdGraphValue(stage: Stage, centimetres: Bool) -> Double? {
        let cm: Double
        switch stage {
        case .hwmI: cm = 700
        case .hwmIIb: cm = 790
        case .hwmIIa: cm = 820
        default: return nil
        }
        return centimetres ? cm : 240 + cm / 100
    }
}
