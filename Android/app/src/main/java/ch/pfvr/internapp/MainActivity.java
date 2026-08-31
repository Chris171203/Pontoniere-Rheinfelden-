package ch.pfvr.internapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String PREFS = "pfvr_prefs";
    private static final String PREF_INTERNAL_URL = "start_url";
    private static final String PREF_BANK_PACKAGE = "bank_package";
    private static final String PREF_BANK_LABEL = "bank_label";
    private static final String PREF_TWINT_PACKAGE = "twint_package";
    private static final String PREF_TWINT_LABEL = "twint_label";
    private static final String PREF_ICS_CACHE = "ics_cache";
    private static final String PREF_ICS_UPDATED = "ics_updated";
    private static final int REQ_SAVE_QR = 2401;
    private static final String PREF_WEATHER_CACHE = "weather_cache";
    private static final String PREF_WEATHER_UPDATED = "weather_updated";
    private static final String PREF_WEATHER_SOURCE = "weather_source";
    private static final String PREF_HYDRO_CACHE = "hydro_cache";
    private static final String PREF_HYDRO_UPDATED = "hydro_updated";
    private static final String PREF_THEME = "theme_mode";

    private static final String SITE = "https://www.pfvr.ch/";
    private static final String NEWS = "https://www.pfvr.ch/verein/newsarchiv/";
    private static final String CLUB = "https://www.pfvr.ch/verein/";
    private static final String BOARD = "https://www.pfvr.ch/verein/vorstand/";
    private static final String HISTORY = "https://www.pfvr.ch/verein/geschichte/";
    private static final String PROGRAM = "https://www.pfvr.ch/verein/jahresprogramm/";
    private static final String CONTACT = "https://www.pfvr.ch/kontakt/";
    private static final String ICS = "https://calendar.google.com/calendar/ical/a8mtko83nd27vsvp4i1cnpt3gs%40group.calendar.google.com/public/basic.ics";
    private static final String CALENDAR_WEB = "https://calendar.google.com/calendar/embed?src=a8mtko83nd27vsvp4i1cnpt3gs%40group.calendar.google.com&ctz=Europe%2FZurich";

    private static final String CLUB_IBAN = "CH58 0076 9440 9013 1200 1";
    private static final String CLUB_PAYEE = "Pontonierfahrverein Rheinfelden";
    private static final String CLUB_PAYMENT_NOTE = "Konsumation Vereinsbeiz";

    private static final int NAVY = Color.rgb(12,45,72);
    private static final int WATER = Color.rgb(43,142,166);
    private static final int SURFACE = Color.rgb(244,247,249);
    private static final int TEXT = Color.rgb(21,35,46);
    private static final int MUTED = Color.rgb(96,113,126);
    private static final int DARK_SURFACE = Color.rgb(17,23,28);
    private static final int DARK_CARD = Color.rgb(26,34,40);
    private static final int DARK_SOFT = Color.rgb(35,46,54);
    private static final int DARK_TEXT = Color.rgb(236,241,244);
    private static final int DARK_MUTED = Color.rgb(160,176,186);

    private enum Screen { HOME, EVENTS, CASH, CLUB, SETTINGS, INTERNAL }

    private FrameLayout content;
    private TextView headerSubtitle;
    private TextView headerBack;
    private final Map<Screen,TextView> navButtons = new HashMap<>();
    private SharedPreferences prefs;
    private Screen current = Screen.HOME;
    private WebView activeWebView;
    private List<Event> events = new ArrayList<>();
    private long eventsUpdated = 0L;
    private volatile boolean eventsLoading = false;
    private Bitmap pendingQrBitmap;
    private Button bankButton;
    private volatile boolean weatherLoading = false;
    private volatile boolean hydroLoading = false;
    private boolean darkMode = false;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkMode = resolveDarkMode();
        loadCachedEvents();
        applyWindowTheme();
        setContentView(buildShell());
        navigate(Screen.HOME);
        refreshEvents(false, () -> {
            if (current == Screen.HOME || current == Screen.EVENTS) navigate(current);
        });
        refreshLive(false);
    }

    private View buildShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(themeBg(SURFACE));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(10),dp(9),dp(10),dp(9));
        header.setBackgroundColor(NAVY);
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(68)));

        headerBack = txt("‹",38,Color.WHITE,false);
        headerBack.setGravity(Gravity.CENTER);
        headerBack.setContentDescription("Zurück");
        headerBack.setVisibility(View.GONE);
        headerBack.setOnClickListener(v -> handleBack());
        header.addView(headerBack,new LinearLayout.LayoutParams(dp(38),dp(48)));

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.pfvr_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(round(Color.WHITE,8));
        logo.setClipToOutline(true);
        header.addView(logo,new LinearLayout.LayoutParams(dp(46),dp(46)));

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.setPadding(dp(11),0,0,0);
        header.addView(titles,new LinearLayout.LayoutParams(0,-1,1));
        titles.addView(txt("Pontoniere Rheinfelden",17,Color.WHITE,true));
        headerSubtitle = txt("Auf dem Rhein zuhause",12,Color.rgb(194,220,232),false);
        titles.addView(headerSubtitle);

        TextView web = txt("↗",25,Color.WHITE,true);
        web.setGravity(Gravity.CENTER);
        web.setOnClickListener(v -> external(SITE));
        header.addView(web,new LinearLayout.LayoutParams(dp(46),dp(46)));

        content = new FrameLayout(this);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6),dp(6),dp(6),dp(8));
        nav.setBackgroundColor(themeBg(Color.WHITE));
        root.addView(nav,new LinearLayout.LayoutParams(-1,dp(70)));
        addNav(nav,Screen.HOME,"Home");
        addNav(nav,Screen.EVENTS,"Termine");
        addNav(nav,Screen.CASH,"Kasse");
        addNav(nav,Screen.CLUB,"Verein");
        addNav(nav,Screen.SETTINGS,"Einst.");
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            header.setPadding(dp(10), dp(9) + top, dp(10), dp(9));
            ViewGroup.LayoutParams hp = header.getLayoutParams(); hp.height = dp(68) + top; header.setLayoutParams(hp);
            nav.setPadding(dp(6), dp(6), dp(6), dp(8) + bottom);
            ViewGroup.LayoutParams np = nav.getLayoutParams(); np.height = dp(70) + bottom; nav.setLayoutParams(np);
            return insets;
        });
        root.requestApplyInsets();
        return root;
    }

    private void addNav(LinearLayout nav, Screen screen, String label) {
        TextView t = txt(label,13,Color.rgb(65,82,96),true);
        t.setGravity(Gravity.CENTER); t.setPadding(dp(2),0,dp(2),0);
        t.setOnClickListener(v -> navigate(screen));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,-1,1); lp.setMargins(dp(2),0,dp(2),0); nav.addView(t,lp);
        navButtons.put(screen,t);
    }

    private void navigate(Screen screen) {
        current = screen; activeWebView = null;
        if (headerBack != null) headerBack.setVisibility(screen == Screen.HOME ? View.GONE : View.VISIBLE);
        for (Map.Entry<Screen,TextView> e: navButtons.entrySet()) {
            boolean selected = e.getKey()==screen;
            e.getValue().setTextColor(selected?Color.WHITE:themeText(Color.rgb(65,82,96)));
            e.getValue().setTypeface(Typeface.DEFAULT_BOLD);
            e.getValue().setBackground(selected?round(NAVY,16):null);
        }
        content.removeAllViews();
        switch(screen) {
            case HOME: headerSubtitle.setText("Auf dem Rhein zuhause"); content.addView(home()); break;
            case EVENTS: headerSubtitle.setText("Jahresprogramm"); content.addView(eventScreen()); break;
            case CASH: headerSubtitle.setText("Vereinsbeiz bezahlen"); content.addView(cash()); break;
            case CLUB: headerSubtitle.setText("Verein & Kontakt"); content.addView(club()); break;
            case SETTINGS: headerSubtitle.setText("Einstellungen"); content.addView(settings()); break;
            case INTERNAL: headerSubtitle.setText("Interner Bereich"); content.addView(internal()); break;
        }
    }

    private View home() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout b = body(); scroll.addView(b);

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20),dp(19),dp(20),dp(19));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(20,91,115),WATER});
        bg.setCornerRadius(dp(22)); hero.setBackground(bg);
        b.addView(hero,margin(-1,-2,0,4,0,18));
        hero.addView(txt("RHEINFELDEN  •  SEIT 1896",12,Color.rgb(208,231,239),true));
        TextView h = txt("Gemeinsam auf dem Rhein.",29,Color.WHITE,true); h.setPadding(0,dp(8),0,dp(5)); hero.addView(h);
        hero.addView(txt("Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit.",15,Color.rgb(232,243,247),false));
        LinearLayout actions = new LinearLayout(this); actions.setPadding(0,dp(17),0,0); hero.addView(actions);
        Button internal = btn("An-/Abmelden",Color.WHITE,NAVY); internal.setOnClickListener(v->navigate(Screen.INTERNAL)); actions.addView(internal,new LinearLayout.LayoutParams(0,dp(46),1));
        Button pay = btn("Bezahlen",Color.argb(45,255,255,255),Color.WHITE); pay.setOnClickListener(v->navigate(Screen.CASH));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0,dp(46),1); pp.setMargins(dp(9),0,0,0); actions.addView(pay,pp);

        section(b,"Training & Rhein","Aktuelle Prognose und Messwerte");
        b.addView(liveInfoRow());
        TextView liveReload=link("Live-Daten aktualisieren  ↻"); liveReload.setOnClickListener(v->{Toast.makeText(this,"Wetter und Rhein werden aktualisiert …",Toast.LENGTH_SHORT).show();refreshLive(true);}); b.addView(liveReload);

        section(b,"Als Nächstes","Aus dem öffentlichen Vereinskalender");
        if(events.isEmpty()) {
            LinearLayout c=card(); c.addView(new ProgressBar(this),new LinearLayout.LayoutParams(dp(34),dp(34)));
            TextView t=txt("Termine werden geladen …",14,MUTED,false); t.setPadding(dp(12),0,0,0); c.addView(t); b.addView(c,margin(-1,-2,0,0,0,10));
        } else {
            for(int i=0;i<Math.min(3,events.size());i++) b.addView(eventCard(events.get(i),true));
        }
        TextView all=link("Alle Termine anzeigen  →"); all.setOnClickListener(v->navigate(Screen.EVENTS)); b.addView(all);

        section(b,"Schnellzugriff",null);
        b.addView(action("Depot","Rheinweg 42 · 4310 Rheinfelden","Route",v->openMap()));
        return scroll;
    }

    private View liveInfoRow(){
        LinearLayout stack=new LinearLayout(this); stack.setOrientation(LinearLayout.VERTICAL); stack.setPadding(0,0,0,dp(2));
        LinearLayout weather=weatherCard(); stack.addView(weather,margin(-1,-2,0,0,0,10));
        LinearLayout river=riverCard(); stack.addView(river,margin(-1,-2,0,0,0,4));
        return stack;
    }

    private LinearLayout weatherCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=weatherSummary();
        c.addView(txt(x[0],11,WATER,true));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(5),0,dp(5)); c.addView(row);
        TextView icon=txt(x[5],38,themeText(TEXT),false); icon.setGravity(Gravity.CENTER); row.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(7),0,0,0); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        info.addView(txt(x[1],15,MUTED,true));
        TextView main=txt(x[2],21,TEXT,true); main.setPadding(0,dp(2),0,0); info.addView(main);
        TextView details=txt(x[3],13,MUTED,false); details.setPadding(0,dp(5),0,0); c.addView(details);
        TextView src=txt(x[4],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(8),0,0); c.addView(src);
        return c;
    }

    private LinearLayout riverCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=hydroSummary(); c.setOnClickListener(v->external("https://www.hydrodaten.admin.ch/de/seen-und-fluesse/stationen-und-daten/2091"));
        c.addView(txt(x[0],11,WATER,true));
        TextView main=txt(x[1],22,TEXT,true); main.setPadding(0,dp(5),0,dp(3)); c.addView(main);
        c.addView(txt(x[2],13,MUTED,false));
        TrendSeries level=hydroSeries("W"), temp=hydroSeries("WT");
        if(level.values.size()>=2){
            TextView title=txt("Pegelverlauf",13,TEXT,true); title.setPadding(0,dp(14),0,dp(3)); c.addView(title);
            c.addView(new TrendView(this,level,"m ü.M.",WATER),new LinearLayout.LayoutParams(-1,dp(132)));
        }
        if(temp.values.size()>=2){
            TextView title=txt("Wassertemperatur",13,TEXT,true); title.setPadding(0,dp(12),0,dp(3)); c.addView(title);
            c.addView(new TrendView(this,temp,"°C",Color.rgb(220,137,63)),new LinearLayout.LayoutParams(-1,dp(132)));
        }
        TextView src=txt(x[3],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(9),0,0); c.addView(src);
        return c;
    }

    private boolean summerTraining(LocalDate d){int m=d.getMonthValue();return m>=4&&m<=9;}
    private boolean regularTrainingDay(LocalDate d){DayOfWeek w=d.getDayOfWeek();return summerTraining(d)?(w==DayOfWeek.MONDAY||w==DayOfWeek.WEDNESDAY):w==DayOfWeek.THURSDAY;}
    private String trainingTime(LocalDate d){return summerTraining(d)?"18:30–20:00 Uhr":"19:30–21:00 Uhr";}
    private int trainingEndHour(LocalDate d){return summerTraining(d)?20:21;}
    private int[] trainingWeatherHours(LocalDate d){return summerTraining(d)?new int[]{18,19,20}:new int[]{19,20,21};}
    private LocalDate nextTrainingDay(){
        ZoneId zone=ZoneId.of("Europe/Zurich"); ZonedDateTime now=ZonedDateTime.now(zone); LocalDate start=now.toLocalDate();
        for(int i=0;i<10;i++){LocalDate d=start.plusDays(i);if(!regularTrainingDay(d))continue;ZonedDateTime cutoff=d.atTime(trainingEndHour(d),0).atZone(zone);if(i>0||now.isBefore(cutoff))return d;}
        return start.plusDays(1);
    }

    private String[] weatherSummary(){
        LocalDate d=nextTrainingDay(); String date=cap(d.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN)))+" · "+trainingTime(d);
        String raw=prefs.getString(PREF_WEATHER_CACHE,""); long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L); String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        if(raw.trim().isEmpty())return new String[]{"NÄCHSTES TRAINING",date,"Wetter wird geladen …","Prognose wird im Hintergrund aktualisiert.",source,"◌"};
        try{
            JSONObject h=new JSONObject(raw).getJSONObject("hourly"); JSONArray times=h.getJSONArray("time"),temp=h.getJSONArray("temperature_2m"),prob=h.getJSONArray("precipitation_probability"),prec=h.getJSONArray("precipitation"),code=h.getJSONArray("weather_code"),wind=h.getJSONArray("wind_speed_10m"),gust=h.getJSONArray("wind_gusts_10m");
            double tFirst=Double.NaN,tLast=Double.NaN,psum=0,wmax=0,gmax=0; int pmax=0,wcode=-1,count=0; String prefix=d.toString()+"T"; int[] hours=trainingWeatherHours(d);
            for(int i=0;i<times.length();i++){
                String tm=times.optString(i,""); boolean inWindow=false; for(int hour:hours)if(tm.equals(prefix+String.format(Locale.ROOT,"%02d:00",hour))){inWindow=true;break;} if(!inWindow)continue;
                double tv=temp.optDouble(i,Double.NaN); if(count==0){tFirst=tv;wcode=code.optInt(i,-1);} tLast=tv;
                pmax=Math.max(pmax,prob.optInt(i,0)); psum+=Math.max(0,prec.optDouble(i,0)); wmax=Math.max(wmax,wind.optDouble(i,0)); gmax=Math.max(gmax,gust.optDouble(i,0)); count++;
            }
            if(count==0)return new String[]{"NÄCHSTES TRAINING",date,"Noch keine Prognose","Für diesen Trainingszeitraum liegen noch keine Stundenwerte vor.",weatherAge(source,updated),"◌"};
            String tempText=Double.isNaN(tFirst)?"":String.format(Locale.GERMAN,"%.0f °C",tFirst);
            if(!Double.isNaN(tLast)&&!Double.isNaN(tFirst)&&Math.abs(tLast-tFirst)>=1.0)tempText+=String.format(Locale.GERMAN," → %.0f °C",tLast);
            String main=tempText+(tempText.isEmpty()?"":" · ")+weatherCode(wcode);
            String details="Regen "+pmax+" % · "+String.format(Locale.GERMAN,"%.1f mm",psum)+"\nWind "+Math.round(wmax)+" km/h · Böen "+Math.round(gmax)+" km/h";
            return new String[]{"NÄCHSTES TRAINING",date,main,details,weatherAge(source,updated),weatherIcon(wcode)};
        }catch(Exception e){return new String[]{"NÄCHSTES TRAINING",date,"Gespeicherte Wetterdaten nicht lesbar","Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen.",weatherAge(source,updated),"◌"};}
    }

    private String weatherIcon(int c){if(c==0)return "☀";if(c<=2)return "⛅";if(c==3)return "☁";if(c==45||c==48)return "🌫";if(c>=51&&c<=67)return "🌧";if(c>=71&&c<=77)return "❄";if(c>=80&&c<=82)return "🌦";if(c>=85&&c<=86)return "🌨";if(c>=95)return "⚡";return "◌";}

    private String weatherAge(String source,long updated){if(updated<=0)return source;long min=Math.max(0,(System.currentTimeMillis()-updated)/60000);return source+(min>90?" · Cache "+(min/60)+" h":" · vor "+min+" min");}
    private String weatherCode(int c){if(c==0)return "klar";if(c<=2)return "leicht bewölkt";if(c==3)return "bewölkt";if(c==45||c==48)return "Nebel";if(c>=51&&c<=57)return "Nieselregen";if(c>=61&&c<=67)return "Regen";if(c>=71&&c<=77)return "Schnee";if(c>=80&&c<=82)return "Schauer";if(c>=85&&c<=86)return "Schneeschauer";if(c>=95)return "Gewitter";return "Wetter";}

    private String[] hydroSummary(){
        String raw=prefs.getString(PREF_HYDRO_CACHE,""); long cache=prefs.getLong(PREF_HYDRO_UPDATED,0L);
        if(raw.isBlank())return new String[]{"RHEIN · BAFU 2091","Wird geladen …","Abfluss · Pegel · Temperatur","BAFU Live-Daten"};
        try{
            JSONArray a=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            Map<String,Double> val=new HashMap<>();Map<String,String> ts=new HashMap<>();
            for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);String p=o.optString("parameterName","");String t=o.optString("timestamp","");if(!(p.equals("Q")||p.equals("W")||p.equals("WT")))continue;if(!ts.containsKey(p)||t.compareTo(ts.get(p))>0){ts.put(p,t);val.put(p,o.optDouble("value",Double.NaN));}}
            double q=val.getOrDefault("Q",Double.NaN),w=val.getOrDefault("W",Double.NaN),wt=val.getOrDefault("WT",Double.NaN);String latest="";for(String t:ts.values())if(t.compareTo(latest)>0)latest=t;
            String main=Double.isNaN(q)?"Rhein Rheinfelden":String.format(Locale.GERMAN,"%.0f m³/s",q);
            StringBuilder sub=new StringBuilder();if(!Double.isNaN(w))sub.append(String.format(Locale.GERMAN,"Pegel %.2f m ü.M.",w));if(!Double.isNaN(wt)){if(sub.length()>0)sub.append("\n");sub.append(String.format(Locale.GERMAN,"Wasser %.1f °C",wt));}
            String stand="BAFU 2091";try{if(!latest.isBlank())stand+=" · Stand "+java.time.Instant.parse(latest).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception ignored){}
            if(cache>0&&(System.currentTimeMillis()-cache)>45*60000L)stand+=" · Cache";
            return new String[]{"RHEIN · BAFU 2091",main,sub.length()==0?"Messwerte derzeit unvollständig":sub.toString(),stand};
        }catch(Exception e){return new String[]{"RHEIN · BAFU 2091","Gespeicherter Stand","Messdaten nicht lesbar","BAFU · Cache"};}
    }

    private TrendSeries hydroSeries(String parameter){
        TrendSeries out=new TrendSeries(); String raw=prefs.getString(PREF_HYDRO_CACHE,""); if(raw.trim().isEmpty())return out;
        try{
            JSONArray a=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            List<HydroPoint> points=new ArrayList<>();
            for(int i=0;i<a.length();i++){
                JSONObject o=a.getJSONObject(i); if(!parameter.equals(o.optString("parameterName","")))continue;
                double value=o.optDouble("value",Double.NaN); String ts=o.optString("timestamp",""); if(Double.isNaN(value)||ts.isEmpty())continue;
                try{points.add(new HydroPoint(java.time.Instant.parse(ts).toEpochMilli(),value));}catch(Exception ignored){}
            }
            points.sort(Comparator.comparingLong(x->x.time));
            long newest=points.isEmpty()?Long.MIN_VALUE:points.get(points.size()-1).time; long cutoff=newest==Long.MIN_VALUE?Long.MIN_VALUE:newest-24L*60L*60L*1000L;
            long last=Long.MIN_VALUE; for(HydroPoint hp:points){if(hp.time<cutoff||hp.time==last)continue;last=hp.time;out.times.add(hp.time);out.values.add(hp.value);}
        }catch(Exception ignored){}
        return out;
    }

    private void refreshLive(boolean force){refreshWeather(force);refreshHydro(force);}
    private void refreshWeather(boolean force){
        long age=System.currentTimeMillis()-prefs.getLong(PREF_WEATHER_UPDATED,0L);if(weatherLoading||(!force&&!prefs.getString(PREF_WEATHER_CACHE,"").isBlank()&&age<30*60000L))return;weatherLoading=true;
        new Thread(()->{try{String base="https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m&timezone=Europe%2FZurich&forecast_days=8";String raw;String src;try{raw=httpGet(base+"&models=meteoswiss_icon_seamless");src="MeteoSwiss ICON via Open-Meteo";}catch(Exception first){raw=httpGet(base);src="Open-Meteo Best Match";}new JSONObject(raw).getJSONObject("hourly");prefs.edit().putString(PREF_WEATHER_CACHE,raw).putLong(PREF_WEATHER_UPDATED,System.currentTimeMillis()).putString(PREF_WEATHER_SOURCE,src).apply();}catch(Exception ignored){}finally{weatherLoading=false;runOnUiThread(()->{if(current==Screen.HOME)navigate(Screen.HOME);});}}).start();
    }
    private void refreshHydro(boolean force){
        long age=System.currentTimeMillis()-prefs.getLong(PREF_HYDRO_UPDATED,0L);if(hydroLoading||(!force&&!prefs.getString(PREF_HYDRO_CACHE,"").isBlank()&&age<10*60000L))return;hydroLoading=true;
        new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL("https://data.bafu.admin.ch/api").openConnection();c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(7000);c.setReadTimeout(9000);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept","application/json");String query="{ water { observations { data_live(where:{stationNo:{_eq:\"2091\"}}) { stationNo parameterName timestamp value releaseStatus } } } }";String body=new JSONObject().put("query",query).toString();try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));}if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());String raw=readConnection(c);JSONObject j=new JSONObject(raw);if(j.has("errors"))throw new Exception("GraphQL");j.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");prefs.edit().putString(PREF_HYDRO_CACHE,raw).putLong(PREF_HYDRO_UPDATED,System.currentTimeMillis()).apply();}catch(Exception ignored){}finally{if(c!=null)c.disconnect();hydroLoading=false;runOnUiThread(()->{if(current==Screen.HOME)navigate(Screen.HOME);});}}).start();
    }
    private String httpGet(String url) throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();try{c.setConnectTimeout(7000);c.setReadTimeout(9000);c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/2.3");if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());return readConnection(c);}finally{c.disconnect();}}
    private String readConnection(HttpURLConnection c) throws Exception{BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),java.nio.charset.StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();return sb.toString();}

    private View settings(){
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        section(b,"Darstellung","Gilt für die native App-Oberfläche");
        LinearLayout theme=card(); theme.setOrientation(LinearLayout.VERTICAL); b.addView(theme,margin(-1,-2,0,0,0,12));
        theme.addView(txt("Farbschema",16,TEXT,true)); TextView currentTheme=txt(themeLabel(),13,MUTED,false); currentTheme.setPadding(0,dp(4),0,dp(10)); theme.addView(currentTheme);
        Button chooseTheme=btn("System / Hell / Dunkel",Color.rgb(232,240,244),NAVY); chooseTheme.setOnClickListener(v->chooseTheme()); theme.addView(chooseTheme,new LinearLayout.LayoutParams(-1,dp(44)));

        section(b,"Persönlicher Zugang","Nur lokal auf diesem Gerät gespeichert");
        LinearLayout access=card(); access.setOrientation(LinearLayout.VERTICAL); b.addView(access,margin(-1,-2,0,0,0,12));
        access.addView(txt("Interner PFVR-Link",16,TEXT,true)); String internal=prefs.getString(PREF_INTERNAL_URL,"");
        TextView status=txt(validInternal(internal)?"intern.pfvr.ch · eingerichtet":"Noch nicht eingerichtet",13,validInternal(internal)?WATER:MUTED,false); status.setPadding(0,dp(4),0,dp(10)); access.addView(status);
        Button edit=btn(validInternal(internal)?"Link ändern":"Link einrichten",NAVY,Color.WHITE); edit.setOnClickListener(v->editInternalSetting()); access.addView(edit,new LinearLayout.LayoutParams(-1,dp(46)));

        section(b,"Daten","Kalender, Training-Wetter und Rhein-Messwerte");
        LinearLayout data=card(); data.setOrientation(LinearLayout.VERTICAL); b.addView(data,margin(-1,-2,0,0,0,12));
        data.addView(txt("Lokaler Cache",16,TEXT,true)); TextView d=txt("Beim Start wird zuerst der letzte erfolgreiche Stand angezeigt und anschließend im Hintergrund aktualisiert.",13,MUTED,false); d.setPadding(0,dp(4),0,dp(10)); data.addView(d);
        Button reload=btn("Alle Daten aktualisieren",Color.rgb(232,240,244),NAVY); reload.setOnClickListener(v->{refreshEvents(true,()->{});refreshLive(true);Toast.makeText(this,"Aktualisierung gestartet.",Toast.LENGTH_SHORT).show();}); data.addView(reload,new LinearLayout.LayoutParams(-1,dp(44)));
        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY); clear.setOnClickListener(v->clearDataCache()); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(44)); clp.setMargins(0,dp(8),0,0); data.addView(clear,clp);

        section(b,"App",null);
        LinearLayout about=card(); about.setOrientation(LinearLayout.VERTICAL); b.addView(about,margin(-1,-2,0,0,0,8));
        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion 0.5.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));
        return scroll;
    }

    private boolean resolveDarkMode(){
        String mode=prefs==null?"system":prefs.getString(PREF_THEME,"system"); if("dark".equals(mode))return true; if("light".equals(mode))return false;
        int night=getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK; return night==Configuration.UI_MODE_NIGHT_YES;
    }
    private String themeLabel(){String mode=prefs.getString(PREF_THEME,"system");if("dark".equals(mode))return "Dunkel";if("light".equals(mode))return "Hell";return "System · folgt Android";}
    private void chooseTheme(){
        String[] labels={"System · folgt Android","Hell","Dunkel"}; String mode=prefs.getString(PREF_THEME,"system"); int selected="light".equals(mode)?1:("dark".equals(mode)?2:0);
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Farbschema").setSingleChoiceItems(labels,selected,(d,which)->{String value=which==1?"light":(which==2?"dark":"system");prefs.edit().putString(PREF_THEME,value).apply();d.dismiss();recreate();}).setNegativeButton("Abbrechen",null).show();
    }
    private int dialogTheme(){return darkMode?android.R.style.Theme_Material_Dialog_Alert:android.R.style.Theme_Material_Light_Dialog_Alert;}
    private void applyWindowTheme(){
        getWindow().setStatusBarColor(NAVY); getWindow().setNavigationBarColor(darkMode?DARK_SURFACE:Color.WHITE);
        int flags=getWindow().getDecorView().getSystemUiVisibility(); if(darkMode)flags&=~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;else flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    private int themeText(int c){if(!darkMode)return c;if(c==TEXT)return DARK_TEXT;if(c==MUTED||c==Color.rgb(65,82,96)||c==Color.rgb(126,140,150))return DARK_MUTED;if(c==NAVY)return Color.rgb(105,193,218);if(c==WATER)return Color.rgb(91,190,213);return c;}
    private int themeBg(int c){if(!darkMode)return c;if(c==SURFACE)return DARK_SURFACE;if(c==Color.WHITE)return DARK_CARD;if(c==Color.rgb(232,240,244)||c==Color.rgb(238,243,246)||c==Color.rgb(231,242,246)||c==Color.rgb(236,243,247))return DARK_SOFT;return c;}
    private void clearDataCache(){prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED).remove(PREF_HYDRO_CACHE).remove(PREF_HYDRO_UPDATED).apply();events=new ArrayList<>();eventsUpdated=0L;Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();refreshEvents(false,()->{});refreshLive(true);}

    private View eventScreen() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout intro=card(); intro.setOrientation(LinearLayout.VERTICAL); b.addView(intro,margin(-1,-2,0,4,0,16));
        intro.addView(txt("Jahresprogramm",22,TEXT,true));
        TextView info=txt("Direkt aus dem öffentlichen Google-Kalender des Vereins.",14,MUTED,false); info.setPadding(0,dp(5),0,dp(3)); intro.addView(info); TextView cacheInfo=txt(calendarStatus(),12,eventsUpdated>0?WATER:MUTED,false); cacheInfo.setPadding(0,0,0,dp(11)); intro.addView(cacheInfo);
        LinearLayout row=new LinearLayout(this);
        Button reload=btn("Aktualisieren",NAVY,Color.WHITE); reload.setOnClickListener(v->refreshEvents(true,()->navigate(Screen.EVENTS))); row.addView(reload,new LinearLayout.LayoutParams(0,dp(44),1));
        Button cal=btn("Kalender öffnen",Color.rgb(232,240,244),NAVY); cal.setOnClickListener(v->external(CALENDAR_WEB)); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,dp(44),1); cp.setMargins(dp(8),0,0,0); row.addView(cal,cp); intro.addView(row);
        if(events.isEmpty()) { ProgressBar p=new ProgressBar(this); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48)); lp.gravity=Gravity.CENTER_HORIZONTAL; b.addView(p,lp); TextView wait=txt(eventsLoading?"Kalender wird im Hintergrund geladen …":"Noch kein gespeicherter Kalenderstand vorhanden.",13,MUTED,false); wait.setGravity(Gravity.CENTER); wait.setPadding(0,dp(8),0,0); b.addView(wait); if(!eventsLoading) refreshEvents(false,()->{if(current==Screen.EVENTS) navigate(Screen.EVENTS);}); return scroll; }
        String month="";
        for(Event e:events) {
            String m=cap(e.start.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.GERMAN)));
            if(!m.equals(month)) { TextView mt=txt(m,17,TEXT,true); mt.setPadding(dp(2),dp(9),0,dp(8)); b.addView(mt); month=m; }
            b.addView(eventCard(e,false));
        }
        return scroll;
    }

    private View eventCard(Event e, boolean compact) {
        LinearLayout c=card(); c.setGravity(Gravity.CENTER_VERTICAL); c.setPadding(dp(14),dp(12),dp(14),dp(12)); c.setLayoutParams(margin(-1,-2,0,0,0,9));
        LinearLayout date=new LinearLayout(this); date.setOrientation(LinearLayout.VERTICAL); date.setGravity(Gravity.CENTER); date.setBackground(round(Color.rgb(231,242,246),14));
        TextView day=txt(String.valueOf(e.start.getDayOfMonth()),23,NAVY,true); day.setGravity(Gravity.CENTER); date.addView(day);
        TextView mon=txt(e.start.format(DateTimeFormatter.ofPattern("MMM",Locale.GERMAN)).replace(".","").toUpperCase(Locale.GERMAN),11,WATER,true); mon.setGravity(Gravity.CENTER); date.addView(mon);
        c.addView(date,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout d=new LinearLayout(this); d.setOrientation(LinearLayout.VERTICAL); d.setPadding(dp(13),0,0,0); c.addView(d,new LinearLayout.LayoutParams(0,-2,1));
        d.addView(txt(e.title,compact?15:16,TEXT,true));
        String when=cap(e.start.format(DateTimeFormatter.ofPattern("EEEE",Locale.GERMAN))); if(!e.allDay) when+=" · "+e.start.format(DateTimeFormatter.ofPattern("HH:mm"))+" Uhr"; d.addView(txt(when,13,MUTED,false));
        if(!compact && e.location!=null && !e.location.isBlank()) d.addView(txt(e.location,12,WATER,false));
        return c;
    }

    private View cash() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(dp(20),dp(18),dp(20),dp(18)); GradientDrawable gb=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(21,90,122),WATER}); gb.setCornerRadius(dp(22)); hero.setBackground(gb); b.addView(hero,margin(-1,-2,0,4,0,16));
        hero.addView(txt("VEREINSBEIZ",12,Color.rgb(208,231,239),true)); TextView ht=txt("Konsumation bezahlen",27,Color.WHITE,true); ht.setPadding(0,dp(5),0,dp(5)); hero.addView(ht); hero.addView(txt("Freien Betrag eingeben. Preise und Warenkorb ergänzen wir später aus der Preisliste.",14,Color.rgb(232,243,247),false));

        LinearLayout amountCard=card(); amountCard.setOrientation(LinearLayout.VERTICAL); b.addView(amountCard,margin(-1,-2,0,0,0,12)); amountCard.addView(txt("Betrag",14,MUTED,true));
        LinearLayout amountRow=new LinearLayout(this); amountRow.setGravity(Gravity.CENTER_VERTICAL); amountRow.setPadding(0,dp(8),0,dp(8)); amountCard.addView(amountRow);
        TextView chf=txt("CHF",20,NAVY,true); chf.setGravity(Gravity.CENTER_VERTICAL); amountRow.addView(chf,new LinearLayout.LayoutParams(dp(55),dp(56)));
        EditText amount=new EditText(this); amount.setHint("0.00"); amount.setTextSize(25); amount.setSingleLine(true); amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); amount.setTextColor(themeText(TEXT)); amount.setHintTextColor(themeText(MUTED)); amount.setBackground(round(Color.rgb(238,243,246),14)); amount.setPadding(dp(14),0,dp(14),0); amountRow.addView(amount,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView hi=txt("Der Swiss QR enthält Empfänger, IBAN, Betrag und Zahlungszweck. So gehen die Zahlungsdaten vollständig mit.",12,MUTED,false); hi.setPadding(0,0,0,dp(10)); amountCard.addView(hi);
        Button qr=btn("Swiss QR mit Betrag erstellen",NAVY,Color.WHITE); qr.setOnClickListener(v->showPaymentQr(amount)); amountCard.addView(qr,new LinearLayout.LayoutParams(-1,dp(52)));
        String bankLabel=prefs.getString(PREF_BANK_LABEL,""); Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY); bankButton=bank; bank.setOnClickListener(v->openPreferred(false,amount)); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(46)); bp.setMargins(0,dp(8),0,0); amountCard.addView(bank,bp);
        if(!bankLabel.trim().isEmpty()) { TextView change=link("Andere Banking-App wählen"); change.setOnClickListener(v->chooseApp(false,amount)); amountCard.addView(change); }

        LinearLayout tw=card(); tw.setOrientation(LinearLayout.VERTICAL); b.addView(tw,margin(-1,-2,0,0,0,12)); tw.addView(txt("TWINT",16,TEXT,true)); TextView ti=txt("Alternative. Ohne separaten Vereins-Zahlungslink öffnet die App deine TWINT-App; bezahlt wird über den bestehenden Vereins-QR.",13,MUTED,false); ti.setPadding(0,dp(4),0,dp(10)); tw.addView(ti); Button twb=btn("TWINT öffnen",Color.rgb(232,240,244),NAVY); twb.setOnClickListener(v->openPreferred(true,amount)); tw.addView(twb,new LinearLayout.LayoutParams(-1,dp(46)));

        section(b,"Zahlungsdaten","Für E-Banking und manuelle Überweisung");
        LinearLayout details=card(); details.setOrientation(LinearLayout.VERTICAL); b.addView(details,margin(-1,-2,0,0,0,12)); details.addView(txt(CLUB_PAYEE,16,TEXT,true)); details.addView(txt("Rheinweg · 4310 Rheinfelden",13,MUTED,false)); TextView iban=txt(CLUB_IBAN,19,NAVY,true); iban.setPadding(0,dp(12),0,dp(4)); details.addView(iban); details.addView(txt(CLUB_PAYMENT_NOTE,13,MUTED,false));
        LinearLayout copies=new LinearLayout(this); copies.setPadding(0,dp(12),0,0); details.addView(copies); Button ci=btn("IBAN kopieren",Color.rgb(232,240,244),NAVY); ci.setOnClickListener(v->copy("PFVR IBAN",CLUB_IBAN.replace(" ",""),"IBAN kopiert")); copies.addView(ci,new LinearLayout.LayoutParams(0,dp(42),1)); Button ca=btn("Alles kopieren",Color.rgb(232,240,244),NAVY); ca.setOnClickListener(v->{String x=CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE; String a=amount(amount.getText().toString()); if(a!=null)x+="\nCHF "+a; copy("PFVR Zahlung",x,"Zahlungsdaten kopiert");}); LinearLayout.LayoutParams cap=new LinearLayout.LayoutParams(0,dp(42),1); cap.setMargins(dp(8),0,0,0); copies.addView(ca,cap);
        LinearLayout future=card(); future.setOrientation(LinearLayout.VERTICAL); b.addView(future,margin(-1,-2,0,0,0,8)); future.addView(txt("Preisliste",16,TEXT,true)); future.addView(txt("Noch nicht hinterlegt. Nach dem Foto der Preise ergänzen wir Artikel, Mengen und automatische Summe; der freie Betrag bleibt.",13,MUTED,false));
        return scroll;
    }

    private void showPaymentQr(EditText amountInput) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,"Bitte zuerst einen gültigen CHF-Betrag eingeben.",Toast.LENGTH_LONG).show();return;}
        try {
            Bitmap qr=makeSwissQr(a);
            pendingQrBitmap=qr;
            LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(8),dp(16),0);
            ImageView image=new ImageView(this); image.setImageBitmap(qr); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(image,new LinearLayout.LayoutParams(-1,dp(330)));
            TextView details=txt("CHF "+a+"\n"+CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE,14,TEXT,false); details.setGravity(Gravity.CENTER); details.setPadding(0,dp(8),0,dp(4)); box.addView(details);
            TextView note=txt("QR speichern und in der Banking-App aus Datei/Foto importieren, sofern die Bank das unterstützt. Alternativ von einem zweiten Gerät scannen.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);
            new AlertDialog.Builder(this,dialogTheme())
                    .setTitle("Bankzahlung · Swiss QR")
                    .setView(box)
                    .setPositiveButton("Banking-App öffnen",(d,w)->openPreferred(false,amountInput))
                    .setNeutralButton("QR speichern",(d,w)->saveQr(a))
                    .setNegativeButton("Schliessen",null)
                    .show();
        } catch(Exception e) {
            Toast.makeText(this,"Swiss QR konnte nicht erzeugt werden.",Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap makeSwissQr(String amount) throws Exception {
        String[] fields=new String[]{
                "SPC","0200","1",CLUB_IBAN.replace(" ",""),
                "S",CLUB_PAYEE,"Rheinweg","","4310","Rheinfelden","CH",
                "","","","","","","",
                amount,"CHF",
                "","","","","","","",
                "NON","",CLUB_PAYMENT_NOTE,"EPD","","",""
        };
        String payload=String.join("\r\n",fields);
        Map<EncodeHintType,Object> hints=new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET,"UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN,4);
        int size=900;
        BitMatrix matrix=new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE,size,size,hints);
        Bitmap bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);
        for(int y=0;y<size;y++)for(int x=0;x<size;x++)bitmap.setPixel(x,y,matrix.get(x,y)?Color.BLACK:Color.WHITE);
        Canvas canvas=new Canvas(bitmap); Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        float mark=size*(7f/46f); float left=(size-mark)/2f, top=(size-mark)/2f, cx=size/2f, cy=size/2f;
        paint.setColor(Color.BLACK); canvas.drawRect(left,top,left+mark,top+mark,paint);
        paint.setColor(Color.WHITE); float thick=mark*0.22f, arm=mark*0.64f;
        canvas.drawRect(cx-thick/2,cy-arm/2,cx+thick/2,cy+arm/2,paint);
        canvas.drawRect(cx-arm/2,cy-thick/2,cx+arm/2,cy+thick/2,paint);
        return bitmap;
    }

    private void saveQr(String amount) {
        if(pendingQrBitmap==null){Toast.makeText(this,"Kein QR-Code vorhanden.",Toast.LENGTH_SHORT).show();return;}
        Intent save=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        save.addCategory(Intent.CATEGORY_OPENABLE); save.setType("image/png");
        save.putExtra(Intent.EXTRA_TITLE,"PFVR-Zahlung-CHF-"+amount.replace('.','_')+".png");
        try{startActivityForResult(save,REQ_SAVE_QR);}catch(Exception e){Toast.makeText(this,"Speichern ist auf diesem Gerät nicht verfügbar.",Toast.LENGTH_LONG).show();}
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_SAVE_QR && resultCode==RESULT_OK && data!=null && data.getData()!=null && pendingQrBitmap!=null){
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())){
                if(out==null||!pendingQrBitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new Exception("write failed");
                Toast.makeText(this,"Swiss QR gespeichert.",Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,"QR-Code konnte nicht gespeichert werden.",Toast.LENGTH_LONG).show();}
        }
    }

    private void openPreferred(boolean twint, EditText amountInput) {
        String pkg=prefs.getString(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE,"");
        if(pkg.isBlank()) { chooseApp(twint,amountInput); return; }
        Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
        if(launch==null) { prefs.edit().remove(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE).remove(twint?PREF_TWINT_LABEL:PREF_BANK_LABEL).apply(); chooseApp(twint,amountInput); return; }
        copyAmount(amountInput); try { startActivity(launch); } catch(Exception e) { chooseApp(twint,amountInput); }
    }

    private void chooseApp(boolean twint, EditText amountInput) {
        PackageManager pm=getPackageManager(); Intent q=new Intent(Intent.ACTION_MAIN); q.addCategory(Intent.CATEGORY_LAUNCHER); List<ResolveInfo> all=pm.queryIntentActivities(q,0); List<AppChoice> found=new ArrayList<>();
        for(ResolveInfo r:all) {
            if(r.activityInfo==null || r.activityInfo.packageName==null || r.activityInfo.packageName.equals(getPackageName())) continue;
            String label=String.valueOf(r.loadLabel(pm)); String hay=(label+" "+r.activityInfo.packageName).toLowerCase(Locale.ROOT); boolean isTwint=hay.contains("twint"); boolean isBank=isTwint||any(hay,"bank","banking","neon","yuh","revolut","ubs","raiffeisen","postfinance","kantonal","zkb","bcv","bekb","cler","zak","migros");
            if((twint&&isTwint)||(!twint&&isBank&&!isTwint)) found.add(new AppChoice(label,r.activityInfo.packageName));
        }
        if(found.isEmpty()&&!twint) for(ResolveInfo r:all) if(r.activityInfo!=null && r.activityInfo.packageName!=null && !r.activityInfo.packageName.equals(getPackageName())) found.add(new AppChoice(String.valueOf(r.loadLabel(pm)),r.activityInfo.packageName));
        found.sort((a,b)->{int pa=bankPriority(a),pb=bankPriority(b);if(pa!=pb)return Integer.compare(pa,pb);return a.label.compareToIgnoreCase(b.label);});
        if(found.isEmpty()) { if(twint) { try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("market://search?q=TWINT&c=apps")));}catch(Exception e){external("https://www.twint.ch/privatkunden/");} } else Toast.makeText(this,"Keine passende Banking-App gefunden.",Toast.LENGTH_LONG).show(); return; }
        String[] labels=new String[found.size()]; for(int i=0;i<found.size();i++) labels[i]=found.get(i).label;
        new AlertDialog.Builder(this,dialogTheme()).setTitle(twint?"TWINT-App auswählen":"Banking-App auswählen").setItems(labels,(d,i)->{AppChoice c=found.get(i); prefs.edit().putString(twint?PREF_TWINT_PACKAGE:PREF_BANK_PACKAGE,c.pkg).putString(twint?PREF_TWINT_LABEL:PREF_BANK_LABEL,c.label).apply(); if(!twint&&bankButton!=null)bankButton.setText(c.label+" öffnen"); copyAmount(amountInput); Intent launch=pm.getLaunchIntentForPackage(c.pkg); if(launch!=null) startActivity(launch);}).setNegativeButton("Abbrechen",null).show();
    }

    private int bankPriority(AppChoice app) {
        String h=(app.label+" "+app.pkg).toLowerCase(Locale.ROOT);
        // Large and commonly used Swiss retail banks first; niche/fintech apps follow.
        if(h.contains("ubs")) return 10;
        if(h.contains("postfinance")) return 20;
        if(h.contains("raiffeisen")) return 30;
        if(h.contains("zkb")||h.contains("zürcher kantonal")||h.contains("zuercher kantonal")) return 40;
        if(h.contains("kantonal")||h.contains("bcv")||h.contains("bekb")||h.contains("bkb")||h.contains("blkb")||h.contains("akb")||h.contains("sgkb")||h.contains("luzerner")||h.contains("thurgauer")||h.contains("graubündner")||h.contains("graubuendner")) return 50;
        if(h.contains("migros")) return 60;
        if(h.contains("cler")||h.contains("zak")) return 70;
        if(h.contains("neon")) return 100;
        if(h.contains("yuh")) return 110;
        if(h.contains("revolut")) return 120;
        if(h.contains("swissquote")) return 130;
        return 200;
    }

    private void copyAmount(EditText input) { String a=amount(input==null?null:input.getText().toString()); if(a==null){Toast.makeText(this,"Banking-App geöffnet. Betrag noch nicht eingegeben.",Toast.LENGTH_SHORT).show();return;} copy("PFVR Betrag",a,"CHF "+a+" kopiert"); }
    private String amount(String raw) { if(raw==null)return null; try{double n=Double.parseDouble(raw.trim().replace(',','.')); if(n<=0||n>100000)return null; return String.format(Locale.US,"%.2f",n);}catch(Exception e){return null;} }
    private boolean any(String s,String... xs){for(String x:xs)if(s.contains(x))return true;return false;}
    private void copy(String label,String value,String toast){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText(label,value)); Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();}

    private View club() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout top=card(); top.setGravity(Gravity.CENTER_VERTICAL); ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.pfvr_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP); top.addView(logo,new LinearLayout.LayoutParams(dp(82),dp(82))); LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(15),0,0,0); info.addView(txt("Pontonierfahrverein Rheinfelden",19,TEXT,true)); info.addView(txt("Gegründet 1896 · Sport und Vereinsleben am Rhein",13,MUTED,false)); top.addView(info,new LinearLayout.LayoutParams(0,-2,1)); b.addView(top,margin(-1,-2,0,4,0,18));
        section(b,"Verein",null); b.addView(action("Verein / Sport","Allgemeine Informationen","Öffnen",v->openInApp(CLUB,"Verein / Sport"))); b.addView(action("Vorstand","Ansprechpersonen und Funktionen","Öffnen",v->openInApp(BOARD,"Vorstand"))); b.addView(action("Geschichte","Tradition seit 1896","Öffnen",v->openInApp(HISTORY,"Geschichte"))); b.addView(action("Jahresprogramm","Originalseite und Kalenderhinweise","Öffnen",v->openInApp(PROGRAM,"Jahresprogramm"))); b.addView(action("News-Archiv","Ältere Beiträge auf pfvr.ch","Öffnen",v->openInApp(NEWS,"News-Archiv"))); b.addView(action("Interner Bereich","An-/Abmeldung und interne PFVR-Seite","Öffnen",v->navigate(Screen.INTERNAL)));
        section(b,"Kontakt",null); b.addView(contact("Depot","Rheinweg 42\n4310 Rheinfelden","Route",v->openMap())); b.addView(contact("Telefon","076 209 18 96","Anrufen",v->startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:+41762091896"))))); b.addView(contact("E-Mail","info@pfvr.ch","Schreiben",v->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:info@pfvr.ch"))))); b.addView(contact("Kontaktseite","pfvr.ch/kontakt","Öffnen",v->openInApp(CONTACT,"Kontakt"))); return scroll;
    }

    private View internal() {
        String url=prefs.getString(PREF_INTERNAL_URL,""); if(!validInternal(url)) return internalMissing();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(themeBg(Color.WHITE));
        LinearLayout tools=new LinearLayout(this); tools.setPadding(dp(9),dp(8),dp(9),dp(8)); tools.setBackgroundColor(themeBg(Color.rgb(236,243,247))); root.addView(tools,new LinearLayout.LayoutParams(-1,dp(56)));
        WebView web=web(false); activeWebView=web;
        Button back=btn("‹ Zurück",Color.WHITE,NAVY); back.setOnClickListener(v->handleBack()); tools.addView(back,new LinearLayout.LayoutParams(0,dp(40),1));
        Button start=btn("Start",NAVY,Color.WHITE); start.setOnClickListener(v->web.loadUrl(prefs.getString(PREF_INTERNAL_URL,""))); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,dp(40),1); sp.setMargins(dp(7),0,0,0); tools.addView(start,sp);
        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->web.reload()); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); web.loadUrl(url); return root;
    }

    private View internalMissing() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(20),dp(20),dp(20),dp(20)); b.addView(c,margin(-1,-2,0,20,0,0));
        c.addView(txt("Kein Intern-Zugang eingerichtet",21,TEXT,true));
        TextView p=txt("Der persönliche PFVR-Link wird zentral unter Einstellungen verwaltet.",14,MUTED,false); p.setPadding(0,dp(7),0,dp(15)); c.addView(p);
        Button settings=btn("Zu den Einstellungen",NAVY,Color.WHITE); settings.setOnClickListener(v->navigate(Screen.SETTINGS)); c.addView(settings,new LinearLayout.LayoutParams(-1,dp(48)));
        return scroll;
    }

    private void editInternalSetting(){
        EditText input=new EditText(this); input.setText(prefs.getString(PREF_INTERNAL_URL,"")); input.setHint("https://intern.pfvr.ch/…"); input.setTextColor(themeText(TEXT)); input.setHintTextColor(themeText(MUTED)); input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI); input.setPadding(dp(12),dp(10),dp(12),dp(10)); input.setBackground(round(Color.rgb(238,243,246),12));
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Persönlichen Intern-Link ändern").setView(input)
            .setPositiveButton("Speichern",(d,w)->{String x=input.getText().toString().trim();if(validInternal(x)){prefs.edit().putString(PREF_INTERNAL_URL,x).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);}else Toast.makeText(this,"Ungültiger https://intern.pfvr.ch-Link",Toast.LENGTH_LONG).show();})
            .setNeutralButton("Entfernen",(d,w)->{prefs.edit().remove(PREF_INTERNAL_URL).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);})
            .setNegativeButton("Abbrechen",null).show();
    }

    private boolean validInternal(String x){if(x==null||x.isBlank())return false;try{Uri u=Uri.parse(x);return "https".equalsIgnoreCase(u.getScheme())&&"intern.pfvr.ch".equalsIgnoreCase(u.getHost());}catch(Exception e){return false;}}

    private View webScreen(String url,boolean simplify){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);root.addView(p,new LinearLayout.LayoutParams(-1,dp(2)));WebView w=web(simplify);activeWebView=w;w.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){p.setProgress(n);p.setVisibility(n>=100?View.GONE:View.VISIBLE);}});root.addView(w,new LinearLayout.LayoutParams(-1,0,1));w.loadUrl(url);return root;}
    private WebView web(boolean simplify){WebView w=new WebView(this);w.setBackgroundColor(simplify?Color.WHITE:themeBg(Color.WHITE));WebSettings s=w.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setCacheMode(WebSettings.LOAD_DEFAULT);s.setLoadsImagesAutomatically(true);s.setLoadWithOverviewMode(true);s.setUseWideViewPort(true);s.setSupportZoom(false);s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);String ua=s.getUserAgentString();if(ua!=null)s.setUserAgentString(ua.replace("; wv","").replace("Version/4.0 ",""));CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(w,true);w.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();String h=u.getHost()==null?"":u.getHost().toLowerCase(Locale.ROOT);if(h.endsWith("pfvr.ch")||h.endsWith("google.com"))return false;external(u.toString());return true;}@Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);if(simplify&&u.contains("pfvr.ch"))skin(v);}});return w;}
    private void skin(WebView v){
        // Keep embedded PFVR content in a controlled light presentation. The native shell may stay dark,
        // but forcing arbitrary WordPress/PDF content dark caused unreadable white-on-white combinations.
        String css="html{color-scheme:light!important;}header,.site-header,.header-wrapper,nav,.main-navigation,footer,.site-footer,.scroll-top,.back-to-top{display:none!important;}html,body{background:#F4F7F9!important;}body{margin:0!important;padding:14px 14px 40px!important;font-family:Arial,sans-serif!important;color:#15232E!important;}main,.site-content,.content-area,.container,.wrapper{width:100%!important;max-width:none!important;margin:0!important;padding:0!important;}article,.post,.entry,.entry-content{background:#FFFFFF!important;color:#15232E!important;border-radius:16px!important;padding:16px!important;margin:0 0 14px!important;box-shadow:0 2px 10px rgba(0,0,0,.10)!important;}article p,article li,article span,.entry-content p,.entry-content li,.entry-content span,.entry-content div{color:#15232E!important;}img{max-width:100%!important;height:auto!important;border-radius:12px!important;}iframe{background:#FFFFFF!important;}a{color:#247E99!important;}h1,h2,h3,h4,h5,h6{color:#0C2D48!important;}";
        String js="(function(){var s=document.getElementById('pfvr-app-style');if(!s){s=document.createElement('style');s.id='pfvr-app-style';document.head.appendChild(s);}s.innerHTML='"+css.replace("\\","\\\\").replace("'","\\'")+"';})();"; v.evaluateJavascript(js,null);
    }
    private void openInApp(String url,String title){headerSubtitle.setText(title);content.removeAllViews();content.addView(webScreen(url,true));}

    private void loadCachedEvents(){String raw=prefs.getString(PREF_ICS_CACHE,"");eventsUpdated=prefs.getLong(PREF_ICS_UPDATED,0L);if(raw.trim().isEmpty())return;try{events=parseIcs(raw);}catch(Exception ex){events=new ArrayList<>();eventsUpdated=0L;prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).apply();}}
    private String calendarStatus(){if(eventsUpdated<=0)return eventsLoading?"Erster Abruf läuft im Hintergrund.":"Noch kein lokaler Kalender-Cache.";ZonedDateTime z=java.time.Instant.ofEpochMilli(eventsUpdated).atZone(ZoneId.of("Europe/Zurich"));String d=z.toLocalDate().equals(LocalDate.now(ZoneId.of("Europe/Zurich")))?"heute "+z.format(DateTimeFormatter.ofPattern("HH:mm")):z.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));return "Lokal gespeichert · zuletzt aktualisiert "+d+" Uhr";}
    private void refreshEvents(boolean toast,Runnable done){if(eventsLoading){if(toast)Toast.makeText(this,"Kalender-Aktualisierung läuft bereits.",Toast.LENGTH_SHORT).show();return;}eventsLoading=true;new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(ICS).openConnection();c.setConnectTimeout(6000);c.setReadTimeout(8000);c.setUseCaches(true);c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/2.1.1");c.setRequestProperty("Accept","text/calendar,text/plain,*/*");if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),java.nio.charset.StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append(System.lineSeparator());br.close();String raw=sb.toString();List<Event> parsed=parseIcs(raw);if(parsed.isEmpty())throw new Exception("Keine kommenden Termine im Feed");long updated=System.currentTimeMillis();prefs.edit().putString(PREF_ICS_CACHE,raw).putLong(PREF_ICS_UPDATED,updated).apply();runOnUiThread(()->{events=parsed;eventsUpdated=updated;eventsLoading=false;if(toast)Toast.makeText(this,parsed.size()+" kommende Termine aktualisiert",Toast.LENGTH_SHORT).show();if(done!=null)done.run();});}catch(Exception ex){runOnUiThread(()->{eventsLoading=false;if(toast){String m=events.isEmpty()?"Kalender konnte gerade nicht geladen werden.":"Keine Verbindung – gespeicherter Kalenderstand bleibt sichtbar.";Toast.makeText(this,m,Toast.LENGTH_LONG).show();}else if(events.isEmpty())Toast.makeText(this,"Kalender lädt im Hintergrund. Bei langsamer Verbindung kann der erste Abruf etwas dauern.",Toast.LENGTH_LONG).show();if(done!=null)done.run();});}finally{if(c!=null)c.disconnect();}}).start();}

    private List<Event> parseIcs(String raw){List<String> lines=unfold(raw);List<Event> base=new ArrayList<>();Event e=null;for(String line:lines){if(line.equals("BEGIN:VEVENT"))e=new Event();else if(line.equals("END:VEVENT")&&e!=null){if(e.start!=null&&e.title!=null&&!e.title.isBlank())base.add(e);e=null;}else if(e!=null){int p=line.indexOf(':');if(p<0)continue;String k=line.substring(0,p),v=unesc(line.substring(p+1));if(k.startsWith("SUMMARY"))e.title=v;else if(k.startsWith("LOCATION"))e.location=v;else if(k.startsWith("DTSTART")){ParsedDate d=date(k,v);if(d!=null){e.start=d.z;e.allDay=d.allDay;}}else if(k.startsWith("DTEND")){ParsedDate d=date(k,v);if(d!=null)e.end=d.z;}else if(k.startsWith("RRULE"))e.rule=v;else if(k.startsWith("EXDATE"))for(String x:v.split(",")){ParsedDate d=date(k,x);if(d!=null)e.ex.add(d.z.toLocalDate());}}}ZonedDateTime now=ZonedDateTime.now(ZoneId.of("Europe/Zurich")).minusHours(6),limit=now.plusMonths(14);List<Event> out=new ArrayList<>();for(Event x:base)expand(x,limit,out);out.removeIf(x->x.start.isBefore(now)||x.start.isAfter(limit));out.sort(Comparator.comparing(x->x.start));Set<String> seen=new LinkedHashSet<>();List<Event> unique=new ArrayList<>();for(Event x:out){String id=x.start+"|"+x.title;if(seen.add(id))unique.add(x);}return unique;}
    private List<String> unfold(String raw){List<String> out=new ArrayList<>();for(String l:raw.replace("\r\n","\n").split("\n")){if((l.startsWith(" ")||l.startsWith("\t"))&&!out.isEmpty())out.set(out.size()-1,out.get(out.size()-1)+l.substring(1));else out.add(l);}return out;}
    private String unesc(String s){return s.replace("\\n","\n").replace("\\N","\n").replace("\\,",",").replace("\\;",";").replace("\\\\","\\");}
    private ParsedDate date(String key,String v){try{ZoneId z=ZoneId.of("Europe/Zurich");if(key.contains("VALUE=DATE")||v.length()==8)return new ParsedDate(LocalDate.parse(v,DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay(z),true);if(v.endsWith("Z"))return new ParsedDate(ZonedDateTime.parse(v,DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")).withZoneSameInstant(z),false);String tz=null;int t=key.indexOf("TZID=");if(t>=0){tz=key.substring(t+5);int semi=tz.indexOf(';');if(semi>=0)tz=tz.substring(0,semi);}ZoneId zone=tz==null?z:ZoneId.of(tz);DateTimeFormatter f=v.length()>=15?DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"):DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");return new ParsedDate(LocalDateTime.parse(v,f).atZone(zone).withZoneSameInstant(z),false);}catch(Exception ex){return null;}}
    private void expand(Event e,ZonedDateTime limit,List<Event> out){if(e.rule==null||e.rule.isBlank()){out.add(e.copy(e.start));return;}Map<String,String> r=new HashMap<>();for(String p:e.rule.split(";")){int q=p.indexOf('=');if(q>0)r.put(p.substring(0,q),p.substring(q+1));}String f=r.getOrDefault("FREQ","");int interval=intval(r.get("INTERVAL"),1),count=intval(r.get("COUNT"),10000),made=0;ZonedDateTime until=limit;if(r.get("UNTIL")!=null){ParsedDate d=date("DTSTART",r.get("UNTIL"));if(d!=null&&d.z.isBefore(until))until=d.z;}if("WEEKLY".equals(f)){List<DayOfWeek> days=days(r.get("BYDAY"));if(days.isEmpty())days.add(e.start.getDayOfWeek());LocalDate week=e.start.toLocalDate().minusDays(e.start.getDayOfWeek().getValue()-1L);for(int w=0;made<count;w+=interval){LocalDate base=week.plusWeeks(w);if(base.atStartOfDay(e.start.getZone()).isAfter(until))break;for(DayOfWeek d:days){ZonedDateTime o=ZonedDateTime.of(base.plusDays(d.getValue()-1L),e.start.toLocalTime(),e.start.getZone());if(o.isBefore(e.start)||o.isAfter(until))continue;made++;if(!e.ex.contains(o.toLocalDate()))out.add(e.copy(o));if(made>=count)break;}}}else{ZonedDateTime o=e.start;while(made<count&&!o.isAfter(until)){made++;if(!e.ex.contains(o.toLocalDate()))out.add(e.copy(o));if("DAILY".equals(f))o=o.plusDays(interval);else if("MONTHLY".equals(f))o=o.plusMonths(interval);else if("YEARLY".equals(f))o=o.plusYears(interval);else break;}}}
    private int intval(String x,int d){try{return x==null?d:Integer.parseInt(x);}catch(Exception e){return d;}}
    private List<DayOfWeek> days(String s){List<DayOfWeek> x=new ArrayList<>();if(s==null)return x;Map<String,DayOfWeek> m=Map.of("MO",DayOfWeek.MONDAY,"TU",DayOfWeek.TUESDAY,"WE",DayOfWeek.WEDNESDAY,"TH",DayOfWeek.THURSDAY,"FR",DayOfWeek.FRIDAY,"SA",DayOfWeek.SATURDAY,"SU",DayOfWeek.SUNDAY);for(String d:s.split(","))if(m.containsKey(d))x.add(m.get(d));x.sort(Comparator.comparingInt(DayOfWeek::getValue));return x;}

    private LinearLayout body(){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(14),dp(12),dp(14),dp(28));b.setBackgroundColor(themeBg(SURFACE));return b;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(round(Color.WHITE,18));return c;}
    private View action(String title,String sub,String go,View.OnClickListener l){LinearLayout c=card();c.setGravity(Gravity.CENTER_VERTICAL);LinearLayout g=new LinearLayout(this);g.setOrientation(LinearLayout.VERTICAL);g.addView(txt(title,16,TEXT,true));g.addView(txt(sub,13,MUTED,false));c.addView(g,new LinearLayout.LayoutParams(0,-2,1));TextView a=txt(go+"  →",13,WATER,true);a.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);c.addView(a,new LinearLayout.LayoutParams(-2,dp(48)));c.setOnClickListener(l);c.setLayoutParams(margin(-1,-2,0,0,0,9));return c;}
    private View contact(String title,String detail,String action,View.OnClickListener l){LinearLayout c=card();c.setGravity(Gravity.CENTER_VERTICAL);LinearLayout g=new LinearLayout(this);g.setOrientation(LinearLayout.VERTICAL);g.addView(txt(title,14,TEXT,true));g.addView(txt(detail,14,MUTED,false));c.addView(g,new LinearLayout.LayoutParams(0,-2,1));Button b=btn(action,Color.rgb(232,240,244),NAVY);b.setOnClickListener(l);c.addView(b,new LinearLayout.LayoutParams(-2,dp(40)));c.setLayoutParams(margin(-1,-2,0,0,0,9));return c;}
    private void section(LinearLayout p,String title,String sub){TextView h=txt(title,20,TEXT,true);h.setPadding(dp(2),dp(2),0,sub==null?dp(10):dp(2));p.addView(h);if(sub!=null){TextView s=txt(sub,12,MUTED,false);s.setPadding(dp(2),0,0,dp(10));p.addView(s);}}
    private TextView link(String s){TextView t=txt(s,14,WATER,true);t.setGravity(Gravity.END);t.setPadding(dp(4),dp(5),dp(4),dp(14));return t;}
    private TextView txt(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(themeText(color));t.setLineSpacing(0,1.08f);if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);return t;}
    private Button btn(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextSize(13);b.setTextColor(themeText(fg));b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT_BOLD);b.setPadding(dp(12),0,dp(12),0);b.setMinHeight(0);b.setMinWidth(0);b.setBackground(round(bg,12));return b;}
    private GradientDrawable round(int color,float r){GradientDrawable d=new GradientDrawable();d.setColor(themeBg(color));d.setCornerRadius(dp(r));return d;}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private String cap(String s){return s==null||s.isEmpty()?s:s.substring(0,1).toUpperCase(Locale.GERMAN)+s.substring(1);}
    private void external(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,"Link konnte nicht geöffnet werden.",Toast.LENGTH_SHORT).show();}}
    private void openMap(){Uri u=Uri.parse("geo:0,0?q="+Uri.encode("Rheinweg 42, 4310 Rheinfelden, Schweiz"));try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){external("https://www.google.com/maps/search/?api=1&query="+Uri.encode("Rheinweg 42, 4310 Rheinfelden, Schweiz"));}}

    private void handleBack(){if(activeWebView!=null&&activeWebView.canGoBack())activeWebView.goBack();else if(current!=Screen.HOME)navigate(Screen.HOME);else super.onBackPressed();}
    @Override public void onBackPressed(){handleBack();}
    @Override protected void onDestroy(){if(activeWebView!=null)activeWebView.destroy();super.onDestroy();}

    private static class HydroPoint {final long time;final double value;HydroPoint(long t,double v){time=t;value=v;}}
    private static class TrendSeries {List<Long> times=new ArrayList<>();List<Double> values=new ArrayList<>();}
    private class TrendView extends View {
        private final TrendSeries series; private final String unit; private final int lineColor;
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG), grid=new Paint(Paint.ANTI_ALIAS_FLAG), label=new Paint(Paint.ANTI_ALIAS_FLAG), axis=new Paint(Paint.ANTI_ALIAS_FLAG);
        TrendView(Context c,TrendSeries s,String u,int color){super(c);series=s;unit=u;lineColor=color;line.setStrokeWidth(dp(2));line.setStyle(Paint.Style.STROKE);grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);axis.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);axis.setTypeface(Typeface.DEFAULT_BOLD);setPadding(0,0,0,0);}
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);if(series.values.size()<2||series.times.size()!=series.values.size())return;
            float w=getWidth(),h=getHeight(),left=dp(50),right=w-dp(7),top=dp(18),bottom=h-dp(30);
            double min=Double.POSITIVE_INFINITY,max=Double.NEGATIVE_INFINITY;for(double v:series.values){min=Math.min(min,v);max=Math.max(max,v);}if(!(max>min)){max=min+1;}
            long minT=series.times.get(0),maxT=series.times.get(series.times.size()-1);if(maxT<=minT)maxT=minT+1;
            grid.setColor(darkMode?Color.rgb(58,72,82):Color.rgb(220,229,234));
            for(int i=0;i<3;i++){float y=top+(bottom-top)*i/2f;canvas.drawLine(left,y,right,y,grid);}for(int i=0;i<3;i++){float x=left+(right-left)*i/2f;canvas.drawLine(x,top,x,bottom,grid);}
            Path path=new Path();for(int i=0;i<series.values.size();i++){float x=left+(right-left)*(series.times.get(i)-minT)/(float)(maxT-minT);float y=(float)(bottom-(series.values.get(i)-min)/(max-min)*(bottom-top));if(i==0)path.moveTo(x,y);else path.lineTo(x,y);}line.setColor(lineColor);canvas.drawPath(path,line);
            label.setColor(themeText(MUTED));axis.setColor(themeText(MUTED));
            canvas.drawText(fmtTrend(max),dp(2),top+dp(4),label);canvas.drawText(fmtTrend(min),dp(2),bottom,label);canvas.drawText(unit,dp(2),dp(10),axis);
            ZoneId zone=ZoneId.of("Europe/Zurich");DateTimeFormatter tf=DateTimeFormatter.ofPattern("HH:mm");String first=java.time.Instant.ofEpochMilli(minT).atZone(zone).format(tf),last=java.time.Instant.ofEpochMilli(maxT).atZone(zone).format(tf);canvas.drawText(first,left,h-dp(6),label);float lw=label.measureText(last);canvas.drawText(last,right-lw,h-dp(6),label);String xLabel="Zeit · 24 h";float xw=axis.measureText(xLabel);canvas.drawText(xLabel,left+(right-left-xw)/2f,h-dp(6),axis);
        }
        private String fmtTrend(double v){return Math.abs(v)>=100?String.format(Locale.GERMAN,"%.1f",v):String.format(Locale.GERMAN,"%.2f",v);}
    }

    private static class AppChoice {final String label,pkg;AppChoice(String l,String p){label=l;pkg=p;}}
    private static class ParsedDate {final ZonedDateTime z;final boolean allDay;ParsedDate(ZonedDateTime z,boolean a){this.z=z;allDay=a;}}
    private static class Event {String title,location,rule;ZonedDateTime start,end;boolean allDay;Set<LocalDate> ex=new LinkedHashSet<>();Event copy(ZonedDateTime s){Event x=new Event();x.title=title;x.location=location;x.start=s;x.allDay=allDay;if(end!=null&&start!=null)x.end=s.plus(java.time.Duration.between(start,end));return x;}}
}
