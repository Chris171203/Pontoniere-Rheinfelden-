package ch.pfvr.internapp;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class BankingAppRegistry {
    enum Capability {
        DIRECT_SHARE("Direkte QR-Übergabe"),
        FILE_IMPORT("QR-Datei importieren"),
        SCAN_ONLY("Scanner / manuell"),
        UNKNOWN("Kompatibilität prüfen");

        final String label;
        Capability(String label){this.label=label;}
    }

    static final class Profile {
        final String label;
        final Capability capability;
        final boolean documented;

        Profile(String label, Capability capability, boolean documented){
            this.label=label;
            this.capability=capability;
            this.documented=documented;
        }
    }

    private static final Map<String,Profile> KNOWN = new LinkedHashMap<>();

    static {
        // Dokumentiert bzw. im PFVR-Test bestätigt: digitale QR-Rechnung kann direkt an die App übergeben werden.
        add("com.yuh", "Yuh", Capability.DIRECT_SHARE, true);
        add("ch.postfinance.android", "PostFinance", Capability.DIRECT_SHARE, true);
        add("ch.raiffeisen.android", "Raiffeisen E-Banking", Capability.DIRECT_SHARE, true);
        add("ch.raiffeisen.kep", "Raiffeisen", Capability.DIRECT_SHARE, true);
        add("ch.zkb.slv.mobile.client.android", "ZKB Mobile Banking", Capability.DIRECT_SHARE, true);
        add("ch.bekb.BEKBApp", "BEKB", Capability.DIRECT_SHARE, true);
        add("ch.blkb.mobile.android", "BLKB", Capability.DIRECT_SHARE, true);
        add("ch.akb.mobile.android", "AKB", Capability.DIRECT_SHARE, true);
        add("ch.sgkb.androidapp", "SGKB", Capability.DIRECT_SHARE, true);
        add("ch.lukb.app", "LUKB", Capability.DIRECT_SHARE, true);
        add("ch.cler.digital.banking.android", "Bank Cler", Capability.DIRECT_SHARE, true);
        add("ch.bankcler.zak", "Zak", Capability.DIRECT_SHARE, true);

        // Die App kann QR-Rechnungen als Datei/Bild importieren; direkter Android-Share ist nicht zugesichert.
        add("com.neonbanking.app", "neon", Capability.FILE_IMPORT, true);

        // Swiss QR wird unterstützt, aber öffentlich dokumentiert ist primär der Kamera-Scanner.
        add("com.ubs.swidKXJ.android", "UBS", Capability.SCAN_ONLY, true);
        add("com.revolut.revolut", "Revolut", Capability.SCAN_ONLY, true);
        add("de.fiduciagad.banking.vr", "VR Banking", Capability.SCAN_ONLY, false);

        // Gängige Schweizer Apps: erkennen und Runtime-Share prüfen; ohne positiven Nachweis konservativ UNKNOWN.
        add("ch.migrosbank.android", "Migros Bank", Capability.UNKNOWN, false);
        add("ch.bcv.mobile.android", "BCV", Capability.UNKNOWN, false);
        add("ch.bkb.digital.banking.android", "BKB", Capability.UNKNOWN, false);
        add("ch.tkb.androidapp", "TKB", Capability.UNKNOWN, false);
        add("com.gkb.mobilebanking.production.release", "GKB", Capability.UNKNOWN, false);
        add("com.zgkb.map4.android", "ZugerKB", Capability.UNKNOWN, false);
        add("com.zgkb.android.mbanking", "ZugerKB Mobile Banking", Capability.UNKNOWN, false);
        add("com.valiant.mobilebanking.release", "Valiant", Capability.UNKNOWN, false);
        add("com.swissquote.android", "Swissquote", Capability.UNKNOWN, false);
        add("com.alpian.alpian", "Alpian", Capability.UNKNOWN, false);
        add("com.radicant.bank", "radicant", Capability.UNKNOWN, false);
    }

    private BankingAppRegistry(){}

    private static void add(String pkg,String label,Capability capability,boolean documented){
        KNOWN.put(pkg,new Profile(label,capability,documented));
    }

    static Map<String,Profile> knownApps(){
        return KNOWN;
    }

    static Profile profile(String pkg,String label,boolean runtimeImageShare){
        Profile known=KNOWN.get(pkg);
        String display=(label==null||label.isBlank())?(known==null?pkg:known.label):label;
        if(runtimeImageShare){
            // Ein tatsächlich registrierter Android-Bild-Share ist für den Geräte-Workflow aussagekräftiger
            // als unsere statische Matrix. Ob die Bank den Swiss-QR fachlich übernimmt, bleibt App-Sache.
            return new Profile(display,Capability.DIRECT_SHARE,known!=null&&known.documented);
        }
        if(known!=null)return new Profile(display,known.capability,known.documented);
        return new Profile(display,Capability.UNKNOWN,false);
    }

    static boolean looksLikeBankingApp(String value){
        if(value==null)return false;
        String v=value.toLowerCase(Locale.ROOT);
        if(v.contains("twint"))return false;
        return v.matches(".*(ubs|postfinance|raiffeisen|zkb|kantonal|bank|banque|banca|neon|yuh|revolut|swissquote|cler|zak|migros|credit suisse|csx|bcv|bcf|bcge|bcj|bcju|bcn|bcne|bcvs|bekb|bkb|blkb|akb|sgkb|gkb|glkb|lukb|nkb|owkb|shkb|szkb|tgkb|tkb|urkb|zuger|alpian|radicant|willbe|cembra|valiant|hypothekar|acrevis|clientis|obwaldner|nidwaldner|schaffhauser|thurgauer|graub.ndner|volksbank|vr banking).*");
    }

    static int priority(String label,String pkg){
        String h=((label==null?"":label)+" "+(pkg==null?"":pkg)).toLowerCase(Locale.ROOT);
        if(h.contains("ubs"))return 10;
        if(h.contains("postfinance"))return 20;
        if(h.contains("raiffeisen"))return 30;
        if(h.contains("zkb")||h.contains("zürcher kantonal")||h.contains("zuercher kantonal"))return 40;
        if(h.contains("kantonal")||h.contains("bcv")||h.contains("bekb")||h.contains("bkb")||h.contains("blkb")||h.contains("akb")||h.contains("sgkb")||h.contains("gkb")||h.contains("lukb")||h.contains("tkb"))return 50;
        if(h.contains("migros"))return 60;
        if(h.contains("cler")||h.contains("zak"))return 70;
        if(h.contains("neon"))return 100;
        if(h.contains("yuh"))return 110;
        if(h.contains("revolut"))return 120;
        if(h.contains("swissquote"))return 130;
        if(h.contains("volksbank")||h.contains("vr banking"))return 150;
        return 200;
    }
}
