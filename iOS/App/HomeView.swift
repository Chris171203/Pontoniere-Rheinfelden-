import SwiftUI
import PFVRCore

struct HomeView: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PageScroll {
            HStack(spacing: 14) {
                Image("PFVRLogo").resizable().scaledToFit().frame(width: 62, height: 62).accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 5) {
                    Text(state.ui("Gemeinsam auf dem Rhein")).font(.title3.bold())
                    Button { state.tab = .internal } label: {
                        Label(state.ui("An- / Abmeldung"), systemImage: "person.crop.circle.badge.checkmark")
                    }.font(.subheadline).accessibilityIdentifier("home.internal")
                }
            }.padding(.vertical, 4)
            if state.loading && state.weather == nil { ProgressView(state.ui("Daten werden geladen …")).frame(maxWidth: .infinity) }
            if !state.failures.isEmpty {
                PFVRCard {
                    Text(state.failures.joined(separator: "\n")).font(.caption).foregroundStyle(.secondary)
                }
            }
            ForEach(state.visibleTiles(.home), id: \.id) { tile in
                switch tile.id {
                case "home_weather": EventWeatherTile()
                case "home_weather_3day": ThreeDayWeatherTile()
                case "home_river_summary": RiverSummaryTile()
                case "home_river_charts":
                    ForEach(state.activeStations) { station in RiverChartCard(station: station) }
                case "home_events":
                    PFVRCard(state.ui("Als Nächstes")) {
                        if state.upcoming.isEmpty { Text(state.ui("Keine kommenden Termine gespeichert.")).font(.subheadline).foregroundStyle(.secondary) }
                        ForEach(Array(state.upcoming.prefix(3))) { event in
                            NavigationLink { EventDetailView(event: event) } label: { EventRow(event: event) }.buttonStyle(.plain)
                        }
                        Button(state.ui("Alle Termine")) { state.tab = .events }
                        SourceStamp(source: "PFVR Vereinskalender", updated: state.events?.metadata.updatedAt, stale: state.events?.metadata.isStale ?? false)
                    }
                case "home_news": NewsTile(limit: 3)
                default: EmptyView()
                }
            }
        }
        .refreshable { await state.refresh(force: true) }
        .accessibilityIdentifier("screen.home")
    }
}

struct EventWeatherTile: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PFVRCard(state.ui("Wetter zum nächsten Termin")) {
            if let event = state.nextWeatherEvent {
                let forecast = WeatherForecast.event(hours: state.weather?.value ?? [], event: event)
                Text(event.fromCalendar ? event.title : state.ui(event.title)).font(.headline)
                Text(eventTime(event)).font(.caption).foregroundStyle(.secondary)
                if forecast.summary.count == 0 {
                    Text(state.ui("Für diesen Termin liegt noch keine Wetterprognose vor.")).font(.subheadline).foregroundStyle(.secondary)
                } else {
                    if !forecast.slots.isEmpty {
                        WeatherSlots(slots: forecast.slots, dayparts: event.allDay)
                    } else {
                        HStack {
                            Image(systemName: WeatherSymbols.symbol(forecast.summary.weatherCode)).font(.largeTitle).symbolRenderingMode(.multicolor)
                            Text(AppDates.number(forecast.summary.minTemperature) + "–" + AppDates.number(forecast.summary.maxTemperature) + " °C").font(.title2.weight(.semibold))
                        }
                    }
                    WeatherSummary(summary: forecast.summary)
                }
            } else { Text(state.ui("Kein nächster Termin verfügbar.")).foregroundStyle(.secondary) }
            SourceStamp(source: state.weather?.metadata.source ?? "MeteoSwiss/Open-Meteo", updated: state.weather?.metadata.updatedAt, stale: state.weather?.metadata.isStale ?? false)
        }.accessibilityIdentifier("home.weather")
    }
    private func eventTime(_ event: PFVREvent) -> String {
        AppDates.day(event.start, language: state.language) + " · " + (event.allDay ? state.ui("Ganztägig") : AppDates.time(event.start) + "–" + AppDates.time(event.end))
    }
}

struct ThreeDayWeatherTile: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PFVRCard(state.ui("3-Tage-Wetter")) {
            let days = WeatherForecast.days(hours: state.weather?.value ?? [], firstDay: state.now)
            ForEach(days) { day in
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text(AppDates.day(day.date, language: state.language)).font(.subheadline.bold())
                        Spacer()
                        Text(AppDates.number(day.minTemperature) + "–" + AppDates.number(day.maxTemperature) + " °C").font(.subheadline.bold())
                    }
                    WeatherSlots(slots: day.slots, dayparts: true)
                    WeatherSummary(summary: day)
                }
                if day.id != days.last?.id { Divider() }
            }
            SourceStamp(source: state.weather?.metadata.source ?? "MeteoSwiss/Open-Meteo", updated: state.weather?.metadata.updatedAt, stale: state.weather?.metadata.isStale ?? false)
        }.accessibilityIdentifier("home.weather.threeDays")
    }
}

struct WeatherSlots: View {
    @EnvironmentObject private var state: AppState
    let slots: [WeatherSlot]
    var dayparts = false
    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            ForEach(slots) { slot in
                VStack(spacing: 7) {
                    Text(label(slot.target)).font(.caption2).foregroundStyle(.secondary).lineLimit(2).multilineTextAlignment(.center)
                    Image(systemName: WeatherSymbols.symbol(slot.hour?.weatherCode))
                        .font(.title2).symbolRenderingMode(.multicolor).accessibilityHidden(true)
                    Text(AppDates.number(slot.hour?.temperature) + " °C").font(.subheadline.bold())
                    Text((slot.hour?.precipitationProbability.map(String.init) ?? "–") + " %").font(.caption).foregroundStyle(.secondary)
                }.frame(maxWidth: .infinity)
            }
        }
    }
    private func label(_ date: Date) -> String {
        let hour = AppDates.zurich.component(.hour, from: date)
        let part = hour < 10 ? "Morgen" : hour < 16 ? "Mittag" : "Abend"
        let localizedPart = hour < 10 && state.language == .swissGerman ? "Morge" : state.ui(part)
        return (dayparts ? localizedPart + " · " : "") + AppDates.time(date)
    }
}

struct WeatherSummary: View {
    @EnvironmentObject private var state: AppState
    let summary: WeatherDay
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(state.ui("Regen") + " " + (summary.precipitationProbabilityMax.map(String.init) ?? "–") + " % · " + AppDates.number(summary.precipitationSum, digits: 1) + " mm")
            Text(state.ui("Wind") + " " + AppDates.number(summary.windMax) + " km/h · " + state.ui("Böen") + " " + AppDates.number(summary.gustMax) + " km/h")
            Text("UV " + AppDates.number(summary.uvMax, digits: 1))
        }.font(.caption).foregroundStyle(.secondary)
    }
}

enum WeatherSymbols {
    static func symbol(_ code: Int?) -> String {
        guard let code else { return "questionmark.circle" }
        switch code {
        case 0: return "sun.max.fill"
        case 1...2: return "cloud.sun.fill"
        case 3: return "cloud.fill"
        case 45, 48: return "cloud.fog.fill"
        case 51...67: return "cloud.rain.fill"
        case 71...77, 85...86: return "cloud.snow.fill"
        case 80...82: return "cloud.heavyrain.fill"
        case 95...99: return "cloud.bolt.rain.fill"
        default: return "cloud"
        }
    }
}
