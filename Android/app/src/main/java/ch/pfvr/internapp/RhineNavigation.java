package ch.pfvr.internapp;

/** Official Rhine navigation stages derived from Basel-Rheinhalle gauge levels. */
final class RhineNavigation {
    enum Stage { UNKNOWN, NORMAL, HWM_I, HWM_IIB, HWM_IIA }

    static final double HWM_I_CM = 700.0d;
    static final double HWM_IIB_CM = 790.0d;
    static final double HWM_IIA_CM = 820.0d;
    static final long MAX_CURRENT_AGE_MS = 60L * 60L * 1000L;

    private RhineNavigation() {}

    static Stage fromBaselGaugeCm(double gaugeCm){
        if(!Double.isFinite(gaugeCm))return Stage.UNKNOWN;
        if(gaugeCm>=HWM_IIA_CM)return Stage.HWM_IIA;
        if(gaugeCm>=HWM_IIB_CM)return Stage.HWM_IIB;
        if(gaugeCm>=HWM_I_CM)return Stage.HWM_I;
        return Stage.NORMAL;
    }

    static boolean isCurrent(long measurementTimestamp,long cacheUpdatedTimestamp,long now){
        if(measurementTimestamp<=0L||cacheUpdatedTimestamp<=0L||now<=0L)return false;
        long measurementAge=now-measurementTimestamp;
        long cacheAge=now-cacheUpdatedTimestamp;
        if(measurementAge<0L||cacheAge<0L)return false;
        return measurementAge<=MAX_CURRENT_AGE_MS&&cacheAge<=MAX_CURRENT_AGE_MS;
    }

    static Stage fromCurrentBaselGaugeCm(double gaugeCm,long measurementTimestamp,long cacheUpdatedTimestamp,long now){
        if(!isCurrent(measurementTimestamp,cacheUpdatedTimestamp,now))return Stage.UNKNOWN;
        return fromBaselGaugeCm(gaugeCm);
    }

    static String shortLabel(Stage stage){
        if(stage==Stage.UNKNOWN)return "Lage unklar";
        if(stage==Stage.HWM_I)return "HWM I";
        if(stage==Stage.HWM_IIB)return "Sperre IIb";
        if(stage==Stage.HWM_IIA)return "Sperre IIa";
        return "Normal";
    }

    static String detail(Stage stage){
        if(stage==Stage.UNKNOWN)return "Basel-Pegel fehlt oder ist älter als 60 Minuten. Massgebend sind die Schweizerischen Rheinhäfen.";
        if(stage==Stage.HWM_I)return "Voralarm ab 700 cm Pegel Basel-Rheinhalle.";
        if(stage==Stage.HWM_IIB)return "Kleinschifffahrt und Fähren Basel–Rheinfelden gesperrt.";
        if(stage==Stage.HWM_IIA)return "Schifffahrt Rheinfelden–Kembs gesperrt.";
        return "Unter Hochwassermarke I (< 700 cm).";
    }

    static Stage[] officialThresholdStages(){
        return new Stage[]{Stage.HWM_I,Stage.HWM_IIB,Stage.HWM_IIA};
    }

    static double thresholdGaugeCm(Stage stage){
        if(stage==Stage.HWM_I)return HWM_I_CM;
        if(stage==Stage.HWM_IIB)return HWM_IIB_CM;
        if(stage==Stage.HWM_IIA)return HWM_IIA_CM;
        return Double.NaN;
    }

    static double thresholdGraphValue(Stage stage,boolean centimetres){
        double cm=thresholdGaugeCm(stage);
        if(!Double.isFinite(cm))return Double.NaN;
        return centimetres?cm:240.0d+cm/100.0d;
    }

    static String thresholdMarker(Stage stage,boolean centimetres){
        double value=thresholdGraphValue(stage,centimetres);
        String prefix=stage==Stage.HWM_I?"I":(stage==Stage.HWM_IIB?"IIb":"IIa");
        return centimetres
                ?String.format(java.util.Locale.GERMAN,"%s %.0f",prefix,value)
                :String.format(java.util.Locale.GERMAN,"%s %.2f",prefix,value);
    }
}
