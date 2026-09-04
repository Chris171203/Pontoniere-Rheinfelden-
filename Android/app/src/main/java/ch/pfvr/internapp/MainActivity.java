package ch.pfvr.internapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
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
    private static final String PREF_LANGUAGE = "ui_language";
    private static final String PREF_ACCESS_UNLOCKED = "access_unlocked_v1";
    private static final String PREF_BACKGROUND_REFRESH = "background_refresh";
    private static final String PREF_RIVER_LOW = "river_low";
    private static final String PREF_RIVER_WARN = "river_warn";
    private static final String PREF_RIVER_ALARM = "river_alarm";
    private static final String PREF_RIVER_RANGE = "river_range";
    private static final String PREF_RIVER_GRAPH_LEVEL_UNIT = "river_graph_level_unit";
    private static final String PREF_RIVER_SLOT1_STATION = "river_slot1_station";
    private static final String PREF_RIVER_SLOT2_STATION = "river_slot2_station";
    private static final String PREF_RIVER_SLOT2_ENABLED = "river_slot2_enabled";
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
    private static final String RIVER_NAVIGATION_SOURCE = "https://port-of-switzerland.ch/hafenservice/pegel/";


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

    private enum Screen { HOME, EVENTS, CASH, CLUB, NEWS, SETTINGS, TILE_SETTINGS, INTERNAL }

    private enum SettingsTab {
        GENERAL("Allgemein"),
        RIVER("Rhein"),
        PAYMENT("Zahlung");

        final String label;

        SettingsTab(String label) {
            this.label = label;
        }
    }

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
        FLOW("Abfluss","Q","m³/s",Color.rgb(77,119,153),0),
        LEVEL("Pegel","W","m ü.M.",WATER,2),
        TEMPERATURE("Temperatur","WT","°C",Color.rgb(70,157,177),1);

        final String label,parameter,unit;
        final int color,decimals;

        RiverMetric(String label,String parameter,String unit,int color,int decimals){
            this.label=label;this.parameter=parameter;this.unit=unit;this.color=color;this.decimals=decimals;
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
    private List<NewsRepository.Article> news = new ArrayList<>();
    private long newsUpdated = 0L;
    private volatile boolean newsLoading = false;
    private Bitmap pendingQrBitmap;
    private final Map<String,Integer> cashCart = new LinkedHashMap<>();
    private final Map<String,TextView> cashQuantityViews = new HashMap<>();
    private CashCatalog.Catalog cashCatalog;
    private LinearLayout cashSummaryContainer;
    private TextView cashTotalView;
    private EditText cashFreeAmountInput;
    private volatile boolean weatherLoading = false;
    private volatile boolean hydroLoading = false;
    private boolean darkMode = false;
    private SettingsTab settingsTab = SettingsTab.GENERAL;
    private TileLayoutStore tileLayoutStore;
    private TileLayoutStore.Area tileSettingsArea = TileLayoutStore.Area.HOME;
    private ScrollView tileSettingsScroll;
    private ScrollView homeScroll;
    private LinearLayout homeLiveStack;
    private Handler dataRefreshHandler;
    private final Runnable dataRefreshTick = new Runnable(){@Override public void run(){refreshLive(false);if(eventsUpdated<=0L||System.currentTimeMillis()-eventsUpdated>=60L*60L*1000L)refreshEvents(false,null);if(newsUpdated<=0L||System.currentTimeMillis()-newsUpdated>=60L*60L*1000L)refreshNews(false);if(dataRefreshHandler!=null)dataRefreshHandler.postDelayed(this,5L*60L*1000L);}};

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkMode = resolveDarkMode();
        applyWindowTheme();
        if(!prefs.getBoolean(PREF_ACCESS_UNLOCKED,false)){
            showFirstUseGate();
            return;
        }
        startUnlockedApp();
    }

    private void startUnlockedApp(){
        tileLayoutStore = new TileLayoutStore(prefs);
        dataRefreshHandler = new Handler(Looper.getMainLooper());
        scheduleBackgroundRefresh();
        loadCachedEvents();
        loadCachedNews();
        setContentView(buildShell());
        navigate(Screen.HOME);
        refreshEvents(false, () -> {
            if (current == Screen.HOME) rebuildHomePreservingScroll();
            else if (current == Screen.EVENTS) navigate(Screen.EVENTS);
        });
        refreshNews(false);
        refreshLive(false);
    }

    @Override protected void onResume(){
        super.onResume();
        if(prefs==null||!prefs.getBoolean(PREF_ACCESS_UNLOCKED,false))return;
        if(prefs!=null){
            loadCachedEvents();
            loadCachedNews();
            refreshNews(false);
            refreshLive(false);
            if(current==Screen.HOME)rebuildHomePreservingScroll();
            else if(current==Screen.EVENTS)navigate(Screen.EVENTS);
            else if(current==Screen.NEWS)navigate(Screen.NEWS);
        }
        if(dataRefreshHandler!=null){dataRefreshHandler.removeCallbacks(dataRefreshTick);dataRefreshHandler.postDelayed(dataRefreshTick,5L*60L*1000L);}
    }

    @Override protected void onPause(){
        if(dataRefreshHandler!=null)dataRefreshHandler.removeCallbacks(dataRefreshTick);
        super.onPause();
    }

    private String uiMode(){
        return UiLanguage.normalizeMode(prefs==null?UiLanguage.DE:prefs.getString(PREF_LANGUAGE,UiLanguage.DE));
    }

    private String ui(String value){
        return UiLanguage.translate(value,uiMode());
    }

    private void setUiLanguage(String mode){
        String normalized=UiLanguage.normalizeMode(mode);
        if(normalized.equals(uiMode()))return;
        prefs.edit().putString(PREF_LANGUAGE,normalized).apply();
        recreate();
    }

    private void showFirstUseGate(){
        ScrollView root=new ScrollView(this);
        root.setFillViewport(true);

        LinearLayout page=new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(dp(24),dp(34),dp(24),dp(32));
        page.setBackgroundColor(themeBg(SURFACE));
        root.addView(page,new ScrollView.LayoutParams(-1,-2));

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.pfvr_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        logo.setBackground(round(Color.WHITE,14));
        logo.setClipToOutline(true);
        LinearLayout.LayoutParams logoParams=new LinearLayout.LayoutParams(dp(84),dp(84));
        logoParams.setMargins(0,0,0,dp(14));
        page.addView(logo,logoParams);

        TextView title=txt("PFVR Rheinfelden",24,TEXT,true);
        title.setGravity(Gravity.CENTER);
        page.addView(title);
        TextView subtitle=txt("Willkommen",16,WATER,true);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0,dp(5),0,dp(12));
        page.addView(subtitle);

        LinearLayout languageOptions=segmentedBackground();
        boolean swissGerman=UiLanguage.isSwissGerman(uiMode());
        TextView german=segmentOption("Deutsch",!swissGerman);
        TextView swiss=segmentOption("Schwiizerdütsch",swissGerman);
        german.setOnClickListener(v->setUiLanguage(UiLanguage.DE));
        swiss.setOnClickListener(v->setUiLanguage(UiLanguage.SWISS_GERMAN));
        languageOptions.addView(german,segmentParams(languageOptions));
        languageOptions.addView(swiss,segmentParams(languageOptions));
        LinearLayout.LayoutParams languageParams=new LinearLayout.LayoutParams(-1,dp(42));
        languageParams.setMargins(0,0,0,dp(14));
        page.addView(languageOptions,languageParams);

        LinearLayout publicInfo=card();
        publicInfo.setOrientation(LinearLayout.VERTICAL);
        publicInfo.setPadding(dp(18),dp(17),dp(18),dp(17));
        page.addView(publicInfo,margin(-1,-2,0,0,0,12));
        publicInfo.addView(txt("Neu beim PFVR?",19,TEXT,true));
        TextView publicText=txt("Schnuppertraining ist auch vor der Mitgliedschaft möglich. Infos zu Einstieg, Mitgliedschaft und Formularen findest du auf pfvr.ch.",13,MUTED,false);
        publicText.setPadding(0,dp(5),0,dp(11));
        publicInfo.addView(publicText);
        Button join=btn("Schnuppertraining & Mitglied werden",NAVY,Color.WHITE);
        join.setOnClickListener(v->external(PublicLinks.JOIN));
        publicInfo.addView(join,new LinearLayout.LayoutParams(-1,dp(46)));
        TextView follow=txt("Folge uns",11,MUTED,true);
        follow.setPadding(0,dp(13),0,dp(6));
        publicInfo.addView(follow);
        LinearLayout socials=new LinearLayout(this);
        Button instagram=btn("Instagram",Color.rgb(232,240,244),NAVY);
        instagram.setOnClickListener(v->external(PublicLinks.INSTAGRAM));
        socials.addView(instagram,new LinearLayout.LayoutParams(0,dp(42),1));
        Button facebook=btn("Facebook",Color.rgb(232,240,244),NAVY);
        facebook.setOnClickListener(v->external(PublicLinks.FACEBOOK));
        LinearLayout.LayoutParams facebookParams=new LinearLayout.LayoutParams(0,dp(42),1);
        facebookParams.setMargins(dp(8),0,0,0);
        socials.addView(facebook,facebookParams);
        publicInfo.addView(socials);

        LinearLayout gate=card();
        gate.setOrientation(LinearLayout.VERTICAL);
        gate.setPadding(dp(18),dp(18),dp(18),dp(18));
        page.addView(gate,new LinearLayout.LayoutParams(-1,-2));
        gate.addView(txt("App freischalten",18,TEXT,true));
        TextView gateText=txt("Diese App kann interne Vereinsinformationen anzeigen. Gib den Freigabecode ein.",14,TEXT,false);
        gateText.setPadding(0,dp(6),0,0);
        gate.addView(gateText);
        TextView note=txt("Der Code wird nur zur lokalen Erstfreigabe geprüft. Persönliche PFVR-Links bleiben weiterhin ausschließlich auf diesem Gerät.",12,MUTED,false);
        note.setPadding(0,dp(8),0,dp(14));
        gate.addView(note);

        EditText codeInput=new EditText(this);
        codeInput.setSingleLine(true);
        codeInput.setHint("XXXX-XXXX-XXXX-XXXX");
        codeInput.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        codeInput.setTextColor(themeText(TEXT));
        codeInput.setHintTextColor(themeText(MUTED));
        codeInput.setBackground(round(Color.rgb(232,240,244),12));
        codeInput.setPadding(dp(14),0,dp(14),0);
        gate.addView(codeInput,new LinearLayout.LayoutParams(-1,dp(50)));

        TextView error=txt("Code stimmt nicht.",12,STATUS_ALARM,true);
        error.setVisibility(View.GONE);
        error.setPadding(0,dp(7),0,0);
        gate.addView(error);

        Button unlock=btn("Freischalten",NAVY,Color.WHITE);
        LinearLayout.LayoutParams unlockParams=new LinearLayout.LayoutParams(-1,dp(48));
        unlockParams.setMargins(0,dp(12),0,0);
        gate.addView(unlock,unlockParams);
        unlock.setOnClickListener(v->{
            if(!AccessGate.matches(codeInput.getText().toString())){
                error.setVisibility(View.VISIBLE);
                codeInput.selectAll();
                codeInput.requestFocus();
                return;
            }
            prefs.edit().putBoolean(PREF_ACCESS_UNLOCKED,true).apply();
            startUnlockedApp();
        });
        setContentView(root);
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
        headerBack.setContentDescription(ui("Zurück"));
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
        if(screen!=Screen.TILE_SETTINGS)tileSettingsScroll=null;
        if (headerBack != null) headerBack.setVisibility((screen == Screen.HOME || screen == Screen.INTERNAL) ? View.GONE : View.VISIBLE);
        Screen selectedNavigation=screen==Screen.TILE_SETTINGS?Screen.SETTINGS:screen;
        for (Map.Entry<Screen,TextView> e: navButtons.entrySet()) {
            boolean selected = e.getKey()==selectedNavigation;
            e.getValue().setTextColor(selected?Color.WHITE:themeText(Color.rgb(65,82,96)));
            e.getValue().setTypeface(Typeface.DEFAULT_BOLD);
            e.getValue().setBackground(selected?round(NAVY,16):null);
        }
        content.removeAllViews();
        switch(screen) {
            case HOME: headerSubtitle.setText(ui("Auf dem Rhein zuhause")); content.addView(home()); break;
            case EVENTS: headerSubtitle.setText(ui("Jahresprogramm")); content.addView(eventScreen()); break;
            case CASH: headerSubtitle.setText(ui("Vereinsbeiz bezahlen")); content.addView(cash()); break;
            case CLUB: headerSubtitle.setText(ui("Verein & Kontakt")); content.addView(club()); break;
            case NEWS: headerSubtitle.setText(ui("Vereinsnews")); content.addView(newsScreen()); break;
            case SETTINGS: headerSubtitle.setText(ui("Einstellungen")); content.addView(settings()); break;
            case TILE_SETTINGS: headerSubtitle.setText(ui("Kacheln anordnen")); content.addView(tileSettingsScreen()); break;
            case INTERNAL: headerSubtitle.setText(ui("Interner Bereich")); content.addView(internal()); break;
        }
    }

    private interface TileViewFactory {
    View create(TileLayoutStore.Spec spec);
}

private View home() {
    ScrollView scroll = new ScrollView(this);
    homeScroll=scroll;
    LinearLayout body = body();
    scroll.addView(body);

    LinearLayout hero = new LinearLayout(this);
    hero.setOrientation(LinearLayout.VERTICAL);
    hero.setPadding(dp(20),dp(19),dp(20),dp(19));
    GradientDrawable background = new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(20,91,115),WATER});
    background.setCornerRadius(dp(22));
    hero.setBackground(background);
    body.addView(hero,margin(-1,-2,0,4,0,14));
    hero.addView(txt("RHEINFELDEN  •  SEIT 1896",12,Color.rgb(208,231,239),true));
    TextView heading = txt("Gemeinsam auf dem Rhein.",29,Color.WHITE,true);
    heading.setPadding(0,dp(8),0,dp(5));
    hero.addView(heading);
    hero.addView(txt("Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit.",15,Color.rgb(232,243,247),false));
    LinearLayout actions = new LinearLayout(this);
    actions.setPadding(0,dp(17),0,0);
    hero.addView(actions);
    Button internal = btn("An-/Abmelden",Color.WHITE,NAVY);
    internal.setOnClickListener(v->navigate(Screen.INTERNAL));
    actions.addView(internal,new LinearLayout.LayoutParams(0,dp(46),1));
    Button pay = btn("Bezahlen",Color.argb(45,255,255,255),Color.WHITE);
    pay.setOnClickListener(v->navigate(Screen.CASH));
    LinearLayout.LayoutParams payParams = new LinearLayout.LayoutParams(0,dp(46),1);
    payParams.setMargins(dp(9),0,0,0);
    actions.addView(pay,payParams);
    TextView joinInfo=txt("Schnuppertraining & Mitglied werden  →",12,Color.WHITE,true);
    joinInfo.setGravity(Gravity.END);
    joinInfo.setPadding(dp(4),dp(12),dp(2),0);
    joinInfo.setOnClickListener(v->external(PublicLinks.JOIN));
    hero.addView(joinInfo);
    homeLiveStack=new LinearLayout(this);
    homeLiveStack.setOrientation(LinearLayout.VERTICAL);
    body.addView(homeLiveStack,new LinearLayout.LayoutParams(-1,-2));
    populateHomeTileStack(homeLiveStack);
    return scroll;
}

private void addConfiguredTiles(LinearLayout parent,TileLayoutStore.Area area,TileViewFactory factory){
    LinearLayout compactRow=null;
    for(TileLayoutStore.Spec spec:tileLayoutStore.ordered(area)){
        if(!tileLayoutStore.isVisible(spec))continue;
        View tile=factory.create(spec);
        if(tile==null)continue;
        if(spec.width==TileLayoutStore.Width.COMPACT){
            if(compactRow==null){
                compactRow=new LinearLayout(this);
                compactRow.setGravity(Gravity.TOP);
                compactRow.setBaselineAligned(false);
                parent.addView(compactRow,margin(-1,-2,0,0,0,10));
            }
            LinearLayout.LayoutParams tileParams=new LinearLayout.LayoutParams(0,-2,1);
            if(compactRow.getChildCount()>0)tileParams.setMargins(dp(8),0,0,0);
            compactRow.addView(tile,tileParams);
            if(compactRow.getChildCount()==2){
                LinearLayout completed=compactRow;
                completed.post(()->equalizeSummaryCardHeights(completed.getChildAt(0),completed.getChildAt(1)));
                compactRow=null;
            }
        }else{
            if(compactRow!=null){
                compactRow.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
                compactRow=null;
            }
            parent.addView(tile,margin(-1,-2,0,0,0,10));
        }
    }
    if(compactRow!=null)compactRow.addView(new View(this),new LinearLayout.LayoutParams(0,1,1));
}

private LinearLayout tileGroup(String title,String subtitle){
    LinearLayout group=new LinearLayout(this);
    group.setOrientation(LinearLayout.VERTICAL);
    TextView heading=txt(title,20,TEXT,true);
    heading.setPadding(dp(2),dp(2),0,subtitle==null?dp(9):dp(2));
    group.addView(heading);
    if(subtitle!=null){
        TextView detail=txt(subtitle,12,MUTED,false);
        detail.setPadding(dp(2),0,0,dp(9));
        group.addView(detail);
    }
    return group;
}

private void populateHomeTileStack(LinearLayout stack){
    stack.removeAllViews();
    addConfiguredTiles(stack,TileLayoutStore.Area.HOME,this::homeTileView);
}

private View homeTileView(TileLayoutStore.Spec spec){
    switch(spec.id){
        case "home_weather":return homeWeatherTile();
        case "home_river_summary":return homeRiverSummaryTile();
        case "home_river_charts":return homeRiverChartsTile();
        case "home_events":return homeEventsTile();
        case "home_news":return homeNewsTile();
        default:return null;
    }
}

private View homeWeatherTile(){
    LinearLayout group=tileGroup("Trainingswetter","Prognose für den nächsten relevanten Termin");
    group.addView(weatherCard(),new LinearLayout.LayoutParams(-1,-2));
    return group;
}

private View homeRiverSummaryTile(){
    LinearLayout group=tileGroup("Rhein aktuell","Abfluss, Pegel, Temperatur und Messdatenstand");
    group.addView(riverSummaryRow(),new LinearLayout.LayoutParams(-1,-2));
    return group;
}

