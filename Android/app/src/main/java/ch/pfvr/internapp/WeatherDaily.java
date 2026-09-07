package ch.pfvr.internapp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Aggregates cached hourly weather values into compact calendar-day summaries. */
final class WeatherDaily {
    static final class Hour {
        final LocalDateTime time;
        final double temperature;
        final int precipitationProbability;
        final double precipitation;
        final double wind;
        final double gust;
        final double uv;
        final int weatherCode;

        Hour(LocalDateTime time,double temperature,int precipitationProbability,double precipitation,double wind,double gust,double uv,int weatherCode){
            this.time=time;
            this.temperature=temperature;
            this.precipitationProbability=Math.max(0,Math.min(100,precipitationProbability));
            this.precipitation=precipitation;
            this.wind=wind;
            this.gust=gust;
            this.uv=uv;
            this.weatherCode=weatherCode;
        }
    }

    static final class Slot {
        final LocalTime targetTime;
        final Hour hour;

        Slot(LocalTime targetTime,Hour hour){
            this.targetTime=targetTime;
            this.hour=hour;
        }

        boolean hasData(){return hour!=null;}
    }

    static final class Summary {
        final LocalDate date;
        final double minTemperature;
        final double maxTemperature;
        final int precipitationProbabilityMax;
        final double precipitationSum;
        final double windMax;
        final double gustMax;
        final double uvMax;
        final int weatherCode;
        final int count;

        Summary(LocalDate date,double minTemperature,double maxTemperature,int precipitationProbabilityMax,double precipitationSum,double windMax,double gustMax,double uvMax,int weatherCode,int count){
            this.date=date;
            this.minTemperature=minTemperature;
            this.maxTemperature=maxTemperature;
            this.precipitationProbabilityMax=precipitationProbabilityMax;
            this.precipitationSum=precipitationSum;
            this.windMax=windMax;
            this.gustMax=gustMax;
            this.uvMax=uvMax;
            this.weatherCode=weatherCode;
            this.count=count;
        }

        boolean hasData(){return count>0;}
    }

    private static final LocalTime REPRESENTATIVE_TIME=LocalTime.of(14,0);

    private WeatherDaily() {}

    static List<Slot> slots(List<Hour> hours,LocalDate day,int... targetHours){
        List<Slot> out=new ArrayList<>();
        if(day==null||targetHours==null)return out;
        List<Hour> safe=hours==null?List.of():hours;
        for(int targetHour:targetHours){
            if(targetHour<0||targetHour>23)continue;
            LocalTime target=LocalTime.of(targetHour,0);
            Hour best=null;
            int bestDistance=Integer.MAX_VALUE;
            for(Hour hour:safe){
                if(hour==null||hour.time==null||!day.equals(hour.time.toLocalDate()))continue;
                int distance=Math.abs(hour.time.toLocalTime().toSecondOfDay()-target.toSecondOfDay());
                if(distance<bestDistance){best=hour;bestDistance=distance;}
            }
            if(bestDistance>90*60)best=null;
            out.add(new Slot(target,best));
        }
        return out;
    }

    static List<Summary> summarize(List<Hour> hours,LocalDate firstDay,int dayCount){
        List<Summary> out=new ArrayList<>();
        if(firstDay==null||dayCount<=0)return out;
        List<Hour> safe=hours==null?List.of():hours;
        for(int offset=0;offset<dayCount;offset++)out.add(summarizeDay(safe,firstDay.plusDays(offset)));
        return out;
    }

    private static Summary summarizeDay(List<Hour> hours,LocalDate day){
        double minTemperature=Double.NaN,maxTemperature=Double.NaN,precipitationSum=0d;
        double windMax=Double.NaN,gustMax=Double.NaN,uvMax=Double.NaN;
        int probabilityMax=0,representativeCode=-1,representativeDistance=Integer.MAX_VALUE,count=0;

        for(Hour hour:hours){
            if(hour==null||hour.time==null||!day.equals(hour.time.toLocalDate()))continue;
            count++;
            if(Double.isFinite(hour.temperature)){
                minTemperature=Double.isFinite(minTemperature)?Math.min(minTemperature,hour.temperature):hour.temperature;
                maxTemperature=Double.isFinite(maxTemperature)?Math.max(maxTemperature,hour.temperature):hour.temperature;
            }
            probabilityMax=Math.max(probabilityMax,hour.precipitationProbability);
            if(Double.isFinite(hour.precipitation))precipitationSum+=Math.max(0d,hour.precipitation);
            if(Double.isFinite(hour.wind))windMax=maxFinite(windMax,Math.max(0d,hour.wind));
            if(Double.isFinite(hour.gust))gustMax=maxFinite(gustMax,Math.max(0d,hour.gust));
            if(Double.isFinite(hour.uv))uvMax=maxFinite(uvMax,Math.max(0d,hour.uv));

            if(hour.weatherCode>=0){
                int distance=Math.abs(hour.time.toLocalTime().toSecondOfDay()-REPRESENTATIVE_TIME.toSecondOfDay());
                if(distance<representativeDistance){
                    representativeDistance=distance;
                    representativeCode=hour.weatherCode;
                }
            }
        }

        return new Summary(day,minTemperature,maxTemperature,probabilityMax,precipitationSum,windMax,gustMax,uvMax,representativeCode,count);
    }

    private static double maxFinite(double current,double candidate){
        return Double.isFinite(current)?Math.max(current,candidate):candidate;
    }
}
