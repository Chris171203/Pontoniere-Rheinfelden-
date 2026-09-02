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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.CalendarContract;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
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

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

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
    private static final String PREF_HYDRO_FINE_CACHE = "hydro_fine_cache";
    private static final String PREF_HYDRO_FINE_UPDATED = "hydro_fine_updated";
    private static final String PREF_HYDRO_HISTORY_CACHE = "hydro_history_cache";
    private static final String PREF_HYDRO_HISTORY_UPDATED = "hydro_history_updated";
    private static final String PREF_THEME = "theme_mode";
    private static final String PREF_INTERNAL_APP_VIEW = "internal_app_view";
    private static final String PREF_BACKGROUND_REFRESH = "background_refresh";
    private static final String PREF_RIVER_LOW = "river_low";
    private static final String PREF_RIVER_WARN = "river_warn";
    private static final String PREF_RIVER_ALARM = "river_alarm";
    private static final String PREF_RIVER_RANGE = "river_range";
    private static final String PREF_RIVER_METRIC = "river_metric";
    private static final String PREF_RIVER_SLOT1_STATION = "river_slot1_station";
    private static final String PREF_RIVER_SLOT1_METRIC = "river_slot1_metric";
    private static final String PREF_RIVER_SLOT2_STATION = "river_slot2_station";
    private static final String PREF_RIVER_SLOT2_METRIC = "river_slot2_metric";
    private static final String PREF_RIVER_SLOT2_ENABLED = "river_slot2_enabled";
    private static final float DEFAULT_RIVER_LOW = 400f;
    private static final float DEFAULT_RIVER_WARN = 2500f;
    private static final float DEFAULT_RIVER_ALARM = 3600f;
    private static final String BACKGROUND_WORK_NAME = "pfvr-public-data-refresh";

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
    private static final String TWINT_QR_PDF = "https://www.pfvr.ch/wp-content/uploads/Seiten/vereinsbeiz_zahlung/Twint_QR.pdf";
    private static final String TWINT_DIRECT_URL = "https://www.pfvr.ch/vereinsbeiz-zahlung/";

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
    private static final int STATUS_LOW = Color.rgb(43,142,166);
    private static final int STATUS_GOOD = Color.rgb(22,134,58);
    private static final int STATUS_WARN = Color.rgb(242,201,76);
    private static final int STATUS_ALARM = Color.rgb(200,55,55);

    private enum Screen { HOME, EVENTS, CASH, CLUB, SETTINGS, INTERNAL }

    private enum RiverRange {
        HOUR("1h", 60L * 60L * 1000L, "Livewerte"),
        DAY("24h", 24L * 60L * 60L * 1000L, "10-Minuten-Mittel"),
        WEEK("7d", 7L * 24L * 60L * 60L * 1000L, "Stundenmittel");

        final String label;
        final long windowMs;
        final String sourceLabel;

        RiverRange(String label,long windowMs,String sourceLabel){
            this.label=label;this.windowMs=windowMs;this.sourceLabel=sourceLabel;
        }

        static RiverRange from(String value){
            for(RiverRange range:values())if(range.label.equals(value))return range;
            return DAY;
        }
    }

    private enum RiverMetric {
        FLOW("Abfluss","Q","m³/s",Color.rgb(220,137,63),0),
        LEVEL("Pegel","W","m ü.M.",WATER,2),
        TEMPERATURE("Temperatur","WT","°C",Color.rgb(70,157,177),1);

        final String label,parameter,unit;
        final int color,decimals;

        RiverMetric(String label,String parameter,String unit,int color,int decimals){
            this.label=label;this.parameter=parameter;this.unit=unit;this.color=color;this.decimals=decimals;
        }

        static RiverMetric from(String value){
            for(RiverMetric metric:values())if(metric.parameter.equals(value))return metric;
            return FLOW;
        }
    }

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
    private final Map<String,Integer> cashCart = new LinkedHashMap<>();
    private final Map<String,TextView> cashQuantityViews = new HashMap<>();
    private CashCatalog.Catalog cashCatalog;
    private LinearLayout cashSummaryContainer;
    private TextView cashTotalView;
    private volatile boolean weatherLoading = false;
    private volatile boolean hydroLoading = false;
    private boolean darkMode = false;
    private ScrollView homeScroll;
    private LinearLayout homeLiveStack;
    private Handler dataRefreshHandler;
    private final Runnable dataRefreshTick = new Runnable(){@Override public void run(){refreshLive(false);if(eventsUpdated<=0L||System.currentTimeMillis()-eventsUpdated>=60L*60L*1000L)refreshEvents(false,null);if(dataRefreshHandler!=null)dataRefreshHandler.postDelayed(this,5L*60L*1000L);}};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        dataRefreshHandler = new Handler(Looper.getMainLooper());
        scheduleBackgroundRefresh();
        darkMode = resolveDarkMode();
        loadCachedEvents();
        applyWindowTheme();
        setContentView(buildShell());
        navigate(Screen.HOME);
        refreshEvents(false, () -> {
            if (current == Screen.HOME) rebuildHomePreservingScroll();
            else if (current == Screen.EVENTS) navigate(Screen.EVENTS);
        });
        refreshLive(false);
    }

    @Override protected void onResume(){
        super.onResume();
        if(prefs!=null){
            loadCachedEvents();
            refreshLive(false);
            if(current==Screen.HOME)rebuildHomePreservingScroll();
            else if(current==Screen.EVENTS)navigate(Screen.EVENTS);
        }
        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}
    }

    @Override protected void onPause(){
        if(dataRefreshHandler!=null)dataRefreshHandler.removeCallbacks(dataRefreshTick);
        super.onPause();
    }

    private void scheduleBackgroundRefresh(){
        if(prefs==null)return;
        WorkManager wm=WorkManager.getInstance(getApplicationContext());
        if(!prefs.getBoolean(PREF_BACKGROUND_REFRESH,true)){wm.cancelUniqueWork(BACKGROUND_WORK_NAME);return;}
        Constraints constraints=new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        PeriodicWorkRequest request=new PeriodicWorkRequest.Builder(BackgroundRefreshWorker.class,30,TimeUnit.MINUTES).setConstraints(constraints).build();
        wm.enqueueUniquePeriodicWork(BACKGROUND_WORK_NAME,ExistingPeriodicWorkPolicy.UPDATE,request);
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
        if(screen!=Screen.HOME){homeScroll=null;homeLiveStack=null;}
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
        homeScroll=scroll;
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

        return scroll;
    }

    private View liveInfoRow(){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        stack.setPadding(0,0,0,dp(2));
        homeLiveStack=stack;
        populateHomeLiveStack(stack);
        return stack;
    }

    private void populateHomeLiveStack(LinearLayout stack){
        stack.removeAllViews();
        LinearLayout weather=weatherCard();
        stack.addView(weather,margin(-1,-2,0,0,0,10));

        TextView rangeLabel=txt("RHEIN · ZEITRAUM",10,MUTED,true);
        rangeLabel.setPadding(dp(2),dp(2),0,dp(5));
        stack.addView(rangeLabel);
        stack.addView(riverRangeSelector(riverRange()),new LinearLayout.LayoutParams(-1,dp(42)));

        stack.addView(riverCard(1),margin(-1,-2,0,10,0,10));
        if(riverSlotEnabled(2))stack.addView(riverCard(2),margin(-1,-2,0,0,0,4));
    }

    private void refreshHomeLiveViews(){
        if(current!=Screen.HOME||homeLiveStack==null)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        populateHomeLiveStack(homeLiveStack);
        if(homeScroll!=null)homeScroll.post(()->homeScroll.scrollTo(0,scrollY));
    }

    private void rebuildHomePreservingScroll(){
        if(current!=Screen.HOME)return;
        final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
        navigate(Screen.HOME);
        if(homeScroll!=null)homeScroll.post(()->homeScroll.scrollTo(0,scrollY));
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

    private LinearLayout riverCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        double flow=currentHydroValue(station,"Q");
        RiverStatus status=riverStatus(station,flow);
        int statusColor=statusTextColor(status.bg);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(15),dp(16),dp(15));

        LinearLayout eyebrow=new LinearLayout(this);
        eyebrow.setGravity(Gravity.CENTER_VERTICAL);
        eyebrow.addView(txt("RHEIN · "+station.label.toUpperCase(Locale.GERMAN)+" · "+station.id,11,WATER,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView sourceLink=txt("BAFU  ↗",11,WATER,true);
        sourceLink.setGravity(Gravity.END);
        sourceLink.setPadding(dp(8),dp(4),0,dp(4));
        sourceLink.setOnClickListener(v->external(station.stationUrl()));
        eyebrow.addView(sourceLink,new LinearLayout.LayoutParams(-2,-2));
        card.addView(eyebrow);

        LinearLayout headline=new LinearLayout(this);
        headline.setGravity(Gravity.CENTER_VERTICAL);
        headline.setPadding(0,dp(4),0,dp(2));
        card.addView(headline);

        LinearLayout valueBox=new LinearLayout(this);
        valueBox.setGravity(Gravity.BOTTOM|Gravity.START);
        TextView value=txt(Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–",34,statusColor,true);
        value.setTextColor(statusColor);
        valueBox.addView(value);
        TextView unit=txt("m³/s",14,statusColor,true);
        unit.setTextColor(statusColor);
        unit.setPadding(dp(5),0,0,dp(5));
        valueBox.addView(unit);
        headline.addView(valueBox,new LinearLayout.LayoutParams(0,dp(54),1f));

        LinearLayout statusArea=new LinearLayout(this);
        statusArea.setGravity(Gravity.CENTER|Gravity.END);
        statusArea.addView(riverStatusPill(status));
        headline.addView(statusArea,new LinearLayout.LayoutParams(0,dp(54),0.9f));

        TextView thresholdHint=txt(String.format(Locale.GERMAN,"Abflussstatus · Niedrig < %.0f · Warn ab %.0f · Alarm ab %.0f m³/s",riverLow(station),riverWarn(station),riverAlarm(station)),10,MUTED,false);
        thresholdHint.setPadding(0,0,0,dp(7));
        card.addView(thresholdHint);

        addRiverChartSection(card,station,RiverMetric.FLOW,range,true);
        addRiverChartSection(card,station,RiverMetric.LEVEL,range,false);
        if(station.supportsTemperature)addRiverChartSection(card,station,RiverMetric.TEMPERATURE,range,false);

        TextView hint=txt("Diagramme berühren, um Einzelwerte anzuzeigen.",10,MUTED,false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0,dp(5),0,0);
        card.addView(hint);

        TextView src=txt(summary[3]+" · "+range.sourceLabel,10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(9),0,0);
        card.addView(src);
        return card;
    }

    private void addRiverChartSection(LinearLayout card,HydroStation station,RiverMetric metric,RiverRange range,boolean first){
        if(!first){
            View divider=new View(this);
            divider.setBackgroundColor(darkMode?Color.rgb(51,65,74):Color.rgb(226,233,237));
            LinearLayout.LayoutParams dividerParams=new LinearLayout.LayoutParams(-1,dp(1));
            dividerParams.setMargins(0,dp(12),0,dp(12));
            card.addView(divider,dividerParams);
        }

        TrendSeries series=hydroSeries(station,metric.parameter,range);
        HydroMath.Stats stats=HydroMath.stats(series.values);
        double currentValue=displayHydroValue(station,metric,currentHydroValue(station,metric.parameter));
        int valueColor=metric==RiverMetric.FLOW?statusTextColor(riverStatus(station,currentHydroValue(station,"Q")).bg):themeText(TEXT);

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txt(riverMetricTitle(metric),15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView current=txt(Double.isFinite(currentValue)?formatMetric(station,metric,currentValue)+" "+metricUnit(station,metric):"–",14,valueColor,true);
        current.setTextColor(valueColor);
        current.setGravity(Gravity.END);
        titleRow.addView(current,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        if(stats.isValid()&&stats.count>=2){
            String statsText="Ø "+formatMetric(station,metric,stats.mean)
                    +"  ·  "+formatMetric(station,metric,stats.min)+"–"+formatMetric(station,metric,stats.max)
                    +"  ·  "+trendText(stats,metric,station);
            TextView statsView=txt(statsText,11,MUTED,false);
            statsView.setPadding(0,dp(3),0,dp(3));
            card.addView(statsView);
        }

        if(series.values.size()>=2){
            RiverTrendView chart=new RiverTrendView(this,series,metric,range,station);
            LinearLayout.LayoutParams chartParams=new LinearLayout.LayoutParams(-1,dp(metric==RiverMetric.TEMPERATURE?158:176));
            chartParams.setMargins(0,dp(3),0,0);
            card.addView(chart,chartParams);
        }else{
            String message=range==RiverRange.HOUR?"Für die letzte Stunde liegen noch nicht genug Livewerte vor.":"Für diesen Zeitraum liegen noch nicht genug Messwerte vor.";
            TextView missing=txt(message,12,MUTED,false);
            missing.setGravity(Gravity.CENTER);
            missing.setPadding(dp(8),dp(18),dp(8),dp(18));
            missing.setBackground(round(Color.rgb(238,243,246),14));
            LinearLayout.LayoutParams missingParams=new LinearLayout.LayoutParams(-1,-2);
            missingParams.setMargins(0,dp(7),0,0);
            card.addView(missing,missingParams);
        }
    }

    private String riverMetricTitle(RiverMetric metric){return metric==RiverMetric.TEMPERATURE?"Wassertemperatur":metric.label;}

    private boolean riverSlotEnabled(int slot){return slot==1||prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);}

    private HydroStation riverStation(int slot){
        String key=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
        HydroStation fallback=slot==1?HydroStation.BASEL_RHEINHALLE:HydroStation.RHEINFELDEN;
        return HydroStation.from(prefs.getString(key,fallback.id),fallback);
    }

    private RiverRange riverRange(){return RiverRange.from(prefs.getString(PREF_RIVER_RANGE,RiverRange.DAY.label));}
    private RiverMetric riverMetric(){return RiverMetric.from(prefs.getString(PREF_RIVER_METRIC,RiverMetric.FLOW.parameter));}

    private View riverRangeSelector(RiverRange selected){
        LinearLayout outer=segmentedBackground();
        for(RiverRange range:RiverRange.values()){
            TextView option=segmentOption(range.label,range==selected);
            option.setContentDescription("Rheinwerte "+range.label);
            option.setOnClickListener(v->{
                prefs.edit().putString(PREF_RIVER_RANGE,range.label).apply();
                if(current==Screen.HOME)refreshHomeLiveViews();
            });
            outer.addView(option,segmentParams(outer));
        }
        return outer;
    }

    private View riverMetricSelector(RiverMetric selected){
        LinearLayout outer=segmentedBackground();
        for(RiverMetric metric:RiverMetric.values()){
            TextView option=segmentOption(metric.label,metric==selected);
            option.setContentDescription("Rheinwert "+metric.label);
            option.setOnClickListener(v->{
                prefs.edit().putString(PREF_RIVER_METRIC,metric.parameter).apply();
                if(current==Screen.HOME)navigate(Screen.HOME);
            });
            outer.addView(option,segmentParams(outer));
        }
        return outer;
    }

    private LinearLayout segmentedBackground(){
        LinearLayout outer=new LinearLayout(this);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(3),dp(3),dp(3),dp(3));
        outer.setBackground(round(Color.rgb(232,240,244),14));
        return outer;
    }

    private TextView segmentOption(String label,boolean active){
        TextView option=txt(label,13,active?Color.WHITE:MUTED,true);
        option.setTextColor(active?Color.WHITE:themeText(MUTED));
        option.setGravity(Gravity.CENTER);
        option.setBackground(active?round(NAVY,11):null);
        return option;
    }

    private LinearLayout.LayoutParams segmentParams(LinearLayout parent){
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-1,1);
        if(parent.getChildCount()>0)params.setMargins(dp(3),0,0,0);
        return params;
    }

    private View riverStatusPill(RiverStatus status){
        int color=statusTextColor(status.bg);
        LinearLayout pill=new LinearLayout(this);
        pill.setGravity(Gravity.CENTER_VERTICAL);
        pill.setPadding(dp(10),dp(7),dp(12),dp(7));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(Color.argb(darkMode?48:22,Color.red(color),Color.green(color),Color.blue(color)));
        bg.setStroke(dp(1),Color.argb(darkMode?140:80,Color.red(color),Color.green(color),Color.blue(color)));
        bg.setCornerRadius(dp(18));
        pill.setBackground(bg);
        View dot=new View(this);
        dot.setBackground(statusDot(color));
        pill.addView(dot,new LinearLayout.LayoutParams(dp(8),dp(8)));
        TextView label=txt(status.label,14,color,true);
        label.setTextColor(color);
        label.setPadding(dp(7),0,0,0);
        pill.addView(label);
        return pill;
    }

    private GradientDrawable statusDot(int color){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);return d;}

    private View riverMetrics(HydroMath.Stats stats,RiverRange range,RiverMetric metric,HydroStation station){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        addMetric(row,formatMetric(station,metric,stats.mean),"Ø "+range.label);
        addMetric(row,formatMetric(station,metric,stats.min)+"–"+formatMetric(station,metric,stats.max),"Min–Max");
        addMetric(row,trendText(stats,metric,station),"Start → Ende");
        return row;
    }

    private void addMetric(LinearLayout row,String value,String label){
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(5),dp(8),dp(5),dp(8));
        tile.setBackground(round(Color.rgb(238,243,246),12));
        TextView valueView=txt(value,14,TEXT,true);valueView.setGravity(Gravity.CENTER);tile.addView(valueView);
        TextView labelView=txt(label,10,MUTED,false);labelView.setGravity(Gravity.CENTER);labelView.setPadding(0,dp(2),0,0);tile.addView(labelView);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
        if(row.getChildCount()>0)lp.setMargins(dp(6),0,0,0);
        row.addView(tile,lp);
    }

    private String trendText(HydroMath.Stats stats,RiverMetric metric,HydroStation station){
        double change=stats.change();
        double tolerance;
        if(metric==RiverMetric.FLOW)tolerance=Math.max(1d,Math.abs(stats.last)*0.0025d);
        else if(metric==RiverMetric.LEVEL)tolerance=station==HydroStation.BASEL_RHEINHALLE?1d:0.01d;
        else tolerance=0.1d;
        if(Math.abs(change)<=tolerance)return "→ stabil";
        return (change>0?"↗ +":"↘ −")+formatMetric(station,metric,Math.abs(change));
    }

    private String formatMetric(RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(metric.decimals==0)return String.format(Locale.GERMAN,"%.0f",value);
        if(metric.decimals==1)return String.format(Locale.GERMAN,"%.1f",value);
        return String.format(Locale.GERMAN,"%.2f",value);
    }

    private String formatMetric(HydroStation station,RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL)return String.format(Locale.GERMAN,"%.0f",value);
        return formatMetric(metric,value);
    }

    private String metricUnit(HydroStation station,RiverMetric metric){
        return station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL?"cm":metric.unit;
    }

    private double displayHydroValue(HydroStation station,RiverMetric metric,double rawValue){
        if(!Double.isFinite(rawValue))return rawValue;
        return station==HydroStation.BASEL_RHEINHALLE&&metric==RiverMetric.LEVEL?baselLevelCm(rawValue):rawValue;
    }

    private double baselLevelCm(double metres){return (metres-240.0d)*100.0d;}

    private View thresholdGrid(){return thresholdGrid(HydroStation.RHEINFELDEN);}

    private View thresholdGrid(HydroStation station){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        LinearLayout first=new LinearLayout(this);
        addThresholdTile(first,"Niedrig",String.format(Locale.GERMAN,"< %.0f",riverLow(station)),STATUS_LOW);
        addThresholdTile(first,"Gut",String.format(Locale.GERMAN,"%.0f – < %.0f",riverLow(station),riverWarn(station)),STATUS_GOOD);
        stack.addView(first,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout second=new LinearLayout(this);
        addThresholdTile(second,"Warnung",String.format(Locale.GERMAN,"%.0f – < %.0f",riverWarn(station),riverAlarm(station)),STATUS_WARN);
        addThresholdTile(second,"Alarm",String.format(Locale.GERMAN,"ab %.0f",riverAlarm(station)),STATUS_ALARM);
        LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(-1,-2);
        secondParams.setMargins(0,dp(6),0,0);
        stack.addView(second,secondParams);
        return stack;
    }

    private void addThresholdTile(LinearLayout row,String label,String range,int statusColor){
        int color=statusTextColor(statusColor);
        LinearLayout tile=new LinearLayout(this);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(9),dp(7),dp(9),dp(7));
        GradientDrawable background=new GradientDrawable();
        background.setColor(themeBg(Color.rgb(238,243,246)));
        background.setStroke(dp(1),Color.argb(darkMode?95:46,Color.red(color),Color.green(color),Color.blue(color)));
        background.setCornerRadius(dp(12));
        tile.setBackground(background);
        View dot=new View(this);
        dot.setBackground(statusDot(color));
        tile.addView(dot,new LinearLayout.LayoutParams(dp(7),dp(7)));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(7),0,0,0);
        TextView name=txt(label,11,color,true);
        name.setTextColor(color);
        copy.addView(name);
        copy.addView(txt(range,10,MUTED,false));
        tile.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,-2,1);
        if(row.getChildCount()>0)params.setMargins(dp(6),0,0,0);
        row.addView(tile,params);
    }

    private int statusTextColor(int color){
        if(!darkMode){if(color==STATUS_WARN)return Color.rgb(166,108,0);return color;}
        if(color==STATUS_LOW)return Color.rgb(91,190,213);
        if(color==STATUS_GOOD)return Color.rgb(73,196,111);
        if(color==STATUS_WARN)return Color.rgb(255,216,92);
        if(color==STATUS_ALARM)return Color.rgb(255,105,105);
        return DARK_MUTED;
    }

    private GradientDrawable statusBadge(int statusColor){
        int color=statusTextColor(statusColor);
        GradientDrawable background=new GradientDrawable();
        background.setColor(Color.argb(darkMode?44:22,Color.red(color),Color.green(color),Color.blue(color)));
        background.setStroke(dp(1),Color.argb(darkMode?170:105,Color.red(color),Color.green(color),Color.blue(color)));
        background.setCornerRadius(dp(14));
        return background;
    }

    private boolean summerTraining(LocalDate day){int month=day.getMonthValue();return month>=4&&month<=9;}
    private boolean regularTrainingDay(LocalDate day){DayOfWeek weekday=day.getDayOfWeek();return summerTraining(day)?(weekday==DayOfWeek.MONDAY||weekday==DayOfWeek.WEDNESDAY):weekday==DayOfWeek.THURSDAY;}
    private LocalTime regularTrainingStart(LocalDate day){return summerTraining(day)?LocalTime.of(18,30):LocalTime.of(19,30);}
    private LocalTime regularTrainingEnd(LocalDate day){return summerTraining(day)?LocalTime.of(20,0):LocalTime.of(21,0);}

    private TrainingSlot nextTrainingSlot(){
        ZoneId zone=ZoneId.of("Europe/Zurich");
        ZonedDateTime now=ZonedDateTime.now(zone);
        TrainingSlot explicit=nextExplicitCalendarTraining(now);
        TrainingSlot regular=nextRegularTraining(now,zone);
        if(explicit==null)return regular;
        if(regular==null)return explicit;
        return explicit.start.isBefore(regular.start)?explicit:regular;
    }

    private TrainingSlot nextExplicitCalendarTraining(ZonedDateTime now){
        TrainingSlot best=null;
        ZonedDateTime limit=now.plusDays(21);
        for(Event event:events){
            if(event.start==null||event.start.isAfter(limit)||isCancelledEvent(event))continue;
            ZonedDateTime end=event.end!=null?event.end:(event.allDay?event.start.plusDays(1):event.start.plusMinutes(90));
            if(!end.isAfter(now))continue;
            if(!TrainingMatcher.isExplicitTraining(event.title,event.description,event.start.getHour(),event.allDay))continue;
            TrainingSlot slot=trainingSlotFromEvent(event);
            if(best==null||slot.start.isBefore(best.start))best=slot;
        }
        return best;
    }

    private TrainingSlot nextRegularTraining(ZonedDateTime now,ZoneId zone){
        LocalDate first=now.toLocalDate();
        for(int offset=0;offset<21;offset++){
            LocalDate day=first.plusDays(offset);
            if(!regularTrainingDay(day)||isCancelledTrainingDate(day))continue;
            Event override=trainingOverrideForDate(day);
            if(override!=null){
                TrainingSlot slot=trainingSlotFromEvent(override);
                if(slot.end.isAfter(now))return slot;
                continue;
            }
            ZonedDateTime start=day.atTime(regularTrainingStart(day)).atZone(zone);
            ZonedDateTime end=day.atTime(regularTrainingEnd(day)).atZone(zone);
            if(end.isAfter(now))return new TrainingSlot(start,end,false,"Regelmässiges Training");
        }
        LocalDate fallback=first.plusDays(1);
        while(!regularTrainingDay(fallback))fallback=fallback.plusDays(1);
        return new TrainingSlot(fallback.atTime(regularTrainingStart(fallback)).atZone(zone),fallback.atTime(regularTrainingEnd(fallback)).atZone(zone),false,"Regelmässiges Training");
    }

    private TrainingSlot trainingSlotFromEvent(Event event){
        ZoneId zone=ZoneId.of("Europe/Zurich");
        LocalDate day=event.start.toLocalDate();
        ZonedDateTime start=event.allDay?day.atTime(regularTrainingStart(day)).atZone(zone):event.start;
        ZonedDateTime end=event.allDay?day.atTime(regularTrainingEnd(day)).atZone(zone):(event.end!=null?event.end:start.plusMinutes(90));
        if(!end.isAfter(start))end=start.plusMinutes(90);
        return new TrainingSlot(start,end,true,event.title);
    }

    private Event trainingOverrideForDate(LocalDate day){
        Event best=null;
        int bestScore=Integer.MIN_VALUE;
        for(Event event:events){
            if(event.start==null||!event.start.toLocalDate().equals(day)||isCancelledEvent(event))continue;
            int score=TrainingMatcher.score(event.title,event.description,event.start.getHour(),event.allDay);
            if(score<4)continue;
            if(score>bestScore||(score==bestScore&&best!=null&&event.start.isBefore(best.start))){best=event;bestScore=score;}
        }
        return best;
    }

    private boolean isCancelledTrainingDate(LocalDate day){
        for(Event event:events){
            if(event.start==null||!event.start.toLocalDate().equals(day))continue;
            if(TrainingMatcher.isRelevant(event.title,event.description,event.start.getHour(),event.allDay)&&isCancelledEvent(event))return true;
        }
        return false;
    }

    private boolean isCancelledEvent(Event event){return TrainingMatcher.isCancelled(event.status,event.title,event.description);}

    private String trainingTimeLabel(TrainingSlot slot){
        DateTimeFormatter format=DateTimeFormatter.ofPattern("HH:mm");
        return slot.start.format(format)+"–"+slot.end.format(format)+" Uhr";
    }

    private boolean weatherHourMatches(String timestamp,TrainingSlot slot){
        try{
            ZonedDateTime hour=LocalDateTime.parse(timestamp).atZone(ZoneId.of("Europe/Zurich"));
            ZonedDateTime intervalEnd=hour.plusHours(1);
            return intervalEnd.isAfter(slot.start)&&hour.isBefore(slot.end);
        }catch(Exception ignored){return false;}
    }

    private String[] weatherSummary(){
        TrainingSlot slot=nextTrainingSlot();
        String date=cap(slot.start.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN)))+" · "+trainingTimeLabel(slot);
        if(slot.fromCalendar&&slot.title!=null&&!slot.title.isBlank())date+="\n"+slot.title;
        String raw=prefs.getString(PREF_WEATHER_CACHE,"");
        long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L);
        String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        String provenance=(slot.fromCalendar?"Vereinskalender":"Regelplan")+" · "+weatherAge(source,updated);
        if(raw.trim().isEmpty())return new String[]{"NÄCHSTES TRAINING",date,"Wetter wird geladen …","Prognose wird im Hintergrund aktualisiert.",provenance,"◌"};
        try{
            JSONObject hourly=new JSONObject(raw).getJSONObject("hourly");
            JSONArray times=hourly.getJSONArray("time"),temperatures=hourly.getJSONArray("temperature_2m"),probabilities=hourly.getJSONArray("precipitation_probability"),precipitation=hourly.getJSONArray("precipitation"),codes=hourly.getJSONArray("weather_code"),wind=hourly.getJSONArray("wind_speed_10m"),gusts=hourly.getJSONArray("wind_gusts_10m");
            JSONArray uv=hourly.optJSONArray("uv_index");
            double firstTemperature=Double.NaN,lastTemperature=Double.NaN,precipitationSum=0,windMax=0,gustMax=0,uvMax=Double.NaN;
            int probabilityMax=0,codeValue=-1,count=0;
            for(int i=0;i<times.length();i++){
                if(!weatherHourMatches(times.optString(i,""),slot))continue;
                double temperature=temperatures.optDouble(i,Double.NaN);
                if(count==0){firstTemperature=temperature;codeValue=codes.optInt(i,-1);}
                lastTemperature=temperature;
                probabilityMax=Math.max(probabilityMax,probabilities.optInt(i,0));
                precipitationSum+=Math.max(0,precipitation.optDouble(i,0));
                windMax=Math.max(windMax,wind.optDouble(i,0));
                gustMax=Math.max(gustMax,gusts.optDouble(i,0));
                if(uv!=null){double value=uv.optDouble(i,Double.NaN);if(Double.isFinite(value)&&(Double.isNaN(uvMax)||value>uvMax))uvMax=value;}
                count++;
            }
            if(count==0)return new String[]{"NÄCHSTES TRAINING",date,"Noch keine Prognose","Für diesen Trainingszeitraum liegen noch keine Stundenwerte vor.",provenance,"◌"};
            String temperatureText=Double.isNaN(firstTemperature)?"":String.format(Locale.GERMAN,"%.0f °C",firstTemperature);
            if(Double.isFinite(lastTemperature)&&Double.isFinite(firstTemperature)&&Math.abs(lastTemperature-firstTemperature)>=1.0)temperatureText+=String.format(Locale.GERMAN," → %.0f °C",lastTemperature);
            String main=temperatureText+(temperatureText.isEmpty()?"":" · ")+weatherCode(codeValue);
            String details="Regen "+probabilityMax+" % · "+String.format(Locale.GERMAN,"%.1f mm",precipitationSum)+"\nWind "+Math.round(windMax)+" km/h · Böen "+Math.round(gustMax)+" km/h";
            if(Double.isFinite(uvMax))details+="\nUV "+String.format(Locale.GERMAN,"%.1f",uvMax)+" · "+uvLabel(uvMax);
            return new String[]{"NÄCHSTES TRAINING",date,main,details,provenance,weatherIcon(codeValue)};
        }catch(Exception e){
            return new String[]{"NÄCHSTES TRAINING",date,"Gespeicherte Wetterdaten nicht lesbar","Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen.",provenance,"◌"};
        }
    }

    private String uvLabel(double uv){if(uv<3)return "niedrig";if(uv<6)return "mässig";if(uv<8)return "hoch";if(uv<11)return "sehr hoch";return "extrem";}

    private String weatherIcon(int c){if(c==0)return "☀";if(c<=2)return "⛅";if(c==3)return "☁";if(c==45||c==48)return "🌫";if(c>=51&&c<=67)return "🌧";if(c>=71&&c<=77)return "❄";if(c>=80&&c<=82)return "🌦";if(c>=85&&c<=86)return "🌨";if(c>=95)return "⚡";return "◌";}

    private String weatherAge(String source,long updated){if(updated<=0)return source;long min=Math.max(0,(System.currentTimeMillis()-updated)/60000);return source+(min>90?" · Cache "+(min/60)+" h":" · vor "+min+" min");}
    private String weatherCode(int c){if(c==0)return "klar";if(c<=2)return "leicht bewölkt";if(c==3)return "bewölkt";if(c==45||c==48)return "Nebel";if(c>=51&&c<=57)return "Nieselregen";if(c>=61&&c<=67)return "Regen";if(c>=71&&c<=77)return "Schnee";if(c>=80&&c<=82)return "Schauer";if(c>=85&&c<=86)return "Schneeschauer";if(c>=95)return "Gewitter";return "Wetter";}

    private double currentHydroValue(String parameter){return currentHydroValue(HydroStation.RHEINFELDEN,parameter);}

    private double currentHydroValue(HydroStation station,String parameter){
        String raw=prefs.getString(station.liveCacheKey(),"");
        if(raw.isBlank())return Double.NaN;
        try{
            JSONArray data=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            String latest="";
            double value=Double.NaN;
            for(int i=0;i<data.length();i++){
                JSONObject row=data.getJSONObject(i);
                if(!parameter.equals(row.optString("parameterName","")))continue;
                String timestamp=row.optString("timestamp","");
                if(timestamp.compareTo(latest)>0){latest=timestamp;value=row.optDouble("value",Double.NaN);}
            }
            return value;
        }catch(Exception ignored){return Double.NaN;}
    }

    private float riverLow(){return riverLow(HydroStation.RHEINFELDEN);}
    private float riverWarn(){return riverWarn(HydroStation.RHEINFELDEN);}
    private float riverAlarm(){return riverAlarm(HydroStation.RHEINFELDEN);}

    private float riverLow(HydroStation station){
        if(prefs.contains(station.lowPreferenceKey()))return prefs.getFloat(station.lowPreferenceKey(),station.defaultLow);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_LOW))return prefs.getFloat(PREF_RIVER_LOW,station.defaultLow);
        return station.defaultLow;
    }
    private float riverWarn(HydroStation station){
        if(prefs.contains(station.warnPreferenceKey()))return prefs.getFloat(station.warnPreferenceKey(),station.defaultWarn);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_WARN))return prefs.getFloat(PREF_RIVER_WARN,station.defaultWarn);
        return station.defaultWarn;
    }
    private float riverAlarm(HydroStation station){
        if(prefs.contains(station.alarmPreferenceKey()))return prefs.getFloat(station.alarmPreferenceKey(),station.defaultAlarm);
        if(station==HydroStation.RHEINFELDEN&&prefs.contains(PREF_RIVER_ALARM))return prefs.getFloat(PREF_RIVER_ALARM,station.defaultAlarm);
        return station.defaultAlarm;
    }

    private RiverStatus riverStatus(double flow){return riverStatus(HydroStation.RHEINFELDEN,flow);}
    private RiverStatus riverStatus(HydroStation station,double flow){
        if(Double.isNaN(flow))return new RiverStatus("Keine Daten",Color.rgb(109,120,128),Color.WHITE);
        if(flow<riverLow(station))return new RiverStatus("Niedrig",STATUS_LOW,Color.WHITE);
        if(flow>=riverAlarm(station))return new RiverStatus("Alarm",STATUS_ALARM,Color.WHITE);
        if(flow>=riverWarn(station))return new RiverStatus("Warnung",STATUS_WARN,Color.rgb(23,34,43));
        return new RiverStatus("Gut",STATUS_GOOD,Color.WHITE);
    }

    private String[] hydroSummary(){return hydroSummary(HydroStation.RHEINFELDEN);}
    private String[] hydroSummary(HydroStation station){
        String raw=prefs.getString(station.liveCacheKey(),"");
        long cache=prefs.getLong(station.liveUpdatedKey(),0L);
        String title="RHEIN · "+station.label+" · "+station.id;
        if(raw.isBlank())return new String[]{title,"Wird geladen …","Abfluss · Pegel"+(station.supportsTemperature?" · Temperatur":""),"BAFU Live-Daten"};
        try{
            JSONArray data=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray("data_live");
            Map<String,Double> values=new HashMap<>();
            Map<String,String> timestamps=new HashMap<>();
            for(int i=0;i<data.length();i++){
                JSONObject row=data.getJSONObject(i);
                String parameter=row.optString("parameterName","");
                String timestamp=row.optString("timestamp","");
                if(!(parameter.equals("Q")||parameter.equals("W")||parameter.equals("WT")))continue;
                if(!timestamps.containsKey(parameter)||timestamp.compareTo(timestamps.get(parameter))>0){timestamps.put(parameter,timestamp);values.put(parameter,row.optDouble("value",Double.NaN));}
            }
            double q=values.getOrDefault("Q",Double.NaN),w=values.getOrDefault("W",Double.NaN),wt=values.getOrDefault("WT",Double.NaN);
            String latest="";for(String timestamp:timestamps.values())if(timestamp.compareTo(latest)>0)latest=timestamp;
            String main=Double.isNaN(q)?station.label:String.format(Locale.GERMAN,"%.0f m³/s",q);
            StringBuilder sub=new StringBuilder();
            if(!Double.isNaN(w))sub.append(String.format(Locale.GERMAN,"Pegel %.2f m ü.M.",w));
            if(station.supportsTemperature&&!Double.isNaN(wt)){if(sub.length()>0)sub.append("\n");sub.append(String.format(Locale.GERMAN,"Wasser %.1f °C",wt));}
            String stand="BAFU "+station.id;
            try{if(!latest.isBlank())stand+=" · Stand "+java.time.Instant.parse(latest).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception ignored){}
            if(cache>0&&(System.currentTimeMillis()-cache)>45*60000L)stand+=" · Cache";
            return new String[]{title,main,sub.length()==0?"Messwerte derzeit unvollständig":sub.toString(),stand};
        }catch(Exception ignored){return new String[]{title,"Gespeicherter Stand","Messdaten nicht lesbar","BAFU · Cache"};}
    }

    private TrendSeries hydroSeries(String parameter,RiverRange range){return hydroSeries(HydroStation.RHEINFELDEN,parameter,range);}
    private TrendSeries hydroSeries(HydroStation station,String parameter,RiverRange range){
        TrendSeries result=new TrendSeries();
        TreeMap<Long,Double> points=new TreeMap<>();
        String live=prefs.getString(station.liveCacheKey(),"");
        String fine=prefs.getString(station.fineCacheKey(),"");
        String history=prefs.getString(station.historyCacheKey(),"");

        if(range==RiverRange.HOUR){
            appendHydroPoints(points,live,"data_live",parameter);
        }else if(range==RiverRange.DAY){
            appendHydroPoints(points,fine,"data_10min_mean",parameter);
            long after=points.isEmpty()?Long.MIN_VALUE:points.lastKey();
            appendHydroTail(points,live,"data_live",parameter,after,9L*60L*1000L);
            if(points.size()<2)appendHydroPoints(points,history,"data_1hour_mean",parameter);
        }else{
            appendHydroPoints(points,history,"data_1hour_mean",parameter);
            HydroPoint latest=latestHydroPoint(live,"data_live",parameter);
            if(latest!=null)points.put(latest.time,latest.value);
        }
        if(points.size()<2)appendHydroPoints(points,live,"data_live",parameter);
        if(points.isEmpty())return result;

        long newest=points.lastKey();
        long cutoff=newest-range.windowMs;
        for(Map.Entry<Long,Double> entry:points.entrySet()){
            if(entry.getKey()<cutoff)continue;
            result.times.add(entry.getKey());
            result.values.add(entry.getValue());
        }
        if(station==HydroStation.BASEL_RHEINHALLE&&"W".equals(parameter)){
            for(int index=0;index<result.values.size();index++)result.values.set(index,baselLevelCm(result.values.get(index)));
        }
        return result;
    }

    private void appendHydroPoints(TreeMap<Long,Double> points,String raw,String arrayName,String parameter){
        if(raw==null||raw.trim().isEmpty())return;
        try{
            JSONArray data=new JSONObject(raw).getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray(arrayName);
            for(int i=0;i<data.length();i++){
                JSONObject row=data.getJSONObject(i);
                if(!parameter.equals(row.optString("parameterName","")))continue;
                double value=row.optDouble("value",Double.NaN);
                String timestamp=row.optString("timestamp","");
                if(!Double.isFinite(value)||timestamp.isEmpty())continue;
                try{points.put(java.time.Instant.parse(timestamp).toEpochMilli(),value);}catch(Exception ignored){}
            }
        }catch(Exception ignored){}
    }

    private void appendHydroTail(TreeMap<Long,Double> points,String raw,String arrayName,String parameter,long after,long minimumSpacing){
        TreeMap<Long,Double> tail=new TreeMap<>();
        appendHydroPoints(tail,raw,arrayName,parameter);
        long lastAdded=after;
        for(Map.Entry<Long,Double> entry:tail.entrySet()){
            if(entry.getKey()<=after)continue;
            if(lastAdded!=Long.MIN_VALUE&&entry.getKey()-lastAdded<minimumSpacing)continue;
            points.put(entry.getKey(),entry.getValue());
            lastAdded=entry.getKey();
        }
        if(!tail.isEmpty()&&tail.lastKey()>after)points.put(tail.lastKey(),tail.lastEntry().getValue());
    }

    private HydroPoint latestHydroPoint(String raw,String arrayName,String parameter){
        TreeMap<Long,Double> points=new TreeMap<>();
        appendHydroPoints(points,raw,arrayName,parameter);
        if(points.isEmpty())return null;
        Map.Entry<Long,Double> latest=points.lastEntry();
        return new HydroPoint(latest.getKey(),latest.getValue());
    }

    private void refreshLive(boolean force){refreshWeather(force);refreshHydro(force);}

    private void refreshWeather(boolean force){
        long age=System.currentTimeMillis()-prefs.getLong(PREF_WEATHER_UPDATED,0L);
        if(weatherLoading||(!force&&!prefs.getString(PREF_WEATHER_CACHE,"").isBlank()&&age<30*60000L))return;
        weatherLoading=true;
        new Thread(()->{
            try{
                String base="https://api.open-meteo.com/v1/forecast?latitude=47.5544&longitude=7.7940&hourly=temperature_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,uv_index&timezone=Europe%2FZurich&forecast_days=8";
                String raw;
                String source;
                try{raw=httpGet(base+"&models=meteoswiss_icon_seamless");source="MeteoSwiss ICON via Open-Meteo";}
                catch(Exception first){raw=httpGet(base);source="Open-Meteo Best Match";}
                new JSONObject(raw).getJSONObject("hourly");
                prefs.edit().putString(PREF_WEATHER_CACHE,raw).putLong(PREF_WEATHER_UPDATED,System.currentTimeMillis()).putString(PREF_WEATHER_SOURCE,source).apply();
            }catch(Exception ignored){}
            finally{
                weatherLoading=false;
                runOnUiThread(this::refreshHomeLiveViews);
            }
        }).start();
    }

    private void refreshHydro(boolean force){
        if(hydroLoading)return;
        long now=System.currentTimeMillis();
        boolean needsRefresh=force;
        for(HydroStation station:HydroStation.values()){
            boolean liveFresh=!prefs.getString(station.liveCacheKey(),"").isBlank()&&now-prefs.getLong(station.liveUpdatedKey(),0L)<10*60000L;
            boolean fineFresh=!prefs.getString(station.fineCacheKey(),"").isBlank()&&now-prefs.getLong(station.fineUpdatedKey(),0L)<30*60000L;
            boolean historyFresh=!prefs.getString(station.historyCacheKey(),"").isBlank()&&now-prefs.getLong(station.historyUpdatedKey(),0L)<60*60000L;
            if(!(liveFresh&&fineFresh&&historyFresh))needsRefresh=true;
        }
        if(!needsRefresh)return;
        hydroLoading=true;
        new Thread(()->{
            try{
                String quote=String.valueOf((char)34);
                for(HydroStation station:HydroStation.values()){
                    long check=System.currentTimeMillis();
                    boolean liveFresh=!prefs.getString(station.liveCacheKey(),"").isBlank()&&check-prefs.getLong(station.liveUpdatedKey(),0L)<10*60000L;
                    boolean fineFresh=!prefs.getString(station.fineCacheKey(),"").isBlank()&&check-prefs.getLong(station.fineUpdatedKey(),0L)<30*60000L;
                    boolean historyFresh=!prefs.getString(station.historyCacheKey(),"").isBlank()&&check-prefs.getLong(station.historyUpdatedKey(),0L)<60*60000L;
                    if(force||!liveFresh){
                        String query="{ water { observations { data_live(where:{stationNo:{_eq:"+quote+station.id+quote+"}}) { stationNo parameterName timestamp value releaseStatus } } } }";
                        refreshHydroCache(query,"data_live",station.liveCacheKey(),station.liveUpdatedKey());
                    }
                    if(force||!fineFresh){
                        String from=java.time.Instant.now().minus(java.time.Duration.ofHours(26)).toString();
                        String query="{ water { observations { data_10min_mean(where:{station:{no:{_eq:"+quote+station.id+quote+"}},timestamp:{_gte:"+quote+from+quote+"}}) { parameterName timestamp value } } } }";
                        refreshHydroCache(query,"data_10min_mean",station.fineCacheKey(),station.fineUpdatedKey());
                    }
                    if(force||!historyFresh){
                        String from=java.time.Instant.now().minus(java.time.Duration.ofDays(8)).toString();
                        String query="{ water { observations { data_1hour_mean(where:{station:{no:{_eq:"+quote+station.id+quote+"}},timestamp:{_gte:"+quote+from+quote+"}}) { parameterName timestamp value } } } }";
                        refreshHydroCache(query,"data_1hour_mean",station.historyCacheKey(),station.historyUpdatedKey());
                    }
                }
            }finally{
                hydroLoading=false;
                runOnUiThread(this::refreshHomeLiveViews);
            }
        }).start();
    }

    private void refreshHydroCache(String query,String arrayName,String cacheKey,String updatedKey){
        try{
            String raw=bafuPost(query);
            JSONObject json=new JSONObject(raw);
            if(json.has("errors"))return;
            json.getJSONObject("data").getJSONObject("water").getJSONObject("observations").getJSONArray(arrayName);
            prefs.edit().putString(cacheKey,raw).putLong(updatedKey,System.currentTimeMillis()).apply();
        }catch(Exception ignored){}
    }

    private String bafuPost(String query) throws Exception{
        HttpURLConnection c=(HttpURLConnection)new URL("https://data.bafu.admin.ch/api").openConnection();
        try{
            c.setRequestMethod("POST");c.setDoOutput(true);c.setConnectTimeout(7000);c.setReadTimeout(12000);
            c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/"+BuildConfig.VERSION_NAME);
            String body=new JSONObject().put("query",query).toString();
            try(OutputStream out=c.getOutputStream()){out.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
            if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());
            return readConnection(c);
        }finally{c.disconnect();}
    }

    private String httpGet(String url) throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();try{c.setConnectTimeout(7000);c.setReadTimeout(9000);c.setRequestProperty("Accept","application/json");c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/"+BuildConfig.VERSION_NAME);if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());return readConnection(c);}finally{c.disconnect();}}
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
        data.addView(dataFreshnessRow(),margin(-1,-2,0,0,0,10));
        Button reload=btn("Alle Daten aktualisieren",Color.rgb(232,240,244),NAVY); reload.setOnClickListener(v->{refreshEvents(true,()->{});refreshLive(true);Toast.makeText(this,"Aktualisierung gestartet.",Toast.LENGTH_SHORT).show();}); data.addView(reload,new LinearLayout.LayoutParams(-1,dp(44)));
        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY); clear.setOnClickListener(v->clearDataCache()); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(44)); clp.setMargins(0,dp(8),0,0); data.addView(clear,clp);
        boolean bgOn=prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
        Button bgRefresh=btn("Hintergrundaktualisierung: "+(bgOn?"Ein":"Aus"),Color.rgb(232,240,244),NAVY);
        bgRefresh.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);prefs.edit().putBoolean(PREF_BACKGROUND_REFRESH,next).apply();scheduleBackgroundRefresh();bgRefresh.setText("Hintergrundaktualisierung: "+(next?"Ein":"Aus"));});
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,dp(44));blp.setMargins(0,dp(8),0,0);data.addView(bgRefresh,blp);
        TextView autoInfo=txt("Live-Daten werden bei geöffneter App regelmäßig geprüft. Im Hintergrund aktualisiert Android bei verfügbarer Verbindung best effort; Energiesparmodi können die Ausführung verzögern.",11,MUTED,false);autoInfo.setPadding(0,dp(8),0,0);data.addView(autoInfo);

        section(b,"Rhein-Anzeige","Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden. Jede Kachel zeigt alle verfügbaren Messwerte.");
        b.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        b.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

        section(b,"Rhein-Grenzwerte","Status basiert auf dem Abfluss der jeweiligen BAFU-Station");
        b.addView(riverThresholdSettingsCard(HydroStation.BASEL_RHEINHALLE),margin(-1,-2,0,0,0,9));
        b.addView(riverThresholdSettingsCard(HydroStation.RHEINFELDEN),margin(-1,-2,0,0,0,12));

        section(b,"App",null);
        LinearLayout about=card(); about.setOrientation(LinearLayout.VERTICAL); b.addView(about,margin(-1,-2,0,0,0,8));
        about.addView(txt("PFVR Rheinfelden",16,TEXT,true)); about.addView(txt("Testversion "+BuildConfig.VERSION_NAME+" · 1.0.0 bleibt für den ersten offiziellen Release reserviert.",13,MUTED,false));
        return scroll;
    }

    private View riverSlotSettingCard(int slot){
        HydroStation station=riverStation(slot);
        boolean enabled=riverSlotEnabled(slot);
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txt("Rhein-Kachel "+slot,15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        int stateColor=enabled?STATUS_GOOD:MUTED;
        TextView state=txt(enabled?"Aktiv":"Aus",11,stateColor,true);
        state.setTextColor(enabled?statusTextColor(STATUS_GOOD):themeText(MUTED));
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(10),dp(5),dp(10),dp(5));
        state.setBackground(enabled?statusBadge(STATUS_GOOD):round(Color.rgb(238,243,246),12));
        titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        String metrics=station.supportsTemperature?"Abfluss · Pegel · Wassertemperatur":"Abfluss · Pegel";
        TextView current=txt(station.label+" · BAFU "+station.id+"\n"+metrics,13,MUTED,false);
        current.setPadding(0,dp(5),0,dp(10));
        card.addView(current);

        LinearLayout actions=new LinearLayout(this);
        if(slot==2){
            Button toggle=btn(enabled?"Ausblenden":"Einblenden",enabled?Color.rgb(232,240,244):NAVY,enabled?NAVY:Color.WHITE);
            toggle.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);prefs.edit().putBoolean(PREF_RIVER_SLOT2_ENABLED,next).apply();navigate(Screen.SETTINGS);});
            actions.addView(toggle,new LinearLayout.LayoutParams(0,dp(44),1));
        }
        Button configure=btn("Station wählen",Color.rgb(232,240,244),NAVY);
        configure.setOnClickListener(v->configureRiverSlot(slot));
        LinearLayout.LayoutParams configureParams=new LinearLayout.LayoutParams(0,dp(44),slot==2?1:2);
        if(slot==2)configureParams.setMargins(dp(8),0,0,0);
        actions.addView(configure,configureParams);
        card.addView(actions);
        return card;
    }

    private void configureRiverSlot(int slot){
        final HydroStation[] stations={HydroStation.BASEL_RHEINHALLE,HydroStation.RHEINFELDEN};
        String[] labels={
                "Basel, Rheinhalle · Abfluss & Pegel",
                "Rheinfelden · Abfluss, Pegel & Wassertemperatur"
        };
        HydroStation currentStation=riverStation(slot);
        int selected=currentStation==HydroStation.RHEINFELDEN?1:0;
        new AlertDialog.Builder(this,dialogTheme()).setTitle("Rhein-Kachel "+slot)
                .setSingleChoiceItems(labels,selected,(dialog,which)->{
                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    String metricKey=slot==1?PREF_RIVER_SLOT1_METRIC:PREF_RIVER_SLOT2_METRIC;
                    prefs.edit().putString(stationKey,stations[which].id).remove(metricKey).apply();
                    dialog.dismiss();
                    navigate(Screen.SETTINGS);
                }).setNegativeButton("Abbrechen",null).show();
    }

    private View riverThresholdSettingsCard(HydroStation station){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(txt(station.label+" · BAFU "+station.id,15,TEXT,true));
        card.addView(thresholdGrid(station),margin(-1,-2,0,7,0,9));
        String official=station==HydroStation.BASEL_RHEINHALLE
                ?"Schifffahrt: ca. 1800 m³/s = 700 cm / Voralarm; ca. 2500 m³/s = 790 cm / Sperrung Kleinschifffahrt und Fähren Basel–Rheinfelden."
                :"BAFU: Gefahrenstufe 2 ab 2500 m³/s, Stufe 4 ab 3600 m³/s.";
        TextView note=txt(official+" Niedrig < 400 m³/s ist nur eine anpassbare App-Vorgabe.",11,MUTED,false);
        note.setPadding(0,0,0,dp(9));
        card.addView(note);
        LinearLayout actions=new LinearLayout(this);
        Button edit=btn("Ändern",Color.rgb(232,240,244),NAVY);
        edit.setOnClickListener(v->editRiverThresholds(station));
        actions.addView(edit,new LinearLayout.LayoutParams(0,dp(42),1));
        Button reset=btn("Standard",Color.rgb(232,240,244),NAVY);
        reset.setOnClickListener(v->{prefs.edit().remove(station.lowPreferenceKey()).remove(station.warnPreferenceKey()).remove(station.alarmPreferenceKey()).apply();navigate(Screen.SETTINGS);});
        LinearLayout.LayoutParams resetParams=new LinearLayout.LayoutParams(0,dp(42),1);resetParams.setMargins(dp(8),0,0,0);actions.addView(reset,resetParams);
        card.addView(actions);
        return card;
    }

    private View dataFreshnessRow(){
        LinearLayout stack=new LinearLayout(this);
        stack.setOrientation(LinearLayout.VERTICAL);
        LinearLayout first=new LinearLayout(this);
        addFreshnessTile(first,"Kalender",prefs.getLong(PREF_ICS_UPDATED,0L),4L*60L*60L*1000L);
        addFreshnessTile(first,"Wetter",prefs.getLong(PREF_WEATHER_UPDATED,0L),90L*60L*1000L);
        stack.addView(first,new LinearLayout.LayoutParams(-1,-2));
        LinearLayout second=new LinearLayout(this);
        addFreshnessTile(second,"Basel 2289",prefs.getLong(HydroStation.BASEL_RHEINHALLE.liveUpdatedKey(),0L),45L*60L*1000L);
        addFreshnessTile(second,"Rheinfelden 2091",prefs.getLong(HydroStation.RHEINFELDEN.liveUpdatedKey(),0L),45L*60L*1000L);
        LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(-1,-2);
        secondParams.setMargins(0,dp(6),0,0);
        stack.addView(second,secondParams);
        return stack;
    }

    private void addFreshnessTile(LinearLayout row,String label,long updated,long staleAfter){
        long age=updated<=0?Long.MAX_VALUE:Math.max(0,System.currentTimeMillis()-updated);
        int color=updated<=0?MUTED:(age>staleAfter?STATUS_WARN:STATUS_GOOD);
        LinearLayout tile=new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(5),dp(8),dp(5),dp(8));
        tile.setBackground(round(Color.rgb(238,243,246),12));
        TextView ageView=txt(dataAge(updated),12,color,true);
        ageView.setTextColor(statusTextColor(color));
        ageView.setGravity(Gravity.CENTER);
        tile.addView(ageView);
        TextView labelView=txt(label,10,MUTED,false);
        labelView.setGravity(Gravity.CENTER);
        labelView.setPadding(0,dp(2),0,0);
        tile.addView(labelView);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);
        if(row.getChildCount()>0)lp.setMargins(dp(6),0,0,0);
        row.addView(tile,lp);
    }

    private String dataAge(long updated){
        if(updated<=0)return "kein Stand";
        long minutes=Math.max(0,(System.currentTimeMillis()-updated)/60000L);
        if(minutes<1)return "gerade eben";
        if(minutes<60)return minutes+" min";
        long hours=minutes/60;
        if(hours<24)return hours+" h";
        return (hours/24)+" d";
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
    private void clearDataCache(){
        SharedPreferences.Editor editor=prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED);
        for(HydroStation station:HydroStation.values())editor.remove(station.liveCacheKey()).remove(station.liveUpdatedKey()).remove(station.fineCacheKey()).remove(station.fineUpdatedKey()).remove(station.historyCacheKey()).remove(station.historyUpdatedKey());
        editor.apply();
        events=new ArrayList<>();eventsUpdated=0L;
        Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();
        refreshEvents(false,()->{});refreshLive(true);
    }

    private void editRiverThresholds(){editRiverThresholds(HydroStation.RHEINFELDEN);}
    private void editRiverThresholds(HydroStation station){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(4),dp(16),0);
        EditText low=thresholdInput("Niedrig unter m³/s",riverLow(station)),warn=thresholdInput("Warnung ab m³/s",riverWarn(station)),alarm=thresholdInput("Alarm ab m³/s",riverAlarm(station));
        box.addView(txt("Niedrig",12,MUTED,true));box.addView(low,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView w1=txt("Warnung",12,MUTED,true);w1.setPadding(0,dp(10),0,0);box.addView(w1);box.addView(warn,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView a1=txt("Alarm",12,MUTED,true);a1.setPadding(0,dp(10),0,0);box.addView(a1);box.addView(alarm,new LinearLayout.LayoutParams(-1,dp(48)));
        new AlertDialog.Builder(this,dialogTheme()).setTitle(station.label+" · Grenzwerte").setView(box).setPositiveButton("Speichern",(d,w)->{
            try{
                float l=Float.parseFloat(low.getText().toString().replace(',','.')),wa=Float.parseFloat(warn.getText().toString().replace(',','.')),al=Float.parseFloat(alarm.getText().toString().replace(',','.'));
                if(l<0||!(l<wa&&wa<al))throw new Exception();
                prefs.edit().putFloat(station.lowPreferenceKey(),l).putFloat(station.warnPreferenceKey(),wa).putFloat(station.alarmPreferenceKey(),al).apply();
                navigate(Screen.SETTINGS);
            }catch(Exception e){Toast.makeText(this,"Grenzwerte müssen aufsteigend sein: Niedrig < Warnung < Alarm.",Toast.LENGTH_LONG).show();}
        }).setNegativeButton("Abbrechen",null).show();
    }
    private EditText thresholdInput(String hint,float value){EditText e=new EditText(this);e.setHint(hint);e.setText(String.format(Locale.US,"%.0f",value));e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setTextColor(themeText(TEXT));e.setHintTextColor(themeText(MUTED));e.setBackground(round(Color.rgb(238,243,246),12));e.setPadding(dp(12),0,dp(12),0);return e;}

    private View eventScreen() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        scroll.addView(body);

        LinearLayout intro=card();
        intro.setOrientation(LinearLayout.VERTICAL);
        intro.setPadding(dp(18),dp(17),dp(18),dp(17));
        body.addView(intro,margin(-1,-2,0,4,0,16));
        intro.addView(txt("Jahresprogramm",23,TEXT,true));
        TextView info=txt("Termine aus dem öffentlichen Vereinskalender – lokal gespeichert und auch ohne Verbindung sichtbar.",13,MUTED,false);
        info.setPadding(0,dp(5),0,dp(4));
        intro.addView(info);
        TextView cacheInfo=txt(calendarStatus(),11,eventsUpdated>0?WATER:MUTED,false);
        cacheInfo.setPadding(0,0,0,dp(12));
        intro.addView(cacheInfo);
        LinearLayout row=new LinearLayout(this);
        Button reload=btn("Aktualisieren",NAVY,Color.WHITE);
        reload.setOnClickListener(v->refreshEvents(true,()->navigate(Screen.EVENTS)));
        row.addView(reload,new LinearLayout.LayoutParams(0,dp(44),1));
        Button calendar=btn("Originalkalender",Color.rgb(232,240,244),NAVY);
        calendar.setOnClickListener(v->external(CALENDAR_WEB));
        LinearLayout.LayoutParams calendarParams=new LinearLayout.LayoutParams(0,dp(44),1);
        calendarParams.setMargins(dp(8),0,0,0);
        row.addView(calendar,calendarParams);
        intro.addView(row);

        if(events.isEmpty()) {
            ProgressBar progress=new ProgressBar(this);
            LinearLayout.LayoutParams progressParams=new LinearLayout.LayoutParams(dp(48),dp(48));
            progressParams.gravity=Gravity.CENTER_HORIZONTAL;
            body.addView(progress,progressParams);
            TextView wait=txt(eventsLoading?"Kalender wird im Hintergrund geladen …":"Noch kein gespeicherter Kalenderstand vorhanden.",13,MUTED,false);
            wait.setGravity(Gravity.CENTER);
            wait.setPadding(0,dp(8),0,0);
            body.addView(wait);
            if(!eventsLoading)refreshEvents(false,()->{if(current==Screen.EVENTS)navigate(Screen.EVENTS);});
            return scroll;
        }

        String month="";
        for(Event event:events) {
            String nextMonth=cap(event.start.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.GERMAN)));
            if(!nextMonth.equals(month)) {
                TextView monthTitle=txt(nextMonth,17,TEXT,true);
                monthTitle.setPadding(dp(2),dp(9),0,dp(8));
                body.addView(monthTitle);
                month=nextMonth;
            }
            body.addView(eventCard(event,false));
        }
        return scroll;
    }

    private View eventCard(Event event,boolean compact) {
        boolean cancelled=isCancelledEvent(event);
        LinearLayout card=card();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14),dp(12),dp(12),dp(12));
        card.setLayoutParams(margin(-1,-2,0,0,0,9));
        card.setContentDescription(event.title+", "+eventWhen(event));
        card.setOnClickListener(v->showEventDetails(event));

        LinearLayout date=new LinearLayout(this);
        date.setOrientation(LinearLayout.VERTICAL);
        date.setGravity(Gravity.CENTER);
        date.setBackground(round(cancelled?Color.rgb(250,232,232):Color.rgb(231,242,246),14));
        TextView day=txt(String.valueOf(event.start.getDayOfMonth()),23,cancelled?STATUS_ALARM:NAVY,true);
        day.setGravity(Gravity.CENTER);
        date.addView(day);
        TextView month=txt(event.start.format(DateTimeFormatter.ofPattern("MMM",Locale.GERMAN)).replace(".","").toUpperCase(Locale.GERMAN),11,cancelled?STATUS_ALARM:WATER,true);
        month.setGravity(Gravity.CENTER);
        date.addView(month);
        card.addView(date,new LinearLayout.LayoutParams(dp(62),dp(62)));

        LinearLayout details=new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(dp(13),0,dp(6),0);
        card.addView(details,new LinearLayout.LayoutParams(0,-2,1));

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=txt(event.title,compact?15:16,TEXT,true);
        if(cancelled)title.setPaintFlags(title.getPaintFlags()|Paint.STRIKE_THRU_TEXT_FLAG);
        titleRow.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        if(cancelled){
            TextView badge=txt("Abgesagt",10,STATUS_ALARM,true);
            badge.setTextColor(statusTextColor(STATUS_ALARM));
            badge.setGravity(Gravity.CENTER);
            badge.setPadding(dp(8),dp(4),dp(8),dp(4));
            badge.setBackground(statusBadge(STATUS_ALARM));
            LinearLayout.LayoutParams badgeParams=new LinearLayout.LayoutParams(-2,-2);
            badgeParams.setMargins(dp(8),0,0,0);
            titleRow.addView(badge,badgeParams);
        }
        details.addView(titleRow);
        details.addView(txt(eventWhenCompact(event),13,MUTED,false));
        if(!compact&&event.location!=null&&!event.location.isBlank()){
            TextView location=txt(event.location.replace("\n"," · "),12,WATER,false);
            location.setMaxLines(2);
            details.addView(location);
        }

        TextView arrow=txt("›",28,cancelled?MUTED:WATER,false);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow,new LinearLayout.LayoutParams(dp(24),dp(52)));
        return card;
    }

    private String eventWhenCompact(Event event){
        String text=cap(event.start.format(DateTimeFormatter.ofPattern("EEEE",Locale.GERMAN)));
        if(!event.allDay){
            text+=" · "+event.start.format(DateTimeFormatter.ofPattern("HH:mm"));
            if(event.end!=null&&event.end.toLocalDate().equals(event.start.toLocalDate()))text+="–"+event.end.format(DateTimeFormatter.ofPattern("HH:mm"));
            text+=" Uhr";
        }else text+=" · ganztägig";
        return text;
    }

    private String eventWhen(Event event){
        DateTimeFormatter date=DateTimeFormatter.ofPattern("EEEE, dd. MMMM yyyy",Locale.GERMAN);
        DateTimeFormatter time=DateTimeFormatter.ofPattern("HH:mm");
        String start=cap(event.start.format(date));
        if(event.allDay)return start+" · ganztägig";
        if(event.end==null)return start+" · "+event.start.format(time)+" Uhr";
        if(event.end.toLocalDate().equals(event.start.toLocalDate()))return start+" · "+event.start.format(time)+"–"+event.end.format(time)+" Uhr";
        return start+" · "+event.start.format(time)+" Uhr\n"+cap(event.end.format(date))+" · "+event.end.format(time)+" Uhr";
    }

    private void showEventDetails(Event event){
        ScrollView scroll=new ScrollView(this);
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20),dp(10),dp(20),dp(8));
        scroll.addView(box);

        if(isCancelledEvent(event)){
            TextView cancelled=txt("●  Termin abgesagt",12,STATUS_ALARM,true);
            cancelled.setTextColor(statusTextColor(STATUS_ALARM));
            cancelled.setPadding(0,0,0,dp(9));
            box.addView(cancelled);
        }
        box.addView(txt(event.title,22,TEXT,true));
        TextView when=txt(eventWhen(event),14,MUTED,false);
        when.setPadding(0,dp(7),0,0);
        box.addView(when);

        if(event.location!=null&&!event.location.isBlank()){
            TextView location=txt("Ort\n"+event.location,13,WATER,false);
            location.setPadding(0,dp(14),0,0);
            box.addView(location);
            Button route=btn("Route öffnen",Color.rgb(232,240,244),NAVY);
            route.setOnClickListener(v->openLocation(event.location));
            LinearLayout.LayoutParams routeParams=new LinearLayout.LayoutParams(-1,dp(44));
            routeParams.setMargins(0,dp(8),0,0);
            box.addView(route,routeParams);
        }

        if(event.description!=null&&!event.description.isBlank()){
            TextView descriptionLabel=txt("Details",12,MUTED,true);
            descriptionLabel.setPadding(0,dp(15),0,dp(4));
            box.addView(descriptionLabel);
            TextView description=txt(event.description.trim(),14,TEXT,false);
            description.setTextIsSelectable(true);
            description.setPadding(0,0,0,dp(4));
            box.addView(description);
        }

        LinearLayout actions=new LinearLayout(this);
        actions.setPadding(0,dp(16),0,0);
        Button share=btn("Teilen",Color.rgb(232,240,244),NAVY);
        share.setOnClickListener(v->shareEvent(event));
        actions.addView(share,new LinearLayout.LayoutParams(0,dp(44),1));
        Button add=btn("Zum Kalender",NAVY,Color.WHITE);
        add.setOnClickListener(v->addEventToCalendar(event));
        LinearLayout.LayoutParams addParams=new LinearLayout.LayoutParams(0,dp(44),1);
        addParams.setMargins(dp(8),0,0,0);
        actions.addView(add,addParams);
        box.addView(actions);

        new AlertDialog.Builder(this,dialogTheme()).setView(scroll).setNegativeButton("Schliessen",null).show();
    }

    private void shareEvent(Event event){
        StringBuilder body=new StringBuilder(event.title).append("\n").append(eventWhen(event));
        if(event.location!=null&&!event.location.isBlank())body.append("\n").append(event.location);
        Intent share=new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT,event.title);
        share.putExtra(Intent.EXTRA_TEXT,body.toString());
        try{startActivity(Intent.createChooser(share,"Termin teilen"));}catch(Exception ignored){Toast.makeText(this,"Teilen ist auf diesem Gerät nicht verfügbar.",Toast.LENGTH_SHORT).show();}
    }

    private void addEventToCalendar(Event event){
        Intent insert=new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI);
        insert.putExtra(CalendarContract.Events.TITLE,event.title);
        insert.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,event.start.toInstant().toEpochMilli());
        ZonedDateTime end=event.end!=null?event.end:(event.allDay?event.start.plusDays(1):event.start.plusHours(1));
        insert.putExtra(CalendarContract.EXTRA_EVENT_END_TIME,end.toInstant().toEpochMilli());
        insert.putExtra(CalendarContract.Events.ALL_DAY,event.allDay);
        if(event.location!=null&&!event.location.isBlank())insert.putExtra(CalendarContract.Events.EVENT_LOCATION,event.location);
        if(event.description!=null&&!event.description.isBlank())insert.putExtra(CalendarContract.Events.DESCRIPTION,event.description);
        try{startActivity(insert);}catch(Exception ignored){Toast.makeText(this,"Keine Kalender-App gefunden.",Toast.LENGTH_SHORT).show();}
    }

    private void openLocation(String location){
        Uri geo=Uri.parse("geo:0,0?q="+Uri.encode(location));
        try{startActivity(new Intent(Intent.ACTION_VIEW,geo));}
        catch(Exception ignored){external("https://www.google.com/maps/search/?api=1&query="+Uri.encode(location));}
    }

    private View cash() {
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        cashQuantityViews.clear();
        scroll.addView(body);

        LinearLayout hero=new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(20),dp(18),dp(20),dp(18));
        GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(21,90,122),WATER});
        gradient.setCornerRadius(dp(22));hero.setBackground(gradient);
        body.addView(hero,margin(-1,-2,0,4,0,16));
        hero.addView(txt("VEREINSBEIZ",12,Color.rgb(208,231,239),true));
        TextView title=txt("Konsumation bezahlen",27,Color.WHITE,true);title.setPadding(0,dp(5),0,dp(5));hero.addView(title);
        hero.addView(txt("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.",14,Color.rgb(232,243,247),false));

        section(body,"Warenkorb","Mengen können mehrere Personen gemeinsam abdecken");
        LinearLayout cart=card();cart.setOrientation(LinearLayout.VERTICAL);body.addView(cart,margin(-1,-2,0,0,0,12));
        cashSummaryContainer=new LinearLayout(this);cashSummaryContainer.setOrientation(LinearLayout.VERTICAL);cart.addView(cashSummaryContainer);
        cashTotalView=txt("Total CHF 0.00",24,TEXT,true);cashTotalView.setPadding(0,dp(12),0,dp(10));cart.addView(cashTotalView);
        LinearLayout payRow=new LinearLayout(this);
        Button cartQr=btn("Swiss QR",NAVY,Color.WHITE);cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});payRow.addView(cartQr,new LinearLayout.LayoutParams(0,dp(46),1));
        Button cartBank=btn("Direkt Bank",Color.rgb(232,240,244),NAVY);cartBank.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)sharePaymentQr(input);});LinearLayout.LayoutParams cbp=new LinearLayout.LayoutParams(0,dp(46),1);cbp.setMargins(dp(7),0,0,0);payRow.addView(cartBank,cbp);
        Button cartTwint=btn("TWINT",Color.rgb(232,240,244),NAVY);cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});LinearLayout.LayoutParams ctp=new LinearLayout.LayoutParams(0,dp(46),1);ctp.setMargins(dp(7),0,0,0);payRow.addView(cartTwint,ctp);
        cart.addView(payRow);
        TextView directInfo=txt("„Direkt Bank“ übergibt den Swiss-QR temporär an eine kompatible Banking-App – ohne ihn vorher dauerhaft zu speichern. Nicht jede Bank unterstützt diesen Android-Import.",11,MUTED,false);directInfo.setPadding(0,dp(8),0,0);cart.addView(directInfo);
        TextView clearCart=link("Warenkorb leeren");clearCart.setOnClickListener(v->{cashCart.clear();for(TextView quantity:cashQuantityViews.values())quantity.setText("0");updateCashSummary();});cart.addView(clearCart);
        updateCashSummary();


        CashCatalog.Catalog catalog=cashCatalog();
        section(body,"Auswahl",catalog==null?"Preisliste konnte nicht geladen werden.":"Trinken, Essen und Feiern · Stand "+catalog.validFrom);
        if(catalog!=null){
            for(CashCatalog.Category category:catalog.categories){
                TextView categoryTitle=txt(category.label,15,TEXT,true);
                categoryTitle.setPadding(dp(2),dp(3),0,dp(7));
                body.addView(categoryTitle);
                LinearLayout categoryCard=card();
                categoryCard.setOrientation(LinearLayout.VERTICAL);
                for(int i=0;i<category.items.size();i++){
                    if(i>0){View divider=new View(this);divider.setBackgroundColor(darkMode?Color.rgb(51,65,74):Color.rgb(226,233,237));categoryCard.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));}
                    categoryCard.addView(cashItemRow(category.items.get(i)));
                }
                body.addView(categoryCard,margin(-1,-2,0,0,0,10));
            }
        }

        section(body,"Freier Betrag","Für Sonderfälle oder Beträge ausserhalb der Preisliste");
        LinearLayout amountCard=card();amountCard.setOrientation(LinearLayout.VERTICAL);body.addView(amountCard,margin(-1,-2,0,0,0,12));
        LinearLayout amountRow=new LinearLayout(this);amountRow.setGravity(Gravity.CENTER_VERTICAL);amountRow.setPadding(0,0,0,dp(8));amountCard.addView(amountRow);
        TextView chf=txt("CHF",20,NAVY,true);chf.setGravity(Gravity.CENTER_VERTICAL);amountRow.addView(chf,new LinearLayout.LayoutParams(dp(55),dp(56)));
        EditText amount=new EditText(this);amount.setHint("0.00");amount.setTextSize(25);amount.setSingleLine(true);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);amount.setTextColor(themeText(TEXT));amount.setHintTextColor(themeText(MUTED));amount.setBackground(round(Color.rgb(238,243,246),14));amount.setPadding(dp(14),0,dp(14),0);amountRow.addView(amount,new LinearLayout.LayoutParams(0,dp(56),1));
        TextView amountInfo=txt("Leer oder 0 erzeugt einen Swiss QR mit offenem Betrag.",12,MUTED,false);amountInfo.setPadding(0,0,0,dp(10));amountCard.addView(amountInfo);
        Button qr=btn("Swiss QR erstellen",NAVY,Color.WHITE);qr.setOnClickListener(v->showPaymentQr(amount));amountCard.addView(qr,new LinearLayout.LayoutParams(-1,dp(48)));
        Button direct=btn("QR direkt an Banking-App",Color.rgb(232,240,244),NAVY);direct.setOnClickListener(v->sharePaymentQr(amount));LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(-1,dp(44));dlp.setMargins(0,dp(8),0,0);amountCard.addView(direct,dlp);
        String bankLabel=prefs.getString(PREF_BANK_LABEL,"");
        Button bank=btn(bankLabel.trim().isEmpty()?"Banking-App auswählen":bankLabel+" öffnen",Color.rgb(232,240,244),NAVY);bankButton=bank;bank.setOnClickListener(v->openPreferred(false,amount));LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(44));bp.setMargins(0,dp(8),0,0);amountCard.addView(bank,bp);
        TextView change=link(bankLabel.trim().isEmpty()?"Banking-App für Direktstart wählen":"Andere Banking-App wählen");change.setOnClickListener(v->chooseApp(false,amount));amountCard.addView(change);

        LinearLayout twint=card();twint.setOrientation(LinearLayout.VERTICAL);body.addView(twint,margin(-1,-2,0,0,0,12));
        twint.addView(txt("TWINT",16,TEXT,true));
        TextView twintInfo=txt("Für Zahlung auf demselben Handy: Betrag auf der PFVR-Seite übernehmen und dort den fünfstelligen TWINT-Code erzeugen. Der Vereins-QR bleibt zusätzlich verfügbar.",13,MUTED,false);twintInfo.setPadding(0,dp(4),0,dp(10));twint.addView(twintInfo);
        Button twDirect=btn("TWINT-Code erzeugen",NAVY,Color.WHITE);twDirect.setOnClickListener(v->openTwintDirect(amount));twint.addView(twDirect,new LinearLayout.LayoutParams(-1,dp(48)));
        Button twQr=btn("Vereins-TWINT-QR öffnen",Color.rgb(232,240,244),NAVY);twQr.setOnClickListener(v->external(TWINT_QR_PDF));LinearLayout.LayoutParams twqp=new LinearLayout.LayoutParams(-1,dp(44));twqp.setMargins(0,dp(8),0,0);twint.addView(twQr,twqp);

        section(body,"Zahlungsdaten","Für E-Banking und manuelle Überweisung");
        LinearLayout details=card();details.setOrientation(LinearLayout.VERTICAL);body.addView(details,margin(-1,-2,0,0,0,12));details.addView(txt(CLUB_PAYEE,16,TEXT,true));details.addView(txt("Rheinweg · 4310 Rheinfelden",13,MUTED,false));TextView iban=txt(CLUB_IBAN,19,NAVY,true);iban.setPadding(0,dp(12),0,dp(4));details.addView(iban);details.addView(txt(CLUB_PAYMENT_NOTE,13,MUTED,false));
        LinearLayout copies=new LinearLayout(this);copies.setPadding(0,dp(12),0,0);details.addView(copies);Button copyIban=btn("IBAN kopieren",Color.rgb(232,240,244),NAVY);copyIban.setOnClickListener(v->copy("PFVR IBAN",CLUB_IBAN.replace(" ",""),"IBAN kopiert"));copies.addView(copyIban,new LinearLayout.LayoutParams(0,dp(42),1));Button copyAll=btn("Alles kopieren",Color.rgb(232,240,244),NAVY);copyAll.setOnClickListener(v->{String x=CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE;String a=amount(amount.getText().toString());if(a!=null&&!a.isBlank())x+="\nCHF "+a;copy("PFVR Zahlung",x,"Zahlungsdaten kopiert");});LinearLayout.LayoutParams cap=new LinearLayout.LayoutParams(0,dp(42),1);cap.setMargins(dp(8),0,0,0);copies.addView(copyAll,cap);
        return scroll;
    }

    private CashCatalog.Catalog cashCatalog(){
        if(cashCatalog!=null)return cashCatalog;
        try{cashCatalog=CashCatalog.load(this);}catch(Exception ignored){cashCatalog=null;}
        return cashCatalog;
    }

    private View cashItemRow(CashCatalog.Item item){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(9),0,dp(9));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(0,0,dp(8),0);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        copy.addView(txt(item.name,14,TEXT,true));
        String detail=(item.variant==null||item.variant.isBlank()?"":item.variant+" · ")+formatCashPrice(item.price)+(item.deposit?" · Depot":"");copy.addView(txt(detail,12,item.deposit?WATER:MUTED,false));
        Button minus=btn("−",Color.rgb(232,240,244),NAVY);row.addView(minus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        TextView quantity=txt(String.valueOf(cashCart.getOrDefault(item.id,0)),16,TEXT,true);quantity.setGravity(Gravity.CENTER);row.addView(quantity,new LinearLayout.LayoutParams(dp(38),dp(40)));
        cashQuantityViews.put(item.id,quantity);
        Button plus=btn("+",NAVY,Color.WHITE);row.addView(plus,new LinearLayout.LayoutParams(dp(40),dp(40)));
        minus.setOnClickListener(v->{int next=Math.max(0,cashCart.getOrDefault(item.id,0)-1);setCashQuantity(item.id,next);updateCashSummary();});
        plus.setOnClickListener(v->{int next=Math.min(99,cashCart.getOrDefault(item.id,0)+1);setCashQuantity(item.id,next);updateCashSummary();});
        return row;
    }

    private void setCashQuantity(String itemId,int quantity){
        if(quantity<=0)cashCart.remove(itemId);else cashCart.put(itemId,quantity);
        TextView view=cashQuantityViews.get(itemId);
        if(view!=null)view.setText(String.valueOf(Math.max(0,quantity)));
    }

    private View cashSummaryRow(CashCatalog.Item item,int quantity){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(4),0,dp(4));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(0,0,dp(8),0);
        copy.addView(txt(quantity+"× "+item.displayName(),13,TEXT,true));
        copy.addView(txt(formatCashPrice(item.price*quantity),12,item.deposit?WATER:MUTED,false));
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        Button remove=btn("Entfernen",Color.rgb(232,240,244),NAVY);
        remove.setOnClickListener(v->{setCashQuantity(item.id,0);updateCashSummary();});
        row.addView(remove,new LinearLayout.LayoutParams(dp(94),dp(38)));
        return row;
    }

    private void updateCashSummary(){
        if(cashSummaryContainer==null||cashTotalView==null)return;
        cashSummaryContainer.removeAllViews();
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null){cashSummaryContainer.addView(txt("Preisliste nicht verfügbar.",13,MUTED,false));cashTotalView.setText("Total CHF 0.00");return;}
        boolean any=false;
        for(CashCatalog.Category category:catalog.categories){
            for(CashCatalog.Item item:category.items){
                int quantity=cashCart.getOrDefault(item.id,0);if(quantity<=0)continue;any=true;
                cashSummaryContainer.addView(cashSummaryRow(item,quantity));
            }
        }
        if(!any)cashSummaryContainer.addView(txt("Noch keine Artikel ausgewählt.",13,MUTED,false));
        cashTotalView.setText("Total "+formatCashPrice(catalog.total(cashCart)));
    }

    private String formatCashPrice(double value){return "CHF "+String.format(Locale.GERMAN,"%.2f",value);}

    private EditText cartAmountInput(){
        CashCatalog.Catalog catalog=cashCatalog();
        if(catalog==null||catalog.itemCount(cashCart)<=0){Toast.makeText(this,"Der Warenkorb ist leer.",Toast.LENGTH_SHORT).show();return null;}
        EditText input=new EditText(this);input.setText(String.format(Locale.US,"%.2f",catalog.total(cashCart)));return input;
    }

    private void openTwintDirect(EditText amountInput){
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen.",Toast.LENGTH_LONG).show();return;}
        if(!a.isBlank())copy("PFVR TWINT-Betrag",a,"CHF "+a+" kopiert – auf der PFVR-Seite eintragen.");
        external(TWINT_DIRECT_URL);
    }

    private void sharePaymentQr(EditText amountInput){
        String value=amount(amountInput==null?null:amountInput.getText().toString());
        if(value==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben.",Toast.LENGTH_LONG).show();return;}
        try{
            Bitmap qr=makeSwissQr(value);
            pendingQrBitmap=qr;
            File directory=new File(getCacheDir(),"shared");
            if(!directory.exists()&&!directory.mkdirs())throw new Exception("cache directory");
            File file=new File(directory,value.isBlank()?"PFVR-Zahlung-offen.png":"PFVR-Zahlung-CHF-"+value.replace('.','_')+".png");
            try(FileOutputStream out=new FileOutputStream(file)){if(!qr.compress(Bitmap.CompressFormat.PNG,100,out))throw new Exception("png");}
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            Intent send=new Intent(Intent.ACTION_SEND);
            send.setType("image/png");
            send.putExtra(Intent.EXTRA_STREAM,uri);
            send.setClipData(ClipData.newUri(getContentResolver(),"PFVR Swiss QR",uri));
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            String preferred=prefs.getString(PREF_BANK_PACKAGE,"");
            if(!preferred.isBlank()){
                send.setPackage(preferred);
                if(send.resolveActivity(getPackageManager())!=null){startActivity(send);Toast.makeText(this,"Swiss QR temporär an Banking-App übergeben.",Toast.LENGTH_SHORT).show();return;}
                send.setPackage(null);
                Toast.makeText(this,"Die gewählte Banking-App nimmt keinen direkten Bild-Import an. Kompatible Apps werden angezeigt.",Toast.LENGTH_LONG).show();
            }
            if(getPackageManager().queryIntentActivities(send,0).isEmpty()){
                Toast.makeText(this,"Keine App unterstützt die direkte QR-Übergabe. QR wird stattdessen angezeigt.",Toast.LENGTH_LONG).show();
                showPaymentQr(amountInput);
                return;
            }
            startActivity(Intent.createChooser(send,"Swiss QR an Banking-App übergeben"));
        }catch(Exception e){Toast.makeText(this,"Direkte QR-Übergabe nicht möglich. QR wird stattdessen angezeigt.",Toast.LENGTH_LONG).show();showPaymentQr(amountInput);}
    }

    private void showPaymentQr(EditText amountInput) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,"Bitte einen gültigen CHF-Betrag eingeben oder Feld leer/0 für offenen Betrag lassen.",Toast.LENGTH_LONG).show();return;}
        try {
            Bitmap qr=makeSwissQr(a);
            pendingQrBitmap=qr;
            LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(8),dp(16),0);
            ImageView image=new ImageView(this); image.setImageBitmap(qr); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(image,new LinearLayout.LayoutParams(-1,dp(330)));
            String amountLine=a.isBlank()?"Betrag offen · in Banking-App eingeben":"CHF "+a;
            TextView details=txt(amountLine+"\n"+CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE,14,TEXT,false); details.setGravity(Gravity.CENTER); details.setPadding(0,dp(8),0,dp(4)); box.addView(details);
            TextView note=txt("Direkte Übergabe versucht den QR als temporäres Bild an eine kompatible Banking-App zu senden. Falls die Bank das nicht unterstützt, bleibt Speichern/Öffnen als Fallback.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);
            new AlertDialog.Builder(this,dialogTheme())
                    .setTitle("Bankzahlung · Swiss QR")
                    .setView(box)
                    .setPositiveButton("Direkt an Banking-App",(d,w)->sharePaymentQr(amountInput))
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
        save.putExtra(Intent.EXTRA_TITLE,amount.isBlank()?"PFVR-Zahlung-offener-Betrag.png":"PFVR-Zahlung-CHF-"+amount.replace('.','_')+".png");
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

    private void copyAmount(EditText input) { String a=amount(input==null?null:input.getText().toString()); if(a==null||a.isBlank()){Toast.makeText(this,"Banking-App geöffnet. Betrag dort eingeben.",Toast.LENGTH_SHORT).show();return;} copy("PFVR Betrag",a,"CHF "+a+" kopiert"); }
    private String amount(String raw) { if(raw==null||raw.trim().isEmpty())return ""; try{double n=Double.parseDouble(raw.trim().replace(',','.')); if(n<0||n>100000)return null; if(n==0)return ""; return String.format(Locale.US,"%.2f",n);}catch(Exception e){return null;} }
    private boolean any(String s,String... xs){for(String x:xs)if(s.contains(x))return true;return false;}
    private void copy(String label,String value,String toast){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText(label,value)); Toast.makeText(this,toast,Toast.LENGTH_SHORT).show();}

    private View club() {
        ScrollView scroll=new ScrollView(this); LinearLayout b=body(); scroll.addView(b);
        LinearLayout top=card(); top.setGravity(Gravity.CENTER_VERTICAL); ImageView logo=new ImageView(this); logo.setImageResource(R.drawable.pfvr_logo); logo.setScaleType(ImageView.ScaleType.CENTER_CROP); top.addView(logo,new LinearLayout.LayoutParams(dp(82),dp(82))); LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(15),0,0,0); info.addView(txt("Pontonierfahrverein Rheinfelden",19,TEXT,true)); info.addView(txt("Gegründet 1896 · Sport und Vereinsleben am Rhein",13,MUTED,false)); top.addView(info,new LinearLayout.LayoutParams(0,-2,1)); b.addView(top,margin(-1,-2,0,4,0,18));

        section(b,"Über den Verein",null);
        LinearLayout about=card();about.setOrientation(LinearLayout.VERTICAL);b.addView(about,margin(-1,-2,0,0,0,12));
        about.addView(txt("Seit 1896 auf dem Rhein",18,TEXT,true));
        TextView a=txt("Beim Pontonierfahren verbinden sich präzise Bootsführung, Kraft, Technik und Teamarbeit. Der PFVR trainiert auf dem Rhein in Rheinfelden, nimmt an Wettfahren teil und pflegt zugleich ein aktives Vereinsleben sowie die Ausbildung des Nachwuchses.",14,MUTED,false);a.setPadding(0,dp(7),0,0);about.addView(a);

        section(b,"Geschichte","Einige feste Meilensteine direkt in der App");
        b.addView(milestone("1896","Gründung","Beginn der Rheinfelder Pontonier-Tradition."));
        b.addView(milestone("1971","75 Jahre","Jubiläumswettfahren in Rheinfelden."));
        b.addView(milestone("1996","100 Jahre","Schweizerisches Jungpontonierwettfahren zum 100-Jahr-Jubiläum."));
        b.addView(milestone("2021","125 Jahre","Jubiläumsjahr und Jungfernfahrt eines neuen Kunststoffweidlings; laut Stadt Rheinfelden war der PFVR damit der erste Pontonierfahrverein der Schweiz mit einem privaten Kunststoffweidling."));
        b.addView(milestone("2023","Schweizer-Meisterschaft","Der PFVR richtete die Schweizer-Meisterschaft auf dem Rhein in Rheinfelden aus."));

        section(b,"Aktuell & Organisation",null);
        b.addView(action("Vorstand","Ansprechpersonen und Funktionen","Öffnen",v->openInApp(BOARD,"Vorstand")));
        b.addView(action("Jahresprogramm","Originalseite und Kalenderhinweise","Öffnen",v->openInApp(PROGRAM,"Jahresprogramm")));
        b.addView(action("News-Archiv","Beiträge auf pfvr.ch","Öffnen",v->openInApp(NEWS,"News-Archiv")));

        section(b,"Kontakt",null); b.addView(contact("Depot","Rheinweg 42\n4310 Rheinfelden","Route",v->openMap())); b.addView(contact("Telefon","076 209 18 96","Anrufen",v->startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:+41762091896"))))); b.addView(contact("E-Mail","info@pfvr.ch","Schreiben",v->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:info@pfvr.ch"))))); b.addView(contact("Kontaktseite","pfvr.ch/kontakt","Öffnen",v->openInApp(CONTACT,"Kontakt"))); return scroll;
    }

    private View milestone(String year,String title,String detail){
        LinearLayout c=card();c.setGravity(Gravity.CENTER_VERTICAL);c.setLayoutParams(margin(-1,-2,0,0,0,9));
        TextView y=txt(year,15,Color.WHITE,true);y.setGravity(Gravity.CENTER);y.setBackground(round(NAVY,13));c.addView(y,new LinearLayout.LayoutParams(dp(62),dp(48)));
        LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(dp(13),0,0,0);text.addView(txt(title,15,TEXT,true));text.addView(txt(detail,12,MUTED,false));c.addView(text,new LinearLayout.LayoutParams(0,-2,1));return c;
    }

    private View internal() {
        String url=normalizeInternalUrl(prefs.getString(PREF_INTERNAL_URL,"")); if(!validInternal(url)) return internalMissing(); prefs.edit().putString(PREF_INTERNAL_URL,url).apply();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(themeBg(Color.WHITE));
        LinearLayout tools=new LinearLayout(this); tools.setPadding(dp(9),dp(8),dp(9),dp(8)); tools.setBackgroundColor(themeBg(Color.rgb(236,243,247))); root.addView(tools,new LinearLayout.LayoutParams(-1,dp(56)));
        WebView web=web(false); activeWebView=web; web.setBackgroundColor(themeBg(Color.WHITE));
        if(android.os.Build.VERSION.SDK_INT>=33) web.getSettings().setAlgorithmicDarkeningAllowed(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        Button back=btn("‹ Zurück",Color.WHITE,NAVY); back.setOnClickListener(v->handleBack()); tools.addView(back,new LinearLayout.LayoutParams(0,dp(40),1));
        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);
        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(next?"Original":"App-Ansicht");web.clearCache(false);web.reload();});
        LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(40),1.25f); mp.setMargins(dp(7),0,0,0); tools.addView(mode,mp);
        Button reload=btn("Neu laden",Color.WHITE,NAVY); reload.setOnClickListener(v->{web.clearCache(false);web.reload();}); LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,dp(40),1); rp.setMargins(dp(7),0,0,0); tools.addView(reload,rp);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){Uri u=r.getUrl();if("https".equalsIgnoreCase(u.getScheme())&&AppLinkPolicy.isInternalPfvrHost(u.getHost()))return false;external(u.toString());return true;}
            @Override public void onPageFinished(WebView v,String u){super.onPageFinished(v,u);if(prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true))internalSkin(v);}
            @Override public void onReceivedError(WebView v,android.webkit.WebResourceRequest r,android.webkit.WebResourceError e){super.onReceivedError(v,r,e);if(r.isForMainFrame())showInternalLoadError(v,"Ladefehler "+e.getErrorCode()+": "+String.valueOf(e.getDescription()));}
            @Override public void onReceivedHttpError(WebView v,android.webkit.WebResourceRequest r,android.webkit.WebResourceResponse e){super.onReceivedHttpError(v,r,e);if(r.isForMainFrame()&&e.getStatusCode()>=400)showInternalLoadError(v,"PFVR antwortet mit HTTP "+e.getStatusCode());}
        });
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1)); web.loadUrl(url); return root;
    }

    private void showInternalLoadError(WebView v,String message){
        String safe=message.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        String bg=darkMode?"#11171C":"#FFFFFF",text=darkMode?"#ECF1F4":"#15232E",muted=darkMode?"#A0B0BA":"#60717E";
        String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1'><meta name='color-scheme' content='"+(darkMode?"dark":"light")+"'></head><body style='font-family:sans-serif;background:"+bg+";color:"+text+";padding:24px'><h2>Interner Bereich konnte nicht geladen werden</h2><p>"+safe+"</p><p style='color:"+muted+"'>Prüfe den persönlichen Link unter Einstellungen oder tippe oben auf Neu laden.</p></body></html>";
        v.loadDataWithBaseURL("https://intern.pfvr.ch/",html,"text/html","UTF-8",null);
    }

    private void internalSkin(WebView v){
        String bg=darkMode?"#11171C":"#F4F7F9",card=darkMode?"#1A2228":"#FFFFFF",soft=darkMode?"#232E36":"#EDF3F6",text=darkMode?"#ECF1F4":"#15232E",muted=darkMode?"#A0B0BA":"#60717E",border=darkMode?"#344550":"#DCE5EA",link=darkMode?"#5BBED5":"#247E99";
        String css="html{color-scheme:"+(darkMode?"dark":"light")+"!important;}body{margin:0!important;padding:10px 10px 34px!important;background:"+bg+"!important;color:"+text+"!important;font-family:Arial,sans-serif!important;font-size:16px!important;}header,nav,footer,.navbar,.site-header,.site-footer{display:none!important;}table{border-collapse:separate!important;border-spacing:8px!important;width:max-content!important;min-width:100%!important;background:transparent!important;}td,th{background:"+card+"!important;color:"+text+"!important;border:1px solid "+border+"!important;border-radius:14px!important;padding:12px 10px!important;vertical-align:top!important;}p,span,div,label,strong{color:"+text+"!important;}small{color:"+muted+"!important;}a{color:"+link+"!important;}select,input[type=text],input[type=number]{background:"+soft+"!important;color:"+text+"!important;border:1px solid "+border+"!important;border-radius:12px!important;padding:10px!important;min-height:44px!important;}button,input[type=submit],input[type=button],a.btn,.btn{min-height:48px!important;border:0!important;border-radius:12px!important;padding:10px 14px!important;font-size:16px!important;font-weight:700!important;line-height:1.25!important;box-shadow:none!important;}";
        String js="(function(){var st=document.getElementById('pfvr-internal-style');if(!st){st=document.createElement('style');st.id='pfvr-internal-style';document.head.appendChild(st);}st.innerHTML="+JSONObject.quote(css)+";var norm=function(x){return (x||'').replace(/\\s+/g,' ').trim().toLowerCase();};var paint=function(el,bg,fg){el.style.setProperty('background',bg,'important');el.style.setProperty('color',fg,'important');el.style.setProperty('border-color',bg,'important');};var controls=document.querySelectorAll('button,input[type=submit],input[type=button],a.btn,.btn');controls.forEach(function(el){var t=norm(el.innerText||el.value);var attend=false;if(t.indexOf('mit essen')>=0){paint(el,'#16863A','#FFFFFF');attend=true;}else if(t.indexOf('ohne essen')>=0){paint(el,'#F2C94C','#17222B');attend=true;}else if(t.indexOf('nicht gewählt')>=0||t.indexOf('nicht gewaehlt')>=0||t.indexOf('keine auswahl')>=0){paint(el,'#6D7880','#FFFFFF');attend=true;}else if(t.indexOf('komme nicht')>=0||t==='nicht'){paint(el,'#C83737','#FFFFFF');attend=true;}else{paint(el,'"+link+"','#FFFFFF');}if(attend&&!el.dataset.pfvrRefreshBound){el.dataset.pfvrRefreshBound='1';el.addEventListener('click',function(){setTimeout(function(){window.location.reload();},2000);});}});document.querySelectorAll('p,div,strong,label').forEach(function(el){var t=norm(el.innerText);if(t.indexOf('tipp: diese seite als favorit')===0&&t.length<350){el.style.display='none';}});})();";
        v.evaluateJavascript(js,null);
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
            .setPositiveButton("Speichern",(d,w)->{String x=normalizeInternalUrl(input.getText().toString().trim());if(validInternal(x)){prefs.edit().putString(PREF_INTERNAL_URL,x).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);}else Toast.makeText(this,"Bitte den persönlichen An-/Abmelde-Link (what=abmeldung) verwenden.",Toast.LENGTH_LONG).show();})
            .setNeutralButton("Entfernen",(d,w)->{prefs.edit().remove(PREF_INTERNAL_URL).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);})
            .setNegativeButton("Abbrechen",null).show();
    }

    private String normalizeInternalUrl(String x){if(x==null)return "";return x.trim().replace("what=abmeldung_ics_feed","what=abmeldung");}
    private boolean validInternal(String x){if(x==null||x.isBlank())return false;try{Uri u=Uri.parse(x);return "https".equalsIgnoreCase(u.getScheme())&&"intern.pfvr.ch".equalsIgnoreCase(u.getHost())&&"abmeldung".equals(u.getQueryParameter("what"));}catch(Exception e){return false;}}

    private View webScreen(String url,boolean simplify){LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);root.addView(p,new LinearLayout.LayoutParams(-1,dp(2)));WebView w=web(simplify);activeWebView=w;w.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){p.setProgress(n);p.setVisibility(n>=100?View.GONE:View.VISIBLE);}});root.addView(w,new LinearLayout.LayoutParams(-1,0,1));w.loadUrl(url);return root;}
    private WebView web(boolean simplify){
        WebView web=new WebView(this);
        web.setBackgroundColor(simplify?Color.WHITE:themeBg(Color.WHITE));
        WebSettings settings=web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setLoadsImagesAutomatically(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        String userAgent=settings.getUserAgentString();
        if(userAgent!=null)settings.setUserAgentString(userAgent.replace("; wv","").replace("Version/4.0 ",""));
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(web,false);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){
                Uri uri=request.getUrl();
                if("https".equalsIgnoreCase(uri.getScheme())&&AppLinkPolicy.mayStayInPublicWebView(uri.getHost()))return false;
                external(uri.toString());
                return true;
            }
            @Override public void onPageFinished(WebView view,String url){
                super.onPageFinished(view,url);
                try{if(simplify&&AppLinkPolicy.isPfvrHost(Uri.parse(url).getHost()))skin(view);}catch(Exception ignored){}
            }
        });
        return web;
    }
    private void skin(WebView v){
        // Keep embedded PFVR content in a controlled light presentation. The native shell may stay dark,
        // but forcing arbitrary WordPress/PDF content dark caused unreadable white-on-white combinations.
        String css="html{color-scheme:light!important;}header,.site-header,.header-wrapper,nav,.main-navigation,footer,.site-footer,.scroll-top,.back-to-top{display:none!important;}html,body{background:#F4F7F9!important;}body{margin:0!important;padding:14px 14px 40px!important;font-family:Arial,sans-serif!important;color:#15232E!important;}main,.site-content,.content-area,.container,.wrapper{width:100%!important;max-width:none!important;margin:0!important;padding:0!important;}article,.post,.entry,.entry-content{background:#FFFFFF!important;color:#15232E!important;border-radius:16px!important;padding:16px!important;margin:0 0 14px!important;box-shadow:0 2px 10px rgba(0,0,0,.10)!important;}article p,article li,article span,.entry-content p,.entry-content li,.entry-content span,.entry-content div{color:#15232E!important;}img{max-width:100%!important;height:auto!important;border-radius:12px!important;}iframe{background:#FFFFFF!important;}a{color:#247E99!important;}h1,h2,h3,h4,h5,h6{color:#0C2D48!important;}";
        String js="(function(){var s=document.getElementById('pfvr-app-style');if(!s){s=document.createElement('style');s.id='pfvr-app-style';document.head.appendChild(s);}s.innerHTML='"+css.replace("\\","\\\\").replace("'","\\'")+"';})();"; v.evaluateJavascript(js,null);
    }
    private void openInApp(String url,String title){headerSubtitle.setText(title);content.removeAllViews();content.addView(webScreen(url,true));}

    private void loadCachedEvents(){String raw=prefs.getString(PREF_ICS_CACHE,"");eventsUpdated=prefs.getLong(PREF_ICS_UPDATED,0L);if(raw.trim().isEmpty())return;try{events=parseIcs(raw);}catch(Exception ex){events=new ArrayList<>();eventsUpdated=0L;prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).apply();}}
    private String calendarStatus(){if(eventsUpdated<=0)return eventsLoading?"Erster Abruf läuft im Hintergrund.":"Noch kein lokaler Kalender-Cache.";ZonedDateTime z=java.time.Instant.ofEpochMilli(eventsUpdated).atZone(ZoneId.of("Europe/Zurich"));String d=z.toLocalDate().equals(LocalDate.now(ZoneId.of("Europe/Zurich")))?"heute "+z.format(DateTimeFormatter.ofPattern("HH:mm")):z.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));return "Lokal gespeichert · zuletzt aktualisiert "+d+" Uhr";}
    private void refreshEvents(boolean toast,Runnable done){if(!toast&&eventsUpdated>0L&&System.currentTimeMillis()-eventsUpdated<60L*60L*1000L){if(done!=null)done.run();return;}if(eventsLoading){if(toast)Toast.makeText(this,"Kalender-Aktualisierung läuft bereits.",Toast.LENGTH_SHORT).show();return;}eventsLoading=true;new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(ICS).openConnection();c.setConnectTimeout(6000);c.setReadTimeout(8000);c.setUseCaches(true);c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/"+BuildConfig.VERSION_NAME);c.setRequestProperty("Accept","text/calendar,text/plain,*/*");if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),java.nio.charset.StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append(System.lineSeparator());br.close();String raw=sb.toString();List<Event> parsed=parseIcs(raw);if(parsed.isEmpty())throw new Exception("Keine kommenden Termine im Feed");long updated=System.currentTimeMillis();prefs.edit().putString(PREF_ICS_CACHE,raw).putLong(PREF_ICS_UPDATED,updated).apply();runOnUiThread(()->{events=parsed;eventsUpdated=updated;eventsLoading=false;if(toast)Toast.makeText(this,parsed.size()+" kommende Termine aktualisiert",Toast.LENGTH_SHORT).show();if(done!=null)done.run();});}catch(Exception ex){runOnUiThread(()->{eventsLoading=false;if(toast){String m=events.isEmpty()?"Kalender konnte gerade nicht geladen werden.":"Keine Verbindung – gespeicherter Kalenderstand bleibt sichtbar.";Toast.makeText(this,m,Toast.LENGTH_LONG).show();}else if(events.isEmpty())Toast.makeText(this,"Kalender lädt im Hintergrund. Bei langsamer Verbindung kann der erste Abruf etwas dauern.",Toast.LENGTH_LONG).show();if(done!=null)done.run();});}finally{if(c!=null)c.disconnect();}}).start();}

    private List<Event> parseIcs(String raw){
        List<String> lines=unfold(raw);
        List<Event> parsed=new ArrayList<>();
        Event currentEvent=null;
        for(String line:lines){
            if("BEGIN:VEVENT".equals(line)){
                currentEvent=new Event();
                continue;
            }
            if("END:VEVENT".equals(line)){
                if(currentEvent!=null&&(currentEvent.start!=null||currentEvent.recurrenceId!=null)){
                    if(currentEvent.start==null)currentEvent.start=currentEvent.recurrenceId;
                    parsed.add(currentEvent);
                }
                currentEvent=null;
                continue;
            }
            if(currentEvent==null)continue;
            int separator=line.indexOf(':');
            if(separator<0)continue;
            String key=line.substring(0,separator);
            String value=unesc(line.substring(separator+1));
            if(key.startsWith("SUMMARY"))currentEvent.title=value;
            else if(key.startsWith("LOCATION"))currentEvent.location=value;
            else if(key.startsWith("DESCRIPTION"))currentEvent.description=value;
            else if(key.startsWith("STATUS"))currentEvent.status=value;
            else if(key.startsWith("UID"))currentEvent.uid=value;
            else if(key.startsWith("DTSTART")){
                ParsedDate parsedDate=date(key,value);
                if(parsedDate!=null){currentEvent.start=parsedDate.z;currentEvent.allDay=parsedDate.allDay;}
            }else if(key.startsWith("DTEND")){
                ParsedDate parsedDate=date(key,value);
                if(parsedDate!=null)currentEvent.end=parsedDate.z;
            }else if(key.startsWith("RECURRENCE-ID")){
                ParsedDate parsedDate=date(key,value);
                if(parsedDate!=null){currentEvent.recurrenceId=parsedDate.z;if(currentEvent.start==null)currentEvent.allDay=parsedDate.allDay;}
            }else if(key.startsWith("RRULE"))currentEvent.rule=value;
            else if(key.startsWith("EXDATE")){
                for(String exception:value.split(",")){
                    ParsedDate parsedDate=date(key,exception);
                    if(parsedDate!=null)currentEvent.ex.add(parsedDate.z.toLocalDate());
                }
            }
        }

        ZoneId zone=ZoneId.of("Europe/Zurich");
        ZonedDateTime now=ZonedDateTime.now(zone).minusHours(6);
        ZonedDateTime limit=now.plusMonths(14);
        Map<String,Event> mastersByUid=new HashMap<>();
        List<Event> overrides=new ArrayList<>();
        List<Event> expanded=new ArrayList<>();

        for(Event event:parsed){
            if(event.recurrenceId!=null){overrides.add(event);continue;}
            if(event.uid!=null&&!event.uid.isBlank())mastersByUid.put(event.uid,event);
            expand(event,limit,expanded);
        }

        for(Event override:overrides){
            Event master=override.uid==null?null:mastersByUid.get(override.uid);
            ZonedDateTime occurrence=override.recurrenceId!=null?override.recurrenceId:override.start;
            expanded.removeIf(candidate->sameOccurrence(candidate,override.uid,occurrence));
            Event replacement=materializeOverride(override,master);
            if(replacement.start!=null)expanded.add(replacement);
        }

        expanded.removeIf(event->event.start==null||event.start.isAfter(limit)||eventEnd(event).isBefore(now));
        expanded.sort(Comparator.comparing(event->event.start));

        Map<String,Event> unique=new LinkedHashMap<>();
        for(Event event:expanded){
            if(event.title==null||event.title.isBlank())event.title="Vereinstermin";
            String identity=(event.uid==null||event.uid.isBlank()?TrainingMatcher.normalize(event.title):event.uid)+"|"+event.start.toInstant();
            Event previous=unique.get(identity);
            if(previous==null||(!isCancelledEvent(previous)&&isCancelledEvent(event)))unique.put(identity,event);
        }
        return new ArrayList<>(unique.values());
    }

    private Event materializeOverride(Event override,Event master){
        ZonedDateTime start=override.start!=null?override.start:override.recurrenceId;
        Event result=override.copy(start);
        result.rule=null;
        if(master==null)return result;
        if(result.title==null||result.title.isBlank())result.title=master.title;
        if(result.location==null||result.location.isBlank())result.location=master.location;
        if(result.description==null||result.description.isBlank())result.description=master.description;
        if(result.status==null||result.status.isBlank())result.status=master.status;
        if(result.uid==null||result.uid.isBlank())result.uid=master.uid;
        if(override.start==null)result.allDay=master.allDay;
        if(result.end==null&&result.start!=null&&master.end!=null&&master.start!=null){
            result.end=result.start.plus(java.time.Duration.between(master.start,master.end));
        }
        return result;
    }

    private boolean sameOccurrence(Event candidate,String uid,ZonedDateTime occurrence){
        if(candidate.start==null||occurrence==null)return false;
        if(uid!=null&&!uid.isBlank()&&!uid.equals(candidate.uid))return false;
        if(candidate.allDay)return candidate.start.toLocalDate().equals(occurrence.toLocalDate());
        return Math.abs(java.time.Duration.between(candidate.start,occurrence).toMinutes())<1;
    }

    private ZonedDateTime eventEnd(Event event){
        if(event.end!=null)return event.end;
        return event.allDay?event.start.plusDays(1):event.start.plusHours(1);
    }

    private List<String> unfold(String raw){
        List<String> out=new ArrayList<>();
        for(String line:raw.replace("\r\n","\n").replace('\r','\n').split("\n")){
            if((line.startsWith(" ")||line.startsWith("\t"))&&!out.isEmpty())out.set(out.size()-1,out.get(out.size()-1)+line.substring(1));
            else out.add(line);
        }
        return out;
    }

    private String unesc(String value){
        return value.replace("\\n","\n").replace("\\N","\n").replace("\\,",",").replace("\\;",";").replace("\\\\","\\");
    }

    private ParsedDate date(String key,String value){
        try{
            ZoneId local=ZoneId.of("Europe/Zurich");
            if(key.contains("VALUE=DATE")||value.length()==8)return new ParsedDate(LocalDate.parse(value,DateTimeFormatter.BASIC_ISO_DATE).atStartOfDay(local),true);
            boolean utc=value.endsWith("Z");
            String plain=utc?value.substring(0,value.length()-1):value;
            DateTimeFormatter formatter=plain.length()>=15?DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"):DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
            LocalDateTime localDateTime=LocalDateTime.parse(plain,formatter);
            if(utc)return new ParsedDate(localDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(local),false);
            String tz=null;
            int tzPosition=key.indexOf("TZID=");
            if(tzPosition>=0){tz=key.substring(tzPosition+5);int semicolon=tz.indexOf(';');if(semicolon>=0)tz=tz.substring(0,semicolon);tz=tz.replace("\"","");}
            ZoneId zone=tz==null?local:ZoneId.of(tz);
            return new ParsedDate(localDateTime.atZone(zone).withZoneSameInstant(local),false);
        }catch(Exception ignored){return null;}
    }

    private void expand(Event event,ZonedDateTime limit,List<Event> out){
        if(event.start==null)return;
        if(event.rule==null||event.rule.isBlank()){
            out.add(event.copy(event.start));
            return;
        }
        Map<String,String> rule=new HashMap<>();
        for(String part:event.rule.split(";")){
            int separator=part.indexOf('=');
            if(separator>0)rule.put(part.substring(0,separator),part.substring(separator+1));
        }
        String frequency=rule.getOrDefault("FREQ","");
        int interval=intval(rule.get("INTERVAL"),1);
        int count=intval(rule.get("COUNT"),10000);
        int made=0;
        ZonedDateTime until=limit;
        if(rule.get("UNTIL")!=null){
            ParsedDate parsedUntil=date("DTSTART",rule.get("UNTIL"));
            if(parsedUntil!=null&&parsedUntil.z.isBefore(until))until=parsedUntil.z;
        }
        if("WEEKLY".equals(frequency)){
            List<DayOfWeek> weekdays=days(rule.get("BYDAY"));
            if(weekdays.isEmpty())weekdays.add(event.start.getDayOfWeek());
            LocalDate week=event.start.toLocalDate().minusDays(event.start.getDayOfWeek().getValue()-1L);
            for(int weekOffset=0;made<count;weekOffset+=interval){
                LocalDate base=week.plusWeeks(weekOffset);
                if(base.atStartOfDay(event.start.getZone()).isAfter(until))break;
                for(DayOfWeek weekday:weekdays){
                    ZonedDateTime occurrence=ZonedDateTime.of(base.plusDays(weekday.getValue()-1L),event.start.toLocalTime(),event.start.getZone());
                    if(occurrence.isBefore(event.start)||occurrence.isAfter(until))continue;
                    made++;
                    if(!event.ex.contains(occurrence.toLocalDate()))out.add(event.copy(occurrence));
                    if(made>=count)break;
                }
            }
            return;
        }
        ZonedDateTime occurrence=event.start;
        while(made<count&&!occurrence.isAfter(until)){
            made++;
            if(!event.ex.contains(occurrence.toLocalDate()))out.add(event.copy(occurrence));
            if("DAILY".equals(frequency))occurrence=occurrence.plusDays(interval);
            else if("MONTHLY".equals(frequency))occurrence=occurrence.plusMonths(interval);
            else if("YEARLY".equals(frequency))occurrence=occurrence.plusYears(interval);
            else break;
        }
    }

    private int intval(String value,int fallback){
        try{return value==null?fallback:Integer.parseInt(value);}catch(Exception ignored){return fallback;}
    }

    private List<DayOfWeek> days(String value){
        List<DayOfWeek> out=new ArrayList<>();
        if(value==null)return out;
        Map<String,DayOfWeek> map=Map.of("MO",DayOfWeek.MONDAY,"TU",DayOfWeek.TUESDAY,"WE",DayOfWeek.WEDNESDAY,"TH",DayOfWeek.THURSDAY,"FR",DayOfWeek.FRIDAY,"SA",DayOfWeek.SATURDAY,"SU",DayOfWeek.SUNDAY);
        for(String day:value.split(",")){
            String plain=day.replaceAll("^[+-]?\\d+","");
            if(map.containsKey(plain))out.add(map.get(plain));
        }
        out.sort(Comparator.comparingInt(DayOfWeek::getValue));
        return out;
    }

    private LinearLayout body(){LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(14),dp(12),dp(14),dp(28));b.setBackgroundColor(themeBg(SURFACE));return b;}
    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(round(Color.WHITE,18));c.setElevation(dp(1));return c;}
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
    @Override protected void onDestroy(){if(dataRefreshHandler!=null)dataRefreshHandler.removeCallbacks(dataRefreshTick);if(activeWebView!=null)activeWebView.destroy();super.onDestroy();}

    private static class HydroPoint {final long time;final double value;HydroPoint(long t,double v){time=t;value=v;}}
    private static class TrendSeries {List<Long> times=new ArrayList<>();List<Double> values=new ArrayList<>();}
    private static class RiverStatus {final String label;final int bg,fg;RiverStatus(String l,int b,int f){label=l;bg=b;fg=f;}}

    private String axisLabel(double value,double step){
        double absolute=Math.abs(step);
        if(absolute>=1)return String.format(Locale.GERMAN,"%.0f",value);
        if(absolute>=0.1)return String.format(Locale.GERMAN,"%.1f",value);
        if(absolute>=0.01)return String.format(Locale.GERMAN,"%.2f",value);
        return String.format(Locale.GERMAN,"%.3f",value);
    }

    private class RiverTrendView extends View {
        private final TrendSeries series;
        private final RiverMetric metric;
        private final RiverRange range;
        private final HydroStation station;
        private final Paint grid=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint line=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint fill=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint point=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint threshold=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltip=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltipText=new Paint(Paint.ANTI_ALIAS_FLAG);
        private int selectedIndex=-1;

        RiverTrendView(Context context,TrendSeries series,RiverMetric metric,RiverRange range,HydroStation station){
            super(context);
            this.series=series;this.metric=metric;this.range=range;this.station=station;
            setClickable(true);setFocusable(true);
            line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(dp(2.4f));line.setStrokeCap(Paint.Cap.ROUND);line.setStrokeJoin(Paint.Join.ROUND);
            fill.setStyle(Paint.Style.FILL);point.setStyle(Paint.Style.FILL);grid.setStrokeWidth(dp(1));
            threshold.setStyle(Paint.Style.STROKE);threshold.setStrokeWidth(dp(1.3f));threshold.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(4)},0));
            label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);
            tooltipText.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);tooltipText.setTypeface(Typeface.DEFAULT_BOLD);
            tooltip.setStyle(Paint.Style.FILL);
            setContentDescription(metric.label+" der letzten "+range.label);
        }

        @Override public boolean performClick(){super.performClick();return true;}

        @Override public boolean onTouchEvent(MotionEvent event){
            if(series.times.size()<2)return super.onTouchEvent(event);
            if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE||event.getAction()==MotionEvent.ACTION_UP){
                float left=dp(52),right=getWidth()-dp(10);
                float x=Math.max(left,Math.min(right,event.getX()));
                long minTime=series.times.get(0),maxTime=series.times.get(series.times.size()-1);
                long target=minTime+Math.round((maxTime-minTime)*(x-left)/Math.max(1f,right-left));
                selectedIndex=HydroMath.nearestIndex(series.times,target);
                invalidate();
                if(event.getAction()==MotionEvent.ACTION_UP)performClick();
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_CANCEL){selectedIndex=-1;invalidate();return true;}
            return super.onTouchEvent(event);
        }

        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            if(series.values.size()<2||series.times.size()!=series.values.size())return;
            float width=getWidth(),height=getHeight();
            float left=dp(52),right=width-dp(10),top=dp(17),bottom=height-dp(30);
            long minTime=series.times.get(0),maxTime=series.times.get(series.times.size()-1);
            if(maxTime<=minTime)maxTime=minTime+1;
            HydroMath.AxisScale scale=HydroMath.niceAxis(series.values);

            grid.setColor(darkMode?Color.rgb(57,72,82):Color.rgb(220,229,234));
            label.setColor(themeText(MUTED));
            for(int index=0;index<=4;index++){
                float y=top+(bottom-top)*index/4f;
                canvas.drawLine(left,y,right,y,grid);
                double axisValue=scale.max-(scale.max-scale.min)*index/4d;
                String axisText=axisLabel(axisValue,scale.step);
                canvas.drawText(axisText,left-dp(5)-label.measureText(axisText),y+dp(3),label);
            }
            drawTimeGrid(canvas,left,right,top,bottom,minTime,maxTime);
            if(metric==RiverMetric.FLOW)drawFlowThresholds(canvas,scale,left,right,top,bottom);

            Path path=new Path();
            for(int index=0;index<series.values.size();index++){
                float x=seriesX(index,left,right,minTime,maxTime);
                float y=seriesY(series.values.get(index),scale,top,bottom);
                if(index==0)path.moveTo(x,y);else path.lineTo(x,y);
            }
            int actual=metric==RiverMetric.FLOW?statusTextColor(riverStatus(station,currentHydroValue(station,"Q")).bg):themeText(metric.color);
            Path area=new Path(path);
            area.lineTo(right,bottom);area.lineTo(left,bottom);area.close();
            fill.setColor(Color.argb(darkMode?34:24,Color.red(actual),Color.green(actual),Color.blue(actual)));
            canvas.drawPath(area,fill);
            line.setColor(actual);canvas.drawPath(path,line);

            int last=series.values.size()-1;
            float lastX=seriesX(last,left,right,minTime,maxTime),lastY=seriesY(series.values.get(last),scale,top,bottom);
            point.setColor(actual);canvas.drawCircle(lastX,lastY,dp(3.5f),point);
            point.setStyle(Paint.Style.STROKE);point.setStrokeWidth(dp(2));point.setColor(themeBg(Color.WHITE));canvas.drawCircle(lastX,lastY,dp(5f),point);point.setStyle(Paint.Style.FILL);
            if(selectedIndex>=0&&selectedIndex<series.values.size())drawSelection(canvas,scale,left,right,top,bottom,minTime,maxTime,actual);
        }

        private float seriesX(int index,float left,float right,long minTime,long maxTime){return left+(right-left)*(series.times.get(index)-minTime)/(float)(maxTime-minTime);}
        private float seriesY(double value,HydroMath.AxisScale scale,float top,float bottom){return (float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));}

        private void drawTimeGrid(Canvas canvas,float left,float right,float top,float bottom,long minTime,long maxTime){
            int intervals=range==RiverRange.HOUR?3:4;
            ZoneId zone=ZoneId.of("Europe/Zurich");
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            for(int index=0;index<=intervals;index++){
                float fraction=index/(float)intervals;
                float x=left+(right-left)*fraction;
                if(index>0&&index<intervals)canvas.drawLine(x,top,x,bottom,grid);
                long timestamp=minTime+Math.round((maxTime-minTime)*fraction);
                String value=java.time.Instant.ofEpochMilli(timestamp).atZone(zone).format(formatter);
                float textWidth=label.measureText(value);
                float textX=Math.max(left,Math.min(right-textWidth,x-textWidth/2f));
                canvas.drawText(value,textX,bottom+dp(18),label);
            }
        }

        private void drawFlowThresholds(Canvas canvas,HydroMath.AxisScale scale,float left,float right,float top,float bottom){
            double[] values={riverLow(station),riverWarn(station),riverAlarm(station)};
            String[] names={"Niedrig","Warnung","Alarm"};
            int[] colors={STATUS_LOW,STATUS_WARN,STATUS_ALARM};
            double epsilon=Math.max(1e-9,scale.step*1e-6);
            for(int index=0;index<values.length;index++){
                double value=values[index];
                if(value<scale.min-epsilon||value>scale.max+epsilon)continue;
                float y=seriesY(value,scale,top,bottom);
                int color=statusTextColor(colors[index]);
                threshold.setColor(color);canvas.drawLine(left,y,right,y,threshold);
                String text=names[index]+" "+String.format(Locale.GERMAN,"%.0f",value);
                tooltipText.setColor(color);
                float textWidth=tooltipText.measureText(text);
                float textHeight=Math.abs(tooltipText.ascent())+Math.abs(tooltipText.descent());
                float baseline=Math.max(top+textHeight+dp(2),Math.min(bottom-dp(2),y-dp(2)));
                RectF box=new RectF(right-textWidth-dp(12),baseline-textHeight-dp(5),right,baseline+dp(3));
                tooltip.setColor(themeBg(Color.WHITE));canvas.drawRoundRect(box,dp(4),dp(4),tooltip);
                canvas.drawText(text,box.left+dp(6),baseline,tooltipText);
            }
        }

        private void drawSelection(Canvas canvas,HydroMath.AxisScale scale,float left,float right,float top,float bottom,long minTime,long maxTime,int color){
            float x=seriesX(selectedIndex,left,right,minTime,maxTime),y=seriesY(series.values.get(selectedIndex),scale,top,bottom);
            Paint crosshair=new Paint(Paint.ANTI_ALIAS_FLAG);
            int muted=themeText(MUTED);
            crosshair.setColor(Color.argb(darkMode?150:105,Color.red(muted),Color.green(muted),Color.blue(muted)));crosshair.setStrokeWidth(dp(1));
            canvas.drawLine(x,top,x,bottom,crosshair);
            point.setColor(color);canvas.drawCircle(x,y,dp(5),point);
            point.setStyle(Paint.Style.STROKE);point.setStrokeWidth(dp(2));point.setColor(themeBg(Color.WHITE));canvas.drawCircle(x,y,dp(7),point);point.setStyle(Paint.Style.FILL);

            ZonedDateTime timestamp=java.time.Instant.ofEpochMilli(series.times.get(selectedIndex)).atZone(ZoneId.of("Europe/Zurich"));
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.MM. · HH:mm",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            String text=timestamp.format(formatter)+" · "+formatMetric(station,metric,series.values.get(selectedIndex))+" "+metricUnit(station,metric);
            tooltipText.setColor(darkMode?DARK_TEXT:Color.WHITE);
            float textWidth=tooltipText.measureText(text),textHeight=Math.abs(tooltipText.ascent())+Math.abs(tooltipText.descent());
            float boxWidth=textWidth+dp(16),boxLeft=Math.max(left,Math.min(right-boxWidth,x-boxWidth/2f));
            RectF box=new RectF(boxLeft,top+dp(4),boxLeft+boxWidth,top+textHeight+dp(14));
            tooltip.setColor(darkMode?DARK_SOFT:NAVY);canvas.drawRoundRect(box,dp(8),dp(8),tooltip);
            canvas.drawText(text,box.left+dp(8),box.bottom-dp(6),tooltipText);
            setContentDescription(metric.label+" "+text);
        }
    }

    private static class AppChoice {final String label,pkg;AppChoice(String l,String p){label=l;pkg=p;}}
    private static class ParsedDate {final ZonedDateTime z;final boolean allDay;ParsedDate(ZonedDateTime z,boolean a){this.z=z;allDay=a;}}
    private static class TrainingSlot {
        final ZonedDateTime start,end;final boolean fromCalendar;final String title;
        TrainingSlot(ZonedDateTime start,ZonedDateTime end,boolean fromCalendar,String title){this.start=start;this.end=end;this.fromCalendar=fromCalendar;this.title=title;}
    }
    private static class Event {
        String title,location,rule,description,status,uid;
        ZonedDateTime start,end,recurrenceId;
        boolean allDay;Set<LocalDate> ex=new LinkedHashSet<>();
        Event copy(ZonedDateTime newStart){
            Event copy=new Event();
            copy.title=title;copy.location=location;copy.rule=rule;copy.description=description;copy.status=status;copy.uid=uid;
            copy.start=newStart;copy.allDay=allDay;copy.recurrenceId=recurrenceId;copy.ex=new LinkedHashSet<>(ex);
            if(end!=null&&start!=null&&newStart!=null)copy.end=newStart.plus(java.time.Duration.between(start,end));
            return copy;
        }
    }
}
