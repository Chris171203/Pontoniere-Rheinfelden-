import SwiftUI
import Charts
import PFVRCore

struct RiverView: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PageScroll {
            RiverSummaryTile()
            Picker(state.ui("Zeitraum"), selection: $state.range) {
                ForEach(HydroRange.allCases) { range in Text(range.label).tag(range) }
            }.pickerStyle(.segmented).accessibilityIdentifier("river.range")
            ForEach(state.activeStations) { station in
                RiverChartCard(station: station)
                if station.supportsTemperature { RiverTemperatureCard(station: station) }
            }
            NavigationNotice()
        }.refreshable { await state.refresh(force: true) }.accessibilityIdentifier("screen.river")
    }
}

struct RiverSummaryTile: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PFVRCard {
            HStack {
                Text(state.ui("Rhein aktuell")).font(.headline)
                Spacer()
                Button { Task { await state.refresh(force: true) } } label: {
                    Image(systemName: "arrow.clockwise").rotationEffect(.degrees(state.loading ? 360 : 0))
                        .animation(state.loading ? .linear(duration: 1).repeatForever(autoreverses: false) : .default, value: state.loading)
                        .frame(width: 44, height: 44)
                }.accessibilityLabel(state.ui("Rhein und Wetter aktualisieren")).accessibilityIdentifier("river.refresh")
            }
            HStack(alignment: .top, spacing: 16) {
                ForEach(state.activeStations) { station in
                    RiverStationSummary(station: station).frame(maxWidth: .infinity, alignment: .leading)
                }
            }
            NavigationNotice(compact: true)
        }
    }
}

struct RiverStationSummary: View {
    @EnvironmentObject private var state: AppState
    let station: HydroStation
    private var dataset: HydroDataset? { state.rivers[station] }
    private var level: HydroObservation? { dataset?.latest(parameter: "W") }
    var body: some View {
        TimelineView(.periodic(from: state.now, by: 30)) { context in
            let stage = RhineNavigation.currentStage(gaugeCentimetres: level.flatMap { RiverDisplay.gaugeCentimetres(station: station, metresAboveSea: $0.value) }, measurement: level?.time, cacheUpdated: dataset?.live?.metadata.updatedAt, now: state.testing ? state.now : context.date)
            summary(stage: stage)
        }
    }
    private func summary(stage: RhineNavigation.Stage) -> some View {
        VStack(alignment: .leading, spacing: 9) {
            Text(station.label).font(.subheadline.bold()).lineLimit(2).frame(minHeight: 36, alignment: .top)
            Text(AppDates.number(level?.value, digits: 2)).font(.system(.title2, design: .rounded).weight(.bold))
                .foregroundStyle(station == .baselRheinhalle ? RiverColors.level(stage) : PFVRTheme.water)
            Text("m ü.M.").font(.caption).foregroundStyle(.secondary)
            if let value = level.flatMap({ RiverDisplay.gaugeCentimetres(station: station, metresAboveSea: $0.value) }) {
                Text(AppDates.number(value) + " cm").font(.caption)
            }
            Text(AppDates.number(dataset?.latest(parameter: "Q")?.value) + " m³/s").font(.subheadline)
            if station.supportsTemperature {
                Text(AppDates.number(dataset?.latest(parameter: "WT")?.value, digits: 1) + " °C").font(.subheadline)
            } else {
                Text(state.ui(stage.shortLabel)).font(.caption.weight(.semibold)).foregroundStyle(RiverColors.level(stage))
            }
            SourceStamp(source: "BAFU", updated: level?.time, stale: dataset?.live?.metadata.isStale ?? true)
        }
        .accessibilityElement(children: .combine)
        .accessibilityIdentifier("river.station.\(station.rawValue)")
    }
}

struct NavigationNotice: View {
    @EnvironmentObject private var state: AppState
    var compact = false
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(state.ui("BAFU-Aktuellwerte sind ungeprüfte Rohdaten. Massgebend für die Schifffahrt sind die Schweizerischen Rheinhäfen."))
            if !compact { Text(state.ui("Basel: 700 cm Voralarm · 790 cm Sperre Kleinschifffahrt/Fähren · 820 cm Sperre Rheinfelden–Kembs. Nach 60 Minuten ist die aktuelle Lage unklar.")) }
            Link(state.ui("Offizielle Schifffahrtslage"), destination: PublicLinks.riverNavigation)
        }.font(.caption2).foregroundStyle(.secondary)
    }
}

