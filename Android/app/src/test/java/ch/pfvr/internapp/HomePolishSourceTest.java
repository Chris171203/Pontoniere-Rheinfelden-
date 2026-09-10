package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class HomePolishSourceTest {
    private static String source() throws Exception {
        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates={Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};
        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate),StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found");
    }

    @Test public void homeRemovesPrototypeStyleExplanatoryCopy() throws Exception {
        String activity=source();
        assertFalse(activity.contains("hero.addView(txt(\"Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit."));
        assertTrue(activity.contains("tileGroup(\"Wetter zum nächsten Termin\",null)"));
        assertTrue(activity.contains("tileGroup(\"3-Tage-Wetter\",null)"));
        assertTrue(activity.contains("tileGroup(\"Rhein aktuell\",null)"));
        assertTrue(activity.contains("tileGroup(\"Als Nächstes\",null)"));
        assertTrue(activity.contains("tileGroup(\"Aktuell vom Verein\",newsStatus())"));
        assertFalse(activity.contains("c.addView(txt(x[0],11,WATER,true))"));
        assertFalse(activity.contains("c.addView(txt(\"NÄCHSTER TERMIN\",11,WATER,true))"));
        assertTrue(activity.contains("BAFU-Rohdaten, ungeprüft. Schifffahrtslage nur zur Orientierung"));
    }

    @Test public void homeLiveRefreshIsSmallAnimatedAndForcesLiveReload() throws Exception {
        String activity=source();
        assertTrue(activity.contains("addHomeLiveRefreshAction(group)"));
        assertTrue(activity.contains("txtRaw(\"↻\",22,WATER,true)"));
        assertTrue(activity.contains("setContentDescription(ui(\"Aktualisieren\"))"));
        assertTrue(activity.contains("rotation(360f).setDuration(420L)"));
        assertTrue(activity.contains("refreshLive(true)"));
        assertTrue(activity.contains("new LinearLayout.LayoutParams(dp(44),dp(44))"));
    }

    @Test public void weatherProvenanceIsCompactButCacheAgeRemainsVisible() throws Exception {
        String activity=source();
        assertTrue(activity.contains("return \"MeteoSwiss/Open-Meteo\""));
        assertTrue(activity.contains("String label=compactWeatherSource(source)"));
        assertTrue(activity.contains("min>90?\" · Cache \"+(min/60)+\" h\""));
    }

    @Test public void cashRemovesRedundantHeroAndCartCopy() throws Exception {
        String activity=source();
        assertFalse(activity.contains("hero.addView(txt(\"Artikel für dich, Kinder oder die ganze Runde zusammenstellen"));
        assertTrue(activity.contains("tileGroup(\"Warenkorb\",null)"));
    }
}
