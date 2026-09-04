package ch.pfvr.internapp;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

public class FirstUseGateSourceTest {
    private static String source() throws Exception {
        byte[] bytes = Files.readAllBytes(
                Paths.get("app/src/main/java/ch/pfvr/internapp/MainActivity.java")
        );
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @Test public void firstUseGateIsAttachedToActivityContent() throws Exception {
        String source = source();
        int start = source.indexOf("private void showFirstUseGate()");
        int end = source.indexOf("private void scheduleBackgroundRefresh()", start);
        assertTrue(start >= 0 && end > start);
        String gate = source.substring(start, end);
        assertTrue(gate.contains("setContentView(root);"));
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
}
