package ch.pfvr.internapp;

import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClubContentParserTest {
    static final String ABOUT="""
        <h2>Allgemeine Informationen</h2>
        <p>Der Testverein wurde 1896 gegründet. Seitdem fährt er auf dem Rhein.</p>
        <p>Der Verein hat derzeit (2021) ca. 38 Mitglieder.</p>
        <p>Das gesellschaftliche Leben umfasst ein Essen nach dem Training.</p>
        <h2>Training</h2><p>In der Sommersaison treffen wir uns beim Depot der Pontoniere Rheinfelden um 18:30 Uhr.<br>
        Während der Wintersaison treffen wir uns bei der Schützenturnhalle um 19:30 Uhr. <a href='/kontakt/'>Kontakt</a></p>
        <h2>Sport</h2><p>Pontonier ist eine Sportart auf dem Wasser.</p><p>Das Schiff wird jeweils zu zweit gesteuert.</p>
        <h3>Weidling</h3><p><img width='758' height='90' src='/wp-content/uploads/weidling.jpg' alt='Weidling'>Ein Weidling wiegt 340 kg.</p>
        <h3>Boot</h3><p><img src='/wp-content/uploads/boot.jpg' alt='Boot'>Ein Boot wiegt 460 kg.</p>
        <p>– Durchfahrt (Ziel: Stangen nicht berühren)</p><img src='/wp-content/uploads/durchfahrt.jpg' alt='Durchfahrt'>
        <img src='/wp-content/uploads/weitere-durchfahrt.jpg' alt='Durchfahrt'>
        <p>– Landung auf höchstes Ziel (weiter oben anlanden)</p>
        <p>Beim Sektionswettfahren fahren wir zusammen.</p><img src='/wp-content/uploads/sektion.jpg'>
        """;
    static ClubPageRepository.Content parse(ClubPageRepository.Page page,String html)throws Exception {
        return ClubPageRepository.parse(page,ClubPageRepositoryTest.response(page,html));
    }
    static ClubContentParser.Section section(ClubPageRepository.Content c,String id){return c.layout().sections().stream().filter(s->s.id().equals(id)).findFirst().orElseThrow();}

    @Test public void separatesSeasonLocationsBoatsAndManoeuvresWithoutPromotingOldCounts()throws Exception {
        var c=parse(ClubPageRepository.Page.ABOUT,ABOUT);
        assertFalse(c.preview().contains("38 Mitglieder"));assertTrue(c.html().contains("38 Mitglieder"));
        var training=section(c,"training").children();assertEquals(2,training.size());
        assertTrue(training.get(0).html().contains("18:30"));assertFalse(training.get(0).html().contains("19:30"));
        assertTrue(training.get(1).html().contains("https://www.pfvr.ch/kontakt/"));
        assertEquals("Depot der Pontoniere Rheinfelden",training.get(0).location());
        assertEquals("Schützenturnhalle Rheinfelden",training.get(1).location());
        var boats=section(c,"boats").children();assertEquals(2,boats.size());
        assertTrue(boats.get(0).html().contains("340 kg"));assertTrue(boats.get(1).html().contains("460 kg"));
        assertFalse(boats.get(1).html().contains("Durchfahrt"));assertTrue(boats.get(0).photo().url().endsWith("weidling.jpg"));
        var sport=section(c,"sport").children();assertEquals(3,sport.size());
        assertTrue(sport.get(0).photo().url().endsWith("/durchfahrt.jpg"));assertNull(sport.get(1).photo());
        assertTrue(sport.get(2).photo().url().endsWith("/sektion.jpg"));
    }

    @Test public void changedHeadingsRetainNativeSourceAndUnknownSeasonRetainsWholeTraining()throws Exception {
        var c=parse(ClubPageRepository.Page.ABOUT,ABOUT.replace("<h2>Training</h2>","<h2>Trainingsangebot</h2>"));
        assertTrue(c.layout().sections().isEmpty());assertTrue(c.html().contains("19:30"));
        c=parse(ClubPageRepository.Page.ABOUT,ABOUT.replace("Während der Wintersaison","Im Winter"));
        assertTrue(section(c,"training").children().isEmpty());assertTrue(section(c,"training").html().contains("19:30"));
    }

    @Test public void youthExcerptsExcludeDatedAgeRulesAndRetainLinks()throws Exception {
        var c=parse(ClubPageRepository.Page.YOUTH,"""
            <h2>Allgemeine Informationen</h2><p>Eine alte Kategorie ab 21 Jahren.</p>
            <p>Eine alte Regel ab 15 Jahren. Jeweils im Herbst üben Kinder Knoten und Fahren. Wer besteht, bekommt ein Abzeichen.</p>
            <h2>JP – Lager</h2><p>Jeweils im Sommer veranstaltet der Verband ein Lager. Dort treffen sich Kinder.
            Wer schon 18 Jahre alt ist, macht eine Prüfung. Weitere Informationen <a href='https://www.pontonier.ch/'>hier</a>.</p>
            """);
        assertTrue(c.html().contains("21 Jahre"));assertTrue(c.html().contains("18 Jahre"));
        String excerpts=c.layout().sections().toString();
        assertFalse(excerpts.contains("21 Jahre"));assertFalse(excerpts.contains("15 Jahre"));assertFalse(excerpts.contains("18 Jahre"));
        assertTrue(excerpts.contains("Knoten"));assertTrue(excerpts.contains("https://www.pontonier.ch/"));
    }

    @Test public void boardPairsEachRoleWithItsOwnPhotoAndDecodedContact()throws Exception {
        var c=parse(ClubPageRepository.Page.BOARD,"""
            <div class='wp-block-columns'><div class='wp-block-column'><h3>Präsident</h3>
            <p>Test Person<br><a href='&#109;ailto:test@example.org'>E-Mail</a><a href='tel:+41000000000'>Telefon</a></p>
            <img src='/wp-content/uploads/person.jpg'></div>
            <div class='wp-block-column'><h3>Jungpontonier-Leiter</h3><p>Andere Testperson</p><p>Bild folgt…</p></div></div>
            <p>Archiv mit weiteren Personen</p>
            """);
        var cards=c.layout().sections();assertEquals(2,cards.size());
        assertTrue(cards.get(0).html().contains("mailto:test@example.org"));assertNotNull(cards.get(0).photo());
        assertNull(cards.get(1).photo());assertFalse(cards.get(1).html().contains("Archiv"));assertFalse(cards.get(1).html().contains("Bild folgt"));
    }

    @Test public void onlyEvidenceCreatesMilestonesAndPdfLinksAreDeduplicated()throws Exception {
        var c=parse(ClubPageRepository.Page.HISTORY,"""
            <p>Die Geschichte bis 1996 steht im Jubiläumsbuch 1896 – 1996.</p><p>Wir arbeiten an einer Erweiterung.</p>
            <a href='/wp-content/uploads/book.pdf'>Buch</a><a href='/wp-content/uploads/book.pdf'>Download</a>
            """);
        assertEquals(1,c.layout().milestones().size());assertEquals("1996",c.layout().milestones().get(0).title());
        assertEquals(1,c.layout().sections().size());assertTrue(c.html().contains("Wir arbeiten"));
        c=parse(ClubPageRepository.Page.HISTORY,"<p>Die Erweiterung ist geplant.</p>");assertTrue(c.layout().milestones().isEmpty());
    }

    @Test public void photosUseProvidedMediumSizeCaptionAndRejectOtherHostsOrSchemes()throws Exception {
        String image="<figure><img src='/wp-content/uploads/large.jpg' srcset='/wp-content/uploads/tiny.jpg 150w, /wp-content/uploads/medium.jpg 768w, /wp-content/uploads/large.jpg 2048w'><figcaption>Originalfoto 2021</figcaption></figure>";
        var c=parse(ClubPageRepository.Page.ABOUT,ABOUT.replace("<h2>Training</h2>",image+"<h2>Training</h2>"));
        assertTrue(c.layout().hero().url().endsWith("medium.jpg"));assertEquals("Originalfoto 2021",c.layout().hero().caption());
        for(String value:new String[]{"https://evil.example/a.jpg","http://www.pfvr.ch/wp-content/uploads/a.jpg","https://www.pfvr.ch/wp-content/uploads/a.svg","https://user@www.pfvr.ch/wp-content/uploads/a.jpg","https://www.pfvr.ch/wp-content/uploads/../a.jpg"})assertFalse(value,ClubContentParser.allowedPhoto(value));
    }

    @Test public void sourceModifiedDateIsOptionalAndSeparateFromRetrieval()throws Exception {
        var page=ClubPageRepository.Page.ABOUT;String raw=ClubPageRepositoryTest.response(page,ABOUT);
        var rows=new JSONArray(raw);rows.getJSONObject(0).put("modified","2022-01-24T22:37:51");
        assertEquals("2022-01-24",ClubPageRepository.parse(page,rows.toString()).modified());
        rows.getJSONObject(0).put("modified","invalid");assertEquals("",ClubPageRepository.parse(page,rows.toString()).modified());
        assertEquals(ClubPageRepository.Page.YOUTH,ClubPageRepository.pageForUrl("https://www.pfvr.ch/verein/jungpontoniere/"));
    }
}