private View homeRiverChartsTile(){
    LinearLayout group=tileGroup("Rhein-Grafiken","Abfluss und Pegel je Station; Temperatur separat");
    TextView rangeLabel=txt("ZEITRAUM",10,MUTED,true);
    rangeLabel.setPadding(dp(2),0,0,dp(5));
    group.addView(rangeLabel);
    group.addView(riverRangeSelector(riverRange()),new LinearLayout.LayoutParams(-1,dp(42)));
    boolean secondRiver=riverSlotEnabled(2);
    addRiverStationCharts(group,1,!secondRiver);
    if(secondRiver)addRiverStationCharts(group,2,true);
    return group;
}

private View homeEventsTile(){
    LinearLayout group=tileGroup("Als Nächstes","Aus dem öffentlichen Vereinskalender");
    if(events.isEmpty()){
        LinearLayout loading=card();
        loading.setGravity(Gravity.CENTER_VERTICAL);
        loading.addView(new ProgressBar(this),new LinearLayout.LayoutParams(dp(34),dp(34)));
        TextView text=txt("Termine werden geladen …",14,MUTED,false);
        text.setPadding(dp(12),0,0,0);
        loading.addView(text);
        group.addView(loading,margin(-1,-2,0,0,0,4));
    }else{
        for(int index=0;index<Math.min(3,events.size());index++)group.addView(eventCard(events.get(index),true));
    }
    TextView all=link("Alle Termine anzeigen  →");
    all.setOnClickListener(v->navigate(Screen.EVENTS));
    group.addView(all);
    return group;
}

private View homeNewsTile(){
    LinearLayout group=tileGroup("Aktuell vom Verein",ui("News von pfvr.ch")+" · "+newsStatus());
    if(news.isEmpty()){
        LinearLayout loading=card();
        loading.setGravity(Gravity.CENTER_VERTICAL);
        loading.addView(new ProgressBar(this),new LinearLayout.LayoutParams(dp(34),dp(34)));
        TextView text=txt(newsLoading?"News werden geladen …":"Noch kein gespeicherter News-Stand.",14,MUTED,false);
        text.setPadding(dp(12),0,0,0);
        loading.addView(text);
        group.addView(loading,margin(-1,-2,0,0,0,4));
    }else{
        for(int index=0;index<Math.min(3,news.size());index++)group.addView(newsCard(news.get(index),true));
    }
    TextView all=link("Alle News anzeigen  →");
    all.setOnClickListener(v->navigate(Screen.NEWS));
    group.addView(all);
    return group;
}

private void addRiverStationCharts(LinearLayout stack,int slot,boolean last){
    HydroStation station=riverStation(slot);
    boolean temperature=station.supportsTemperature;
    stack.addView(riverCombinedCard(slot),margin(-1,-2,0,10,0,temperature?10:(last?4:10)));
    if(temperature)stack.addView(riverTemperatureCard(slot),margin(-1,-2,0,0,0,last?4:10));
}

private void refreshHomeLiveViews(){
    if(current!=Screen.HOME||homeLiveStack==null)return;
    final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
    populateHomeTileStack(homeLiveStack);
    if(homeScroll!=null){
        homeScroll.postOnAnimation(()->homeScroll.scrollTo(0,scrollY));
        homeScroll.postDelayed(()->homeScroll.scrollTo(0,scrollY),80);
    }
}

