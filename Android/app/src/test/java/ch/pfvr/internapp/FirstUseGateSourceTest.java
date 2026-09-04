package ch.pfvr.internapp;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class FirstUseGateSourceTest {
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

    private static String gate(String source) {
        int start = source.indexOf("private void showFirstUseGate()");
        int end = source.indexOf("private void scheduleBackgroundRefresh()", start);
        assertTrue(start >= 0 && end > start);
        return source.substring(start, end);
    }

    @Test public void firstUseGateIsAttachedToActivityContent() throws Exception {
        assertTrue(gate(source()).contains("setContentView(root);"));
    }

    @Test public void lockedStartupRoutesToFirstUseGate() throws Exception {
        String source = source();
        int start = source.indexOf("@Override protected void onCreate(Bundle state)");
        int end = source.indexOf("private void startUnlockedApp()", start);
        assertTrue(start >= 0 && end > start);
        String startup = source.substring(start, end);
        assertTrue(startup.contains("if(!prefs.getBoolean(PREF_ACCESS_UNLOCKED,false))"));
        assertTrue(startup.contains("showFirstUseGate();"));
    }

    @Test public void publicLandingOffersDiscoveryAndSocialLinksBeforeUnlock() throws Exception {
        String source = source();
        String gate = gate(source);
        assertTrue(source.contains("https://www.pfvr.ch/schnuppertraining-mitglied-werden-formulare/"));
        assertTrue(source.contains("https://www.facebook.com/PontoniereRheinfelden"));
        assertTrue(source.contains("https://www.instagram.com/pontoniererheinfelden"));
        assertTrue(gate.contains("Schnuppertraining & Mitglied werden"));
        assertTrue(gate.contains("external(JOIN_INFO)"));
        assertTrue(gate.contains("external(INSTAGRAM)"));
        assertTrue(gate.contains("external(FACEBOOK)"));
        assertTrue(gate.contains("setUiLanguage(UiLanguage.SWISS_GERMAN)"));
    }
}
