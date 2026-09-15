package ch.pfvr.internapp;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/** Decides when event weather needs a morning/noon/evening style overview. */
final class WeatherEventPolicy {
    private static final long THREE_POINT_MINUTES=5L*60L;

    private WeatherEventPolicy() {}

    static final class Interval {
        final ZonedDateTime start,end;
        final boolean allDay;
        Interval(ZonedDateTime start,ZonedDateTime end,boolean allDay){this.start=start;this.end=end;this.allDay=allDay;}
        boolean containsHour(ZonedDateTime hour){return hour.plusHours(1).isAfter(start)&&hour.isBefore(end);}
    }

    static List<Interval> merge(List<Interval> intervals){
        List<Interval> sorted=new ArrayList<>(intervals), result=new ArrayList<>();
        sorted.sort(Comparator.comparing(interval->interval.start));
        for(Interval current:sorted){
            if(!current.end.isAfter(current.start))continue;
            if(result.isEmpty()||current.start.isAfter(result.get(result.size()-1).end))result.add(current);
            else{
                Interval previous=result.remove(result.size()-1);
                result.add(new Interval(previous.start,current.end.isAfter(previous.end)?current.end:previous.end,previous.allDay||current.allDay));
            }
        }
        return result;
    }

    static int[] sharedTargetHours(List<Interval> intervals){
        SortedSet<Integer> targets=new TreeSet<>();
        for(Interval interval:merge(intervals)){
            int[] points=targetHours(interval.allDay,interval.start,interval.end);
            if(points.length==0)targets.add(interval.start.getHour());
            else for(int point:points)targets.add(point);
        }
        return targets.stream().mapToInt(Integer::intValue).toArray();
    }

    static boolean usesThreePoints(boolean allDay,ZonedDateTime start,ZonedDateTime end){
        if(allDay)return true;
        if(start==null||end==null||!end.isAfter(start))return false;
        return Duration.between(start,end).toMinutes()>=THREE_POINT_MINUTES;
    }

    static int[] targetHours(boolean allDay,ZonedDateTime start,ZonedDateTime end){
        if(!usesThreePoints(allDay,start,end))return new int[0];
        if(allDay||start==null||end==null)return new int[]{6,12,18};
        long minutes=Duration.between(start,end).toMinutes();
        ZonedDateTime middle=start.plusMinutes(minutes/2L);
        return new int[]{start.getHour(),middle.getHour(),end.minusNanos(1).getHour()};
    }
}
