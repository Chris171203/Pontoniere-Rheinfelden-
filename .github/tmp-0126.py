from pathlib import Path


def replace_once(text, old, new, label):
    count=text.count(old)
    if count!=1:
        raise SystemExit(f"{label}: expected 1 occurrence, got {count}")
    return text.replace(old,new,1)

# Main UI cleanup + home refresh affordance
path=Path("Android/app/src/main/java/ch/pfvr/internapp/MainActivity.java")
s=path.read_text()

s=replace_once(s,
'''    TextView heading = txt("Gemeinsam auf dem Rhein.",29,Color.WHITE,true);
    heading.setPadding(0,dp(8),0,dp(5));
    hero.addView(heading);
    hero.addView(txt("Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit.",15,Color.rgb(232,243,247),false));
    LinearLayout actions = new LinearLayout(this);
    actions.setPadding(0,dp(17),0,0);
''',
'''    TextView heading = txt("Gemeinsam auf dem Rhein.",29,Color.WHITE,true);
    heading.setPadding(0,dp(8),0,0);
    hero.addView(heading);
    LinearLayout actions = new LinearLayout(this);
    actions.setPadding(0,dp(14),0,0);
''','home hero copy')

s=replace_once(s,
'    LinearLayout group=tileGroup("Wetter zum nächsten Termin","Prognose für den nächsten relevanten Vereinsanlass");',
'    LinearLayout group=tileGroup("Wetter zum nächsten Termin",null);','weather subtitle')
s=replace_once(s,
'    LinearLayout group=tileGroup("3-Tage-Wetter","Heute und die nächsten zwei Tage · Rheinfelden");',
'    LinearLayout group=tileGroup("3-Tage-Wetter",null);','3-day subtitle')

old='''private View homeRiverSummaryTile(){
    LinearLayout group=tileGroup("Rhein aktuell","Abfluss, Pegel, Temperatur und Messdatenstand");
    group.addView(riverSummaryRow(),new LinearLayout.LayoutParams(-1,-2));
    TextView safety=txt("BAFU-Aktuellwerte sind ungeprüfte Rohdaten und können Fehler enthalten. Die angezeigte Schifffahrtslage dient der Orientierung und ist keine amtliche Freigabe. Massgebend sind die Schweizerischen Rheinhäfen.  →",10,MUTED,false);
    safety.setPadding(dp(2),dp(7),dp(2),dp(3));
    safety.setOnClickListener(v->external(RIVER_NAVIGATION_SOURCE));
    group.addView(safety);
    return group;
}
'''
new='''private View homeRiverSummaryTile(){
    LinearLayout group=tileGroup("Rhein aktuell",null);
    addHomeLiveRefreshAction(group);
    group.addView(riverSummaryRow(),new LinearLayout.LayoutParams(-1,-2));
    TextView safety=txt("BAFU-Rohdaten, ungeprüft. Schifffahrtslage nur zur Orientierung – verbindlich sind die Schweizerischen Rheinhäfen.  →",10,MUTED,false);
    safety.setPadding(dp(2),dp(7),dp(2),dp(3));
    safety.setOnClickListener(v->external(RIVER_NAVIGATION_SOURCE));
    group.addView(safety);
    return group;
}

private void addHomeLiveRefreshAction(LinearLayout group){
    if(group==null||group.getChildCount()==0)return;
    View heading=group.getChildAt(0);
    group.removeViewAt(0);
    LinearLayout row=new LinearLayout(this);
    row.setGravity(Gravity.CENTER_VERTICAL);
    row.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
    TextView refresh=txtRaw("↻",22,WATER,true);
    refresh.setGravity(Gravity.CENTER);
    refresh.setAlpha(0.82f);
    refresh.setContentDescription(ui("Aktualisieren"));
    refresh.setOnClickListener(v->{
        v.animate().cancel();
        v.setRotation(0f);
        v.animate().rotation(360f).setDuration(420L).withEndAction(()->v.setRotation(0f)).start();
        refreshLive(true);
    });
    LinearLayout.LayoutParams refreshParams=new LinearLayout.LayoutParams(dp(44),dp(44));
    refreshParams.setMargins(dp(6),0,0,dp(3));
    row.addView(refresh,refreshParams);
    group.addView(row,0);
}
'''
s=replace_once(s,old,new,'river live refresh and safety copy')

s=replace_once(s,
'    LinearLayout group=tileGroup("Als Nächstes","Aus dem öffentlichen Vereinskalender");',
'    LinearLayout group=tileGroup("Als Nächstes",null);','event subtitle')
s=replace_once(s,
'    LinearLayout group=tileGroup("Aktuell vom Verein",ui("News von pfvr.ch")+" · "+newsStatus());',
'    LinearLayout group=tileGroup("Aktuell vom Verein",newsStatus());','news subtitle')