private void rebuildHomePreservingScroll(){
    if(current!=Screen.HOME)return;
    final int scrollY=homeScroll==null?0:homeScroll.getScrollY();
    navigate(Screen.HOME);
    if(homeScroll!=null){
        homeScroll.postOnAnimation(()->homeScroll.scrollTo(0,scrollY));
        homeScroll.postDelayed(()->homeScroll.scrollTo(0,scrollY),80);
    }
}

    private LinearLayout weatherCard(){
        LinearLayout c=card(); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(15),dp(16),dp(14));
        String[] x=weatherSummary();
        c.addView(txt(x[0],11,WATER,true));
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(0,dp(5),0,dp(5)); c.addView(row);
        TextView icon=txtRaw(x[5],38,themeText(TEXT),false); icon.setGravity(Gravity.CENTER); row.addView(icon,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout info=new LinearLayout(this); info.setOrientation(LinearLayout.VERTICAL); info.setPadding(dp(7),0,0,0); row.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        info.addView(txtRaw(x[1],15,MUTED,true));
        TextView main=txtRaw(x[2],21,TEXT,true); main.setPadding(0,dp(2),0,0); info.addView(main);
        TextView details=txtRaw(x[3],13,MUTED,false); details.setPadding(0,dp(5),0,0); c.addView(details);
        TextView src=txtRaw(x[4],10,Color.rgb(126,140,150),false); src.setPadding(0,dp(8),0,0); c.addView(src);
        return c;
    }

    private View riverSummaryRow(){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.TOP);
        row.setBaselineAligned(false);

        LinearLayout first=riverSummaryCard(1);
        row.addView(first,new LinearLayout.LayoutParams(0,-2,1));

        if(riverSlotEnabled(2)){
            LinearLayout second=riverSummaryCard(2);
            LinearLayout.LayoutParams secondParams=new LinearLayout.LayoutParams(0,-2,1);
            secondParams.setMargins(dp(8),0,0,0);
            row.addView(second,secondParams);
            row.post(()->equalizeSummaryCardHeights(first,second));
        }
        return row;
    }

    private void equalizeSummaryCardHeights(View first,View second){
        int height=Math.max(first.getMeasuredHeight(),second.getMeasuredHeight());
        if(height<=0)return;
        setViewHeight(first,height);
        setViewHeight(second,height);
    }

    private void setViewHeight(View view,int height){
        ViewGroup.LayoutParams params=view.getLayoutParams();
        if(params==null||params.height==height)return;
        params.height=height;
        view.setLayoutParams(params);
    }

    private LinearLayout riverSummaryCard(int slot){
        HydroStation station=riverStation(slot);
        double flow=currentHydroValue(station,"Q");
        double rawLevel=currentHydroValue(station,"W");
        double level=displayHydroValue(station,RiverMetric.LEVEL,rawLevel);
        double gaugeCm=RiverDisplay.hasVerifiedGaugeCentimetres(station)?RiverDisplay.gaugeCentimetres(station,rawLevel):Double.NaN;
        double temperature=station.supportsTemperature?currentHydroValue(station,"WT"):Double.NaN;
        RhineNavigation.Stage stage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;
        int levelColor=navigationLevelColor(stage);
        int flowColor=navigationFlowColor(stage);

        LinearLayout c=card();
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(12),dp(11),dp(12),dp(11));
        c.setOnClickListener(v->external(station.stationUrl()));

        TextView stationName=txt(station.label,11,WATER,true);
        stationName.setMaxLines(1);
        c.addView(stationName);

        LinearLayout levelRow=new LinearLayout(this);
        levelRow.setGravity(Gravity.CENTER_VERTICAL);
        levelRow.setPadding(0,dp(6),0,0);
        String levelText=Double.isFinite(level)?formatMetric(station,RiverMetric.LEVEL,level):"–";
        levelRow.addView(riverSummaryMetric("Pegel",levelText,metricUnit(station,RiverMetric.LEVEL),levelColor,25),new LinearLayout.LayoutParams(0,-2,1));
        if(station==HydroStation.BASEL_RHEINHALLE){
            LinearLayout.LayoutParams badgeParams=new LinearLayout.LayoutParams(-2,-2);
            badgeParams.setMargins(dp(6),dp(8),0,0);
            levelRow.addView(navigationBadge(stage),badgeParams);
        }
        c.addView(levelRow);

        if(Double.isFinite(gaugeCm)){
            TextView gaugeView=txt(String.format(Locale.GERMAN,"%.0f cm",gaugeCm),10,levelColor,true);
            gaugeView.setTextColor(levelColor);
            gaugeView.setPadding(0,dp(1),0,0);
            c.addView(gaugeView);
        }

        String flowText=Double.isFinite(flow)?formatMetric(station,RiverMetric.FLOW,flow):"–";
        LinearLayout flowMetric=riverSummaryMetric("Abfluss",flowText,"m³/s",flowColor,17);
        flowMetric.setPadding(0,dp(6),0,0);
        c.addView(flowMetric,new LinearLayout.LayoutParams(-1,-2));

        if(Double.isFinite(temperature)){
            TextView temperatureView=txtRaw(ui("Wasser")+" "+formatMetric(station,RiverMetric.TEMPERATURE,temperature)+" °C",11,MUTED,false);
            temperatureView.setPadding(0,dp(5),0,0);
            c.addView(temperatureView);
        }

        View timestampSpacer=new View(this);
        c.addView(timestampSpacer,new LinearLayout.LayoutParams(1,0,1));
        String[] summary=hydroSummary(station);
        TextView stand=txtRaw(summary[3],10,Color.rgb(126,140,150),false);
        stand.setMaxLines(1);
        stand.setPadding(0,dp(7),0,0);
        c.addView(stand);
        return c;
    }

    private LinearLayout riverSummaryMetric(String label,String value,String unit,int color,float valueSize){
        LinearLayout metric=new LinearLayout(this);
        metric.setOrientation(LinearLayout.VERTICAL);
        TextView labelView=txt(label,9,MUTED,true);
        metric.addView(labelView);
        LinearLayout valueLine=new LinearLayout(this);
        valueLine.setGravity(Gravity.CENTER_VERTICAL);
        TextView valueView=txt(value,valueSize,color,true);
        valueView.setTextColor(color);
        valueLine.addView(valueView);
        TextView unitView=txt(unit,10,color,true);
        unitView.setTextColor(color);
        unitView.setPadding(dp(4),dp(2),0,0);
        valueLine.addView(unitView);
        metric.addView(valueLine);
        return metric;
    }

    private View navigationBadge(RhineNavigation.Stage stage){
        int color=navigationLevelColor(stage);
        TextView badge=txt(RhineNavigation.shortLabel(stage),10,color,true);
        badge.setTextColor(color);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(8),dp(4),dp(8),dp(4));
        badge.setBackground(statusBadge(color));
        return badge;
    }

    private View riverStatusCompact(RiverStatus status){
        int color=statusTextColor(status.bg);
        TextView label=txt(status.label,10,color,true);
        label.setTextColor(color);
        label.setGravity(Gravity.CENTER);
        label.setPadding(dp(8),dp(4),dp(8),dp(4));
        label.setBackground(statusBadge(status.bg));
        return label;
    }

    private LinearLayout riverTemperatureCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        TrendSeries temperature=hydroSeries(station,"WT",range);
        double temperatureNow=currentHydroValue(station,"WT");
        HydroMath.Stats stats=HydroMath.stats(temperature.values);
        int temperatureColor=themeText(RiverMetric.TEMPERATURE.color);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(14));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(txtRaw(ui("Wassertemperatur")+" · "+station.label,12,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView source=txt("BAFU "+station.id+"  ↗",11,WATER,true);
        source.setOnClickListener(v->external(station.stationUrl()));
        source.setPadding(dp(8),dp(4),0,dp(4));
        header.addView(source,new LinearLayout.LayoutParams(-2,-2));
        card.addView(header);

        TextView value=txt(Double.isFinite(temperatureNow)?formatMetric(station,RiverMetric.TEMPERATURE,temperatureNow)+" °C":"–",24,temperatureColor,true);
        value.setTextColor(temperatureColor);
        value.setPadding(0,dp(7),0,dp(3));
        card.addView(value);

        if(stats.isValid()){
            String change=(stats.change()>=0?"+":"")+formatMetric(station,RiverMetric.TEMPERATURE,stats.change());
            TextView summary=txt(
                    "Min "+formatMetric(station,RiverMetric.TEMPERATURE,stats.min)+" °C · Max "+formatMetric(station,RiverMetric.TEMPERATURE,stats.max)+" °C · Δ "+change+" °C",
                    11,
                    MUTED,
                    false
            );
            summary.setPadding(0,0,0,dp(5));
            card.addView(summary);
        }

        if(temperature.values.size()>=2){
            SingleSeriesTrendView graph=new SingleSeriesTrendView(
                    this,
                    temperature.times,
                    temperature.values,
                    range==RiverRange.WEEK,
                    range.label,
                    "Wassertemperatur",
                    "°C",
                    1,
                    temperatureColor,
                    darkMode?Color.rgb(57,72,82):Color.rgb(220,229,234),
                    themeText(MUTED),
                    themeBg(Color.WHITE),
                    darkMode?DARK_SOFT:NAVY,
                    darkMode?DARK_TEXT:Color.WHITE
            );
            card.addView(graph,new LinearLayout.LayoutParams(-1,dp(205)));
            TextView hint=txt("Diagramm berühren für Einzelwerte",10,MUTED,false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0,dp(4),0,0);
            card.addView(hint);
        }else{
            TextView unavailable=txt("Für den gewählten Zeitraum liegen noch nicht genügend Temperaturwerte vor.",12,MUTED,false);
            unavailable.setPadding(0,dp(8),0,dp(3));
            card.addView(unavailable);
        }
        return card;
    }

    private LinearLayout riverCombinedCard(int slot){
        HydroStation station=riverStation(slot);
        RiverRange range=riverRange();
        String[] summary=hydroSummary(station);
        TrendSeries flow=hydroSeries(station,"Q",range);
        TrendSeries level=hydroSeries(station,"W",range);
        double flowNow=currentHydroValue(station,"Q");
        double levelNow=graphLevelValue(station,currentHydroValue(station,"W"));
        RhineNavigation.Stage stage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;
        int flowColor=navigationFlowColor(stage);
        int levelColor=navigationLevelColor(stage);

        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16),dp(14),dp(16),dp(14));

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(txt(station.label+" · BAFU "+station.id,12,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        TextView source=txt("BAFU  ↗",11,WATER,true);
        source.setOnClickListener(v->external(station.stationUrl()));
        source.setPadding(dp(8),dp(4),0,dp(4));
        header.addView(source,new LinearLayout.LayoutParams(-2,-2));
        card.addView(header);

        LinearLayout values=new LinearLayout(this);
        values.setPadding(0,dp(8),0,dp(6));
        LinearLayout flowBox=new LinearLayout(this);
        flowBox.setOrientation(LinearLayout.VERTICAL);
        flowBox.addView(txt("Abfluss",11,MUTED,true));
        TextView q=txt(Double.isFinite(flowNow)?formatMetric(station,RiverMetric.FLOW,flowNow)+" m³/s":"–",16,flowColor,true);
        q.setTextColor(flowColor);
        flowBox.addView(q);
        values.addView(flowBox,new LinearLayout.LayoutParams(0,-2,1));

        LinearLayout levelBox=new LinearLayout(this);
        levelBox.setOrientation(LinearLayout.VERTICAL);
        levelBox.setGravity(Gravity.END);
        TextView levelTitle=txt("Pegel",11,MUTED,true);
        levelTitle.setGravity(Gravity.END);
        levelBox.addView(levelTitle);
        TextView w=txt(Double.isFinite(levelNow)?formatGraphLevel(station,levelNow)+" "+graphLevelUnit(station):"–",24,levelColor,true);
        w.setTextColor(levelColor);
        w.setGravity(Gravity.END);
        levelBox.addView(w);
        values.addView(levelBox,new LinearLayout.LayoutParams(0,-2,1));
        card.addView(values);

        if(station==HydroStation.BASEL_RHEINHALLE){
            TextView navigation=txt(ui("Schifffahrtslage")+" · "+ui(RhineNavigation.shortLabel(stage)),11,levelColor,true);
            navigation.setTextColor(levelColor);
            card.addView(navigation);
            TextView navigationDetail=txt(RhineNavigation.detail(stage),10,MUTED,false);
            navigationDetail.setPadding(0,dp(2),0,dp(5));
            card.addView(navigationDetail);
        }else{
            TextView basis=txt("Schifffahrtslage richtet sich nach Pegel Basel-Rheinhalle.",10,MUTED,false);
            basis.setPadding(0,0,0,dp(5));
            card.addView(basis);
        }

        if(RiverDisplay.hasVerifiedGaugeCentimetres(station)){
            TextView levelUnitLabel=txt("PEGEL-EINHEIT",9,MUTED,true);
            levelUnitLabel.setPadding(0,dp(2),0,dp(4));
            card.addView(levelUnitLabel);
            card.addView(riverGraphLevelUnitSelector(station),new LinearLayout.LayoutParams(-1,dp(38)));
        }

        if(flow.values.size()>=2&&level.values.size()>=2){
            DualRiverTrendView graph=new DualRiverTrendView(this,flow,level,range,station);
            card.addView(graph,new LinearLayout.LayoutParams(-1,dp(224)));
            TextView hint=txt(ui("Abfluss links")+" · "+ui("Pegel rechts")+" ("+graphLevelUnit(station)+") · "+ui("Diagramm berühren für Einzelwerte"),10,MUTED,false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0,dp(4),0,0);
            card.addView(hint);
        }else{
            TextView missing=txt("Für diesen Zeitraum liegen noch nicht genug Abfluss- und Pegelwerte vor.",12,MUTED,false);
            missing.setGravity(Gravity.CENTER);
            missing.setPadding(dp(8),dp(22),dp(8),dp(22));
            missing.setBackground(round(Color.rgb(238,243,246),14));
            card.addView(missing,margin(-1,-2,0,7,0,0));
        }

        TextView src=txtRaw(summary[3]+" · "+ui(range.sourceLabel),10,Color.rgb(126,140,150),false);
        src.setPadding(0,dp(8),0,0);
        card.addView(src);
        return card;
    }

    private boolean riverSlotEnabled(int slot){return slot==1||prefs.getBoolean(PREF_RIVER_SLOT2_ENABLED,true);}

    private HydroStation riverStation(int slot){
        String key=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
        HydroStation fallback=slot==1?HydroStation.BASEL_RHEINHALLE:HydroStation.RHEINFELDEN;
        return HydroStation.from(prefs.getString(key,fallback.id),fallback);
    }

    private RiverRange riverRange(){return RiverRange.from(prefs.getString(PREF_RIVER_RANGE,RiverRange.DAY.label));}
    private View riverRangeSelector(RiverRange selected){
        LinearLayout outer=segmentedBackground();
        for(RiverRange range:RiverRange.values()){
            TextView option=segmentOption(range.label,range==selected);
            option.setContentDescription(ui("Rheinwerte")+" "+range.label);
            option.setOnClickListener(v->{
                prefs.edit().putString(PREF_RIVER_RANGE,range.label).apply();
                if(current==Screen.HOME)refreshHomeLiveViews();
            });
            outer.addView(option,segmentParams(outer));
        }
        return outer;
    }

    private String riverGraphLevelUnitPreferenceKey(HydroStation station){
        return PREF_RIVER_GRAPH_LEVEL_UNIT+"_"+station.id;
    }

    private boolean riverGraphLevelCentimetres(HydroStation station){
        if(!RiverDisplay.hasVerifiedGaugeCentimetres(station))return false;
        String key=riverGraphLevelUnitPreferenceKey(station);
        if(prefs.contains(key))return "cm".equals(prefs.getString(key,"m"));
        return "cm".equals(prefs.getString(PREF_RIVER_GRAPH_LEVEL_UNIT,"m"));
    }

    private View riverGraphLevelUnitSelector(HydroStation station){
        LinearLayout outer=segmentedBackground();
        boolean centimetres=riverGraphLevelCentimetres(station);
        TextView absolute=segmentOption("m ü.M.",!centimetres);
        absolute.setContentDescription(ui("Pegel")+" "+station.label+" "+ui("im Diagramm in Meter über Meer"));
        absolute.setOnClickListener(v->{
            prefs.edit().putString(riverGraphLevelUnitPreferenceKey(station),"m").apply();
            if(current==Screen.HOME)refreshHomeLiveViews();
        });
        outer.addView(absolute,segmentParams(outer));
        TextView relative=segmentOption("cm",centimetres);
        relative.setContentDescription(ui("Pegel")+" "+station.label+" "+ui("im Diagramm in Zentimetern"));
        relative.setOnClickListener(v->{
            prefs.edit().putString(riverGraphLevelUnitPreferenceKey(station),"cm").apply();
            if(current==Screen.HOME)refreshHomeLiveViews();
        });
        outer.addView(relative,segmentParams(outer));
        return outer;
    }

    private double graphLevelValue(HydroStation station,double rawValue){
        return RiverDisplay.graphLevelValue(station,rawValue,riverGraphLevelCentimetres(station));
    }

    private String graphLevelUnit(HydroStation station){
        return RiverDisplay.graphLevelUnit(station,riverGraphLevelCentimetres(station));
    }

    private String formatGraphLevel(HydroStation station,double value){
        if(!Double.isFinite(value))return "–";
        return RiverDisplay.graphLevelDecimals(station,riverGraphLevelCentimetres(station))==0
                ?String.format(Locale.GERMAN,"%.0f",value)
                :String.format(Locale.GERMAN,"%.2f",value);
    }

    private RhineNavigation.Stage navigationStage(){
        double raw=currentHydroValue(HydroStation.BASEL_RHEINHALLE,"W");
        return RhineNavigation.fromBaselGaugeCm(RiverDisplay.gaugeCentimetres(HydroStation.BASEL_RHEINHALLE,raw));
    }

    private RhineNavigation.Stage navigationStageForGraphValue(HydroStation station,double displayedLevel){
        if(station!=HydroStation.BASEL_RHEINHALLE)return RhineNavigation.Stage.NORMAL;
        if(!Double.isFinite(displayedLevel))return RhineNavigation.Stage.UNKNOWN;
        double centimetres=riverGraphLevelCentimetres(station)
                ?displayedLevel
                :RiverDisplay.gaugeCentimetres(station,displayedLevel);
        return RhineNavigation.fromBaselGaugeCm(centimetres);
    }

    private int navigationLevelColor(RhineNavigation.Stage stage){
        if(stage==RhineNavigation.Stage.UNKNOWN)return themeText(MUTED);
        if(stage==RhineNavigation.Stage.NORMAL)return themeText(WATER);
        if(stage==RhineNavigation.Stage.HWM_I)return darkMode?Color.rgb(255,216,92):Color.rgb(205,143,0);
        if(stage==RhineNavigation.Stage.HWM_IIB)return darkMode?Color.rgb(255,148,86):Color.rgb(220,92,38);
        return darkMode?Color.rgb(255,105,105):Color.rgb(200,55,55);
    }

    private int navigationFlowColor(RhineNavigation.Stage stage){
        if(stage==RhineNavigation.Stage.UNKNOWN)return themeText(MUTED);
        if(stage==RhineNavigation.Stage.NORMAL)return darkMode?Color.rgb(126,171,204):Color.rgb(77,119,153);
        if(stage==RhineNavigation.Stage.HWM_I)return darkMode?Color.rgb(224,169,72):Color.rgb(174,103,0);
        if(stage==RhineNavigation.Stage.HWM_IIB)return darkMode?Color.rgb(225,111,76):Color.rgb(166,66,31);
        return darkMode?Color.rgb(210,77,88):Color.rgb(139,39,45);
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

    private GradientDrawable statusDot(int color){GradientDrawable d=new GradientDrawable();d.setShape(GradientDrawable.OVAL);d.setColor(color);return d;}

    private String formatMetric(RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(metric.decimals==0)return String.format(Locale.GERMAN,"%.0f",value);
        if(metric.decimals==1)return String.format(Locale.GERMAN,"%.1f",value);
        return String.format(Locale.GERMAN,"%.2f",value);
    }

    private String formatMetric(HydroStation station,RiverMetric metric,double value){
        if(!Double.isFinite(value))return "–";
        if(metric==RiverMetric.LEVEL){
            int decimals=RiverDisplay.levelDecimals(station);
            if(decimals==0)return String.format(Locale.GERMAN,"%.0f",value);
            return String.format(Locale.GERMAN,"%.2f",value);
        }
        return formatMetric(metric,value);
    }

    private String metricUnit(HydroStation station,RiverMetric metric){
        return metric==RiverMetric.LEVEL?RiverDisplay.levelUnit(station):metric.unit;
    }

    private double displayHydroValue(HydroStation station,RiverMetric metric,double rawValue){
        if(!Double.isFinite(rawValue))return rawValue;
        return metric==RiverMetric.LEVEL?RiverDisplay.levelValue(station,rawValue):rawValue;
    }

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
            if(end.isAfter(now))return new TrainingSlot(start,end,false,ui("Regelmässiges Training"));
        }
        LocalDate fallback=first.plusDays(1);
        while(!regularTrainingDay(fallback))fallback=fallback.plusDays(1);
        return new TrainingSlot(fallback.atTime(regularTrainingStart(fallback)).atZone(zone),fallback.atTime(regularTrainingEnd(fallback)).atZone(zone),false,ui("Regelmässiges Training"));
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
        String date=localizedDateWords(cap(slot.start.format(DateTimeFormatter.ofPattern("EEEE, dd.MM.",Locale.GERMAN))))+" · "+trainingTimeLabel(slot);
        if(slot.fromCalendar&&slot.title!=null&&!slot.title.isBlank())date+="\n"+slot.title;
        String raw=prefs.getString(PREF_WEATHER_CACHE,"");
        long updated=prefs.getLong(PREF_WEATHER_UPDATED,0L);
        String source=prefs.getString(PREF_WEATHER_SOURCE,"MeteoSwiss ICON via Open-Meteo");
        String provenance=ui(slot.fromCalendar?"Vereinskalender":"Regelplan")+" · "+weatherAge(source,updated);
        if(raw.trim().isEmpty())return new String[]{ui("NÄCHSTES TRAINING"),date,ui("Wetter wird geladen …"),ui("Prognose wird im Hintergrund aktualisiert."),provenance,"◌"};
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
            if(count==0)return new String[]{ui("NÄCHSTES TRAINING"),date,ui("Noch keine Prognose"),ui("Für diesen Trainingszeitraum liegen noch keine Stundenwerte vor."),provenance,"◌"};
            String temperatureText=Double.isNaN(firstTemperature)?"":String.format(Locale.GERMAN,"%.0f °C",firstTemperature);
            if(Double.isFinite(lastTemperature)&&Double.isFinite(firstTemperature)&&Math.abs(lastTemperature-firstTemperature)>=1.0)temperatureText+=String.format(Locale.GERMAN," → %.0f °C",lastTemperature);
            String main=temperatureText+(temperatureText.isEmpty()?"":" · ")+weatherCode(codeValue);
            String details=ui("Regen")+" "+probabilityMax+" % · "+String.format(Locale.GERMAN,"%.1f mm",precipitationSum)+"\n"+ui("Wind")+" "+Math.round(windMax)+" km/h · "+ui("Böen")+" "+Math.round(gustMax)+" km/h";
            if(Double.isFinite(uvMax))details+="\nUV "+String.format(Locale.GERMAN,"%.1f",uvMax)+" · "+uvLabel(uvMax);
            return new String[]{ui("NÄCHSTES TRAINING"),date,main,details,provenance,weatherIcon(codeValue)};
        }catch(Exception e){
            return new String[]{ui("NÄCHSTES TRAINING"),date,ui("Gespeicherte Wetterdaten nicht lesbar"),ui("Letzter Stand bleibt erhalten, sobald wieder gültige Daten vorliegen."),provenance,"◌"};
        }
    }

    private String uvLabel(double uv){if(uv<3)return ui("niedrig");if(uv<6)return ui("mässig");if(uv<8)return ui("hoch");if(uv<11)return ui("sehr hoch");return ui("extrem");}

    private String weatherIcon(int c){if(c==0)return "☀";if(c<=2)return "⛅";if(c==3)return "☁";if(c==45||c==48)return "🌫";if(c>=51&&c<=67)return "🌧";if(c>=71&&c<=77)return "❄";if(c>=80&&c<=82)return "🌦";if(c>=85&&c<=86)return "🌨";if(c>=95)return "⚡";return "◌";}

    private String weatherAge(String source,long updated){if(updated<=0)return source;long min=Math.max(0,(System.currentTimeMillis()-updated)/60000);return source+(min>90?" · Cache "+(min/60)+" h":" · "+ui("vor")+" "+min+" min");}
    private String weatherCode(int c){if(c==0)return ui("klar");if(c<=2)return ui("leicht bewölkt");if(c==3)return ui("bewölkt");if(c==45||c==48)return ui("Nebel");if(c>=51&&c<=57)return ui("Nieselregen");if(c>=61&&c<=67)return ui("Regen");if(c>=71&&c<=77)return ui("Schnee");if(c>=80&&c<=82)return ui("Schauer");if(c>=85&&c<=86)return ui("Schneeschauer");if(c>=95)return ui("Gewitter");return ui("Wetter");}

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

    private RiverStatus riverStatus(HydroStation station,double flow){
        if(Double.isNaN(flow))return new RiverStatus("Keine Daten",Color.rgb(109,120,128),Color.WHITE);
        if(flow<riverLow(station))return new RiverStatus("Niedrig",STATUS_LOW,Color.WHITE);
        if(flow>=riverAlarm(station))return new RiverStatus("Alarm",STATUS_ALARM,Color.WHITE);
        if(flow>=riverWarn(station))return new RiverStatus("Warnung",STATUS_WARN,Color.rgb(23,34,43));
        return new RiverStatus("Gut",STATUS_GOOD,Color.WHITE);
    }

    private String[] hydroSummary(HydroStation station){
        String raw=prefs.getString(station.liveCacheKey(),"");
        long cache=prefs.getLong(station.liveUpdatedKey(),0L);
        String title=ui("RHEIN")+" · "+station.label+" · "+station.id;
        if(raw.isBlank())return new String[]{title,ui("Wird geladen …"),ui("Abfluss")+" · "+ui("Pegel")+(station.supportsTemperature?" · "+ui("Temperatur"):""),"BAFU Live-Daten"};
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
            double q=values.getOrDefault("Q",Double.NaN),wRaw=values.getOrDefault("W",Double.NaN),wt=values.getOrDefault("WT",Double.NaN);
            double w=displayHydroValue(station,RiverMetric.LEVEL,wRaw);
            String latest="";for(String timestamp:timestamps.values())if(timestamp.compareTo(latest)>0)latest=timestamp;
            String main=Double.isNaN(q)?station.label:String.format(Locale.GERMAN,"%.0f m³/s",q);
            StringBuilder sub=new StringBuilder();
            if(Double.isFinite(w))sub.append(ui("Pegel")).append(' ').append(formatMetric(station,RiverMetric.LEVEL,w)).append(' ').append(metricUnit(station,RiverMetric.LEVEL));
            if(station.supportsTemperature&&!Double.isNaN(wt)){if(sub.length()>0)sub.append("\n");sub.append(ui("Wasser")).append(' ').append(String.format(Locale.GERMAN,"%.1f °C",wt));}
            String stand="BAFU "+station.id;
            try{if(!latest.isBlank())stand+=" · "+ui("Stand")+" "+java.time.Instant.parse(latest).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("HH:mm"));}catch(Exception ignored){}
            if(cache>0&&(System.currentTimeMillis()-cache)>45*60000L)stand+=" · Cache";
            return new String[]{title,main,sub.length()==0?ui("Messwerte derzeit unvollständig"):sub.toString(),stand};
        }catch(Exception ignored){return new String[]{title,ui("Gespeicherter Stand"),ui("Messdaten nicht lesbar"),"BAFU · Cache"};}
    }

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
        if("W".equals(parameter)){
            for(int index=0;index<result.values.size();index++){
                result.values.set(index,graphLevelValue(station,result.values.get(index)));
            }
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
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=body();
        scroll.addView(body);

        TextView intro=txt("Einstellungen nach Bereich",13,MUTED,false);
        intro.setPadding(dp(2),0,0,dp(9));
        body.addView(intro);
        body.addView(settingsTabSelector(),new LinearLayout.LayoutParams(-1,dp(44)));

        View divider=new View(this);
        body.addView(divider,new LinearLayout.LayoutParams(-1,dp(12)));

        switch(settingsTab){
            case RIVER:
                addRiverSettings(body);
                break;
            case PAYMENT:
                addPaymentSettings(body);
                break;
            default:
                addGeneralSettings(body);
                break;
        }
        return scroll;
    }

    private View settingsTabSelector(){
        LinearLayout tabs=segmentedBackground();
        for(SettingsTab tab:SettingsTab.values()){
            TextView option=segmentOption(tab.label,tab==settingsTab);
            option.setContentDescription(ui("Einstellungen")+" "+ui(tab.label));
            option.setOnClickListener(v->{
                settingsTab=tab;
                navigate(Screen.SETTINGS);
            });
            tabs.addView(option,segmentParams(tabs));
        }
        return tabs;
    }

    private void addGeneralSettings(LinearLayout body){
        section(body,"Darstellung","Gilt für die native App-Oberfläche");
        LinearLayout theme=card();
        theme.setOrientation(LinearLayout.VERTICAL);
        body.addView(theme,margin(-1,-2,0,0,0,12));
        theme.addView(txt("Farbschema",16,TEXT,true));
        TextView currentTheme=txt(themeLabel(),13,MUTED,false);
        currentTheme.setPadding(0,dp(4),0,dp(10));
        theme.addView(currentTheme);
        Button chooseTheme=btn("System / Hell / Dunkel",Color.rgb(232,240,244),NAVY);
        chooseTheme.setOnClickListener(v->chooseTheme());
        theme.addView(chooseTheme,new LinearLayout.LayoutParams(-1,dp(44)));

        section(body,"Sprache","App-Texte; externe und originale PFVR-Inhalte bleiben unverändert");
        LinearLayout language=card();
        language.setOrientation(LinearLayout.VERTICAL);
        body.addView(language,margin(-1,-2,0,0,0,12));
        LinearLayout languageOptions=segmentedBackground();
        boolean swissGerman=UiLanguage.isSwissGerman(uiMode());
        TextView german=segmentOption("Deutsch",!swissGerman);
        TextView swiss=segmentOption("Schwiizerdütsch",swissGerman);
        german.setOnClickListener(v->setUiLanguage(UiLanguage.DE));
        swiss.setOnClickListener(v->setUiLanguage(UiLanguage.SWISS_GERMAN));
        languageOptions.addView(german,segmentParams(languageOptions));
        languageOptions.addView(swiss,segmentParams(languageOptions));
        language.addView(languageOptions,new LinearLayout.LayoutParams(-1,dp(44)));

        section(body,"Persönlicher Zugang","Nur lokal auf diesem Gerät gespeichert");
        LinearLayout access=card();
        access.setOrientation(LinearLayout.VERTICAL);
        body.addView(access,margin(-1,-2,0,0,0,12));
        access.addView(txt("Interner PFVR-Link",16,TEXT,true));
        String internal=prefs.getString(PREF_INTERNAL_URL,"");
        TextView status=txt(validInternal(internal)?"intern.pfvr.ch · eingerichtet":"Noch nicht eingerichtet",13,validInternal(internal)?WATER:MUTED,false);
        status.setPadding(0,dp(4),0,dp(10));
        access.addView(status);
        Button edit=btn(validInternal(internal)?"Link ändern":"Link einrichten",NAVY,Color.WHITE);
        edit.setOnClickListener(v->editInternalSetting());
        access.addView(edit,new LinearLayout.LayoutParams(-1,dp(46)));

        section(body,"Ansicht & Kacheln","Home, Kasse und Verein persönlich anordnen");
    LinearLayout layoutCard=card();
    layoutCard.setOrientation(LinearLayout.VERTICAL);
    body.addView(layoutCard,margin(-1,-2,0,0,0,12));
    layoutCard.addView(txt("Kacheln anordnen und ausblenden",16,TEXT,true));
    TextView layoutInfo=txt("Die Auswahl wird nur auf diesem Gerät gespeichert. Neue Kacheln werden bei späteren Updates automatisch ergänzt.",13,MUTED,false);
    layoutInfo.setPadding(0,dp(4),0,dp(10));
    layoutCard.addView(layoutInfo);
    Button layoutButton=btn("Ansicht & Kacheln öffnen",NAVY,Color.WHITE);
    layoutButton.setOnClickListener(v->{tileSettingsArea=TileLayoutStore.Area.HOME;navigate(Screen.TILE_SETTINGS);});
    layoutCard.addView(layoutButton,new LinearLayout.LayoutParams(-1,dp(46)));

        section(body,"Daten","Kalender, Training-Wetter und Rhein-Messwerte");
        LinearLayout data=card();
        data.setOrientation(LinearLayout.VERTICAL);
        body.addView(data,margin(-1,-2,0,0,0,12));
        data.addView(txt("Lokaler Cache",16,TEXT,true));
        TextView description=txt("Beim Start wird zuerst der letzte erfolgreiche Stand angezeigt und anschließend im Hintergrund aktualisiert.",13,MUTED,false);
        description.setPadding(0,dp(4),0,dp(10));
        data.addView(description);
        data.addView(dataFreshnessRow(),margin(-1,-2,0,0,0,10));

        Button reload=btn("Alle Daten aktualisieren",Color.rgb(232,240,244),NAVY);
        reload.setOnClickListener(v->{
            refreshEvents(true,()->{});
            refreshLive(true);
            Toast.makeText(this,ui("Aktualisierung gestartet."),Toast.LENGTH_SHORT).show();
        });
        data.addView(reload,new LinearLayout.LayoutParams(-1,dp(44)));

        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY);
        clear.setOnClickListener(v->clearDataCache());
        LinearLayout.LayoutParams clearParams=new LinearLayout.LayoutParams(-1,dp(44));
        clearParams.setMargins(0,dp(8),0,0);
        data.addView(clear,clearParams);

        boolean backgroundOn=prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
        Button background=btn(ui("Hintergrundaktualisierung")+": "+ui(backgroundOn?"Ein":"Aus"),Color.rgb(232,240,244),NAVY);
        background.setOnClickListener(v->{
            boolean next=!prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
            prefs.edit().putBoolean(PREF_BACKGROUND_REFRESH,next).apply();
            scheduleBackgroundRefresh();
            background.setText(ui("Hintergrundaktualisierung")+": "+ui(next?"Ein":"Aus"));
        });
        LinearLayout.LayoutParams backgroundParams=new LinearLayout.LayoutParams(-1,dp(44));
        backgroundParams.setMargins(0,dp(8),0,0);
        data.addView(background,backgroundParams);

        TextView autoInfo=txt("Live-Daten werden bei geöffneter App regelmäßig geprüft. Im Hintergrund aktualisiert Android bei verfügbarer Verbindung best effort; Energiesparmodi können die Ausführung verzögern.",11,MUTED,false);
        autoInfo.setPadding(0,dp(8),0,0);
        data.addView(autoInfo);

        section(body,"App",null);
        LinearLayout about=card();
        about.setOrientation(LinearLayout.VERTICAL);
        body.addView(about,margin(-1,-2,0,0,0,8));
        about.addView(txt("PFVR Rheinfelden",16,TEXT,true));
        about.addView(txt("Testversion "+BuildConfig.VERSION_NAME+" · "+ui("1.0.0 bleibt für den ersten offiziellen Release reserviert."),13,MUTED,false));
    }

    private View tileSettingsScreen(){
    ScrollView scroll=new ScrollView(this);
    tileSettingsScroll=scroll;
    LinearLayout body=body();
    scroll.addView(body);

    LinearLayout intro=card();
    intro.setOrientation(LinearLayout.VERTICAL);
    body.addView(intro,margin(-1,-2,0,4,0,14));
    intro.addView(txt("Ansicht & Kacheln",23,TEXT,true));
    TextView description=txt("Reihenfolge und Sichtbarkeit gelten nur auf diesem Gerät. Kompakte Kacheln werden automatisch paarweise angeordnet.",13,MUTED,false);
    description.setPadding(0,dp(5),0,dp(11));
    intro.addView(description);
    intro.addView(tileAreaSelector(),new LinearLayout.LayoutParams(-1,dp(44)));

    for(TileLayoutStore.Spec spec:tileLayoutStore.ordered(tileSettingsArea)){
        body.addView(tileSettingsRow(spec),margin(-1,-2,0,0,0,9));
    }

    Button reset=btn(ui(tileSettingsArea.label)+" "+ui("auf Standard zurücksetzen"),Color.rgb(232,240,244),NAVY);
    reset.setOnClickListener(v->{
        tileLayoutStore.reset(tileSettingsArea);
        Toast.makeText(this,ui(tileSettingsArea.label)+" "+ui("Kacheln zurückgesetzt."),Toast.LENGTH_SHORT).show();
        rebuildTileSettingsPreservingScroll();
    });
    body.addView(reset,margin(-1,dp(46),0,6,0,12));
    return scroll;
}

