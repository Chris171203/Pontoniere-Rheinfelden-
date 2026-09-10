import SwiftUI
import PFVRCore

@main
struct PFVRApp: App {
    @StateObject private var state = AppState()

    var body: some Scene {
        WindowGroup {
            Group {
                if state.unlocked {
                    AppShellView()
                } else {
                    LandingView()
                }
            }
            .environmentObject(state)
            .tint(PFVRTheme.water)
            .preferredColorScheme(state.colorScheme)
        }
    }
}

enum AppTab: String, CaseIterable, Identifiable {
    case home, river, events, `internal`, cash, club
    var id: String { rawValue }
    var title: String {
        switch self {
        case .home: return "Home"
        case .river: return "Rhein"
        case .events: return "Termine"
        case .internal: return "Intern"
        case .cash: return "Kasse"
        case .club: return "Verein"
        }
    }
    var symbol: String {
        switch self {
        case .home: return "house"
        case .river: return "water.waves"
        case .events: return "calendar"
        case .internal: return "person.2"
        case .cash: return "cart"
        case .club: return "flag"
        }
    }
}

struct AppShellView: View {
    @EnvironmentObject private var state: AppState
    @Environment(\.scenePhase) private var scenePhase
    @State private var settings = false

    var body: some View {
        NavigationStack {
            Group {
                switch state.tab {
                case .home: HomeView()
                case .river: RiverView()
                case .events: CalendarView()
                case .internal: InternalAttendanceView(language: state.language.rawValue, openSettings: { settings = true })
                case .cash: CashView()
                case .club: ClubView()
                }
            }
            .navigationTitle(state.tab == .home ? "PFVR Rheinfelden" : state.ui(state.tab.title))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { settings = true } label: { Image(systemName: "gearshape") }
                        .accessibilityLabel(state.ui("Einstellungen"))
                        .accessibilityIdentifier("settings.open")
                }
            }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                HStack(spacing: 0) {
                    ForEach(AppTab.allCases) { tab in
                        Button { state.tab = tab } label: {
                            VStack(spacing: 4) {
                                Image(systemName: tab.symbol).font(.system(size: 20))
                                Text(state.ui(tab.title)).font(.system(size: 10, weight: .medium)).lineLimit(1)
                            }
                            .frame(maxWidth: .infinity, minHeight: 52)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .foregroundStyle(state.tab == tab ? PFVRTheme.water : Color.secondary)
                        .accessibilityIdentifier("tab.\(tab.rawValue)")
                        .accessibilityAddTraits(state.tab == tab ? .isSelected : [])
                    }
                }
                .padding(.horizontal, 4)
                .background(.bar)
                .overlay(alignment: .top) { Divider() }
            }
        }
        .sheet(isPresented: $settings) { SettingsView() }
        .alert(state.ui("War die Zahlung erfolgreich?"), isPresented: $state.showPaymentConfirmation) {
            Button(state.ui("Ja")) { state.confirmPayment(true) }.accessibilityIdentifier("payment.confirm.yes")
            Button(state.ui("Nein"), role: .cancel) { state.confirmPayment(false) }.accessibilityIdentifier("payment.confirm.no")
        } message: {
            Text(state.ui("Ja leert den Warenkorb. Ohne Bestätigung bleibt er gespeichert."))
        }
        .task { await state.startIfNeeded() }
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            state.checkPaymentConfirmation()
            while !Task.isCancelled {
                await state.refresh()
                do { try await Task.sleep(for: .seconds(300)) } catch { return }
            }
        }
    }
}
