from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
text = p.read_text(encoding='utf-8')

if 'PREF_HYDRO_HISTORY_CACHE' in text and 'data_1hour_mean' in text and '7 Tage · Stundenmittel' in text:
    print('7-day hydro patch already applied')
    raise SystemExit(0)

text = text.replace(
    '    private static final String PREF_HYDRO_UPDATED = "hydro_updated";\n',
    '    private static final String PREF_HYDRO_UPDATED = "hydro_updated";\n'
    '    private static final String PREF_HYDRO_HISTORY_CACHE = "hydro_history_cache";\n'
    '    private static final String PREF_HYDRO_HISTORY_UPDATED = "hydro_history_updated";\n',
    1,
)

text = text.replace('TextView title=txt("Pegelverlauf",13,TEXT,true);', 'TextView title=txt("Pegelverlauf · 7 Tage",13,TEXT,true);', 1)
text = text.replace('TextView title=txt("Wassertemperatur",13,TEXT,true);', 'TextView title=txt("Wassertemperatur · 7 Tage",13,TEXT,true);', 1)

start = text.index('    private TrendSeries hydroSeries(String parameter){')
end = text.index('    private void refreshLive(boolean force){', start)
new_hydro_series = '''    private TrendSeries hydroSeries(String parameter){
        TrendSeries out=new TrendSeries();
        List<HydroPoint> points=new ArrayList<>();
        String history=prefs.getString(PREF_HYDRO_HISTORY_CACHE,"");
        if(!history.trim().isEmpty()){
            try{
                JSONArray a=new JSONObject(history).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_1hour_mean");
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.getJSONObject(i); if(!parameter.equals(o.optString("parameterName","")))continue;
                    double value=o.optDouble("value",Double.NaN); String ts=o.optString("timestamp",""); if(Double.isNaN(value)||ts.isEmpty())continue;
                    try{points.add(new HydroPoint(java.time.Instant.parse(ts).toEpochMilli(),value));}catch(Exception ignored){}
                }
            }catch(Exception ignored){}
        }
        String live=prefs.getString(PREF_HYDRO_CACHE,"");
        if(!live.trim().isEmpty()){
            try{
                JSONArray a=new JSONObject(live).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
                HydroPoint newest=null;
                for(int i=0;i<a.length();i++){
                    JSONObject o=a.getJSONObject(i); if(!parameter.equals(o.optString("parameterName","")))continue;
                    double value=o.optDouble("value",Double.NaN); String ts=o.optString("timestamp",""); if(Double.isNaN(value)||ts.isEmpty())continue;
                    try{HydroPoint hp=new HydroPoint(java.time.Instant.parse(ts).toEpochMilli(),value);if(newest==null||hp.time>newest.time)newest=hp;}catch(Exception ignored){}
                }
                if(newest!=null)points.add(newest);
            }catch(Exception ignored){}
        }
        points.sort(Comparator.comparingLong(x->x.time));
        long newest=points.isEmpty()?Long.MIN_VALUE:points.get(points.size()-1).time;
        long cutoff=newest==Long.MIN_VALUE?Long.MIN_VALUE:newest-7L*24L*60L*60L*1000L;
        long last=Long.MIN_VALUE;
        for(HydroPoint hp:points){if(hp.time<cutoff||hp.time==last)continue;last=hp.time;out.times.add(hp.time);out.values.add(hp.value);}
        return out;
    }

'''
text = text[:start] + new_hydro_series + text[end:]

