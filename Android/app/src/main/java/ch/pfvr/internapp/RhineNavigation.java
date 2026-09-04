package ch.pfvr.internapp;

/** Official Rhine navigation stages derived from Basel-Rheinhalle gauge levels. */
final class RhineNavigation {
    enum Stage { UNKNOWN, NORMAL, HWM_I, HWM_IIB, HWM_IIA }

    static final double HWM_I_CM = 700.0d;
    static final double HWM_IIB_CM = 790.0d;
    static final double HWM_IIA_CM = 820.0d;

    private RhineNavigation() {}

    static Stage fromBaselGaugeCm(double gaugeCm){
        if(!Double.isFinite(gaugeCm))return Stage.UNKNOWN;
        if(gaugeCm>=HWM_IIA_CM)return Stage.HWM_IIA;
        if(gaugeCm>=HWM_IIB_CM)return Stage.HWM_IIB;
        if(gaugeCm>=HWM_I_CM)return Stage.HWM_I;
        return Stage.NORMAL;
    }

    static String shortLabel(Stage stage){
        if(stage==Stage.UNKNOWN)return "Keine Lage";
        if(stage==Stage.HWM_I)return "HWM I";
        if(stage==Stage.HWM_IIB)return "Sperre IIb";
        if(stage==Stage.HWM_IIA)return "Sperre IIa";
        return "Normal";
    }

    static String detail(Stage stage){
        if(stage==Stage.UNKNOWN)return "Basel-Pegel derzeit nicht verfügbar.";
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
