import SwiftUI
import PFVRCore

struct SettingsView: View {
    @EnvironmentObject private var state: AppState
    @Environment(\.dismiss) private var dismiss
    @State private var internalURL = ""
    @State private var linkResult: String?
    @State private var confirmRemove = false
    @State private var confirmClearCache = false
    var body: some View {
        NavigationStack {
            Form {
                Section(state.ui("Allgemein")) {
                    LanguagePicker()
                    Picker(state.ui("Darstellung"), selection: $state.theme) {
                        Text(state.ui("System")).tag("system")
                        Text(state.ui("Hell")).tag("light")
                        Text(state.ui("Dunkel")).tag("dark")
                    }.accessibilityIdentifier("settings.theme")
                    NavigationLink { TileSettingsView() } label: { Label(state.ui("Ansicht & Kacheln"), systemImage: "square.grid.2x2") }
                        .accessibilityIdentifier("settings.tiles")
                    Toggle(state.ui("Hintergrundaktualisierung"), isOn: $state.backgroundRefresh)
                        .accessibilityIdentifier("settings.backgroundRefresh")
                    Button { Task { await state.refresh(force: true) } } label: {
                        HStack { Text(state.ui("Daten aktualisieren")); Spacer(); if state.loading { ProgressView() } }
                    }.disabled(state.loading)
                    Button(state.ui("Daten-Cache leeren"), role: .destructive) { confirmClearCache = true }
                        .disabled(state.loading).accessibilityIdentifier("settings.cache.clear")
                }
                Section {
                    SecureField(state.ui("Persönlicher intern.pfvr.ch-Link"), text: $internalURL)
                        .textInputAutocapitalization(.never).autocorrectionDisabled().keyboardType(.URL)
                        .accessibilityIdentifier("settings.internal.url")
                    Button(state.ui("Link speichern"), action: saveLink).accessibilityIdentifier("settings.internal.save")
                    Button(state.ui("Link entfernen"), role: .destructive) { confirmRemove = true }
                    if let linkResult { Text(linkResult).font(.caption).accessibilityIdentifier("settings.internal.result") }
                } header: { Text(state.ui("Interner Bereich")) } footer: {
                    Text(state.ui("Der persönliche Link bleibt geschützt auf diesem Gerät. Die Konfiguration erfolgt ausschliesslich hier."))
                }
                Section(state.ui("Rhein")) {
                    Picker(state.ui("Erste Station"), selection: $state.firstStation) {
                        ForEach(HydroStation.allCases) { Text($0.label).tag($0) }
                    }
                    Toggle(state.ui("Zweite Station anzeigen"), isOn: $state.secondStationEnabled)
                    if state.secondStationEnabled {
                        Picker(state.ui("Zweite Station"), selection: $state.secondStation) {
                            ForEach(HydroStation.allCases) { Text($0.label).tag($0) }
                        }
                    }
                    Picker(state.ui("Zeitraum"), selection: $state.range) {
                        ForEach(HydroRange.allCases) { Text($0.label).tag($0) }
                    }
                    Toggle(state.ui("Basel-Pegel in cm"), isOn: $state.baselCentimetres)
                    Text(state.ui("Rheinfelden bleibt in m ü.M.; ein verifizierter cm-Bezug liegt nur für Basel vor."))
                        .font(.caption).foregroundStyle(.secondary)
                }
                Section(state.ui("Zahlung")) {
                    Label(state.ui("Swiss QR über iOS-Teilen"), systemImage: "square.and.arrow.up")
                    Text(state.ui("Beim Bezahlen wählst du eine Banking-App im Teilen-Menü. Unterstützt sie keinen QR-Bildimport, speichere den QR und importiere ihn in der Bank-App."))
                        .font(.caption).foregroundStyle(.secondary)
                    Text(PaymentDetails.payee).font(.subheadline)
                    Text(PaymentDetails.iban).font(.caption).textSelection(.enabled)
                }
                Section {
                    LabeledContent(state.ui("Version"), value: Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "0.12.6")
                    Link("pfvr.ch", destination: PublicLinks.site)
                }
            }
            .navigationTitle(state.ui("Einstellungen"))
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button(state.ui("Fertig")) { dismiss() }.accessibilityIdentifier("settings.done") } }
        }
        .task {
            if !state.testing { internalURL = SecureInternalStore.shared.loadURL()?.absoluteString ?? "" }
        }
        .confirmationDialog(state.ui("Persönlichen Link entfernen?"), isPresented: $confirmRemove, titleVisibility: .visible) {
            Button(state.ui("Entfernen"), role: .destructive) {
                do { try SecureInternalStore.shared.removeURL(); internalURL = ""; linkResult = state.ui("Link entfernt.") }
                catch { linkResult = state.ui("Der Link konnte nicht entfernt werden.") }
            }
            Button(state.ui("Abbrechen"), role: .cancel) {}
        }
        .confirmationDialog(state.ui("Daten-Cache leeren?"), isPresented: $confirmClearCache, titleVisibility: .visible) {
            Button(state.ui("Daten-Cache leeren"), role: .destructive) { Task { await state.clearPublicCache() } }
                .accessibilityIdentifier("settings.cache.clear.confirm")
            Button(state.ui("Abbrechen"), role: .cancel) {}
        } message: {
            Text(state.ui("Wetter, Rhein, Kalender und News werden neu geladen. Warenkorb, Einstellungen und persönlicher Zugang bleiben erhalten."))
        }
    }
    private func saveLink() {
        do {
            try SecureInternalStore.shared.saveURL(internalURL)
            linkResult = state.ui("Link gespeichert.")
        } catch { linkResult = state.ui("Bitte einen gültigen persönlichen HTTPS-Link von intern.pfvr.ch eingeben.") }
    }
}