s=replace_once(s,'        c.addView(txt(x[0],11,WATER,true));\n','', 'compact weather redundant label')
s=replace_once(s,'        c.addView(txt("NÄCHSTER TERMIN",11,WATER,true));\n','', 'multi weather redundant label')

old='''    private String weatherAge(String source,long updated){
        if(updated<=0)return source;
        long min=Math.max(0,(System.currentTimeMillis()-updated)/60000);
        return source+(min>90?" · Cache "+(min/60)+" h":" · "+ui("vor")+" "+min+" min");
    }
'''
new='''    private String compactWeatherSource(String source){
        if(source!=null&&source.contains("Open-Meteo"))return "MeteoSwiss/Open-Meteo";
        return source==null||source.isBlank()?"MeteoSwiss/Open-Meteo":source;
    }

    private String weatherAge(String source,long updated){
        String label=compactWeatherSource(source);
        if(updated<=0)return label;
        long min=Math.max(0,(System.currentTimeMillis()-updated)/60000);
        return label+(min>90?" · Cache "+(min/60)+" h":" · "+ui("vor")+" "+min+" min");
    }
'''
s=replace_once(s,old,new,'compact weather provenance')

s=replace_once(s,
'''    TextView title=txt("Konsumation bezahlen",27,Color.WHITE,true);
    title.setPadding(0,dp(5),0,dp(5));
    hero.addView(title);
    hero.addView(txt("Artikel für dich, Kinder oder die ganze Runde zusammenstellen – oder weiterhin einen freien Betrag verwenden.",14,Color.rgb(232,243,247),false));
''',
'''    TextView title=txt("Konsumation bezahlen",27,Color.WHITE,true);
    title.setPadding(0,dp(5),0,0);
    hero.addView(title);
''','cash hero copy')
s=replace_once(s,
'    LinearLayout group=tileGroup("Warenkorb","Ausgewählte Artikel und Zahlungswege");',
'    LinearLayout group=tileGroup("Warenkorb",null);','cart subtitle')

path.write_text(s)

# Swiss German mapping for the shortened safety-critical river note.
path=Path("Android/app/src/main/java/ch/pfvr/internapp/UiLanguage.java")
s=path.read_text()
anchor='''        put("BAFU Live-Daten", "BAFU-Livedate");
'''
insert='''        put("BAFU Live-Daten", "BAFU-Livedate");
        put("BAFU-Rohdaten, ungeprüft. Schifffahrtslage nur zur Orientierung – verbindlich sind die Schweizerischen Rheinhäfen.  →", "BAFU-Rohdate, ungeprüeft. D Schifffahrtslag isch nume zur Orientierig – verbindlich sind d Schwiizerische Rhyhäfe.  →");
'''
s=replace_once(s,anchor,insert,'river note translation')
path.write_text(s)

# Update existing general-event weather source contract for the cleaner header.
path=Path("Android/app/src/test/java/ch/pfvr/internapp/WeatherEventSourceTest.java")
s=path.read_text()
s=replace_once(s,
'assertTrue(activity.contains("tileGroup(\\\"Wetter zum nächsten Termin\\\",\\\"Prognose für den nächsten relevanten Vereinsanlass\\\")"));',
'assertTrue(activity.contains("tileGroup(\\\"Wetter zum nächsten Termin\\\",null)"));','weather source assertion')
path.write_text(s)

