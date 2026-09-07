package ch.pfvr.internapp;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WeatherDailyTest {
    @Test public void aggregatesThreeCalendarDaysFromHourlyValues(){
        LocalDate start=LocalDate.of(2026,9,7);
        List<WeatherDaily.Hour> hours=List.of(
                hour(start,6,10,10,0.0,5,8,0.0,1),
                hour(start,14,25,30,0.2,12,20,6.2,2),
                hour(start,18,20,60,1.3,9,15,1.0,61),
                hour(start.plusDays(1),14,22,45,0.8,18,32,4.1,80),
                hour(start.plusDays(2),14,19,5,0.0,7,11,2.5,0),
                hour(start.plusDays(3),14,99,100,9.9,99,99,12.0,95)
        );

        List<WeatherDaily.Summary> summaries=WeatherDaily.summarize(hours,start,3);
        assertEquals(3,summaries.size());

        WeatherDaily.Summary today=summaries.get(0);
        assertTrue(today.hasData());
        assertEquals(10.0,today.minTemperature,0.001);
        assertEquals(25.0,today.maxTemperature,0.001);
        assertEquals(60,today.precipitationProbabilityMax);
        assertEquals(1.5,today.precipitationSum,0.001);
        assertEquals(12.0,today.windMax,0.001);
        assertEquals(20.0,today.gustMax,0.001);
        assertEquals(6.2,today.uvMax,0.001);
        assertEquals(2,today.weatherCode); // representative condition nearest 14:00

        assertEquals(80,summaries.get(1).weatherCode);
        assertEquals(0,summaries.get(2).weatherCode);
    }

    @Test public void returnsEmptySummariesForMissingDays(){
        LocalDate start=LocalDate.of(2026,9,7);
        List<WeatherDaily.Summary> summaries=WeatherDaily.summarize(List.of(),start,3);
        assertEquals(3,summaries.size());
        assertFalse(summaries.get(0).hasData());
        assertEquals(-1,summaries.get(0).weatherCode);
        assertTrue(Double.isNaN(summaries.get(0).minTemperature));
    }

    private static WeatherDaily.Hour hour(LocalDate day,int hour,double temperature,int rainProbability,double rain,double wind,double gust,double uv,int code){
        return new WeatherDaily.Hour(LocalDateTime.of(day,java.time.LocalTime.of(hour,0)),temperature,rainProbability,rain,wind,gust,uv,code);
    }
}
