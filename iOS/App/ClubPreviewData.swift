#if DEBUG
import Foundation
import PFVRCore

/// Synthetic source data only; never used outside explicitly enabled UI tests.
enum ClubPreviewData {
    static func pages(now: Date) -> [ClubPage: Loaded<ClubContent>] {
        let html: [ClubPage: String] = [
            .about: "<h2>Allgemeine Informationen</h2>\n<p>Der Testverein wurde 1896 gegründet. Seitdem fährt er auf dem Rhein.</p>\n<p>Der Verein hat derzeit (2021) ca. 38 Mitglieder.</p>\n<p>Das gesellschaftliche Leben umfasst ein Essen nach dem Training.</p>\n<h2>Training</h2><p>In der Sommersaison (April-September) treffen wir uns am Montag- und Mittwochabend, von 18:30 Uhr bis 20:00 Uhr beim Depot der Pontoniere Rheinfelden.<br>\nWährend der Wintersaison (Oktober-März) treffen wir uns am Donnerstag um 19:30 Uhr bei der Schützenturnhalle. <a href='/kontakt/'>Kontakt</a></p>\n<h2>Sport</h2><p>Pontonier ist eine Sportart auf dem Wasser.</p><p>Das Schiff wird jeweils zu zweit gesteuert.</p>\n<h3>Weidling</h3><p>Ein Weidling wiegt 340 kg.</p>\n<h3>Boot</h3><p>Ein Boot wiegt 460 kg.</p>\n<p>– Durchfahrt (Ziel: Stangen nicht berühren)</p>\n\n<p>– Landung auf höchstes Ziel (weiter oben anlanden)</p>\n<p>Beim Sektionswettfahren fahren wir zusammen.</p>",
            .youth: "<p>Alte Kategorie 21 Jahre. Jeweils im Herbst lernen Kinder Knoten und Fahren.</p><p>Jeweils im Sommer veranstaltet der Verband ein Lager. Wer schon 18 Jahre alt ist, macht eine Prüfung. Weitere Informationen zum Lager.</p>",
            .board: "<div class='wp-block-column'><h3>Präsident</h3><p>Test Person <a href='mailto:president@example.org'>E-Mail</a></p></div><div class='wp-block-column'><h3>Jungpontonier-Leiter</h3><p>JP Testkontakt <a href='mailto:youth@example.org'>E-Mail</a></p></div>",
            .history: "<p>Die Geschichte bis 1996 steht im Jubiläumsbuch 1896 – 1996.</p><a href='/wp-content/uploads/book.pdf'>Jubiläumsbuch</a>",
            .contact: "<p>Öffentliche Vereinsadresse</p><p><a href='mailto:info@pfvr.ch'>E-Mail</a></p>"
        ]
        return Dictionary(uniqueKeysWithValues: html.compactMap { page, source in
            let row: [String: Any] = ["slug": page.rawValue, "link": page.url.absoluteString,
                "title": ["rendered": "Vereinsquelle"], "content": ["rendered": source], "modified": "2022-01-24T22:37:51"]
            guard let data = try? JSONSerialization.data(withJSONObject: [row]),
                  let content = try? ClubContentParser.parse(page: page, data: data) else { return nil }
            return (page, Loaded(value: content, metadata: CacheMetadata(source: "pfvr.ch · Testdaten", updatedAt: now)))
        })
    }
}
#endif