private void rebuildTileSettingsPreservingScroll(){
    final int scrollY=tileSettingsScroll==null?0:tileSettingsScroll.getScrollY();
    navigate(Screen.TILE_SETTINGS);
    final ScrollView target=tileSettingsScroll;
    if(target!=null){
        target.postOnAnimation(()->target.scrollTo(0,scrollY));
        target.postDelayed(()->target.scrollTo(0,scrollY),80);
    }
}

private View tileAreaSelector(){
    LinearLayout tabs=segmentedBackground();
    for(TileLayoutStore.Area area:TileLayoutStore.Area.values()){
        TextView option=segmentOption(area.label,area==tileSettingsArea);
        option.setOnClickListener(v->{tileSettingsArea=area;navigate(Screen.TILE_SETTINGS);});
        tabs.addView(option,segmentParams(tabs));
    }
    return tabs;
}

private View tileSettingsRow(TileLayoutStore.Spec spec){
    boolean visible=tileLayoutStore.isVisible(spec);
    LinearLayout card=card();
    card.setOrientation(LinearLayout.VERTICAL);

    LinearLayout titleRow=new LinearLayout(this);
    titleRow.setGravity(Gravity.CENTER_VERTICAL);
    LinearLayout copy=new LinearLayout(this);
    copy.setOrientation(LinearLayout.VERTICAL);
    copy.addView(txt(spec.label,15,TEXT,true));
    copy.addView(txt(ui(spec.width.label)+(spec.pinned?" · "+ui("fixiert"):""),11,MUTED,false));
    titleRow.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
    int stateColor=visible?STATUS_GOOD:MUTED;
    TextView state=txt(spec.pinned?"Immer an":(visible?"Sichtbar":"Aus"),10,stateColor,true);
    state.setTextColor(visible?statusTextColor(STATUS_GOOD):themeText(MUTED));
    state.setGravity(Gravity.CENTER);
    state.setPadding(dp(9),dp(4),dp(9),dp(4));
    state.setBackground(visible?statusBadge(STATUS_GOOD):round(Color.rgb(238,243,246),12));
    titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
    card.addView(titleRow);

    LinearLayout actions=new LinearLayout(this);
    actions.setPadding(0,dp(11),0,0);
    boolean canUp=tileLayoutStore.canMove(spec.area,spec.id,-1);
    boolean canDown=tileLayoutStore.canMove(spec.area,spec.id,1);
    Button up=btn("↑",Color.rgb(232,240,244),NAVY);
    up.setContentDescription(ui(spec.label)+" "+ui("nach oben"));
    up.setEnabled(canUp);up.setAlpha(canUp?1f:0.35f);
    up.setOnClickListener(v->{tileLayoutStore.move(spec.area,spec.id,-1);rebuildTileSettingsPreservingScroll();});
    actions.addView(up,new LinearLayout.LayoutParams(0,dp(42),0.8f));
    Button down=btn("↓",Color.rgb(232,240,244),NAVY);
    down.setContentDescription(ui(spec.label)+" "+ui("nach unten"));
    down.setEnabled(canDown);down.setAlpha(canDown?1f:0.35f);
    down.setOnClickListener(v->{tileLayoutStore.move(spec.area,spec.id,1);rebuildTileSettingsPreservingScroll();});
    LinearLayout.LayoutParams downParams=new LinearLayout.LayoutParams(0,dp(42),0.8f);
    downParams.setMargins(dp(7),0,0,0);
    actions.addView(down,downParams);
    Button toggle=btn(spec.pinned?"Fixiert":(visible?"Ausblenden":"Einblenden"),spec.pinned?Color.rgb(232,240,244):(visible?Color.rgb(232,240,244):NAVY),spec.pinned||visible?NAVY:Color.WHITE);
    toggle.setEnabled(!spec.pinned);toggle.setAlpha(spec.pinned?0.55f:1f);
    toggle.setOnClickListener(v->{tileLayoutStore.setVisible(spec,!visible);rebuildTileSettingsPreservingScroll();});
    LinearLayout.LayoutParams toggleParams=new LinearLayout.LayoutParams(0,dp(42),1.8f);
    toggleParams.setMargins(dp(7),0,0,0);
    actions.addView(toggle,toggleParams);
    card.addView(actions);
    return card;
}

    private void addRiverSettings(LinearLayout body){
        section(body,"Rhein-Anzeige","Kachel 1 ist immer sichtbar; Kachel 2 kann ein- oder ausgeblendet werden.");
        body.addView(riverSlotSettingCard(1),margin(-1,-2,0,0,0,9));
        body.addView(riverSlotSettingCard(2),margin(-1,-2,0,0,0,12));

        section(body,"Schifffahrtslage","Offizielle Hochwassermarken des Pegels Basel-Rheinhalle; der Abfluss bleibt ein separater Messwert.");
        body.addView(riverNavigationSettingsCard(),margin(-1,-2,0,0,0,12));
    }

    private void addPaymentSettings(LinearLayout body){
        section(body,"Banking-App","Für die direkte Übergabe des Swiss QR");
        body.addView(bankChoiceSettingsCard(),margin(-1,-2,0,0,0,12));

        LinearLayout info=card();
        info.setOrientation(LinearLayout.VERTICAL);
        body.addView(info,margin(-1,-2,0,0,0,8));
        info.addView(txt("So funktioniert die Direktzahlung",16,TEXT,true));
        TextView explanation=txt("Je nach Banking-App nutzt PFVR direkte QR-Übergabe, Dateiimport oder einen sicheren manuellen Fallback. Die App prüft zusätzlich zur bekannten Bank-Matrix die tatsächlich auf diesem Gerät angebotenen Android-Schnittstellen.",13,MUTED,false);
        explanation.setPadding(0,dp(5),0,0);
        info.addView(explanation);
    }

    private void openPaymentSettings(){
        settingsTab=SettingsTab.PAYMENT;
        navigate(Screen.SETTINGS);
    }

    private View riverNavigationSettingsCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);
        card.addView(txt("Pegel Basel-Rheinhalle · offizielle Schifffahrtslage",15,TEXT,true));
        TextView intro=txt("Ausschlaggebend für die Hochwassermarken ist der Pegel Basel-Rheinhalle. Abflusswerte werden nur ergänzend angezeigt.",12,MUTED,false);
        intro.setPadding(0,dp(5),0,dp(8));
        card.addView(intro);
        card.addView(navigationRuleRow("700 cm","HWM I · Voralarm","Hochwassermarke I",navigationLevelColor(RhineNavigation.Stage.HWM_I)));
        card.addView(navigationRuleRow("790 cm","HWM IIb · Sperre","Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt",navigationLevelColor(RhineNavigation.Stage.HWM_IIB)));
        card.addView(navigationRuleRow("820 cm","HWM IIa · Sperre","Schifffahrt Rheinfelden–Kembs gesperrt",navigationLevelColor(RhineNavigation.Stage.HWM_IIA)));
        TextView source=txt("Quelle: Schweizerische Rheinhäfen  ↗",10,WATER,true);
        source.setPadding(0,dp(9),0,0);
        source.setOnClickListener(v->external(RIVER_NAVIGATION_SOURCE));
        card.addView(source);
        return card;
    }

    private View navigationRuleRow(String level,String labelText,String detail,int color){
        LinearLayout row=new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0,dp(5),0,dp(5));
        View dot=new View(this);
        dot.setBackground(statusDot(color));
        row.addView(dot,new LinearLayout.LayoutParams(dp(9),dp(9)));
        LinearLayout copy=new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(8),0,0,0);
        TextView title=txtRaw(level+" · "+ui(labelText),12,color,true);
        title.setTextColor(color);
        copy.addView(title);
        copy.addView(txt(detail,10,MUTED,false));
        row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        return row;
    }

    private View riverSlotSettingCard(int slot){
        HydroStation station=riverStation(slot);
        boolean enabled=riverSlotEnabled(slot);
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(txtRaw(ui("Rhein-Kachel")+" "+slot,15,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));
        int stateColor=enabled?STATUS_GOOD:MUTED;
        TextView state=txt(enabled?"Aktiv":"Aus",11,stateColor,true);
        state.setTextColor(enabled?statusTextColor(STATUS_GOOD):themeText(MUTED));
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(10),dp(5),dp(10),dp(5));
        state.setBackground(enabled?statusBadge(STATUS_GOOD):round(Color.rgb(238,243,246),12));
        titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        String metrics=station.supportsTemperature?"Abfluss · Pegel · Wassertemperatur":"Abfluss · Pegel";
        TextView current=txtRaw(station.label+" · BAFU "+station.id+"\n"+ui(metrics),13,MUTED,false);
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
                ui("Basel, Rheinhalle · Abfluss & Pegel"),
                ui("Rheinfelden · Abfluss, Pegel & Wassertemperatur")
        };
        HydroStation currentStation=riverStation(slot);
        int selected=currentStation==HydroStation.RHEINFELDEN?1:0;
        new AlertDialog.Builder(this,dialogTheme()).setTitle(ui("Rhein-Kachel")+" "+slot)
                .setSingleChoiceItems(labels,selected,(dialog,which)->{
                    String stationKey=slot==1?PREF_RIVER_SLOT1_STATION:PREF_RIVER_SLOT2_STATION;
                    prefs.edit().putString(stationKey,stations[which].id).apply();
                    dialog.dismiss();
                    navigate(Screen.SETTINGS);
                }).setNegativeButton(ui("Abbrechen"),null).show();
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
        addFreshnessTile(first,"News",prefs.getLong(NewsRepository.PREF_UPDATED,0L),2L*60L*60L*1000L);
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
        if(updated<=0)return ui("kein Stand");
        long minutes=Math.max(0,(System.currentTimeMillis()-updated)/60000L);
        if(minutes<1)return ui("gerade eben");
        if(minutes<60)return minutes+" min";
        long hours=minutes/60;
        if(hours<24)return hours+" h";
        return (hours/24)+" d";
    }

    private boolean resolveDarkMode(){
        String mode=prefs==null?"system":prefs.getString(PREF_THEME,"system"); if("dark".equals(mode))return true; if("light".equals(mode))return false;
        int night=getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK; return night==Configuration.UI_MODE_NIGHT_YES;
    }
    private String themeLabel(){String mode=prefs.getString(PREF_THEME,"system");if("dark".equals(mode))return ui("Dunkel");if("light".equals(mode))return ui("Hell");return ui("System · folgt Android");}
    private void chooseTheme(){
        String[] labels={ui("System · folgt Android"),ui("Hell"),ui("Dunkel")}; String mode=prefs.getString(PREF_THEME,"system"); int selected="light".equals(mode)?1:("dark".equals(mode)?2:0);
        new AlertDialog.Builder(this,dialogTheme()).setTitle(ui("Farbschema")).setSingleChoiceItems(labels,selected,(d,which)->{String value=which==1?"light":(which==2?"dark":"system");prefs.edit().putString(PREF_THEME,value).apply();d.dismiss();recreate();}).setNegativeButton(ui("Abbrechen"),null).show();
    }
    private int dialogTheme(){return darkMode?android.R.style.Theme_Material_Dialog_Alert:android.R.style.Theme_Material_Light_Dialog_Alert;}
    private void applyWindowTheme(){
        getWindow().setStatusBarColor(NAVY); getWindow().setNavigationBarColor(darkMode?DARK_SURFACE:Color.WHITE);
        int flags=getWindow().getDecorView().getSystemUiVisibility(); if(darkMode)flags&=~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;else flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR; getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    private int themeText(int c){if(!darkMode)return c;if(c==TEXT)return DARK_TEXT;if(c==MUTED||c==Color.rgb(65,82,96)||c==Color.rgb(126,140,150))return DARK_MUTED;if(c==NAVY)return Color.rgb(105,193,218);if(c==WATER)return Color.rgb(91,190,213);return c;}
    private int themeBg(int c){if(!darkMode)return c;if(c==SURFACE)return DARK_SURFACE;if(c==Color.WHITE)return DARK_CARD;if(c==Color.rgb(232,240,244)||c==Color.rgb(238,243,246)||c==Color.rgb(231,242,246)||c==Color.rgb(236,243,247))return DARK_SOFT;return c;}
    private void clearDataCache(){
        SharedPreferences.Editor editor=prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(NewsRepository.PREF_CACHE).remove(NewsRepository.PREF_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED);
        for(HydroStation station:HydroStation.values())editor.remove(station.liveCacheKey()).remove(station.liveUpdatedKey()).remove(station.fineCacheKey()).remove(station.fineUpdatedKey()).remove(station.historyCacheKey()).remove(station.historyUpdatedKey());
        editor.apply();
        events=new ArrayList<>();eventsUpdated=0L;news=new ArrayList<>();newsUpdated=0L;
        Toast.makeText(this,ui("Daten-Cache geleert. Neue Daten werden nachgeladen."),Toast.LENGTH_SHORT).show();
        refreshEvents(false,()->{});refreshNews(false);refreshLive(true);
    }

    private void editRiverThresholds(HydroStation station){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(4),dp(16),0);
        EditText low=thresholdInput("Niedrig unter m³/s",riverLow(station)),warn=thresholdInput("Warnung ab m³/s",riverWarn(station)),alarm=thresholdInput("Alarm ab m³/s",riverAlarm(station));
        box.addView(txt("Niedrig",12,MUTED,true));box.addView(low,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView w1=txt("Warnung",12,MUTED,true);w1.setPadding(0,dp(10),0,0);box.addView(w1);box.addView(warn,new LinearLayout.LayoutParams(-1,dp(48)));
        TextView a1=txt("Alarm",12,MUTED,true);a1.setPadding(0,dp(10),0,0);box.addView(a1);box.addView(alarm,new LinearLayout.LayoutParams(-1,dp(48)));
        new AlertDialog.Builder(this,dialogTheme()).setTitle(station.label+" · "+ui("Grenzwerte")).setView(box).setPositiveButton(ui("Speichern"),(d,w)->{
            try{
                float l=Float.parseFloat(low.getText().toString().replace(',','.')),wa=Float.parseFloat(warn.getText().toString().replace(',','.')),al=Float.parseFloat(alarm.getText().toString().replace(',','.'));
                if(l<0||!(l<wa&&wa<al))throw new Exception();
                prefs.edit().putFloat(station.lowPreferenceKey(),l).putFloat(station.warnPreferenceKey(),wa).putFloat(station.alarmPreferenceKey(),al).apply();
                navigate(Screen.SETTINGS);
            }catch(Exception e){Toast.makeText(this,ui("Grenzwerte müssen aufsteigend sein: Niedrig < Warnung < Alarm."),Toast.LENGTH_LONG).show();}
        }).setNegativeButton(ui("Abbrechen"),null).show();
    }
    private EditText thresholdInput(String hint,float value){EditText e=new EditText(this);e.setHint(ui(hint));e.setText(String.format(Locale.US,"%.0f",value));e.setSingleLine(true);e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);e.setTextColor(themeText(TEXT));e.setHintTextColor(themeText(MUTED));e.setBackground(round(Color.rgb(238,243,246),12));e.setPadding(dp(12),0,dp(12),0);return e;}

    private void loadCachedNews(){
        newsUpdated=prefs.getLong(NewsRepository.PREF_UPDATED,0L);
        try{news=NewsRepository.parse(prefs.getString(NewsRepository.PREF_CACHE,""));}
        catch(Exception ignored){news=new ArrayList<>();newsUpdated=0L;prefs.edit().remove(NewsRepository.PREF_CACHE).remove(NewsRepository.PREF_UPDATED).apply();}
    }

    private String newsStatus(){
        if(newsUpdated<=0L)return ui(newsLoading?"Abruf läuft":"noch kein Stand");
        ZonedDateTime time=java.time.Instant.ofEpochMilli(newsUpdated).atZone(ZoneId.of("Europe/Zurich"));
        if(time.toLocalDate().equals(LocalDate.now(ZoneId.of("Europe/Zurich"))))return ui("Stand")+" "+time.format(DateTimeFormatter.ofPattern("HH:mm"));
        return ui("Stand")+" "+time.format(DateTimeFormatter.ofPattern("dd.MM. HH:mm"));
    }

    private String newsDate(NewsRepository.Article article){
        if(article.publishedAt<=0L)return "PFVR";
        return java.time.Instant.ofEpochMilli(article.publishedAt).atZone(ZoneId.of("Europe/Zurich")).format(DateTimeFormatter.ofPattern("dd.MM.yyyy",Locale.GERMAN));
    }

    private View newsCard(NewsRepository.Article article,boolean compact){
        LinearLayout c=card();c.setOrientation(LinearLayout.VERTICAL);c.setLayoutParams(margin(-1,-2,0,0,0,9));
        c.addView(txt(newsDate(article),11,WATER,true));
        TextView title=txtRaw(article.title,compact?15:17,TEXT,true);title.setPadding(0,dp(4),0,dp(4));c.addView(title);
        if(article.excerpt!=null&&!article.excerpt.isBlank()){
            TextView excerpt=txtRaw(article.excerpt,compact?12:13,MUTED,false);excerpt.setMaxLines(compact?3:6);c.addView(excerpt);
        }
        TextView open=txt("Artikel öffnen  →",12,WATER,true);open.setGravity(Gravity.END);open.setPadding(0,dp(8),0,0);c.addView(open);
        c.setOnClickListener(v->openNewsArticle(article));
        return c;
    }

    private void openNewsArticle(NewsRepository.Article article){navigate(Screen.NEWS);openInApp(article.link,"Vereinsnews");}

    private View newsScreen(){
        ScrollView scroll=new ScrollView(this);LinearLayout body=body();scroll.addView(body);
        LinearLayout intro=card();intro.setOrientation(LinearLayout.VERTICAL);body.addView(intro,margin(-1,-2,0,4,0,16));
        intro.addView(txt("Vereinsnews",23,TEXT,true));
        TextView source=txt("Direkt aus dem öffentlichen PFVR-WordPress-Feed · lokal gespeichert.",13,MUTED,false);source.setPadding(0,dp(5),0,dp(3));intro.addView(source);
        intro.addView(txt(newsStatus(),11,newsUpdated>0?WATER:MUTED,false));
        Button reload=btn(newsLoading?"Aktualisierung läuft …":"News aktualisieren",NAVY,Color.WHITE);reload.setEnabled(!newsLoading);reload.setOnClickListener(v->refreshNews(true));
        LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(44));rp.setMargins(0,dp(11),0,0);intro.addView(reload,rp);
        if(news.isEmpty()){
            TextView empty=txt(newsLoading?"News werden geladen …":"Noch keine Vereinsnews im lokalen Cache.",13,MUTED,false);empty.setGravity(Gravity.CENTER);empty.setPadding(0,dp(18),0,0);body.addView(empty);
        }else for(NewsRepository.Article article:news)body.addView(newsCard(article,false));
        TextView archive=link("Original-Newsarchiv auf pfvr.ch  ↗");archive.setOnClickListener(v->external(NEWS));body.addView(archive);
        return scroll;
    }

    private void refreshNews(boolean toast){
        long age=System.currentTimeMillis()-newsUpdated;
        if(newsLoading)return;
        if(!toast&&newsUpdated>0L&&!news.isEmpty()&&age<60L*60L*1000L)return;
        newsLoading=true;
        new Thread(()->{
            try{
                String raw=NewsRepository.fetchRaw();List<NewsRepository.Article> parsed=NewsRepository.parse(raw);if(parsed.isEmpty())throw new Exception("Keine News");
                long updated=System.currentTimeMillis();prefs.edit().putString(NewsRepository.PREF_CACHE,raw).putLong(NewsRepository.PREF_UPDATED,updated).apply();
                runOnUiThread(()->{news=parsed;newsUpdated=updated;newsLoading=false;if(toast)Toast.makeText(this,parsed.size()+" "+ui("News aktualisiert"),Toast.LENGTH_SHORT).show();if(current==Screen.HOME)rebuildHomePreservingScroll();else if(current==Screen.NEWS)navigate(Screen.NEWS);});
            }catch(Exception ignored){runOnUiThread(()->{newsLoading=false;if(toast)Toast.makeText(this,ui(news.isEmpty()?"News konnten gerade nicht geladen werden.":"Keine Verbindung – gespeicherte News bleiben sichtbar."),Toast.LENGTH_LONG).show();if(current==Screen.NEWS)navigate(Screen.NEWS);});}
        }).start();
    }

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
        TextView title=txtRaw(event.title,compact?15:16,TEXT,true);
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
        details.addView(txtRaw(eventWhenCompact(event),13,MUTED,false));
        if(!compact&&event.location!=null&&!event.location.isBlank()){
            TextView location=txtRaw(event.location.replace("\n"," · "),12,WATER,false);
            location.setMaxLines(2);
            details.addView(location);
        }

        TextView arrow=txt("›",28,cancelled?MUTED:WATER,false);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow,new LinearLayout.LayoutParams(dp(24),dp(52)));
        return card;
    }

    private String eventWhenCompact(Event event){
        String text=localizedDateWords(cap(event.start.format(DateTimeFormatter.ofPattern("EEEE",Locale.GERMAN))));
        if(!event.allDay){
            text+=" · "+event.start.format(DateTimeFormatter.ofPattern("HH:mm"));
            if(event.end!=null&&event.end.toLocalDate().equals(event.start.toLocalDate()))text+="–"+event.end.format(DateTimeFormatter.ofPattern("HH:mm"));
            text+=" Uhr";
        }else text+=" · "+ui("ganztägig");
        return text;
    }

    private String eventWhen(Event event){
        DateTimeFormatter date=DateTimeFormatter.ofPattern("EEEE, dd. MMMM yyyy",Locale.GERMAN);
        DateTimeFormatter time=DateTimeFormatter.ofPattern("HH:mm");
        String start=localizedDateWords(cap(event.start.format(date)));
        if(event.allDay)return start+" · "+ui("ganztägig");
        if(event.end==null)return start+" · "+event.start.format(time)+" Uhr";
        if(event.end.toLocalDate().equals(event.start.toLocalDate()))return start+" · "+event.start.format(time)+"–"+event.end.format(time)+" Uhr";
        return start+" · "+event.start.format(time)+" Uhr\n"+localizedDateWords(cap(event.end.format(date)))+" · "+event.end.format(time)+" Uhr";
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
        box.addView(txtRaw(event.title,22,TEXT,true));
        TextView when=txtRaw(eventWhen(event),14,MUTED,false);
        when.setPadding(0,dp(7),0,0);
        box.addView(when);

        if(event.location!=null&&!event.location.isBlank()){
            TextView location=txtRaw(ui("Ort")+"\n"+event.location,13,WATER,false);
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
            TextView description=txtRaw(event.description.trim(),14,TEXT,false);
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

        new AlertDialog.Builder(this,dialogTheme()).setView(scroll).setNegativeButton(ui("Schliessen"),null).show();
    }

    private void shareEvent(Event event){
        StringBuilder body=new StringBuilder(event.title).append("\n").append(eventWhen(event));
        if(event.location!=null&&!event.location.isBlank())body.append("\n").append(event.location);
        Intent share=new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT,event.title);
        share.putExtra(Intent.EXTRA_TEXT,body.toString());
        try{startActivity(Intent.createChooser(share,ui("Termin teilen")));}catch(Exception ignored){Toast.makeText(this,ui("Teilen ist auf diesem Gerät nicht verfügbar."),Toast.LENGTH_SHORT).show();}
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
        try{startActivity(insert);}catch(Exception ignored){Toast.makeText(this,ui("Keine Kalender-App gefunden."),Toast.LENGTH_SHORT).show();}
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
    cashFreeAmountInput=null;
    scroll.addView(body);

    LinearLayout hero=new LinearLayout(this);
    hero.setOrientation(LinearLayout.VERTICAL);
    hero.setPadding(dp(20),dp(18),dp(20),dp(18));
    GradientDrawable gradient=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{NAVY,Color.rgb(21,90,122),WATER});
    gradient.setCornerRadius(dp(22));
    hero.setBackground(gradient);
    body.addView(hero,margin(-1,-2,0,4,0,14));
    hero.addView(txt("VEREINSBEIZ",12,Color.rgb(208,231,239),true));
    TextView title=txt("Konsumation bezahlen",27,Color.WHITE,true);
    title.setPadding(0,dp(5),0,dp(5));
    hero.addView(title);
    hero.addView(txt("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.",14,Color.rgb(232,243,247),false));
    if(!hasPreferredBank()){
        section(body,"Zahlungsweg","Für Direktzahlungen einmalig eine Banking-App festlegen");
        body.addView(cashBankStatusCard(),margin(-1,-2,0,0,0,12));
    }

    LinearLayout tiles=new LinearLayout(this);
    tiles.setOrientation(LinearLayout.VERTICAL);
    body.addView(tiles,new LinearLayout.LayoutParams(-1,-2));
    addConfiguredTiles(tiles,TileLayoutStore.Area.CASH,this::cashTileView);
    return scroll;
}

