package ch.pfvr.internapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class WeatherEventSourceTest {
    private static String source() throws Exception {
        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";
        Path[] candidates=new Path[]{Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};
        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate),StandardCharsets.UTF_8);
        throw new IllegalStateException("MainActivity.java not found from "+System.getProperty("user.dir"));
    }

    @Test public void weatherUsesNextGeneralClubEventBeforeRegularTraining() throws Exception {
        String activity=source();
        assertTrue(activity.contains("tileGroup(\"Wetter zum nächsten Termin\",\"Prognose für den nächsten relevanten Vereinsanlass\")"));
        int start=activity.indexOf("private TrainingSlot nextCalendarWeatherSlot");
        int end=activity.indexOf("private TrainingSlot weatherSlotFromEvent",start);
        assertTrue(start>=0&&end>start);
        String selector=activity.substring(start,end);
        assertTrue(selector.contains("for(Event event:events)"));
        assertTrue(selector.contains("isCancelledEvent(event)"));
        assertTrue(selector.contains("eventEnd(event).isAfter(now)"));
        assertFalse(selector.contains("TrainingMatcher"));
        assertTrue(activity.contains("return !calendar.start.isAfter(regular.start)?calendar:regular;"));
        assertTrue(activity.contains("new TrainingSlot(start,end,true,event.title,event.allDay)"));
    }

    @Test public void longAndAllDayEventsRenderThreeForecastPoints() throws Exception {
        String activity=source();
        assertTrue(activity.contains("WeatherEventPolicy.usesThreePoints(slot.allDay,slot.start,slot.end)"));
        assertTrue(activity.contains("WeatherEventPolicy.targetHours(slot.allDay,slot.start,slot.end)"));
        assertTrue(activity.contains("WeatherDaily.slots(hours,slot.start.toLocalDate(),targets)"));
        assertTrue(activity.contains("weatherDaySlotView(values.get(index))"));
        assertTrue(activity.contains("ui(\"NÄCHSTER TERMIN\")"));
        assertTrue(activity.contains("Für diesen Terminzeitraum liegen noch keine Stundenwerte vor."));
        assertFalse(activity.contains("tileGroup(\"Trainingswetter\""));
    }
}
