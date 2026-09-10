import SwiftUI
import SafariServices
import PFVRCore

struct ClubView: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PageScroll {
            HStack(spacing: 14) {
                Image("PFVRLogo").resizable().scaledToFit().frame(width: 76, height: 76).accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 6) {
                    Text("Pontonierfahrverein Rheinfelden").font(.headline)
                    Text(state.ui("Gegründet 1896 · Sport und Vereinsleben am Rhein")).font(.caption).foregroundStyle(.secondary)
                }
            }.padding(.vertical, 8)
            ForEach(state.visibleTiles(.club), id: \.id) { tile in
                switch tile.id {
                case "club_about":
                    PFVRCard(state.ui("Über den Verein")) {
                        Text(state.ui("Seit 1896 auf dem Rhein")).font(.headline)
                        Text(state.ui("Beim Pontonierfahren verbinden sich präzise Bootsführung, Kraft, Technik und Teamarbeit. Der PFVR trainiert auf dem Rhein in Rheinfelden, nimmt an Wettfahren teil und pflegt zugleich ein aktives Vereinsleben sowie die Ausbildung des Nachwuchses."))
                            .font(.subheadline).foregroundStyle(.secondary)
                    }
                case "club_news":
                    NavigationLink { NewsView() } label: { ClubActionCard(title: state.ui("Vereinsnews"), detail: state.ui("Aktuelle Meldungen"), symbol: "newspaper") }.buttonStyle(.plain)
                case "club_program": publicLink("Jahresprogramm", "Termine und Kalender", "calendar", PublicLinks.program)
                case "club_board": publicLink("Vorstand", "Funktionen und Kontakte", "person.3", PublicLinks.board)
                case "club_history": publicLink("Geschichte", "Seit 1896 auf dem Rhein", "clock.arrow.circlepath", PublicLinks.history)
                case "club_depot":
                    if let url = PlatformLinks.map("Rheinweg 42, 4310 Rheinfelden, Schweiz") {
                        Link(destination: url) { ClubActionCard(title: state.ui("Depot & Route"), detail: "Rheinweg 42", symbol: "map") }.buttonStyle(.plain)
                    }
                case "club_phone":
                    Link(destination: URL(string: "tel:+41762091896")!) { ClubActionCard(title: state.ui("Telefon"), detail: "076 209 18 96", symbol: "phone") }.buttonStyle(.plain)
                case "club_email":
                    Link(destination: URL(string: "mailto:info@pfvr.ch")!) { ClubActionCard(title: "E-Mail", detail: "info@pfvr.ch", symbol: "envelope") }.buttonStyle(.plain)
                case "club_contact": publicLink("Kontaktseite", "Weitere Ansprechwege", "person.crop.circle", PublicLinks.contact)
                case "club_instagram": publicLink("Instagram", "@pontoniererheinfelden", "camera", PublicLinks.instagram, rawDetail: true)
                case "club_facebook": publicLink("Facebook", "Pontoniere Rheinfelden", "person.2", PublicLinks.facebook, rawDetail: true)
                default: EmptyView()
                }
            }
        }.accessibilityIdentifier("screen.club")
    }
    private func publicLink(_ title: String, _ detail: String, _ symbol: String, _ url: URL, rawDetail: Bool = false) -> some View {
        NavigationLink { PublicWebsiteView(url: url).ignoresSafeArea(edges: .bottom).navigationTitle(state.ui(title)).navigationBarTitleDisplayMode(.inline) } label: {
            ClubActionCard(title: state.ui(title), detail: rawDetail ? detail : state.ui(detail), symbol: symbol)
        }.buttonStyle(.plain)
    }
}

struct ClubActionCard: View {
    let title: String
    let detail: String
    let symbol: String
    var body: some View {
        PFVRCard {
            HStack(spacing: 14) {
                Image(systemName: symbol).font(.title3).foregroundStyle(PFVRTheme.water).frame(width: 30)
                VStack(alignment: .leading, spacing: 5) {
                    Text(title).font(.headline)
                    Text(detail).font(.caption).foregroundStyle(.secondary)
                }.frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.right").font(.caption).foregroundStyle(.tertiary)
            }
        }
    }
}

struct NewsView: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        PageScroll { NewsTile(limit: 100) }
            .navigationTitle(state.ui("Vereinsnews"))
            .refreshable { await state.refresh(force: true) }
    }
}

struct NewsTile: View {
    @EnvironmentObject private var state: AppState
    var limit = 3
    var body: some View {
        PFVRCard(state.ui("Aktuell vom Verein")) {
            if state.news?.value.isEmpty ?? true {
                Text(state.ui("Noch keine News gespeichert.")).font(.subheadline).foregroundStyle(.secondary)
            }
            ForEach(Array((state.news?.value ?? []).prefix(limit))) { article in
                NavigationLink { PublicWebsiteView(url: article.url).navigationTitle(state.ui("Vereinsnews")).navigationBarTitleDisplayMode(.inline) } label: {
                    VStack(alignment: .leading, spacing: 7) {
                        Text(article.title).font(.headline)
                        if let date = article.publishedAt { Text(AppDates.format(date, "dd.MM.yyyy")).font(.caption2).foregroundStyle(.secondary) }
                        Text(article.excerpt).font(.subheadline).foregroundStyle(.secondary).lineLimit(4)
                    }.frame(maxWidth: .infinity, alignment: .leading)
                }.buttonStyle(.plain)
                Divider()
            }
            SourceStamp(source: state.news?.metadata.source ?? "PFVR WordPress", updated: state.news?.metadata.updatedAt, stale: state.news?.metadata.isStale ?? false)
        }
    }
}

struct PublicWebsiteView: UIViewControllerRepresentable {
    let url: URL
    func makeUIViewController(context: Context) -> SFSafariViewController {
        let config = SFSafariViewController.Configuration()
        config.entersReaderIfAvailable = false
        return SFSafariViewController(url: url, configuration: config)
    }
    func updateUIViewController(_ controller: SFSafariViewController, context: Context) {}
}

enum PlatformLinks {
    static func map(_ query: String) -> URL? {
        var components = URLComponents(string: "https://maps.apple.com/")
        components?.queryItems = [URLQueryItem(name: "q", value: query)]
        return components?.url
    }
}
