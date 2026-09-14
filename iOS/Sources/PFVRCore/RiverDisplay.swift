import Foundation

public enum RiverDisplay {
    public static func hasVerifiedGaugeCentimetres(_ station: HydroStation) -> Bool { station == .baselRheinhalle }
    public static func gaugeCentimetres(station: HydroStation, metresAboveSea: Double) -> Double? {
        guard station == .baselRheinhalle, metresAboveSea.isFinite else { return nil }
        return (metresAboveSea - 240) * 100
    }
    public static func graphLevelValue(station: HydroStation, metresAboveSea: Double, centimetres: Bool) -> Double {
        centimetres ? gaugeCentimetres(station: station, metresAboveSea: metresAboveSea) ?? metresAboveSea : metresAboveSea
    }
    public static func graphLevelUnit(station: HydroStation, centimetres: Bool) -> String {
        centimetres && hasVerifiedGaugeCentimetres(station) ? "cm" : "m ü.M."
    }
    public static func graphLevelDecimals(station: HydroStation, centimetres: Bool) -> Int {
        centimetres && hasVerifiedGaugeCentimetres(station) ? 0 : 2
    }
}
