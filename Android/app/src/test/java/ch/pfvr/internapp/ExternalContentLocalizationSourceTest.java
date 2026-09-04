package ch.pfvr.internapp;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class ExternalContentLocalizationSourceTest {
    private static String source() throws Exception {
        String relative = "src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates = new Path[]{
                Paths.get(relative),
                Paths.get("app", relative),
                Paths.get("Android", "app", relative)
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("MainActivity.java not found from " + System.getProperty("user.dir"));
    }

    @Test public void externalCalendarAndNewsContentBypassesDialectMapping() throws Exception {
        String source = source();
        assertTrue(source.contains("txtRaw(article.title"));
        assertTrue(source.contains("txtRaw(article.excerpt"));
        assertTrue(source.contains("txtRaw(event.title"));
        assertTrue(source.contains("txtRaw(event.description.trim()"));
        assertTrue(source.contains("txtRaw(ui(\"Ort\")+\"\\n\"+event.location"));
    }

    @Test public void externalTrainingTitleAndCanonicalPaymentDataStayUntouched() throws Exception {
        String source = source();
        assertTrue(source.contains("txtRaw(x[1],15,MUTED,true)"));
        assertTrue(source.contains("txtRaw(amountLine+\"\\n\"+CLUB_PAYEE+\"\\n\"+CLUB_IBAN+\"\\n\"+CLUB_PAYMENT_NOTE"));
    }
    @Test public void appOwnedWeatherFallbacksAreLocalizedBeforeRawRendering() throws Exception {
        String source = source();
        assertTrue(source.contains("ui(\"NÄCHSTES TRAINING\")"));
        assertTrue(source.contains("ui(\"Wetter wird geladen …\")"));
        assertTrue(source.contains("ui(\"Noch keine Prognose\")"));
        assertTrue(source.contains("ui(\"Gespeicherte Wetterdaten nicht lesbar\")"));
    }

}