private View cashTileView(TileLayoutStore.Spec spec){
    switch(spec.id){
        case "cash_cart":return cashCartTile();
        case "cash_drinks":return cashCategoryTile("drinks");
        case "cash_food":return cashCategoryTile("food");
        case "cash_celebrations":return cashCategoryTile("celebrations");
        case "cash_free_amount":return cashFreeAmountTile();
        case "cash_twint":return cashTwintTile();
        case "cash_payment_details":return cashPaymentDetailsTile();
        default:return null;
    }
}

private View cashCartTile(){
    LinearLayout group=tileGroup("Warenkorb","Ausgewählte Artikel und Zahlungswege");
    LinearLayout cart=card();
    cart.setOrientation(LinearLayout.VERTICAL);
    group.addView(cart,new LinearLayout.LayoutParams(-1,-2));
    cashSummaryContainer=new LinearLayout(this);
    cashSummaryContainer.setOrientation(LinearLayout.VERTICAL);
    cart.addView(cashSummaryContainer);
    cashTotalView=txt("Total CHF 0.00",24,TEXT,true);
    cashTotalView.setPadding(0,dp(12),0,dp(10));
    cart.addView(cashTotalView);
    Button cartBank=btn(preferredBankPaymentLabel(),NAVY,Color.WHITE);
    cartBank.setOnClickListener(v->{
        if(!hasPreferredBank()){openPaymentSettings();return;}
        EditText input=cartAmountInput();
        if(input!=null)payWithPreferredBank(input);
    });
    cart.addView(cartBank,new LinearLayout.LayoutParams(-1,dp(48)));
    LinearLayout payRow=new LinearLayout(this);
    payRow.setPadding(0,dp(8),0,0);
    Button cartQr=btn("Swiss QR",Color.rgb(232,240,244),NAVY);
    cartQr.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)showPaymentQr(input);});
    payRow.addView(cartQr,new LinearLayout.LayoutParams(0,dp(44),1));
    Button cartTwint=btn("TWINT",Color.rgb(232,240,244),NAVY);
    cartTwint.setOnClickListener(v->{EditText input=cartAmountInput();if(input!=null)openTwintDirect(input);});
    LinearLayout.LayoutParams twintParams=new LinearLayout.LayoutParams(0,dp(44),1);
    twintParams.setMargins(dp(7),0,0,0);
    payRow.addView(cartTwint,twintParams);
    cart.addView(payRow);
    TextView directInfo=txt(selectedBankPaymentHint(),11,MUTED,false);
    directInfo.setPadding(0,dp(8),0,0);
    cart.addView(directInfo);
    TextView clearCart=link("Warenkorb leeren");
    clearCart.setOnClickListener(v->{cashCart.clear();for(TextView quantity:cashQuantityViews.values())quantity.setText("0");updateCashSummary();});
    cart.addView(clearCart);
    updateCashSummary();
    return group;
}

private View cashCategoryTile(String categoryId){
    CashCatalog.Catalog catalog=cashCatalog();
    if(catalog==null){
        LinearLayout group=tileGroup("Auswahl",ui("Preisliste konnte nicht geladen werden."));
        LinearLayout unavailable=card();
        unavailable.addView(txt("Die lokale Preisliste ist derzeit nicht verfügbar.",13,MUTED,false));
        group.addView(unavailable);
        return group;
    }
    CashCatalog.Category category=findCashCategory(catalog,categoryId);
    if(category==null)return null;
    LinearLayout group=tileGroup(category.label,ui("Preisliste Vereinsbeiz · Stand")+" "+catalog.validFrom);
    LinearLayout categoryCard=card();
    categoryCard.setOrientation(LinearLayout.VERTICAL);
    for(int index=0;index<category.items.size();index++){
        if(index>0){
            View divider=new View(this);
            divider.setBackgroundColor(darkMode?Color.rgb(51,65,74):Color.rgb(226,233,237));
            categoryCard.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));
        }
        categoryCard.addView(cashItemRow(category.items.get(index)));
    }
    group.addView(categoryCard,new LinearLayout.LayoutParams(-1,-2));
    return group;
}

private CashCatalog.Category findCashCategory(CashCatalog.Catalog catalog,String categoryId){
    if(catalog==null)return null;
    for(CashCatalog.Category category:catalog.categories)if(category.id.equals(categoryId))return category;
    return null;
}

private View cashFreeAmountTile(){
    LinearLayout group=tileGroup("Freier Betrag","Für Sonderfälle oder Beträge ausserhalb der Preisliste");
    LinearLayout amountCard=card();
    amountCard.setOrientation(LinearLayout.VERTICAL);
    group.addView(amountCard,new LinearLayout.LayoutParams(-1,-2));
    LinearLayout amountRow=new LinearLayout(this);
    amountRow.setGravity(Gravity.CENTER_VERTICAL);
    amountRow.setPadding(0,0,0,dp(8));
    amountCard.addView(amountRow);
    TextView chf=txt("CHF",20,NAVY,true);
    chf.setGravity(Gravity.CENTER_VERTICAL);
    amountRow.addView(chf,new LinearLayout.LayoutParams(dp(55),dp(56)));
    EditText amount=new EditText(this);
    cashFreeAmountInput=amount;
    amount.setHint("0.00");
    amount.setTextSize(25);
    amount.setSingleLine(true);
    amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
    amount.setTextColor(themeText(TEXT));
    amount.setHintTextColor(themeText(MUTED));
    amount.setBackground(round(Color.rgb(238,243,246),14));
    amount.setPadding(dp(14),0,dp(14),0);
    amountRow.addView(amount,new LinearLayout.LayoutParams(0,dp(56),1));
    TextView amountInfo=txt("Leer oder 0 erzeugt einen Swiss QR mit offenem Betrag.",12,MUTED,false);
    amountInfo.setPadding(0,0,0,dp(10));
    amountCard.addView(amountInfo);
    Button qr=btn("Swiss QR erstellen",NAVY,Color.WHITE);
    qr.setOnClickListener(v->showPaymentQr(amount));
    amountCard.addView(qr,new LinearLayout.LayoutParams(-1,dp(48)));
    Button direct=btn(preferredBankPaymentLabel(),Color.rgb(232,240,244),NAVY);
    direct.setOnClickListener(v->payWithPreferredBank(amount));
    LinearLayout.LayoutParams directParams=new LinearLayout.LayoutParams(-1,dp(44));
    directParams.setMargins(0,dp(8),0,0);
    amountCard.addView(direct,directParams);
    return group;
}

private EditText cashOptionalAmountInput(){
    if(cashFreeAmountInput!=null)return cashFreeAmountInput;
    EditText empty=new EditText(this);
    empty.setText("");
    return empty;
}

private View cashTwintTile(){
    LinearLayout group=tileGroup("TWINT","Code oder Vereins-QR verwenden");
    LinearLayout twint=card();
    twint.setOrientation(LinearLayout.VERTICAL);
    group.addView(twint,new LinearLayout.LayoutParams(-1,-2));
    TextView info=txt("Für Zahlung auf demselben Handy: Betrag übernehmen und auf der PFVR-Seite den fünfstelligen TWINT-Code erzeugen. Der Vereins-QR bleibt zusätzlich verfügbar.",13,MUTED,false);
    info.setPadding(0,0,0,dp(10));
    twint.addView(info);
    Button direct=btn("TWINT-Code erzeugen",NAVY,Color.WHITE);
    direct.setOnClickListener(v->openTwintDirect(cashOptionalAmountInput()));
    twint.addView(direct,new LinearLayout.LayoutParams(-1,dp(48)));
    Button qr=btn("Vereins-TWINT-QR öffnen",Color.rgb(232,240,244),NAVY);
    qr.setOnClickListener(v->external(TWINT_QR_PDF));
    LinearLayout.LayoutParams qrParams=new LinearLayout.LayoutParams(-1,dp(44));
    qrParams.setMargins(0,dp(8),0,0);
    twint.addView(qr,qrParams);
    return group;
}

