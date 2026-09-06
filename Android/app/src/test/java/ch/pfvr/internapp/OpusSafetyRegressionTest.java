package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class OpusSafetyRegressionTest {
    private static String source() throws Exception {
        String relative = "src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates = {
                Paths.get(relative),
                Paths.get("app", relative),
                Paths.get("Android", "app", relative)
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
            }
        }
        throw new IllegalStateException("MainActivity.java not found");
    }

    @Test
    public void currentNavigationRequiresFreshData() throws Exception {
        String source = source();
        assertTrue(source.contains("fromCurrentBaselGaugeCm"));
        assertTrue(source.contains("dd.MM. HH:mm"));
        assertTrue(source.contains("riverLevelColor(HydroStation station"));
    }

    @Test
    public void webFlowsAreHardened() throws Exception {
        String source = source();
        assertTrue(source.contains("AppLinkPolicy.mayOpenExternally(uri.getScheme())"));
        assertFalse(source.contains("window.__pfvrBaseInternalUrl="));
    }
}