struct RiverChartCard: View {
    @EnvironmentObject private var state: AppState
    let station: HydroStation
    @State private var selectedTime: Date?
    private var centimetres: Bool { station == .baselRheinhalle && state.baselCentimetres }
    private var dataset: HydroDataset? { state.rivers[station] }
    private var levels: [HydroObservation] { dataset?.series(parameter: "W", range: state.range) ?? [] }
    private var flows: [HydroObservation] { dataset?.series(parameter: "Q", range: state.range) ?? [] }
    private var levelUnit: String { RiverDisplay.graphLevelUnit(station: station, centimetres: centimetres) }
    var body: some View {
        PFVRCard(station.label + " · " + state.range.label) {
            HStack {
                Label(state.ui("Abfluss") + " · m³/s", systemImage: "line.diagonal").foregroundStyle(RiverColors.flow(.normal))
                Spacer()
                Label(state.ui("Pegel") + " · " + levelUnit, systemImage: "line.diagonal").foregroundStyle(PFVRTheme.water)
            }.font(.caption2)
            if station == .baselRheinhalle {
                Picker(state.ui("Pegel-Einheit"), selection: $state.baselCentimetres) {
                    Text("m ü.M.").tag(false)
                    Text("cm").tag(true)
                }.pickerStyle(.segmented).accessibilityIdentifier("river.basel.unit")
            }
            if levels.isEmpty && flows.isEmpty {
                ContentUnavailableView(state.ui("Noch keine Messdaten"), systemImage: "water.waves")
                    .frame(height: 160)
            } else {
                DualRiverGraph(station: station, levels: levels, flows: flows, centimetres: centimetres, selection: $selectedTime)
                    .frame(height: 205)
                    .accessibilityLabel(station.label + " · " + state.ui("Abfluss und Pegel") + " · " + state.range.label)
                    .accessibilityIdentifier("river.graph.\(station.rawValue)")
                if let selectedTime {
                    let level = levels.min { abs($0.time.timeIntervalSince(selectedTime)) < abs($1.time.timeIntervalSince(selectedTime)) }
                    let flow = flows.min { abs($0.time.timeIntervalSince(selectedTime)) < abs($1.time.timeIntervalSince(selectedTime)) }
                    Text(AppDates.stamp(selectedTime) + " · " + AppDates.number(flow?.value) + " m³/s · " + AppDates.number(level.map { RiverDisplay.graphLevelValue(station: station, metresAboveSea: $0.value, centimetres: centimetres) }, digits: centimetres ? 0 : 2) + " " + levelUnit)
                        .font(.caption2).monospacedDigit()
                }
            }
            SourceStamp(source: "BAFU", updated: dataset?.live?.metadata.updatedAt, stale: dataset?.live?.metadata.isStale ?? true)
            if let dataset, !dataset.failures.isEmpty {
                Text(state.ui("Einzelne Messreihen konnten nicht aktualisiert werden.")).font(.caption2).foregroundStyle(.secondary)
            }
        }
    }
}