private View cashPaymentDetailsTile(){
    LinearLayout group=tileGroup("Zahlungsdaten","Für E-Banking und manuelle Überweisung");
    LinearLayout details=card();
    details.setOrientation(LinearLayout.VERTICAL);
    group.addView(details,new LinearLayout.LayoutParams(-1,-2));
    details.addView(txtRaw(CLUB_PAYEE,16,TEXT,true));
    details.addView(txtRaw("Rheinweg · 4310 Rheinfelden",13,MUTED,false));
    TextView iban=txtRaw(CLUB_IBAN,19,NAVY,true);
    iban.setPadding(0,dp(12),0,dp(4));
    details.addView(iban);
    details.addView(txtRaw(CLUB_PAYMENT_NOTE,13,MUTED,false));
    LinearLayout copies=new LinearLayout(this);
    copies.setPadding(0,dp(12),0,0);
    details.addView(copies);
    Button copyIban=btn("IBAN kopieren",Color.rgb(232,240,244),NAVY);
    copyIban.setOnClickListener(v->copy("PFVR IBAN",CLUB_IBAN.replace(" ",""),"IBAN kopiert"));
    copies.addView(copyIban,new LinearLayout.LayoutParams(0,dp(42),1));
    Button copyAll=btn("Alles kopieren",Color.rgb(232,240,244),NAVY);
    copyAll.setOnClickListener(v->{
        String value=CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE;
        EditText amountInput=cashFreeAmountInput;
        String amount=amount(amountInput==null?"":amountInput.getText().toString());
        if(amount!=null&&!amount.isBlank())value+="\nCHF "+amount;
        copy("PFVR Zahlung",value,"Zahlungsdaten kopiert");
    });
    LinearLayout.LayoutParams allParams=new LinearLayout.LayoutParams(0,dp(42),1);
    allParams.setMargins(dp(8),0,0,0);
    copies.addView(copyAll,allParams);
    return group;
}

    private View cashBankStatusCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        boolean selected=hasPreferredBank();
        String label=selectedBankLabel();

        LinearLayout titleRow=new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        titleRow.addView(selected?txtRaw(label,16,TEXT,true):txt("Keine Banking-App gewählt",16,TEXT,true),new LinearLayout.LayoutParams(0,-2,1));

        int stateColor=selected?STATUS_GOOD:STATUS_WARN;
        TextView state=txt(selected?"Bereit":"Einrichten",10,stateColor,true);
        state.setTextColor(statusTextColor(stateColor));
        state.setGravity(Gravity.CENTER);
        state.setPadding(dp(9),dp(4),dp(9),dp(4));
        state.setBackground(statusBadge(stateColor));
        titleRow.addView(state,new LinearLayout.LayoutParams(-2,-2));
        card.addView(titleRow);

        TextView info=txt(
                selected?selectedBankPaymentHint():"Für Direktzahlungen zuerst unter Einstellungen → Zahlung eine Banking-App festlegen.",
                12,
                selected?MUTED:statusTextColor(STATUS_WARN),
                false
        );
        info.setTextColor(selected?themeText(MUTED):statusTextColor(STATUS_WARN));
        info.setPadding(0,dp(5),0,dp(10));
        card.addView(info);

        Button manage=btn(selected?"In Einstellungen ändern":"Banking-App auswählen",selected?Color.rgb(232,240,244):NAVY,selected?NAVY:Color.WHITE);
        manage.setOnClickListener(v->openPaymentSettings());
        card.addView(manage,new LinearLayout.LayoutParams(-1,dp(44)));
        return card;
    }

    private View bankChoiceSettingsCard(){
        LinearLayout card=card();
        card.setOrientation(LinearLayout.VERTICAL);

        boolean selected=hasPreferredBank();
        String label=selectedBankLabel();
        card.addView(selected?txtRaw(label,16,TEXT,true):txt("Noch keine Banking-App festgelegt",16,TEXT,true));

        if(selected){
            BankingAppRegistry.Profile profile=selectedBankProfile();
            TextView capability=txtRaw(ui(profile.capability.label)+(profile.documented?" · "+ui("dokumentiert"):" · "+ui("Geräteprüfung")),12,WATER,true);
            capability.setPadding(0,dp(4),0,dp(4));
            card.addView(capability);
        }

        TextView info=txt(
                selected
                        ?selectedBankPaymentHint()
                        :"Gängige Schweizer Banking-Apps sowie Neon, Revolut und VR Banking werden gezielt erkannt. Zusätzlich werden installierte Apps dynamisch auf QR-Bildübergabe geprüft; „Alle Apps“ bleibt als Fallback verfügbar.",
                12,
                MUTED,
                false
        );
        info.setPadding(0,dp(4),0,dp(10));
        card.addView(info);

        LinearLayout actions=new LinearLayout(this);
        Button choose=btn(selected?"Ändern":"Bank auswählen",selected?Color.rgb(232,240,244):NAVY,selected?NAVY:Color.WHITE);
        choose.setOnClickListener(v->chooseBankingApp());
        actions.addView(choose,new LinearLayout.LayoutParams(0,dp(44),1));

        if(selected){
            Button clear=btn("Entfernen",Color.rgb(232,240,244),NAVY);
            clear.setOnClickListener(v->{
                prefs.edit().remove(PREF_BANK_PACKAGE).remove(PREF_BANK_LABEL).apply();
                navigate(Screen.SETTINGS);
            });
            LinearLayout.LayoutParams clearParams=new LinearLayout.LayoutParams(0,dp(44),1);
            clearParams.setMargins(dp(8),0,0,0);
            actions.addView(clear,clearParams);
        }
        card.addView(actions);
        return card;
    }

    private boolean hasPreferredBank(){
        String packageName=prefs.getString(PREF_BANK_PACKAGE,"").trim();
        String label=prefs.getString(PREF_BANK_LABEL,"").trim();
        if(packageName.isEmpty()||label.isEmpty())return false;
        PackageManager packageManager=getPackageManager();
        if(packageManager.getLaunchIntentForPackage(packageName)!=null)return true;
        Intent share=new Intent(Intent.ACTION_SEND);
        share.setType("image/png");
        share.setPackage(packageName);
        return share.resolveActivity(packageManager)!=null;
    }

    private String selectedBankLabel(){
        return prefs.getString(PREF_BANK_LABEL,"").trim();
    }

    private boolean supportsImageShare(String packageName){
        if(packageName==null||packageName.isBlank())return false;
        Intent direct=new Intent(Intent.ACTION_SEND);
        direct.setType("image/png");
        direct.setPackage(packageName);
        return direct.resolveActivity(getPackageManager())!=null;
    }

    private BankingAppRegistry.Profile selectedBankProfile(){
        String packageName=prefs.getString(PREF_BANK_PACKAGE,"").trim();
        return BankingAppRegistry.profile(packageName,selectedBankLabel(),supportsImageShare(packageName));
    }

    private String selectedBankPaymentHint(){
        if(!hasPreferredBank())return ui("Für Direktzahlungen zuerst eine Banking-App festlegen.");
        String bank=selectedBankLabel();
        BankingAppRegistry.Profile profile=selectedBankProfile();
        switch(profile.capability){
            case DIRECT_SHARE:
                return formatBankHint(
                        "PFVR versucht immer zuerst die direkte Swiss-QR-Bildübergabe an %s. Falls die App den QR nicht übernimmt, folgen sichere Fallbacks.",
                        "PFVR probiert immer zerscht d direkt Swiss-QR-Bildübergab a %s. Wenn d App de QR nöd übernimmt, folged sicheri Fallbacks.",
                        bank
                );
            case FILE_IMPORT:
                return formatBankHint(
                        "PFVR versucht zuerst die direkte QR-Bildübergabe an %s. Falls Android sie nicht anbietet, kann die QR-Datei gespeichert und in der Banking-App importiert werden.",
                        "PFVR probiert zerscht d direkt QR-Bildübergab a %s. Wenn Android sie nöd aabietet, cha d QR-Datei gspeicheret und i d Banking-App importiert werde.",
                        bank
                );
            case SCAN_ONLY:
                return formatBankHint(
                        "PFVR versucht auch bei %s zuerst die direkte QR-Bildübergabe. Erst wenn die App kein Bild annimmt, wird sie geöffnet und die Zahlungsdaten werden kopiert.",
                        "PFVR probiert au bi %s zerscht d direkt QR-Bildübergab. Erscht wenn d App kei Bild annimmt, wird sie göffnet und d Zahligsdate wärded kopiert.",
                        bank
                );
            default:
                return formatBankHint(
                        "PFVR versucht bei %s immer zuerst die QR-Bildübergabe und prüft danach weitere Datei- und Textwege. Erst zuletzt wird die App mit kopierten Zahlungsdaten geöffnet.",
                        "PFVR probiert bi %s immer zerscht d QR-Bildübergab und prüeft nachher witeri Datei- und Textwäg. Erscht zletscht wird d App mit kopierte Zahligsdate göffnet.",
                        bank
                );
        }
    }

    private String preferredBankPaymentLabel(){
        String label=selectedBankLabel();
        return hasPreferredBank()?ui("Mit")+" "+label+" "+ui("bezahlen"):ui("Banking-App festlegen");
    }

    private String formatBankHint(String german,String swissGerman,String bank){
        String pattern=UiLanguage.isSwissGerman(uiMode())?swissGerman:german;
        return String.format(Locale.GERMAN,pattern,bank);
    }


    private void payWithPreferredBank(EditText amountInput){
        if(!hasPreferredBank()){
            Toast.makeText(this,ui("Bitte unter Einstellungen → Zahlung eine Banking-App festlegen."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        sharePaymentQr(amountInput);
    }

    private CashCatalog.Catalog cashCatalog(){
        if(cashCatalog!=null)return cashCatalog;
        try{cashCatalog=CashCatalog.load(this);}catch(Exception ignored){cashCatalog=null;}
        return cashCatalog;
    }

    private View cashItemRow(CashCatalog.Item item){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,dp(9),0,dp(9));
        LinearLayout copy=new LinearLayout(this);copy.setOrientation(LinearLayout.VERTICAL);copy.setPadding(0,0,dp(8),0);row.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        copy.addView(txtRaw(item.name,14,TEXT,true));
        String detail=(item.variant==null||item.variant.isBlank()?"":item.variant+" · ")+formatCashPrice(item.price)+(item.deposit?" · Depot":"");copy.addView(txtRaw(detail,12,item.deposit?WATER:MUTED,false));
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
        copy.addView(txtRaw(quantity+"× "+item.displayName(),13,TEXT,true));
        copy.addView(txtRaw(formatCashPrice(item.price*quantity),12,item.deposit?WATER:MUTED,false));
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
        if(catalog==null||catalog.itemCount(cashCart)<=0){Toast.makeText(this,ui("Der Warenkorb ist leer."),Toast.LENGTH_SHORT).show();return null;}
        EditText input=new EditText(this);input.setText(String.format(Locale.US,"%.2f",catalog.total(cashCart)));return input;
    }

    private void openTwintDirect(EditText amountInput){
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,ui("Bitte einen gültigen CHF-Betrag eingeben oder das Feld leer lassen."),Toast.LENGTH_LONG).show();return;}
        if(!a.isBlank())copy("PFVR TWINT-Betrag",a,"CHF "+a+" "+(UiLanguage.isSwissGerman(uiMode())?"kopiert – uf de PFVR-Site iitrage.":"kopiert – auf der PFVR-Seite eintragen."));
        external(TWINT_DIRECT_URL);
    }

    private void sharePaymentQr(EditText amountInput){
        String value=amount(amountInput==null?null:amountInput.getText().toString());
        if(value==null){Toast.makeText(this,ui("Bitte einen gültigen CHF-Betrag eingeben."),Toast.LENGTH_LONG).show();return;}
        try{
            Bitmap qr=makeSwissQr(value);
            pendingQrBitmap=qr;
            File directory=new File(getCacheDir(),"shared");
            if(!directory.exists()&&!directory.mkdirs())throw new Exception("cache directory");
            File file=new File(directory,PaymentQrFileName.forAmount(value));
            try(FileOutputStream out=new FileOutputStream(file)){if(!qr.compress(Bitmap.CompressFormat.PNG,100,out))throw new Exception("png");}
            Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
            String paymentText=paymentShareText(value);
            String preferred=prefs.getString(PREF_BANK_PACKAGE,"").trim();
            PackageManager packageManager=getPackageManager();

            if(!preferred.isBlank()){
                BankingAppRegistry.Profile profile=BankingAppRegistry.profile(preferred,selectedBankLabel(),supportsImageShare(preferred));

                // Share-first: jede Banking-App bekommt dieselben QR-Bildversuche. Die statische
                // Capability steuert ausschließlich den Fallback, niemals ob Share versucht wird.
                if(tryQrImageHandoff(preferred,uri,paymentText))return;

                if(profile.capability==BankingAppRegistry.Capability.FILE_IMPORT){
                    showBankFileImportFallback(qr,value,paymentText,preferred);
                    return;
                }

                if(profile.capability==BankingAppRegistry.Capability.SCAN_ONLY){
                    launchPreferredBankWithCopiedData(preferred,paymentText,"QR-Bildübergabe wurde von dieser App nicht angeboten – Banking-App geöffnet und Zahlungsdaten kopiert.");
                    return;
                }

                Intent textShare=new Intent(Intent.ACTION_SEND);
                textShare.setType("text/plain");
                textShare.putExtra(Intent.EXTRA_SUBJECT,"PFVR Zahlung");
                textShare.putExtra(Intent.EXTRA_TEXT,paymentText);
                textShare.setPackage(preferred);
                if(startIfResolvable(textShare)){
                    Toast.makeText(this,ui("Zahlungsdaten an")+" "+selectedBankLabel()+" "+ui("übergeben."),Toast.LENGTH_SHORT).show();
                    return;
                }

                launchPreferredBankWithCopiedData(preferred,paymentText,"Direkter QR-Import wurde von dieser App nicht angeboten – Zahlungsdaten wurden kopiert.");
                return;
            }

            Intent send=qrShareIntent(uri,paymentText,"image/png",null);
            if(packageManager.queryIntentActivities(send,0).isEmpty()){
                Toast.makeText(this,ui("Keine App unterstützt die direkte QR-Übergabe. QR wird stattdessen angezeigt."),Toast.LENGTH_LONG).show();
                showPaymentQr(amountInput);
                return;
            }
            startActivity(Intent.createChooser(send,"Swiss QR an Banking-App übergeben"));
        }catch(Exception e){
            Toast.makeText(this,ui("Direkte QR-Übergabe nicht möglich. QR wird stattdessen angezeigt."),Toast.LENGTH_LONG).show();
            showPaymentQr(amountInput);
        }
    }

    private Intent qrShareIntent(Uri uri,String paymentText,String mimeType,String preferred){
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType(mimeType);
        send.putExtra(Intent.EXTRA_SUBJECT,"PFVR Zahlung");
        send.putExtra(Intent.EXTRA_TEXT,paymentText);
        send.putExtra(Intent.EXTRA_STREAM,uri);
        send.setClipData(ClipData.newUri(getContentResolver(),"PFVR Swiss QR",uri));
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if(preferred!=null&&!preferred.isBlank())send.setPackage(preferred);
        return send;
    }

    private boolean startIfResolvable(Intent intent){
        try{
            if(intent.resolveActivity(getPackageManager())==null)return false;
            startActivity(intent);
            return true;
        }catch(Exception ignored){return false;}
    }

    private boolean tryQrImageHandoff(String preferred,Uri uri,String paymentText){
        Intent direct=qrShareIntent(uri,paymentText,"image/png",preferred);
        if(startIfResolvable(direct)){
            Toast.makeText(this,ui("Swiss QR an")+" "+selectedBankLabel()+" "+ui("übergeben."),Toast.LENGTH_SHORT).show();
            return true;
        }

        Intent imageView=new Intent(Intent.ACTION_VIEW);
        imageView.setDataAndType(uri,"image/png");
        imageView.setClipData(ClipData.newUri(getContentResolver(),"PFVR Swiss QR",uri));
        imageView.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        imageView.setPackage(preferred);
        if(startIfResolvable(imageView)){
            Toast.makeText(this,ui("Swiss QR mit")+" "+selectedBankLabel()+" "+ui("geöffnet."),Toast.LENGTH_SHORT).show();
            return true;
        }

        Intent genericImage=qrShareIntent(uri,paymentText,"image/*",preferred);
        if(startIfResolvable(genericImage)){
            Toast.makeText(this,ui("Zahlungsbild an")+" "+selectedBankLabel()+" "+ui("übergeben."),Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void launchPreferredBankWithCopiedData(String preferred,String paymentText,String reason){
        Intent launch=getPackageManager().getLaunchIntentForPackage(preferred);
        if(launch==null){
            Toast.makeText(this,ui("Die gewählte Banking-App ist nicht mehr verfügbar."),Toast.LENGTH_LONG).show();
            openPaymentSettings();
            return;
        }
        copy("PFVR Zahlung",paymentText,"Zahlungsdaten kopiert");
        startActivity(launch);
        Toast.makeText(this,ui(reason),Toast.LENGTH_LONG).show();
    }

    private void showBankFileImportFallback(Bitmap qr,String value,String paymentText,String preferred){
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16),dp(8),dp(16),0);
        ImageView image=new ImageView(this);
        image.setImageBitmap(qr);
        image.setAdjustViewBounds(true);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        box.addView(image,new LinearLayout.LayoutParams(-1,dp(270)));
        TextView info=txtRaw(formatBankHint("Die direkte QR-Bildübergabe an %s wurde versucht, aber von der installierten App-Version nicht angeboten. QR speichern und anschließend in der Banking-App hochladen.","D direkt QR-Bildübergab a %s isch probiert worde, aber vo de installierte App-Version nöd aabote worde. De QR speichere und nachher i de Banking-App ufelade.",selectedBankLabel()),13,MUTED,false);
        info.setPadding(0,dp(8),0,0);
        box.addView(info);
        new AlertDialog.Builder(this,dialogTheme())
                .setTitle(selectedBankLabel()+" · QR-Datei")
                .setView(box)
                .setPositiveButton(ui("Banking-App öffnen"),(d,w)->launchPreferredBankWithCopiedData(preferred,paymentText,"Zahlungsdaten kopiert – QR-Datei bei Bedarf in der Banking-App auswählen."))
                .setNeutralButton(ui("QR speichern"),(d,w)->saveQr(value))
                .setNegativeButton(ui("Schliessen"),null)
                .show();
    }

    private String paymentShareText(String value){
        StringBuilder text=new StringBuilder();
        text.append(CLUB_PAYEE).append("\n").append(CLUB_IBAN).append("\n").append(CLUB_PAYMENT_NOTE);
        if(value!=null&&!value.isBlank())text.append("\nCHF ").append(value);
        return text.toString();
    }

    private void showPaymentQr(EditText amountInput) {
        String a=amount(amountInput==null?null:amountInput.getText().toString());
        if(a==null){Toast.makeText(this,ui("Bitte einen gültigen CHF-Betrag eingeben oder Feld leer/0 für offenen Betrag lassen."),Toast.LENGTH_LONG).show();return;}
        try {
            Bitmap qr=makeSwissQr(a);
            pendingQrBitmap=qr;
            LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(16),dp(8),dp(16),0);
            ImageView image=new ImageView(this); image.setImageBitmap(qr); image.setAdjustViewBounds(true); image.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(image,new LinearLayout.LayoutParams(-1,dp(330)));
            String amountLine=a.isBlank()?"Betrag offen · in Banking-App eingeben":"CHF "+a;
            TextView details=txtRaw(amountLine+"\n"+CLUB_PAYEE+"\n"+CLUB_IBAN+"\n"+CLUB_PAYMENT_NOTE,14,TEXT,false); details.setGravity(Gravity.CENTER); details.setPadding(0,dp(8),0,dp(4)); box.addView(details);
            TextView note=txt("Direkte Übergabe versucht den QR als temporäres Bild an eine kompatible Banking-App zu senden. Falls die Bank das nicht unterstützt, bleibt Speichern/Öffnen als Fallback.",12,MUTED,false); note.setGravity(Gravity.CENTER); box.addView(note);
            new AlertDialog.Builder(this,dialogTheme())
                    .setTitle(ui("Bankzahlung · Swiss QR"))
                    .setView(box)
                    .setPositiveButton(ui("Direkt an Banking-App"),(d,w)->sharePaymentQr(amountInput))
                    .setNeutralButton(ui("QR speichern"),(d,w)->saveQr(a))
                    .setNegativeButton(ui("Schliessen"),null)
                    .show();
        } catch(Exception e) {
            Toast.makeText(this,ui("Swiss QR konnte nicht erzeugt werden."),Toast.LENGTH_LONG).show();
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
        if(pendingQrBitmap==null){Toast.makeText(this,ui("Kein QR-Code vorhanden."),Toast.LENGTH_SHORT).show();return;}
        Intent save=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        save.addCategory(Intent.CATEGORY_OPENABLE); save.setType("image/png");
        save.putExtra(Intent.EXTRA_TITLE,PaymentQrFileName.forAmount(amount));
        try{startActivityForResult(save,REQ_SAVE_QR);}catch(Exception e){Toast.makeText(this,ui("Speichern ist auf diesem Gerät nicht verfügbar."),Toast.LENGTH_LONG).show();}
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==REQ_SAVE_QR && resultCode==RESULT_OK && data!=null && data.getData()!=null && pendingQrBitmap!=null){
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())){
                if(out==null||!pendingQrBitmap.compress(Bitmap.CompressFormat.PNG,100,out))throw new Exception("write failed");
                Toast.makeText(this,ui("Swiss QR gespeichert."),Toast.LENGTH_SHORT).show();
            }catch(Exception e){Toast.makeText(this,ui("QR-Code konnte nicht gespeichert werden."),Toast.LENGTH_LONG).show();}
        }
    }

    private void chooseBankingApp(){
        PackageManager packageManager=getPackageManager();
        Map<String,AppChoice> byPackage=new LinkedHashMap<>();
        Set<String> directPackages=new LinkedHashSet<>();

        for(Map.Entry<String,BankingAppRegistry.Profile> known:BankingAppRegistry.knownApps().entrySet()){
            try{
                ApplicationInfo info=packageManager.getApplicationInfo(known.getKey(),0);
                if(!info.enabled)continue;
                String label=String.valueOf(packageManager.getApplicationLabel(info)).trim();
                if(label.isEmpty())label=known.getValue().label;
                boolean directShare=supportsImageShare(known.getKey());
                if(directShare)directPackages.add(known.getKey());
                BankingAppRegistry.Profile profile=BankingAppRegistry.profile(known.getKey(),label,directShare);
                byPackage.put(known.getKey(),new AppChoice(label,known.getKey(),directShare,profile.capability,profile.documented));
            }catch(PackageManager.NameNotFoundException ignored){}
        }

        Intent shareIntent=new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        for(ResolveInfo resolveInfo:packageManager.queryIntentActivities(shareIntent,PackageManager.MATCH_DEFAULT_ONLY)){
            if(resolveInfo.activityInfo==null||resolveInfo.activityInfo.packageName==null)continue;
            String packageName=resolveInfo.activityInfo.packageName;
            if(packageName.equals(getPackageName()))continue;
            directPackages.add(packageName);
            String label=String.valueOf(resolveInfo.loadLabel(packageManager)).trim();
            String haystack=(label+" "+packageName).toLowerCase(Locale.ROOT);
            if(BankingAppRegistry.looksLikeBankingApp(haystack)){
                BankingAppRegistry.Profile profile=BankingAppRegistry.profile(packageName,label,true);
                byPackage.put(packageName,new AppChoice(label,packageName,true,profile.capability,profile.documented));
            }
        }

        Intent launcherIntent=new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        for(ResolveInfo resolveInfo:packageManager.queryIntentActivities(launcherIntent,PackageManager.MATCH_DEFAULT_ONLY)){
            if(resolveInfo.activityInfo==null||resolveInfo.activityInfo.packageName==null)continue;
            String packageName=resolveInfo.activityInfo.packageName;
            if(packageName.equals(getPackageName()))continue;
            String label=String.valueOf(resolveInfo.loadLabel(packageManager)).trim();
            String haystack=(label+" "+packageName).toLowerCase(Locale.ROOT);
            if(!BankingAppRegistry.looksLikeBankingApp(haystack))continue;
            boolean directShare=directPackages.contains(packageName)||supportsImageShare(packageName);
            BankingAppRegistry.Profile profile=BankingAppRegistry.profile(packageName,label,directShare);
            byPackage.put(packageName,new AppChoice(label,packageName,directShare,profile.capability,profile.documented));
        }

        List<AppChoice> found=new ArrayList<>(byPackage.values());
        found.sort((first,second)->{
            int firstPriority=BankingAppRegistry.priority(first.label,first.pkg);
            int secondPriority=BankingAppRegistry.priority(second.label,second.pkg);
            if(firstPriority!=secondPriority)return Integer.compare(firstPriority,secondPriority);
            int firstCapability=bankCapabilityRank(first.capability);
            int secondCapability=bankCapabilityRank(second.capability);
            if(firstCapability!=secondCapability)return Integer.compare(firstCapability,secondCapability);
            return first.label.compareToIgnoreCase(second.label);
        });

        if(found.isEmpty()){
            chooseAnyInstalledApp();
            return;
        }

        String currentPackage=prefs.getString(PREF_BANK_PACKAGE,"");
        int selected=-1;
        String[] labels=new String[found.size()];
        for(int index=0;index<found.size();index++){
            AppChoice app=found.get(index);
            labels[index]=app.label+" · "+ui(app.capability.label);
            if(app.pkg.equals(currentPackage))selected=index;
        }

        new AlertDialog.Builder(this,dialogTheme())
                .setTitle(ui("Banking-App auswählen"))
                .setSingleChoiceItems(labels,selected,(dialog,index)->{
                    saveBankChoice(found.get(index));
                    dialog.dismiss();
                })
                .setNeutralButton(ui("Alle Apps"),(dialog,which)->chooseAnyInstalledApp())
                .setNegativeButton(ui("Abbrechen"),null)
                .show();
    }

    private int bankCapabilityRank(BankingAppRegistry.Capability capability){
        switch(capability){
            case DIRECT_SHARE:return 0;
            case FILE_IMPORT:return 1;
            case SCAN_ONLY:return 2;
            default:return 3;
        }
    }

    private void chooseAnyInstalledApp(){
        PackageManager packageManager=getPackageManager();
        Intent launcherIntent=new Intent(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        Map<String,AppChoice> byPackage=new LinkedHashMap<>();
        for(ResolveInfo resolveInfo:packageManager.queryIntentActivities(launcherIntent,PackageManager.MATCH_DEFAULT_ONLY)){
            if(resolveInfo.activityInfo==null||resolveInfo.activityInfo.packageName==null)continue;
            String packageName=resolveInfo.activityInfo.packageName;
            if(packageName.equals(getPackageName()))continue;
            String label=String.valueOf(resolveInfo.loadLabel(packageManager)).trim();
            boolean directShare=supportsImageShare(packageName);
            BankingAppRegistry.Profile profile=BankingAppRegistry.profile(packageName,label,directShare);
            byPackage.putIfAbsent(packageName,new AppChoice(label,packageName,directShare,profile.capability,profile.documented));
        }
        List<AppChoice> apps=new ArrayList<>(byPackage.values());
        apps.sort(Comparator.comparing(app->app.label.toLowerCase(Locale.ROOT)));
        if(apps.isEmpty()){
            Toast.makeText(this,ui("Keine weitere installierte App gefunden."),Toast.LENGTH_LONG).show();
            return;
        }
        String[] labels=new String[apps.size()];
        String currentPackage=prefs.getString(PREF_BANK_PACKAGE,"");
        int selected=-1;
        for(int index=0;index<apps.size();index++){
            AppChoice app=apps.get(index);
            labels[index]=app.label+" · "+ui(app.capability.label);
            if(app.pkg.equals(currentPackage))selected=index;
        }
        new AlertDialog.Builder(this,dialogTheme())
                .setTitle(ui("Installierte App auswählen"))
                .setSingleChoiceItems(labels,selected,(dialog,index)->{
                    saveBankChoice(apps.get(index));
                    dialog.dismiss();
                })
                .setNegativeButton(ui("Abbrechen"),null)
                .show();
    }

    private void saveBankChoice(AppChoice choice){
        prefs.edit()
                .putString(PREF_BANK_PACKAGE,choice.pkg)
                .putString(PREF_BANK_LABEL,choice.label)
                .apply();
        Toast.makeText(this,choice.label+" "+ui("festgelegt."),Toast.LENGTH_SHORT).show();
        if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);
    }


    private String amount(String raw) { if(raw==null||raw.trim().isEmpty())return ""; try{double n=Double.parseDouble(raw.trim().replace(',','.')); if(n<0||n>100000)return null; if(n==0)return ""; return String.format(Locale.US,"%.2f",n);}catch(Exception e){return null;} }
    private void copy(String label,String value,String toast){ClipboardManager cm=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE); if(cm!=null)cm.setPrimaryClip(ClipData.newPlainText(label,value)); Toast.makeText(this,ui(toast),Toast.LENGTH_SHORT).show();}

    private View club() {
    ScrollView scroll=new ScrollView(this);
    LinearLayout body=body();
    scroll.addView(body);

    LinearLayout hero=card();
    hero.setGravity(Gravity.CENTER_VERTICAL);
    ImageView logo=new ImageView(this);
    logo.setImageResource(R.drawable.pfvr_logo);
    logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
    hero.addView(logo,new LinearLayout.LayoutParams(dp(82),dp(82)));
    LinearLayout info=new LinearLayout(this);
    info.setOrientation(LinearLayout.VERTICAL);
    info.setPadding(dp(15),0,0,0);
    info.addView(txt("Pontonierfahrverein Rheinfelden",19,TEXT,true));
    info.addView(txt("Gegründet 1896 · Sport und Vereinsleben am Rhein",13,MUTED,false));
    hero.addView(info,new LinearLayout.LayoutParams(0,-2,1));
    body.addView(hero,margin(-1,-2,0,4,0,14));
    LinearLayout tiles=new LinearLayout(this);
    tiles.setOrientation(LinearLayout.VERTICAL);
    body.addView(tiles,new LinearLayout.LayoutParams(-1,-2));
    addConfiguredTiles(tiles,TileLayoutStore.Area.CLUB,this::clubTileView);
    return scroll;
}

