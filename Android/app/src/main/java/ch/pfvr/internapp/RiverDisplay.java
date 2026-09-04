package ch.pfvr.internapp;

/** Station-specific presentation rules for hydrological values. */
final class RiverDisplay {
    // Verified for Basel-Rheinhalle: 247.20 m ü.M. corresponds to 720 cm.
    private static final double BASEL_RHEINHALLE_GAUGE_ZERO_M = 240.0d;

    private RiverDisplay() {}

    static double levelValue(HydroStation station,double rawMetresAboveSea){
        return rawMetresAboveSea;
    }

    static String levelUnit(HydroStation station){
        return "m ü.M.";
    }

    static int levelDecimals(HydroStation station){
        return 2;
    }

    static boolean hasVerifiedGaugeCentimetres(HydroStation station){
        return station==HydroStation.BASEL_RHEINHALLE;
    }

    static double graphLevelValue(HydroStation station,double rawMetresAboveSea,boolean centimetres){
        return centimetres&&hasVerifiedGaugeCentimetres(station)
                ?gaugeCentimetres(station,rawMetresAboveSea)
                :levelValue(station,rawMetresAboveSea);
    }

    static String graphLevelUnit(HydroStation station,boolean centimetres){
        return centimetres&&hasVerifiedGaugeCentimetres(station)?"cm":"m ü.M.";
    }

    static int graphLevelDecimals(HydroStation station,boolean centimetres){
        return centimetres&&hasVerifiedGaugeCentimetres(station)?0:2;
    }

    static double gaugeCentimetres(HydroStation station,double rawMetresAboveSea){
        if(!Double.isFinite(rawMetresAboveSea))return rawMetresAboveSea;
        if(!hasVerifiedGaugeCentimetres(station))return Double.NaN;
        return (rawMetresAboveSea-BASEL_RHEINHALLE_GAUGE_ZERO_M)*100.0d;
    }
}
