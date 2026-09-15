package ch.pfvr.internapp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.assertEquals;

public class WeatherEventPolicyTest {
    private static final ZoneId ZONE=ZoneId.of("Europe/Zurich");

    @Test public void shortTimedEventKeepsCompactIntervalWeather(){
        ZonedDateTime start=ZonedDateTime.of(2026,9,14,18,30,0,0,ZONE);
        ZonedDateTime end=ZonedDateTime.of(2026,9,14,20,0,0,0,ZONE);
        assertFalse(WeatherEventPolicy.usesThreePoints(false,start,end));
        assertArrayEquals(new int[0],WeatherEventPolicy.targetHours(false,start,end));
    }

    @Test public void longTimedEventUsesStartMiddleAndEndHours(){
        ZonedDateTime start=ZonedDateTime.of(2026,9,12,8,0,0,0,ZONE);
        ZonedDateTime end=ZonedDateTime.of(2026,9,12,18,0,0,0,ZONE);
        assertTrue(WeatherEventPolicy.usesThreePoints(false,start,end));
        assertArrayEquals(new int[]{8,13,17},WeatherEventPolicy.targetHours(false,start,end));
    }

    @Test public void allDayEventUsesMorningNoonEvening(){
        ZonedDateTime start=ZonedDateTime.of(2026,9,20,0,0,0,0,ZONE);
        ZonedDateTime end=start.plusDays(1);
        assertTrue(WeatherEventPolicy.usesThreePoints(true,start,end));
        assertArrayEquals(new int[]{6,12,18},WeatherEventPolicy.targetHours(true,start,end));
    }
    @Test public void sharedIntervalsDeduplicateOverlapAndExcludeGaps(){
        ZonedDateTime day=ZonedDateTime.of(2026,9,15,0,0,0,0,ZONE);
        var noon=new WeatherEventPolicy.Interval(day.plusHours(12).plusMinutes(30),day.plusHours(14),false);
        var evening=new WeatherEventPolicy.Interval(day.plusHours(18).plusMinutes(30),day.plusHours(20),false);
        var overlap=new WeatherEventPolicy.Interval(day.plusHours(19),day.plusHours(21),false);
        var merged=WeatherEventPolicy.merge(Arrays.asList(evening,noon,evening,overlap));
        assertEquals(2,merged.size());
        assertArrayEquals(new int[]{12,18},WeatherEventPolicy.sharedTargetHours(merged));
        List<Integer> selected=new ArrayList<>();
        for(int h=0;h<24;h++){final ZonedDateTime hour=day.plusHours(h);if(merged.stream().anyMatch(i->i.containsHour(hour)))selected.add(h);}
        assertEquals(Arrays.asList(12,13,18,19,20),selected);
        assertArrayEquals(new int[]{18},WeatherEventPolicy.sharedTargetHours(Arrays.asList(evening,evening)));
        var all=new WeatherEventPolicy.Interval(day,day.plusDays(1),true);
        assertArrayEquals(new int[]{6,12,18},WeatherEventPolicy.sharedTargetHours(Arrays.asList(all,evening)));
    }
}
