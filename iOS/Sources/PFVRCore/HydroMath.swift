import Foundation

public enum HydroMath {
    public struct AxisScale: Equatable, Sendable { public let min: Double; public let max: Double; public let step: Double }
    public struct Stats: Equatable, Sendable {
        public let count: Int
        public let first: Double
        public let last: Double
        public let min: Double
        public let max: Double
        public let mean: Double
        public var change: Double { last - first }
    }
    public static func niceAxis(_ values: [Double?]) -> AxisScale {
        let finite = values.compactMap { $0 }.filter(\.isFinite)
        guard var lower = finite.min(), var upper = finite.max() else { return AxisScale(min: 0, max: 1, step: 1) }
        if upper <= lower {
            let magnitude = abs(lower)
            let half = max(magnitude * 0.02, magnitude >= 100 ? 10 : (magnitude >= 10 ? 1 : 0.1))
            lower -= half
            upper += half
        }
        let raw = (upper - lower) / 4
        guard raw.isFinite, raw > 0 else { return AxisScale(min: 0, max: 1, step: 1) }
        let base = pow(10, floor(log10(raw)))
        let fraction = raw / base
        let nice = fraction <= 1 ? 1.0 : (fraction <= 2 ? 2.0 : (fraction <= 5 ? 5.0 : 10.0))
        let step = nice * base
        guard step.isFinite, step > 0 else { return AxisScale(min: 0, max: 1, step: 1) }
        var minValue = floor(lower / step + 1e-9) * step
        var maxValue = ceil(upper / step - 1e-9) * step
        if maxValue <= minValue { minValue -= step; maxValue += step }
        return AxisScale(min: abs(minValue) < 1e-12 ? 0 : minValue, max: abs(maxValue) < 1e-12 ? 0 : maxValue, step: step)
    }
    public static func stats(_ values: [Double?]) -> Stats? {
        let finite = values.compactMap { $0 }.filter(\.isFinite)
        guard let first = finite.first, let last = finite.last, let min = finite.min(), let max = finite.max() else { return nil }
        // Dividing first avoids overflow when several valid values approach Double's largest magnitude.
        let mean = finite.reduce(0) { $0 + $1 / Double(finite.count) }
        return Stats(count: finite.count, first: first, last: last, min: min, max: max, mean: mean)
    }
    /// Input timestamps must be ascending. An exact tie selects the earlier timestamp, as on Android.
    public static func nearestIndex(times: [Date], target: Date) -> Int? {
        guard !times.isEmpty else { return nil }
        var lower = 0
        var upper = times.count - 1
        while lower < upper {
            let middle = (lower + upper) / 2
            if times[middle] < target { lower = middle + 1 } else { upper = middle }
        }
        if lower == 0 { return 0 }
        return abs(times[lower - 1].timeIntervalSince(target)) <= abs(times[lower].timeIntervalSince(target)) ? lower - 1 : lower
    }
    public static func periodSeconds(_ code: String) -> TimeInterval {
        if code == "1h" { return 3600 }
        if code == "7d" { return 7 * 86400 }
        return 86400
    }
}