struct TileSettingsView: View {
    @EnvironmentObject private var state: AppState
    @State private var area: TileArea = .home
    var body: some View {
        List {
            Section {
                HStack {
                    ForEach([TileArea.home, .cash, .club], id: \.rawValue) { area in
                        Button { self.area = area } label: {
                            Text(state.ui(area.label)).font(.subheadline.weight(self.area == area ? .bold : .regular))
                                .frame(maxWidth: .infinity, minHeight: 40)
                                .background(self.area == area ? PFVRTheme.water.opacity(0.13) : Color.clear, in: RoundedRectangle(cornerRadius: 8))
                        }.buttonStyle(.plain).accessibilityIdentifier("tiles.area.\(area.rawValue)")
                    }
                }
            }
            Section {
                ForEach(state.orderedTiles(area), id: \.id) { tile in
                    HStack(spacing: 8) {
                        if tile.pinned {
                            Label(state.ui(tile.label), systemImage: "pin.fill").font(.subheadline)
                            Spacer()
                            Text(state.ui("Fixiert")).font(.caption).foregroundStyle(.secondary)
                        } else {
                            Toggle(state.ui(tile.label), isOn: Binding(get: { state.tileVisible(tile) }, set: { state.setTileVisible($0, tile: tile) }))
                                .font(.subheadline).accessibilityIdentifier("tiles.toggle.\(tile.id)")
                            Button { state.moveTile(tile, by: -1) } label: { Image(systemName: "arrow.up").frame(width: 32, height: 44) }
                                .buttonStyle(.borderless).accessibilityLabel(state.ui("Nach oben")).accessibilityIdentifier("tiles.up.\(tile.id)")
                            Button { state.moveTile(tile, by: 1) } label: { Image(systemName: "arrow.down").frame(width: 32, height: 44) }
                                .buttonStyle(.borderless).accessibilityLabel(state.ui("Nach unten")).accessibilityIdentifier("tiles.down.\(tile.id)")
                        }
                    }
                }
            }
            Button(state.ui("Standard wiederherstellen")) { state.resetTiles(area) }
        }
        .navigationTitle(state.ui("Ansicht & Kacheln"))
        .navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier("settings.tiles.detail")
    }
}