private View clubTileView(TileLayoutStore.Spec spec){
    switch(spec.id){
        case "club_about":return clubAboutTile();
        case "club_join":return clubActionTile("Schnuppertraining & Mitglied werden","Schnuppertraining auch vor der Mitgliedschaft · Infos und Formulare",v->external(PublicLinks.JOIN));
        case "club_news":return clubActionTile("Vereinsnews","Aktuelle Meldungen",v->navigate(Screen.NEWS));
        case "club_program":return clubActionTile("Jahresprogramm","Termine und Kalender",v->openInApp(PROGRAM,"Jahresprogramm"));
        case "club_board":return clubActionTile("Vorstand","Funktionen und Kontakte",v->openInApp(BOARD,"Vorstand"));
        case "club_history":return clubActionTile("Geschichte","Seit 1896 auf dem Rhein",v->openInApp(HISTORY,"Geschichte"));
        case "club_depot":return clubActionTile("Depot & Route","Rheinweg 42",v->openMap());
        case "club_phone":return clubActionTile("Telefon","076 209 18 96",v->startActivity(new Intent(Intent.ACTION_DIAL,Uri.parse("tel:+41762091896"))));
        case "club_email":return clubActionTile("E-Mail","info@pfvr.ch",v->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("mailto:info@pfvr.ch"))));
        case "club_contact":return clubActionTile("Kontaktseite","Weitere Ansprechwege",v->openInApp(CONTACT,"Kontakt"));
        case "club_instagram":return clubActionTile("Instagram","@pontoniererheinfelden",v->external(PublicLinks.INSTAGRAM));
        case "club_facebook":return clubActionTile("Facebook","Pontoniere Rheinfelden",v->external(PublicLinks.FACEBOOK));
        default:return null;
    }
}

private View clubAboutTile(){
    LinearLayout group=tileGroup("Über den Verein",null);
    LinearLayout about=card();
    about.setOrientation(LinearLayout.VERTICAL);
    about.addView(txt("Seit 1896 auf dem Rhein",18,TEXT,true));
    TextView text=txt("Beim Pontonierfahren verbinden sich präzise Bootsführung, Kraft, Technik und Teamarbeit. Der PFVR trainiert auf dem Rhein in Rheinfelden, nimmt an Wettfahren teil und pflegt zugleich ein aktives Vereinsleben sowie die Ausbildung des Nachwuchses.",14,MUTED,false);
    text.setPadding(0,dp(7),0,dp(10));
    about.addView(text);
    TextView history=txt("Geschichte und Meilensteine öffnen  →",12,WATER,true);
    history.setGravity(Gravity.END);
    history.setOnClickListener(v->openInApp(HISTORY,"Geschichte"));
    about.addView(history);
    group.addView(about,new LinearLayout.LayoutParams(-1,-2));
    return group;
}

