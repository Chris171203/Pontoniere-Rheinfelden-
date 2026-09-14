import SwiftUI
import SafariServices
import PFVRCore

struct ClubView: View {
    @EnvironmentObject private var state: AppState
    private var source: Loaded<ClubContent>? { state.clubPages[.about] }
    var body: some View {
        PageScroll {
            if let photo = source?.value.hero { ClubPhotoView(photo: photo) }
            else { Image("PFVRLogo").resizable().scaledToFit().frame(height: 82).frame(maxWidth: .infinity).accessibilityHidden(true) }
            Text("Pontonierfahrverein Rheinfelden").font(.title2.bold())
            if let source { Text(source.value.overviewIntro).accessibilityIdentifier("club.intro") }
            ClubTrainingOverview(content: source?.value)
            Text(state.ui("Verein entdecken")).font(.title2.bold()).padding(.top, 8)
            ForEach(state.visibleTiles(.club), id: \.id) { tile in
                if tile.id == "club_news" {
                    NavigationLink { NewsView() } label: { ClubNavigationRow(title: state.ui("Vereinsnews"), detail: state.ui("Aktuelle Meldungen")) }
                        .buttonStyle(.plain).accessibilityIdentifier("club.row.news")
                } else if let destination = destination(tile.id) {
                    NavigationLink { ClubDetailView(destination: destination) } label: {
                        ClubNavigationRow(title: state.ui(destination.label), detail: state.ui(detail(destination)))
                    }.buttonStyle(.plain).accessibilityIdentifier("club.row." + destination.rawValue)
                }
            }
            ClubSourceStatus(page: .about)
            ClubFooter()
        }
        .task { await state.loadClub(.about) }
        .refreshable { await state.loadClub(.about, force: true) }
        .accessibilityIdentifier("screen.club")
    }
    private func destination(_ id: String) -> ClubDestination? {
        switch id { case "club_sport": return .sport; case "club_about": return .life; case "club_youth": return .youth
        case "club_board": return .board; case "club_history": return .history; case "club_contact": return .contact; default: return nil }
    }
    private func detail(_ destination: ClubDestination) -> String {
        switch destination { case .sport: return "Weidling, Boot und Fahrtechnik"; case .life: return "Gemeinsam unterwegs"
        case .youth: return "Fahren, Knoten und Lager"; case .board: return "Funktionen und Kontakte"
        case .history: return "Seit 1896 auf dem Rhein"; case .contact: return "Weitere Ansprechwege"
        case .training: return "Ablauf und Trainingsinhalte" }
    }
}
struct ClubTrainingOverview: View {
    @EnvironmentObject private var state: AppState
    let content: ClubContent?
    var body: some View {
        Text(state.ui("Training")).font(.title2.bold()).padding(.top, 8)
        if let content {
            let training = ClubTraining.summaries(content)
            ForEach(training) { season in
                PFVRCard(state.ui(season.label)) {
                    if !season.season.isEmpty { Text(season.season).font(.caption).foregroundStyle(.secondary) }
                    if season.fallback.isEmpty {
                        Text(state.ui(season.days) + " · " + season.time + " " + state.ui("Uhr")).font(.headline)
                            .accessibilityIdentifier("club.training." + season.id)
                    } else { Text(season.fallback) }
                    if !season.location.isEmpty, let url = PlatformLinks.map(season.location) {
                        Link(destination: url) { Label(season.location, systemImage: "location") }
                            .accessibilityLabel(state.ui("Treffpunkt auf Karte") + ": " + season.location)
                    }
                }
            }
            if training.isEmpty { Text(state.ui("Trainingszeiten stehen im Originaltext.")).font(.caption) }
        } else { Text(state.ui("Vereinsinfos werden geladen …")).font(.caption).foregroundStyle(.secondary) }
        Text(state.ui("Zeiten laut Homepage. Aktuelle Termine im Kalender.")).font(.caption).foregroundStyle(.secondary)
        Button(state.ui("Termine im Kalender")) { state.tab = .events }
            .buttonStyle(.bordered).accessibilityIdentifier("club.calendar")
        NavigationLink { ClubDetailView(destination: .training) } label: {
            ClubNavigationRow(title: state.ui("Trainingsinfos"), detail: state.ui("Ablauf und Trainingsinhalte"))
        }.buttonStyle(.plain).accessibilityIdentifier("club.row.training")
    }
}
struct ClubNavigationRow: View {
    let title: String
    let detail: String
    var body: some View {
        HStack(spacing: 12) {
            VStack(alignment: .leading, spacing: 5) {
                Text(title).font(.headline)
                Text(detail).font(.caption).foregroundStyle(.secondary)
            }.frame(maxWidth: .infinity, alignment: .leading)
            Image(systemName: "chevron.right").foregroundStyle(PFVRTheme.water).accessibilityHidden(true)
        }.frame(minHeight: 60).contentShape(Rectangle())
    }
}
struct ClubDetailView: View {
    @EnvironmentObject private var state: AppState
    let destination: ClubDestination
    @State private var originalExpanded = false
    var body: some View {
        PageScroll {
            ClubSourceStatus(page: destination.page)
            if let content = state.clubPages[destination.page]?.value {
                let sections = destination.sections(in: content)
                if destination == .history {
                    ForEach(state.clubPages[.about]?.value.milestones ?? []) { ClubArticleSection(section: $0) }
                    ForEach(content.milestones) { ClubArticleSection(section: $0) }
                }
                ForEach(sections) { section in
                    if section.children.isEmpty { ClubArticleSection(section: section) }
                    else {
                        Text(section.appTitle ? state.ui(section.title) : section.title).font(.title2.bold()).padding(.top, 8)
                        if !section.text.isEmpty { Text(section.text).fixedSize(horizontal: false, vertical: true) }
                        ClubLinks(links: section.links)
                        ForEach(section.children) { ClubArticleSection(section: $0) }
                    }
                }
                if destination == .youth {
                    if let contact = state.clubPages[.board]?.value.sections.first(where: { $0.title.lowercased().contains("jungpontonier") }) {
                        ClubArticleSection(section: contact)
                    }
                    Text(state.ui("Alters- und Prüfungsangaben bitte mit der aktuellen Vereinsauskunft abgleichen."))
                        .font(.caption).foregroundStyle(.secondary)
                }
                if sections.isEmpty && (destination != .history || content.milestones.isEmpty) {
                    Text(content.text).fixedSize(horizontal: false, vertical: true).accessibilityIdentifier("club.source.visible")
                    ClubLinks(links: content.links)
                } else {
                    DisclosureGroup(isExpanded: $originalExpanded) {
                        Text(content.text).fixedSize(horizontal: false, vertical: true)
                        ClubLinks(links: content.links)
                    } label: { Text(state.ui("Vollständiger Quelltext")).font(.caption) }
                    .accessibilityIdentifier("club.original")
                }
            }
            if destination == .contact { Link(state.ui("Kontaktformular öffnen"), destination: ClubPage.contact.url) }
            Link(state.ui("Quelle: pfvr.ch  →"), destination: destination.page.url).font(.caption)
        }
        .navigationTitle(state.ui(destination.label)).navigationBarTitleDisplayMode(.inline)
        .accessibilityIdentifier("club.detail." + destination.rawValue)
        .task {
            await state.loadClub(destination.page)
            if destination == .history { await state.loadClub(.about) }
            if destination == .youth { await state.loadClub(.board) }
        }
        .refreshable {
            await state.loadClub(destination.page, force: true)
            if destination == .history { await state.loadClub(.about, force: true) }
            if destination == .youth { await state.loadClub(.board, force: true) }
        }
    }
}
struct ClubArticleSection: View {
    @EnvironmentObject private var state: AppState
    let section: ClubSection
    var body: some View {
        PFVRCard(section.appTitle ? state.ui(section.title) : section.title) {
            if let photo = section.photo { ClubPhotoView(photo: photo) }
            if !section.text.isEmpty {
                Text(section.text).fixedSize(horizontal: false, vertical: true)
                    .accessibilityIdentifier("club.section." + section.id)
            }
            ClubLinks(links: section.links)
            if !section.location.isEmpty, let url = PlatformLinks.map(section.location) {
                Link(state.ui("Treffpunkt auf Karte"), destination: url)
            }
        }
    }
}
struct ClubLinks: View {
    let links: [ClubLink]
    var body: some View {
        ForEach(links) { link in
            if let destination = ClubDestination.matching(link.url) {
                NavigationLink(link.label) { ClubDetailView(destination: destination) }
            } else { Link(link.label, destination: link.url) }
        }
    }
}
struct ClubSourceStatus: View {
    @EnvironmentObject private var state: AppState
    let page: ClubPage
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            if let source = state.clubPages[page] {
                if let modified = source.value.modified {
                    Text("pfvr.ch · " + state.ui("Homepage geändert am") + " " + AppDates.format(modified, "dd.MM.yyyy"))
                }
                Text(state.ui("Abgerufen") + " " + AppDates.stamp(source.metadata.updatedAt))
                if source.metadata.isStale { Text(state.ui("Gespeicherter Stand")) }
                if let failure = source.metadata.failure { Text(state.ui(failure)) }
            } else if state.clubFailed.contains(page) { Text(state.ui("Vereinsinfos konnten gerade nicht geladen werden.")) }
            Button { Task { await state.loadClub(page, force: true) } } label: {
                Label(state.ui("Aktualisieren"), systemImage: "arrow.clockwise")
            }.disabled(state.clubLoading.contains(page)).frame(minHeight: 44)
        }.font(.caption).foregroundStyle(.secondary)
    }
}
struct ClubPhotoView: View {
    @EnvironmentObject private var state: AppState
    let photo: ClubPhoto
    @State private var image: UIImage?
    @State private var failed = false
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            if let image {
                Image(uiImage: image).resizable().scaledToFit().frame(maxWidth: .infinity)
                    .accessibilityLabel(photo.caption.isEmpty ? state.ui("Foto von pfvr.ch") : photo.caption)
            } else {
                Text(state.ui(failed ? "Bild nicht verfügbar" : "Bild wird geladen …")).font(.caption).foregroundStyle(.secondary)
            }
            if !photo.caption.isEmpty { Text(photo.caption).font(.caption).foregroundStyle(.secondary) }
        }.task(id: photo.url) {
            do { image = UIImage(data: try await ClubImageStore.shared.image(photo.url)); failed = image == nil }
            catch { failed = true }
        }
    }
}
struct ClubFooter: View {
    @EnvironmentObject private var state: AppState
    var body: some View {
        HStack(spacing: 8) {
            footer("Telefon", "phone.fill", URL(string: "tel:+41762091896")!, id: "phone")
            footer("Navigation", "location.fill", PlatformLinks.map("Rheinweg 42, 4310 Rheinfelden, Schweiz")!, id: "navigation")
            footer("E-Mail", "envelope", URL(string: "mailto:info@pfvr.ch")!, id: "email")
            Link(destination: PublicLinks.instagram) { Image("InstagramLogo").resizable().scaledToFit().frame(width: 26, height: 26).frame(width: 44, height: 44) }
                .accessibilityLabel("Instagram").accessibilityIdentifier("club.footer.instagram")
            Link(destination: PublicLinks.facebook) { Image("FacebookLogo").resizable().scaledToFit().frame(width: 26, height: 26).frame(width: 44, height: 44) }
                .accessibilityLabel("Facebook").accessibilityIdentifier("club.footer.facebook")
        }.frame(maxWidth: .infinity).padding(.vertical, 10)
    }
    private func footer(_ title: String, _ symbol: String, _ url: URL, id: String) -> some View {
        Link(destination: url) { Image(systemName: symbol).font(.title2).frame(width: 44, height: 44) }
            .accessibilityLabel(state.ui(title)).accessibilityIdentifier("club.footer." + id)
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
