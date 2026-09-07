package ch.pfvr.internapp;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class InternalAppViewSourceTest {
    private static String source() throws Exception {
        String relative = "src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates = new Path[]{Paths.get(relative), Paths.get("app", relative), Paths.get("Android", "app", relative)};
        for (Path candidate : candidates) if (Files.isRegularFile(candidate)) return new String(Files.readAllBytes(candidate), StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found from " + System.getProperty("user.dir"));
    }
    private static String internalSection(String source) {
        int start=source.indexOf("private View internal()"),end=source.indexOf("private View internalMissing()",start);
        assertTrue(start>=0&&end>start);
        return source.substring(start,end);
    }
    @Test public void savedAppViewStaysInvisibleUntilProjectionExists() throws Exception {
        String section=internalSection(source());
        assertTrue(section.contains("if(appView)hideInternalWebForAppView(web)"));
        assertTrue(section.contains("onPageStarted(WebView v,String u,Bitmap icon)"));
        assertTrue(section.contains("internalSkin(v);revealInternalAppViewWhenReady(v,0)"));
        assertTrue(section.contains("document.querySelector('.pfvr-attendance-mobile .pfvr-attendance-matrix')"));
    }
    @Test public void revealIsBoundedAndOriginalModeShowsImmediately() throws Exception {
        String section=internalSection(source());
        assertTrue(section.contains("if(attempt>=24){showInternalWeb(web);return;}"));
        assertTrue(section.contains("postDelayed(()->revealInternalAppViewWhenReady(web,attempt+1),100L)"));
        assertTrue(section.contains("if(next)hideInternalWebForAppView(web);else showInternalWeb(web)"));
        assertTrue(section.contains("else showInternalWeb(v)"));
    }
}
