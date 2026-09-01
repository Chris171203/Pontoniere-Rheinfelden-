from pathlib import Path

p = Path('Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java')
text = p.read_text(encoding='utf-8')

if 'scheduleBackgroundRefresh()' in text and 'BackgroundRefreshWorker.class' in text and 'Hintergrundaktualisierung' in text:
    print('background refresh patch already applied')
    raise SystemExit(0)

text = text.replace('import android.os.Bundle;\n', 'import android.os.Bundle;\nimport android.os.Handler;\nimport android.os.Looper;\n', 1)
text = text.replace('import android.widget.Toast;\n\n', 'import android.widget.Toast;\n\nimport androidx.work.Constraints;\nimport androidx.work.ExistingPeriodicWorkPolicy;\nimport androidx.work.NetworkType;\nimport androidx.work.PeriodicWorkRequest;\nimport androidx.work.WorkManager;\n\n', 1)
text = text.replace('import java.util.Set;\n', 'import java.util.Set;\nimport java.util.concurrent.TimeUnit;\n', 1)

text = text.replace(
    '    private static final String PREF_INTERNAL_APP_VIEW = "internal_app_view";\n',
    '    private static final String PREF_INTERNAL_APP_VIEW = "internal_app_view";\n'
    '    private static final String PREF_BACKGROUND_REFRESH = "background_refresh";\n'
    '    private static final String BACKGROUND_WORK_NAME = "pfvr-public-data-refresh";\n',
    1,
)

text = text.replace(
    '    private boolean darkMode = false;\n',
    '    private boolean darkMode = false;\n'
    '    private Handler dataRefreshHandler;\n'
    '    private final Runnable dataRefreshTick = new Runnable(){@Override public void run(){refreshLive(false);if(eventsUpdated<=0L||System.currentTimeMillis()-eventsUpdated>=60L*60L*1000L)refreshEvents(false,null);if(dataRefreshHandler!=null)dataRefreshHandler.postDelayed(this,5L*60L*1000L);}};\n',
    1,
)

old = '''        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkMode = resolveDarkMode();'''
new = '''        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        dataRefreshHandler = new Handler(Looper.getMainLooper());
        scheduleBackgroundRefresh();
        darkMode = resolveDarkMode();'''
if old not in text:
    raise SystemExit('onCreate prefs block not found')
text = text.replace(old, new, 1)

marker = '    private View buildShell() {'
lifecycle = '''    @Override protected void onResume(){
        super.onResume();
        if(prefs!=null){loadCachedEvents();refreshLive(false);if(current==Screen.HOME||current==Screen.EVENTS)navigate(current);}
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

'''
if marker not in text:
    raise SystemExit('buildShell marker not found')
text = text.replace(marker, lifecycle + marker, 1)

old = '    private void refreshEvents(boolean toast,Runnable done){if(eventsLoading){if(toast)Toast.makeText(this,"Kalender-Aktualisierung läuft bereits.",Toast.LENGTH_SHORT).show();return;}eventsLoading=true;'
new = '    private void refreshEvents(boolean toast,Runnable done){if(!toast&&eventsUpdated>0L&&System.currentTimeMillis()-eventsUpdated<60L*60L*1000L){if(done!=null)done.run();return;}if(eventsLoading){if(toast)Toast.makeText(this,"Kalender-Aktualisierung läuft bereits.",Toast.LENGTH_SHORT).show();return;}eventsLoading=true;'
if old not in text:
    raise SystemExit('refreshEvents signature block not found')
text = text.replace(old, new, 1)

old = '''        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY); clear.setOnClickListener(v->clearDataCache()); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(44)); clp.setMargins(0,dp(8),0,0); data.addView(clear,clp);

        section(b,"App",null);'''
new = '''        Button clear=btn("Daten-Cache leeren",Color.rgb(232,240,244),NAVY); clear.setOnClickListener(v->clearDataCache()); LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-1,dp(44)); clp.setMargins(0,dp(8),0,0); data.addView(clear,clp);
        boolean bgOn=prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);
        Button bgRefresh=btn("Hintergrundaktualisierung: "+(bgOn?"Ein":"Aus"),Color.rgb(232,240,244),NAVY);
        bgRefresh.setOnClickListener(v->{boolean next=!prefs.getBoolean(PREF_BACKGROUND_REFRESH,true);prefs.edit().putBoolean(PREF_BACKGROUND_REFRESH,next).apply();scheduleBackgroundRefresh();bgRefresh.setText("Hintergrundaktualisierung: "+(next?"Ein":"Aus"));});
        LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,dp(44));blp.setMargins(0,dp(8),0,0);data.addView(bgRefresh,blp);
        TextView autoInfo=txt("Live-Daten werden bei geöffneter App regelmäßig geprüft. Im Hintergrund aktualisiert Android bei verfügbarer Verbindung best effort; Energiesparmodi können die Ausführung verzögern.",11,MUTED,false);autoInfo.setPadding(0,dp(8),0,0);data.addView(autoInfo);

        section(b,"App",null);'''
if old not in text:
    raise SystemExit('settings cache block not found')
text = text.replace(old, new, 1)

text = text.replace('Testversion 0.6.0 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.', 'Testversion 0.6.1 · 1.0.0 bleibt für den ersten offiziellen Release reserviert.', 1)

old = '    private void clearDataCache(){prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED).remove(PREF_HYDRO_CACHE).remove(PREF_HYDRO_UPDATED).apply();events=new ArrayList<>();eventsUpdated=0L;Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();refreshEvents(false,()->{});refreshLive(true);}'
new = '    private void clearDataCache(){prefs.edit().remove(PREF_ICS_CACHE).remove(PREF_ICS_UPDATED).remove(PREF_WEATHER_CACHE).remove(PREF_WEATHER_UPDATED).remove(PREF_HYDRO_CACHE).remove(PREF_HYDRO_UPDATED).remove(PREF_HYDRO_HISTORY_CACHE).remove(PREF_HYDRO_HISTORY_UPDATED).apply();events=new ArrayList<>();eventsUpdated=0L;Toast.makeText(this,"Daten-Cache geleert. Neue Daten werden nachgeladen.",Toast.LENGTH_SHORT).show();refreshEvents(false,()->{});refreshLive(true);}'
if old not in text:
    raise SystemExit('clearDataCache line not found')
text = text.replace(old, new, 1)

old = '    @Override protected void onDestroy(){if(activeWebView!=null)activeWebView.destroy();super.onDestroy();}'
new = '    @Override protected void onDestroy(){if(dataRefreshHandler!=null)dataRefreshHandler.removeCallbacks(dataRefreshTick);if(activeWebView!=null)activeWebView.destroy();super.onDestroy();}'
if old not in text:
    raise SystemExit('onDestroy line not found')
text = text.replace(old, new, 1)

p.write_text(text, encoding='utf-8')
print('Applied automatic foreground/background refresh')
