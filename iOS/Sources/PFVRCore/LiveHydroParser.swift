import Foundation
import CoreFoundation

public enum LiveHydroParser {
    public static func parse(_ data: Data, arrayName: String, station: HydroStation? = nil) throws -> [HydroObservation] {
        guard let root = try JSONSerialization.jsonObject(with: data) as? [String: Any], root["errors"] == nil,
              let body = root["data"] as? [String: Any], let water = body["water"] as? [String: Any],
              let observations = water["observations"] as? [String: Any], let rows = observations[arrayName] as? [[String: Any]] else { throw PFVRDataError.invalidPayload("BAFU: ungültige Messreihe") }
        var result: [String: HydroObservation] = [:]
        for row in rows {
            guard let parameter = row["parameterName"] as? String, ["Q","W","WT"].contains(parameter),
                  let timestamp = row["timestamp"] as? String, let date = PFVRDate.parseISO(timestamp),
                  let number = row["value"] as? NSNumber, CFGetTypeID(number) != CFBooleanGetTypeID(), number.doubleValue.isFinite else { continue }
            if let station, let stationNo = row["stationNo"] as? String, stationNo != station.rawValue { continue }
            let point = HydroObservation(time: date, value: number.doubleValue, parameter: parameter)
            result[point.id] = point
        }
        guard !result.isEmpty else { throw PFVRDataError.invalidPayload("BAFU: keine gültigen Messwerte") }
        return result.values.sorted { $0.time == $1.time ? $0.parameter < $1.parameter : $0.time < $1.time }
    }
}
