package ch.pfvr.internapp;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

public class FirstUseGateSourceTest {
    @Test public void firstUseGateIsAttachedToActivityContent() throws Exception {
        String source = Files.readString(
                Path.of("app/src/main/java/ch/pfvr/internapp/MainActivity.java"),
                StandardCharsets.UTF_8
        );
        int start = source.indexOf("private void showFirstUseGate()");
        int end = source.indexOf("private void scheduleBackgroundRefresh()", start);
        assertTrue(start >= 0 && end > start);
        String gate = source.substring(start, end);
        assertTrue(gate.contains("setContentView(root);"));
    }
}