start = text.index('    private void refreshHydro(boolean force){')
end = text.index('    private String httpGet(String url) throws Exception{', start)
new_refresh = '''    private void refreshHydro(boolean force){
        long age=System.currentTimeMillis()-prefs.getLong(PREF_HYDRO_UPDATED,0L);
        long histAge=System.currentTimeMillis()-prefs.getLong(PREF_HYDRO_HISTORY_UPDATED,0L);
        boolean liveFresh=!prefs.getString(PREF_HYDRO_CACHE,"").isBlank()&&age<10*60000L;
        boolean histFresh=!prefs.getString(PREF_HYDRO_HISTORY_CACHE,"").isBlank()&&histAge<60*60000L;
        if(hydroLoading||(!force&&liveFresh&&histFresh))return;
        hydroLoading=true;
        new Thread(()->{
            try{
                String dq=String.valueOf((char)34);
                if(force||!liveFresh){
                    try{
                        String liveQuery="{ water { observations { data_live(where:{stationNo:{_eq:"+dq+"2091"+dq+"}}) { stationNo parameterName timestamp value releaseStatus } } } }";
                        String raw=bafuPost(liveQuery);
                        JSONObject j=new JSONObject(raw);if(j.has("errors"))throw new Exception("GraphQL live");
                        j.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
                        prefs.edit().putString(PREF_HYDRO_CACHE,raw).putLong(PREF_HYDRO_UPDATED,System.currentTimeMillis()).apply();
                    }catch(Exception ignored){}
                }
                if(force||!histFresh){
                    try{
                        String from=java.time.Instant.now().minus(java.time.Duration.ofDays(7)).toString();
                        String historyQuery="{ water { observations { data_1hour_mean(where:{station:{no:{_eq:"+dq+"2091"+dq+"}},timestamp:{_gte:"+dq+from+dq+"}}) { parameterName timestamp value } } } }";
                        String raw=bafuPost(historyQuery);
                        JSONObject j=new JSONObject(raw);if(j.has("errors"))throw new Exception("GraphQL history");
                        j.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_1hour_mean");
                        prefs.edit().putString(PREF_HYDRO_HISTORY_CACHE,raw).putLong(PREF_HYDRO_HISTORY_UPDATED,System.currentTimeMillis()).apply();
                    }catch(Exception ignored){}
                }
            }finally{
                hydroLoading=false;
                runOnUiThread(()->{if(current==Screen.HOME)navigate(Screen.HOME);});
            }
        }).start();
    }

    private String bafuPost(String query) throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL("https://data.bafu.admin.ch/api").openConnection();
        try{
            c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(7000);c.setReadTimeout(12000);
            c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept","application/json");
            String body=new JSONObject().put("query",query).toString();
            try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
            if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());
            return readConnection(c);
        }finally{c.disconnect();}
    }

'''
text = text[:start] + new_refresh + text[end:]

old = '''            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));
            for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}for(int i=0;i<3;i++){float x=left+(right-left)*i/2f;canvas.drawLine(x,top,x,bottom,grid);}
            Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*(series.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(series.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(lineColor);canvas.drawPath(path,line);
            label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            canvas.drawText(fmtTrend(max),dp(2),top+dp(4),label);canvas.drawText(fmtTrend(min),dp(2),bottom,label);canvas.drawText(unit,dp(2),dp(10),axis);
            ZoneId zone=ZoneId.of("Europe/Zurich");DateTimeFormatter tf=DateTimeFormatter.ofPattern("HH:mm");String first=java.time.Instant.ofEpochMilli(minT).atZone(zone).format(tf),last=java.time.Instant.ofEpochMilli(maxT).atZone(zone).format(tf);canvas.drawText(first,left,h-dp(6),label);float lw=label.measureText(last);canvas.drawText(last,right-lw,h-dp(6),label);String xLabel="Zeit · 24 h";float xw=axis.measureText(xLabel);canvas.drawText(xLabel,left+(right-left-xw)/2f,h-dp(6),axis);
'''
new = '''            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));
            label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}
            ZoneId zone=ZoneId.of("Europe/Zurich");
            ZonedDateTime firstZ=java.time.Instant.ofEpochMilli(minT).atZone(zone),lastZ=java.time.Instant.ofEpochMilli(maxT).atZone(zone);
            LocalDate tickDay=firstZ.toLocalDate().plusDays(1);DateTimeFormatter dayFmt=DateTimeFormatter.ofPattern("EE",Locale.GERMAN);
            while(!tickDay.isAfter(lastZ.toLocalDate())){long tt=tickDay.atStartOfDay(zone).toInstant().toEpochMilli();if(tt>=minT&&tt<=maxT){float x=left+(right-left)*(tt-minT)/(float)(maxT-minT);canvas.drawLine(x,top,x,bottom,grid);String lab=tickDay.format(dayFmt);float tw=label.measureText(lab);canvas.drawText(lab,x-tw/2f,h-dp(6),label);}tickDay=tickDay.plusDays(1);}
            Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*(series.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(series.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(lineColor);canvas.drawPath(path,line);
            canvas.drawText(fmtTrend(max),dp(2),top+dp(4),label);canvas.drawText(fmtTrend(min),dp(2),bottom,label);canvas.drawText(unit,dp(2),dp(10),axis);
            String xLabel="7 Tage · Stundenmittel";float xw=axis.measureText(xLabel);canvas.drawText(xLabel,left+(right-left-xw)/2f,dp(11),axis);
'''
if old not in text:
    raise SystemExit('TrendView axis block not found')
text = text.replace(old, new, 1)

p.write_text(text, encoding='utf-8')
print('Applied 7-day hourly BAFU trend view')
