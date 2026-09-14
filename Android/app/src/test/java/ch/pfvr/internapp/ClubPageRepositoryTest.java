package ch.pfvr.internapp;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClubPageRepositoryTest {
    static String response(ClubPageRepository.Page page,String html){
        return new JSONArray().put(new JSONObject().put("slug",page.slug).put("link",page.url)
                .put("title",new JSONObject().put("rendered",page.label))
                .put("content",new JSONObject().put("rendered",html))).toString();
    }

    @Test public void preservesSourceTextHeadingsAndWorkingContactLinks() throws Exception {
        var page=ClubPageRepository.Page.BOARD;
        var content=ClubPageRepository.parse(page,response(page,"<h3>Präsident</h3><p>Test Person<br><a href='mailto:test@example.org'>test@example.org</a> <a href='tel:+41000000000'>Telefon</a></p>"));
        assertTrue(content.html().contains("<h3>Präsident</h3>"));
        assertTrue(content.html().contains("mailto:test@example.org"));
        assertTrue(content.html().contains("tel:+41000000000"));
        assertTrue(content.preview().contains("Test Person"));
    }

    @Test public void removesActiveContentFormsAndUnsafeLinksWithoutChangingBodyText() throws Exception {
        var page=ClubPageRepository.Page.CONTACT;
        var content=ClubPageRepository.parse(page,response(page,"<p>Kontakt &amp; Hilfe <a href='/geschichte/'>Buch</a></p><script>secret()</script><form><p>Send button</p><input></form><iframe src='https://other.example'></iframe><a href='javascript:alert(1)' onclick='alert(2)'>unsafe</a><img src='tracker'>"));
        assertTrue(content.html().contains("https://www.pfvr.ch/geschichte/"));
        for(String forbidden:new String[]{"secret()","Send button","<form","<iframe","javascript:","onclick","<img"})assertFalse(content.html().contains(forbidden));
        assertTrue(content.preview().contains("Kontakt & Hilfe"));
    }

    @Test public void rejectsWrongEmptyAndBrokenResponsesRatherThanReplacingCache() throws Exception {
        var page=ClubPageRepository.Page.ABOUT;
        for(String raw:new String[]{"[]","<html>error</html>",response(ClubPageRepository.Page.HISTORY,"<p>Wrong page</p>"),response(page,"<script>only()</script>")}){
            try{ClubPageRepository.parse(page,raw);fail(raw);}catch(Exception expected){}
        }
    }

    @Test public void routesCanonicalPagesAndLeavesDocumentsForExternalViewer(){
        assertEquals(ClubPageRepository.Page.HISTORY,ClubPageRepository.pageForUrl("https://www.pfvr.ch/geschichte/#buch"));
        assertNull(ClubPageRepository.pageForUrl("https://www.pfvr.ch/wp-content/uploads/history.pdf"));
        assertNull(ClubPageRepository.pageForUrl("https://www.pfvr.ch.evil.example/geschichte/"));
        assertNull(ClubPageRepository.pageForUrl("https://evil@www.pfvr.ch/geschichte/"));
    }
}
