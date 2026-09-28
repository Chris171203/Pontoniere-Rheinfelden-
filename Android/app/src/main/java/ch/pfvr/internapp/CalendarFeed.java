package ch.pfvr.internapp;

/** Shared minimum ICS envelope validation for foreground and background refresh. */
final class CalendarFeed {
    private CalendarFeed() {}

    static boolean validStructure(String raw){
        if(raw==null||raw.isBlank())return false;
        int calendars=0,closedCalendars=0,events=0,closedEvents=0;
        boolean insideCalendar=false,insideEvent=false;
        for(String line:raw.replace("\r\n","\n").replace('\r','\n').split("\n")){
            if("BEGIN:VCALENDAR".equals(line)){
                if(insideCalendar||insideEvent)return false;
                insideCalendar=true;calendars++;
            }else if("END:VCALENDAR".equals(line)){
                if(!insideCalendar||insideEvent)return false;
                insideCalendar=false;closedCalendars++;
            }else if("BEGIN:VEVENT".equals(line)){
                if(!insideCalendar||insideEvent)return false;
                insideEvent=true;events++;
            }else if("END:VEVENT".equals(line)){
                if(!insideEvent)return false;
                insideEvent=false;closedEvents++;
            }else if(insideEvent&&line.startsWith("RRULE:")){
                if(!validRuleNumbers(line.substring(6)))return false;
            }
        }
        return calendars==1&&closedCalendars==1&&events==closedEvents&&!insideCalendar&&!insideEvent;
    }

    private static boolean validRuleNumbers(String rule){
        for(String part:rule.split(";")){
            if(!part.startsWith("INTERVAL=")&&!part.startsWith("COUNT="))continue;
            try{
                int number=Integer.parseInt(part.substring(part.indexOf('=')+1));
                if(number<=0||number>(part.startsWith("INTERVAL=")?10000:100000))return false;
            }catch(NumberFormatException error){return false;}
        }
        return true;
    }
}
