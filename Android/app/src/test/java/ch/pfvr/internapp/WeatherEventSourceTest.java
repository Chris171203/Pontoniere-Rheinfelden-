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
        assertTrue(activity.contains("\"Wetter zu den nächsten Terminen\":\"Wetter zum nächsten Termin\""));
        int start=activity.indexOf("private List<TrainingSlot> nextWeatherSlots");
        int end=activity.indexOf("private TrainingSlot weatherSlotFromEvent",start);
        assertTrue(start>=0&&end>start);
        String selector=activity.substring(start,end);
        assertTrue(selector.contains("for(Event event:events)"));
        assertTrue(selector.contains("isCancelledEvent(event)"));
        assertTrue(selector.contains("eventEnd(event).isAfter(now)"));
        assertFalse(selector.contains("TrainingMatcher"));
        assertTrue(selector.contains("!regular.fromCalendar"));
        assertTrue(activity.contains("group.addView(sharedWeatherCard(slots),params)"));
        assertTrue(activity.contains("new TrainingSlot(start,end,true,event.title,event.allDay)"));
    }

    @Test public void longAndAllDayEventsRenderThreeForecastPoints() throws Exception {
        String activity=source();
        assertTrue(activity.contains("WeatherEventPolicy.merge(intervals)"));
        assertTrue(activity.contains("WeatherEventPolicy.sharedTargetHours(intervals)"));
        assertTrue(activity.contains("WeatherDaily.slots(hours,slot.start.toLocalDate(),targets)"));
        assertTrue(activity.contains("weatherDaySlotView(values.get(index))"));
        assertTrue(activity.contains("ui(\"NÄCHSTER TERMIN\")"));
        assertTrue(activity.contains("Für diesen Terminzeitraum liegen noch keine Stundenwerte vor."));
        assertFalse(activity.contains("tileGroup(\"Trainingswetter\""));
    }
}
