package ch.pfvr.internapp;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28)
public class AuditCalendarRegressionTest {
    private static final ZoneId ZONE=ZoneId.of("Europe/Zurich");

    private List<?> parse(String body,String clock) throws Exception {
        Method method=MainActivity.class.getDeclaredMethod("parseIcs",String.class,ZonedDateTime.class);
        method.setAccessible(true);
        return (List<?>)method.invoke(new MainActivity(),"BEGIN:VCALENDAR\n"+body+"END:VCALENDAR\n",LocalDateTime.parse(clock).atZone(ZONE));
    }

    private String event(String uid,String start,String end,String extras){
        return "BEGIN:VEVENT\nUID:"+uid+"\nSUMMARY:Termin\nDTSTART"+start+"\nDTEND"+end+"\n"+extras+"END:VEVENT\n";
    }

    private ZonedDateTime field(Object event,String name) throws Exception {
        Field field=event.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return (ZonedDateTime)field.get(event);
    }

    @Test public void explicitDateTimeAndGenuinelyEmptyFeed(){
        assertTrue(CalendarFeed.validStructure("BEGIN:VCALENDAR\nEND:VCALENDAR\n"));
        assertFalse(CalendarFeed.validStructure("BEGIN:VCALENDAR\nBEGIN:VEVENT\nEND:VCALENDAR\n"));
        assertFalse(CalendarFeed.validStructure("BEGIN:VCALENDAR\nBEGIN:VEVENT\nUID:missing-date\nEND:VEVENT\nEND:VCALENDAR\n"));
    }

    @Test public void parsesExplicitDateTimeAndClearedCalendar() throws Exception {
        assertTrue(parse("","2026-09-01T12:00").isEmpty());
        var values=parse(event("timed",";VALUE=DATE-TIME;TZID=Europe/Zurich:20260910T183000",";VALUE=DATE-TIME;TZID=Europe/Zurich:20260910T200000",""),"2026-09-01T12:00");
        assertEquals(1,values.size());
        assertEquals(LocalDateTime.parse("2026-09-10T18:30"),field(values.get(0),"start").toLocalDateTime());
    }

    @Test public void monthlyRulesSkipInvalidDatesAndHonorOrdinals() throws Exception {
        var dates=parse(event("month",";TZID=Europe/Zurich:20260131T183000",";TZID=Europe/Zurich:20260131T200000","RRULE:FREQ=MONTHLY;COUNT=3\n"),"2026-01-01T12:00");
        assertEquals(List.of("2026-01-31","2026-03-31","2026-05-31"),dates.stream().map(value->{try{return field(value,"start").toLocalDate().toString();}catch(Exception e){throw new RuntimeException(e);}}).toList());
        var mondays=parse(event("monday",";TZID=Europe/Zurich:20260907T183000",";TZID=Europe/Zurich:20260907T200000","RRULE:FREQ=MONTHLY;BYDAY=1MO;COUNT=3\n"),"2026-09-01T12:00");
        assertEquals(List.of("2026-09-07","2026-10-05","2026-11-02"),mondays.stream().map(value->{try{return field(value,"start").toLocalDate().toString();}catch(Exception e){throw new RuntimeException(e);}}).toList());
    }

    @Test public void allDayRepeatsStayAtMidnightAcrossDstAndRdateHonorsExdate() throws Exception {
        var values=parse(event("dst",";VALUE=DATE:20261025",";VALUE=DATE:20261026","RRULE:FREQ=DAILY;COUNT=3\n"),"2026-10-24T12:00");
        assertEquals(3,values.size());
        for(Object value:values)assertEquals(0,field(value,"end").getHour());
        var additional=parse(event("rdate",";VALUE=DATE:20261025",";VALUE=DATE:20261026","RDATE;VALUE=DATE:20261028\nEXDATE;VALUE=DATE:20261025\n"),"2026-10-24T12:00");
        assertEquals(1,additional.size());
        assertEquals("2026-10-28",field(additional.get(0),"start").toLocalDate().toString());
    }

    @Test(timeout=2000) public void invalidWeeklyIntervalFailsFast() throws Exception {
        for(String interval:List.of("0","-1")){
            try{
                parse(event("bad",";TZID=Europe/Zurich:20260909T183000",";TZID=Europe/Zurich:20260909T200000","RRULE:FREQ=WEEKLY;BYDAY=MO;INTERVAL="+interval+"\n"),"2026-09-01T12:00");
                fail("invalid INTERVAL was accepted: "+interval);
            }catch(InvocationTargetException expected){assertTrue(expected.getCause() instanceof IllegalArgumentException);}
        }
    }
}
