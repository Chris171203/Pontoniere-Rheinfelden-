package ch.pfvr.internapp;

import java.time.Duration;
import java.time.ZonedDateTime;

/** Decides when event weather needs a morning/noon/evening style overview. */
final class WeatherEventPolicy {
    private static final long THREE_POINT_MINUTES=5L*60L;

    private WeatherEventPolicy() {}

    static boolean usesThreePoints(boolean allDay,ZonedDateTime start,ZonedDateTime end){
        if(allDay)return true;
        if(start==null||end==null||!end.isAfter(start))return false;
        if(!start.toLocalDate().equals(end.toLocalDate()))return true;
        return Duration.between(start,end).toMinutes()>=THREE_POINT_MINUTES;
    }

    static int[] targetHours(boolean allDay,ZonedDateTime start,ZonedDateTime end){
        if(!usesThreePoints(allDay,start,end))return new int[0];
        if(allDay||start==null||end==null||!start.toLocalDate().equals(end.toLocalDate()))return new int[]{6,12,18};
        long minutes=Duration.between(start,end).toMinutes();
        ZonedDateTime middle=start.plusMinutes(minutes/2L);
        return new int[]{start.getHour(),middle.getHour(),end.getHour()};
    }
}
