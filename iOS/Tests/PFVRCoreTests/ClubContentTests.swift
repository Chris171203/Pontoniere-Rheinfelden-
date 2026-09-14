import XCTest
@testable import PFVRCore

enum ClubTestData {
    static let about = "<h2>Allgemeine Informationen</h2>\n<p>Der Testverein wurde 1896 gegründet. Seitdem fährt er auf dem Rhein.</p>\n<p>Der Verein hat derzeit (2021) ca. 38 Mitglieder.</p>\n<p>Das gesellschaftliche Leben umfasst ein Essen nach dem Training.</p>\n<h2>Training</h2><p>In der Sommersaison (April-September) treffen wir uns am Montag- und Mittwochabend, von 18:30 Uhr bis 20:00 Uhr beim Depot der Pontoniere Rheinfelden.<br>\nWährend der Wintersaison (Oktober-März) treffen wir uns am Donnerstag um 19:30 Uhr bei der Schützenturnhalle. <a href='/kontakt/'>Kontakt</a></p>\n<h2>Sport</h2><p>Pontonier ist eine Sportart auf dem Wasser.</p><p>Das Schiff wird jeweils zu zweit gesteuert.</p>\n<h3>Weidling</h3><p><img width='758' height='90' src='/wp-content/uploads/weidling.jpg' alt='Weidling'>Ein Weidling wiegt 340 kg.</p>\n<h3>Boot</h3><p><img src='/wp-content/uploads/boot.jpg' alt='Boot'>Ein Boot wiegt 460 kg.</p>\n<p>– Durchfahrt (Ziel: Stangen nicht berühren)</p><img src='/wp-content/uploads/durchfahrt.jpg' alt='Durchfahrt'>\n<img src='/wp-content/uploads/weitere-durchfahrt.jpg' alt='Durchfahrt'>\n<p>– Landung auf höchstes Ziel (weiter oben anlanden)</p>\n<p>Beim Sektionswettfahren fahren wir zusammen.</p><img src='/wp-content/uploads/sektion.jpg'>"
    static let youth = "<p>Alte Kategorie 21 Jahre. Jeweils im Herbst lernen Kinder Knoten und Fahren.</p><p>Jeweils im Sommer veranstaltet der Verband ein Lager. Wer schon 18 Jahre alt ist, macht eine Prüfung. Weitere Informationen zum Lager.</p>"
    static let board = "<div class='wp-block-column'><h3>Präsident</h3><p>Test Person <a href='mailto:president@example.org'>E-Mail</a></p></div><div class='wp-block-column'><h3>Jungpontonier-Leiter</h3><p>JP Testkontakt <a href='mailto:youth@example.org'>E-Mail</a></p></div>"
    static let history = "<p>Die Geschichte bis 1996 steht im Jubiläumsbuch 1896 – 1996.</p><a href='/wp-content/uploads/book.pdf'>Jubiläumsbuch</a>"
    static func data(_ page: ClubPage = .about, html: String = about, modified: String = "2022-01-24T22:37:51") throws -> Data {
        try JSONSerialization.data(withJSONObject: [["slug": page.rawValue, "link": page.url.absoluteString,
            "title": ["rendered": "Vereinsquelle"], "content": ["rendered": html], "modified": modified]])
    }
}

