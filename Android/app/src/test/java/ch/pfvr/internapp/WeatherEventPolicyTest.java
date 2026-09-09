package ch.pfvr.internapp;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.Test;

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
        assertArrayEquals(new int[]{8,13,18},WeatherEventPolicy.targetHours(false,start,end));
    }

    @Test public void allDayEventUsesMorningNoonEvening(){
        ZonedDateTime start=ZonedDateTime.of(2026,9,20,0,0,0,0,ZONE);
        ZonedDateTime end=start.plusDays(1);
        assertTrue(WeatherEventPolicy.usesThreePoints(true,start,end));
        assertArrayEquals(new int[]{6,12,18},WeatherEventPolicy.targetHours(true,start,end));
    }
}