# Regression coverage for the text cleanup and manual home refresh.
Path("Android/app/src/test/java/ch/pfvr/internapp/HomePolishSourceTest.java").write_text('''package ch.pfvr.internapp;\n\nimport static org.junit.Assert.assertFalse;\nimport static org.junit.Assert.assertTrue;\n\nimport java.nio.charset.StandardCharsets;\nimport java.nio.file.Files;\nimport java.nio.file.Path;\nimport java.nio.file.Paths;\n\nimport org.junit.Test;\n\npublic class HomePolishSourceTest {\n    private static String source() throws Exception {\n        String relative="src/main/java/ch/pfvr/internapp/MainActivity.java";\n        Path[] candidates={Paths.get(relative),Paths.get("app",relative),Paths.get("Android","app",relative)};\n        for(Path candidate:candidates)if(Files.isRegularFile(candidate))return new String(Files.readAllBytes(candidate),StandardCharsets.UTF_8);\n        throw new IllegalStateException("MainActivity.java not found");\n    }\n\n    @Test public void homeRemovesPrototypeStyleExplanatoryCopy() throws Exception {\n        String activity=source();\n        assertFalse(activity.contains("hero.addView(txt(\\\"Training, Wettfahren und Vereinsleben – alles Wichtige direkt griffbereit."));\n        assertTrue(activity.contains("tileGroup(\\\"Wetter zum nächsten Termin\\\",null)"));\n        assertTrue(activity.contains("tileGroup(\\\"3-Tage-Wetter\\\",null)"));\n        assertTrue(activity.contains("tileGroup(\\\"Rhein aktuell\\\",null)"));\n        assertTrue(activity.contains("tileGroup(\\\"Als Nächstes\\\",null)"));\n        assertTrue(activity.contains("tileGroup(\\\"Aktuell vom Verein\\\",newsStatus())"));\n        assertFalse(activity.contains("c.addView(txt(x[0],11,WATER,true))"));\n        assertFalse(activity.contains("c.addView(txt(\\\"NÄCHSTER TERMIN\\\",11,WATER,true))"));\n        assertTrue(activity.contains("BAFU-Rohdaten, ungeprüft. Schifffahrtslage nur zur Orientierung"));\n    }\n\n    @Test public void homeLiveRefreshIsSmallAnimatedAndForcesLiveReload() throws Exception {\n        String activity=source();\n        assertTrue(activity.contains("addHomeLiveRefreshAction(group)"));\n        assertTrue(activity.contains("txtRaw(\\\"↻\\\",22,WATER,true)"));\n        assertTrue(activity.contains("setContentDescription(ui(\\\"Aktualisieren\\\"))"));\n        assertTrue(activity.contains("rotation(360f).setDuration(420L)"));\n        assertTrue(activity.contains("refreshLive(true)"));\n        assertTrue(activity.contains("new LinearLayout.LayoutParams(dp(44),dp(44))"));\n    }\n\n    @Test public void weatherProvenanceIsCompactButCacheAgeRemainsVisible() throws Exception {\n        String activity=source();\n        assertTrue(activity.contains("return \\\"MeteoSwiss/Open-Meteo\\\""));\n        assertTrue(activity.contains("String label=compactWeatherSource(source)"));\n        assertTrue(activity.contains("min>90?\\\" · Cache \\\"+(min/60)+\\\" h\\\""));\n    }\n\n    @Test public void cashRemovesRedundantHeroAndCartCopy() throws Exception {\n        String activity=source();\n        assertFalse(activity.contains("hero.addView(txt(\\\"Artikel für dich, Kinder oder die ganze Runde zusammenstellen"));\n        assertTrue(activity.contains("tileGroup(\\\"Warenkorb\\\",null)"));\n    }\n}\n''')

# Version bump
path=Path("Android/app/build.gradle")
s=path.read_text()
s=replace_once(s,"versionCode 62","versionCode 63",'version code')
s=replace_once(s,"versionName '0.12.5'","versionName '0.12.6'",'version name')
path.write_text(s)

# Project/working memory
path=Path("PROJECT.md")
s=path.read_text()
anchor='''- Home mit Wetter zum nächsten relevanten Vereinsanlass.\n'''
replacement='''- Home mit Wetter zum nächsten relevanten Vereinsanlass. Ein dezenter Refresh-Tap direkt bei `Rhein aktuell` erzwingt bei Bedarf einen neuen Abruf der Live-Wetter- und Rheindaten, ohne den Umweg über Einstellungen.\n'''
s=replace_once(s,anchor,replacement,'project home refresh')
path.write_text(s)

path=Path("STATUS.md")
s=path.read_text()
s=replace_once(s,"Stand: Testversion `0.12.5` · aktualisiert 2026-09-10.","Stand: Testversion `0.12.6` · aktualisiert 2026-09-10.",'status header')
anchor="## Aktueller Teststand\n\n"
entry="- `0.12.6` reduziert erklärenden Prototyp-Text auf Home und im Warenkorb: Hero-Zusatzsatz sowie redundante Untertitel bei Wetter, 3-Tage-Wetter, Rhein, Terminen und Warenkorb entfallen; News zeigt nur noch den Stand, Wetterquellen werden kompakt als `MeteoSwiss/Open-Meteo` ausgewiesen und der sicherheitsrelevante BAFU-Hinweis bleibt in gekürzter Form erhalten. Rechts bei `Rhein aktuell` sitzt nun ein dezentes `↻` mit 44-dp-Tapfläche; ein Tap dreht das Symbol kurz um 360° und erzwingt über `refreshLive(true)` einen neuen Wetter- und Rheinabruf.\n"
if anchor not in s: raise SystemExit('STATUS anchor missing')
s=s.replace(anchor,anchor+entry,1)
path.write_text(s)