final class ClubContentTests: XCTestCase {
    func testTrainingAndBoatsUseSourceWithoutPromotingOldMemberCounts() throws {
        let source = try ClubContentParser.parse(page: .about, data: ClubTestData.data())
        XCTAssertFalse(source.overviewIntro.contains("38 Mitglieder"))
        XCTAssertTrue(source.text.contains("38 Mitglieder"))
        let training = ClubTraining.summaries(source)
        XCTAssertEqual(training.count, 2)
        XCTAssertEqual(training[0].days, "Montag- und Mittwochabend")
        XCTAssertEqual(training[0].time, "18:30–20:00")
        XCTAssertEqual(training[0].location, "Depot der Pontoniere Rheinfelden")
        XCTAssertEqual(training[1].days, "Donnerstag")
        XCTAssertEqual(training[1].time, "19:30")
        XCTAssertEqual(training[1].location, "Schützenturnhalle Rheinfelden")
        let sections = ClubDestination.sport.sections(in: source)
        let boats = try XCTUnwrap(sections.first { $0.id == "boats" })
        XCTAssertEqual(boats.children.count, 2)
        XCTAssertTrue(boats.children[0].text.contains("340 kg"))
        XCTAssertTrue(boats.children[1].text.contains("460 kg"))
        XCTAssertFalse(boats.children[1].text.contains("Durchfahrt"))
        XCTAssertEqual(boats.children[0].photo?.url.lastPathComponent, "weidling.jpg")
        let sport = try XCTUnwrap(sections.first { $0.id == "sport" })
        XCTAssertEqual(sport.children.count, 3)
        XCTAssertEqual(sport.children[0].photo?.url.lastPathComponent, "durchfahrt.jpg")
        XCTAssertNil(sport.children[1].photo)
    }
    func testChangedSourceUpdatesSummaryAndUnknownHeadingsRetainText() throws {
        let source = try ClubContentParser.parse(page: .about, data: ClubTestData.data(html: ClubTestData.about.replacingOccurrences(of: "18:30", with: "18:45")))
        XCTAssertEqual(ClubTraining.summaries(source)[0].time, "18:45–20:00")
        let fallback = try ClubContentParser.parse(page: .about, data: ClubTestData.data(html: ClubTestData.about.replacingOccurrences(of: "<h2>Training</h2>", with: "<h2>Trainingsangebot</h2>")))
        XCTAssertTrue(fallback.sections.isEmpty)
        XCTAssertTrue(fallback.text.contains("19:30"))
        let incomplete = try ClubContentParser.parse(page: .about, data: ClubTestData.data(html: ClubTestData.about.replacingOccurrences(of: "am Donnerstag um 19:30 Uhr", with: "nach Absprache")))
        XCTAssertFalse(ClubTraining.summaries(incomplete)[1].fallback.isEmpty)
        XCTAssertTrue(ClubTraining.summaries(incomplete)[1].time.isEmpty)
    }
    func testYouthExcludesOldAgeRulesButKeepsOriginal() throws {
        let source = try ClubContentParser.parse(page: .youth, data: ClubTestData.data(.youth, html: ClubTestData.youth))
        XCTAssertTrue(source.text.contains("21 Jahre"))
        XCTAssertTrue(source.text.contains("18 Jahre"))
        XCTAssertFalse(source.sections.map(\.text).joined().contains("21 Jahre"))
        XCTAssertFalse(source.sections.map(\.text).joined().contains("18 Jahre"))
        XCTAssertTrue(source.sections.map(\.text).joined().contains("Knoten"))
    }
    func testBoardContactsStayWithTheirRoleAndHistoryLinksAreUnique() throws {
        let board = try ClubContentParser.parse(page: .board, data: ClubTestData.data(.board, html: ClubTestData.board))
        XCTAssertEqual(board.sections.count, 2)
        XCTAssertEqual(board.sections[1].title, "Jungpontonier-Leiter")
        XCTAssertEqual(board.sections[1].links.first?.url.absoluteString, "mailto:youth@example.org")
        let history = try ClubContentParser.parse(page: .history, data: ClubTestData.data(.history, html: ClubTestData.history + "<a href='/wp-content/uploads/book.pdf'>Download</a>"))
        XCTAssertEqual(history.milestones.count, 1)
        XCTAssertEqual(history.milestones[0].title, "1996")
        XCTAssertEqual(history.sections.count, 1)
    }
    func testPassiveSourceRejectsWrongPageAndUnsafeLinksAndPreservesOptionalDate() throws {
        let html = "<p>Kontakt <a href='javascript:alert(1)'>unsicher</a><a href='/verein/jungpontoniere/'>Nachwuchs</a></p><script>secretScript()</script><form><p>Formular</p></form>"
        let source = try ClubContentParser.parse(page: .contact, data: ClubTestData.data(.contact, html: html, modified: "invalid"))
        XCTAssertNil(source.modified)
        XCTAssertFalse(source.text.contains("secretScript"))
        XCTAssertFalse(source.text.contains("Formular"))
        XCTAssertEqual(source.links.count, 1)
        XCTAssertEqual(ClubDestination.matching(source.links[0].url), .youth)
        XCTAssertThrowsError(try ClubContentParser.parse(page: .board, data: ClubTestData.data(.contact, html: html)))
    }
    func testImagesUseSourceMediumAndRejectUntrustedURLs() throws {
        let html = ClubTestData.about.replacingOccurrences(of: "<h2>Training</h2>", with: "<figure><img src='/wp-content/uploads/large.jpg' srcset='/wp-content/uploads/tiny.jpg 150w, /wp-content/uploads/medium.jpg 768w, /wp-content/uploads/large.jpg 2048w'><figcaption>Originalfoto</figcaption></figure><h2>Training</h2>")
        let source = try ClubContentParser.parse(page: .about, data: ClubTestData.data(html: html))
        XCTAssertEqual(source.hero?.url.lastPathComponent, "medium.jpg")
        XCTAssertEqual(source.hero?.caption, "Originalfoto")
        for value in ["https://evil.example/a.jpg","http://www.pfvr.ch/wp-content/uploads/a.jpg","https://www.pfvr.ch/wp-content/uploads/a.svg","https://user@www.pfvr.ch/wp-content/uploads/a.jpg","https://www.pfvr.ch/wp-content/uploads/../a.jpg"] {
            XCTAssertFalse(ClubPhoto.allowed(try XCTUnwrap(URL(string: value))), value)
        }
    }
    func testClubLayoutMigratesOldDefaultAndPreservesCustomOrder() {
        let previous = ["club_about","club_news","club_program","club_board","club_history","club_depot","club_phone","club_email","club_contact","club_instagram","club_facebook"]
        XCTAssertEqual(TileLayoutStore.normalizeOrder(area: .club, requested: previous), TileLayoutStore.specs(.club).map(\.id))
        XCTAssertEqual(Array(TileLayoutStore.normalizeOrder(area: .club, requested: ["club_history","club_about","club_phone"]).prefix(2)), ["club_history","club_about"])
        XCTAssertTrue(TileLayoutStore.specs(.club).allSatisfy { $0.width == .wide })
        XCTAssertTrue(TileLayoutStore.sanitizeHidden(area: .club, requested: ["club_phone","club_depot","club_program","club_facebook"]).isEmpty)
    }
    func testSwissGermanCoversNewNavigationAndSourceWeekdays() {
        for (de, gsw) in [("Verein entdecken","Verein entdecke"),("Boote & Sport","Boot & Sport"),("Montag- und Mittwochabend","Mäntig- und Mittwuchaabig"),("Donnerstag","Dunnschtig")] {
            XCTAssertEqual(Language.translate(de, mode: .swissGerman), gsw)
            XCTAssertEqual(Language.translate(de, mode: .german), de)
        }
    }
}
