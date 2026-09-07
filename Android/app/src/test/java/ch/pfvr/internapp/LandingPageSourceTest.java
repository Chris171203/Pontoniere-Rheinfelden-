package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class LandingPageSourceTest {
    private static String source(String relative) throws Exception {
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
        throw new IllegalStateException(relative + " not found from " + System.getProperty("user.dir"));
    }

    private static String landing(String activity) {
        int start = activity.indexOf("private void showFirstUseGate()");
        int end = activity.indexOf("private void scheduleBackgroundRefresh()", start);
        assertTrue(start >= 0 && end > start);
        return activity.substring(start, end);
    }

    private static String unlockedSource(String activity) {
        int start = activity.indexOf("private void showFirstUseGate()");
        int end = activity.indexOf("private void scheduleBackgroundRefresh()", start);
        assertTrue(start >= 0 && end > start);
        return activity.substring(0, start) + activity.substring(end);
    }

    @Test public void firstUseLandingOffersPublicDestinationsBeforeUnlock() throws Exception {
        String activity = source("src/main/java/ch/pfvr/internapp/MainActivity.java");
        String landing = landing(activity);
        assertTrue(landing.contains("Schnuppertraining ist auch vor der Mitgliedschaft möglich."));
        assertTrue(landing.contains("external(PublicLinks.JOIN)"));
        assertTrue(landing.contains("external(PublicLinks.FACEBOOK)"));
        assertTrue(landing.contains("external(PublicLinks.INSTAGRAM)"));
    }

    @Test public void unlockedAppKeepsSocialButNotJoinLinksReachable() throws Exception {
        String activity = source("src/main/java/ch/pfvr/internapp/MainActivity.java");
        String unlocked = unlockedSource(activity);
        assertFalse(unlocked.contains("joinInfo.setOnClickListener(v->external(PublicLinks.JOIN))"));
        assertFalse(unlocked.contains("case \"club_join\":return clubActionTile"));
        assertTrue(unlocked.contains("case \"club_instagram\":return clubActionTile"));
        assertTrue(unlocked.contains("case \"club_facebook\":return clubActionTile"));
    }
}