/// Both values are drawn against independent, explicitly labelled scales. No mixed unit axis.
struct DualRiverGraph: View {
    let station: HydroStation
    let levels: [HydroObservation]
    let flows: [HydroObservation]
    let centimetres: Bool
    @Binding var selection: Date?
    private var times: [Date] { (levels.map(\.time) + flows.map(\.time)).sorted() }
    private func displayed(_ value: Double) -> Double { RiverDisplay.graphLevelValue(station: station, metresAboveSea: value, centimetres: centimetres) }
    var body: some View {
        GeometryReader { geometry in
            let plot = CGRect(x: 42, y: 12, width: max(1, geometry.size.width - 94), height: max(1, geometry.size.height - 44))
            let minTime = times.first ?? Date()
            let maxTime = times.last ?? minTime.addingTimeInterval(3600)
            let seconds = max(1, maxTime.timeIntervalSince(minTime))
            let flowScale = HydroMath.niceAxis(flows.map { Optional($0.value) })
            let levelScale = HydroMath.niceAxis(levels.map { Optional(displayed($0.value)) })
            Canvas { context, _ in
                func x(_ date: Date) -> CGFloat { plot.minX + CGFloat(date.timeIntervalSince(minTime) / seconds) * plot.width }
                func y(_ value: Double, minimum: Double, maximum: Double) -> CGFloat {
                    plot.maxY - CGFloat((value - minimum) / max(0.000001, maximum - minimum)) * plot.height
                }
                func label(_ text: String, at point: CGPoint, anchor: UnitPoint = .center) {
                    context.draw(Text(text).font(.system(size: 9)).foregroundColor(.secondary), at: point, anchor: anchor)
                }
                for index in 0...4 {
                    let fraction = Double(index) / 4
                    let py = plot.maxY - CGFloat(fraction) * plot.height
                    var line = Path(); line.move(to: CGPoint(x: plot.minX, y: py)); line.addLine(to: CGPoint(x: plot.maxX, y: py))
                    context.stroke(line, with: .color(.secondary.opacity(0.13)), lineWidth: 1)
                    label(AppDates.number(flowScale.min + fraction * (flowScale.max - flowScale.min)), at: CGPoint(x: plot.minX - 5, y: py), anchor: .trailing)
                    label(AppDates.number(levelScale.min + fraction * (levelScale.max - levelScale.min), digits: centimetres ? 0 : 2), at: CGPoint(x: plot.maxX + 5, y: py), anchor: .leading)
                }
                for fraction in [0.0, 0.5, 1.0] {
                    let date = minTime.addingTimeInterval(seconds * fraction)
                    label(AppDates.format(date, seconds > 86400 ? "dd.MM." : "HH:mm"), at: CGPoint(x: x(date), y: plot.maxY + 15))
                }
                if station == .baselRheinhalle {
                    for stage in RhineNavigation.officialThresholdStages {
                        if let threshold = RhineNavigation.thresholdGraphValue(stage: stage, centimetres: centimetres), threshold >= levelScale.min, threshold <= levelScale.max {
                            let py = y(threshold, minimum: levelScale.min, maximum: levelScale.max)
                            var line = Path(); line.move(to: CGPoint(x: plot.minX, y: py)); line.addLine(to: CGPoint(x: plot.maxX, y: py))
                            context.stroke(line, with: .color(RiverColors.level(stage).opacity(0.7)), style: StrokeStyle(lineWidth: 1, dash: [4, 3]))
                        }
                    }
                }
                func historicalStage(_ date: Date) -> RhineNavigation.Stage {
                    guard station == .baselRheinhalle, let nearest = levels.min(by: { abs($0.time.timeIntervalSince(date)) < abs($1.time.timeIntervalSince(date)) }) else { return .normal }
                    return RhineNavigation.stage(gaugeCentimetres: RiverDisplay.gaugeCentimetres(station: station, metresAboveSea: nearest.value))
                }
                func draw(_ series: [HydroObservation], isLevel: Bool) {
                    guard series.count > 1 else { return }
                    for index in 1..<series.count {
                        let before = series[index - 1], current = series[index]
                        let scale = isLevel ? levelScale : flowScale
                        var segment = Path()
                        segment.move(to: CGPoint(x: x(before.time), y: y(isLevel ? displayed(before.value) : before.value, minimum: scale.min, maximum: scale.max)))
                        segment.addLine(to: CGPoint(x: x(current.time), y: y(isLevel ? displayed(current.value) : current.value, minimum: scale.min, maximum: scale.max)))
                        let stage = historicalStage(current.time)
                        context.stroke(segment, with: .color(isLevel ? RiverColors.level(stage) : RiverColors.flow(stage)), lineWidth: 2)
                    }
                }
                draw(flows, isLevel: false); draw(levels, isLevel: true)
                if let selection {
                    var line = Path(); line.move(to: CGPoint(x: x(selection), y: plot.minY)); line.addLine(to: CGPoint(x: x(selection), y: plot.maxY))
                    context.stroke(line, with: .color(.secondary), style: StrokeStyle(lineWidth: 1, dash: [3]))
                }
            }
            .contentShape(Rectangle())
            .gesture(DragGesture(minimumDistance: 0).onChanged { value in
                let fraction = min(1, max(0, (value.location.x - plot.minX) / plot.width))
                selection = minTime.addingTimeInterval(Double(fraction) * seconds)
            })
        }
    }
}

struct RiverTemperatureCard: View {
    @EnvironmentObject private var state: AppState
    let station: HydroStation
    var body: some View {
        PFVRCard(state.ui("Wassertemperatur") + " · " + station.label) {
            let points = state.rivers[station]?.series(parameter: "WT", range: state.range) ?? []
            if points.isEmpty { Text(state.ui("Keine Temperaturdaten verfügbar.")).font(.caption).foregroundStyle(.secondary) }
            else {
                Chart(points) { point in
                    LineMark(x: .value("Zeit", point.time), y: .value("°C", point.value)).foregroundStyle(PFVRTheme.water)
                }.chartYScale(domain: .automatic(includesZero: false)).frame(height: 150)
                SourceStamp(source: "BAFU · °C", updated: points.last?.time)
            }
        }
    }
}

enum RiverColors {
    static func level(_ stage: RhineNavigation.Stage) -> Color {
        switch stage {
        case .unknown: return .secondary
        case .normal: return PFVRTheme.water
        case .hwmI: return Color(red: 0.65, green: 0.49, blue: 0.02)
        case .hwmIIb: return .orange
        case .hwmIIa: return .red
        }
    }
    static func flow(_ stage: RhineNavigation.Stage) -> Color {
        switch stage {
        case .hwmI: return Color(red: 0.45, green: 0.36, blue: 0.08)
        case .hwmIIb: return Color(red: 0.62, green: 0.29, blue: 0.04)
        case .hwmIIa: return Color(red: 0.60, green: 0.12, blue: 0.16)
        default: return Color(red: 0.35, green: 0.47, blue: 0.60)
        }
    }
}