private View clubActionTile(String title,String detail,View.OnClickListener listener){
    LinearLayout tile=card();
    tile.setOrientation(LinearLayout.VERTICAL);
    tile.setMinimumHeight(dp(118));
    tile.setOnClickListener(listener);
    TextView heading=txt(title,16,TEXT,true);
    tile.addView(heading);
    TextView description=txt(detail,12,MUTED,false);
    description.setPadding(0,dp(5),0,0);
    tile.addView(description);
    tile.addView(new View(this),new LinearLayout.LayoutParams(1,0,1));
    TextView open=txt("Öffnen  →",12,WATER,true);
    open.setGravity(Gravity.END);
    tile.addView(open);
    return tile;
}

    private View internal() {
        String url=normalizeInternalUrl(prefs.getString(PREF_INTERNAL_URL,"")); if(!validInternal(url)) return internalMissing(); prefs.edit().putString(PREF_INTERNAL_URL,url).apply();
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(themeBg(Color.WHITE));
        LinearLayout tools=new LinearLayout(this); tools.setPadding(dp(9),dp(8),dp(9),dp(8)); tools.setBackgroundColor(themeBg(Color.rgb(236,243,247))); root.addView(tools,new LinearLayout.LayoutParams(-1,dp(56)));
        WebView web=web(false); activeWebView=web; web.setBackgroundColor(themeBg(Color.WHITE));
        if(android.os.Build.VERSION.SDK_INT>=33) web.getSettings().setAlgorithmicDarkeningAllowed(false);
        web.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        boolean appView=prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);
        Button people=btn("Personen",Color.WHITE,NAVY);
        people.setContentDescription(ui("Personen hinzufügen oder entfernen"));
        people.setVisibility(appView?View.VISIBLE:View.GONE);
        people.setOnClickListener(v->openInternalPeopleManager(web,0));
        tools.addView(people,new LinearLayout.LayoutParams(0,dp(40),1));
        Button mode=btn(appView?"Original":"App-Ansicht",NAVY,Color.WHITE);
        mode.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_INTERNAL_APP_VIEW,true);prefs.edit().putBoolean(PREF_INTERNAL_APP_VIEW,next).apply();mode.setText(ui(next?"Original":"App-Ansicht"));people.setVisibility(next?View.VISIBLE:View.GONE);web.clearCache(false);web.reload();});
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

    private void openInternalPeopleManager(WebView web,int attempt){
        if(web==null)return;
        String script="(function(){try{return !!(window.pfvrOpenPeopleManager&&window.pfvrOpenPeopleManager());}catch(e){return false;}})();";
        web.evaluateJavascript(script,result->{
            if("true".equalsIgnoreCase(String.valueOf(result)))return;
            if(attempt>=2){Toast.makeText(this,ui("Personenverwaltung ist auf dieser Seite nicht verfügbar."),Toast.LENGTH_SHORT).show();return;}
            if(attempt==0)internalSkin(web);
            new Handler(Looper.getMainLooper()).postDelayed(()->openInternalPeopleManager(web,attempt+1),attempt==0?260L:650L);
        });
    }

    private void showInternalLoadError(WebView v,String message){
        String safe=message.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        String bg=darkMode?"#11171C":"#FFFFFF",text=darkMode?"#ECF1F4":"#15232E",muted=darkMode?"#A0B0BA":"#60717E";
        String html="<html><head><meta name='viewport' content='width=device-width,initial-scale=1'><meta name='color-scheme' content='"+(darkMode?"dark":"light")+"'></head><body style='font-family:sans-serif;background:"+bg+";color:"+text+";padding:24px'><h2>"+ui("Interner Bereich konnte nicht geladen werden")+"</h2><p>"+safe+"</p><p style='color:"+muted+"'>"+ui("Prüfe den persönlichen Link unter Einstellungen oder tippe oben auf Neu laden.")+"</p></body></html>";
        v.loadDataWithBaseURL("https://intern.pfvr.ch/",html,"text/html","UTF-8",null);
    }

    private void internalSkin(WebView view){
    String background=darkMode?"#11171C":"#F4F7F9";
    String card=darkMode?"#1A2228":"#FFFFFF";
    String soft=darkMode?"#232E36":"#EDF3F6";
    String text=darkMode?"#ECF1F4":"#15232E";
    String muted=darkMode?"#A0B0BA":"#60717E";
    String border=darkMode?"#344550":"#DCE5EA";
    String link=darkMode?"#5BBED5":"#247E99";
    String baseInternalUrl=normalizeInternalUrl(prefs.getString(PREF_INTERNAL_URL,""));
    view.evaluateJavascript("window.__pfvrBaseInternalUrl="+JSONObject.quote(baseInternalUrl)+";"+InternalAttendanceSkin.javascript(background,card,soft,text,muted,border,link,uiMode()),null);
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
        new AlertDialog.Builder(this,dialogTheme()).setTitle(ui("Persönlichen Intern-Link ändern")).setView(input)
            .setPositiveButton(ui("Speichern"),(d,w)->{String x=normalizeInternalUrl(input.getText().toString().trim());if(validInternal(x)){prefs.edit().putString(PREF_INTERNAL_URL,x).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);}else Toast.makeText(this,ui("Bitte den persönlichen An-/Abmelde-Link (what=abmeldung) verwenden."),Toast.LENGTH_LONG).show();})
            .setNeutralButton(ui("Entfernen"),(d,w)->{prefs.edit().remove(PREF_INTERNAL_URL).apply();if(current==Screen.SETTINGS)navigate(Screen.SETTINGS);})
            .setNegativeButton(ui("Abbrechen"),null).show();
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
    private void openInApp(String url,String title){headerSubtitle.setText(ui(title));content.removeAllViews();content.addView(webScreen(url,true));}

    private void loadCachedEvents(){String raw=prefs.getString(PREF_ICS_CACHE,"");eventsUpdated=prefs.getLong(PREF_ICS_UPDATED,0L);if(raw.trim().isEmpty())return;try{events=parseIcs(raw);}catch(Exception ex){events=new ArrayList<>();eventsUpdated=0L;prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).apply();}}
    private String calendarStatus(){if(eventsUpdated<=0)return ui(eventsLoading?"Erster Abruf läuft im Hintergrund.":"Noch kein lokaler Kalender-Cache.");ZonedDateTime z=java.time.Instant.ofEpochMilli(eventsUpdated).atZone(ZoneId.of("Europe/Zurich"));String d=z.toLocalDate().equals(LocalDate.now(ZoneId.of("Europe/Zurich")))?ui("heute")+" "+z.format(DateTimeFormatter.ofPattern("HH:mm")):z.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));return ui("Lokal gespeichert · zuletzt aktualisiert")+" "+d+" Uhr";}
    private void refreshEvents(boolean toast,Runnable done){if(!toast&&eventsUpdated>0L&&System.currentTimeMillis()-eventsUpdated<60L*60L*1000L){if(done!=null)done.run();return;}if(eventsLoading){if(toast)Toast.makeText(this,ui("Kalender-Aktualisierung läuft bereits."),Toast.LENGTH_SHORT).show();return;}eventsLoading=true;new Thread(()->{HttpURLConnection c=null;try{c=(HttpURLConnection)new URL(ICS).openConnection();c.setConnectTimeout(6000);c.setReadTimeout(8000);c.setUseCaches(true);c.setRequestProperty("User-Agent","PFVR-Rheinfelden-App/"+BuildConfig.VERSION_NAME);c.setRequestProperty("Accept","text/calendar,text/plain,*/*");if(c.getResponseCode()/100!=2)throw new Exception("HTTP "+c.getResponseCode());BufferedReader br=new BufferedReader(new InputStreamReader(c.getInputStream(),java.nio.charset.StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line).append(System.lineSeparator());br.close();String raw=sb.toString();List<Event> parsed=parseIcs(raw);if(parsed.isEmpty())throw new Exception("Keine kommenden Termine im Feed");long updated=System.currentTimeMillis();prefs.edit().putString(PREF_ICS_CACHE,raw).putLong(PREF_ICS_UPDATED,updated).apply();runOnUiThread(()->{events=parsed;eventsUpdated=updated;eventsLoading=false;if(toast)Toast.makeText(this,parsed.size()+" "+ui("kommende Termine aktualisiert"),Toast.LENGTH_SHORT).show();if(done!=null)done.run();});}catch(Exception ex){runOnUiThread(()->{eventsLoading=false;if(toast){String m=events.isEmpty()?"Kalender konnte gerade nicht geladen werden.":"Keine Verbindung – gespeicherter Kalenderstand bleibt sichtbar.";Toast.makeText(this,ui(m),Toast.LENGTH_LONG).show();}else if(events.isEmpty())Toast.makeText(this,ui("Kalender lädt im Hintergrund. Bei langsamer Verbindung kann der erste Abruf etwas dauern."),Toast.LENGTH_LONG).show();if(done!=null)done.run();});}finally{if(c!=null)c.disconnect();}}).start();}

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
    private TextView txtRaw(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(themeText(color));t.setLineSpacing(0,1.08f);if(bold)t.setTypeface(Typeface.DEFAULT_BOLD);return t;}
    private TextView txt(String s,float size,int color,boolean bold){return txtRaw(ui(s),size,color,bold);}
    private Button btn(String s,int bg,int fg){Button b=new Button(this);b.setText(ui(s));b.setTextSize(13);b.setTextColor(themeText(fg));b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT_BOLD);b.setPadding(dp(12),0,dp(12),0);b.setMinHeight(0);b.setMinWidth(0);b.setBackground(round(bg,12));return b;}
    private GradientDrawable round(int color,float r){GradientDrawable d=new GradientDrawable();d.setColor(themeBg(color));d.setCornerRadius(dp(r));return d;}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;}
    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private String localizedDateWords(String value){
        if(value==null||!UiLanguage.isSwissGerman(uiMode()))return value;
        return value.replace("Montag","Mäntig")
                .replace("Dienstag","Zischtig")
                .replace("Mittwoch","Mittwuch")
                .replace("Donnerstag","Dunschtig")
                .replace("Freitag","Fritig")
                .replace("Samstag","Samschtig")
                .replace("Sonntag","Sunntig");
    }
    private String cap(String s){return s==null||s.isEmpty()?s:s.substring(0,1).toUpperCase(Locale.GERMAN)+s.substring(1);}
    private void external(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){Toast.makeText(this,ui("Link konnte nicht geöffnet werden."),Toast.LENGTH_SHORT).show();}}
    private void openMap(){Uri u=Uri.parse("geo:0,0?q="+Uri.encode("Rheinweg 42, 4310 Rheinfelden, Schweiz"));try{startActivity(new Intent(Intent.ACTION_VIEW,u));}catch(Exception e){external("https://www.google.com/maps/search/?api=1&query="+Uri.encode("Rheinweg 42, 4310 Rheinfelden, Schweiz"));}}

    private void handleBack(){if(current==Screen.INTERNAL){navigate(Screen.HOME);return;}if(activeWebView!=null&&activeWebView.canGoBack())activeWebView.goBack();else if(current==Screen.TILE_SETTINGS)navigate(Screen.SETTINGS);else if(current!=Screen.HOME)navigate(Screen.HOME);else super.onBackPressed();}
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

    private class DualRiverTrendView extends View {
        private final TrendSeries flow;
        private final TrendSeries level;
        private final RiverRange range;
        private final HydroStation station;
        private final Paint grid=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint label=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint flowLine=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint levelLine=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint threshold=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint point=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltip=new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint tooltipText=new Paint(Paint.ANTI_ALIAS_FLAG);
        private long selectedTime=Long.MIN_VALUE;

        DualRiverTrendView(Context context,TrendSeries flow,TrendSeries level,RiverRange range,HydroStation station){
            super(context);this.flow=flow;this.level=level;this.range=range;this.station=station;
            setClickable(true);setFocusable(true);
            grid.setStrokeWidth(dp(1));label.setTextSize(9*getResources().getDisplayMetrics().scaledDensity);
            flowLine.setStyle(Paint.Style.STROKE);flowLine.setStrokeWidth(dp(2.5f));flowLine.setStrokeCap(Paint.Cap.ROUND);flowLine.setStrokeJoin(Paint.Join.ROUND);
            levelLine.setStyle(Paint.Style.STROKE);levelLine.setStrokeWidth(dp(2.2f));levelLine.setStrokeCap(Paint.Cap.ROUND);levelLine.setStrokeJoin(Paint.Join.ROUND);
            threshold.setStyle(Paint.Style.STROKE);threshold.setStrokeWidth(dp(1.2f));threshold.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(4)},0));
            point.setStyle(Paint.Style.FILL);tooltip.setStyle(Paint.Style.FILL);
            tooltipText.setTextSize(10*getResources().getDisplayMetrics().scaledDensity);tooltipText.setTypeface(Typeface.DEFAULT_BOLD);
            setContentDescription("Abfluss und Pegel "+station.label+" · "+range.label);
        }

        @Override public boolean performClick(){super.performClick();return true;}

        @Override public boolean onTouchEvent(MotionEvent event){
            if(flow.times.size()<2||level.times.size()<2)return super.onTouchEvent(event);
            if(event.getAction()==MotionEvent.ACTION_DOWN||event.getAction()==MotionEvent.ACTION_MOVE||event.getAction()==MotionEvent.ACTION_UP){
                float left=dp(53),right=getWidth()-dp(53);
                float x=Math.max(left,Math.min(right,event.getX()));
                long minTime=Math.min(flow.times.get(0),level.times.get(0));
                long maxTime=Math.max(flow.times.get(flow.times.size()-1),level.times.get(level.times.size()-1));
                selectedTime=minTime+Math.round((maxTime-minTime)*(x-left)/Math.max(1f,right-left));
                invalidate();
                if(event.getAction()==MotionEvent.ACTION_UP)performClick();
                return true;
            }
            if(event.getAction()==MotionEvent.ACTION_CANCEL){selectedTime=Long.MIN_VALUE;invalidate();return true;}
            return super.onTouchEvent(event);
        }

        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            if(flow.values.size()<2||level.values.size()<2)return;
            float left=dp(53),right=getWidth()-dp(53),top=dp(18),bottom=getHeight()-dp(31);
            long minTime=Math.min(flow.times.get(0),level.times.get(0));
            long maxTime=Math.max(flow.times.get(flow.times.size()-1),level.times.get(level.times.size()-1));
            if(maxTime<=minTime)maxTime=minTime+1;
            HydroMath.AxisScale flowScale=HydroMath.niceAxis(flow.values);
            HydroMath.AxisScale levelScale=HydroMath.niceAxis(level.values);
            RhineNavigation.Stage currentStage=station==HydroStation.BASEL_RHEINHALLE?navigationStage():RhineNavigation.Stage.NORMAL;
            int flowColor=navigationFlowColor(currentStage);
            int levelColor=navigationLevelColor(currentStage);

            grid.setColor(darkMode?Color.rgb(57,72,82):Color.rgb(220,229,234));
            label.setColor(themeText(MUTED));
            for(int i=0;i<=4;i++){
                float y=top+(bottom-top)*i/4f;
                canvas.drawLine(left,y,right,y,grid);
                double q=flowScale.max-(flowScale.max-flowScale.min)*i/4d;
                double w=levelScale.max-(levelScale.max-levelScale.min)*i/4d;
                String qText=axisLabel(q,flowScale.step);
                String wText=axisLabel(w,levelScale.step);
                label.setColor(flowColor);
                canvas.drawText(qText,left-dp(5)-label.measureText(qText),y+dp(3),label);
                label.setColor(levelColor);
                canvas.drawText(wText,right+dp(5),y+dp(3),label);
            }
            drawDualTimeGrid(canvas,left,right,top,bottom,minTime,maxTime);
            drawNavigationThresholds(canvas,levelScale,left,right,top,bottom);
            drawDualSeries(canvas,flow,flowScale,left,right,top,bottom,minTime,maxTime,flowLine,false,flowColor);
            drawDualSeries(canvas,level,levelScale,left,right,top,bottom,minTime,maxTime,levelLine,true,levelColor);

            if(selectedTime!=Long.MIN_VALUE)drawDualSelection(canvas,flowScale,levelScale,left,right,top,bottom,minTime,maxTime,flowColor,levelColor);
        }

        private RhineNavigation.Stage navigationStageAtGraphTime(long timestamp){
            if(station!=HydroStation.BASEL_RHEINHALLE)return RhineNavigation.Stage.NORMAL;
            int index=HydroMath.nearestIndex(level.times,timestamp);
            if(index<0)return RhineNavigation.Stage.UNKNOWN;
            return navigationStageForGraphValue(station,level.values.get(index));
        }

        private void drawDualSeries(Canvas canvas,TrendSeries series,HydroMath.AxisScale scale,float left,float right,float top,float bottom,long minTime,long maxTime,Paint paint,boolean levelSeries,int fallbackColor){
            if(series.values.size()<2)return;
            if(station!=HydroStation.BASEL_RHEINHALLE){
                Path path=new Path();
                boolean started=false;
                for(int i=0;i<series.values.size();i++){
                    double value=series.values.get(i);if(!Double.isFinite(value))continue;
                    float x=left+(right-left)*(series.times.get(i)-minTime)/(float)(maxTime-minTime);
                    float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                    if(!started){path.moveTo(x,y);started=true;}else path.lineTo(x,y);
                }
                paint.setColor(fallbackColor);
                canvas.drawPath(path,paint);
                return;
            }
            boolean previous=false;
            float previousX=0f,previousY=0f;
            for(int i=0;i<series.values.size();i++){
                double value=series.values.get(i);if(!Double.isFinite(value)){previous=false;continue;}
                float x=left+(right-left)*(series.times.get(i)-minTime)/(float)(maxTime-minTime);
                float y=(float)(bottom-(value-scale.min)/(scale.max-scale.min)*(bottom-top));
                if(previous){
                    RhineNavigation.Stage segmentStage=navigationStageAtGraphTime(series.times.get(i));
                    paint.setColor(levelSeries?navigationLevelColor(segmentStage):navigationFlowColor(segmentStage));
                    canvas.drawLine(previousX,previousY,x,y,paint);
                }
                previous=true;previousX=x;previousY=y;
            }
        }

        private void drawDualTimeGrid(Canvas canvas,float left,float right,float top,float bottom,long minTime,long maxTime){
            int intervals=range==RiverRange.HOUR?3:4;
            ZoneId zone=ZoneId.of("Europe/Zurich");
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            label.setColor(themeText(MUTED));
            for(int i=0;i<=intervals;i++){
                float fraction=i/(float)intervals;float x=left+(right-left)*fraction;
                if(i>0&&i<intervals)canvas.drawLine(x,top,x,bottom,grid);
                long timestamp=minTime+Math.round((maxTime-minTime)*fraction);
                String value=java.time.Instant.ofEpochMilli(timestamp).atZone(zone).format(formatter);
                float width=label.measureText(value);float textX=Math.max(left,Math.min(right-width,x-width/2f));
                canvas.drawText(value,textX,bottom+dp(18),label);
            }
        }

        private void drawNavigationThresholds(Canvas canvas,HydroMath.AxisScale levelScale,float left,float right,float top,float bottom){
            if(station!=HydroStation.BASEL_RHEINHALLE)return;
            boolean centimetres=riverGraphLevelCentimetres(station);
            for(RhineNavigation.Stage mark:RhineNavigation.officialThresholdStages()){
                double value=RhineNavigation.thresholdGraphValue(mark,centimetres);
                if(!Double.isFinite(value)||value<levelScale.min||value>levelScale.max)continue;
                float y=(float)(bottom-(value-levelScale.min)/(levelScale.max-levelScale.min)*(bottom-top));
                int color=navigationLevelColor(mark);
                threshold.setColor(color);
                canvas.drawLine(left,y,right,y,threshold);
                String marker=RhineNavigation.thresholdMarker(mark,centimetres);
                label.setColor(color);
                float markerWidth=label.measureText(marker);
                canvas.drawText(marker,Math.max(left+dp(4),right-markerWidth-dp(4)),Math.max(top+dp(10),y-dp(3)),label);
            }
        }

        private void drawDualSelection(Canvas canvas,HydroMath.AxisScale flowScale,HydroMath.AxisScale levelScale,float left,float right,float top,float bottom,long minTime,long maxTime,int flowColor,int levelColor){
            int qi=HydroMath.nearestIndex(flow.times,selectedTime),wi=HydroMath.nearestIndex(level.times,selectedTime);
            if(qi<0||wi<0)return;
            float x=left+(right-left)*(selectedTime-minTime)/(float)(maxTime-minTime);
            Paint cross=new Paint(Paint.ANTI_ALIAS_FLAG);cross.setColor(Color.argb(darkMode?150:100,128,145,155));cross.setStrokeWidth(dp(1));canvas.drawLine(x,top,x,bottom,cross);

            double q=flow.values.get(qi),w=level.values.get(wi);
            RhineNavigation.Stage selectedStage=station==HydroStation.BASEL_RHEINHALLE?navigationStageForGraphValue(station,w):RhineNavigation.Stage.NORMAL;
            if(station==HydroStation.BASEL_RHEINHALLE){
                flowColor=navigationFlowColor(selectedStage);
                levelColor=navigationLevelColor(selectedStage);
            }
            float qx=left+(right-left)*(flow.times.get(qi)-minTime)/(float)(maxTime-minTime);
            float qy=(float)(bottom-(q-flowScale.min)/(flowScale.max-flowScale.min)*(bottom-top));
            float wx=left+(right-left)*(level.times.get(wi)-minTime)/(float)(maxTime-minTime);
            float wy=(float)(bottom-(w-levelScale.min)/(levelScale.max-levelScale.min)*(bottom-top));
            point.setColor(flowColor);canvas.drawCircle(qx,qy,dp(4.5f),point);
            point.setColor(levelColor);canvas.drawCircle(wx,wy,dp(4.5f),point);

            long timestamp=Math.max(flow.times.get(qi),level.times.get(wi));
            ZonedDateTime time=java.time.Instant.ofEpochMilli(timestamp).atZone(ZoneId.of("Europe/Zurich"));
            DateTimeFormatter formatter=range==RiverRange.WEEK?DateTimeFormatter.ofPattern("EE dd.MM. HH:mm",Locale.GERMAN):DateTimeFormatter.ofPattern("HH:mm",Locale.GERMAN);
            String text=time.format(formatter)+" · "+formatMetric(station,RiverMetric.FLOW,q)+" m³/s · "+formatGraphLevel(station,w)+" "+graphLevelUnit(station)+(station==HydroStation.BASEL_RHEINHALLE?" · "+RhineNavigation.shortLabel(selectedStage):"");
            tooltipText.setColor(darkMode?DARK_TEXT:Color.WHITE);
            float textWidth=tooltipText.measureText(text),textHeight=Math.abs(tooltipText.ascent())+Math.abs(tooltipText.descent());
            float boxWidth=Math.min(right-left,textWidth+dp(16));float boxLeft=Math.max(left,Math.min(right-boxWidth,x-boxWidth/2f));
            RectF box=new RectF(boxLeft,top+dp(4),boxLeft+boxWidth,top+textHeight+dp(14));
            tooltip.setColor(darkMode?DARK_SOFT:NAVY);canvas.drawRoundRect(box,dp(8),dp(8),tooltip);
            canvas.save();canvas.clipRect(box);canvas.drawText(text,box.left+dp(8),box.bottom-dp(6),tooltipText);canvas.restore();
            setContentDescription(text);
        }
    }

    private static class AppChoice {final String label,pkg;final boolean directShare,documented;final BankingAppRegistry.Capability capability;AppChoice(String l,String p,boolean d,BankingAppRegistry.Capability c,boolean doc){label=l;pkg=p;directShare=d;capability=c;documented=doc;}}
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
