import SwiftUI
import EventKit
import EventKitUI
import PFVRCore

struct CalendarView: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PageScroll {
            SourceStamp(source: state.events?.metadata.source ?? "PFVR Vereinskalender", updated: state.events?.metadata.updatedAt, stale: state.events?.metadata.isStale ?? false)
            if state.upcoming.isEmpty {
                ContentUnavailableView(state.ui("Keine kommenden Termine"), systemImage: "calendar", description: Text(state.ui("Aktualisiere den Kalender, sobald eine Verbindung verfügbar ist.")))
            }
            ForEach(state.upcoming) { event in
                NavigationLink { EventDetailView(event: event) } label: {
                    PFVRCard { EventRow(event: event) }
                }.buttonStyle(.plain).accessibilityIdentifier("event.\(event.id)")
            }
        }
        .refreshable { await state.refresh(force: true) }
        .accessibilityIdentifier("screen.events")
    }
}

struct EventRow: View {
    @EnvironmentObject private var state: AppState
    let event: PFVREvent
    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            VStack(spacing: 3) {
                Text(AppDates.format(event.start, "dd")).font(.title2.bold())
                Text(AppDates.format(event.start, "MMM").uppercased()).font(.caption2.weight(.semibold))
            }.foregroundStyle(PFVRTheme.water).frame(width: 40)
            VStack(alignment: .leading, spacing: 5) {
                Text(event.fromCalendar ? event.title : state.ui(event.title)).font(.subheadline.bold())
                Text(event.allDay ? state.ui("Ganztägig") : AppDates.time(event.start) + "–" + AppDates.time(event.end))
                    .font(.caption).foregroundStyle(.secondary)
                if !event.location.isEmpty { Text(event.location).font(.caption).foregroundStyle(.secondary).lineLimit(2) }
                if event.isCancelled { Text(state.ui("Abgesagt")).font(.caption.bold()).foregroundStyle(.red) }
            }.frame(maxWidth: .infinity, alignment: .leading)
            Image(systemName: "chevron.right").font(.caption).foregroundStyle(.tertiary)
        }.accessibilityElement(children: .combine)
    }
}

struct EventDetailView: View {
    @EnvironmentObject private var state: AppState
    @State private var calendarEditor = false
    let event: PFVREvent
    var body: some View {
        PageScroll {
            PFVRCard {
                Text(event.fromCalendar ? event.title : state.ui(event.title)).font(.title2.bold())
                if event.isCancelled { Text(state.ui("Abgesagt")).foregroundStyle(.red).font(.headline) }
                Label(AppDates.day(event.start, language: state.language), systemImage: "calendar")
                if event.allDay {
                    Text(state.ui("Ganztägig"))
                    if !AppDates.zurich.isDate(event.start, inSameDayAs: event.end.addingTimeInterval(-1)) {
                        Text(state.ui("Bis") + " " + AppDates.day(event.end.addingTimeInterval(-1), language: state.language))
                    }
                } else {
                    if AppDates.zurich.isDate(event.start, inSameDayAs: event.end) {
                        Label(AppDates.time(event.start) + "–" + AppDates.time(event.end), systemImage: "clock")
                    } else {
                        Label(AppDates.stamp(event.start) + " – " + AppDates.stamp(event.end), systemImage: "clock")
                    }
                }
                if !event.location.isEmpty { Label(event.location, systemImage: "mappin.and.ellipse") }
                if !event.details.isEmpty { Divider(); Text(event.details).font(.body).textSelection(.enabled) }
            }
            PFVRCard {
                ShareLink(item: shareText) { Label(state.ui("Termin teilen"), systemImage: "square.and.arrow.up") }
                    .accessibilityIdentifier("event.share")
                Button { calendarEditor = true } label: {
                    Label(state.ui("In Kalender übernehmen"), systemImage: "calendar.badge.plus")
                }.accessibilityIdentifier("event.calendar")
                if !event.location.isEmpty, let url = PlatformLinks.map(event.location) {
                    Link(destination: url) { Label(state.ui("Route öffnen"), systemImage: "map") }
                        .accessibilityIdentifier("event.route")
                }
            }
        }
        .navigationTitle(state.ui("Termin"))
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier("event.detail")
        .sheet(isPresented: $calendarEditor) { CalendarEventEditor(event: event) }
    }
    private var shareText: String {
        [event.title, AppDates.stamp(event.start) + " – " + AppDates.stamp(event.end), event.location, event.details].filter { !$0.isEmpty }.joined(separator: "\n")
    }
}

/// iOS 17's native editor owns permission and save confirmation; presenting does not create an event.
struct CalendarEventEditor: UIViewControllerRepresentable {
    @Environment(\.dismiss) private var dismiss
    let event: PFVREvent
    func makeCoordinator() -> Coordinator { Coordinator { dismiss() } }
    func makeUIViewController(context: Context) -> EKEventEditViewController {
        let controller = EKEventEditViewController()
        controller.eventStore = context.coordinator.store
        let item = EKEvent(eventStore: context.coordinator.store)
        item.title = event.title
        item.startDate = event.start
        item.endDate = event.end
        item.isAllDay = event.allDay
        item.timeZone = AppDates.zurich.timeZone
        item.location = event.location
        item.notes = event.details
        controller.event = item
        controller.editViewDelegate = context.coordinator
        return controller
    }
    func updateUIViewController(_ controller: EKEventEditViewController, context: Context) {}
    final class Coordinator: NSObject, EKEventEditViewDelegate {
        let store = EKEventStore()
        let close: () -> Void
        init(close: @escaping () -> Void) { self.close = close }
        func eventEditViewController(_ controller: EKEventEditViewController, didCompleteWith action: EKEventEditViewAction) { close() }
    }
}
