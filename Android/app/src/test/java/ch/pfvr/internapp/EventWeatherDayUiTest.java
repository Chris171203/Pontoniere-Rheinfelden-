package ch.pfvr.internapp;

import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.lang.reflect.*;
import java.time.*;
import java.util.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class EventWeatherDayUiTest {
    private static final ZoneId ZONE=ZoneId.of("Europe/Zurich");
    private static ZonedDateTime time(String value){return LocalDateTime.parse(value).atZone(ZONE);}
    private static Object field(Object object,String name) throws Exception {Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
    private static void set(Object object,String name,Object value) throws Exception {Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
    private static Object event(String title,String start,String end,boolean allDay) throws Exception {
        Constructor<?> c=Class.forName("ch.pfvr.internapp.MainActivity$Event").getDeclaredConstructor();c.setAccessible(true);Object e=c.newInstance();
        set(e,"title",title);set(e,"start",time(start));set(e,"end",time(end));set(e,"allDay",allDay);set(e,"status","");set(e,"description","");return e;
    }
    @SuppressWarnings("unchecked") private static void events(MainActivity app,Object... values) throws Exception {List<Object> events=(List<Object>)field(app,"events");events.clear();events.addAll(Arrays.asList(values));}
    private static Object call(MainActivity app,String method,String now) throws Exception {Method m=MainActivity.class.getDeclaredMethod(method,ZonedDateTime.class);m.setAccessible(true);return m.invoke(app,time(now));}
    private static List<?> selected(MainActivity app,String now) throws Exception {return (List<?>)call(app,"nextWeatherSlots",now);}
    private static String text(View v){String s=v instanceof TextView?((TextView)v).getText().toString():"";if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)s+="\n"+text(((ViewGroup)v).getChildAt(i));return s;}
    private static String weather() throws Exception {
        JSONObject hourly=new JSONObject();String[] keys={"time","temperature_2m","precipitation_probability","precipitation","weather_code","wind_speed_10m","wind_gusts_10m","uv_index"};
        for(String key:keys)hourly.put(key,new JSONArray());
        for(int h=0;h<24;h++){
            Object[] values={String.format(Locale.ROOT,"2026-09-15T%02d:00",h),h<16?28:14,0,0,0,h<16?99:5,h<16?99:10,h<16?9:1};
            for(int k=0;k<keys.length;k++)hourly.getJSONArray(keys[k]).put(values[k]);
        }
        return new JSONObject().put("hourly",hourly).toString();
    }
    @Test public void middayAndEveningHaveSeparateWeatherAndSwissGermanHeading() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity app=controller.get();
            Object noon=event("Mittagsanlass","2026-09-15T12:30","2026-09-15T14:00",false), evening=event("Abendanlass","2026-09-15T18:30","2026-09-15T20:00",false);
            events(app,evening,noon);
            SharedPreferences prefs=(SharedPreferences)field(app,"prefs");prefs.edit().putString((String)field(app,"PREF_WEATHER_CACHE"),weather()).putString("ui_language","gsw").commit();
            ViewGroup tile=(ViewGroup)call(app,"homeWeatherTile","2026-09-15T09:00");String rendered=text(tile);
            assertTrue(rendered,rendered.contains("Wätter zu de nöchschte Termin"));assertTrue(rendered.contains("Mittagsanlass"));assertTrue(rendered.contains("Abendanlass"));assertTrue(rendered.indexOf("Mittagsanlass")<rendered.indexOf("Abendanlass"));
            assertTrue(rendered.contains("12:30–14:00"));assertTrue(rendered.contains("18:30–20:00"));assertTrue(rendered.contains("28 °C"));assertTrue(rendered.contains("14 °C"));
            events(app,evening);String only=text((View)call(app,"homeWeatherTile","2026-09-15T09:00"));
            assertFalse(only.contains("Mittagsanlass"));assertFalse(only.contains("28 °C"));assertFalse(only.contains("99 km/h"));assertTrue(only.contains("14 °C"));
        }
    }
    @Test public void runningCompletedCancelledAndSimultaneousEvents() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity app=controller.get();events(app,event("Mittag","2026-09-15T12:30","2026-09-15T14:00",false),event("Abend A","2026-09-15T18:00","2026-09-15T20:00",false),event("Abend B","2026-09-15T18:00","2026-09-15T21:00",false),event("Abgesagt","2026-09-15T10:00","2026-09-15T22:00",false));
            assertEquals(3,selected(app,"2026-09-15T13:00").size());assertEquals(2,selected(app,"2026-09-15T14:00").size());
            List<?> next=selected(app,"2026-09-15T21:00");assertEquals(1,next.size());assertEquals(time("2026-09-16T18:30"),field(next.get(0),"start"));
        }
    }
    @Test public void regularTrainingComplementsNoonButExplicitTrainingIsNotDuplicated() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity app=controller.get();Object noon=event("Mittag","2026-09-16T12:30","2026-09-16T14:00",false);
            events(app,noon);List<?> values=selected(app,"2026-09-16T09:00");assertEquals(2,values.size());assertEquals(false,field(values.get(1),"fromCalendar"));
            events(app,noon,event("Training","2026-09-16T18:30","2026-09-16T20:00",false));assertEquals(2,selected(app,"2026-09-16T09:00").size());
            events(app,event("Training","2026-09-16T00:00","2026-09-17T00:00",true));values=selected(app,"2026-09-16T09:00");assertEquals(1,values.size());assertEquals(true,field(values.get(0),"allDay"));
        }
    }
    @Test public void multiDayEventUsesTodayAlongsideTodaysEvening() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity app=controller.get();events(app,event("Fest","2026-09-14T00:00","2026-09-17T00:00",true),event("Abend","2026-09-15T18:00","2026-09-15T20:00",false));
            List<?> values=selected(app,"2026-09-15T09:00");assertEquals(2,values.size());assertEquals(time("2026-09-15T00:00"),field(values.get(0),"start"));assertEquals(time("2026-09-16T00:00"),field(values.get(0),"end"));
        }
    }
    @Test public void longEveningSummaryExcludesNoonAndStopsAtMidnight() throws Exception {
        try(var controller=Robolectric.buildActivity(MainActivity.class).setup()){
            MainActivity app=controller.get();events(app,event("Abendfest","2026-09-15T18:00","2026-09-16T00:00",false));
            SharedPreferences prefs=(SharedPreferences)field(app,"prefs");prefs.edit().putString((String)field(app,"PREF_WEATHER_CACHE"),weather()).commit();
            String rendered=text((View)call(app,"homeWeatherTile","2026-09-15T09:00"));assertTrue(rendered.contains("14 °C"));assertFalse(rendered.contains("28 °C"));assertFalse(rendered.contains("99 km/h"));assertFalse(rendered.contains("06 Uhr"));assertFalse(rendered.contains("12 Uhr"));
        }
    }
}
